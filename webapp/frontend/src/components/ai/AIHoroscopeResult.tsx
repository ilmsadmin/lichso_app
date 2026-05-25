"use client";

import { BatTuInfo, NguHanhBalance } from "@/types/ai";
import { AIStreamingText } from "./AIStreamingText";
import { Sparkles, BarChart3 } from "lucide-react";

interface AIHoroscopeResultProps {
  batTu?: BatTuInfo;
  nguHanh?: NguHanhBalance;
  aiResult: string;
  isStreaming?: boolean;
  quotaRemaining?: number;
  tokensUsed?: number;
}

const ELEMENT_COLORS: Record<string, string> = {
  Kim: "bg-gray-400",
  Mộc: "bg-green-500",
  Thủy: "bg-blue-500",
  Hỏa: "bg-red-500",
  Thổ: "bg-yellow-600",
  // English fallbacks
  Metal: "bg-gray-400",
  Wood: "bg-green-500",
  Water: "bg-blue-500",
  Fire: "bg-red-500",
  Earth: "bg-yellow-600",
};

const ELEMENT_BG: Record<string, string> = {
  Kim: "bg-gray-100 text-gray-700",
  Mộc: "bg-green-100 text-green-800",
  Thủy: "bg-blue-100 text-blue-800",
  Hỏa: "bg-red-100 text-red-800",
  Thổ: "bg-yellow-100 text-yellow-800",
  Metal: "bg-gray-100 text-gray-700",
  Wood: "bg-green-100 text-green-800",
  Water: "bg-blue-100 text-blue-800",
  Fire: "bg-red-100 text-red-800",
  Earth: "bg-yellow-100 text-yellow-800",
};

const PILLARS = [
  { key: "year_pillar" as const, label: "Năm" },
  { key: "month_pillar" as const, label: "Tháng" },
  { key: "day_pillar" as const, label: "Ngày" },
  { key: "hour_pillar" as const, label: "Giờ" },
];

export function AIHoroscopeResult({
  batTu,
  nguHanh,
  aiResult,
  isStreaming = false,
  quotaRemaining,
  tokensUsed,
}: AIHoroscopeResultProps) {
  return (
    <div className="space-y-5 animate-[fadeUp_0.4s_ease-out_both]">
      {/* Bát Tự — 4 pillars table */}
      {batTu && (
        <div
          className="rounded-2xl p-5 backdrop-blur-[14px]"
          style={{
            background: "var(--ls-card-bg-strong)",
            border: "1px solid var(--ls-border-warm)",
            boxShadow: "0 4px 24px var(--ls-shadow-warm)",
          }}
        >
          <h3 className="text-text-dark mb-4 flex items-center gap-2 text-sm font-[var(--font-lora)] font-semibold">
            <Sparkles className="h-4 w-4 text-amber-500" /> Bát Tự (Tứ Trụ)
          </h3>
          <div className="grid grid-cols-4 gap-2">
            {PILLARS.map(({ key, label }) => {
              const pillar = batTu[key];
              if (!pillar) return null;
              const elemColor = ELEMENT_BG[pillar.element] ?? "bg-gray-100 text-gray-700";
              return (
                <div
                  key={key}
                  className="flex flex-col items-center rounded-xl p-3 text-center"
                  style={{
                    background: "rgba(255,252,248,0.5)",
                    border: "1px solid var(--ls-border-soft)",
                  }}
                >
                  <span className="text-text-soft mb-1 text-[10px] uppercase tracking-widest">
                    {label}
                  </span>
                  <span className="text-text-dark text-lg font-bold font-[var(--font-lora)]">
                    {pillar.heavenly_stem}
                  </span>
                  <span className="text-text-mid text-base">{pillar.earthly_branch}</span>
                  <span
                    className={`mt-2 rounded-full px-2 py-0.5 text-[10px] font-medium ${elemColor}`}
                  >
                    {pillar.element}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Ngũ Hành Balance */}
      {nguHanh && (
        <div
          className="rounded-2xl p-5 backdrop-blur-[14px]"
          style={{
            background: "var(--ls-card-bg-strong)",
            border: "1px solid var(--ls-border-warm)",
          }}
        >
          <h3 className="text-text-dark mb-4 flex items-center gap-2 text-sm font-[var(--font-lora)] font-semibold">
            <BarChart3 className="h-4 w-4 text-amber-500" /> Ngũ Hành Cân Bằng
          </h3>
          <div className="space-y-2">
            {(["Kim", "Mộc", "Thủy", "Hỏa", "Thổ"] as const).map((elem) => {
              const val = (nguHanh as unknown as Record<string, number | string>)[elem] as number ?? 0;
              const pct = Math.min(100, val * 25); // assume max ~4
              const barColor = ELEMENT_COLORS[elem] ?? "bg-gray-400";
              const isStrongest = nguHanh.strongest === elem;
              const isWeakest = nguHanh.weakest === elem;
              return (
                <div key={elem} className="flex items-center gap-3">
                  <span
                    className={`w-8 text-right text-xs font-medium ${
                      isStrongest ? "text-amber-600" : isWeakest ? "text-gray-400" : "text-text-mid"
                    }`}
                  >
                    {elem}
                  </span>
                  <div className="h-2.5 flex-1 rounded-full bg-gray-100 overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-700 ${barColor}`}
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                  <span className="w-4 text-left text-xs text-text-soft">{val}</span>
                  {isStrongest && (
                    <span className="rounded-full bg-amber-100 px-1.5 py-0.5 text-[10px] font-medium text-amber-700">
                      Mạnh
                    </span>
                  )}
                  {isWeakest && (
                    <span className="rounded-full bg-gray-100 px-1.5 py-0.5 text-[10px] text-gray-500">
                      Yếu
                    </span>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* AI Analysis Text */}
      <div
        className="rounded-2xl p-5 backdrop-blur-[14px]"
        style={{
          background: "var(--ls-card-bg-strong)",
          border: "1px solid var(--ls-border-warm)",
          boxShadow: "0 4px 24px var(--ls-shadow-warm)",
        }}
      >
        <h3 className="text-text-dark mb-3 flex items-center gap-2 text-sm font-[var(--font-lora)] font-semibold">
          <Sparkles className="h-4 w-4 text-purple-500" /> Luận Giải AI
        </h3>
        <AIStreamingText
          text={aiResult}
          isStreaming={isStreaming}
          className="text-text-mid"
        />

        {/* Footer meta */}
        {!isStreaming && tokensUsed !== undefined && (
          <div className="mt-4 flex items-center gap-3 border-t pt-3 text-[11px] text-text-soft"
            style={{ borderColor: "var(--ls-border-soft)" }}
          >
            <span>{tokensUsed.toLocaleString()} tokens</span>
            {quotaRemaining !== undefined && (
              <>
                <span>·</span>
                <span>Còn {quotaRemaining} lượt hôm nay</span>
              </>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
