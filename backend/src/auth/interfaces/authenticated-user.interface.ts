import { Role, UserStatus } from '@prisma/client';

export interface AuthenticatedUser {
  id: string;
  role: Role;
  status: UserStatus;
}
