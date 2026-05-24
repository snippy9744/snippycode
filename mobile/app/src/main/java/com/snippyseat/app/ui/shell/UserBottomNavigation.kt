package com.snippyseat.app.ui.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.snippyseat.app.feature.auth.GuestAccessViewModel
import com.snippyseat.app.feature.auth.GuestLoginPromptSheet
import com.snippyseat.app.navigation.Screen
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.TextSecondary

@Composable
fun UserBottomNavigation(
    selected: Screen,
    navController: NavHostController,
    guestViewModel: GuestAccessViewModel = hiltViewModel(),
) {
    val isGuest by guestViewModel.isGuest.collectAsStateWithLifecycle()
    var showGuestPrompt by remember { mutableStateOf(false) }
    var blockedDestination by remember { mutableStateOf<String?>(null) }

    NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
        userItems.forEach { item ->
            NavigationBarItem(
                selected = item.screen.route == selected.route,
                onClick = {
                    if (isGuest && item.screen in guestBlockedTabs) {
                        blockedDestination = item.screen.route
                        showGuestPrompt = true
                        return@NavigationBarItem
                    }
                    navController.navigate(item.screen.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(Screen.UserHome.route) { saveState = true }
                    }
                },
                icon = { Icon(imageVector = item.icon, contentDescription = item.screen.label) },
                label = { Text(text = item.screen.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    indicatorColor = LightRed,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                ),
            )
        }
    }

    if (showGuestPrompt) {
        GuestLoginPromptSheet(
            onContinuePhone = {
                navController.currentBackStackEntry?.savedStateHandle?.set("postAuthRoute", blockedDestination ?: Screen.UserHome.route)
                navController.currentBackStackEntry?.savedStateHandle?.set("startGoogle", false)
                showGuestPrompt = false
                navController.navigate(Screen.Auth.route)
            },
            onContinueGoogle = {
                navController.currentBackStackEntry?.savedStateHandle?.set("postAuthRoute", blockedDestination ?: Screen.UserHome.route)
                navController.currentBackStackEntry?.savedStateHandle?.set("startGoogle", true)
                showGuestPrompt = false
                navController.navigate(Screen.Auth.route)
            },
            onMaybeLater = { showGuestPrompt = false },
        )
    }
}

private data class UserNavItem(val screen: Screen, val icon: ImageVector)

private val userItems = listOf(
    UserNavItem(Screen.UserHome, Icons.Outlined.Home),
    UserNavItem(Screen.Search, Icons.Outlined.Search),
    UserNavItem(Screen.Bookings, Icons.Outlined.DateRange),
    UserNavItem(Screen.Profile, Icons.Outlined.Person),
)

private val guestBlockedTabs = setOf(Screen.Bookings, Screen.Profile)
