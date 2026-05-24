package com.snippyseat.app.feature.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snippyseat.app.data.salon.GenderFilter
import com.snippyseat.app.data.salon.Salon
import com.snippyseat.app.data.salon.ServiceCategoryFilter
import com.snippyseat.app.feature.auth.GuestAccessViewModel
import com.snippyseat.app.feature.auth.GuestLoginPromptSheet
import com.snippyseat.app.ui.components.SalonCard
import com.snippyseat.app.ui.components.ShimmerBlock
import com.snippyseat.app.ui.theme.DarkRed
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.Surface
import com.snippyseat.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    onNavigateSearch: () -> Unit,
    onOpenSalon: (Salon) -> Unit,
    onLoginRequired: (Boolean) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    guestViewModel: GuestAccessViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedGender by viewModel.selectedGender.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val isGuest by guestViewModel.isGuest.collectAsStateWithLifecycle()
    var showGuestPrompt by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(uiState.refreshing, viewModel::refresh)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues)
            .pullRefresh(pullRefreshState),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item { HomeTopBar(isGuest = isGuest, onGuestClick = { showGuestPrompt = true }) }
            item { SearchEntry(onClick = onNavigateSearch) }
            item {
                GenderToggle(
                    selected = selectedGender,
                    onSelected = viewModel::selectGender,
                )
            }
            item {
                CategoryPills(
                    selected = selectedCategory,
                    onSelected = { category ->
                        viewModel.selectCategory(if (selectedCategory == category) null else category)
                    },
                )
            }
            if (uiState.loading) {
                items(4) {
                    ShimmerBlock(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (it == 0) 160.dp else 128.dp),
                    )
                }
            } else {
                item { SectionTitle("Featured salons") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(uiState.feed.featured, key = { it.id }) { salon ->
                            SalonCard(salon = salon, compact = true, onClick = { onOpenSalon(salon) })
                        }
                    }
                }
                item { SectionTitle("Nearby salons") }
                items(uiState.feed.nearby, key = { it.id }) { salon ->
                    SalonCard(salon = salon, modifier = Modifier.fillMaxWidth(), onClick = { onOpenSalon(salon) })
                }
                item { PremiumBanner() }
            }
        }
        PullRefreshIndicator(
            refreshing = uiState.refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Surface,
            contentColor = Primary,
        )
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
            onMaybeLater = { showGuestPrompt = false },
        )
    }
}

@Composable
private fun HomeTopBar(
    isGuest: Boolean,
    onGuestClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { },
        ) {
            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Primary)
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Current area", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    if (isGuest) {
                        Surface(
                            modifier = Modifier.clickable(onClick = onGuestClick),
                            color = Color.White,
                            contentColor = Primary,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.4f)),
                        ) {
                            Text(
                                text = "Guest mode",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                            )
                        }
                    }
                }
                Text(text = "Mumbai, India", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            BadgedBox(badge = { Badge { Text("2") } }) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onBackground)
            }
            Icon(
                Icons.Outlined.AccountCircle,
                contentDescription = "Profile",
                tint = Primary,
                modifier = Modifier.size(34.dp),
            )
        }
    }
}

@Composable
private fun SearchEntry(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(onClick = onClick),
        color = Surface,
        shadowElevation = 3.dp,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Search salons, services...", color = TextSecondary)
        }
    }
}

@Composable
private fun GenderToggle(
    selected: GenderFilter,
    onSelected: (GenderFilter) -> Unit,
) {
    val options = GenderFilter.entries
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)
    val offset by animateFloatAsState(targetValue = selectedIndex.toFloat(), label = "gender-indicator")

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(LightRed, RoundedCornerShape(8.dp)),
    ) {
        val optionWidth = maxWidth / options.size
        Box(
            modifier = Modifier
                .width(optionWidth)
                .height(36.dp)
                .offset(x = optionWidth * offset)
                .padding(start = 4.dp, top = 4.dp)
                .background(Primary, RoundedCornerShape(8.dp)),
        )
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            options.forEach { option ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clickable { onSelected(option) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = option.label,
                        color = if (selected == option) Color.White else Primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryPills(
    selected: ServiceCategoryFilter?,
    onSelected: (ServiceCategoryFilter) -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ServiceCategoryFilter.entries.forEach { category ->
            val active = selected == category
            val scale by animateFloatAsState(targetValue = if (active) 1.05f else 1f, label = "category-scale")
            FilterChip(
                selected = active,
                onClick = { onSelected(category) },
                label = { Text(category.label) },
                modifier = Modifier.scale(scale),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary,
                    selectedLabelColor = Color.White,
                ),
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun PremiumBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(listOf(Primary, DarkRed)),
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Go Premium", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text(text = "Faster booking and exclusive discounts", color = Color.White.copy(alpha = 0.82f))
            }
            AssistChip(
                onClick = { },
                label = { Text("View") },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color.White, labelColor = Primary),
            )
        }
    }
}
