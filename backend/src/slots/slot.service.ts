import { BadRequestException, ConflictException, Injectable, NotFoundException } from '@nestjs/common';
import { BookingStatus, SellerStatus } from '@prisma/client';
import { createHash } from 'crypto';
import { PrismaService } from '../prisma/prisma.service';
import { RedisService } from '../redis/redis.service';

interface TimeRange {
  startMinute: number;
  endMinute: number;
}

interface CachedWorkingHours {
  dayOfWeek: number;
  isOpen: boolean;
  openTime: string;
  closeTime: string;
  breaks: Array<{ startTime: string; endTime: string }>;
}

@Injectable()
export class SlotService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly redis: RedisService,
  ) {}

  async getAvailableSlots(
    salonId: string,
    date: string,
    serviceIds: string[],
    staffId?: string,
  ) {
    this.assertDateFormat(date);
    const sortedServiceIds = [...new Set(serviceIds)].sort();
    const serviceHash = this.hashServiceIds(sortedServiceIds);
    const staffKey = staffId ?? 'any';
    const cacheKey = `slots:${salonId}:${date}:${staffKey}:${serviceHash}`;
    const cached = await this.redis.get(cacheKey);

    if (cached) {
      return JSON.parse(cached);
    }

    const salon = await this.prisma.salon.findFirst({
      where: {
        id: salonId,
        seller: { status: SellerStatus.ACTIVE },
      },
      select: { id: true },
    });

    if (!salon) {
      throw new NotFoundException('Salon not found.');
    }

    if (staffId) {
      await this.ensureStaffBelongsToSalon(salonId, staffId);
    }

    const durationMinutes = await this.getTotalDurationMinutes(salonId, sortedServiceIds);
    const workingHours = await this.getWorkingHoursForDate(salonId, date);
    const slots = workingHours
      ? await this.buildSlots(salonId, date, workingHours, durationMinutes, staffId)
      : [];
    const result = {
      date,
      durationMinutes,
      staffId: staffId ?? null,
      slots,
    };

    await this.redis.set(cacheKey, JSON.stringify(result), 'EX', 30);

    return result;
  }

  async lockSlot(
    salonId: string,
    date: string,
    startMinute: number,
    durationMinutes: number,
    userId: string,
  ) {
    this.assertDateFormat(date);
    const key = this.getSlotLockKey(salonId, date, startMinute);
    const locked = await this.redis.set(
      key,
      JSON.stringify({ userId, lockedAt: new Date().toISOString(), durationMinutes }),
      'EX',
      600,
      'NX',
    );

    if (locked !== 'OK') {
      throw new ConflictException('Slot just taken, please pick another');
    }

    await this.invalidateSlotCache(salonId, date);

    return {
      locked: true,
      key,
      expiresIn: 600,
    };
  }

  async releaseSlot(salonId: string, date: string, startMinute: number) {
    this.assertDateFormat(date);
    await this.redis.del(this.getSlotLockKey(salonId, date, startMinute));
    await this.invalidateSlotCache(salonId, date);

    return { released: true };
  }

  async confirmSlot(salonId: string, date: string, startMinute: number) {
    this.assertDateFormat(date);
    await this.redis.del(this.getSlotLockKey(salonId, date, startMinute));
    await this.invalidateSlotCache(salonId, date);

    return { confirmed: true };
  }

  private async buildSlots(
    salonId: string,
    date: string,
    workingHours: CachedWorkingHours,
    durationMinutes: number,
    staffId?: string,
  ) {
    if (!workingHours.isOpen) {
      return [];
    }

    const openMinute = this.timeToMinutes(workingHours.openTime);
    const closeMinute = this.timeToMinutes(workingHours.closeTime);
    const breakRanges = workingHours.breaks.map((breakItem) => ({
      startMinute: this.timeToMinutes(breakItem.startTime),
      endMinute: this.timeToMinutes(breakItem.endTime),
    }));
    const bookingRanges = await this.getConfirmedBookingRanges(salonId, date, staffId);
    const lockedRanges = await this.getLockedSlotRanges(salonId, date);
    const blockedRanges = [...breakRanges, ...bookingRanges, ...lockedRanges];
    const slots: Array<{ startTime: string; endTime: string; available: boolean }> = [];

    for (let startMinute = openMinute; startMinute + durationMinutes <= closeMinute; startMinute += 15) {
      const endMinute = startMinute + durationMinutes;
      const candidate = { startMinute, endMinute };
      const available = !blockedRanges.some((range) => this.rangesOverlap(candidate, range));

      slots.push({
        startTime: this.minutesToTime(startMinute),
        endTime: this.minutesToTime(endMinute),
        available,
      });
    }

    return slots;
  }

  private async getTotalDurationMinutes(salonId: string, serviceIds: string[]) {
    const services = await this.prisma.service.findMany({
      where: {
        id: { in: serviceIds },
        salonId,
        isActive: true,
      },
      select: { id: true, durationMinutes: true },
    });

    if (services.length !== serviceIds.length) {
      throw new BadRequestException('All serviceIds must belong to the salon and be active.');
    }

    return services.reduce((total, service) => total + service.durationMinutes, 0);
  }

  private async getWorkingHoursForDate(salonId: string, date: string) {
    const hours = await this.getCachedWorkingHours(salonId);
    const dayOfWeek = this.getDayOfWeek(date);

    return hours.find((item) => item.dayOfWeek === dayOfWeek);
  }

  private async getCachedWorkingHours(salonId: string): Promise<CachedWorkingHours[]> {
    const key = `hours:${salonId}`;
    const cached = await this.redis.get(key);

    if (cached) {
      return JSON.parse(cached);
    }

    const workingHours = await this.prisma.workingHours.findMany({
      where: { salonId },
      include: { breaks: true },
      orderBy: { dayOfWeek: 'asc' },
    });
    const normalized = workingHours.map((item) => ({
      dayOfWeek: item.dayOfWeek,
      isOpen: item.isOpen,
      openTime: item.openTime,
      closeTime: item.closeTime,
      breaks: item.breaks.map((breakItem) => ({
        startTime: breakItem.startTime,
        endTime: breakItem.endTime,
      })),
    }));

    await this.redis.set(key, JSON.stringify(normalized), 'EX', 3600);

    return normalized;
  }

  private async getConfirmedBookingRanges(salonId: string, date: string, staffId?: string) {
    const { startUtc, endUtc } = this.getIstDateRangeUtc(date);
    const bookings = await this.prisma.booking.findMany({
      where: {
        salonId,
        status: BookingStatus.CONFIRMED,
        scheduledAt: {
          gte: startUtc,
          lt: endUtc,
        },
        ...(staffId ? { staffId } : {}),
      },
      select: {
        scheduledAt: true,
        durationMinutes: true,
      },
    });

    return bookings.map((booking) => {
      const startMinute = this.getIstMinutesOfDay(booking.scheduledAt);

      return {
        startMinute,
        endMinute: startMinute + booking.durationMinutes,
      };
    });
  }

  private async getLockedSlotRanges(salonId: string, date: string) {
    const keys = await this.redis.keys(`slot_lock:${salonId}:${date}:*`);
    const ranges: TimeRange[] = [];

    for (const key of keys) {
      const startMinute = Number(key.split(':').at(-1));
      const payload = await this.redis.get(key);
      const durationMinutes = this.getLockedDurationMinutes(payload);

      if (Number.isFinite(startMinute)) {
        ranges.push({
          startMinute,
          endMinute: startMinute + durationMinutes,
        });
      }
    }

    return ranges;
  }

  private async ensureStaffBelongsToSalon(salonId: string, staffId: string) {
    const staff = await this.prisma.staff.findFirst({
      where: { id: staffId, salonId, isAvailable: true },
      select: { id: true },
    });

    if (!staff) {
      throw new BadRequestException('staffId must belong to the salon and be available.');
    }
  }

  private getSlotLockKey(salonId: string, date: string, startMinute: number) {
    return `slot_lock:${salonId}:${date}:${startMinute}`;
  }

  private async invalidateSlotCache(salonId: string, date: string) {
    const keys = await this.redis.keys(`slots:${salonId}:${date}:*`);

    if (keys.length > 0) {
      await this.redis.del(...keys);
    }
  }

  private rangesOverlap(left: TimeRange, right: TimeRange) {
    return left.startMinute < right.endMinute && right.startMinute < left.endMinute;
  }

  private timeToMinutes(time: string) {
    const [hour, minute] = time.split(':').map(Number);

    return hour * 60 + minute;
  }

  private minutesToTime(minutes: number) {
    const hour = Math.floor(minutes / 60);
    const minute = minutes % 60;

    return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
  }

  private getDayOfWeek(date: string) {
    const [year, month, day] = date.split('-').map(Number);

    return new Date(Date.UTC(year, month - 1, day)).getUTCDay();
  }

  private getIstDateRangeUtc(date: string) {
    const startUtc = new Date(`${date}T00:00:00+05:30`);
    const endUtc = new Date(startUtc.getTime() + 24 * 60 * 60 * 1000);

    return { startUtc, endUtc };
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

  private getLockedDurationMinutes(payload: string | null) {
    if (!payload) {
      return 30;
    }

    try {
      const parsed = JSON.parse(payload);
      const durationMinutes = Number(parsed.durationMinutes ?? 30);

      return Number.isFinite(durationMinutes) && durationMinutes > 0 ? durationMinutes : 30;
    } catch {
      return 30;
    }
  }

  private assertDateFormat(date: string) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) {
      throw new BadRequestException('date must be in YYYY-MM-DD format.');
    }
  }

  private hashServiceIds(serviceIds: string[]) {
    return createHash('sha1').update(serviceIds.join('|')).digest('hex');
  }
}
