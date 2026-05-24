import { BadRequestException, Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { RedisService } from '../redis/redis.service';
import { WorkingHoursDto } from './dto/working-hours.dto';

@Injectable()
export class SellerWorkingHoursService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly redis: RedisService,
  ) {}

  async findAll(sellerId: string) {
    const salon = await this.getSellerSalon(sellerId);

    return this.prisma.workingHours.findMany({
      where: { salonId: salon.id },
      include: { breaks: true },
      orderBy: { dayOfWeek: 'asc' },
    });
  }

  async replace(sellerId: string, workingHours: WorkingHoursDto[]) {
    const salon = await this.getSellerSalon(sellerId);
    const uniqueDays = new Set(workingHours.map((day) => day.dayOfWeek));

    if (uniqueDays.size !== workingHours.length) {
      throw new BadRequestException('Each dayOfWeek can only appear once.');
    }

    await this.prisma.$transaction(async (tx) => {
      await tx.workingHours.deleteMany({ where: { salonId: salon.id } });

      for (const day of workingHours) {
        await tx.workingHours.create({
          data: {
            salonId: salon.id,
            dayOfWeek: day.dayOfWeek,
            isOpen: day.isOpen,
            openTime: day.openTime,
            closeTime: day.closeTime,
            breaks: {
              create: (day.breaks ?? []).map((breakItem) => ({
                startTime: breakItem.start,
                endTime: breakItem.end,
              })),
            },
          },
        });
      }
    });

    await this.redis.del(`hours:${salon.id}`);

    return this.findAll(sellerId);
  }

  private async getSellerSalon(sellerId: string) {
    const salon = await this.prisma.salon.findUnique({
      where: { sellerId },
      select: { id: true },
    });

    if (!salon) {
      throw new BadRequestException('Complete seller location before managing working hours.');
    }

    return salon;
  }
}
