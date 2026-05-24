export const dashboard = {
  stats: {
    totalUsers: 12480,
    activeSellers: 386,
    todayBookings: 214,
    todayRevenue: 428600,
    platformRevenue: 51290,
  },
  revenue30Days: Array.from({ length: 30 }, (_, index) => ({
    day: `${index + 1}`,
    gross: 90000 + index * 2400 + (index % 5) * 7000,
    platform: 12000 + index * 480 + (index % 4) * 1300,
  })),
  bookings7Days: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"].map((day, index) => ({
    day,
    bookings: 90 + index * 17,
  })),
  recentBookings: [
    ["BK-10291", "Priya M.", "Red Chair Studio", 498, "CONFIRMED", "Today 6:30 PM"],
    ["BK-10290", "Arjun P.", "Glow & Snip", 699, "PAID", "Today 5:00 PM"],
    ["BK-10288", "Neha S.", "Urban Trim", 1499, "COMPLETED", "Today 2:10 PM"],
  ],
  pendingSellerApprovals: 18,
};

export const users = [
  { id: "usr-1", avatar: "PS", name: "Priya Sharma", phone: "+91 98765 11001", email: "priya@example.com", role: "USER", status: "ACTIVE", warningCount: 0, joinedAt: "2026-05-10" },
  { id: "usr-2", avatar: "AP", name: "Arjun Pai", phone: "+91 98765 11002", email: "arjun@example.com", role: "USER", status: "BLOCKED", warningCount: 3, joinedAt: "2026-04-18" },
  { id: "usr-3", avatar: "MS", name: "Meera Shah", phone: "+91 98765 11003", email: "meera@example.com", role: "SELLER", status: "ACTIVE", warningCount: 0, joinedAt: "2026-03-21" },
];

export const sellers = [
  { id: "sel-1", shopName: "Red Chair Studio", owner: "Ayaan Khan", type: "SHOP", status: "ACTIVE", subscriptionStatus: "ACTIVE", joinedAt: "2026-03-01" },
  { id: "sel-2", shopName: "Home Glow Pro", owner: "Isha Rao", type: "HOME_SERVICE", status: "PENDING_APPROVAL", subscriptionStatus: "TRIAL", joinedAt: "2026-05-19" },
  { id: "sel-3", shopName: "Urban Trim Lounge", owner: "Rahul Nair", type: "SHOP", status: "BLOCKED", subscriptionStatus: "EXPIRED", joinedAt: "2026-02-08" },
];

export const bookings = [
  { id: "BK-10291", user: "Priya Sharma", salon: "Red Chair Studio", services: "Haircut + Beard", scheduledAt: "2026-05-22T18:30:00+05:30", amount: 498, paymentStatus: "PAID", status: "CONFIRMED", cancelledBy: "" },
  { id: "BK-10290", user: "Arjun Pai", salon: "Glow & Snip", services: "Facial", scheduledAt: "2026-05-22T17:00:00+05:30", amount: 699, paymentStatus: "AT_SHOP", status: "PENDING", cancelledBy: "" },
  { id: "BK-10270", user: "Neha Shah", salon: "Urban Trim", services: "Hair Color", scheduledAt: "2026-05-20T12:00:00+05:30", amount: 1499, paymentStatus: "REFUNDED", status: "CANCELLED", cancelledBy: "USER" },
];

export const adminConfig = {
  userCommissionPct: 2.5,
  convenienceFeeMin: 10,
  convenienceFeeMax: 30,
  sellerCommissionPct: 12,
  homeServiceCommissionPct: 15,
  homePriceMultiplier: 1.6,
  travelFeePerKm: 10,
  gstPct: 18,
  additionalTaxLabel: "State Cess",
  additionalTaxPct: 0,
  cancellationWindowMinutes: 15,
  maxWarningsBeforeBlock: 3,
  sellerTrialDays: 30,
  featuredListingPriceMonthly: 499,
  userPremiumPriceMonthly: 199,
  sellerSubscriptionMonthly: 999,
  updatedAt: "2026-05-22T10:00:00+05:30",
};

export const subscriptions = [
  { id: "sub-1", seller: "Ayaan Khan", shop: "Red Chair Studio", plan: "Seller Monthly", status: "ACTIVE", startDate: "2026-05-01", expiryDate: "2026-06-01", amount: 999 },
  { id: "sub-2", seller: "Isha Rao", shop: "Home Glow Pro", plan: "Trial", status: "TRIAL", startDate: "2026-05-19", expiryDate: "2026-06-18", amount: 0 },
];

export const promotions = [
  { id: "promo-1", salon: "Red Chair Studio", featuredSince: "2026-05-01", featuredExpiry: "2026-06-01", status: "ACTIVE" },
  { id: "promo-2", salon: "Glow & Snip", featuredSince: "2026-04-20", featuredExpiry: "2026-05-25", status: "ACTIVE" },
];

export const premiumUsers = [
  { id: "pre-1", user: "Priya Sharma", phone: "+91 98765 11001", premiumSince: "2026-05-01", premiumExpiry: "2026-06-01", status: "ACTIVE" },
  { id: "pre-2", user: "Neha Shah", phone: "+91 98765 11044", premiumSince: "2026-04-01", premiumExpiry: "2026-05-01", status: "EXPIRED" },
];

export const revenueRows = Array.from({ length: 10 }, (_, index) => ({
  date: `2026-05-${String(13 + index).padStart(2, "0")}`,
  bookings: 120 + index * 9,
  gross: 220000 + index * 19000,
  commission: 24000 + index * 2100,
  platformFee: 5500 + index * 450,
  netToSellers: 190000 + index * 16500,
}));

export const notificationHistory = [
  { id: "n-1", target: "All Users", title: "Weekend slots open", sentAt: "2026-05-21 10:30", recipientCount: 9820 },
  { id: "n-2", target: "All Sellers", title: "Subscription reminder", sentAt: "2026-05-20 12:00", recipientCount: 386 },
];
