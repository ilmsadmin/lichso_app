"use client";

import { useState, useCallback } from "react";
import { useAuthStore } from "@/stores/authStore";
import { useReminders, useCreateReminder, useDeleteReminder } from "@/hooks/useBookmarks";
import type { ReminderType } from "@/types/bookmark";
import { toast } from "sonner";

const REMINDER_TYPES: { value: ReminderType; label: string; emoji: string }[] = [
  { value: "gio", label: "Ngày giỗ", emoji: "🕯️" },
  { value: "birthday", label: "Sinh nhật", emoji: "🎂" },
  { value: "anniversary", label: "Kỷ niệm", emoji: "💕" },
  { value: "holiday", label: "Ngày lễ", emoji: "🎊" },
  { value: "custom", label: "Tùy chọn", emoji: "📌" },
];

export function ReminderPanel() {
  const { isAuthenticated } = useAuthStore();
  const { data: reminders, isLoading } = useReminders();
  const createMutation = useCreateReminder();
  const deleteMutation = useDeleteReminder();
  const [showForm, setShowForm] = useState(false);

  // Form state
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [reminderType, setReminderType] = useState<ReminderType>("gio");
  const [isLunar, setIsLunar] = useState(true);
  const [day, setDay] = useState(1);
  const [month, setMonth] = useState(1);
  const [remindBefore, setRemindBefore] = useState(1);

  const handleCreate = useCallback(() => {
    if (!title.trim()) {
      toast.warning("Vui lòng nhập tiêu đề");
      return;
    }

    createMutation.mutate(
      {
        title: title.trim(),
        description: description.trim(),
        reminder_type: reminderType,
        is_lunar: isLunar,
        ...(isLunar
          ? { lunar_day: day, lunar_month: month }
          : { solar_day: day, solar_month: month }),
        is_recurring: true,
        remind_before_days: remindBefore,
        notify_push: true,
      },
      {
        onSuccess: () => {
          toast.success("Đã tạo nhắc nhở!");
          setShowForm(false);
          resetForm();
        },
        onError: () => toast.error("Không thể tạo nhắc nhở"),
      }
    );
  }, [title, description, reminderType, isLunar, day, month, remindBefore, createMutation]);

  const handleDelete = useCallback(
    (id: string) => {
      deleteMutation.mutate(id, {
        onSuccess: () => toast.success("Đã xóa nhắc nhở"),
        onError: () => toast.error("Không thể xóa"),
      });
    },
    [deleteMutation]
  );

  const resetForm = () => {
    setTitle("");
    setDescription("");
    setReminderType("gio");
    setIsLunar(true);
    setDay(1);
    setMonth(1);
    setRemindBefore(1);
  };

  if (!isAuthenticated) {
    return (
      <div className="py-6 text-center">
        <div className="mb-2 text-3xl">🔔</div>
        <p className="text-text-mid mb-1 text-[13px]">Đăng nhập để sử dụng nhắc nhở</p>
        <p className="text-text-soft text-[11px]">
          Nhận thông báo ngày giỗ, sinh nhật, ngày lễ quan trọng
        </p>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-3 flex items-center justify-between">
        <div className="text-text-muted-ls text-[10px] tracking-[2.5px] uppercase">🔔 Nhắc Nhở</div>
        <button
          onClick={() => setShowForm(!showForm)}
          className="rounded-full px-3 py-1 text-[11px] font-medium transition-all"
          style={{
            background: showForm ? "var(--ls-border-soft)" : "var(--warm-amber)",
            color: showForm ? "var(--ls-text-mid)" : "#fff",
          }}
        >
          {showForm ? "Đóng" : "+ Thêm"}
        </button>
      </div>

      {/* Create form */}
      {showForm && (
        <div
          className="mb-4 animate-[fadeUp_0.2s_ease-out_both] rounded-xl p-4"
          style={{
            background: "var(--ls-card-bg)",
            border: "1px solid var(--ls-border-soft)",
          }}
        >
          {/* Title */}
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Tiêu đề (vd: Giỗ Ông Nội)"
            className="mb-2 w-full rounded-lg px-3 py-2 text-[12px] outline-none"
            style={{
              background: "var(--ls-card-bg-solid)",
              border: "1px solid var(--ls-border-soft)",
              color: "var(--ls-text-dark)",
            }}
          />

          {/* Type selector */}
          <div className="mb-3 flex flex-wrap gap-1.5">
            {REMINDER_TYPES.map((t) => (
              <button
                key={t.value}
                onClick={() => setReminderType(t.value)}
                className="rounded-full px-2.5 py-1 text-[10px] font-medium transition-all"
                style={{
                  background:
                    reminderType === t.value ? "var(--warm-amber)" : "var(--ls-card-bg-solid)",
                  color: reminderType === t.value ? "#fff" : "var(--ls-text-soft)",
                  border: `1px solid ${reminderType === t.value ? "var(--warm-amber)" : "var(--ls-border-soft)"}`,
                }}
              >
                {t.emoji} {t.label}
              </button>
            ))}
          </div>

          {/* Lunar/Solar toggle */}
          <div className="mb-3 flex items-center gap-3">
            <label className="text-text-mid flex cursor-pointer items-center gap-1.5 text-[11px]">
              <input
                type="radio"
                checked={isLunar}
                onChange={() => setIsLunar(true)}
                className="accent-[var(--warm-amber)]"
              />
              Âm lịch
            </label>
            <label className="text-text-mid flex cursor-pointer items-center gap-1.5 text-[11px]">
              <input
                type="radio"
                checked={!isLunar}
                onChange={() => setIsLunar(false)}
                className="accent-[var(--warm-amber)]"
              />
              Dương lịch
            </label>
          </div>

          {/* Day / Month */}
          <div className="mb-2 flex gap-2">
            <div className="flex-1">
              <label className="text-text-soft mb-1 block text-[10px]">Ngày</label>
              <input
                type="number"
                min={1}
                max={isLunar ? 30 : 31}
                value={day}
                onChange={(e) => setDay(Number(e.target.value))}
                className="w-full rounded-lg px-3 py-1.5 text-[12px] outline-none"
                style={{
                  background: "var(--ls-card-bg-solid)",
                  border: "1px solid var(--ls-border-soft)",
                  color: "var(--ls-text-dark)",
                }}
              />
            </div>
            <div className="flex-1">
              <label className="text-text-soft mb-1 block text-[10px]">Tháng</label>
              <input
                type="number"
                min={1}
                max={12}
                value={month}
                onChange={(e) => setMonth(Number(e.target.value))}
                className="w-full rounded-lg px-3 py-1.5 text-[12px] outline-none"
                style={{
                  background: "var(--ls-card-bg-solid)",
                  border: "1px solid var(--ls-border-soft)",
                  color: "var(--ls-text-dark)",
                }}
              />
            </div>
            <div className="flex-1">
              <label className="text-text-soft mb-1 block text-[10px]">Nhắc trước</label>
              <select
                value={remindBefore}
                onChange={(e) => setRemindBefore(Number(e.target.value))}
                className="w-full rounded-lg px-2 py-1.5 text-[12px] outline-none"
                style={{
                  background: "var(--ls-card-bg-solid)",
                  border: "1px solid var(--ls-border-soft)",
                  color: "var(--ls-text-dark)",
                }}
              >
                <option value={0}>Cùng ngày</option>
                <option value={1}>1 ngày</option>
                <option value={3}>3 ngày</option>
                <option value={7}>7 ngày</option>
              </select>
            </div>
          </div>

          {/* Description */}
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Mô tả (tuỳ chọn)"
            rows={2}
            className="mb-3 w-full resize-none rounded-lg px-3 py-2 text-[12px] outline-none"
            style={{
              background: "var(--ls-card-bg-solid)",
              border: "1px solid var(--ls-border-soft)",
              color: "var(--ls-text-dark)",
            }}
          />

          <button
            onClick={handleCreate}
            disabled={createMutation.isPending}
            className="w-full rounded-lg py-2 text-[12px] font-semibold text-white transition-all"
            style={{
              background: "linear-gradient(135deg, var(--warm-amber), var(--warm-gold))",
            }}
          >
            {createMutation.isPending ? "Đang tạo..." : "Tạo nhắc nhở"}
          </button>
        </div>
      )}

      {/* Reminders list */}
      {isLoading ? (
        <div className="text-text-soft py-4 text-center text-[12px]">Đang tải...</div>
      ) : reminders && reminders.length > 0 ? (
        <div className="space-y-2">
          {reminders.map((r) => {
            const typeInfo = REMINDER_TYPES.find((t) => t.value === r.reminder_type);
            return (
              <div
                key={r.id}
                className="group flex items-start gap-2.5 rounded-xl p-3 transition-all"
                style={{
                  background: "var(--ls-card-bg)",
                  border: "1px solid var(--ls-border-soft)",
                }}
              >
                <span className="shrink-0 text-lg">{typeInfo?.emoji || "📌"}</span>
                <div className="min-w-0 flex-1">
                  <div className="text-text-dark truncate text-[13px] font-medium">{r.title}</div>
                  <div className="text-text-soft text-[11px]">
                    {r.is_lunar ? "Âm lịch" : "Dương lịch"} {r.is_lunar ? r.lunar_day : r.solar_day}
                    /{r.is_lunar ? r.lunar_month : r.solar_month}
                    {r.remind_before_days > 0 && ` · Nhắc trước ${r.remind_before_days} ngày`}
                  </div>
                </div>
                <button
                  onClick={() => handleDelete(r.id)}
                  className="text-danger shrink-0 text-[11px] opacity-0 transition-opacity group-hover:opacity-100"
                  title="Xóa"
                >
                  ✕
                </button>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="text-text-soft py-4 text-center text-[12px]">Chưa có nhắc nhở nào</div>
      )}
    </div>
  );
}
