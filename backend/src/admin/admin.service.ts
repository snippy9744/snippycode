import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import {
  BookingStatus,
  CancelledBy,
  PaymentStatus,
  Prisma,
  RefundStatus,
  Role,
  SellerStatus,
  UserStatus,
} from '@prisma/client';
import { NotificationsService } from '../notifications/notifications.service';
import { PrismaService } from '../prisma/prisma.service';
import { RedisService } from '../redis/redis.service';
import { SlotService } from '../slots/slot.service';
import { UserWarningService } from '../users/user-warning.service';
import { AdminBookingsQueryDto } from './dto/admin-bookings-query.dto';
import { AdminBroadcastDto } from './dto/admin-broadcast.dto';
import { AdminConfigUpdateDto } from './dto/admin-config-update.dto';
import { AdminPaginationDto } from './dto/admin-pagination.dto';
import { AdminReasonDto } from './dto/admin-reason.dto';
import { AdminRevenueQueryDto } from './dto/admin-revenue-query.dto';
import { AdminSellersQueryDto } from './dto/admin-sellers-query.dto';
import { AdminUsersQueryDto } from './dto/admin-users-query.dto';

@Injectable()
export class AdminService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly redis: RedisService,
    private readonly slotService: SlotService,
    private readonly notificationsService: NotificationsService,
    private readonly userWarningService: UserWarningService,
  ) {}

  async getDashboard() {
    const today = this.getIstDateString(new Date());
    const { startUtc, endUtc } = this.getIstDateRangeUtc(today);
    const [
      totalUsers,
      activeUsers,
      blockedUsers,
      totalSellers,
      pendingSellers,
      activeSellers,
      totalBookings,
      todayBookings,
      todayRevenue,
      platformRevenueBookings,
      recentBookings,
    ] = await Promise.all([
      this.prisma.user.count(),
      this.prisma.user.count({ where: { status: UserStatus.ACTIVE } }),
      this.prisma.user.count({ where: { status: UserStatus.BLOCKED } }),
      this.prisma.seller.count(),
      this.prisma.seller.count({ where: { status: SellerStatus.PENDING_APPROVAL } }),
      this.prisma.seller.count({ where: { status: SellerStatus.ACTIVE } }),
      this.prisma.booking.count(),
      this.prisma.booking.count({ where: { createdAt: { gte: startUtc, lt: endUtc } } }),
      this.prisma.booking.aggregate({
        where: { status: BookingStatus.COMPLETED, scheduledAt: { gte: startUtc, lt: endUtc } },
        _sum: { totalAmount: true },
      }),
      this.prisma.booking.findMany({
        where: { status: BookingStatus.COMPLETED },
        select: { platformFee: true, commissionAmount: true },
      }),
      this.prisma.booking.findMany({
        take: 10,
        orderBy: { createdAt: 'desc' },
        include: {
          user: { select: { id: true, name: true, phone: true } },
          salon: { select: { id: true, name: true } },
          services: { select: { serviceName: true } },
        },
      }),
    ]);

    return {
      totalUsers,
      activeUsers,
      blockedUsers,
      totalSellers,
      pendingSellers,
      activeSellers,
      totalBookings,
      todayBookings,
      todayRevenue: Number(todayRevenue._sum.totalAmount ?? 0),
      platformRevenue: this.roundMoney(
        platformRevenueBookings.reduce(
          (total, booking) =>
            total + Number(booking.platformFee) + Number(booking.commissionAmount),
          0,
        ),
      ),
      recentBookings,
    };
  }

  async listUsers(query: AdminUsersQueryDto) {
    const limit = query.limit ?? 20;
    const where: Prisma.UserWhereInput = {
      ...(query.status ? { status: query.status } : {}),
      ...(query.search
        ? {
            OR: [
              { name: { contains: query.search, mode: 'insensitive' } },
              { phone: { contains: query.search, mode: 'insensitive' } },
              { email: { contains: query.search, mode: 'insensitive' } },
            ],
          }
        : {}),
    };
    const users = await this.prisma.user.findMany({
      where,
      take: limit + 1,
      ...(query.cursor ? { cursor: { id: query.cursor }, skip: 1 } : {}),
      orderBy: [{ createdAt: 'desc' }, { id: 'desc' }],
      include: { seller: { select: { id: true, shopName: true, status: true } } },
    });

    return this.paginate(users, limit);
  }

  async getUser(id: string) {
    const user = await this.prisma.user.findUnique({
      where: { id },
      include: {
        seller: { include: { salon: true } },
        savedSalons: { include: { salon: { select: { id: true, name: true, photos: true } } } },
        bookings: {
          take: 20,
          orderBy: { scheduledAt: 'desc' },
          include: { salon: { select: { id: true, name: true } }, services: true },
        },
        reviews: { take: 10, orderBy: { createdAt: 'desc' } },
      },
    });

    if (!user) {
      throw new NotFoundException('User not found.');
    }

    const bookingSummary = await this.prisma.booking.groupBy({
      by: ['status'],
      where: { userId: id },
      _count: { status: true },
    });

    return { ...user, bookingSummary };
  }

  blockUser(id: string, dto: AdminReasonDto) {
    return this.userWarningService.blockUser(id, dto.reason);
  }

  unblockUser(id: string) {
    return this.userWarningService.unblockUser(id);
  }

  resetUserWarnings(id: string) {
    return this.userWarningService.resetWarnings(id);
  }

  async listSellers(query: AdminSellersQueryDto) {
    const limit = query.limit ?? 20;
    const sellers = await this.prisma.seller.findMany({
      where: {
        ...(query.status ? { status: query.status } : {}),
        ...(query.sellerType ? { sellerType: query.sellerType } : {}),
        ...(query.subscriptionStatus ? { subscriptionStatus: query.subscriptionStatus } : {}),
      },
      take: limit + 1,
      ...(query.cursor ? { cursor: { id: query.cursor }, skip: 1 } : {}),
      orderBy: [{ createdAt: 'desc' }, { id: 'desc' }],
      include: {
        user: { select: { id: true, name: true, phone: true, email: true, avatar: true } },
        salon: { select: { id: true, name: true, addressText: true, photos: true } },
      },
    });

    return this.paginate(sellers, limit);
  }

  async getSeller(id: string) {
    const seller = await this.prisma.seller.findUnique({
      where: { id },
      include: {
        user: { select: { id: true, name: true, phone: true, email: true, avatar: true } },
        salon: {
          include: {
            services: true,
            staff: true,
            workingHours: { include: { breaks: true }, orderBy: { dayOfWeek: 'asc' } },
          },
        },
      },
    });

    if (!seller) {
      throw new NotFoundException('Seller not found.');
    }

    return seller;
  }

  async approveSeller(id: string) {
    const seller = await this.updateSellerStatus(id, SellerStatus.ACTIVE);
    await this.notificationsService.sendToUser(seller.userId, {
      title: 'Account approved',
      body: 'Your Snippy Seat seller account has been approved.',
      data: { type: 'SELLER_APPROVED', sellerId: seller.id },
    });
    if (seller.subscriptionExpiry) {
      await this.notificationsService.scheduleSellerSubscriptionExpiring(
        seller.id,
        seller.userId,
        seller.subscriptionExpiry,
      );
    }

    return seller;
  }

  async rejectSeller(id: string, dto: AdminReasonDto) {
    const seller = await this.updateSellerStatus(id, SellerStatus.BLOCKED);
    await this.notificationsService.sendToUser(seller.userId, {
      title: 'Seller account rejected',
      body: dto.reason,
      data: { type: 'SELLER_REJECTED', sellerId: seller.id },
    });

    return seller;
  }

  async blockSeller(id: string, dto: AdminReasonDto) {
    const seller = await this.updateSellerStatus(id, SellerStatus.BLOCKED);
    await this.notificationsService.sendToUser(seller.userId, {
      title: 'Seller account blocked',
      body: dto.reason,
      data: { type: 'SELLER_BLOCKED', sellerId: seller.id },
    });

    return seller;
  }

  async unblockSeller(id: string) {
    const seller = await this.updateSellerStatus(id, SellerStatus.ACTIVE);
    await this.notificationsService.sendToUser(seller.userId, {
      title: 'Seller account unblocked',
      body: 'Your seller account is active again.',
      data: { type: 'SELLER_UNBLOCKED', sellerId: seller.id },
    });

    return seller;
  }

  async listBookings(query: AdminBookingsQueryDto) {
    const limit = query.limit ?? 20;
    const dateFilter = this.getOptionalDateFilter(query.startDate, query.endDate);
    const bookings = await this.prisma.booking.findMany({
      where: {
        ...(query.status ? { status: query.status } : {}),
        ...(query.salonId ? { salonId: query.salonId } : {}),
        ...(query.userId ? { userId: query.userId } : {}),
        ...(dateFilter ? { scheduledAt: dateFilter } : {}),
      },
      take: limit + 1,
      ...(query.cursor ? { cursor: { id: query.cursor }, skip: 1 } : {}),
      orderBy: [{ scheduledAt: 'desc' }, { id: 'desc' }],
      include: {
        user: { select: { id: true, name: true, phone: true } },
        salon: { select: { id: true, name: true } },
        services: true,
        staff: { select: { id: true, name: true } },
      },
    });

    return this.paginate(bookings, limit);
  }

  async getBooking(id: string) {
    const booking = await this.prisma.booking.findUnique({
      where: { id },
      include: {
        user: { select: { id: true, name: true, phone: true, email: true } },
        salon: { include: { seller: true } },
        services: true,
        staff: true,
        review: true,
      },
    });

    if (!booking) {
      throw new NotFoundException('Booking not found.');
    }

    return booking;
  }

  async cancelBooking(id: string, dto: AdminReasonDto) {
    const booking = await this.prisma.booking.findUnique({
      where: { id },
      include: { salon: { include: { seller: true } } },
    });

    if (!booking) {
      throw new NotFoundException('Booking not found.');
    }

    if (booking.status === BookingStatus.CANCELLED) {
      throw new BadRequestException('Booking is already cancelled.');
    }

    const paid = booking.paymentStatus === PaymentStatus.PAID;
    const updated = await this.prisma.booking.update({
      where: { id },
      data: {
        status: BookingStatus.CANCELLED,
        cancelledAt: new Date(),
        cancelledBy: CancelledBy.ADMIN,
        cancelReason: dto.reason,
        ...(paid
          ? {
              refundStatus: RefundStatus.PENDING,
              refundAmount: booking.totalAmount,
            }
          : {}),
      },
    });

    await this.slotService.releaseSlot(
      booking.salonId,
      this.getIstDateString(booking.scheduledAt),
      this.getIstMinutesOfDay(booking.scheduledAt),
    );
    await this.notificationsService.sendToUser(booking.userId, {
      title: 'Booking cancelled',
      body: `Your booking at ${booking.salon.name} was cancelled by admin. ${
        paid ? 'Refund initiated.' : ''
      }`,
      data: { type: 'BOOKING_CANCELLED_BY_ADMIN', bookingId: booking.id },
    });
    if (paid) {
      await this.notificationsService.enqueueRefund(
        booking.id,
        Number(booking.totalAmount),
        'Admin cancellation full refund',
      );
    }

    return {
      cancelled: true,
      refundAmount: paid ? Number(booking.totalAmount) : 0,
      paymentGateway: paid ? 'PAYMENT_GATEWAY_PLACEHOLDER: initiate full refund here' : null,
      booking: updated,
    };
  }

  async getConfig() {
    return this.ensureConfig();
  }

  async updateConfig(dto: AdminConfigUpdateDto) {
    const existing = await this.ensureConfig();
    const effectiveConvenienceFeeMin = dto.convenienceFeeMin ?? Number(existing.convenienceFeeMin);
    const effectiveConvenienceFeeMax = dto.convenienceFeeMax ?? Number(existing.convenienceFeeMax);

    if (effectiveConvenienceFeeMin > effectiveConvenienceFeeMax) {
      throw new BadRequestException('convenienceFeeMin cannot be greater than convenienceFeeMax.');
    }

    const updated = await this.prisma.adminConfig.update({
      where: { id: existing.id },
      data: dto,
    });
    await this.invalidateConfigCaches();

    return updated;
  }

  async getRevenue(query: AdminRevenueQueryDto) {
    const { startUtc, endUtc } = this.getPeriodRange(query);
    const bookings = await this.prisma.booking.findMany({
      where: {
        status: BookingStatus.COMPLETED,
        scheduledAt: { gte: startUtc, lt: endUtc },
      },
      select: {
        scheduledAt: true,
        totalAmount: true,
        platformFee: true,
        commissionAmount: true,
      },
      orderBy: { scheduledAt: 'asc' },
    });
    const grouped = new Map<
      string,
      { date: string; bookings: number; grossRevenue: number; platformRevenue: number; commissions: number }
    >();

    for (const booking of bookings) {
      const key = this.getRevenueGroupKey(booking.scheduledAt, query.groupBy);
      const current =
        grouped.get(key) ??
        { date: key, bookings: 0, grossRevenue: 0, platformRevenue: 0, commissions: 0 };
      current.bookings += 1;
      current.grossRevenue += Number(booking.totalAmount);
      current.commissions += Number(booking.commissionAmount);
      current.platformRevenue += Number(booking.platformFee) + Number(booking.commissionAmount);
      grouped.set(key, current);
    }

    return Array.from(grouped.values()).map((item) => ({
      ...item,
      grossRevenue: this.roundMoney(item.grossRevenue),
      platformRevenue: this.roundMoney(item.platformRevenue),
      commissions: this.roundMoney(item.commissions),
    }));
  }

  async listCommissions(query: AdminPaginationDto) {
    const limit = query.limit ?? 20;
    const bookings = await this.prisma.booking.findMany({
      where: { status: BookingStatus.COMPLETED, commissionAmount: { gt: 0 } },
      take: limit + 1,
      ...(query.cursor ? { cursor: { id: query.cursor }, skip: 1 } : {}),
      orderBy: [{ scheduledAt: 'desc' }, { id: 'desc' }],
      include: {
        salon: { select: { id: true, name: true, seller: { select: { id: true, shopName: true } } } },
        user: { select: { id: true, name: true, phone: true } },
      },
    });

    return this.paginate(
      bookings.map((booking) => ({
        id: booking.id,
        bookingId: booking.id,
        scheduledAt: booking.scheduledAt,
        salon: booking.salon,
        user: booking.user,
        subtotal: booking.subtotal,
        platformFee: booking.platformFee,
        commissionRate: booking.commissionRate,
        commissionAmount: booking.commissionAmount,
        totalPlatformRevenue: Number(booking.platformFee) + Number(booking.commissionAmount),
      })),
      limit,
    );
  }

  async listSubscriptions(query: AdminPaginationDto) {
    const limit = query.limit ?? 20;
    const sellers = await this.prisma.seller.findMany({
      take: limit + 1,
      ...(query.cursor ? { cursor: { id: query.cursor }, skip: 1 } : {}),
      orderBy: [{ subscriptionExpiry: 'asc' }, { id: 'asc' }],
      include: {
        user: { select: { id: true, name: true, phone: true, email: true } },
        salon: { select: { id: true, name: true } },
      },
    });

    return this.paginate(
      sellers.map((seller) => ({
        id: seller.id,
        sellerId: seller.id,
        sellerName: seller.user.name,
        shopName: seller.shopName,
        salon: seller.salon,
        status: seller.subscriptionStatus,
        subscriptionExpiry: seller.subscriptionExpiry,
        sellerType: seller.sellerType,
      })),
      limit,
    );
  }

  async listPromotions(query: AdminPaginationDto) {
    const limit = query.limit ?? 20;
    const sellers = await this.prisma.seller.findMany({
      where: { isFeatured: true },
      take: limit + 1,
      ...(query.cursor ? { cursor: { id: query.cursor }, skip: 1 } : {}),
      orderBy: [{ featuredExpiry: 'desc' }, { id: 'desc' }],
      include: { salon: { select: { id: true, name: true, photos: true } } },
    });

    return this.paginate(
      sellers.map((seller) => ({
        id: seller.id,
        sellerId: seller.id,
        salon: seller.salon,
        isFeatured: seller.isFeatured,
        featuredExpiry: seller.featuredExpiry,
        active: Boolean(seller.featuredExpiry && seller.featuredExpiry > new Date()),
      })),
      limit,
    );
  }

  async broadcast(dto: AdminBroadcastDto) {
    const tokens = await this.getBroadcastTokens(dto.target);

    return {
      target: dto.target,
      recipientCount: tokens.length,
      result: await this.notificationsService.sendToDevices(tokens, {
        title: dto.title,
        body: dto.body,
        data: dto.data,
      }),
    };
  }

  private async updateSellerStatus(id: string, status: SellerStatus) {
    const seller = await this.prisma.seller.update({
      where: { id },
      data: { status },
      include: { salon: true },
    });

    return seller;
  }

  private async ensureConfig() {
    const existing = await this.prisma.adminConfig.findFirst();

    if (existing) {
      return existing;
    }

    return this.prisma.adminConfig.create({ data: {} });
  }

  private async invalidateConfigCaches() {
    const patterns = ['admin-config', 'config:*', 'pricing-config', 'commission-config'];

    for (const pattern of patterns) {
      if (pattern.includes('*')) {
        const keys = await this.redis.keys(pattern);
        if (keys.length > 0) {
          await this.redis.del(...keys);
        }
      } else {
        await this.redis.del(pattern);
      }
    }
  }

  private async getBroadcastTokens(target: 'USERS' | 'SELLERS' | 'ALL') {
    const where: Prisma.UserWhereInput =
      target === 'USERS'
        ? { role: Role.USER, fcmToken: { not: null } }
        : target === 'SELLERS'
          ? { role: Role.SELLER, fcmToken: { not: null } }
          : { fcmToken: { not: null } };
    const users = await this.prisma.user.findMany({
      where,
      select: { fcmToken: true },
    });

    return users.map((user) => user.fcmToken).filter((token): token is string => Boolean(token));
  }

  private paginate<T extends { id: string }>(items: T[], limit: number) {
    const hasMore = items.length > limit;
    const pageItems = hasMore ? items.slice(0, limit) : items;

    return {
      items: pageItems,
      pageInfo: {
        hasMore,
        nextCursor: hasMore ? pageItems[pageItems.length - 1]?.id : null,
      },
    };
  }

  private getOptionalDateFilter(startDate?: string, endDate?: string): Prisma.DateTimeFilter | null {
    if (!startDate && !endDate) {
      return null;
    }

    return {
      ...(startDate ? { gte: this.getIstDateRangeUtc(startDate).startUtc } : {}),
      ...(endDate ? { lt: this.getIstDateRangeUtc(endDate).endUtc } : {}),
    };
  }

  private getPeriodRange(query: AdminRevenueQueryDto) {
    const today = this.getIstDateString(new Date());

    if (query.period === 'custom') {
      if (!query.startDate || !query.endDate) {
        throw new BadRequestException('startDate and endDate are required for custom period.');
      }

      return {
        startUtc: this.getIstDateRangeUtc(query.startDate).startUtc,
        endUtc: this.getIstDateRangeUtc(query.endDate).endUtc,
      };
    }

    if (query.period === 'today') {
      return this.getIstDateRangeUtc(today);
    }

    const endUtc = this.getIstDateRangeUtc(today).endUtc;
    const startUtc = new Date(endUtc);

    if (query.period === 'week') {
      startUtc.setUTCDate(startUtc.getUTCDate() - 7);
    } else {
      startUtc.setUTCMonth(startUtc.getUTCMonth() - 1);
    }

    return { startUtc, endUtc };
  }

  private getRevenueGroupKey(date: Date, groupBy: 'day' | 'week' | 'month') {
    const day = this.getIstDateString(date);

    if (groupBy === 'day') {
      return day;
    }

    const start = new Date(`${day}T00:00:00+05:30`);

    if (groupBy === 'week') {
      const dayOfWeek = start.getUTCDay();
      start.setUTCDate(start.getUTCDate() - dayOfWeek);

      return this.getIstDateString(start);
    }

    return day.slice(0, 7);
  }

  private getIstDateRangeUtc(date: string) {
    const startUtc = new Date(`${date}T00:00:00+05:30`);
    const endUtc = new Date(startUtc.getTime() + 24 * 60 * 60 * 1000);

    return { startUtc, endUtc };
  }

  private getIstDateString(date: Date) {
    const parts = new Intl.DateTimeFormat('en-CA', {
      timeZone: 'Asia/Kolkata',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    }).formatToParts(date);

    return `${parts.find((part) => part.type === 'year')?.value}-${parts.find((part) => part.type === 'month')?.value}-${parts.find((part) => part.type === 'day')?.value}`;
  }

  private getIstMinutesOfDay(date: Date) {
    const parts = new Intl.DateTimeFormat('en-US', {
      timeZone: 'Asia/Kolkata',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    }).formatToParts(date);
    const rawHour = Number(parts.find((part) => part.type === 'hour')?.value ?? 0);
    const hour = rawHour === 24 ? 0 : rawHour;
    const minute = Number(parts.find((part) => part.type === 'minute')?.value ?? 0);

    return hour * 60 + minute;
  }

  private roundMoney(value: number) {
    return Math.round(value * 100) / 100;
  }
}
