package com.snippyseat.app.feature.bookings

import androidx.lifecycle.ViewModel
import com.snippyseat.app.data.user.RefundStatus
import com.snippyseat.app.data.user.UserBooking
import com.snippyseat.app.data.user.UserBookingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class BookingsTab(val label: String) {
    UPCOMING("Upcoming"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
}

data class BookingsUiState(
    val selectedTab: BookingsTab = BookingsTab.UPCOMING,
    val bookings: List<UserBooking> = sampleUserBookings,
    val cancellationTarget: UserBooking? = null,
    val reviewTarget: UserBooking? = null,
    val warningCount: Int = 2,
)

@HiltViewModel
class BookingsViewModel @Inject constructor() : ViewModel() {
    private val mutableUiState = MutableStateFlow(BookingsUiState())
    val uiState: StateFlow<BookingsUiState> = mutableUiState.asStateFlow()

    fun selectTab(tab: BookingsTab) {
        mutableUiState.update { it.copy(selectedTab = tab) }
    }

    fun openCancellation(booking: UserBooking) {
        mutableUiState.update { it.copy(cancellationTarget = booking) }
    }

    fun closeCancellation() {
        mutableUiState.update { it.copy(cancellationTarget = null) }
    }

    fun confirmCancellation() {
        val target = mutableUiState.value.cancellationTarget ?: return
        mutableUiState.update { state ->
            state.copy(
                selectedTab = BookingsTab.CANCELLED,
                cancellationTarget = null,
                bookings = state.bookings.map {
                    if (it.id == target.id) {
                        it.copy(status = UserBookingStatus.CANCELLED, refundStatus = RefundStatus.PROCESSING)
                    } else {
                        it
                    }
                },
            )
        }
    }

    fun openReview(booking: UserBooking) {
        mutableUiState.update { it.copy(reviewTarget = booking) }
    }

    fun closeReview() {
        mutableUiState.update { it.copy(reviewTarget = null) }
    }

    fun submitReview() {
        mutableUiState.update { it.copy(reviewTarget = null) }
    }
}

private val sampleUserBookings = listOf(
    UserBooking(
        id = "BK-10291",
        salonName = "Red Chair Studio Bandra",
        salonPhotoUrl = "https://images.unsplash.com/photo-1521590832167-7bcbfaa6381f",
        servicesSummary = "Haircut + Beard Trim",
        dateTimeLabel = "Today, 6:30 PM",
        amount = 498,
        status = UserBookingStatus.CONFIRMED,
        minutesUntilStart = 90,
    ),
    UserBooking(
        id = "BK-10277",
        salonName = "Glow & Snip Indiranagar",
        salonPhotoUrl = "https://images.unsplash.com/photo-1560066984-138dadb4c035",
        servicesSummary = "Instant Glow Facial",
        dateTimeLabel = "Today, 4:10 PM",
        amount = 699,
        status = UserBookingStatus.PENDING,
        minutesUntilStart = 8,
    ),
    UserBooking(
        id = "BK-10118",
        salonName = "Urban Trim Lounge",
        salonPhotoUrl = "https://images.unsplash.com/photo-1503951914875-452162b0f3f1",
        servicesSummary = "Classic Haircut",
        dateTimeLabel = "May 18, 1:00 PM",
        amount = 199,
        status = UserBookingStatus.COMPLETED,
        daysSinceService = 4,
    ),
    UserBooking(
        id = "BK-10082",
        salonName = "Red Chair Studio Bandra",
        salonPhotoUrl = "https://images.unsplash.com/photo-1521590832167-7bcbfaa6381f",
        servicesSummary = "Global Hair Color",
        dateTimeLabel = "May 7, 11:30 AM",
        amount = 1499,
        status = UserBookingStatus.CANCELLED,
        refundStatus = RefundStatus.REFUNDED,
    ),
)
