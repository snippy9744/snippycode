import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsDateString, IsIn, IsOptional } from 'class-validator';

export class SellerEarningsQueryDto {
  @ApiPropertyOptional({ enum: ['today', 'week', 'month', 'custom'], default: 'today' })
  @IsOptional()
  @IsIn(['today', 'week', 'month', 'custom'])
  period: 'today' | 'week' | 'month' | 'custom' = 'today';

  @ApiPropertyOptional({ example: '2026-05-01' })
  @IsOptional()
  @IsDateString()
  startDate?: string;

  @ApiPropertyOptional({ example: '2026-05-16' })
  @IsOptional()
  @IsDateString()
  endDate?: string;
}
