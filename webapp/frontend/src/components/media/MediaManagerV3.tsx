"use client";

import { useState, useCallback, useMemo } from "react";
import {
  Upload,
  Grid,
  List,
  CheckSquare,
  Square,
  MoreVertical,
  Eye,
  Pencil,
  Download,
  Trash2,
  Star,
  Image,
  Film,
  Music,
  FileText,
  File,
  Loader2,
  FolderOpen,
  Images,
  HardDrive,
  Link,
} from "lucide-react";
import { formatDistanceToNow } from "date-fns";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
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
import { Pagination } from "@/components/shared/Pagination";
import { cn, getImageUrl } from "@/lib/utils";
import {
  useMediaListV3,
  useUploadV3,
  useUploadMultipleV3,
  useUploadFromURL,
  useSoftDeleteMedia,
  useToggleFavorite,
} from "@/hooks/useMediaV3";
import type { MediaFileV3, MediaListParams, MediaAlbum } from "@/types/media";

// Sub-components
import { FolderTree } from "@/components/media/FolderTree";
import { MediaSearchBar } from "@/components/media/MediaSearchBar";
import { MediaDropzone } from "@/components/media/MediaDropzone";
import { UploadImageEditor, isEditableImage } from "@/components/media/UploadImageEditor";
import { MediaStatsCards } from "@/components/media/MediaStatsCards";
import { MediaDetailPanel } from "@/components/media/MediaDetailPanel";
import { MediaBulkActions } from "@/components/media/MediaBulkActions";
import { AlbumManager } from "@/components/media/AlbumManager";
import { TrashManager } from "@/components/media/TrashManager";

// ============================================
// Helpers
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
// Tab Types
// ============================================

type ManagerTab = "files" | "albums" | "trash";

// ============================================
// Main Component
// ============================================

export default function MediaManagerV3() {
  // Tab state
  const [activeTab, setActiveTab] = useState<ManagerTab>("files");

  // List params
  const [params, setParams] = useState<MediaListParams>({
    page: 1,
    limit: 24,
    sort_by: "created_at",
    sort_order: "desc",
  });

  // View / selection
  const [viewMode, setViewMode] = useState<"grid" | "list">("grid");
  const [selectedFiles, setSelectedFiles] = useState<Set<string>>(new Set());
  const [detailMediaId, setDetailMediaId] = useState<string | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [showDropzone, setShowDropzone] = useState(false);
  const [urlDialogOpen, setUrlDialogOpen] = useState(false);
  const [importUrl, setImportUrl] = useState("");

  // Pre-upload image editor (crop/resize a single image before uploading)
  const [editorFile, setEditorFile] = useState<File | null>(null);

  // Folder
  const [selectedFolderId, setSelectedFolderId] = useState<string | null>(null);

  // Data fetching
  const queryParams = useMemo<MediaListParams>(
    () => ({
      ...params,
      folder_id: selectedFolderId || undefined,
    }),
    [params, selectedFolderId]
  );

  const { data: mediaData, isLoading, refetch } = useMediaListV3(queryParams);
  const uploadV3 = useUploadV3();
  const uploadMultipleV3 = useUploadMultipleV3();
  const uploadFromUrl = useUploadFromURL();
  const softDelete = useSoftDeleteMedia();
  const toggleFav = useToggleFavorite();

  const files = mediaData?.data ?? [];
  const meta = mediaData?.meta;
  const isUploading = uploadV3.isPending || uploadMultipleV3.isPending;

  // ============================================
  // Handlers
  // ============================================

  const doUpload = useCallback(
    (uploadFiles: File[]) => {
      if (uploadFiles.length === 1) {
        uploadV3.mutate({ file: uploadFiles[0], folder: selectedFolderId || undefined });
      } else {
        uploadMultipleV3.mutate({
          files: uploadFiles,
          folder: selectedFolderId || undefined,
        });
      }
    },
    [uploadV3, uploadMultipleV3, selectedFolderId]
  );

  const handleUpload = useCallback(
    (uploadFiles: File[]) => {
      // A single editable image opens the crop/resize editor before uploading.
      if (uploadFiles.length === 1 && isEditableImage(uploadFiles[0])) {
        setEditorFile(uploadFiles[0]);
        return;
      }
      doUpload(uploadFiles);
    },
    [doUpload]
  );

  const handleEditorConfirm = useCallback(
    (file: File) => {
      setEditorFile(null);
      doUpload([file]);
    },
    [doUpload]
  );

  const handleImportUrl = () => {
    if (!importUrl.trim()) return;
    uploadFromUrl.mutate(
      { url: importUrl.trim() },
      {
        onSuccess: () => {
          setUrlDialogOpen(false);
          setImportUrl("");
        },
      }
    );
  };

  const handleSelectFolder = useCallback((folderId: string | null) => {
    setSelectedFolderId(folderId);
    setParams((prev) => ({ ...prev, page: 1 }));
    setSelectedFiles(new Set());
  }, []);

  const toggleSelectFile = (id: string) => {
    setSelectedFiles((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
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

  const openDetail = (file: MediaFileV3) => {
    setDetailMediaId(file.id);
    setDetailOpen(true);
  };

  const handleParamsChange = useCallback((newParams: MediaListParams) => {
    setParams((prev) => ({ ...prev, ...newParams }));
    setSelectedFiles(new Set());
  }, []);

  // ============================================
  // Render
  // ============================================

  return (
    <div className="space-y-5">
      {/* Stats Cards */}
      <MediaStatsCards />

      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Media Manager</h1>
          <p className="text-muted-foreground text-sm">Quản lý tất cả file media của bạn</p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={() => setUrlDialogOpen(true)}>
            <Link className="mr-1.5 h-3.5 w-3.5" />
            Import URL
          </Button>
          <Button size="sm" onClick={() => setShowDropzone(!showDropzone)} disabled={isUploading}>
            {isUploading ? (
              <Loader2 className="mr-1.5 h-3.5 w-3.5 animate-spin" />
            ) : (
              <Upload className="mr-1.5 h-3.5 w-3.5" />
            )}
            Upload
          </Button>
        </div>
      </div>

      {/* Upload Dropzone (toggleable) */}
      {showDropzone && <MediaDropzone onUpload={handleUpload} isUploading={isUploading} />}

      {/* Tabs */}
      <div className="flex items-center gap-1 border-b">
        {[
          { key: "files" as ManagerTab, label: "Files", icon: HardDrive },
          { key: "albums" as ManagerTab, label: "Albums", icon: Images },
          { key: "trash" as ManagerTab, label: "Thùng rác", icon: Trash2 },
        ].map(({ key, label, icon: Icon }) => (
          <button
            key={key}
            className={cn(
              "-mb-[1px] flex items-center gap-1.5 border-b-2 px-4 py-2.5 text-sm font-medium transition-colors",
              activeTab === key
                ? "border-primary text-primary"
                : "text-muted-foreground hover:text-foreground hover:border-muted-foreground/30 border-transparent"
            )}
            onClick={() => setActiveTab(key)}
          >
            <Icon className="h-4 w-4" />
            {label}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {activeTab === "files" && (
        <div className="flex gap-5">
          {/* Sidebar — Folder Tree */}
          <aside className="hidden w-56 shrink-0 lg:block">
            <div className="sticky top-4 overflow-hidden rounded-lg border">
              <FolderTree
                selectedFolderId={selectedFolderId}
                onSelectFolder={handleSelectFolder}
                className="max-h-[calc(100vh-280px)]"
              />
            </div>
          </aside>

          {/* Main content */}
          <div className="min-w-0 flex-1 space-y-4">
            {/* Search & Filters */}
            <MediaSearchBar params={params} onParamsChange={handleParamsChange} />

            {/* Bulk Actions */}
            <MediaBulkActions
              selectedIds={selectedFiles}
              onClearSelection={() => setSelectedFiles(new Set())}
              onComplete={() => refetch()}
            />

            {/* Toolbar */}
            <div className="flex items-center justify-between">
              <div className="text-muted-foreground text-sm">
                {meta ? `${meta.total} files` : isLoading ? "Đang tải..." : "0 files"}
              </div>
              <div className="flex items-center gap-2">
                {files.length > 0 && (
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-7 text-xs"
                    onClick={toggleSelectAll}
                  >
                    {selectedFiles.size === files.length && files.length > 0 ? (
                      <CheckSquare className="text-primary mr-1 h-3 w-3" />
                    ) : (
                      <Square className="mr-1 h-3 w-3" />
                    )}
                    Chọn tất cả
                  </Button>
                )}
                <div className="flex rounded-md border">
                  <Button
                    variant={viewMode === "grid" ? "secondary" : "ghost"}
                    size="icon"
                    className="h-7 w-7 rounded-r-none"
                    onClick={() => setViewMode("grid")}
                  >
                    <Grid className="h-3.5 w-3.5" />
                  </Button>
                  <Button
                    variant={viewMode === "list" ? "secondary" : "ghost"}
                    size="icon"
                    className="h-7 w-7 rounded-l-none"
                    onClick={() => setViewMode("list")}
                  >
                    <List className="h-3.5 w-3.5" />
                  </Button>
                </div>
              </div>
            </div>

            {/* File Grid / List */}
            {isLoading ? (
              <div className="flex items-center justify-center py-20">
                <Loader2 className="text-muted-foreground h-8 w-8 animate-spin" />
              </div>
            ) : files.length === 0 ? (
              <MediaDropzone onUpload={handleUpload} isUploading={isUploading} />
            ) : viewMode === "grid" ? (
              /* Grid View */
              <div
                className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6"
                onDrop={(e) => {
                  e.preventDefault();
                  const droppedFiles = Array.from(e.dataTransfer.files);
                  if (droppedFiles.length > 0) handleUpload(droppedFiles);
                }}
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
                      {/* Checkbox */}
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
                          <Square className="text-muted-foreground h-5 w-5 rounded bg-white/70" />
                        )}
                      </button>

                      {/* Favorite badge */}
                      {file.is_favorite && (
                        <div className="absolute top-2 right-2 z-10">
                          <Star className="h-4 w-4 fill-yellow-500 text-yellow-500" />
                        </div>
                      )}

                      {/* Actions (hidden behind favorite if present) */}
                      <div
                        className={cn(
                          "absolute z-10 opacity-0 transition-opacity group-hover:opacity-100",
                          file.is_favorite ? "top-8 right-2" : "top-2 right-2"
                        )}
                      >
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button
                              variant="secondary"
                              size="icon"
                              className="h-7 w-7"
                              onClick={(e) => e.stopPropagation()}
                            >
                              <MoreVertical className="h-3.5 w-3.5" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem onClick={() => openDetail(file)}>
                              <Eye className="mr-2 h-4 w-4" />
                              Chi tiết
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() =>
                                toggleFav.mutate({
                                  id: file.id,
                                  isFavorite: !file.is_favorite,
                                })
                              }
                            >
                              <Star className="mr-2 h-4 w-4" />
                              {file.is_favorite ? "Bỏ thích" : "Yêu thích"}
                            </DropdownMenuItem>
                            <DropdownMenuItem asChild>
                              <a
                                href={getImageUrl(file.url)}
                                download={file.original_name}
                                target="_blank"
                                rel="noreferrer"
                              >
                                <Download className="mr-2 h-4 w-4" />
                                Tải xuống
                              </a>
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() => softDelete.mutate(file.id)}
                              variant="destructive"
                            >
                              <Trash2 className="mr-2 h-4 w-4" />
                              Xóa
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </div>

                      {/* Thumbnail / Icon */}
                      <div
                        className="bg-muted/50 flex aspect-square items-center justify-center overflow-hidden"
                        style={{
                          backgroundColor:
                            isImage && file.dominant_color ? file.dominant_color + "20" : undefined,
                        }}
                        onClick={() => openDetail(file)}
                      >
                        {isImage ? (
                          // eslint-disable-next-line @next/next/no-img-element
                          <img
                            src={getImageUrl(file.url)}
                            alt={file.alt || file.original_name}
                            className="h-full w-full object-cover"
                          />
                        ) : (
                          <Icon className="text-muted-foreground/40 h-12 w-12" />
                        )}
                      </div>

                      {/* Info */}
                      <CardContent className="p-2.5">
                        <p className="truncate text-xs font-medium">{file.original_name}</p>
                        <div className="mt-0.5 flex items-center justify-between">
                          <p className="text-muted-foreground text-[11px]">
                            {formatFileSize(file.size)}
                          </p>
                          {file.dimensions && (
                            <p className="text-muted-foreground text-[10px]">
                              {file.dimensions.width}×{file.dimensions.height}
                            </p>
                          )}
                        </div>
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            ) : (
              /* List View */
              <div className="rounded-lg border">
                {/* Header */}
                <div className="bg-muted/50 text-muted-foreground flex items-center gap-4 border-b px-4 py-2 text-xs font-medium">
                  <button className="shrink-0" onClick={toggleSelectAll}>
                    {selectedFiles.size === files.length && files.length > 0 ? (
                      <CheckSquare className="text-primary h-4 w-4" />
                    ) : (
                      <Square className="h-4 w-4" />
                    )}
                  </button>
                  <span className="flex-1">Tên</span>
                  <span className="hidden w-16 text-right sm:block">Loại</span>
                  <span className="w-20 text-right">Kích thước</span>
                  <span className="hidden w-20 text-right md:block">Kích thước ảnh</span>
                  <span className="hidden w-28 text-right lg:block">Ngày tải</span>
                  <span className="w-8" />
                </div>

                {files.map((file) => {
                  const Icon = getFileIcon(file.mime_type);
                  const isSelected = selectedFiles.has(file.id);
                  const isImage = file.mime_type.startsWith("image/");

                  return (
                    <div
                      key={file.id}
                      className={cn(
                        "hover:bg-muted/30 flex cursor-pointer items-center gap-4 border-b px-4 py-2.5 transition-colors last:border-b-0",
                        isSelected && "bg-primary/5"
                      )}
                      onClick={() => openDetail(file)}
                    >
                      <button
                        className="shrink-0"
                        onClick={(e) => {
                          e.stopPropagation();
                          toggleSelectFile(file.id);
                        }}
                      >
                        {isSelected ? (
                          <CheckSquare className="text-primary h-4 w-4" />
                        ) : (
                          <Square className="text-muted-foreground h-4 w-4" />
                        )}
                      </button>

                      <div className="flex min-w-0 flex-1 items-center gap-3">
                        {isImage ? (
                          // eslint-disable-next-line @next/next/no-img-element
                          <img
                            src={getImageUrl(file.url)}
                            alt={file.alt || file.original_name}
                            className="h-8 w-8 shrink-0 rounded object-cover"
                          />
                        ) : (
                          <Icon className="text-muted-foreground h-5 w-5 shrink-0" />
                        )}
                        <div className="min-w-0">
                          <span className="block truncate text-sm">{file.original_name}</span>
                          {file.tags && file.tags.length > 0 && (
                            <div className="mt-0.5 flex gap-1">
                              {file.tags.slice(0, 2).map((t) => (
                                <Badge key={t} variant="outline" className="px-1 py-0 text-[9px]">
                                  {t}
                                </Badge>
                              ))}
                            </div>
                          )}
                        </div>
                        {file.is_favorite && (
                          <Star className="h-3.5 w-3.5 shrink-0 fill-yellow-500 text-yellow-500" />
                        )}
                      </div>

                      <span className="text-muted-foreground hidden w-16 text-right text-xs uppercase sm:block">
                        {file.extension}
                      </span>
                      <span className="text-muted-foreground w-20 text-right text-xs">
                        {formatFileSize(file.size)}
                      </span>
                      <span className="text-muted-foreground hidden w-20 text-right text-xs md:block">
                        {file.dimensions
                          ? `${file.dimensions.width}×${file.dimensions.height}`
                          : "—"}
                      </span>
                      <span className="text-muted-foreground hidden w-28 text-right text-xs lg:block">
                        {formatDistanceToNow(new Date(file.created_at), {
                          addSuffix: true,
                        })}
                      </span>

                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-8 w-8"
                            onClick={(e) => e.stopPropagation()}
                          >
                            <MoreVertical className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onClick={() => openDetail(file)}>
                            <Eye className="mr-2 h-4 w-4" />
                            Chi tiết
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            onClick={() =>
                              toggleFav.mutate({
                                id: file.id,
                                isFavorite: !file.is_favorite,
                              })
                            }
                          >
                            <Star className="mr-2 h-4 w-4" />
                            {file.is_favorite ? "Bỏ thích" : "Yêu thích"}
                          </DropdownMenuItem>
                          <DropdownMenuItem asChild>
                            <a
                              href={getImageUrl(file.url)}
                              download={file.original_name}
                              target="_blank"
                              rel="noreferrer"
                            >
                              <Download className="mr-2 h-4 w-4" />
                              Tải xuống
                            </a>
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            onClick={() => softDelete.mutate(file.id)}
                            variant="destructive"
                          >
                            <Trash2 className="mr-2 h-4 w-4" />
                            Xóa
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
                page={params.page || 1}
                limit={params.limit || 24}
                total={meta.total}
                totalPages={meta.total_pages}
                onPageChange={(p) => setParams((prev) => ({ ...prev, page: p }))}
                onLimitChange={(l) => setParams((prev) => ({ ...prev, limit: l, page: 1 }))}
              />
            )}
          </div>
        </div>
      )}

      {activeTab === "albums" && (
        <AlbumManager
          onViewAlbum={(album) => {
            // Could navigate to album detail page in future
          }}
        />
      )}

      {activeTab === "trash" && <TrashManager />}

      {/* Detail Panel */}
      <MediaDetailPanel
        mediaId={detailMediaId}
        open={detailOpen}
        onClose={() => {
          setDetailOpen(false);
          setDetailMediaId(null);
        }}
        onDeleted={() => refetch()}
      />

      {/* Import URL Dialog */}
      <Dialog open={urlDialogOpen} onOpenChange={setUrlDialogOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Import từ URL</DialogTitle>
          </DialogHeader>
          <div>
            <Label htmlFor="import-url">URL ảnh/file</Label>
            <Input
              id="import-url"
              value={importUrl}
              onChange={(e) => setImportUrl(e.target.value)}
              placeholder="https://example.com/image.jpg"
              onKeyDown={(e) => e.key === "Enter" && handleImportUrl()}
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setUrlDialogOpen(false)}>
              Hủy
            </Button>
            <Button
              onClick={handleImportUrl}
              disabled={!importUrl.trim() || uploadFromUrl.isPending}
            >
              {uploadFromUrl.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Import
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Pre-upload image editor (crop / resize a single image) */}
      <UploadImageEditor
        open={editorFile !== null}
        file={editorFile}
        onCancel={() => setEditorFile(null)}
        onConfirm={handleEditorConfirm}
      />
    </div>
  );
}
