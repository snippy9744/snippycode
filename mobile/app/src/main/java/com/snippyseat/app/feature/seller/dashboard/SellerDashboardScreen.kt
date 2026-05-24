package com.snippyseat.app.feature.seller.dashboard

import androidx.compose.animation.core.animateIntAsState
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
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AddBusiness
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snippyseat.app.data.seller.SellerAppointment
import com.snippyseat.app.data.seller.SellerApprovalStatus
import com.snippyseat.app.data.seller.SellerDashboard
import com.snippyseat.app.core.format.SnippyZoneId
import com.snippyseat.app.ui.theme.JetBrainsMono
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.Success
import com.snippyseat.app.ui.theme.TextSecondary
import com.snippyseat.app.ui.theme.Warning
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SellerDashboardScreen(
    paddingValues: PaddingValues,
    onViewAppointments: () -> Unit,
    onManageServices: () -> Unit,
    onManageStaff: () -> Unit,
    onViewEarnings: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenOnboarding: () -> Unit,
    viewModel: SellerDashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dashboard = state.dashboard

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(paddingValues),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { DashboardHeader(dashboard, onOpenOnboarding) }
        item { SubscriptionBanner(dashboard.subscriptionMessage) }
        item { StatsGrid(dashboard) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Upcoming appointments", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("View All", color = Primary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onViewAppointments))
            }
        }
        items(count = dashboard.upcomingAppointments.take(5).size) { index ->
            CompactAppointmentCard(dashboard.upcomingAppointments[index])
        }
        item {
            Text("Quick actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item {
            QuickActionsGrid(
                onManageServices = onManageServices,
                onManageStaff = onManageStaff,
                onViewEarnings = onViewEarnings,
                onOpenSettings = onOpenSettings,
            )
        }
    }
}

@Composable
private fun DashboardHeader(dashboard: SellerDashboard, onOpenOnboarding: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(dashboard.shopName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(LocalDate.now(SnippyZoneId).format(DateTimeFormatter.ofPattern("EEE, dd MMM")), color = TextSecondary)
        }
        ApprovalChip(dashboard.approvalStatus)
    }
    Spacer(Modifier.height(10.dp))
    Button(onClick = onOpenOnboarding, colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text("Open seller setup")
    }
}

@Composable
private fun ApprovalChip(status: SellerApprovalStatus) {
    val color = when (status) {
        SellerApprovalStatus.ACTIVE -> Success
        SellerApprovalStatus.PENDING_APPROVAL -> Warning
        SellerApprovalStatus.BLOCKED -> Primary
    }
    Surface(color = color.copy(alpha = 0.12f), contentColor = color, shape = RoundedCornerShape(6.dp)) {
        Row(modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Verified, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.size(5.dp))
            Text(status.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SubscriptionBanner(message: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = Warning.copy(alpha = 0.12f), contentColor = Warning) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.WarningAmber, contentDescription = null)
            Text(message, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatsGrid(dashboard: SellerDashboard) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Today's appts", dashboard.stats.todayAppointments, "", Modifier.weight(1f))
            StatCard("Revenue", dashboard.stats.revenueToday, "Rs ", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Pending", dashboard.stats.pending, "", Modifier.weight(1f))
            StatCard("Completed", dashboard.stats.completed, "", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int, prefix: String, modifier: Modifier) {
    val animated by animateIntAsState(value, label = "seller-stat-$label")
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = LightRed) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, color = TextSecondary)
            Text("$prefix$animated", style = MaterialTheme.typography.headlineSmall.copy(fontFamily = JetBrainsMono), color = Primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CompactAppointmentCard(appointment: SellerAppointment) {
    Surface(shape = RoundedCornerShape(8.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(42.dp).background(LightRed, CircleShape), contentAlignment = Alignment.Center) {
                Text(appointment.customerName.take(1), color = Primary, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(appointment.customerName, fontWeight = FontWeight.Bold)
                Text("${appointment.services} / ${appointment.timeLabel}", color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("Rs ${appointment.amount}", fontFamily = JetBrainsMono, color = Primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onManageServices: () -> Unit,
    onManageStaff: () -> Unit,
    onViewEarnings: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAction("Add Service", Icons.Outlined.AddBusiness, Modifier.weight(1f), onManageServices)
            QuickAction("Manage Staff", Icons.Outlined.Group, Modifier.weight(1f), onManageStaff)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAction("View Earnings", Icons.Outlined.AccountBalanceWallet, Modifier.weight(1f), onViewEarnings)
            QuickAction("Settings", Icons.Outlined.Settings, Modifier.weight(1f), onOpenSettings)
        }
    }
}

@Composable
private fun QuickAction(title: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(8.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, contentDescription = null, tint = Primary)
            Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}
