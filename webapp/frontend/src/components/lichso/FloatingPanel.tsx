"use client";

import { useEffect, useRef } from "react";
import type { MenuPanel } from "./FloatingMenu";

interface FloatingPanelProps {
  panel: MenuPanel;
  onClose: () => void;
  children: React.ReactNode;
  /** Which side the panel slides from */
  side?: "left" | "right";
}

export function FloatingPanel({ panel, onClose, children, side = "right" }: FloatingPanelProps) {
  const panelRef = useRef<HTMLDivElement>(null);

  // Close on Escape
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", handler);
    return () => document.removeEventListener("keydown", handler);
  }, [onClose]);

  // Close on click outside
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (panelRef.current && !panelRef.current.contains(e.target as Node)) {
        // Ignore clicks on floating bubbles
        const target = e.target as HTMLElement;
        if (target.closest("[aria-label]")?.closest(".z-30")) return;
        onClose();
      }
    };
    // Small delay to avoid closing immediately from the click that opened it
    const timer = setTimeout(() => {
      document.addEventListener("mousedown", handler);
    }, 100);
    return () => {
      clearTimeout(timer);
      document.removeEventListener("mousedown", handler);
    };
  }, [onClose]);

  if (!panel) return null;

  return (
    <div
      ref={panelRef}
      className={`fixed right-2 bottom-14 left-2 z-40 max-h-[70vh] lg:fixed lg:top-[50%] lg:right-auto lg:bottom-auto lg:left-auto lg:-translate-y-1/2 ${side === "right" ? "lg:right-[calc(50%-340px)]" : "lg:left-[calc(50%-340px)]"} animate-[panelSlideIn_0.35s_ease-out_both] overflow-y-auto rounded-[18px] backdrop-blur-[12px] lg:max-h-[80vh] lg:w-[680px]`}
      style={{
        background: "var(--ls-card-bg-solid-strong)",
        border: "1px solid var(--ls-border-warm)",
        boxShadow: "0 16px 48px var(--ls-shadow-deep)",
        fontSize: "12.5px",
        lineHeight: "1.7",
        color: "var(--ls-text-mid)",
        ["--slide-from" as string]: side === "right" ? "20px" : "-20px",
      }}
    >
      {/* Close button */}
      <button
        onClick={onClose}
        className="text-text-muted-ls hover:text-text-dark hover:bg-warm-amber/10 absolute top-3 right-3 z-10 flex h-7 w-7 items-center justify-center rounded-full text-sm transition-all"
      >
        ✕
      </button>

      <div className="p-5 pt-4">{children}</div>
    </div>
  );
}
