import { MiddlewareConsumer, Module, NestModule, RequestMethod } from '@nestjs/common';
import { AuthModule } from '../auth/auth.module';
import { BlockBookingForBlockedUserGuard } from './guards/block-booking-for-blocked-user.guard';
import { BlockedBookingUserMiddleware } from './middleware/blocked-booking-user.middleware';
import { UserWarningService } from './user-warning.service';
import { UsersController } from './users.controller';
import { UsersService } from './users.service';

@Module({
  imports: [AuthModule],
  controllers: [UsersController],
  providers: [
    UsersService,
    UserWarningService,
    BlockBookingForBlockedUserGuard,
    BlockedBookingUserMiddleware,
  ],
  exports: [UsersService, UserWarningService, BlockBookingForBlockedUserGuard],
})
export class UsersModule implements NestModule {
  configure(consumer: MiddlewareConsumer) {
    consumer
      .apply(BlockedBookingUserMiddleware)
      .forRoutes({ path: 'bookings*', method: RequestMethod.ALL });
  }
}
