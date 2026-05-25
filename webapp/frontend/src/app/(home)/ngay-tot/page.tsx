"use client";

import { useState } from "react";
import {
  Star,
  BookOpen,
  CheckCircle2,
  Sparkles as SparklesIcon,
  BarChart3,
  Clock,
} from "lucide-react";
import { BackgroundLayer } from "@/components/lichso/BackgroundLayer";
import { GoodDaysTab } from "@/components/lichso/GoodDaysTab";
import { GoodDayPurposeTab } from "@/components/lichso/GoodDayPurposeTab";

export default function NgayTotPage() {
  return (
    <>
      <BackgroundLayer />
      <div className="relative z-[1] mx-auto max-w-[1180px] px-4 pb-16 sm:px-7">
        {/* Page header */}
        <div className="pt-10 pb-8 text-center">
          <div
            className="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-2xl"
            style={{
              background: "linear-gradient(135deg, var(--jade-teal), #4ade80)",
              boxShadow: "0 4px 16px rgba(74,139,127,0.25)",
            }}
          >
            <Star className="h-6 w-6 text-white" />
          </div>
          <h1 className="text-text-dark mb-3 text-3xl font-[var(--font-lora)] font-semibold">
            Ngày Tốt Trong Tháng
          </h1>
          <p className="text-text-soft mx-auto max-w-lg text-sm">
            Danh sách các ngày tốt, phù hợp để xuất hành, khai trương, cưới hỏi, xây dựng và các
            việc trọng đại.
          </p>
        </div>

        {/* Good Days Tab (reuse component) */}
        <GoodDaysTab />

        {/* ══════════════════════════════════════
            Xem Ngày Tốt Theo Mục Đích
           ══════════════════════════════════════ */}
        <div className="mt-10">
          <div className="mb-6 flex items-center gap-2 opacity-40">
            <span className="h-px flex-1" style={{ background: "var(--ls-border-warm)" }} />
            <span className="text-warm-gold text-[11px] font-[var(--font-noto)]">✦</span>
            <span className="h-px flex-1" style={{ background: "var(--ls-border-warm)" }} />
          </div>
          <div className="mb-6 text-center">
            <h2 className="text-text-dark mb-2 text-xl font-[var(--font-lora)] font-semibold">
              Xem Ngày Tốt Theo Mục Đích
            </h2>
            <p className="text-text-soft mx-auto max-w-lg text-sm">
              Chọn mục đích cụ thể để tìm ngày tốt phù hợp nhất: cưới hỏi, động thổ, khai trương,
              xuất hành...
            </p>
          </div>
          <GoodDayPurposeTab />
        </div>

        {/* Info section */}
        <div
          className="mt-8 rounded-2xl p-6 backdrop-blur-[14px]"
          style={{
            background: "var(--ls-card-bg)",
            border: "1px solid var(--ls-border-soft)",
          }}
        >
          <h3 className="text-text-dark mb-4 flex items-center gap-2 text-base font-[var(--font-lora)]">
            <BookOpen className="text-warm-amber h-4 w-4" />
            Cách đánh giá ngày tốt xấu
          </h3>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="text-text-mid space-y-2 text-sm">
              <p className="text-text-dark flex items-center gap-1.5 font-medium">
                <CheckCircle2 className="text-jade-teal h-3.5 w-3.5" />
                Tiêu chí đánh giá:
              </p>
              <ul className="space-y-1.5">
                <li className="flex items-start gap-2">
                  <SparklesIcon className="text-jade-teal mt-0.5 h-3 w-3 shrink-0" />
                  <span>
                    <strong>Trực ngày</strong> — 12 Trực (Kiến, Trừ, Mãn, Bình...)
                  </span>
                </li>
                <li className="flex items-start gap-2">
                  <SparklesIcon className="text-jade-teal mt-0.5 h-3 w-3 shrink-0" />
                  <span>
                    <strong>Sao chiếu mệnh</strong> — 28 Sao (Thái Dương, Thái Âm...)
                  </span>
                </li>
                <li className="flex items-start gap-2">
                  <SparklesIcon className="text-jade-teal mt-0.5 h-3 w-3 shrink-0" />
                  <span>
                    <strong>Can Chi ngày</strong> — Ngũ hành, Âm dương
                  </span>
                </li>
                <li className="flex items-start gap-2">
                  <SparklesIcon className="text-jade-teal mt-0.5 h-3 w-3 shrink-0" />
                  <span>
                    <strong>Giờ hoàng đạo</strong> — Số giờ tốt trong ngày
                  </span>
                </li>
              </ul>
            </div>
            <div className="text-text-mid space-y-2 text-sm">
              <p className="text-text-dark flex items-center gap-1.5 font-medium">
                <BarChart3 className="text-warm-amber h-3.5 w-3.5" />
                Thang điểm:
              </p>
              <ul className="space-y-1.5">
                <li className="flex items-center gap-2">
                  <span className="bg-jade-teal inline-block h-3 w-3 rounded-full" />
                  <span>
                    <strong>85 – 100:</strong> Rất tốt — Đại cát đại lợi
                  </span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="bg-jade-soft inline-block h-3 w-3 rounded-full" />
                  <span>
                    <strong>70 – 84:</strong> Tốt — Phù hợp các việc lớn
                  </span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="bg-warm-amber inline-block h-3 w-3 rounded-full" />
                  <span>
                    <strong>40 – 69:</strong> Bình thường — Cần cân nhắc
                  </span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="bg-danger inline-block h-3 w-3 rounded-full" />
                  <span>
                    <strong>0 – 39:</strong> Không tốt — Nên tránh
                  </span>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
