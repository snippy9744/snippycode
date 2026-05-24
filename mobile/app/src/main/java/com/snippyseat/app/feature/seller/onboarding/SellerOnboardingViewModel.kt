package com.snippyseat.app.feature.seller.onboarding

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.snippyseat.app.data.seller.SellerAccountType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val LAST_STEP_INDEX = 6

data class SellerOnboardingUiState(
    val step: Int = 0,
    val accountType: SellerAccountType = SellerAccountType.SHOP,
    val shopName: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val radiusKm: Float = 8f,
    val gstNumber: String = "",
    val shopPhotos: List<Uri> = emptyList(),
    val aadhaarFront: Uri? = null,
    val aadhaarBack: Uri? = null,
    val selfie: Uri? = null,
    val serviceName: String = "",
    val servicePrice: String = "",
    val error: String? = null,
)

@HiltViewModel
class SellerOnboardingViewModel @Inject constructor() : ViewModel() {
    private val mutableUiState = MutableStateFlow(SellerOnboardingUiState())
    val uiState: StateFlow<SellerOnboardingUiState> = mutableUiState.asStateFlow()

    fun selectAccountType(type: SellerAccountType) = update { it.copy(accountType = type, error = null) }
    fun updateShopName(value: String) = update { it.copy(shopName = value, error = null) }
    fun updateOwnerName(value: String) = update { it.copy(ownerName = value, error = null) }
    fun updatePhone(value: String) = update { it.copy(phone = value, error = null) }
    fun updateEmail(value: String) = update { it.copy(email = value, error = null) }
    fun updateAddress(value: String) = update { it.copy(address = value, error = null) }
    fun updateRadius(value: Float) = update { it.copy(radiusKm = value, error = null) }
    fun updateGst(value: String) = update { it.copy(gstNumber = value.uppercase(), error = null) }
    fun updateServiceName(value: String) = update { it.copy(serviceName = value, error = null) }
    fun updateServicePrice(value: String) = update { it.copy(servicePrice = value.filter(Char::isDigit), error = null) }

    fun addShopPhotos(uris: List<Uri>) {
        update { it.copy(shopPhotos = (it.shopPhotos + uris).take(10), error = null) }
    }

    fun setAadhaarFront(uri: Uri?) = update { it.copy(aadhaarFront = uri, error = null) }
    fun setAadhaarBack(uri: Uri?) = update { it.copy(aadhaarBack = uri, error = null) }
    fun setSelfie(uri: Uri?) = update { it.copy(selfie = uri, error = null) }

    fun next() {
        val state = mutableUiState.value
        val error = validate(state)
        if (error != null) {
            mutableUiState.update { it.copy(error = error) }
        } else {
            mutableUiState.update { it.copy(step = (it.step + 1).coerceAtMost(LAST_STEP_INDEX), error = null) }
        }
    }

    fun back() {
        mutableUiState.update { it.copy(step = (it.step - 1).coerceAtLeast(0), error = null) }
    }

    fun skipService() {
        mutableUiState.update { it.copy(step = 5, serviceName = "", servicePrice = "", error = null) }
    }

    private fun validate(state: SellerOnboardingUiState): String? = when (state.step) {
        1 -> when {
            state.shopName.isBlank() -> "Enter shop name."
            state.ownerName.isBlank() -> "Enter owner name."
            state.phone.length < 10 -> "Enter a valid phone number."
            !state.email.contains("@") -> "Enter a valid email."
            else -> null
        }
        2 -> if (state.address.isBlank()) "Enter service address." else null
        3 -> if (state.accountType == SellerAccountType.SHOP) {
            when {
                !gstRegex.matches(state.gstNumber) -> "Enter a valid GST number."
                state.shopPhotos.size < 2 -> "Add at least 2 shop photos."
                else -> null
            }
        } else {
            if (state.aadhaarFront == null || state.aadhaarBack == null || state.selfie == null) {
                "Add Aadhaar front, back, and selfie."
            } else {
                null
            }
        }
        4 -> if (state.serviceName.isBlank() && state.servicePrice.isBlank()) {
            null
        } else if (state.serviceName.isBlank() || state.servicePrice.isBlank()) {
            "Complete service name and price, or skip."
        } else {
            null
        }
        else -> null
    }

    private fun update(block: (SellerOnboardingUiState) -> SellerOnboardingUiState) {
        mutableUiState.update(block)
    }
}

private val gstRegex = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")
