import { Body, Controller, Post, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Throttle } from '@nestjs/throttler';
import { AuthService } from './auth.service';
import { CurrentUser } from './decorators/current-user.decorator';
import { GoogleAuthDto } from './dto/google-auth.dto';
import { LogoutDto } from './dto/logout.dto';
import { RefreshTokenDto } from './dto/refresh-token.dto';
import { SelectRoleDto } from './dto/select-role.dto';
import { SendOtpDto } from './dto/send-otp.dto';
import { VerifyOtpDto } from './dto/verify-otp.dto';
import { TemporaryJwtAuthGuard } from './guards/temporary-jwt-auth.guard';
import { AuthenticatedUser } from './interfaces/authenticated-user.interface';

@ApiTags('auth')
@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('send-otp')
  @Throttle({ default: { limit: 5, ttl: 60_000 } })
  @ApiOperation({ summary: 'Send OTP to an Indian phone number via MSG91.' })
  sendOtp(@Body() dto: SendOtpDto) {
    return this.authService.sendOtp(dto);
  }

  @Post('verify-otp')
  @ApiOperation({ summary: 'Verify OTP and issue either a temporary token or login tokens.' })
  verifyOtp(@Body() dto: VerifyOtpDto) {
    return this.authService.verifyOtp(dto);
  }

  @Post('google')
  @ApiOperation({ summary: 'Verify Google ID token and login or create user.' })
  google(@Body() dto: GoogleAuthDto) {
    return this.authService.google(dto);
  }

  @Post('select-role')
  @UseGuards(TemporaryJwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Select USER or SELLER role for newly created users.' })
  selectRole(@CurrentUser() currentUser: AuthenticatedUser, @Body() dto: SelectRoleDto) {
    return this.authService.selectRole(currentUser, dto);
  }

  @Post('refresh')
  @ApiOperation({ summary: 'Rotate a valid refresh token and issue a new token pair.' })
  refresh(@Body() dto: RefreshTokenDto) {
    return this.authService.refresh(dto);
  }

  @Post('logout')
  @ApiOperation({ summary: 'Invalidate the active refresh token.' })
  logout(@Body() dto: LogoutDto) {
    return this.authService.logout(dto.refreshToken);
  }
}
