import { Process, Processor } from '@nestjs/bull';
import { Job } from 'bull';
import { PrismaService } from '../prisma/prisma.service';
import { FCMService } from './fcm.service';
import {
  NOTIFICATION_JOBS,
  NOTIFICATION_QUEUE,
} from './notification-queue.constants';
import {
  ExpiringNotificationJob,
  SendToDeviceJob,
  SendToDevicesJob,
  SendToTopicJob,
} from './interfaces/notification-job.interface';

@Processor(NOTIFICATION_QUEUE)
export class NotificationProcessor {
  constructor(
    private readonly fcmService: FCMService,
    private readonly prisma: PrismaService,
  ) {}

  @Process(NOTIFICATION_JOBS.SEND_TO_DEVICE)
  sendToDevice(job: Job<SendToDeviceJob>) {
    return this.fcmService.sendToDevice(
      job.data.fcmToken,
      job.data.title,
      job.data.body,
      job.data.data,
    );
  }

  @Process(NOTIFICATION_JOBS.SEND_TO_DEVICES)
  sendToDevices(job: Job<SendToDevicesJob>) {
    return this.fcmService.sendToDevices(
      job.data.fcmTokens,
      job.data.title,
      job.data.body,
      job.data.data,
    );
  }

  @Process(NOTIFICATION_JOBS.SEND_TO_TOPIC)
  sendToTopic(job: Job<SendToTopicJob>) {
    return this.fcmService.sendToTopic(
      job.data.topic,
      job.data.title,
      job.data.body,
      job.data.data,
    );
  }

  @Process(NOTIFICATION_JOBS.PREMIUM_EXPIRING)
  async premiumExpiring(job: Job<ExpiringNotificationJob>) {
    const user = await this.prisma.user.findUnique({
      where: { id: job.data.userId },
      select: { fcmToken: true, isPremium: true, premiumExpiry: true },
    });

    if (!user?.fcmToken || !user.isPremium) {
      return { sent: false, reason: 'User is not eligible for premium expiry notification.' };
    }

    return this.fcmService.sendToDevice(
      user.fcmToken,
      job.data.title,
      job.data.body,
      job.data.data,
    );
  }

  @Process(NOTIFICATION_JOBS.SELLER_SUBSCRIPTION_EXPIRING)
  async sellerSubscriptionExpiring(job: Job<ExpiringNotificationJob>) {
    const user = await this.prisma.user.findUnique({
      where: { id: job.data.userId },
      select: { fcmToken: true, seller: { select: { subscriptionExpiry: true } } },
    });

    if (!user?.fcmToken || !user.seller?.subscriptionExpiry) {
      return { sent: false, reason: 'Seller is not eligible for subscription expiry notification.' };
    }

    return this.fcmService.sendToDevice(
      user.fcmToken,
      job.data.title,
      job.data.body,
      job.data.data,
    );
  }
}
