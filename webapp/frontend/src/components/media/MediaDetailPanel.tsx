"use client";

import { useState } from "react";
import {
  X,
  Download,
  Star,
  Copy,
  Pencil,
  Trash2,
  RotateCw,
  Image,
  Film,
  Music,
  FileText,
  File,
  Link2,
  Info,
  Loader2,
  ExternalLink,
  Eye,
  Crop,
} from "lucide-react";
import { formatDistanceToNow } from "date-fns";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { Sheet, SheetContent, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { cn, getImageUrl } from "@/lib/utils";
import {
  useMediaDetailV3,
  useMediaVariants,
  useMediaUsages,
  useUpdateMediaV3,
  useToggleFavorite,
  useSoftDeleteMedia,
  useRegenerateVariants,
} from "@/hooks/useMediaV3";
import type { MediaFileV3, MediaVariant } from "@/types/media";
import { ImageEditor } from "@/components/media/ImageEditor";

// ============================================
// Helpers
// ============================================

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

// ============================================
// Types
// ============================================

interface MediaDetailPanelProps {
  mediaId: string | null;
  open: boolean;
  onClose: () => void;
  onDeleted?: () => void;
}

// ============================================
// MediaDetailPanel Component
// ============================================

export function MediaDetailPanel({ mediaId, open, onClose, onDeleted }: MediaDetailPanelProps) {
  const { data: detailData, isLoading } = useMediaDetailV3(open ? mediaId : null);
  const { data: variantsData } = useMediaVariants(open ? mediaId : null);
  const { data: usagesData } = useMediaUsages(open ? mediaId : null);

  const updateMedia = useUpdateMediaV3();
  const toggleFav = useToggleFavorite();
  const softDelete = useSoftDeleteMedia();
  const regenVariants = useRegenerateVariants();

  const [isEditing, setIsEditing] = useState(false);
  const [imageEditorOpen, setImageEditorOpen] = useState(false);
  const [editAlt, setEditAlt] = useState("");
  const [editDescription, setEditDescription] = useState("");
  const [editCaption, setEditCaption] = useState("");
  const [editCredit, setEditCredit] = useState("");
  const [editTags, setEditTags] = useState("");

  const media = detailData?.data;
  const variants = variantsData?.data ?? [];
  const usages = usagesData?.data ?? [];

  const handleStartEdit = () => {
    if (!media) return;
    setEditAlt(media.alt || "");
    setEditDescription(media.description || "");
    setEditCaption(media.caption || "");
    setEditCredit(media.credit || "");
    setEditTags(media.tags?.join(", ") || "");
    setIsEditing(true);
  };

  const handleSaveEdit = () => {
    if (!media) return;
    updateMedia.mutate(
      {
        id: media.id,
        data: {
          alt: editAlt || undefined,
          description: editDescription || undefined,
          caption: editCaption || undefined,
          credit: editCredit || undefined,
          tags: editTags
            .split(",")
            .map((t) => t.trim())
            .filter(Boolean),
        },
      },
      { onSuccess: () => setIsEditing(false) }
    );
  };

  const handleToggleFavorite = () => {
    if (!media) return;
    toggleFav.mutate({ id: media.id, isFavorite: !media.is_favorite });
  };

  const handleDelete = () => {
    if (!media) return;
    softDelete.mutate(media.id, {
      onSuccess: () => {
        onClose();
        onDeleted?.();
      },
    });
  };

  const handleCopyUrl = () => {
    if (!media) return;
    navigator.clipboard.writeText(getImageUrl(media.url));
    toast.success("URL đã sao chép");
  };

  const handleRegenVariants = () => {
    if (!media) return;
    regenVariants.mutate(media.id);
  };

  const isImage = media?.mime_type?.startsWith("image/");

  return (
    <>
      <Sheet open={open} onOpenChange={(o) => !o && onClose()}>
        <SheetContent
          side="right"
          className="w-full overflow-y-auto sm:max-w-lg"
          showCloseButton={false}
        >
          {/* Custom header */}
          <SheetHeader className="flex-row items-center justify-between gap-2 space-y-0 border-b p-4">
            <SheetTitle className="truncate text-base">
              {media?.original_name || "Chi tiết file"}
            </SheetTitle>
            <Button variant="ghost" size="icon" className="h-8 w-8" onClick={onClose}>
              <X className="h-4 w-4" />
            </Button>
          </SheetHeader>

          {isLoading || !media ? (
            <div className="flex items-center justify-center py-20">
              <Loader2 className="text-muted-foreground h-8 w-8 animate-spin" />
            </div>
          ) : (
            <div className="space-y-5 p-4">
              {/* Preview */}
              <div className="bg-muted/30 flex items-center justify-center overflow-hidden rounded-lg">
                {isImage ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    src={getImageUrl(media.url)}
                    alt={media.alt || media.original_name}
                    className="max-h-[40vh] w-full object-contain"
                    style={{
                      backgroundColor: media.dominant_color || undefined,
                    }}
                  />
                ) : media.mime_type.startsWith("video/") ? (
                  <video src={getImageUrl(media.url)} controls className="max-h-[40vh] w-full" />
                ) : media.mime_type.startsWith("audio/") ? (
                  <div className="p-8">
                    <audio src={getImageUrl(media.url)} controls className="w-full" />
                  </div>
                ) : (
                  <div className="flex flex-col items-center py-12">
                    {(() => {
                      const FIcon = getFileIcon(media.mime_type);
                      return <FIcon className="text-muted-foreground/40 h-16 w-16" />;
                    })()}
                  </div>
                )}
              </div>

              {/* Quick Actions */}
              <div className="flex flex-wrap gap-2">
                <Button variant="outline" size="sm" onClick={handleToggleFavorite}>
                  <Star
                    className={cn(
                      "mr-1.5 h-3.5 w-3.5",
                      media.is_favorite && "fill-yellow-500 text-yellow-500"
                    )}
                  />
                  {media.is_favorite ? "Bỏ thích" : "Yêu thích"}
                </Button>
                <Button variant="outline" size="sm" onClick={handleCopyUrl}>
                  <Copy className="mr-1.5 h-3.5 w-3.5" />
                  Sao chép URL
                </Button>
                <Button variant="outline" size="sm" asChild>
                  <a
                    href={getImageUrl(media.url)}
                    download={media.original_name}
                    target="_blank"
                    rel="noreferrer"
                  >
                    <Download className="mr-1.5 h-3.5 w-3.5" />
                    Tải xuống
                  </a>
                </Button>
                {!isEditing && (
                  <Button variant="outline" size="sm" onClick={handleStartEdit}>
                    <Pencil className="mr-1.5 h-3.5 w-3.5" />
                    Sửa
                  </Button>
                )}
                {isImage && (
                  <Button variant="outline" size="sm" onClick={() => setImageEditorOpen(true)}>
                    <Crop className="mr-1.5 h-3.5 w-3.5" />
                    Chỉnh ảnh
                  </Button>
                )}
                <Button
                  variant="outline"
                  size="sm"
                  className="text-destructive hover:text-destructive"
                  onClick={handleDelete}
                  disabled={softDelete.isPending}
                >
                  <Trash2 className="mr-1.5 h-3.5 w-3.5" />
                  Xóa
                </Button>
              </div>

              <Separator />

              {/* Edit Form */}
              {isEditing ? (
                <div className="space-y-3">
                  <div>
                    <Label htmlFor="detail-alt">Alt text</Label>
                    <Input
                      id="detail-alt"
                      value={editAlt}
                      onChange={(e) => setEditAlt(e.target.value)}
                      placeholder="Mô tả hình ảnh..."
                    />
                  </div>
                  <div>
                    <Label htmlFor="detail-caption">Caption</Label>
                    <Input
                      id="detail-caption"
                      value={editCaption}
                      onChange={(e) => setEditCaption(e.target.value)}
                      placeholder="Chú thích..."
                    />
                  </div>
                  <div>
                    <Label htmlFor="detail-credit">Credit</Label>
                    <Input
                      id="detail-credit"
                      value={editCredit}
                      onChange={(e) => setEditCredit(e.target.value)}
                      placeholder="Nguồn ảnh..."
                    />
                  </div>
                  <div>
                    <Label htmlFor="detail-desc">Mô tả</Label>
                    <Textarea
                      id="detail-desc"
                      value={editDescription}
                      onChange={(e) => setEditDescription(e.target.value)}
                      placeholder="Mô tả chi tiết..."
                      rows={3}
                    />
                  </div>
                  <div>
                    <Label htmlFor="detail-tags">Tags (cách nhau bởi dấu phẩy)</Label>
                    <Input
                      id="detail-tags"
                      value={editTags}
                      onChange={(e) => setEditTags(e.target.value)}
                      placeholder="tag1, tag2, tag3"
                    />
                  </div>
                  <div className="flex gap-2">
                    <Button size="sm" onClick={handleSaveEdit} disabled={updateMedia.isPending}>
                      {updateMedia.isPending && (
                        <Loader2 className="mr-1.5 h-3.5 w-3.5 animate-spin" />
                      )}
                      Lưu
                    </Button>
                    <Button variant="outline" size="sm" onClick={() => setIsEditing(false)}>
                      Hủy
                    </Button>
                  </div>
                </div>
              ) : (
                /* Info Display */
                <div className="space-y-3 text-sm">
                  <h4 className="flex items-center gap-1.5 font-semibold">
                    <Info className="h-4 w-4" />
                    Thông tin
                  </h4>
                  <div className="grid grid-cols-2 gap-x-4 gap-y-2">
                    <div>
                      <span className="text-muted-foreground text-xs">Tên file</span>
                      <p className="truncate font-medium">{media.original_name}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground text-xs">Kích thước</span>
                      <p className="font-medium">{formatFileSize(media.size)}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground text-xs">Loại</span>
                      <p className="font-medium">{media.mime_type}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground text-xs">Định dạng</span>
                      <p className="font-medium uppercase">{media.extension}</p>
                    </div>
                    {media.dimensions && (
                      <div>
                        <span className="text-muted-foreground text-xs">Kích thước ảnh</span>
                        <p className="font-medium">
                          {media.dimensions.width} × {media.dimensions.height}
                        </p>
                      </div>
                    )}
                    {media.duration && media.duration > 0 && (
                      <div>
                        <span className="text-muted-foreground text-xs">Thời lượng</span>
                        <p className="font-medium">
                          {Math.floor(media.duration / 60)}:
                          {String(Math.floor(media.duration % 60)).padStart(2, "0")}
                        </p>
                      </div>
                    )}
                    <div>
                      <span className="text-muted-foreground text-xs">Upload bởi</span>
                      <p className="font-medium">{media.uploaded_name || "Unknown"}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground text-xs">Ngày tải lên</span>
                      <p className="font-medium">
                        {formatDistanceToNow(new Date(media.created_at), {
                          addSuffix: true,
                        })}
                      </p>
                    </div>
                    <div>
                      <span className="text-muted-foreground text-xs">Sử dụng</span>
                      <p className="font-medium">{media.usage_count} lần</p>
                    </div>
                    {media.process_status && (
                      <div>
                        <span className="text-muted-foreground text-xs">Trạng thái xử lý</span>
                        <Badge
                          variant={
                            media.process_status === "completed"
                              ? "default"
                              : media.process_status === "failed"
                                ? "destructive"
                                : "secondary"
                          }
                          className="text-[10px]"
                        >
                          {media.process_status}
                        </Badge>
                      </div>
                    )}
                  </div>

                  {/* Tags */}
                  {media.tags && media.tags.length > 0 && (
                    <div>
                      <span className="text-muted-foreground text-xs">Tags</span>
                      <div className="mt-1 flex flex-wrap gap-1">
                        {media.tags.map((tag) => (
                          <Badge key={tag} variant="outline" className="text-xs">
                            {tag}
                          </Badge>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Caption/Credit */}
                  {(media.alt || media.caption || media.credit) && (
                    <div className="space-y-1.5">
                      {media.alt && (
                        <div>
                          <span className="text-muted-foreground text-xs">Alt text</span>
                          <p className="text-sm">{media.alt}</p>
                        </div>
                      )}
                      {media.caption && (
                        <div>
                          <span className="text-muted-foreground text-xs">Caption</span>
                          <p className="text-sm">{media.caption}</p>
                        </div>
                      )}
                      {media.credit && (
                        <div>
                          <span className="text-muted-foreground text-xs">Credit</span>
                          <p className="text-sm">{media.credit}</p>
                        </div>
                      )}
                    </div>
                  )}

                  {/* Dominant Color */}
                  {media.dominant_color && (
                    <div>
                      <span className="text-muted-foreground text-xs">Màu chủ đạo</span>
                      <div className="mt-1 flex items-center gap-2">
                        <div
                          className="h-6 w-6 rounded border"
                          style={{ backgroundColor: media.dominant_color }}
                        />
                        <span className="font-mono text-xs">{media.dominant_color}</span>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {/* Variants */}
              {isImage && variants.length > 0 && (
                <>
                  <Separator />
                  <div>
                    <div className="mb-2 flex items-center justify-between">
                      <h4 className="flex items-center gap-1.5 text-sm font-semibold">
                        <Image className="h-4 w-4" />
                        Variants ({variants.length})
                      </h4>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-7 text-xs"
                        onClick={handleRegenVariants}
                        disabled={regenVariants.isPending}
                      >
                        <RotateCw
                          className={cn("mr-1 h-3 w-3", regenVariants.isPending && "animate-spin")}
                        />
                        Tạo lại
                      </Button>
                    </div>
                    <div className="space-y-1.5">
                      {variants.map((v) => (
                        <div
                          key={v.id}
                          className="bg-muted/30 flex items-center justify-between rounded p-2 text-xs"
                        >
                          <div className="flex items-center gap-2">
                            <Badge variant="outline" className="font-mono text-[10px]">
                              {v.variant_name}
                            </Badge>
                            <span className="text-muted-foreground">
                              {v.width}×{v.height}
                            </span>
                          </div>
                          <span className="text-muted-foreground">{formatFileSize(v.size)}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                </>
              )}

              {/* Usages */}
              {usages.length > 0 && (
                <>
                  <Separator />
                  <div>
                    <h4 className="mb-2 flex items-center gap-1.5 text-sm font-semibold">
                      <Link2 className="h-4 w-4" />
                      Đang sử dụng ({usages.length})
                    </h4>
                    <div className="space-y-1.5">
                      {usages.map((u) => (
                        <div
                          key={u.id}
                          className="bg-muted/30 flex items-center justify-between rounded p-2 text-xs"
                        >
                          <div className="flex items-center gap-2">
                            <Badge variant="secondary" className="text-[10px]">
                              {u.entity_type}
                            </Badge>
                            <span className="text-muted-foreground font-mono">
                              {u.entity_id.slice(0, 8)}...
                            </span>
                          </div>
                          <Badge variant="outline" className="text-[10px]">
                            {u.attachment_type}
                          </Badge>
                        </div>
                      ))}
                    </div>
                  </div>
                </>
              )}
            </div>
          )}
        </SheetContent>
      </Sheet>

      {/* Image Editor Dialog */}
      <ImageEditor open={imageEditorOpen} onOpenChange={setImageEditorOpen} mediaId={mediaId} />
    </>
  );
}
