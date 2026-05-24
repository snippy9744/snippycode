package com.snippyseat.app.feature.booking

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snippyseat.app.data.booking.SalonStaff
import com.snippyseat.app.data.booking.TimeSlot
import com.snippyseat.app.feature.auth.GuestAccessViewModel
import com.snippyseat.app.feature.auth.GuestLoginPromptSheet
import com.snippyseat.app.ui.components.ShimmerBlock
import com.snippyseat.app.ui.theme.JetBrainsMono
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.SlotUnavailable
import com.snippyseat.app.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SlotPickerScreen(
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    onLoginRequired: (Boolean) -> Unit,
    viewModel: SlotPickerViewModel = hiltViewModel(),
    guestViewModel: GuestAccessViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedSlot = state.selectedSlotId != null
    var showGuestPrompt by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (guestViewModel.isGuestSession()) {
            showGuestPrompt = true
        }
    }

    Scaffold(
        bottomBar = {
            Surface(shadowElevation = 10.dp, color = Color.White) {
                Button(
                    onClick = { if (viewModel.confirmSlot()) onConfirm() },
                    enabled = selectedSlot,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(18.dp)
                        .height(52.dp),
                ) {
                    Text("Confirm Slot")
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(bottom = innerPadding.calculateBottomPadding()),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
                    Column {
                        Text("Pick a slot", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(state.draft.salon?.name.orEmpty(), color = TextSecondary)
                    }
                }
            }
            item {
                CalendarStrip(selected = state.selectedDate, onSelect = viewModel::selectDate)
            }
            if (state.draft.salon?.staff.orEmpty().isNotEmpty()) {
                item {
                    StylistSelector(
                        staff = state.draft.salon?.staff.orEmpty(),
                        selectedStaffId = state.selectedStaffId,
                        onSelect = viewModel::selectStaff,
                    )
                }
            }
            item {
                if (state.loading) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(4) { ShimmerBlock(modifier = Modifier.fillMaxWidth().height(48.dp)) }
                    }
                } else {
                    SlotGrid(
                        slots = state.slots,
                        selectedSlotId = state.selectedSlotId,
                        onSelect = viewModel::selectSlot,
                    )
                }
            }
        }
    }

    if (showGuestPrompt) {
        GuestLoginPromptSheet(
            onContinuePhone = {
                showGuestPrompt = false
                onLoginRequired(false)
            },
            onContinueGoogle = {
                showGuestPrompt = false
                onLoginRequired(true)
            },
            onMaybeLater = {
                showGuestPrompt = false
                onBack()
            },
        )
    }
}

@Composable
private fun CalendarStrip(selected: LocalDate, onSelect: (LocalDate) -> Unit) {
    val today = LocalDate.now()
    val dayFormatter = DateTimeFormatter.ofPattern("EEE")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items((0 until 14).map { today.plusDays(it.toLong()) }) { date ->
            val active = selected == date
            val scale by animateFloatAsState(if (active) 1.07f else 1f, label = "date-scale")
            Column(
                modifier = Modifier
                    .width(64.dp)
                    .scale(scale)
                    .clipDate(active)
                    .clickable { onSelect(date) }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(date.format(dayFormatter), color = if (active) Color.White else TextSecondary)
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(if (active) Color.White else LightRed, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${date.dayOfMonth}", color = if (active) Primary else Primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun Modifier.clipDate(active: Boolean): Modifier = this.background(
    color = if (active) Primary else Color.White,
    shape = RoundedCornerShape(8.dp),
)

@Composable
private fun StylistSelector(
    staff: List<SalonStaff>,
    selectedStaffId: String?,
    onSelect: (SalonStaff?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Stylist", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                StylistChip(label = "Any", selected = selectedStaffId == null, onClick = { onSelect(null) })
            }
            items(staff.filterNot { it.id == "staff-any" }) { member ->
                StylistChip(
                    label = member.name,
                    selected = selectedStaffId == member.id,
                    onClick = { onSelect(member) },
                )
            }
        }
    }
}

@Composable
private fun StylistChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(146.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) LightRed else Color.White,
        tonalElevation = if (selected) 0.dp else 2.dp,
        shadowElevation = if (selected) 0.dp else 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Primary else Color(0xFFE6E6E6)),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Person, contentDescription = null, tint = Primary)
            Spacer(Modifier.width(8.dp))
            Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SlotGrid(
    slots: List<TimeSlot>,
    selectedSlotId: String?,
    onSelect: (TimeSlot) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Available time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(360.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(slots) { slot ->
                val selected = selectedSlotId == slot.id
                val scale by animateFloatAsState(if (selected) 1.08f else 1f, label = "slot-scale")
                Card(
                    modifier = Modifier.scale(scale).height(50.dp),
                    onClick = { onSelect(slot) },
                    enabled = slot.available,
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            selected -> Primary
                            slot.available -> Color.White
                            else -> SlotUnavailable
                        },
                        contentColor = when {
                            selected -> Color.White
                            slot.available -> Primary
                            else -> TextSecondary
                        },
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (slot.available) Primary else SlotUnavailable),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(slot.label, style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMono), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
