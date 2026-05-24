import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { fetchOrFallback } from "../api/client";
import { Badge } from "../components/ui/badge";
import { Select } from "../components/ui/input";
import { Table, Td, Th } from "../components/ui/table";
import { premiumUsers } from "../data/mock";
import { formatDate } from "../lib";

export function PremiumPage() {
  const [status, setStatus] = useState("ALL");
  const { data = premiumUsers } = useQuery({ queryKey: ["admin-premium"], queryFn: () => fetchOrFallback("/admin/premium", premiumUsers) });
  const rows = data.filter((row) => status === "ALL" || row.status === status);
  return (
    <div className="space-y-5">
      <Title title="Premium" subtitle="User premium memberships and expiry state." />
      <Select value={status} onChange={(event) => setStatus(event.target.value)} className="max-w-xs"><option value="ALL">All</option><option value="ACTIVE">Active</option><option value="EXPIRED">Expired</option></Select>
      <Table><thead><tr>{["User", "Phone", "Premium Since", "Premium Expiry", "Status"].map((head) => <Th key={head}>{head}</Th>)}</tr></thead>
        <tbody>{rows.map((row) => <tr key={row.id}><Td>{row.user}</Td><Td>{row.phone}</Td><Td>{formatDate(row.premiumSince)}</Td><Td>{formatDate(row.premiumExpiry)}</Td><Td><Badge value={row.status} /></Td></tr>)}</tbody>
      </Table>
    </div>
  );
}

function Title({ title, subtitle }: { title: string; subtitle: string }) {
  return <div><h1 className="text-2xl font-bold">{title}</h1><p className="text-sm text-brand-muted">{subtitle}</p></div>;
}
