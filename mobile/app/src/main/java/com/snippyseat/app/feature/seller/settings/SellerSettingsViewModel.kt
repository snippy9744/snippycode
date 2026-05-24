package com.snippyseat.app.feature.seller.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.snippyseat.app.data.seller.SellerAccountType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SellerSettingsUiState(
    val shopName: String = "Red Chair Studio Bandra",
    val description: String = "Premium haircuts, beard styling, facials, and home grooming across Bandra.",
    val address: String = "Linking Road, Bandra West, Mumbai",
    val gstNumber: String = "27AABCR4825K1Z9",
    val instagram: String = "@redchairstudio",
    val photos: List<Uri> = emptyList(),
    val featured: Boolean = false,
    val bookingNotifications: Boolean = true,
    val promoNotifications: Boolean = true,
    val logoutDialog: Boolean = false,
    val sellerType: SellerAccountType = SellerAccountType.SHOP,
)

@HiltViewModel
class SellerSettingsViewModel @Inject constructor() : ViewModel() {
    private val mutableUiState = MutableStateFlow(SellerSettingsUiState())
    val uiState: StateFlow<SellerSettingsUiState> = mutableUiState.asStateFlow()

    fun updateShopName(value: String) = mutableUiState.update { it.copy(shopName = value) }
    fun updateDescription(value: String) = mutableUiState.update { it.copy(description = value) }
    fun updateAddress(value: String) = mutableUiState.update { it.copy(address = value) }
    fun updateInstagram(value: String) = mutableUiState.update { it.copy(instagram = value) }
    fun addPhotos(value: List<Uri>) = mutableUiState.update { it.copy(photos = (it.photos + value).take(10)) }
    fun updateFeatured(value: Boolean) = mutableUiState.update { it.copy(featured = value) }
    fun updateBookingNotifications(value: Boolean) = mutableUiState.update { it.copy(bookingNotifications = value) }
    fun updatePromoNotifications(value: Boolean) = mutableUiState.update { it.copy(promoNotifications = value) }
    fun setLogoutDialog(value: Boolean) = mutableUiState.update { it.copy(logoutDialog = value) }
}
