import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import {
  IsArray,
  IsOptional,
  IsString,
  IsUrl,
  MaxLength,
  MinLength,
  ValidateNested,
} from 'class-validator';
import { WorkingHoursDto } from './working-hours.dto';

export class UpdateSellerProfileDto {
  @ApiPropertyOptional({ example: 'Snippy Seat Studio' })
  @IsOptional()
  @IsString()
  @MinLength(2)
  @MaxLength(120)
  shopName?: string;

  @ApiPropertyOptional({ example: 'Premium salon for men and women.' })
  @IsOptional()
  @IsString()
  @MaxLength(1000)
  description?: string;

  @ApiPropertyOptional({ type: [String], example: ['https://cdn.snippyseat.in/shop/photo.jpg'] })
  @IsOptional()
  @IsArray()
  @IsUrl({ require_protocol: true }, { each: true })
  photos?: string[];

  @ApiPropertyOptional({ type: [WorkingHoursDto] })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => WorkingHoursDto)
  workingHours?: WorkingHoursDto[];
}
