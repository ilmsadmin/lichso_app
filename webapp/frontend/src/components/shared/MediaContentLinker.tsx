"use client";

import { useState } from "react";
import { ImageIcon, Film, Music, FileText, Plus, X, ExternalLink, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { MediaPickerV3 } from "@/components/shared/MediaPickerV3";
import { useEntityAttachments, useAttachMedia, useDetachMedia } from "@/hooks/useMediaV3";
import { cn, getImageUrl } from "@/lib/utils";
import type { MediaFileV3 } from "@/types/media";

// ============================================
// Types
// ============================================

interface MediaContentLinkerProps {
  /** The entity type (e.g. "article", "event", "festival", "person", "quote") */
  entityType: string;
  /** The entity ID */
  entityId: string;
  /** Field label */
  label?: string;
  /** Description text */
  description?: string;
  /** Max attached media */
  maxAttachments?: number;
  /** Filter media types */
  accept?: string[];
  /** Additional CSS class */
  className?: string;
}

// ============================================
// Helper
// ============================================

function formatFileSize(bytes: number): string {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
}

function getTypeIcon(type: string) {
  if (type === "image") return <ImageIcon className="h-4 w-4" />;
  if (type === "video") return <Film className="h-4 w-4" />;
  if (type === "audio") return <Music className="h-4 w-4" />;
  return <FileText className="h-4 w-4" />;
}

function getAttachmentLabel(type: string) {
  const map: Record<string, string> = {
    featured_image: "Ảnh đại diện",
    og_image: "OG Image",
    gallery: "Thư viện ảnh",
    content: "Nội dung",
    avatar: "Ảnh đại diện",
    cover: "Ảnh bìa",
    thumbnail: "Thumbnail",
  };
  return map[type] || type;
}

// ============================================
// Component
// ============================================

export function MediaContentLinker({
  entityType,
  entityId,
  label = "Media đính kèm",
  description = "Quản lý các file media được liên kết với nội dung này.",
  maxAttachments = 20,
  accept,
  className,
}: MediaContentLinkerProps) {
  const [pickerOpen, setPickerOpen] = useState(false);
  const [detachTarget, setDetachTarget] = useState<{
    mediaId: string;
    attachmentType: string;
  } | null>(null);

  const { data: attachmentsData, isLoading } = useEntityAttachments(entityType, entityId);
  const attachMutation = useAttachMedia();
  const detachMutation = useDetachMedia();

  const attachments = attachmentsData?.data ?? [];
  const canAdd = attachments.length < maxAttachments;

  const handleAttach = (file: MediaFileV3) => {
    attachMutation.mutate({
      media_id: file.id,
      entity_type: entityType,
      entity_id: entityId,
      attachment_type: "content",
    });
    setPickerOpen(false);
  };

  const handleDetach = () => {
    if (!detachTarget) return;
    detachMutation.mutate(
      {
        media_id: detachTarget.mediaId,
        entity_type: entityType,
        entity_id: entityId,
      },
      { onSettled: () => setDetachTarget(null) }
    );
  };

  return (
    <Card className={className}>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-base">{label}</CardTitle>
            <CardDescription className="text-xs">{description}</CardDescription>
          </div>
          {canAdd && (
            <Button type="button" variant="outline" size="sm" onClick={() => setPickerOpen(true)}>
              <Plus className="mr-1.5 h-3.5 w-3.5" />
              Đính kèm
            </Button>
          )}
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="flex items-center justify-center py-6">
            <Loader2 className="text-muted-foreground h-5 w-5 animate-spin" />
          </div>
        ) : attachments.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-6 text-center">
            <ImageIcon className="text-muted-foreground/30 mb-2 h-8 w-8" />
            <p className="text-muted-foreground text-sm">Chưa có media nào được đính kèm</p>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              className="mt-2"
              onClick={() => setPickerOpen(true)}
            >
              <Plus className="mr-1.5 h-3.5 w-3.5" />
              Thêm media
            </Button>
          </div>
        ) : (
          <div className="space-y-2">
            {attachments.map((att) => (
              <div key={att.id} className="flex items-center gap-3 rounded-lg border p-2.5">
                <div className="bg-muted/50 flex h-10 w-10 shrink-0 items-center justify-center rounded border">
                  {getTypeIcon(att.attachment_type)}
                </div>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium">{att.media_id}</p>
                  <div className="mt-0.5 flex items-center gap-2">
                    <Badge variant="secondary" className="px-1.5 py-0 text-[10px]">
                      {getAttachmentLabel(att.attachment_type)}
                    </Badge>
                    {att.caption && (
                      <span className="text-muted-foreground truncate text-xs">{att.caption}</span>
                    )}
                  </div>
                </div>
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  className="text-muted-foreground hover:text-destructive h-7 w-7 shrink-0"
                  onClick={() =>
                    setDetachTarget({
                      mediaId: att.media_id,
                      attachmentType: att.attachment_type,
                    })
                  }
                >
                  <X className="h-3.5 w-3.5" />
                </Button>
              </div>
            ))}
          </div>
        )}
      </CardContent>

      {/* Media Picker */}
      <MediaPickerV3
        open={pickerOpen}
        onOpenChange={setPickerOpen}
        accept={accept}
        title="Chọn media để đính kèm"
        onSelect={handleAttach}
      />

      {/* Confirm Detach */}
      <ConfirmDialog
        open={!!detachTarget}
        onOpenChange={(open) => !open && setDetachTarget(null)}
        title="Gỡ đính kèm"
        description="Bạn có chắc muốn gỡ file media này khỏi nội dung?"
        onConfirm={handleDetach}
        loading={detachMutation.isPending}
      />
    </Card>
  );
}
