import { useMemo, useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { api, fetchOrFallback } from "../api/client";
import { Badge } from "../components/ui/badge";
import { Button } from "../components/ui/button";
import { Card, CardContent } from "../components/ui/card";
import { Input, Select, Textarea } from "../components/ui/input";
import { Modal } from "../components/ui/modal";
import { Table, Td, Th } from "../components/ui/table";
import { bookings } from "../data/mock";
import { formatCurrency, formatDate } from "../lib";

type BookingRow = (typeof bookings)[number];

export function BookingsPage() {
  const [filters, setFilters] = useState({ status: "ALL", salonId: "", userId: "", from: "", to: "" });
  const [selected, setSelected] = useState<BookingRow | null>(null);
  const [cancelTarget, setCancelTarget] = useState<BookingRow | null>(null);
  const [reason, setReason] = useState("");
  const { data = bookings } = useQuery({ queryKey: ["admin-bookings"], queryFn: () => fetchOrFallback("/admin/bookings", bookings, filters) });
  const cancel = useMutation({ mutationFn: (id: string) => api.put(`/admin/bookings/${id}/cancel`, { reason }) });
  const rows = useMemo(() => data.filter((booking) => filters.status === "ALL" || booking.status === filters.status), [data, filters.status]);

  return (
    <div className="space-y-5">
      <PageTitle title="Bookings" subtitle="Filter, inspect, and administratively cancel bookings." />
      <Card><CardContent className="grid gap-3 p-4 md:grid-cols-5">
        <Select value={filters.status} onChange={(event) => setFilters((prev) => ({ ...prev, status: event.target.value }))}>
          <option value="ALL">All statuses</option><option value="CONFIRMED">Confirmed</option><option value="PENDING">Pending</option><option value="CANCELLED">Cancelled</option>
        </Select>
        <Input type="date" value={filters.from} onChange={(event) => setFilters((prev) => ({ ...prev, from: event.target.value }))} />
        <Input type="date" value={filters.to} onChange={(event) => setFilters((prev) => ({ ...prev, to: event.target.value }))} />
        <Input placeholder="Salon ID" value={filters.salonId} onChange={(event) => setFilters((prev) => ({ ...prev, salonId: event.target.value }))} />
        <Input placeholder="User ID" value={filters.userId} onChange={(event) => setFilters((prev) => ({ ...prev, userId: event.target.value }))} />
      </CardContent></Card>
      <Table>
        <thead><tr>{["Booking", "User", "Salon", "Services", "Scheduled", "Amount", "Payment", "Status", "Cancelled By", "Actions"].map((head) => <Th key={head}>{head}</Th>)}</tr></thead>
        <tbody>{rows.map((booking) => (
          <tr key={booking.id}>
            <Td className="font-semibold">{booking.id}</Td><Td>{booking.user}</Td><Td>{booking.salon}</Td><Td>{booking.services}</Td>
            <Td>{formatDate(booking.scheduledAt)}</Td><Td>{formatCurrency(booking.amount)}</Td><Td><Badge value={booking.paymentStatus} /></Td><Td><Badge value={booking.status} /></Td><Td>{booking.cancelledBy || "-"}</Td>
            <Td><div className="flex gap-2"><Button variant="outline" onClick={() => setSelected(booking)}>View</Button><Button variant="danger" onClick={() => setCancelTarget(booking)}>Cancel</Button></div></Td>
          </tr>
        ))}</tbody>
      </Table>
      <Modal title="Booking detail" open={Boolean(selected)} onClose={() => setSelected(null)}>
        {selected && <div className="space-y-3">
          <Detail label="Services" value={selected.services} />
          <Detail label="Amount" value={formatCurrency(selected.amount)} />
          <div className="rounded-snippy bg-brand-light p-4 text-sm text-brand-muted">Breakdown: services, tax, platform fee, seller commission, and refund status load from /admin/bookings/{selected.id}.</div>
        </div>}
      </Modal>
      <Modal title="Cancel booking" open={Boolean(cancelTarget)} onClose={() => setCancelTarget(null)}>
        <div className="space-y-4">
          <Textarea placeholder="Admin cancellation reason" value={reason} onChange={(event) => setReason(event.target.value)} />
          <Button variant="danger" disabled={!reason} onClick={() => { if (cancelTarget) cancel.mutate(cancelTarget.id); setCancelTarget(null); setReason(""); }}>Cancel Booking</Button>
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
