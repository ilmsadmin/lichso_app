"use client";

import { useState, useCallback } from "react";
import {
  Plus,
  CalendarClock,
  Trash2,
  Edit2,
  MessageSquareQuote,
  Landmark,
  Newspaper,
  Crown,
  Sparkles,
  Star,
  Wand2,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Pagination } from "@/components/shared/Pagination";
import { PermissionGate } from "@/components/auth/PermissionGate";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { useDailyContentSchedules, useDeleteDailyContent } from "@/hooks/useV3";
import { DEFAULT_PAGE_SIZE } from "@/lib/constants";
import { DailyContentFormDialog } from "./DailyContentFormDialog";
import { AutoFillWizard } from "./AutoFillWizard";

const CONTENT_TYPE_MAP: Record<string, { label: string; icon: typeof MessageSquareQuote; color: string }> = {
  quote: { label: "Danh ngôn", icon: MessageSquareQuote, color: "text-warm-amber" },
  event: { label: "Sự kiện", icon: Landmark, color: "text-jade-teal" },
  article: { label: "Bài viết", icon: Newspaper, color: "text-blue-500" },
  famous_person: { label: "Nhân vật", icon: Crown, color: "text-purple-500" },
  folk_festival: { label: "Lễ hội", icon: Sparkles, color: "text-pink-500" },
  custom: { label: "Tùy chỉnh", icon: Star, color: "text-orange-500" },
};

const SCHEDULE_MODE_LABELS: Record<string, string> = {
  fixed_date: "Ngày cố định",
  recurring_annual: "Hằng năm",
  day_of_year: "Ngày thứ N",
  lunar_date: "Ngày âm lịch",
};

/** Extract a display label from the resolved `content` object */
function getContentTitle(contentType: string, content: unknown): string {
  if (!content || typeof content !== "object") return "";
  const c = content as Record<string, unknown>;
  switch (contentType) {
    case "quote": {
      const q = (c.quote as string) ?? "";
      return q.length > 60 ? q.slice(0, 60) + "…" : q;
    }
    case "event":
      return (c.title as string) ?? "";
    case "article":
      return (c.title as string) ?? "";
    case "famous_person":
      return (c.name as string) ?? "";
    case "folk_festival":
      return (c.name as string) ?? "";
    default:
      return "";
  }
}

export default function DailyContentPage() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);
  const [filterType, setFilterType] = useState<string>("all");
  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [wizardOpen, setWizardOpen] = useState(false);

  const { data, isLoading } = useDailyContentSchedules({
    page,
    limit,
    content_type: filterType !== "all" ? filterType : undefined,
  });

  const deleteMutation = useDeleteDailyContent();

  const schedules = data?.data ?? [];
  const meta = data?.meta;

  const handleLimitChange = useCallback((newLimit: number) => {
    setLimit(newLimit);
    setPage(1);
  }, []);

  const handleDelete = () => {
    if (deleteId) {
      deleteMutation.mutate(deleteId, {
        onSuccess: () => setDeleteId(null),
      });
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="flex items-center gap-2 text-2xl font-bold tracking-tight">
            <CalendarClock className="text-warm-amber h-6 w-6" />
            Nội dung theo ngày
          </h1>
          <p className="text-muted-foreground">
            Quản lý nội dung danh ngôn, sự kiện, bài viết hiển thị theo ngày trên lịch.
          </p>
        </div>
        <PermissionGate permission="content.create">
          <div className="flex items-center gap-2">
            <Button size="sm" variant="outline" onClick={() => setWizardOpen(true)}>
              <Wand2 className="mr-2 h-4 w-4" />
              Auto-fill
            </Button>
            <Button
              size="sm"
              onClick={() => {
                setEditingId(null);
                setFormOpen(true);
              }}
            >
              <Plus className="mr-2 h-4 w-4" />
              Thêm nội dung
            </Button>
          </div>
        </PermissionGate>
      </div>

      {/* Filters */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <Select
          value={filterType}
          onValueChange={(v) => {
            setFilterType(v);
            setPage(1);
          }}
        >
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Loại nội dung" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            <SelectItem value="quote">Danh ngôn</SelectItem>
            <SelectItem value="event">Sự kiện</SelectItem>
            <SelectItem value="article">Bài viết</SelectItem>
            <SelectItem value="famous_person">Nhân vật</SelectItem>
            <SelectItem value="folk_festival">Lễ hội</SelectItem>
            <SelectItem value="custom">Tùy chỉnh</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Table */}
      {isLoading ? (
        <div className="space-y-3">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-16 rounded-lg" />
          ))}
        </div>
      ) : schedules.length === 0 ? (
        <div className="py-12 text-center">
          <CalendarClock className="text-muted-foreground/30 mx-auto mb-3 h-12 w-12" />
          <p className="text-muted-foreground">Chưa có nội dung nào được lên lịch.</p>
        </div>
      ) : (
        <div className="rounded-lg border">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-muted/50 border-b">
                <th className="px-4 py-3 text-left font-medium">Loại</th>
                <th className="px-4 py-3 text-left font-medium">Nội dung</th>
                <th className="px-4 py-3 text-left font-medium">Lịch trình</th>
                <th className="px-4 py-3 text-left font-medium">Section</th>
                <th className="px-4 py-3 text-center font-medium">Active</th>
                <th className="px-4 py-3 text-right font-medium">Hành động</th>
              </tr>
            </thead>
            <tbody>
              {schedules.map((schedule) => {
                const typeInfo = CONTENT_TYPE_MAP[schedule.content_type] ?? {
                  label: schedule.content_type,
                  icon: Star,
                  color: "text-gray-500",
                };
                const TypeIcon = typeInfo.icon;

                return (
                  <tr key={schedule.id} className="hover:bg-muted/30 border-b transition-colors">
                    {/* Type */}
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-2">
                        <TypeIcon className={`h-4 w-4 ${typeInfo.color}`} />
                        <span className="text-xs font-medium">{typeInfo.label}</span>
                      </div>
                    </td>

                    {/* Content */}
                    <td className="max-w-[250px] px-4 py-3">
                      {schedule.custom_title ? (
                        <span className="block truncate text-xs font-medium">{schedule.custom_title}</span>
                      ) : (() => {
                        const title = getContentTitle(schedule.content_type, schedule.content);
                        return title ? (
                          <span className="block truncate text-xs">{title}</span>
                        ) : schedule.content_id ? (
                          <span className="text-muted-foreground block truncate font-mono text-[10px]">
                            {schedule.content_id.slice(0, 12)}…
                          </span>
                        ) : (
                          <span className="text-muted-foreground text-xs">—</span>
                        );
                      })()}
                    </td>

                    {/* Schedule */}
                    <td className="px-4 py-3">
                      <Badge variant="outline" className="text-xs">
                        {SCHEDULE_MODE_LABELS[schedule.schedule_mode] ?? schedule.schedule_mode}
                      </Badge>
                      <div className="text-muted-foreground mt-0.5 text-[10px]">
                        {schedule.fixed_date && `Ngày: ${schedule.fixed_date}`}
                        {schedule.recurring_month &&
                          schedule.recurring_day &&
                          `${schedule.recurring_day}/${schedule.recurring_month}`}
                        {schedule.lunar_month &&
                          schedule.lunar_day &&
                          `Âm: ${schedule.lunar_day}/${schedule.lunar_month}`}
                        {schedule.day_of_year && `Ngày thứ ${schedule.day_of_year}`}
                      </div>
                    </td>

                    {/* Section */}
                    <td className="text-muted-foreground px-4 py-3 text-xs">
                      {schedule.display_section}
                    </td>

                    {/* Active */}
                    <td className="px-4 py-3 text-center">
                      <span
                        className={`inline-block h-2 w-2 rounded-full ${
                          schedule.is_active ? "bg-green-500" : "bg-gray-300"
                        }`}
                      />
                    </td>

                    {/* Actions */}
                    <td className="px-4 py-3 text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setEditingId(schedule.id);
                            setFormOpen(true);
                          }}
                        >
                          <Edit2 className="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="text-destructive hover:text-destructive"
                          onClick={() => setDeleteId(schedule.id)}
                        >
                          <Trash2 className="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination */}
      {meta && (
        <Pagination
          page={meta.page}
          limit={meta.limit}
          total={meta.total}
          totalPages={meta.total_pages}
          onPageChange={setPage}
          onLimitChange={handleLimitChange}
        />
      )}

      {/* Form Dialog */}
      <DailyContentFormDialog open={formOpen} onOpenChange={setFormOpen} editingId={editingId} />

      {/* Delete confirm */}
      <ConfirmDialog
        open={!!deleteId}
        onOpenChange={(open) => !open && setDeleteId(null)}
        title="Xóa nội dung ngày"
        description="Bạn có chắc chắn muốn xóa nội dung này? Hành động này không thể hoàn tác."
        confirmText="Xóa"
        onConfirm={handleDelete}
        loading={deleteMutation.isPending}
        variant="destructive"
      />

      {/* Auto-fill Wizard */}
      <AutoFillWizard open={wizardOpen} onClose={() => setWizardOpen(false)} />
    </div>
  );
}
