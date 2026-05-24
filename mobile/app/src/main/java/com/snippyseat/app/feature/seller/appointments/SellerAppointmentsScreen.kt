package com.snippyseat.app.feature.seller.appointments

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.snippyseat.app.data.seller.SellerAppointment
import com.snippyseat.app.data.seller.SellerAppointmentStatus
import com.snippyseat.app.ui.theme.Error
import com.snippyseat.app.ui.theme.JetBrainsMono
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.Success
import com.snippyseat.app.ui.theme.TextSecondary
import com.snippyseat.app.ui.theme.Warning

@Composable
fun SellerAppointmentsScreen(
    paddingValues: PaddingValues,
    viewModel: SellerAppointmentsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val visibleAppointments = state.appointments.filter { it.status == state.selectedTab }

    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(paddingValues)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Appointments", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(if (state.calendarMode) "Week view" else "List view", color = TextSecondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarViewWeek, contentDescription = null, tint = Primary)
                Switch(checked = state.calendarMode, onCheckedChange = viewModel::setCalendarMode)
            }
        }
        TabRow(selectedTabIndex = state.selectedTab.ordinal, containerColor = Color.White, contentColor = Primary) {
            SellerAppointmentStatus.entries.forEach { tab ->
                Tab(selected = state.selectedTab == tab, onClick = { viewModel.selectTab(tab) }, text = { Text(tab.label) })
            }
        }
        if (state.calendarMode) {
            WeekCalendarView(state.appointments)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (visibleAppointments.isEmpty()) {
                    item { EmptyAppointmentsState(state.selectedTab) }
                } else {
                    items(visibleAppointments, key = { it.id }) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            onComplete = { viewModel.markComplete(appointment) },
                            onCancel = { viewModel.openCancelDialog(appointment) },
                        )
                    }
                }
            }
        }
    }

    state.cancellationTarget?.let { target ->
        AlertDialog(
            onDismissRequest = viewModel::closeCancelDialog,
            title = { Text("Cancel appointment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add a reason so the customer gets a clear update.")
                    OutlinedTextField(
                        value = state.cancelReason,
                        onValueChange = viewModel::updateCancelReason,
                        minLines = 3,
                        label = { Text("Reason") },
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmCancel,
                    enabled = state.cancelReason.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                ) {
                    Text("Cancel")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = viewModel::closeCancelDialog) {
                    Text("Back")
                }
            },
        )
    }
}

@Composable
private fun AppointmentCard(
    appointment: SellerAppointment,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(8.dp), color = Color.White, shadowElevation = 2.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = appointment.customerAvatarUrl,
                    contentDescription = appointment.customerName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(54.dp).background(LightRed, CircleShape),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(appointment.customerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(appointment.services, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("Rs ${appointment.amount}", style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono), color = Primary, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(appointment.timeLabel, fontWeight = FontWeight.Bold)
                PaymentChip(appointment.paymentStatus.label)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                Text("Stylist: ${appointment.stylistName}", color = TextSecondary)
            }
            if (appointment.homeService) {
                Surface(color = Success.copy(alpha = 0.12f), contentColor = Success, shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.HomeWork, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Home service: ${appointment.address.orEmpty()} / travel fee Rs 80", fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (appointment.status == SellerAppointmentStatus.TODAY || appointment.status == SellerAppointmentStatus.UPCOMING) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onComplete, colors = ButtonDefaults.buttonColors(containerColor = Success), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Mark Complete")
                    }
                    OutlinedButton(onClick = onCancel, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentChip(label: String) {
    val paid = label == "Paid Online"
    Surface(color = if (paid) Success.copy(alpha = 0.12f) else Warning.copy(alpha = 0.12f), contentColor = if (paid) Success else Warning, shape = RoundedCornerShape(6.dp)) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WeekCalendarView(appointments: List<SellerAppointment>) {
    val colors = listOf(Primary, Warning, Success, Error, Primary, Success, Warning)
    Column(modifier = Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Week slots", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Surface(shape = RoundedCornerShape(8.dp), color = LightRed) {
            Canvas(modifier = Modifier.fillMaxWidth().height(360.dp).padding(12.dp)) {
                val dayWidth = size.width / 7f
                val hourHeight = size.height / 8f
                for (day in 0..7) {
                    drawLine(Color.White, Offset(day * dayWidth, 0f), Offset(day * dayWidth, size.height), strokeWidth = 2f)
                }
                for (hour in 0..8) {
                    drawLine(Color.White, Offset(0f, hour * hourHeight), Offset(size.width, hour * hourHeight), strokeWidth = 2f)
                }
                appointments.forEachIndexed { index, appointment ->
                    val day = index % 7
                    val hour = (index * 2) % 8
                    drawRoundRect(
                        color = colors[index % colors.size],
                        topLeft = Offset(day * dayWidth + 8f, hour * hourHeight + 8f),
                        size = Size(dayWidth - 16f, hourHeight * 1.2f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                    )
                }
            }
        }
        Text("Colored blocks represent confirmed appointments for the week.", color = TextSecondary)
    }
}

@Composable
private fun EmptyAppointmentsState(tab: SellerAppointmentStatus) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("No ${tab.label.lowercase()} appointments", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("New bookings will appear here.", color = TextSecondary)
    }
}
