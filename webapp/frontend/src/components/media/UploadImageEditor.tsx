"use client";

import { useState, useRef, useCallback, useEffect } from "react";
import {
  Crop,
  Maximize,
  RotateCcw,
  RotateCw,
  Upload,
  X,
  Loader2,
  SquareIcon,
  RectangleHorizontal,
  MonitorSmartphone,
  Share2,
  Lock,
  Unlock,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { cn } from "@/lib/utils";

// ============================================
// Types
// ============================================

interface UploadImageEditorProps {
  /** Whether the dialog is open */
  open: boolean;
  /** The image file to edit before uploading */
  file: File | null;
  /** Called when the user cancels — no upload happens */
  onCancel: () => void;
  /** Called with the (possibly transformed) file ready to upload */
  onConfirm: (file: File) => void;
}

interface CropPreset {
  label: string;
  icon: React.ReactNode;
  ratio: number | null; // null = free crop
}

interface CropBox {
  x: number;
  y: number;
  width: number;
  height: number;
}

// ============================================
// Crop Presets (mirrors ImageEditor)
// ============================================

const CROP_PRESETS: CropPreset[] = [
  { label: "Tự do", icon: <Crop className="h-3.5 w-3.5" />, ratio: null },
  { label: "1:1", icon: <SquareIcon className="h-3.5 w-3.5" />, ratio: 1 },
  { label: "16:9", icon: <RectangleHorizontal className="h-3.5 w-3.5" />, ratio: 16 / 9 },
  { label: "4:3", icon: <MonitorSmartphone className="h-3.5 w-3.5" />, ratio: 4 / 3 },
  { label: "OG 1200×630", icon: <Share2 className="h-3.5 w-3.5" />, ratio: 1200 / 630 },
];

// Image mime types we can safely transform on a canvas without losing data
// (animated GIF and SVG are handled by uploading the original untouched).
const CANVAS_SAFE_TYPES = ["image/jpeg", "image/png", "image/webp"];

export function isEditableImage(file: File): boolean {
  return CANVAS_SAFE_TYPES.includes(file.type);
}

// ============================================
// Component
// ============================================

export function UploadImageEditor({ open, file, onCancel, onConfirm }: UploadImageEditorProps) {
  const [objectUrl, setObjectUrl] = useState<string | null>(null);
  const [natural, setNatural] = useState<{ width: number; height: number } | null>(null);
  const [processing, setProcessing] = useState(false);

  // Active tool
  const [activeTool, setActiveTool] = useState<"crop" | "resize" | null>(null);

  // Rotation applied to the source (degrees, multiples of 90)
  const [rotation, setRotation] = useState(0);

  // Crop state (display-space coordinates relative to the rendered image)
  const [cropPresetIdx, setCropPresetIdx] = useState(0);
  const [cropBox, setCropBox] = useState<CropBox | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
  const imageRef = useRef<HTMLDivElement>(null);

  // Resize state
  const [resizeW, setResizeW] = useState("");
  const [resizeH, setResizeH] = useState("");
  const [lockAspect, setLockAspect] = useState(true);

  // Create / revoke object URL for the selected file
  useEffect(() => {
    if (!file) {
      setObjectUrl(null);
      return;
    }
    const url = URL.createObjectURL(file);
    setObjectUrl(url);
    return () => URL.revokeObjectURL(url);
  }, [file]);

  // Reset all editing state whenever a new file/open cycle starts
  useEffect(() => {
    if (open) {
      setActiveTool(null);
      setRotation(0);
      setCropPresetIdx(0);
      setCropBox(null);
      setNatural(null);
      setResizeW("");
      setResizeH("");
      setLockAspect(true);
      setProcessing(false);
    }
  }, [open, file]);

  // The "effective" natural dimensions account for 90/270° rotation swapping axes
  const effectiveNatural = (() => {
    if (!natural) return null;
    const rotated = rotation % 180 !== 0;
    return rotated
      ? { width: natural.height, height: natural.width }
      : { width: natural.width, height: natural.height };
  })();

  const onImageLoad = (e: React.SyntheticEvent<HTMLImageElement>) => {
    const img = e.currentTarget;
    setNatural({ width: img.naturalWidth, height: img.naturalHeight });
  };

  // Default the resize inputs to the effective natural size when known
  useEffect(() => {
    if (effectiveNatural && resizeW === "" && resizeH === "") {
      setResizeW(String(effectiveNatural.width));
      setResizeH(String(effectiveNatural.height));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [effectiveNatural]);

  // ============================================
  // Crop drag handlers
  // ============================================

  const handleMouseDown = useCallback(
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

  const handleMouseMove = useCallback(
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

  const handleMouseUp = useCallback(() => setIsDragging(false), []);

  // ============================================
  // Resize input handlers (with aspect lock)
  // ============================================

  // The aspect ratio used to lock width/height — derived from the crop region
  // if one is drawn, otherwise from the effective natural dimensions.
  const aspectRatio = (() => {
    if (cropBox && cropBox.width > 2 && cropBox.height > 2) {
      return cropBox.width / cropBox.height;
    }
    if (effectiveNatural) return effectiveNatural.width / effectiveNatural.height;
    return null;
  })();

  const handleResizeWChange = (val: string) => {
    setResizeW(val);
    if (lockAspect && aspectRatio) {
      const w = parseInt(val, 10);
      if (w > 0) setResizeH(String(Math.round(w / aspectRatio)));
    }
  };

  const handleResizeHChange = (val: string) => {
    setResizeH(val);
    if (lockAspect && aspectRatio) {
      const h = parseInt(val, 10);
      if (h > 0) setResizeW(String(Math.round(h * aspectRatio)));
    }
  };

  // ============================================
  // Build the transformed file
  // ============================================

  const buildTransformedFile = useCallback(async (): Promise<File | null> => {
    if (!file || !objectUrl || !natural) return null;

    // Load a fresh image element to draw from
    const img = await new Promise<HTMLImageElement>((resolve, reject) => {
      const el = new Image();
      el.onload = () => resolve(el);
      el.onerror = reject;
      el.src = objectUrl;
    });

    // 1. Apply rotation to an intermediate canvas (in source pixel space)
    const rot = ((rotation % 360) + 360) % 360;
    const rotated = document.createElement("canvas");
    if (rot % 180 !== 0) {
      rotated.width = img.naturalHeight;
      rotated.height = img.naturalWidth;
    } else {
      rotated.width = img.naturalWidth;
      rotated.height = img.naturalHeight;
    }
    const rctx = rotated.getContext("2d");
    if (!rctx) return null;
    rctx.save();
    rctx.translate(rotated.width / 2, rotated.height / 2);
    rctx.rotate((rot * Math.PI) / 180);
    rctx.drawImage(img, -img.naturalWidth / 2, -img.naturalHeight / 2);
    rctx.restore();

    // 2. Determine the crop region in the rotated image's pixel space
    let sx = 0;
    let sy = 0;
    let sw = rotated.width;
    let sh = rotated.height;

    if (cropBox && cropBox.width > 2 && cropBox.height > 2 && imageRef.current) {
      const rect = imageRef.current.getBoundingClientRect();
      const scaleX = rotated.width / rect.width;
      const scaleY = rotated.height / rect.height;
      sx = Math.max(0, Math.round(cropBox.x * scaleX));
      sy = Math.max(0, Math.round(cropBox.y * scaleY));
      sw = Math.min(rotated.width - sx, Math.round(cropBox.width * scaleX));
      sh = Math.min(rotated.height - sy, Math.round(cropBox.height * scaleY));
    }

    // 3. Determine output dimensions (resize)
    let outW = sw;
    let outH = sh;
    const rw = parseInt(resizeW, 10);
    const rh = parseInt(resizeH, 10);
    if (rw > 0 && rh > 0 && (rw !== effectiveNatural?.width || rh !== effectiveNatural?.height)) {
      outW = rw;
      outH = rh;
    }

    // 4. Draw final result
    const out = document.createElement("canvas");
    out.width = outW;
    out.height = outH;
    const octx = out.getContext("2d");
    if (!octx) return null;
    octx.imageSmoothingQuality = "high";
    // White backing for opaque JPEGs so transparency doesn't render black
    if (file.type === "image/jpeg") {
      octx.fillStyle = "#ffffff";
      octx.fillRect(0, 0, outW, outH);
    }
    octx.drawImage(rotated, sx, sy, sw, sh, 0, 0, outW, outH);

    // 5. Export to a Blob of the same type, wrapped in a File with the same name
    const blob = await new Promise<Blob | null>((resolve) =>
      out.toBlob(resolve, file.type, file.type === "image/jpeg" ? 0.92 : undefined)
    );
    if (!blob) return null;

    return new File([blob], file.name, { type: file.type, lastModified: Date.now() });
  }, [file, objectUrl, natural, rotation, cropBox, resizeW, resizeH, effectiveNatural]);

  const handleUploadEdited = async () => {
    setProcessing(true);
    try {
      const edited = await buildTransformedFile();
      onConfirm(edited ?? file!);
    } catch {
      // On any failure, fall back to the original file
      onConfirm(file!);
    } finally {
      setProcessing(false);
    }
  };

  const handleUploadOriginal = () => {
    if (file) onConfirm(file);
  };

  // Whether any edit has been applied (controls "apply" vs "original" labelling)
  const hasEdits =
    rotation !== 0 ||
    (cropBox !== null && cropBox.width > 2 && cropBox.height > 2) ||
    (!!effectiveNatural &&
      (parseInt(resizeW, 10) !== effectiveNatural.width ||
        parseInt(resizeH, 10) !== effectiveNatural.height));

  if (!file) return null;

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onCancel()}>
      <DialogContent className="flex max-h-[90vh] flex-col p-0 sm:max-w-4xl">
        <DialogHeader className="px-6 pt-6 pb-0">
          <DialogTitle className="flex flex-wrap items-center gap-2">
            Chỉnh sửa trước khi tải lên
            <Badge variant="secondary" className="max-w-[240px] truncate text-xs font-normal">
              {file.name}
            </Badge>
            {effectiveNatural && (
              <Badge variant="outline" className="text-xs font-normal">
                {effectiveNatural.width} × {effectiveNatural.height}
              </Badge>
            )}
          </DialogTitle>
        </DialogHeader>

        {/* Toolbar */}
        <div className="flex flex-wrap items-center gap-1 border-b px-6 py-2">
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

          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={() => setRotation((r) => (r - 90 + 360) % 360)}
          >
            <RotateCcw className="mr-1.5 h-4 w-4" />
            Xoay trái
          </Button>
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={() => setRotation((r) => (r + 90) % 360)}
          >
            <RotateCw className="mr-1.5 h-4 w-4" />
            Xoay phải
          </Button>

          <Separator orientation="vertical" className="h-6" />

          <Button
            type="button"
            variant={activeTool === "resize" ? "default" : "ghost"}
            size="sm"
            onClick={() => setActiveTool(activeTool === "resize" ? null : "resize")}
          >
            <Maximize className="mr-1.5 h-4 w-4" />
            Thay kích thước
          </Button>
        </div>

        {/* Crop presets bar */}
        {activeTool === "crop" && (
          <div className="bg-muted/50 flex flex-wrap items-center gap-2 border-b px-6 py-2">
            <span className="text-muted-foreground mr-1 text-xs font-medium">Tỉ lệ:</span>
            {CROP_PRESETS.map((preset, idx) => (
              <Button
                key={preset.label}
                type="button"
                variant={cropPresetIdx === idx ? "default" : "outline"}
                size="sm"
                className="h-7 text-xs"
                onClick={() => {
                  setCropPresetIdx(idx);
                  setCropBox(null);
                }}
              >
                {preset.icon}
                <span className="ml-1">{preset.label}</span>
              </Button>
            ))}
            <span className="text-muted-foreground ml-auto text-xs">
              Kéo chọn vùng cần cắt trên ảnh
            </span>
          </div>
        )}

        {/* Resize panel */}
        {activeTool === "resize" && (
          <div className="bg-muted/50 flex flex-wrap items-center gap-3 border-b px-6 py-2">
            <Label className="text-xs">Rộng:</Label>
            <Input
              value={resizeW}
              onChange={(e) => handleResizeWChange(e.target.value)}
              className="h-7 w-20 text-xs"
              type="number"
              min={1}
            />
            <Label className="text-xs">Cao:</Label>
            <Input
              value={resizeH}
              onChange={(e) => handleResizeHChange(e.target.value)}
              className="h-7 w-20 text-xs"
              type="number"
              min={1}
            />
            <span className="text-muted-foreground text-xs">px</span>
            <Button
              type="button"
              variant={lockAspect ? "default" : "outline"}
              size="sm"
              className="h-7 text-xs"
              onClick={() => setLockAspect((v) => !v)}
            >
              {lockAspect ? (
                <Lock className="mr-1 h-3.5 w-3.5" />
              ) : (
                <Unlock className="mr-1 h-3.5 w-3.5" />
              )}
              Giữ tỉ lệ
            </Button>
          </div>
        )}

        {/* Image canvas */}
        <div className="flex flex-1 items-center justify-center overflow-auto bg-[repeating-conic-gradient(#80808015_0%_25%,transparent_0%_50%)_50%_/_20px_20px] p-6">
          {objectUrl && (
            <div
              ref={imageRef}
              className="relative inline-block max-h-[55vh] max-w-full select-none"
              onMouseDown={handleMouseDown}
              onMouseMove={handleMouseMove}
              onMouseUp={handleMouseUp}
              onMouseLeave={handleMouseUp}
              style={{ cursor: activeTool === "crop" ? "crosshair" : "default" }}
            >
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src={objectUrl}
                alt={file.name}
                onLoad={onImageLoad}
                className="block max-h-[55vh] max-w-full object-contain"
                draggable={false}
                style={{ transform: `rotate(${rotation}deg)` }}
              />

              {/* Crop overlay */}
              {activeTool === "crop" && cropBox && cropBox.width > 2 && cropBox.height > 2 && (
                <div
                  className="pointer-events-none absolute border-2 border-white"
                  style={{
                    left: cropBox.x,
                    top: cropBox.y,
                    width: cropBox.width,
                    height: cropBox.height,
                    boxShadow: "0 0 0 9999px rgba(0,0,0,0.4)",
                  }}
                >
                  <div className="absolute inset-0 grid grid-cols-3 grid-rows-3">
                    {Array.from({ length: 9 }).map((_, i) => (
                      <div key={i} className="border border-white/20" />
                    ))}
                  </div>
                  <div className="absolute -bottom-6 left-1/2 -translate-x-1/2 rounded bg-black/60 px-1.5 py-0.5 text-[10px] whitespace-nowrap text-white">
                    {Math.round(cropBox.width)} × {Math.round(cropBox.height)}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="flex flex-wrap items-center justify-end gap-3 border-t px-6 py-3">
          <Button type="button" variant="ghost" onClick={onCancel} disabled={processing}>
            <X className="mr-1.5 h-4 w-4" />
            Hủy
          </Button>
          <Button
            type="button"
            variant="outline"
            onClick={handleUploadOriginal}
            disabled={processing}
            className={cn(!hasEdits && "hidden")}
          >
            Tải ảnh gốc
          </Button>
          <Button type="button" onClick={handleUploadEdited} disabled={processing || !natural}>
            {processing ? (
              <Loader2 className="mr-1.5 h-4 w-4 animate-spin" />
            ) : (
              <Upload className="mr-1.5 h-4 w-4" />
            )}
            {hasEdits ? "Áp dụng & tải lên" : "Tải lên"}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
