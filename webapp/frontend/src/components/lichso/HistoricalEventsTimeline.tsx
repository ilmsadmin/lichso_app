"use client";

import { Landmark } from "lucide-react";

interface HistoricalEvent {
  id?: string;
  title: string;
  description?: string;
  event_year?: number;
  event_day?: number;
  event_month?: number;
  emoji?: string;
}

interface HistoricalEventsTimelineProps {
  events: HistoricalEvent[];
}

/**
 * A vertical timeline showing historical events for a specific date.
 * Used inside the enhanced DayDetailModal.
 */
export function HistoricalEventsTimeline({ events }: HistoricalEventsTimelineProps) {
  if (!events || events.length === 0) return null;

  // Sort by year descending (most recent first)
  const sorted = [...events].sort((a, b) => (b.event_year ?? 0) - (a.event_year ?? 0));

  return (
    <div>
      <div className="mb-3 flex items-center gap-1.5">
        <Landmark className="text-jade-teal h-3.5 w-3.5" />
        <span className="text-jade-teal text-[9px] font-semibold tracking-[2px] uppercase">
          Ngày này trong lịch sử
        </span>
        <span className="text-text-muted-ls ml-auto text-[10px]">{sorted.length} sự kiện</span>
      </div>

      <div className="border-jade-teal/20 relative ml-3 space-y-3 border-l-2 pl-4">
        {sorted.map((event, idx) => (
          <div key={event.id ?? idx} className="relative">
            {/* Timeline dot */}
            <div
              className="border-card absolute top-1.5 -left-[21px] h-2.5 w-2.5 rounded-full border-2"
              style={{ background: "var(--jade-soft)" }}
            />

            {/* Year badge */}
            {event.event_year && (
              <span className="text-jade-teal bg-jade-teal/10 mb-0.5 inline-block rounded px-1.5 py-0.5 text-[9px] font-bold">
                {event.event_year}
              </span>
            )}

            {/* Title */}
            <p className="text-text-dark text-[12px] leading-snug font-medium">
              {event.emoji && <span className="mr-1">{event.emoji}</span>}
              {event.title}
            </p>

            {/* Description */}
            {event.description && (
              <p className="text-text-soft mt-0.5 line-clamp-2 text-[11px]">{event.description}</p>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
