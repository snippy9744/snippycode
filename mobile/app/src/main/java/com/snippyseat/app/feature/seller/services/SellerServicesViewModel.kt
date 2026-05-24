package com.snippyseat.app.feature.seller.services

import androidx.lifecycle.ViewModel
import com.snippyseat.app.data.seller.SellerServiceGender
import com.snippyseat.app.data.seller.SellerServiceItem
import com.snippyseat.app.data.seller.sampleSellerServices
import com.snippyseat.app.data.seller.sellerServiceCategories
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class SellerManagementTab(val label: String) {
    SERVICES("Services"),
    STAFF("Staff"),
    HOURS("Hours"),
}

data class ServiceDraft(
    val id: String? = null,
    val name: String = "",
    val category: String = sellerServiceCategories.first(),
    val gender: SellerServiceGender = SellerServiceGender.UNISEX,
    val durationMinutes: Int = 30,
    val price: String = "",
    val homeService: Boolean = false,
    val active: Boolean = true,
)

data class SellerServicesUiState(
    val selectedTab: SellerManagementTab = SellerManagementTab.SERVICES,
    val services: List<SellerServiceItem> = sampleSellerServices,
    val sheetOpen: Boolean = false,
    val draft: ServiceDraft = ServiceDraft(),
)

@HiltViewModel
class SellerServicesViewModel @Inject constructor() : ViewModel() {
    private val mutableUiState = MutableStateFlow(SellerServicesUiState())
    val uiState: StateFlow<SellerServicesUiState> = mutableUiState.asStateFlow()

    fun selectTab(tab: SellerManagementTab) = mutableUiState.update { it.copy(selectedTab = tab) }
    fun openAddSheet() = mutableUiState.update { it.copy(sheetOpen = true, draft = ServiceDraft()) }
    fun openEditSheet(service: SellerServiceItem) = mutableUiState.update {
        it.copy(
            sheetOpen = true,
            draft = ServiceDraft(
                id = service.id,
                name = service.name,
                category = service.category,
                gender = service.gender,
                durationMinutes = service.durationMinutes,
                price = service.price.toString(),
                homeService = service.homeService,
                active = service.active,
            ),
        )
    }
    fun closeSheet() = mutableUiState.update { it.copy(sheetOpen = false) }
    fun updateName(value: String) = updateDraft { it.copy(name = value) }
    fun updateCategory(value: String) = updateDraft { it.copy(category = value) }
    fun updateGender(value: SellerServiceGender) = updateDraft { it.copy(gender = value) }
    fun updateDuration(value: Int) = updateDraft { it.copy(durationMinutes = value) }
    fun updatePrice(value: String) = updateDraft { it.copy(price = value.filter(Char::isDigit)) }
    fun updateHomeService(value: Boolean) = updateDraft { it.copy(homeService = value) }
    fun updateActive(value: Boolean) = updateDraft { it.copy(active = value) }

    fun toggleServiceActive(service: SellerServiceItem) {
        mutableUiState.update { state ->
            state.copy(services = state.services.map { if (it.id == service.id) it.copy(active = !it.active) else it })
        }
    }

    fun deleteService(service: SellerServiceItem) {
        mutableUiState.update { it.copy(services = it.services.filterNot { item -> item.id == service.id }) }
    }

    fun saveService() {
        val draft = mutableUiState.value.draft
        val price = draft.price.toIntOrNull() ?: return
        if (draft.name.isBlank()) return
        val item = SellerServiceItem(
            id = draft.id ?: "svc-${System.currentTimeMillis()}",
            name = draft.name,
            category = draft.category,
            gender = draft.gender,
            durationMinutes = draft.durationMinutes,
            price = price,
            homeService = draft.homeService,
            active = draft.active,
        )
        mutableUiState.update { state ->
            val next = if (draft.id == null) state.services + item else state.services.map { if (it.id == draft.id) item else it }
            state.copy(services = next, sheetOpen = false, draft = ServiceDraft())
        }
    }

    private fun updateDraft(block: (ServiceDraft) -> ServiceDraft) {
        mutableUiState.update { it.copy(draft = block(it.draft)) }
    }
}
