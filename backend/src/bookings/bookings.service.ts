import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectQueue } from '@nestjs/bull';
import {
  BookingStatus,
  CancelledBy,
  PaymentMethod,
  PaymentStatus,
  RefundStatus,
  Role,
  SellerStatus,
  SubscriptionStatus,
} from '@prisma/client';
import { Queue } from 'bull';
import { NotificationsService } from '../notifications/notifications.service';
import { REMINDER_QUEUE } from '../notifications/notification-queue.constants';
import { PrismaService } from '../prisma/prisma.service';
import { SlotService } from '../slots/slot.service';
import { UserWarningService } from '../users/user-warning.service';
import { CreateBookingDto } from './dto/create-booking.dto';
import { CreateReviewDto } from './dto/create-review.dto';
import { RescheduleBookingDto } from './dto/reschedule-booking.dto';
import { BookingReminderJob } from './interfaces/reminder-job.interface';

interface PricingConfig {
  userCommissionPct: number;
  sellerCommissionPct: number;
  homeServiceCommissionPct: number;
  convenienceFeeMin: number;
  convenienceFeeMax: number;
  travelFeePerKm: number;
  gstPct: number;
  additionalTaxPct: number;
  cancellationWindowMinutes: number;
}

@Injectable()
export class BookingsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly slotService: SlotService,
    private readonly userWarningService: UserWarningService,
    private readonly notificationsService: NotificationsService,
    @InjectQueue(REMINDER_QUEUE)
    private readonly reminderQueue: Queue<BookingReminderJob>,
  ) {}

  async create(userId: string, dto: CreateBookingDto) {
    const scheduledAt = new Date(dto.scheduledAt);
    this.assertFutureDate(scheduledAt, 'scheduledAt');
    const date = this.getIstDateString(scheduledAt);
    const startMinute = this.getIstMinutesOfDay(scheduledAt);
    const serviceIds = [...new Set(dto.serviceIds)];
    const [user, salon, config] = await Promise.all([
      this.prisma.user.findUnique({ where: { id: userId } }),
      this.getBookableSalon(dto.salonId),
      this.getPricingConfig(),
    ]);

    if (!user) {
      throw new NotFoundException('User not found.');
    }

    if (dto.isHomeService && !salon.offersHomeService) {
      throw new BadRequestException('This salon does not offer home service.');
    }

    if (dto.isHomeService && (!dto.homeAddress || dto.homeLat === undefined || dto.homeLng === undefined)) {
      throw new BadRequestException('Home service requires homeAddress, homeLat, and homeLng.');
    }

    if (dto.staffId) {
      await this.ensureStaffBelongsToSalon(dto.salonId, dto.staffId);
    }

    const services = await this.getActiveServices(dto.salonId, serviceIds);
    const durationMinutes = services.reduce((total, service) => total + service.durationMinutes, 0);
    const availability = await this.slotService.getAvailableSlots(
      dto.salonId,
      date,
      serviceIds,
      dto.staffId,
    );
    this.ensureRequestedSlotAvailable(availability.slots, startMinute);
    await this.slotService.lockSlot(dto.salonId, date, startMinute, durationMinutes, userId);

    try {
      const price = this.calculatePrice({
        config,
        services,
        isHomeService: dto.isHomeService,
        isPremium: user.isPremium,
        salonLat: salon.lat,
        salonLng: salon.lng,
        homeLat: dto.homeLat,
        homeLng: dto.homeLng,
        sellerCommissionOverride: salon.seller.commissionOverride,
      });

      const booking = await this.prisma.booking.create({
        data: {
          userId,
          salonId: dto.salonId,
          staffId: dto.staffId,
          scheduledAt,
          durationMinutes,
          status: BookingStatus.CONFIRMED,
          isHomeService: dto.isHomeService,
          homeAddress: dto.homeAddress,
          homeLat: dto.homeLat,
          homeLng: dto.homeLng,
          travelFee: price.travelFee,
          subtotal: price.subtotal,
          convenienceFee: price.convenienceFee,
          taxAmount: price.taxAmount,
          platformFee: price.platformFee,
          totalAmount: price.totalAmount,
          paymentStatus: PaymentStatus.PENDING,
          paymentMethod: dto.paymentMethod,
          commissionAmount: price.commissionAmount,
          commissionRate: price.commissionRate,
          services: {
            create: services.map((service) => ({
              serviceId: service.id,
              serviceName: service.name,
              durationMinutes: service.durationMinutes,
              price: dto.isHomeService
                ? Number(service.price) * service.homePriceMultiplier
                : service.price,
            })),
          },
        },
      });

      await this.slotService.confirmSlot(dto.salonId, date, startMinute);
      await this.scheduleReminderJobs(booking.id, userId, scheduledAt);
      await this.notificationsService.sendToUser(userId, {
        title: 'Booking confirmed',
        body: `Your booking has been confirmed for ${this.formatIstTime(scheduledAt)}.`,
        data: { type: 'BOOKING_CONFIRMED', bookingId: booking.id },
      });
      await this.notificationsService.sendToUser(salon.seller.userId, {
        title: 'New booking received',
        body: `New booking from ${user.name ?? user.phone ?? 'customer'} at ${this.formatIstTime(scheduledAt)}.`,
        data: { type: 'NEW_BOOKING', bookingId: booking.id },
      });

      return {
        booking: await this.getBookingDetail(booking.id),
        payment:
          dto.paymentMethod === PaymentMethod.ONLINE
            ? {
                provider: 'PAYMENT_GATEWAY_PLACEHOLDER',
                orderId: `PAYMENT_PLACEHOLDER_${booking.id}`,
                amount: price.totalAmount,
                currency: 'INR',
              }
            : null,
      };
    } catch (error) {
      await this.slotService.releaseSlot(dto.salonId, date, startMinute);
      throw error;
    }
  }

  async findOne(currentUser: { id: string; role: Role }, bookingId: string) {
    const booking = await this.getBookingDetail(bookingId);
    this.assertBookingAccess(currentUser, booking.userId);

    return booking;
  }

  async cancel(userId: string, bookingId: string) {
    const booking = await this.prisma.booking.findUnique({
      where: { id: bookingId },
      include: {
        salon: { include: { seller: true } },
      },
    });

    if (!booking || booking.userId !== userId) {
      throw new NotFoundException('Booking not found.');
    }

    if (booking.status !== BookingStatus.CONFIRMED) {
      throw new BadRequestException('Only confirmed bookings can be cancelled.');
    }

    const config = await this.getPricingConfig();
    const minutesUntil = (booking.scheduledAt.getTime() - Date.now()) / 60000;
    const isLate = minutesUntil < config.cancellationWindowMinutes;
    const warning = isLate && !booking.warningIssued
      ? await this.userWarningService.issueWarning(userId)
      : null;
    const paid = booking.paymentStatus === PaymentStatus.PAID;
    const refundAmount = paid
      ? isLate
        ? Number(booking.totalAmount) * 0.9
        : Number(booking.totalAmount)
      : 0;

    const updatedBooking = await this.prisma.booking.update({
      where: { id: booking.id },
      data: {
        status: BookingStatus.CANCELLED,
        cancelledAt: new Date(),
        cancelledBy: CancelledBy.USER,
        cancelReason: isLate ? 'Late cancellation by customer' : 'Cancelled by customer',
        warningIssued: booking.warningIssued || isLate,
        ...(paid
          ? {
              refundStatus: RefundStatus.PENDING,
              refundAmount,
            }
          : {}),
      },
    });

    await this.releaseBookingSlot(updatedBooking);
    await this.cancelReminderJobs(booking.id);
    await this.notificationsService.sendToUser(booking.salon.seller.userId, {
      title: 'Booking cancelled',
      body: 'Booking cancelled by customer.',
      data: { type: 'BOOKING_CANCELLED_BY_USER', bookingId: booking.id },
    });
    if (paid && refundAmount > 0) {
      await this.notificationsService.enqueueRefund(
        booking.id,
        this.roundMoney(refundAmount),
        isLate ? 'Late customer cancellation partial refund' : 'Customer cancellation full refund',
      );
    }

    return {
      cancelled: true,
      warning: Boolean(warning),
      warningCount: warning?.warningCount ?? null,
      refundAmount,
      paymentGateway: paid ? 'PAYMENT_GATEWAY_PLACEHOLDER: initiate refund here' : null,
      booking: updatedBooking,
    };
  }

  async reschedule(userId: string, bookingId: string, dto: RescheduleBookingDto) {
    const booking = await this.prisma.booking.findUnique({
      where: { id: bookingId },
      include: { services: true },
    });

    if (!booking || booking.userId !== userId) {
      throw new NotFoundException('Booking not found.');
    }

    if (booking.status !== BookingStatus.CONFIRMED) {
      throw new BadRequestException('Only confirmed bookings can be rescheduled.');
    }

    const minutesUntil = (booking.scheduledAt.getTime() - Date.now()) / 60000;
    if (minutesUntil <= 30) {
      throw new BadRequestException('Bookings can only be rescheduled more than 30 minutes before the slot.');
    }

    const newScheduledAt = new Date(dto.newScheduledAt);
    this.assertFutureDate(newScheduledAt, 'newScheduledAt');
    const newDate = this.getIstDateString(newScheduledAt);
    const newStartMinute = this.getIstMinutesOfDay(newScheduledAt);
    const serviceIds = booking.services.map((service) => service.serviceId);
    const availability = await this.slotService.getAvailableSlots(
      booking.salonId,
      newDate,
      serviceIds,
      booking.staffId ?? undefined,
    );
    this.ensureRequestedSlotAvailable(availability.slots, newStartMinute);
    await this.slotService.lockSlot(
      booking.salonId,
      newDate,
      newStartMinute,
      booking.durationMinutes,
      userId,
    );

    try {
      const updatedBooking = await this.prisma.booking.update({
        where: { id: booking.id },
        data: { scheduledAt: newScheduledAt },
      });

      await this.releaseBookingSlot(booking);
      await this.slotService.confirmSlot(booking.salonId, newDate, newStartMinute);
      await this.cancelReminderJobs(booking.id);
      await this.scheduleReminderJobs(booking.id, userId, newScheduledAt);

      return this.getBookingDetail(updatedBooking.id);
    } catch (error) {
      await this.slotService.releaseSlot(booking.salonId, newDate, newStartMinute);
      throw error;
    }
  }

  async review(userId: string, bookingId: string, dto: CreateReviewDto) {
    const booking = await this.prisma.booking.findUnique({
      where: { id: bookingId },
      include: { review: true },
    });

    if (!booking || booking.userId !== userId) {
      throw new NotFoundException('Booking not found.');
    }

    if (booking.status !== BookingStatus.COMPLETED) {
      throw new BadRequestException('Only completed bookings can be reviewed.');
    }

    if (booking.review) {
      throw new BadRequestException('This booking already has a review.');
    }

    const review = await this.prisma.review.create({
      data: {
        bookingId: booking.id,
        userId,
        salonId: booking.salonId,
        rating: dto.rating,
        comment: dto.comment,
        photos: dto.photos ?? [],
      },
    });

    await this.recalculateSalonRating(booking.salonId);

    return review;
  }

  private async getBookableSalon(salonId: string) {
    const salon = await this.prisma.salon.findFirst({
      where: {
        id: salonId,
        seller: {
          status: SellerStatus.ACTIVE,
          subscriptionStatus: { in: [SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL] },
        },
      },
      include: { seller: true },
    });

    if (!salon) {
      throw new BadRequestException('Salon is not active or seller subscription is not valid.');
    }

    return salon;
  }

  private async getActiveServices(salonId: string, serviceIds: string[]) {
    const services = await this.prisma.service.findMany({
      where: { id: { in: serviceIds }, salonId, isActive: true },
    });

    if (services.length !== serviceIds.length) {
      throw new BadRequestException('All serviceIds must be active and belong to the salon.');
    }

    return services;
  }

  private async ensureStaffBelongsToSalon(salonId: string, staffId: string) {
    const staff = await this.prisma.staff.findFirst({
      where: { id: staffId, salonId, isAvailable: true },
      select: { id: true },
    });

    if (!staff) {
      throw new BadRequestException('staffId must belong to the salon and be available.');
    }
  }

  private calculatePrice(input: {
    config: PricingConfig;
    services: Array<{ price: unknown; homePriceMultiplier: number; isHomeAvailable: boolean }>;
    isHomeService: boolean;
    isPremium: boolean;
    salonLat: number;
    salonLng: number;
    homeLat?: number;
    homeLng?: number;
    sellerCommissionOverride?: number | null;
  }) {
    if (input.isHomeService && input.services.some((service) => !service.isHomeAvailable)) {
      throw new BadRequestException('One or more selected services are not available for home service.');
    }

    const subtotal = input.services.reduce((total, service) => {
      const basePrice = Number(service.price);
      return total + (input.isHomeService ? basePrice * service.homePriceMultiplier : basePrice);
    }, 0);
    const distanceKm =
      input.isHomeService && input.homeLat !== undefined && input.homeLng !== undefined
        ? this.calculateDistanceKm(input.salonLat, input.salonLng, input.homeLat, input.homeLng)
        : 0;
    const travelFee = distanceKm * input.config.travelFeePerKm;
    const convenienceFee = input.isPremium
      ? 0
      : this.randomFee(input.config.convenienceFeeMin, input.config.convenienceFeeMax);
    const platformFee = (subtotal * input.config.userCommissionPct) / 100;
    const taxBase = subtotal + convenienceFee;
    const taxAmount =
      (taxBase * input.config.gstPct) / 100 +
      (input.config.additionalTaxPct > 0 ? (taxBase * input.config.additionalTaxPct) / 100 : 0);
    const commissionRate = input.isHomeService
      ? input.config.homeServiceCommissionPct
      : input.sellerCommissionOverride ?? input.config.sellerCommissionPct;
    const commissionAmount = (subtotal * commissionRate) / 100;
    const totalAmount = subtotal + convenienceFee + travelFee + platformFee + taxAmount;

    return {
      subtotal: this.roundMoney(subtotal),
      convenienceFee: this.roundMoney(convenienceFee),
      travelFee: this.roundMoney(travelFee),
      platformFee: this.roundMoney(platformFee),
      taxAmount: this.roundMoney(taxAmount),
      totalAmount: this.roundMoney(totalAmount),
      commissionRate,
      commissionAmount: this.roundMoney(commissionAmount),
    };
  }

  private async getPricingConfig(): Promise<PricingConfig> {
    const config = await this.prisma.adminConfig.findFirst();

    return {
      userCommissionPct: config?.userCommissionPct ?? 2.5,
      sellerCommissionPct: config?.sellerCommissionPct ?? 12,
      homeServiceCommissionPct: config?.homeServiceCommissionPct ?? 15,
      convenienceFeeMin: Number(config?.convenienceFeeMin ?? 10),
      convenienceFeeMax: Number(config?.convenienceFeeMax ?? 30),
      travelFeePerKm: Number(config?.travelFeePerKm ?? 10),
      gstPct: config?.gstPct ?? 18,
      additionalTaxPct: config?.additionalTaxPct ?? 0,
      cancellationWindowMinutes: config?.cancellationWindowMinutes ?? 15,
    };
  }

  private randomFee(min: number, max: number) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
  }

  private roundMoney(value: number) {
    return Math.round(value * 100) / 100;
  }

  private calculateDistanceKm(lat1: number, lng1: number, lat2: number, lng2: number) {
    const earthRadiusKm = 6371;
    const dLat = this.toRadians(lat2 - lat1);
    const dLng = this.toRadians(lng2 - lng1);
    const a =
      Math.sin(dLat / 2) ** 2 +
      Math.cos(this.toRadians(lat1)) * Math.cos(this.toRadians(lat2)) * Math.sin(dLng / 2) ** 2;

    return earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  private toRadians(value: number) {
    return (value * Math.PI) / 180;
  }

  private async scheduleReminderJobs(bookingId: string, userId: string, scheduledAt: Date) {
    for (const minutesBefore of [60, 15] as const) {
      const delay = scheduledAt.getTime() - Date.now() - minutesBefore * 60 * 1000;
      if (delay > 0) {
        await this.reminderQueue.add(
          'appointment-reminder',
          { bookingId, userId, minutesBefore },
          {
            delay,
            jobId: this.getReminderJobId(bookingId, minutesBefore),
            removeOnComplete: true,
            removeOnFail: false,
          },
        );
      }
    }
  }

  private async cancelReminderJobs(bookingId: string) {
    for (const minutesBefore of [60, 15] as const) {
      const job = await this.reminderQueue.getJob(this.getReminderJobId(bookingId, minutesBefore));
      await job?.remove();
    }
  }

  private getReminderJobId(bookingId: string, minutesBefore: 60 | 15) {
    return `booking:${bookingId}:reminder:${minutesBefore}`;
  }

  private async releaseBookingSlot(booking: { salonId: string; scheduledAt: Date }) {
    await this.slotService.releaseSlot(
      booking.salonId,
      this.getIstDateString(booking.scheduledAt),
      this.getIstMinutesOfDay(booking.scheduledAt),
    );
  }

  private ensureRequestedSlotAvailable(
    slots: Array<{ startTime: string; available: boolean }>,
    startMinute: number,
  ) {
    const startTime = this.minutesToTime(startMinute);
    const slot = slots.find((item) => item.startTime === startTime);

    if (!slot?.available) {
      throw new BadRequestException('Selected slot is not available.');
    }
  }

  private async getBookingDetail(bookingId: string) {
    const booking = await this.prisma.booking.findUnique({
      where: { id: bookingId },
      include: {
        user: { select: { id: true, name: true, phone: true, avatar: true } },
        salon: { select: { id: true, name: true, addressText: true, photos: true } },
        staff: { select: { id: true, name: true, photo: true, speciality: true } },
        services: true,
        review: true,
      },
    });

    if (!booking) {
      throw new NotFoundException('Booking not found.');
    }

    return booking;
  }

  private assertBookingAccess(currentUser: { id: string; role: Role }, bookingUserId: string) {
    if (currentUser.role !== Role.ADMIN && currentUser.id !== bookingUserId) {
      throw new ForbiddenException('You can only access your own bookings.');
    }
  }

  private async recalculateSalonRating(salonId: string) {
    const aggregate = await this.prisma.review.aggregate({
      where: { salonId },
      _avg: { rating: true },
      _count: { rating: true },
    });

    await this.prisma.salon.update({
      where: { id: salonId },
      data: {
        averageRating: aggregate._avg.rating ?? 0,
        totalReviews: aggregate._count.rating,
      },
    });
  }

  private getIstDateString(date: Date) {
    const parts = new Intl.DateTimeFormat('en-CA', {
      timeZone: 'Asia/Kolkata',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    }).formatToParts(date);
    const year = parts.find((part) => part.type === 'year')?.value;
    const month = parts.find((part) => part.type === 'month')?.value;
    const day = parts.find((part) => part.type === 'day')?.value;

    return `${year}-${month}-${day}`;
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

  private minutesToTime(minutes: number) {
    const hour = Math.floor(minutes / 60);
    const minute = minutes % 60;

    return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
  }

  private formatIstTime(date: Date) {
    return new Intl.DateTimeFormat('en-IN', {
      timeZone: 'Asia/Kolkata',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date);
  }

  private assertFutureDate(date: Date, fieldName: string) {
    if (Number.isNaN(date.getTime()) || date.getTime() <= Date.now()) {
      throw new BadRequestException(`${fieldName} must be a future date.`);
    }
  }
}
