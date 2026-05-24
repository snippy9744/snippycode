import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateStaffDto } from './dto/create-staff.dto';
import { UpdateStaffDto } from './dto/update-staff.dto';

@Injectable()
export class SellerStaffService {
  constructor(private readonly prisma: PrismaService) {}

  async findAll(sellerId: string) {
    const salon = await this.getSellerSalon(sellerId);

    return this.prisma.staff.findMany({
      where: { salonId: salon.id },
      include: {
        services: {
          include: {
            service: {
              select: { id: true, name: true, category: true, gender: true, durationMinutes: true },
            },
          },
        },
      },
      orderBy: [{ isAvailable: 'desc' }, { name: 'asc' }],
    });
  }

  async create(sellerId: string, dto: CreateStaffDto) {
    const salon = await this.getSellerSalon(sellerId);
    await this.ensureServicesBelongToSalon(dto.serviceIds, salon.id);

    return this.prisma.staff.create({
      data: {
        salonId: salon.id,
        name: dto.name,
        phone: dto.phone,
        photo: dto.photo,
        speciality: dto.speciality,
        services: {
          create: dto.serviceIds.map((serviceId) => ({ serviceId })),
        },
      },
      include: {
        services: { include: { service: true } },
      },
    });
  }

  async update(sellerId: string, staffId: string, dto: UpdateStaffDto) {
    const salon = await this.getSellerSalon(sellerId);
    await this.ensureStaffBelongsToSalon(staffId, salon.id);

    if (dto.serviceIds) {
      await this.ensureServicesBelongToSalon(dto.serviceIds, salon.id);
    }

    return this.prisma.$transaction(async (tx) => {
      if (dto.serviceIds) {
        await tx.staffService.deleteMany({ where: { staffId } });
      }

      return tx.staff.update({
        where: { id: staffId },
        data: {
          ...(dto.name !== undefined ? { name: dto.name } : {}),
          ...(dto.phone !== undefined ? { phone: dto.phone } : {}),
          ...(dto.photo !== undefined ? { photo: dto.photo } : {}),
          ...(dto.speciality !== undefined ? { speciality: dto.speciality } : {}),
          ...(dto.serviceIds
            ? { services: { create: dto.serviceIds.map((serviceId) => ({ serviceId })) } }
            : {}),
        },
        include: {
          services: { include: { service: true } },
        },
      });
    });
  }

  async delete(sellerId: string, staffId: string) {
    const salon = await this.getSellerSalon(sellerId);
    await this.ensureStaffBelongsToSalon(staffId, salon.id);

    return this.prisma.staff.delete({
      where: { id: staffId },
    });
  }

  async toggleAvailability(sellerId: string, staffId: string) {
    const salon = await this.getSellerSalon(sellerId);
    const staff = await this.ensureStaffBelongsToSalon(staffId, salon.id);

    return this.prisma.staff.update({
      where: { id: staffId },
      data: { isAvailable: !staff.isAvailable },
    });
  }

  private async getSellerSalon(sellerId: string) {
    const salon = await this.prisma.salon.findUnique({
      where: { sellerId },
      select: { id: true },
    });

    if (!salon) {
      throw new BadRequestException('Complete seller location before managing staff.');
    }

    return salon;
  }

  private async ensureStaffBelongsToSalon(staffId: string, salonId: string) {
    const staff = await this.prisma.staff.findFirst({
      where: { id: staffId, salonId },
    });

    if (!staff) {
      throw new NotFoundException('Staff member not found.');
    }

    return staff;
  }

  private async ensureServicesBelongToSalon(serviceIds: string[], salonId: string) {
    if (serviceIds.length === 0) {
      return;
    }

    const servicesCount = await this.prisma.service.count({
      where: {
        id: { in: serviceIds },
        salonId,
        isActive: true,
      },
    });

    if (servicesCount !== new Set(serviceIds).size) {
      throw new BadRequestException('All staff service IDs must belong to this seller salon.');
    }
  }
}
