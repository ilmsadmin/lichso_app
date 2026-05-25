"use client";

import { useState, useCallback, useRef } from "react";
import { Upload, Loader2, X, CloudUpload } from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";

interface MediaDropzoneProps {
  onUpload: (files: File[]) => void;
  isUploading?: boolean;
  accept?: string;
  maxFiles?: number;
  className?: string;
  compact?: boolean;
}

export function MediaDropzone({
  onUpload,
  isUploading = false,
  accept = "image/*,video/*,audio/*,application/pdf,.doc,.docx,.xls,.xlsx,.txt",
  maxFiles = 50,
  className,
  compact = false,
}: MediaDropzoneProps) {
  const [isDragging, setIsDragging] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const dragCounter = useRef(0);

  const handleDragEnter = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    dragCounter.current++;
    setIsDragging(true);
  }, []);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    dragCounter.current--;
    if (dragCounter.current === 0) {
      setIsDragging(false);
    }
  }, []);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
  }, []);

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      e.stopPropagation();
      setIsDragging(false);
      dragCounter.current = 0;

      const droppedFiles = Array.from(e.dataTransfer.files).slice(0, maxFiles);
      if (droppedFiles.length > 0) {
        onUpload(droppedFiles);
      }
    },
    [onUpload, maxFiles]
  );

  const handleFileSelect = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const files = e.target.files;
      if (!files) return;
      const arr = Array.from(files).slice(0, maxFiles);
      if (arr.length > 0) onUpload(arr);
      if (fileInputRef.current) fileInputRef.current.value = "";
    },
    [onUpload, maxFiles]
  );

  if (compact) {
    return (
      <div
        className={cn(
          "relative flex items-center gap-2 rounded-lg border-2 border-dashed p-3 transition-colors",
          isDragging
            ? "border-primary bg-primary/5"
            : "border-muted-foreground/20 hover:border-muted-foreground/40",
          className
        )}
        onDragEnter={handleDragEnter}
        onDragLeave={handleDragLeave}
        onDragOver={handleDragOver}
        onDrop={handleDrop}
      >
        <input
          ref={fileInputRef}
          type="file"
          multiple
          accept={accept}
          className="hidden"
          onChange={handleFileSelect}
        />
        {isUploading ? (
          <Loader2 className="text-primary h-5 w-5 animate-spin" />
        ) : (
          <CloudUpload className="text-muted-foreground h-5 w-5" />
        )}
        <span className="text-muted-foreground text-sm">
          Kéo thả hoặc{" "}
          <button
            className="text-primary font-medium underline"
            onClick={() => fileInputRef.current?.click()}
            disabled={isUploading}
          >
            chọn file
          </button>
        </span>
      </div>
    );
  }

  return (
    <div
      className={cn(
        "relative flex flex-col items-center justify-center rounded-xl border-2 border-dashed p-10 text-center transition-all",
        isDragging
          ? "border-primary bg-primary/5 scale-[1.01]"
          : "border-muted-foreground/20 hover:border-muted-foreground/40 hover:bg-muted/30",
        className
      )}
      onDragEnter={handleDragEnter}
      onDragLeave={handleDragLeave}
      onDragOver={handleDragOver}
      onDrop={handleDrop}
    >
      <input
        ref={fileInputRef}
        type="file"
        multiple
        accept={accept}
        className="hidden"
        onChange={handleFileSelect}
      />

      {isUploading ? (
        <>
          <Loader2 className="text-primary mb-4 h-10 w-10 animate-spin" />
          <p className="text-sm font-medium">Đang tải lên...</p>
        </>
      ) : isDragging ? (
        <>
          <Upload className="text-primary mb-4 h-10 w-10 animate-bounce" />
          <p className="text-primary text-sm font-medium">Thả file vào đây!</p>
        </>
      ) : (
        <>
          <CloudUpload className="text-muted-foreground/50 mb-4 h-10 w-10" />
          <p className="text-sm font-medium">Kéo & thả files vào đây</p>
          <p className="text-muted-foreground mt-1 text-xs">Hỗ trợ: ảnh, video, audio, tài liệu</p>
          <Button
            variant="outline"
            size="sm"
            className="mt-4"
            onClick={() => fileInputRef.current?.click()}
          >
            <Upload className="mr-1.5 h-3.5 w-3.5" />
            Chọn file
          </Button>
        </>
      )}
    </div>
  );
}
