import { useMemo, useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { api, fetchOrFallback } from "../api/client";
import { Badge } from "../components/ui/badge";
import { Button } from "../components/ui/button";
import { Card, CardContent } from "../components/ui/card";
import { Input, Select } from "../components/ui/input";
import { Modal } from "../components/ui/modal";
import { Table, Td, Th } from "../components/ui/table";
import { users } from "../data/mock";
import { formatDate } from "../lib";

type UserRow = (typeof users)[number];

export function UsersPage() {
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState("ALL");
  const [selected, setSelected] = useState<UserRow | null>(null);
  const { data = users } = useQuery({ queryKey: ["admin-users"], queryFn: () => fetchOrFallback("/admin/users", users) });
  const action = useMutation({ mutationFn: (path: string) => api.put(path) });

  const filtered = useMemo(
    () =>
      data.filter(
        (user) =>
          (status === "ALL" || user.status === status) &&
          [user.name, user.phone, user.email].join(" ").toLowerCase().includes(query.toLowerCase()),
      ),
    [data, query, status],
  );

  return (
    <div className="space-y-5">
      <PageTitle title="Users" subtitle="Search, block, unblock, and reset warning counts." />
      <Card>
        <CardContent className="flex flex-col gap-3 p-4 md:flex-row">
          <Input placeholder="Search name, phone, email" value={query} onChange={(event) => setQuery(event.target.value)} />
          <Select value={status} onChange={(event) => setStatus(event.target.value)} className="md:w-48">
            <option value="ALL">All statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="BLOCKED">Blocked</option>
          </Select>
        </CardContent>
      </Card>
      <Table>
        <thead>
          <tr>
            {["User", "Phone", "Email", "Role", "Status", "Warnings", "Joined", "Actions"].map((head) => <Th key={head}>{head}</Th>)}
          </tr>
        </thead>
        <tbody>
          {filtered.map((user) => (
            <tr key={user.id}>
              <Td><div className="flex items-center gap-2"><span className="grid h-8 w-8 place-items-center rounded-full bg-brand-light font-bold text-brand-red">{user.avatar}</span>{user.name}</div></Td>
              <Td>{user.phone}</Td>
              <Td>{user.email}</Td>
              <Td>{user.role}</Td>
              <Td><Badge value={user.status} /></Td>
              <Td>{user.warningCount}</Td>
              <Td>{formatDate(user.joinedAt)}</Td>
              <Td>
                <div className="flex flex-wrap gap-2">
                  <Button variant="outline" onClick={() => setSelected(user)}>View</Button>
                  <Button variant={user.status === "BLOCKED" ? "secondary" : "danger"} onClick={() => action.mutate(`/admin/users/${user.id}/${user.status === "BLOCKED" ? "unblock" : "block"}`)}>
                    {user.status === "BLOCKED" ? "Unblock" : "Block"}
                  </Button>
                  <Button variant="ghost" onClick={() => action.mutate(`/admin/users/${user.id}/warnings/reset`)}>Reset</Button>
                </div>
              </Td>
            </tr>
          ))}
        </tbody>
      </Table>
      <Modal title="User detail" open={Boolean(selected)} onClose={() => setSelected(null)}>
        {selected && (
          <div className="space-y-4">
            <div className="grid gap-2 md:grid-cols-2">
              <Detail label="Name" value={selected.name} />
              <Detail label="Phone" value={selected.phone} />
              <Detail label="Email" value={selected.email} />
              <Detail label="Warnings" value={`${selected.warningCount}/3`} />
            </div>
            <div className="rounded-snippy bg-brand-light p-4">
              <h3 className="font-bold">Booking history</h3>
              <p className="text-sm text-brand-muted">Latest appointments, warning log, and premium status load here from /admin/users/{selected.id}.</p>
            </div>
          </div>
        )}
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
