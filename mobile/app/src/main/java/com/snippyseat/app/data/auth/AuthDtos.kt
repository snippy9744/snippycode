package com.snippyseat.app.data.auth

import com.squareup.moshi.Json

data class SendOtpRequest(
    val phone: String,
)

data class SendOtpResponse(
    val phone: String? = null,
    val expiresIn: Int? = null,
)

data class VerifyOtpRequest(
    val phone: String,
    val otp: String,
)

data class GoogleAuthRequest(
    val idToken: String,
)

data class RoleSelectionRequest(
    val role: String,
)

data class LoginResponse(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val temporaryToken: String? = null,
    val tempToken: String? = null,
    val token: String? = null,
    val isNewUser: Boolean = false,
    val user: AuthUser? = null,
)

data class AuthUser(
    val id: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    @Json(name = "role")
    val roleRaw: String? = null,
) {
    val role: String? get() = roleRaw
}
