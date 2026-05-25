"use client";

import { useState, useCallback } from "react";
import {
  Compass,
  ClipboardList,
  Sparkles,
  MoonStar,
  Navigation,
  Clock,
  CheckCircle2,
  XCircle,
} from "lucide-react";
import { useCalendarToday } from "@/hooks/useCalendar";
import { BackgroundLayer } from "@/components/lichso/BackgroundLayer";
import { DayDetailModal } from "@/components/lichso/DayDetailModal";
import { Skeleton } from "@/components/ui/skeleton";

const COMPASS_DIRECTIONS: Record<string, { angle: number; x: number; y: number }> = {
  Bắc: { angle: 0, x: 150, y: 30 },
  "Đông Bắc": { angle: 45, x: 240, y: 60 },
  Đông: { angle: 90, x: 270, y: 150 },
  "Đông Nam": { angle: 135, x: 240, y: 240 },
  Nam: { angle: 180, x: 150, y: 270 },
  "Tây Nam": { angle: 225, x: 60, y: 240 },
  Tây: { angle: 270, x: 30, y: 150 },
  "Tây Bắc": { angle: 315, x: 60, y: 60 },
};

export default function PhongThuyPage() {
  const { data, isLoading } = useCalendarToday();
  const [selectedDate, setSelectedDate] = useState<string | null>(null);

  const today = data
    ? `${data.solar_year}-${String(data.solar_month).padStart(2, "0")}-${String(data.solar_day).padStart(2, "0")}`
    : null;

  return (
    <>
      <BackgroundLayer />
      <div className="relative z-[1] mx-auto max-w-[1180px] px-4 pb-16 sm:px-7">
        {/* Page header */}
        <div className="pt-10 pb-8 text-center">
          <div
            className="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-2xl"
            style={{
              background: "linear-gradient(135deg, #6366f1, #8b5cf6)",
              boxShadow: "0 4px 16px rgba(99,102,241,0.25)",
            }}
          >
            <Compass className="h-6 w-6 text-white" />
          </div>
          <h1 className="text-text-dark mb-3 text-3xl font-[var(--font-lora)] font-semibold">
            Phong Thủy Hôm Nay
          </h1>
          <p className="text-text-soft mx-auto max-w-lg text-sm">
            Thông tin phong thủy chi tiết: hướng xuất hành, giờ hoàng đạo, trực ngày, sao chiếu
            mệnh, việc nên / không nên.
          </p>
        </div>

        {isLoading ? (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {Array.from({ length: 6 }).map((_, i) => (
              <div
                key={i}
                className="rounded-2xl p-6"
                style={{
                  background: "var(--ls-card-bg)",
                  border: "1px solid var(--ls-border-soft)",
                }}
              >
                <Skeleton className="mb-3 h-4 w-32" />
                <Skeleton className="mb-2 h-6 w-48" />
                <Skeleton className="h-3 w-40" />
              </div>
            ))}
          </div>
        ) : data ? (
          <div className="space-y-6">
            {/* Overview score */}
            <div
              className="rounded-2xl p-6 backdrop-blur-[14px]"
              style={{
                background: "var(--ls-card-bg-strong)",
                border: "1px solid var(--ls-border-warm)",
                boxShadow: "0 4px 24px var(--ls-shadow-warm)",
              }}
            >
              <div className="flex flex-col items-start gap-4 sm:flex-row sm:items-center">
                <div className="flex flex-1 items-center gap-4">
                  <div
                    className="flex h-20 w-20 items-center justify-center rounded-full text-2xl font-bold"
                    style={{
                      background: `conic-gradient(${data.phong_thuy.chi_so_ngay >= 70 ? "var(--jade-teal)" : data.phong_thuy.chi_so_ngay >= 40 ? "var(--warm-amber)" : "var(--ls-danger)"} ${data.phong_thuy.chi_so_ngay * 3.6}deg, rgba(0,0,0,0.08) 0deg)`,
                      color:
                        data.phong_thuy.chi_so_ngay >= 70
                          ? "var(--jade-teal)"
                          : data.phong_thuy.chi_so_ngay >= 40
                            ? "var(--warm-amber)"
                            : "var(--ls-danger)",
                    }}
                  >
                    <span className="flex h-16 w-16 items-center justify-center rounded-full bg-[var(--ls-card-bg-strong)] text-xl">
                      {data.phong_thuy.chi_so_ngay}
                    </span>
                  </div>
                  <div>
                    <h2 className="text-text-dark text-xl font-[var(--font-lora)]">
                      {data.phong_thuy.danh_gia}
                    </h2>
                    <p className="text-text-soft text-sm">
                      Chỉ số ngày {data.solar_day}/{data.solar_month}/{data.solar_year} —{" "}
                      {data.day_of_week}
                    </p>
                    <p className="text-text-muted-ls mt-1 text-xs">
                      {data.tu_tru.ngay.can_chi} · {data.phong_thuy.truc_ngay.name}
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => today && setSelectedDate(today)}
                  className="text-warm-amber hover:bg-warm-amber/10 rounded-xl px-4 py-2 text-sm transition-all"
                  style={{ border: "1px solid var(--ls-border-warm)" }}
                >
                  Xem chi tiết đầy đủ →
                </button>
              </div>
            </div>

            {/* Cards grid */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {/* Trực Ngày */}
              <InfoCard
                icon={<ClipboardList className="h-3.5 w-3.5" />}
                title="Trực Ngày"
                main={data.phong_thuy.truc_ngay.name}
                sub={data.phong_thuy.truc_ngay.danh_gia}
                desc={data.phong_thuy.truc_ngay.mo_ta}
              />

              {/* Sao Chiếu Mệnh */}
              <InfoCard
                icon={<Sparkles className="h-3.5 w-3.5" />}
                title="Sao Chiếu Mệnh"
                main={data.phong_thuy.sao_chieu.name}
                sub={data.phong_thuy.sao_chieu.tot_xau}
                desc={data.phong_thuy.sao_chieu.mo_ta}
              />

              {/* Pha Trăng */}
              <InfoCard
                icon={<MoonStar className="h-3.5 w-3.5" />}
                title="Pha Trăng"
                main={data.phong_thuy.moon_phase.desc}
                sub=""
                desc={`${data.lunar_day_name} · ${data.lunar_month_name}`}
              />

              {/* Hướng Xuất Hành */}
              <div
                className="rounded-2xl p-5 backdrop-blur-[14px] md:col-span-2 lg:col-span-1"
                style={{
                  background: "var(--ls-card-bg)",
                  border: "1px solid var(--ls-border-soft)",
                }}
              >
                <h3 className="text-text-soft mb-3 flex items-center gap-1.5 text-[11px] tracking-[2px] uppercase">
                  <Navigation className="h-3 w-3" /> Hướng Xuất Hành
                </h3>
                <div className="space-y-2 text-sm">
                  <div className="flex items-center gap-2">
                    <span className="text-warm-amber text-xs">Tài thần:</span>
                    <span className="text-text-dark font-medium">
                      {data.phong_thuy.huong_xuat_hanh.tai_than}
                    </span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-warm-amber text-xs">Hỷ thần:</span>
                    <span className="text-text-dark font-medium">
                      {data.phong_thuy.huong_xuat_hanh.hy_than}
                    </span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-danger text-xs">Hắc thần:</span>
                    <span className="text-text-dark font-medium">
                      {data.phong_thuy.huong_xuat_hanh.hac_than}
                    </span>
                  </div>
                  <div
                    className="mt-2 pt-2"
                    style={{ borderTop: "1px solid var(--ls-border-soft)" }}
                  >
                    <p className="text-text-soft mb-1 text-xs">Hướng tốt:</p>
                    <div className="flex flex-wrap gap-1.5">
                      {data.phong_thuy.huong_xuat_hanh.huong_tot.map((h) => (
                        <span
                          key={h}
                          className="bg-jade-teal/10 text-jade-teal border-jade-teal/20 rounded-full border px-2 py-0.5 text-xs"
                        >
                          {h}
                        </span>
                      ))}
                    </div>
                  </div>
                  <div>
                    <p className="text-text-soft mb-1 text-xs">Hướng xấu:</p>
                    <div className="flex flex-wrap gap-1.5">
                      {data.phong_thuy.huong_xuat_hanh.huong_xau.map((h) => (
                        <span
                          key={h}
                          className="bg-danger/10 text-danger border-danger/20 rounded-full border px-2 py-0.5 text-xs"
                        >
                          {h}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
              </div>

              {/* Giờ Hoàng Đạo */}
              <div
                className="rounded-2xl p-5 backdrop-blur-[14px] md:col-span-2"
                style={{
                  background: "var(--ls-card-bg)",
                  border: "1px solid var(--ls-border-soft)",
                }}
              >
                <h3 className="text-text-soft mb-3 flex items-center gap-1.5 text-[11px] tracking-[2px] uppercase">
                  <Clock className="h-3 w-3" /> Giờ Hoàng Đạo
                </h3>
                <div className="grid grid-cols-3 gap-2 sm:grid-cols-4 lg:grid-cols-6">
                  {data.gio_hoang_dao.map((gio) => (
                    <div
                      key={gio.name}
                      className={`rounded-xl p-2.5 text-center text-sm transition-all ${
                        gio.is_hoang_dao
                          ? "bg-jade-teal/10 border-jade-teal/25 text-jade-teal"
                          : "text-text-muted-ls border-transparent bg-transparent"
                      }`}
                      style={{ border: "1px solid" }}
                    >
                      <div className="font-medium">{gio.name}</div>
                      <div className="mt-0.5 text-[10px]">{gio.range}</div>
                      {gio.is_hoang_dao && <div className="mt-0.5 text-[9px]">✦ Hoàng Đạo</div>}
                    </div>
                  ))}
                </div>
              </div>

              {/* Việc Nên / Không Nên */}
              <div
                className="rounded-2xl p-5 backdrop-blur-[14px] md:col-span-2 lg:col-span-3"
                style={{
                  background: "var(--ls-card-bg)",
                  border: "1px solid var(--ls-border-soft)",
                }}
              >
                <div className="grid gap-6 sm:grid-cols-2">
                  <div>
                    <h3 className="text-jade-teal mb-3 flex items-center gap-1.5 text-[11px] tracking-[2px] uppercase">
                      <CheckCircle2 className="h-3 w-3" /> Việc Nên Làm
                    </h3>
                    <ul className="space-y-1.5">
                      {data.phong_thuy.viec_nen.map((v) => (
                        <li key={v} className="text-text-mid flex items-center gap-2 text-sm">
                          <span className="bg-jade-teal h-1.5 w-1.5 rounded-full" />
                          {v}
                        </li>
                      ))}
                    </ul>
                  </div>
                  <div>
                    <h3 className="text-danger mb-3 flex items-center gap-1.5 text-[11px] tracking-[2px] uppercase">
                      <XCircle className="h-3 w-3" /> Việc Không Nên
                    </h3>
                    <ul className="space-y-1.5">
                      {data.phong_thuy.viec_khong.map((v) => (
                        <li key={v} className="text-text-mid flex items-center gap-2 text-sm">
                          <span className="bg-danger h-1.5 w-1.5 rounded-full" />
                          {v}
                        </li>
                      ))}
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>
        ) : null}
      </div>

      {selectedDate && <DayDetailModal date={selectedDate} onClose={() => setSelectedDate(null)} />}
    </>
  );
}

function InfoCard({
  icon,
  title,
  main,
  sub,
  desc,
}: {
  icon: React.ReactNode;
  title: string;
  main: string;
  sub: string;
  desc: string;
}) {
  return (
    <div
      className="rounded-2xl p-5 backdrop-blur-[14px]"
      style={{
        background: "var(--ls-card-bg)",
        border: "1px solid var(--ls-border-soft)",
      }}
    >
      <h3 className="text-text-soft mb-2 flex items-center gap-1.5 text-[11px] tracking-[2px] uppercase">
        {icon} {title}
      </h3>
      <p className="text-text-dark text-lg font-[var(--font-lora)] font-medium">{main}</p>
      {sub && (
        <span
          className={`mt-1 inline-block rounded-full px-2 py-0.5 text-xs ${
            sub === "Tốt"
              ? "bg-jade-teal/10 text-jade-teal"
              : sub === "Xấu"
                ? "bg-danger/10 text-danger"
                : "bg-warm-amber/10 text-warm-amber"
          }`}
        >
          {sub}
        </span>
      )}
      <p className="text-text-soft mt-2 text-xs">{desc}</p>
    </div>
  );
}
