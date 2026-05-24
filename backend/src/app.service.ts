import { Injectable } from '@nestjs/common';

@Injectable()
export class AppService {
  getRoot() {
    return {
      name: 'Snippy Seat API',
      docs: '/api/docs',
      status: 'ready',
    };
  }
}
