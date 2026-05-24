package com.snippyseat.app.feature.seller.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.snippyseat.app.ui.theme.Warning

@Composable
fun SellerSettingsScreen(
    paddingValues: PaddingValues,
    onOpenBlocked: () -> Unit,
    viewModel: SellerSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        viewModel.addPhotos(uris)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Seller settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        SectionTitle("Shop profile")
        OutlinedTextField(state.shopName, viewModel::updateShopName, modifier = Modifier.fillMaxWidth(), label = { Text("Shop name") })
        OutlinedTextField(state.description, viewModel::updateDescription, modifier = Modifier.fillMaxWidth(), minLines = 3, label = { Text("Description") })
        Surface(modifier = Modifier.fillMaxWidth().clickable { photoLauncher.launch("image/*") }, color = LightRed, shape = RoundedCornerShape(8.dp)) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, tint = Primary)
                Text("Shop photos: ${state.photos.size}/10 selected", fontWeight = FontWeight.Bold)
            }
        }
        SectionTitle("Address")
        OutlinedTextField(state.address, viewModel::updateAddress, modifier = Modifier.fillMaxWidth(), minLines = 2, label = { Text("Address") })
        Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(LightRed, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Primary, modifier = Modifier.size(38.dp))
                Text("Map placeholder", color = Primary, fontWeight = FontWeight.Bold)
            }
        }
        SettingInfo("GST number", state.gstNumber)
        OutlinedTextField(state.instagram, viewModel::updateInstagram, modifier = Modifier.fillMaxWidth(), label = { Text("Instagram") })
        ToggleRow(Icons.Outlined.Star, "Featured listing", "Paid promotion - payment placeholder", state.featured, viewModel::updateFeatured)
        SubscriptionCard()
        SectionTitle("Notifications")
        ToggleRow(Icons.Outlined.Payments, "Booking updates", "New slots, payments, cancellations", state.bookingNotifications, viewModel::updateBookingNotifications)
        ToggleRow(Icons.Outlined.Star, "Promotions", "Offers and seller tips", state.promoNotifications, viewModel::updatePromoNotifications)
        SettingsItem(Icons.Outlined.HelpOutline, "Help & Support", "Open email support") {
            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@snippyseat.in")))
        }
        SettingsItem(Icons.Outlined.Block, "Blocked screen preview", "Review account restriction copy", onOpenBlocked)
        SettingsItem(Icons.Outlined.Logout, "Logout", "Sign out from seller account") {
            viewModel.setLogoutDialog(true)
        }
        Spacer(Modifier.height(20.dp))
    }

    if (state.logoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setLogoutDialog(false) },
            title = { Text("Logout?") },
            text = { Text("You will stop receiving seller updates on this device until you sign in again.") },
            confirmButton = {
                Button(onClick = { viewModel.setLogoutDialog(false) }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("Logout")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.setLogoutDialog(false) }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
fun SellerBlockedScreen(
    sellerType: SellerAccountType = SellerAccountType.SHOP,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val message = if (sellerType == SellerAccountType.SHOP) {
        "Your account is under review. Contact support."
    } else {
        "Your account is pending admin verification. This usually takes 24-48 hours."
    }
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(modifier = Modifier.size(120.dp).background(Error.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Block, contentDescription = null, tint = Error, modifier = Modifier.size(64.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text("Seller account restricted", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(message, color = TextSecondary)
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@snippyseat.in"))) },
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text("Contact Support")
        }
        OutlinedButton(onClick = onLogout, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 8.dp)) {
            Text("Logout")
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}

@Composable
private fun SettingInfo(label: String, value: String) {
    Surface(color = LightRed, shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono), color = DarkRed, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SubscriptionCard() {
    Surface(color = Warning.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Subscription", fontWeight = FontWeight.Bold)
            Text("Trial active. Renews at Rs 999/month on 28 Jun 2026.", color = TextSecondary)
            Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(8.dp)) {
                Text("Renew - Payment Placeholder")
            }
        }
    }
}

@Composable
private fun ToggleRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Surface(shape = RoundedCornerShape(8.dp), color = Color.White, shadowElevation = 1.dp) {
        ListItem(
            headlineContent = { Text(title, fontWeight = FontWeight.Bold) },
            supportingContent = { Text(subtitle, color = TextSecondary) },
            leadingContent = { Icon(icon, contentDescription = null, tint = Primary) },
            trailingContent = { Switch(checked = checked, onCheckedChange = onChecked) },
        )
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(8.dp), color = Color.White, shadowElevation = 1.dp) {
        ListItem(
            headlineContent = { Text(title, fontWeight = FontWeight.Bold) },
            supportingContent = { Text(subtitle, color = TextSecondary) },
            leadingContent = { Icon(icon, contentDescription = null, tint = Primary) },
        )
    }
}
