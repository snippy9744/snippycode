package com.snippyseat.app.feature.auth

import androidx.lifecycle.ViewModel
import com.snippyseat.app.data.auth.GuestSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GuestAccessViewModel @Inject constructor(
    private val guestSessionManager: GuestSessionManager,
) : ViewModel() {
    val isGuest = guestSessionManager.isGuest

    fun isGuestSession(): Boolean = guestSessionManager.isGuestSession()
    fun enterGuest() = guestSessionManager.enterGuest()
    fun exitGuest() = guestSessionManager.exitGuest()
}

