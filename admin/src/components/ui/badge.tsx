import { cn } from "../../lib";

const colorMap: Record<string, string> = {
  ACTIVE: "bg-green-50 text-brand-success",
  CONFIRMED: "bg-blue-50 text-blue-700",
  PAID: "bg-green-50 text-brand-success",
  COMPLETED: "bg-green-50 text-brand-success",
  PENDING: "bg-orange-50 text-brand-warning",
  PENDING_APPROVAL: "bg-orange-50 text-brand-warning",
  TRIAL: "bg-orange-50 text-brand-warning",
  EXPIRED: "bg-neutral-100 text-brand-muted",
  CANCELLED: "bg-neutral-100 text-brand-muted",
  BLOCKED: "bg-red-50 text-brand-error",
};

export function Badge({ value, className }: { value: string; className?: string }) {
  return (
    <span className={cn("inline-flex rounded-full px-2.5 py-1 text-xs font-bold", colorMap[value] ?? "bg-neutral-100 text-brand-muted", className)}>
      {value.replace(/_/g, " ")}
    </span>
  );
}
