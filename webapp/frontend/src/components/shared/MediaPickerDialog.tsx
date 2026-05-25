"use client";

import { useState, useRef, useCallback } from "react";
import { Search, Upload, Loader2, ImageIcon, Check, Grid, List } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { useMedia } from "@/hooks/useMedia";
import { cn, getImageUrl } from "@/lib/utils";
import type { MediaFile } from "@/types/media";

// ============================================
// Types
// ============================================

interface MediaPickerDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSelect: (file: MediaFile) => void;
  /** Filter to only show images */
  imagesOnly?: boolean;
  /** Dialog title */
  title?: string;
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

// ============================================
// Component
// ============================================

export function MediaPickerDialog({
  open,
  onOpenChange,
  onSelect,
  imagesOnly = true,
  title = "Chọn hình ảnh từ thư viện",
}: MediaPickerDialogProps) {
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [folder, setFolder] = useState("all");
  const [selectedFile, setSelectedFile] = useState<MediaFile | null>(null);
  const [view, setView] = useState<"grid" | "list">("grid");
  const fileInputRef = useRef<HTMLInputElement>(null);

  const { files, meta, folders, isLoading, upload, isUploading } = useMedia(page, 24, {
    search: search || undefined,
    type: imagesOnly ? "image" : undefined,
    folder: folder !== "all" ? folder : undefined,
  });

  const handleUploadAndSelect = useCallback(
    async (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (!file) return;

      if (imagesOnly && !file.type.startsWith("image/")) {
        return;
      }

      upload(
        { file, folder: folder !== "all" ? folder : "content" },
        {
          onSuccess: (response) => {
            if (response.success && response.data) {
              onSelect(response.data);
              onOpenChange(false);
            }
          },
        }
      );

      // Reset input
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    },
    [upload, folder, imagesOnly, onSelect, onOpenChange]
  );

  const handleConfirm = () => {
    if (selectedFile) {
      onSelect(selectedFile);
      onOpenChange(false);
      setSelectedFile(null);
    }
  };

  const handleDoubleClick = (file: MediaFile) => {
    onSelect(file);
    onOpenChange(false);
    setSelectedFile(null);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="flex max-h-[85vh] flex-col p-0 sm:max-w-4xl">
        <DialogHeader className="px-6 pt-6 pb-0">
          <DialogTitle>{title}</DialogTitle>
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

          {/* Folder filter */}
          {folders.length > 0 && (
            <Select
              value={folder}
              onValueChange={(v) => {
                setFolder(v);
                setPage(1);
              }}
            >
              <SelectTrigger className="h-9 w-[150px]">
                <SelectValue placeholder="Thư mục" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Tất cả</SelectItem>
                {folders.map((f) => (
                  <SelectItem key={f} value={f}>
                    {f === "/" ? "Gốc" : f}
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
            accept={imagesOnly ? "image/*" : "*/*"}
            className="hidden"
            onChange={handleUploadAndSelect}
          />
          <Button
            type="button"
            size="sm"
            onClick={() => fileInputRef.current?.click()}
            disabled={isUploading}
          >
            {isUploading ? (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            ) : (
              <Upload className="mr-2 h-4 w-4" />
            )}
            {isUploading ? "Đang upload..." : "Upload mới"}
          </Button>
        </div>

        {/* File Grid / List */}
        <ScrollArea className="min-h-0 flex-1 px-6">
          {isLoading ? (
            <div className="flex items-center justify-center py-16">
              <Loader2 className="text-primary h-8 w-8 animate-spin" />
            </div>
          ) : files.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-16 text-center">
              <ImageIcon className="text-muted-foreground/30 mb-3 h-12 w-12" />
              <p className="text-muted-foreground">
                {search ? "Không tìm thấy hình ảnh nào" : "Chưa có hình ảnh nào trong thư viện"}
              </p>
              <Button
                type="button"
                variant="outline"
                size="sm"
                className="mt-3"
                onClick={() => fileInputRef.current?.click()}
              >
                <Upload className="mr-2 h-4 w-4" />
                Upload hình ảnh đầu tiên
              </Button>
            </div>
          ) : view === "grid" ? (
            /* Grid View */
            <div className="grid grid-cols-3 gap-2 py-3 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6">
              {files.map((file) => {
                const isSelected = selectedFile?.id === file.id;
                const isImage = file.mime_type.startsWith("image/");
                return (
                  <button
                    key={file.id}
                    type="button"
                    className={cn(
                      "group hover:border-primary/50 relative aspect-square overflow-hidden rounded-lg border-2 transition-all",
                      isSelected ? "border-primary ring-primary/20 ring-2" : "border-transparent"
                    )}
                    onClick={() => setSelectedFile(file)}
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
                      <div className="bg-muted/50 flex h-full w-full items-center justify-center">
                        <ImageIcon className="text-muted-foreground/50 h-8 w-8" />
                      </div>
                    )}

                    {/* Selected overlay */}
                    {isSelected && (
                      <div className="bg-primary/20 absolute inset-0 flex items-center justify-center">
                        <div className="bg-primary rounded-full p-1">
                          <Check className="text-primary-foreground h-4 w-4" />
                        </div>
                      </div>
                    )}

                    {/* Hover file name */}
                    <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/70 to-transparent p-1.5 opacity-0 transition-opacity group-hover:opacity-100">
                      <p className="truncate text-[10px] text-white">{file.original_name}</p>
                    </div>
                  </button>
                );
              })}
            </div>
          ) : (
            /* List View */
            <div className="divide-y py-3">
              {files.map((file) => {
                const isSelected = selectedFile?.id === file.id;
                const isImage = file.mime_type.startsWith("image/");
                return (
                  <button
                    key={file.id}
                    type="button"
                    className={cn(
                      "hover:bg-accent/50 flex w-full items-center gap-3 rounded-md px-3 py-2 text-left transition-colors",
                      isSelected && "bg-primary/10"
                    )}
                    onClick={() => setSelectedFile(file)}
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
                          <ImageIcon className="text-muted-foreground/50 h-5 w-5" />
                        </div>
                      )}
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm">{file.original_name}</p>
                      <p className="text-muted-foreground text-xs">
                        {formatFileSize(file.size)} · {file.extension.toUpperCase()}
                      </p>
                    </div>
                    {isSelected && <Check className="text-primary h-4 w-4 shrink-0" />}
                  </button>
                );
              })}
            </div>
          )}
        </ScrollArea>

        {/* Pagination + Footer */}
        <div className="border-t px-6 py-3">
          <div className="flex items-center justify-between">
            {/* Info */}
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

            {/* Selected preview + Confirm */}
            <div className="flex items-center gap-3">
              {selectedFile && (
                <div className="flex items-center gap-2">
                  <div className="h-8 w-8 overflow-hidden rounded border">
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img
                      src={getImageUrl(selectedFile.url)}
                      alt=""
                      className="h-full w-full object-cover"
                    />
                  </div>
                  <span className="text-muted-foreground max-w-[150px] truncate text-sm">
                    {selectedFile.original_name}
                  </span>
                </div>
              )}
              <Button type="button" variant="outline" size="sm" onClick={() => onOpenChange(false)}>
                Hủy
              </Button>
              <Button type="button" size="sm" onClick={handleConfirm} disabled={!selectedFile}>
                Chọn ảnh này
              </Button>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
