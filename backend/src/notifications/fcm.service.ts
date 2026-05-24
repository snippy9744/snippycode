import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { cert, getApps, initializeApp } from 'firebase-admin/app';
import { getMessaging } from 'firebase-admin/messaging';
import type { PushNotificationPayload } from './notifications.service';

@Injectable()
export class FCMService {
  private readonly logger = new Logger(FCMService.name);
  private readonly firebaseReady: boolean;

  constructor(private readonly configService: ConfigService) {
    this.firebaseReady = this.initializeFirebase();
  }

  async sendToDevice(fcmToken: string, title: string, body: string, data?: Record<string, string>) {
    if (!fcmToken) {
      return { sent: false, reason: 'Missing FCM token.' };
    }

    if (!this.firebaseReady) {
      this.logger.log(`FCM not configured. Skipped device push: ${title}`);
      return { sent: false, reason: 'Firebase credentials are not configured.' };
    }

    const messageId = await getMessaging().send({
      token: fcmToken,
      notification: { title, body },
      data,
    });

    return { sent: true, messageId };
  }

  async sendToDevices(fcmTokens: string[], title: string, body: string, data?: Record<string, string>) {
    const tokens = [...new Set(fcmTokens.filter(Boolean))];

    if (tokens.length === 0) {
      return { sent: 0, failed: 0, batches: 0 };
    }

    if (!this.firebaseReady) {
      this.logger.log(`FCM not configured. Skipped batch push to ${tokens.length} devices.`);
      return {
        sent: 0,
        failed: 0,
        skipped: tokens.length,
        batches: Math.ceil(tokens.length / 500),
        reason: 'Firebase credentials are not configured.',
      };
    }

    let sent = 0;
    let failed = 0;
    const batches = this.chunk(tokens, 500);

    for (const batch of batches) {
      const response = await getMessaging().sendEachForMulticast({
        tokens: batch,
        notification: { title, body },
        data,
      });
      sent += response.successCount;
      failed += response.failureCount;
    }

    return { sent, failed, batches: batches.length };
  }

  async sendToTopic(topic: string, title: string, body: string, data?: Record<string, string>) {
    if (!this.firebaseReady) {
      this.logger.log(`FCM not configured. Skipped topic push to ${topic}: ${title}`);
      return { sent: false, reason: 'Firebase credentials are not configured.' };
    }

    const messageId = await getMessaging().send({
      topic,
      notification: { title, body },
      data,
    });

    return { sent: true, messageId };
  }

  async sendPayloadToDevice(fcmToken: string, payload: PushNotificationPayload) {
    return this.sendToDevice(fcmToken, payload.title, payload.body, payload.data);
  }

  private initializeFirebase() {
    if (getApps().length > 0) {
      return true;
    }

    const projectId = this.configService.get<string>('FIREBASE_PROJECT_ID');
    const privateKey = this.configService.get<string>('FIREBASE_PRIVATE_KEY');
    const clientEmail = this.configService.get<string>('FIREBASE_CLIENT_EMAIL');

    if (
      !projectId ||
      !privateKey ||
      !clientEmail ||
      projectId.startsWith('replace-with') ||
      privateKey.startsWith('replace-with') ||
      clientEmail.startsWith('replace-with')
    ) {
      return false;
    }

    initializeApp({
      credential: cert({
        projectId,
        clientEmail,
        privateKey: privateKey.replace(/\\n/g, '\n'),
      }),
    });

    return true;
  }

  private chunk<T>(items: T[], size: number) {
    const chunks: T[][] = [];

    for (let index = 0; index < items.length; index += size) {
      chunks.push(items.slice(index, index + size));
    }

    return chunks;
  }
}
