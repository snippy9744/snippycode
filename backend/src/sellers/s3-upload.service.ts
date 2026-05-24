import { Injectable, ServiceUnavailableException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PutObjectCommand, S3Client } from '@aws-sdk/client-s3';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';
import { randomUUID } from 'crypto';
import { extname } from 'path';

@Injectable()
export class S3UploadService {
  private readonly client: S3Client;
  private readonly bucket: string;
  private readonly region: string;
  private readonly isConfigured: boolean;

  constructor(private readonly configService: ConfigService) {
    this.bucket = this.configService.get<string>('AWS_S3_BUCKET', '');
    this.region = this.configService.get<string>('AWS_REGION', 'ap-south-1');
    const accessKeyId = this.configService.get<string>('AWS_ACCESS_KEY', '');
    const secretAccessKey = this.configService.get<string>('AWS_SECRET_KEY', '');
    this.isConfigured =
      Boolean(this.bucket) &&
      Boolean(accessKeyId) &&
      Boolean(secretAccessKey) &&
      !this.bucket.startsWith('replace-with') &&
      !accessKeyId.startsWith('replace-with') &&
      !secretAccessKey.startsWith('replace-with');

    this.client = new S3Client({
      region: this.region,
      credentials: this.isConfigured ? { accessKeyId, secretAccessKey } : undefined,
    });
  }

  async uploadSellerFile(
    sellerId: string,
    type: 'shop-photos' | 'aadhaar-front' | 'aadhaar-back' | 'aadhaar-selfie',
    file: Express.Multer.File,
  ) {
    const key = this.buildSellerKey(sellerId, type, file.originalname);

    if (!this.isConfigured) {
      return `https://storage.snippyseat.placeholder/${key}`;
    }

    await this.client.send(
      new PutObjectCommand({
        Bucket: this.bucket,
        Key: key,
        Body: file.buffer,
        ContentType: file.mimetype,
      }),
    );

    return this.getPublicUrl(key);
  }

  async createPresignedUploadUrl(
    sellerId: string,
    type: 'shop-photos' | 'aadhaar-front' | 'aadhaar-back' | 'aadhaar-selfie',
    filename: string,
    contentType: string,
  ) {
    if (!this.isConfigured) {
      throw new ServiceUnavailableException('S3 storage is not configured.');
    }

    const key = this.buildSellerKey(sellerId, type, filename);
    const url = await getSignedUrl(
      this.client,
      new PutObjectCommand({
        Bucket: this.bucket,
        Key: key,
        ContentType: contentType,
      }),
      { expiresIn: 900 },
    );

    return {
      key,
      url,
      publicUrl: this.getPublicUrl(key),
      expiresIn: 900,
    };
  }

  private buildSellerKey(sellerId: string, type: string, filename: string) {
    const cleanExtension = extname(filename).toLowerCase() || '.bin';

    return `sellers/${sellerId}/${type}/${Date.now()}-${randomUUID()}${cleanExtension}`;
  }

  private getPublicUrl(key: string) {
    return `https://${this.bucket}.s3.${this.region}.amazonaws.com/${key}`;
  }
}
