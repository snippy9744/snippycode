package com.snippyseat.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.snippyseat.app.core.model.UserRole
import com.snippyseat.app.data.auth.TokenManager
import com.snippyseat.app.navigation.Screen
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AppStartViewModel @Inject constructor(
    tokenManager: TokenManager,
) : ViewModel() {
    val startDestination: StateFlow<String?> = tokenManager.session
        .map { session ->
            if (!session.isAuthenticated) {
                Screen.Splash.route
            } else {
                when (session.userRole) {
                    UserRole.SELLER -> Screen.SellerDashboard.route
                    UserRole.ADMIN -> Screen.UserHome.route
                    UserRole.USER,
                    null,
                    -> Screen.UserHome.route
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
