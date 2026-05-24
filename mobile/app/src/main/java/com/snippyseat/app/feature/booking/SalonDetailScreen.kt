package com.snippyseat.app.feature.booking

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.snippyseat.app.data.booking.SalonDetail
import com.snippyseat.app.data.booking.SalonReview
import com.snippyseat.app.data.booking.SalonService
import com.snippyseat.app.data.booking.SalonStaff
import com.snippyseat.app.feature.auth.GuestAccessViewModel
import com.snippyseat.app.feature.auth.GuestLoginPromptSheet
import com.snippyseat.app.ui.components.ShimmerBlock
import com.snippyseat.app.ui.theme.Divider
import com.snippyseat.app.ui.theme.JetBrainsMono
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.Success
import com.snippyseat.app.ui.theme.TextSecondary
import com.snippyseat.app.ui.theme.Warning
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SalonDetailScreen(
    salonId: String?,
    onBack: () -> Unit,
    onBookNow: () -> Unit,
    onLoginRequired: (Boolean) -> Unit = { _ -> },
    viewModel: SalonDetailViewModel = hiltViewModel(),
    guestViewModel: GuestAccessViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showGuestPrompt by remember { mutableStateOf(false) }

    LaunchedEffect(salonId) {
        viewModel.load(salonId)
    }

    val salon = state.salon
    val selectedServices = salon?.services.orEmpty().filter { it.id in state.selectedServiceIds }
    val total = selectedServices.sumOf { it.price }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = selectedServices.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                Surface(shadowElevation = 12.dp, color = Color.White) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${selectedServices.size} services / Rs $total",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                                fontWeight = FontWeight.Bold,
                            )
                            Text(text = "Ready for slot selection", color = TextSecondary)
                        }
                        Button(
                            onClick = {
                                if (viewModel.saveDraftForBooking()) onBookNow()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text("Book Now")
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .background(Color.White),
        ) {
            when {
                state.loading -> LoadingSalonDetail(onBack)
                salon == null -> EmptySalonDetail(onBack)
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    item {
                        SalonHero(
                            salon = salon,
                            onBack = onBack,
                        )
                    }
                    stickyHeader {
                        SalonInfoHeader(salon = salon)
                    }
                    item {
                        ActionRow(
                            onSave = {
                                if (guestViewModel.isGuestSession()) {
                                    showGuestPrompt = true
                                }
                            },
                        )
                    }
                    stickyHeader {
                        TabRow(
                            selectedTabIndex = state.selectedTab.ordinal,
                            containerColor = Color.White,
                            contentColor = Primary,
                        ) {
                            SalonDetailTab.entries.forEach { tab ->
                                Tab(
                                    selected = state.selectedTab == tab,
                                    onClick = { viewModel.selectTab(tab) },
                                    text = { Text(tab.label, maxLines = 1) },
                                )
                            }
                        }
                    }
                    item {
                        AnimatedContent(targetState = state.selectedTab, label = "detail-tab") { tab ->
                            when (tab) {
                                SalonDetailTab.SERVICES -> ServicesTab(
                                    salon = salon,
                                    genderFilter = state.genderFilter,
                                    selectedServiceIds = state.selectedServiceIds,
                                    onGender = viewModel::selectGender,
                                    onService = viewModel::toggleService,
                                )

                                SalonDetailTab.STAFF -> StaffTab(
                                    staff = salon.staff,
                                    selectedStaffId = state.selectedStaffId,
                                    onStaff = viewModel::selectStaff,
                                )

                                SalonDetailTab.REVIEWS -> ReviewsTab(salon.reviews)
                                SalonDetailTab.ABOUT -> AboutTab(salon)
                            }
                        }
                    }
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
            onMaybeLater = { showGuestPrompt = false },
        )
    }
}

@Composable
private fun LoadingSalonDetail(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
        ShimmerBlock(modifier = Modifier.fillMaxWidth().height(260.dp))
        repeat(5) { ShimmerBlock(modifier = Modifier.fillMaxWidth().height(72.dp)) }
    }
}

@Composable
private fun EmptySalonDetail(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Salon not found", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
            Text("Go Back")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SalonHero(salon: SalonDetail, onBack: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { salon.photos.size })
    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.18f)) {
        HorizontalPager(state = pagerState, modifier = Modifier.matchParentSize()) { page ->
            AsyncImage(
                model = salon.photos[page],
                contentDescription = salon.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(12.dp)
                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                .align(Alignment.TopStart),
        ) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            salon.photos.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(width = if (pagerState.currentPage == index) 22.dp else 8.dp, height = 8.dp)
                        .background(if (pagerState.currentPage == index) Primary else Color.White, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun SalonInfoHeader(salon: SalonDetail) {
    Surface(color = Color.White, shadowElevation = 4.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(salon.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Warning, modifier = Modifier.size(18.dp))
                Text(String.format(Locale.US, "%.1f", salon.rating), fontWeight = FontWeight.Bold)
                Text("(${salon.reviewCount} reviews)", color = TextSecondary)
                Text("${String.format(Locale.US, "%.1f", salon.distanceKm)} km", color = TextSecondary)
                Surface(
                    color = if (salon.isOpen) Success.copy(alpha = 0.12f) else TextSecondary.copy(alpha = 0.12f),
                    contentColor = if (salon.isOpen) Success else TextSecondary,
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(if (salon.isOpen) "Open" else "Closed", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
            Text(salon.workingHours, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ActionRow(onSave: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AssistChip(onClick = onSave, label = { Text("Save") }, leadingIcon = { Icon(Icons.Outlined.WorkspacePremium, null) })
        AssistChip(onClick = { }, label = { Text("Share") }, leadingIcon = { Icon(Icons.Outlined.Share, null) })
        AssistChip(onClick = { }, label = { Text("Directions") }, leadingIcon = { Icon(Icons.Outlined.Directions, null) })
    }
}

@Composable
private fun ServicesTab(
    salon: SalonDetail,
    genderFilter: ServiceGenderFilter,
    selectedServiceIds: Set<String>,
    onGender: (ServiceGenderFilter) -> Unit,
    onService: (SalonService) -> Unit,
) {
    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ServiceGenderFilter.entries) { filter ->
                FilterChip(
                    selected = genderFilter == filter,
                    onClick = { onGender(filter) },
                    label = { Text(filter.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White,
                        containerColor = LightRed,
                        labelColor = Primary,
                    ),
                )
            }
        }
        val filtered = salon.services.filter {
            genderFilter.apiValue == null || it.gender == genderFilter.apiValue || it.gender == "UNISEX"
        }.groupBy { it.category }
        filtered.forEach { (category, services) ->
            Text(category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            services.forEach { service ->
                ServiceRow(service = service, selected = service.id in selectedServiceIds, onClick = { onService(service) })
            }
        }
    }
}

@Composable
private fun ServiceRow(service: SalonService, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1.04f else 1f, label = "service-add-scale")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) LightRed else Color.White)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(service.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Surface(color = Divider, shape = RoundedCornerShape(6.dp)) {
                Text("${service.durationMinutes} min", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = TextSecondary)
            }
        }
        Text("Rs ${service.price}", style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono), fontWeight = FontWeight.Bold)
        Surface(
            modifier = Modifier.scale(scale),
            color = if (selected) Success else Primary,
            contentColor = Color.White,
            shape = CircleShape,
        ) {
            Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                Icon(if (selected) Icons.Filled.Check else Icons.Outlined.Add, contentDescription = null)
            }
        }
    }
}

@Composable
private fun StaffTab(staff: List<SalonStaff>, selectedStaffId: String?, onStaff: (SalonStaff) -> Unit) {
    LazyRow(
        modifier = Modifier.padding(18.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(staff) { member ->
            val selected = member.id == selectedStaffId
            ElevatedCard(
                modifier = Modifier.width(180.dp).clickable { onStaff(member) },
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AsyncImage(
                        model = member.photoUrl,
                        contentDescription = member.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(82.dp).clip(CircleShape).background(LightRed),
                    )
                    Text(member.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(member.speciality, color = TextSecondary, maxLines = 2)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, tint = Warning, modifier = Modifier.size(16.dp))
                        Text(String.format(Locale.US, "%.1f", member.rating))
                    }
                    if (selected) Text("Selected", color = Primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ReviewsTab(reviews: List<SalonReview>) {
    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Verified booker reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        (5 downTo 1).forEach { star ->
            val count = reviews.count { it.rating == star }
            val progress = if (reviews.isEmpty()) 0f else count / reviews.size.toFloat()
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$star", modifier = Modifier.width(16.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.weight(1f), color = Primary, trackColor = LightRed)
                Text("$count", color = TextSecondary)
            }
        }
        reviews.forEach { review ->
            Surface(color = Color.White, shadowElevation = 1.dp, shape = RoundedCornerShape(8.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(review.customerName, fontWeight = FontWeight.Bold)
                        Text("${review.rating}.0", color = Warning, fontWeight = FontWeight.Bold)
                    }
                    Text(review.comment, color = TextSecondary)
                    Text("Verified booking", color = Success, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun AboutTab(salon: SalonDetail) {
    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("GST: ${salon.gstNumber}", fontWeight = FontWeight.Bold)
        Text("Hours: ${salon.workingHours}", color = TextSecondary)
        Text("Address: ${salon.address}", color = TextSecondary)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(270.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(salon.photos) { photo ->
                AsyncImage(
                    model = photo,
                    contentDescription = salon.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(8.dp)).background(LightRed),
                )
            }
        }
    }
}
