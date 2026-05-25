"use client";

import { useEffect, useState, useRef } from "react";

const GIO_NAMES = [
  "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ",
  "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi",
];
const GIO_HD = [true, true, false, false, true, false, true, true, false, true, false, false];

export function ClockWidget() {
  const [time, setTime] = useState("");
  const [gioInfo, setGioInfo] = useState({ name: "", isHD: true });
  const rafRef = useRef<number>(0);

  useEffect(() => {
    let lastSec = -1;

    function tick() {
      const n = new Date();
      const s = n.getSeconds();
      // Only update state when second actually changes — avoids 60fps re-renders
      if (s !== lastSec) {
        lastSec = s;
        const h = String(n.getHours()).padStart(2, "0");
        const m = String(n.getMinutes()).padStart(2, "0");
        const ss = String(s).padStart(2, "0");
        setTime(`${h}:${m}:${ss}`);
        const idx = Math.floor(((n.getHours() + 1) % 24) / 2);
        setGioInfo({ name: GIO_NAMES[idx], isHD: GIO_HD[idx] });
      }
      rafRef.current = requestAnimationFrame(tick);
    }

    rafRef.current = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(rafRef.current);
  }, []);

  return (
    <div
      className="mt-6 w-full pt-5 text-center"
      style={{ borderTop: "1px solid rgba(200,144,42,0.15)", contentVisibility: "auto" }}
    >
      <div className="font-playfair text-text-dark text-[28px] leading-none font-bold tracking-[2px] tabular-nums sm:text-[32px]">
        {time || "--:--:--"}
      </div>
      <div
        className="mt-1.5 text-[12px] tracking-wide"
        style={{ color: gioInfo.isHD ? "var(--jade-teal)" : "var(--ls-danger)" }}
      >
        Giờ {gioInfo.name} · {gioInfo.isHD ? "✦ Hoàng Đạo" : "— Hắc Đạo"}
      </div>
    </div>
  );
}
