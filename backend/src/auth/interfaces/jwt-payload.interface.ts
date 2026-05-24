import { Role, UserStatus } from '@prisma/client';

export interface JwtPayload {
  sub: string;
  role: Role;
  status: UserStatus;
}

export interface TemporaryJwtPayload {
  sub: string;
  purpose: 'ROLE_SELECTION';
}
