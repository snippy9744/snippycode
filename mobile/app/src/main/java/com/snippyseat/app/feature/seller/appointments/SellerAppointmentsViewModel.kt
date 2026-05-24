package com.snippyseat.app.feature.seller.appointments

import androidx.lifecycle.ViewModel
import com.snippyseat.app.data.seller.SellerAppointment
import com.snippyseat.app.data.seller.SellerAppointmentStatus
import com.snippyseat.app.data.seller.sampleSellerAppointments
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SellerAppointmentsUiState(
    val selectedTab: SellerAppointmentStatus = SellerAppointmentStatus.TODAY,
    val appointments: List<SellerAppointment> = sampleSellerAppointments,
    val calendarMode: Boolean = false,
    val cancellationTarget: SellerAppointment? = null,
    val cancelReason: String = "",
)

@HiltViewModel
class SellerAppointmentsViewModel @Inject constructor() : ViewModel() {
    private val mutableUiState = MutableStateFlow(SellerAppointmentsUiState())
    val uiState: StateFlow<SellerAppointmentsUiState> = mutableUiState.asStateFlow()

    fun selectTab(tab: SellerAppointmentStatus) {
        mutableUiState.update { it.copy(selectedTab = tab) }
    }

    fun setCalendarMode(enabled: Boolean) {
        mutableUiState.update { it.copy(calendarMode = enabled) }
    }

    fun markComplete(appointment: SellerAppointment) {
        mutableUiState.update { state ->
            state.copy(
                selectedTab = SellerAppointmentStatus.COMPLETED,
                appointments = state.appointments.map {
                    if (it.id == appointment.id) it.copy(status = SellerAppointmentStatus.COMPLETED) else it
                },
            )
        }
    }

    fun openCancelDialog(appointment: SellerAppointment) {
        mutableUiState.update { it.copy(cancellationTarget = appointment, cancelReason = "") }
    }

    fun updateCancelReason(reason: String) {
        mutableUiState.update { it.copy(cancelReason = reason) }
    }

    fun closeCancelDialog() {
        mutableUiState.update { it.copy(cancellationTarget = null, cancelReason = "") }
    }

    fun confirmCancel() {
        val target = mutableUiState.value.cancellationTarget ?: return
        mutableUiState.update { state ->
            state.copy(
                selectedTab = SellerAppointmentStatus.CANCELLED,
                cancellationTarget = null,
                cancelReason = "",
                appointments = state.appointments.map {
                    if (it.id == target.id) it.copy(status = SellerAppointmentStatus.CANCELLED) else it
                },
            )
        }
    }
}
