"use client";

import { useState, useMemo } from "react";
import { useCalendarMonth } from "@/hooks/useCalendar";
import { useMonthContentSummary } from "@/hooks/useV3";
import { Skeleton } from "@/components/ui/skeleton";
import type { CalendarEvent } from "@/types/calendar";
import type { DayContentSummary } from "@/types/v3";

const WEEK_DAYS = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];
const MONTHS_VI = [
  "Tháng 1",
  "Tháng 2",
  "Tháng 3",
  "Tháng 4",
  "Tháng 5",
  "Tháng 6",
  "Tháng 7",
  "Tháng 8",
  "Tháng 9",
  "Tháng 10",
  "Tháng 11",
  "Tháng 12",
];

interface CalendarGridProps {
  onDateSelect?: (day: number, month: number, year: number) => void;
}

export function CalendarGrid({ onDateSelect }: CalendarGridProps) {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);

  const { data, isLoading } = useCalendarMonth(year, month);
  const { data: contentSummary } = useMonthContentSummary(year, month);

  // Build content map: day -> DayContentSummary
  const contentMap = useMemo(() => {
    const map = new Map<number, DayContentSummary>();
    if (contentSummary?.data?.days) {
      for (const d of contentSummary.data.days) {
        map.set(d.day, d);
      }
    }
    return map;
  }, [contentSummary]);

  const prev = () => {
    if (month === 1) {
      setMonth(12);
      setYear((y) => y - 1);
    } else {
      setMonth((m) => m - 1);
    }
  };

  const next = () => {
    if (month === 12) {
      setMonth(1);
      setYear((y) => y + 1);
    } else {
      setMonth((m) => m + 1);
    }
  };

  // Build calendar grid cells (42 = 6 rows * 7 columns)
  const cells = useMemo(() => {
    if (!data) return [];

    const firstDay = new Date(year, month - 1, 1).getDay(); // 0=Sun
    const daysInMonth = new Date(year, month, 0).getDate();
    const prevMonthDays = new Date(year, month - 1, 0).getDate();

    const grid: Array<{
      day: number;
      isOther: boolean;
      isToday: boolean;
      isGoodDay: boolean;
      isHoliday: boolean;
      lunarDayName: string;
      dow: number;
      events: CalendarEvent[];
    }> = [];

    // Previous month days
    for (let i = 0; i < firstDay; i++) {
      const d = prevMonthDays - firstDay + i + 1;
      grid.push({
        day: d,
        isOther: true,
        isToday: false,
        isGoodDay: false,
        isHoliday: false,
        lunarDayName: "",
        dow: i,
        events: [],
      });
    }

    // Current month days
    for (let d = 1; d <= daysInMonth; d++) {
      const dayData = data.days?.find((dd) => dd.solar_day === d);
      const dow = (firstDay + d - 1) % 7;
      grid.push({
        day: d,
        isOther: false,
        isToday: dayData?.is_today ?? false,
        isGoodDay: dayData?.is_good_day ?? false,
        isHoliday: dayData?.is_holiday ?? false,
        lunarDayName: dayData?.lunar_day_name ?? "",
        dow,
        events: dayData?.events ?? [],
      });
    }

    // Next month days to fill to 42
    const remaining = 42 - grid.length;
    for (let i = 1; i <= remaining; i++) {
      grid.push({
        day: i,
        isOther: true,
        isToday: false,
        isGoodDay: false,
        isHoliday: false,
        lunarDayName: "",
        dow: (firstDay + daysInMonth + i - 1) % 7,
        events: [],
      });
    }

    return grid;
  }, [data, year, month]);

  return (
    <div className="mb-6">
      {/* Header */}
      <div className="mb-4 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <span
            className="h-4 w-1 rounded-sm"
            style={{
              background: "linear-gradient(to bottom, var(--warm-amber), var(--warm-gold))",
            }}
          />
          <span className="text-text-dark text-base font-[var(--font-lora)] tracking-wide">
            Lịch Tháng
          </span>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={prev}
            className="text-warm-amber hover:bg-warm-amber/10 hover:border-warm-amber/40 flex h-8 w-8 items-center justify-center rounded-lg backdrop-blur-lg transition-all"
            style={{
              background: "var(--ls-card-bg)",
              border: "1px solid var(--ls-border-warm)",
            }}
          >
            ‹
          </button>
          <span className="text-text-mid min-w-[180px] text-center text-[15px] font-[var(--font-lora)]">
            {MONTHS_VI[month - 1]} · {year}
          </span>
          <button
            onClick={next}
            className="text-warm-amber hover:bg-warm-amber/10 hover:border-warm-amber/40 flex h-8 w-8 items-center justify-center rounded-lg backdrop-blur-lg transition-all"
            style={{
              background: "var(--ls-card-bg)",
              border: "1px solid var(--ls-border-warm)",
            }}
          >
            ›
          </button>
        </div>
      </div>

      {/* Calendar wrap */}
      <div
        className="overflow-hidden rounded-2xl backdrop-blur-[14px]"
        style={{
          background: "var(--ls-card-bg)",
          border: "1px solid var(--ls-border-soft)",
          boxShadow: "0 4px 24px var(--ls-shadow-warm)",
        }}
      >
        {/* Week header */}
        <div
          className="grid grid-cols-7"
          style={{
            background: "rgba(196,120,58,0.06)",
            borderBottom: "1px solid var(--ls-border-soft)",
          }}
        >
          {WEEK_DAYS.map((d, i) => (
            <span
              key={d}
              className={`py-2.5 text-center text-[11px] font-semibold tracking-[1.5px] uppercase ${
                i === 0 ? "text-danger" : i === 6 ? "text-[#5080A0]" : "text-text-muted-ls"
              }`}
            >
              {d}
            </span>
          ))}
        </div>

        {/* Days grid */}
        {isLoading ? (
          <div className="grid grid-cols-7 gap-0">
            {Array.from({ length: 42 }).map((_, i) => (
              <div
                key={i}
                className="min-h-[68px] p-2.5"
                style={{
                  borderRightWidth: 1,
                  borderRightStyle: "solid",
                  borderRightColor: "var(--ls-border-soft)",
                  borderBottomWidth: 1,
                  borderBottomStyle: "solid",
                  borderBottomColor: "var(--ls-border-soft)",
                }}
              >
                <Skeleton className="mb-1 h-5 w-5" />
                <Skeleton className="h-3 w-6" />
              </div>
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-7">
            {cells.map((cell, i) => (
              <div
                key={i}
                className={`hover:bg-warm-amber/5 relative min-h-[68px] cursor-pointer px-2.5 py-2 transition-all duration-200 ${
                  cell.isOther ? "opacity-30" : ""
                } ${cell.isToday ? "bg-warm-amber/[0.08]" : ""}`}
                style={{
                  borderRightWidth: 1,
                  borderRightStyle: "solid",
                  borderRightColor: cell.isToday
                    ? "rgba(196,120,58,0.25)"
                    : "var(--ls-border-soft)",
                  borderBottomWidth: 1,
                  borderBottomStyle: "solid",
                  borderBottomColor: cell.isToday
                    ? "rgba(196,120,58,0.25)"
                    : "var(--ls-border-soft)",
                }}
                onClick={() => {
                  if (!cell.isOther && onDateSelect) {
                    onDateSelect(cell.day, month, year);
                  }
                }}
              >
                {/* Today top accent line */}
                {cell.isToday && (
                  <div
                    className="absolute top-0 right-0 left-0 h-0.5"
                    style={{
                      background: "linear-gradient(90deg, var(--warm-amber), var(--warm-gold))",
                    }}
                  />
                )}

                <div
                  className={`text-lg leading-none font-[var(--font-lora)] font-semibold ${
                    cell.dow === 0
                      ? "text-danger"
                      : cell.dow === 6
                        ? "text-[#5080A0]"
                        : "text-text-dark"
                  }`}
                >
                  {cell.day}
                </div>

                {!cell.isOther && cell.lunarDayName && (
                  <div
                    className={`mt-0.5 text-[10px] font-[var(--font-noto)] ${
                      cell.isToday
                        ? "text-warm-amber"
                        : cell.isHoliday
                          ? "text-danger"
                          : "text-text-muted-ls"
                    }`}
                  >
                    {cell.lunarDayName}
                  </div>
                )}

                {/* Event emoji indicators */}
                {!cell.isOther && cell.events.length > 0 && (
                  <div
                    className="mt-0.5 flex items-center gap-0.5"
                    title={cell.events.map((e) => e.name).join(", ")}
                  >
                    {cell.events.slice(0, 2).map((ev, idx) => (
                      <span key={idx} className="text-[9px] leading-none">
                        {ev.emoji}
                      </span>
                    ))}
                    {cell.events.length > 2 && (
                      <span className="text-text-muted-ls text-[8px]">
                        +{cell.events.length - 2}
                      </span>
                    )}
                  </div>
                )}

                {/* Content indicator dots */}
                {!cell.isOther &&
                  (() => {
                    const cs = contentMap.get(cell.day);
                    if (!cs || cs.total === 0) return null;
                    return (
                      <div className="mt-1 flex items-center gap-[3px]">
                        {cs.quotes > 0 && (
                          <span
                            className="h-[5px] w-[5px] rounded-full"
                            style={{ background: "#C4783A" }}
                            title={`${cs.quotes} câu nói`}
                          />
                        )}
                        {cs.events > 0 && (
                          <span
                            className="h-[5px] w-[5px] rounded-full"
                            style={{ background: "#5080A0" }}
                            title={`${cs.events} sự kiện`}
                          />
                        )}
                        {cs.famous_people > 0 && (
                          <span
                            className="h-[5px] w-[5px] rounded-full"
                            style={{ background: "#8B5CF6" }}
                            title={`${cs.famous_people} nhân vật`}
                          />
                        )}
                        {cs.festivals > 0 && (
                          <span
                            className="h-[5px] w-[5px] rounded-full"
                            style={{ background: "#E67E22" }}
                            title={`${cs.festivals} lễ hội`}
                          />
                        )}
                        {cs.custom > 0 && (
                          <span
                            className="h-[5px] w-[5px] rounded-full"
                            style={{ background: "#4A9B73" }}
                            title={`${cs.custom} nội dung`}
                          />
                        )}
                      </div>
                    );
                  })()}

                {/* Holiday dot (red) */}
                {cell.isHoliday && !cell.isOther && (
                  <div
                    className="absolute bottom-1.5 left-1.5 h-[5px] w-[5px] rounded-full"
                    style={{
                      background: "var(--ls-danger)",
                      boxShadow: "0 0 5px rgba(192,96,96,0.4)",
                    }}
                  />
                )}

                {/* Good day dot */}
                {cell.isGoodDay && !cell.isOther && (
                  <div
                    className="absolute right-1.5 bottom-1.5 h-[5px] w-[5px] rounded-full"
                    style={{
                      background: "var(--jade-soft)",
                      boxShadow: "0 0 5px rgba(74,155,115,0.4)",
                    }}
                  />
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
