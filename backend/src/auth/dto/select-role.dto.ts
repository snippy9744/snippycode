import { ApiProperty } from '@nestjs/swagger';
import { Role } from '@prisma/client';
import { IsIn } from 'class-validator';

export class SelectRoleDto {
  @ApiProperty({ enum: [Role.USER, Role.SELLER] })
  @IsIn([Role.USER, Role.SELLER])
  role: 'USER' | 'SELLER';
}
