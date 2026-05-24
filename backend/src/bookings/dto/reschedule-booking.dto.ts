import { ApiProperty } from '@nestjs/swagger';
import { IsDateString } from 'class-validator';

export class RescheduleBookingDto {
  @ApiProperty({ example: '2026-05-17T11:00:00+05:30' })
  @IsDateString()
  newScheduledAt: string;
}
