package com.snippyseat.app.feature.seller.hours

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snippyseat.app.data.seller.SellerWorkingDay
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.Success
import com.snippyseat.app.ui.theme.TextSecondary

private val timeOptions = listOf("08:00", "09:00", "10:00", "11:00", "18:00", "19:00", "20:00", "21:30", "22:00")

@Composable
fun WorkingHoursScreen(
    contentPadding: PaddingValues,
    viewModel: WorkingHoursViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Working hours", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(if (state.saved) "Saved" else "Update your weekly schedule", color = if (state.saved) Success else TextSecondary)
                }
                Button(onClick = viewModel::save, colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(8.dp)) {
                    Text("Save")
                }
            }
        }
        items(state.days, key = { it.id }) { day ->
            WorkingDayRow(
                day = day,
                onToggle = { viewModel.toggleOpen(day) },
                onStart = { viewModel.updateStart(day, it) },
                onEnd = { viewModel.updateEnd(day, it) },
                onAddBreak = { viewModel.addBreak(day) },
            )
        }
        item {
            Surface(color = LightRed, shape = RoundedCornerShape(8.dp)) {
                Text("Holiday dates picker placeholder", modifier = Modifier.fillMaxWidth().padding(14.dp), color = Primary, fontWeight = FontWeight.Bold)
            }
        }
        item { Spacer(Modifier.padding(contentPadding)) }
    }
}

@Composable
private fun WorkingDayRow(
    day: SellerWorkingDay,
    onToggle: () -> Unit,
    onStart: (String) -> Unit,
    onEnd: (String) -> Unit,
    onAddBreak: () -> Unit,
) {
    val alpha by animateFloatAsState(if (day.open) 1f else 0.45f, label = "working-day-alpha")
    Surface(shape = RoundedCornerShape(8.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.alpha(alpha)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(day.dayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Switch(checked = day.open, onCheckedChange = { onToggle() })
            }
            AnimatedVisibility(day.open) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimePickerRow("Open", day.startTime, onStart)
                    TimePickerRow("Close", day.endTime, onEnd)
                    if (day.breaks.isNotEmpty()) {
                        Text("Breaks: ${day.breaks.joinToString()}", color = TextSecondary)
                    }
                    OutlinedButton(onClick = onAddBreak, shape = RoundedCornerShape(8.dp)) {
                        Text("Add break time")
                    }
                }
            }
        }
    }
}

@Composable
private fun TimePickerRow(label: String, selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = TextSecondary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(timeOptions) { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(option) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary, selectedLabelColor = Color.White),
                )
            }
        }
    }
}
