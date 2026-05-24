package com.snippyseat.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.compose.rememberNavController
import com.snippyseat.app.core.model.UserRole
import com.snippyseat.app.feature.auth.AuthScreen
import com.snippyseat.app.feature.auth.GuestAccessViewModel
import com.snippyseat.app.feature.booking.BookingConfirmationScreen
import com.snippyseat.app.feature.booking.BookingSummaryScreen
import com.snippyseat.app.feature.booking.SalonDetailScreen
import com.snippyseat.app.feature.booking.SlotPickerScreen
import com.snippyseat.app.feature.bookings.BookingDetailScreen
import com.snippyseat.app.feature.bookings.BookingsScreen
import com.snippyseat.app.feature.home.HomeScreen
import com.snippyseat.app.feature.notifications.NotificationsScreen
import com.snippyseat.app.feature.onboarding.OnboardingScreen
import com.snippyseat.app.feature.premium.PremiumScreen
import com.snippyseat.app.feature.profile.ProfileScreen
import com.snippyseat.app.feature.role.RoleSelectionScreen
import com.snippyseat.app.feature.search.SearchScreen
import com.snippyseat.app.feature.seller.appointments.SellerAppointmentsScreen
import com.snippyseat.app.feature.seller.dashboard.SellerDashboardScreen
import com.snippyseat.app.feature.seller.earnings.SellerEarningsScreen
import com.snippyseat.app.feature.seller.onboarding.SellerOnboardingScreen
import com.snippyseat.app.feature.seller.services.SellerServicesScreen
import com.snippyseat.app.feature.seller.settings.SellerBlockedScreen
import com.snippyseat.app.feature.seller.settings.SellerSettingsScreen
import com.snippyseat.app.feature.splash.SplashScreen
import com.snippyseat.app.ui.placeholder.PlaceholderScreen
import com.snippyseat.app.ui.shell.SellerShell
import com.snippyseat.app.ui.shell.UserShell

@Composable
fun SnippySeatNavGraph(
    startDestination: String,
    navController: NavHostController = rememberNavController(),
    guestViewModel: GuestAccessViewModel = hiltViewModel(),
) {
    val isGuest by guestViewModel.isGuest.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { enterTransition() },
        exitTransition = { exitTransition() },
        popEnterTransition = { popEnterTransition() },
        popExitTransition = { popExitTransition() },
    ) {
        appScreen(Screen.Splash) {
            SplashScreen(
                onFinished = { destination ->
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }
        appScreen(Screen.Onboarding) {
            OnboardingScreen(
                onDone = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }
        appScreen(Screen.Auth) {
            val autoStartGoogle = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Boolean>("startGoogle") == true
            AuthScreen(
                autoStartGoogle = autoStartGoogle,
                onNavigate = { destination ->
                    val postAuthRoute = navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.get<String>("postAuthRoute")
                    val target = if (destination == Screen.RoleSelection.route) destination else postAuthRoute ?: destination
                    navController.navigate(target) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
            )
        }
        appScreen(Screen.RoleSelection) {
            RoleSelectionScreen(
                onDone = { role ->
                    val destination = if (role == UserRole.SELLER) {
                        Screen.SellerDashboard.route
                    } else {
                        Screen.UserHome.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                },
            )
        }

        appScreen(Screen.UserHome) {
            UserShell(navController, Screen.UserHome) { paddingValues ->
                HomeScreen(
                    paddingValues = paddingValues,
                    onNavigateSearch = { navController.navigate(Screen.Search.route) },
                    onOpenSalon = { salon ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("salonId", salon.id)
                        navController.navigate(Screen.SalonDetail.route)
                    },
                    onLoginRequired = { startGoogle ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("postAuthRoute", Screen.UserHome.route)
                        navController.currentBackStackEntry?.savedStateHandle?.set("startGoogle", startGoogle)
                        navController.navigate(Screen.Auth.route)
                    },
                )
            }
        }
        appScreen(Screen.Search) {
            UserShell(navController, Screen.Search) { paddingValues ->
                SearchScreen(
                    paddingValues = paddingValues,
                    onOpenSalon = { salon ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("salonId", salon.id)
                        navController.navigate(Screen.SalonDetail.route)
                    },
                )
            }
        }
        appScreen(Screen.Bookings) {
            if (isGuest) {
                RedirectGuestToAuth(navController, Screen.Bookings.route)
                return@appScreen
            }
            UserShell(navController, Screen.Bookings) { paddingValues ->
                BookingsScreen(paddingValues = paddingValues)
            }
        }
        appScreen(Screen.Profile) {
            if (isGuest) {
                RedirectGuestToAuth(navController, Screen.Profile.route)
                return@appScreen
            }
            UserShell(navController, Screen.Profile) { paddingValues ->
                ProfileScreen(
                    paddingValues = paddingValues,
                    onNavigateNotifications = { navController.navigate(Screen.Notifications.route) },
                    onNavigatePremium = { navController.navigate(Screen.Premium.route) },
                )
            }
        }
        appScreen(Screen.Notifications) {
            if (isGuest) {
                RedirectGuestToAuth(navController, Screen.Notifications.route)
                return@appScreen
            }
            NotificationsScreen(onBack = { navController.popBackStack() })
        }
        appScreen(Screen.Premium) {
            PremiumScreen(onBack = { navController.popBackStack() })
        }
        appScreen(Screen.SalonDetail) {
            val salonId = navController.previousBackStackEntry?.savedStateHandle?.get<String>("salonId")
            SalonDetailScreen(
                salonId = salonId,
                onBack = { navController.popBackStack() },
                onBookNow = { navController.navigate(Screen.SlotPicker.route) },
                onLoginRequired = { startGoogle ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("postAuthRoute", Screen.SalonDetail.route)
                    navController.currentBackStackEntry?.savedStateHandle?.set("startGoogle", startGoogle)
                    navController.navigate(Screen.Auth.route)
                },
            )
        }
        appScreen(Screen.SlotPicker) {
            SlotPickerScreen(
                onBack = { navController.popBackStack() },
                onConfirm = { navController.navigate(Screen.BookingSummary.route) },
                onLoginRequired = { startGoogle ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("postAuthRoute", Screen.SlotPicker.route)
                    navController.currentBackStackEntry?.savedStateHandle?.set("startGoogle", startGoogle)
                    navController.navigate(Screen.Auth.route)
                },
            )
        }
        appScreen(Screen.BookingSummary) {
            BookingSummaryScreen(
                onBack = { navController.popBackStack() },
                onConfirmed = {
                    navController.navigate(Screen.BookingConfirmation.route) {
                        popUpTo(Screen.SalonDetail.route)
                    }
                },
            )
        }
        appScreen(Screen.BookingConfirmation) {
            BookingConfirmationScreen(
                onBackHome = {
                    navController.navigate(Screen.UserHome.route) {
                        popUpTo(Screen.UserHome.route) { inclusive = true }
                    }
                },
            )
        }

        appScreen(
            screen = Screen.SellerDashboard,
            deepLinks = listOf(navDeepLink { uriPattern = "snippyseat://seller/dashboard" }),
        ) {
            SellerShell(navController, Screen.SellerDashboard) { paddingValues ->
                SellerDashboardScreen(
                    paddingValues = paddingValues,
                    onViewAppointments = { navController.navigate(Screen.SellerAppointments.route) },
                    onManageServices = { navController.navigate(Screen.SellerServices.route) },
                    onManageStaff = { navController.navigate(Screen.SellerServices.route) },
                    onViewEarnings = { navController.navigate(Screen.SellerEarnings.route) },
                    onOpenSettings = { navController.navigate(Screen.SellerSettings.route) },
                    onOpenOnboarding = { navController.navigate(Screen.SellerOnboarding.route) },
                )
            }
        }
        appScreen(Screen.SellerAppointments) {
            SellerShell(navController, Screen.SellerAppointments) { paddingValues ->
                SellerAppointmentsScreen(paddingValues = paddingValues)
            }
        }
        appScreen(Screen.SellerServices) {
            SellerShell(navController, Screen.SellerServices) { paddingValues ->
                SellerServicesScreen(paddingValues = paddingValues)
            }
        }
        appScreen(Screen.SellerEarnings) {
            SellerShell(navController, Screen.SellerEarnings) { paddingValues ->
                SellerEarningsScreen(paddingValues = paddingValues)
            }
        }
        appScreen(Screen.SellerSettings) {
            SellerShell(navController, Screen.SellerSettings) { paddingValues ->
                SellerSettingsScreen(
                    paddingValues = paddingValues,
                    onOpenBlocked = { navController.navigate(Screen.SellerBlocked.route) },
                )
            }
        }
        appScreen(Screen.SellerOnboarding) {
            SellerOnboardingScreen(
                onBack = { navController.popBackStack() },
                onDone = {
                    navController.navigate(Screen.SellerDashboard.route) {
                        popUpTo(Screen.SellerOnboarding.route) { inclusive = true }
                    }
                },
            )
        }
        appScreen(Screen.SellerBlocked) {
            SellerBlockedScreen(
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.SellerDashboard.route) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = "user/booking-detail/{bookingId}",
            arguments = listOf(navArgument("bookingId") { defaultValue = "" }),
            deepLinks = listOf(navDeepLink { uriPattern = "snippyseat://booking/{bookingId}" }),
        ) { entry ->
            BookingDetailScreen(
                bookingId = entry.arguments?.getString("bookingId").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = "user/salon-detail-deeplink/{salonId}",
            arguments = listOf(navArgument("salonId") { defaultValue = "" }),
            deepLinks = listOf(navDeepLink { uriPattern = "snippyseat://salon/{salonId}" }),
        ) { entry ->
            SalonDetailScreen(
                salonId = entry.arguments?.getString("salonId"),
                onBack = { navController.popBackStack() },
                onBookNow = { navController.navigate(Screen.SlotPicker.route) },
                onLoginRequired = { startGoogle ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("postAuthRoute", "user/salon-detail-deeplink/${entry.arguments?.getString("salonId").orEmpty()}")
                    navController.currentBackStackEntry?.savedStateHandle?.set("startGoogle", startGoogle)
                    navController.navigate(Screen.Auth.route)
                },
            )
        }
    }
}

@Composable
private fun RedirectGuestToAuth(navController: NavHostController, postAuthRoute: String) {
    LaunchedEffect(postAuthRoute) {
        navController.currentBackStackEntry?.savedStateHandle?.set("postAuthRoute", postAuthRoute)
        navController.currentBackStackEntry?.savedStateHandle?.set("startGoogle", false)
        navController.navigate(Screen.Auth.route) {
            launchSingleTop = true
        }
    }
}

private fun NavGraphBuilder.appScreen(
    screen: Screen,
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable () -> Unit,
) {
    composable(route = screen.route, deepLinks = deepLinks) { content() }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    if (isTabSwitch()) {
        return fadeIn(animationSpec = tween(200))
    }

    if (targetState.destination.route in Screen.modalRoutes) {
        return slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300, easing = FastOutSlowInEasing),
        ) + fadeIn(animationSpec = tween(300))
    }

    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
    if (isTabSwitch()) {
        return fadeOut(animationSpec = tween(200))
    }

    if (initialState.destination.route in Screen.modalRoutes) {
        return slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(250, easing = FastOutSlowInEasing),
        ) + fadeOut(animationSpec = tween(250))
    }

    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(): EnterTransition {
    if (isTabSwitch()) {
        return fadeIn(animationSpec = tween(200))
    }

    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(): ExitTransition {
    if (isTabSwitch()) {
        return fadeOut(animationSpec = tween(200))
    }

    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(): Boolean {
    return initialState.destination.route in Screen.tabRoutes &&
        targetState.destination.route in Screen.tabRoutes
}
