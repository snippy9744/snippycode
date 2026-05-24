package com.snippyseat.app.feature.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.snippyseat.app.data.user.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProfileUiState(
    val profile: UserProfile = UserProfile(
        name = "Manu S",
        phone = "+91 98765 43210",
        email = "manu@snippyseat.in",
        avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e",
        bookingsCount = 18,
        savedSalons = 6,
        reviewsGiven = 11,
        premiumActive = true,
        premiumExpiry = "28 Jun 2026",
        isBlocked = false,
    ),
    val localAvatarUri: Uri? = null,
    val showLogoutDialog: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val mutableUiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = mutableUiState.asStateFlow()

    fun updateAvatar(uri: Uri?) {
        if (uri != null) {
            mutableUiState.update { it.copy(localAvatarUri = uri) }
        }
    }

    fun setLogoutDialog(visible: Boolean) {
        mutableUiState.update { it.copy(showLogoutDialog = visible) }
    }
}
