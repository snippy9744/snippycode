import { ApiProperty } from '@nestjs/swagger';
import { IsUUID } from 'class-validator';

export class InitiatePaymentDto {
  @ApiProperty({ example: '4e5b7729-9c5a-49d9-8fd5-24c6b85c71aa' })
  @IsUUID()
  bookingId: string;
}
