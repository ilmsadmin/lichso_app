"use client";

import { useState, useRef, useCallback, useEffect } from "react";
import {
  Crop,
  RotateCcw,
  RotateCw,
  FlipHorizontal,
  FlipVertical,
  Maximize,
  Save,
  X,
  Loader2,
  Crosshair,
  RefreshCw,
  SquareIcon,
  RectangleHorizontal,
  MonitorSmartphone,
  Share2,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import {
  useCropImage,
  useResizeImage,
  useRotateImage,
  useSetFocalPoint,
  useRegenerateVariants,
  useMediaDetailV3,
} from "@/hooks/useMediaV3";
import { cn, getImageUrl } from "@/lib/utils";
import type { MediaFileV3, CropImageRequest } from "@/types/media";

// ============================================
// Types
// ============================================

interface ImageEditorProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** Media file to edit */
  mediaId: string | null;
  /** Callback after successful edit */
  onSaved?: (file: MediaFileV3) => void;
}

interface CropPreset {
  label: string;
  icon: React.ReactNode;
  ratio: number | null; // null = free crop
  width?: number;
  height?: number;
}

interface CropBox {
  x: number;
  y: number;
  width: number;
  height: number;
}

// ============================================
// Crop Presets
// ============================================

const CROP_PRESETS: CropPreset[] = [
  {
    label: "Tự do",
    icon: <Crop className="h-3.5 w-3.5" />,
    ratio: null,
  },
  {
    label: "1:1",
    icon: <SquareIcon className="h-3.5 w-3.5" />,
    ratio: 1,
  },
  {
    label: "16:9",
    icon: <RectangleHorizontal className="h-3.5 w-3.5" />,
    ratio: 16 / 9,
  },
  {
    label: "4:3",
    icon: <MonitorSmartphone className="h-3.5 w-3.5" />,
    ratio: 4 / 3,
  },
  {
    label: "OG 1200×630",
    icon: <Share2 className="h-3.5 w-3.5" />,
    ratio: 1200 / 630,
    width: 1200,
    height: 630,
  },
];

// ============================================
// Component
// ============================================

export function ImageEditor({ open, onOpenChange, mediaId, onSaved }: ImageEditorProps) {
  const { data: mediaResponse } = useMediaDetailV3(mediaId);
  const media = mediaResponse?.data ?? null;

  const cropMutation = useCropImage();
  const resizeMutation = useResizeImage();
  const rotateMutation = useRotateImage();
  const focalMutation = useSetFocalPoint();
  const regenMutation = useRegenerateVariants();

  const isProcessing =
    cropMutation.isPending ||
    resizeMutation.isPending ||
    rotateMutation.isPending ||
    focalMutation.isPending ||
    regenMutation.isPending;

  // Active tool
  const [activeTool, setActiveTool] = useState<"crop" | "resize" | "focal" | null>(null);

  // Crop state
  const [cropPresetIdx, setCropPresetIdx] = useState(0);
  const [cropBox, setCropBox] = useState<CropBox>({
    x: 0,
    y: 0,
    width: 100,
    height: 100,
  });
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
  const imageRef = useRef<HTMLDivElement>(null);

  // Resize state
  const [resizeW, setResizeW] = useState("");
  const [resizeH, setResizeH] = useState("");

  // Focal point state
  const [focalPoint, setFocalPoint] = useState({ x: 0.5, y: 0.5 });

  // Reset states when media changes
  useEffect(() => {
    if (media) {
      setActiveTool(null);
      if (media.dimensions) {
        setCropBox({
          x: 0,
          y: 0,
          width: media.dimensions.width,
          height: media.dimensions.height,
        });
        setResizeW(String(media.dimensions.width));
        setResizeH(String(media.dimensions.height));
      }
      if (media.focal_point) {
        setFocalPoint(media.focal_point);
      } else {
        setFocalPoint({ x: 0.5, y: 0.5 });
      }
    }
  }, [media]);

  // ============================================
  // Crop handlers
  // ============================================

  const handleCropMouseDown = useCallback(
    (e: React.MouseEvent) => {
      if (activeTool !== "crop" || !imageRef.current) return;
      const rect = imageRef.current.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;
      setIsDragging(true);
      setDragStart({ x, y });
      setCropBox({ x, y, width: 0, height: 0 });
    },
    [activeTool]
  );

  const handleCropMouseMove = useCallback(
    (e: React.MouseEvent) => {
      if (!isDragging || activeTool !== "crop" || !imageRef.current) return;
      const rect = imageRef.current.getBoundingClientRect();
      const currentX = Math.max(0, Math.min(e.clientX - rect.left, rect.width));
      const currentY = Math.max(0, Math.min(e.clientY - rect.top, rect.height));

      const width = currentX - dragStart.x;
      const ratio = CROP_PRESETS[cropPresetIdx].ratio;
      const height = ratio ? width / ratio : currentY - dragStart.y;

      setCropBox({
        x: Math.min(dragStart.x, dragStart.x + width),
        y: Math.min(dragStart.y, dragStart.y + height),
        width: Math.abs(width),
        height: Math.abs(height),
      });
    },
    [isDragging, activeTool, dragStart, cropPresetIdx]
  );

  const handleCropMouseUp = useCallback(() => {
    setIsDragging(false);
  }, []);

  const applyCrop = () => {
    if (!media || !media.dimensions || !imageRef.current) return;
    const rect = imageRef.current.getBoundingClientRect();
    const scaleX = media.dimensions.width / rect.width;
    const scaleY = media.dimensions.height / rect.height;

    const data: CropImageRequest = {
      x: Math.round(cropBox.x * scaleX),
      y: Math.round(cropBox.y * scaleY),
      width: Math.round(cropBox.width * scaleX),
      height: Math.round(cropBox.height * scaleY),
    };

    cropMutation.mutate(
      { id: media.id, data },
      {
        onSuccess: (resp) => {
          if (resp.data) onSaved?.(resp.data);
          setActiveTool(null);
        },
      }
    );
  };

  // ============================================
  // Rotate handler
  // ============================================

  const handleRotate = (angle: 90 | -90 | 180) => {
    if (!media) return;
    rotateMutation.mutate(
      { id: media.id, data: { angle } },
      {
        onSuccess: (resp) => {
          if (resp.data) onSaved?.(resp.data);
        },
      }
    );
  };

  // ============================================
  // Resize handler
  // ============================================

  const applyResize = () => {
    if (!media) return;
    const w = parseInt(resizeW, 10);
    const h = parseInt(resizeH, 10);
    if (!w || !h || w <= 0 || h <= 0) return;

    resizeMutation.mutate(
      { id: media.id, data: { width: w, height: h, fit: "cover" } },
      {
        onSuccess: (resp) => {
          if (resp.data) onSaved?.(resp.data);
          setActiveTool(null);
        },
      }
    );
  };

  // ============================================
  // Focal point handler
  // ============================================

  const handleFocalClick = (e: React.MouseEvent) => {
    if (activeTool !== "focal" || !imageRef.current) return;
    const rect = imageRef.current.getBoundingClientRect();
    const x = (e.clientX - rect.left) / rect.width;
    const y = (e.clientY - rect.top) / rect.height;
    setFocalPoint({
      x: Math.max(0, Math.min(1, x)),
      y: Math.max(0, Math.min(1, y)),
    });
  };

  const applyFocalPoint = () => {
    if (!media) return;
    focalMutation.mutate(
      { id: media.id, data: focalPoint },
      {
        onSuccess: (resp) => {
          if (resp.data) onSaved?.(resp.data);
          setActiveTool(null);
        },
      }
    );
  };

  // ============================================
  // Regenerate variants
  // ============================================

  const handleRegenerate = () => {
    if (!media) return;
    regenMutation.mutate(media.id);
  };

  if (!media) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="flex max-h-[90vh] flex-col p-0 sm:max-w-4xl">
        <DialogHeader className="px-6 pt-6 pb-0">
          <DialogTitle className="flex items-center gap-2">
            Chỉnh sửa ảnh
            <Badge variant="secondary" className="text-xs font-normal">
              {media.original_name}
            </Badge>
            {media.dimensions && (
              <Badge variant="outline" className="text-xs font-normal">
                {media.dimensions.width} × {media.dimensions.height}
              </Badge>
            )}
          </DialogTitle>
        </DialogHeader>

        {/* Toolbar */}
        <div className="flex flex-wrap items-center gap-1 border-b px-6 py-2">
          {/* Crop */}
          <Button
            type="button"
            variant={activeTool === "crop" ? "default" : "ghost"}
            size="sm"
            onClick={() => setActiveTool(activeTool === "crop" ? null : "crop")}
          >
            <Crop className="mr-1.5 h-4 w-4" />
            Cắt
          </Button>

          <Separator orientation="vertical" className="h-6" />

          {/* Rotate */}
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={() => handleRotate(-90)}
            disabled={isProcessing}
          >
            <RotateCcw className="mr-1.5 h-4 w-4" />
            Xoay trái
          </Button>
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={() => handleRotate(90)}
            disabled={isProcessing}
          >
            <RotateCw className="mr-1.5 h-4 w-4" />
            Xoay phải
          </Button>
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={() => handleRotate(180)}
            disabled={isProcessing}
          >
            <FlipVertical className="mr-1.5 h-4 w-4" />
            Lật
          </Button>

          <Separator orientation="vertical" className="h-6" />

          {/* Resize */}
          <Button
            type="button"
            variant={activeTool === "resize" ? "default" : "ghost"}
            size="sm"
            onClick={() => setActiveTool(activeTool === "resize" ? null : "resize")}
          >
            <Maximize className="mr-1.5 h-4 w-4" />
            Thay kích thước
          </Button>

          {/* Focal Point */}
          <Button
            type="button"
            variant={activeTool === "focal" ? "default" : "ghost"}
            size="sm"
            onClick={() => setActiveTool(activeTool === "focal" ? null : "focal")}
          >
            <Crosshair className="mr-1.5 h-4 w-4" />
            Focal Point
          </Button>

          <Separator orientation="vertical" className="h-6" />

          {/* Regenerate */}
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={handleRegenerate}
            disabled={isProcessing}
          >
            <RefreshCw className="mr-1.5 h-4 w-4" />
            Tạo lại variants
          </Button>

          {isProcessing && <Loader2 className="text-primary ml-2 h-4 w-4 animate-spin" />}
        </div>

        {/* Crop Presets bar */}
        {activeTool === "crop" && (
          <div className="bg-muted/50 flex items-center gap-2 border-b px-6 py-2">
            <span className="text-muted-foreground mr-1 text-xs font-medium">Tỉ lệ:</span>
            {CROP_PRESETS.map((preset, idx) => (
              <Button
                key={preset.label}
                type="button"
                variant={cropPresetIdx === idx ? "default" : "outline"}
                size="sm"
                className="h-7 text-xs"
                onClick={() => setCropPresetIdx(idx)}
              >
                {preset.icon}
                <span className="ml-1">{preset.label}</span>
              </Button>
            ))}
            <div className="flex-1" />
            <Button
              type="button"
              size="sm"
              onClick={applyCrop}
              disabled={cropBox.width < 5 || cropBox.height < 5 || isProcessing}
            >
              <Save className="mr-1.5 h-3.5 w-3.5" />
              Áp dụng cắt
            </Button>
          </div>
        )}

        {/* Resize Panel */}
        {activeTool === "resize" && (
          <div className="bg-muted/50 flex items-center gap-3 border-b px-6 py-2">
            <Label className="text-xs">Rộng:</Label>
            <Input
              value={resizeW}
              onChange={(e) => setResizeW(e.target.value)}
              className="h-7 w-20 text-xs"
              type="number"
              min={1}
            />
            <Label className="text-xs">Cao:</Label>
            <Input
              value={resizeH}
              onChange={(e) => setResizeH(e.target.value)}
              className="h-7 w-20 text-xs"
              type="number"
              min={1}
            />
            <span className="text-muted-foreground text-xs">px</span>
            <Button type="button" size="sm" onClick={applyResize} disabled={isProcessing}>
              <Save className="mr-1.5 h-3.5 w-3.5" />
              Áp dụng
            </Button>
          </div>
        )}

        {/* Focal Point Panel */}
        {activeTool === "focal" && (
          <div className="bg-muted/50 flex items-center gap-3 border-b px-6 py-2">
            <span className="text-muted-foreground text-xs">Nhấn vào ảnh để chọn focal point</span>
            <Badge variant="outline" className="text-xs">
              X: {(focalPoint.x * 100).toFixed(0)}% · Y: {(focalPoint.y * 100).toFixed(0)}%
            </Badge>
            <Button type="button" size="sm" onClick={applyFocalPoint} disabled={isProcessing}>
              <Save className="mr-1.5 h-3.5 w-3.5" />
              Lưu focal point
            </Button>
          </div>
        )}

        {/* Image Canvas */}
        <div className="flex flex-1 items-center justify-center overflow-auto bg-[repeating-conic-gradient(#80808015_0%_25%,transparent_0%_50%)_50%_/_20px_20px] p-6">
          <div
            ref={imageRef}
            className="relative inline-block max-h-[55vh] max-w-full select-none"
            onMouseDown={handleCropMouseDown}
            onMouseMove={handleCropMouseMove}
            onMouseUp={handleCropMouseUp}
            onMouseLeave={handleCropMouseUp}
            onClick={handleFocalClick}
            style={{
              cursor:
                activeTool === "crop"
                  ? "crosshair"
                  : activeTool === "focal"
                    ? "crosshair"
                    : "default",
            }}
          >
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src={getImageUrl(media.url)}
              alt={media.alt || media.original_name}
              className="block max-h-[55vh] max-w-full object-contain"
              draggable={false}
            />

            {/* Crop overlay */}
            {activeTool === "crop" && cropBox.width > 2 && cropBox.height > 2 && (
              <>
                {/* Darkened area outside crop */}
                <div className="pointer-events-none absolute inset-0 bg-black/40" />
                {/* Clear crop area */}
                <div
                  className="pointer-events-none absolute border-2 border-white"
                  style={{
                    left: cropBox.x,
                    top: cropBox.y,
                    width: cropBox.width,
                    height: cropBox.height,
                    boxShadow: "0 0 0 9999px rgba(0,0,0,0.4)",
                    background: "transparent",
                  }}
                >
                  {/* Rule-of-thirds grid */}
                  <div className="absolute inset-0 grid grid-cols-3 grid-rows-3">
                    {Array.from({ length: 9 }).map((_, i) => (
                      <div key={i} className="border border-white/20" />
                    ))}
                  </div>
                  {/* Size label */}
                  <div className="absolute -bottom-6 left-1/2 -translate-x-1/2 rounded bg-black/60 px-1.5 py-0.5 text-[10px] whitespace-nowrap text-white">
                    {Math.round(cropBox.width)} × {Math.round(cropBox.height)}
                  </div>
                </div>
              </>
            )}

            {/* Focal point marker */}
            {activeTool === "focal" && (
              <div
                className="pointer-events-none absolute -mt-3 -ml-3 h-6 w-6 rounded-full border-2 border-red-500 bg-red-500/30"
                style={{
                  left: `${focalPoint.x * 100}%`,
                  top: `${focalPoint.y * 100}%`,
                }}
              >
                <div className="absolute inset-0 m-auto h-1.5 w-1.5 rounded-full bg-red-500" />
              </div>
            )}

            {/* Existing focal point indicator (subtle) when not editing */}
            {activeTool !== "focal" && media.focal_point && (
              <div
                className="pointer-events-none absolute -mt-2 -ml-2 h-4 w-4 rounded-full border border-blue-400 bg-blue-400/20"
                style={{
                  left: `${media.focal_point.x * 100}%`,
                  top: `${media.focal_point.y * 100}%`,
                }}
              />
            )}
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end gap-3 border-t px-6 py-3">
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
            <X className="mr-1.5 h-4 w-4" />
            Đóng
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
