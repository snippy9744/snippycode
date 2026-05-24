import { Module } from '@nestjs/common';
import { AuthModule } from '../auth/auth.module';
import { SellersModule } from '../sellers/sellers.module';
import { SellerServicesController } from './seller-services.controller';
import { SellerServicesService } from './seller-services.service';

@Module({
  imports: [AuthModule, SellersModule],
  controllers: [SellerServicesController],
  providers: [SellerServicesService],
  exports: [SellerServicesService],
})
export class ServicesModule {}
