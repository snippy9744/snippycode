package com.snippyseat.app.feature.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snippyseat.app.core.connectivity.ConnectivityObserver
import com.snippyseat.app.data.booking.BookingDraft
import com.snippyseat.app.data.booking.BookingDraftStore
import com.snippyseat.app.data.booking.BookingRepository
import com.snippyseat.app.data.booking.BookingVisitType
import com.snippyseat.app.data.booking.PaymentMethodOption
import com.snippyseat.app.data.booking.PriceBreakdown
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookingSummaryUiState(
    val draft: BookingDraft = BookingDraft(),
    val priceBreakdown: PriceBreakdown = PriceBreakdown(0, 0, 0, 0, 0, 0, 0),
    val breakdownExpanded: Boolean = true,
    val confirming: Boolean = false,
    val isOnline: Boolean = true,
)

@HiltViewModel
class BookingSummaryViewModel @Inject constructor(
    private val repository: BookingRepository,
    private val draftStore: BookingDraftStore,
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(BookingSummaryUiState())
    val uiState: StateFlow<BookingSummaryUiState> = mutableUiState.asStateFlow()

    init {
        refreshDraft()
        viewModelScope.launch {
            connectivityObserver.isOnline.collect { online ->
                mutableUiState.update { it.copy(isOnline = online) }
            }
        }
    }

    fun selectVisitType(type: BookingVisitType) {
        val state = mutableUiState.value
        draftStore.setSummaryOptions(type, state.draft.homeAddress, state.draft.paymentMethod)
        refreshDraft()
    }

    fun updateHomeAddress(address: String) {
        val state = mutableUiState.value
        draftStore.setSummaryOptions(state.draft.visitType, address, state.draft.paymentMethod)
        refreshDraft()
    }

    fun selectPayment(method: PaymentMethodOption) {
        val state = mutableUiState.value
        draftStore.setSummaryOptions(state.draft.visitType, state.draft.homeAddress, method)
        refreshDraft()
    }

    fun toggleBreakdown() {
        mutableUiState.update { it.copy(breakdownExpanded = !it.breakdownExpanded) }
    }

    fun confirmBooking(onDone: () -> Unit) {
        viewModelScope.launch {
            if (!mutableUiState.value.isOnline) return@launch
            val draft = mutableUiState.value.draft
            mutableUiState.update { it.copy(confirming = true) }
            val code = repository.createBooking(draft)
            draftStore.setBookingCode(code)
            refreshDraft()
            mutableUiState.update { it.copy(confirming = false) }
            onDone()
        }
    }

    private fun refreshDraft() {
        val draft = draftStore.draft.value
        mutableUiState.update { it.copy(draft = draft, priceBreakdown = repository.priceBreakdown(draft)) }
    }
}
