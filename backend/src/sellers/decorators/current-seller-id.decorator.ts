import { createParamDecorator, ExecutionContext } from '@nestjs/common';

export const CurrentSellerId = createParamDecorator((_data: unknown, context: ExecutionContext) => {
  const request = context.switchToHttp().getRequest<{ sellerId?: string }>();

  return request.sellerId;
});
