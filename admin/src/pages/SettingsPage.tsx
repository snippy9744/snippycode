import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { api } from "../api/client";
import { Button } from "../components/ui/button";
import { Card, CardContent, CardHeader } from "../components/ui/card";
import { Input } from "../components/ui/input";

export function SettingsPage() {
  const [password, setPassword] = useState("");
  const [admin, setAdmin] = useState({ email: "", tempPassword: "" });
  const changePassword = useMutation({ mutationFn: () => api.put("/admin/account/password", { password }) });
  const createAdmin = useMutation({ mutationFn: () => api.post("/admin/accounts", admin) });
  return (
    <div className="space-y-5">
      <Title title="Settings" subtitle="Admin account management." />
      <div className="grid gap-4 xl:grid-cols-2">
        <Card><CardHeader><h2 className="font-bold">Change password</h2></CardHeader><CardContent className="space-y-3"><Input type="password" placeholder="New password" value={password} onChange={(event) => setPassword(event.target.value)} /><Button disabled={!password} onClick={() => changePassword.mutate()}>Save Password</Button></CardContent></Card>
        <Card><CardHeader><h2 className="font-bold">Create admin user</h2></CardHeader><CardContent className="space-y-3"><Input placeholder="Email" value={admin.email} onChange={(event) => setAdmin((prev) => ({ ...prev, email: event.target.value }))} /><Input placeholder="Temporary password" value={admin.tempPassword} onChange={(event) => setAdmin((prev) => ({ ...prev, tempPassword: event.target.value }))} /><Button disabled={!admin.email || !admin.tempPassword} onClick={() => createAdmin.mutate()}>Create Admin</Button></CardContent></Card>
      </div>
    </div>
  );
}

function Title({ title, subtitle }: { title: string; subtitle: string }) {
  return <div><h1 className="text-2xl font-bold">{title}</h1><p className="text-sm text-brand-muted">{subtitle}</p></div>;
}
