import { Module } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { PassportModule } from '@nestjs/passport';
import { AuthTokenService } from './auth-token.service';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';
import { GoogleAuthService } from './google-auth.service';
import { JwtAuthGuard } from './guards/jwt-auth.guard';
import { RolesGuard } from './guards/roles.guard';
import { TemporaryJwtAuthGuard } from './guards/temporary-jwt-auth.guard';
import { Msg91Service } from './msg91.service';
import { JwtStrategy } from './strategies/jwt.strategy';
import { TemporaryJwtStrategy } from './strategies/temporary-jwt.strategy';

@Module({
  imports: [PassportModule, JwtModule.register({})],
  controllers: [AuthController],
  providers: [
    AuthService,
    AuthTokenService,
    Msg91Service,
    GoogleAuthService,
    JwtAuthGuard,
    RolesGuard,
    TemporaryJwtAuthGuard,
    JwtStrategy,
    TemporaryJwtStrategy,
  ],
  exports: [JwtModule, AuthTokenService, JwtAuthGuard, RolesGuard, TemporaryJwtAuthGuard],
})
export class AuthModule {}
