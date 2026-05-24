package com.snippyseat.app.feature.premium

import androidx.lifecycle.ViewModel
import com.snippyseat.app.data.user.PremiumPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PremiumUiState(
    val plan: PremiumPlan = PremiumPlan(
        monthlyPrice = 199,
        active = false,
        expiryLabel = "22 Jun 2026",
    ),
    val benefits: List<String> = listOf(
        "Priority slots during peak hours",
        "Exclusive discounts on salon services",
        "No convenience fee on eligible bookings",
        "Early access to promoted salon offers",
    ),
)

@HiltViewModel
class PremiumViewModel @Inject constructor() : ViewModel() {
    private val mutableUiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = mutableUiState.asStateFlow()

    fun subscribe() {
        mutableUiState.update {
            it.copy(plan = it.plan.copy(active = true))
        }
    }
}
