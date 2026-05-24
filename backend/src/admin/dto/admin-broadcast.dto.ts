import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsIn, IsObject, IsOptional, IsString, MaxLength, MinLength } from 'class-validator';

export class AdminBroadcastDto {
  @ApiProperty({ enum: ['USERS', 'SELLERS', 'ALL'] })
  @IsIn(['USERS', 'SELLERS', 'ALL'])
  target: 'USERS' | 'SELLERS' | 'ALL';

  @ApiProperty({ example: 'Weekend offer' })
  @IsString()
  @MinLength(2)
  @MaxLength(120)
  title: string;

  @ApiProperty({ example: 'Book your salon slot today.' })
  @IsString()
  @MinLength(2)
  @MaxLength(500)
  body: string;

  @ApiPropertyOptional({ example: { type: 'PROMO' } })
  @IsOptional()
  @IsObject()
  data?: Record<string, string>;
}
