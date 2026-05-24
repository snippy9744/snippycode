package com.snippyseat.app.feature.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snippyseat.app.data.booking.BookingDraftStore
import com.snippyseat.app.data.booking.BookingRepository
import com.snippyseat.app.data.booking.SalonDetail
import com.snippyseat.app.data.booking.SalonService
import com.snippyseat.app.data.booking.SalonStaff
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SalonDetailTab(val label: String) {
    SERVICES("Services"),
    STAFF("Staff"),
    REVIEWS("Reviews"),
    ABOUT("About"),
}

enum class ServiceGenderFilter(val label: String, val apiValue: String?) {
    ALL("All", null),
    MEN("Men", "MEN"),
    WOMEN("Women", "WOMEN"),
}

data class SalonDetailUiState(
    val loading: Boolean = true,
    val salon: SalonDetail? = null,
    val selectedTab: SalonDetailTab = SalonDetailTab.SERVICES,
    val genderFilter: ServiceGenderFilter = ServiceGenderFilter.ALL,
    val selectedServiceIds: Set<String> = emptySet(),
    val selectedStaffId: String? = null,
    val error: String? = null,
)

@HiltViewModel
class SalonDetailViewModel @Inject constructor(
    private val repository: BookingRepository,
    private val draftStore: BookingDraftStore,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(SalonDetailUiState())
    val uiState: StateFlow<SalonDetailUiState> = mutableUiState.asStateFlow()

    fun load(salonId: String?) {
        viewModelScope.launch {
            mutableUiState.update { it.copy(loading = true, error = null) }
            runCatching { repository.getSalonDetail(salonId) }
                .onSuccess { salon ->
                    mutableUiState.update {
                        it.copy(
                            loading = false,
                            salon = salon,
                            selectedStaffId = salon.staff.firstOrNull()?.id,
                        )
                    }
                }
                .onFailure { error ->
                    mutableUiState.update {
                        it.copy(loading = false, error = error.message ?: "Could not load salon.")
                    }
                }
        }
    }

    fun selectTab(tab: SalonDetailTab) {
        mutableUiState.update { it.copy(selectedTab = tab) }
    }

    fun selectGender(filter: ServiceGenderFilter) {
        mutableUiState.update { it.copy(genderFilter = filter) }
    }

    fun toggleService(service: SalonService) {
        mutableUiState.update {
            val next = if (service.id in it.selectedServiceIds) {
                it.selectedServiceIds - service.id
            } else {
                it.selectedServiceIds + service.id
            }
            it.copy(selectedServiceIds = next)
        }
    }

    fun selectStaff(staff: SalonStaff) {
        mutableUiState.update { it.copy(selectedStaffId = staff.id) }
    }

    fun saveDraftForBooking(): Boolean {
        val state = mutableUiState.value
        val salon = state.salon ?: return false
        val services = salon.services.filter { it.id in state.selectedServiceIds }
        if (services.isEmpty()) return false
        val staff = salon.staff.firstOrNull { it.id == state.selectedStaffId }
        draftStore.setSalonSelection(salon, services, staff)
        return true
    }
}
