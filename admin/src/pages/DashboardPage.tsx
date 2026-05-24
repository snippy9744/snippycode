import { useQuery } from "@tanstack/react-query";
import { Bar, BarChart, CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { fetchOrFallback } from "../api/client";
import { Card, CardContent, CardHeader } from "../components/ui/card";
import { Badge } from "../components/ui/badge";
import { Table, Td, Th } from "../components/ui/table";
import { dashboard } from "../data/mock";
import { formatCurrency } from "../lib";

export function DashboardPage() {
  const { data: queryData } = useQuery({
    queryKey: ["admin-dashboard"],
    queryFn: () => fetchOrFallback("/admin/dashboard", dashboard),
  });
  const data = queryData ?? dashboard;

  const stats = [
    ["Total Users", data.stats.totalUsers.toLocaleString("en-IN")],
    ["Active Sellers", data.stats.activeSellers.toLocaleString("en-IN")],
    ["Today Bookings", data.stats.todayBookings.toLocaleString("en-IN")],
    ["Today Revenue", formatCurrency(data.stats.todayRevenue)],
    ["Platform Revenue", formatCurrency(data.stats.platformRevenue)],
  ];

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <p className="text-sm text-brand-muted">Operational snapshot across users, sellers, bookings, and revenue.</p>
      </div>
      <div className="grid gap-3 md:grid-cols-5">
        {stats.map(([label, value]) => (
          <Card key={label}>
            <CardContent className="p-4">
              <div className="text-xs font-bold uppercase text-brand-muted">{label}</div>
              <div className="mt-2 text-xl font-bold">{value}</div>
            </CardContent>
          </Card>
        ))}
      </div>
      <div className="grid gap-4 xl:grid-cols-[1.5fr_1fr]">
        <Card>
          <CardHeader>
            <h2 className="font-bold">Revenue last 30 days</h2>
          </CardHeader>
          <CardContent className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={data.revenue30Days}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="day" />
                <YAxis />
                <Tooltip formatter={(value) => formatCurrency(Number(value))} />
                <Line type="monotone" dataKey="gross" stroke="#D32F2F" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="platform" stroke="#1A1A1A" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <h2 className="font-bold">Bookings by day</h2>
          </CardHeader>
          <CardContent className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data.bookings7Days}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="day" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="bookings" fill="#D32F2F" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>
      <div className="grid gap-4 xl:grid-cols-[1fr_20rem]">
        <Card>
          <CardHeader>
            <h2 className="font-bold">Recent bookings</h2>
          </CardHeader>
          <CardContent>
            <Table>
              <thead>
                <tr>
                  {["Booking", "User", "Salon", "Amount", "Status", "Time"].map((head) => (
                    <Th key={head}>{head}</Th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {data.recentBookings.map((row) => (
                  <tr key={row[0]}>
                    <Td>{row[0]}</Td>
                    <Td>{row[1]}</Td>
                    <Td>{row[2]}</Td>
                    <Td>{formatCurrency(Number(row[3]))}</Td>
                    <Td><Badge value={String(row[4])} /></Td>
                    <Td>{row[5]}</Td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <h2 className="font-bold">Pending seller approvals</h2>
          </CardHeader>
          <CardContent>
            <div className="text-4xl font-bold text-brand-red">{data.pendingSellerApprovals}</div>
            <p className="mt-2 text-sm text-brand-muted">Home service and shop sellers waiting for review.</p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
