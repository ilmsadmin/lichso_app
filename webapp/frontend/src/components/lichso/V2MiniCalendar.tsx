"use client";

import { useState, useMemo } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";

/**
 * Mini calendar sidebar widget — V2 design
 * Shows a month grid with solar + lunar day numbers
 */
export function V2MiniCalendar({ onDateSelect }: { onDateSelect?: (date: string) => void }) {
  const today = new Date();
  const [year, setYear] = useState(today.getFullYear());
  const [month, setMonth] = useState(today.getMonth()); // 0-indexed

  const daysOfWeek = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];

  const calendarDays = useMemo(() => {
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const startDow = firstDay.getDay(); // 0=Sunday
    const totalDays = lastDay.getDate();

    const days: Array<{ day: number; isOther: boolean; isToday: boolean; isSunday: boolean }> = [];

    // Previous month fill
    const prevLastDay = new Date(year, month, 0).getDate();
    for (let i = startDow - 1; i >= 0; i--) {
      days.push({
        day: prevLastDay - i,
        isOther: true,
        isToday: false,
        isSunday: days.length % 7 === 0,
      });
    }

    // Current month
    for (let d = 1; d <= totalDays; d++) {
      const date = new Date(year, month, d);
      const dow = date.getDay();
      days.push({
        day: d,
        isOther: false,
        isToday:
          d === today.getDate() &&
          month === today.getMonth() &&
          year === today.getFullYear(),
        isSunday: dow === 0,
      });
    }

    // Next month fill
    const remaining = 7 - (days.length % 7);
    if (remaining < 7) {
      for (let i = 1; i <= remaining; i++) {
        days.push({ day: i, isOther: true, isToday: false, isSunday: false });
      }
    }

    return days;
  }, [year, month, today]);

  const monthNames = [
    "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4",
    "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
    "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12",
  ];

  const prevMonth = () => {
    if (month === 0) { setMonth(11); setYear(y => y - 1); }
    else setMonth(m => m - 1);
  };
  const nextMonth = () => {
    if (month === 11) { setMonth(0); setYear(y => y + 1); }
    else setMonth(m => m + 1);
  };

  return (
    <div
      className="v2-card rounded-xl p-5"
      style={{
        background: "var(--v2-bg-card)",
        border: "1px solid var(--v2-border-primary)",
        boxShadow: "var(--v2-shadow-xs)",
      }}
    >
      {/* Header */}
      <div className="mb-3.5 flex items-center justify-between">
        <h3
          className="font-playfair text-[15px] font-bold"
          style={{ color: "var(--v2-text-primary)" }}
        >
          {monthNames[month]}, {year}
        </h3>
        <div className="flex gap-1">
          <button
            onClick={prevMonth}
            className="flex h-7 w-7 items-center justify-center rounded-lg border transition-all"
            style={{ borderColor: "var(--v2-border-primary)", color: "var(--v2-text-secondary)" }}
            onMouseEnter={(e) => {
              (e.currentTarget as HTMLElement).style.background = "var(--v2-bg-accent)";
              (e.currentTarget as HTMLElement).style.color = "white";
            }}
            onMouseLeave={(e) => {
              (e.currentTarget as HTMLElement).style.background = "transparent";
              (e.currentTarget as HTMLElement).style.color = "var(--v2-text-secondary)";
            }}
          >
            <ChevronLeft className="h-3.5 w-3.5" />
          </button>
          <button
            onClick={nextMonth}
            className="flex h-7 w-7 items-center justify-center rounded-lg border transition-all"
            style={{ borderColor: "var(--v2-border-primary)", color: "var(--v2-text-secondary)" }}
            onMouseEnter={(e) => {
              (e.currentTarget as HTMLElement).style.background = "var(--v2-bg-accent)";
              (e.currentTarget as HTMLElement).style.color = "white";
            }}
            onMouseLeave={(e) => {
              (e.currentTarget as HTMLElement).style.background = "transparent";
              (e.currentTarget as HTMLElement).style.color = "var(--v2-text-secondary)";
            }}
          >
            <ChevronRight className="h-3.5 w-3.5" />
          </button>
        </div>
      </div>

      {/* Grid */}
      <div className="grid grid-cols-7 gap-0.5 text-center">
        {/* Day of week headers */}
        {daysOfWeek.map((dow, i) => (
          <div
            key={dow}
            className="py-1.5 text-[10px] font-semibold uppercase"
            style={{
              color: i === 0 ? "var(--v2-text-accent)" : "var(--v2-text-muted)",
              letterSpacing: "0.5px",
            }}
          >
            {dow}
          </div>
        ))}

        {/* Day cells */}
        {calendarDays.map((d, i) => (
          <button
            key={i}
            className="flex aspect-square flex-col items-center justify-center rounded-lg text-[13px] font-medium transition-all"
            style={{
              opacity: d.isOther ? 0.25 : 1,
              color: d.isToday
                ? "white"
                : d.isSunday
                  ? "var(--v2-text-accent)"
                  : "var(--v2-text-primary)",
              background: d.isToday ? "var(--v2-bg-accent)" : "transparent",
              boxShadow: d.isToday ? "0 2px 10px rgba(155, 35, 53, 0.3)" : "none",
              fontWeight: d.isToday ? 700 : 500,
            }}
            onMouseEnter={(e) => {
              if (!d.isToday) (e.currentTarget as HTMLElement).style.background = "var(--v2-bg-hover)";
            }}
            onMouseLeave={(e) => {
              if (!d.isToday) (e.currentTarget as HTMLElement).style.background = "transparent";
            }}
            onClick={() => {
              if (!d.isOther && onDateSelect) {
                const dateStr = `${year}-${String(month + 1).padStart(2, "0")}-${String(d.day).padStart(2, "0")}`;
                onDateSelect(dateStr);
              }
            }}
          >
            {d.day}
          </button>
        ))}
      </div>
    </div>
  );
}
