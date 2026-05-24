import { useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { api, fetchOrFallback } from "../api/client";
import { Badge } from "../components/ui/badge";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Table, Td, Th } from "../components/ui/table";
import { promotions } from "../data/mock";
import { formatDate } from "../lib";

export function PromotionsPage() {
  const { data = promotions } = useQuery({ queryKey: ["admin-promotions"], queryFn: () => fetchOrFallback("/admin/promotions", promotions) });
  const [expiry, setExpiry] = useState<Record<string, string>>({});
  const mutate = useMutation({ mutationFn: (payload: { id: string; enabled: boolean; expiry?: string }) => api.put(`/admin/promotions/${payload.id}`, payload) });

  return (
    <div className="space-y-5">
      <Title title="Promotions" subtitle="Featured salon status and expiry dates." />
      <Table><thead><tr>{["Salon", "Featured Since", "Featured Expiry", "Status", "Set Expiry", "Actions"].map((head) => <Th key={head}>{head}</Th>)}</tr></thead>
        <tbody>{data.map((row) => <tr key={row.id}><Td className="font-semibold">{row.salon}</Td><Td>{formatDate(row.featuredSince)}</Td><Td>{formatDate(row.featuredExpiry)}</Td><Td><Badge value={row.status} /></Td><Td><Input type="date" value={expiry[row.id] ?? ""} onChange={(event) => setExpiry((prev) => ({ ...prev, [row.id]: event.target.value }))} /></Td><Td><Button onClick={() => mutate.mutate({ id: row.id, enabled: row.status !== "ACTIVE", expiry: expiry[row.id] })}>{row.status === "ACTIVE" ? "Disable" : "Enable"}</Button></Td></tr>)}</tbody>
      </Table>
    </div>
  );
}

function Title({ title, subtitle }: { title: string; subtitle: string }) {
  return <div><h1 className="text-2xl font-bold">{title}</h1><p className="text-sm text-brand-muted">{subtitle}</p></div>;
}
