package com.snippyseat.app.data.auth

import com.snippyseat.app.core.model.UserRole

data class AuthSession(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val temporaryToken: String? = null,
    val userRole: UserRole? = null,
) {
    val isAuthenticated: Boolean = !accessToken.isNullOrBlank() && userRole != null
}
