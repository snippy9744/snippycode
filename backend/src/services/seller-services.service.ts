import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateServiceDto } from './dto/create-service.dto';
import { UpdateServiceDto } from './dto/update-service.dto';

@Injectable()
export class SellerServicesService {
  constructor(private readonly prisma: PrismaService) {}

  async findAll(sellerId: string) {
    const salon = await this.getSellerSalon(sellerId);

    return this.prisma.service.findMany({
      where: { salonId: salon.id },
      orderBy: [{ isActive: 'desc' }, { category: 'asc' }, { name: 'asc' }],
    });
  }

  async create(sellerId: string, dto: CreateServiceDto) {
    const salon = await this.getSellerSalon(sellerId);
    const homePriceMultiplier = await this.getHomePriceMultiplier();

    const service = await this.prisma.service.create({
      data: {
        salonId: salon.id,
        name: dto.name,
        category: dto.category,
        gender: dto.gender,
        durationMinutes: dto.durationMinutes,
        price: dto.price,
        homePriceMultiplier,
        isHomeAvailable: dto.isHomeAvailable,
      },
    });

    await this.syncSalonHomeServiceFlag(salon.id);

    return {
      ...service,
      homePrice: dto.isHomeAvailable ? Number(dto.price) * homePriceMultiplier : null,
    };
  }

  async update(sellerId: string, serviceId: string, dto: UpdateServiceDto) {
    const salon = await this.getSellerSalon(sellerId);
    await this.ensureServiceBelongsToSalon(serviceId, salon.id);
    const homePriceMultiplier =
      dto.isHomeAvailable !== undefined ? await this.getHomePriceMultiplier() : undefined;
    const service = await this.prisma.service.update({
      where: { id: serviceId },
      data: {
        ...(dto.name !== undefined ? { name: dto.name } : {}),
        ...(dto.category !== undefined ? { category: dto.category } : {}),
        ...(dto.gender !== undefined ? { gender: dto.gender } : {}),
        ...(dto.durationMinutes !== undefined ? { durationMinutes: dto.durationMinutes } : {}),
        ...(dto.price !== undefined ? { price: dto.price } : {}),
        ...(dto.isHomeAvailable !== undefined
          ? {
              isHomeAvailable: dto.isHomeAvailable,
              ...(homePriceMultiplier ? { homePriceMultiplier } : {}),
            }
          : {}),
      },
    });

    await this.syncSalonHomeServiceFlag(salon.id);

    return service;
  }

  async softDelete(sellerId: string, serviceId: string) {
    const salon = await this.getSellerSalon(sellerId);
    await this.ensureServiceBelongsToSalon(serviceId, salon.id);
    const service = await this.prisma.service.update({
      where: { id: serviceId },
      data: { isActive: false },
    });

    await this.syncSalonHomeServiceFlag(salon.id);

    return service;
  }

  async toggle(sellerId: string, serviceId: string) {
    const salon = await this.getSellerSalon(sellerId);
    const service = await this.ensureServiceBelongsToSalon(serviceId, salon.id);
    const updatedService = await this.prisma.service.update({
      where: { id: serviceId },
      data: { isActive: !service.isActive },
    });

    await this.syncSalonHomeServiceFlag(salon.id);

    return updatedService;
  }

  private async getSellerSalon(sellerId: string) {
    const salon = await this.prisma.salon.findUnique({
      where: { sellerId },
      select: { id: true },
    });

    if (!salon) {
      throw new BadRequestException('Complete seller location before managing services.');
    }

    return salon;
  }

  private async ensureServiceBelongsToSalon(serviceId: string, salonId: string) {
    const service = await this.prisma.service.findFirst({
      where: { id: serviceId, salonId },
    });

    if (!service) {
      throw new NotFoundException('Service not found.');
    }

    return service;
  }

  private async getHomePriceMultiplier() {
    const config = await this.prisma.adminConfig.findFirst({
      select: { homePriceMultiplier: true },
    });

    return config?.homePriceMultiplier ?? 1.6;
  }

  private async syncSalonHomeServiceFlag(salonId: string) {
    const hasHomeService = await this.prisma.service.count({
      where: { salonId, isActive: true, isHomeAvailable: true },
    });

    await this.prisma.salon.update({
      where: { id: salonId },
      data: { offersHomeService: hasHomeService > 0 },
    });
  }
}
