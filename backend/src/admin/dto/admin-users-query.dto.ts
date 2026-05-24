import { ApiPropertyOptional, IntersectionType } from '@nestjs/swagger';
import { UserStatus } from '@prisma/client';
import { IsEnum, IsOptional, IsString } from 'class-validator';
import { AdminPaginationDto } from './admin-pagination.dto';

class AdminUsersFilterDto {
  @ApiPropertyOptional({ enum: UserStatus })
  @IsOptional()
  @IsEnum(UserStatus)
  status?: UserStatus;

  @ApiPropertyOptional({ example: 'manu' })
  @IsOptional()
  @IsString()
  search?: string;
}

export class AdminUsersQueryDto extends IntersectionType(
  AdminPaginationDto,
  AdminUsersFilterDto,
) {}
