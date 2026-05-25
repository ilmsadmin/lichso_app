"use client";

import { useState, useRef, useCallback } from "react";
import {
  Upload,
  Loader2,
  Film,
  Youtube,
  Link as LinkIcon,
  Play,
  Pause,
  Volume2,
  VolumeX,
  Maximize,
  X,
  CheckCircle2,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { cn, getImageUrl } from "@/lib/utils";
import { uploadFileV3 } from "@/services/mediaV3Service";
import { toast } from "sonner";
import type { MediaFileV3 } from "@/types/media";

// ============================================
// Video URL Parser
// ============================================

export interface ParsedVideoUrl {
  provider: "youtube" | "vimeo" | "unknown";
  videoId: string;
  embedUrl: string;
  thumbnailUrl: string;
  originalUrl: string;
}

/**
 * Parse YouTube/Vimeo URLs and extract video IDs + embed/thumbnail URLs
 */
export function parseVideoUrl(url: string): ParsedVideoUrl | null {
  if (!url) return null;

  // YouTube patterns
  const ytPatterns = [
    /(?:youtube\.com\/watch\?v=|youtu\.be\/|youtube\.com\/embed\/)([a-zA-Z0-9_-]{11})/,
    /youtube\.com\/shorts\/([a-zA-Z0-9_-]{11})/,
  ];

  for (const pattern of ytPatterns) {
    const match = url.match(pattern);
    if (match?.[1]) {
      return {
        provider: "youtube",
        videoId: match[1],
        embedUrl: `https://www.youtube.com/embed/${match[1]}`,
        thumbnailUrl: `https://img.youtube.com/vi/${match[1]}/maxresdefault.jpg`,
        originalUrl: url,
      };
    }
  }

  // Vimeo patterns
  const vimeoPatterns = [/vimeo\.com\/(\d+)/, /player\.vimeo\.com\/video\/(\d+)/];

  for (const pattern of vimeoPatterns) {
    const match = url.match(pattern);
    if (match?.[1]) {
      return {
        provider: "vimeo",
        videoId: match[1],
        embedUrl: `https://player.vimeo.com/video/${match[1]}`,
        thumbnailUrl: `https://vumbnail.com/${match[1]}.jpg`,
        originalUrl: url,
      };
    }
  }

  return null;
}

// ============================================
// Video Embed Player
// ============================================

interface VideoEmbedPlayerProps {
  url: string;
  className?: string;
  autoplay?: boolean;
}

export function VideoEmbedPlayer({ url, className, autoplay = false }: VideoEmbedPlayerProps) {
  const parsed = parseVideoUrl(url);

  if (!parsed) {
    // Fallback: try as direct video URL
    return (
      <video
        src={url.startsWith("/") ? getImageUrl(url) : url}
        controls
        autoPlay={autoplay}
        className={cn("w-full rounded-lg", className)}
      />
    );
  }

  const embedSrc = `${parsed.embedUrl}?autoplay=${autoplay ? 1 : 0}&rel=0`;

  return (
    <div className={cn("relative aspect-video overflow-hidden rounded-lg bg-black", className)}>
      <iframe
        src={embedSrc}
        className="absolute inset-0 h-full w-full"
        allow="autoplay; fullscreen; picture-in-picture"
        allowFullScreen
        title="Video player"
      />
    </div>
  );
}

// ============================================
// Video Player (for uploaded videos)
// ============================================

interface VideoPlayerProps {
  src: string;
  poster?: string;
  className?: string;
}

export function VideoPlayer({ src, poster, className }: VideoPlayerProps) {
  const videoRef = useRef<HTMLVideoElement>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isMuted, setIsMuted] = useState(false);
  const [progress, setProgress] = useState(0);
  const [duration, setDuration] = useState(0);

  const togglePlay = () => {
    if (!videoRef.current) return;
    if (isPlaying) {
      videoRef.current.pause();
    } else {
      videoRef.current.play();
    }
    setIsPlaying(!isPlaying);
  };

  const toggleMute = () => {
    if (!videoRef.current) return;
    videoRef.current.muted = !isMuted;
    setIsMuted(!isMuted);
  };

  const handleTimeUpdate = () => {
    if (!videoRef.current) return;
    setProgress(videoRef.current.currentTime);
  };

  const handleLoadedMetadata = () => {
    if (!videoRef.current) return;
    setDuration(videoRef.current.duration);
  };

  const handleSeek = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!videoRef.current) return;
    const time = parseFloat(e.target.value);
    videoRef.current.currentTime = time;
    setProgress(time);
  };

  const handleFullscreen = () => {
    videoRef.current?.requestFullscreen();
  };

  const formatTime = (seconds: number) => {
    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds % 60);
    return `${m}:${s.toString().padStart(2, "0")}`;
  };

  const videoSrc = src.startsWith("/") ? getImageUrl(src) : src;

  return (
    <div className={cn("group relative overflow-hidden rounded-lg bg-black", className)}>
      <video
        ref={videoRef}
        src={videoSrc}
        poster={poster ? getImageUrl(poster) : undefined}
        onTimeUpdate={handleTimeUpdate}
        onLoadedMetadata={handleLoadedMetadata}
        onEnded={() => setIsPlaying(false)}
        className="w-full"
        onClick={togglePlay}
      />

      {/* Controls overlay */}
      <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/80 to-transparent p-3 opacity-0 transition-opacity group-hover:opacity-100">
        {/* Progress bar */}
        <input
          type="range"
          min={0}
          max={duration || 0}
          step={0.1}
          value={progress}
          onChange={handleSeek}
          className="accent-primary mb-2 h-1 w-full cursor-pointer"
        />

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={togglePlay}
            className="hover:text-primary text-white transition-colors"
          >
            {isPlaying ? <Pause className="h-5 w-5" /> : <Play className="h-5 w-5" />}
          </button>

          <span className="min-w-[80px] text-xs text-white/80">
            {formatTime(progress)} / {formatTime(duration)}
          </span>

          <div className="flex-1" />

          <button
            type="button"
            onClick={toggleMute}
            className="hover:text-primary text-white transition-colors"
          >
            {isMuted ? <VolumeX className="h-4 w-4" /> : <Volume2 className="h-4 w-4" />}
          </button>

          <button
            type="button"
            onClick={handleFullscreen}
            className="hover:text-primary text-white transition-colors"
          >
            <Maximize className="h-4 w-4" />
          </button>
        </div>
      </div>

      {/* Play button overlay when paused */}
      {!isPlaying && (
        <button
          type="button"
          onClick={togglePlay}
          className="absolute inset-0 flex items-center justify-center"
        >
          <div className="rounded-full bg-black/50 p-4 backdrop-blur-sm transition-transform hover:scale-110">
            <Play className="h-8 w-8 fill-white text-white" />
          </div>
        </button>
      )}
    </div>
  );
}

// ============================================
// Chunk Upload Progress UI
// ============================================

interface ChunkUploadProgress {
  filename: string;
  totalChunks: number;
  uploadedChunks: number;
  totalSize: number;
  status: "uploading" | "processing" | "completed" | "error";
  errorMessage?: string;
}

interface ChunkUploadProgressUIProps {
  uploads: ChunkUploadProgress[];
  className?: string;
}

export function ChunkUploadProgressUI({ uploads, className }: ChunkUploadProgressUIProps) {
  if (uploads.length === 0) return null;

  return (
    <div className={cn("space-y-2", className)}>
      {uploads.map((upload, idx) => {
        const progress =
          upload.totalChunks > 0 ? (upload.uploadedChunks / upload.totalChunks) * 100 : 0;

        return (
          <div
            key={`${upload.filename}-${idx}`}
            className="flex items-center gap-3 rounded-lg border p-3"
          >
            <Film className="text-muted-foreground h-5 w-5 shrink-0" />
            <div className="min-w-0 flex-1">
              <div className="mb-1 flex items-center justify-between">
                <p className="truncate text-sm font-medium">{upload.filename}</p>
                <Badge
                  variant={
                    upload.status === "completed"
                      ? "default"
                      : upload.status === "error"
                        ? "destructive"
                        : "secondary"
                  }
                  className="shrink-0 text-[10px]"
                >
                  {upload.status === "uploading" && "Đang tải lên"}
                  {upload.status === "processing" && "Đang xử lý"}
                  {upload.status === "completed" && "Hoàn tất"}
                  {upload.status === "error" && "Lỗi"}
                </Badge>
              </div>
              {/* Progress bar */}
              <div className="bg-muted h-1.5 w-full overflow-hidden rounded-full">
                <div
                  className={cn(
                    "h-full rounded-full transition-all duration-300",
                    upload.status === "error"
                      ? "bg-destructive"
                      : upload.status === "completed"
                        ? "bg-green-500"
                        : "bg-primary"
                  )}
                  style={{ width: `${progress}%` }}
                />
              </div>
              <div className="mt-1 flex items-center justify-between">
                <span className="text-muted-foreground text-[10px]">
                  {upload.uploadedChunks}/{upload.totalChunks} chunks
                </span>
                <span className="text-muted-foreground text-[10px]">
                  {formatFileSize(upload.totalSize)}
                </span>
              </div>
              {upload.errorMessage && (
                <p className="text-destructive mt-1 text-xs">{upload.errorMessage}</p>
              )}
            </div>
            {upload.status === "completed" && (
              <CheckCircle2 className="h-5 w-5 shrink-0 text-green-500" />
            )}
            {upload.status === "uploading" && (
              <Loader2 className="text-primary h-5 w-5 shrink-0 animate-spin" />
            )}
          </div>
        );
      })}
    </div>
  );
}

// ============================================
// Video Upload Dialog
// ============================================

interface VideoUploadDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onUploaded?: (file: MediaFileV3) => void;
}

export function VideoUploadDialog({ open, onOpenChange, onUploaded }: VideoUploadDialogProps) {
  const [activeTab, setActiveTab] = useState<"upload" | "url">("upload");
  const [urlInput, setUrlInput] = useState("");
  const [isUploading, setIsUploading] = useState(false);
  const [parsedUrl, setParsedUrl] = useState<ParsedVideoUrl | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileUpload = useCallback(
    async (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (!file) return;

      if (!file.type.startsWith("video/")) {
        toast.error("Vui lòng chọn file video");
        return;
      }

      if (file.size > 500 * 1024 * 1024) {
        toast.error("Kích thước tối đa 500MB");
        return;
      }

      setIsUploading(true);
      try {
        const response = await uploadFileV3(file, "videos");
        if (response.success && response.data) {
          onUploaded?.(response.data);
          onOpenChange(false);
          toast.success("Upload video thành công");
        } else {
          toast.error("Không thể upload video");
        }
      } catch {
        toast.error("Upload video thất bại");
      } finally {
        setIsUploading(false);
        if (fileInputRef.current) fileInputRef.current.value = "";
      }
    },
    [onUploaded, onOpenChange]
  );

  const handleUrlChange = (url: string) => {
    setUrlInput(url);
    setParsedUrl(parseVideoUrl(url));
  };

  const handleUrlSubmit = () => {
    // For external URLs, we don't upload but return the parsed data
    if (parsedUrl) {
      toast.success(`Đã nhận URL ${parsedUrl.provider}`);
      onOpenChange(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Thêm video</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          {/* Tab toggle */}
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
              <Youtube className="mr-1.5 inline h-4 w-4" />
              YouTube / Vimeo
            </button>
          </div>

          {/* Upload tab */}
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
                accept="video/mp4,video/webm,video/ogg"
                className="hidden"
                onChange={handleFileUpload}
              />
              {isUploading ? (
                <>
                  <Loader2 className="text-primary mb-2 h-8 w-8 animate-spin" />
                  <span className="text-muted-foreground text-sm">Đang upload video...</span>
                </>
              ) : (
                <>
                  <Film className="text-muted-foreground/50 mb-2 h-8 w-8" />
                  <p className="text-muted-foreground text-sm">Kéo thả hoặc chọn file video</p>
                  <p className="text-muted-foreground mt-1 text-xs">MP4, WebM (tối đa 500MB)</p>
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

          {/* URL tab */}
          {activeTab === "url" && (
            <div className="space-y-3">
              <div className="space-y-2">
                <Label htmlFor="video-url-input">URL video</Label>
                <Input
                  id="video-url-input"
                  value={urlInput}
                  onChange={(e) => handleUrlChange(e.target.value)}
                  placeholder="https://youtube.com/watch?v=... hoặc https://vimeo.com/..."
                  onKeyDown={(e) => e.key === "Enter" && handleUrlSubmit()}
                />
              </div>

              {/* Parsed preview */}
              {parsedUrl && (
                <div className="overflow-hidden rounded-lg border">
                  <div className="relative aspect-video">
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img
                      src={parsedUrl.thumbnailUrl}
                      alt="Video thumbnail"
                      className="h-full w-full object-cover"
                      onError={(e) => {
                        (e.target as HTMLImageElement).style.display = "none";
                      }}
                    />
                    <div className="absolute inset-0 flex items-center justify-center">
                      <div className="rounded-full bg-black/50 p-3 backdrop-blur-sm">
                        <Play className="h-6 w-6 fill-white text-white" />
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 p-2.5">
                    <Badge variant="secondary" className="text-xs capitalize">
                      {parsedUrl.provider === "youtube" && <Youtube className="mr-1 h-3 w-3" />}
                      {parsedUrl.provider}
                    </Badge>
                    <span className="text-muted-foreground text-xs">ID: {parsedUrl.videoId}</span>
                  </div>
                </div>
              )}

              {urlInput && !parsedUrl && (
                <p className="text-muted-foreground text-xs">Hỗ trợ URL YouTube và Vimeo</p>
              )}

              <Button
                type="button"
                onClick={handleUrlSubmit}
                disabled={!parsedUrl}
                className="w-full"
              >
                Sử dụng video này
              </Button>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
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
