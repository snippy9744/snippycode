import { Injectable, UnauthorizedException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { User } from '@prisma/client';
import { createHash, randomUUID } from 'crypto';
import { PrismaService } from '../prisma/prisma.service';
import { JwtPayload, TemporaryJwtPayload } from './interfaces/jwt-payload.interface';

const ACCESS_TOKEN_EXPIRES_IN = '15m';
const REFRESH_TOKEN_EXPIRES_IN = '7d';
const TEMPORARY_TOKEN_EXPIRES_IN = '15m';
const REFRESH_TOKEN_TTL_MS = 7 * 24 * 60 * 60 * 1000;

type TokenUser = Pick<User, 'id' | 'role' | 'status'>;

@Injectable()
export class AuthTokenService {
  constructor(
    private readonly configService: ConfigService,
    private readonly jwtService: JwtService,
    private readonly prisma: PrismaService,
  ) {}

  signAccessToken(user: TokenUser) {
    const payload: JwtPayload = {
      sub: user.id,
      role: user.role,
      status: user.status,
    };

    return this.jwtService.sign(payload, {
      secret: this.configService.getOrThrow<string>('JWT_SECRET'),
      expiresIn: ACCESS_TOKEN_EXPIRES_IN,
    });
  }

  async signRefreshToken(user: TokenUser) {
    const tokenId = randomUUID();
    const payload: JwtPayload & { jti: string } = {
      sub: user.id,
      role: user.role,
      status: user.status,
      jti: tokenId,
    };
    const refreshToken = this.jwtService.sign(payload, {
      secret: this.configService.getOrThrow<string>('JWT_REFRESH_SECRET'),
      expiresIn: REFRESH_TOKEN_EXPIRES_IN,
    });

    await this.prisma.user.update({
      where: { id: user.id },
      data: {
        refreshTokenHash: this.hashRefreshToken(refreshToken),
        refreshTokenExpiresAt: new Date(Date.now() + REFRESH_TOKEN_TTL_MS),
      },
    });

    return refreshToken;
  }

  signTemporaryToken(userId: string) {
    const payload: TemporaryJwtPayload = {
      sub: userId,
      purpose: 'ROLE_SELECTION',
    };

    return this.jwtService.sign(payload, {
      secret: this.configService.getOrThrow<string>('JWT_SECRET'),
      expiresIn: TEMPORARY_TOKEN_EXPIRES_IN,
    });
  }

  async issueTokenPair(user: TokenUser) {
    const accessToken = this.signAccessToken(user);
    const refreshToken = await this.signRefreshToken(user);

    return { accessToken, refreshToken };
  }

  async verifyRefreshToken(refreshToken: string) {
    let payload: JwtPayload;

    try {
      payload = await this.jwtService.verifyAsync<JwtPayload>(refreshToken, {
        secret: this.configService.getOrThrow<string>('JWT_REFRESH_SECRET'),
      });
    } catch {
      throw new UnauthorizedException('Invalid refresh token.');
    }

    const user = await this.prisma.user.findUnique({
      where: { id: payload.sub },
    });

    if (
      !user?.refreshTokenHash ||
      !user.refreshTokenExpiresAt ||
      user.refreshTokenExpiresAt <= new Date()
    ) {
      throw new UnauthorizedException('Refresh token has expired or was revoked.');
    }

    if (user.refreshTokenHash !== this.hashRefreshToken(refreshToken)) {
      throw new UnauthorizedException('Refresh token does not match the active session.');
    }

    return user;
  }

  async revokeRefreshToken(userId: string) {
    await this.prisma.user.update({
      where: { id: userId },
      data: {
        refreshTokenHash: null,
        refreshTokenExpiresAt: null,
      },
    });
  }

  private hashRefreshToken(refreshToken: string) {
    const pepper = this.configService.getOrThrow<string>('JWT_REFRESH_SECRET');

    return createHash('sha256').update(`${refreshToken}.${pepper}`).digest('hex');
  }
}
