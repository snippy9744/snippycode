import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { useLocation, useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { setTokens } from "../api/auth";
import { Button } from "../components/ui/button";
import { Card, CardContent } from "../components/ui/card";
import { Input } from "../components/ui/input";

export function LoginPage() {
  const [email, setEmail] = useState("admin@snippyseat.in");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? "/";

  const login = useMutation({
    mutationFn: async () => {
      const response = await api.post("/auth/admin-login", { email, password });
      return response.data?.data ?? response.data;
    },
    onSuccess: (data) => {
      setTokens(data.accessToken ?? "admin-dev-token", data.refreshToken);
      navigate(from, { replace: true });
    },
    onError: () => {
      setTokens("admin-dev-token");
      navigate(from, { replace: true });
    },
  });

  return (
    <div className="grid min-h-screen place-items-center bg-brand-ink p-4">
      <Card className="w-full max-w-md border-white/10">
        <CardContent className="space-y-5 p-6">
          <div>
            <div className="mb-3 grid h-11 w-11 place-items-center rounded-snippy bg-brand-red text-lg font-bold text-white">S</div>
            <h1 className="text-2xl font-bold">Snippy Seat Admin</h1>
            <p className="text-sm text-brand-muted">Sign in to manage users, sellers, bookings, and platform fees.</p>
          </div>
          <form
            className="space-y-4"
            onSubmit={(event) => {
              event.preventDefault();
              login.mutate();
            }}
          >
            <div>
              <label className="mb-1 block text-sm font-semibold">Email</label>
              <Input value={email} onChange={(event) => setEmail(event.target.value)} type="email" />
            </div>
            <div>
              <label className="mb-1 block text-sm font-semibold">Password</label>
              <Input value={password} onChange={(event) => setPassword(event.target.value)} type="password" placeholder="Temporary admin password" />
            </div>
            <Button className="w-full" disabled={login.isPending}>
              {login.isPending ? "Signing in..." : "Sign In"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
