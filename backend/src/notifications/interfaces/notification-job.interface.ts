import { PushNotificationPayload } from '../notifications.service';

export interface SendToDeviceJob extends PushNotificationPayload {
  fcmToken: string;
}

export interface SendToDevicesJob extends PushNotificationPayload {
  fcmTokens: string[];
}

export interface SendToTopicJob extends PushNotificationPayload {
  topic: string;
}

export interface ExpiringNotificationJob {
  userId: string;
  title: string;
  body: string;
  data?: Record<string, string>;
}

export interface RefundJob {
  bookingId: string;
  amount: number;
  reason: string;
}
