import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, Matches } from 'class-validator';

export const GST_REGEX = /^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$/;

export class OnboardDocumentsDto {
  @ApiPropertyOptional({ example: '32ABCDE1234F1Z5' })
  @IsOptional()
  @Matches(GST_REGEX, { message: 'gstNumber must be a valid Indian GST number' })
  gstNumber?: string;
}
