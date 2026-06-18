"use client";

import { useMemo } from "react";
import { ChevronLeft, ChevronRight, Share2, Bell, CalendarDays } from "lucide-react";
import { BookmarkButton } from "@/components/lichso/BookmarkButton";
import type { DayResponse } from "@/types/calendar";

interface DailyBlocSheetProps {
  data: DayResponse;
  onPrevDay?: () => void;
  onNextDay?: () => void;
  onToday?: () => void;
  isToday?: boolean;
}

const WEEKDAY_FULL = ["Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy"];

/**
 * Tờ lịch ngày — bloc truyền thống cách điệu.
 * Trung tâm của trang chủ: dải đỏ son, số ngày khổng lồ, đường xé răng cưa,
 * hoa văn trống đồng, kèm âm lịch · can chi · giờ hoàng đạo · việc nên/kỵ.
 */
export function DailyBlocSheet({
  data,
  onPrevDay,
  onNextDay,
  onToday,
  isToday = false,
}: DailyBlocSheetProps) {
  const dateStr = `${data.solar_year}-${String(data.solar_month).padStart(2, "0")}-${String(
    data.solar_day
  ).padStart(2, "0")}`;

  const isSunday = data.day_of_week_n === 0;
  const weekdayLabel = WEEKDAY_FULL[data.day_of_week_n] ?? data.day_of_week;

  const hoangDao = useMemo(
    () => data.gio_hoang_dao.filter((g) => g.is_hoang_dao),
    [data.gio_hoang_dao]
  );

  const huongTot = data.phong_thuy?.huong_xuat_hanh?.huong_tot ?? [];
  const vietNen = data.phong_thuy?.viec_nen ?? [];
  const vietKy = data.phong_thuy?.viec_khong ?? [];
  const danhGia = data.phong_thuy?.danh_gia ?? "";
  const isGoodDay = /tốt|hoàng đạo|cát/i.test(danhGia);

  return (
    <section
      className="relative mx-auto w-full overflow-hidden rounded-[26px]"
      style={{
        background: "var(--v2-bloc-paper)",
        boxShadow: "var(--v2-bloc-shadow)",
        border: "1px solid var(--v2-bloc-line)",
      }}
    >
      {/* ═══════ Dải đỏ son (header band) ═══════ */}
      <header
        className="relative overflow-hidden px-5 pt-4 pb-5 text-white sm:px-7"
        style={{ background: "var(--v2-bloc-header)" }}
      >
        {/* watermark trống đồng */}
        <div
          aria-hidden
          className="v2-trongdong pointer-events-none absolute -top-16 -right-12 h-56 w-56 rounded-full opacity-[0.12]"
          style={{ color: "var(--v2-bloc-gold)" }}
        />

        <div className="relative z-10 flex items-center justify-between gap-3">
          {/* Tháng + năm */}
          <div>
            <div
              className="font-playfair text-2xl leading-none font-bold sm:text-[28px]"
              style={{ color: "var(--v2-bloc-gold)" }}
            >
              Tháng {data.solar_month}
            </div>
            <div className="mt-1 text-[13px] font-medium tracking-wide text-white/70">
              Năm {data.solar_year} · Can chi {data.tu_tru.nam.can_chi}
            </div>
          </div>

          {/* Điều hướng ngày */}
          <div className="flex items-center gap-1.5">
            <NavBtn label="Ngày trước" onClick={onPrevDay}>
              <ChevronLeft className="h-4 w-4" />
            </NavBtn>
            {!isToday && (
              <button
                onClick={onToday}
                className="flex h-9 items-center gap-1.5 rounded-lg border border-white/15 bg-white/10 px-3 text-[12px] font-medium text-white/90 transition-all hover:bg-white/20"
              >
                <CalendarDays className="h-3.5 w-3.5" />
                Hôm nay
              </button>
            )}
            <NavBtn label="Ngày sau" onClick={onNextDay}>
              <ChevronRight className="h-4 w-4" />
            </NavBtn>
          </div>
        </div>

        {/* con giáp + ngũ hành chips */}
        <div className="relative z-10 mt-4 flex flex-wrap gap-2">
          <HeaderChip icon="🐎" text={`${data.tu_tru.nam.con_giap} · ${data.tu_tru.nam.chi}`} />
          <HeaderChip icon="🔥" text={`Ngũ hành ngày: ${data.tu_tru.ngay.ngu_hanh}`} />
          {data.tiet_khi?.current?.name && (
            <HeaderChip icon="🍃" text={`Tiết ${data.tiet_khi.current.name}`} />
          )}
        </div>
      </header>

      {/* ═══════ Đường xé răng cưa ═══════ */}
      <div className="v2-perforation" />

      {/* ═══════ Thân tờ lịch (paper) ═══════ */}
      <div
        className="relative px-5 pt-2 pb-6 sm:px-8"
        style={{ backgroundImage: "var(--v2-bloc-grain)" }}
      >
        {/* Số ngày khổng lồ */}
        <div className="flex flex-col items-center pt-3 text-center">
          <div
            className="text-[13px] font-bold tracking-[0.3em] uppercase"
            style={{ color: isSunday ? "var(--v2-bloc-sunday)" : "var(--v2-bloc-muted)" }}
          >
            {weekdayLabel}
          </div>
          <div
            className="font-playfair leading-[0.85] font-black tabular-nums"
            style={{
              fontSize: "clamp(110px, 26vw, 180px)",
              color: isSunday ? "var(--v2-bloc-sunday)" : "var(--v2-bloc-ink)",
              textShadow: "0 6px 22px rgba(110,20,34,0.18)",
            }}
          >
            {data.solar_day}
          </div>

          {/* Âm lịch */}
          <div className="mt-1 flex flex-col items-center gap-1">
            <div className="text-[15px] font-semibold" style={{ color: "var(--v2-bloc-weekday)" }}>
              Âm lịch: {data.lunar_day} {data.lunar_month_name}
              {data.is_leap_month ? " (nhuận)" : ""}
            </div>
            <div className="text-[13px]" style={{ color: "var(--v2-bloc-muted)" }}>
              Ngày {data.tu_tru.ngay.can_chi} · Tháng {data.tu_tru.thang.can_chi} · Năm{" "}
              {data.tu_tru.nam.can_chi}
            </div>
          </div>

          {/* Đánh giá ngày */}
          {danhGia && (
            <div
              className="mt-3 inline-flex items-center gap-1.5 rounded-full px-3.5 py-1 text-[12px] font-semibold"
              style={{
                background: isGoodDay ? "rgba(34,139,69,0.1)" : "var(--v2-bg-accent-soft)",
                color: isGoodDay ? "#1F9D55" : "var(--v2-text-accent)",
              }}
            >
              {isGoodDay ? "★" : "◆"} {danhGia}
            </div>
          )}
        </div>

        {/* ── Lưới thông tin can chi / phong thủy ── */}
        <div className="mt-6 grid grid-cols-2 gap-2.5 sm:grid-cols-4">
          <InfoTile label="Giờ hoàng đạo" value={hoangDao.map((g) => g.name).join(", ") || "—"} />
          <InfoTile
            label="Hướng xuất hành"
            value={
              huongTot.slice(0, 2).join(", ") || data.phong_thuy?.huong_xuat_hanh?.tai_than || "—"
            }
          />
          <InfoTile label="Trực ngày" value={data.phong_thuy?.truc_ngay?.name || "—"} />
          <InfoTile
            label="Sao chiếu mệnh"
            value={data.phong_thuy?.sao_chieu?.name || "—"}
            tone={/tốt|cát/i.test(data.phong_thuy?.sao_chieu?.tot_xau || "") ? "good" : "bad"}
          />
        </div>

        {/* ── Việc nên / Việc kỵ ── */}
        {(vietNen.length > 0 || vietKy.length > 0) && (
          <div className="mt-4 grid gap-3 sm:grid-cols-2">
            {vietNen.length > 0 && <ActivityBox title="Việc nên làm" tone="good" items={vietNen} />}
            {vietKy.length > 0 && <ActivityBox title="Việc nên tránh" tone="bad" items={vietKy} />}
          </div>
        )}

        {/* ── Hành động ── */}
        <div className="mt-5 flex items-center justify-center gap-2">
          <BookmarkButton date={dateStr} />
          <ActionBtn label="Nhắc nhở">
            <Bell className="h-4 w-4" />
          </ActionBtn>
          <ActionBtn label="Chia sẻ tờ lịch">
            <Share2 className="h-4 w-4" />
          </ActionBtn>
        </div>
      </div>
    </section>
  );
}

/* ─────────── sub-components ─────────── */

function NavBtn({
  children,
  onClick,
  label,
}: {
  children: React.ReactNode;
  onClick?: () => void;
  label: string;
}) {
  return (
    <button
      onClick={onClick}
      aria-label={label}
      className="flex h-9 w-9 items-center justify-center rounded-lg border border-white/15 bg-white/10 text-white/90 transition-all hover:-translate-y-0.5 hover:bg-white/20"
    >
      {children}
    </button>
  );
}

function HeaderChip({ icon, text }: { icon: string; text: string }) {
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full border border-white/10 bg-white/10 px-3 py-1 text-[12px] font-medium text-white/90 backdrop-blur-sm">
      <span>{icon}</span>
      {text}
    </span>
  );
}

function InfoTile({ label, value, tone }: { label: string; value: string; tone?: "good" | "bad" }) {
  return (
    <div
      className="rounded-xl border px-3 py-2.5"
      style={{
        background: "var(--v2-bloc-paper-alt)",
        borderColor: "var(--v2-bloc-line)",
      }}
    >
      <div
        className="text-[10px] font-semibold tracking-wider uppercase"
        style={{ color: "var(--v2-bloc-muted)" }}
      >
        {label}
      </div>
      <div
        className="mt-0.5 text-[13px] leading-snug font-semibold"
        style={{
          color:
            tone === "good"
              ? "#1F9D55"
              : tone === "bad"
                ? "var(--v2-text-accent)"
                : "var(--v2-bloc-weekday)",
        }}
      >
        {value}
      </div>
    </div>
  );
}

function ActivityBox({
  title,
  tone,
  items,
}: {
  title: string;
  tone: "good" | "bad";
  items: string[];
}) {
  const good = tone === "good";
  return (
    <div
      className="rounded-xl border p-3.5"
      style={{
        background: good ? "rgba(34,139,69,0.06)" : "var(--v2-bg-accent-soft)",
        borderColor: good ? "rgba(34,139,69,0.18)" : "rgba(155,35,53,0.18)",
      }}
    >
      <div
        className="mb-2 flex items-center gap-1.5 text-[12px] font-bold"
        style={{ color: good ? "#1F9D55" : "var(--v2-text-accent)" }}
      >
        {good ? "✓" : "✕"} {title}
      </div>
      <div className="flex flex-wrap gap-1.5">
        {items.slice(0, 8).map((it, i) => (
          <span
            key={i}
            className="rounded-md px-2 py-1 text-[12px]"
            style={{
              background: "var(--v2-bloc-paper)",
              color: "var(--v2-bloc-weekday)",
              border: "1px solid var(--v2-bloc-line)",
            }}
          >
            {it}
          </span>
        ))}
      </div>
    </div>
  );
}

function ActionBtn({ children, label }: { children: React.ReactNode; label: string }) {
  return (
    <button
      aria-label={label}
      title={label}
      className="flex h-[38px] w-[38px] items-center justify-center rounded-lg border transition-all hover:-translate-y-0.5"
      style={{
        borderColor: "var(--v2-bloc-line)",
        background: "var(--v2-bloc-paper-alt)",
        color: "var(--v2-text-accent)",
      }}
    >
      {children}
    </button>
  );
}
