import { Global, Module } from '@nestjs/common';
import { BullModule } from '@nestjs/bull';
import { FCMService } from './fcm.service';
import { NotificationProcessor } from './notification.processor';
import { NOTIFICATION_QUEUE, REFUND_QUEUE, REMINDER_QUEUE } from './notification-queue.constants';
import { NotificationsService } from './notifications.service';
import { RefundProcessor } from './refund.processor';

@Global()
@Module({
  imports: [
    BullModule.registerQueue(
      { name: NOTIFICATION_QUEUE },
      { name: REMINDER_QUEUE },
      { name: REFUND_QUEUE },
    ),
  ],
  providers: [FCMService, NotificationsService, NotificationProcessor, RefundProcessor],
  exports: [BullModule, FCMService, NotificationsService],
})
export class NotificationsModule {}
