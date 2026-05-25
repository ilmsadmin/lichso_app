"use client";

import Link from "next/link";
import { Bookmark, ArrowRight, Plus } from "lucide-react";
import { useBookmarks } from "@/hooks/useBookmarks";
import { useAuthStore } from "@/stores/authStore";
import { ROUTES } from "@/lib/constants";

export function V2RemindersWidget() {
  const { isAuthenticated } = useAuthStore();
  const { data: bookmarks, isLoading } = useBookmarks();

  const list = bookmarks ?? [];

  return (
    <div
      className="v2-card rounded-xl p-5"
      style={{
        background: "var(--v2-bg-card)",
        border: "1px solid var(--v2-border-primary)",
        boxShadow: "var(--v2-shadow-xs)",
      }}
    >
      {/* Header */}
      <div className="mb-3.5 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div
            className="flex h-8 w-8 items-center justify-center rounded-lg"
            style={{ background: "var(--v2-bg-accent-soft)", color: "var(--v2-text-accent)" }}
          >
            <Bookmark className="h-4 w-4" />
          </div>
          <h3 className="text-[16px] font-bold" style={{ color: "var(--v2-text-primary)" }}>
            Nhắc nhở
          </h3>
        </div>
        {list.length > 0 && (
          <Link
            href={ROUTES.PROFILE_BOOKMARKS}
            className="flex items-center gap-1 text-[12px] font-medium transition-all hover:gap-2"
            style={{ color: "var(--v2-text-accent)" }}
          >
            Tất cả <ArrowRight className="h-3 w-3" />
          </Link>
        )}
      </div>

      {/* Content */}
      {!isAuthenticated ? (
        <p className="py-4 text-center text-[13px]" style={{ color: "var(--v2-text-muted)" }}>
          <Link href={ROUTES.LOGIN} className="underline" style={{ color: "var(--v2-text-accent)" }}>
            Đăng nhập
          </Link>{" "}
          để quản lý nhắc nhở
        </p>
      ) : isLoading ? (
        <div className="space-y-2">
          {[1, 2].map((i) => (
            <div key={i} className="h-10 animate-pulse rounded-lg" style={{ background: "var(--v2-bg-hover)" }} />
          ))}
        </div>
      ) : list.length === 0 ? (
        <p className="py-4 text-center text-[13px]" style={{ color: "var(--v2-text-muted)" }}>
          Chưa có nhắc nhở nào
        </p>
      ) : (
        <div>
          {list.slice(0, 3).map((item) => (
            <div
              key={item.id}
              className="flex items-center gap-2.5 py-2.5"
              style={{ borderBottom: "1px solid var(--v2-border-light)" }}
            >
              <div
                className="h-4 w-4 shrink-0 rounded-full border-2"
                style={{ borderColor: "var(--v2-border-primary)" }}
              />
              <span className="flex-1 text-[13px]" style={{ color: "var(--v2-text-primary)" }}>
                {item.title || item.solar_date}
              </span>
              <span className="text-[11px]" style={{ color: "var(--v2-text-muted)" }}>
                {item.solar_date}
              </span>
            </div>
          ))}
        </div>
      )}

      {/* Add button */}
      <button
        className="mt-2.5 flex w-full items-center justify-center gap-1.5 rounded-lg border border-dashed py-2.5 text-[12px] transition-all"
        style={{ borderColor: "var(--v2-border-primary)", color: "var(--v2-text-muted)" }}
        onMouseEnter={(e) => {
          (e.currentTarget as HTMLElement).style.borderColor = "var(--v2-text-accent)";
          (e.currentTarget as HTMLElement).style.color = "var(--v2-text-accent)";
          (e.currentTarget as HTMLElement).style.background = "var(--v2-bg-accent-soft)";
        }}
        onMouseLeave={(e) => {
          (e.currentTarget as HTMLElement).style.borderColor = "var(--v2-border-primary)";
          (e.currentTarget as HTMLElement).style.color = "var(--v2-text-muted)";
          (e.currentTarget as HTMLElement).style.background = "transparent";
        }}
      >
        <Plus className="h-3 w-3" /> Thêm nhắc nhở
      </button>
    </div>
  );
}
