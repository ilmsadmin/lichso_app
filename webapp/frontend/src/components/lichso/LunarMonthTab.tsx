"use client";

import { useState } from "react";
import { useCalendarMonth } from "@/hooks/useCalendar";
import { Skeleton } from "@/components/ui/skeleton";

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

const WEEK_DAYS = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];

export function LunarMonthTab() {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);

  const { data, isLoading } = useCalendarMonth(year, month);

  const prev = () => {
    if (month === 1) {
      setMonth(12);
      setYear((y) => y - 1);
    } else setMonth((m) => m - 1);
  };

  const next = () => {
    if (month === 12) {
      setMonth(1);
      setYear((y) => y + 1);
    } else setMonth((m) => m + 1);
  };

  return (
    <div className="animate-[fadeUp_0.65s_ease-out_both]">
      {/* Header */}
      <div className="mb-5 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <span
            className="h-4 w-1 rounded-sm"
            style={{
              background: "linear-gradient(to bottom, var(--warm-amber), var(--warm-gold))",
            }}
          />
          <span className="text-text-dark text-base font-[var(--font-lora)] tracking-wide">
            Tháng Âm Lịch
          </span>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={prev}
            className="text-warm-amber hover:bg-warm-amber/10 flex h-8 w-8 items-center justify-center rounded-lg backdrop-blur-lg transition-all"
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
            className="text-warm-amber hover:bg-warm-amber/10 flex h-8 w-8 items-center justify-center rounded-lg backdrop-blur-lg transition-all"
            style={{
              background: "var(--ls-card-bg)",
              border: "1px solid var(--ls-border-warm)",
            }}
          >
            ›
          </button>
        </div>
      </div>

      {/* Lunar info banner */}
      {data && (
        <div
          className="mb-5 rounded-xl px-5 py-3 text-center"
          style={{
            background: "linear-gradient(135deg, rgba(74,139,127,0.08), rgba(196,120,58,0.08))",
            border: "1px solid var(--ls-border-soft)",
          }}
        >
          <span className="text-jade-teal text-[15px] font-[var(--font-noto)] tracking-wider">
            {data.lunar_info}
          </span>
        </div>
      )}

      {/* Lunar calendar grid — emphasis on lunar day names */}
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
            background: "rgba(74,139,127,0.06)",
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

        {isLoading ? (
          <div className="grid grid-cols-7">
            {Array.from({ length: 35 }).map((_, i) => (
              <div
                key={i}
                className="min-h-[80px] p-3"
                style={{
                  borderRight: "1px solid var(--ls-border-soft)",
                  borderBottom: "1px solid var(--ls-border-soft)",
                }}
              >
                <Skeleton className="mb-1.5 h-4 w-5" />
                <Skeleton className="h-5 w-10" />
              </div>
            ))}
          </div>
        ) : data ? (
          <LunarGrid year={year} month={month} days={data.days} />
        ) : null}
      </div>
    </div>
  );
}

function LunarGrid({
  year,
  month,
  days,
}: {
  year: number;
  month: number;
  days: {
    solar_day: number;
    lunar_day: number;
    lunar_day_name: string;
    day_of_week: number;
    is_today: boolean;
    is_good_day: boolean;
    chi_so_ngay: number;
  }[];
}) {
  const firstDay = new Date(year, month - 1, 1).getDay();
  const daysInMonth = new Date(year, month, 0).getDate();
  const prevMonthDays = new Date(year, month - 1, 0).getDate();

  const cells: Array<{
    solarDay: number;
    lunarDayName: string;
    isOther: boolean;
    isToday: boolean;
    isGoodDay: boolean;
    dow: number;
    chiSo: number;
  }> = [];

  // Previous month
  for (let i = 0; i < firstDay; i++) {
    cells.push({
      solarDay: prevMonthDays - firstDay + i + 1,
      lunarDayName: "",
      isOther: true,
      isToday: false,
      isGoodDay: false,
      dow: i,
      chiSo: 0,
    });
  }

  // Current month
  for (let d = 1; d <= daysInMonth; d++) {
    const dayData = days.find((dd) => dd.solar_day === d);
    const dow = (firstDay + d - 1) % 7;
    cells.push({
      solarDay: d,
      lunarDayName: dayData?.lunar_day_name ?? "",
      isOther: false,
      isToday: dayData?.is_today ?? false,
      isGoodDay: dayData?.is_good_day ?? false,
      dow,
      chiSo: dayData?.chi_so_ngay ?? 0,
    });
  }

  // Fill to 42
  const remaining = 42 - cells.length;
  for (let i = 1; i <= remaining; i++) {
    cells.push({
      solarDay: i,
      lunarDayName: "",
      isOther: true,
      isToday: false,
      isGoodDay: false,
      dow: (firstDay + daysInMonth + i - 1) % 7,
      chiSo: 0,
    });
  }

  return (
    <div className="grid grid-cols-7">
      {cells.map((cell, i) => {
        const isLunarStart = cell.lunarDayName === "Mồng 1" || cell.lunarDayName === "Rằm";
        return (
          <div
            key={i}
            className={`hover:bg-warm-amber/5 relative min-h-[80px] px-2.5 py-2 transition-all duration-200 ${
              cell.isOther ? "opacity-25" : ""
            } ${cell.isToday ? "bg-warm-amber/[0.08]" : ""}`}
            style={{
              borderRight: "1px solid var(--ls-border-soft)",
              borderBottom: "1px solid var(--ls-border-soft)",
              ...(cell.isToday ? { borderColor: "rgba(196,120,58,0.25)" } : {}),
            }}
          >
            {cell.isToday && (
              <div
                className="absolute top-0 right-0 left-0 h-0.5"
                style={{
                  background: "linear-gradient(90deg, var(--warm-amber), var(--warm-gold))",
                }}
              />
            )}

            {/* Solar day — small */}
            <div className="text-text-muted-ls mb-0.5 text-[11px]">{cell.solarDay}</div>

            {/* Lunar day — big emphasis */}
            {!cell.isOther && cell.lunarDayName && (
              <div
                className={`text-[16px] font-[var(--font-noto)] font-medium tracking-wide ${
                  isLunarStart
                    ? "text-warm-amber"
                    : cell.isToday
                      ? "text-jade-teal"
                      : "text-text-dark"
                }`}
              >
                {cell.lunarDayName}
              </div>
            )}

            {/* Good day indicator */}
            {cell.isGoodDay && !cell.isOther && (
              <div className="mt-0.5 flex items-center gap-1">
                <div className="bg-jade-soft h-1.5 w-1.5 rounded-full" />
                <span className="text-jade-teal text-[9px]">Tốt</span>
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}
