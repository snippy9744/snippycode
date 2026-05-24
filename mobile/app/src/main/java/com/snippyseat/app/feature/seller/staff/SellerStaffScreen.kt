package com.snippyseat.app.feature.seller.staff

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.snippyseat.app.data.seller.SellerStaffMember
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.Success
import com.snippyseat.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerStaffScreen(
    contentPadding: PaddingValues,
    viewModel: SellerStaffViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.staff.isEmpty()) {
                item { EmptyStaffState() }
            } else {
                items(state.staff, key = { it.id }) { member ->
                    StaffCard(member, onClick = { viewModel.openEditSheet(member) }, onToggle = { viewModel.toggleAvailability(member) })
                }
            }
            item { Spacer(Modifier.padding(contentPadding)) }
        }
        ExtendedFloatingActionButton(
            onClick = viewModel::openAddSheet,
            containerColor = Primary,
            contentColor = Color.White,
            icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
            text = { Text("Staff") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp),
        )
    }

    if (state.sheetOpen) {
        AddStaffBottomSheet(
            state = state,
            onDismiss = viewModel::closeSheet,
            onName = viewModel::updateName,
            onPhone = viewModel::updatePhone,
            onSpeciality = viewModel::updateSpeciality,
            onPhoto = viewModel::updatePhoto,
            onService = viewModel::toggleService,
            onAvailable = viewModel::updateAvailable,
            onSave = viewModel::saveStaff,
        )
    }
}

@Composable
private fun StaffCard(member: SellerStaffMember, onClick: () -> Unit, onToggle: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(8.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(
                model = member.photoUrl,
                contentDescription = member.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(62.dp).background(LightRed, CircleShape),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(member.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(member.speciality, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(member.services.joinToString().ifBlank { "No services assigned" }, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (member.available) "Available" else "Unavailable", color = if (member.available) Success else TextSecondary, fontWeight = FontWeight.Bold)
            }
            Switch(checked = member.available, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
private fun EmptyStaffState() {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 72.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(82.dp).background(LightRed, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(42.dp))
        }
        Text("Add your first team member", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Staff added here will appear in the user slot picker.", color = TextSecondary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddStaffBottomSheet(
    state: SellerStaffUiState,
    onDismiss: () -> Unit,
    onName: (String) -> Unit,
    onPhone: (String) -> Unit,
    onSpeciality: (String) -> Unit,
    onPhoto: (android.net.Uri?) -> Unit,
    onService: (String) -> Unit,
    onAvailable: (Boolean) -> Unit,
    onSave: () -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> onPhoto(uri) }
    val draft = state.draft
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Text(if (draft.id == null) "Add staff" else "Edit staff", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            item {
                Surface(modifier = Modifier.fillMaxWidth().clickable { launcher.launch("image/*") }, color = LightRed, shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.AddAPhoto, contentDescription = null, tint = Primary)
                        Text(if (draft.photoUri == null && draft.photoUrl == null) "Add photo" else "Photo selected", fontWeight = FontWeight.Bold)
                    }
                }
            }
            item { OutlinedTextField(draft.name, onName, modifier = Modifier.fillMaxWidth(), label = { Text("Name") }) }
            item { OutlinedTextField(draft.phone, onPhone, modifier = Modifier.fillMaxWidth(), label = { Text("Phone") }) }
            item { OutlinedTextField(draft.speciality, onSpeciality, modifier = Modifier.fillMaxWidth(), label = { Text("Speciality") }) }
            item {
                Text("Assign services", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.serviceOptions) { option ->
                        FilterChip(
                            selected = option in draft.services,
                            onClick = { onService(option) },
                            label = { Text(option) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary, selectedLabelColor = Color.White),
                        )
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Available", fontWeight = FontWeight.Bold)
                    Switch(checked = draft.available, onCheckedChange = onAvailable)
                }
            }
            item {
                Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Save Staff")
                }
            }
            item { Spacer(Modifier.height(18.dp)) }
        }
    }
}
