package com.snippyseat.app.feature.booking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snippyseat.app.data.booking.BookingVisitType
import com.snippyseat.app.data.booking.PaymentMethodOption
import com.snippyseat.app.data.booking.PriceBreakdown
import com.snippyseat.app.ui.theme.JetBrainsMono
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.Success
import com.snippyseat.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun BookingSummaryScreen(
    onBack: () -> Unit,
    onConfirmed: () -> Unit,
    viewModel: BookingSummaryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val draft = state.draft

    Scaffold(
        bottomBar = {
            Surface(shadowElevation = 10.dp, color = Color.White) {
                Button(
                    onClick = { viewModel.confirmBooking(onConfirmed) },
                    enabled = state.isOnline && !state.confirming && draft.salon != null && draft.selectedSlot != null,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(18.dp)
                        .height(52.dp),
                ) {
                    if (state.confirming) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.height(20.dp))
                    } else {
                        Text(if (state.isOnline) "Confirm Booking" else "Offline")
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(bottom = innerPadding.calculateBottomPadding())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
                Text("Booking summary", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = LightRed) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(draft.salon?.name.orEmpty(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(draft.salon?.address.orEmpty(), color = TextSecondary)
                    Text("${draft.selectedDate ?: ""} / ${draft.selectedSlot?.label.orEmpty()} / ${draft.selectedStaff?.name ?: "Any stylist"}")
                }
            }
            SectionTitle("Services")
            draft.selectedServices.forEach { service ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(service.name)
                    Text("Rs ${service.price}", fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold)
                }
            }
            VisitTypeToggle(
                selected = draft.visitType,
                homeEnabled = draft.salon?.offersHomeService == true,
                onSelected = viewModel::selectVisitType,
            )
            AnimatedVisibility(draft.visitType == BookingVisitType.HOME_SERVICE) {
                OutlinedTextField(
                    value = draft.homeAddress,
                    onValueChange = viewModel::updateHomeAddress,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    label = { Text("Home address") },
                    placeholder = { Text("Flat, street, landmark") },
                )
            }
            PriceBreakdownCard(
                breakdown = state.priceBreakdown,
                expanded = state.breakdownExpanded,
                onToggle = viewModel::toggleBreakdown,
            )
            PaymentSelector(
                selected = draft.paymentMethod,
                onSelected = viewModel::selectPayment,
            )
            if (state.priceBreakdown.premiumDiscount > 0) {
                Surface(color = Success.copy(alpha = 0.12f), contentColor = Success, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        "Premium discount applied: Rs ${state.priceBreakdown.premiumDiscount}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun VisitTypeToggle(
    selected: BookingVisitType,
    homeEnabled: Boolean,
    onSelected: (BookingVisitType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Location")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BookingVisitType.entries.forEach { type ->
                FilterChip(
                    selected = selected == type,
                    enabled = type != BookingVisitType.HOME_SERVICE || homeEnabled,
                    onClick = { onSelected(type) },
                    label = { Text(type.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White,
                        containerColor = LightRed,
                        labelColor = Primary,
                    ),
                )
            }
        }
    }
}

@Composable
private fun PriceBreakdownCard(
    breakdown: PriceBreakdown,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val visibleLines = remember { mutableStateMapOf<Int, Boolean>() }
    val lines = listOf(
        "Services subtotal" to breakdown.servicesSubtotal,
        "Convenience fee" to breakdown.convenienceFee,
        "Home travel fee" to breakdown.homeTravelFee,
        "GST / tax" to breakdown.tax,
        "Platform fee" to breakdown.platformFee,
        "Premium discount" to -breakdown.premiumDiscount,
    ).filter { it.second != 0 }

    LaunchedEffect(expanded, breakdown) {
        visibleLines.clear()
        if (expanded) {
            lines.indices.forEach { index ->
                delay(70)
                visibleLines[index] = true
            }
        }
    }

    Surface(shape = RoundedCornerShape(8.dp), shadowElevation = 2.dp, color = Color.White) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle("Price breakdown")
                Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, contentDescription = null)
            }
            AnimatedVisibility(expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    lines.forEachIndexed { index, line ->
                        AnimatedVisibility(visible = visibleLines[index] == true, enter = fadeIn()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(line.first, color = TextSecondary)
                                Text("Rs ${line.second}", fontFamily = JetBrainsMono)
                            }
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Rs ${breakdown.total}", style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PaymentSelector(
    selected: PaymentMethodOption,
    onSelected: (PaymentMethodOption) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Payment method")
        PaymentMethodOption.entries.forEach { method ->
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onSelected(method) },
                shape = RoundedCornerShape(8.dp),
                color = if (selected == method) LightRed else Color.White,
                shadowElevation = 1.dp,
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected == method, onClick = { onSelected(method) })
                    Column {
                        Text(method.label, fontWeight = FontWeight.Bold)
                        Text(if (method == PaymentMethodOption.PAY_ONLINE) "Payment gateway placeholder" else "Cash or card at counter", color = TextSecondary)
                    }
                }
            }
        }
    }
}
