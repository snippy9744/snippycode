import { Body, Controller, Delete, Get, Param, Patch, Post, Put, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { CurrentSellerId } from '../sellers/decorators/current-seller-id.decorator';
import { SellerActiveGuard } from '../sellers/guards/seller-active.guard';
import { CreateStaffDto } from './dto/create-staff.dto';
import { UpdateStaffDto } from './dto/update-staff.dto';
import { SellerStaffService } from './seller-staff.service';

@ApiTags('seller staff')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, SellerActiveGuard)
@Controller('seller/staff')
export class SellerStaffController {
  constructor(private readonly sellerStaffService: SellerStaffService) {}

  @Get()
  @ApiOperation({ summary: 'Return staff for the current seller salon.' })
  findAll(@CurrentSellerId() sellerId: string) {
    return this.sellerStaffService.findAll(sellerId);
  }

  @Post()
  @ApiOperation({ summary: 'Create a staff member and assign services.' })
  create(@CurrentSellerId() sellerId: string, @Body() dto: CreateStaffDto) {
    return this.sellerStaffService.create(sellerId, dto);
  }

  @Put(':id')
  @ApiOperation({ summary: 'Update a staff member.' })
  update(
    @CurrentSellerId() sellerId: string,
    @Param('id') staffId: string,
    @Body() dto: UpdateStaffDto,
  ) {
    return this.sellerStaffService.update(sellerId, staffId, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Delete a staff member.' })
  delete(@CurrentSellerId() sellerId: string, @Param('id') staffId: string) {
    return this.sellerStaffService.delete(sellerId, staffId);
  }

  @Patch(':id/availability')
  @ApiOperation({ summary: 'Toggle staff availability.' })
  toggleAvailability(@CurrentSellerId() sellerId: string, @Param('id') staffId: string) {
    return this.sellerStaffService.toggleAvailability(sellerId, staffId);
  }
}
