import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsInt, IsNumber, IsOptional, IsString, Max, MaxLength, Min } from 'class-validator';

export class AdminConfigUpdateDto {
  @ApiPropertyOptional({ example: 2.5 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  @Max(5)
  userCommissionPct?: number;

  @ApiPropertyOptional({ example: 12 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(10)
  @Max(15)
  sellerCommissionPct?: number;

  @ApiPropertyOptional({ example: 15 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  homeServiceCommissionPct?: number;

  @ApiPropertyOptional({ example: 10 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  convenienceFeeMin?: number;

  @ApiPropertyOptional({ example: 30 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  convenienceFeeMax?: number;

  @ApiPropertyOptional({ example: 1.6 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(1)
  homePriceMultiplier?: number;

  @ApiPropertyOptional({ example: 10 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  travelFeePerKm?: number;

  @ApiPropertyOptional({ example: 18 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  gstPct?: number;

  @ApiPropertyOptional({ example: 'State Cess' })
  @IsOptional()
  @IsString()
  @MaxLength(80)
  additionalTaxLabel?: string;

  @ApiPropertyOptional({ example: 0 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  additionalTaxPct?: number;

  @ApiPropertyOptional({ example: 199 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  userPremiumPriceMonthly?: number;

  @ApiPropertyOptional({ example: 999 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  sellerSubscriptionMonthly?: number;

  @ApiPropertyOptional({ example: 30 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(0)
  sellerTrialDays?: number;

  @ApiPropertyOptional({ example: 15 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(0)
  cancellationWindowMinutes?: number;

  @ApiPropertyOptional({ example: 3 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  maxWarningsBeforeBlock?: number;

  @ApiPropertyOptional({ example: 499 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  featuredListingPriceMonthly?: number;
}
