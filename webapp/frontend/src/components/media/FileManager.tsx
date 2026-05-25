"use client";

import { useState, useCallback, useRef } from "react";
import {
  Upload,
  Trash2,
  Search,
  Grid,
  List,
  FolderOpen,
  Image,
  FileText,
  Film,
  Music,
  File,
  MoreVertical,
  Download,
  Pencil,
  Eye,
  CheckSquare,
  Square,
  Loader2,
  HardDrive,
} from "lucide-react";
import { formatDistanceToNow } from "date-fns";
import { useMedia } from "@/hooks/useMedia";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
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
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Pagination } from "@/components/shared/Pagination";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { cn, getImageUrl } from "@/lib/utils";
import type { MediaFile, MediaTypeFilter } from "@/types/media";

// ============================================
// Helper Functions
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
// FileManagerPage Component
// ============================================

export default function FileManagerPage() {
  const [page, setPage] = useState(1);
  const [limit] = useState(24);
  const [search, setSearch] = useState("");
  const [searchInput, setSearchInput] = useState("");
  const [typeFilter, setTypeFilter] = useState<MediaTypeFilter>("");
  const [folderFilter, setFolderFilter] = useState("");
  const [viewMode, setViewMode] = useState<"grid" | "list">("grid");
  const [selectedFiles, setSelectedFiles] = useState<Set<string>>(new Set());
  const [previewFile, setPreviewFile] = useState<MediaFile | null>(null);
  const [editFile, setEditFile] = useState<MediaFile | null>(null);
  const [editAlt, setEditAlt] = useState("");
  const [editDescription, setEditDescription] = useState("");
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const {
    files,
    meta,
    folders,
    stats,
    isLoading,
    upload,
    uploadMultiple,
    updateFile,
    deleteFile,
    deleteMultiple,
    isUploading,
    isDeleting,
  } = useMedia(page, 24, {
    folder: folderFilter || undefined,
    type: typeFilter || undefined,
    search: search || undefined,
  });

  // ============================================
  // Handlers
  // ============================================

  const handleFileSelect = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const fileList = e.target.files;
      if (!fileList || fileList.length === 0) return;

      const filesArray = Array.from(fileList);
      if (filesArray.length === 1) {
        upload({ file: filesArray[0], folder: folderFilter || undefined });
      } else {
        uploadMultiple({
          files: filesArray,
          folder: folderFilter || undefined,
        });
      }

      // Reset input
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    },
    [upload, uploadMultiple, folderFilter]
  );

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      const droppedFiles = Array.from(e.dataTransfer.files);
      if (droppedFiles.length === 0) return;

      if (droppedFiles.length === 1) {
        upload({ file: droppedFiles[0], folder: folderFilter || undefined });
      } else {
        uploadMultiple({
          files: droppedFiles,
          folder: folderFilter || undefined,
        });
      }
    },
    [upload, uploadMultiple, folderFilter]
  );

  const toggleSelectFile = (id: string) => {
    setSelectedFiles((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  const toggleSelectAll = () => {
    if (selectedFiles.size === files.length) {
      setSelectedFiles(new Set());
    } else {
      setSelectedFiles(new Set(files.map((f) => f.id)));
    }
  };

  const handleDeleteSelected = () => {
    if (selectedFiles.size > 0) {
      deleteMultiple(Array.from(selectedFiles));
      setSelectedFiles(new Set());
    }
  };

  const handleDeleteSingle = (id: string) => {
    setDeleteTarget(id);
    setDeleteConfirmOpen(true);
  };

  const confirmDelete = () => {
    if (deleteTarget) {
      deleteFile(deleteTarget);
      setDeleteTarget(null);
    }
    setDeleteConfirmOpen(false);
  };

  const openEditDialog = (file: MediaFile) => {
    setEditFile(file);
    setEditAlt(file.alt || "");
    setEditDescription(file.description || "");
  };

  const handleSaveEdit = () => {
    if (editFile) {
      updateFile({
        id: editFile.id,
        data: {
          alt: editAlt,
          description: editDescription,
        },
      });
      setEditFile(null);
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setSearch(searchInput);
    setPage(1);
  };

  // ============================================
  // Render
  // ============================================

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">File Manager</h1>
          <p className="text-muted-foreground">
            Upload and manage your files
            {stats && (
              <span className="ml-2">
                · {stats.total_files} files · {formatFileSize(stats.total_size)}
              </span>
            )}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <input
            ref={fileInputRef}
            type="file"
            multiple
            className="hidden"
            onChange={handleFileSelect}
            accept="image/*,application/pdf,.doc,.docx,.xls,.xlsx,.txt"
          />
          <Button onClick={() => fileInputRef.current?.click()} disabled={isUploading}>
            {isUploading ? (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            ) : (
              <Upload className="mr-2 h-4 w-4" />
            )}
            Upload Files
          </Button>
        </div>
      </div>

      {/* Toolbar */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex flex-1 items-center gap-2">
          {/* Search */}
          <form onSubmit={handleSearch} className="relative max-w-sm flex-1">
            <Search className="text-muted-foreground absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
            <Input
              type="search"
              placeholder="Search files..."
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              className="h-9 pl-9"
            />
          </form>

          {/* Type filter */}
          <Select
            value={typeFilter}
            onValueChange={(v) => {
              setTypeFilter(v as MediaTypeFilter);
              setPage(1);
            }}
          >
            <SelectTrigger className="h-9 w-[140px]">
              <SelectValue placeholder="All types" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All types</SelectItem>
              <SelectItem value="image">Images</SelectItem>
              <SelectItem value="document">Documents</SelectItem>
              <SelectItem value="video">Videos</SelectItem>
              <SelectItem value="audio">Audio</SelectItem>
            </SelectContent>
          </Select>

          {/* Folder filter */}
          {folders.length > 0 && (
            <Select
              value={folderFilter}
              onValueChange={(v) => {
                setFolderFilter(v === "all" ? "" : v);
                setPage(1);
              }}
            >
              <SelectTrigger className="h-9 w-[140px]">
                <SelectValue placeholder="All folders" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All folders</SelectItem>
                {folders.map((f) => (
                  <SelectItem key={f} value={f}>
                    <FolderOpen className="mr-1 inline h-3 w-3" />
                    {f === "/" ? "Root" : f}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
        </div>

        <div className="flex items-center gap-2">
          {/* Selection actions */}
          {selectedFiles.size > 0 && (
            <>
              <Badge variant="secondary">{selectedFiles.size} selected</Badge>
              <Button
                variant="destructive"
                size="sm"
                onClick={handleDeleteSelected}
                disabled={isDeleting}
              >
                <Trash2 className="mr-1 h-3 w-3" />
                Delete
              </Button>
              <Separator orientation="vertical" className="h-6" />
            </>
          )}

          {/* View toggle */}
          <div className="flex rounded-md border">
            <Button
              variant={viewMode === "grid" ? "secondary" : "ghost"}
              size="icon"
              className="h-8 w-8 rounded-r-none"
              onClick={() => setViewMode("grid")}
            >
              <Grid className="h-4 w-4" />
            </Button>
            <Button
              variant={viewMode === "list" ? "secondary" : "ghost"}
              size="icon"
              className="h-8 w-8 rounded-l-none"
              onClick={() => setViewMode("list")}
            >
              <List className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </div>

      {/* Drop zone (shown when not loading and no files) */}
      {!isLoading && files.length === 0 && !search && (
        <div
          className="hover:border-primary/50 flex flex-col items-center justify-center rounded-lg border-2 border-dashed p-12 text-center transition-colors"
          onDrop={handleDrop}
          onDragOver={(e) => e.preventDefault()}
        >
          <HardDrive className="text-muted-foreground/50 mb-4 h-12 w-12" />
          <h3 className="text-lg font-semibold">No files yet</h3>
          <p className="text-muted-foreground mt-1 text-sm">
            Drag & drop files here, or click Upload Files
          </p>
        </div>
      )}

      {/* File Grid / List */}
      {isLoading ? (
        <div className="flex items-center justify-center py-20">
          <Loader2 className="text-muted-foreground h-8 w-8 animate-spin" />
        </div>
      ) : viewMode === "grid" ? (
        <div
          className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6"
          onDrop={handleDrop}
          onDragOver={(e) => e.preventDefault()}
        >
          {files.map((file) => {
            const Icon = getFileIcon(file.mime_type);
            const isSelected = selectedFiles.has(file.id);
            const isImage = file.mime_type.startsWith("image/");

            return (
              <Card
                key={file.id}
                className={cn(
                  "group relative cursor-pointer overflow-hidden transition-all hover:shadow-md",
                  isSelected && "ring-primary ring-2"
                )}
              >
                {/* Select checkbox */}
                <button
                  className="absolute top-2 left-2 z-10 opacity-0 transition-opacity group-hover:opacity-100"
                  onClick={(e) => {
                    e.stopPropagation();
                    toggleSelectFile(file.id);
                  }}
                >
                  {isSelected ? (
                    <CheckSquare className="text-primary h-5 w-5" />
                  ) : (
                    <Square className="text-muted-foreground h-5 w-5" />
                  )}
                </button>

                {/* Actions */}
                <div className="absolute top-2 right-2 z-10 opacity-0 transition-opacity group-hover:opacity-100">
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="secondary" size="icon" className="h-7 w-7">
                        <MoreVertical className="h-3.5 w-3.5" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuItem onClick={() => setPreviewFile(file)}>
                        <Eye className="mr-2 h-4 w-4" />
                        Preview
                      </DropdownMenuItem>
                      <DropdownMenuItem onClick={() => openEditDialog(file)}>
                        <Pencil className="mr-2 h-4 w-4" />
                        Edit
                      </DropdownMenuItem>
                      <DropdownMenuItem asChild>
                        <a
                          href={getImageUrl(file.url)}
                          download={file.original_name}
                          target="_blank"
                          rel="noreferrer"
                        >
                          <Download className="mr-2 h-4 w-4" />
                          Download
                        </a>
                      </DropdownMenuItem>
                      <DropdownMenuItem
                        onClick={() => handleDeleteSingle(file.id)}
                        variant="destructive"
                      >
                        <Trash2 className="mr-2 h-4 w-4" />
                        Delete
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>

                {/* Thumbnail / Icon */}
                <div
                  className="bg-muted/50 flex aspect-square items-center justify-center overflow-hidden"
                  onClick={() => setPreviewFile(file)}
                >
                  {isImage ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img
                      src={getImageUrl(file.url)}
                      alt={file.alt || file.original_name}
                      className="h-full w-full object-cover"
                    />
                  ) : (
                    <Icon className="text-muted-foreground/50 h-12 w-12" />
                  )}
                </div>

                {/* Info */}
                <CardContent className="p-3">
                  <p className="truncate text-xs font-medium">{file.original_name}</p>
                  <p className="text-muted-foreground text-[11px]">{formatFileSize(file.size)}</p>
                </CardContent>
              </Card>
            );
          })}
        </div>
      ) : (
        /* List View */
        <div
          className="rounded-lg border"
          onDrop={handleDrop}
          onDragOver={(e) => e.preventDefault()}
        >
          {/* List header */}
          <div className="bg-muted/50 text-muted-foreground flex items-center gap-4 border-b px-4 py-2 text-xs font-medium">
            <button className="shrink-0" onClick={toggleSelectAll}>
              {selectedFiles.size === files.length && files.length > 0 ? (
                <CheckSquare className="text-primary h-4 w-4" />
              ) : (
                <Square className="h-4 w-4" />
              )}
            </button>
            <span className="flex-1">Name</span>
            <span className="w-20 text-right">Size</span>
            <span className="hidden w-24 text-right sm:block">Type</span>
            <span className="hidden w-32 text-right md:block">Uploaded</span>
            <span className="w-10" />
          </div>

          {files.map((file) => {
            const Icon = getFileIcon(file.mime_type);
            const isSelected = selectedFiles.has(file.id);

            return (
              <div
                key={file.id}
                className={cn(
                  "hover:bg-muted/30 flex items-center gap-4 border-b px-4 py-2.5 transition-colors last:border-b-0",
                  isSelected && "bg-primary/5"
                )}
              >
                <button className="shrink-0" onClick={() => toggleSelectFile(file.id)}>
                  {isSelected ? (
                    <CheckSquare className="text-primary h-4 w-4" />
                  ) : (
                    <Square className="text-muted-foreground h-4 w-4" />
                  )}
                </button>

                <div className="flex min-w-0 flex-1 items-center gap-3">
                  {file.mime_type.startsWith("image/") ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img
                      src={getImageUrl(file.url)}
                      alt={file.alt || file.original_name}
                      className="h-8 w-8 shrink-0 rounded object-cover"
                    />
                  ) : (
                    <Icon className="text-muted-foreground h-5 w-5 shrink-0" />
                  )}
                  <span className="truncate text-sm">{file.original_name}</span>
                </div>

                <span className="text-muted-foreground w-20 text-right text-xs">
                  {formatFileSize(file.size)}
                </span>
                <span className="text-muted-foreground hidden w-24 text-right text-xs sm:block">
                  {file.extension.toUpperCase()}
                </span>
                <span className="text-muted-foreground hidden w-32 text-right text-xs md:block">
                  {formatDistanceToNow(new Date(file.created_at), {
                    addSuffix: true,
                  })}
                </span>

                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="icon" className="h-8 w-8">
                      <MoreVertical className="h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem onClick={() => setPreviewFile(file)}>
                      <Eye className="mr-2 h-4 w-4" />
                      Preview
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => openEditDialog(file)}>
                      <Pencil className="mr-2 h-4 w-4" />
                      Edit
                    </DropdownMenuItem>
                    <DropdownMenuItem asChild>
                      <a
                        href={getImageUrl(file.url)}
                        download={file.original_name}
                        target="_blank"
                        rel="noreferrer"
                      >
                        <Download className="mr-2 h-4 w-4" />
                        Download
                      </a>
                    </DropdownMenuItem>
                    <DropdownMenuItem
                      onClick={() => handleDeleteSingle(file.id)}
                      variant="destructive"
                    >
                      <Trash2 className="mr-2 h-4 w-4" />
                      Delete
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>
            );
          })}
        </div>
      )}

      {/* Pagination */}
      {meta && meta.total_pages > 1 && (
        <Pagination
          page={page}
          limit={limit}
          total={meta.total}
          totalPages={meta.total_pages}
          onPageChange={setPage}
          onLimitChange={() => {}}
        />
      )}

      {/* Preview Dialog */}
      <Dialog open={!!previewFile} onOpenChange={(open) => !open && setPreviewFile(null)}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle className="truncate pr-8">{previewFile?.original_name}</DialogTitle>
          </DialogHeader>
          {previewFile && (
            <div className="space-y-4">
              {previewFile.mime_type.startsWith("image/") ? (
                <div className="bg-muted/30 flex justify-center rounded-lg p-4">
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    src={getImageUrl(previewFile.url)}
                    alt={previewFile.alt || previewFile.original_name}
                    className="max-h-[60vh] rounded object-contain"
                  />
                </div>
              ) : previewFile.mime_type.startsWith("video/") ? (
                <video src={getImageUrl(previewFile.url)} controls className="w-full rounded-lg" />
              ) : previewFile.mime_type.startsWith("audio/") ? (
                <audio src={getImageUrl(previewFile.url)} controls className="w-full" />
              ) : (
                <div className="flex flex-col items-center justify-center py-12">
                  {(() => {
                    const PIcon = getFileIcon(previewFile.mime_type);
                    return <PIcon className="text-muted-foreground h-16 w-16" />;
                  })()}
                  <p className="text-muted-foreground mt-4">Preview not available</p>
                </div>
              )}

              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span className="text-muted-foreground">Size:</span>{" "}
                  {formatFileSize(previewFile.size)}
                </div>
                <div>
                  <span className="text-muted-foreground">Type:</span> {previewFile.mime_type}
                </div>
                <div>
                  <span className="text-muted-foreground">Uploaded:</span>{" "}
                  {formatDistanceToNow(new Date(previewFile.created_at), {
                    addSuffix: true,
                  })}
                </div>
                <div>
                  <span className="text-muted-foreground">By:</span>{" "}
                  {previewFile.uploaded_name || "Unknown"}
                </div>
              </div>

              <div className="flex justify-end gap-2">
                <Button variant="outline" asChild>
                  <a
                    href={getImageUrl(previewFile.url)}
                    download={previewFile.original_name}
                    target="_blank"
                    rel="noreferrer"
                  >
                    <Download className="mr-2 h-4 w-4" />
                    Download
                  </a>
                </Button>
                <Button
                  variant="outline"
                  onClick={() => {
                    navigator.clipboard.writeText(getImageUrl(previewFile.url));
                    import("sonner").then(({ toast }) => toast.success("URL copied to clipboard"));
                  }}
                >
                  Copy URL
                </Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={!!editFile} onOpenChange={(open) => !open && setEditFile(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit File Details</DialogTitle>
          </DialogHeader>
          {editFile && (
            <div className="space-y-4">
              <div className="flex items-center gap-3">
                {editFile.mime_type.startsWith("image/") ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    src={getImageUrl(editFile.url)}
                    alt={editFile.original_name}
                    className="h-12 w-12 rounded object-cover"
                  />
                ) : (
                  (() => {
                    const EIcon = getFileIcon(editFile.mime_type);
                    return <EIcon className="text-muted-foreground h-8 w-8" />;
                  })()
                )}
                <div>
                  <p className="max-w-[300px] truncate text-sm font-medium">
                    {editFile.original_name}
                  </p>
                  <p className="text-muted-foreground text-xs">{formatFileSize(editFile.size)}</p>
                </div>
              </div>

              <div className="space-y-3">
                <div>
                  <Label htmlFor="alt">Alt Text</Label>
                  <Input
                    id="alt"
                    value={editAlt}
                    onChange={(e) => setEditAlt(e.target.value)}
                    placeholder="Image description for accessibility"
                  />
                </div>
                <div>
                  <Label htmlFor="description">Description</Label>
                  <Textarea
                    id="description"
                    value={editDescription}
                    onChange={(e) => setEditDescription(e.target.value)}
                    placeholder="File description"
                    rows={3}
                  />
                </div>
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditFile(null)}>
              Cancel
            </Button>
            <Button onClick={handleSaveEdit}>Save Changes</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation */}
      <ConfirmDialog
        open={deleteConfirmOpen}
        onOpenChange={setDeleteConfirmOpen}
        title="Delete File"
        description="Are you sure you want to delete this file? This action cannot be undone."
        onConfirm={confirmDelete}
        variant="destructive"
      />
    </div>
  );
}
