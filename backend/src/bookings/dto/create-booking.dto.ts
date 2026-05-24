import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { PaymentMethod } from '@prisma/client';
import { Type } from 'class-transformer';
import {
  ArrayMinSize,
  IsArray,
  IsBoolean,
  IsDateString,
  IsEnum,
  IsNumber,
  IsOptional,
  IsString,
  Max,
  MaxLength,
  Min,
} from 'class-validator';

export class CreateBookingDto {
  @ApiProperty({ example: 'salon-id' })
  @IsString()
  salonId: string;

  @ApiPropertyOptional({ example: 'staff-id' })
  @IsOptional()
  @IsString()
  staffId?: string;

  @ApiProperty({ type: [String], example: ['service-id-1', 'service-id-2'] })
  @IsArray()
  @ArrayMinSize(1)
  @IsString({ each: true })
  serviceIds: string[];

  @ApiProperty({ example: '2026-05-16T10:30:00+05:30' })
  @IsDateString()
  scheduledAt: string;

  @ApiProperty({ example: false })
  @Type(() => Boolean)
  @IsBoolean()
  isHomeService: boolean;

  @ApiPropertyOptional({ example: 'Flat 12, MG Road, Kochi' })
  @IsOptional()
  @IsString()
  @MaxLength(400)
  homeAddress?: string;

  @ApiPropertyOptional({ example: 9.9816 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(-90)
  @Max(90)
  homeLat?: number;

  @ApiPropertyOptional({ example: 76.2999 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(-180)
  @Max(180)
  homeLng?: number;

  @ApiProperty({ enum: PaymentMethod, example: PaymentMethod.AT_SHOP })
  @IsEnum(PaymentMethod)
  paymentMethod: PaymentMethod;
}
