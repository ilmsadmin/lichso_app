"use client";

import { usePWA } from "@/hooks/usePWA";

/**
 * A small offline indicator banner shown when the user loses connectivity.
 */
export function OfflineIndicator() {
  const { isOnline } = usePWA();

  if (isOnline) return null;

  return (
    <div
      className="fixed inset-x-0 top-0 z-[400] flex animate-[fadeIn_0.3s_ease-out_both] items-center justify-center py-1.5 text-[12px] font-medium text-white"
      style={{
        background: "linear-gradient(90deg, #c0392b, #e74c3c)",
      }}
    >
      <span className="mr-1.5">⚡</span>
      Không có kết nối mạng — Đang sử dụng dữ liệu đã lưu
    </div>
  );
}
