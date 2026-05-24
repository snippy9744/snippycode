import { PrismaClient, Gender, Role, SellerStatus, SellerType, ServiceCategory, SubscriptionStatus } from '@prisma/client';
import { randomBytes, scryptSync } from 'crypto';

const prisma = new PrismaClient();

function hashPassword(password: string) {
  const salt = randomBytes(16).toString('hex');
  const hash = scryptSync(password, salt, 64).toString('hex');

  return `scrypt:${salt}:${hash}`;
}

async function seedAdmin() {
  await prisma.user.upsert({
    where: { email: 'admin@snippyseat.in' },
    update: {
      name: 'Snippy Seat Admin',
      role: Role.ADMIN,
      passwordHash: hashPassword('Admin@12345'),
    },
    create: {
      email: 'admin@snippyseat.in',
      name: 'Snippy Seat Admin',
      role: Role.ADMIN,
      passwordHash: hashPassword('Admin@12345'),
    },
  });
}

async function seedConfig() {
  const existing = await prisma.adminConfig.findFirst();

  if (existing) {
    await prisma.adminConfig.update({
      where: { id: existing.id },
      data: defaultAdminConfig(),
    });
    return;
  }

  await prisma.adminConfig.create({ data: defaultAdminConfig() });
}

function defaultAdminConfig() {
  return {
    userCommissionPct: 2.5,
    sellerCommissionPct: 12,
    homeServiceCommissionPct: 15,
    convenienceFeeMin: 10,
    convenienceFeeMax: 30,
    homePriceMultiplier: 1.6,
    travelFeePerKm: 10,
    gstPct: 18,
    additionalTaxLabel: 'Additional local tax',
    additionalTaxPct: 0,
    userPremiumPriceMonthly: 199,
    sellerSubscriptionMonthly: 999,
    sellerTrialDays: 30,
    cancellationWindowMinutes: 15,
    maxWarningsBeforeBlock: 3,
    featuredListingPriceMonthly: 499,
  };
}

async function seedSalon(sample: {
  userEmail: string;
  userPhone: string;
  ownerName: string;
  shopName: string;
  name: string;
  description: string;
  addressText: string;
  lat: number;
  lng: number;
  offersHomeService: boolean;
  photos: string[];
  services: Array<{
    name: string;
    category: ServiceCategory;
    gender: Gender;
    durationMinutes: number;
    price: number;
    isHomeAvailable?: boolean;
  }>;
}) {
  const user = await prisma.user.upsert({
    where: { email: sample.userEmail },
    update: {
      phone: sample.userPhone,
      name: sample.ownerName,
      role: Role.SELLER,
    },
    create: {
      email: sample.userEmail,
      phone: sample.userPhone,
      name: sample.ownerName,
      role: Role.SELLER,
    },
  });

  const seller = await prisma.seller.upsert({
    where: { userId: user.id },
    update: {
      shopName: sample.shopName,
      ownerName: sample.ownerName,
      sellerType: sample.offersHomeService ? SellerType.HOME_SERVICE : SellerType.SHOP,
      status: SellerStatus.ACTIVE,
      subscriptionStatus: SubscriptionStatus.TRIAL,
      subscriptionExpiry: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
      onboardingCompletedAt: new Date(),
    },
    create: {
      userId: user.id,
      shopName: sample.shopName,
      ownerName: sample.ownerName,
      sellerType: sample.offersHomeService ? SellerType.HOME_SERVICE : SellerType.SHOP,
      status: SellerStatus.ACTIVE,
      subscriptionStatus: SubscriptionStatus.TRIAL,
      subscriptionExpiry: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
      onboardingCompletedAt: new Date(),
    },
  });

  const salon = await prisma.salon.upsert({
    where: { sellerId: seller.id },
    update: {
      name: sample.name,
      description: sample.description,
      addressText: sample.addressText,
      lat: sample.lat,
      lng: sample.lng,
      offersHomeService: sample.offersHomeService,
      photos: sample.photos,
      averageRating: 4.6,
      totalReviews: 38,
    },
    create: {
      sellerId: seller.id,
      name: sample.name,
      description: sample.description,
      addressText: sample.addressText,
      lat: sample.lat,
      lng: sample.lng,
      offersHomeService: sample.offersHomeService,
      photos: sample.photos,
      averageRating: 4.6,
      totalReviews: 38,
    },
  });

  await prisma.service.deleteMany({ where: { salonId: salon.id } });
  await prisma.workingHours.deleteMany({ where: { salonId: salon.id } });

  await prisma.service.createMany({
    data: sample.services.map((service) => ({
      salonId: salon.id,
      ...service,
      homePriceMultiplier: 1.6,
      isActive: true,
      isHomeAvailable: service.isHomeAvailable ?? false,
    })),
  });

  for (const dayOfWeek of [0, 1, 2, 3, 4, 5, 6]) {
    await prisma.workingHours.create({
      data: {
        salonId: salon.id,
        dayOfWeek,
        isOpen: dayOfWeek !== 2,
        openTime: '10:00',
        closeTime: '21:00',
        breaks: {
          create: [{ startTime: '14:00', endTime: '14:30' }],
        },
      },
    });
  }
}

async function main() {
  await seedAdmin();
  await seedConfig();
  await seedSalon({
    userEmail: 'owner.bandra@snippyseat.in',
    userPhone: '+919810000001',
    ownerName: 'Aarav Mehta',
    shopName: 'Red Chair Studio',
    name: 'Red Chair Studio Bandra',
    description: 'Premium haircuts, beard care, facials, and express grooming.',
    addressText: 'Linking Road, Bandra West, Mumbai, Maharashtra',
    lat: 19.0607,
    lng: 72.8362,
    offersHomeService: true,
    photos: ['https://images.unsplash.com/photo-1521590832167-7bcbfaa6381f'],
    services: [
      { name: 'Signature Haircut', category: ServiceCategory.HAIRCUT, gender: Gender.UNISEX, durationMinutes: 45, price: 399, isHomeAvailable: true },
      { name: 'Beard Trim', category: ServiceCategory.BEARD_TRIM, gender: Gender.MEN, durationMinutes: 25, price: 199, isHomeAvailable: true },
      { name: 'Hydra Facial', category: ServiceCategory.FACIAL, gender: Gender.UNISEX, durationMinutes: 60, price: 999 },
    ],
  });
  await seedSalon({
    userEmail: 'owner.indiranagar@snippyseat.in',
    userPhone: '+919810000002',
    ownerName: 'Nisha Rao',
    shopName: 'Glow & Snip',
    name: 'Glow & Snip Indiranagar',
    description: 'Women-first salon for cuts, threading, waxing, bridal, and cleanup services.',
    addressText: '100 Feet Road, Indiranagar, Bengaluru, Karnataka',
    lat: 12.9784,
    lng: 77.6408,
    offersHomeService: false,
    photos: ['https://images.unsplash.com/photo-1560066984-138dadb4c035'],
    services: [
      { name: 'Layered Haircut', category: ServiceCategory.HAIRCUT, gender: Gender.WOMEN, durationMinutes: 50, price: 599 },
      { name: 'Threading', category: ServiceCategory.THREADING, gender: Gender.WOMEN, durationMinutes: 20, price: 99 },
      { name: 'Bridal Trial', category: ServiceCategory.BRIDAL, gender: Gender.WOMEN, durationMinutes: 120, price: 2499 },
    ],
  });
}

main()
  .then(async () => {
    await prisma.$disconnect();
  })
  .catch(async (error) => {
    console.error(error);
    await prisma.$disconnect();
    process.exit(1);
  });
