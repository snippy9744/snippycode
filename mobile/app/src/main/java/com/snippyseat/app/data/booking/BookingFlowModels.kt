package com.snippyseat.app.data.booking

import java.time.LocalDate

data class SalonDetailDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val addressText: String? = null,
    val photos: List<String> = emptyList(),
    val averageRating: Double = 0.0,
    val totalReviews: Int = 0,
    val distanceKm: Double? = null,
    val isOpen: Boolean = true,
    val offersHomeService: Boolean = false,
    val gstNumber: String? = null,
    val workingHours: String? = null,
    val services: List<SalonServiceDto> = emptyList(),
)

data class SalonServiceDto(
    val id: String,
    val name: String,
    val category: String,
    val gender: String = "UNISEX",
    val durationMinutes: Int = 30,
    val price: Int,
)

data class SalonStaffDto(
    val id: String,
    val name: String,
    val speciality: String,
    val photoUrl: String? = null,
    val rating: Double = 4.5,
)

data class SalonReviewDto(
    val id: String,
    val customerName: String,
    val rating: Int,
    val comment: String,
    val verifiedBooking: Boolean = true,
    val photoUrls: List<String> = emptyList(),
)

data class TimeSlotDto(
    val id: String,
    val startsAt: String,
    val available: Boolean = true,
)

data class BookingCreateRequest(
    val salonId: String,
    val serviceIds: List<String>,
    val staffId: String?,
    val date: String,
    val slotId: String,
    val visitType: String,
    val homeAddress: String?,
    val paymentMethod: String,
)

data class BookingCreateResponse(
    val id: String,
    val bookingCode: String,
)

data class SalonDetail(
    val id: String,
    val name: String,
    val description: String,
    val address: String,
    val photos: List<String>,
    val rating: Double,
    val reviewCount: Int,
    val distanceKm: Double,
    val isOpen: Boolean,
    val offersHomeService: Boolean,
    val gstNumber: String,
    val workingHours: String,
    val services: List<SalonService>,
    val staff: List<SalonStaff>,
    val reviews: List<SalonReview>,
)

data class SalonService(
    val id: String,
    val name: String,
    val category: String,
    val gender: String,
    val durationMinutes: Int,
    val price: Int,
)

data class SalonStaff(
    val id: String,
    val name: String,
    val speciality: String,
    val photoUrl: String?,
    val rating: Double,
)

data class SalonReview(
    val id: String,
    val customerName: String,
    val rating: Int,
    val comment: String,
    val photoUrls: List<String>,
)

data class TimeSlot(
    val id: String,
    val label: String,
    val available: Boolean,
)

enum class BookingVisitType(val label: String, val apiValue: String) {
    AT_SALON("At Salon", "AT_SALON"),
    HOME_SERVICE("Home Service", "HOME_SERVICE"),
}

enum class PaymentMethodOption(val label: String, val apiValue: String) {
    PAY_AT_SHOP("Pay at Shop", "PAY_AT_SHOP"),
    PAY_ONLINE("Pay Online", "PAY_ONLINE"),
}

data class BookingDraft(
    val salon: SalonDetail? = null,
    val selectedServices: List<SalonService> = emptyList(),
    val selectedStaff: SalonStaff? = null,
    val selectedDate: LocalDate? = null,
    val selectedSlot: TimeSlot? = null,
    val visitType: BookingVisitType = BookingVisitType.AT_SALON,
    val homeAddress: String = "",
    val paymentMethod: PaymentMethodOption = PaymentMethodOption.PAY_AT_SHOP,
    val bookingCode: String? = null,
)

data class PriceBreakdown(
    val servicesSubtotal: Int,
    val convenienceFee: Int,
    val homeTravelFee: Int,
    val tax: Int,
    val platformFee: Int,
    val premiumDiscount: Int,
    val total: Int,
)
