import { CanActivate, ExecutionContext, ForbiddenException, Injectable } from '@nestjs/common';
import { UserStatus } from '@prisma/client';
import { AuthenticatedUser } from '../../auth/interfaces/authenticated-user.interface';
import { PrismaService } from '../../prisma/prisma.service';

@Injectable()
export class BlockBookingForBlockedUserGuard implements CanActivate {
  constructor(private readonly prisma: PrismaService) {}

  async canActivate(context: ExecutionContext) {
    const request = context.switchToHttp().getRequest<{ user?: AuthenticatedUser }>();
    const currentUser = request.user;

    if (!currentUser) {
      return true;
    }

    const user = await this.prisma.user.findUnique({
      where: { id: currentUser.id },
      select: { status: true },
    });

    if (user?.status === UserStatus.BLOCKED) {
      throw new ForbiddenException(
        'Your account is blocked due to 3 no-show cancellations. Contact support to unblock.',
      );
    }

    return true;
  }
}
