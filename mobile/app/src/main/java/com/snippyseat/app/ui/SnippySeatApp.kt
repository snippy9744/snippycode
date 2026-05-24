package com.snippyseat.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.snippyseat.app.core.connectivity.OfflineBanner
import com.snippyseat.app.core.notifications.InAppNotificationBanner
import com.snippyseat.app.navigation.Screen
import com.snippyseat.app.navigation.SnippySeatNavGraph

@Composable
fun SnippySeatApp(
    viewModel: AppChromeViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val notification by viewModel.notification.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        SnippySeatNavGraph(startDestination = Screen.Splash.route, navController = navController)
        OfflineBanner(visible = !isOnline, modifier = Modifier.align(Alignment.TopCenter))
        InAppNotificationBanner(
            notification = notification,
            onClick = {
                notification?.deepLink?.let { navController.navigate(android.net.Uri.parse(it)) }
                viewModel.dismissNotification()
            },
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}
