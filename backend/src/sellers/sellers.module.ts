import { Module } from '@nestjs/common';
import { AuthModule } from '../auth/auth.module';
import { NotificationsModule } from '../notifications/notifications.module';
import { SlotsModule } from '../slots/slots.module';
import { SellerActiveGuard } from './guards/seller-active.guard';
import { S3UploadService } from './s3-upload.service';
import { SellerDashboardController } from './seller-dashboard.controller';
import { SellerDashboardService } from './seller-dashboard.service';
import { SellerWorkingHoursController } from './seller-working-hours.controller';
import { SellerWorkingHoursService } from './seller-working-hours.service';
import { SellersController } from './sellers.controller';
import { SellersService } from './sellers.service';

@Module({
  imports: [AuthModule, SlotsModule, NotificationsModule],
  controllers: [SellersController, SellerWorkingHoursController, SellerDashboardController],
  providers: [
    SellersService,
    S3UploadService,
    SellerActiveGuard,
    SellerWorkingHoursService,
    SellerDashboardService,
  ],
  exports: [
    SellersService,
    S3UploadService,
    SellerActiveGuard,
    SellerWorkingHoursService,
    SellerDashboardService,
  ],
})
export class SellersModule {}
