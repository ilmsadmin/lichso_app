"use client";

import { use } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Pencil, Trash2, Sparkles, MapPin, Calendar, Hash, ImageIcon, ExternalLink } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { PermissionGate } from "@/components/auth/PermissionGate";
import { useFolkFestival, useDeleteFolkFestival } from "@/hooks/useFolkFestivals";
import { formatDate, getImageUrl } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import { useState } from "react";

const festivalTypeLabels: Record<string, string> = {
  folk_festival: "Lễ hội dân gian",
  religion: "Tôn giáo",
  national_holiday: "Quốc lễ",
  seasonal: "Mùa vụ",
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

export default function FestivalDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const { data, isLoading } = useFolkFestival(id);
  const deleteFestival = useDeleteFolkFestival();
  const [showDelete, setShowDelete] = useState(false);

  const festival = data?.data;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    );
  }

  if (!festival) {
    return (
      <div className="flex flex-col items-center justify-center gap-4 py-24">
        <p className="text-muted-foreground">Không tìm thấy lễ hội.</p>
        <Button variant="outline" onClick={() => router.push(ROUTES.ADMIN_FESTIVALS)}>
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
          <Button variant="ghost" size="icon" onClick={() => router.push(ROUTES.ADMIN_FESTIVALS)}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h1 className="text-2xl font-bold tracking-tight">Chi tiết lễ hội</h1>
            <p className="text-muted-foreground text-sm">Xem và quản lý thông tin lễ hội</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          {festival.slug && (
            <Button variant="outline" size="sm" asChild>
              <a href={`${ROUTES.FESTIVALS}/${festival.slug}`} target="_blank" rel="noopener noreferrer">
                <ExternalLink className="mr-2 h-4 w-4" />
                Xem trang công khai
              </a>
            </Button>
          )}
          <PermissionGate permission="content.update">
            <Button variant="outline" size="sm" asChild>
              <Link href={`${ROUTES.ADMIN_FESTIVALS}/${festival.id}/edit`}>
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
        {festival.image_url ? (
          <div className="relative">
            <img
              src={getImageUrl(festival.image_url)}
              alt={festival.name}
              className="h-56 w-full object-cover"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
            <div className="absolute bottom-0 left-0 right-0 p-6 text-white">
              <h2 className="text-2xl font-bold">{festival.name}</h2>
              {festival.alternate_name && (
                <p className="mt-0.5 text-sm text-white/80">{festival.alternate_name}</p>
              )}
              {festival.short_description && (
                <p className="mt-1 text-sm text-white/70">{festival.short_description}</p>
              )}
              <div className="mt-3 flex flex-wrap items-center gap-2">
                <Badge variant="outline" className="border-white/30 text-white">
                  {festivalTypeLabels[festival.festival_type] || festival.festival_type}
                </Badge>
                <Badge className={importanceColors[festival.importance] || ""}>
                  {importanceLabels[festival.importance] || festival.importance}
                </Badge>
                <Badge variant={festival.is_active ? "default" : "secondary"}>
                  {festival.is_active ? "Hoạt động" : "Ẩn"}
                </Badge>
              </div>
            </div>
          </div>
        ) : (
          <div className="bg-gradient-to-br from-primary/5 via-primary/10 to-primary/5 p-8">
            <div className="flex items-start gap-6">
              <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary">
                <Sparkles className="h-8 w-8" />
              </div>
              <div className="flex-1 min-w-0">
                <h2 className="text-2xl font-bold">{festival.name}</h2>
                {festival.alternate_name && (
                  <p className="text-muted-foreground mt-0.5 text-sm">{festival.alternate_name}</p>
                )}
                {festival.short_description && (
                  <p className="text-muted-foreground mt-2 text-sm">{festival.short_description}</p>
                )}
                <div className="mt-3 flex flex-wrap items-center gap-2">
                  <Badge variant="outline">
                    {festivalTypeLabels[festival.festival_type] || festival.festival_type}
                  </Badge>
                  <Badge className={importanceColors[festival.importance] || ""}>
                    {importanceLabels[festival.importance] || festival.importance}
                  </Badge>
                  <Badge variant={festival.is_active ? "default" : "secondary"}>
                    {festival.is_active ? "Hoạt động" : "Ẩn"}
                  </Badge>
                </div>
              </div>
            </div>
          </div>
        )}
      </Card>

      {/* Gallery */}
      {festival.gallery_urls && festival.gallery_urls.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <ImageIcon className="h-4 w-4 text-primary" />
              Thư viện ảnh
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4">
              {festival.gallery_urls.map((url, index) => (
                <img
                  key={index}
                  src={getImageUrl(url)}
                  alt={`${festival.name} - ${index + 1}`}
                  className="h-32 w-full rounded-lg object-cover transition-transform hover:scale-105"
                />
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      <div className="grid gap-6 md:grid-cols-2">
        {/* Date & Calendar Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <Calendar className="h-4 w-4 text-primary" />
              Thông tin lịch
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Loại lịch</p>
                <p className="text-sm mt-1 font-medium">{festival.calendar_type || "—"}</p>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Ngày Âm lịch</p>
                <p className="text-sm mt-1">
                  {(festival.lunar_day ?? 0) > 0 && (festival.lunar_month ?? 0) > 0
                    ? `${festival.lunar_day}/${festival.lunar_month}`
                    : "—"}
                </p>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Ngày Dương lịch</p>
                <p className="text-sm mt-1">
                  {(festival.solar_day ?? 0) > 0 && (festival.solar_month ?? 0) > 0
                    ? `${festival.solar_day}/${festival.solar_month}`
                    : "—"}
                </p>
              </div>
              {festival.duration_days && (
                <div>
                  <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Thời gian</p>
                  <p className="text-sm mt-1">{festival.duration_days} ngày</p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Location & Classification Card */}
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
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Loại lễ hội</p>
                <Badge variant="outline" className="mt-1">
                  {festivalTypeLabels[festival.festival_type] || festival.festival_type}
                </Badge>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Mức độ quan trọng</p>
                <Badge className={`mt-1 ${importanceColors[festival.importance] || ""}`}>
                  {importanceLabels[festival.importance] || festival.importance}
                </Badge>
              </div>
              {festival.region && (
                <div>
                  <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Vùng miền</p>
                  <p className="text-sm mt-1">{festival.region}</p>
                </div>
              )}
              {festival.country && (
                <div>
                  <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Quốc gia</p>
                  <p className="text-sm mt-1">{festival.country}</p>
                </div>
              )}
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Slug</p>
                <p className="text-sm font-mono mt-1 truncate">{festival.slug}</p>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Ngày tạo</p>
                <p className="text-sm mt-1">{formatDate(festival.created_at)}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Traditions Card */}
      {festival.traditions && festival.traditions.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <Sparkles className="h-4 w-4 text-primary" />
              Phong tục truyền thống
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-2">
              {festival.traditions.map((tradition) => (
                <Badge key={tradition} variant="secondary" className="px-3 py-1">
                  {tradition}
                </Badge>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Tags Card */}
      {festival.tags && festival.tags.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <Hash className="h-4 w-4 text-primary" />
              Tags
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-2">
              {festival.tags.map((tag) => (
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
        title="Xóa lễ hội"
        description={`Bạn có chắc chắn muốn xóa lễ hội "${festival.name}"? Hành động này không thể hoàn tác.`}
        confirmText="Xóa"
        variant="destructive"
        loading={deleteFestival.isPending}
        onConfirm={() => {
          deleteFestival.mutate(festival.id, {
            onSuccess: () => router.push(ROUTES.ADMIN_FESTIVALS),
          });
        }}
      />
    </div>
  );
}
