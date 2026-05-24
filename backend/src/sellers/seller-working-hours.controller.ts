import { Body, Controller, Get, ParseArrayPipe, Put, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiBody, ApiOperation, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { CurrentSellerId } from './decorators/current-seller-id.decorator';
import { WorkingHoursDto } from './dto/working-hours.dto';
import { SellerActiveGuard } from './guards/seller-active.guard';
import { SellerWorkingHoursService } from './seller-working-hours.service';

@ApiTags('seller working hours')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, SellerActiveGuard)
@Controller('seller/working-hours')
export class SellerWorkingHoursController {
  constructor(private readonly workingHoursService: SellerWorkingHoursService) {}

  @Get()
  @ApiOperation({ summary: 'Return seller salon working hours.' })
  findAll(@CurrentSellerId() sellerId: string) {
    return this.workingHoursService.findAll(sellerId);
  }

  @Put()
  @ApiOperation({ summary: 'Replace seller salon working hours.' })
  @ApiBody({ type: [WorkingHoursDto] })
  replace(
    @CurrentSellerId() sellerId: string,
    @Body(new ParseArrayPipe({ items: WorkingHoursDto })) dto: WorkingHoursDto[],
  ) {
    return this.workingHoursService.replace(sellerId, dto);
  }
}
