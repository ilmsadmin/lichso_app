"use client";

import { useState, useCallback } from "react";
import { Plus, Pencil, Trash2, MoreHorizontal, Hash, ExternalLink } from "lucide-react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { SearchInput } from "@/components/shared/SearchInput";
import { Pagination } from "@/components/shared/Pagination";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { PermissionGate } from "@/components/auth/PermissionGate";
import { useTags, useCreateTag, useUpdateTag, useDeleteTag } from "@/hooks/useArticles";
import { usePermission } from "@/hooks/usePermission";
import { DEFAULT_PAGE_SIZE } from "@/lib/constants";
import { formatDate } from "@/lib/utils";
import type { ArticleTag } from "@/types/article";

export default function TagsPage() {
  const { can } = usePermission();
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);
  const [search, setSearch] = useState("");

  const [editTag, setEditTag] = useState<ArticleTag | null>(null);
  const [deleteTag, setDeleteTag] = useState<ArticleTag | null>(null);
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [formName, setFormName] = useState("");
  const [formSlug, setFormSlug] = useState("");
  const [formDescription, setFormDescription] = useState("");

  const { data, isLoading } = useTags({ page, limit, search: search || undefined });
  const createTag = useCreateTag();
  const updateTag = useUpdateTag(editTag?.id ?? "");
  const deleteTagMut = useDeleteTag();

  const tags = data?.data ?? [];
  const meta = data?.meta;

  const handleSearch = useCallback((value: string) => {
    setSearch(value);
    setPage(1);
  }, []);

  const openCreate = () => {
    setFormName("");
    setFormSlug("");
    setFormDescription("");
    setShowCreateDialog(true);
  };

  const openEdit = (tag: ArticleTag) => {
    setFormName(tag.name);
    setFormSlug(tag.slug ?? "");
    setFormDescription(tag.description ?? "");
    setEditTag(tag);
  };

  const handleCreate = () => {
    createTag.mutate(
      { name: formName, slug: formSlug || undefined, description: formDescription || undefined },
      {
        onSuccess: (res) => {
          if (res.success) setShowCreateDialog(false);
        },
      }
    );
  };

  const handleUpdate = () => {
    updateTag.mutate(
      { name: formName, slug: formSlug || undefined, description: formDescription || undefined },
      {
        onSuccess: (res) => {
          if (res.success) setEditTag(null);
        },
      }
    );
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Tags</h1>
          <p className="text-muted-foreground">Quản lý tags bài viết.</p>
        </div>
        <PermissionGate permission="content.create">
          <Button size="sm" onClick={openCreate}>
            <Plus className="mr-2 h-4 w-4" />
            Thêm tag
          </Button>
        </PermissionGate>
      </div>

      <SearchInput
        value={search}
        onChange={handleSearch}
        placeholder="Tìm kiếm tag..."
        className="w-full sm:max-w-sm"
      />

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
        </div>
      ) : (
        <>
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Tên</TableHead>
                  <TableHead>Slug</TableHead>
                  <TableHead>Mô tả</TableHead>
                  <TableHead>Số bài viết</TableHead>
                  <TableHead>Ngày tạo</TableHead>
                  <TableHead className="w-[70px]" />
                </TableRow>
              </TableHeader>
              <TableBody>
                {tags.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className="text-muted-foreground h-24 text-center">
                      Không tìm thấy tag nào.
                    </TableCell>
                  </TableRow>
                ) : (
                  tags.map((tag) => (
                    <TableRow key={tag.id}>
                      <TableCell>
                        <div className="flex items-center gap-3">
                          <Avatar className="h-8 w-8 shrink-0 rounded-md">
                            <AvatarFallback className="bg-primary/10 text-primary rounded-md">
                              <Hash className="h-3.5 w-3.5" />
                            </AvatarFallback>
                          </Avatar>
                          <p className="truncate text-sm font-medium">
                            {tag.name}
                          </p>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-1.5">
                          <span className="text-muted-foreground text-sm">{tag.slug}</span>
                          <Link
                            href={`/bai-viet/tag/${tag.slug}`}
                            target="_blank"
                            className="text-muted-foreground hover:text-primary"
                            title="Xem trang tag"
                          >
                            <ExternalLink className="h-3.5 w-3.5" />
                          </Link>
                        </div>
                      </TableCell>
                      <TableCell className="text-muted-foreground max-w-[200px] truncate text-sm">
                        {tag.description || <span className="italic opacity-50">Chưa có</span>}
                      </TableCell>
                      <TableCell className="text-sm">{tag.article_count ?? 0}</TableCell>
                      <TableCell className="text-muted-foreground text-sm">
                        {formatDate(tag.created_at)}
                      </TableCell>
                      <TableCell>
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon" className="h-8 w-8">
                              <MoreHorizontal className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            {can("content.update") && (
                              <DropdownMenuItem onClick={() => openEdit(tag)}>
                                <Pencil className="mr-2 h-4 w-4" />
                                Chỉnh sửa
                              </DropdownMenuItem>
                            )}
                            {can("content.delete") && (
                              <>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem
                                  onClick={() => setDeleteTag(tag)}
                                  className="text-destructive focus:text-destructive"
                                >
                                  <Trash2 className="mr-2 h-4 w-4" />
                                  Xóa
                                </DropdownMenuItem>
                              </>
                            )}
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>

          {meta && (
            <Pagination
              page={meta.page}
              limit={meta.limit}
              total={meta.total}
              totalPages={meta.total_pages}
              onPageChange={setPage}
              onLimitChange={(l) => {
                setLimit(l);
                setPage(1);
              }}
            />
          )}
        </>
      )}

      {/* Create Dialog */}
      <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Tạo tag mới</DialogTitle>
            <DialogDescription>Thêm tag mới cho bài viết.</DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Tên tag *</Label>
              <Input
                value={formName}
                onChange={(e) => setFormName(e.target.value)}
                placeholder="Nhập tên tag"
              />
            </div>
            <div className="space-y-2">
              <Label>Slug <span className="text-muted-foreground text-xs">(tự động tạo nếu để trống)</span></Label>
              <Input
                value={formSlug}
                onChange={(e) => setFormSlug(e.target.value)}
                placeholder="vd: anh-hung-dan-toc"
              />
            </div>
            <div className="space-y-2">
              <Label>Mô tả</Label>
              <Textarea
                value={formDescription}
                onChange={(e) => setFormDescription(e.target.value)}
                placeholder="Mô tả ngắn về tag này..."
                rows={3}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowCreateDialog(false)}>
              Hủy
            </Button>
            <Button onClick={handleCreate} disabled={!formName || createTag.isPending}>
              {createTag.isPending ? "Đang tạo..." : "Tạo"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={!!editTag} onOpenChange={(open) => !open && setEditTag(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Chỉnh sửa tag</DialogTitle>
            <DialogDescription>Cập nhật thông tin tag.</DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Tên tag *</Label>
              <Input value={formName} onChange={(e) => setFormName(e.target.value)} />
            </div>
            <div className="space-y-2">
              <Label>Slug</Label>
              <Input
                value={formSlug}
                onChange={(e) => setFormSlug(e.target.value)}
                placeholder="vd: anh-hung-dan-toc"
              />
            </div>
            <div className="space-y-2">
              <Label>Mô tả</Label>
              <Textarea
                value={formDescription}
                onChange={(e) => setFormDescription(e.target.value)}
                placeholder="Mô tả ngắn về tag này..."
                rows={3}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditTag(null)}>
              Hủy
            </Button>
            <Button onClick={handleUpdate} disabled={!formName || updateTag.isPending}>
              {updateTag.isPending ? "Đang cập nhật..." : "Cập nhật"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirm */}
      <ConfirmDialog
        open={!!deleteTag}
        onOpenChange={(open) => !open && setDeleteTag(null)}
        title="Xóa tag"
        description={`Bạn có chắc chắn muốn xóa tag "${deleteTag?.name}"?`}
        confirmText="Xóa"
        variant="destructive"
        loading={deleteTagMut.isPending}
        onConfirm={() => {
          if (deleteTag) {
            deleteTagMut.mutate(deleteTag.id);
            setDeleteTag(null);
          }
        }}
      />
    </div>
  );
}
