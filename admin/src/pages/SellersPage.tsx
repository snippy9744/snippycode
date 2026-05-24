import { useMemo, useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { api, fetchOrFallback } from "../api/client";
import { Badge } from "../components/ui/badge";
import { Button } from "../components/ui/button";
import { Card, CardContent } from "../components/ui/card";
import { Input, Select, Textarea } from "../components/ui/input";
import { Modal } from "../components/ui/modal";
import { Table, Td, Th } from "../components/ui/table";
import { sellers } from "../data/mock";
import { formatDate } from "../lib";

type SellerRow = (typeof sellers)[number];

export function SellersPage() {
  const [filters, setFilters] = useState({ status: "ALL", type: "ALL", subscriptionStatus: "ALL" });
  const [selected, setSelected] = useState<SellerRow | null>(null);
  const [decision, setDecision] = useState<{ seller: SellerRow; action: "approve" | "reject" } | null>(null);
  const [reason, setReason] = useState("");
  const { data = sellers } = useQuery({ queryKey: ["admin-sellers"], queryFn: () => fetchOrFallback("/admin/sellers", sellers) });
  const mutation = useMutation({ mutationFn: (path: string) => api.put(path, { reason }) });
  const rows = useMemo(
    () =>
      data.filter(
        (seller) =>
          (filters.status === "ALL" || seller.status === filters.status) &&
          (filters.type === "ALL" || seller.type === filters.type) &&
          (filters.subscriptionStatus === "ALL" || seller.subscriptionStatus === filters.subscriptionStatus),
      ),
    [data, filters],
  );

  return (
    <div className="space-y-5">
      <PageTitle title="Sellers" subtitle="Approve, reject, block, and inspect seller documents." />
      <Card>
        <CardContent className="grid gap-3 p-4 md:grid-cols-3">
          {(["status", "type", "subscriptionStatus"] as const).map((key) => (
            <Select key={key} value={filters[key]} onChange={(event) => setFilters((prev) => ({ ...prev, [key]: event.target.value }))}>
              <option value="ALL">{key}</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="PENDING_APPROVAL">PENDING_APPROVAL</option>
              <option value="BLOCKED">BLOCKED</option>
              <option value="SHOP">SHOP</option>
              <option value="HOME_SERVICE">HOME_SERVICE</option>
              <option value="TRIAL">TRIAL</option>
              <option value="EXPIRED">EXPIRED</option>
            </Select>
          ))}
        </CardContent>
      </Card>
      <Table>
        <thead><tr>{["Shop", "Owner", "Type", "Status", "Subscription", "Joined", "Actions"].map((head) => <Th key={head}>{head}</Th>)}</tr></thead>
        <tbody>
          {rows.map((seller) => (
            <tr key={seller.id}>
              <Td className="font-semibold">{seller.shopName}</Td>
              <Td>{seller.owner}</Td>
              <Td>{seller.type}</Td>
              <Td><Badge value={seller.status} /></Td>
              <Td><Badge value={seller.subscriptionStatus} /></Td>
              <Td>{formatDate(seller.joinedAt)}</Td>
              <Td><div className="flex flex-wrap gap-2">
                <Button variant="outline" onClick={() => setSelected(seller)}>View</Button>
                <Button variant="secondary" onClick={() => setDecision({ seller, action: "approve" })}>Approve</Button>
                <Button variant="danger" onClick={() => setDecision({ seller, action: "reject" })}>Reject</Button>
                <Button variant="ghost" onClick={() => mutation.mutate(`/admin/sellers/${seller.id}/block`)}>Block</Button>
              </div></Td>
            </tr>
          ))}
        </tbody>
      </Table>
      <Modal title="Seller detail" open={Boolean(selected)} onClose={() => setSelected(null)}>
        {selected && <div className="space-y-4">
          <div className="grid gap-2 md:grid-cols-2">
            <Detail label="Shop" value={selected.shopName} />
            <Detail label="Owner" value={selected.owner} />
            <Detail label="Type" value={selected.type} />
            <Detail label="Subscription" value={selected.subscriptionStatus} />
          </div>
          <div className="rounded-snippy border border-neutral-200 p-4">
            <h3 className="font-bold">Documents</h3>
            <p className="text-sm text-brand-muted">GST/Aadhaar photos and salon profile load from /admin/sellers/{selected.id}.</p>
          </div>
        </div>}
      </Modal>
      <Modal title={`${decision?.action === "approve" ? "Approve" : "Reject"} seller`} open={Boolean(decision)} onClose={() => setDecision(null)}>
        <div className="space-y-4">
          <p className="text-sm text-brand-muted">{decision?.seller.shopName}</p>
          <Textarea placeholder="Optional reason" value={reason} onChange={(event) => setReason(event.target.value)} />
          <Button onClick={() => { if (decision) mutation.mutate(`/admin/sellers/${decision.seller.id}/${decision.action}`); setDecision(null); setReason(""); }}>
            Confirm
          </Button>
        </div>
      </Modal>
    </div>
  );
}

function PageTitle({ title, subtitle }: { title: string; subtitle: string }) {
  return <div><h1 className="text-2xl font-bold">{title}</h1><p className="text-sm text-brand-muted">{subtitle}</p></div>;
}
function Detail({ label, value }: { label: string; value: string }) {
  return <div><div className="text-xs font-bold uppercase text-brand-muted">{label}</div><div className="font-semibold">{value}</div></div>;
}
