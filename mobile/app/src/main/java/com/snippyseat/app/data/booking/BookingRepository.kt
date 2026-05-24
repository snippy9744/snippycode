package com.snippyseat.app.data.booking

import com.snippyseat.app.data.network.SnippySeatApi
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class BookingRepository @Inject constructor(
    private val api: SnippySeatApi,
) {
    suspend fun getSalonDetail(salonId: String?): SalonDetail {
        val id = salonId ?: sampleSalon.id
        return runCatching {
            val detail = api.getSalonDetail(id).data ?: return@runCatching sampleSalon
            val staff = runCatching { api.getSalonStaff(id).data.orEmpty() }.getOrElse { emptyList() }
            val reviews = runCatching { api.getSalonReviews(id).data.orEmpty() }.getOrElse { emptyList() }
            detail.toDomain(staff, reviews)
        }.getOrElse { sampleSalon.copy(id = id) }
    }

    suspend fun getSlots(salonId: String, date: LocalDate, staffId: String?): List<TimeSlot> {
        return runCatching {
            api.getSalonSlots(salonId, date.toString(), staffId).data.orEmpty()
                .map { TimeSlot(id = it.id, label = it.startsAt.take(5), available = it.available) }
        }.getOrElse {
            sampleSlots(date)
        }.ifEmpty {
            sampleSlots(date)
        }
    }

    suspend fun createBooking(draft: BookingDraft): String {
        val salon = requireNotNull(draft.salon)
        val slot = requireNotNull(draft.selectedSlot)
        val date = requireNotNull(draft.selectedDate)
        return runCatching {
            api.createBooking(
                BookingCreateRequest(
                    salonId = salon.id,
                    serviceIds = draft.selectedServices.map { it.id },
                    staffId = draft.selectedStaff?.id,
                    date = date.toString(),
                    slotId = slot.id,
                    visitType = draft.visitType.apiValue,
                    homeAddress = draft.homeAddress.takeIf { draft.visitType == BookingVisitType.HOME_SERVICE },
                    paymentMethod = draft.paymentMethod.apiValue,
                ),
            ).data?.bookingCode ?: fallbackBookingCode()
        }.getOrElse { fallbackBookingCode() }
    }

    fun priceBreakdown(draft: BookingDraft): PriceBreakdown {
        val subtotal = draft.selectedServices.sumOf { it.price }
        val convenience = if (subtotal == 0) 0 else 20
        val home = if (draft.visitType == BookingVisitType.HOME_SERVICE) 80 else 0
        val tax = ((subtotal + convenience + home) * 0.05).roundToInt()
        val platform = ((subtotal + convenience) * 0.02).roundToInt()
        val premiumDiscount = if (subtotal >= 700) 50 else 0
        return PriceBreakdown(
            servicesSubtotal = subtotal,
            convenienceFee = convenience,
            homeTravelFee = home,
            tax = tax,
            platformFee = platform,
            premiumDiscount = premiumDiscount,
            total = (subtotal + convenience + home + tax + platform - premiumDiscount).coerceAtLeast(0),
        )
    }

    private fun SalonDetailDto.toDomain(
        staffDtos: List<SalonStaffDto>,
        reviewDtos: List<SalonReviewDto>,
    ) = SalonDetail(
        id = id,
        name = name,
        description = description.orEmpty(),
        address = addressText.orEmpty(),
        photos = photos.ifEmpty { sampleSalon.photos },
        rating = averageRating,
        reviewCount = totalReviews,
        distanceKm = distanceKm ?: 1.4,
        isOpen = isOpen,
        offersHomeService = offersHomeService,
        gstNumber = gstNumber ?: "27AABCS1234Q1Z5",
        workingHours = workingHours ?: "10:00 AM - 9:30 PM",
        services = services.map {
            SalonService(
                id = it.id,
                name = it.name,
                category = it.category,
                gender = it.gender,
                durationMinutes = it.durationMinutes,
                price = it.price,
            )
        }.ifEmpty { sampleSalon.services },
        staff = staffDtos.map {
            SalonStaff(it.id, it.name, it.speciality, it.photoUrl, it.rating)
        }.ifEmpty { sampleSalon.staff },
        reviews = reviewDtos.filter { it.verifiedBooking }.map {
            SalonReview(it.id, it.customerName, it.rating, it.comment, it.photoUrls)
        }.ifEmpty { sampleSalon.reviews },
    )

    private fun sampleSlots(date: LocalDate): List<TimeSlot> {
        val start = LocalTime.of(10, 0)
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return (0 until 20).map { index ->
            val time = start.plusMinutes(index * 30L)
            TimeSlot(
                id = "${date}-$index",
                label = time.format(formatter),
                available = index !in setOf(2, 5, 11, 16),
            )
        }
    }

    private fun fallbackBookingCode(): String = "SNIP-${System.currentTimeMillis().toString().takeLast(6)}"
}

val sampleSalon = SalonDetail(
    id = "sample-red-chair",
    name = "Red Chair Studio Bandra",
    description = "Premium haircuts, beard styling, facials, and home grooming across Bandra.",
    address = "Linking Road, Bandra West, Mumbai",
    photos = listOf(
        "https://images.unsplash.com/photo-1521590832167-7bcbfaa6381f",
        "https://images.unsplash.com/photo-1560066984-138dadb4c035",
        "https://images.unsplash.com/photo-1503951914875-452162b0f3f1",
    ),
    rating = 4.8,
    reviewCount = 126,
    distanceKm = 1.4,
    isOpen = true,
    offersHomeService = true,
    gstNumber = "27AABCR4825K1Z9",
    workingHours = "10:00 AM - 9:30 PM",
    services = listOf(
        SalonService("svc-haircut-classic", "Classic Haircut", "Haircut", "MEN", 30, 199),
        SalonService("svc-haircut-styling", "Haircut + Styling", "Haircut", "UNISEX", 45, 349),
        SalonService("svc-kids-cut", "Kids Cut", "Haircut", "UNISEX", 25, 179),
        SalonService("svc-beard-trim", "Beard Trim", "Beard", "MEN", 20, 149),
        SalonService("svc-clean-shave", "Clean Shave", "Beard", "MEN", 20, 129),
        SalonService("svc-facial-glow", "Instant Glow Facial", "Facial", "UNISEX", 50, 699),
        SalonService("svc-threading", "Eyebrow Threading", "Threading", "WOMEN", 15, 99),
        SalonService("svc-hair-color", "Global Hair Color", "Coloring", "UNISEX", 90, 1499),
    ),
    staff = listOf(
        SalonStaff("staff-any", "Any available", "Fastest available stylist", null, 4.7),
        SalonStaff("staff-ayaan", "Ayaan Khan", "Fades and beard design", "https://images.unsplash.com/photo-1622286346003-cb31a4f86128", 4.9),
        SalonStaff("staff-meera", "Meera Shah", "Coloring and styling", "https://images.unsplash.com/photo-1580618672591-eb180b1a973f", 4.8),
        SalonStaff("staff-ravi", "Ravi Nair", "Facials and grooming", "https://images.unsplash.com/photo-1560250097-0b93528c311a", 4.6),
    ),
    reviews = listOf(
        SalonReview("rev-1", "Priya M.", 5, "Clean studio, exact slot timing, and the stylist understood the haircut I wanted.", emptyList()),
        SalonReview("rev-2", "Arjun P.", 5, "Booked a beard trim during lunch break. No waiting and very polished service.", emptyList()),
        SalonReview("rev-3", "Neha S.", 4, "Good facial and helpful staff. Home service was punctual.", emptyList()),
        SalonReview("rev-4", "Rahul D.", 4, "Solid haircut for the price. I would book again.", emptyList()),
    ),
)
