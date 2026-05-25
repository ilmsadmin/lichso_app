"use client";

import Link from "next/link";
import { Quote, ChevronRight } from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";
import { useQuoteOfTheDay } from "@/hooks/usePublicContent";
import { ROUTES } from "@/lib/constants";

/**
 * Compact "Quote of the Day" widget for the calendar view.
 */
export function QuoteOfTheDayWidget() {
  const { data, isLoading } = useQuoteOfTheDay();
  const quote = data?.data;

  if (isLoading) {
    return (
      <div
        className="rounded-xl p-4"
        style={{
          background: "var(--ls-card-bg)",
          border: "1px solid var(--ls-border-warm)",
        }}
      >
        <Skeleton className="mb-2 h-3 w-24" />
        <Skeleton className="mb-1 h-4 w-full" />
        <Skeleton className="mb-2 h-4 w-3/4" />
        <Skeleton className="h-3 w-32" />
      </div>
    );
  }

  if (!quote) return null;

  return (
    <Link
      href={ROUTES.QUOTES}
      className="group block rounded-xl p-4 transition-all hover:shadow-md"
      style={{
        background: "linear-gradient(135deg, var(--warm-cream) 0%, var(--warm-peach) 100%)",
        border: "1px solid var(--ls-border-warm)",
      }}
    >
      <div className="mb-2 flex items-center justify-between">
        <div className="flex items-center gap-1.5">
          <Quote className="text-warm-amber/60 h-3.5 w-3.5" />
          <span className="text-warm-amber text-[10px] font-medium tracking-wider uppercase">
            Danh ngôn hôm nay
          </span>
        </div>
        <ChevronRight className="text-text-muted-ls group-hover:text-warm-amber h-3.5 w-3.5 transition-colors" />
      </div>
      <p className="text-text-dark mb-1.5 line-clamp-2 text-sm leading-relaxed font-[var(--font-lora)] italic">
        &ldquo;{quote.quote}&rdquo;
      </p>
      <p className="text-text-soft text-xs">— {quote.author}</p>
    </Link>
  );
}
