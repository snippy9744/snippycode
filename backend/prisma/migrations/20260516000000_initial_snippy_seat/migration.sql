CREATE TYPE "Role" AS ENUM ('USER', 'SELLER', 'ADMIN');
CREATE TYPE "UserStatus" AS ENUM ('ACTIVE', 'BLOCKED', 'SUSPENDED');
CREATE TYPE "SellerStatus" AS ENUM ('PENDING_APPROVAL', 'ACTIVE', 'BLOCKED', 'SUSPENDED');
CREATE TYPE "SellerType" AS ENUM ('SHOP', 'HOME_SERVICE');
CREATE TYPE "SubscriptionStatus" AS ENUM ('TRIAL', 'ACTIVE', 'EXPIRED', 'CANCELLED');
CREATE TYPE "Gender" AS ENUM ('MEN', 'WOMEN', 'UNISEX');
CREATE TYPE "BookingStatus" AS ENUM ('CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW');
CREATE TYPE "PaymentStatus" AS ENUM ('PENDING', 'PAID', 'REFUNDED', 'PARTIALLY_REFUNDED', 'FAILED');
CREATE TYPE "PaymentMethod" AS ENUM ('ONLINE', 'AT_SHOP');
CREATE TYPE "RefundStatus" AS ENUM ('PENDING', 'PROCESSED', 'FAILED');
CREATE TYPE "CancelledBy" AS ENUM ('USER', 'SELLER', 'ADMIN');
CREATE TYPE "ServiceCategory" AS ENUM (
  'HAIRCUT',
  'SHAVING',
  'BEARD_TRIM',
  'COLORING',
  'SMOOTHENING',
  'STRAIGHTENING',
  'KERATIN',
  'FACIAL',
  'CLEANUP',
  'THREADING',
  'WAXING',
  'BRIDAL',
  'KIDS_CUT',
  'HEAD_MASSAGE',
  'OTHER'
);

CREATE TABLE "User" (
  "id" TEXT NOT NULL,
  "phone" TEXT,
  "email" TEXT,
  "name" TEXT,
  "avatar" TEXT,
  "googleId" TEXT,
  "role" "Role" NOT NULL DEFAULT 'USER',
  "status" "UserStatus" NOT NULL DEFAULT 'ACTIVE',
  "warningCount" INTEGER NOT NULL DEFAULT 0,
  "isPremium" BOOLEAN NOT NULL DEFAULT false,
  "premiumExpiry" TIMESTAMP(3),
  "fcmToken" TEXT,
  "refreshTokenHash" TEXT,
  "refreshTokenExpiresAt" TIMESTAMP(3),
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "User_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "Seller" (
  "id" TEXT NOT NULL,
  "userId" TEXT NOT NULL,
  "shopName" TEXT NOT NULL,
  "ownerName" TEXT NOT NULL,
  "sellerType" "SellerType" NOT NULL,
  "gstNumber" TEXT,
  "aadhaarFront" TEXT,
  "aadhaarBack" TEXT,
  "aadhaarSelfie" TEXT,
  "shopPhotos" TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
  "status" "SellerStatus" NOT NULL DEFAULT 'PENDING_APPROVAL',
  "subscriptionStatus" "SubscriptionStatus" NOT NULL DEFAULT 'TRIAL',
  "subscriptionExpiry" TIMESTAMP(3),
  "isFeatured" BOOLEAN NOT NULL DEFAULT false,
  "featuredExpiry" TIMESTAMP(3),
  "commissionOverride" DOUBLE PRECISION,
  "onboardingCompletedAt" TIMESTAMP(3),
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "Seller_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "Salon" (
  "id" TEXT NOT NULL,
  "sellerId" TEXT NOT NULL,
  "name" TEXT NOT NULL,
  "description" TEXT,
  "addressText" TEXT NOT NULL,
  "lat" DOUBLE PRECISION NOT NULL,
  "lng" DOUBLE PRECISION NOT NULL,
  "serviceRadius" DOUBLE PRECISION,
  "photos" TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
  "averageRating" DOUBLE PRECISION NOT NULL DEFAULT 0,
  "totalReviews" INTEGER NOT NULL DEFAULT 0,
  "offersHomeService" BOOLEAN NOT NULL DEFAULT false,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "Salon_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "Service" (
  "id" TEXT NOT NULL,
  "salonId" TEXT NOT NULL,
  "name" TEXT NOT NULL,
  "category" "ServiceCategory" NOT NULL,
  "gender" "Gender" NOT NULL,
  "durationMinutes" INTEGER NOT NULL,
  "price" DECIMAL(10,2) NOT NULL,
  "homePriceMultiplier" DOUBLE PRECISION NOT NULL DEFAULT 1.6,
  "isActive" BOOLEAN NOT NULL DEFAULT true,
  "isHomeAvailable" BOOLEAN NOT NULL DEFAULT false,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "Service_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "Staff" (
  "id" TEXT NOT NULL,
  "salonId" TEXT NOT NULL,
  "name" TEXT NOT NULL,
  "phone" TEXT,
  "photo" TEXT,
  "speciality" TEXT,
  "isAvailable" BOOLEAN NOT NULL DEFAULT true,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "Staff_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "StaffService" (
  "staffId" TEXT NOT NULL,
  "serviceId" TEXT NOT NULL,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "StaffService_pkey" PRIMARY KEY ("staffId", "serviceId")
);

CREATE TABLE "Booking" (
  "id" TEXT NOT NULL,
  "userId" TEXT NOT NULL,
  "salonId" TEXT NOT NULL,
  "staffId" TEXT,
  "scheduledAt" TIMESTAMP(3) NOT NULL,
  "durationMinutes" INTEGER NOT NULL,
  "status" "BookingStatus" NOT NULL DEFAULT 'CONFIRMED',
  "isHomeService" BOOLEAN NOT NULL DEFAULT false,
  "homeAddress" TEXT,
  "homeLat" DOUBLE PRECISION,
  "homeLng" DOUBLE PRECISION,
  "travelFee" DECIMAL(10,2) NOT NULL DEFAULT 0,
  "subtotal" DECIMAL(10,2) NOT NULL,
  "convenienceFee" DECIMAL(10,2) NOT NULL,
  "taxAmount" DECIMAL(10,2) NOT NULL,
  "platformFee" DECIMAL(10,2) NOT NULL,
  "totalAmount" DECIMAL(10,2) NOT NULL,
  "paymentStatus" "PaymentStatus" NOT NULL DEFAULT 'PENDING',
  "paymentMethod" "PaymentMethod" NOT NULL,
  "paymentGatewayRef" TEXT,
  "commissionAmount" DECIMAL(10,2) NOT NULL,
  "commissionRate" DOUBLE PRECISION NOT NULL,
  "refundStatus" "RefundStatus",
  "refundAmount" DECIMAL(10,2),
  "cancelledAt" TIMESTAMP(3),
  "cancelReason" TEXT,
  "cancelledBy" "CancelledBy",
  "warningIssued" BOOLEAN NOT NULL DEFAULT false,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "Booking_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "BookingService" (
  "id" TEXT NOT NULL,
  "bookingId" TEXT NOT NULL,
  "serviceId" TEXT NOT NULL,
  "serviceName" TEXT NOT NULL,
  "durationMinutes" INTEGER NOT NULL,
  "price" DECIMAL(10,2) NOT NULL,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "BookingService_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "Review" (
  "id" TEXT NOT NULL,
  "bookingId" TEXT NOT NULL,
  "userId" TEXT NOT NULL,
  "salonId" TEXT NOT NULL,
  "rating" INTEGER NOT NULL,
  "comment" TEXT,
  "photos" TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "Review_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "AdminConfig" (
  "id" TEXT NOT NULL,
  "userCommissionPct" DOUBLE PRECISION NOT NULL DEFAULT 2.5,
  "sellerCommissionPct" DOUBLE PRECISION NOT NULL DEFAULT 12.0,
  "homeServiceCommissionPct" DOUBLE PRECISION NOT NULL DEFAULT 15.0,
  "convenienceFeeMin" DECIMAL(10,2) NOT NULL DEFAULT 10,
  "convenienceFeeMax" DECIMAL(10,2) NOT NULL DEFAULT 30,
  "homePriceMultiplier" DOUBLE PRECISION NOT NULL DEFAULT 1.6,
  "travelFeePerKm" DECIMAL(10,2) NOT NULL DEFAULT 10,
  "gstPct" DOUBLE PRECISION NOT NULL DEFAULT 18.0,
  "additionalTaxLabel" TEXT,
  "additionalTaxPct" DOUBLE PRECISION NOT NULL DEFAULT 0,
  "userPremiumPriceMonthly" DECIMAL(10,2) NOT NULL DEFAULT 199,
  "sellerSubscriptionMonthly" DECIMAL(10,2) NOT NULL DEFAULT 999,
  "sellerTrialDays" INTEGER NOT NULL DEFAULT 30,
  "cancellationWindowMinutes" INTEGER NOT NULL DEFAULT 15,
  "maxWarningsBeforeBlock" INTEGER NOT NULL DEFAULT 3,
  "featuredListingPriceMonthly" DECIMAL(10,2) NOT NULL DEFAULT 499,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "AdminConfig_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "WorkingHours" (
  "id" TEXT NOT NULL,
  "salonId" TEXT NOT NULL,
  "dayOfWeek" INTEGER NOT NULL,
  "isOpen" BOOLEAN NOT NULL DEFAULT true,
  "openTime" TEXT NOT NULL,
  "closeTime" TEXT NOT NULL,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "WorkingHours_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "WorkingHoursBreak" (
  "id" TEXT NOT NULL,
  "workingHoursId" TEXT NOT NULL,
  "startTime" TEXT NOT NULL,
  "endTime" TEXT NOT NULL,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "WorkingHoursBreak_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "SavedSalon" (
  "userId" TEXT NOT NULL,
  "salonId" TEXT NOT NULL,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "SavedSalon_pkey" PRIMARY KEY ("userId", "salonId")
);

CREATE UNIQUE INDEX "User_phone_key" ON "User"("phone");
CREATE UNIQUE INDEX "User_email_key" ON "User"("email");
CREATE UNIQUE INDEX "User_googleId_key" ON "User"("googleId");
CREATE INDEX "User_role_idx" ON "User"("role");
CREATE INDEX "User_status_idx" ON "User"("status");
CREATE INDEX "User_createdAt_idx" ON "User"("createdAt");

CREATE UNIQUE INDEX "Seller_userId_key" ON "Seller"("userId");
CREATE INDEX "Seller_sellerType_idx" ON "Seller"("sellerType");
CREATE INDEX "Seller_status_idx" ON "Seller"("status");
CREATE INDEX "Seller_subscriptionStatus_idx" ON "Seller"("subscriptionStatus");
CREATE INDEX "Seller_isFeatured_featuredExpiry_idx" ON "Seller"("isFeatured", "featuredExpiry");

CREATE UNIQUE INDEX "Salon_sellerId_key" ON "Salon"("sellerId");
CREATE INDEX "Salon_lat_lng_idx" ON "Salon"("lat", "lng");
CREATE INDEX "Salon_averageRating_idx" ON "Salon"("averageRating");
CREATE INDEX "Salon_offersHomeService_idx" ON "Salon"("offersHomeService");

CREATE INDEX "Service_salonId_isActive_idx" ON "Service"("salonId", "isActive");
CREATE INDEX "Service_category_idx" ON "Service"("category");
CREATE INDEX "Service_gender_idx" ON "Service"("gender");

CREATE INDEX "Staff_salonId_isAvailable_idx" ON "Staff"("salonId", "isAvailable");
CREATE INDEX "StaffService_serviceId_idx" ON "StaffService"("serviceId");

CREATE INDEX "Booking_userId_status_idx" ON "Booking"("userId", "status");
CREATE INDEX "Booking_salonId_scheduledAt_idx" ON "Booking"("salonId", "scheduledAt");
CREATE INDEX "Booking_staffId_scheduledAt_idx" ON "Booking"("staffId", "scheduledAt");
CREATE INDEX "Booking_paymentStatus_idx" ON "Booking"("paymentStatus");
CREATE INDEX "Booking_createdAt_idx" ON "Booking"("createdAt");

CREATE UNIQUE INDEX "BookingService_bookingId_serviceId_key" ON "BookingService"("bookingId", "serviceId");
CREATE INDEX "BookingService_serviceId_idx" ON "BookingService"("serviceId");

CREATE UNIQUE INDEX "Review_bookingId_key" ON "Review"("bookingId");
CREATE INDEX "Review_userId_idx" ON "Review"("userId");
CREATE INDEX "Review_salonId_createdAt_idx" ON "Review"("salonId", "createdAt");

CREATE UNIQUE INDEX "WorkingHours_salonId_dayOfWeek_key" ON "WorkingHours"("salonId", "dayOfWeek");
CREATE INDEX "WorkingHours_dayOfWeek_idx" ON "WorkingHours"("dayOfWeek");

CREATE INDEX "WorkingHoursBreak_workingHoursId_idx" ON "WorkingHoursBreak"("workingHoursId");
CREATE INDEX "SavedSalon_salonId_idx" ON "SavedSalon"("salonId");

ALTER TABLE "Seller" ADD CONSTRAINT "Seller_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "Salon" ADD CONSTRAINT "Salon_sellerId_fkey" FOREIGN KEY ("sellerId") REFERENCES "Seller"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "Service" ADD CONSTRAINT "Service_salonId_fkey" FOREIGN KEY ("salonId") REFERENCES "Salon"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "Staff" ADD CONSTRAINT "Staff_salonId_fkey" FOREIGN KEY ("salonId") REFERENCES "Salon"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "StaffService" ADD CONSTRAINT "StaffService_staffId_fkey" FOREIGN KEY ("staffId") REFERENCES "Staff"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "StaffService" ADD CONSTRAINT "StaffService_serviceId_fkey" FOREIGN KEY ("serviceId") REFERENCES "Service"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "Booking" ADD CONSTRAINT "Booking_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "Booking" ADD CONSTRAINT "Booking_salonId_fkey" FOREIGN KEY ("salonId") REFERENCES "Salon"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "Booking" ADD CONSTRAINT "Booking_staffId_fkey" FOREIGN KEY ("staffId") REFERENCES "Staff"("id") ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE "BookingService" ADD CONSTRAINT "BookingService_bookingId_fkey" FOREIGN KEY ("bookingId") REFERENCES "Booking"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "BookingService" ADD CONSTRAINT "BookingService_serviceId_fkey" FOREIGN KEY ("serviceId") REFERENCES "Service"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "Review" ADD CONSTRAINT "Review_bookingId_fkey" FOREIGN KEY ("bookingId") REFERENCES "Booking"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "Review" ADD CONSTRAINT "Review_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "Review" ADD CONSTRAINT "Review_salonId_fkey" FOREIGN KEY ("salonId") REFERENCES "Salon"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "WorkingHours" ADD CONSTRAINT "WorkingHours_salonId_fkey" FOREIGN KEY ("salonId") REFERENCES "Salon"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "WorkingHoursBreak" ADD CONSTRAINT "WorkingHoursBreak_workingHoursId_fkey" FOREIGN KEY ("workingHoursId") REFERENCES "WorkingHours"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "SavedSalon" ADD CONSTRAINT "SavedSalon_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "SavedSalon" ADD CONSTRAINT "SavedSalon_salonId_fkey" FOREIGN KEY ("salonId") REFERENCES "Salon"("id") ON DELETE CASCADE ON UPDATE CASCADE;
