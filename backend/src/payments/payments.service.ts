import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { BookingStatus, PaymentStatus, RefundStatus } from '@prisma/client';
import { NotificationsService } from '../notifications/notifications.service';
import { PrismaService } from '../prisma/prisma.service';
import { SlotService } from '../slots/slot.service';
import { VerifyPaymentDto } from './dto/verify-payment.dto';

@Injectable()
export class PaymentsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly slotService: SlotService,
    private readonly notificationsService: NotificationsService,
  ) {}

  async initiatePayment(userId: string, bookingId: string) {
    const booking = await this.getUserBooking(userId, bookingId);

    if (booking.paymentStatus === PaymentStatus.PAID) {
      throw new BadRequestException('Booking is already paid.');
    }

    return this.createOrder(booking.id, Number(booking.totalAmount));
  }

  createOrder(bookingId: string, amount: number, currency = 'INR') {
    // TODO: Replace with Razorpay orders.create() when payment gateway keys are provided.
    return {
      orderId: `PAY_PLACEHOLDER_${bookingId}`,
      amount,
      currency,
    };
  }

  verifyPayment(orderId: string, paymentId: string, signature: string) {
    void orderId;
    void paymentId;
    void signature;
    // TODO: Replace with Razorpay signature verification using HMAC SHA256.
    return { verified: true };
  }

  async verifyBookingPayment(userId: string, dto: VerifyPaymentDto) {
    const booking = await this.getUserBooking(userId, dto.bookingId);
    const verification = this.verifyPayment(dto.orderId, dto.paymentId, dto.signature);

    if (!verification.verified) {
      throw new BadRequestException('Payment verification failed.');
    }

    const updatedBooking = await this.prisma.booking.update({
      where: { id: booking.id },
      data: {
        paymentStatus: PaymentStatus.PAID,
        paymentGatewayRef: dto.paymentId,
      },
    });

    await this.slotService.confirmSlot(
      booking.salonId,
      this.getIstDateString(booking.scheduledAt),
      this.getIstMinutesOfDay(booking.scheduledAt),
    );

    return {
      verified: true,
      booking: updatedBooking,
    };
  }

  async initiateRefund(bookingId: string, amount?: number, reason = 'Refund requested') {
    const booking = await this.prisma.booking.findUnique({
      where: { id: bookingId },
      select: {
        id: true,
        totalAmount: true,
        paymentStatus: true,
        refundStatus: true,
        status: true,
      },
    });

    if (!booking) {
      throw new NotFoundException('Booking not found.');
    }

    if (booking.paymentStatus !== PaymentStatus.PAID) {
      throw new BadRequestException('Only paid bookings can be refunded.');
    }

    if (booking.refundStatus === RefundStatus.PROCESSED) {
      throw new BadRequestException('Booking is already refunded.');
    }

    const refundAmount = amount ?? Number(booking.totalAmount);

    if (refundAmount <= 0 || refundAmount > Number(booking.totalAmount)) {
      throw new BadRequestException('Refund amount must be greater than 0 and not exceed booking total.');
    }

    // TODO: Replace with Razorpay refund.create() when payment gateway keys are provided.
    await this.prisma.booking.update({
      where: { id: booking.id },
      data: {
        refundStatus: RefundStatus.PENDING,
        refundAmount,
      },
    });
    await this.notificationsService.enqueueRefund(booking.id, refundAmount, reason);

    return {
      refundId: `REFUND_PLACEHOLDER_${booking.id}`,
      bookingId: booking.id,
      amount: refundAmount,
      status: RefundStatus.PENDING,
    };
  }

  handleWebhook(payload: Record<string, unknown>) {
    // TODO: Verify Razorpay webhook signature and process payment/refund events.
    return {
      received: true,
      provider: 'RAZORPAY_PLACEHOLDER',
      event: payload.event ?? null,
    };
  }

  private async getUserBooking(userId: string, bookingId: string) {
    const booking = await this.prisma.booking.findUnique({
      where: { id: bookingId },
    });

    if (!booking) {
      throw new NotFoundException('Booking not found.');
    }

    if (booking.userId !== userId) {
      throw new ForbiddenException('You can only pay for your own booking.');
    }

    if (booking.status !== BookingStatus.CONFIRMED) {
      throw new BadRequestException('Only confirmed bookings can be paid.');
    }

    return booking;
  }

  private getIstDateString(date: Date) {
    return new Intl.DateTimeFormat('en-CA', {
      timeZone: 'Asia/Kolkata',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    }).format(date);
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
}
