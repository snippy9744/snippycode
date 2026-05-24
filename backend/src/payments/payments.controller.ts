import { Body, Controller, Param, Post, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Role } from '@prisma/client';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import { Roles } from '../auth/decorators/roles.decorator';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../auth/guards/roles.guard';
import { AuthenticatedUser } from '../auth/interfaces/authenticated-user.interface';
import { InitiatePaymentDto } from './dto/initiate-payment.dto';
import { RefundPaymentDto } from './dto/refund-payment.dto';
import { VerifyPaymentDto } from './dto/verify-payment.dto';
import { PaymentsService } from './payments.service';

@ApiTags('payments')
@Controller('payments')
export class PaymentsController {
  constructor(private readonly paymentsService: PaymentsService) {}

  @Post('initiate')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Create a payment gateway order placeholder for a booking.' })
  initiate(@CurrentUser() currentUser: AuthenticatedUser, @Body() dto: InitiatePaymentDto) {
    return this.paymentsService.initiatePayment(currentUser.id, dto.bookingId);
  }

  @Post('verify')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Verify a payment placeholder and mark the booking as paid.' })
  verify(@CurrentUser() currentUser: AuthenticatedUser, @Body() dto: VerifyPaymentDto) {
    return this.paymentsService.verifyBookingPayment(currentUser.id, dto);
  }

  @Post('webhook')
  @ApiOperation({ summary: 'Receive Razorpay webhook payloads. Placeholder until keys are configured.' })
  webhook(@Body() payload: Record<string, unknown>) {
    return this.paymentsService.handleWebhook(payload);
  }

  @Post('refund/:bookingId')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(Role.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Admin-only refund placeholder. Queues a refund job.' })
  refund(@Param('bookingId') bookingId: string, @Body() dto: RefundPaymentDto) {
    return this.paymentsService.initiateRefund(
      bookingId,
      dto.amount,
      dto.reason ?? 'Admin initiated refund',
    );
  }
}
