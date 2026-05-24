import { ApiProperty } from '@nestjs/swagger';
import { IsString, MaxLength, MinLength } from 'class-validator';

export class AdminReasonDto {
  @ApiProperty({ example: 'Repeated policy violations.' })
  @IsString()
  @MinLength(3)
  @MaxLength(400)
  reason: string;
}
