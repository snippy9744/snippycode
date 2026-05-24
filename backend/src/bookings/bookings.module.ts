import { Module } from '@nestjs/common';
import { AuthModule } from '../auth/auth.module';
import { NotificationsModule } from '../notifications/notifications.module';
import { SlotsModule } from '../slots/slots.module';
import { UsersModule } from '../users/users.module';
import { BookingReminderProcessor } from './booking-reminder.processor';
import { BookingsController } from './bookings.controller';
import { BookingsService } from './bookings.service';

@Module({
  imports: [
    AuthModule,
    UsersModule,
    SlotsModule,
    NotificationsModule,
  ],
  controllers: [BookingsController],
  providers: [BookingsService, BookingReminderProcessor],
  exports: [BookingsService],
})
export class BookingsModule {}
