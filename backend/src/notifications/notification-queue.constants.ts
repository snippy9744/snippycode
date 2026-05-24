export const NOTIFICATION_QUEUE = 'notification-queue';
export const REMINDER_QUEUE = 'reminder-queue';
export const REFUND_QUEUE = 'refund-queue';

export const NOTIFICATION_JOBS = {
  SEND_TO_DEVICE: 'send-to-device',
  SEND_TO_DEVICES: 'send-to-devices',
  SEND_TO_TOPIC: 'send-to-topic',
  PREMIUM_EXPIRING: 'premium-expiring',
  SELLER_SUBSCRIPTION_EXPIRING: 'seller-subscription-expiring',
} as const;

export const REFUND_JOBS = {
  PROCESS_REFUND: 'process-refund',
} as const;
