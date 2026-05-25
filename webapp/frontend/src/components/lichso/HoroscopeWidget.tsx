"use client";

import { useState, useMemo } from "react";
import { useAllHoroscopes, useHoroscopeByBirthYear } from "@/hooks/useV3";
import { Skeleton } from "@/components/ui/skeleton";
import { Star, Sparkles, ChevronDown, ChevronUp } from "lucide-react";
import type { DailyHoroscope, HoroscopeRating } from "@/types/v3";

// ============================================
// Zodiac Icons
// ============================================
const ZODIAC_EMOJIS = ["🐀", "🐂", "🐅", "🐇", "🐉", "🐍", "🐴", "🐐", "🐒", "🐓", "🐕", "🐖"];

const ZODIAC_NAMES = [
  "Tý",
  "Sửu",
  "Dần",
  "Mão",
  "Thìn",
  "Tỵ",
  "Ngọ",
  "Mùi",
  "Thân",
  "Dậu",
  "Tuất",
  "Hợi",
];

// ============================================
// Sub-components
// ============================================
function StarRating({ stars, maxStars = 5 }: { stars: number; maxStars?: number }) {
  return (
    <div className="flex items-center gap-0.5">
      {Array.from({ length: maxStars }).map((_, i) => (
        <Star
          key={i}
          className={`h-3 w-3 ${
            i < stars ? "fill-warm-amber text-warm-amber" : "text-text-muted-ls/30"
          }`}
        />
      ))}
    </div>
  );
}

function RatingRow({ rating }: { rating: HoroscopeRating }) {
  return (
    <div className="flex items-center justify-between py-1.5">
      <div className="flex items-center gap-2">
        <span className="text-sm">{rating.emoji}</span>
        <span className="text-text-mid text-[13px]">{rating.category}</span>
      </div>
      <div className="flex items-center gap-2">
        <StarRating stars={rating.stars} />
        <span className="text-text-muted-ls min-w-[20px] text-right text-[11px]">
          {rating.stars}/5
        </span>
      </div>
    </div>
  );
}

function HoroscopeCard({
  horoscope,
  expanded,
  onToggle,
}: {
  horoscope: DailyHoroscope;
  expanded: boolean;
  onToggle: () => void;
}) {
  const overallColor =
    horoscope.overall >= 4
      ? "text-jade-teal"
      : horoscope.overall >= 3
        ? "text-warm-amber"
        : "text-red-500/80";

  return (
    <div
      className="overflow-hidden rounded-2xl transition-all duration-300"
      style={{
        background: "var(--ls-card-bg)",
        border: expanded ? "1px solid rgba(74,139,127,0.3)" : "1px solid var(--ls-border-soft)",
        boxShadow: expanded
          ? "0 4px 20px rgba(74,139,127,0.08)"
          : "0 2px 12px var(--ls-shadow-warm)",
      }}
    >
      {/* Header — always visible */}
      <button
        onClick={onToggle}
        className="hover:bg-warm-cream/30 flex w-full items-center gap-3 p-4 text-left transition-colors"
      >
        <span className="flex-shrink-0 text-2xl">{horoscope.zodiac.emoji}</span>
        <div className="min-w-0 flex-1">
          <div className="mb-0.5 flex items-center gap-2">
            <span className="text-text-dark text-[15px] font-[var(--font-lora)] font-medium">
              {horoscope.zodiac.name} — {horoscope.zodiac.animal}
            </span>
            <span className={`text-[12px] font-semibold ${overallColor}`}>
              {horoscope.overall}/5
            </span>
          </div>
          <p className="text-text-muted-ls truncate text-[11px]">{horoscope.overall_text}</p>
        </div>
        <div className="flex flex-shrink-0 items-center gap-1">
          <StarRating stars={horoscope.overall} />
          {expanded ? (
            <ChevronUp className="text-text-muted-ls ml-1 h-4 w-4" />
          ) : (
            <ChevronDown className="text-text-muted-ls ml-1 h-4 w-4" />
          )}
        </div>
      </button>

      {/* Expanded details */}
      {expanded && (
        <div className="animate-[fadeUp_0.3s_ease-out_both] px-4 pb-4">
          <div className="mb-3 h-px" style={{ background: "var(--ls-border-soft)" }} />

          {/* Ratings */}
          <div className="mb-3">
            {horoscope.ratings.map((rating, i) => (
              <RatingRow key={i} rating={rating} />
            ))}
          </div>

          {/* Advice */}
          <div
            className="mb-3 rounded-xl p-3"
            style={{
              background: "linear-gradient(135deg, var(--warm-cream) 0%, var(--warm-peach) 100%)",
              border: "1px solid var(--ls-border-warm)",
            }}
          >
            <p className="text-text-mid text-[12px] leading-relaxed italic">
              💡 {horoscope.advice}
            </p>
          </div>

          {/* Lucky info grid */}
          <div className="grid grid-cols-2 gap-2">
            <InfoChip label="Màu may mắn" value={horoscope.lucky_color.join(", ")} emoji="🎨" />
            <InfoChip label="Số may mắn" value={horoscope.lucky_number.join(", ")} emoji="🔢" />
            <InfoChip label="Hướng tốt" value={horoscope.direction} emoji="🧭" />
            <InfoChip label="Hợp với" value={horoscope.compatibility} emoji="💞" />
          </div>

          {/* Lucky hours */}
          {horoscope.lucky_hour.length > 0 && (
            <div className="mt-2 flex flex-wrap gap-1">
              <span className="text-text-muted-ls mr-1 text-[11px]">⏰ Giờ tốt:</span>
              {horoscope.lucky_hour.map((h, i) => (
                <span
                  key={i}
                  className="rounded px-1.5 py-0.5 text-[10px] font-medium"
                  style={{
                    background: "rgba(74,139,127,0.1)",
                    color: "var(--jade-teal)",
                    border: "1px solid rgba(74,139,127,0.2)",
                  }}
                >
                  {h}
                </span>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function InfoChip({ label, value, emoji }: { label: string; value: string; emoji: string }) {
  return (
    <div
      className="rounded-lg px-2.5 py-1.5"
      style={{
        background: "rgba(74,139,127,0.04)",
        border: "1px solid var(--ls-border-soft)",
      }}
    >
      <span className="text-text-muted-ls mb-0.5 block text-[10px]">
        {emoji} {label}
      </span>
      <span className="text-text-dark text-[12px] font-medium">{value}</span>
    </div>
  );
}

// ============================================
// Main Component
// ============================================

interface HoroscopeWidgetProps {
  /** Pre-select zodiac by birth year */
  birthYear?: number;
}

export function HoroscopeWidget({ birthYear }: HoroscopeWidgetProps) {
  const now = new Date();
  const [expandedIndex, setExpandedIndex] = useState<number | null>(null);
  const [selectedBirthYear, setSelectedBirthYear] = useState(birthYear || 0);
  const [mode, setMode] = useState<"all" | "personal">(birthYear ? "personal" : "all");

  // Fetch all horoscopes
  const { data: allData, isLoading: allLoading } = useAllHoroscopes(
    now.getFullYear(),
    now.getMonth() + 1,
    now.getDate()
  );

  // Fetch personal horoscope by birth year
  const { data: personalData, isLoading: personalLoading } =
    useHoroscopeByBirthYear(selectedBirthYear);

  const allHoroscopes = allData?.data?.horoscopes ?? [];
  const personalHoroscope = personalData?.data;

  // Pre-expand user's zodiac
  const userZodiacIndex = useMemo(() => {
    if (selectedBirthYear > 0) {
      return (selectedBirthYear - 4) % 12;
    }
    return null;
  }, [selectedBirthYear]);

  const isLoading = mode === "all" ? allLoading : personalLoading;

  return (
    <div className="animate-[fadeUp_0.65s_ease-out_both]">
      {/* Header */}
      <div className="mb-4 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <Sparkles className="text-warm-amber h-4 w-4" />
          <span className="text-text-dark text-base font-[var(--font-lora)] tracking-wide">
            Tử Vi Hôm Nay
          </span>
        </div>

        {/* Mode tabs */}
        <div
          className="flex overflow-hidden rounded-lg"
          style={{ border: "1px solid var(--ls-border-soft)" }}
        >
          <button
            onClick={() => setMode("all")}
            className={`px-3 py-1.5 text-[11px] font-medium transition-colors ${
              mode === "all"
                ? "bg-jade-teal/10 text-jade-teal"
                : "text-text-muted-ls hover:bg-warm-cream/50"
            }`}
          >
            12 con giáp
          </button>
          <button
            onClick={() => setMode("personal")}
            className={`px-3 py-1.5 text-[11px] font-medium transition-colors ${
              mode === "personal"
                ? "bg-jade-teal/10 text-jade-teal"
                : "text-text-muted-ls hover:bg-warm-cream/50"
            }`}
          >
            Xem theo tuổi
          </button>
        </div>
      </div>

      {/* Birth year input for personal mode */}
      {mode === "personal" && (
        <div
          className="mb-4 rounded-xl p-4"
          style={{
            background: "linear-gradient(135deg, rgba(74,139,127,0.06), rgba(196,120,58,0.04))",
            border: "1px solid var(--ls-border-soft)",
          }}
        >
          <label className="text-text-muted-ls mb-2 block text-[12px]">Nhập năm sinh của bạn</label>
          <div className="flex items-center gap-3">
            <input
              type="number"
              min={1940}
              max={2010}
              placeholder="VD: 1990"
              value={selectedBirthYear || ""}
              onChange={(e) => setSelectedBirthYear(Number(e.target.value))}
              className="text-text-dark placeholder:text-text-muted-ls/50 flex-1 rounded-lg px-3 py-2 text-sm outline-none"
              style={{
                background: "var(--ls-card-bg)",
                border: "1px solid var(--ls-border-soft)",
              }}
            />
            {selectedBirthYear >= 1940 && selectedBirthYear <= 2010 && (
              <div className="bg-warm-amber/10 border-warm-amber/25 flex items-center gap-1.5 rounded-lg border px-3 py-2">
                <span className="text-lg">{ZODIAC_EMOJIS[(selectedBirthYear - 4) % 12]}</span>
                <span className="text-warm-amber text-[12px] font-medium">
                  {ZODIAC_NAMES[(selectedBirthYear - 4) % 12]}
                </span>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Loading */}
      {isLoading && (
        <div className="flex flex-col gap-3">
          {Array.from({ length: mode === "all" ? 4 : 1 }).map((_, i) => (
            <div
              key={i}
              className="rounded-2xl p-4"
              style={{ background: "var(--ls-card-bg)", border: "1px solid var(--ls-border-soft)" }}
            >
              <div className="flex items-center gap-3">
                <Skeleton className="h-10 w-10 rounded-full" />
                <div className="flex-1">
                  <Skeleton className="mb-1.5 h-4 w-32" />
                  <Skeleton className="h-3 w-48" />
                </div>
                <Skeleton className="h-3 w-20" />
              </div>
            </div>
          ))}
        </div>
      )}

      {/* All zodiac view */}
      {!isLoading && mode === "all" && allHoroscopes.length > 0 && (
        <div className="flex flex-col gap-2.5">
          {allHoroscopes.map((h, idx) => (
            <HoroscopeCard
              key={idx}
              horoscope={h}
              expanded={expandedIndex === idx || userZodiacIndex === idx}
              onToggle={() => setExpandedIndex((prev) => (prev === idx ? null : idx))}
            />
          ))}
        </div>
      )}

      {/* Personal horoscope view */}
      {!isLoading && mode === "personal" && personalHoroscope && (
        <HoroscopeCard horoscope={personalHoroscope} expanded={true} onToggle={() => {}} />
      )}

      {!isLoading && mode === "personal" && !personalHoroscope && selectedBirthYear > 0 && (
        <div
          className="rounded-2xl p-8 text-center"
          style={{ background: "var(--ls-card-bg)", border: "1px solid var(--ls-border-soft)" }}
        >
          <p className="text-text-muted-ls text-sm">Không thể lấy tử vi. Vui lòng thử lại.</p>
        </div>
      )}
    </div>
  );
}
