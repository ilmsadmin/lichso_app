"use client";

import { useState, useCallback } from "react";
import {
  FolderOpen,
  FolderPlus,
  ChevronRight,
  ChevronDown,
  Pencil,
  Trash2,
  MoreVertical,
  Loader2,
  FolderIcon,
  Home,
} from "lucide-react";
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
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { cn } from "@/lib/utils";
import {
  useFolderTree,
  useCreateFolder,
  useUpdateFolder,
  useDeleteFolder,
} from "@/hooks/useMediaV3";
import type { MediaFolder } from "@/types/media";

// ============================================
// Types
// ============================================

interface FolderTreeProps {
  selectedFolderId: string | null;
  onSelectFolder: (folderId: string | null) => void;
  className?: string;
}

interface FolderNodeProps {
  folder: MediaFolder;
  level: number;
  selectedFolderId: string | null;
  onSelectFolder: (folderId: string | null) => void;
  onEdit: (folder: MediaFolder) => void;
  onDelete: (folder: MediaFolder) => void;
  onCreateChild: (parentId: string) => void;
}

// ============================================
// Folder Node Component
// ============================================

function FolderNode({
  folder,
  level,
  selectedFolderId,
  onSelectFolder,
  onEdit,
  onDelete,
  onCreateChild,
}: FolderNodeProps) {
  const [expanded, setExpanded] = useState(true);
  const hasChildren = folder.children && folder.children.length > 0;
  const isSelected = selectedFolderId === folder.id;

  return (
    <div>
      <div
        className={cn(
          "group flex cursor-pointer items-center gap-1 rounded-md px-2 py-1.5 text-sm transition-colors",
          isSelected
            ? "bg-primary/10 text-primary font-medium"
            : "hover:bg-muted/50 text-muted-foreground hover:text-foreground"
        )}
        style={{ paddingLeft: `${level * 16 + 8}px` }}
      >
        {/* Expand/Collapse */}
        <button
          className="hover:bg-muted shrink-0 rounded p-0.5"
          onClick={(e) => {
            e.stopPropagation();
            setExpanded(!expanded);
          }}
        >
          {hasChildren ? (
            expanded ? (
              <ChevronDown className="h-3.5 w-3.5" />
            ) : (
              <ChevronRight className="h-3.5 w-3.5" />
            )
          ) : (
            <span className="w-3.5" />
          )}
        </button>

        {/* Folder Icon + Name */}
        <div
          className="flex min-w-0 flex-1 items-center gap-2"
          onClick={() => onSelectFolder(folder.id)}
        >
          <FolderOpen className="h-4 w-4 shrink-0" style={{ color: folder.color || undefined }} />
          <span className="truncate">{folder.name}</span>
          {folder.media_count > 0 && (
            <span className="text-muted-foreground ml-auto text-[11px]">{folder.media_count}</span>
          )}
        </div>

        {/* Actions */}
        {!folder.is_system && (
          <div className="shrink-0 opacity-0 transition-opacity group-hover:opacity-100">
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-6 w-6"
                  onClick={(e) => e.stopPropagation()}
                >
                  <MoreVertical className="h-3.5 w-3.5" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={() => onCreateChild(folder.id)}>
                  <FolderPlus className="mr-2 h-4 w-4" />
                  Thư mục con
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => onEdit(folder)}>
                  <Pencil className="mr-2 h-4 w-4" />
                  Sửa tên
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => onDelete(folder)} variant="destructive">
                  <Trash2 className="mr-2 h-4 w-4" />
                  Xóa
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        )}
      </div>

      {/* Children */}
      {expanded && hasChildren && (
        <div>
          {folder.children!.map((child) => (
            <FolderNode
              key={child.id}
              folder={child}
              level={level + 1}
              selectedFolderId={selectedFolderId}
              onSelectFolder={onSelectFolder}
              onEdit={onEdit}
              onDelete={onDelete}
              onCreateChild={onCreateChild}
            />
          ))}
        </div>
      )}
    </div>
  );
}

// ============================================
// Main FolderTree Component
// ============================================

export function FolderTree({ selectedFolderId, onSelectFolder, className }: FolderTreeProps) {
  const { data: treeData, isLoading } = useFolderTree();
  const createFolder = useCreateFolder();
  const updateFolder = useUpdateFolder();
  const deleteFolder = useDeleteFolder();

  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [parentIdForCreate, setParentIdForCreate] = useState<string | undefined>();
  const [editTarget, setEditTarget] = useState<MediaFolder | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<MediaFolder | null>(null);

  const [folderName, setFolderName] = useState("");
  const [folderIcon, setFolderIcon] = useState("");
  const [folderColor, setFolderColor] = useState("");

  const folders = treeData?.data ?? [];

  const handleOpenCreate = useCallback((parentId?: string) => {
    setParentIdForCreate(parentId);
    setFolderName("");
    setFolderIcon("");
    setFolderColor("");
    setCreateDialogOpen(true);
  }, []);

  const handleCreate = () => {
    if (!folderName.trim()) return;
    createFolder.mutate(
      {
        name: folderName.trim(),
        parent_id: parentIdForCreate,
        icon: folderIcon || undefined,
        color: folderColor || undefined,
      },
      { onSuccess: () => setCreateDialogOpen(false) }
    );
  };

  const handleOpenEdit = useCallback((folder: MediaFolder) => {
    setEditTarget(folder);
    setFolderName(folder.name);
    setFolderIcon(folder.icon || "");
    setFolderColor(folder.color || "");
    setEditDialogOpen(true);
  }, []);

  const handleEdit = () => {
    if (!editTarget || !folderName.trim()) return;
    updateFolder.mutate(
      {
        id: editTarget.id,
        data: {
          name: folderName.trim(),
          icon: folderIcon || undefined,
          color: folderColor || undefined,
        },
      },
      { onSuccess: () => setEditDialogOpen(false) }
    );
  };

  const handleOpenDelete = useCallback((folder: MediaFolder) => {
    setDeleteTarget(folder);
    setDeleteDialogOpen(true);
  }, []);

  const handleDelete = () => {
    if (!deleteTarget) return;
    deleteFolder.mutate(deleteTarget.id, {
      onSuccess: () => {
        setDeleteDialogOpen(false);
        if (selectedFolderId === deleteTarget.id) {
          onSelectFolder(null);
        }
      },
    });
  };

  const FOLDER_COLORS = [
    "#6366f1",
    "#8b5cf6",
    "#ec4899",
    "#f43f5e",
    "#f97316",
    "#eab308",
    "#22c55e",
    "#06b6d4",
    "#3b82f6",
    "#64748b",
  ];

  return (
    <div className={cn("flex flex-col", className)}>
      {/* Header */}
      <div className="flex items-center justify-between border-b px-3 py-2">
        <span className="text-muted-foreground text-xs font-semibold tracking-wider uppercase">
          Thư mục
        </span>
        <Button
          variant="ghost"
          size="icon"
          className="h-6 w-6"
          onClick={() => handleOpenCreate()}
          title="Tạo thư mục"
        >
          <FolderPlus className="h-4 w-4" />
        </Button>
      </div>

      {/* All Files (root) */}
      <div
        className={cn(
          "flex cursor-pointer items-center gap-2 px-3 py-2 text-sm transition-colors",
          selectedFolderId === null
            ? "bg-primary/10 text-primary font-medium"
            : "hover:bg-muted/50 text-muted-foreground hover:text-foreground"
        )}
        onClick={() => onSelectFolder(null)}
      >
        <Home className="h-4 w-4 shrink-0" />
        <span>Tất cả files</span>
      </div>

      {/* Tree */}
      <div className="flex-1 overflow-y-auto py-1">
        {isLoading ? (
          <div className="flex items-center justify-center py-6">
            <Loader2 className="text-muted-foreground h-5 w-5 animate-spin" />
          </div>
        ) : folders.length === 0 ? (
          <div className="text-muted-foreground py-6 text-center text-sm">
            <FolderIcon className="mx-auto mb-2 h-8 w-8 opacity-40" />
            <p>Chưa có thư mục nào</p>
          </div>
        ) : (
          folders.map((folder) => (
            <FolderNode
              key={folder.id}
              folder={folder}
              level={0}
              selectedFolderId={selectedFolderId}
              onSelectFolder={onSelectFolder}
              onEdit={handleOpenEdit}
              onDelete={handleOpenDelete}
              onCreateChild={(parentId) => handleOpenCreate(parentId)}
            />
          ))
        )}
      </div>

      {/* Create Folder Dialog */}
      <Dialog open={createDialogOpen} onOpenChange={setCreateDialogOpen}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Tạo thư mục mới</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="folder-name">Tên thư mục</Label>
              <Input
                id="folder-name"
                value={folderName}
                onChange={(e) => setFolderName(e.target.value)}
                placeholder="Nhập tên thư mục..."
                onKeyDown={(e) => e.key === "Enter" && handleCreate()}
              />
            </div>
            <div>
              <Label>Màu sắc</Label>
              <div className="mt-1.5 flex gap-2">
                {FOLDER_COLORS.map((c) => (
                  <button
                    key={c}
                    className={cn(
                      "h-6 w-6 rounded-full transition-all",
                      folderColor === c ? "ring-primary ring-2 ring-offset-2" : "hover:scale-110"
                    )}
                    style={{ backgroundColor: c }}
                    onClick={() => setFolderColor(folderColor === c ? "" : c)}
                  />
                ))}
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCreateDialogOpen(false)}>
              Hủy
            </Button>
            <Button onClick={handleCreate} disabled={!folderName.trim() || createFolder.isPending}>
              {createFolder.isPending ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
              Tạo
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Folder Dialog */}
      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Sửa thư mục</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="edit-folder-name">Tên thư mục</Label>
              <Input
                id="edit-folder-name"
                value={folderName}
                onChange={(e) => setFolderName(e.target.value)}
                placeholder="Nhập tên thư mục..."
                onKeyDown={(e) => e.key === "Enter" && handleEdit()}
              />
            </div>
            <div>
              <Label>Màu sắc</Label>
              <div className="mt-1.5 flex gap-2">
                {FOLDER_COLORS.map((c) => (
                  <button
                    key={c}
                    className={cn(
                      "h-6 w-6 rounded-full transition-all",
                      folderColor === c ? "ring-primary ring-2 ring-offset-2" : "hover:scale-110"
                    )}
                    style={{ backgroundColor: c }}
                    onClick={() => setFolderColor(folderColor === c ? "" : c)}
                  />
                ))}
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditDialogOpen(false)}>
              Hủy
            </Button>
            <Button onClick={handleEdit} disabled={!folderName.trim() || updateFolder.isPending}>
              {updateFolder.isPending ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
              Lưu
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Folder Confirm */}
      <ConfirmDialog
        open={deleteDialogOpen}
        onOpenChange={setDeleteDialogOpen}
        title="Xóa thư mục"
        description={`Bạn có chắc muốn xóa thư mục "${deleteTarget?.name}"? Các file trong thư mục sẽ được chuyển về gốc.`}
        confirmText="Xóa"
        cancelText="Hủy"
        variant="destructive"
        onConfirm={handleDelete}
        loading={deleteFolder.isPending}
      />
    </div>
  );
}
