package com.snippyseat.app.feature.seller.staff

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.snippyseat.app.data.seller.SellerStaffMember
import com.snippyseat.app.data.seller.sampleSellerServices
import com.snippyseat.app.data.seller.sampleSellerStaff
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class StaffDraft(
    val id: String? = null,
    val name: String = "",
    val phone: String = "",
    val speciality: String = "",
    val photoUri: Uri? = null,
    val photoUrl: String? = null,
    val services: Set<String> = emptySet(),
    val available: Boolean = true,
)

data class SellerStaffUiState(
    val staff: List<SellerStaffMember> = sampleSellerStaff,
    val sheetOpen: Boolean = false,
    val draft: StaffDraft = StaffDraft(),
    val serviceOptions: List<String> = sampleSellerServices.map { it.name },
)

@HiltViewModel
class SellerStaffViewModel @Inject constructor() : ViewModel() {
    private val mutableUiState = MutableStateFlow(SellerStaffUiState())
    val uiState: StateFlow<SellerStaffUiState> = mutableUiState.asStateFlow()

    fun openAddSheet() = mutableUiState.update { it.copy(sheetOpen = true, draft = StaffDraft()) }
    fun openEditSheet(member: SellerStaffMember) = mutableUiState.update {
        it.copy(
            sheetOpen = true,
            draft = StaffDraft(
                id = member.id,
                name = member.name,
                phone = member.phone,
                speciality = member.speciality,
                photoUrl = member.photoUrl,
                services = member.services,
                available = member.available,
            ),
        )
    }
    fun closeSheet() = mutableUiState.update { it.copy(sheetOpen = false) }
    fun updateName(value: String) = updateDraft { it.copy(name = value) }
    fun updatePhone(value: String) = updateDraft { it.copy(phone = value) }
    fun updateSpeciality(value: String) = updateDraft { it.copy(speciality = value) }
    fun updatePhoto(uri: Uri?) = updateDraft { it.copy(photoUri = uri) }
    fun updateAvailable(value: Boolean) = updateDraft { it.copy(available = value) }

    fun toggleService(name: String) = updateDraft {
        it.copy(services = if (name in it.services) it.services - name else it.services + name)
    }

    fun toggleAvailability(member: SellerStaffMember) {
        mutableUiState.update { state ->
            state.copy(staff = state.staff.map { if (it.id == member.id) it.copy(available = !it.available) else it })
        }
    }

    fun saveStaff() {
        val draft = mutableUiState.value.draft
        if (draft.name.isBlank() || draft.phone.isBlank() || draft.speciality.isBlank()) return
        val member = SellerStaffMember(
            id = draft.id ?: "staff-${System.currentTimeMillis()}",
            name = draft.name,
            phone = draft.phone,
            speciality = draft.speciality,
            photoUrl = draft.photoUri?.toString() ?: draft.photoUrl,
            services = draft.services,
            available = draft.available,
        )
        mutableUiState.update { state ->
            val next = if (draft.id == null) state.staff + member else state.staff.map { if (it.id == draft.id) member else it }
            state.copy(staff = next, sheetOpen = false, draft = StaffDraft())
        }
    }

    private fun updateDraft(block: (StaffDraft) -> StaffDraft) {
        mutableUiState.update { it.copy(draft = block(it.draft)) }
    }
}
