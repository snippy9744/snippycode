package com.snippyseat.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.snippyseat.app.data.auth.AuthRepository
import com.snippyseat.app.data.auth.GuestSessionManager
import com.snippyseat.app.navigation.Screen
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val phone: String = "",
    val otp: String = "",
    val otpSent: Boolean = false,
    val countdown: Int = 0,
    val loading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val guestSessionManager: GuestSessionManager,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = mutableUiState.asStateFlow()
    private var countdownJob: Job? = null

    fun updatePhone(value: String) {
        mutableUiState.value = mutableUiState.value.copy(
            phone = value.filter(Char::isDigit).take(10),
            error = null,
        )
    }

    fun updateOtp(value: String) {
        mutableUiState.value = mutableUiState.value.copy(
            otp = value.filter(Char::isDigit).take(6),
            error = null,
        )
    }

    fun sendOtp() {
        val phone = normalizedPhoneOrNull()
        if (phone == null) {
            mutableUiState.value = mutableUiState.value.copy(error = "Enter a valid 10 digit mobile number.")
            return
        }

        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(loading = true, error = null)
            runCatching { authRepository.sendOtp(phone) }
                .onSuccess {
                    mutableUiState.value = mutableUiState.value.copy(
                        otpSent = true,
                        loading = false,
                        countdown = it.expiresIn ?: 30,
                    )
                    startCountdown()
                }
                .onFailure { error ->
                    mutableUiState.value = mutableUiState.value.copy(
                        loading = false,
                        error = error.message ?: "Could not send OTP. Try again.",
                    )
                }
        }
    }

    fun verifyOtp(onNavigate: (String) -> Unit) {
        val phone = normalizedPhoneOrNull()
        val otp = mutableUiState.value.otp

        if (phone == null) {
            mutableUiState.value = mutableUiState.value.copy(error = "Enter a valid 10 digit mobile number.")
            return
        }

        if (otp.length != 6) {
            mutableUiState.value = mutableUiState.value.copy(error = "Enter the 6 digit OTP.")
            return
        }

        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(loading = true, error = null)
            runCatching { authRepository.verifyOtp(phone, otp) }
                .onSuccess { response ->
                    guestSessionManager.exitGuest()
                    mutableUiState.value = mutableUiState.value.copy(loading = false)
                    onNavigate(if (response.isNewUser) Screen.RoleSelection.route else Screen.UserHome.route)
                }
                .onFailure { error ->
                    mutableUiState.value = mutableUiState.value.copy(
                        loading = false,
                        error = error.message ?: "Wrong or expired OTP.",
                    )
                }
        }
    }

    fun continueWithGoogle(idToken: String?, onNavigate: (String) -> Unit) {
        if (idToken.isNullOrBlank()) {
            mutableUiState.value = mutableUiState.value.copy(error = "Google sign-in did not return an ID token.")
            return
        }

        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(loading = true, error = null)
            runCatching { authRepository.googleAuth(idToken) }
                .onSuccess { response ->
                    guestSessionManager.exitGuest()
                    mutableUiState.value = mutableUiState.value.copy(loading = false)
                    onNavigate(if (response.isNewUser) Screen.RoleSelection.route else Screen.UserHome.route)
                }
                .onFailure { error ->
                    mutableUiState.value = mutableUiState.value.copy(
                        loading = false,
                        error = error.message ?: "Google sign-in failed.",
                    )
                }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (mutableUiState.value.countdown > 0) {
                delay(1_000)
                mutableUiState.value = mutableUiState.value.copy(
                    countdown = (mutableUiState.value.countdown - 1).coerceAtLeast(0),
                )
            }
        }
    }

    private fun normalizedPhoneOrNull(): String? {
        val phone = mutableUiState.value.phone

        return if (phone.matches(Regex("^[6-9][0-9]{9}$"))) "+91$phone" else null
    }

    fun enterGuest(onNavigate: (String) -> Unit) {
        guestSessionManager.enterGuest()
        onNavigate(Screen.UserHome.route)
    }
}
