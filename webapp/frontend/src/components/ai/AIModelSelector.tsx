"use client";

import { cn } from "@/lib/utils";

const AI_MODELS = [
  { value: "openai/gpt-4o-mini", label: "GPT-4o Mini (nhanh, rẻ)", badge: "Đề xuất" },
  { value: "openai/gpt-4o", label: "GPT-4o (mạnh nhất)", badge: "Premium" },
  { value: "anthropic/claude-haiku-4.5", label: "Claude 4 Haiku (cân bằng)" },
  { value: "anthropic/claude-sonnet-4", label: "Claude 4 Sonnet (sâu sắc)", badge: "Premium" },
  { value: "google/gemini-flash-1.5", label: "Gemini Flash 1.5 (siêu nhanh)" },
  { value: "deepseek/deepseek-chat", label: "DeepSeek Chat (tiết kiệm)" },
];

interface AIModelSelectorProps {
  value?: string;
  onChange: (model: string) => void;
  className?: string;
}

export function AIModelSelector({ value, onChange, className }: AIModelSelectorProps) {
  return (
    <div className={cn("", className)}>
      <label className="text-text-soft mb-1.5 block text-[11px] tracking-[2px] uppercase">
        AI Model
      </label>
      <select
        value={value ?? "openai/gpt-4o-mini"}
        onChange={(e) => onChange(e.target.value)}
        className="text-text-dark w-full rounded-xl px-3 py-2.5 text-sm outline-none transition-all"
        style={{ background: "rgba(255,252,248,0.7)", border: "1px solid var(--ls-border-warm)" }}
      >
        {AI_MODELS.map((m) => (
          <option key={m.value} value={m.value}>
            {m.label} {m.badge ? `· ${m.badge}` : ""}
          </option>
        ))}
      </select>
    </div>
  );
}
