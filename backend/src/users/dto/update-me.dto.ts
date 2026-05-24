import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsEmail, IsOptional, IsString, IsUrl, MaxLength, MinLength } from 'class-validator';

export class UpdateMeDto {
  @ApiPropertyOptional({ example: 'Manu S' })
  @IsOptional()
  @IsString()
  @MinLength(2)
  @MaxLength(80)
  name?: string;

  @ApiPropertyOptional({ example: 'https://cdn.snippyseat.in/avatars/user.png' })
  @IsOptional()
  @IsUrl({ require_protocol: true })
  avatar?: string;

  @ApiPropertyOptional({ example: 'manu@example.com' })
  @IsOptional()
  @IsEmail()
  @MaxLength(120)
  email?: string;

  @ApiPropertyOptional({ example: 'fcm-device-token' })
  @IsOptional()
  @IsString()
  @MaxLength(4096)
  fcmToken?: string;
}
