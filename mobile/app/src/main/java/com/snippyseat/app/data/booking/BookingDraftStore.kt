package com.snippyseat.app.data.booking

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class BookingDraftStore @Inject constructor() {
    private val mutableDraft = MutableStateFlow(BookingDraft())
    val draft: StateFlow<BookingDraft> = mutableDraft.asStateFlow()

    fun setSalonSelection(
        salon: SalonDetail,
        services: List<SalonService>,
        staff: SalonStaff?,
    ) {
        mutableDraft.update {
            it.copy(
                salon = salon,
                selectedServices = services,
                selectedStaff = staff,
                selectedDate = null,
                selectedSlot = null,
                bookingCode = null,
            )
        }
    }

    fun setSlot(date: java.time.LocalDate, slot: TimeSlot, staff: SalonStaff?) {
        mutableDraft.update { it.copy(selectedDate = date, selectedSlot = slot, selectedStaff = staff) }
    }

    fun setSummaryOptions(
        visitType: BookingVisitType,
        homeAddress: String,
        paymentMethod: PaymentMethodOption,
    ) {
        mutableDraft.update {
            it.copy(
                visitType = visitType,
                homeAddress = homeAddress,
                paymentMethod = paymentMethod,
            )
        }
    }

    fun setBookingCode(code: String) {
        mutableDraft.update { it.copy(bookingCode = code) }
    }

    fun resetToSalonOnly() {
        mutableDraft.update {
            BookingDraft(
                salon = it.salon,
                selectedServices = it.selectedServices,
                selectedStaff = it.selectedStaff,
            )
        }
    }
}
