"use client";

import { useState } from "react";
import { Trash2, FolderInput, Star, Tag, Images, X, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
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
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import {
  useSoftDeleteMedia,
  useMoveMediaToFolder,
  useToggleFavorite,
  useAddMediaToAlbum,
  useFolderTree,
  useAlbums,
} from "@/hooks/useMediaV3";
import type { MediaFolder } from "@/types/media";

interface MediaBulkActionsProps {
  selectedIds: Set<string>;
  onClearSelection: () => void;
  onComplete?: () => void;
}

// Flatten folder tree for selector
function flattenFolders(
  folders: MediaFolder[],
  level = 0
): { id: string; name: string; level: number }[] {
  const result: { id: string; name: string; level: number }[] = [];
  for (const f of folders) {
    result.push({ id: f.id, name: f.name, level });
    if (f.children) {
      result.push(...flattenFolders(f.children, level + 1));
    }
  }
  return result;
}

export function MediaBulkActions({
  selectedIds,
  onClearSelection,
  onComplete,
}: MediaBulkActionsProps) {
  const count = selectedIds.size;

  const [deleteOpen, setDeleteOpen] = useState(false);
  const [moveOpen, setMoveOpen] = useState(false);
  const [albumOpen, setAlbumOpen] = useState(false);
  const [moveFolderId, setMoveFolderId] = useState("");
  const [albumId, setAlbumId] = useState("");

  const softDelete = useSoftDeleteMedia();
  const moveMedia = useMoveMediaToFolder();
  const toggleFav = useToggleFavorite();
  const addToAlbum = useAddMediaToAlbum();
  const { data: folderTreeData } = useFolderTree();
  const { data: albumsData } = useAlbums(1, 100);

  const folders = folderTreeData?.data ?? [];
  const flatFolders = flattenFolders(folders);
  const albums = albumsData?.data ?? [];
  const ids = Array.from(selectedIds);

  if (count === 0) return null;

  const handleBulkDelete = async () => {
    for (const id of ids) {
      await softDelete.mutateAsync(id);
    }
    setDeleteOpen(false);
    onClearSelection();
    onComplete?.();
  };

  const handleBulkMove = () => {
    if (!moveFolderId) return;
    moveMedia.mutate(
      { media_ids: ids, folder_id: moveFolderId },
      {
        onSuccess: () => {
          setMoveOpen(false);
          onClearSelection();
          onComplete?.();
        },
      }
    );
  };

  const handleBulkFavorite = async () => {
    for (const id of ids) {
      await toggleFav.mutateAsync({ id, isFavorite: true });
    }
    onClearSelection();
    onComplete?.();
  };

  const handleBulkAddToAlbum = () => {
    if (!albumId) return;
    addToAlbum.mutate(
      { albumId, data: { media_ids: ids } },
      {
        onSuccess: () => {
          setAlbumOpen(false);
          onClearSelection();
          onComplete?.();
        },
      }
    );
  };

  return (
    <>
      <div className="bg-muted/30 animate-in slide-in-from-bottom-2 flex items-center gap-2 rounded-lg border p-2">
        <Badge variant="secondary">{count} đã chọn</Badge>

        <Button
          variant="outline"
          size="sm"
          className="h-7 text-xs"
          onClick={() => setMoveOpen(true)}
        >
          <FolderInput className="mr-1.5 h-3 w-3" />
          Di chuyển
        </Button>

        <Button
          variant="outline"
          size="sm"
          className="h-7 text-xs"
          onClick={() => setAlbumOpen(true)}
        >
          <Images className="mr-1.5 h-3 w-3" />
          Thêm vào album
        </Button>

        <Button
          variant="outline"
          size="sm"
          className="h-7 text-xs"
          onClick={handleBulkFavorite}
          disabled={toggleFav.isPending}
        >
          <Star className="mr-1.5 h-3 w-3" />
          Yêu thích
        </Button>

        <Button
          variant="outline"
          size="sm"
          className="text-destructive hover:text-destructive h-7 text-xs"
          onClick={() => setDeleteOpen(true)}
        >
          <Trash2 className="mr-1.5 h-3 w-3" />
          Xóa
        </Button>

        <Button variant="ghost" size="icon" className="ml-auto h-7 w-7" onClick={onClearSelection}>
          <X className="h-3.5 w-3.5" />
        </Button>
      </div>

      {/* Move to folder dialog */}
      <Dialog open={moveOpen} onOpenChange={setMoveOpen}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Di chuyển {count} file vào thư mục</DialogTitle>
          </DialogHeader>
          <Select value={moveFolderId} onValueChange={setMoveFolderId}>
            <SelectTrigger>
              <SelectValue placeholder="Chọn thư mục..." />
            </SelectTrigger>
            <SelectContent>
              {flatFolders.map((f) => (
                <SelectItem key={f.id} value={f.id}>
                  {"  ".repeat(f.level)}
                  {f.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <DialogFooter>
            <Button variant="outline" onClick={() => setMoveOpen(false)}>
              Hủy
            </Button>
            <Button onClick={handleBulkMove} disabled={!moveFolderId || moveMedia.isPending}>
              {moveMedia.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Di chuyển
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Add to album dialog */}
      <Dialog open={albumOpen} onOpenChange={setAlbumOpen}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Thêm {count} file vào album</DialogTitle>
          </DialogHeader>
          <Select value={albumId} onValueChange={setAlbumId}>
            <SelectTrigger>
              <SelectValue placeholder="Chọn album..." />
            </SelectTrigger>
            <SelectContent>
              {albums.map((a) => (
                <SelectItem key={a.id} value={a.id}>
                  {a.title}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <DialogFooter>
            <Button variant="outline" onClick={() => setAlbumOpen(false)}>
              Hủy
            </Button>
            <Button onClick={handleBulkAddToAlbum} disabled={!albumId || addToAlbum.isPending}>
              {addToAlbum.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Thêm
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete confirm */}
      <ConfirmDialog
        open={deleteOpen}
        onOpenChange={setDeleteOpen}
        title="Xóa file"
        description={`Bạn có chắc muốn chuyển ${count} file vào thùng rác?`}
        confirmText="Xóa"
        cancelText="Hủy"
        variant="destructive"
        onConfirm={handleBulkDelete}
        loading={softDelete.isPending}
      />
    </>
  );
}
