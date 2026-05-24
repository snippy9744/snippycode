import { cn } from "../../lib";

type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "secondary" | "outline" | "ghost" | "danger";
};

export function Button({ className, variant = "primary", ...props }: ButtonProps) {
  const variants = {
    primary: "bg-brand-red text-white hover:bg-brand-dark",
    secondary: "bg-brand-light text-brand-red hover:bg-red-100",
    outline: "border border-neutral-300 bg-white text-brand-ink hover:bg-neutral-50",
    ghost: "text-brand-ink hover:bg-neutral-100",
    danger: "bg-brand-error text-white hover:bg-red-800",
  };

  return (
    <button
      className={cn(
        "inline-flex h-10 items-center justify-center rounded-snippy px-4 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-50",
        variants[variant],
        className,
      )}
      {...props}
    />
  );
}
