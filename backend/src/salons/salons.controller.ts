import { Controller, Get, Param, Query } from '@nestjs/common';
import { ApiOperation, ApiTags } from '@nestjs/swagger';
import { SalonListQueryDto } from './dto/salon-list-query.dto';
import { SalonReviewsQueryDto } from './dto/salon-reviews-query.dto';
import { SalonServicesQueryDto } from './dto/salon-services-query.dto';
import { SalonsService } from './salons.service';
import { SlotQueryDto } from '../slots/dto/slot-query.dto';
import { SlotService } from '../slots/slot.service';

@ApiTags('salons')
@Controller('salons')
export class SalonsController {
  constructor(
    private readonly salonsService: SalonsService,
    private readonly slotService: SlotService,
  ) {}

  @Get()
  @ApiOperation({ summary: 'Return nearby active salons with filters and Redis caching.' })
  findNearby(@Query() query: SalonListQueryDto) {
    return this.salonsService.findNearby(query);
  }

  @Get('featured')
  @ApiOperation({ summary: 'Return promoted salons with active featured listings.' })
  findFeatured() {
    return this.salonsService.findFeatured();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Return full salon details.' })
  findOne(@Param('id') id: string) {
    return this.salonsService.findOne(id);
  }

  @Get(':id/services')
  @ApiOperation({ summary: 'Return active services for a salon, optionally filtered by gender.' })
  findServices(@Param('id') id: string, @Query() query: SalonServicesQueryDto) {
    return this.salonsService.findServices(id, query);
  }

  @Get(':id/staff')
  @ApiOperation({ summary: 'Return available staff for a salon.' })
  findStaff(@Param('id') id: string) {
    return this.salonsService.findStaff(id);
  }

  @Get(':id/reviews')
  @ApiOperation({ summary: 'Return verified booking reviews for a salon.' })
  findReviews(@Param('id') id: string, @Query() query: SalonReviewsQueryDto) {
    return this.salonsService.findReviews(id, query);
  }

  @Get(':id/slots')
  @ApiOperation({ summary: 'Return slot availability for a date and selected service IDs.' })
  findSlots(@Param('id') id: string, @Query() query: SlotQueryDto) {
    return this.slotService.getAvailableSlots(id, query.date, query.serviceIds, query.staffId);
  }
}
