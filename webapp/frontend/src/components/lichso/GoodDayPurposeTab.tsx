"use client";

import { useState } from "react";
import { useGoodDayPurposes, useGoodDaysForPurpose } from "@/hooks/useV3";
import { Skeleton } from "@/components/ui/skeleton";
import { CalendarCheck, ChevronDown, ChevronUp } from "lucide-react";
import type { PurposeGoodDay } from "@/types/v3";

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

function GoodDayCard({
  day,
  month,
  year,
  expanded,
  onToggle,
}: {
  day: PurposeGoodDay;
  month: number;
  year: number;
  expanded: boolean;
  onToggle: () => void;
}) {
  const scoreColor =
    day.score >= 85 ? "text-jade-teal" : day.score >= 70 ? "text-jade-soft" : "text-warm-amber";

  return (
    <div
      className="overflow-hidden rounded-2xl transition-all duration-300"
      style={{
        background: "var(--ls-card-bg)",
        border: expanded ? "1px solid rgba(74,139,127,0.3)" : "1px solid var(--ls-border-soft)",
        boxShadow: "0 2px 12px var(--ls-shadow-warm)",
      }}
    >
      <button
        onClick={onToggle}
        className="hover:bg-warm-cream/20 flex w-full items-start gap-4 p-4 text-left transition-colors"
      >
        {/* Date badge */}
        <div
          className="flex h-14 w-14 flex-shrink-0 flex-col items-center justify-center rounded-xl"
          style={{
            background: "linear-gradient(135deg, rgba(74,139,127,0.1), rgba(74,139,127,0.05))",
            border: "1px solid rgba(74,139,127,0.2)",
          }}
        >
          <span className="text-jade-teal text-xl leading-none font-[var(--font-lora)] font-semibold">
            {String(day.solar_day).padStart(2, "0")}
          </span>
          <span className="text-text-muted-ls mt-0.5 text-[9px]">
            {day.day_of_week.replace("Thứ ", "T").replace("Chủ Nhật", "CN")}
          </span>
        </div>

        {/* Info */}
        <div className="min-w-0 flex-1">
          <div className="mb-1 flex items-center gap-2">
            <span className="text-text-dark text-sm font-medium">
              {day.solar_day}/{month}/{year}
            </span>
            <span className="text-text-muted-ls text-[10px]">·</span>
            <span className="text-text-soft text-[12px]">
              Âm: {day.lunar_day}/{day.lunar_month}
            </span>
          </div>
          <div className="mb-1.5 flex items-center gap-2">
            <span className="bg-warm-amber/10 border-warm-amber/25 text-warm-amber rounded-full border px-2 py-0.5 text-[11px] font-medium">
              {day.day_can_chi}
            </span>
            <span className="bg-jade-teal/10 border-jade-teal/25 text-jade-teal rounded-full border px-2 py-0.5 text-[11px]">
              {day.truc_ngay}
            </span>
          </div>
          {/* Score bar */}
          <div className="flex items-center gap-2">
            <div
              className="h-1 max-w-[180px] flex-1 overflow-hidden rounded-sm"
              style={{ background: "rgba(74,139,127,0.1)" }}
            >
              <div
                className="h-full rounded-sm transition-all duration-500"
                style={{
                  width: `${day.score}%`,
                  background: "linear-gradient(90deg, var(--jade-soft), var(--warm-gold))",
                }}
              />
            </div>
            <span className={`text-[12px] font-semibold ${scoreColor}`}>{day.score}%</span>
          </div>
        </div>

        {expanded ? (
          <ChevronUp className="text-text-muted-ls mt-1 h-4 w-4 flex-shrink-0" />
        ) : (
          <ChevronDown className="text-text-muted-ls mt-1 h-4 w-4 flex-shrink-0" />
        )}
      </button>

      {/* Expanded */}
      {expanded && (
        <div className="animate-[fadeUp_0.3s_ease-out_both] px-4 pb-4">
          <div className="mb-3 h-px" style={{ background: "var(--ls-border-soft)" }} />

          {/* Reasons */}
          {day.reasons.length > 0 && (
            <div className="mb-3">
              <span className="text-text-muted-ls mb-1 block text-[11px]">✅ Lý do ngày tốt:</span>
              <ul className="space-y-1">
                {day.reasons.map((r, i) => (
                  <li key={i} className="text-text-mid flex items-start gap-1.5 text-[12px]">
                    <span className="text-jade-teal mt-0.5">•</span>
                    {r}
                  </li>
                ))}
              </ul>
            </div>
          )}

          {/* Recommended activities */}
          {day.viec_nen.length > 0 && (
            <div className="mb-3">
              <span className="text-text-muted-ls mb-1 block text-[11px]">📋 Việc nên làm:</span>
              <div className="flex flex-wrap gap-1.5">
                {day.viec_nen.map((v, i) => (
                  <span
                    key={i}
                    className="rounded-full px-2 py-0.5 text-[10px] font-medium"
                    style={{
                      background: "rgba(74,139,127,0.08)",
                      border: "1px solid rgba(74,139,127,0.15)",
                      color: "var(--jade-teal)",
                    }}
                  >
                    {v}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Good hours */}
          {day.gio_tot.length > 0 && (
            <div>
              <span className="text-text-muted-ls mb-1 block text-[11px]">⏰ Giờ tốt:</span>
              <div className="flex flex-wrap gap-1.5">
                {day.gio_tot.map((g, i) => (
                  <span
                    key={i}
                    className="bg-warm-amber/10 text-warm-amber border-warm-amber/20 rounded border px-2 py-0.5 text-[10px] font-medium"
                  >
                    {g}
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

// ============================================
// Main
// ============================================

export function GoodDayPurposeTab() {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [purpose, setPurpose] = useState("cuoi_hoi");
  const [birthYear, setBirthYear] = useState(0);
  const [spouseYear, setSpouseYear] = useState(0);
  const [expandedDay, setExpandedDay] = useState<number | null>(null);

  const { data: purposesData } = useGoodDayPurposes();
  const { data: goodDaysData, isLoading } = useGoodDaysForPurpose(
    year,
    month,
    purpose,
    birthYear || undefined,
    spouseYear || undefined
  );

  const purposes = purposesData?.data ?? [];
  const result = goodDaysData?.data;

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
      <div className="mb-4 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <CalendarCheck className="text-jade-teal h-4 w-4" />
          <span className="text-text-dark text-base font-[var(--font-lora)] tracking-wide">
            Ngày Tốt Theo Mục Đích
          </span>
        </div>
      </div>

      {/* Purpose selector */}
      <div
        className="mb-4 flex flex-wrap gap-2 rounded-xl p-3"
        style={{
          background: "rgba(74,139,127,0.03)",
          border: "1px solid var(--ls-border-soft)",
        }}
      >
        {purposes.map((p) => (
          <button
            key={p.key}
            onClick={() => setPurpose(p.key)}
            className={`rounded-lg px-3 py-1.5 text-[12px] font-medium transition-all ${
              purpose === p.key
                ? "bg-jade-teal/15 text-jade-teal border-jade-teal/30 border"
                : "text-text-muted-ls hover:bg-warm-cream/50 border border-transparent"
            }`}
          >
            {p.emoji} {p.name}
          </button>
        ))}
      </div>

      {/* Birth year inputs (especially for cuoi_hoi) */}
      <div
        className="mb-4 rounded-xl p-3"
        style={{
          background: "rgba(196,120,58,0.03)",
          border: "1px solid var(--ls-border-soft)",
        }}
      >
        <span className="text-text-muted-ls mb-2 block text-[11px]">
          Nhập năm sinh để kiểm tra tương hợp (không bắt buộc)
        </span>
        <div className="flex items-center gap-3">
          <input
            type="number"
            min={1940}
            max={2010}
            placeholder="Năm sinh"
            value={birthYear || ""}
            onChange={(e) => setBirthYear(Number(e.target.value))}
            className="text-text-dark placeholder:text-text-muted-ls/50 flex-1 rounded-lg px-3 py-2 text-sm outline-none"
            style={{
              background: "var(--ls-card-bg)",
              border: "1px solid var(--ls-border-soft)",
            }}
          />
          {purpose === "cuoi_hoi" && (
            <input
              type="number"
              min={1940}
              max={2010}
              placeholder="Năm sinh đối phương"
              value={spouseYear || ""}
              onChange={(e) => setSpouseYear(Number(e.target.value))}
              className="text-text-dark placeholder:text-text-muted-ls/50 flex-1 rounded-lg px-3 py-2 text-sm outline-none"
              style={{
                background: "var(--ls-card-bg)",
                border: "1px solid var(--ls-border-soft)",
              }}
            />
          )}
        </div>
      </div>

      {/* Month navigation */}
      <div className="mb-4 flex items-center justify-between">
        <button
          onClick={prev}
          className="text-warm-amber hover:bg-warm-amber/10 flex h-8 w-8 items-center justify-center rounded-lg transition-all"
          style={{
            background: "var(--ls-card-bg)",
            border: "1px solid var(--ls-border-warm)",
          }}
        >
          ‹
        </button>
        <span className="text-text-mid text-[15px] font-[var(--font-lora)]">
          {MONTHS_VI[month - 1]} · {year}
        </span>
        <button
          onClick={next}
          className="text-warm-amber hover:bg-warm-amber/10 flex h-8 w-8 items-center justify-center rounded-lg transition-all"
          style={{
            background: "var(--ls-card-bg)",
            border: "1px solid var(--ls-border-warm)",
          }}
        >
          ›
        </button>
      </div>

      {/* Summary */}
      {result && (
        <div
          className="mb-4 flex items-center justify-between rounded-xl px-5 py-3"
          style={{
            background: "linear-gradient(135deg, rgba(74,139,127,0.08), rgba(196,120,58,0.06))",
            border: "1px solid var(--ls-border-soft)",
          }}
        >
          <span className="text-text-mid text-[13px]">
            Tìm thấy <strong className="text-jade-teal">{result.total}</strong> ngày tốt cho{" "}
            <strong className="text-warm-amber">{result.purpose_name}</strong>
          </span>
        </div>
      )}

      {/* Loading */}
      {isLoading && (
        <div className="flex flex-col gap-3">
          {Array.from({ length: 4 }).map((_, i) => (
            <div
              key={i}
              className="rounded-2xl p-4"
              style={{ background: "var(--ls-card-bg)", border: "1px solid var(--ls-border-soft)" }}
            >
              <div className="flex gap-4">
                <Skeleton className="h-14 w-14 rounded-xl" />
                <div className="flex-1">
                  <Skeleton className="mb-2 h-4 w-40" />
                  <Skeleton className="mb-1.5 h-3 w-56" />
                  <Skeleton className="h-3 w-32" />
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Results */}
      {!isLoading && result && (
        <div className="flex flex-col gap-2.5">
          {result.good_days.length === 0 ? (
            <div
              className="rounded-2xl p-10 text-center"
              style={{ background: "var(--ls-card-bg)", border: "1px solid var(--ls-border-soft)" }}
            >
              <p className="text-text-muted-ls text-sm">
                Không có ngày tốt cho {result.purpose_name} trong tháng này
              </p>
            </div>
          ) : (
            result.good_days.map((day, idx) => (
              <GoodDayCard
                key={day.solar_day}
                day={day}
                month={month}
                year={year}
                expanded={expandedDay === idx}
                onToggle={() => setExpandedDay((prev) => (prev === idx ? null : idx))}
              />
            ))
          )}
        </div>
      )}
    </div>
  );
}
