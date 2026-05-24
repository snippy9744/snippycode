import { Module } from '@nestjs/common';
import { AuthModule } from '../auth/auth.module';
import { SellersModule } from '../sellers/sellers.module';
import { SellerStaffController } from './seller-staff.controller';
import { SellerStaffService } from './seller-staff.service';

@Module({
  imports: [AuthModule, SellersModule],
  controllers: [SellerStaffController],
  providers: [SellerStaffService],
  exports: [SellerStaffService],
})
export class StaffModule {}
