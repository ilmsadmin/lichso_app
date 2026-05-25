"use client";

import { useState } from "react";
import {
  Trash2,
  RotateCcw,
  AlertTriangle,
  Loader2,
  Image,
  Film,
  Music,
  FileText,
  File,
} from "lucide-react";
import { formatDistanceToNow } from "date-fns";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Pagination } from "@/components/shared/Pagination";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { cn, getImageUrl } from "@/lib/utils";
import {
  useTrashedMedia,
  useRestoreMedia,
  usePermanentDeleteMedia,
  useEmptyTrash,
} from "@/hooks/useMediaV3";
import type { MediaFileV3 } from "@/types/media";

function getFileIcon(mimeType: string) {
  if (mimeType.startsWith("image/")) return Image;
  if (mimeType.startsWith("video/")) return Film;
  if (mimeType.startsWith("audio/")) return Music;
  if (mimeType.includes("pdf") || mimeType.includes("document") || mimeType.includes("text"))
    return FileText;
  return File;
}

function formatFileSize(bytes: number): string {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
}

interface TrashManagerProps {
  className?: string;
}

export function TrashManager({ className }: TrashManagerProps) {
  const [page, setPage] = useState(1);
  const { data: trashData, isLoading } = useTrashedMedia(page);
  const restoreMedia = useRestoreMedia();
  const permanentDelete = usePermanentDeleteMedia();
  const emptyTrash = useEmptyTrash();

  const [emptyConfirmOpen, setEmptyConfirmOpen] = useState(false);
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<string | null>(null);

  const files = trashData?.data ?? [];
  const meta = trashData?.meta;

  return (
    <div className={cn("space-y-4", className)}>
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Trash2 className="text-muted-foreground h-5 w-5" />
          <h3 className="font-semibold">Thùng rác</h3>
          <Badge variant="secondary" className="text-xs">
            {meta?.total ?? 0}
          </Badge>
        </div>
        {files.length > 0 && (
          <Button variant="destructive" size="sm" onClick={() => setEmptyConfirmOpen(true)}>
            <Trash2 className="mr-1.5 h-3.5 w-3.5" />
            Dọn sạch
          </Button>
        )}
      </div>

      {/* File list */}
      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="text-muted-foreground h-6 w-6 animate-spin" />
        </div>
      ) : files.length === 0 ? (
        <div className="bg-muted/20 rounded-lg border py-12 text-center">
          <Trash2 className="text-muted-foreground/40 mx-auto mb-3 h-10 w-10" />
          <p className="text-muted-foreground text-sm">Thùng rác trống</p>
        </div>
      ) : (
        <div className="rounded-lg border">
          {files.map((file) => {
            const Icon = getFileIcon(file.mime_type);
            const isImage = file.mime_type.startsWith("image/");
            return (
              <div
                key={file.id}
                className="hover:bg-muted/20 flex items-center gap-3 border-b p-3 transition-colors last:border-b-0"
              >
                {/* Thumb */}
                <div className="bg-muted/40 flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded">
                  {isImage ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img
                      src={getImageUrl(file.url)}
                      alt={file.original_name}
                      className="h-full w-full object-cover"
                    />
                  ) : (
                    <Icon className="text-muted-foreground/50 h-5 w-5" />
                  )}
                </div>

                {/* Info */}
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium">{file.original_name}</p>
                  <p className="text-muted-foreground text-xs">
                    {formatFileSize(file.size)} ·{" "}
                    {file.deleted_at &&
                      `Đã xóa ${formatDistanceToNow(new Date(file.deleted_at), {
                        addSuffix: true,
                      })}`}
                  </p>
                </div>

                {/* Actions */}
                <div className="flex gap-1.5">
                  <Button
                    variant="outline"
                    size="sm"
                    className="h-7 text-xs"
                    onClick={() => restoreMedia.mutate(file.id)}
                    disabled={restoreMedia.isPending}
                  >
                    <RotateCcw className="mr-1 h-3 w-3" />
                    Khôi phục
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    className="text-destructive hover:text-destructive h-7 text-xs"
                    onClick={() => {
                      setDeleteTarget(file.id);
                      setDeleteConfirmOpen(true);
                    }}
                  >
                    <Trash2 className="mr-1 h-3 w-3" />
                    Xóa vĩnh viễn
                  </Button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Pagination */}
      {meta && meta.total_pages > 1 && (
        <Pagination
          page={page}
          limit={24}
          total={meta.total}
          totalPages={meta.total_pages}
          onPageChange={setPage}
          onLimitChange={() => {}}
        />
      )}

      {/* Empty trash confirm */}
      <ConfirmDialog
        open={emptyConfirmOpen}
        onOpenChange={setEmptyConfirmOpen}
        title="Dọn sạch thùng rác"
        description="Tất cả file trong thùng rác sẽ bị xóa vĩnh viễn. Không thể hoàn tác!"
        confirmText="Dọn sạch"
        cancelText="Hủy"
        variant="destructive"
        onConfirm={() => {
          emptyTrash.mutate(undefined, {
            onSuccess: () => setEmptyConfirmOpen(false),
          });
        }}
        loading={emptyTrash.isPending}
      />

      {/* Permanent delete confirm */}
      <ConfirmDialog
        open={deleteConfirmOpen}
        onOpenChange={setDeleteConfirmOpen}
        title="Xóa vĩnh viễn"
        description="File này sẽ bị xóa vĩnh viễn và không thể khôi phục. Bạn chắc chắn?"
        confirmText="Xóa"
        cancelText="Hủy"
        variant="destructive"
        onConfirm={() => {
          if (deleteTarget) {
            permanentDelete.mutate(deleteTarget, {
              onSuccess: () => {
                setDeleteConfirmOpen(false);
                setDeleteTarget(null);
              },
            });
          }
        }}
        loading={permanentDelete.isPending}
      />
    </div>
  );
}
