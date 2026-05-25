"use client";

import { Quote } from "lucide-react";

interface DailyQuoteCardProps {
  quote: string;
  author: string;
  authorTitle?: string;
}

/**
 * A beautiful card to display a daily quote inside DayDetailModal.
 */
export function DailyQuoteCard({ quote, author, authorTitle }: DailyQuoteCardProps) {
  return (
    <div
      className="relative overflow-hidden rounded-xl px-5 py-4"
      style={{
        background: "linear-gradient(135deg, var(--warm-cream) 0%, var(--warm-peach) 100%)",
        border: "1px solid var(--ls-border-warm)",
      }}
    >
      {/* Background icon */}
      <Quote
        className="text-warm-amber/10 absolute top-2 right-3 h-10 w-10 rotate-180"
        strokeWidth={1.5}
      />

      <div className="mb-2 flex items-center gap-1.5">
        <Quote className="text-warm-amber/60 h-3 w-3" />
        <span className="text-warm-amber text-[9px] font-medium tracking-[2px] uppercase">
          Danh ngôn hôm nay
        </span>
      </div>

      <p className="text-text-dark relative z-10 mb-2 text-[13px] leading-relaxed font-[var(--font-lora)] italic">
        &ldquo;{quote}&rdquo;
      </p>

      <div className="text-text-soft text-[11px]">
        — <span className="text-text-mid font-medium">{author}</span>
        {authorTitle && <span className="text-text-muted-ls"> · {authorTitle}</span>}
      </div>
    </div>
  );
}
