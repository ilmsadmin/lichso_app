"use client";

import Link from "next/link";
import { Pencil } from "lucide-react";
import { useAuth } from "@/hooks/useAuth";

interface AdminEditButtonProps {
  href: string;
  label?: string;
}

/**
 * Nút "Sửa nhanh" chỉ hiển thị với admin/editor.
 * Đặt ở góc trên-phải của trang detail, dẫn thẳng vào trang edit admin.
 */
export function AdminEditButton({ href, label = "Sửa nhanh" }: AdminEditButtonProps) {
  const { hasAdminAccess } = useAuth();

  if (!hasAdminAccess()) return null;

  return (
    <Link
      href={href}
      target="_blank"
      rel="noopener noreferrer"
      className="fixed bottom-6 right-6 z-50 flex items-center gap-2 rounded-full px-4 py-2.5 text-sm font-medium shadow-lg transition-all hover:scale-105 active:scale-95 sm:bottom-8 sm:right-8"
      style={{
        background: "linear-gradient(135deg, #f59e0b 0%, #d97706 100%)",
        color: "#fff",
        boxShadow: "0 4px 20px rgba(245,158,11,0.4)",
      }}
      title={label}
    >
      <Pencil className="h-4 w-4" />
      <span className="hidden sm:inline">{label}</span>
    </Link>
  );
}
