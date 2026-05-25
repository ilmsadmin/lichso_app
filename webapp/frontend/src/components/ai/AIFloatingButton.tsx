"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { Sparkles } from "lucide-react";

export function AIFloatingButton() {
  const [visible, setVisible] = useState(false);
  const [hovered, setHovered] = useState(false);
  const [ripple, setRipple] = useState(false);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Fade-in on mount with small delay for smooth entrance
  useEffect(() => {
    timerRef.current = setTimeout(() => setVisible(true), 600);
    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
    };
  }, []);

  // Periodic pulse animation
  useEffect(() => {
    const interval = setInterval(() => {
      setRipple(true);
      setTimeout(() => setRipple(false), 800);
    }, 4000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div
      className="fixed bottom-24 right-4 z-50 flex flex-col items-center gap-2"
      style={{
        opacity: visible ? 1 : 0,
        transform: visible ? "translateY(0) scale(1)" : "translateY(20px) scale(0.8)",
        transition: "opacity 0.5s cubic-bezier(0.34,1.56,0.64,1), transform 0.5s cubic-bezier(0.34,1.56,0.64,1)",
        willChange: "opacity, transform",
      }}
    >
      {/* Tooltip label */}
      <div
        aria-hidden="true"
        style={{
          opacity: hovered ? 1 : 0,
          transform: hovered ? "translateX(0) scale(1)" : "translateX(8px) scale(0.92)",
          transition: "opacity 0.22s ease, transform 0.22s ease",
          pointerEvents: "none",
          whiteSpace: "nowrap",
          fontSize: "11px",
          fontWeight: 600,
          letterSpacing: "0.04em",
          color: "#fff",
          background: "linear-gradient(135deg, #c4783a 0%, #4a8b7f 100%)",
          borderRadius: "10px",
          padding: "4px 10px",
          boxShadow: "0 4px 16px rgba(196,120,58,0.32)",
          position: "absolute",
          right: "calc(100% + 10px)",
          top: "50%",
          translate: "0 -50%",
        }}
      >
        Tử Vi AI ✨
      </div>

      {/* Main FAB button */}
      <Link
        href="/tu-vi-ai"
        aria-label="Xem Tử Vi AI"
        onMouseEnter={() => setHovered(true)}
        onMouseLeave={() => setHovered(false)}
        style={{
          position: "relative",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          width: 52,
          height: 52,
          borderRadius: "50%",
          background: hovered
            ? "linear-gradient(135deg, #d4956a 0%, #4a8b7f 100%)"
            : "linear-gradient(135deg, #c4783a 0%, #3d806e 100%)",
          boxShadow: hovered
            ? "0 8px 32px rgba(196,120,58,0.55), 0 0 0 4px rgba(196,120,58,0.18)"
            : "0 4px 20px rgba(196,120,58,0.4), 0 0 0 3px rgba(196,120,58,0.12)",
          transform: hovered ? "scale(1.12)" : "scale(1)",
          transition: "background 0.3s ease, box-shadow 0.3s ease, transform 0.25s cubic-bezier(0.34,1.56,0.64,1)",
          outline: "none",
          border: "none",
          cursor: "pointer",
          textDecoration: "none",
          flexShrink: 0,
        }}
      >
        {/* Ripple ring */}
        <span
          style={{
            position: "absolute",
            inset: 0,
            borderRadius: "50%",
            border: "2px solid rgba(196,120,58,0.55)",
            opacity: ripple ? 0 : 0.6,
            transform: ripple ? "scale(1.8)" : "scale(1)",
            transition: ripple
              ? "transform 0.8s ease-out, opacity 0.8s ease-out"
              : "none",
            pointerEvents: "none",
          }}
        />

        {/* Sparkle icon */}
        <Sparkles
          size={22}
          strokeWidth={2}
          style={{
            color: "#fff",
            filter: "drop-shadow(0 1px 4px rgba(255,255,255,0.5))",
            transition: "transform 0.3s ease",
            transform: hovered ? "rotate(15deg) scale(1.1)" : "rotate(0deg) scale(1)",
            position: "relative",
            zIndex: 1,
          }}
        />

        {/* AI badge chip */}
        <span
          style={{
            position: "absolute",
            bottom: -3,
            right: -3,
            width: 18,
            height: 18,
            borderRadius: "50%",
            background: "linear-gradient(135deg, #ffd700 0%, #ffaa00 100%)",
            border: "2px solid #fff",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            fontSize: "7px",
            fontWeight: 800,
            color: "#7a4e00",
            letterSpacing: "-0.01em",
            boxShadow: "0 2px 6px rgba(200,120,0,0.4)",
            lineHeight: 1,
          }}
        >
          AI
        </span>
      </Link>
    </div>
  );
}
