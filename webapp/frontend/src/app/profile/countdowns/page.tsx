"use client";

import { useState, useEffect } from "react";
import { Timer, Plus, Pencil, Trash2, CalendarClock, Repeat, Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Switch } from "@/components/ui/switch";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import {
  useUserCountdowns,
  useCreateCountdown,
  useUpdateCountdown,
  useDeleteCountdown,
} from "@/hooks/useV3";
import type {
  UserCountdown,
  CreateUserCountdownRequest,
  UpdateUserCountdownRequest,
} from "@/types/v3";

// ============================================
// Countdown Colors & Icons
// ============================================

const COUNTDOWN_COLORS = [
  { value: "#EF4444", label: "Đỏ" },
  { value: "#F97316", label: "Cam" },
  { value: "#EAB308", label: "Vàng" },
  { value: "#22C55E", label: "Xanh lá" },
  { value: "#3B82F6", label: "Xanh dương" },
  { value: "#8B5CF6", label: "Tím" },
  { value: "#EC4899", label: "Hồng" },
  { value: "#6B7280", label: "Xám" },
];

const COUNTDOWN_ICONS = [
  "🎂",
  "🎉",
  "💍",
  "✈️",
  "🎓",
  "💼",
  "🏠",
  "❤️",
  "🎯",
  "⭐",
  "🔔",
  "📅",
  "🎄",
  "🌸",
  "🎁",
  "🏆",
];

// ============================================
// Countdown Form Dialog
// ============================================

function CountdownFormDialog({
  open,
  onOpenChange,
  editingCountdown,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  editingCountdown?: UserCountdown | null;
}) {
  const isEditing = !!editingCountdown;
  const createCountdown = useCreateCountdown();
  const updateCountdown = useUpdateCountdown();

  const [title, setTitle] = useState(editingCountdown?.title ?? "");
  const [description, setDescription] = useState(editingCountdown?.description ?? "");
  const [targetDate, setTargetDate] = useState(editingCountdown?.target_date?.split("T")[0] ?? "");
  const [targetTime, setTargetTime] = useState(editingCountdown?.target_time ?? "");
  const [color, setColor] = useState(editingCountdown?.color ?? COUNTDOWN_COLORS[4].value);
  const [icon, setIcon] = useState(editingCountdown?.icon ?? "🎯");
  const [isRecurring, setIsRecurring] = useState(editingCountdown?.is_recurring ?? false);
  const [recurringType, setRecurringType] = useState<"yearly" | "monthly">(
    editingCountdown?.recurring_type ?? "yearly"
  );
  const [notifyBefore, setNotifyBefore] = useState(
    String(editingCountdown?.notify_before_days ?? 3)
  );

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !targetDate) return;

    if (isEditing && editingCountdown) {
      const data: UpdateUserCountdownRequest = {
        title: title.trim(),
        description: description.trim() || undefined,
        target_date: targetDate,
        target_time: targetTime || undefined,
        color,
        icon,
        is_recurring: isRecurring,
        recurring_type: isRecurring ? recurringType : undefined,
        notify_before_days: Number(notifyBefore) || 3,
      };
      updateCountdown.mutate(
        { id: editingCountdown.id, data },
        { onSuccess: () => onOpenChange(false) }
      );
    } else {
      const data: CreateUserCountdownRequest = {
        title: title.trim(),
        description: description.trim() || undefined,
        target_date: targetDate,
        target_time: targetTime || undefined,
        color,
        icon,
        is_recurring: isRecurring,
        recurring_type: isRecurring ? recurringType : undefined,
        notify_before_days: Number(notifyBefore) || 3,
      };
      createCountdown.mutate(data, {
        onSuccess: () => onOpenChange(false),
      });
    }
  };

  const isPending = createCountdown.isPending || updateCountdown.isPending;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[85vh] overflow-y-auto sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{isEditing ? "Sửa đếm ngược" : "Tạo đếm ngược mới"}</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="cd-title">Tiêu đề *</Label>
            <Input
              id="cd-title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Ví dụ: Sinh nhật, Kỷ niệm..."
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="cd-desc">Mô tả</Label>
            <Textarea
              id="cd-desc"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Mô tả ngắn..."
              rows={2}
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-2">
              <Label htmlFor="cd-date">Ngày đích *</Label>
              <Input
                id="cd-date"
                type="date"
                value={targetDate}
                onChange={(e) => setTargetDate(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="cd-time">Giờ (tùy chọn)</Label>
              <Input
                id="cd-time"
                type="time"
                value={targetTime}
                onChange={(e) => setTargetTime(e.target.value)}
              />
            </div>
          </div>

          {/* Icon picker */}
          <div className="space-y-2">
            <Label>Biểu tượng</Label>
            <div className="flex flex-wrap gap-1.5">
              {COUNTDOWN_ICONS.map((ic) => (
                <button
                  key={ic}
                  type="button"
                  className={`flex h-9 w-9 items-center justify-center rounded-lg text-lg transition-all ${
                    icon === ic ? "bg-primary/10 ring-primary scale-110 ring-2" : "hover:bg-accent"
                  }`}
                  onClick={() => setIcon(ic)}
                >
                  {ic}
                </button>
              ))}
            </div>
          </div>

          {/* Color picker */}
          <div className="space-y-2">
            <Label>Màu sắc</Label>
            <div className="flex gap-2">
              {COUNTDOWN_COLORS.map((c) => (
                <button
                  key={c.value}
                  type="button"
                  className={`h-7 w-7 rounded-full border-2 transition-all ${
                    color === c.value
                      ? "border-foreground scale-110 shadow-md"
                      : "hover:border-muted-foreground/30 border-transparent"
                  }`}
                  style={{ backgroundColor: c.value }}
                  onClick={() => setColor(c.value)}
                  title={c.label}
                />
              ))}
            </div>
          </div>

          {/* Recurring */}
          <div className="space-y-3">
            <div className="flex items-center gap-3">
              <Switch checked={isRecurring} onCheckedChange={setIsRecurring} />
              <Label>Lặp lại</Label>
            </div>
            {isRecurring && (
              <Select
                value={recurringType}
                onValueChange={(v) => setRecurringType(v as "yearly" | "monthly")}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="yearly">Hàng năm</SelectItem>
                  <SelectItem value="monthly">Hàng tháng</SelectItem>
                </SelectContent>
              </Select>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="cd-notify">Nhắc trước (ngày)</Label>
            <Input
              id="cd-notify"
              type="number"
              min={0}
              max={365}
              value={notifyBefore}
              onChange={(e) => setNotifyBefore(e.target.value)}
            />
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isPending}
            >
              Hủy
            </Button>
            <Button type="submit" disabled={isPending || !title.trim() || !targetDate}>
              {isPending ? "Đang xử lý..." : isEditing ? "Cập nhật" : "Tạo"}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}

// ============================================
// Countdown Card Component
// ============================================

function CountdownCard({
  countdown,
  onEdit,
  onDelete,
}: {
  countdown: UserCountdown;
  onEdit: () => void;
  onDelete: () => void;
}) {
  const [timeLeft, setTimeLeft] = useState("");

  useEffect(() => {
    const calculate = () => {
      const target = new Date(countdown.target_date);
      if (countdown.target_time) {
        const [h, m] = countdown.target_time.split(":");
        target.setHours(Number(h), Number(m));
      }
      const now = new Date();
      const diff = target.getTime() - now.getTime();

      if (diff <= 0) {
        setTimeLeft("Đã đến!");
        return;
      }

      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const mins = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

      if (days > 0) {
        setTimeLeft(`${days} ngày ${hours} giờ`);
      } else if (hours > 0) {
        setTimeLeft(`${hours} giờ ${mins} phút`);
      } else {
        setTimeLeft(`${mins} phút`);
      }
    };

    calculate();
    const interval = setInterval(calculate, 60000); // update every minute
    return () => clearInterval(interval);
  }, [countdown.target_date, countdown.target_time]);

  const isPast = countdown.days_remaining <= 0;

  return (
    <Card className="group relative overflow-hidden transition-all hover:shadow-lg">
      {/* Color accent bar */}
      <div
        className="absolute top-0 right-0 left-0 h-1"
        style={{ backgroundColor: countdown.color }}
      />
      <CardContent className="pt-5 pb-4">
        <div className="flex items-start gap-3">
          <div
            className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl text-2xl"
            style={{ backgroundColor: countdown.color + "20" }}
          >
            {countdown.icon}
          </div>
          <div className="min-w-0 flex-1">
            <h3 className="truncate text-sm font-semibold">{countdown.title}</h3>
            {countdown.description && (
              <p className="text-muted-foreground mt-0.5 line-clamp-1 text-xs">
                {countdown.description}
              </p>
            )}
            <div className="mt-2 flex items-center gap-2">
              <span
                className={`text-lg font-bold ${isPast ? "text-green-600" : ""}`}
                style={!isPast ? { color: countdown.color } : {}}
              >
                {timeLeft}
              </span>
            </div>
            <div className="mt-1.5 flex items-center gap-2">
              <div className="text-muted-foreground flex items-center gap-1 text-[11px]">
                <CalendarClock className="h-3 w-3" />
                {new Date(countdown.target_date).toLocaleDateString("vi-VN", {
                  day: "2-digit",
                  month: "2-digit",
                  year: "numeric",
                })}
                {countdown.target_time && ` · ${countdown.target_time}`}
              </div>
              {countdown.is_recurring && (
                <Badge variant="outline" className="gap-0.5 px-1.5 py-0 text-[10px]">
                  <Repeat className="h-2.5 w-2.5" />
                  {countdown.recurring_type === "yearly" ? "Năm" : "Tháng"}
                </Badge>
              )}
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="absolute top-3 right-3 flex items-center gap-1 opacity-0 transition-opacity group-hover:opacity-100">
          <Button variant="ghost" size="icon" className="h-7 w-7" onClick={onEdit}>
            <Pencil className="h-3.5 w-3.5" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            className="text-destructive/60 hover:text-destructive h-7 w-7"
            onClick={onDelete}
          >
            <Trash2 className="h-3.5 w-3.5" />
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

// ============================================
// Countdowns Page
// ============================================

export default function CountdownsPage() {
  const [page, setPage] = useState(1);
  const [formOpen, setFormOpen] = useState(false);
  const [editingCountdown, setEditingCountdown] = useState<UserCountdown | null>(null);
  const [deleteId, setDeleteId] = useState<string | null>(null);

  const { data, isLoading } = useUserCountdowns({ page, limit: 20 });
  const deleteCountdown = useDeleteCountdown();

  const countdowns = data?.data ?? [];

  const activeCountdowns = countdowns.filter((c) => c.is_active && c.days_remaining > 0);
  const pastCountdowns = countdowns.filter((c) => !c.is_active || c.days_remaining <= 0);

  const handleEdit = (cd: UserCountdown) => {
    setEditingCountdown(cd);
    setFormOpen(true);
  };

  const handleAdd = () => {
    setEditingCountdown(null);
    setFormOpen(true);
  };

  const handleDelete = () => {
    if (!deleteId) return;
    deleteCountdown.mutate(deleteId, {
      onSuccess: () => setDeleteId(null),
    });
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="flex items-center gap-2 text-3xl font-bold tracking-tight">
            <Timer className="h-8 w-8" />
            Đếm ngược
          </h1>
          <p className="text-muted-foreground mt-1">Đếm ngược đến những ngày quan trọng</p>
        </div>
        <Button onClick={handleAdd}>
          <Plus className="mr-2 h-4 w-4" />
          Tạo đếm ngược
        </Button>
      </div>

      {/* Loading */}
      {isLoading ? (
        <div className="grid gap-4 sm:grid-cols-2">
          {Array.from({ length: 4 }).map((_, i) => (
            <Skeleton key={i} className="h-28 rounded-xl" />
          ))}
        </div>
      ) : countdowns.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Sparkles className="text-muted-foreground mb-4 h-12 w-12 opacity-50" />
            <p className="text-lg font-medium">Chưa có đếm ngược</p>
            <p className="text-muted-foreground mt-1 text-sm">
              Tạo đếm ngược cho sinh nhật, kỷ niệm, sự kiện...
            </p>
            <Button onClick={handleAdd} className="mt-4" variant="outline">
              <Plus className="mr-2 h-4 w-4" />
              Tạo đếm ngược đầu tiên
            </Button>
          </CardContent>
        </Card>
      ) : (
        <>
          {/* Active countdowns */}
          {activeCountdowns.length > 0 && (
            <div>
              <h2 className="text-muted-foreground mb-3 flex items-center gap-1.5 text-sm font-medium">
                <Timer className="h-3.5 w-3.5" />
                Đang đếm ngược ({activeCountdowns.length})
              </h2>
              <div className="grid gap-4 sm:grid-cols-2">
                {activeCountdowns
                  .sort((a, b) => a.days_remaining - b.days_remaining)
                  .map((cd) => (
                    <CountdownCard
                      key={cd.id}
                      countdown={cd}
                      onEdit={() => handleEdit(cd)}
                      onDelete={() => setDeleteId(cd.id)}
                    />
                  ))}
              </div>
            </div>
          )}

          {/* Past / inactive */}
          {pastCountdowns.length > 0 && (
            <div>
              <h2 className="text-muted-foreground mb-3 text-sm font-medium">
                Đã qua ({pastCountdowns.length})
              </h2>
              <div className="grid gap-4 opacity-60 sm:grid-cols-2">
                {pastCountdowns.map((cd) => (
                  <CountdownCard
                    key={cd.id}
                    countdown={cd}
                    onEdit={() => handleEdit(cd)}
                    onDelete={() => setDeleteId(cd.id)}
                  />
                ))}
              </div>
            </div>
          )}
        </>
      )}

      {/* Form Dialog */}
      {formOpen && (
        <CountdownFormDialog
          open={formOpen}
          onOpenChange={setFormOpen}
          editingCountdown={editingCountdown}
        />
      )}

      {/* Delete confirmation */}
      <ConfirmDialog
        open={!!deleteId}
        onOpenChange={(open) => !open && setDeleteId(null)}
        title="Xóa đếm ngược"
        description="Bạn có chắc muốn xóa bộ đếm ngược này?"
        confirmText="Xóa"
        variant="destructive"
        onConfirm={handleDelete}
        loading={deleteCountdown.isPending}
      />
    </div>
  );
}
