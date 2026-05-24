package com.snippyseat.app.feature.seller.services

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snippyseat.app.data.seller.SellerServiceGender
import com.snippyseat.app.data.seller.SellerServiceItem
import com.snippyseat.app.data.seller.sellerServiceCategories
import com.snippyseat.app.feature.seller.hours.WorkingHoursScreen
import com.snippyseat.app.feature.seller.staff.SellerStaffScreen
import com.snippyseat.app.ui.theme.Error
import com.snippyseat.app.ui.theme.JetBrainsMono
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.Success
import com.snippyseat.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerServicesScreen(
    paddingValues: PaddingValues,
    viewModel: SellerServicesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        floatingActionButton = {
            if (state.selectedTab == SellerManagementTab.SERVICES) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::openAddSheet,
                    containerColor = Primary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("Service") },
                )
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(innerPadding)) {
            Text(
                "Seller tools",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            TabRow(selectedTabIndex = state.selectedTab.ordinal, containerColor = Color.White, contentColor = Primary) {
                SellerManagementTab.entries.forEach { tab ->
                    Tab(selected = state.selectedTab == tab, onClick = { viewModel.selectTab(tab) }, text = { Text(tab.label) })
                }
            }
            when (state.selectedTab) {
                SellerManagementTab.SERVICES -> ServiceList(
                    services = state.services,
                    onEdit = viewModel::openEditSheet,
                    onDelete = viewModel::deleteService,
                    onToggle = viewModel::toggleServiceActive,
                )
                SellerManagementTab.STAFF -> SellerStaffScreen(contentPadding = PaddingValues(bottom = 96.dp))
                SellerManagementTab.HOURS -> WorkingHoursScreen(contentPadding = PaddingValues(bottom = 96.dp))
            }
        }
    }

    if (state.sheetOpen) {
        AddServiceBottomSheet(
            draft = state.draft,
            onDismiss = viewModel::closeSheet,
            onName = viewModel::updateName,
            onCategory = viewModel::updateCategory,
            onGender = viewModel::updateGender,
            onDuration = viewModel::updateDuration,
            onPrice = viewModel::updatePrice,
            onHome = viewModel::updateHomeService,
            onActive = viewModel::updateActive,
            onSave = viewModel::saveService,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceList(
    services: List<SellerServiceItem>,
    onEdit: (SellerServiceItem) -> Unit,
    onDelete: (SellerServiceItem) -> Unit,
    onToggle: (SellerServiceItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(services, key = { it.id }) { service ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    when (value) {
                        SwipeToDismissBoxValue.StartToEnd -> onEdit(service)
                        SwipeToDismissBoxValue.EndToStart -> onDelete(service)
                        SwipeToDismissBoxValue.Settled -> Unit
                    }
                    false
                },
            )
            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    Row(
                        modifier = Modifier.fillMaxSize().background(LightRed, RoundedCornerShape(8.dp)).padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, tint = Primary)
                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = Error)
                    }
                },
            ) {
                ServiceCard(service = service, onToggle = { onToggle(service) })
            }
        }
    }
}

@Composable
private fun ServiceCard(service: SellerServiceItem, onToggle: () -> Unit) {
    val alpha by animateFloatAsState(if (service.active) 1f else 0.55f, label = "service-card-alpha")
    Surface(shape = RoundedCornerShape(8.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.alpha(alpha)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(service.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${service.gender.label} / ${service.durationMinutes} min", color = TextSecondary)
                }
                Switch(checked = service.active, onCheckedChange = { onToggle() })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = LightRed, contentColor = Primary, shape = RoundedCornerShape(6.dp)) {
                    Text(service.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                }
                Text("Rs ${service.price}", style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono), color = Primary, fontWeight = FontWeight.Bold)
                if (service.homeService) {
                    Text("Home Rs ${(service.price * 1.6).toInt()}", color = Success, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddServiceBottomSheet(
    draft: ServiceDraft,
    onDismiss: () -> Unit,
    onName: (String) -> Unit,
    onCategory: (String) -> Unit,
    onGender: (SellerServiceGender) -> Unit,
    onDuration: (Int) -> Unit,
    onPrice: (String) -> Unit,
    onHome: (Boolean) -> Unit,
    onActive: (Boolean) -> Unit,
    onSave: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { Text(if (draft.id == null) "Add service" else "Edit service", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            item { OutlinedTextField(value = draft.name, onValueChange = onName, modifier = Modifier.fillMaxWidth(), label = { Text("Service name") }) }
            item {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = draft.category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        sellerServiceCategories.forEach { category ->
                            DropdownMenuItem(text = { Text(category) }, onClick = { onCategory(category); expanded = false })
                        }
                    }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SellerServiceGender.entries) { gender ->
                        FilterChip(
                            selected = draft.gender == gender,
                            onClick = { onGender(gender) },
                            label = { Text(gender.label) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary, selectedLabelColor = Color.White),
                        )
                    }
                }
            }
            item {
                Text("Duration: ${draft.durationMinutes} min", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items((15..180 step 15).toList()) { minutes ->
                        FilterChip(selected = draft.durationMinutes == minutes, onClick = { onDuration(minutes) }, label = { Text("$minutes") })
                    }
                }
            }
            item { OutlinedTextField(value = draft.price, onValueChange = onPrice, modifier = Modifier.fillMaxWidth(), label = { Text("Price") }, prefix = { Text("Rs ") }, textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMono)) }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Home service", fontWeight = FontWeight.Bold)
                        Text("Auto price: Rs ${((draft.price.toIntOrNull() ?: 0) * 1.6).toInt()}", color = TextSecondary)
                    }
                    Switch(checked = draft.homeService, onCheckedChange = onHome)
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Active", fontWeight = FontWeight.Bold)
                    Switch(checked = draft.active, onCheckedChange = onActive)
                }
            }
            item {
                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Text("Save Service")
                }
            }
            item { Spacer(Modifier.height(18.dp)) }
        }
    }
}
