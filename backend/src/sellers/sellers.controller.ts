import {
  Body,
  Controller,
  Get,
  Post,
  Put,
  UploadedFiles,
  UseGuards,
  UseInterceptors,
} from '@nestjs/common';
import { FileFieldsInterceptor } from '@nestjs/platform-express';
import {
  ApiBearerAuth,
  ApiBody,
  ApiConsumes,
  ApiOperation,
  ApiTags,
} from '@nestjs/swagger';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { AuthenticatedUser } from '../auth/interfaces/authenticated-user.interface';
import { OnboardBasicDto } from './dto/onboard-basic.dto';
import { OnboardDocumentsDto } from './dto/onboard-documents.dto';
import { OnboardLocationDto } from './dto/onboard-location.dto';
import { UpdateSellerProfileDto } from './dto/update-seller-profile.dto';
import { SellersService } from './sellers.service';
import { SellerDocumentFiles } from './types/seller-document-files.type';

@ApiTags('seller')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('seller')
export class SellersController {
  constructor(private readonly sellersService: SellersService) {}

  @Post('onboard/basic')
  @ApiOperation({ summary: 'Create or update seller basic profile and set user role to SELLER.' })
  onboardBasic(@CurrentUser() currentUser: AuthenticatedUser, @Body() dto: OnboardBasicDto) {
    return this.sellersService.onboardBasic(currentUser.id, dto);
  }

  @Post('onboard/location')
  @ApiOperation({ summary: 'Create or update seller salon location.' })
  onboardLocation(@CurrentUser() currentUser: AuthenticatedUser, @Body() dto: OnboardLocationDto) {
    return this.sellersService.onboardLocation(currentUser.id, dto);
  }

  @Post('onboard/documents')
  @ApiOperation({ summary: 'Upload GST/shop photos or Aadhaar documents for verification.' })
  @ApiConsumes('multipart/form-data')
  @ApiBody({
    schema: {
      type: 'object',
      properties: {
        gstNumber: { type: 'string', example: '32ABCDE1234F1Z5' },
        shopPhotos: {
          type: 'array',
          items: { type: 'string', format: 'binary' },
        },
        aadhaarFront: { type: 'string', format: 'binary' },
        aadhaarBack: { type: 'string', format: 'binary' },
        aadhaarSelfie: { type: 'string', format: 'binary' },
      },
    },
  })
  @UseInterceptors(
    FileFieldsInterceptor([
      { name: 'shopPhotos', maxCount: 10 },
      { name: 'aadhaarFront', maxCount: 1 },
      { name: 'aadhaarBack', maxCount: 1 },
      { name: 'aadhaarSelfie', maxCount: 1 },
    ]),
  )
  onboardDocuments(
    @CurrentUser() currentUser: AuthenticatedUser,
    @Body() dto: OnboardDocumentsDto,
    @UploadedFiles() files: SellerDocumentFiles,
  ) {
    return this.sellersService.onboardDocuments(currentUser.id, dto, files ?? {});
  }

  @Post('onboard/complete')
  @ApiOperation({ summary: 'Complete onboarding and start seller trial.' })
  completeOnboarding(@CurrentUser() currentUser: AuthenticatedUser) {
    return this.sellersService.completeOnboarding(currentUser.id);
  }

  @Get('profile')
  @ApiOperation({ summary: 'Return current seller and salon details.' })
  getProfile(@CurrentUser() currentUser: AuthenticatedUser) {
    return this.sellersService.getProfile(currentUser.id);
  }

  @Put('profile')
  @ApiOperation({ summary: 'Update seller shop profile, photos, description, and working hours.' })
  updateProfile(
    @CurrentUser() currentUser: AuthenticatedUser,
    @Body() dto: UpdateSellerProfileDto,
  ) {
    return this.sellersService.updateProfile(currentUser.id, dto);
  }
}
