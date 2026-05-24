import { Injectable, UnauthorizedException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { OAuth2Client } from 'google-auth-library';

export interface VerifiedGoogleProfile {
  email: string;
  name?: string;
  googleId: string;
  avatar?: string;
}

@Injectable()
export class GoogleAuthService {
  private readonly client: OAuth2Client;

  constructor(private readonly configService: ConfigService) {
    this.client = new OAuth2Client(this.configService.get<string>('GOOGLE_CLIENT_ID'));
  }

  async verifyIdToken(idToken: string): Promise<VerifiedGoogleProfile> {
    const clientId = this.configService.getOrThrow<string>('GOOGLE_CLIENT_ID');

    try {
      const ticket = await this.client.verifyIdToken({
        idToken,
        audience: clientId,
      });
      const payload = ticket.getPayload();

      if (!payload?.sub || !payload.email) {
        throw new UnauthorizedException('Google token is missing required profile fields.');
      }

      return {
        email: payload.email,
        name: payload.name,
        googleId: payload.sub,
        avatar: payload.picture,
      };
    } catch (error) {
      if (error instanceof UnauthorizedException) {
        throw error;
      }

      throw new UnauthorizedException('Invalid Google ID token.');
    }
  }
}
