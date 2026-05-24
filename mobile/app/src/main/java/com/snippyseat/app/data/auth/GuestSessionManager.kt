package com.snippyseat.app.data.auth

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class GuestSessionManager @Inject constructor() {
    private val mutableIsGuest = MutableStateFlow(false)
    val isGuest: StateFlow<Boolean> = mutableIsGuest.asStateFlow()

    fun enterGuest() {
        mutableIsGuest.value = true
    }

    fun exitGuest() {
        mutableIsGuest.value = false
    }

    fun isGuestSession(): Boolean = mutableIsGuest.value
}

