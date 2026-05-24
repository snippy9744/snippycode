import { Module } from '@nestjs/common';
import { AuthModule } from '../auth/auth.module';
import { SlotsModule } from '../slots/slots.module';
import { UsersModule } from '../users/users.module';
import { AdminController } from './admin.controller';
import { AdminService } from './admin.service';

@Module({
  imports: [AuthModule, UsersModule, SlotsModule],
  controllers: [AdminController],
  providers: [AdminService],
  exports: [AdminService],
})
export class AdminModule {}
