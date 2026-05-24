import { cn } from "../../lib";

export function Table({ children, className }: { children: React.ReactNode; className?: string }) {
  return (
    <div className={cn("overflow-x-auto rounded-snippy border border-neutral-200 bg-white", className)}>
      <table className="w-full min-w-[760px] text-left text-sm">{children}</table>
    </div>
  );
}

export function Th({ children }: { children: React.ReactNode }) {
  return <th className="border-b border-neutral-200 bg-neutral-50 px-3 py-3 text-xs font-bold uppercase tracking-wide text-brand-muted">{children}</th>;
}

export function Td({ children, className }: { children: React.ReactNode; className?: string }) {
  return <td className={cn("border-b border-neutral-100 px-3 py-3 align-middle", className)}>{children}</td>;
}
