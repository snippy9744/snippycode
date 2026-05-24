import { Injectable, UnauthorizedException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PassportStrategy } from '@nestjs/passport';
import { ExtractJwt, Strategy } from 'passport-jwt';
import { PrismaService } from '../../prisma/prisma.service';
import { TemporaryJwtPayload } from '../interfaces/jwt-payload.interface';

@Injectable()
export class TemporaryJwtStrategy extends PassportStrategy(Strategy, 'jwt-temporary') {
  constructor(
    configService: ConfigService,
    private readonly prisma: PrismaService,
  ) {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      ignoreExpiration: false,
      secretOrKey: configService.getOrThrow<string>('JWT_SECRET'),
    });
  }

  async validate(payload: TemporaryJwtPayload) {
    if (payload.purpose !== 'ROLE_SELECTION') {
      throw new UnauthorizedException('Temporary token is not valid for role selection.');
    }

    const user = await this.prisma.user.findUnique({
      where: { id: payload.sub },
      select: { id: true, role: true, status: true },
    });

    if (!user) {
      throw new UnauthorizedException('Invalid temporary token.');
    }

    return user;
  }
}
