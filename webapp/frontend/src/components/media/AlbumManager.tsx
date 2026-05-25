"use client";

import { useState, useCallback } from "react";
import {
  Images,
  Plus,
  Pencil,
  Trash2,
  Eye,
  MoreVertical,
  Loader2,
  Globe,
  Lock,
  EyeOff,
  GripVertical,
} from "lucide-react";
import { formatDistanceToNow } from "date-fns";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { cn } from "@/lib/utils";
import { useAlbums, useCreateAlbum, useUpdateAlbum, useDeleteAlbum } from "@/hooks/useMediaV3";
import type { MediaAlbum } from "@/types/media";

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

const visibilityIcons = {
  public: Globe,
  private: Lock,
  unlisted: EyeOff,
};

const visibilityLabels = {
  public: "Công khai",
  private: "Riêng tư",
  unlisted: "Không liệt kê",
};

// ============================================
// Types
// ============================================

interface AlbumManagerProps {
  onViewAlbum?: (album: MediaAlbum) => void;
  className?: string;
}

// ============================================
// AlbumManager Component
// ============================================

export function AlbumManager({ onViewAlbum, className }: AlbumManagerProps) {
  const [page, setPage] = useState(1);
  const { data: albumsData, isLoading } = useAlbums(page);
  const createAlbum = useCreateAlbum();
  const updateAlbum = useUpdateAlbum();
  const deleteAlbum = useDeleteAlbum();

  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<MediaAlbum | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<MediaAlbum | null>(null);

  // Form state
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [visibility, setVisibility] = useState<"public" | "private" | "unlisted">("private");
  const [tags, setTags] = useState("");

  const albums = albumsData?.data ?? [];

  const resetForm = () => {
    setTitle("");
    setDescription("");
    setVisibility("private");
    setTags("");
  };

  const handleOpenCreate = () => {
    resetForm();
    setCreateDialogOpen(true);
  };

  const handleCreate = () => {
    if (!title.trim()) return;
    createAlbum.mutate(
      {
        title: title.trim(),
        description: description.trim() || undefined,
        visibility,
        tags: tags
          .split(",")
          .map((t) => t.trim())
          .filter(Boolean),
      },
      { onSuccess: () => setCreateDialogOpen(false) }
    );
  };

  const handleOpenEdit = useCallback((album: MediaAlbum) => {
    setEditTarget(album);
    setTitle(album.title);
    setDescription(album.description || "");
    setVisibility(album.visibility);
    setTags(album.tags?.join(", ") || "");
    setEditDialogOpen(true);
  }, []);

  const handleEdit = () => {
    if (!editTarget || !title.trim()) return;
    updateAlbum.mutate(
      {
        id: editTarget.id,
        data: {
          title: title.trim(),
          description: description.trim() || undefined,
          visibility,
          tags: tags
            .split(",")
            .map((t) => t.trim())
            .filter(Boolean),
        },
      },
      { onSuccess: () => setEditDialogOpen(false) }
    );
  };

  const handleOpenDelete = useCallback((album: MediaAlbum) => {
    setDeleteTarget(album);
    setDeleteDialogOpen(true);
  }, []);

  const handleDelete = () => {
    if (!deleteTarget) return;
    deleteAlbum.mutate(deleteTarget.id, {
      onSuccess: () => setDeleteDialogOpen(false),
    });
  };

  // ============================================
  // Album Form Dialog (shared for create/edit)
  // ============================================

  const AlbumFormDialog = ({
    open,
    onOpenChange,
    titleText,
    onSubmit,
    isPending,
    submitText,
  }: {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    titleText: string;
    onSubmit: () => void;
    isPending: boolean;
    submitText: string;
  }) => (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{titleText}</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div>
            <Label htmlFor="album-title">Tiêu đề</Label>
            <Input
              id="album-title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Nhập tiêu đề album..."
            />
          </div>
          <div>
            <Label htmlFor="album-desc">Mô tả</Label>
            <Textarea
              id="album-desc"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Mô tả album..."
              rows={3}
            />
          </div>
          <div>
            <Label>Hiển thị</Label>
            <Select
              value={visibility}
              onValueChange={(v) => setVisibility(v as "public" | "private" | "unlisted")}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="public">
                  <span className="flex items-center gap-2">
                    <Globe className="h-3.5 w-3.5" />
                    Công khai
                  </span>
                </SelectItem>
                <SelectItem value="private">
                  <span className="flex items-center gap-2">
                    <Lock className="h-3.5 w-3.5" />
                    Riêng tư
                  </span>
                </SelectItem>
                <SelectItem value="unlisted">
                  <span className="flex items-center gap-2">
                    <EyeOff className="h-3.5 w-3.5" />
                    Không liệt kê
                  </span>
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label htmlFor="album-tags">Tags (cách nhau bởi dấu phẩy)</Label>
            <Input
              id="album-tags"
              value={tags}
              onChange={(e) => setTags(e.target.value)}
              placeholder="tag1, tag2, tag3"
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Hủy
          </Button>
          <Button onClick={onSubmit} disabled={!title.trim() || isPending}>
            {isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            {submitText}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );

  return (
    <div className={cn("space-y-4", className)}>
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Images className="text-muted-foreground h-5 w-5" />
          <h3 className="font-semibold">Albums</h3>
          <Badge variant="secondary" className="text-xs">
            {albumsData?.meta?.total ?? 0}
          </Badge>
        </div>
        <Button size="sm" onClick={handleOpenCreate}>
          <Plus className="mr-1 h-3.5 w-3.5" />
          Tạo album
        </Button>
      </div>

      {/* Album Grid */}
      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="text-muted-foreground h-6 w-6 animate-spin" />
        </div>
      ) : albums.length === 0 ? (
        <div className="bg-muted/20 rounded-lg border py-12 text-center">
          <Images className="text-muted-foreground/40 mx-auto mb-3 h-10 w-10" />
          <p className="text-muted-foreground text-sm">Chưa có album nào</p>
          <Button variant="outline" size="sm" className="mt-3" onClick={handleOpenCreate}>
            <Plus className="mr-1 h-3.5 w-3.5" />
            Tạo album đầu tiên
          </Button>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {albums.map((album) => {
            const VisIcon = visibilityIcons[album.visibility];
            return (
              <Card
                key={album.id}
                className="group cursor-pointer overflow-hidden transition-shadow hover:shadow-md"
                onClick={() => onViewAlbum?.(album)}
              >
                {/* Album cover placeholder */}
                <div className="from-muted/50 to-muted relative flex h-32 items-center justify-center bg-gradient-to-br">
                  <Images className="text-muted-foreground/30 h-10 w-10" />

                  {/* Actions overlay */}
                  <div className="absolute top-2 right-2 opacity-0 transition-opacity group-hover:opacity-100">
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
                        <DropdownMenuItem
                          onClick={(e) => {
                            e.stopPropagation();
                            onViewAlbum?.(album);
                          }}
                        >
                          <Eye className="mr-2 h-4 w-4" />
                          Xem album
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          onClick={(e) => {
                            e.stopPropagation();
                            handleOpenEdit(album);
                          }}
                        >
                          <Pencil className="mr-2 h-4 w-4" />
                          Chỉnh sửa
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          onClick={(e) => {
                            e.stopPropagation();
                            handleOpenDelete(album);
                          }}
                          variant="destructive"
                        >
                          <Trash2 className="mr-2 h-4 w-4" />
                          Xóa
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>

                  {/* Visibility badge */}
                  <div className="absolute top-2 left-2">
                    <Badge variant="secondary" className="gap-1 text-[10px]">
                      <VisIcon className="h-3 w-3" />
                      {visibilityLabels[album.visibility]}
                    </Badge>
                  </div>
                </div>

                <CardContent className="p-3">
                  <h4 className="truncate text-sm font-medium">{album.title}</h4>
                  {album.description && (
                    <p className="text-muted-foreground mt-0.5 truncate text-xs">
                      {album.description}
                    </p>
                  )}
                  <div className="text-muted-foreground mt-2 flex items-center justify-between text-[11px]">
                    <span>{album.media_count} files</span>
                    <span>{formatFileSize(album.total_size)}</span>
                  </div>
                  {album.tags && album.tags.length > 0 && (
                    <div className="mt-2 flex flex-wrap gap-1">
                      {album.tags.slice(0, 3).map((tag) => (
                        <Badge key={tag} variant="outline" className="px-1.5 py-0 text-[10px]">
                          {tag}
                        </Badge>
                      ))}
                      {album.tags.length > 3 && (
                        <span className="text-muted-foreground text-[10px]">
                          +{album.tags.length - 3}
                        </span>
                      )}
                    </div>
                  )}
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}

      {/* Create Dialog */}
      <AlbumFormDialog
        open={createDialogOpen}
        onOpenChange={setCreateDialogOpen}
        titleText="Tạo album mới"
        onSubmit={handleCreate}
        isPending={createAlbum.isPending}
        submitText="Tạo"
      />

      {/* Edit Dialog */}
      <AlbumFormDialog
        open={editDialogOpen}
        onOpenChange={setEditDialogOpen}
        titleText="Chỉnh sửa album"
        onSubmit={handleEdit}
        isPending={updateAlbum.isPending}
        submitText="Lưu"
      />

      {/* Delete Confirm */}
      <ConfirmDialog
        open={deleteDialogOpen}
        onOpenChange={setDeleteDialogOpen}
        title="Xóa album"
        description={`Bạn có chắc muốn xóa album "${deleteTarget?.title}"? Các file trong album sẽ không bị xóa.`}
        confirmText="Xóa"
        cancelText="Hủy"
        variant="destructive"
        onConfirm={handleDelete}
        loading={deleteAlbum.isPending}
      />
    </div>
  );
}
