import { useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { api, fetchOrFallback } from "../api/client";
import { Button } from "../components/ui/button";
import { Card, CardContent, CardHeader } from "../components/ui/card";
import { Input, Textarea } from "../components/ui/input";
import { Table, Td, Th } from "../components/ui/table";
import { notificationHistory } from "../data/mock";

export function NotificationsPage() {
  const [target, setTarget] = useState("All Users");
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const { data = notificationHistory } = useQuery({ queryKey: ["notification-history"], queryFn: () => fetchOrFallback("/admin/notifications/history", notificationHistory) });
  const send = useMutation({ mutationFn: () => api.post("/admin/notifications/broadcast", { target, title, body }), onSuccess: () => { setTitle(""); setBody(""); } });
  return (
    <div className="space-y-5">
      <Title title="Notifications" subtitle="Broadcast FCM notifications to users and sellers." />
      <Card><CardHeader><h2 className="font-bold">Broadcast</h2></CardHeader><CardContent className="space-y-4">
        <div className="flex flex-wrap gap-3">{["All Users", "All Sellers", "Everyone"].map((option) => <label key={option} className="flex items-center gap-2 text-sm font-semibold"><input type="radio" checked={target === option} onChange={() => setTarget(option)} />{option}</label>)}</div>
        <Input placeholder="Title" value={title} onChange={(event) => setTitle(event.target.value)} />
        <Textarea placeholder="Body" value={body} onChange={(event) => setBody(event.target.value)} />
        <Button disabled={!title || !body || send.isPending} onClick={() => send.mutate()}>{send.isPending ? "Sending..." : "Send Notification"}</Button>
      </CardContent></Card>
      <Table><thead><tr>{["Target", "Title", "Sent At", "Recipients"].map((head) => <Th key={head}>{head}</Th>)}</tr></thead><tbody>{data.map((row) => <tr key={row.id}><Td>{row.target}</Td><Td>{row.title}</Td><Td>{row.sentAt}</Td><Td>{row.recipientCount.toLocaleString("en-IN")}</Td></tr>)}</tbody></Table>
    </div>
  );
}

function Title({ title, subtitle }: { title: string; subtitle: string }) {
  return <div><h1 className="text-2xl font-bold">{title}</h1><p className="text-sm text-brand-muted">{subtitle}</p></div>;
}
