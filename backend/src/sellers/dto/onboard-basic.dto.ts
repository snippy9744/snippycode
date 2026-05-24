import { ApiProperty } from '@nestjs/swagger';
import { SellerType } from '@prisma/client';
import { IsEnum, IsString, MaxLength, MinLength } from 'class-validator';

export class OnboardBasicDto {
  @ApiProperty({ example: 'Snippy Seat Studio' })
  @IsString()
  @MinLength(2)
  @MaxLength(120)
  shopName: string;

  @ApiProperty({ example: 'Manu S' })
  @IsString()
  @MinLength(2)
  @MaxLength(80)
  ownerName: string;

  @ApiProperty({ enum: SellerType, example: SellerType.SHOP })
  @IsEnum(SellerType)
  sellerType: SellerType;
}
