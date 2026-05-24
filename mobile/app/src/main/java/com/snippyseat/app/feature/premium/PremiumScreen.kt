package com.snippyseat.app.feature.premium

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snippyseat.app.ui.theme.DarkRed
import com.snippyseat.app.ui.theme.JetBrainsMono
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.Success
import com.snippyseat.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun PremiumScreen(
    onBack: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val visibleBenefits = remember { mutableStateMapOf<Int, Boolean>() }

    LaunchedEffect(Unit) {
        state.benefits.indices.forEach { index ->
            delay(110)
            visibleBenefits[index] = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
            Text("Premium", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        Surface(shape = RoundedCornerShape(8.dp), color = Color.Transparent) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Primary, DarkRed)), RoundedCornerShape(8.dp))
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(modifier = Modifier.size(72.dp).background(Color.White.copy(alpha = 0.16f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Text("Snippy Seat Premium", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Faster bookings, fewer fees, better offers.", color = Color.White.copy(alpha = 0.86f), textAlign = TextAlign.Center)
            }
        }
        PriceCard(
            price = state.plan.monthlyPrice,
            active = state.plan.active,
            expiry = state.plan.expiryLabel,
            onSubscribe = viewModel::subscribe,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Benefits", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            state.benefits.forEachIndexed { index, benefit ->
                AnimatedVisibility(visible = visibleBenefits[index] == true, enter = fadeIn()) {
                    BenefitRow(text = benefit, index = index)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun PriceCard(
    price: Int,
    active: Boolean,
    expiry: String,
    onSubscribe: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(8.dp), color = LightRed) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Monthly plan", color = TextSecondary)
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Rs $price", style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMono), color = Primary, fontWeight = FontWeight.Bold)
                Text("/month", color = TextSecondary, modifier = Modifier.padding(bottom = 3.dp))
            }
            if (active) {
                Surface(color = Success.copy(alpha = 0.12f), contentColor = Success, shape = RoundedCornerShape(8.dp)) {
                    Text("Active until $expiry", modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(8.dp)) {
                    Text("Manage Membership")
                }
            } else {
                Button(
                    onClick = onSubscribe,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Subscribe - Payment Gateway Placeholder")
                }
            }
        }
    }
}

@Composable
private fun BenefitRow(text: String, index: Int) {
    val scale by animateFloatAsState(targetValue = 1f + (index * 0.01f), label = "benefit-check")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(30.dp).scale(scale).background(Success.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Success, modifier = Modifier.size(18.dp))
        }
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
