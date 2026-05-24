import { Controller, Get } from '@nestjs/common';
import { ApiOperation, ApiTags } from '@nestjs/swagger';
import { HealthCheck, HealthCheckService, PrismaHealthIndicator } from '@nestjs/terminus';
import { PrismaService } from '../prisma/prisma.service';
import { RedisHealthIndicator } from './redis-health.indicator';

@ApiTags('health')
@Controller('health')
export class HealthController {
  constructor(
    private readonly health: HealthCheckService,
    private readonly prisma: PrismaService,
    private readonly prismaHealth: PrismaHealthIndicator,
    private readonly redisHealth: RedisHealthIndicator,
  ) {}

  @Get()
  @HealthCheck()
  @ApiOperation({ summary: 'Return database and Redis health.' })
  async check() {
    await this.health.check([
      () => this.prismaHealth.pingCheck('db', this.prisma, { timeout: 1000 }),
      () => this.redisHealth.pingCheck('redis'),
    ]);

    return {
      status: 'ok',
      db: 'connected',
      redis: 'connected',
      timestamp: new Date().toISOString(),
    };
  }
}
