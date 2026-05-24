package com.snippyseat.app.feature.bookings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.snippyseat.app.data.user.RefundStatus
import com.snippyseat.app.data.user.UserBooking
import com.snippyseat.app.data.user.UserBookingStatus
import com.snippyseat.app.ui.theme.Error
import com.snippyseat.app.ui.theme.JetBrainsMono
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.Success
import com.snippyseat.app.ui.theme.TextSecondary
import com.snippyseat.app.ui.theme.Warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    paddingValues: PaddingValues,
    viewModel: BookingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val visibleBookings = state.bookings.filter {
        when (state.selectedTab) {
            BookingsTab.UPCOMING -> it.status == UserBookingStatus.CONFIRMED || it.status == UserBookingStatus.PENDING
            BookingsTab.COMPLETED -> it.status == UserBookingStatus.COMPLETED
            BookingsTab.CANCELLED -> it.status == UserBookingStatus.CANCELLED
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues),
    ) {
        Text(
            "My bookings",
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        TabRow(selectedTabIndex = state.selectedTab.ordinal, containerColor = Color.White, contentColor = Primary) {
            BookingsTab.entries.forEach { tab ->
                Tab(selected = state.selectedTab == tab, onClick = { viewModel.selectTab(tab) }, text = { Text(tab.label) })
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (visibleBookings.isEmpty()) {
                item { EmptyBookingsState(state.selectedTab) }
            } else {
                items(visibleBookings, key = { it.id }) { booking ->
                    BookingCard(
                        booking = booking,
                        onCancel = { viewModel.openCancellation(booking) },
                        onReview = { viewModel.openReview(booking) },
                    )
                }
            }
        }
    }

    state.cancellationTarget?.let { booking ->
        CancellationBottomSheet(
            booking = booking,
            warningCount = state.warningCount,
            onDismiss = viewModel::closeCancellation,
            onConfirm = viewModel::confirmCancellation,
        )
    }

    state.reviewTarget?.let { booking ->
        ReviewBottomSheet(
            booking = booking,
            onDismiss = viewModel::closeReview,
            onSubmit = viewModel::submitReview,
        )
    }
}

@Composable
private fun BookingCard(
    booking: UserBooking,
    onCancel: () -> Unit,
    onReview: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(
                model = booking.salonPhotoUrl,
                contentDescription = booking.salonName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(88.dp).background(LightRed, RoundedCornerShape(8.dp)),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(booking.salonName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    StatusChip(booking.status)
                }
                Text(booking.servicesSummary, color = TextSecondary)
                Text(booking.dateTimeLabel, fontWeight = FontWeight.Bold)
                Text("Rs ${booking.amount}", style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono), color = Primary, fontWeight = FontWeight.Bold)
                when (booking.status) {
                    UserBookingStatus.CONFIRMED, UserBookingStatus.PENDING -> UpcomingActions(booking, onCancel)
                    UserBookingStatus.COMPLETED -> CompletedActions(booking, onReview)
                    UserBookingStatus.CANCELLED -> CancelledRefundChip(booking.refundStatus)
                }
            }
        }
    }
}

@Composable
private fun UpcomingActions(booking: UserBooking, onCancel: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = onCancel, shape = RoundedCornerShape(8.dp)) {
            Text("Cancel")
        }
        if (booking.minutesUntilStart > 30) {
            Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(8.dp)) {
                Text("Reschedule")
            }
        }
    }
}

@Composable
private fun CompletedActions(booking: UserBooking, onReview: () -> Unit) {
    AnimatedVisibility(visible = booking.daysSinceService in 0..7) {
        Button(onClick = onReview, colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(8.dp)) {
            Text("Rate & Review")
        }
    }
}

@Composable
private fun StatusChip(status: UserBookingStatus) {
    val color = when (status) {
        UserBookingStatus.CONFIRMED, UserBookingStatus.COMPLETED -> Success
        UserBookingStatus.PENDING -> Warning
        UserBookingStatus.CANCELLED -> TextSecondary
    }
    Surface(color = color.copy(alpha = 0.12f), contentColor = color, shape = RoundedCornerShape(6.dp)) {
        Text(status.label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun CancelledRefundChip(refundStatus: RefundStatus?) {
    Surface(color = LightRed, contentColor = Primary, shape = RoundedCornerShape(6.dp)) {
        Text(refundStatus?.label ?: "Cancelled", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyBookingsState(tab: BookingsTab) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("No ${tab.label.lowercase()} bookings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Your appointments will show up here.", color = TextSecondary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CancellationBottomSheet(
    booking: UserBooking,
    warningCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val canCancel = booking.minutesUntilStart > 15
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Cancel booking", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Free cancellation is available until 15 minutes before the appointment. Late cancellations are blocked and count toward account warnings.", color = TextSecondary)
            Surface(
                color = if (canCancel) Success.copy(alpha = 0.12f) else Error.copy(alpha = 0.12f),
                contentColor = if (canCancel) Success else Error,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = if (canCancel) "Refund amount: Rs ${booking.amount}" else "Too late to cancel",
                    modifier = Modifier.padding(12.dp),
                    fontWeight = FontWeight.Bold,
                )
            }
            Text("Warning count: $warningCount/3 warnings", color = Warning, fontWeight = FontWeight.Bold)
            Button(
                onClick = onConfirm,
                enabled = canCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("Confirm Cancel")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewBottomSheet(
    booking: UserBooking,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
) {
    var rating by remember { mutableIntStateOf(5) }
    var review by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri = uri
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Rate ${booking.salonName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { star ->
                    val selected = star <= rating
                    val scale by animateFloatAsState(if (selected) 1.18f else 1f, label = "review-star-scale")
                    IconButton(onClick = { rating = star }, modifier = Modifier.scale(scale)) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = if (selected) Warning else Color(0xFFD8D8D8))
                    }
                }
            }
            OutlinedTextField(
                value = review,
                onValueChange = { review = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                label = { Text("Review") },
            )
            FilterChip(
                selected = photoUri != null,
                onClick = { launcher.launch("image/*") },
                label = { Text(if (photoUri == null) "Add photo" else "Photo attached") },
                leadingIcon = { Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = LightRed, selectedLabelColor = Primary),
            )
            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("Submit Review")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
