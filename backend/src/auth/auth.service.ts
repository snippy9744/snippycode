import { BadRequestException, Injectable, UnauthorizedException } from '@nestjs/common';
import { User } from '@prisma/client';
import { PrismaService } from '../prisma/prisma.service';
import { RedisService } from '../redis/redis.service';
import { AuthTokenService } from './auth-token.service';
import { GoogleAuthDto } from './dto/google-auth.dto';
import { RefreshTokenDto } from './dto/refresh-token.dto';
import { SelectRoleDto } from './dto/select-role.dto';
import { SendOtpDto } from './dto/send-otp.dto';
import { VerifyOtpDto } from './dto/verify-otp.dto';
import { AuthenticatedUser } from './interfaces/authenticated-user.interface';
import { GoogleAuthService } from './google-auth.service';
import { Msg91Service } from './msg91.service';

const OTP_TTL_SECONDS = 300;

@Injectable()
export class AuthService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly redis: RedisService,
    private readonly msg91Service: Msg91Service,
    private readonly googleAuthService: GoogleAuthService,
    private readonly authTokenService: AuthTokenService,
  ) {}

  async sendOtp(dto: SendOtpDto) {
    const phone = this.normalizeIndianPhone(dto.phone);
    const otp = this.generateOtp();

    await this.redis.set(this.getOtpKey(phone), otp, 'EX', OTP_TTL_SECONDS);
    await this.msg91Service.sendOtp(phone, otp);

    return {
      message: 'OTP sent',
      expiresIn: OTP_TTL_SECONDS,
    };
  }

  async verifyOtp(dto: VerifyOtpDto) {
    const phone = this.normalizeIndianPhone(dto.phone);
    const storedOtp = await this.redis.get(this.getOtpKey(phone));

    if (!storedOtp || storedOtp !== dto.otp) {
      throw new UnauthorizedException('Invalid or expired OTP.');
    }

    await this.redis.del(this.getOtpKey(phone));

    const existingUser = await this.prisma.user.findUnique({ where: { phone } });
    const user =
      existingUser ??
      (await this.prisma.user.create({
        data: { phone },
      }));

    return this.buildAuthResponse(user, !existingUser);
  }

  async google(dto: GoogleAuthDto) {
    const profile = await this.googleAuthService.verifyIdToken(dto.idToken);
    const existingUser =
      (await this.prisma.user.findUnique({ where: { googleId: profile.googleId } })) ??
      (await this.prisma.user.findUnique({ where: { email: profile.email } }));

    const user = existingUser
      ? await this.prisma.user.update({
          where: { id: existingUser.id },
          data: {
            googleId: profile.googleId,
            email: profile.email,
            name: existingUser.name ?? profile.name,
            avatar: existingUser.avatar ?? profile.avatar,
          },
        })
      : await this.prisma.user.create({
          data: {
            googleId: profile.googleId,
            email: profile.email,
            name: profile.name,
            avatar: profile.avatar,
          },
        });

    return this.buildAuthResponse(user, !existingUser);
  }

  async selectRole(currentUser: AuthenticatedUser, dto: SelectRoleDto) {
    const user = await this.prisma.user.update({
      where: { id: currentUser.id },
      data: { role: dto.role },
    });
    const tokens = await this.authTokenService.issueTokenPair(user);

    return {
      ...tokens,
      user: this.sanitizeUser(user),
    };
  }

  async refresh(dto: RefreshTokenDto) {
    const user = await this.authTokenService.verifyRefreshToken(dto.refreshToken);
    const tokens = await this.authTokenService.issueTokenPair(user);

    return {
      ...tokens,
      user: this.sanitizeUser(user),
    };
  }

  async logout(refreshToken: string) {
    const user = await this.authTokenService.verifyRefreshToken(refreshToken);
    await this.authTokenService.revokeRefreshToken(user.id);

    return {
      message: 'Logged out successfully',
    };
  }

  private async buildAuthResponse(user: User, isNewUser: boolean) {
    if (isNewUser) {
      return {
        isNewUser: true,
        token: this.authTokenService.signTemporaryToken(user.id),
      };
    }

    const tokens = await this.authTokenService.issueTokenPair(user);

    return {
      isNewUser: false,
      ...tokens,
      user: this.sanitizeUser(user),
    };
  }

  private normalizeIndianPhone(phone: string) {
    const digits = phone.replace(/\D/g, '');

    if (digits.length === 10) {
      return `+91${digits}`;
    }

    if (digits.length === 12 && digits.startsWith('91')) {
      return `+${digits}`;
    }

    throw new BadRequestException('phone must be a valid Indian mobile number.');
  }

  private generateOtp() {
    return Math.floor(100000 + Math.random() * 900000).toString();
  }

  private getOtpKey(phone: string) {
    return `otp:${phone}`;
  }

  private sanitizeUser(user: User) {
    const {
      refreshTokenHash: _hash,
      refreshTokenExpiresAt: _expiresAt,
      passwordHash: _passwordHash,
      ...safeUser
    } = user;

    return safeUser;
  }
}
