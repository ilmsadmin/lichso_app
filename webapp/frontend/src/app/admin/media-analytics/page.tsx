"use client";

import { useMemo } from "react";
import {
  HardDrive,
  Image as ImageIcon,
  Video,
  FileText,
  Star,
  Trash2,
  FolderOpen,
  Album,
  AlertTriangle,
  Copy,
  TrendingUp,
  BarChart3,
  RefreshCw,
  Eye,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Separator } from "@/components/ui/separator";
import { useMediaStatsV3, useDuplicateMedia, useUnusedMedia } from "@/hooks/useMediaV3";
import { cn, getImageUrl } from "@/lib/utils";

// ============================================
// Helpers
// ============================================

function formatBytes(bytes: number): string {
  if (bytes === 0) return "0 B";
  const sizes = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  return `${(bytes / Math.pow(1024, i)).toFixed(i > 1 ? 1 : 0)} ${sizes[i]}`;
}

function formatNumber(n: number): string {
  return n.toLocaleString("vi-VN");
}

// ============================================
// Stat Card
// ============================================

interface StatCardProps {
  label: string;
  value: string | number;
  subtitle?: string;
  icon: React.ElementType;
  iconColor?: string;
  iconBg?: string;
}

function StatCard({
  label,
  value,
  subtitle,
  icon: Icon,
  iconColor = "text-warm-amber",
  iconBg = "bg-warm-amber/10",
}: StatCardProps) {
  return (
    <Card className="border-0 shadow-sm">
      <CardContent className="p-4">
        <div className="flex items-start gap-3">
          <div className={cn("rounded-lg p-2.5", iconBg)}>
            <Icon className={cn("h-5 w-5", iconColor)} />
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-text-muted-ls text-xs font-medium">{label}</p>
            <p className="text-text-dark mt-0.5 text-xl font-bold">
              {typeof value === "number" ? formatNumber(value) : value}
            </p>
            {subtitle && <p className="text-text-muted-ls mt-0.5 text-[10px]">{subtitle}</p>}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

// ============================================
// Progress Bar
// ============================================

interface ProgressBarProps {
  label: string;
  value: number;
  total: number;
  color?: string;
  showSize?: boolean;
}

function ProgressBar({
  label,
  value,
  total,
  color = "bg-warm-amber",
  showSize = false,
}: ProgressBarProps) {
  const percent = total > 0 ? Math.round((value / total) * 100) : 0;
  return (
    <div className="space-y-1.5">
      <div className="flex items-center justify-between text-xs">
        <span className="text-text-mid font-medium">{label}</span>
        <span className="text-text-muted-ls">
          {showSize ? formatBytes(value) : formatNumber(value)} ({percent}%)
        </span>
      </div>
      <div className="bg-warm-cream/80 h-2 overflow-hidden rounded-full">
        <div
          className={cn("h-full rounded-full transition-all duration-500", color)}
          style={{ width: `${Math.max(percent, 1)}%` }}
        />
      </div>
    </div>
  );
}

// ============================================
// Distribution list item (mime type / format)
// ============================================

interface DistributionItemProps {
  label: string;
  count: number;
  total: number;
  emoji?: string;
}

function DistributionItem({ label, count, total, emoji }: DistributionItemProps) {
  const percent = total > 0 ? ((count / total) * 100).toFixed(1) : "0";
  return (
    <div className="flex items-center gap-2.5 py-1.5">
      {emoji && <span className="text-sm">{emoji}</span>}
      <span className="text-text-mid flex-1 truncate text-xs">{label}</span>
      <span className="text-text-dark text-xs font-medium tabular-nums">{formatNumber(count)}</span>
      <Badge variant="secondary" className="min-w-[42px] justify-center text-[10px]">
        {percent}%
      </Badge>
    </div>
  );
}

// ============================================
// Media file row (for duplicate/unused lists)
// ============================================

interface MediaFileRowProps {
  file: {
    id: string;
    original_name: string;
    url: string;
    mime_type: string;
    size: number;
    usage_count?: number;
  };
}

function MediaFileRow({ file }: MediaFileRowProps) {
  const isImage = file.mime_type?.startsWith("image/");
  return (
    <div className="hover:bg-warm-cream/30 flex items-center gap-3 rounded-lg px-1 py-2 transition-colors">
      <div className="bg-warm-cream/50 h-10 w-10 flex-shrink-0 overflow-hidden rounded-md">
        {isImage ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={getImageUrl(file.url)} alt="" className="h-full w-full object-cover" />
        ) : (
          <div className="flex h-full w-full items-center justify-center">
            <FileText className="text-text-muted-ls h-4 w-4" />
          </div>
        )}
      </div>
      <div className="min-w-0 flex-1">
        <p className="text-text-dark truncate text-xs font-medium">{file.original_name}</p>
        <p className="text-text-muted-ls text-[10px]">
          {formatBytes(file.size)} · {file.mime_type}
        </p>
      </div>
      {file.usage_count !== undefined && (
        <Badge variant="outline" className="text-[10px]">
          <Eye className="mr-0.5 h-3 w-3" />
          {file.usage_count}
        </Badge>
      )}
    </div>
  );
}

// ============================================
// MIME type → emoji mapping
// ============================================

function getMimeEmoji(mime: string): string {
  if (mime.includes("jpeg") || mime.includes("jpg")) return "🖼️";
  if (mime.includes("png")) return "🎨";
  if (mime.includes("webp")) return "🌐";
  if (mime.includes("gif")) return "🎞️";
  if (mime.includes("svg")) return "📐";
  if (mime.includes("pdf")) return "📄";
  if (mime.includes("video")) return "🎬";
  if (mime.includes("audio")) return "🎵";
  return "📁";
}

// Type color mapping
const TYPE_COLORS: Record<string, string> = {
  image: "bg-blue-500",
  video: "bg-purple-500",
  document: "bg-emerald-500",
  audio: "bg-orange-500",
};

// ============================================
// Main Page Component
// ============================================

export default function MediaAnalyticsPage() {
  const { data: statsData, isLoading: statsLoading, refetch: refetchStats } = useMediaStatsV3();
  const { data: duplicatesData, isLoading: dupsLoading } = useDuplicateMedia();
  const { data: unusedData, isLoading: unusedLoading } = useUnusedMedia();

  const stats = statsData?.data;
  const duplicates = duplicatesData?.data ?? [];
  const unused = unusedData?.data ?? [];

  // Compute total type count for percentages
  const totalByType = useMemo(() => {
    if (!stats?.by_type) return 0;
    return Object.values(stats.by_type).reduce((a, b) => a + b, 0);
  }, [stats?.by_type]);

  const totalByMime = useMemo(() => {
    if (!stats?.by_mime) return 0;
    return Object.values(stats.by_mime).reduce((a, b) => a + b, 0);
  }, [stats?.by_mime]);

  // Sort mime types by count descending
  const sortedMime = useMemo(() => {
    if (!stats?.by_mime) return [];
    return Object.entries(stats.by_mime).sort(([, a], [, b]) => b - a);
  }, [stats?.by_mime]);

  if (statsLoading) {
    return (
      <div className="space-y-6 p-6">
        <div className="flex items-center justify-between">
          <Skeleton className="h-8 w-48" />
          <Skeleton className="h-9 w-24" />
        </div>
        <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-6">
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className="h-24 rounded-xl" />
          ))}
        </div>
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
          <Skeleton className="h-64 rounded-xl" />
          <Skeleton className="h-64 rounded-xl" />
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 p-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-text-dark flex items-center gap-2 text-2xl font-bold">
            <BarChart3 className="text-warm-amber h-6 w-6" />
            Media Analytics
          </h1>
          <p className="text-text-soft mt-1 text-sm">Thống kê tổng quan về media library</p>
        </div>
        <Button variant="outline" size="sm" onClick={() => refetchStats()}>
          <RefreshCw className="mr-1.5 h-4 w-4" />
          Refresh
        </Button>
      </div>

      {/* Overview Stats Cards */}
      <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-6">
        <StatCard label="Tổng file" value={stats?.total_files ?? 0} icon={HardDrive} />
        <StatCard
          label="Dung lượng"
          value={formatBytes(stats?.total_size ?? 0)}
          subtitle={`Max: ${formatBytes(stats?.max_size ?? 0)}`}
          icon={HardDrive}
          iconColor="text-blue-600"
          iconBg="bg-blue-100"
        />
        <StatCard
          label="Thư mục"
          value={stats?.folder_count ?? 0}
          icon={FolderOpen}
          iconColor="text-emerald-600"
          iconBg="bg-emerald-100"
        />
        <StatCard
          label="Albums"
          value={stats?.album_count ?? 0}
          icon={Album}
          iconColor="text-purple-600"
          iconBg="bg-purple-100"
        />
        <StatCard
          label="Yêu thích"
          value={stats?.favorite_count ?? 0}
          icon={Star}
          iconColor="text-yellow-600"
          iconBg="bg-yellow-100"
        />
        <StatCard
          label="Thùng rác"
          value={stats?.trash_count ?? 0}
          icon={Trash2}
          iconColor="text-red-600"
          iconBg="bg-red-100"
        />
      </div>

      {/* Charts Row */}
      <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
        {/* Type Distribution */}
        <Card className="border-0 shadow-sm">
          <CardHeader className="pb-3">
            <CardTitle className="flex items-center gap-2 text-sm font-semibold">
              <TrendingUp className="text-warm-amber h-4 w-4" />
              Phân bổ theo loại
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {stats?.by_type ? (
              Object.entries(stats.by_type)
                .sort(([, a], [, b]) => b - a)
                .map(([type, count]) => (
                  <ProgressBar
                    key={type}
                    label={typeLabel(type)}
                    value={count}
                    total={totalByType}
                    color={TYPE_COLORS[type] ?? "bg-gray-400"}
                  />
                ))
            ) : (
              <p className="text-text-muted-ls py-8 text-center text-xs">Chưa có dữ liệu</p>
            )}

            {/* Visual pie-like summary */}
            {stats?.by_type && totalByType > 0 && (
              <>
                <Separator className="my-3" />
                <div className="flex flex-wrap items-center gap-2">
                  {Object.entries(stats.by_type)
                    .sort(([, a], [, b]) => b - a)
                    .map(([type, count]) => {
                      const percent = Math.round((count / totalByType) * 100);
                      return (
                        <div key={type} className="flex items-center gap-1.5">
                          <div
                            className={cn(
                              "h-2.5 w-2.5 rounded-full",
                              TYPE_COLORS[type] ?? "bg-gray-400"
                            )}
                          />
                          <span className="text-text-muted-ls text-[10px]">
                            {typeLabel(type)} ({percent}%)
                          </span>
                        </div>
                      );
                    })}
                </div>
              </>
            )}
          </CardContent>
        </Card>

        {/* MIME Type Distribution */}
        <Card className="border-0 shadow-sm">
          <CardHeader className="pb-3">
            <CardTitle className="flex items-center gap-2 text-sm font-semibold">
              <FileText className="text-warm-amber h-4 w-4" />
              Phân bổ theo định dạng
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="divide-border/50 divide-y">
              {sortedMime.length > 0 ? (
                sortedMime
                  .slice(0, 10)
                  .map(([mime, count]) => (
                    <DistributionItem
                      key={mime}
                      label={mime}
                      count={count}
                      total={totalByMime}
                      emoji={getMimeEmoji(mime)}
                    />
                  ))
              ) : (
                <p className="text-text-muted-ls py-8 text-center text-xs">Chưa có dữ liệu</p>
              )}
            </div>
            {sortedMime.length > 10 && (
              <p className="text-text-muted-ls mt-2 text-center text-[10px]">
                +{sortedMime.length - 10} định dạng khác
              </p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Issues Row */}
      <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
        {/* Unused Media */}
        <Card className="border-0 shadow-sm">
          <CardHeader className="pb-3">
            <CardTitle className="flex items-center gap-2 text-sm font-semibold">
              <AlertTriangle className="h-4 w-4 text-orange-500" />
              Media chưa sử dụng
              {!unusedLoading && (
                <Badge variant="secondary" className="ml-auto text-[10px]">
                  {formatNumber(unused.length)} file
                </Badge>
              )}
            </CardTitle>
          </CardHeader>
          <CardContent>
            {unusedLoading ? (
              <div className="space-y-2">
                {Array.from({ length: 3 }).map((_, i) => (
                  <Skeleton key={i} className="h-14 rounded-lg" />
                ))}
              </div>
            ) : unused.length === 0 ? (
              <div className="py-8 text-center">
                <span className="text-2xl">✅</span>
                <p className="text-text-muted-ls mt-2 text-xs">
                  Tất cả media đều đang được sử dụng
                </p>
              </div>
            ) : (
              <div className="max-h-[320px] space-y-1 overflow-y-auto pr-1">
                {unused.slice(0, 20).map((file) => (
                  <MediaFileRow key={file.id} file={file} />
                ))}
                {unused.length > 20 && (
                  <p className="text-text-muted-ls py-2 text-center text-[10px]">
                    +{unused.length - 20} file khác
                  </p>
                )}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Duplicate Media */}
        <Card className="border-0 shadow-sm">
          <CardHeader className="pb-3">
            <CardTitle className="flex items-center gap-2 text-sm font-semibold">
              <Copy className="h-4 w-4 text-red-500" />
              Media trùng lặp
              {!dupsLoading && (
                <Badge variant="secondary" className="ml-auto text-[10px]">
                  {formatNumber(duplicates.length)} file
                </Badge>
              )}
            </CardTitle>
          </CardHeader>
          <CardContent>
            {dupsLoading ? (
              <div className="space-y-2">
                {Array.from({ length: 3 }).map((_, i) => (
                  <Skeleton key={i} className="h-14 rounded-lg" />
                ))}
              </div>
            ) : duplicates.length === 0 ? (
              <div className="py-8 text-center">
                <span className="text-2xl">✅</span>
                <p className="text-text-muted-ls mt-2 text-xs">Không phát hiện file trùng lặp</p>
              </div>
            ) : (
              <div className="max-h-[320px] space-y-1 overflow-y-auto pr-1">
                {duplicates.slice(0, 20).map((file) => (
                  <MediaFileRow key={file.id} file={file} />
                ))}
                {duplicates.length > 20 && (
                  <p className="text-text-muted-ls py-2 text-center text-[10px]">
                    +{duplicates.length - 20} file khác
                  </p>
                )}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Storage Optimization Summary */}
      {stats && (
        <Card className="border-0 shadow-sm">
          <CardHeader className="pb-3">
            <CardTitle className="flex items-center gap-2 text-sm font-semibold">
              <HardDrive className="text-warm-amber h-4 w-4" />
              Tóm tắt dung lượng
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
              <div className="bg-warm-cream/30 rounded-lg p-3 text-center">
                <p className="text-text-dark text-lg font-bold">{formatBytes(stats.total_size)}</p>
                <p className="text-text-muted-ls mt-0.5 text-[10px]">Tổng dung lượng</p>
              </div>
              <div className="bg-warm-cream/30 rounded-lg p-3 text-center">
                <p className="text-text-dark text-lg font-bold">
                  {stats.total_files > 0
                    ? formatBytes(Math.round(stats.total_size / stats.total_files))
                    : "0 B"}
                </p>
                <p className="text-text-muted-ls mt-0.5 text-[10px]">TB / file</p>
              </div>
              <div className="rounded-lg bg-orange-50 p-3 text-center">
                <p className="text-lg font-bold text-orange-600">{unused.length}</p>
                <p className="text-text-muted-ls mt-0.5 text-[10px]">File chưa dùng</p>
              </div>
              <div className="rounded-lg bg-red-50 p-3 text-center">
                <p className="text-lg font-bold text-red-600">{duplicates.length}</p>
                <p className="text-text-muted-ls mt-0.5 text-[10px]">File trùng lặp</p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

// ============================================
// Helpers
// ============================================

function typeLabel(type: string): string {
  switch (type) {
    case "image":
      return "Hình ảnh";
    case "video":
      return "Video";
    case "document":
      return "Tài liệu";
    case "audio":
      return "Audio";
    default:
      return type;
  }
}
