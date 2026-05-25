"use client";

import type { DayResponse } from "@/types/calendar";

interface InfoPanelProps {
  data: DayResponse;
}

function InfoCard({
  title,
  value,
  sub,
  children,
}: {
  title: string;
  value: string;
  sub?: string;
  children?: React.ReactNode;
}) {
  return (
    <div
      className="rounded-[14px] px-4 py-3.5 backdrop-blur-[12px] transition-all duration-300 hover:translate-x-[3px]"
      style={{
        background: "var(--ls-card-bg)",
        border: "1px solid var(--ls-border-soft)",
        boxShadow: "0 2px 12px var(--ls-shadow-warm)",
      }}
      onMouseEnter={(e) => {
        const el = e.currentTarget;
        el.style.background = "var(--ls-card-bg-strong)";
        el.style.borderColor = "var(--ls-border-warm, rgba(196,120,58,0.18))";
        el.style.boxShadow = "0 4px 20px rgba(180,120,60,0.12)";
      }}
      onMouseLeave={(e) => {
        const el = e.currentTarget;
        el.style.background = "var(--ls-card-bg)";
        el.style.borderColor = "var(--ls-border-soft, rgba(196,120,58,0.10))";
        el.style.boxShadow = "0 2px 12px var(--ls-shadow-warm)";
      }}
    >
      <div className="text-text-muted-ls mb-1.5 text-[10px] tracking-[2.5px] uppercase">
        {title}
      </div>
      <div className="text-text-dark text-sm font-medium">{value}</div>
      {sub && <div className="text-text-soft mt-1 text-xs">{sub}</div>}
      {children}
    </div>
  );
}

export function InfoPanel({ data }: InfoPanelProps) {
  return (
    <div className="flex flex-col gap-3">
      {/* Trực Ngày & Giờ Hoàng Đạo */}
      <InfoCard
        title="✦ Trực Ngày & Giờ Hoàng Đạo"
        value={`${data.phong_thuy.truc_ngay.name} — ${data.phong_thuy.truc_ngay.danh_gia}`}
      >
        <div className="mt-2 flex flex-wrap gap-[5px]">
          {data.gio_hoang_dao.map((gio) => (
            <span
              key={gio.name}
              className={`rounded-xl border px-2 py-0.5 text-[11px] ${
                gio.is_hoang_dao
                  ? "bg-jade-teal/10 border-jade-teal/25 text-jade-teal font-medium"
                  : "bg-warm-amber/[0.07] border-warm-amber/[0.18] text-text-soft"
              }`}
            >
              {gio.name} ({gio.range})
            </span>
          ))}
        </div>
      </InfoCard>

      {/* Hướng Xuất Hành */}
      <InfoCard
        title="🧭 Hướng Xuất Hành Tốt"
        value={data.phong_thuy.huong_xuat_hanh.huong_tot.join(" · ")}
        sub={`Tài thần: ${data.phong_thuy.huong_xuat_hanh.tai_than} · Hỷ thần: ${data.phong_thuy.huong_xuat_hanh.hy_than}`}
      />

      {/* Kiêng Kỵ */}
      <InfoCard
        title="⚠ Kiêng Kỵ Hôm Nay"
        value={`${data.phong_thuy.huong_xuat_hanh.huong_xau.join(" · ")} · ${data.phong_thuy.sao_chieu.name}`}
        sub={data.phong_thuy.viec_khong.slice(0, 2).join(", ")}
      />

      {/* Tiết Khí & Sao */}
      <InfoCard
        title="🌿 Tiết Khí · Sao Chiếu Mệnh"
        value={`${data.tiet_khi.current.name} · ${data.phong_thuy.sao_chieu.name} ${data.phong_thuy.sao_chieu.tot_xau === "Tốt" ? "✦" : ""}`}
        sub={`Còn ${data.tiet_khi.days_left} ngày → ${data.tiet_khi.next.name}`}
      />
    </div>
  );
}
