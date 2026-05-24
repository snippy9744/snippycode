import { Process, Processor } from '@nestjs/bull';
import { Job } from 'bull';
import { NotificationsService } from '../notifications/notifications.service';
import { PrismaService } from '../prisma/prisma.service';
import { BookingReminderJob } from './interfaces/reminder-job.interface';
import { REMINDER_QUEUE } from '../notifications/notification-queue.constants';

@Processor(REMINDER_QUEUE)
export class BookingReminderProcessor {
  constructor(
    private readonly prisma: PrismaService,
    private readonly notificationsService: NotificationsService,
  ) {}

  @Process('appointment-reminder')
  async handleAppointmentReminder(job: Job<BookingReminderJob>) {
    const booking = await this.prisma.booking.findUnique({
      where: { id: job.data.bookingId },
      select: {
        id: true,
        status: true,
        salon: { select: { name: true } },
      },
    });

    if (!booking || booking.status !== 'CONFIRMED') {
      return { sent: false, reason: 'Booking is no longer confirmed.' };
    }

    return this.notificationsService.sendToUser(job.data.userId, {
      title: `Appointment in ${job.data.minutesBefore} minutes`,
      body:
        job.data.minutesBefore === 60
          ? `Reminder: Your appointment at ${booking.salon.name} is in 1 hour`
          : `Your appointment at ${booking.salon.name} starts in 15 minutes. Head out now!`,
      data: {
        type: 'BOOKING_REMINDER',
        bookingId: booking.id,
        minutesBefore: String(job.data.minutesBefore),
      },
    });
  }

  @Process('review-request')
  async handleReviewRequest(job: Job<{ bookingId: string; userId: string }>) {
    const booking = await this.prisma.booking.findUnique({
      where: { id: job.data.bookingId },
      select: {
        id: true,
        status: true,
        review: { select: { id: true } },
        salon: { select: { name: true } },
      },
    });

    if (!booking || booking.status !== 'COMPLETED' || booking.review) {
      return { sent: false, reason: 'Booking is not eligible for review request.' };
    }

    return this.notificationsService.sendToUser(job.data.userId, {
      title: 'Rate your salon visit',
      body: `How was your experience at ${booking.salon.name}? Rate your visit.`,
      data: {
        type: 'REVIEW_REQUEST',
        bookingId: booking.id,
      },
    });
  }
}
