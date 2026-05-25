"use client";

import { useState, useRef, useCallback, useMemo } from "react";
import {
  Search,
  Upload,
  Loader2,
  ImageIcon,
  Check,
  Grid,
  List,
  FolderOpen,
  Film,
  FileText,
  Music,
  Star,
  X,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { useMediaListV3, useUploadV3, useFolderTree } from "@/hooks/useMediaV3";
import { cn, getImageUrl } from "@/lib/utils";
import type { MediaFileV3, MediaFolder, MediaListParams } from "@/types/media";

// ============================================
// Types
// ============================================

export type MediaPickerMode = "single" | "multiple";

export interface MediaPickerV3Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** Single file selection callback */
  onSelect?: (file: MediaFileV3) => void;
  /** Multiple file selection callback */
  onSelectMultiple?: (files: MediaFileV3[]) => void;
  /** Selection mode */
  mode?: MediaPickerMode;
  /** Filter media types: "image" | "video" | "audio" | "document" */
  accept?: string[];
  /** Max number of files in multiple mode */
  maxFiles?: number;
  /** Dialog title */
  title?: string;
  /** Pre-selected file IDs (for multiple mode) */
  selectedIds?: string[];
}

// ============================================
// Helpers
// ============================================

function formatFileSize(bytes: number): string {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
}

function getMediaTypeIcon(mimeType: string) {
  if (mimeType.startsWith("image/")) return <ImageIcon className="h-5 w-5" />;
  if (mimeType.startsWith("video/")) return <Film className="h-5 w-5" />;
  if (mimeType.startsWith("audio/")) return <Music className="h-5 w-5" />;
  return <FileText className="h-5 w-5" />;
}

// ============================================
// Component
// ============================================

export function MediaPickerV3({
  open,
  onOpenChange,
  onSelect,
  onSelectMultiple,
  mode = "single",
  accept,
  maxFiles = 20,
  title,
  selectedIds: initialSelectedIds = [],
}: MediaPickerV3Props) {
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [mediaType, setMediaType] = useState("all");
  const [folderId, setFolderId] = useState("all");
  const [view, setView] = useState<"grid" | "list">("grid");
  const [selectedFiles, setSelectedFiles] = useState<Map<string, MediaFileV3>>(new Map());
  const [selectedSingle, setSelectedSingle] = useState<MediaFileV3 | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Build query params
  const params = useMemo<MediaListParams>(() => {
    const p: MediaListParams = { page, limit: 30 };
    if (search) p.search = search;
    if (mediaType !== "all") p.media_type = mediaType;
    if (folderId !== "all") p.folder_id = folderId;
    // If accept is provided, filter by first type for simpler UX
    if (accept && accept.length === 1) p.media_type = accept[0];
    return p;
  }, [page, search, mediaType, folderId, accept]);

  const { data: mediaData, isLoading } = useMediaListV3(params);
  const { data: foldersData } = useFolderTree();
  const uploadMutation = useUploadV3();

  const files = mediaData?.data ?? [];
  const meta = mediaData?.meta;
  const folders = foldersData?.data ?? [];

  const dialogTitle =
    title ??
    (mode === "multiple"
      ? "Chọn nhiều file từ thư viện"
      : accept?.length === 1 && accept[0] === "image"
        ? "Chọn hình ảnh từ thư viện"
        : "Chọn file từ thư viện");

  const acceptInput = useMemo(() => {
    if (!accept) return undefined;
    return accept.map((t) => `${t}/*`).join(",");
  }, [accept]);

  // ============================================
  // Handlers
  // ============================================

  const handleUploadAndSelect = useCallback(
    async (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (!file) return;

      // Validate accept
      if (accept) {
        const fileType = file.type.split("/")[0];
        if (!accept.includes(fileType)) return;
      }

      uploadMutation.mutate(
        { file, folder: folderId !== "all" ? folderId : undefined },
        {
          onSuccess: (response) => {
            if (response.data && mode === "single") {
              onSelect?.(response.data);
              onOpenChange(false);
            }
          },
        }
      );

      if (fileInputRef.current) fileInputRef.current.value = "";
    },
    [uploadMutation, folderId, accept, mode, onSelect, onOpenChange]
  );

  const handleClickFile = (file: MediaFileV3) => {
    if (mode === "single") {
      setSelectedSingle(file);
    } else {
      setSelectedFiles((prev) => {
        const next = new Map(prev);
        if (next.has(file.id)) {
          next.delete(file.id);
        } else if (next.size < maxFiles) {
          next.set(file.id, file);
        }
        return next;
      });
    }
  };

  const handleDoubleClick = (file: MediaFileV3) => {
    if (mode === "single") {
      onSelect?.(file);
      onOpenChange(false);
    }
  };

  const handleConfirm = () => {
    if (mode === "single" && selectedSingle) {
      onSelect?.(selectedSingle);
      onOpenChange(false);
      setSelectedSingle(null);
    } else if (mode === "multiple") {
      onSelectMultiple?.(Array.from(selectedFiles.values()));
      onOpenChange(false);
      setSelectedFiles(new Map());
    }
  };

  const isSelected = (fileId: string) => {
    if (mode === "single") return selectedSingle?.id === fileId;
    return selectedFiles.has(fileId);
  };

  // ============================================
  // Render
  // ============================================

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="flex max-h-[85vh] flex-col p-0 sm:max-w-5xl">
        <DialogHeader className="px-6 pt-6 pb-0">
          <DialogTitle>{dialogTitle}</DialogTitle>
        </DialogHeader>

        {/* Toolbar */}
        <div className="flex flex-wrap items-center gap-2 border-b px-6 py-3">
          {/* Search */}
          <div className="relative min-w-[200px] flex-1">
            <Search className="text-muted-foreground absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
            <Input
              placeholder="Tìm kiếm..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(1);
              }}
              className="h-9 pl-9"
            />
          </div>

          {/* Media type filter */}
          {(!accept || accept.length > 1) && (
            <Select
              value={mediaType}
              onValueChange={(v) => {
                setMediaType(v);
                setPage(1);
              }}
            >
              <SelectTrigger className="h-9 w-[130px]">
                <SelectValue placeholder="Loại file" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Tất cả</SelectItem>
                <SelectItem value="image">Hình ảnh</SelectItem>
                <SelectItem value="video">Video</SelectItem>
                <SelectItem value="audio">Âm thanh</SelectItem>
                <SelectItem value="document">Tài liệu</SelectItem>
              </SelectContent>
            </Select>
          )}

          {/* Folder filter */}
          {folders.length > 0 && (
            <Select
              value={folderId}
              onValueChange={(v) => {
                setFolderId(v);
                setPage(1);
              }}
            >
              <SelectTrigger className="h-9 w-[150px]">
                <SelectValue placeholder="Thư mục" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Tất cả thư mục</SelectItem>
                {folders.map((f) => (
                  <SelectItem key={f.id} value={f.id}>
                    <span className="flex items-center gap-1.5">
                      <FolderOpen className="h-3.5 w-3.5" />
                      {f.name}
                    </span>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}

          {/* View toggle */}
          <div className="flex rounded-md border">
            <button
              type="button"
              className={cn("p-1.5 transition-colors", view === "grid" && "bg-accent")}
              onClick={() => setView("grid")}
            >
              <Grid className="h-4 w-4" />
            </button>
            <button
              type="button"
              className={cn("p-1.5 transition-colors", view === "list" && "bg-accent")}
              onClick={() => setView("list")}
            >
              <List className="h-4 w-4" />
            </button>
          </div>

          {/* Upload button */}
          <input
            ref={fileInputRef}
            type="file"
            accept={acceptInput}
            className="hidden"
            onChange={handleUploadAndSelect}
          />
          <Button
            type="button"
            size="sm"
            onClick={() => fileInputRef.current?.click()}
            disabled={uploadMutation.isPending}
          >
            {uploadMutation.isPending ? (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            ) : (
              <Upload className="mr-2 h-4 w-4" />
            )}
            {uploadMutation.isPending ? "Đang upload..." : "Upload mới"}
          </Button>
        </div>

        {/* File Grid / List */}
        <ScrollArea className="min-h-0 flex-1 px-6">
          {isLoading ? (
            <div className="grid grid-cols-3 gap-2 py-3 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6">
              {Array.from({ length: 18 }).map((_, i) => (
                <Skeleton key={i} className="aspect-square rounded-lg" />
              ))}
            </div>
          ) : files.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-16 text-center">
              <ImageIcon className="text-muted-foreground/30 mb-3 h-12 w-12" />
              <p className="text-muted-foreground">
                {search ? "Không tìm thấy file nào" : "Chưa có file nào trong thư viện"}
              </p>
              <Button
                type="button"
                variant="outline"
                size="sm"
                className="mt-3"
                onClick={() => fileInputRef.current?.click()}
              >
                <Upload className="mr-2 h-4 w-4" />
                Upload file đầu tiên
              </Button>
            </div>
          ) : view === "grid" ? (
            <div className="grid grid-cols-3 gap-2 py-3 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6">
              {files.map((file) => {
                const selected = isSelected(file.id);
                const isImage = file.mime_type.startsWith("image/");
                const isVideo = file.mime_type.startsWith("video/");
                return (
                  <button
                    key={file.id}
                    type="button"
                    className={cn(
                      "group hover:border-primary/50 relative aspect-square overflow-hidden rounded-lg border-2 transition-all",
                      selected ? "border-primary ring-primary/20 ring-2" : "border-transparent"
                    )}
                    onClick={() => handleClickFile(file)}
                    onDoubleClick={() => handleDoubleClick(file)}
                  >
                    {isImage ? (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img
                        src={getImageUrl(file.url)}
                        alt={file.alt || file.original_name}
                        className="h-full w-full object-cover"
                        loading="lazy"
                      />
                    ) : (
                      <div className="bg-muted/50 flex h-full w-full flex-col items-center justify-center gap-1">
                        {isVideo ? (
                          <Film className="text-muted-foreground/50 h-8 w-8" />
                        ) : (
                          getMediaTypeIcon(file.mime_type)
                        )}
                        <span className="text-muted-foreground w-full truncate px-1 text-center text-[10px]">
                          {file.extension.toUpperCase()}
                        </span>
                      </div>
                    )}

                    {/* Selected overlay */}
                    {selected && (
                      <div className="bg-primary/20 absolute inset-0 flex items-center justify-center">
                        <div className="bg-primary rounded-full p-1">
                          <Check className="text-primary-foreground h-4 w-4" />
                        </div>
                      </div>
                    )}

                    {/* Hover info */}
                    <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/70 to-transparent p-1.5 opacity-0 transition-opacity group-hover:opacity-100">
                      <p className="truncate text-[10px] text-white">{file.original_name}</p>
                    </div>

                    {/* Favorite badge */}
                    {file.is_favorite && (
                      <Star className="absolute top-1 right-1 h-3 w-3 fill-yellow-400 text-yellow-400" />
                    )}
                  </button>
                );
              })}
            </div>
          ) : (
            <div className="divide-y py-3">
              {files.map((file) => {
                const selected = isSelected(file.id);
                const isImage = file.mime_type.startsWith("image/");
                return (
                  <button
                    key={file.id}
                    type="button"
                    className={cn(
                      "hover:bg-accent/50 flex w-full items-center gap-3 rounded-md px-3 py-2 text-left transition-colors",
                      selected && "bg-primary/10"
                    )}
                    onClick={() => handleClickFile(file)}
                    onDoubleClick={() => handleDoubleClick(file)}
                  >
                    <div className="h-10 w-10 shrink-0 overflow-hidden rounded border">
                      {isImage ? (
                        // eslint-disable-next-line @next/next/no-img-element
                        <img
                          src={getImageUrl(file.url)}
                          alt={file.alt || file.original_name}
                          className="h-full w-full object-cover"
                          loading="lazy"
                        />
                      ) : (
                        <div className="bg-muted/50 flex h-full w-full items-center justify-center">
                          {getMediaTypeIcon(file.mime_type)}
                        </div>
                      )}
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm">{file.original_name}</p>
                      <p className="text-muted-foreground text-xs">
                        {formatFileSize(file.size)} · {file.extension.toUpperCase()}
                        {file.dimensions && ` · ${file.dimensions.width}×${file.dimensions.height}`}
                      </p>
                    </div>
                    {file.is_favorite && (
                      <Star className="h-3.5 w-3.5 shrink-0 fill-yellow-400 text-yellow-400" />
                    )}
                    {selected && <Check className="text-primary h-4 w-4 shrink-0" />}
                  </button>
                );
              })}
            </div>
          )}
        </ScrollArea>

        {/* Footer */}
        <div className="border-t px-6 py-3">
          <div className="flex items-center justify-between">
            {/* Pagination & Info */}
            <div className="flex items-center gap-3">
              {meta && (
                <span className="text-muted-foreground text-xs">
                  {meta.total} file · Trang {page}/{meta.total_pages || 1}
                </span>
              )}
              {meta && meta.total_pages > 1 && (
                <div className="flex items-center gap-1">
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    className="h-7 px-2 text-xs"
                    disabled={page <= 1}
                    onClick={() => setPage((p) => p - 1)}
                  >
                    ←
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    className="h-7 px-2 text-xs"
                    disabled={page >= (meta?.total_pages ?? 1)}
                    onClick={() => setPage((p) => p + 1)}
                  >
                    →
                  </Button>
                </div>
              )}
            </div>

            {/* Selection info + Actions */}
            <div className="flex items-center gap-3">
              {mode === "multiple" && selectedFiles.size > 0 && (
                <div className="flex items-center gap-2">
                  <Badge variant="secondary">
                    {selectedFiles.size} / {maxFiles} đã chọn
                  </Badge>
                  <button
                    type="button"
                    className="text-muted-foreground hover:text-foreground text-xs"
                    onClick={() => setSelectedFiles(new Map())}
                  >
                    <X className="h-3.5 w-3.5" />
                  </button>
                </div>
              )}
              {mode === "single" && selectedSingle && (
                <div className="flex items-center gap-2">
                  <div className="h-8 w-8 overflow-hidden rounded border">
                    {selectedSingle.mime_type.startsWith("image/") ? (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img
                        src={getImageUrl(selectedSingle.url)}
                        alt=""
                        className="h-full w-full object-cover"
                      />
                    ) : (
                      <div className="bg-muted/50 flex h-full w-full items-center justify-center">
                        {getMediaTypeIcon(selectedSingle.mime_type)}
                      </div>
                    )}
                  </div>
                  <span className="text-muted-foreground max-w-[150px] truncate text-sm">
                    {selectedSingle.original_name}
                  </span>
                </div>
              )}
              <Button type="button" variant="outline" size="sm" onClick={() => onOpenChange(false)}>
                Hủy
              </Button>
              <Button
                type="button"
                size="sm"
                onClick={handleConfirm}
                disabled={mode === "single" ? !selectedSingle : selectedFiles.size === 0}
              >
                {mode === "single" ? "Chọn file này" : `Chọn ${selectedFiles.size} file`}
              </Button>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
