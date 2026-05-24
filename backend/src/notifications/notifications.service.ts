import { Injectable } from '@nestjs/common';
import { InjectQueue } from '@nestjs/bull';
import { Queue } from 'bull';
import { PrismaService } from '../prisma/prisma.service';
import {
  NOTIFICATION_JOBS,
  NOTIFICATION_QUEUE,
  REFUND_JOBS,
  REFUND_QUEUE,
} from './notification-queue.constants';
import {
  ExpiringNotificationJob,
  RefundJob,
  SendToDeviceJob,
  SendToDevicesJob,
  SendToTopicJob,
} from './interfaces/notification-job.interface';

export interface PushNotificationPayload {
  title: string;
  body: string;
  data?: Record<string, string>;
}

@Injectable()
export class NotificationsService {
  constructor(
    private readonly prisma: PrismaService,
    @InjectQueue(NOTIFICATION_QUEUE)
    private readonly notificationQueue: Queue,
    @InjectQueue(REFUND_QUEUE)
    private readonly refundQueue: Queue<RefundJob>,
  ) {}

  async sendToUser(userId: string, payload: PushNotificationPayload) {
    const user = await this.prisma.user.findUnique({
      where: { id: userId },
      select: { fcmToken: true },
    });

    if (!user?.fcmToken) {
      return { queued: false, reason: 'User does not have an FCM token.' };
    }

    return this.sendToDevice(user.fcmToken, payload.title, payload.body, payload.data);
  }

  async sendToDevice(
    fcmToken: string,
    title: string,
    body: string,
    data?: Record<string, string>,
  ) {
    const job = await this.notificationQueue.add(
      NOTIFICATION_JOBS.SEND_TO_DEVICE,
      { fcmToken, title, body, data } satisfies SendToDeviceJob,
      { removeOnComplete: true, removeOnFail: false },
    );

    return { queued: true, jobId: job.id };
  }

  async sendToDevices(fcmTokens: string[], payload: PushNotificationPayload) {
    const tokens = [...new Set(fcmTokens.filter(Boolean))];

    if (tokens.length === 0) {
      return { queued: false, recipientCount: 0 };
    }

    const job = await this.notificationQueue.add(
      NOTIFICATION_JOBS.SEND_TO_DEVICES,
      {
        fcmTokens: tokens,
        title: payload.title,
        body: payload.body,
        data: payload.data,
      } satisfies SendToDevicesJob,
      { removeOnComplete: true, removeOnFail: false },
    );

    return {
      queued: true,
      jobId: job.id,
      recipientCount: tokens.length,
      batches: Math.ceil(tokens.length / 500),
    };
  }

  async sendToTopic(
    topic: string,
    title: string,
    body: string,
    data?: Record<string, string>,
  ) {
    const job = await this.notificationQueue.add(
      NOTIFICATION_JOBS.SEND_TO_TOPIC,
      { topic, title, body, data } satisfies SendToTopicJob,
      { removeOnComplete: true, removeOnFail: false },
    );

    return { queued: true, jobId: job.id };
  }

  async schedulePremiumExpiring(userId: string, premiumExpiry: Date) {
    const delay = premiumExpiry.getTime() - Date.now() - 3 * 24 * 60 * 60 * 1000;

    if (delay <= 0) {
      return { queued: false, reason: 'Premium expiry is less than 3 days away.' };
    }

    const job = await this.notificationQueue.add(
      NOTIFICATION_JOBS.PREMIUM_EXPIRING,
      {
        userId,
        title: 'Premium expiring soon',
        body: 'Your Snippy Seat Premium membership expires in 3 days.',
        data: { type: 'PREMIUM_EXPIRING' },
      } satisfies ExpiringNotificationJob,
      {
        delay,
        jobId: `user:${userId}:premium-expiring`,
        removeOnComplete: true,
        removeOnFail: false,
      },
    );

    return { queued: true, jobId: job.id };
  }

  async scheduleSellerSubscriptionExpiring(sellerId: string, userId: string, expiry: Date) {
    const delay = expiry.getTime() - Date.now() - 7 * 24 * 60 * 60 * 1000;

    if (delay <= 0) {
      return { queued: false, reason: 'Seller subscription expiry is less than 7 days away.' };
    }

    const job = await this.notificationQueue.add(
      NOTIFICATION_JOBS.SELLER_SUBSCRIPTION_EXPIRING,
      {
        userId,
        title: 'Subscription expiring soon',
        body: 'Your seller subscription expires in 7 days. Renew to keep receiving bookings.',
        data: { type: 'SELLER_SUBSCRIPTION_EXPIRING', sellerId },
      } satisfies ExpiringNotificationJob,
      {
        delay,
        jobId: `seller:${sellerId}:subscription-expiring`,
        removeOnComplete: true,
        removeOnFail: false,
      },
    );

    return { queued: true, jobId: job.id };
  }

  async enqueueRefund(bookingId: string, amount: number, reason: string) {
    const job = await this.refundQueue.add(
      REFUND_JOBS.PROCESS_REFUND,
      { bookingId, amount, reason },
      {
        jobId: `booking:${bookingId}:refund`,
        removeOnComplete: true,
        removeOnFail: false,
      },
    );

    return { queued: true, jobId: job.id };
  }
}
