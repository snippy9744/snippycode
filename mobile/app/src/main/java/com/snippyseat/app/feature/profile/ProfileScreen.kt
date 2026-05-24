package com.snippyseat.app.feature.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.snippyseat.app.data.user.UserProfile
import com.snippyseat.app.ui.theme.DarkRed
import com.snippyseat.app.ui.theme.Error
import com.snippyseat.app.ui.theme.JetBrainsMono
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.Surface
import com.snippyseat.app.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    paddingValues: PaddingValues,
    onNavigateNotifications: () -> Unit,
    onNavigatePremium: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = state.profile
    val avatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.updateAvatar(uri)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item {
            AnimatedVisibility(profile.isBlocked) {
                BlockedBanner()
            }
        }
        item {
            ProfileHeader(
                profile = profile,
                avatarModel = state.localAvatarUri ?: profile.avatarUrl,
                onEditAvatar = { avatarLauncher.launch("image/*") },
            )
        }
        item { StatsRow(profile) }
        item {
            PremiumCard(profile = profile, onClick = onNavigatePremium)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ProfileMenuItem(Icons.Outlined.BookmarkBorder, "Saved Salons", "Your favourite places") { }
                ProfileMenuItem(Icons.Outlined.CreditCard, "Payment Methods", "Gateway placeholder") { }
                ProfileMenuItem(Icons.Outlined.Notifications, "Notifications settings", "Bookings, promos, reminders", onNavigateNotifications)
                ProfileMenuItem(Icons.Outlined.Campaign, "Referral code", "SNIPPYMANU") { }
                ProfileMenuItem(Icons.Outlined.HelpOutline, "Help & Support", "Chat, call, or email support") { }
                ProfileMenuItem(Icons.Outlined.Policy, "Terms & Privacy", "Policies and account rules") { }
                ProfileMenuItem(Icons.Outlined.Logout, "Logout", "Sign out of this device") {
                    viewModel.setLogoutDialog(true)
                }
            }
        }
    }

    if (state.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setLogoutDialog(false) },
            title = { Text("Logout?") },
            text = { Text("You will need to sign in again to book or manage appointments.") },
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
private fun BlockedBanner() {
    Surface(color = Error, contentColor = Color.White, shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Your account is blocked due to 3 no-show cancellations.", fontWeight = FontWeight.Bold)
            Text("Contact support to unblock your account.", color = Color.White.copy(alpha = 0.86f))
            OutlinedButton(onClick = { }, shape = RoundedCornerShape(8.dp)) {
                Text("Contact Support", color = Color.White)
            }
        }
    }
}

@Composable
private fun ProfileHeader(profile: UserProfile, avatarModel: Any?, onEditAvatar: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box {
            AsyncImage(
                model = avatarModel,
                contentDescription = profile.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(86.dp).clip(CircleShape).background(LightRed),
            )
            IconButton(
                onClick = onEditAvatar,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(30.dp)
                    .background(Primary, CircleShape),
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = "Edit avatar", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(profile.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(profile.phone, color = TextSecondary)
            Text(profile.email, color = TextSecondary)
        }
    }
}

@Composable
private fun StatsRow(profile: UserProfile) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        StatTile("Bookings", profile.bookingsCount.toString(), Modifier.weight(1f))
        StatTile("Saved", profile.savedSalons.toString(), Modifier.weight(1f))
        StatTile("Reviews", profile.reviewsGiven.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier) {
    Surface(modifier = modifier, color = LightRed, shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono), color = Primary, fontWeight = FontWeight.Bold)
            Text(label, color = TextSecondary)
        }
    }
}

@Composable
private fun PremiumCard(profile: UserProfile, onClick: () -> Unit) {
    val brush = if (profile.premiumActive) {
        Brush.horizontalGradient(listOf(Primary, DarkRed))
    } else {
        Brush.horizontalGradient(listOf(LightRed, Color.White))
    }
    Surface(shape = RoundedCornerShape(8.dp), color = Color.Transparent, modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush, RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Premium Membership", color = if (profile.premiumActive) Color.White else Primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (profile.premiumActive) "Active until ${profile.premiumExpiry}" else "Unlock priority slots and discounts",
                    color = if (profile.premiumActive) Color.White.copy(alpha = 0.86f) else TextSecondary,
                )
            }
            Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = if (profile.premiumActive) Color.White else Primary)
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = Surface,
        shape = RoundedCornerShape(8.dp),
    ) {
        ListItem(
            headlineContent = { Text(title, fontWeight = FontWeight.Bold) },
            supportingContent = { Text(subtitle, color = TextSecondary) },
            leadingContent = { Icon(icon, contentDescription = null, tint = Primary) },
            trailingContent = { Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextSecondary) },
        )
    }
}
