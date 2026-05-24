package com.snippyseat.app.ui.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import com.snippyseat.app.navigation.Screen
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.TextSecondary

@Composable
fun SellerBottomNavigation(
    selected: Screen,
    navController: NavHostController,
) {
    NavigationBar(containerColor = Color.White) {
        sellerItems.forEach { item ->
            NavigationBarItem(
                selected = item.screen.route == selected.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(Screen.SellerDashboard.route) { saveState = true }
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
}

private data class SellerNavItem(val screen: Screen, val icon: ImageVector)

private val sellerItems = listOf(
    SellerNavItem(Screen.SellerDashboard, Icons.Outlined.Dashboard),
    SellerNavItem(Screen.SellerAppointments, Icons.Outlined.Event),
    SellerNavItem(Screen.SellerServices, Icons.Outlined.Build),
    SellerNavItem(Screen.SellerEarnings, Icons.Outlined.AccountBalanceWallet),
    SellerNavItem(Screen.SellerSettings, Icons.Outlined.Settings),
)
