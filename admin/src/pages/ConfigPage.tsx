import { useEffect, useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { api, fetchOrFallback } from "../api/client";
import { Button } from "../components/ui/button";
import { Card, CardContent, CardHeader } from "../components/ui/card";
import { Input } from "../components/ui/input";
import { adminConfig } from "../data/mock";
import { formatDate } from "../lib";

type Config = typeof adminConfig;

const sections: Array<{ title: string; fields: Array<{ key: keyof Config; label: string; description?: string; type?: string }> }> = [
  {
    title: "User Charges",
    fields: [
      { key: "userCommissionPct", label: "User Commission %", description: "Added to user's total" },
      { key: "convenienceFeeMin", label: "Convenience Fee Min Rs" },
      { key: "convenienceFeeMax", label: "Convenience Fee Max Rs" },
    ],
  },
  {
    title: "Seller Charges",
    fields: [
      { key: "sellerCommissionPct", label: "Seller Commission %", description: "Deducted from seller payout" },
      { key: "homeServiceCommissionPct", label: "Home Service Commission %" },
      { key: "homePriceMultiplier", label: "Home Price Multiplier" },
      { key: "travelFeePerKm", label: "Travel Fee per KM Rs" },
    ],
  },
  {
    title: "Tax",
    fields: [
      { key: "gstPct", label: "GST %" },
      { key: "additionalTaxLabel", label: "Additional Tax Label", type: "text" },
      { key: "additionalTaxPct", label: "Additional Tax %" },
    ],
  },
  {
    title: "Platform",
    fields: [
      { key: "cancellationWindowMinutes", label: "Cancellation Window (minutes)" },
      { key: "maxWarningsBeforeBlock", label: "Max Warnings Before Block" },
      { key: "sellerTrialDays", label: "Seller Trial Days" },
      { key: "featuredListingPriceMonthly", label: "Featured Listing Price/month Rs" },
    ],
  },
  {
    title: "Membership",
    fields: [
      { key: "userPremiumPriceMonthly", label: "User Premium Price/month Rs" },
      { key: "sellerSubscriptionMonthly", label: "Seller Subscription Price/month Rs" },
    ],
  },
];

export function ConfigPage() {
  const { data = adminConfig } = useQuery({ queryKey: ["admin-config"], queryFn: () => fetchOrFallback("/admin/config", adminConfig) });
  const [form, setForm] = useState<Config>(data);
  const [savedAt, setSavedAt] = useState(data.updatedAt);
  const save = useMutation({
    mutationFn: async () => api.put("/admin/config", form),
    onSettled: () => setSavedAt(new Date().toISOString()),
  });

  useEffect(() => setForm(data), [data]);

  return (
    <div className="space-y-5">
      <div className="flex flex-col justify-between gap-3 md:flex-row md:items-end">
        <div>
          <h1 className="text-2xl font-bold">Commission & Fees</h1>
          <p className="text-sm text-brand-muted">Changes apply to the next booking calculation.</p>
        </div>
        <Button onClick={() => save.mutate()} disabled={save.isPending}>{save.isPending ? "Saving..." : "Save Changes"}</Button>
      </div>
      <div className="grid gap-4 xl:grid-cols-2">
        {sections.map((section) => (
          <Card key={section.title}>
            <CardHeader>
              <h2 className="font-bold">{section.title}</h2>
              <p className="text-xs text-brand-muted">Last updated: {formatDate(savedAt)}</p>
            </CardHeader>
            <CardContent className="grid gap-4 md:grid-cols-2">
              {section.fields.map((field) => (
                <label key={String(field.key)} className="space-y-1">
                  <span className="text-sm font-semibold">{field.label}</span>
                  <Input
                    type={field.type ?? "number"}
                    value={String(form[field.key] ?? "")}
                    onChange={(event) =>
                      setForm((prev) => ({
                        ...prev,
                        [field.key]: field.type === "text" ? event.target.value : Number(event.target.value),
                      }))
                    }
                  />
                  {field.description && <span className="text-xs text-brand-muted">{field.description}</span>}
                </label>
              ))}
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
