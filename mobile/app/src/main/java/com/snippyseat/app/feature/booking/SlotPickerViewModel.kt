package com.snippyseat.app.feature.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snippyseat.app.data.booking.BookingDraft
import com.snippyseat.app.data.booking.BookingDraftStore
import com.snippyseat.app.data.booking.BookingRepository
import com.snippyseat.app.data.booking.SalonStaff
import com.snippyseat.app.data.booking.TimeSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SlotPickerUiState(
    val draft: BookingDraft = BookingDraft(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedStaffId: String? = null,
    val slots: List<TimeSlot> = emptyList(),
    val selectedSlotId: String? = null,
    val loading: Boolean = true,
)

@HiltViewModel
class SlotPickerViewModel @Inject constructor(
    private val repository: BookingRepository,
    private val draftStore: BookingDraftStore,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(SlotPickerUiState())
    val uiState: StateFlow<SlotPickerUiState> = mutableUiState.asStateFlow()

    init {
        val draft = draftStore.draft.value
        mutableUiState.update {
            it.copy(
                draft = draft,
                selectedStaffId = draft.selectedStaff?.id,
            )
        }
        loadSlots()
    }

    fun selectDate(date: LocalDate) {
        mutableUiState.update { it.copy(selectedDate = date, selectedSlotId = null) }
        loadSlots()
    }

    fun selectStaff(staff: SalonStaff?) {
        mutableUiState.update { it.copy(selectedStaffId = staff?.id, selectedSlotId = null) }
        loadSlots()
    }

    fun selectSlot(slot: TimeSlot) {
        if (slot.available) {
            mutableUiState.update { it.copy(selectedSlotId = slot.id) }
        }
    }

    fun confirmSlot(): Boolean {
        val state = mutableUiState.value
        val slot = state.slots.firstOrNull { it.id == state.selectedSlotId } ?: return false
        val staff = state.draft.salon?.staff.orEmpty().firstOrNull { it.id == state.selectedStaffId }
        draftStore.setSlot(state.selectedDate, slot, staff)
        return true
    }

    private fun loadSlots() {
        viewModelScope.launch {
            val state = mutableUiState.value
            val salon = state.draft.salon ?: return@launch
            mutableUiState.update { it.copy(loading = true) }
            val slots = repository.getSlots(salon.id, state.selectedDate, state.selectedStaffId)
            mutableUiState.update { it.copy(loading = false, slots = slots) }
        }
    }
}
