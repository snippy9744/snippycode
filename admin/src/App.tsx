import { BrowserRouter, Route, Routes } from "react-router-dom";
import { AdminLayout } from "./components/layout/AdminLayout";
import { ProtectedRoute } from "./components/layout/ProtectedRoute";
import { BookingsPage } from "./pages/BookingsPage";
import { ConfigPage } from "./pages/ConfigPage";
import { DashboardPage } from "./pages/DashboardPage";
import { LoginPage } from "./pages/LoginPage";
import { NotificationsPage } from "./pages/NotificationsPage";
import { PremiumPage } from "./pages/PremiumPage";
import { PromotionsPage } from "./pages/PromotionsPage";
import { RevenuePage } from "./pages/RevenuePage";
import { SellersPage } from "./pages/SellersPage";
import { SettingsPage } from "./pages/SettingsPage";
import { SubscriptionsPage } from "./pages/SubscriptionsPage";
import { UsersPage } from "./pages/UsersPage";

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<AdminLayout />}>
            <Route index element={<DashboardPage />} />
            <Route path="/users" element={<UsersPage />} />
            <Route path="/sellers" element={<SellersPage />} />
            <Route path="/bookings" element={<BookingsPage />} />
            <Route path="/config" element={<ConfigPage />} />
            <Route path="/subscriptions" element={<SubscriptionsPage />} />
            <Route path="/promotions" element={<PromotionsPage />} />
            <Route path="/premium" element={<PremiumPage />} />
            <Route path="/revenue" element={<RevenuePage />} />
            <Route path="/notifications" element={<NotificationsPage />} />
            <Route path="/settings" element={<SettingsPage />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
