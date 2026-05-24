package com.snippyseat.app.navigation

sealed class Screen(val route: String, val label: String) {
    data object Splash : Screen("splash", "Splash")
    data object Onboarding : Screen("onboarding", "Onboarding")
    data object Auth : Screen("auth", "Auth")
    data object RoleSelection : Screen("role-selection", "Role")

    data object UserHome : Screen("user/home", "Home")
    data object Search : Screen("user/search", "Search")
    data object Bookings : Screen("user/bookings", "Bookings")
    data object Profile : Screen("user/profile", "Profile")
    data object Notifications : Screen("user/notifications", "Notifications")
    data object Premium : Screen("user/premium", "Premium")
    data object SalonDetail : Screen("user/salon-detail", "Salon")
    data object SlotPicker : Screen("user/slot-picker", "Slots")
    data object BookingSummary : Screen("user/booking-summary", "Summary")
    data object BookingConfirmation : Screen("user/booking-confirmation", "Confirmed")

    data object SellerDashboard : Screen("seller/dashboard", "Dashboard")
    data object SellerAppointments : Screen("seller/appointments", "Appointments")
    data object SellerServices : Screen("seller/services", "Services")
    data object SellerEarnings : Screen("seller/earnings", "Earnings")
    data object SellerSettings : Screen("seller/settings", "Settings")
    data object SellerOnboarding : Screen("seller/onboarding", "Onboarding")
    data object SellerBlocked : Screen("seller/blocked", "Blocked")

    companion object {
        val userTabs = listOf(UserHome, Search, Bookings, Profile)
        val sellerTabs = listOf(
            SellerDashboard,
            SellerAppointments,
            SellerServices,
            SellerEarnings,
            SellerSettings,
        )
        val modalRoutes = setOf(RoleSelection.route, BookingSummary.route, SellerOnboarding.route, SellerBlocked.route)
        val tabRoutes = (userTabs + sellerTabs).map { it.route }.toSet()
    }
}
