"use client";

import { useQuoteOfTheDay, useRandomQuote } from "@/hooks/usePublicContent";
import { Heart, RefreshCw, Share2 } from "lucide-react";
import { useState } from "react";

export function V2QuoteCard() {
  const { data: todayQuote, isLoading } = useQuoteOfTheDay();
  const { refetch: fetchRandom } = useRandomQuote();
  const [currentQuote, setCurrentQuote] = useState<typeof todayQuote>(undefined);

  const quote = currentQuote ?? todayQuote;
  const quoteData = quote?.data;

  const handleRandom = async () => {
    const result = await fetchRandom();
    if (result.data) {
      setCurrentQuote(result.data);
    }
  };

  if (isLoading) {
    return (
      <div
        className="mb-5 animate-pulse rounded-xl p-7"
        style={{
          background: "var(--v2-bg-card)",
          border: "1px solid var(--v2-border-primary)",
          borderLeft: "3px solid var(--v2-bg-gold)",
        }}
      >
        <div className="h-3 w-32 rounded" style={{ background: "var(--v2-bg-hover)" }} />
        <div className="mt-4 h-5 w-3/4 rounded" style={{ background: "var(--v2-bg-hover)" }} />
        <div className="mt-2 h-5 w-2/3 rounded" style={{ background: "var(--v2-bg-hover)" }} />
      </div>
    );
  }

  if (!quoteData) return null;

  return (
    <div
      className="v2-card relative mb-5 rounded-xl px-7 py-7"
      style={{
        background: "var(--v2-bg-card)",
        border: "1px solid var(--v2-border-primary)",
        borderLeft: "3px solid var(--v2-bg-gold)",
        boxShadow: "var(--v2-shadow-xs)",
      }}
    >
      {/* Big quote mark */}
      <span
        className="pointer-events-none absolute top-2 left-5 font-playfair text-[80px] leading-none select-none"
        style={{ color: "var(--v2-bg-accent)", opacity: 0.06 }}
      >
        &ldquo;
      </span>

      {/* Label */}
      <div
        className="mb-3.5 flex items-center gap-1.5 text-[11px] font-semibold uppercase"
        style={{ color: "var(--v2-text-gold)", letterSpacing: "1.5px" }}
      >
        <span>✦</span>
        CHÂM NGÔN HÔM NAY
      </div>

      {/* Quote text */}
      <p
        className="font-playfair mb-2.5 text-lg leading-relaxed italic sm:text-[19px]"
        style={{ color: "var(--v2-text-primary)", lineHeight: 1.9 }}
      >
        &ldquo;{quoteData.quote}&rdquo;
      </p>

      {/* Original quote if available */}
      {quoteData.original_quote && (
        <p
          className="mb-2 text-[13px] italic"
          style={{ color: "var(--v2-text-muted)", opacity: 0.7 }}
        >
          {quoteData.original_quote}
        </p>
      )}

      {/* Source */}
      <p
        className="text-right text-[13px]"
        style={{ color: "var(--v2-text-muted)" }}
      >
        — {quoteData.author}
      </p>

      {/* Actions */}
      <div className="mt-3.5 flex justify-end gap-1.5">
        <QuoteBtn icon={<Heart className="h-3 w-3" />} label="Yêu thích" />
        <QuoteBtn icon={<RefreshCw className="h-3 w-3" />} label="Câu khác" onClick={handleRandom} />
        <QuoteBtn icon={<Share2 className="h-3 w-3" />} label="Chia sẻ" />
      </div>
    </div>
  );
}

function QuoteBtn({
  icon,
  label,
  onClick,
}: {
  icon: React.ReactNode;
  label: string;
  onClick?: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className="flex items-center gap-1 rounded-full border px-3 py-1.5 text-[12px] transition-all"
      style={{
        borderColor: "var(--v2-border-primary)",
        color: "var(--v2-text-muted)",
      }}
      onMouseEnter={(e) => {
        (e.currentTarget as HTMLElement).style.borderColor = "var(--v2-text-accent)";
        (e.currentTarget as HTMLElement).style.color = "var(--v2-text-accent)";
        (e.currentTarget as HTMLElement).style.background = "var(--v2-bg-accent-soft)";
      }}
      onMouseLeave={(e) => {
        (e.currentTarget as HTMLElement).style.borderColor = "var(--v2-border-primary)";
        (e.currentTarget as HTMLElement).style.color = "var(--v2-text-muted)";
        (e.currentTarget as HTMLElement).style.background = "transparent";
      }}
    >
      {icon}
      {label}
    </button>
  );
}
