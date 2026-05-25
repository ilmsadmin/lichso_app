"use client";

import { useState } from "react";
import { useStreamHoroscope, useHoroscopeQuota } from "@/hooks/useAI";
import { HoroscopeAIRequest, HoroscopeReadingType, HoroscopeDepth } from "@/types/ai";
import { AIUsageQuota } from "./AIUsageQuota";
import { AIHoroscopeResult } from "./AIHoroscopeResult";
import { Sparkles, Loader2 } from "lucide-react";

const READING_TYPES: { value: HoroscopeReadingType; label: string }[] = [
  { value: "overview", label: "Tổng quan bản mệnh" },
  { value: "yearly", label: "Vận hạn năm nay" },
  { value: "monthly", label: "Vận hạn tháng này" },
  { value: "question", label: "Hỏi cụ thể" },
  { value: "compatibility", label: "Hợp / Khắc tuổi" },
  { value: "choose_date", label: "Chọn ngày tốt" },
];

const DEPTHS: { value: HoroscopeDepth; label: string }[] = [
  { value: "brief", label: "Tóm tắt" },
  { value: "standard", label: "Chi tiết vừa" },
  { value: "detailed", label: "Phân tích sâu" },
];

const currentYear = new Date().getFullYear();

export function AIHoroscopeForm() {
  const { data: quotaResponse, isLoading: quotaLoading } = useHoroscopeQuota();
  const quota = quotaResponse?.data;
  const { isStreaming, streamText, summary, error, startStream, reset } = useStreamHoroscope();

  const [form, setForm] = useState<Omit<HoroscopeAIRequest, "stream">>({
    birth_year: currentYear - 30,
    birth_month: 1,
    birth_day: 1,
    birth_hour: undefined,
    gender: "male",
    reading_type: "overview",
    depth: "standard",
    question: "",
  });

  const handleChange = <K extends keyof typeof form>(key: K, value: (typeof form)[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (isStreaming) return;
    reset();
    startStream({ ...form, stream: true });
  };

  const exhausted = quota && quota.remaining === 0;
  const hasResult = !!(summary || streamText);

  return (
    <div className="space-y-6">
      {/* Quota badge */}
      <div className="flex items-center justify-between">
        <h2 className="text-text-dark font-[var(--font-lora)] text-xl font-semibold">
          🔮 Xem Tử Vi AI — Bát Tự
        </h2>
        <AIUsageQuota quota={quota} isLoading={quotaLoading} />
      </div>

      {/* Form */}
      <form
        onSubmit={handleSubmit}
        className="rounded-2xl p-6 backdrop-blur-[14px] space-y-4"
        style={{
          background: "var(--ls-card-bg-strong)",
          border: "1px solid var(--ls-border-warm)",
          boxShadow: "0 4px 24px var(--ls-shadow-warm)",
        }}
      >
        {/* Birth info row */}
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          <div>
            <label className="text-text-soft mb-1 block text-[11px] tracking-[2px] uppercase">Năm sinh</label>
            <input
              type="number"
              min={1900}
              max={currentYear}
              value={form.birth_year}
              onChange={(e) => handleChange("birth_year", parseInt(e.target.value))}
              className="w-full rounded-xl px-3 py-2 text-sm outline-none transition-all"
              style={{ background: "rgba(255,252,248,0.5)", border: "1px solid var(--ls-border-warm)" }}
              required
            />
          </div>
          <div>
            <label className="text-text-soft mb-1 block text-[11px] tracking-[2px] uppercase">Tháng</label>
            <input
              type="number"
              min={1}
              max={12}
              value={form.birth_month}
              onChange={(e) => handleChange("birth_month", parseInt(e.target.value))}
              className="w-full rounded-xl px-3 py-2 text-sm outline-none transition-all"
              style={{ background: "rgba(255,252,248,0.5)", border: "1px solid var(--ls-border-warm)" }}
              required
            />
          </div>
          <div>
            <label className="text-text-soft mb-1 block text-[11px] tracking-[2px] uppercase">Ngày</label>
            <input
              type="number"
              min={1}
              max={31}
              value={form.birth_day}
              onChange={(e) => handleChange("birth_day", parseInt(e.target.value))}
              className="w-full rounded-xl px-3 py-2 text-sm outline-none transition-all"
              style={{ background: "rgba(255,252,248,0.5)", border: "1px solid var(--ls-border-warm)" }}
              required
            />
          </div>
          <div>
            <label className="text-text-soft mb-1 block text-[11px] tracking-[2px] uppercase">Giờ sinh (0-23)</label>
            <input
              type="number"
              min={0}
              max={23}
              value={form.birth_hour ?? ""}
              placeholder="Tùy chọn"
              onChange={(e) =>
                handleChange("birth_hour", e.target.value ? parseInt(e.target.value) : undefined)
              }
              className="w-full rounded-xl px-3 py-2 text-sm outline-none transition-all"
              style={{ background: "rgba(255,252,248,0.5)", border: "1px solid var(--ls-border-warm)" }}
            />
          </div>
        </div>

        {/* Gender */}
        <div>
          <label className="text-text-soft mb-2 block text-[11px] tracking-[2px] uppercase">Giới tính</label>
          <div className="flex gap-3">
            {(["male", "female"] as const).map((g) => (
              <label key={g} className="flex cursor-pointer items-center gap-2">
                <input
                  type="radio"
                  name="gender"
                  value={g}
                  checked={form.gender === g}
                  onChange={() => handleChange("gender", g)}
                  className="accent-amber-500"
                />
                <span className="text-text-mid text-sm">{g === "male" ? "👨 Nam" : "👩 Nữ"}</span>
              </label>
            ))}
          </div>
        </div>

        {/* Reading type + Depth */}
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <div>
            <label className="text-text-soft mb-1 block text-[11px] tracking-[2px] uppercase">Loại luận giải</label>
            <select
              value={form.reading_type}
              onChange={(e) => handleChange("reading_type", e.target.value as HoroscopeReadingType)}
              className="text-text-dark w-full rounded-xl px-3 py-2 text-sm outline-none"
              style={{ background: "rgba(255,252,248,0.7)", border: "1px solid var(--ls-border-warm)" }}
            >
              {READING_TYPES.map((rt) => (
                <option key={rt.value} value={rt.value}>
                  {rt.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="text-text-soft mb-1 block text-[11px] tracking-[2px] uppercase">Độ sâu</label>
            <select
              value={form.depth}
              onChange={(e) => handleChange("depth", e.target.value as HoroscopeDepth)}
              className="text-text-dark w-full rounded-xl px-3 py-2 text-sm outline-none"
              style={{ background: "rgba(255,252,248,0.7)", border: "1px solid var(--ls-border-warm)" }}
            >
              {DEPTHS.map((d) => (
                <option key={d.value} value={d.value}>
                  {d.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Question (only for "question" type) */}
        {form.reading_type === "question" && (
          <div>
            <label className="text-text-soft mb-1 block text-[11px] tracking-[2px] uppercase">Câu hỏi của bạn</label>
            <textarea
              value={form.question}
              onChange={(e) => handleChange("question", e.target.value)}
              placeholder="Ví dụ: Năm nay tôi có nên đổi việc làm không?"
              rows={3}
              className="text-text-dark w-full rounded-xl px-3 py-2 text-sm outline-none resize-none"
              style={{ background: "rgba(255,252,248,0.5)", border: "1px solid var(--ls-border-warm)" }}
            />
          </div>
        )}

        {/* Error */}
        {error && (
          <p className="rounded-xl bg-red-50 px-3 py-2 text-sm text-red-600 border border-red-100">
            {error}
          </p>
        )}

        {/* Submit */}
        <button
          type="submit"
          disabled={isStreaming || !!exhausted}
          className="flex w-full items-center justify-center gap-2 rounded-xl py-3 text-sm font-medium text-white transition-all hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed"
          style={{
            background: "linear-gradient(135deg, #a855f7, #6366f1)",
            boxShadow: "0 4px 16px rgba(168,85,247,0.3)",
          }}
        >
          {isStreaming ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              Đang phân tích bát tự…
            </>
          ) : (
            <>
              <Sparkles className="h-4 w-4" />
              {exhausted ? "Hết lượt hôm nay" : "Phân tích Bát Tự với AI"}
            </>
          )}
        </button>
      </form>

      {/* Result */}
      {hasResult && (
        <AIHoroscopeResult
          batTu={summary?.bat_tu}
          nguHanh={summary?.ngu_hanh}
          aiResult={streamText}
          isStreaming={isStreaming}
          quotaRemaining={summary?.quota_remaining}
          tokensUsed={summary?.tokens_used}
        />
      )}
    </div>
  );
}
