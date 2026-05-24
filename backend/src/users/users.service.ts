import { Injectable, NotFoundException } from '@nestjs/common';
import { Prisma } from '@prisma/client';
import { PrismaService } from '../prisma/prisma.service';
import { UpdateMeDto } from './dto/update-me.dto';
import { UserBookingsQueryDto } from './dto/user-bookings-query.dto';

@Injectable()
export class UsersService {
  constructor(private readonly prisma: PrismaService) {}

  async getMe(userId: string) {
    const user = await this.prisma.user.findUnique({
      where: { id: userId },
      select: {
        id: true,
        phone: true,
        email: true,
        name: true,
        avatar: true,
        googleId: true,
        role: true,
        status: true,
        warningCount: true,
        isPremium: true,
        premiumExpiry: true,
        fcmToken: true,
        createdAt: true,
        updatedAt: true,
        seller: {
          select: {
            id: true,
            shopName: true,
            sellerType: true,
            status: true,
            subscriptionStatus: true,
            subscriptionExpiry: true,
          },
        },
      },
    });

    if (!user) {
      throw new NotFoundException('User not found.');
    }

    return user;
  }

  async updateMe(userId: string, dto: UpdateMeDto) {
    return this.prisma.user.update({
      where: { id: userId },
      data: dto,
      select: {
        id: true,
        phone: true,
        email: true,
        name: true,
        avatar: true,
        googleId: true,
        role: true,
        status: true,
        warningCount: true,
        isPremium: true,
        premiumExpiry: true,
        fcmToken: true,
        createdAt: true,
        updatedAt: true,
      },
    });
  }

  async getMyBookings(userId: string, query: UserBookingsQueryDto) {
    const limit = query.limit ?? 20;
    const where: Prisma.BookingWhereInput = {
      userId,
      ...(query.status ? { status: query.status } : {}),
    };

    const bookings = await this.prisma.booking.findMany({
      where,
      take: limit + 1,
      ...(query.cursor ? { cursor: { id: query.cursor }, skip: 1 } : {}),
      orderBy: [{ scheduledAt: 'desc' }, { id: 'desc' }],
      include: {
        salon: {
          select: {
            id: true,
            name: true,
            photos: true,
            addressText: true,
            averageRating: true,
            totalReviews: true,
          },
        },
        staff: {
          select: {
            id: true,
            name: true,
            photo: true,
            speciality: true,
          },
        },
        services: {
          select: {
            id: true,
            serviceId: true,
            serviceName: true,
            durationMinutes: true,
            price: true,
          },
        },
        review: {
          select: {
            id: true,
            rating: true,
            comment: true,
            photos: true,
            createdAt: true,
          },
        },
      },
    });

    const hasMore = bookings.length > limit;
    const items = hasMore ? bookings.slice(0, limit) : bookings;

    return {
      items,
      pageInfo: {
        hasMore,
        nextCursor: hasMore ? items[items.length - 1]?.id : null,
      },
    };
  }

  async getSavedSalons(userId: string) {
    const savedSalons = await this.prisma.savedSalon.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
      include: {
        salon: {
          select: {
            id: true,
            name: true,
            photos: true,
            addressText: true,
            lat: true,
            lng: true,
            averageRating: true,
            totalReviews: true,
            offersHomeService: true,
            seller: {
              select: {
                id: true,
                sellerType: true,
                status: true,
                isFeatured: true,
                featuredExpiry: true,
              },
            },
            services: {
              where: { isActive: true },
              orderBy: { price: 'asc' },
              take: 1,
              select: {
                id: true,
                name: true,
                category: true,
                gender: true,
                price: true,
              },
            },
          },
        },
      },
    });

    return savedSalons.map((savedSalon) => ({
      savedAt: savedSalon.createdAt,
      salon: savedSalon.salon,
    }));
  }

  async toggleSavedSalon(userId: string, salonId: string) {
    const salon = await this.prisma.salon.findUnique({
      where: { id: salonId },
      select: { id: true },
    });

    if (!salon) {
      throw new NotFoundException('Salon not found.');
    }

    const existing = await this.prisma.savedSalon.findUnique({
      where: {
        userId_salonId: {
          userId,
          salonId,
        },
      },
    });

    if (existing) {
      await this.prisma.savedSalon.delete({
        where: {
          userId_salonId: {
            userId,
            salonId,
          },
        },
      });

      return {
        saved: false,
        salonId,
      };
    }

    await this.prisma.savedSalon.create({
      data: {
        userId,
        salonId,
      },
    });

    return {
      saved: true,
      salonId,
    };
  }
}
