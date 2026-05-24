package com.snippyseat.app.ui.shell

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.snippyseat.app.navigation.Screen
import com.snippyseat.app.ui.placeholder.PlaceholderScreen

@Composable
fun SellerShell(
    navController: NavHostController,
    selected: Screen,
    content: @Composable (PaddingValues) -> Unit = { paddingValues ->
        PlaceholderScreen(
            title = selected.label,
            modifier = Modifier.padding(paddingValues),
        )
    },
) {
    Scaffold(
        bottomBar = { SellerBottomNavigation(selected = selected, navController = navController) },
    ) { paddingValues: PaddingValues -> content(paddingValues) }
}
