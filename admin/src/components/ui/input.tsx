import { cn } from "../../lib";

export function Input(props: React.InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      {...props}
      className={cn(
        "h-10 w-full rounded-snippy border border-neutral-300 bg-white px-3 text-sm focus:border-brand-red focus:ring-2 focus:ring-brand-red/15",
        props.className,
      )}
    />
  );
}

export function Textarea(props: React.TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return (
    <textarea
      {...props}
      className={cn(
        "min-h-24 w-full rounded-snippy border border-neutral-300 bg-white px-3 py-2 text-sm focus:border-brand-red focus:ring-2 focus:ring-brand-red/15",
        props.className,
      )}
    />
  );
}

export function Select(props: React.SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <select
      {...props}
      className={cn(
        "h-10 w-full rounded-snippy border border-neutral-300 bg-white px-3 text-sm focus:border-brand-red focus:ring-2 focus:ring-brand-red/15",
        props.className,
      )}
    />
  );
}
