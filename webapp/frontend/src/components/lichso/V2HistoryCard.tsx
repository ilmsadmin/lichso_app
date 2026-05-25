"use client";

import Link from "next/link";
import { useEventsByDate } from "@/hooks/usePublicContent";
import { ROUTES } from "@/lib/constants";
import { ArrowRight, Landmark } from "lucide-react";

interface V2HistoryCardProps {
  month: number;
  day: number;
  lunarMonth?: number;
  lunarDay?: number;
}

export function V2HistoryCard({ month, day, lunarMonth, lunarDay }: V2HistoryCardProps) {
  const { data, isLoading } = useEventsByDate(month, day, lunarMonth, lunarDay);
  const events = data?.data ?? [];

  if (isLoading) {
    return (
      <div
        className="mb-5 animate-pulse rounded-xl p-6"
        style={{ background: "var(--v2-bg-card)", border: "1px solid var(--v2-border-primary)" }}
      >
        <div className="h-4 w-48 rounded" style={{ background: "var(--v2-bg-hover)" }} />
        <div className="mt-4 h-12 w-full rounded" style={{ background: "var(--v2-bg-hover)" }} />
        <div className="mt-3 h-12 w-full rounded" style={{ background: "var(--v2-bg-hover)" }} />
      </div>
    );
  }

  if (events.length === 0) return null;

  return (
    <div
      className="v2-card mb-5 rounded-xl p-6"
      style={{
        background: "var(--v2-bg-card)",
        border: "1px solid var(--v2-border-primary)",
        boxShadow: "var(--v2-shadow-xs)",
      }}
    >
      {/* Header */}
      <div className="mb-4 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div
            className="flex h-8 w-8 items-center justify-center rounded-lg"
            style={{ background: "var(--v2-bg-accent-soft)", color: "var(--v2-text-accent)" }}
          >
            <Landmark className="h-4 w-4" />
          </div>
          <h3 className="text-[16px] font-bold" style={{ color: "var(--v2-text-primary)" }}>
            Ngày này trong lịch sử
          </h3>
        </div>
        <Link
          href={ROUTES.TODAY_IN_HISTORY}
          className="flex items-center gap-1 text-[12px] font-medium transition-all hover:gap-2"
          style={{ color: "var(--v2-text-accent)" }}
        >
          Xem tất cả <ArrowRight className="h-3 w-3" />
        </Link>
      </div>

      {/* Events */}
      <div>
        {events.slice(0, 3).map((event, idx) => (
          <Link
            key={event.id}
            href={`${ROUTES.EVENTS}/${event.slug}`}
            className="flex gap-4 py-3.5 transition-colors"
            style={{
              borderBottom: idx < Math.min(events.length, 3) - 1 ? "1px solid var(--v2-border-light)" : "none",
            }}
          >
            {/* Year */}
            {event.event_year && (
              <span
                className="font-playfair min-w-[50px] pt-0.5 text-[15px] font-bold"
                style={{ color: "var(--v2-text-accent)" }}
              >
                {event.event_year}
              </span>
            )}
            <div className="flex-1">
              <h4
                className="mb-1 text-[14px] font-semibold leading-snug"
                style={{ color: "var(--v2-text-primary)" }}
              >
                {event.title}
              </h4>
              {event.short_description && (
                <p
                  className="line-clamp-2 text-[13px] leading-relaxed"
                  style={{ color: "var(--v2-text-muted)" }}
                >
                  {event.short_description}
                </p>
              )}
              {event.event_type && (
                <span
                  className="mt-1.5 inline-block rounded-full px-2.5 py-0.5 text-[11px] font-medium"
                  style={{
                    background: "var(--v2-bg-tag)",
                    color: "var(--v2-text-accent)",
                  }}
                >
                  {event.event_type}
                </span>
              )}
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
