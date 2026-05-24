package com.snippyseat.app.feature.seller.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snippyseat.app.data.seller.SellerAccountType
import com.snippyseat.app.ui.theme.DarkRed
import com.snippyseat.app.ui.theme.Error
import com.snippyseat.app.ui.theme.JetBrainsMono
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.Success
import com.snippyseat.app.ui.theme.TextSecondary

@Composable
fun SellerOnboardingScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: SellerOnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val progress by animateFloatAsState((state.step + 1) / 7f, label = "seller-onboarding-progress")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding(),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = if (state.step == 0) onBack else viewModel::back) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Seller setup", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Step ${state.step + 1} of 7", color = TextSecondary)
            }
        }
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = Primary, trackColor = LightRed)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AnimatedContent(targetState = state.step, label = "seller-step") { step ->
                when (step) {
                    0 -> AccountTypeStep(state.accountType, viewModel::selectAccountType)
                    1 -> BasicInfoStep(state, viewModel)
                    2 -> LocationStep(state, viewModel)
                    3 -> DocumentsStep(state, viewModel)
                    4 -> FirstServiceStep(state, viewModel)
                    5 -> SubscriptionStep()
                    else -> PendingApprovalStep()
                }
            }
            state.error?.let {
                Surface(color = Error.copy(alpha = 0.12f), contentColor = Error, shape = RoundedCornerShape(8.dp)) {
                    Text(it, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.step == 4) {
                OutlinedButton(onClick = viewModel::skipService, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(8.dp)) {
                    Text("Skip")
                }
            }
            Button(
                onClick = if (state.step == 6) onDone else viewModel::next,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(52.dp),
            ) {
                Text(if (state.step == 6) "Go to Dashboard" else "Next")
            }
        }
    }
}

@Composable
private fun AccountTypeStep(selected: SellerAccountType, onSelected: (SellerAccountType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        StepTitle("Choose account type", "Tell us how you serve customers.")
        SellerAccountType.entries.forEach { type ->
            val active = selected == type
            val scale by animateFloatAsState(if (active) 1.02f else 1f, label = "account-type-scale")
            Card(
                modifier = Modifier.fillMaxWidth().scale(scale).clickable { onSelected(type) },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = if (active) LightRed else Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (active) Primary else Color(0xFFE6E6E6)),
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Icon(if (type == SellerAccountType.SHOP) Icons.Outlined.Store else Icons.Outlined.HomeWork, contentDescription = null, tint = Primary, modifier = Modifier.size(36.dp))
                    Column {
                        Text(type.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(if (type == SellerAccountType.SHOP) "List your salon location and team." else "Serve customers at their home.", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun BasicInfoStep(state: SellerOnboardingUiState, viewModel: SellerOnboardingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StepTitle("Basic info", "This appears on your seller profile.")
        OutlinedTextField(state.shopName, viewModel::updateShopName, modifier = Modifier.fillMaxWidth(), label = { Text("Shop name") })
        OutlinedTextField(state.ownerName, viewModel::updateOwnerName, modifier = Modifier.fillMaxWidth(), label = { Text("Owner name") })
        OutlinedTextField(state.phone, viewModel::updatePhone, modifier = Modifier.fillMaxWidth(), label = { Text("Phone") })
        OutlinedTextField(state.email, viewModel::updateEmail, modifier = Modifier.fillMaxWidth(), label = { Text("Email") })
    }
}

@Composable
private fun LocationStep(state: SellerOnboardingUiState, viewModel: SellerOnboardingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StepTitle("Location", "Add the base location customers will see.")
        OutlinedTextField(state.address, viewModel::updateAddress, modifier = Modifier.fillMaxWidth(), minLines = 2, label = { Text("Address") })
        Box(
            modifier = Modifier.fillMaxWidth().height(170.dp).background(LightRed, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Primary, modifier = Modifier.size(42.dp))
                Text("Map placeholder", color = Primary, fontWeight = FontWeight.Bold)
            }
        }
        if (state.accountType == SellerAccountType.HOME_SERVICE) {
            Text("Service radius: ${state.radiusKm.toInt()} km", fontWeight = FontWeight.Bold)
            Slider(value = state.radiusKm, onValueChange = viewModel::updateRadius, valueRange = 1f..30f)
        }
    }
}

@Composable
private fun DocumentsStep(state: SellerOnboardingUiState, viewModel: SellerOnboardingViewModel) {
    val shopPhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        viewModel.addShopPhotos(uris)
    }
    val frontLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setAadhaarFront(uri) }
    val backLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setAadhaarBack(uri) }
    val selfieLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setSelfie(uri) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StepTitle("Documents", "Verification keeps Snippy Seat trusted.")
        if (state.accountType == SellerAccountType.SHOP) {
            OutlinedTextField(state.gstNumber, viewModel::updateGst, modifier = Modifier.fillMaxWidth(), label = { Text("GST number") })
            UploadTile("Shop photos", "${state.shopPhotos.size}/10 added, minimum 2", onClick = { shopPhotoLauncher.launch("image/*") })
        } else {
            UploadTile("Aadhaar front", state.aadhaarFront.statusText(), onClick = { frontLauncher.launch("image/*") })
            UploadTile("Aadhaar back", state.aadhaarBack.statusText(), onClick = { backLauncher.launch("image/*") })
            UploadTile("Selfie with Aadhaar", state.selfie.statusText(), onClick = { selfieLauncher.launch("image/*") })
            Text("Your account will be reviewed by admin within 24-48 hours.", color = TextSecondary)
        }
    }
}

@Composable
private fun FirstServiceStep(state: SellerOnboardingUiState, viewModel: SellerOnboardingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StepTitle("First service", "Add one service now, or skip and add later.")
        OutlinedTextField(state.serviceName, viewModel::updateServiceName, modifier = Modifier.fillMaxWidth(), label = { Text("Service name") })
        OutlinedTextField(state.servicePrice, viewModel::updateServicePrice, modifier = Modifier.fillMaxWidth(), label = { Text("Price") }, prefix = { Text("Rs ") })
        Surface(color = LightRed, shape = RoundedCornerShape(8.dp)) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Chair, contentDescription = null, tint = Primary)
                Text("Duration and staff assignment can be tuned in Services after approval.", color = TextSecondary)
            }
        }
    }
}

@Composable
private fun SubscriptionStep() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        StepTitle("Subscription", "Start listing with the seller plan.")
        Surface(shape = RoundedCornerShape(8.dp), color = LightRed) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Payments, contentDescription = null, tint = Primary, modifier = Modifier.size(42.dp))
                Text("Rs 999/month", style = MaterialTheme.typography.headlineSmall.copy(fontFamily = JetBrainsMono), color = Primary, fontWeight = FontWeight.Bold)
                Text("30 day trial included. Payment gateway placeholder will connect here.", color = TextSecondary, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun PendingApprovalStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(modifier = Modifier.size(126.dp).background(Success.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(78.dp))
        }
        Text("Pending approval", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("We are reviewing your details. You will receive a notification once approved.", color = TextSecondary, textAlign = TextAlign.Center)
        Surface(color = DarkRed.copy(alpha = 0.1f), contentColor = DarkRed, shape = RoundedCornerShape(8.dp)) {
            Text("Status: PENDING_APPROVAL", modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StepTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(subtitle, color = TextSecondary)
    }
}

@Composable
private fun UploadTile(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), color = LightRed, shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.AddAPhoto, contentDescription = null, tint = Primary)
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextSecondary)
            }
        }
    }
}

private fun Uri?.statusText(): String = if (this == null) "Tap to upload" else "Uploaded"
