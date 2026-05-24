import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsArray, IsOptional, IsString, IsUrl, MaxLength, MinLength } from 'class-validator';

export class CreateStaffDto {
  @ApiProperty({ example: 'Arjun Nair' })
  @IsString()
  @MinLength(2)
  @MaxLength(80)
  name: string;

  @ApiPropertyOptional({ example: '+919876543210' })
  @IsOptional()
  @IsString()
  @MaxLength(20)
  phone?: string;

  @ApiPropertyOptional({ example: 'https://cdn.snippyseat.in/staff/arjun.jpg' })
  @IsOptional()
  @IsUrl({ require_protocol: true })
  photo?: string;

  @ApiPropertyOptional({ example: 'Haircuts and beard styling' })
  @IsOptional()
  @IsString()
  @MaxLength(160)
  speciality?: string;

  @ApiProperty({ type: [String], example: ['service-id-1', 'service-id-2'] })
  @IsArray()
  @IsString({ each: true })
  serviceIds: string[];
}
