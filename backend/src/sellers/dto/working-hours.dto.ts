import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Transform, Type } from 'class-transformer';
import {
  IsArray,
  IsBoolean,
  IsInt,
  IsOptional,
  IsString,
  Matches,
  Max,
  Min,
  ValidateNested,
} from 'class-validator';

const TIME_REGEX = /^([01]\d|2[0-3]):[0-5]\d$/;

export class WorkingHoursBreakDto {
  @ApiProperty({ example: '13:00' })
  @IsString()
  @Matches(TIME_REGEX)
  @Transform(({ obj, value }) => value ?? obj.startTime)
  start: string;

  @ApiProperty({ example: '14:00' })
  @IsString()
  @Matches(TIME_REGEX)
  @Transform(({ obj, value }) => value ?? obj.endTime)
  end: string;
}

export class WorkingHoursDto {
  @ApiProperty({ example: 1 })
  @IsInt()
  @Min(0)
  @Max(6)
  dayOfWeek: number;

  @ApiProperty({ example: true })
  @IsBoolean()
  isOpen: boolean;

  @ApiProperty({ example: '09:00' })
  @IsString()
  @Matches(TIME_REGEX)
  openTime: string;

  @ApiProperty({ example: '20:00' })
  @IsString()
  @Matches(TIME_REGEX)
  closeTime: string;

  @ApiPropertyOptional({ type: [WorkingHoursBreakDto] })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => WorkingHoursBreakDto)
  breaks?: WorkingHoursBreakDto[];
}
