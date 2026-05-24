package com.snippyseat.app.data.user

enum class UserBookingStatus(val label: String) {
    CONFIRMED("Confirmed"),
    PENDING("Pending"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
}

enum class RefundStatus(val label: String) {
    PROCESSING("Refund processing"),
    REFUNDED("Refunded"),
    PAY_AT_SHOP("No payment collected"),
}

data class UserBooking(
    val id: String,
    val salonName: String,
    val salonPhotoUrl: String,
    val servicesSummary: String,
    val dateTimeLabel: String,
    val amount: Int,
    val status: UserBookingStatus,
    val minutesUntilStart: Int = 0,
    val daysSinceService: Int = 0,
    val refundStatus: RefundStatus? = null,
)

data class UserProfile(
    val name: String,
    val phone: String,
    val email: String,
    val avatarUrl: String?,
    val bookingsCount: Int,
    val savedSalons: Int,
    val reviewsGiven: Int,
    val premiumActive: Boolean,
    val premiumExpiry: String,
    val isBlocked: Boolean,
)

enum class UserNotificationType {
    BOOKING_CONFIRMED,
    REMINDER,
    CANCELLATION,
    REVIEW_REQUEST,
    PROMO,
}

data class UserNotification(
    val id: String,
    val title: String,
    val body: String,
    val group: String,
    val type: UserNotificationType,
    val timeLabel: String,
)

data class PremiumPlan(
    val monthlyPrice: Int,
    val active: Boolean,
    val expiryLabel: String,
)
