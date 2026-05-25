"use client";

import { useState, useRef, useCallback } from "react";
import { Upload, X, ImageIcon, Loader2, GripVertical, Plus, Link as LinkIcon } from "lucide-react";
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
import { uploadFile } from "@/services/mediaService";
import { toast } from "sonner";
import { cn, getImageUrl } from "@/lib/utils";

// ============================================
// GalleryManager Props
// ============================================

interface GalleryManagerProps {
  images: string[];
  onChange: (images: string[]) => void;
  label?: string;
  maxImages?: number;
  className?: string;
}

// ============================================
// GalleryManager Component
// ============================================

export function GalleryManager({
  images,
  onChange,
  label = "Thư viện ảnh",
  maxImages = 20,
  className,
}: GalleryManagerProps) {
  const [dialogOpen, setDialogOpen] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [urlInput, setUrlInput] = useState("");
  const [activeTab, setActiveTab] = useState<"upload" | "url">("upload");
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [dragIndex, setDragIndex] = useState<number | null>(null);

  const canAddMore = images.length < maxImages;

  const handleFileUpload = useCallback(
    async (e: React.ChangeEvent<HTMLInputElement>) => {
      const files = e.target.files;
      if (!files || files.length === 0) return;

      const validFiles = Array.from(files).filter((f) => {
        if (!f.type.startsWith("image/")) return false;
        if (f.size > 5 * 1024 * 1024) return false;
        return true;
      });

      if (validFiles.length === 0) {
        toast.error("Không có file hình ảnh hợp lệ (tối đa 5MB)");
        return;
      }

      const remaining = maxImages - images.length;
      const toUpload = validFiles.slice(0, remaining);

      setIsUploading(true);
      try {
        const uploadedUrls: string[] = [];
        for (const file of toUpload) {
          const response = await uploadFile(file, "gallery");
          if (response.success && response.data) {
            uploadedUrls.push(response.data.url);
          }
        }
        if (uploadedUrls.length > 0) {
          onChange([...images, ...uploadedUrls]);
          toast.success(`Đã upload ${uploadedUrls.length} hình ảnh`);
        }
        setDialogOpen(false);
      } catch {
        toast.error("Không thể upload hình ảnh");
      } finally {
        setIsUploading(false);
        if (fileInputRef.current) {
          fileInputRef.current.value = "";
        }
      }
    },
    [images, maxImages, onChange]
  );

  const handleAddUrl = () => {
    if (urlInput.trim() && canAddMore) {
      onChange([...images, urlInput.trim()]);
      setUrlInput("");
      setDialogOpen(false);
    }
  };

  const handleRemove = (index: number) => {
    onChange(images.filter((_, i) => i !== index));
  };

  // Drag & drop reorder
  const handleDragStart = (index: number) => {
    setDragIndex(index);
  };

  const handleDragOver = (e: React.DragEvent, index: number) => {
    e.preventDefault();
    if (dragIndex === null || dragIndex === index) return;

    const newImages = [...images];
    const [moved] = newImages.splice(dragIndex, 1);
    newImages.splice(index, 0, moved);
    onChange(newImages);
    setDragIndex(index);
  };

  const handleDragEnd = () => {
    setDragIndex(null);
  };

  return (
    <div className={cn("space-y-2", className)}>
      <div className="flex items-center justify-between">
        <Label>{label}</Label>
        <span className="text-muted-foreground text-xs">
          {images.length}/{maxImages}
        </span>
      </div>

      {/* Image Grid */}
      {images.length > 0 && (
        <div className="grid grid-cols-3 gap-2 sm:grid-cols-4 md:grid-cols-5">
          {images.map((url, index) => (
            <div
              key={`${url}-${index}`}
              draggable
              onDragStart={() => handleDragStart(index)}
              onDragOver={(e) => handleDragOver(e, index)}
              onDragEnd={handleDragEnd}
              className={cn(
                "group relative aspect-square cursor-grab overflow-hidden rounded-lg border active:cursor-grabbing",
                dragIndex === index && "opacity-50"
              )}
            >
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src={getImageUrl(url)}
                alt={`Gallery image ${index + 1}`}
                className="h-full w-full object-cover"
              />
              <div className="absolute inset-0 flex items-center justify-center bg-black/50 opacity-0 transition-opacity group-hover:opacity-100">
                <Button
                  type="button"
                  variant="destructive"
                  size="icon"
                  className="h-7 w-7"
                  onClick={() => handleRemove(index)}
                >
                  <X className="h-3.5 w-3.5" />
                </Button>
              </div>
              <div className="absolute top-1 left-1 opacity-0 transition-opacity group-hover:opacity-100">
                <GripVertical className="h-4 w-4 text-white drop-shadow" />
              </div>
              <div className="absolute right-1 bottom-1">
                <span className="rounded bg-black/50 px-1 text-[10px] font-medium text-white">
                  {index + 1}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add Button */}
      {canAddMore && (
        <button
          type="button"
          onClick={() => setDialogOpen(true)}
          className="text-muted-foreground hover:border-primary/50 hover:bg-accent/50 flex w-full items-center justify-center gap-2 rounded-lg border-2 border-dashed p-4 text-sm transition-colors"
        >
          <Plus className="h-4 w-4" />
          Thêm hình ảnh
        </button>
      )}

      {/* Upload Dialog */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Thêm hình ảnh</DialogTitle>
          </DialogHeader>

          <div className="space-y-4">
            {/* Tab Toggle */}
            <div className="flex rounded-lg border p-1">
              <button
                type="button"
                className={cn(
                  "flex-1 rounded-md px-3 py-1.5 text-sm font-medium transition-colors",
                  activeTab === "upload" ? "bg-primary text-primary-foreground" : "hover:bg-accent"
                )}
                onClick={() => setActiveTab("upload")}
              >
                <Upload className="mr-1.5 inline h-4 w-4" />
                Upload
              </button>
              <button
                type="button"
                className={cn(
                  "flex-1 rounded-md px-3 py-1.5 text-sm font-medium transition-colors",
                  activeTab === "url" ? "bg-primary text-primary-foreground" : "hover:bg-accent"
                )}
                onClick={() => setActiveTab("url")}
              >
                <LinkIcon className="mr-1.5 inline h-4 w-4" />
                URL
              </button>
            </div>

            {activeTab === "upload" && (
              <div
                className="hover:border-primary/50 flex flex-col items-center justify-center rounded-lg border-2 border-dashed p-8 transition-colors"
                onDrop={(e) => {
                  e.preventDefault();
                  const files = e.dataTransfer.files;
                  if (files.length > 0 && fileInputRef.current) {
                    fileInputRef.current.files = files;
                    fileInputRef.current.dispatchEvent(new Event("change", { bubbles: true }));
                  }
                }}
                onDragOver={(e) => e.preventDefault()}
              >
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  multiple
                  className="hidden"
                  onChange={handleFileUpload}
                />
                {isUploading ? (
                  <>
                    <Loader2 className="text-primary mb-2 h-8 w-8 animate-spin" />
                    <span className="text-muted-foreground text-sm">Đang upload...</span>
                  </>
                ) : (
                  <>
                    <Upload className="text-muted-foreground/50 mb-2 h-8 w-8" />
                    <p className="text-muted-foreground text-sm">Kéo thả hoặc chọn nhiều file</p>
                    <p className="text-muted-foreground mt-1 text-xs">
                      PNG, JPG, WebP (tối đa 5MB mỗi file)
                    </p>
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      className="mt-3"
                      onClick={() => fileInputRef.current?.click()}
                    >
                      Chọn files
                    </Button>
                  </>
                )}
              </div>
            )}

            {activeTab === "url" && (
              <div className="space-y-3">
                <div className="space-y-2">
                  <Label htmlFor="gallery-url-input">URL hình ảnh</Label>
                  <Input
                    id="gallery-url-input"
                    value={urlInput}
                    onChange={(e) => setUrlInput(e.target.value)}
                    placeholder="https://example.com/image.jpg"
                    onKeyDown={(e) => e.key === "Enter" && handleAddUrl()}
                  />
                </div>
                <Button
                  type="button"
                  onClick={handleAddUrl}
                  disabled={!urlInput.trim()}
                  className="w-full"
                >
                  Thêm hình ảnh
                </Button>
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
