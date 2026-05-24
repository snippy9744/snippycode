# Snippy Seat Backend

NestJS backend for Snippy Seat, a salon booking app for India. It uses PostgreSQL through Prisma, Redis for cache and Bull queues, JWT auth, MSG91 OTP, Firebase Cloud Messaging, and payment placeholders ready for Razorpay.

## Setup

1. Install dependencies:

```bash
npm install
```

2. Create `.env` from `.env.example` and fill secrets.

3. Start local services:

```bash
docker compose up -d postgres redis
```

4. Run database setup:

```bash
npx prisma migrate dev
npx prisma db seed
```

5. Start the API:

```bash
npm run start:dev
```

API docs are available at `http://localhost:3000/api/docs`. Health check is available at `http://localhost:3000/health`.

## Environment Variables

`DATABASE_URL`: PostgreSQL connection string.
`REDIS_URL`: Redis connection string for cache and Bull queues.
`PORT`: Backend HTTP port. Defaults to `3000`.
`ADMIN_ORIGIN`: Allowed admin web origin for CORS. Supports comma-separated origins.
`JWT_SECRET`: Access token signing secret.
`JWT_REFRESH_SECRET`: Refresh token signing secret.
`GOOGLE_CLIENT_ID`: Google auth client ID.
`GOOGLE_CLIENT_SECRET`: Google auth client secret placeholder.
`MSG91_AUTH_KEY`: MSG91 API key for OTP.
`MSG91_TEMPLATE_ID`: MSG91 OTP template ID.
`FIREBASE_PROJECT_ID`: Firebase project ID.
`FIREBASE_PRIVATE_KEY`: Firebase service account private key.
`FIREBASE_CLIENT_EMAIL`: Firebase service account email.
`AWS_S3_BUCKET`: S3 or R2 bucket for seller uploads.
`AWS_ACCESS_KEY`: S3 access key.
`AWS_SECRET_KEY`: S3 secret key.
`AWS_REGION`: S3 region, defaults to `ap-south-1`.
`MAP_PROVIDER_PLACEHOLDER`: Google Maps SDK key placeholder.
`PAYMENT_GATEWAY_PLACEHOLDER`: Payment gateway marker until Razorpay is connected.
`RAZORPAY_KEY_ID`: Razorpay key ID placeholder.
`RAZORPAY_KEY_SECRET`: Razorpay key secret placeholder.
`RAZORPAY_WEBHOOK_SECRET`: Razorpay webhook secret placeholder.

## Payment Placeholders

The payment module exposes:

`POST /api/payments/initiate`: Creates `PAY_PLACEHOLDER_{bookingId}`.
`POST /api/payments/verify`: Mock-verifies payment, marks booking `PAID`, and confirms the slot.
`POST /api/payments/webhook`: Accepts webhook payloads for future Razorpay processing.
`POST /api/payments/refund/:bookingId`: Admin-only refund placeholder that updates refund status and queues `refund-queue`.

## Deployment

Docker:

```bash
docker compose up --build
```

PM2:

```bash
npm run build
pm2 start ecosystem.config.js
```

## Codex Task Order

Backend: B1 -> B2 -> B3 -> B4 -> B5 -> B6 -> B7 -> B8 -> B9 -> B10 -> B11

Mobile: M1 -> M2 -> M3 -> M4 -> M5 -> M6 -> M7 -> M8

Admin Panel: M9 inside `admin/`
