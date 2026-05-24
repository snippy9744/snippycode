package com.snippyseat.app.feature.seller.hours

import androidx.lifecycle.ViewModel
import com.snippyseat.app.data.seller.SellerWorkingDay
import com.snippyseat.app.data.seller.sampleWorkingDays
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WorkingHoursUiState(
    val days: List<SellerWorkingDay> = sampleWorkingDays,
    val saved: Boolean = false,
)

@HiltViewModel
class WorkingHoursViewModel @Inject constructor() : ViewModel() {
    private val mutableUiState = MutableStateFlow(WorkingHoursUiState())
    val uiState: StateFlow<WorkingHoursUiState> = mutableUiState.asStateFlow()

    fun toggleOpen(day: SellerWorkingDay) = updateDay(day.id) { it.copy(open = !it.open) }
    fun updateStart(day: SellerWorkingDay, value: String) = updateDay(day.id) { it.copy(startTime = value) }
    fun updateEnd(day: SellerWorkingDay, value: String) = updateDay(day.id) { it.copy(endTime = value) }
    fun addBreak(day: SellerWorkingDay) = updateDay(day.id) { it.copy(breaks = it.breaks + "14:00-14:30") }
    fun save() = mutableUiState.update { it.copy(saved = true) }

    private fun updateDay(id: String, block: (SellerWorkingDay) -> SellerWorkingDay) {
        mutableUiState.update { state ->
            state.copy(saved = false, days = state.days.map { if (it.id == id) block(it) else it })
        }
    }
}
