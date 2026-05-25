"use client";

import { useState, useRef, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Bell, CheckCheck, Trash2, Loader2, BellOff, BookmarkCheck } from "lucide-react";
import { formatDistanceToNow } from "date-fns";
import { vi } from "date-fns/locale";
import { useNotifications } from "@/hooks/useNotifications";
import { cn } from "@/lib/utils";
import type { Notification, NotificationType } from "@/types/notification";

// ============================================
// Helpers
// ============================================

const TYPE_META: Record<
  NotificationType,
  { emoji: string; bg: string; border: string; text: string }
> = {
  info: {
    emoji: "ℹ️",
    bg: "rgba(59,130,246,0.1)",
    border: "rgba(59,130,246,0.25)",
    text: "#3b82f6",
  },
  success: {
    emoji: "✅",
    bg: "rgba(34,197,94,0.1)",
    border: "rgba(34,197,94,0.25)",
    text: "#16a34a",
  },
  warning: {
    emoji: "🔔",
    bg: "rgba(196,120,58,0.10)",
    border: "rgba(196,120,58,0.25)",
    text: "var(--warm-amber)",
  },
  error: {
    emoji: "❌",
    bg: "rgba(239,68,68,0.08)",
    border: "rgba(239,68,68,0.22)",
    text: "#ef4444",
  },
};

function NotifIcon({ notif }: { notif: Notification }) {
  const refEmoji = notif.ref_type === "reminder" ? "🔔" : notif.ref_type === "bookmark" ? "🔖" : null;
  const emoji = refEmoji ?? TYPE_META[notif.type].emoji;
  return (
    <span
      className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full text-base"
      style={{
        background: TYPE_META[notif.type].bg,
        border: `1px solid ${TYPE_META[notif.type].border}`,
      }}
    >
      {emoji}
    </span>
  );
}

// ============================================
// NotificationDropdown Component
// ============================================

export function NotificationDropdown() {
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const {
    notifications,
    unreadCount,
    isLoadingNotifications,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    deleteAll,
    isMarkingAllRead,
    isDeletingAll,
  } = useNotifications();

  // Close on outside click
  useEffect(() => {
    if (!open) return;
    const handler = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, [open]);

  const handleNotificationClick = (notif: Notification) => {
    if (!notif.is_read) markAsRead(notif.id);
    if (notif.link) {
      setOpen(false);
      router.push(notif.link);
    }
  };

  return (
    <div className="relative" ref={dropdownRef}>
      {/* Bell trigger */}
      <button
        onClick={() => setOpen((v) => !v)}
        className="relative flex h-9 w-9 items-center justify-center rounded-full transition-all"
        style={{
          background: open ? "var(--warm-amber)" : "transparent",
          color: open ? "#fff" : "var(--ls-text-mid)",
        }}
        title="Thông báo"
        aria-label="Thông báo"
      >
        <Bell className="h-[18px] w-[18px]" />
        {unreadCount > 0 && (
          <span
            className="absolute -top-0.5 -right-0.5 flex h-[18px] min-w-[18px] items-center justify-center rounded-full px-1 text-[10px] font-bold text-white"
            style={{ background: "var(--ls-danger, #ef4444)" }}
          >
            {unreadCount > 99 ? "99+" : unreadCount}
          </span>
        )}
      </button>

      {/* Dropdown panel */}
      {open && (
        <div
          className="animate-[fadeUp_0.18s_ease-out_both] overflow-hidden
            fixed left-0 right-0 top-[60px] z-[200] mx-2 rounded-2xl
            sm:absolute sm:top-full sm:left-auto sm:right-0 sm:mx-0 sm:mt-2 sm:w-[360px]"
          style={{
            background: "var(--ls-card-bg-solid-strong, rgba(255,252,248,0.99))",
            border: "1px solid var(--ls-border-warm)",
            boxShadow: "0 20px 60px rgba(0,0,0,0.14), 0 1px 0 rgba(255,255,255,0.8) inset",
          }}
        >
          {/* Header */}
          <div
            className="flex items-center justify-between px-4 py-3"
            style={{ borderBottom: "1px solid var(--ls-border-soft)" }}
          >
            <div className="flex items-center gap-2">
              <Bell className="h-4 w-4" style={{ color: "var(--warm-amber)" }} />
              <span className="text-sm font-semibold" style={{ color: "var(--ls-text-dark)" }}>
                Thông Báo
              </span>
              {unreadCount > 0 && (
                <span
                  className="rounded-full px-2 py-0.5 text-[11px] font-medium text-white"
                  style={{ background: "var(--warm-amber)" }}
                >
                  {unreadCount}
                </span>
              )}
            </div>
            <div className="flex items-center gap-1">
              {unreadCount > 0 && (
                <button
                  onClick={() => markAllAsRead()}
                  disabled={isMarkingAllRead}
                  className="flex items-center gap-1 rounded-lg px-2.5 py-1 text-[11px] font-medium transition-all disabled:opacity-50"
                  style={{ color: "var(--jade-teal)", background: "var(--ls-card-bg)" }}
                  title="Đánh dấu tất cả đã đọc"
                >
                  {isMarkingAllRead ? (
                    <Loader2 className="h-3 w-3 animate-spin" />
                  ) : (
                    <CheckCheck className="h-3 w-3" />
                  )}
                  Đọc tất cả
                </button>
              )}
              {notifications.length > 0 && (
                <button
                  onClick={() => deleteAll()}
                  disabled={isDeletingAll}
                  className="flex h-7 w-7 items-center justify-center rounded-lg transition-all disabled:opacity-50"
                  style={{ color: "var(--ls-text-soft)" }}
                  title="Xóa tất cả"
                >
                  {isDeletingAll ? (
                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                  ) : (
                    <Trash2 className="h-3.5 w-3.5" />
                  )}
                </button>
              )}
            </div>
          </div>

          {/* Body */}
          <div className="max-h-[380px] overflow-y-auto">
            {isLoadingNotifications ? (
              <div className="flex flex-col items-center justify-center gap-2 py-10">
                <Loader2 className="h-6 w-6 animate-spin" style={{ color: "var(--warm-amber)" }} />
                <span className="text-[12px]" style={{ color: "var(--ls-text-soft)" }}>
                  Đang tải...
                </span>
              </div>
            ) : notifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center gap-2 py-10">
                <BellOff className="h-8 w-8 opacity-30" style={{ color: "var(--ls-text-mid)" }} />
                <p className="text-[13px]" style={{ color: "var(--ls-text-soft)" }}>
                  Chưa có thông báo nào
                </p>
              </div>
            ) : (
              <div>
                {notifications.map((notif, idx) => (
                  <NotificationItem
                    key={notif.id}
                    notif={notif}
                    isLast={idx === notifications.length - 1}
                    onClick={() => handleNotificationClick(notif)}
                    onMarkRead={(e) => {
                      e.stopPropagation();
                      markAsRead(notif.id);
                    }}
                    onDelete={(e) => {
                      e.stopPropagation();
                      deleteNotification(notif.id);
                    }}
                  />
                ))}
              </div>
            )}
          </div>

          {/* Footer */}
          {notifications.length > 0 && (
            <div
              className="px-4 py-2 text-center"
              style={{ borderTop: "1px solid var(--ls-border-soft)" }}
            >
              <span className="text-[11px]" style={{ color: "var(--ls-text-muted)" }}>
                {notifications.length} thông báo
                {unreadCount > 0 && ` · ${unreadCount} chưa đọc`}
              </span>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

// ============================================
// NotificationItem sub-component
// ============================================

interface NotificationItemProps {
  notif: Notification;
  isLast: boolean;
  onClick: () => void;
  onMarkRead: (e: React.MouseEvent) => void;
  onDelete: (e: React.MouseEvent) => void;
}

function NotificationItem({ notif, isLast, onClick, onMarkRead, onDelete }: NotificationItemProps) {
  const isReminder = notif.ref_type === "reminder";
  const isBookmark = notif.ref_type === "bookmark";

  return (
    <div
      className={cn(
        "group relative flex cursor-pointer items-start gap-3 px-4 py-3 transition-colors",
        "hover:bg-warm-amber/[0.04]",
        !notif.is_read && "bg-warm-amber/[0.06]",
        !isLast && "border-b"
      )}
      style={{ borderColor: "var(--ls-border-soft)" }}
      onClick={onClick}
    >
      {/* Unread dot */}
      {!notif.is_read && (
        <span
          className="absolute top-4 left-1.5 h-1.5 w-1.5 rounded-full"
          style={{ background: "var(--warm-amber)" }}
        />
      )}

      {/* Icon */}
      <NotifIcon notif={notif} />

      {/* Content */}
      <div className="min-w-0 flex-1 space-y-0.5">
        {(isReminder || isBookmark) && (
          <div className="mb-1">
            <span
              className="rounded-full px-2 py-0.5 text-[10px] font-medium"
              style={{
                background: TYPE_META[notif.type].bg,
                color: TYPE_META[notif.type].text,
                border: `1px solid ${TYPE_META[notif.type].border}`,
              }}
            >
              {isReminder ? "🔔 Nhắc nhở" : "🔖 Bookmark"}
            </span>
          </div>
        )}
        <p
          className={cn("text-[13px] leading-snug", !notif.is_read && "font-semibold")}
          style={{ color: "var(--ls-text-dark)" }}
        >
          {notif.title}
        </p>
        <p
          className="line-clamp-2 text-[12px] leading-relaxed"
          style={{ color: "var(--ls-text-mid)" }}
        >
          {notif.message}
        </p>
        <p className="text-[11px]" style={{ color: "var(--ls-text-muted)" }}>
          {formatDistanceToNow(new Date(notif.created_at), { addSuffix: true, locale: vi })}
        </p>
      </div>

      {/* Hover actions */}
      <div className="flex shrink-0 flex-col items-center gap-1 opacity-0 transition-opacity group-hover:opacity-100">
        {!notif.is_read && (
          <button
            onClick={onMarkRead}
            className="flex h-6 w-6 items-center justify-center rounded-md transition-all"
            style={{ color: "var(--jade-teal)", background: "var(--ls-card-bg)" }}
            title="Đánh dấu đã đọc"
          >
            <BookmarkCheck className="h-3.5 w-3.5" />
          </button>
        )}
        <button
          onClick={onDelete}
          className="flex h-6 w-6 items-center justify-center rounded-md transition-all"
          style={{ color: "var(--ls-text-soft)" }}
          title="Xóa thông báo"
        >
          <Trash2 className="h-3.5 w-3.5" />
        </button>
      </div>
    </div>
  );
}
