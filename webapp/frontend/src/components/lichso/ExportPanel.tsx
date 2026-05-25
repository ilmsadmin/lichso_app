"use client";

import { useState, useCallback } from "react";
import { getExportICalUrl, getExportTextUrl } from "@/services/bookmarkService";
import { toast } from "sonner";

export function ExportPanel() {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);

  const handleExportICal = useCallback(() => {
    const url = getExportICalUrl(year, month);
    window.open(url, "_blank");
    toast.success(`Đang tải lịch tháng ${month}/${year} (iCal)`);
  }, [year, month]);

  const handleExportText = useCallback(() => {
    const url = getExportTextUrl(year, month);
    window.open(url, "_blank");
    toast.success(`Đang tải lịch tháng ${month}/${year} (Text)`);
  }, [year, month]);

  const handlePrint = useCallback(() => {
    // Open a printable view
    const url = getExportTextUrl(year, month);
    const printWindow = window.open(url, "_blank");
    if (printWindow) {
      printWindow.addEventListener("load", () => {
        printWindow.print();
      });
    }
  }, [year, month]);

  return (
    <div>
      <div className="text-text-muted-ls mb-3 text-[10px] tracking-[2.5px] uppercase">
        📥 Xuất Lịch
      </div>

      {/* Month/Year selector */}
      <div className="mb-4 flex gap-2">
        <div className="flex-1">
          <label className="text-text-soft mb-1 block text-[10px]">Tháng</label>
          <select
            value={month}
            onChange={(e) => setMonth(Number(e.target.value))}
            className="w-full rounded-lg px-3 py-2 text-[12px] outline-none"
            style={{
              background: "var(--ls-card-bg)",
              border: "1px solid var(--ls-border-soft)",
              color: "var(--ls-text-dark)",
            }}
          >
            {Array.from({ length: 12 }, (_, i) => (
              <option key={i + 1} value={i + 1}>
                Tháng {i + 1}
              </option>
            ))}
          </select>
        </div>
        <div className="flex-1">
          <label className="text-text-soft mb-1 block text-[10px]">Năm</label>
          <input
            type="number"
            min={1900}
            max={2100}
            value={year}
            onChange={(e) => setYear(Number(e.target.value))}
            className="w-full rounded-lg px-3 py-2 text-[12px] outline-none"
            style={{
              background: "var(--ls-card-bg)",
              border: "1px solid var(--ls-border-soft)",
              color: "var(--ls-text-dark)",
            }}
          />
        </div>
      </div>

      {/* Export options */}
      <div className="space-y-2">
        <button
          onClick={handleExportICal}
          className="flex w-full items-center gap-3 rounded-xl p-3 transition-all hover:translate-x-[2px]"
          style={{
            background: "var(--ls-card-bg)",
            border: "1px solid var(--ls-border-soft)",
          }}
        >
          <div
            className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-lg"
            style={{ background: "rgba(74,139,127,0.1)" }}
          >
            📅
          </div>
          <div className="text-left">
            <div className="text-text-dark text-[13px] font-medium">Xuất iCal (.ics)</div>
            <div className="text-text-soft text-[11px]">
              Import vào Apple Calendar, Google Calendar, Outlook
            </div>
          </div>
        </button>

        <button
          onClick={handleExportText}
          className="flex w-full items-center gap-3 rounded-xl p-3 transition-all hover:translate-x-[2px]"
          style={{
            background: "var(--ls-card-bg)",
            border: "1px solid var(--ls-border-soft)",
          }}
        >
          <div
            className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-lg"
            style={{ background: "rgba(196,120,58,0.1)" }}
          >
            📄
          </div>
          <div className="text-left">
            <div className="text-text-dark text-[13px] font-medium">Xuất Text (.txt)</div>
            <div className="text-text-soft text-[11px]">Lịch dạng text thuần, dễ chia sẻ</div>
          </div>
        </button>

        <button
          onClick={handlePrint}
          className="flex w-full items-center gap-3 rounded-xl p-3 transition-all hover:translate-x-[2px]"
          style={{
            background: "var(--ls-card-bg)",
            border: "1px solid var(--ls-border-soft)",
          }}
        >
          <div
            className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-lg"
            style={{ background: "rgba(100,80,160,0.1)" }}
          >
            🖨️
          </div>
          <div className="text-left">
            <div className="text-text-dark text-[13px] font-medium">In lịch (PDF)</div>
            <div className="text-text-soft text-[11px]">Mở cửa sổ in, lưu thành PDF</div>
          </div>
        </button>
      </div>

      <div className="text-text-muted-ls mt-3 text-center text-[10px]">
        Lịch tháng {month}/{year} · Bao gồm Âm lịch & Phong Thuỷ
      </div>
    </div>
  );
}
