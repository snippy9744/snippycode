import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import {
  Role,
  Seller,
  SellerStatus,
  SellerType,
  SubscriptionStatus,
} from '@prisma/client';
import { NotificationsService } from '../notifications/notifications.service';
import { PrismaService } from '../prisma/prisma.service';
import { OnboardBasicDto } from './dto/onboard-basic.dto';
import { OnboardDocumentsDto } from './dto/onboard-documents.dto';
import { OnboardLocationDto } from './dto/onboard-location.dto';
import { UpdateSellerProfileDto } from './dto/update-seller-profile.dto';
import { WorkingHoursDto } from './dto/working-hours.dto';
import { S3UploadService } from './s3-upload.service';
import { SellerDocumentFiles } from './types/seller-document-files.type';

const DEFAULT_SELLER_TRIAL_DAYS = 30;
type SellerWithSalon = Seller & { salon: { id: string } | null };

@Injectable()
export class SellersService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly s3UploadService: S3UploadService,
    private readonly notificationsService: NotificationsService,
  ) {}

  async onboardBasic(userId: string, dto: OnboardBasicDto) {
    const seller = await this.prisma.seller.upsert({
      where: { userId },
      update: {
        shopName: dto.shopName,
        ownerName: dto.ownerName,
        sellerType: dto.sellerType,
        status: SellerStatus.PENDING_APPROVAL,
      },
      create: {
        userId,
        shopName: dto.shopName,
        ownerName: dto.ownerName,
        sellerType: dto.sellerType,
        status: SellerStatus.PENDING_APPROVAL,
      },
      include: {
        user: true,
        salon: true,
      },
    });

    await this.prisma.user.update({
      where: { id: userId },
      data: {
        role: Role.SELLER,
        name: dto.ownerName,
      },
    });

    return seller;
  }

  async onboardLocation(userId: string, dto: OnboardLocationDto) {
    const seller = await this.getSellerForUser(userId);

    if (seller.sellerType === SellerType.HOME_SERVICE && !dto.serviceRadius) {
      throw new BadRequestException('serviceRadius is required for home service sellers.');
    }

    return this.prisma.salon.upsert({
      where: { sellerId: seller.id },
      update: {
        name: seller.shopName,
        addressText: dto.addressText,
        lat: dto.lat,
        lng: dto.lng,
        serviceRadius: dto.serviceRadius,
        offersHomeService: seller.sellerType === SellerType.HOME_SERVICE,
      },
      create: {
        sellerId: seller.id,
        name: seller.shopName,
        addressText: dto.addressText,
        lat: dto.lat,
        lng: dto.lng,
        serviceRadius: dto.serviceRadius,
        offersHomeService: seller.sellerType === SellerType.HOME_SERVICE,
      },
      include: {
        seller: {
          select: {
            id: true,
            shopName: true,
            ownerName: true,
            sellerType: true,
            status: true,
          },
        },
      },
    });
  }

  async onboardDocuments(
    userId: string,
    dto: OnboardDocumentsDto,
    files: SellerDocumentFiles,
  ) {
    const seller = await this.getSellerForUser(userId);

    if (seller.sellerType === SellerType.SHOP) {
      if (!dto.gstNumber) {
        throw new BadRequestException('gstNumber is required for shop sellers.');
      }

      const shopPhotos = files.shopPhotos ?? [];
      if (shopPhotos.length < 2 || shopPhotos.length > 10) {
        throw new BadRequestException('Shop sellers must upload between 2 and 10 shop photos.');
      }

      const shopPhotoUrls = await Promise.all(
        shopPhotos.map((file) =>
          this.s3UploadService.uploadSellerFile(seller.id, 'shop-photos', file),
        ),
      );

      return this.prisma.seller.update({
        where: { id: seller.id },
        data: {
          gstNumber: dto.gstNumber,
          shopPhotos: shopPhotoUrls,
        },
        include: { salon: true },
      });
    }

    const aadhaarFront = files.aadhaarFront?.[0];
    const aadhaarBack = files.aadhaarBack?.[0];
    const aadhaarSelfie = files.aadhaarSelfie?.[0];

    if (!aadhaarFront || !aadhaarBack || !aadhaarSelfie) {
      throw new BadRequestException(
        'Home service sellers must upload aadhaarFront, aadhaarBack, and aadhaarSelfie.',
      );
    }

    const [aadhaarFrontUrl, aadhaarBackUrl, aadhaarSelfieUrl] = await Promise.all([
      this.s3UploadService.uploadSellerFile(seller.id, 'aadhaar-front', aadhaarFront),
      this.s3UploadService.uploadSellerFile(seller.id, 'aadhaar-back', aadhaarBack),
      this.s3UploadService.uploadSellerFile(seller.id, 'aadhaar-selfie', aadhaarSelfie),
    ]);

    return this.prisma.seller.update({
      where: { id: seller.id },
      data: {
        aadhaarFront: aadhaarFrontUrl,
        aadhaarBack: aadhaarBackUrl,
        aadhaarSelfie: aadhaarSelfieUrl,
      },
      include: { salon: true },
    });
  }

  async completeOnboarding(userId: string) {
    const seller = await this.getSellerWithSalonForUser(userId);
    this.validateOnboardingComplete(seller);
    const trialDays = await this.getSellerTrialDays();
    const subscriptionExpiry = new Date(Date.now() + trialDays * 24 * 60 * 60 * 1000);
    const status =
      seller.sellerType === SellerType.SHOP
        ? SellerStatus.ACTIVE
        : SellerStatus.PENDING_APPROVAL;

    const updatedSeller = await this.prisma.seller.update({
      where: { id: seller.id },
      data: {
        status,
        subscriptionStatus: SubscriptionStatus.TRIAL,
        subscriptionExpiry,
        onboardingCompletedAt: new Date(),
      },
      include: {
        user: true,
        salon: true,
      },
    });

    if (seller.sellerType === SellerType.HOME_SERVICE) {
      await this.notifyAdminsAboutHomeServiceSeller(updatedSeller);
    }

    await this.notificationsService.scheduleSellerSubscriptionExpiring(
      updatedSeller.id,
      updatedSeller.userId,
      subscriptionExpiry,
    );

    return updatedSeller;
  }

  async getProfile(userId: string) {
    const seller = await this.prisma.seller.findUnique({
      where: { userId },
      include: {
        user: {
          select: {
            id: true,
            name: true,
            phone: true,
            email: true,
            avatar: true,
            role: true,
            status: true,
          },
        },
        salon: {
          include: {
            workingHours: {
              include: { breaks: true },
              orderBy: { dayOfWeek: 'asc' },
            },
          },
        },
      },
    });

    if (!seller) {
      throw new NotFoundException('Seller profile not found.');
    }

    return seller;
  }

  async updateProfile(userId: string, dto: UpdateSellerProfileDto) {
    const seller = await this.getSellerWithSalonForUser(userId);
    const salonId = seller.salon?.id;

    if (!salonId) {
      throw new BadRequestException('Complete seller location before updating shop profile.');
    }

    if (dto.shopName) {
      await this.prisma.seller.update({
        where: { id: seller.id },
        data: { shopName: dto.shopName },
      });
    }

    await this.prisma.salon.update({
      where: { id: salonId },
      data: {
        ...(dto.shopName ? { name: dto.shopName } : {}),
        ...(dto.description !== undefined ? { description: dto.description } : {}),
        ...(dto.photos ? { photos: dto.photos } : {}),
      },
    });

    if (dto.workingHours) {
      await this.replaceWorkingHours(salonId, dto.workingHours);
    }

    return this.getProfile(userId);
  }

  private async getSellerForUser(userId: string) {
    const seller = await this.prisma.seller.findUnique({
      where: { userId },
    });

    if (!seller) {
      throw new NotFoundException('Create seller basic profile first.');
    }

    return seller;
  }

  private async getSellerWithSalonForUser(userId: string): Promise<SellerWithSalon> {
    const seller = await this.prisma.seller.findUnique({
      where: { userId },
      include: { salon: { select: { id: true } } },
    });

    if (!seller) {
      throw new NotFoundException('Create seller basic profile first.');
    }

    return seller;
  }

  private validateOnboardingComplete(seller: SellerWithSalon) {
    if (!seller.salon) {
      throw new BadRequestException('Seller location is required before completing onboarding.');
    }

    if (seller.sellerType === SellerType.SHOP) {
      if (!seller.gstNumber || seller.shopPhotos.length < 2) {
        throw new BadRequestException(
          'Shop sellers must submit GST number and at least 2 shop photos.',
        );
      }

      return;
    }

    if (!seller.aadhaarFront || !seller.aadhaarBack || !seller.aadhaarSelfie) {
      throw new BadRequestException(
        'Home service sellers must submit Aadhaar front, back, and selfie photos.',
      );
    }
  }

  private async replaceWorkingHours(salonId: string, workingHours: WorkingHoursDto[]) {
    await this.prisma.$transaction(async (tx) => {
      await tx.workingHours.deleteMany({ where: { salonId } });

      for (const day of workingHours) {
        await tx.workingHours.create({
          data: {
            salonId,
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
  }

  private async getSellerTrialDays() {
    const config = await this.prisma.adminConfig.findFirst({
      select: { sellerTrialDays: true },
    });

    return config?.sellerTrialDays ?? DEFAULT_SELLER_TRIAL_DAYS;
  }

  private async notifyAdminsAboutHomeServiceSeller(seller: Seller) {
    const admins = await this.prisma.user.findMany({
      where: { role: Role.ADMIN },
      select: { id: true },
    });

    await Promise.all(
      admins.map((admin) =>
        this.notificationsService.sendToUser(admin.id, {
          title: 'Home service seller pending approval',
          body: `${seller.shopName} is waiting for Aadhaar verification.`,
          data: {
            type: 'SELLER_PENDING_APPROVAL',
            sellerId: seller.id,
          },
        }),
      ),
    );
  }
}
