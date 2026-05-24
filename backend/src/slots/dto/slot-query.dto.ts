import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Transform } from 'class-transformer';
import { ArrayMinSize, IsArray, IsOptional, IsString, Matches } from 'class-validator';

export class SlotQueryDto {
  @ApiProperty({ example: '2026-05-16' })
  @IsString()
  @Matches(/^\d{4}-\d{2}-\d{2}$/, { message: 'date must be in YYYY-MM-DD format' })
  date: string;

  @ApiProperty({ example: 'service-id-1,service-id-2' })
  @Transform(({ value }) =>
    Array.isArray(value)
      ? value
      : String(value ?? '')
          .split(',')
          .map((item) => item.trim())
          .filter(Boolean),
  )
  @IsArray()
  @ArrayMinSize(1)
  @IsString({ each: true })
  serviceIds: string[];

  @ApiPropertyOptional({ example: 'staff-id' })
  @IsOptional()
  @IsString()
  staffId?: string;
}
