import { Body, Controller, Get, Param, Post, Put, Query, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { AuthenticatedUser } from '../auth/interfaces/authenticated-user.interface';
import { UpdateMeDto } from './dto/update-me.dto';
import { UserBookingsQueryDto } from './dto/user-bookings-query.dto';
import { UsersService } from './users.service';
import { UserWarningService } from './user-warning.service';

@ApiTags('users')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('users')
export class UsersController {
  constructor(
    private readonly usersService: UsersService,
    private readonly userWarningService: UserWarningService,
  ) {}

  @Get('me')
  @ApiOperation({ summary: 'Return the current user profile.' })
  getMe(@CurrentUser() currentUser: AuthenticatedUser) {
    return this.usersService.getMe(currentUser.id);
  }

  @Put('me')
  @ApiOperation({ summary: 'Update current user profile fields.' })
  updateMe(@CurrentUser() currentUser: AuthenticatedUser, @Body() dto: UpdateMeDto) {
    return this.usersService.updateMe(currentUser.id, dto);
  }

  @Get('me/bookings')
  @ApiOperation({ summary: 'Return current user bookings, cursor-paginated.' })
  getMyBookings(
    @CurrentUser() currentUser: AuthenticatedUser,
    @Query() query: UserBookingsQueryDto,
  ) {
    return this.usersService.getMyBookings(currentUser.id, query);
  }

  @Get('me/saved-salons')
  @ApiOperation({ summary: 'Return salons saved by the current user.' })
  getSavedSalons(@CurrentUser() currentUser: AuthenticatedUser) {
    return this.usersService.getSavedSalons(currentUser.id);
  }

  @Post('me/saved-salons/:salonId')
  @ApiOperation({ summary: 'Toggle saved state for a salon.' })
  toggleSavedSalon(
    @CurrentUser() currentUser: AuthenticatedUser,
    @Param('salonId') salonId: string,
  ) {
    return this.usersService.toggleSavedSalon(currentUser.id, salonId);
  }

  @Get('me/warnings')
  @ApiOperation({ summary: 'Return warning count, warning limit, and blocked state.' })
  getWarnings(@CurrentUser() currentUser: AuthenticatedUser) {
    return this.userWarningService.getWarningStatus(currentUser.id);
  }
}
