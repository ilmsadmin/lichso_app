"use client";

import type { BatTuInfo, NguHanhBalance } from "@/types/ai";

// ─── 12 Cung Tử Vi ────────────────────────────────────────────────────────────
// Thứ tự 12 cung theo vị trí lá số truyền thống (đọc từ Dần, ngược chiều kim đồng hồ)
const TWELVE_CUNG = [
  "Mệnh",    // 0
  "Phụ Mẫu", // 1
  "Phúc Đức",// 2
  "Điền Trạch",// 3
  "Quan Lộc",// 4
  "Nô Bộc",  // 5
  "Thiên Di", // 6
  "Tật Ách",  // 7
  "Tài Bạch", // 8
  "Tử Tức",  // 9
  "Phu Thê",  // 10
  "Huynh Đệ", // 11
];

// 12 Địa Chi theo thứ tự
const DIA_CHI = ["Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi"];

// Ngũ hành của từng địa chi
const DIA_CHI_NGU_HANH: Record<string, string> = {
  Tý: "Thuỷ", Sửu: "Thổ", Dần: "Mộc", Mão: "Mộc",
  Thìn: "Thổ", Tỵ: "Hoả", Ngọ: "Hoả", Mùi: "Thổ",
  Thân: "Kim", Dậu: "Kim", Tuất: "Thổ", Hợi: "Thuỷ",
};

// Màu ngũ hành
const NGU_HANH_COLOR: Record<string, { text: string; bg: string; border: string }> = {
  Mộc:  { text: "#16a34a", bg: "rgba(22,163,74,0.08)",  border: "rgba(22,163,74,0.25)"  },
  Hoả:  { text: "#dc2626", bg: "rgba(220,38,38,0.08)",  border: "rgba(220,38,38,0.25)"  },
  Thổ:  { text: "#d97706", bg: "rgba(217,119,6,0.08)",  border: "rgba(217,119,6,0.25)"  },
  Kim:   { text: "#6b7280", bg: "rgba(107,114,128,0.08)", border: "rgba(107,114,128,0.25)" },
  Thuỷ: { text: "#2563eb", bg: "rgba(37,99,235,0.08)",  border: "rgba(37,99,235,0.25)"  },
};

// Một số sao chính của từng cung dựa trên địa chi (đơn giản hoá)
// Trong thực tế cần tính toán phức tạp hơn, đây là version hiển thị demo
const SAO_THEO_CHI: Record<string, string[]> = {
  Tý:   ["Tử Vi", "Thiên Đồng"],
  Sửu:  ["Thái Âm", "Thiên Cơ"],
  Dần:  ["Tham Lang", "Cự Môn"],
  Mão:  ["Liêm Trinh", "Thiên Phủ"],
  Thìn: ["Vũ Khúc", "Thiên Tướng"],
  Tỵ:   ["Thái Dương", "Thiên Lương"],
  Ngọ:  ["Tử Vi", "Thất Sát"],
  Mùi:  ["Phá Quân", "Thiên Cơ"],
  Thân: ["Tham Lang", "Thái Âm"],
  Dậu:  ["Vũ Khúc", "Cự Môn"],
  Tuất: ["Thái Dương", "Thiên Đồng"],
  Hợi:  ["Liêm Trinh", "Thiên Phủ"],
};

// ─── Tính cung Mệnh từ tháng và giờ sinh ─────────────────────────────────────
// Công thức đơn giản: (14 - tháng âm - giờ_chi_index + 12) % 12
// Cung Mệnh bắt đầu từ Dần (index 2 trong DIA_CHI)
function tinhCungMenh(birthMonth: number, birthHour?: number): number {
  // Giờ sinh → địa chi giờ (index trong DIA_CHI)
  // Giờ Tý: 23-1h = 0, Sửu: 1-3h = 1, ...
  const hourIdx = birthHour !== undefined
    ? Math.floor(((birthHour + 1) % 24) / 2)
    : 0;
  // Cung mệnh: (14 - tháng - giờ chi) % 12, tính từ Dần (2)
  const menhChi = ((14 - birthMonth - hourIdx) % 12 + 12) % 12;
  // menhChi 0 = Dần → index 2 trong DIA_CHI
  return (menhChi + 2) % 12;
}

// ─── Bố cục 4×4 → 12 ô viền + 4 ô giữa ─────────────────────────────────────
// Lưới 4 cột × 3 hàng, 12 ô xung quanh theo thứ tự cung
// Layout theo tuvi.vn: hàng trên trái→phải: cung 3,4,5,6
//                      cột trái: cung 2,1,0    cột phải: cung 7,8,9
//                      hàng dưới phải→trái: cung 10,11,12...
//
// Theo ảnh mẫu: 4 cột × 4 hàng, 12 ô ngoài + 4 ô giữa (info)
// Thứ tự cung theo địa chi bắt đầu từ Dần ở góc dưới trái, ngược chiều kim đồng hồ

// Mapping vị trí grid [row, col] cho 12 ô (0-based, 4×4 grid)
// Dần ở góc dưới trái, đi ngược chiều kim đồng hồ (như ảnh tuvi.vn)
const GRID_POSITIONS: [number, number][] = [
  [3, 0], // Dần  (0)  - góc dưới trái
  [3, 1], // Mão  (1)  - dưới, 2
  [3, 2], // Thìn (2)  - dưới, 3
  [3, 3], // Tỵ   (3)  - góc dưới phải
  [2, 3], // Ngọ  (4)  - phải, dưới
  [1, 3], // Mùi  (5)  - phải, giữa
  [0, 3], // Thân (6)  - góc trên phải
  [0, 2], // Dậu  (7)  - trên, 3
  [0, 1], // Tuất (8)  - trên, 2
  [0, 0], // Hợi  (9)  - góc trên trái
  [1, 0], // Tý   (10) - trái, giữa
  [2, 0], // Sửu  (11) - trái, dưới
];

interface CungData {
  diaChi: string;
  cungName: string;
  sao: string[];
  nguHanh: string;
  chiSo?: number; // cung số (1-12)
}

function buildCungData(
  menhChiIndex: number, // index trong DIA_CHI của cung Mệnh
): CungData[] {
  return GRID_POSITIONS.map((_, gridIdx) => {
    // Địa chi tại vị trí này
    // gridIdx 0 = Dần = DIA_CHI index 2
    const diaChiIdx = (2 + gridIdx) % 12;
    const diaChi = DIA_CHI[diaChiIdx];

    // Cung nào nằm tại đây dựa trên khoảng cách từ Mệnh
    const cungOffset = (diaChiIdx - menhChiIndex + 12) % 12;
    const cungName = TWELVE_CUNG[cungOffset];

    return {
      diaChi,
      cungName,
      sao: SAO_THEO_CHI[diaChi] ?? [],
      nguHanh: DIA_CHI_NGU_HANH[diaChi] ?? "",
      chiSo: gridIdx + 1,
    };
  });
}

// ─── Component một ô cung ─────────────────────────────────────────────────────
function CungCell({
  data,
  isMenh,
  size,
}: {
  data: CungData;
  isMenh: boolean;
  size: "sm" | "md";
}) {
  const nh = NGU_HANH_COLOR[data.nguHanh] ?? { text: "#666", bg: "rgba(0,0,0,0.04)", border: "rgba(0,0,0,0.1)" };
  const cellSize = size === "sm" ? { minHeight: 90 } : { minHeight: 110 };

  return (
    <div
      style={{
        ...cellSize,
        padding: size === "sm" ? "8px 6px" : "10px 8px",
        background: isMenh
          ? "linear-gradient(135deg,rgba(168,85,247,0.06),rgba(99,102,241,0.04))"
          : "var(--ls-card-bg-strong)",
        border: isMenh
          ? "1.5px solid rgba(168,85,247,0.35)"
          : "1px solid var(--ls-border-soft)",
        display: "flex",
        flexDirection: "column",
        gap: 3,
        position: "relative",
        overflow: "hidden",
      }}
    >
      {/* Tên cung */}
      <div style={{
        fontSize: size === "sm" ? 9 : 10,
        fontWeight: 700,
        color: isMenh ? "#7c3aed" : "var(--ls-text-soft)",
        textTransform: "uppercase",
        letterSpacing: "0.04em",
        lineHeight: 1,
      }}>
        {isMenh && <span style={{ marginRight: 2 }}>⊛</span>}
        {data.cungName}
      </div>

      {/* Địa chi + ngũ hành */}
      <div style={{ display: "flex", alignItems: "center", gap: 4, flexWrap: "wrap" }}>
        <span style={{
          fontSize: size === "sm" ? 16 : 18,
          fontWeight: 800,
          color: "var(--ls-text-dark)",
          fontFamily: "var(--font-lora)",
          lineHeight: 1,
        }}>
          {data.diaChi}
        </span>
        <span style={{
          fontSize: 9,
          fontWeight: 700,
          padding: "1px 5px",
          borderRadius: 4,
          background: nh.bg,
          color: nh.text,
          border: `1px solid ${nh.border}`,
          lineHeight: 1.4,
        }}>
          {data.nguHanh}
        </span>
      </div>

      {/* Sao */}
      <div style={{ display: "flex", flexDirection: "column", gap: 1, marginTop: 2 }}>
        {data.sao.slice(0, 2).map((s) => (
          <span key={s} style={{
            fontSize: size === "sm" ? 9 : 10,
            color: "var(--ls-text-mid)",
            fontWeight: 500,
            lineHeight: 1.3,
          }}>
            {s}
          </span>
        ))}
      </div>
    </div>
  );
}

// ─── Ô giữa: Thông tin tứ trụ + ngũ hành ─────────────────────────────────────
function CenterInfo({
  batTu,
  nguHanh,
  name,
  birthDay,
  birthMonth,
  birthYear,
  birthHour,
  gender,
  size,
}: {
  batTu: BatTuInfo;
  nguHanh: NguHanhBalance;
  name: string;
  birthDay: number;
  birthMonth: number;
  birthYear: number;
  birthHour?: number;
  gender: "male" | "female";
  size: "sm" | "md";
}) {
  const PILLARS = [
    { label: "Năm", p: batTu.year_pillar },
    { label: "Tháng", p: batTu.month_pillar },
    { label: "Ngày", p: batTu.day_pillar },
    { label: "Giờ", p: batTu.hour_pillar },
  ];

  const ELEMENT_BARS: { key: keyof NguHanhBalance; label: string }[] = [
    { key: "Kim",  label: "Kim" },
    { key: "Moc",  label: "Mộc" },
    { key: "Thuy", label: "Thuỷ" },
    { key: "Hoa",  label: "Hoả" },
    { key: "Tho",  label: "Thổ" },
  ];

  const nhKeys: Array<keyof NguHanhBalance> = ["Kim", "Moc", "Thuy", "Hoa", "Tho"];
  const total = nhKeys.reduce((s, k) => s + ((nguHanh[k] as number) || 0), 0) || 1;

  const fs = size === "sm";

  return (
    <div style={{
      gridColumn: "2 / 4",
      gridRow: "2 / 4",
      padding: fs ? "12px 10px" : "16px 14px",
      background: "linear-gradient(135deg,rgba(196,120,58,0.04),rgba(61,128,110,0.04))",
      border: "1px solid var(--ls-border-warm)",
      display: "flex",
      flexDirection: "column",
      gap: fs ? 8 : 10,
      justifyContent: "center",
    }}>
      {/* Tiêu đề */}
      <div style={{ textAlign: "center" }}>
        <div style={{
          fontSize: fs ? 11 : 13,
          fontWeight: 800,
          color: "var(--ls-text-dark)",
          fontFamily: "var(--font-lora)",
          marginBottom: 2,
        }}>
          {name || "Lá Số Tử Vi"}
        </div>
        <div style={{ fontSize: fs ? 9 : 10, color: "var(--ls-text-soft)" }}>
          {birthDay}/{birthMonth}/{birthYear}
          {birthHour !== undefined ? ` · ${birthHour}h` : ""}
          {" · "}{gender === "male" ? "♂ Nam" : "♀ Nữ"}
        </div>
      </div>

      {/* Tứ trụ */}
      <div>
        <div style={{
          fontSize: fs ? 8 : 9,
          fontWeight: 700,
          color: "var(--ls-text-muted)",
          textTransform: "uppercase",
          letterSpacing: "0.08em",
          marginBottom: 4,
          textAlign: "center",
        }}>
          Tứ Trụ (Bát Tự)
        </div>
        <div style={{
          display: "grid",
          gridTemplateColumns: "repeat(4, 1fr)",
          gap: 3,
        }}>
          {PILLARS.map(({ label, p }) => {
            const nh = NGU_HANH_COLOR[p?.element ?? ""] ?? { text: "#999", bg: "rgba(0,0,0,0.04)", border: "transparent" };
            return (
              <div key={label} style={{
                textAlign: "center",
                padding: "4px 2px",
                background: nh.bg,
                borderRadius: 6,
                border: `1px solid ${nh.border}`,
              }}>
                <div style={{ fontSize: fs ? 7 : 8, color: "var(--ls-text-muted)", fontWeight: 600, marginBottom: 2 }}>
                  {label}
                </div>
                <div style={{ fontSize: fs ? 12 : 14, fontWeight: 800, color: "var(--ls-text-dark)", lineHeight: 1, fontFamily: "var(--font-lora)" }}>
                  {p?.heavenly_stem ?? "—"}
                </div>
                <div style={{ fontSize: fs ? 11 : 13, fontWeight: 600, color: nh.text, lineHeight: 1, marginTop: 1 }}>
                  {p?.earthly_branch ?? "—"}
                </div>
                <div style={{ fontSize: fs ? 7 : 8, color: nh.text, fontWeight: 700, marginTop: 2 }}>
                  {p?.element ?? ""}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Ngũ hành bars */}
      <div>
        <div style={{
          fontSize: fs ? 8 : 9,
          fontWeight: 700,
          color: "var(--ls-text-muted)",
          textTransform: "uppercase",
          letterSpacing: "0.08em",
          marginBottom: 4,
          textAlign: "center",
        }}>
          Ngũ Hành
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 3 }}>
          {ELEMENT_BARS.map(({ key, label }) => {
            const val = (nguHanh[key] as number) ?? 0;
            const pct = Math.round((val / total) * 100);
            const nh = NGU_HANH_COLOR[label] ?? { text: "#999", bg: "rgba(0,0,0,0.04)", border: "transparent" };
            const isStrongest = nguHanh.strongest === label || nguHanh.strongest === key;
            const isWeakest = nguHanh.weakest === label || nguHanh.weakest === key;
            return (
              <div key={key} style={{ display: "flex", alignItems: "center", gap: 4 }}>
                <span style={{ width: fs ? 22 : 26, fontSize: fs ? 8 : 9, color: nh.text, fontWeight: 700, flexShrink: 0 }}>
                  {label}
                </span>
                <div style={{ flex: 1, height: 4, borderRadius: 2, background: "rgba(0,0,0,0.06)", overflow: "hidden" }}>
                  <div style={{
                    height: "100%",
                    borderRadius: 2,
                    width: `${Math.max(pct, val > 0 ? 8 : 0)}%`,
                    background: nh.text,
                    transition: "width 1s ease",
                  }} />
                </div>
                <span style={{ width: 12, fontSize: fs ? 8 : 9, fontWeight: 700, color: "var(--ls-text-dark)", textAlign: "right", flexShrink: 0 }}>
                  {val}
                </span>
                {isStrongest && <span style={{ fontSize: fs ? 7 : 8, color: "#d97706", fontWeight: 800 }}>↑</span>}
                {isWeakest && !isStrongest && <span style={{ fontSize: fs ? 7 : 8, color: "#9ca3af", fontWeight: 800 }}>↓</span>}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

// ─── Main TuViChart ───────────────────────────────────────────────────────────
interface TuViChartProps {
  batTu: BatTuInfo;
  nguHanh: NguHanhBalance;
  name: string;
  birthDay: number;
  birthMonth: number;
  birthYear: number;
  birthHour?: number;
  gender: "male" | "female";
}

export function TuViChart({
  batTu,
  nguHanh,
  name,
  birthDay,
  birthMonth,
  birthYear,
  birthHour,
  gender,
}: TuViChartProps) {
  // Tính cung Mệnh: index địa chi trong DIA_CHI
  const menhChiIdx = tinhCungMenh(birthMonth, birthHour);
  const cungData = buildCungData(menhChiIdx);

  // Responsive: dùng size sm trên mobile
  const size = "md";

  // Build grid: 4 cột × 4 hàng
  // 12 ô ngoài tại GRID_POSITIONS + 4 ô giữa (col 1-2, row 1-2) = center info
  const cells: (CungData | null)[][] = Array.from({ length: 4 }, () => Array(4).fill(null));
  GRID_POSITIONS.forEach(([row, col], i) => {
    cells[row][col] = cungData[i];
  });

  return (
    <div style={{
      background: "var(--ls-card-bg-strong)",
      border: "1.5px solid var(--ls-border-warm)",
      borderRadius: 16,
      overflow: "hidden",
      boxShadow: "0 4px 24px var(--ls-shadow-warm)",
    }}>
      {/* Header */}
      <div style={{
        padding: "10px 16px",
        background: "linear-gradient(90deg,rgba(196,120,58,0.08),rgba(61,128,110,0.08))",
        borderBottom: "1px solid var(--ls-border-warm)",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
      }}>
        <span style={{
          fontSize: 12,
          fontWeight: 800,
          color: "var(--ls-text-dark)",
          fontFamily: "var(--font-lora)",
          letterSpacing: "0.03em",
        }}>
          ☯ Lá Số Tử Vi
        </span>
        <span style={{ fontSize: 10, color: "var(--ls-text-soft)", fontWeight: 500 }}>
          {birthDay}/{birthMonth}/{birthYear}
          {birthHour !== undefined ? ` · Giờ ${birthHour}h` : " · Không rõ giờ"}
          {" · "}{gender === "male" ? "Nam" : "Nữ"}
        </span>
      </div>

      {/* Grid 4×4 */}
      <div style={{
        display: "grid",
        gridTemplateColumns: "repeat(4, 1fr)",
        gridTemplateRows: "repeat(4, auto)",
      }}>
        {cells.map((row, ri) =>
          row.map((cung, ci) => {
            // Ô trung tâm (2×2: row 1-2, col 1-2)
            if (ri === 1 && ci === 1) {
              return (
                <CenterInfo
                  key="center"
                  batTu={batTu}
                  nguHanh={nguHanh}
                  name={name}
                  birthDay={birthDay}
                  birthMonth={birthMonth}
                  birthYear={birthYear}
                  birthHour={birthHour}
                  gender={gender}
                  size={size}
                />
              );
            }
            // Bỏ qua 3 ô còn lại của center (span)
            if ((ri === 1 && ci === 2) || (ri === 2 && ci === 1) || (ri === 2 && ci === 2)) {
              return null;
            }

            if (!cung) return (
              <div key={`${ri}-${ci}`} style={{
                border: "1px solid var(--ls-border-soft)",
                background: "var(--ls-card-bg-strong)",
              }} />
            );

            const isMenh = cung.cungName === "Mệnh";
            return (
              <CungCell
                key={`${ri}-${ci}`}
                data={cung}
                isMenh={isMenh}
                size={size}
              />
            );
          })
        )}
      </div>

      {/* Footer */}
      <div style={{
        padding: "8px 16px",
        borderTop: "1px solid var(--ls-border-soft)",
        display: "flex",
        gap: 12,
        flexWrap: "wrap",
        justifyContent: "center",
      }}>
        {(["Mộc","Hoả","Thổ","Kim","Thuỷ"] as const).map((nh) => {
          const c = NGU_HANH_COLOR[nh];
          return (
            <span key={nh} style={{ display: "flex", alignItems: "center", gap: 4, fontSize: 10 }}>
              <span style={{ width: 8, height: 8, borderRadius: "50%", background: c.text, display: "inline-block" }} />
              <span style={{ color: c.text, fontWeight: 700 }}>{nh}</span>
            </span>
          );
        })}
        <span style={{ fontSize: 10, color: "var(--ls-text-muted)", marginLeft: 4 }}>
          ⊛ = Cung Mệnh
        </span>
      </div>
    </div>
  );
}
