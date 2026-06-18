"use client";

import { useState, useMemo } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { useCalendarMonth } from "@/hooks/useCalendar";
import type { MonthDayBrief } from "@/types/calendar";

const DOW = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];
const MONTH_NAMES = [
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

function pad(n: number) {
  return String(n).padStart(2, "0");
}

interface MonthCalendarProps {
  /** YYYY-MM-DD đang được chọn (đồng bộ với tờ lịch ngày) */
  selectedDate: string;
  onSelectDate: (date: string) => void;
}

/**
 * Lịch tháng đầy đủ — âm/dương lịch, đánh dấu ngày tốt, ngày lễ, sự kiện.
 * Click 1 ngày để cập nhật tờ lịch ngày ở trên.
 */
export function MonthCalendar({ selectedDate, onSelectDate }: MonthCalendarProps) {
  const [selY, selM, selD] = selectedDate.split("-").map(Number);

  // Tháng đang xem (theo dõi ngày được chọn). Khi tháng của ngày được chọn đổi,
  // đồng bộ view ngay trong lúc render — tránh setState trong effect (cascading render).
  const [view, setView] = useState({ year: selY, month: selM }); // month 1-12
  const [anchor, setAnchor] = useState(`${selY}-${selM}`);
  if (anchor !== `${selY}-${selM}`) {
    setAnchor(`${selY}-${selM}`);
    setView({ year: selY, month: selM });
  }

  const { data, isLoading } = useCalendarMonth(view.year, view.month);

  // Bù ô trống đầu tháng theo thứ của ngày 1
  const leadingBlanks = useMemo(() => {
    if (!data?.days?.length) return 0;
    return data.days[0].day_of_week; // 0 = CN
  }, [data]);

  const prev = () =>
    setView((v) =>
      v.month === 1 ? { year: v.year - 1, month: 12 } : { ...v, month: v.month - 1 }
    );
  const next = () =>
    setView((v) =>
      v.month === 12 ? { year: v.year + 1, month: 1 } : { ...v, month: v.month + 1 }
    );

  return (
    <div
      className="rounded-2xl p-4 sm:p-5"
      style={{
        background: "var(--v2-bg-card)",
        border: "1px solid var(--v2-border-primary)",
        boxShadow: "var(--v2-shadow-sm)",
      }}
    >
      {/* Header */}
      <div className="mb-3 flex items-center justify-between">
        <div>
          <h3
            className="font-playfair text-lg font-bold"
            style={{ color: "var(--v2-text-primary)" }}
          >
            {MONTH_NAMES[view.month - 1]}, {view.year}
          </h3>
          {data?.lunar_info && (
            <p className="text-[12px]" style={{ color: "var(--v2-text-muted)" }}>
              {data.lunar_info}
            </p>
          )}
        </div>
        <div className="flex gap-1.5">
          <NavBtn onClick={prev} label="Tháng trước">
            <ChevronLeft className="h-4 w-4" />
          </NavBtn>
          <NavBtn onClick={next} label="Tháng sau">
            <ChevronRight className="h-4 w-4" />
          </NavBtn>
        </div>
      </div>

      {/* Weekday header */}
      <div className="grid grid-cols-7 gap-1">
        {DOW.map((d, i) => (
          <div
            key={d}
            className="py-1.5 text-center text-[11px] font-semibold uppercase"
            style={{ color: i === 0 ? "var(--v2-text-accent)" : "var(--v2-text-muted)" }}
          >
            {d}
          </div>
        ))}
      </div>

      {/* Grid */}
      <div className="grid grid-cols-7 gap-1">
        {isLoading && !data ? (
          Array.from({ length: 35 }).map((_, i) => (
            <div
              key={i}
              className="aspect-square animate-pulse rounded-lg"
              style={{ background: "var(--v2-bg-hover)" }}
            />
          ))
        ) : (
          <>
            {Array.from({ length: leadingBlanks }).map((_, i) => (
              <div key={`b${i}`} />
            ))}
            {data?.days.map((day) => (
              <DayCell
                key={day.solar_day}
                day={day}
                year={view.year}
                month={view.month}
                isSelected={view.year === selY && view.month === selM && day.solar_day === selD}
                onClick={() =>
                  onSelectDate(`${view.year}-${pad(view.month)}-${pad(day.solar_day)}`)
                }
              />
            ))}
          </>
        )}
      </div>

      {/* Chú thích */}
      <div
        className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-1.5 border-t pt-3 text-[11px]"
        style={{ borderColor: "var(--v2-border-light)", color: "var(--v2-text-muted)" }}
      >
        <Legend color="var(--v2-bg-accent)" label="Hôm nay" filled />
        <Legend color="#1F9D55" label="Ngày tốt" />
        <Legend color="var(--v2-bg-gold)" label="Ngày lễ / sự kiện" />
      </div>
    </div>
  );
}

function DayCell({
  day,
  isSelected,
  onClick,
}: {
  day: MonthDayBrief;
  year: number;
  month: number;
  isSelected: boolean;
  onClick: () => void;
}) {
  const isSunday = day.day_of_week === 0;
  const hasEvent = day.is_holiday || (day.events && day.events.length > 0);

  return (
    <button
      onClick={onClick}
      className="group relative flex aspect-square flex-col items-center justify-center rounded-lg transition-all"
      style={{
        background: day.is_today
          ? "var(--v2-bg-accent)"
          : isSelected
            ? "var(--v2-bg-accent-soft)"
            : "transparent",
        boxShadow: isSelected && !day.is_today ? "inset 0 0 0 1.5px var(--v2-bg-accent)" : "none",
      }}
      onMouseEnter={(e) => {
        if (!day.is_today && !isSelected)
          (e.currentTarget as HTMLElement).style.background = "var(--v2-bg-hover)";
      }}
      onMouseLeave={(e) => {
        if (!day.is_today && !isSelected)
          (e.currentTarget as HTMLElement).style.background = "transparent";
      }}
    >
      {/* badge ngày lễ */}
      {hasEvent && (
        <span
          className="absolute top-1 right-1 h-1.5 w-1.5 rounded-full"
          style={{ background: "var(--v2-bg-gold)" }}
        />
      )}
      {/* số dương */}
      <span
        className="text-[15px] leading-none font-semibold tabular-nums"
        style={{
          color: day.is_today
            ? "#fff"
            : isSunday || hasEvent
              ? "var(--v2-text-accent)"
              : "var(--v2-text-primary)",
        }}
      >
        {day.solar_day}
      </span>
      {/* số âm */}
      <span
        className="mt-0.5 text-[10px] leading-none tabular-nums"
        style={{ color: day.is_today ? "rgba(255,255,255,0.75)" : "var(--v2-text-muted)" }}
      >
        {day.lunar_day === 1 ? day.lunar_day_name : day.lunar_day}
      </span>
      {/* chấm ngày tốt */}
      {day.is_good_day && !day.is_today && (
        <span
          className="absolute bottom-1 h-1 w-1 rounded-full"
          style={{ background: "#1F9D55" }}
        />
      )}
    </button>
  );
}

function NavBtn({
  children,
  onClick,
  label,
}: {
  children: React.ReactNode;
  onClick: () => void;
  label: string;
}) {
  return (
    <button
      onClick={onClick}
      aria-label={label}
      className="flex h-8 w-8 items-center justify-center rounded-lg border transition-all hover:text-white"
      style={{ borderColor: "var(--v2-border-primary)", color: "var(--v2-text-secondary)" }}
      onMouseEnter={(e) => {
        (e.currentTarget as HTMLElement).style.background = "var(--v2-bg-accent)";
        (e.currentTarget as HTMLElement).style.color = "#fff";
      }}
      onMouseLeave={(e) => {
        (e.currentTarget as HTMLElement).style.background = "transparent";
        (e.currentTarget as HTMLElement).style.color = "var(--v2-text-secondary)";
      }}
    >
      {children}
    </button>
  );
}

function Legend({ color, label, filled }: { color: string; label: string; filled?: boolean }) {
  return (
    <span className="inline-flex items-center gap-1.5">
      <span
        className="h-2 w-2 rounded-full"
        style={{
          background: filled ? color : "transparent",
          border: filled ? "none" : `1.5px solid ${color}`,
        }}
      />
      {label}
    </span>
  );
}
