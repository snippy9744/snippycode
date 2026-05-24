package com.snippyseat.app.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snippyseat.app.data.salon.GenderFilter
import com.snippyseat.app.data.salon.Salon
import com.snippyseat.app.data.salon.ServiceCategoryFilter
import com.snippyseat.app.data.salon.SortOption
import com.snippyseat.app.ui.components.SalonCard
import com.snippyseat.app.ui.components.ShimmerBlock
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    paddingValues: PaddingValues,
    onOpenSalon: (Salon) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val focusRequester = FocusRequester()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setFilterSheet(true) },
                containerColor = Primary,
                contentColor = Color.White,
            ) {
                Icon(Icons.Outlined.FilterList, contentDescription = "Filters")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::updateQuery,
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    placeholder = { Text("Search salons, services...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { viewModel.submitSearch() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
            }
            if (recentSearches.isNotEmpty() && uiState.query.isBlank()) {
                item { Text(text = "Recent searches", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recentSearches) { query ->
                            FilterChip(
                                selected = false,
                                onClick = { viewModel.submitSearch(query) },
                                label = { Text(query) },
                            )
                        }
                    }
                }
            }
            if (uiState.loading) {
                items(4) {
                    ShimmerBlock(modifier = Modifier.fillMaxWidth().height(126.dp))
                }
            } else if (uiState.results.isEmpty()) {
                item { EmptySearchState() }
            } else {
                items(uiState.results, key = { it.id }) { salon ->
                    SalonCard(salon = salon, modifier = Modifier.fillMaxWidth(), onClick = { onOpenSalon(salon) })
                }
            }
        }
    }

    if (uiState.filterSheetOpen) {
        FilterBottomSheet(
            uiState = uiState,
            onDismiss = { viewModel.setFilterSheet(false) },
            onGender = viewModel::updateGender,
            onDistance = viewModel::updateDistance,
            onRating = viewModel::updateRating,
            onService = viewModel::toggleService,
            onHomeService = viewModel::updateHomeServiceOnly,
            onSort = viewModel::updateSort,
            onApply = viewModel::applyFilters,
        )
    }
}

@Composable
private fun EmptySearchState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 52.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "No salons found", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try a different service, distance, or rating filter.",
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    uiState: SearchUiState,
    onDismiss: () -> Unit,
    onGender: (GenderFilter) -> Unit,
    onDistance: (Float) -> Unit,
    onRating: (Double?) -> Unit,
    onService: (ServiceCategoryFilter) -> Unit,
    onHomeService: (Boolean) -> Unit,
    onSort: (SortOption) -> Unit,
    onApply: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Filters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            ChipRow {
                GenderFilter.entries.forEach { gender ->
                    FilterChip(
                        selected = uiState.filters.gender == gender,
                        onClick = { onGender(gender) },
                        label = { Text(gender.label) },
                        colors = redFilterChipColors(),
                    )
                }
            }
            Column {
                Text(text = "Distance: ${uiState.filters.distanceKm.toInt()} km", style = MaterialTheme.typography.titleMedium)
                RangeSlider(
                    value = 1f..uiState.filters.distanceKm,
                    onValueChange = { onDistance(it.endInclusive.coerceIn(1f, 20f)) },
                    valueRange = 1f..20f,
                )
            }
            ChipRow {
                listOf(null, 3.0, 4.0, 4.5).forEach { rating ->
                    FilterChip(
                        selected = uiState.filters.rating == rating,
                        onClick = { onRating(rating) },
                        label = { Text(if (rating == null) "Any rating" else "${rating}+ ") },
                        colors = redFilterChipColors(),
                    )
                }
            }
            ChipRow {
                ServiceCategoryFilter.entries.forEach { service ->
                    FilterChip(
                        selected = service in uiState.filters.services,
                        onClick = { onService(service) },
                        label = { Text(service.label) },
                        colors = redFilterChipColors(),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Home service only", style = MaterialTheme.typography.titleMedium)
                Switch(checked = uiState.filters.homeServiceOnly, onCheckedChange = onHomeService)
            }
            Column {
                SortOption.entries.forEach { sort ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSort(sort) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = uiState.filters.sort == sort, onClick = { onSort(sort) })
                        Text(text = sort.label)
                    }
                }
            }
            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text(text = "Apply")
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { content() } }
    }
}

@Composable
private fun redFilterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = Primary,
    selectedLabelColor = Color.White,
    containerColor = LightRed,
    labelColor = Primary,
)
