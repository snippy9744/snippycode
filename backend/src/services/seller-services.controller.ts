import { Body, Controller, Delete, Get, Param, Patch, Post, Put, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { CurrentSellerId } from '../sellers/decorators/current-seller-id.decorator';
import { SellerActiveGuard } from '../sellers/guards/seller-active.guard';
import { CreateServiceDto } from './dto/create-service.dto';
import { UpdateServiceDto } from './dto/update-service.dto';
import { SellerServicesService } from './seller-services.service';

@ApiTags('seller services')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, SellerActiveGuard)
@Controller('seller/services')
export class SellerServicesController {
  constructor(private readonly sellerServicesService: SellerServicesService) {}

  @Get()
  @ApiOperation({ summary: 'Return services for the current seller salon.' })
  findAll(@CurrentSellerId() sellerId: string) {
    return this.sellerServicesService.findAll(sellerId);
  }

  @Post()
  @ApiOperation({ summary: 'Create a seller salon service.' })
  create(@CurrentSellerId() sellerId: string, @Body() dto: CreateServiceDto) {
    return this.sellerServicesService.create(sellerId, dto);
  }

  @Put(':id')
  @ApiOperation({ summary: 'Update a seller salon service.' })
  update(
    @CurrentSellerId() sellerId: string,
    @Param('id') serviceId: string,
    @Body() dto: UpdateServiceDto,
  ) {
    return this.sellerServicesService.update(sellerId, serviceId, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Soft delete a seller salon service.' })
  softDelete(@CurrentSellerId() sellerId: string, @Param('id') serviceId: string) {
    return this.sellerServicesService.softDelete(sellerId, serviceId);
  }

  @Patch(':id/toggle')
  @ApiOperation({ summary: 'Toggle service active state.' })
  toggle(@CurrentSellerId() sellerId: string, @Param('id') serviceId: string) {
    return this.sellerServicesService.toggle(sellerId, serviceId);
  }
}
