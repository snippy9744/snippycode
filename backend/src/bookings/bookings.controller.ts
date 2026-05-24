import { Body, Controller, Get, Param, Post, Put, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { AuthenticatedUser } from '../auth/interfaces/authenticated-user.interface';
import { BlockBookingForBlockedUserGuard } from '../users/guards/block-booking-for-blocked-user.guard';
import { BookingsService } from './bookings.service';
import { CreateBookingDto } from './dto/create-booking.dto';
import { CreateReviewDto } from './dto/create-review.dto';
import { RescheduleBookingDto } from './dto/reschedule-booking.dto';

@ApiTags('bookings')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, BlockBookingForBlockedUserGuard)
@Controller('bookings')
export class BookingsController {
  constructor(private readonly bookingsService: BookingsService) {}

  @Post()
  @ApiOperation({ summary: 'Create booking, lock slot, price order, and schedule reminders.' })
  create(@CurrentUser() currentUser: AuthenticatedUser, @Body() dto: CreateBookingDto) {
    return this.bookingsService.create(currentUser.id, dto);
  }

  @Get(':id')
  @ApiOperation({ summary: 'Return booking detail for owner or admin.' })
  findOne(@CurrentUser() currentUser: AuthenticatedUser, @Param('id') bookingId: string) {
    return this.bookingsService.findOne(currentUser, bookingId);
  }

  @Put(':id/cancel')
  @ApiOperation({ summary: 'Cancel booking and apply late-warning/refund policy.' })
  cancel(@CurrentUser() currentUser: AuthenticatedUser, @Param('id') bookingId: string) {
    return this.bookingsService.cancel(currentUser.id, bookingId);
  }

  @Put(':id/reschedule')
  @ApiOperation({ summary: 'Reschedule booking if more than 30 minutes before appointment.' })
  reschedule(
    @CurrentUser() currentUser: AuthenticatedUser,
    @Param('id') bookingId: string,
    @Body() dto: RescheduleBookingDto,
  ) {
    return this.bookingsService.reschedule(currentUser.id, bookingId, dto);
  }

  @Post(':id/review')
  @ApiOperation({ summary: 'Create verified review for completed booking.' })
  review(
    @CurrentUser() currentUser: AuthenticatedUser,
    @Param('id') bookingId: string,
    @Body() dto: CreateReviewDto,
  ) {
    return this.bookingsService.review(currentUser.id, bookingId, dto);
  }
}
