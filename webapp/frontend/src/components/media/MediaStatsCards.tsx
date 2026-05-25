"use client";

import {
  HardDrive,
  Image,
  Film,
  Music,
  FileText,
  FolderOpen,
  Images,
  Heart,
  Trash2,
} from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { useMediaStatsV3 } from "@/hooks/useMediaV3";
import { Skeleton } from "@/components/ui/skeleton";

function formatSize(bytes: number): string {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
}

interface StatsCardProps {
  icon: React.ElementType;
  label: string;
  value: string | number;
  color: string;
}

function StatsCard({ icon: Icon, label, value, color }: StatsCardProps) {
  return (
    <Card className="overflow-hidden">
      <CardContent className="flex items-center gap-3 p-4">
        <div
          className="flex h-10 w-10 items-center justify-center rounded-lg"
          style={{ backgroundColor: `${color}15` }}
        >
          <Icon className="h-5 w-5" style={{ color }} />
        </div>
        <div>
          <p className="text-muted-foreground text-xs">{label}</p>
          <p className="text-lg font-bold tracking-tight">{value}</p>
        </div>
      </CardContent>
    </Card>
  );
}

interface MediaStatsCardsProps {
  className?: string;
}

export function MediaStatsCards({ className }: MediaStatsCardsProps) {
  const { data: statsData, isLoading } = useMediaStatsV3();
  const stats = statsData?.data;

  if (isLoading) {
    return (
      <div className={className}>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
          {Array.from({ length: 5 }).map((_, i) => (
            <Card key={i}>
              <CardContent className="p-4">
                <Skeleton className="h-10 w-full" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  if (!stats) return null;

  const cards: StatsCardProps[] = [
    {
      icon: HardDrive,
      label: "Tổng file",
      value: `${stats.total_files} (${formatSize(stats.total_size)})`,
      color: "#3b82f6",
    },
    {
      icon: Image,
      label: "Hình ảnh",
      value: stats.by_type?.image ?? 0,
      color: "#22c55e",
    },
    {
      icon: Film,
      label: "Video",
      value: stats.by_type?.video ?? 0,
      color: "#a855f7",
    },
    {
      icon: FolderOpen,
      label: "Thư mục",
      value: stats.folder_count,
      color: "#f97316",
    },
    {
      icon: Images,
      label: "Albums",
      value: stats.album_count,
      color: "#06b6d4",
    },
    {
      icon: Heart,
      label: "Yêu thích",
      value: stats.favorite_count,
      color: "#ec4899",
    },
    {
      icon: Trash2,
      label: "Thùng rác",
      value: stats.trash_count,
      color: "#ef4444",
    },
  ];

  return (
    <div className={className}>
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-7">
        {cards.map((card) => (
          <StatsCard key={card.label} {...card} />
        ))}
      </div>
    </div>
  );
}
