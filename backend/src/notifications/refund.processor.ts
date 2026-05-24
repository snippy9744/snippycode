import { Process, Processor } from '@nestjs/bull';
import { RefundStatus } from '@prisma/client';
import { Job } from 'bull';
import { PrismaService } from '../prisma/prisma.service';
import { REFUND_JOBS, REFUND_QUEUE } from './notification-queue.constants';
import { RefundJob } from './interfaces/notification-job.interface';
import { NotificationsService } from './notifications.service';

@Processor(REFUND_QUEUE)
export class RefundProcessor {
  constructor(
    private readonly prisma: PrismaService,
    private readonly notificationsService: NotificationsService,
  ) {}

  @Process(REFUND_JOBS.PROCESS_REFUND)
  async processRefund(job: Job<RefundJob>) {
    // PAYMENT_PLACEHOLDER: replace with Razorpay refund API in B11.
    const booking = await this.prisma.booking.update({
      where: { id: job.data.bookingId },
      data: {
        refundStatus: RefundStatus.PENDING,
        refundAmount: job.data.amount,
      },
      include: { salon: { select: { name: true } } },
    });

    await this.notificationsService.sendToUser(booking.userId, {
      title: 'Refund initiated',
      body: `Refund of Rs ${job.data.amount} for ${booking.salon.name} has been initiated.`,
      data: {
        type: 'REFUND_INITIATED',
        bookingId: booking.id,
        amount: String(job.data.amount),
      },
    });

    return {
      refundId: `REFUND_PLACEHOLDER_${booking.id}`,
      bookingId: booking.id,
      amount: job.data.amount,
      status: RefundStatus.PENDING,
      reason: job.data.reason,
    };
  }
}
