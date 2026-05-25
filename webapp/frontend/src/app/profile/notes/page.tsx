"use client";

import { useState } from "react";
import { StickyNote, Plus, Pencil, Trash2, Pin, PinOff, Calendar } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Skeleton } from "@/components/ui/skeleton";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Pagination } from "@/components/shared/Pagination";
import { useUserNotes, useCreateNote, useUpdateNote, useDeleteNote } from "@/hooks/useV3";
import type { UserNote, CreateUserNoteRequest, UpdateUserNoteRequest } from "@/types/v3";

// ============================================
// Note Colors
// ============================================

const NOTE_COLORS = [
  { value: "#FEF3C7", label: "Vàng", tw: "bg-amber-100" },
  { value: "#DBEAFE", label: "Xanh dương", tw: "bg-blue-100" },
  { value: "#DCFCE7", label: "Xanh lá", tw: "bg-green-100" },
  { value: "#FCE7F3", label: "Hồng", tw: "bg-pink-100" },
  { value: "#F3E8FF", label: "Tím", tw: "bg-purple-100" },
  { value: "#FFF7ED", label: "Cam", tw: "bg-orange-50" },
];

// ============================================
// Note Form Dialog
// ============================================

function NoteFormDialog({
  open,
  onOpenChange,
  editingNote,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  editingNote?: UserNote | null;
}) {
  const isEditing = !!editingNote;
  const createNote = useCreateNote();
  const updateNote = useUpdateNote();

  const [title, setTitle] = useState(editingNote?.title ?? "");
  const [content, setContent] = useState(editingNote?.content ?? "");
  const [noteDate, setNoteDate] = useState(
    editingNote?.note_date?.split("T")[0] ?? new Date().toISOString().split("T")[0]
  );
  const [color, setColor] = useState(editingNote?.color ?? NOTE_COLORS[0].value);
  const [isPinned, setIsPinned] = useState(editingNote?.is_pinned ?? false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;

    if (isEditing && editingNote) {
      const data: UpdateUserNoteRequest = {
        title: title.trim(),
        content: content.trim() || undefined,
        note_date: noteDate,
        color,
        is_pinned: isPinned,
      };
      updateNote.mutate({ id: editingNote.id, data }, { onSuccess: () => onOpenChange(false) });
    } else {
      const data: CreateUserNoteRequest = {
        title: title.trim(),
        content: content.trim() || undefined,
        note_date: noteDate,
        color,
        is_pinned: isPinned,
      };
      createNote.mutate(data, {
        onSuccess: () => onOpenChange(false),
      });
    }
  };

  const isPending = createNote.isPending || updateNote.isPending;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{isEditing ? "Sửa ghi chú" : "Tạo ghi chú mới"}</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="note-title">Tiêu đề *</Label>
            <Input
              id="note-title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Nhập tiêu đề ghi chú..."
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="note-content">Nội dung</Label>
            <Textarea
              id="note-content"
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Ghi chú chi tiết..."
              rows={4}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="note-date">Ngày</Label>
            <Input
              id="note-date"
              type="date"
              value={noteDate}
              onChange={(e) => setNoteDate(e.target.value)}
            />
          </div>

          <div className="space-y-2">
            <Label>Màu sắc</Label>
            <div className="flex gap-2">
              {NOTE_COLORS.map((c) => (
                <button
                  key={c.value}
                  type="button"
                  className={`h-8 w-8 rounded-full border-2 transition-all ${
                    color === c.value
                      ? "border-primary scale-110 shadow-md"
                      : "hover:border-muted-foreground/30 border-transparent"
                  }`}
                  style={{ backgroundColor: c.value }}
                  onClick={() => setColor(c.value)}
                  title={c.label}
                />
              ))}
            </div>
          </div>

          <div className="flex items-center gap-2">
            <Button
              type="button"
              variant={isPinned ? "default" : "outline"}
              size="sm"
              onClick={() => setIsPinned(!isPinned)}
            >
              {isPinned ? <Pin className="mr-1 h-4 w-4" /> : <PinOff className="mr-1 h-4 w-4" />}
              {isPinned ? "Đã ghim" : "Ghim"}
            </Button>
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isPending}
            >
              Hủy
            </Button>
            <Button type="submit" disabled={isPending || !title.trim()}>
              {isPending ? "Đang xử lý..." : isEditing ? "Cập nhật" : "Tạo"}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}

// ============================================
// Notes Page
// ============================================

export default function NotesPage() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(20);
  const [formOpen, setFormOpen] = useState(false);
  const [editingNote, setEditingNote] = useState<UserNote | null>(null);
  const [deleteId, setDeleteId] = useState<string | null>(null);

  const { data, isLoading } = useUserNotes({ page, limit });
  const deleteNote = useDeleteNote();

  const notes = data?.data ?? [];
  const meta = data?.meta;

  const pinnedNotes = notes.filter((n) => n.is_pinned);
  const otherNotes = notes.filter((n) => !n.is_pinned);

  const handleEdit = (note: UserNote) => {
    setEditingNote(note);
    setFormOpen(true);
  };

  const handleAdd = () => {
    setEditingNote(null);
    setFormOpen(true);
  };

  const handleDelete = () => {
    if (!deleteId) return;
    deleteNote.mutate(deleteId, {
      onSuccess: () => setDeleteId(null),
    });
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  };

  const renderNote = (note: UserNote) => (
    <div
      key={note.id}
      className="group relative rounded-xl border p-4 transition-all hover:shadow-md"
      style={{ backgroundColor: note.color + "40" }}
    >
      {note.is_pinned && <Pin className="absolute top-2 right-2 h-3.5 w-3.5 text-amber-500" />}
      <h3 className="line-clamp-2 pr-6 text-sm font-medium">{note.title}</h3>
      {note.content && (
        <p className="text-muted-foreground mt-1.5 line-clamp-3 text-xs">{note.content}</p>
      )}
      <div className="mt-3 flex items-center justify-between">
        <div className="text-muted-foreground flex items-center gap-1.5 text-[11px]">
          <Calendar className="h-3 w-3" />
          {formatDate(note.note_date)}
        </div>
        <div className="flex items-center gap-1 opacity-0 transition-opacity group-hover:opacity-100">
          <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => handleEdit(note)}>
            <Pencil className="h-3.5 w-3.5" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            className="text-destructive/60 hover:text-destructive h-7 w-7"
            onClick={() => setDeleteId(note.id)}
          >
            <Trash2 className="h-3.5 w-3.5" />
          </Button>
        </div>
      </div>
    </div>
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="flex items-center gap-2 text-3xl font-bold tracking-tight">
            <StickyNote className="h-8 w-8" />
            Ghi chú
          </h1>
          <p className="text-muted-foreground mt-1">Ghi chú cá nhân cho các ngày trong lịch</p>
        </div>
        <Button onClick={handleAdd}>
          <Plus className="mr-2 h-4 w-4" />
          Thêm ghi chú
        </Button>
      </div>

      {/* Loading */}
      {isLoading ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className="h-32 rounded-xl" />
          ))}
        </div>
      ) : notes.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <StickyNote className="text-muted-foreground mb-4 h-12 w-12 opacity-50" />
            <p className="text-lg font-medium">Chưa có ghi chú</p>
            <p className="text-muted-foreground mt-1 text-sm">
              Tạo ghi chú để nhớ những điều quan trọng
            </p>
            <Button onClick={handleAdd} className="mt-4" variant="outline">
              <Plus className="mr-2 h-4 w-4" />
              Tạo ghi chú đầu tiên
            </Button>
          </CardContent>
        </Card>
      ) : (
        <>
          {/* Pinned notes */}
          {pinnedNotes.length > 0 && (
            <div>
              <h2 className="text-muted-foreground mb-3 flex items-center gap-1.5 text-sm font-medium">
                <Pin className="h-3.5 w-3.5" />
                Đã ghim ({pinnedNotes.length})
              </h2>
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {pinnedNotes.map(renderNote)}
              </div>
            </div>
          )}

          {/* Other notes */}
          {otherNotes.length > 0 && (
            <div>
              {pinnedNotes.length > 0 && (
                <h2 className="text-muted-foreground mb-3 text-sm font-medium">
                  Ghi chú khác ({otherNotes.length})
                </h2>
              )}
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {otherNotes.map(renderNote)}
              </div>
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
              onLimitChange={setLimit}
            />
          )}
        </>
      )}

      {/* Form Dialog */}
      {formOpen && (
        <NoteFormDialog open={formOpen} onOpenChange={setFormOpen} editingNote={editingNote} />
      )}

      {/* Delete confirmation */}
      <ConfirmDialog
        open={!!deleteId}
        onOpenChange={(open) => !open && setDeleteId(null)}
        title="Xóa ghi chú"
        description="Bạn có chắc muốn xóa ghi chú này? Hành động này không thể hoàn tác."
        confirmText="Xóa"
        variant="destructive"
        onConfirm={handleDelete}
        loading={deleteNote.isPending}
      />
    </div>
  );
}
