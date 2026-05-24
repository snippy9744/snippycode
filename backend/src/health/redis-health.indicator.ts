import { Injectable } from '@nestjs/common';
import { HealthIndicatorResult, HealthIndicatorService } from '@nestjs/terminus';
import { RedisService } from '../redis/redis.service';

@Injectable()
export class RedisHealthIndicator {
  constructor(
    private readonly redis: RedisService,
    private readonly healthIndicatorService: HealthIndicatorService,
  ) {}

  async pingCheck(key = 'redis'): Promise<HealthIndicatorResult> {
    const check = this.healthIndicatorService.check(key);

    try {
      const response = await this.redis.ping();

      if (response !== 'PONG') {
        return check.down({ message: `Unexpected Redis ping response: ${response}` });
      }

      return check.up();
    } catch (error) {
      return check.down({
        message: error instanceof Error ? error.message : 'Redis ping failed.',
      });
    }
  }
}
