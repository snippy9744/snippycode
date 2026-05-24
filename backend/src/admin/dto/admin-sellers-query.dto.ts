import { ApiPropertyOptional, IntersectionType } from '@nestjs/swagger';
import { SellerStatus, SellerType, SubscriptionStatus } from '@prisma/client';
import { IsEnum, IsOptional } from 'class-validator';
import { AdminPaginationDto } from './admin-pagination.dto';

class AdminSellersFilterDto {
  @ApiPropertyOptional({ enum: SellerStatus })
  @IsOptional()
  @IsEnum(SellerStatus)
  status?: SellerStatus;

  @ApiPropertyOptional({ enum: SellerType })
  @IsOptional()
  @IsEnum(SellerType)
  sellerType?: SellerType;

  @ApiPropertyOptional({ enum: SubscriptionStatus })
  @IsOptional()
  @IsEnum(SubscriptionStatus)
  subscriptionStatus?: SubscriptionStatus;
}

export class AdminSellersQueryDto extends IntersectionType(
  AdminPaginationDto,
  AdminSellersFilterDto,
) {}
