import {
  BadgeIndianRupee,
  Bell,
  CalendarCheck,
  Crown,
  Gauge,
  Megaphone,
  Menu,
  Settings,
  ShieldCheck,
  Store,
  Users,
  WalletCards,
  X,
} from "lucide-react";
import { useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { clearTokens } from "../../api/auth";
import { cn } from "../../lib";
import { Button } from "../ui/button";

const nav = [
  { to: "/", label: "Dashboard", icon: Gauge },
  { to: "/users", label: "Users", icon: Users },
  { to: "/sellers", label: "Sellers", icon: Store },
  { to: "/bookings", label: "Bookings", icon: CalendarCheck },
  { to: "/config", label: "Commission & Fees", icon: BadgeIndianRupee },
  { to: "/subscriptions", label: "Subscriptions", icon: WalletCards },
  { to: "/promotions", label: "Promotions", icon: Megaphone },
  { to: "/premium", label: "Premium", icon: Crown },
  { to: "/revenue", label: "Revenue", icon: ShieldCheck },
  { to: "/notifications", label: "Notifications", icon: Bell },
  { to: "/settings", label: "Settings", icon: Settings },
];

export function AdminLayout() {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();

  const sidebar = (
    <aside className="flex h-full w-72 flex-col bg-brand-ink text-white">
      <div className="flex h-16 items-center justify-between border-b border-white/10 px-4">
        <div className="flex items-center gap-3">
          <div className="grid h-9 w-9 place-items-center rounded-snippy bg-brand-red font-bold">S</div>
          <div>
            <div className="font-bold">Snippy Seat</div>
            <div className="text-xs text-white/60">Admin Console</div>
          </div>
        </div>
        <button className="lg:hidden" onClick={() => setOpen(false)}>
          <X className="h-5 w-5" />
        </button>
      </div>
      <nav className="flex-1 space-y-1 overflow-y-auto p-3">
        {nav.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            onClick={() => setOpen(false)}
            className={({ isActive }) =>
              cn(
                "flex items-center gap-3 rounded-snippy px-3 py-2.5 text-sm font-semibold text-white/72 hover:bg-white/10 hover:text-white",
                isActive && "bg-brand-red text-white",
              )
            }
          >
            <item.icon className="h-4 w-4" />
            {item.label}
          </NavLink>
        ))}
      </nav>
      <div className="border-t border-white/10 p-3">
        <Button
          variant="ghost"
          className="w-full justify-start text-white hover:bg-white/10"
          onClick={() => {
            clearTokens();
            navigate("/login");
          }}
        >
          Logout
        </Button>
      </div>
    </aside>
  );

  return (
    <div className="min-h-screen lg:grid lg:grid-cols-[18rem_1fr]">
      <div className="hidden lg:block">{sidebar}</div>
      {open && <div className="fixed inset-0 z-50 lg:hidden">{sidebar}</div>}
      <main className="min-w-0">
        <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-neutral-200 bg-white px-4">
          <button className="lg:hidden" onClick={() => setOpen(true)}>
            <Menu className="h-5 w-5" />
          </button>
          <div>
            <div className="text-sm font-bold text-brand-red">Operations</div>
            <div className="text-xs text-brand-muted">Live admin tools for Snippy Seat</div>
          </div>
        </header>
        <div className="p-4 lg:p-6">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
