"use client";

import { useState } from "react";
import { useLunarAge } from "@/hooks/useV3";
import { Skeleton } from "@/components/ui/skeleton";
import { User, Calendar, Sparkles } from "lucide-react";

const NGU_HANH_COLORS: Record<string, string> = {
  Kim: "from-amber-200/30 to-yellow-100/30 border-amber-300/40",
  Mộc: "from-green-200/30 to-emerald-100/30 border-green-300/40",
  Thuỷ: "from-blue-200/30 to-cyan-100/30 border-blue-300/40",
  Hoả: "from-red-200/30 to-orange-100/30 border-red-300/40",
  Thổ: "from-yellow-200/30 to-amber-100/30 border-yellow-400/40",
};

const NGU_HANH_EMOJIS: Record<string, string> = {
  Kim: "🪙",
  Mộc: "🌿",
  Thuỷ: "💧",
  Hoả: "🔥",
  Thổ: "🪨",
};

export function LunarAgeCalculator() {
  const [inputYear, setInputYear] = useState("");
  const [birthYear, setBirthYear] = useState(0);

  const { data, isLoading } = useLunarAge(birthYear);
  const result = data?.data;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const y = Number(inputYear);
    if (y >= 1900 && y <= 2100) {
      setBirthYear(y);
    }
  };

  const nguHanhClass = result ? (NGU_HANH_COLORS[result.ngu_hanh] ?? "") : "";
  const nguHanhEmoji = result ? (NGU_HANH_EMOJIS[result.ngu_hanh] ?? "⭐") : "";

  return (
    <div className="animate-[fadeUp_0.65s_ease-out_both]">
      {/* Header */}
      <div className="mb-4 flex items-center gap-2.5">
        <User className="text-warm-amber h-4 w-4" />
        <span className="text-text-dark text-base font-[var(--font-lora)] tracking-wide">
          Tra Cứu Tuổi Âm Lịch
        </span>
      </div>

      {/* Input form */}
      <form
        onSubmit={handleSubmit}
        className="mb-5 rounded-xl p-4"
        style={{
          background: "linear-gradient(135deg, rgba(74,139,127,0.06), rgba(196,120,58,0.04))",
          border: "1px solid var(--ls-border-soft)",
        }}
      >
        <label className="text-text-muted-ls mb-2 block text-[12px]">
          Nhập năm sinh dương lịch
        </label>
        <div className="flex gap-3">
          <input
            type="number"
            min={1900}
            max={2100}
            placeholder="VD: 1990"
            value={inputYear}
            onChange={(e) => setInputYear(e.target.value)}
            className="text-text-dark placeholder:text-text-muted-ls/50 flex-1 rounded-lg px-3 py-2.5 text-sm outline-none"
            style={{
              background: "var(--ls-card-bg)",
              border: "1px solid var(--ls-border-soft)",
            }}
          />
          <button
            type="submit"
            className="rounded-lg px-5 py-2.5 text-[13px] font-medium transition-all hover:opacity-90"
            style={{
              background: "linear-gradient(135deg, var(--jade-teal), var(--jade-soft))",
              color: "white",
            }}
          >
            Tra cứu
          </button>
        </div>
      </form>

      {/* Loading */}
      {isLoading && (
        <div
          className="rounded-2xl p-6"
          style={{ background: "var(--ls-card-bg)", border: "1px solid var(--ls-border-soft)" }}
        >
          <Skeleton className="mx-auto mb-4 h-20 w-20 rounded-full" />
          <Skeleton className="mx-auto mb-3 h-5 w-40" />
          <Skeleton className="mx-auto mb-2 h-3 w-60" />
          <Skeleton className="mx-auto h-3 w-48" />
        </div>
      )}

      {/* Result */}
      {!isLoading && result && (
        <div className={`overflow-hidden rounded-2xl border bg-gradient-to-br ${nguHanhClass}`}>
          {/* Hero section */}
          <div className="px-4 pt-6 pb-4 text-center">
            <span className="mb-2 block text-5xl">{result.con_giap_emoji}</span>
            <h3 className="text-text-dark mb-1 text-xl font-[var(--font-lora)] font-semibold">
              {result.con_giap} — {result.can_chi}
            </h3>
            <p className="text-text-soft text-[12px]">Năm sinh: {result.birth_year}</p>
          </div>

          {/* Age info cards */}
          <div className="mb-4 grid grid-cols-3 gap-2 px-4">
            <AgeCard
              label="Tuổi dương"
              value={result.tuoi_duong}
              icon={<Calendar className="h-3.5 w-3.5" />}
            />
            <AgeCard
              label="Tuổi âm"
              value={result.tuoi_am}
              icon={<Calendar className="h-3.5 w-3.5" />}
            />
            <AgeCard
              label="Tuổi mụ"
              value={result.tuoi_mu}
              icon={<Sparkles className="h-3.5 w-3.5" />}
            />
          </div>

          {/* Details */}
          <div
            className="mx-4 mb-4 rounded-xl p-4"
            style={{
              background: "var(--ls-card-bg)",
              border: "1px solid var(--ls-border-soft)",
            }}
          >
            <div className="grid grid-cols-2 gap-3">
              <DetailRow label="Can Chi" value={result.can_chi} />
              <DetailRow label="Con Giáp" value={`${result.con_giap_emoji} ${result.con_giap}`} />
              <DetailRow label="Ngũ Hành" value={`${nguHanhEmoji} ${result.ngu_hanh}`} />
              <DetailRow label="Mệnh" value={result.menh} />
            </div>
          </div>

          {/* Explanation */}
          <div
            className="mx-4 mb-4 rounded-xl p-3"
            style={{
              background: "linear-gradient(135deg, var(--warm-cream) 0%, var(--warm-peach) 100%)",
              border: "1px solid var(--ls-border-warm)",
            }}
          >
            <p className="text-text-mid text-[11px] leading-relaxed">
              💡 <strong>Tuổi mụ</strong> (tuổi âm) là cách tính tuổi truyền thống Việt Nam, tính cả
              năm sinh và năm hiện tại. Tuổi mụ = Năm hiện tại − Năm sinh + 1.
            </p>
          </div>
        </div>
      )}

      {/* Empty state */}
      {!isLoading && !result && birthYear === 0 && (
        <div
          className="rounded-2xl p-10 text-center"
          style={{ background: "var(--ls-card-bg)", border: "1px solid var(--ls-border-soft)" }}
        >
          <span className="mb-3 block text-3xl">🎂</span>
          <p className="text-text-muted-ls text-sm">Nhập năm sinh để xem thông tin tuổi âm lịch</p>
        </div>
      )}
    </div>
  );
}

function AgeCard({ label, value, icon }: { label: string; value: number; icon: React.ReactNode }) {
  return (
    <div
      className="rounded-xl p-3 text-center"
      style={{
        background: "var(--ls-card-bg)",
        border: "1px solid var(--ls-border-soft)",
      }}
    >
      <div className="text-text-muted-ls mb-1 flex items-center justify-center gap-1">
        {icon}
        <span className="text-[10px]">{label}</span>
      </div>
      <span className="text-text-dark text-2xl font-[var(--font-lora)] font-bold">{value}</span>
    </div>
  );
}

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <span className="text-text-muted-ls mb-0.5 block text-[10px]">{label}</span>
      <span className="text-text-dark text-[13px] font-medium">{value}</span>
    </div>
  );
}
