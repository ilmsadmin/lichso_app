"use client";

import { useState } from "react";
import { useCalendarConvert } from "@/hooks/useCalendar";

export function ConvertTab() {
  const [mode, setMode] = useState<"solar2lunar" | "lunar2solar">("solar2lunar");
  const [day, setDay] = useState("");
  const [month, setMonth] = useState("");
  const [year, setYear] = useState("");
  const [leapMonth, setLeapMonth] = useState(false);
  const [doConvert, setDoConvert] = useState(false);

  const params = {
    day: parseInt(day) || 0,
    month: parseInt(month) || 0,
    year: parseInt(year) || 0,
    to_lunar: mode === "solar2lunar",
    leap_month: leapMonth,
  };

  const isValid =
    params.day >= 1 &&
    params.day <= 31 &&
    params.month >= 1 &&
    params.month <= 12 &&
    params.year >= 1900 &&
    params.year <= 2100;

  const { data, isLoading, error } = useCalendarConvert(params, doConvert && isValid);

  const handleConvert = () => {
    if (isValid) setDoConvert(true);
  };

  const handleReset = () => {
    setDay("");
    setMonth("");
    setYear("");
    setLeapMonth(false);
    setDoConvert(false);
  };

  const inputStyle = {
    background: "var(--ls-card-bg)",
    border: "1px solid var(--ls-border-warm)",
    color: "var(--ls-text-dark, #3D2E1A)",
  };

  return (
    <div className="mx-auto max-w-[600px] animate-[fadeUp_0.65s_ease-out_both]">
      {/* Header */}
      <div className="mb-5 flex items-center gap-2.5">
        <span
          className="h-4 w-1 rounded-sm"
          style={{
            background: "linear-gradient(to bottom, var(--warm-amber), var(--warm-gold))",
          }}
        />
        <span className="text-text-dark text-base font-[var(--font-lora)] tracking-wide">
          Đổi Lịch Âm / Dương
        </span>
      </div>

      {/* Mode toggle */}
      <div
        className="mb-6 flex rounded-xl p-1"
        style={{
          background: "rgba(196,120,58,0.06)",
          border: "1px solid var(--ls-border-soft)",
        }}
      >
        <button
          onClick={() => {
            setMode("solar2lunar");
            setDoConvert(false);
          }}
          className={`flex-1 rounded-lg py-2.5 text-[13px] font-medium transition-all ${
            mode === "solar2lunar" ? "text-white shadow-md" : "text-text-soft hover:text-text-mid"
          }`}
          style={
            mode === "solar2lunar"
              ? {
                  background: "linear-gradient(135deg, var(--warm-amber), var(--warm-gold))",
                  boxShadow: "0 2px 10px rgba(196,120,58,0.3)",
                }
              : {}
          }
        >
          Dương → Âm
        </button>
        <button
          onClick={() => {
            setMode("lunar2solar");
            setDoConvert(false);
          }}
          className={`flex-1 rounded-lg py-2.5 text-[13px] font-medium transition-all ${
            mode === "lunar2solar" ? "text-white shadow-md" : "text-text-soft hover:text-text-mid"
          }`}
          style={
            mode === "lunar2solar"
              ? {
                  background: "linear-gradient(135deg, var(--jade-teal), var(--jade-soft))",
                  boxShadow: "0 2px 10px rgba(74,139,127,0.3)",
                }
              : {}
          }
        >
          Âm → Dương
        </button>
      </div>

      {/* Input form */}
      <div
        className="mb-5 rounded-2xl p-6 backdrop-blur-[14px]"
        style={{
          background: "var(--ls-card-bg)",
          border: "1px solid var(--ls-border-soft)",
          boxShadow: "0 4px 20px var(--ls-shadow-warm)",
        }}
      >
        <div className="text-text-muted-ls mb-4 text-[10px] tracking-[2.5px] uppercase">
          {mode === "solar2lunar" ? "📅 Nhập Ngày Dương Lịch" : "🌙 Nhập Ngày Âm Lịch"}
        </div>

        <div className="mb-4 grid grid-cols-3 gap-3">
          <div>
            <label className="text-text-soft mb-1.5 block text-[11px] tracking-widest uppercase">
              Ngày
            </label>
            <input
              type="number"
              value={day}
              onChange={(e) => {
                setDay(e.target.value);
                setDoConvert(false);
              }}
              placeholder="DD"
              min={1}
              max={31}
              className="focus:ring-warm-amber/30 w-full rounded-lg px-3 py-2.5 text-center text-[15px] font-[var(--font-lora)] font-medium transition-all outline-none focus:ring-2"
              style={inputStyle}
            />
          </div>
          <div>
            <label className="text-text-soft mb-1.5 block text-[11px] tracking-widest uppercase">
              Tháng
            </label>
            <input
              type="number"
              value={month}
              onChange={(e) => {
                setMonth(e.target.value);
                setDoConvert(false);
              }}
              placeholder="MM"
              min={1}
              max={12}
              className="focus:ring-warm-amber/30 w-full rounded-lg px-3 py-2.5 text-center text-[15px] font-[var(--font-lora)] font-medium transition-all outline-none focus:ring-2"
              style={inputStyle}
            />
          </div>
          <div>
            <label className="text-text-soft mb-1.5 block text-[11px] tracking-widest uppercase">
              Năm
            </label>
            <input
              type="number"
              value={year}
              onChange={(e) => {
                setYear(e.target.value);
                setDoConvert(false);
              }}
              placeholder="YYYY"
              min={1900}
              max={2100}
              className="focus:ring-warm-amber/30 w-full rounded-lg px-3 py-2.5 text-center text-[15px] font-[var(--font-lora)] font-medium transition-all outline-none focus:ring-2"
              style={inputStyle}
            />
          </div>
        </div>

        {/* Leap month checkbox (only for lunar input) */}
        {mode === "lunar2solar" && (
          <label className="mb-4 flex cursor-pointer items-center gap-2">
            <input
              type="checkbox"
              checked={leapMonth}
              onChange={(e) => {
                setLeapMonth(e.target.checked);
                setDoConvert(false);
              }}
              className="border-warm-amber/40 text-warm-amber focus:ring-warm-amber/30 h-4 w-4 rounded"
            />
            <span className="text-text-mid text-[13px]">Tháng nhuận</span>
          </label>
        )}

        {/* Actions */}
        <div className="flex gap-3">
          <button
            onClick={handleConvert}
            disabled={!isValid || isLoading}
            className="flex-1 rounded-lg py-2.5 text-[13px] font-medium tracking-wide text-white transition-all hover:-translate-y-px disabled:translate-y-0 disabled:opacity-50"
            style={{
              background:
                mode === "solar2lunar"
                  ? "linear-gradient(135deg, var(--warm-amber), var(--warm-gold))"
                  : "linear-gradient(135deg, var(--jade-teal), var(--jade-soft))",
              boxShadow: "0 4px 14px rgba(196,120,58,0.25)",
            }}
          >
            {isLoading ? "Đang chuyển đổi..." : "Chuyển đổi ▸"}
          </button>
          <button
            onClick={handleReset}
            className="text-text-soft hover:bg-warm-amber/5 rounded-lg px-5 py-2.5 text-[13px] transition-all"
            style={{
              border: "1px solid var(--ls-border-soft)",
            }}
          >
            Xoá
          </button>
        </div>
      </div>

      {/* Error */}
      {error && doConvert && (
        <div
          className="text-danger mb-5 rounded-xl p-4 text-center text-sm"
          style={{
            background: "rgba(192,96,96,0.06)",
            border: "1px solid rgba(192,96,96,0.2)",
          }}
        >
          Không thể chuyển đổi. Vui lòng kiểm tra lại ngày nhập.
        </div>
      )}

      {/* Result */}
      {data && doConvert && (
        <div
          className="overflow-hidden rounded-2xl backdrop-blur-[14px]"
          style={{
            background: "var(--ls-card-bg)",
            border: "1px solid var(--ls-border-soft)",
            boxShadow: "0 4px 24px var(--ls-shadow-warm)",
          }}
        >
          {/* Header */}
          <div
            className="px-6 py-3 text-center text-[11px] font-semibold tracking-[2.5px] uppercase"
            style={{
              background:
                mode === "solar2lunar"
                  ? "linear-gradient(135deg, rgba(196,120,58,0.1), rgba(212,149,106,0.08))"
                  : "linear-gradient(135deg, rgba(74,139,127,0.1), rgba(107,168,152,0.08))",
              borderBottom: "1px solid var(--ls-border-soft)",
              color: mode === "solar2lunar" ? "var(--warm-amber)" : "var(--jade-teal)",
            }}
          >
            Kết Quả Chuyển Đổi
          </div>

          <div
            className="grid grid-cols-2 divide-x"
            style={{ borderColor: "var(--ls-border-soft)" }}
          >
            {/* Solar side */}
            <div className="p-6 text-center">
              <div className="text-text-muted-ls mb-2 text-[10px] tracking-[2px] uppercase">
                📅 Dương Lịch
              </div>
              <div className="text-warm-amber mb-1 text-3xl leading-none font-[var(--font-lora)] font-semibold">
                {String(data.solar.day).padStart(2, "0")}
              </div>
              <div className="text-text-mid text-sm">
                Tháng {data.solar.month} · {data.solar.year}
              </div>
            </div>

            {/* Lunar side */}
            <div className="p-6 text-center">
              <div className="text-text-muted-ls mb-2 text-[10px] tracking-[2px] uppercase">
                🌙 Âm Lịch
              </div>
              <div className="text-jade-teal mb-1 text-2xl leading-none font-[var(--font-noto)] font-medium">
                {data.lunar.day <= 15 ? `Mồng ${data.lunar.day}` : data.lunar.day}
              </div>
              <div className="text-text-mid text-sm">
                Tháng {data.lunar.month} · {data.lunar.year}
                {data.lunar.leap_month && (
                  <span className="text-warm-amber ml-1 text-[11px]">(Nhuận)</span>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
