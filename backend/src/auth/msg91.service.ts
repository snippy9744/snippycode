import { Injectable, ServiceUnavailableException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class Msg91Service {
  constructor(private readonly configService: ConfigService) {}

  async sendOtp(phone: string, otp: string) {
    const authKey = this.configService.get<string>('MSG91_AUTH_KEY');
    const templateId = this.configService.get<string>('MSG91_TEMPLATE_ID');

    if (!authKey || !templateId || authKey.startsWith('replace-with')) {
      return {
        provider: 'MSG91',
        skipped: true,
        reason: 'MSG91 credentials are not configured.',
      };
    }

    const response = await fetch('https://control.msg91.com/api/v5/otp', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        authkey: authKey,
      },
      body: JSON.stringify({
        template_id: templateId,
        mobile: phone.replace('+', ''),
        otp,
      }),
    });

    if (!response.ok) {
      const body = await response.text();
      throw new ServiceUnavailableException(`MSG91 failed to send OTP: ${body}`);
    }

    return response.json();
  }
}
