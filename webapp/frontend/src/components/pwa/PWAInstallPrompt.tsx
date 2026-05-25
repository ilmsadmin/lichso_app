"use client";

import { useState, useEffect } from "react";
import { usePWA } from "@/hooks/usePWA";

export function PWAInstallPrompt() {
  const { isInstallable, installApp } = usePWA();
  const [dismissed, setDismissed] = useState(false);
  const [show, setShow] = useState(false);

  useEffect(() => {
    // Check if user has previously dismissed the prompt
    const wasDismissed = localStorage.getItem("pwa-install-dismissed");
    if (wasDismissed) {
      const dismissedAt = parseInt(wasDismissed, 10);
      // Show again after 7 days
      if (Date.now() - dismissedAt < 7 * 24 * 60 * 60 * 1000) {
        setDismissed(true);
        return;
      }
    }

    // Delay showing to avoid overwhelming user on first visit
    if (isInstallable) {
      const timer = setTimeout(() => setShow(true), 5000);
      return () => clearTimeout(timer);
    }
  }, [isInstallable]);

  if (!isInstallable || dismissed || !show) return null;

  const handleDismiss = () => {
    setDismissed(true);
    setShow(false);
    localStorage.setItem("pwa-install-dismissed", String(Date.now()));
  };

  const handleInstall = async () => {
    const success = await installApp();
    if (success) {
      setShow(false);
    }
  };

  return (
    <div className="fixed right-4 bottom-4 left-4 z-[300] mx-auto max-w-md animate-[fadeUp_0.4s_ease-out_both]">
      <div
        className="relative flex items-center gap-3 rounded-2xl px-4 py-3 shadow-lg"
        style={{
          background: "rgba(255,252,248,0.97)",
          border: "1px solid var(--ls-border-warm, rgba(196,120,58,0.2))",
          boxShadow: "0 12px 40px rgba(0,0,0,0.12), 0 1px 0 rgba(255,255,255,0.8) inset",
        }}
      >
        {/* App icon */}
        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-amber-500 to-amber-700 shadow-sm">
          <span className="text-lg font-bold text-white">曆</span>
        </div>

        {/* Text */}
        <div className="min-w-0 flex-1">
          <div className="text-text-dark truncate text-[13px] font-medium">Cài đặt Lịch Số</div>
          <div className="text-text-muted-ls text-[11px] leading-snug">
            Truy cập nhanh từ màn hình chính
          </div>
        </div>

        {/* Actions */}
        <div className="flex shrink-0 items-center gap-2">
          <button
            onClick={handleDismiss}
            className="text-text-muted-ls hover:text-text-mid px-2 py-1 text-[11px] transition-colors"
          >
            Để sau
          </button>
          <button
            onClick={handleInstall}
            className="rounded-lg px-3 py-1.5 text-[12px] font-medium text-white transition-all hover:brightness-110 active:scale-95"
            style={{
              background: "linear-gradient(135deg, #C4783A, #E8B86D)",
              boxShadow: "0 2px 8px rgba(196,120,58,0.3)",
            }}
          >
            Cài đặt
          </button>
        </div>
      </div>
    </div>
  );
}
