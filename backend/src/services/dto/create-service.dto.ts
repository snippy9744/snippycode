import { ApiProperty } from '@nestjs/swagger';
import { Gender, ServiceCategory } from '@prisma/client';
import { Type } from 'class-transformer';
import { IsBoolean, IsEnum, IsInt, IsNumber, IsOptional, IsString, Max, MaxLength, Min } from 'class-validator';

export class CreateServiceDto {
  @ApiProperty({ example: 'Classic Haircut' })
  @IsString()
  @MaxLength(120)
  name: string;

  @ApiProperty({ enum: ServiceCategory, example: ServiceCategory.HAIRCUT })
  @IsEnum(ServiceCategory)
  category: ServiceCategory;

  @ApiProperty({ enum: Gender, example: Gender.MEN })
  @IsEnum(Gender)
  gender: Gender;

  @ApiProperty({ example: 30 })
  @Type(() => Number)
  @IsInt()
  @Min(15)
  @Max(180)
  durationMinutes: number;

  @ApiProperty({ example: 250 })
  @Type(() => Number)
  @IsNumber()
  @Min(1)
  price: number;

  @ApiProperty({ example: false })
  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  isHomeAvailable = false;
}
