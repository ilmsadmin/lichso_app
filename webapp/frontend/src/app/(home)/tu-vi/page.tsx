"use client";

import { useState } from "react";
import { Wand2, ScrollText, Drama, Heart, ShieldAlert, CircleDot, Orbit, Bot } from "lucide-react";
import { BackgroundLayer } from "@/components/lichso/BackgroundLayer";
import { HoroscopeWidget } from "@/components/lichso/HoroscopeWidget";
import { LunarAgeCalculator } from "@/components/lichso/LunarAgeCalculator";
import { AIHoroscopeForm } from "@/components/ai/AIHoroscopeForm";
import Link from "next/link";

const canList = ["Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý"];
const chiList = [
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
const conGiapList = [
  "🐀 Chuột",
  "🐃 Trâu",
  "🐅 Hổ",
  "🐈 Mèo",
  "🐉 Rồng",
  "🐍 Rắn",
  "🐎 Ngựa",
  "🐐 Dê",
  "🐒 Khỉ",
  "🐓 Gà",
  "🐕 Chó",
  "🐖 Heo",
];
const nguHanhList = ["Mộc", "Mộc", "Hoả", "Hoả", "Thổ", "Thổ", "Kim", "Kim", "Thuỷ", "Thuỷ"];
const nguHanhColors: Record<string, string> = {
  Mộc: "text-green-600",
  Hoả: "text-red-500",
  Thổ: "text-yellow-700",
  Kim: "text-gray-500",
  Thuỷ: "text-blue-500",
};

const nguHanhEmoji: Record<string, string> = {
  Mộc: "🌳",
  Hoả: "🔥",
  Thổ: "🏔️",
  Kim: "⚔️",
  Thuỷ: "💧",
};

interface TuViResult {
  canChi: string;
  conGiap: string;
  nguHanh: string;
  canDesc: string;
  chiDesc: string;
  moTa: string;
  tinhCach: string[];
  hop: string[];
  xung: string[];
}

function calculateTuVi(year: number): TuViResult {
  const canIdx = (year + 6) % 10;
  const chiIdx = (year + 8) % 12;
  const can = canList[canIdx];
  const chi = chiList[chiIdx];
  const conGiap = conGiapList[chiIdx];
  const nguHanh = nguHanhList[canIdx];

  // Nạp âm ngũ hành (simplified)
  const napAmIdx = Math.floor(((year - 4) % 60) / 2) % 5;
  const napAmList = ["Kim", "Thuỷ", "Hoả", "Thổ", "Mộc"];
  const napAmNguHanh = napAmList[napAmIdx];

  const tinhCachMap: Record<string, string[]> = {
    Tý: ["Thông minh", "Nhanh nhẹn", "Linh hoạt", "Tiết kiệm"],
    Sửu: ["Kiên nhẫn", "Chăm chỉ", "Đáng tin cậy", "Bền bỉ"],
    Dần: ["Dũng cảm", "Quyết đoán", "Mạnh mẽ", "Tham vọng"],
    Mão: ["Ôn hòa", "Thanh lịch", "Khéo léo", "Tinh tế"],
    Thìn: ["Uy nghi", "Cao thượng", "Tham vọng", "Quyền lực"],
    Tỵ: ["Sâu sắc", "Bí ẩn", "Trí tuệ", "Quyến rũ"],
    Ngọ: ["Nhiệt huyết", "Tự do", "Phóng khoáng", "Năng động"],
    Mùi: ["Hiền lành", "Sáng tạo", "Nhạy cảm", "Tử tế"],
    Thân: ["Thông minh", "Khéo léo", "Hài hước", "Linh hoạt"],
    Dậu: ["Cần cù", "Tỉ mỉ", "Trung thực", "Thẳng thắn"],
    Tuất: ["Trung thành", "Chính trực", "Dũng cảm", "Chân thành"],
    Hợi: ["Hào phóng", "Chân thành", "Kiên nhẫn", "Lạc quan"],
  };

  const hopMap: Record<string, string[]> = {
    Tý: ["Sửu", "Thìn", "Thân"],
    Sửu: ["Tý", "Tỵ", "Dậu"],
    Dần: ["Hợi", "Ngọ", "Tuất"],
    Mão: ["Tuất", "Mùi", "Hợi"],
    Thìn: ["Dậu", "Tý", "Thân"],
    Tỵ: ["Thân", "Sửu", "Dậu"],
    Ngọ: ["Mùi", "Dần", "Tuất"],
    Mùi: ["Ngọ", "Mão", "Hợi"],
    Thân: ["Tỵ", "Tý", "Thìn"],
    Dậu: ["Thìn", "Sửu", "Tỵ"],
    Tuất: ["Mão", "Dần", "Ngọ"],
    Hợi: ["Dần", "Mão", "Mùi"],
  };

  const xungMap: Record<string, string[]> = {
    Tý: ["Ngọ", "Mùi"],
    Sửu: ["Mùi", "Ngọ"],
    Dần: ["Thân", "Tỵ"],
    Mão: ["Dậu", "Thìn"],
    Thìn: ["Tuất", "Mão"],
    Tỵ: ["Hợi", "Dần"],
    Ngọ: ["Tý", "Sửu"],
    Mùi: ["Sửu", "Tý"],
    Thân: ["Dần", "Hợi"],
    Dậu: ["Mão", "Tuất"],
    Tuất: ["Thìn", "Dậu"],
    Hợi: ["Tỵ", "Thân"],
  };

  const moTaMap: Record<string, string> = {
    Tý: "Người tuổi Tý sinh ra trong năm Chuột, sở hữu trí tuệ sáng suốt và tính cách linh hoạt.",
    Sửu: "Người tuổi Sửu sinh ra trong năm Trâu, nổi tiếng với sự kiên nhẫn và đức tính chăm chỉ.",
    Dần: "Người tuổi Dần sinh ra trong năm Hổ, mang phong thái dũng mãnh và tinh thần chiến đấu.",
    Mão: "Người tuổi Mão sinh ra trong năm Mèo, thanh lịch và có khiếu thẩm mỹ tinh tế.",
    Thìn: "Người tuổi Thìn sinh ra trong năm Rồng, uy nghi quyền quý, tham vọng lớn lao.",
    Tỵ: "Người tuổi Tỵ sinh ra trong năm Rắn, sâu sắc trí tuệ và đầy bí ẩn.",
    Ngọ: "Người tuổi Ngọ sinh ra trong năm Ngựa, năng động, yêu tự do và phóng khoáng.",
    Mùi: "Người tuổi Mùi sinh ra trong năm Dê, hiền lành, sáng tạo và giàu cảm xúc.",
    Thân: "Người tuổi Thân sinh ra trong năm Khỉ, thông minh, nhanh nhẹn và hài hước.",
    Dậu: "Người tuổi Dậu sinh ra trong năm Gà, cần cù, tỉ mỉ và rất có trách nhiệm.",
    Tuất: "Người tuổi Tuất sinh ra trong năm Chó, trung thành, chính trực và đáng tin cậy.",
    Hợi: "Người tuổi Hợi sinh ra trong năm Heo, hào phóng, lạc quan và giàu lòng vị tha.",
  };

  return {
    canChi: `${can} ${chi}`,
    conGiap: conGiap,
    nguHanh: napAmNguHanh,
    canDesc: `Thiên Can: ${can} — ${nguHanhList[canIdx]} (${canIdx % 2 === 0 ? "Dương" : "Âm"})`,
    chiDesc: `Địa Chi: ${chi} — Con giáp ${conGiap}`,
    moTa: moTaMap[chi] ?? "",
    tinhCach: tinhCachMap[chi] ?? [],
    hop: hopMap[chi] ?? [],
    xung: xungMap[chi] ?? [],
  };
}

export default function TuViPage() {
  const currentYear = new Date().getFullYear();
  const [year, setYear] = useState(currentYear);
  const [result, setResult] = useState<TuViResult | null>(null);
  const [inputVal, setInputVal] = useState(String(currentYear));

  const handleLookup = () => {
    const y = parseInt(inputVal);
    if (isNaN(y) || y < 1900 || y > 2100) return;
    setYear(y);
    setResult(calculateTuVi(y));
  };

  return (
    <>
      <BackgroundLayer />
      <div className="relative z-[1] mx-auto max-w-[1180px] px-4 pb-16 sm:px-7">
        {/* Header */}
        <div className="pt-10 pb-8 text-center">
          <div
            className="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-2xl"
            style={{
              background: "linear-gradient(135deg, #a855f7, #6366f1)",
              boxShadow: "0 4px 16px rgba(168,85,247,0.25)",
            }}
          >
            <Wand2 className="h-6 w-6 text-white" />
          </div>
          <h1 className="text-text-dark mb-3 text-3xl font-[var(--font-lora)] font-semibold">
            Tử Vi Giản Lược
          </h1>
          <p className="text-text-soft mx-auto max-w-lg text-sm">
            Nhập năm sinh để xem thông tin tử vi cơ bản: Can Chi, ngũ hành, con giáp, tính cách, tam
            hợp, tứ xung.
          </p>
        </div>

        {/* Input */}
        <div
          className="mx-auto mb-8 max-w-md rounded-2xl p-6 backdrop-blur-[14px]"
          style={{
            background: "var(--ls-card-bg-strong)",
            border: "1px solid var(--ls-border-warm)",
            boxShadow: "0 4px 24px var(--ls-shadow-warm)",
          }}
        >
          <label className="text-text-soft mb-2 block text-[11px] tracking-[2px] uppercase">
            Năm Sinh (Dương lịch)
          </label>
          <div className="flex gap-3">
            <input
              type="number"
              min={1900}
              max={2100}
              value={inputVal}
              onChange={(e) => setInputVal(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && handleLookup()}
              className="text-text-dark focus:border-warm-amber/40 flex-1 rounded-xl px-4 py-2.5 text-lg font-[var(--font-lora)] backdrop-blur-md transition-all outline-none"
              style={{
                background: "rgba(255,252,248,0.5)",
                border: "1px solid var(--ls-border-warm)",
              }}
            />
            <button
              onClick={handleLookup}
              className="rounded-xl px-6 py-2.5 text-sm font-medium text-white transition-all hover:opacity-90"
              style={{
                background: "linear-gradient(135deg, var(--warm-amber), var(--warm-gold))",
                boxShadow: "0 4px 16px rgba(196,120,58,0.3)",
              }}
            >
              Xem
            </button>
          </div>
        </div>

        {/* Results */}
        {result && (
          <div className="animate-[fadeUp_0.5s_ease-out_both] space-y-6">
            {/* Main info card */}
            <div
              className="rounded-2xl p-8 text-center backdrop-blur-[14px]"
              style={{
                background: "var(--ls-card-bg-strong)",
                border: "1px solid var(--ls-border-warm)",
                boxShadow: "0 8px 40px var(--ls-shadow-warm)",
              }}
            >
              <div className="mb-4 text-6xl">{result.conGiap.split(" ")[0]}</div>
              <h2 className="text-text-dark mb-2 text-2xl font-[var(--font-lora)] font-semibold">
                Năm {result.canChi} — {year}
              </h2>
              <p className="text-text-mid mb-3 text-sm">Con giáp: {result.conGiap}</p>
              <div
                className="inline-flex items-center gap-2 rounded-full px-4 py-1.5"
                style={{
                  background: "rgba(196,120,58,0.08)",
                  border: "1px solid var(--ls-border-warm)",
                }}
              >
                <span>{nguHanhEmoji[result.nguHanh] || "🌐"}</span>
                <span
                  className={`text-sm font-medium ${nguHanhColors[result.nguHanh] || "text-text-dark"}`}
                >
                  Ngũ Hành Nạp Âm: {result.nguHanh}
                </span>
              </div>
              <p className="text-text-muted-ls mt-3 text-xs">{result.canDesc}</p>
              <p className="text-text-muted-ls text-xs">{result.chiDesc}</p>
            </div>

            {/* Description */}
            <div
              className="rounded-2xl p-6 backdrop-blur-[14px]"
              style={{
                background: "var(--ls-card-bg)",
                border: "1px solid var(--ls-border-soft)",
              }}
            >
              <h3 className="text-text-dark mb-3 flex items-center gap-2 text-base font-[var(--font-lora)]">
                <ScrollText className="text-warm-amber h-4 w-4" /> Tổng Quan
              </h3>
              <p className="text-text-mid text-sm leading-relaxed">{result.moTa}</p>
            </div>

            <div className="grid gap-4 sm:grid-cols-3">
              {/* Tính cách */}
              <div
                className="rounded-2xl p-5 backdrop-blur-[14px]"
                style={{
                  background: "var(--ls-card-bg)",
                  border: "1px solid var(--ls-border-soft)",
                }}
              >
                <h3 className="text-text-soft mb-3 flex items-center gap-1.5 text-[11px] tracking-[2px] uppercase">
                  <Drama className="h-3 w-3" /> Tính Cách
                </h3>
                <ul className="space-y-2">
                  {result.tinhCach.map((t) => (
                    <li key={t} className="text-text-mid flex items-center gap-2 text-sm">
                      <span className="bg-warm-amber h-1.5 w-1.5 rounded-full" />
                      {t}
                    </li>
                  ))}
                </ul>
              </div>

              {/* Tam hợp */}
              <div
                className="rounded-2xl p-5 backdrop-blur-[14px]"
                style={{
                  background: "var(--ls-card-bg)",
                  border: "1px solid var(--ls-border-soft)",
                }}
              >
                <h3 className="text-jade-teal mb-3 flex items-center gap-1.5 text-[11px] tracking-[2px] uppercase">
                  <Heart className="h-3 w-3" /> Tam Hợp
                </h3>
                <ul className="space-y-2">
                  {result.hop.map((h) => (
                    <li key={h} className="text-text-mid flex items-center gap-2 text-sm">
                      <span className="bg-jade-teal h-1.5 w-1.5 rounded-full" />
                      {h} — {conGiapList[chiList.indexOf(h)]}
                    </li>
                  ))}
                </ul>
                <p className="text-text-muted-ls mt-3 text-xs">Tương hợp, hỗ trợ lẫn nhau</p>
              </div>

              {/* Tứ xung */}
              <div
                className="rounded-2xl p-5 backdrop-blur-[14px]"
                style={{
                  background: "var(--ls-card-bg)",
                  border: "1px solid var(--ls-border-soft)",
                }}
              >
                <h3 className="text-danger mb-3 flex items-center gap-1.5 text-[11px] tracking-[2px] uppercase">
                  <ShieldAlert className="h-3 w-3" /> Tương Xung
                </h3>
                <ul className="space-y-2">
                  {result.xung.map((x) => (
                    <li key={x} className="text-text-mid flex items-center gap-2 text-sm">
                      <span className="bg-danger h-1.5 w-1.5 rounded-full" />
                      {x} — {conGiapList[chiList.indexOf(x)]}
                    </li>
                  ))}
                </ul>
                <p className="text-text-muted-ls mt-3 text-xs">Tương khắc, nên cẩn trọng</p>
              </div>
            </div>

            {/* Zodiac wheel */}
            <div
              className="rounded-2xl p-6 backdrop-blur-[14px]"
              style={{
                background: "var(--ls-card-bg)",
                border: "1px solid var(--ls-border-soft)",
              }}
            >
              <h3 className="text-text-dark mb-4 flex items-center gap-2 text-base font-[var(--font-lora)]">
                <Orbit className="text-warm-amber h-4 w-4" /> Vòng 12 Con Giáp
              </h3>
              <div className="grid grid-cols-4 gap-2 sm:grid-cols-6 lg:grid-cols-12">
                {chiList.map((chi, i) => {
                  const isActive = chi === chiList[(year + 8) % 12];
                  const isHop = result.hop.includes(chi);
                  const isXung = result.xung.includes(chi);
                  return (
                    <div
                      key={chi}
                      className={`rounded-xl p-2 text-center text-sm transition-all ${
                        isActive
                          ? "ring-warm-amber bg-warm-amber/10 ring-2"
                          : isHop
                            ? "bg-jade-teal/10 border-jade-teal/20"
                            : isXung
                              ? "bg-danger/10 border-danger/20"
                              : ""
                      }`}
                      style={{
                        border: isActive
                          ? "none"
                          : `1px solid ${isHop ? "rgba(74,139,127,0.2)" : isXung ? "rgba(192,96,96,0.2)" : "var(--ls-border-soft)"}`,
                      }}
                    >
                      <div className="text-xl">{conGiapList[i].split(" ")[0]}</div>
                      <div className="text-text-mid mt-0.5 text-xs">{chi}</div>
                    </div>
                  );
                })}
              </div>
              <div className="text-text-soft mt-4 flex items-center gap-4 text-xs">
                <span className="flex items-center gap-1.5">
                  <span className="bg-warm-amber/20 ring-warm-amber h-3 w-3 rounded-sm ring-1" />{" "}
                  Tuổi của bạn
                </span>
                <span className="flex items-center gap-1.5">
                  <span className="bg-jade-teal/20 h-3 w-3 rounded-sm" /> Tam hợp
                </span>
                <span className="flex items-center gap-1.5">
                  <span className="bg-danger/20 h-3 w-3 rounded-sm" /> Tương xung
                </span>
              </div>
            </div>
          </div>
        )}

        {/* Initial state hint */}
        {!result && (
          <div className="text-text-soft py-16 text-center">
            <div
              className="mb-4 inline-flex h-16 w-16 items-center justify-center rounded-2xl"
              style={{
                background: "rgba(168,85,247,0.08)",
                border: "1px solid rgba(168,85,247,0.15)",
              }}
            >
              <Wand2 className="h-8 w-8 text-purple-400" />
            </div>
            <p className="text-sm">Nhập năm sinh ở trên để xem tử vi giản lược</p>
          </div>
        )}

        {/* ══════════════════════════════════════
            Tử Vi AI — Bát Tự
           ══════════════════════════════════════ */}
        <div className="mt-12">
          <div className="mb-2 flex items-center gap-2 opacity-40">
            <span className="h-px flex-1" style={{ background: "var(--ls-border-warm)" }} />
            <span className="text-warm-gold text-[11px] font-[var(--font-noto)]">✦</span>
            <span className="h-px flex-1" style={{ background: "var(--ls-border-warm)" }} />
          </div>
          {/* Promo banner + inline form */}
          <div
            className="mb-5 flex items-center justify-between rounded-2xl px-5 py-4"
            style={{
              background: "linear-gradient(135deg, rgba(168,85,247,0.06), rgba(99,102,241,0.06))",
              border: "1px solid rgba(168,85,247,0.15)",
            }}
          >
            <div className="flex items-center gap-3">
              <div
                className="flex h-9 w-9 items-center justify-center rounded-xl"
                style={{ background: "linear-gradient(135deg, #a855f7, #6366f1)" }}
              >
                <Bot className="h-4.5 w-4.5 text-white" />
              </div>
              <div>
                <p className="text-text-dark text-sm font-semibold">Tử Vi AI — Phân Tích Bát Tự</p>
                <p className="text-text-soft text-xs">Luận giải tứ trụ, ngũ hành bằng trí tuệ nhân tạo</p>
              </div>
            </div>
            <Link
              href="/tu-vi-ai"
              className="rounded-xl px-4 py-2 text-xs font-medium text-white transition-all hover:opacity-90"
              style={{ background: "linear-gradient(135deg, #a855f7, #6366f1)" }}
            >
              Mở trang đầy đủ →
            </Link>
          </div>
          <AIHoroscopeForm />
        </div>

        {/* ══════════════════════════════════════
            Tử Vi Hàng Ngày (12 Con Giáp)
           ══════════════════════════════════════ */}
        <div className="mt-12">
          <div className="mb-2 flex items-center gap-2 opacity-40">
            <span className="h-px flex-1" style={{ background: "var(--ls-border-warm)" }} />
            <span className="text-warm-gold text-[11px] font-[var(--font-noto)]">✦</span>
            <span className="h-px flex-1" style={{ background: "var(--ls-border-warm)" }} />
          </div>
          <HoroscopeWidget />
        </div>

        {/* ══════════════════════════════════════
            Tra Cứu Tuổi Âm Lịch
           ══════════════════════════════════════ */}
        <div className="mt-12">
          <div className="mb-2 flex items-center gap-2 opacity-40">
            <span className="h-px flex-1" style={{ background: "var(--ls-border-warm)" }} />
            <span className="text-warm-gold text-[11px] font-[var(--font-noto)]">✦</span>
            <span className="h-px flex-1" style={{ background: "var(--ls-border-warm)" }} />
          </div>
          <div
            className="rounded-2xl p-6 backdrop-blur-[14px]"
            style={{
              background: "var(--ls-card-bg-strong)",
              border: "1px solid var(--ls-border-warm)",
              boxShadow: "0 4px 24px var(--ls-shadow-warm)",
            }}
          >
            <LunarAgeCalculator />
          </div>
        </div>
      </div>
    </>
  );
}
