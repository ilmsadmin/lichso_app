"use client";

import { useState, useCallback } from "react";
import { X, ImageIcon, GripVertical, Plus, Images } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { MediaPickerV3 } from "@/components/shared/MediaPickerV3";
import { cn, getImageUrl } from "@/lib/utils";
import type { MediaFileV3 } from "@/types/media";

// ============================================
// Types
// ============================================

interface GalleryManagerV3Props {
  /** Current list of image URLs */
  images: string[];
  /** Callback when images change */
  onChange: (images: string[]) => void;
  /** Field label */
  label?: string;
  /** Max images allowed */
  maxImages?: number;
  /** Additional CSS class */
  className?: string;
}

// ============================================
// Component
// ============================================

export function GalleryManagerV3({
  images,
  onChange,
  label = "Thư viện ảnh",
  maxImages = 20,
  className,
}: GalleryManagerV3Props) {
  const [pickerOpen, setPickerOpen] = useState(false);
  const [dragIndex, setDragIndex] = useState<number | null>(null);

  const canAddMore = images.length < maxImages;
  const remaining = maxImages - images.length;

  const handleRemove = (index: number) => {
    onChange(images.filter((_, i) => i !== index));
  };

  const handleSelectMultiple = useCallback(
    (files: MediaFileV3[]) => {
      const newUrls = files.map((f) => f.url).slice(0, remaining);
      onChange([...images, ...newUrls]);
      setPickerOpen(false);
    },
    [images, remaining, onChange]
  );

  // Drag & drop reorder
  const handleDragStart = (index: number) => setDragIndex(index);

  const handleDragOver = (e: React.DragEvent, index: number) => {
    e.preventDefault();
    if (dragIndex === null || dragIndex === index) return;
    const newImages = [...images];
    const [moved] = newImages.splice(dragIndex, 1);
    newImages.splice(index, 0, moved);
    onChange(newImages);
    setDragIndex(index);
  };

  const handleDragEnd = () => setDragIndex(null);

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
          onClick={() => setPickerOpen(true)}
          className="text-muted-foreground hover:border-primary/50 hover:bg-accent/50 flex w-full items-center justify-center gap-2 rounded-lg border-2 border-dashed p-4 text-sm transition-colors"
        >
          <Plus className="h-4 w-4" />
          Thêm hình ảnh từ thư viện
        </button>
      )}

      {/* V3 Media Picker (multiple mode) */}
      <MediaPickerV3
        open={pickerOpen}
        onOpenChange={setPickerOpen}
        mode="multiple"
        accept={["image"]}
        maxFiles={remaining}
        title={`Chọn hình ảnh (tối đa ${remaining} ảnh)`}
        onSelectMultiple={handleSelectMultiple}
      />
    </div>
  );
}
