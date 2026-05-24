package com.snippyseat.app.feature.seller.earnings

import androidx.lifecycle.ViewModel
import com.snippyseat.app.data.seller.SellerTransaction
import com.snippyseat.app.data.seller.sampleSellerTransactions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class EarningsPeriod(val label: String) {
    TODAY("Today"),
    WEEK("Week"),
    MONTH("Month"),
    CUSTOM("Custom"),
}

data class SellerEarningsUiState(
    val selectedPeriod: EarningsPeriod = EarningsPeriod.TODAY,
    val transactions: List<SellerTransaction> = sampleSellerTransactions,
    val chartValues: List<Int> = listOf(820, 1240, 980, 1420, 1780, 2210, 1890),
) {
    val gross: Int = transactions.sumOf { it.gross }
    val commission: Int = transactions.sumOf { it.commission }
    val net: Int = transactions.sumOf { it.net }
}

@HiltViewModel
class SellerEarningsViewModel @Inject constructor() : ViewModel() {
    private val mutableUiState = MutableStateFlow(SellerEarningsUiState())
    val uiState: StateFlow<SellerEarningsUiState> = mutableUiState.asStateFlow()

    fun selectPeriod(period: EarningsPeriod) {
        mutableUiState.update { it.copy(selectedPeriod = period) }
    }
}
