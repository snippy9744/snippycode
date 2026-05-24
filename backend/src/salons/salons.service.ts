import { Injectable, NotFoundException } from '@nestjs/common';
import { Gender, Prisma, SellerStatus } from '@prisma/client';
import { createHash } from 'crypto';
import { PrismaService } from '../prisma/prisma.service';
import { RedisService } from '../redis/redis.service';
import { SalonListQueryDto } from './dto/salon-list-query.dto';
import { SalonReviewsQueryDto } from './dto/salon-reviews-query.dto';
import { SalonServicesQueryDto } from './dto/salon-services-query.dto';

interface NearbySalonRow {
  id: string;
  name: string;
  description: string | null;
  addressText: string;
  lat: number;
  lng: number;
  photos: string[];
  averageRating: number;
  totalReviews: number;
  offersHomeService: boolean;
  isFeatured: boolean;
  featuredExpiry: Date | null;
  distanceKm: number;
  priceFrom: Prisma.Decimal | null;
}

@Injectable()
export class SalonsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly redis: RedisService,
  ) {}

  async findNearby(query: SalonListQueryDto) {
    const cacheKey = this.getNearbyCacheKey(query);
    const cached = await this.redis.get(cacheKey);

    if (cached) {
      return JSON.parse(cached);
    }

    const rows = await this.queryNearbySalons(query);
    const cursorIndex = query.cursor ? rows.findIndex((row) => row.id === query.cursor) : -1;
    const slicedRows = cursorIndex >= 0 ? rows.slice(cursorIndex + 1) : rows;
    const items = await Promise.all(
      slicedRows.slice(0, query.limit).map(async (row) => ({
        ...row,
        distanceKm: Number(row.distanceKm),
        priceFrom: row.priceFrom,
        isOpen: await this.isSalonOpen(row.id),
      })),
    );
    const result = {
      items,
      pageInfo: {
        hasMore: slicedRows.length > query.limit,
        nextCursor: slicedRows.length > query.limit ? items[items.length - 1]?.id : null,
      },
      meta: {
        radiusKm: query.radius,
        sort: query.sort,
        mapProvider: 'MAP_PROVIDER_PLACEHOLDER: replace straight-line distance with distance matrix API when configured.',
      },
    };

    await this.redis.set(cacheKey, JSON.stringify(result), 'EX', 120);

    return result;
  }

  async findFeatured() {
    return this.prisma.salon.findMany({
      where: {
        seller: {
          status: SellerStatus.ACTIVE,
          isFeatured: true,
          featuredExpiry: { gt: new Date() },
        },
      },
      orderBy: {
        seller: { featuredExpiry: 'desc' },
      },
      include: {
        seller: {
          select: {
            id: true,
            shopName: true,
            sellerType: true,
            isFeatured: true,
            featuredExpiry: true,
          },
        },
        services: {
          where: { isActive: true },
          orderBy: { price: 'asc' },
          take: 1,
          select: { id: true, name: true, category: true, gender: true, price: true },
        },
      },
    });
  }

  async findOne(id: string) {
    const salon = await this.prisma.salon.findFirst({
      where: {
        id,
        seller: { status: SellerStatus.ACTIVE },
      },
      include: {
        seller: {
          select: {
            id: true,
            shopName: true,
            ownerName: true,
            sellerType: true,
            gstNumber: true,
            isFeatured: true,
            featuredExpiry: true,
          },
        },
        workingHours: {
          include: { breaks: true },
          orderBy: { dayOfWeek: 'asc' },
        },
        services: {
          where: { isActive: true },
          orderBy: [{ category: 'asc' }, { price: 'asc' }],
        },
        staff: {
          where: { isAvailable: true },
          include: {
            services: {
              include: {
                service: {
                  select: { id: true, name: true, category: true },
                },
              },
            },
          },
        },
      },
    });

    if (!salon) {
      throw new NotFoundException('Salon not found.');
    }

    return {
      ...salon,
      isOpen: await this.isSalonOpen(salon.id),
    };
  }

  async findServices(id: string, query: SalonServicesQueryDto) {
    await this.ensureSalonExists(id);

    return this.prisma.service.findMany({
      where: {
        salonId: id,
        isActive: true,
        ...(query.gender ? { gender: { in: [query.gender, Gender.UNISEX] } } : {}),
      },
      orderBy: [{ category: 'asc' }, { price: 'asc' }],
    });
  }

  async findStaff(id: string) {
    await this.ensureSalonExists(id);

    return this.prisma.staff.findMany({
      where: { salonId: id, isAvailable: true },
      include: {
        services: {
          include: {
            service: {
              select: { id: true, name: true, category: true, gender: true, durationMinutes: true },
            },
          },
        },
      },
      orderBy: { name: 'asc' },
    });
  }

  async findReviews(id: string, query: SalonReviewsQueryDto) {
    await this.ensureSalonExists(id);
    const limit = query.limit ?? 20;
    const reviews = await this.prisma.review.findMany({
      where: { salonId: id },
      take: limit + 1,
      ...(query.cursor ? { cursor: { id: query.cursor }, skip: 1 } : {}),
      orderBy: [{ createdAt: 'desc' }, { id: 'desc' }],
      include: {
        user: {
          select: { id: true, name: true, avatar: true },
        },
      },
    });
    const hasMore = reviews.length > limit;
    const items = hasMore ? reviews.slice(0, limit) : reviews;

    return {
      items,
      pageInfo: {
        hasMore,
        nextCursor: hasMore ? items[items.length - 1]?.id : null,
      },
    };
  }

  private async queryNearbySalons(query: SalonListQueryDto) {
    const distanceSql = Prisma.sql`(
      6371 * acos(
        LEAST(1, GREATEST(-1,
          cos(radians(${query.lat})) *
          cos(radians("Salon"."lat")) *
          cos(radians("Salon"."lng") - radians(${query.lng})) +
          sin(radians(${query.lat})) *
          sin(radians("Salon"."lat"))
        ))
      )
    )`;
    const filters: Prisma.Sql[] = [
      Prisma.sql`"Seller"."status" = ${SellerStatus.ACTIVE}::"SellerStatus"`,
      Prisma.sql`${distanceSql} <= ${query.radius}`,
    ];

    if (query.minRating !== undefined) {
      filters.push(Prisma.sql`"Salon"."averageRating" >= ${query.minRating}`);
    }

    if (query.homeServiceOnly) {
      filters.push(Prisma.sql`"Salon"."offersHomeService" = true`);
    }

    if (query.gender) {
      filters.push(Prisma.sql`EXISTS (
        SELECT 1 FROM "Service"
        WHERE "Service"."salonId" = "Salon"."id"
        AND "Service"."isActive" = true
        AND "Service"."gender" IN (${query.gender}::"Gender", ${Gender.UNISEX}::"Gender")
      )`);
    }

    if (query.serviceCategory) {
      filters.push(Prisma.sql`EXISTS (
        SELECT 1 FROM "Service"
        WHERE "Service"."salonId" = "Salon"."id"
        AND "Service"."isActive" = true
        AND "Service"."category" = ${query.serviceCategory}::"ServiceCategory"
      )`);
    }

    const orderBy =
      query.sort === 'rating'
        ? Prisma.raw('"Salon"."averageRating" DESC, "Salon"."totalReviews" DESC, "distanceKm" ASC')
        : query.sort === 'price'
          ? Prisma.raw('"priceFrom" ASC NULLS LAST, "distanceKm" ASC')
          : Prisma.raw('"distanceKm" ASC');

    return this.prisma.$queryRaw<NearbySalonRow[]>(Prisma.sql`
      SELECT
        "Salon"."id",
        "Salon"."name",
        "Salon"."description",
        "Salon"."addressText",
        "Salon"."lat",
        "Salon"."lng",
        "Salon"."photos",
        "Salon"."averageRating",
        "Salon"."totalReviews",
        "Salon"."offersHomeService",
        "Seller"."isFeatured",
        "Seller"."featuredExpiry",
        ${distanceSql} AS "distanceKm",
        service_prices."priceFrom"
      FROM "Salon"
      INNER JOIN "Seller" ON "Seller"."id" = "Salon"."sellerId"
      LEFT JOIN LATERAL (
        SELECT MIN("Service"."price") AS "priceFrom"
        FROM "Service"
        WHERE "Service"."salonId" = "Salon"."id"
        AND "Service"."isActive" = true
      ) service_prices ON true
      WHERE ${Prisma.join(filters, ' AND ')}
      ORDER BY ${orderBy}
      LIMIT ${Math.max((query.limit ?? 20) * 3, 50)}
    `);
  }

  private async ensureSalonExists(id: string) {
    const salon = await this.prisma.salon.findFirst({
      where: { id, seller: { status: SellerStatus.ACTIVE } },
      select: { id: true },
    });

    if (!salon) {
      throw new NotFoundException('Salon not found.');
    }
  }

  private async isSalonOpen(salonId: string) {
    const nowInIst = this.getIstNowParts();
    const workingHours = await this.prisma.workingHours.findUnique({
      where: {
        salonId_dayOfWeek: {
          salonId,
          dayOfWeek: nowInIst.dayOfWeek,
        },
      },
      include: { breaks: true },
    });

    if (!workingHours?.isOpen) {
      return false;
    }

    const nowMinutes = nowInIst.hour * 60 + nowInIst.minute;
    const openMinutes = this.timeToMinutes(workingHours.openTime);
    const closeMinutes = this.timeToMinutes(workingHours.closeTime);
    const inBusinessHours = nowMinutes >= openMinutes && nowMinutes < closeMinutes;
    const inBreak = workingHours.breaks.some((breakItem) => {
      const start = this.timeToMinutes(breakItem.startTime);
      const end = this.timeToMinutes(breakItem.endTime);
      return nowMinutes >= start && nowMinutes < end;
    });

    return inBusinessHours && !inBreak;
  }

  private getIstNowParts() {
    const parts = new Intl.DateTimeFormat('en-US', {
      timeZone: 'Asia/Kolkata',
      weekday: 'short',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    }).formatToParts(new Date());
    const weekday = parts.find((part) => part.type === 'weekday')?.value ?? 'Sun';
    const hour = Number(parts.find((part) => part.type === 'hour')?.value ?? 0);
    const minute = Number(parts.find((part) => part.type === 'minute')?.value ?? 0);
    const dayMap: Record<string, number> = {
      Sun: 0,
      Mon: 1,
      Tue: 2,
      Wed: 3,
      Thu: 4,
      Fri: 5,
      Sat: 6,
    };

    return { dayOfWeek: dayMap[weekday] ?? 0, hour, minute };
  }

  private timeToMinutes(time: string) {
    const [hour, minute] = time.split(':').map(Number);

    return hour * 60 + minute;
  }

  private getNearbyCacheKey(query: SalonListQueryDto) {
    const filterHash = createHash('sha1')
      .update(
        JSON.stringify({
          gender: query.gender,
          serviceCategory: query.serviceCategory,
          minRating: query.minRating,
          homeServiceOnly: query.homeServiceOnly,
          sort: query.sort,
          cursor: query.cursor,
          limit: query.limit,
        }),
      )
      .digest('hex');

    return `salons:nearby:${query.lat}:${query.lng}:${query.radius}:${filterHash}`;
  }
}
