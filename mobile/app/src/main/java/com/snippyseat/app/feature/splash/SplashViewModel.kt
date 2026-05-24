package com.snippyseat.app.feature.splash

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.snippyseat.app.core.model.UserRole
import com.snippyseat.app.data.auth.TokenManager
import com.snippyseat.app.navigation.Screen
import javax.inject.Inject
import kotlinx.coroutines.flow.first

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager,
) : ViewModel() {
    suspend fun resolveDestination(): String {
        val session = tokenManager.session.first()

        if (!session.isAuthenticated) {
            return Screen.Onboarding.route
        }

        return when (session.userRole) {
            UserRole.SELLER -> Screen.SellerDashboard.route
            UserRole.USER,
            UserRole.ADMIN,
            null,
            -> Screen.UserHome.route
        }
    }
}
