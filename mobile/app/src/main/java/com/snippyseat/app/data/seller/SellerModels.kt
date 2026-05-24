package com.snippyseat.app.data.seller

enum class SellerAccountType(val label: String) {
    SHOP("I have a shop"),
    HOME_SERVICE("I do home service only"),
}

enum class SellerApprovalStatus(val label: String) {
    ACTIVE("Active"),
    PENDING_APPROVAL("Pending approval"),
    BLOCKED("Blocked"),
}

enum class SellerAppointmentStatus(val label: String) {
    TODAY("Today"),
    UPCOMING("Upcoming"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
}

enum class SellerPaymentStatus(val label: String) {
    PAID_ONLINE("Paid Online"),
    PAY_AT_SHOP("Pay at Shop"),
}

enum class SellerServiceGender(val label: String) {
    MEN("Men"),
    WOMEN("Women"),
    UNISEX("Unisex"),
}

data class SellerServiceItem(
    val id: String,
    val name: String,
    val category: String,
    val gender: SellerServiceGender,
    val durationMinutes: Int,
    val price: Int,
    val homeService: Boolean,
    val active: Boolean,
)

data class SellerStaffMember(
    val id: String,
    val name: String,
    val phone: String,
    val speciality: String,
    val photoUrl: String?,
    val services: Set<String>,
    val available: Boolean,
)

data class SellerWorkingDay(
    val id: String,
    val dayName: String,
    val open: Boolean,
    val startTime: String,
    val endTime: String,
    val breaks: List<String>,
)

data class SellerTransaction(
    val id: String,
    val bookingRef: String,
    val service: String,
    val gross: Int,
    val commission: Int,
    val net: Int,
)

data class SellerAppointment(
    val id: String,
    val customerName: String,
    val customerAvatarUrl: String,
    val services: String,
    val timeLabel: String,
    val stylistName: String,
    val amount: Int,
    val paymentStatus: SellerPaymentStatus,
    val status: SellerAppointmentStatus,
    val homeService: Boolean = false,
    val address: String? = null,
)

data class SellerDashboardStats(
    val todayAppointments: Int,
    val revenueToday: Int,
    val pending: Int,
    val completed: Int,
)

data class SellerDashboard(
    val shopName: String,
    val approvalStatus: SellerApprovalStatus,
    val subscriptionMessage: String,
    val stats: SellerDashboardStats,
    val upcomingAppointments: List<SellerAppointment>,
)

val sampleSellerAppointments = listOf(
    SellerAppointment(
        id = "SA-3301",
        customerName = "Priya Mehta",
        customerAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330",
        services = "Haircut + Styling",
        timeLabel = "Today, 11:30 AM",
        stylistName = "Meera",
        amount = 349,
        paymentStatus = SellerPaymentStatus.PAID_ONLINE,
        status = SellerAppointmentStatus.TODAY,
    ),
    SellerAppointment(
        id = "SA-3302",
        customerName = "Arjun Pai",
        customerAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e",
        services = "Beard Trim",
        timeLabel = "Today, 1:00 PM",
        stylistName = "Ayaan",
        amount = 149,
        paymentStatus = SellerPaymentStatus.PAY_AT_SHOP,
        status = SellerAppointmentStatus.TODAY,
        homeService = true,
        address = "Pali Hill, Bandra West",
    ),
    SellerAppointment(
        id = "SA-3303",
        customerName = "Neha Shah",
        customerAvatarUrl = "https://images.unsplash.com/photo-1580489944761-15a19d654956",
        services = "Instant Glow Facial",
        timeLabel = "Tomorrow, 4:30 PM",
        stylistName = "Ravi",
        amount = 699,
        paymentStatus = SellerPaymentStatus.PAID_ONLINE,
        status = SellerAppointmentStatus.UPCOMING,
    ),
    SellerAppointment(
        id = "SA-3291",
        customerName = "Rahul Nair",
        customerAvatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d",
        services = "Classic Haircut",
        timeLabel = "May 21, 6:00 PM",
        stylistName = "Ayaan",
        amount = 199,
        paymentStatus = SellerPaymentStatus.PAY_AT_SHOP,
        status = SellerAppointmentStatus.COMPLETED,
    ),
    SellerAppointment(
        id = "SA-3288",
        customerName = "Isha Rao",
        customerAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
        services = "Global Hair Color",
        timeLabel = "May 19, 12:00 PM",
        stylistName = "Meera",
        amount = 1499,
        paymentStatus = SellerPaymentStatus.PAID_ONLINE,
        status = SellerAppointmentStatus.CANCELLED,
    ),
)

val sampleSellerDashboard = SellerDashboard(
    shopName = "Red Chair Studio Bandra",
    approvalStatus = SellerApprovalStatus.ACTIVE,
    subscriptionMessage = "Subscription renews in 5 days. Keep auto-renew ready.",
    stats = SellerDashboardStats(
        todayAppointments = 8,
        revenueToday = 4890,
        pending = 3,
        completed = 5,
    ),
    upcomingAppointments = sampleSellerAppointments.take(3),
)

val sellerServiceCategories = listOf(
    "Haircut",
    "Shaving",
    "Beard Trim",
    "Coloring",
    "Smoothening",
    "Straightening",
    "Keratin",
    "Facial",
    "Cleanup",
    "Threading",
    "Waxing",
    "Bridal Package",
    "Kids Cut",
    "Head Massage",
    "Others",
)

val sampleSellerServices = listOf(
    SellerServiceItem("svc-1", "Classic Haircut", "Haircut", SellerServiceGender.MEN, 30, 199, true, true),
    SellerServiceItem("svc-2", "Haircut + Styling", "Haircut", SellerServiceGender.UNISEX, 45, 349, false, true),
    SellerServiceItem("svc-3", "Instant Glow Facial", "Facial", SellerServiceGender.UNISEX, 50, 699, true, true),
    SellerServiceItem("svc-4", "Global Hair Color", "Coloring", SellerServiceGender.UNISEX, 90, 1499, false, false),
)

val sampleSellerStaff = listOf(
    SellerStaffMember(
        id = "staff-1",
        name = "Ayaan Khan",
        phone = "+91 90000 11001",
        speciality = "Fades and beard design",
        photoUrl = "https://images.unsplash.com/photo-1622286346003-cb31a4f86128",
        services = setOf("Classic Haircut", "Haircut + Styling"),
        available = true,
    ),
    SellerStaffMember(
        id = "staff-2",
        name = "Meera Shah",
        phone = "+91 90000 11002",
        speciality = "Coloring and styling",
        photoUrl = "https://images.unsplash.com/photo-1580618672591-eb180b1a973f",
        services = setOf("Haircut + Styling", "Global Hair Color"),
        available = true,
    ),
)

val sampleWorkingDays = listOf(
    SellerWorkingDay("mon", "Mon", true, "10:00", "21:30", listOf("14:00-15:00")),
    SellerWorkingDay("tue", "Tue", true, "10:00", "21:30", emptyList()),
    SellerWorkingDay("wed", "Wed", true, "10:00", "21:30", emptyList()),
    SellerWorkingDay("thu", "Thu", true, "10:00", "21:30", emptyList()),
    SellerWorkingDay("fri", "Fri", true, "10:00", "22:00", listOf("15:00-15:30")),
    SellerWorkingDay("sat", "Sat", true, "09:00", "22:00", emptyList()),
    SellerWorkingDay("sun", "Sun", false, "10:00", "18:00", emptyList()),
)

val sampleSellerTransactions = listOf(
    SellerTransaction("txn-1", "SA-3301", "Haircut + Styling", 349, 35, 314),
    SellerTransaction("txn-2", "SA-3302", "Beard Trim", 149, 15, 134),
    SellerTransaction("txn-3", "SA-3303", "Instant Glow Facial", 699, 70, 629),
    SellerTransaction("txn-4", "SA-3291", "Classic Haircut", 199, 20, 179),
)
