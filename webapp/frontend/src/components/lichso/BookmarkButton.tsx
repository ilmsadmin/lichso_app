"use client";

import { useState, useCallback } from "react";
import { useAuthStore } from "@/stores/authStore";
import { useBookmarksByDate, useCreateBookmark, useDeleteBookmark } from "@/hooks/useBookmarks";
import { toast } from "sonner";

interface BookmarkButtonProps {
  date: string; // YYYY-MM-DD
  className?: string;
}

export function BookmarkButton({ date, className = "" }: BookmarkButtonProps) {
  const { isAuthenticated } = useAuthStore();
  const { data: bookmarks } = useBookmarksByDate(date);
  const createMutation = useCreateBookmark();
  const deleteMutation = useDeleteBookmark();
  const [showForm, setShowForm] = useState(false);
  const [title, setTitle] = useState("");
  const [note, setNote] = useState("");

  const isBookmarked = bookmarks && bookmarks.length > 0;

  const handleToggle = useCallback(() => {
    if (!isAuthenticated) {
      toast.info("Vui lòng đăng nhập để lưu bookmark");
      return;
    }

    if (isBookmarked && bookmarks) {
      // Remove first bookmark for this date
      deleteMutation.mutate(bookmarks[0].id, {
        onSuccess: () => toast.success("Đã xóa bookmark"),
        onError: () => toast.error("Không thể xóa bookmark"),
      });
    } else {
      setShowForm(true);
    }
  }, [isAuthenticated, isBookmarked, bookmarks, deleteMutation]);

  const handleSave = useCallback(() => {
    if (!title.trim()) {
      toast.warning("Vui lòng nhập tiêu đề");
      return;
    }

    createMutation.mutate(
      { solar_date: date, title: title.trim(), note: note.trim() },
      {
        onSuccess: () => {
          toast.success("Đã lưu bookmark!");
          setShowForm(false);
          setTitle("");
          setNote("");
        },
        onError: () => toast.error("Không thể lưu bookmark"),
      }
    );
  }, [date, title, note, createMutation]);

  return (
    <div className={`relative ${className}`}>
      <button
        onClick={handleToggle}
        className="hover:bg-warm-amber/10 flex h-8 w-8 items-center justify-center rounded-lg transition-all active:scale-95"
        style={{
          color: isBookmarked ? "var(--warm-amber)" : "var(--ls-text-mid)",
        }}
        title={isBookmarked ? "Bỏ bookmark" : "Bookmark ngày này"}
      >
        <svg
          width="18"
          height="18"
          viewBox="0 0 24 24"
          fill={isBookmarked ? "currentColor" : "none"}
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M19 21l-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16z" />
        </svg>
      </button>

      {/* Bookmark form popup */}
      {showForm && (
        <>
          {/* Backdrop overlay */}
          <div className="fixed inset-0 z-40" onClick={() => setShowForm(false)} />
          <div
            className="absolute top-full right-0 z-50 mt-2 w-72 animate-[fadeUp_0.2s_ease-out_both] rounded-2xl p-5"
            style={{
              background: "var(--ls-card-bg-solid-strong, rgba(255,252,248,0.98))",
              border: "1px solid var(--ls-border-warm)",
              boxShadow: "0 16px 48px rgba(0,0,0,0.12), 0 2px 0 rgba(255,255,255,0.8) inset",
            }}
          >
            <div
              className="mb-3 text-[10px] font-medium tracking-[2px] uppercase"
              style={{ color: "var(--warm-amber)" }}
            >
              ★ Bookmark ngày
            </div>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Tiêu đề (vd: Sinh nhật Mẹ)"
              className="mb-2.5 w-full rounded-xl px-3 py-2.5 text-[13px] transition-all outline-none focus:ring-2 focus:ring-amber-200"
              style={{
                background: "var(--ls-card-bg, rgba(255,252,248,0.6))",
                border: "1px solid var(--ls-border-soft)",
                color: "var(--ls-text-dark)",
              }}
              autoFocus
            />
            <textarea
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="Ghi chú (tuỳ chọn)"
              rows={2}
              className="mb-4 w-full resize-none rounded-xl px-3 py-2.5 text-[13px] transition-all outline-none focus:ring-2 focus:ring-amber-200"
              style={{
                background: "var(--ls-card-bg, rgba(255,252,248,0.6))",
                border: "1px solid var(--ls-border-soft)",
                color: "var(--ls-text-dark)",
              }}
            />
            <div className="flex gap-2">
              <button
                onClick={handleSave}
                disabled={createMutation.isPending}
                className="flex-1 rounded-xl py-2 text-[12px] font-semibold text-white transition-all hover:opacity-90 active:scale-[0.98]"
                style={{
                  background: "linear-gradient(135deg, var(--warm-amber), var(--warm-gold))",
                  boxShadow: "0 2px 8px rgba(200, 150, 80, 0.3)",
                }}
              >
                {createMutation.isPending ? "Đang lưu..." : "Lưu"}
              </button>
              <button
                onClick={() => setShowForm(false)}
                className="rounded-xl px-4 py-2 text-[12px] font-medium transition-all hover:bg-black/5 active:scale-[0.98]"
                style={{
                  color: "var(--ls-text-mid)",
                  border: "1px solid var(--ls-border-soft)",
                }}
              >
                Huỷ
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
