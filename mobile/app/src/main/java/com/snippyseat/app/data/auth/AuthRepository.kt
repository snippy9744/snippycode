package com.snippyseat.app.data.auth

import com.snippyseat.app.core.model.UserRole
import com.snippyseat.app.data.network.SnippySeatApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: SnippySeatApi,
    private val tokenManager: TokenManager,
) {
    suspend fun sendOtp(phone: String): SendOtpResponse {
        return api.sendOtp(SendOtpRequest(phone)).data ?: SendOtpResponse(phone = phone, expiresIn = 30)
    }

    suspend fun verifyOtp(phone: String, otp: String): LoginResponse {
        val response = api.verifyOtp(VerifyOtpRequest(phone = phone, otp = otp)).data
            ?: LoginResponse(isNewUser = true)
        persistAuthResponse(response)
        return response
    }

    suspend fun googleAuth(idToken: String): LoginResponse {
        val response = api.googleAuth(GoogleAuthRequest(idToken)).data ?: LoginResponse(isNewUser = true)
        persistAuthResponse(response)
        return response
    }

    suspend fun selectRole(role: UserRole): LoginResponse {
        val response = api.selectRole(RoleSelectionRequest(role = role.name)).data
            ?: LoginResponse(isNewUser = false, user = AuthUser(roleRaw = role.name))
        persistAuthResponse(response.copy(user = response.user ?: AuthUser(roleRaw = role.name)))
        return response
    }

    private suspend fun persistAuthResponse(response: LoginResponse) {
        val role = UserRole.from(response.user?.role)
        if (!response.accessToken.isNullOrBlank() && !response.refreshToken.isNullOrBlank() && role != null) {
            tokenManager.saveTokens(response.accessToken, response.refreshToken, role)
            return
        }

        val temporaryToken = response.temporaryToken ?: response.tempToken ?: response.token
        if (!temporaryToken.isNullOrBlank()) {
            tokenManager.saveTemporaryToken(temporaryToken)
        }
    }
}
