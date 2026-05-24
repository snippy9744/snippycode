import { ApiPropertyOptional, IntersectionType } from '@nestjs/swagger';
import { BookingStatus } from '@prisma/client';
import { IsDateString, IsEnum, IsOptional, IsString } from 'class-validator';
import { AdminPaginationDto } from './admin-pagination.dto';

class AdminBookingsFilterDto {
  @ApiPropertyOptional({ enum: BookingStatus })
  @IsOptional()
  @IsEnum(BookingStatus)
  status?: BookingStatus;

  @ApiPropertyOptional({ example: 'salon-id' })
  @IsOptional()
  @IsString()
  salonId?: string;

  @ApiPropertyOptional({ example: 'user-id' })
  @IsOptional()
  @IsString()
  userId?: string;

  @ApiPropertyOptional({ example: '2026-05-01' })
  @IsOptional()
  @IsDateString()
  startDate?: string;

  @ApiPropertyOptional({ example: '2026-05-17' })
  @IsOptional()
  @IsDateString()
  endDate?: string;
}

export class AdminBookingsQueryDto extends IntersectionType(
  AdminPaginationDto,
  AdminBookingsFilterDto,
) {}
