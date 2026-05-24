import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsUUID } from 'class-validator';

export class VerifyPaymentDto {
  @ApiProperty({ example: '4e5b7729-9c5a-49d9-8fd5-24c6b85c71aa' })
  @IsUUID()
  bookingId: string;

  @ApiProperty({ example: 'PAY_PLACEHOLDER_4e5b7729-9c5a-49d9-8fd5-24c6b85c71aa' })
  @IsString()
  orderId: string;

  @ApiProperty({ example: 'pay_placeholder_123' })
  @IsString()
  paymentId: string;

  @ApiProperty({ example: 'signature_placeholder' })
  @IsString()
  signature: string;
}
