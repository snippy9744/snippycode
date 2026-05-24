import { Body, Controller, Get, Param, Put, Query, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { CurrentSellerId } from './decorators/current-seller-id.decorator';
import { SellerAppointmentsQueryDto } from './dto/seller-appointments-query.dto';
import { SellerCancelAppointmentDto } from './dto/seller-cancel-appointment.dto';
import { SellerEarningsQueryDto } from './dto/seller-earnings-query.dto';
import { SellerActiveGuard } from './guards/seller-active.guard';
import { SellerDashboardService } from './seller-dashboard.service';

@ApiTags('seller dashboard')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, SellerActiveGuard)
@Controller('seller')
export class SellerDashboardController {
  constructor(private readonly sellerDashboardService: SellerDashboardService) {}

  @Get('dashboard')
  @ApiOperation({ summary: 'Return seller dashboard stats and next 5 appointments.' })
  getDashboard(@CurrentSellerId() sellerId: string) {
    return this.sellerDashboardService.getDashboard(sellerId);
  }

  @Get('appointments')
  @ApiOperation({ summary: 'Return seller appointments with optional status/date filters.' })
  getAppointments(
    @CurrentSellerId() sellerId: string,
    @Query() query: SellerAppointmentsQueryDto,
  ) {
    return this.sellerDashboardService.getAppointments(sellerId, query);
  }

  @Put('appointments/:id/complete')
  @ApiOperation({ summary: 'Mark seller appointment complete.' })
  completeAppointment(@CurrentSellerId() sellerId: string, @Param('id') bookingId: string) {
    return this.sellerDashboardService.completeAppointment(sellerId, bookingId);
  }

  @Put('appointments/:id/cancel')
  @ApiOperation({ summary: 'Cancel seller appointment with required reason.' })
  cancelAppointment(
    @CurrentSellerId() sellerId: string,
    @Param('id') bookingId: string,
    @Body() dto: SellerCancelAppointmentDto,
  ) {
    return this.sellerDashboardService.cancelAppointment(sellerId, bookingId, dto);
  }

  @Get('earnings')
  @ApiOperation({ summary: 'Return seller earnings and commission breakdown.' })
  getEarnings(@CurrentSellerId() sellerId: string, @Query() query: SellerEarningsQueryDto) {
    return this.sellerDashboardService.getEarnings(sellerId, query);
  }
}
