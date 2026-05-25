"use client";

import { useState } from "react";
import {
  Bell,
  Plus,
  Pencil,
  Trash2,
  Calendar,
  Moon,
  Sun,
  Mail,
  ToggleLeft,
  ToggleRight,
  ChevronLeft,
  Cake,
  Heart,
  Star,
  Flower2,
  StickyNote,
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import Link from "next/link";
import { toast } from "sonner";
import {
  useReminders,
  useCreateReminder,
  useUpdateReminder,
  useDeleteReminder,
} from "@/hooks/useBookmarks";
import type { Reminder, ReminderType, CreateReminderRequest, UpdateReminderRequest } from "@/types/bookmark";

// ============================================
// Constants
// ============================================

const REMINDER_TYPES: { value: ReminderType; label: string; icon: React.ReactNode; color: string }[] = [
  { value: "holiday", label: "Ngày lễ", icon: <Star className="h-4 w-4" />, color: "bg-amber-500/10 text-amber-600" },
  { value: "anniversary", label: "Kỷ niệm", icon: <Heart className="h-4 w-4" />, color: "bg-rose-500/10 text-rose-600" },
  { value: "birthday", label: "Sinh nhật", icon: <Cake className="h-4 w-4" />, color: "bg-pink-500/10 text-pink-600" },
  { value: "gio", label: "Ngày giỗ", icon: <Flower2 className="h-4 w-4" />, color: "bg-emerald-500/10 text-emerald-600" },
  { value: "custom", label: "Nhắc nhở", icon: <StickyNote className="h-4 w-4" />, color: "bg-blue-500/10 text-blue-600" },
];

const DAYS = Array.from({ length: 30 }, (_, i) => i + 1);
const MONTHS = [
  "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
  "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12",
];

// ============================================
// Helper Functions
// ============================================

function getReminderTypeInfo(type: ReminderType) {
  return REMINDER_TYPES.find((t) => t.value === type) ?? REMINDER_TYPES[4];
}

function formatDateLabel(reminder: Reminder): string {
  if (reminder.is_lunar) {
    const d = reminder.lunar_day ?? "?";
    const m = reminder.lunar_month ?? "?";
    return `${d}/${m} âm lịch`;
  }
  const d = reminder.solar_day ?? "?";
  const m = reminder.solar_month ?? "?";
  return `${d}/${m} dương lịch`;
}

// ============================================
// Empty Form State
// ============================================

const EMPTY_FORM: CreateReminderRequest = {
  title: "",
  description: "",
  reminder_type: "custom",
  is_lunar: false,
  solar_day: undefined,
  solar_month: undefined,
  lunar_day: undefined,
  lunar_month: undefined,
  is_recurring: true,
  remind_before_days: 0,
  notify_email: true,
  notify_push: false,
};

// ============================================
// Main Page Component
// ============================================

export default function RemindersPage() {
  const { data: reminders, isLoading } = useReminders();
  const createReminder = useCreateReminder();
  const updateReminder = useUpdateReminder();
  const deleteReminder = useDeleteReminder();

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingReminder, setEditingReminder] = useState<Reminder | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Reminder | null>(null);
  const [form, setForm] = useState<CreateReminderRequest>(EMPTY_FORM);

  // ---- Form helpers ----
  const openCreate = () => {
    setEditingReminder(null);
    setForm(EMPTY_FORM);
    setDialogOpen(true);
  };

  const openEdit = (r: Reminder) => {
    setEditingReminder(r);
    setForm({
      title: r.title,
      description: r.description ?? "",
      reminder_type: r.reminder_type,
      is_lunar: r.is_lunar,
      solar_day: r.solar_day,
      solar_month: r.solar_month,
      lunar_day: r.lunar_day,
      lunar_month: r.lunar_month,
      is_recurring: r.is_recurring,
      remind_before_days: r.remind_before_days,
      notify_email: r.notify_email,
      notify_push: r.notify_push,
    });
    setDialogOpen(true);
  };

  const handleSubmit = async () => {
    if (!form.title.trim()) {
      toast.error("Vui lòng nhập tiêu đề");
      return;
    }

    const day = form.is_lunar ? form.lunar_day : form.solar_day;
    const month = form.is_lunar ? form.lunar_month : form.solar_month;
    if (!day || !month) {
      toast.error("Vui lòng chọn ngày và tháng");
      return;
    }

    try {
      if (editingReminder) {
        const updateData: UpdateReminderRequest = { ...form };
        await updateReminder.mutateAsync({ id: editingReminder.id, data: updateData });
        toast.success("Đã cập nhật nhắc nhở");
      } else {
        await createReminder.mutateAsync(form);
        toast.success("Đã tạo nhắc nhở mới");
      }
      setDialogOpen(false);
    } catch {
      toast.error("Có lỗi xảy ra, vui lòng thử lại");
    }
  };

  const handleToggleActive = async (r: Reminder) => {
    try {
      await updateReminder.mutateAsync({ id: r.id, data: { is_active: !r.is_active } });
      toast.success(r.is_active ? "Đã tắt nhắc nhở" : "Đã bật nhắc nhở");
    } catch {
      toast.error("Có lỗi xảy ra");
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    try {
      await deleteReminder.mutateAsync(deleteTarget.id);
      toast.success("Đã xoá nhắc nhở");
      setDeleteTarget(null);
    } catch {
      toast.error("Không thể xoá nhắc nhở");
    }
  };

  // ---- Render ----
  return (
    <div className="container mx-auto max-w-3xl px-4 py-8 space-y-6">
      {/* Header */}
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="icon" asChild>
          <Link href="/profile">
            <ChevronLeft className="h-5 w-5" />
          </Link>
        </Button>
        <div className="flex-1">
          <h1 className="text-2xl font-bold tracking-tight flex items-center gap-2">
            <Bell className="h-6 w-6 text-orange-500" />
            Nhắc nhở
          </h1>
          <p className="text-muted-foreground text-sm">Quản lý ngày kỷ niệm, sinh nhật và sự kiện định kỳ</p>
        </div>
        <Button onClick={openCreate} className="gap-2">
          <Plus className="h-4 w-4" />
          Thêm nhắc nhở
        </Button>
      </div>

      {/* Reminder List */}
      {isLoading ? (
        <div className="space-y-3">
          {[1, 2, 3].map((i) => (
            <Skeleton key={i} className="h-24 w-full rounded-xl" />
          ))}
        </div>
      ) : !reminders || reminders.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-16 text-center gap-4">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-orange-500/10">
              <Bell className="h-8 w-8 text-orange-400" />
            </div>
            <div>
              <p className="font-semibold text-lg">Chưa có nhắc nhở nào</p>
              <p className="text-muted-foreground text-sm mt-1">
                Thêm ngày sinh nhật, kỷ niệm hay ngày giỗ để không bao giờ quên
              </p>
            </div>
            <Button onClick={openCreate} className="gap-2 mt-2">
              <Plus className="h-4 w-4" />
              Tạo nhắc nhở đầu tiên
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {reminders.map((r) => (
            <ReminderCard
              key={r.id}
              reminder={r}
              onEdit={() => openEdit(r)}
              onDelete={() => setDeleteTarget(r)}
              onToggleActive={() => handleToggleActive(r)}
            />
          ))}
        </div>
      )}

      {/* Create / Edit Dialog */}
      <ReminderFormDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        form={form}
        setForm={setForm}
        isEdit={!!editingReminder}
        onSubmit={handleSubmit}
        isSubmitting={createReminder.isPending || updateReminder.isPending}
      />

      {/* Delete Confirm */}
      <AlertDialog open={!!deleteTarget} onOpenChange={(v) => !v && setDeleteTarget(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xoá nhắc nhở?</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn có chắc muốn xoá{" "}
              <span className="font-semibold">"{deleteTarget?.title}"</span>? Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Huỷ</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              Xoá
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

// ============================================
// Reminder Card Component
// ============================================

function ReminderCard({
  reminder,
  onEdit,
  onDelete,
  onToggleActive,
}: {
  reminder: Reminder;
  onEdit: () => void;
  onDelete: () => void;
  onToggleActive: () => void;
}) {
  const typeInfo = getReminderTypeInfo(reminder.reminder_type);

  return (
    <Card className={`transition-all ${!reminder.is_active ? "opacity-60" : ""}`}>
      <CardContent className="flex items-center gap-4 py-4">
        {/* Type Icon */}
        <div className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl ${typeInfo.color}`}>
          {typeInfo.icon}
        </div>

        {/* Info */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <p className="font-semibold truncate">{reminder.title}</p>
            <Badge variant="secondary" className="text-xs shrink-0">
              {typeInfo.label}
            </Badge>
            {!reminder.is_active && (
              <Badge variant="outline" className="text-xs shrink-0 text-muted-foreground">
                Đang tắt
              </Badge>
            )}
          </div>
          {reminder.description && (
            <p className="text-muted-foreground text-sm truncate mt-0.5">{reminder.description}</p>
          )}
          <div className="flex items-center gap-3 mt-1.5 text-xs text-muted-foreground flex-wrap">
            <span className="flex items-center gap-1">
              {reminder.is_lunar ? (
                <Moon className="h-3 w-3" />
              ) : (
                <Sun className="h-3 w-3" />
              )}
              {formatDateLabel(reminder)}
            </span>
            {reminder.is_recurring && (
              <span className="flex items-center gap-1">
                <Calendar className="h-3 w-3" />
                Hàng năm
              </span>
            )}
            {reminder.notify_email && (
              <span className="flex items-center gap-1">
                <Mail className="h-3 w-3" />
                Email
              </span>
            )}
            {reminder.remind_before_days > 0 && (
              <span>Nhắc trước {reminder.remind_before_days} ngày</span>
            )}
          </div>
        </div>

        {/* Actions */}
        <div className="flex items-center gap-1 shrink-0">
          <button
            onClick={onToggleActive}
            className="text-muted-foreground hover:text-foreground transition-colors p-1.5 rounded-md hover:bg-accent"
            title={reminder.is_active ? "Tắt nhắc nhở" : "Bật nhắc nhở"}
          >
            {reminder.is_active ? (
              <ToggleRight className="h-5 w-5 text-primary" />
            ) : (
              <ToggleLeft className="h-5 w-5" />
            )}
          </button>
          <Button variant="ghost" size="icon" onClick={onEdit} className="h-8 w-8">
            <Pencil className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" onClick={onDelete} className="h-8 w-8 text-destructive hover:text-destructive">
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

// ============================================
// Reminder Form Dialog
// ============================================

function ReminderFormDialog({
  open,
  onOpenChange,
  form,
  setForm,
  isEdit,
  onSubmit,
  isSubmitting,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  form: CreateReminderRequest;
  setForm: React.Dispatch<React.SetStateAction<CreateReminderRequest>>;
  isEdit: boolean;
  onSubmit: () => void;
  isSubmitting: boolean;
}) {
  const currentDay = form.is_lunar ? form.lunar_day : form.solar_day;
  const currentMonth = form.is_lunar ? form.lunar_month : form.solar_month;

  const handleDayChange = (val: string) => {
    const n = parseInt(val);
    if (form.is_lunar) setForm((f) => ({ ...f, lunar_day: n }));
    else setForm((f) => ({ ...f, solar_day: n }));
  };

  const handleMonthChange = (val: string) => {
    const n = parseInt(val);
    if (form.is_lunar) setForm((f) => ({ ...f, lunar_month: n }));
    else setForm((f) => ({ ...f, solar_month: n }));
  };

  const handleIsLunarChange = (val: boolean) => {
    setForm((f) => ({
      ...f,
      is_lunar: val,
      solar_day: val ? undefined : (f.lunar_day ?? f.solar_day),
      solar_month: val ? undefined : (f.lunar_month ?? f.solar_month),
      lunar_day: val ? (f.solar_day ?? f.lunar_day) : undefined,
      lunar_month: val ? (f.solar_month ?? f.lunar_month) : undefined,
    }));
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEdit ? "Chỉnh sửa nhắc nhở" : "Thêm nhắc nhở mới"}</DialogTitle>
          <DialogDescription>
            {isEdit
              ? "Cập nhật thông tin nhắc nhở của bạn"
              : "Tạo nhắc nhở cho ngày kỷ niệm, sinh nhật hoặc sự kiện định kỳ"}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-5 py-2">
          {/* Title */}
          <div className="space-y-1.5">
            <Label htmlFor="title">Tiêu đề *</Label>
            <Input
              id="title"
              placeholder="Ví dụ: Sinh nhật mẹ, Ngày cưới..."
              value={form.title}
              onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
            />
          </div>

          {/* Description */}
          <div className="space-y-1.5">
            <Label htmlFor="description">Mô tả</Label>
            <Textarea
              id="description"
              placeholder="Ghi chú thêm (tuỳ chọn)"
              value={form.description ?? ""}
              onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
              rows={2}
              className="resize-none"
            />
          </div>

          {/* Type */}
          <div className="space-y-1.5">
            <Label>Loại nhắc nhở</Label>
            <div className="grid grid-cols-3 gap-2">
              {REMINDER_TYPES.map((t) => (
                <button
                  key={t.value}
                  type="button"
                  onClick={() => setForm((f) => ({ ...f, reminder_type: t.value }))}
                  className={`flex flex-col items-center gap-1.5 rounded-lg border p-3 text-xs font-medium transition-all ${
                    form.reminder_type === t.value
                      ? "border-primary bg-primary/5 text-primary"
                      : "border-border hover:border-primary/40 hover:bg-accent"
                  }`}
                >
                  <span className={`${form.reminder_type === t.value ? "text-primary" : "text-muted-foreground"}`}>
                    {t.icon}
                  </span>
                  {t.label}
                </button>
              ))}
            </div>
          </div>

          <Separator />

          {/* Calendar type toggle */}
          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label className="text-sm">Lịch âm</Label>
              <p className="text-xs text-muted-foreground">
                {form.is_lunar ? "Sử dụng ngày tháng âm lịch" : "Sử dụng ngày tháng dương lịch"}
              </p>
            </div>
            <Switch
              checked={form.is_lunar}
              onCheckedChange={handleIsLunarChange}
            />
          </div>

          {/* Day / Month */}
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label>Ngày *</Label>
              <Select
                value={currentDay ? String(currentDay) : ""}
                onValueChange={handleDayChange}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Chọn ngày" />
                </SelectTrigger>
                <SelectContent className="max-h-56">
                  {DAYS.map((d) => (
                    <SelectItem key={d} value={String(d)}>
                      {d}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label>Tháng *</Label>
              <Select
                value={currentMonth ? String(currentMonth) : ""}
                onValueChange={handleMonthChange}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Chọn tháng" />
                </SelectTrigger>
                <SelectContent className="max-h-56">
                  {MONTHS.map((m, i) => (
                    <SelectItem key={i + 1} value={String(i + 1)}>
                      {m}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <Separator />

          {/* Options */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div className="space-y-0.5">
                <Label className="text-sm">Lặp lại hàng năm</Label>
                <p className="text-xs text-muted-foreground">Nhắc nhở mỗi năm vào ngày này</p>
              </div>
              <Switch
                checked={form.is_recurring ?? true}
                onCheckedChange={(v) => setForm((f) => ({ ...f, is_recurring: v }))}
              />
            </div>

            <div className="flex items-center justify-between">
              <div className="space-y-0.5">
                <Label className="text-sm flex items-center gap-1.5">
                  <Mail className="h-3.5 w-3.5" />
                  Thông báo qua email
                </Label>
                <p className="text-xs text-muted-foreground">Gửi email nhắc nhở vào ngày diễn ra</p>
              </div>
              <Switch
                checked={form.notify_email ?? true}
                onCheckedChange={(v) => setForm((f) => ({ ...f, notify_email: v }))}
              />
            </div>

            <div className="space-y-1.5">
              <Label className="text-sm">Nhắc trước (ngày)</Label>
              <Input
                type="number"
                min={0}
                max={30}
                value={form.remind_before_days ?? 0}
                onChange={(e) =>
                  setForm((f) => ({ ...f, remind_before_days: parseInt(e.target.value) || 0 }))
                }
                className="w-28"
              />
              <p className="text-xs text-muted-foreground">
                0 = nhắc đúng ngày, 1 = nhắc trước 1 ngày, v.v.
              </p>
            </div>
          </div>
        </div>

        <DialogFooter className="gap-2">
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Huỷ
          </Button>
          <Button onClick={onSubmit} disabled={isSubmitting}>
            {isSubmitting ? "Đang lưu..." : isEdit ? "Cập nhật" : "Tạo nhắc nhở"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
