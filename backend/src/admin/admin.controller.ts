import { Body, Controller, Get, Param, Post, Put, Query, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Role } from '@prisma/client';
import { Roles } from '../auth/decorators/roles.decorator';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../auth/guards/roles.guard';
import { AdminService } from './admin.service';
import { AdminBookingsQueryDto } from './dto/admin-bookings-query.dto';
import { AdminBroadcastDto } from './dto/admin-broadcast.dto';
import { AdminConfigUpdateDto } from './dto/admin-config-update.dto';
import { AdminPaginationDto } from './dto/admin-pagination.dto';
import { AdminReasonDto } from './dto/admin-reason.dto';
import { AdminRevenueQueryDto } from './dto/admin-revenue-query.dto';
import { AdminSellersQueryDto } from './dto/admin-sellers-query.dto';
import { AdminUsersQueryDto } from './dto/admin-users-query.dto';

@ApiTags('admin')
@ApiBearerAuth()
@Roles(Role.ADMIN)
@UseGuards(JwtAuthGuard, RolesGuard)
@Controller('admin')
export class AdminController {
  constructor(private readonly adminService: AdminService) {}

  @Get('dashboard')
  @ApiOperation({ summary: 'Return global admin dashboard statistics.' })
  getDashboard() {
    return this.adminService.getDashboard();
  }

  @Get('users')
  @ApiOperation({ summary: 'Return paginated users with status/search filters.' })
  listUsers(@Query() query: AdminUsersQueryDto) {
    return this.adminService.listUsers(query);
  }

  @Get('users/:id')
  @ApiOperation({ summary: 'Return full user detail and booking summary.' })
  getUser(@Param('id') id: string) {
    return this.adminService.getUser(id);
  }

  @Put('users/:id/block')
  @ApiOperation({ summary: 'Block a user.' })
  blockUser(@Param('id') id: string, @Body() dto: AdminReasonDto) {
    return this.adminService.blockUser(id, dto);
  }

  @Put('users/:id/unblock')
  @ApiOperation({ summary: 'Unblock a user and reset warnings.' })
  unblockUser(@Param('id') id: string) {
    return this.adminService.unblockUser(id);
  }

  @Put('users/:id/warnings/reset')
  @ApiOperation({ summary: 'Reset user warning count.' })
  resetUserWarnings(@Param('id') id: string) {
    return this.adminService.resetUserWarnings(id);
  }

  @Get('sellers')
  @ApiOperation({ summary: 'Return paginated sellers with status/type/subscription filters.' })
  listSellers(@Query() query: AdminSellersQueryDto) {
    return this.adminService.listSellers(query);
  }

  @Get('sellers/:id')
  @ApiOperation({ summary: 'Return full seller, salon, and verification document details.' })
  getSeller(@Param('id') id: string) {
    return this.adminService.getSeller(id);
  }

  @Put('sellers/:id/approve')
  @ApiOperation({ summary: 'Approve a seller.' })
  approveSeller(@Param('id') id: string) {
    return this.adminService.approveSeller(id);
  }

  @Put('sellers/:id/reject')
  @ApiOperation({ summary: 'Reject a seller with reason.' })
  rejectSeller(@Param('id') id: string, @Body() dto: AdminReasonDto) {
    return this.adminService.rejectSeller(id, dto);
  }

  @Put('sellers/:id/block')
  @ApiOperation({ summary: 'Block a seller with reason.' })
  blockSeller(@Param('id') id: string, @Body() dto: AdminReasonDto) {
    return this.adminService.blockSeller(id, dto);
  }

  @Put('sellers/:id/unblock')
  @ApiOperation({ summary: 'Unblock a seller.' })
  unblockSeller(@Param('id') id: string) {
    return this.adminService.unblockSeller(id);
  }

  @Get('bookings')
  @ApiOperation({ summary: 'Return paginated bookings with admin filters.' })
  listBookings(@Query() query: AdminBookingsQueryDto) {
    return this.adminService.listBookings(query);
  }

  @Get('bookings/:id')
  @ApiOperation({ summary: 'Return booking detail.' })
  getBooking(@Param('id') id: string) {
    return this.adminService.getBooking(id);
  }

  @Put('bookings/:id/cancel')
  @ApiOperation({ summary: 'Admin-cancel a booking and initiate full refund if paid.' })
  cancelBooking(@Param('id') id: string, @Body() dto: AdminReasonDto) {
    return this.adminService.cancelBooking(id, dto);
  }

  @Get('config')
  @ApiOperation({ summary: 'Return editable admin config.' })
  getConfig() {
    return this.adminService.getConfig();
  }

  @Put('config')
  @ApiOperation({ summary: 'Update editable admin config fields.' })
  updateConfig(@Body() dto: AdminConfigUpdateDto) {
    return this.adminService.updateConfig(dto);
  }

  @Get('revenue')
  @ApiOperation({ summary: 'Return grouped revenue report.' })
  getRevenue(@Query() query: AdminRevenueQueryDto) {
    return this.adminService.getRevenue(query);
  }

  @Get('commissions')
  @ApiOperation({ summary: 'Return commission ledger.' })
  listCommissions(@Query() query: AdminPaginationDto) {
    return this.adminService.listCommissions(query);
  }

  @Get('subscriptions')
  @ApiOperation({ summary: 'Return seller subscription list.' })
  listSubscriptions(@Query() query: AdminPaginationDto) {
    return this.adminService.listSubscriptions(query);
  }

  @Get('promotions')
  @ApiOperation({ summary: 'Return featured listing promotions.' })
  listPromotions(@Query() query: AdminPaginationDto) {
    return this.adminService.listPromotions(query);
  }

  @Post('notifications/broadcast')
  @ApiOperation({ summary: 'Broadcast an FCM notification to users, sellers, or all.' })
  broadcast(@Body() dto: AdminBroadcastDto) {
    return this.adminService.broadcast(dto);
  }
}
