"use client";

import { use } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Pencil, Trash2, Calendar, MapPin, Hash, Info, ImageIcon, ExternalLink } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { PermissionGate } from "@/components/auth/PermissionGate";
import { useEvent, useDeleteEvent } from "@/hooks/useEvents";
import { formatDate, getImageUrl } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import { useState } from "react";

const eventTypeLabels: Record<string, string> = {
  historical_event: "Lịch sử",
  national_day: "Ngày lễ quốc gia",
  world_day: "Ngày quốc tế",
  anniversary: "Kỷ niệm",
  cultural: "Văn hóa",
  military: "Quân sự",
  other: "Khác",
};

const importanceLabels: Record<string, string> = {
  high: "Cao",
  medium: "Trung bình",
  low: "Thấp",
};

const importanceColors: Record<string, string> = {
  high: "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400",
  medium: "bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400",
  low: "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400",
};

export default function EventDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const { data, isLoading } = useEvent(id);
  const deleteEvent = useDeleteEvent();
  const [showDelete, setShowDelete] = useState(false);

  const event = data?.data;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    );
  }

  if (!event) {
    return (
      <div className="flex flex-col items-center justify-center gap-4 py-24">
        <p className="text-muted-foreground">Không tìm thấy sự kiện.</p>
        <Button variant="outline" onClick={() => router.push(ROUTES.ADMIN_EVENTS)}>
          Quay lại danh sách
        </Button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => router.push(ROUTES.ADMIN_EVENTS)}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h1 className="text-2xl font-bold tracking-tight">Chi tiết sự kiện</h1>
            <p className="text-muted-foreground text-sm">Xem và quản lý thông tin sự kiện</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          {event.slug && (
            <Button variant="outline" size="sm" asChild>
              <a href={`${ROUTES.EVENTS}/${event.slug}`} target="_blank" rel="noopener noreferrer">
                <ExternalLink className="mr-2 h-4 w-4" />
                Xem trang công khai
              </a>
            </Button>
          )}
          <PermissionGate permission="content.update">
            <Button variant="outline" size="sm" asChild>
              <Link href={`${ROUTES.ADMIN_EVENTS}/${event.id}/edit`}>
                <Pencil className="mr-2 h-4 w-4" />
                Chỉnh sửa
              </Link>
            </Button>
          </PermissionGate>
          <PermissionGate permission="content.delete">
            <Button
              variant="outline"
              size="sm"
              className="text-destructive hover:text-destructive"
              onClick={() => setShowDelete(true)}
            >
              <Trash2 className="mr-2 h-4 w-4" />
              Xóa
            </Button>
          </PermissionGate>
        </div>
      </div>

      {/* Hero Card with Image */}
      <Card className="overflow-hidden">
        {event.image_url ? (
          <div className="relative">
            <img
              src={getImageUrl(event.image_url)}
              alt={event.title}
              className="h-56 w-full object-cover"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
            <div className="absolute bottom-0 left-0 right-0 p-6 text-white">
              <h2 className="text-2xl font-bold">{event.title}</h2>
              {event.short_description && (
                <p className="mt-1 text-sm text-white/80">{event.short_description}</p>
              )}
              <div className="mt-3 flex flex-wrap items-center gap-2">
                <Badge variant="outline" className="border-white/30 text-white">
                  {eventTypeLabels[event.event_type] || event.event_type}
                </Badge>
                <Badge className={importanceColors[event.importance] || ""}>
                  {importanceLabels[event.importance] || event.importance}
                </Badge>
                <Badge variant={event.is_active ? "default" : "secondary"}>
                  {event.is_active ? "Hoạt động" : "Ẩn"}
                </Badge>
              </div>
            </div>
          </div>
        ) : (
          <div className="bg-gradient-to-br from-primary/5 via-primary/10 to-primary/5 p-8">
            <div className="flex items-start gap-6">
              <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary">
                <Calendar className="h-8 w-8" />
              </div>
              <div className="flex-1 min-w-0">
                <h2 className="text-2xl font-bold">{event.title}</h2>
                {event.short_description && (
                  <p className="text-muted-foreground mt-1 text-sm">{event.short_description}</p>
                )}
                <div className="mt-3 flex flex-wrap items-center gap-2">
                  <Badge variant="outline">
                    {eventTypeLabels[event.event_type] || event.event_type}
                  </Badge>
                  <Badge className={importanceColors[event.importance] || ""}>
                    {importanceLabels[event.importance] || event.importance}
                  </Badge>
                  <Badge variant={event.is_active ? "default" : "secondary"}>
                    {event.is_active ? "Hoạt động" : "Ẩn"}
                  </Badge>
                </div>
              </div>
            </div>
          </div>
        )}
      </Card>

      <div className="grid gap-6 md:grid-cols-2">
        {/* Date Info Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <Calendar className="h-4 w-4 text-primary" />
              Thông tin thời gian
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Ngày sự kiện</p>
                <p className="text-sm mt-1 font-medium">{event.event_date ? formatDate(event.event_date) : "—"}</p>
              </div>
              {(event.event_day || event.event_month || event.event_year) && (
                <div>
                  <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Ngày/Tháng/Năm</p>
                  <p className="text-sm mt-1">
                    {[event.event_day, event.event_month, event.event_year].filter(Boolean).join("/")}
                  </p>
                </div>
              )}
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Lặp lại hàng năm</p>
                <Badge variant={event.is_recurring ? "default" : "secondary"} className="mt-1">
                  {event.is_recurring ? "Có" : "Không"}
                </Badge>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Ngày tạo</p>
                <p className="text-sm mt-1">{formatDate(event.created_at)}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Location & Type Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <MapPin className="h-4 w-4 text-primary" />
              Phân loại & Vị trí
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Loại sự kiện</p>
                <Badge variant="outline" className="mt-1">
                  {eventTypeLabels[event.event_type] || event.event_type}
                </Badge>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Mức độ quan trọng</p>
                <Badge className={`mt-1 ${importanceColors[event.importance] || ""}`}>
                  {importanceLabels[event.importance] || event.importance}
                </Badge>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Quốc gia</p>
                <p className="text-sm mt-1">
                  {event.flag_emoji && `${event.flag_emoji} `}
                  {event.country || "—"}
                  {event.country_code && ` (${event.country_code})`}
                </p>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Slug</p>
                <p className="text-sm font-mono mt-1 truncate">{event.slug}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Tags Card */}
      {event.tags && event.tags.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <Hash className="h-4 w-4 text-primary" />
              Tags
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-2">
              {event.tags.map((tag) => (
                <Badge key={tag} variant="outline" className="px-3 py-1">
                  {tag}
                </Badge>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Delete Dialog */}
      <ConfirmDialog
        open={showDelete}
        onOpenChange={setShowDelete}
        title="Xóa sự kiện"
        description={`Bạn có chắc chắn muốn xóa sự kiện "${event.title}"? Hành động này không thể hoàn tác.`}
        confirmText="Xóa"
        variant="destructive"
        loading={deleteEvent.isPending}
        onConfirm={() => {
          deleteEvent.mutate(event.id, {
            onSuccess: () => router.push(ROUTES.ADMIN_EVENTS),
          });
        }}
      />
    </div>
  );
}
