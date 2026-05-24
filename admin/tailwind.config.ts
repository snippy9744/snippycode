import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        brand: {
          red: "#D32F2F",
          dark: "#B71C1C",
          light: "#FFF5F5",
          ink: "#1A1A1A",
          muted: "#616161",
          success: "#2E7D32",
          warning: "#F57F17",
          error: "#C62828",
        },
      },
      borderRadius: {
        snippy: "8px",
      },
      fontFamily: {
        sans: ["Inter", "ui-sans-serif", "system-ui"],
        mono: ["JetBrains Mono", "ui-monospace", "SFMono-Regular", "monospace"],
      },
    },
  },
  plugins: [],
} satisfies Config;
