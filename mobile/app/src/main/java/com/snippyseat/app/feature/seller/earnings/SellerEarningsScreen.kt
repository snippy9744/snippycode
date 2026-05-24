package com.snippyseat.app.feature.seller.earnings

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snippyseat.app.data.seller.SellerTransaction
import com.snippyseat.app.ui.theme.JetBrainsMono
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.Success
import com.snippyseat.app.ui.theme.TextSecondary
import com.snippyseat.app.ui.theme.Warning

@Composable
fun SellerEarningsScreen(
    paddingValues: PaddingValues,
    viewModel: SellerEarningsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(paddingValues),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("Earnings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(EarningsPeriod.entries) { period ->
                    FilterChip(
                        selected = state.selectedPeriod == period,
                        onClick = { viewModel.selectPeriod(period) },
                        label = { Text(period.label) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary, selectedLabelColor = Color.White),
                    )
                }
            }
        }
        item { RevenueSummaryCard(state) }
        item { EarningsBarChart(state.chartValues) }
        item {
            Surface(color = LightRed, shape = RoundedCornerShape(8.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Commission breakdown", fontWeight = FontWeight.Bold)
                    Text("Booking fee: 10% / Home service commission: included in platform deduction", color = TextSecondary)
                    Text("Upcoming payout: 28 May 2026", color = Primary, fontWeight = FontWeight.Bold)
                }
            }
        }
        item { Text("Transactions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(state.transactions, key = { it.id }) { transaction ->
            TransactionRow(transaction)
        }
    }
}

@Composable
private fun RevenueSummaryCard(state: SellerEarningsUiState) {
    Surface(shape = RoundedCornerShape(8.dp), color = LightRed) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RevenueMetric("Gross", state.gross, Primary, Modifier.weight(1f))
            RevenueMetric("Commission", state.commission, Warning, Modifier.weight(1f))
            RevenueMetric("Net", state.net, Success, Modifier.weight(1f))
        }
    }
}

@Composable
private fun RevenueMetric(label: String, value: Int, color: Color, modifier: Modifier) {
    val animated by animateIntAsState(value, label = "earning-$label")
    Column(modifier = modifier) {
        Text(label, color = TextSecondary)
        Text("Rs $animated", style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono), color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EarningsBarChart(values: List<Int>) {
    val maxValue = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    Surface(shape = RoundedCornerShape(8.dp), color = Color.White, shadowElevation = 2.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Revenue trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Canvas(modifier = Modifier.fillMaxWidth().height(190.dp)) {
                val gap = 14f
                val barWidth = (size.width - gap * (values.size + 1)) / values.size
                values.forEachIndexed { index, value ->
                    val barHeight = size.height * (value / maxValue.toFloat())
                    val left = gap + index * (barWidth + gap)
                    drawRoundRect(
                        color = Primary.copy(alpha = 0.9f),
                        topLeft = Offset(left, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(12f, 12f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: SellerTransaction) {
    Surface(shape = RoundedCornerShape(8.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(transaction.bookingRef, fontWeight = FontWeight.Bold)
                Text("Net Rs ${transaction.net}", fontFamily = JetBrainsMono, color = Success, fontWeight = FontWeight.Bold)
            }
            Text(transaction.service, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Gross Rs ${transaction.gross}", color = TextSecondary)
                Text("Commission Rs ${transaction.commission}", color = Warning)
            }
        }
    }
}
