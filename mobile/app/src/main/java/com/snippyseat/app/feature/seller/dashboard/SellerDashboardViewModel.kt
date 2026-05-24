package com.snippyseat.app.feature.seller.dashboard

import androidx.lifecycle.ViewModel
import com.snippyseat.app.data.seller.SellerDashboard
import com.snippyseat.app.data.seller.sampleSellerDashboard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SellerDashboardUiState(
    val dashboard: SellerDashboard = sampleSellerDashboard,
)

@HiltViewModel
class SellerDashboardViewModel @Inject constructor() : ViewModel() {
    private val mutableUiState = MutableStateFlow(SellerDashboardUiState())
    val uiState: StateFlow<SellerDashboardUiState> = mutableUiState.asStateFlow()
}
