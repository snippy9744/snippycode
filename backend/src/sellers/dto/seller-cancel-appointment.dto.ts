import { ApiProperty } from '@nestjs/swagger';
import { IsString, MaxLength, MinLength } from 'class-validator';

export class SellerCancelAppointmentDto {
  @ApiProperty({ example: 'Stylist unavailable due to emergency.' })
  @IsString()
  @MinLength(3)
  @MaxLength(400)
  reason: string;
}
