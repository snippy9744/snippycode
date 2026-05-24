import { CanActivate, ExecutionContext, ForbiddenException, Injectable } from '@nestjs/common';
import { Role, SellerStatus } from '@prisma/client';
import { AuthenticatedUser } from '../../auth/interfaces/authenticated-user.interface';
import { PrismaService } from '../../prisma/prisma.service';

@Injectable()
export class SellerActiveGuard implements CanActivate {
  constructor(private readonly prisma: PrismaService) {}

  async canActivate(context: ExecutionContext) {
    const request = context.switchToHttp().getRequest<{ user?: AuthenticatedUser }>();
    const user = request.user;

    if (!user || user.role !== Role.SELLER) {
      throw new ForbiddenException('Seller access is required.');
    }

    const seller = await this.prisma.seller.findUnique({
      where: { userId: user.id },
      select: { id: true, status: true },
    });

    if (!seller) {
      throw new ForbiddenException('Seller profile is required.');
    }

    if (seller.status !== SellerStatus.ACTIVE) {
      throw new ForbiddenException('Seller account must be active to manage this resource.');
    }

    request['sellerId'] = seller.id;

    return true;
  }
}
