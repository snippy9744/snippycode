import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { fetchOrFallback } from "../api/client";
import { Badge } from "../components/ui/badge";
import { Card, CardContent, CardHeader } from "../components/ui/card";
import { Select } from "../components/ui/input";
import { Table, Td, Th } from "../components/ui/table";
import { subscriptions } from "../data/mock";
import { formatCurrency, formatDate } from "../lib";

export function SubscriptionsPage() {
  const [status, setStatus] = useState("ALL");
  const { data = subscriptions } = useQuery({ queryKey: ["admin-subscriptions"], queryFn: () => fetchOrFallback("/admin/subscriptions", subscriptions) });
  const rows = data.filter((row) => status === "ALL" || row.status === status);

  return (
    <div className="space-y-5">
      <Title title="Subscriptions" subtitle="Seller plan status and expiry monitoring." />
      <Card>
        <CardHeader><h2 className="font-bold text-brand-warning">Expiring soon</h2></CardHeader>
        <CardContent className="text-sm text-brand-muted">Highlight sellers expiring within 7 days for renewal outreach.</CardContent>
      </Card>
      <Select value={status} onChange={(event) => setStatus(event.target.value)} className="max-w-xs">
        <option value="ALL">All</option><option value="ACTIVE">Active</option><option value="EXPIRED">Expired</option><option value="TRIAL">Trial</option><option value="CANCELLED">Cancelled</option>
      </Select>
      <Table><thead><tr>{["Seller", "Shop", "Plan", "Status", "Start", "Expiry", "Amount"].map((head) => <Th key={head}>{head}</Th>)}</tr></thead>
        <tbody>{rows.map((row) => <tr key={row.id}><Td>{row.seller}</Td><Td>{row.shop}</Td><Td>{row.plan}</Td><Td><Badge value={row.status} /></Td><Td>{formatDate(row.startDate)}</Td><Td>{formatDate(row.expiryDate)}</Td><Td>{formatCurrency(row.amount)}</Td></tr>)}</tbody>
      </Table>
    </div>
  );
}

function Title({ title, subtitle }: { title: string; subtitle: string }) {
  return <div><h1 className="text-2xl font-bold">{title}</h1><p className="text-sm text-brand-muted">{subtitle}</p></div>;
}
