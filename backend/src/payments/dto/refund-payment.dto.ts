import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsNumber, IsOptional, IsPositive, IsString, MaxLength } from 'class-validator';

export class RefundPaymentDto {
  @ApiPropertyOptional({ example: 499 })
  @IsOptional()
  @IsNumber({ maxDecimalPlaces: 2 })
  @IsPositive()
  amount?: number;

  @ApiPropertyOptional({ example: 'Admin approved customer refund' })
  @IsOptional()
  @IsString()
  @MaxLength(250)
  reason?: string;
}
