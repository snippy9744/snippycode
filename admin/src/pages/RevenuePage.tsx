import { useQuery } from "@tanstack/react-query";
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { fetchOrFallback } from "../api/client";
import { Button } from "../components/ui/button";
import { Card, CardContent } from "../components/ui/card";
import { Select } from "../components/ui/input";
import { Table, Td, Th } from "../components/ui/table";
import { dashboard, revenueRows } from "../data/mock";
import { formatCurrency, formatDate } from "../lib";

export function RevenuePage() {
  const { data = revenueRows } = useQuery({ queryKey: ["admin-revenue"], queryFn: () => fetchOrFallback("/admin/revenue", revenueRows) });
  const gross = data.reduce((sum, row) => sum + row.gross, 0);
  const commission = data.reduce((sum, row) => sum + row.commission, 0);
  const bookings = data.reduce((sum, row) => sum + row.bookings, 0);
  return (
    <div className="space-y-5">
      <div className="flex flex-col justify-between gap-3 md:flex-row md:items-end"><Title title="Revenue" subtitle="Gross revenue, platform revenue, commission, and seller payouts." /><div className="flex gap-2"><Select className="w-48"><option>This Week</option><option>This Month</option><option>Last 3 Months</option><option>Custom range</option></Select><Button onClick={() => exportCsv(data)}>CSV Export</Button></div></div>
      <div className="grid gap-3 md:grid-cols-4">{[["Gross Revenue", gross], ["Platform Revenue", dashboard.stats.platformRevenue], ["Total Commission", commission], ["Total Bookings", bookings]].map(([label, value]) => <Card key={label}><CardContent className="p-4"><div className="text-xs font-bold uppercase text-brand-muted">{label}</div><div className="mt-2 text-xl font-bold">{typeof value === "number" && label !== "Total Bookings" ? formatCurrency(value) : value}</div></CardContent></Card>)}</div>
      <Card><CardContent className="h-80 p-4"><ResponsiveContainer width="100%" height="100%"><AreaChart data={dashboard.revenue30Days}><CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="day" /><YAxis /><Tooltip formatter={(value) => formatCurrency(Number(value))} /><Area dataKey="gross" stroke="#D32F2F" fill="#FFF5F5" /><Area dataKey="platform" stroke="#1A1A1A" fill="#D9D9D9" /></AreaChart></ResponsiveContainer></CardContent></Card>
      <Table><thead><tr>{["Date", "Bookings", "Gross", "Commission", "Platform Fee", "Net to Sellers"].map((head) => <Th key={head}>{head}</Th>)}</tr></thead><tbody>{data.map((row) => <tr key={row.date}><Td>{formatDate(row.date)}</Td><Td>{row.bookings}</Td><Td>{formatCurrency(row.gross)}</Td><Td>{formatCurrency(row.commission)}</Td><Td>{formatCurrency(row.platformFee)}</Td><Td>{formatCurrency(row.netToSellers)}</Td></tr>)}</tbody></Table>
    </div>
  );
}

function exportCsv(rows: typeof revenueRows) {
  const csv = ["date,bookings,gross,commission,platformFee,netToSellers", ...rows.map((row) => `${row.date},${row.bookings},${row.gross},${row.commission},${row.platformFee},${row.netToSellers}`)].join("\n");
  const url = URL.createObjectURL(new Blob([csv], { type: "text/csv" }));
  const a = document.createElement("a");
  a.href = url;
  a.download = "snippy-revenue.csv";
  a.click();
  URL.revokeObjectURL(url);
}

function Title({ title, subtitle }: { title: string; subtitle: string }) {
  return <div><h1 className="text-2xl font-bold">{title}</h1><p className="text-sm text-brand-muted">{subtitle}</p></div>;
}
