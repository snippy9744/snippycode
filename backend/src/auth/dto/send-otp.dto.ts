import { ApiProperty } from '@nestjs/swagger';
import { Matches } from 'class-validator';

export class SendOtpDto {
  @ApiProperty({ example: '+919876543210' })
  @Matches(/^(\+91)?[6-9]\d{9}$/, {
    message: 'phone must be a valid Indian mobile number with optional +91 prefix',
  })
  phone: string;
}
