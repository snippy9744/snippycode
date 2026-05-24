import { Injectable, NotFoundException } from '@nestjs/common';
import { UserStatus } from '@prisma/client';
import { NotificationsService } from '../notifications/notifications.service';
import { PrismaService } from '../prisma/prisma.service';

const DEFAULT_MAX_WARNINGS_BEFORE_BLOCK = 3;

@Injectable()
export class UserWarningService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly notificationsService: NotificationsService,
  ) {}

  async getWarningStatus(userId: string) {
    const [user, maxWarnings] = await Promise.all([
      this.prisma.user.findUnique({
        where: { id: userId },
        select: {
          warningCount: true,
          status: true,
        },
      }),
      this.getMaxWarningsBeforeBlock(),
    ]);

    if (!user) {
      throw new NotFoundException('User not found.');
    }

    return {
      warningCount: user.warningCount,
      maxWarnings,
      isBlocked: user.status === UserStatus.BLOCKED,
    };
  }

  async issueWarning(userId: string) {
    const maxWarnings = await this.getMaxWarningsBeforeBlock();
    const user = await this.prisma.user.update({
      where: { id: userId },
      data: { warningCount: { increment: 1 } },
      select: {
        id: true,
        warningCount: true,
        status: true,
      },
    });
    const isBlocked = user.warningCount >= maxWarnings;

    if (isBlocked && user.status !== UserStatus.BLOCKED) {
      await this.prisma.user.update({
        where: { id: userId },
        data: { status: UserStatus.BLOCKED },
      });
      await this.notificationsService.sendToUser(userId, {
        title: 'Account blocked',
        body: 'Your account has been blocked due to repeated late cancellations. Contact support to unblock.',
        data: {
          type: 'ACCOUNT_BLOCKED',
        },
      });
    } else {
      await this.notificationsService.sendToUser(userId, {
        title: `Warning ${user.warningCount}/${maxWarnings}`,
        body: `Warning ${user.warningCount}/${maxWarnings}: Late cancellation recorded on your account.`,
        data: {
          type: 'WARNING_ISSUED',
          warningCount: String(user.warningCount),
          maxWarnings: String(maxWarnings),
        },
      });
    }

    return {
      warningCount: user.warningCount,
      maxWarnings,
      isBlocked,
    };
  }

  async resetWarnings(userId: string) {
    const user = await this.prisma.user.update({
      where: { id: userId },
      data: { warningCount: 0 },
      select: {
        id: true,
        warningCount: true,
        status: true,
      },
    });

    return {
      userId: user.id,
      warningCount: user.warningCount,
      isBlocked: user.status === UserStatus.BLOCKED,
    };
  }

  async blockUser(userId: string, reason?: string) {
    const user = await this.prisma.user.update({
      where: { id: userId },
      data: { status: UserStatus.BLOCKED },
      select: {
        id: true,
        warningCount: true,
        status: true,
      },
    });

    await this.notificationsService.sendToUser(userId, {
      title: 'Account blocked',
      body: reason ?? 'Your account has been blocked. Contact support to unblock.',
      data: {
        type: 'ACCOUNT_BLOCKED',
      },
    });

    return {
      userId: user.id,
      warningCount: user.warningCount,
      isBlocked: true,
    };
  }

  async unblockUser(userId: string, resetWarnings = true) {
    const user = await this.prisma.user.update({
      where: { id: userId },
      data: {
        status: UserStatus.ACTIVE,
        ...(resetWarnings ? { warningCount: 0 } : {}),
      },
      select: {
        id: true,
        warningCount: true,
        status: true,
      },
    });

    await this.notificationsService.sendToUser(userId, {
      title: 'Account unblocked',
      body: 'Your account has been unblocked. You can book appointments again.',
      data: {
        type: 'ACCOUNT_UNBLOCKED',
      },
    });

    return {
      userId: user.id,
      warningCount: user.warningCount,
      isBlocked: user.status === UserStatus.BLOCKED,
    };
  }

  private async getMaxWarningsBeforeBlock() {
    const config = await this.prisma.adminConfig.findFirst({
      select: { maxWarningsBeforeBlock: true },
    });

    return config?.maxWarningsBeforeBlock ?? DEFAULT_MAX_WARNINGS_BEFORE_BLOCK;
  }
}
