import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { InjectQueue } from '@nestjs/bull';
import {
  BookingStatus,
  CancelledBy,
  PaymentMethod,
  PaymentStatus,
  RefundStatus,
} from '@prisma/client';
import { Queue } from 'bull';
import { REMINDER_QUEUE } from '../notifications/notification-queue.constants';
import { NotificationsService } from '../notifications/notifications.service';
import { PrismaService } from '../prisma/prisma.service';
import { SlotService } from '../slots/slot.service';
import { SellerAppointmentsQueryDto } from './dto/seller-appointments-query.dto';
import { SellerCancelAppointmentDto } from './dto/seller-cancel-appointment.dto';
import { SellerEarningsQueryDto } from './dto/seller-earnings-query.dto';

@Injectable()
export class SellerDashboardService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly slotService: SlotService,
    private readonly notificationsService: NotificationsService,
    @InjectQueue(REMINDER_QUEUE)
    private readonly reminderQueue: Queue,
  ) {}

  async getDashboard(sellerId: string) {
    const salon = await this.getSellerSalon(sellerId);
    const { startUtc, endUtc } = this.getIstDateRangeUtc(this.getIstDateString(new Date()));
    const [todayAppointments, todayRevenueAggregate, pendingCount, completedCount, upcomingAppointments] =
      await Promise.all([
        this.prisma.booking.count({
          where: { salonId: salon.id, scheduledAt: { gte: startUtc, lt: endUtc } },
        }),
        this.prisma.booking.aggregate({
          where: {
            salonId: salon.id,
            status: BookingStatus.COMPLETED,
            scheduledAt: { gte: startUtc, lt: endUtc },
          },
          _sum: { subtotal: true },
        }),
        this.prisma.booking.count({
          where: { salonId: salon.id, status: BookingStatus.CONFIRMED },
        }),
        this.prisma.booking.count({
          where: {
            salonId: salon.id,
            status: BookingStatus.COMPLETED,
            scheduledAt: { gte: startUtc, lt: endUtc },
          },
        }),
        this.prisma.booking.findMany({
          where: {
            salonId: salon.id,
            status: BookingStatus.CONFIRMED,
            scheduledAt: { gte: new Date() },
          },
          take: 5,
          orderBy: { scheduledAt: 'asc' },
          include: {
            user: { select: { id: true, name: true, phone: true, avatar: true } },
            services: { select: { serviceName: true, price: true, durationMinutes: true } },
            staff: { select: { id: true, name: true } },
          },
        }),
      ]);

    return {
      todayAppointments,
      todayRevenue: Number(todayRevenueAggregate._sum.subtotal ?? 0),
      pendingCount,
      completedCount,
      upcomingAppointments,
    };
  }

  async getAppointments(sellerId: string, query: SellerAppointmentsQueryDto) {
    const salon = await this.getSellerSalon(sellerId);
    const limit = query.limit ?? 20;
    const dateRange = query.date ? this.getIstDateRangeUtc(query.date) : null;
    const skip = query.page && !query.cursor ? (query.page - 1) * limit : undefined;
    const bookings = await this.prisma.booking.findMany({
      where: {
        salonId: salon.id,
        ...(query.status ? { status: query.status } : {}),
        ...(dateRange ? { scheduledAt: { gte: dateRange.startUtc, lt: dateRange.endUtc } } : {}),
      },
      take: limit + 1,
      ...(query.cursor ? { cursor: { id: query.cursor }, skip: 1 } : {}),
      ...(skip !== undefined ? { skip } : {}),
      orderBy: [{ scheduledAt: 'desc' }, { id: 'desc' }],
      include: {
        user: { select: { id: true, name: true, phone: true, avatar: true } },
        services: { select: { serviceName: true, price: true, durationMinutes: true } },
        staff: { select: { id: true, name: true } },
      },
    });
    const hasMore = bookings.length > limit;
    const items = hasMore ? bookings.slice(0, limit) : bookings;

    return {
      items,
      pageInfo: {
        hasMore,
        nextCursor: hasMore ? items[items.length - 1]?.id : null,
        page: query.page ?? null,
      },
    };
  }

  async completeAppointment(sellerId: string, bookingId: string) {
    const booking = await this.getSellerBooking(sellerId, bookingId);

    if (booking.status !== BookingStatus.CONFIRMED) {
      throw new BadRequestException('Only confirmed appointments can be marked complete.');
    }

    const updatedBooking = await this.prisma.booking.update({
      where: { id: booking.id },
      data: {
        status: BookingStatus.COMPLETED,
        ...(booking.paymentMethod === PaymentMethod.AT_SHOP
          ? { paymentStatus: PaymentStatus.PAID }
          : {}),
      },
      include: {
        user: { select: { id: true, name: true, phone: true, avatar: true } },
        salon: { select: { id: true, name: true } },
        services: true,
        staff: { select: { id: true, name: true } },
      },
    });

    await this.reminderQueue.add(
      'review-request',
      { bookingId: booking.id, userId: booking.userId },
      {
        delay: 24 * 60 * 60 * 1000,
        jobId: `booking:${booking.id}:review-request`,
        removeOnComplete: true,
        removeOnFail: false,
      },
    );

    return updatedBooking;
  }

  async cancelAppointment(sellerId: string, bookingId: string, dto: SellerCancelAppointmentDto) {
    const booking = await this.getSellerBooking(sellerId, bookingId);

    if (booking.status !== BookingStatus.CONFIRMED) {
      throw new BadRequestException('Only confirmed appointments can be cancelled.');
    }

    const paidOnline =
      booking.paymentMethod === PaymentMethod.ONLINE && booking.paymentStatus === PaymentStatus.PAID;
    const updatedBooking = await this.prisma.booking.update({
      where: { id: booking.id },
      data: {
        status: BookingStatus.CANCELLED,
        cancelledAt: new Date(),
        cancelledBy: CancelledBy.SELLER,
        cancelReason: dto.reason,
        ...(paidOnline
          ? {
              refundStatus: RefundStatus.PENDING,
              refundAmount: booking.totalAmount,
            }
          : {}),
      },
    });

    await this.slotService.releaseSlot(
      booking.salonId,
      this.getIstDateString(booking.scheduledAt),
      this.getIstMinutesOfDay(booking.scheduledAt),
    );
    await this.cancelBookingJobs(booking.id);
    await this.notificationsService.sendToUser(booking.userId, {
      title: `Booking cancelled by ${booking.salon.name}`,
      body: `Your booking at ${booking.salon.name} was cancelled by the salon. ${
        paidOnline ? 'Refund initiated.' : ''
      }`,
      data: {
        type: 'BOOKING_CANCELLED_BY_SELLER',
        bookingId: booking.id,
      },
    });
    if (paidOnline) {
      await this.notificationsService.enqueueRefund(
        booking.id,
        Number(booking.totalAmount),
        'Seller cancellation full refund',
      );
    }

    return {
      cancelled: true,
      refundAmount: paidOnline ? Number(booking.totalAmount) : 0,
      paymentGateway: paidOnline ? 'PAYMENT_GATEWAY_PLACEHOLDER: initiate full refund here' : null,
      booking: updatedBooking,
    };
  }

  async getEarnings(sellerId: string, query: SellerEarningsQueryDto) {
    const salon = await this.getSellerSalon(sellerId);
    const { startUtc, endUtc } = this.getPeriodRange(query);
    const bookings = await this.prisma.booking.findMany({
      where: {
        salonId: salon.id,
        status: BookingStatus.COMPLETED,
        scheduledAt: { gte: startUtc, lt: endUtc },
      },
      orderBy: { scheduledAt: 'desc' },
      include: {
        services: { select: { serviceName: true, price: true } },
        user: { select: { id: true, name: true, phone: true } },
      },
    });
    const grossRevenue = bookings.reduce((total, booking) => total + Number(booking.subtotal), 0);
    const commissionDeducted = bookings.reduce(
      (total, booking) => total + Number(booking.commissionAmount),
      0,
    );

    return {
      period: query.period,
      startDate: startUtc,
      endDate: endUtc,
      grossRevenue: this.roundMoney(grossRevenue),
      commissionDeducted: this.roundMoney(commissionDeducted),
      netRevenue: this.roundMoney(grossRevenue - commissionDeducted),
      bookingCount: bookings.length,
      transactions: bookings.map((booking) => ({
        bookingId: booking.id,
        scheduledAt: booking.scheduledAt,
        user: booking.user,
        services: booking.services.map((service) => service.serviceName),
        gross: Number(booking.subtotal),
        commissionRate: booking.commissionRate,
        commissionDeducted: Number(booking.commissionAmount),
        net: this.roundMoney(Number(booking.subtotal) - Number(booking.commissionAmount)),
      })),
      payoutHistory: [],
    };
  }

  private async getSellerSalon(sellerId: string) {
    const salon = await this.prisma.salon.findUnique({
      where: { sellerId },
      select: { id: true },
    });

    if (!salon) {
      throw new NotFoundException('Seller salon not found.');
    }

    return salon;
  }

  private async getSellerBooking(sellerId: string, bookingId: string) {
    const booking = await this.prisma.booking.findFirst({
      where: { id: bookingId, salon: { sellerId } },
      include: {
        salon: { select: { id: true, name: true } },
      },
    });

    if (!booking) {
      throw new NotFoundException('Appointment not found.');
    }

    return booking;
  }

  private async cancelBookingJobs(bookingId: string) {
    for (const jobId of [
      `booking:${bookingId}:reminder:60`,
      `booking:${bookingId}:reminder:15`,
      `booking:${bookingId}:review-request`,
    ]) {
      const job = await this.reminderQueue.getJob(jobId);
      await job?.remove();
    }
  }

  private getPeriodRange(query: SellerEarningsQueryDto) {
    const today = this.getIstDateString(new Date());

    if (query.period === 'custom') {
      if (!query.startDate || !query.endDate) {
        throw new BadRequestException('startDate and endDate are required for custom period.');
      }

      const start = this.getIstDateRangeUtc(query.startDate).startUtc;
      const endStart = this.getIstDateRangeUtc(query.endDate).startUtc;
      const end = new Date(endStart.getTime() + 24 * 60 * 60 * 1000);

      return { startUtc: start, endUtc: end };
    }

    if (query.period === 'today') {
      return this.getIstDateRangeUtc(today);
    }

    const now = new Date(`${today}T00:00:00+05:30`);
    const start = new Date(now);

    if (query.period === 'week') {
      start.setUTCDate(start.getUTCDate() - 6);
    }

    if (query.period === 'month') {
      start.setUTCDate(1);
    }

    return {
      startUtc: start,
      endUtc: new Date(now.getTime() + 24 * 60 * 60 * 1000),
    };
  }

  private getIstDateRangeUtc(date: string) {
    const startUtc = new Date(`${date}T00:00:00+05:30`);
    const endUtc = new Date(startUtc.getTime() + 24 * 60 * 60 * 1000);

    return { startUtc, endUtc };
  }

  private getIstDateString(date: Date) {
    const parts = new Intl.DateTimeFormat('en-CA', {
      timeZone: 'Asia/Kolkata',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    }).formatToParts(date);

    return `${parts.find((part) => part.type === 'year')?.value}-${parts.find((part) => part.type === 'month')?.value}-${parts.find((part) => part.type === 'day')?.value}`;
  }

  private getIstMinutesOfDay(date: Date) {
    const parts = new Intl.DateTimeFormat('en-US', {
      timeZone: 'Asia/Kolkata',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    }).formatToParts(date);
    const rawHour = Number(parts.find((part) => part.type === 'hour')?.value ?? 0);
    const hour = rawHour === 24 ? 0 : rawHour;
    const minute = Number(parts.find((part) => part.type === 'minute')?.value ?? 0);

    return hour * 60 + minute;
  }

  private roundMoney(value: number) {
    return Math.round(value * 100) / 100;
  }
}
