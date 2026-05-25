"use client";

import { useState, useRef, useCallback } from "react";
import { Upload, X, ImageIcon, Loader2, Link as LinkIcon, Images } from "lucide-react";
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
import { MediaPickerDialog } from "@/components/shared/MediaPickerDialog";
import { uploadFile } from "@/services/mediaService";
import { toast } from "sonner";
import { cn, getImageUrl } from "@/lib/utils";

// ============================================
// Tabs Component (inline if not available)
// ============================================

interface ImagePickerProps {
  value?: string;
  onChange: (url: string) => void;
  label?: string;
  className?: string;
  aspectRatio?: "square" | "video" | "banner";
}

export function ImagePicker({
  value,
  onChange,
  label = "Hình ảnh",
  className,
  aspectRatio = "video",
}: ImagePickerProps) {
  const [dialogOpen, setDialogOpen] = useState(false);
  const [mediaPickerOpen, setMediaPickerOpen] = useState(false);
  const [urlInput, setUrlInput] = useState("");
  const [isUploading, setIsUploading] = useState(false);
  const [activeTab, setActiveTab] = useState("library");
  const fileInputRef = useRef<HTMLInputElement>(null);

  const aspectClass = {
    square: "aspect-square",
    video: "aspect-video",
    banner: "aspect-[3/1]",
  }[aspectRatio];

  const handleFileUpload = useCallback(
    async (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (!file) return;

      // Validate file type
      if (!file.type.startsWith("image/")) {
        toast.error("Vui lòng chọn file hình ảnh");
        return;
      }

      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        toast.error("Kích thước file tối đa 5MB");
        return;
      }

      setIsUploading(true);
      try {
        const response = await uploadFile(file, "content");
        if (response.success && response.data) {
          onChange(response.data.url);
          setDialogOpen(false);
          toast.success("Upload hình ảnh thành công");
        } else {
          toast.error("Không thể upload hình ảnh");
        }
      } catch {
        toast.error("Không thể upload hình ảnh");
      } finally {
        setIsUploading(false);
        if (fileInputRef.current) {
          fileInputRef.current.value = "";
        }
      }
    },
    [onChange]
  );

  const handleUrlSubmit = () => {
    if (urlInput.trim()) {
      onChange(urlInput.trim());
      setDialogOpen(false);
      setUrlInput("");
    }
  };

  const handleRemove = () => {
    onChange("");
  };

  return (
    <div className={cn("space-y-2", className)}>
      <Label>{label}</Label>

      {value ? (
        <div className={cn("relative overflow-hidden rounded-lg border", aspectClass)}>
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={getImageUrl(value)} alt="Preview" className="h-full w-full object-cover" />
          <div className="absolute inset-0 flex items-center justify-center gap-2 bg-black/50 opacity-0 transition-opacity hover:opacity-100">
            <Button type="button" variant="secondary" size="sm" onClick={() => setDialogOpen(true)}>
              Thay đổi
            </Button>
            <Button type="button" variant="destructive" size="sm" onClick={handleRemove}>
              <X className="h-4 w-4" />
            </Button>
          </div>
        </div>
      ) : (
        <button
          type="button"
          onClick={() => setDialogOpen(true)}
          className={cn(
            "hover:border-primary/50 hover:bg-accent/50 flex w-full flex-col items-center justify-center rounded-lg border-2 border-dashed p-6 text-center transition-colors",
            aspectClass
          )}
        >
          <ImageIcon className="text-muted-foreground/50 mb-2 h-8 w-8" />
          <span className="text-muted-foreground text-sm">Nhấn để chọn hoặc upload hình ảnh</span>
        </button>
      )}

      {/* Image Picker Dialog */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Chọn hình ảnh</DialogTitle>
          </DialogHeader>

          <div className="space-y-4">
            {/* Tab-like UI */}
            <div className="flex rounded-lg border p-1">
              <button
                type="button"
                className={cn(
                  "flex-1 rounded-md px-3 py-1.5 text-sm font-medium transition-colors",
                  activeTab === "library" ? "bg-primary text-primary-foreground" : "hover:bg-accent"
                )}
                onClick={() => setActiveTab("library")}
              >
                <Images className="mr-1.5 inline h-4 w-4" />
                Thư viện
              </button>
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

            {/* Library tab */}
            {activeTab === "library" && (
              <div className="flex flex-col items-center justify-center rounded-lg border-2 border-dashed p-8">
                <Images className="text-muted-foreground/50 mb-2 h-8 w-8" />
                <p className="text-muted-foreground mb-3 text-sm">
                  Chọn ảnh từ thư viện media đã upload
                </p>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => {
                    setDialogOpen(false);
                    setTimeout(() => setMediaPickerOpen(true), 150);
                  }}
                >
                  <Images className="mr-2 h-4 w-4" />
                  Mở thư viện ảnh
                </Button>
              </div>
            )}

            {activeTab === "upload" && (
              <div
                className="hover:border-primary/50 flex flex-col items-center justify-center rounded-lg border-2 border-dashed p-8 transition-colors"
                onDrop={(e) => {
                  e.preventDefault();
                  const file = e.dataTransfer.files[0];
                  if (file) {
                    const dt = new DataTransfer();
                    dt.items.add(file);
                    if (fileInputRef.current) {
                      fileInputRef.current.files = dt.files;
                      fileInputRef.current.dispatchEvent(new Event("change", { bubbles: true }));
                    }
                  }
                }}
                onDragOver={(e) => e.preventDefault()}
              >
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
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
                    <p className="text-muted-foreground text-sm">Kéo thả hoặc nhấn để chọn file</p>
                    <p className="text-muted-foreground mt-1 text-xs">
                      PNG, JPG, WebP (tối đa 5MB)
                    </p>
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      className="mt-3"
                      onClick={() => fileInputRef.current?.click()}
                    >
                      Chọn file
                    </Button>
                  </>
                )}
              </div>
            )}

            {activeTab === "url" && (
              <div className="space-y-3">
                <div className="space-y-2">
                  <Label htmlFor="image-url-input">URL hình ảnh</Label>
                  <Input
                    id="image-url-input"
                    value={urlInput}
                    onChange={(e) => setUrlInput(e.target.value)}
                    placeholder="https://example.com/image.jpg"
                    onKeyDown={(e) => e.key === "Enter" && handleUrlSubmit()}
                  />
                </div>
                {urlInput && (
                  <div className="bg-muted/30 aspect-video overflow-hidden rounded-lg border">
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img
                      src={urlInput}
                      alt="Preview"
                      className="h-full w-full object-contain"
                      onError={(e) => {
                        (e.target as HTMLImageElement).style.display = "none";
                      }}
                    />
                  </div>
                )}
                <Button
                  type="button"
                  onClick={handleUrlSubmit}
                  disabled={!urlInput.trim()}
                  className="w-full"
                >
                  Sử dụng URL này
                </Button>
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>

      {/* Media Library Picker */}
      <MediaPickerDialog
        open={mediaPickerOpen}
        onOpenChange={setMediaPickerOpen}
        imagesOnly
        onSelect={(file) => {
          onChange(file.url);
          setMediaPickerOpen(false);
        }}
      />
    </div>
  );
}
