"use client";

import { useState, useCallback } from "react";
import { Plus, Pencil, Trash2, MoreHorizontal, Layers3 } from "lucide-react";
import { getCategoryIcon } from "@/lib/categoryIcons";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
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
import { Checkbox } from "@/components/ui/checkbox";
import { SearchInput } from "@/components/shared/SearchInput";
import { Pagination } from "@/components/shared/Pagination";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { PermissionGate } from "@/components/auth/PermissionGate";
import {
  useCategories,
  useCreateCategory,
  useUpdateCategory,
  useDeleteCategory,
} from "@/hooks/useArticles";
import { usePermission } from "@/hooks/usePermission";
import { DEFAULT_PAGE_SIZE } from "@/lib/constants";
import { formatDate } from "@/lib/utils";
import type { ArticleCategory } from "@/types/article";

export default function CategoriesPage() {
  const { can } = usePermission();
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);
  const [search, setSearch] = useState("");

  const [editCategory, setEditCategory] = useState<ArticleCategory | null>(null);
  const [deleteCategory, setDeleteCategory] = useState<ArticleCategory | null>(null);
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [formName, setFormName] = useState("");
  const [formDescription, setFormDescription] = useState("");
  const [formIcon, setFormIcon] = useState("");
  const [formSortOrder, setFormSortOrder] = useState(0);
  const [formIsActive, setFormIsActive] = useState(true);


  const { data, isLoading } = useCategories({ page, limit, search: search || undefined });
  const createCategory = useCreateCategory();
  const updateCategory = useUpdateCategory(editCategory?.id ?? "");
  const deleteCategoryMut = useDeleteCategory();

  const categories = data?.data ?? [];
  const meta = data?.meta;

  const handleSearch = useCallback((value: string) => {
    setSearch(value);
    setPage(1);
  }, []);

  const openCreate = () => {
    setFormName("");
    setFormDescription("");
    setFormIcon("");
    setFormSortOrder(0);
    setFormIsActive(true);
    setShowCreateDialog(true);
  };

  const openEdit = (cat: ArticleCategory) => {
    setFormName(cat.name);
    setFormDescription(cat.description || "");
    setFormIcon(cat.icon || "");
    setFormSortOrder(cat.sort_order);
    setFormIsActive(cat.is_active);
    setEditCategory(cat);
  };

  const handleCreate = () => {
    createCategory.mutate(
      {
        name: formName,
        description: formDescription || undefined,
        icon: formIcon || undefined,
        sort_order: formSortOrder,
        is_active: formIsActive,
      },
      {
        onSuccess: (res) => {
          if (res.success) setShowCreateDialog(false);
        },
      }
    );
  };

  const handleUpdate = () => {
    updateCategory.mutate(
      {
        name: formName,
        description: formDescription || undefined,
        icon: formIcon || undefined,
        sort_order: formSortOrder,
        is_active: formIsActive,
      },
      {
        onSuccess: (res) => {
          if (res.success) setEditCategory(null);
        },
      }
    );
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Danh mục</h1>
          <p className="text-muted-foreground">Quản lý danh mục bài viết.</p>
        </div>
        <PermissionGate permission="content.create">
          <Button size="sm" onClick={openCreate}>
            <Plus className="mr-2 h-4 w-4" />
            Thêm danh mục
          </Button>
        </PermissionGate>
      </div>

      <SearchInput
        value={search}
        onChange={handleSearch}
        placeholder="Tìm kiếm danh mục..."
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
                  <TableHead>Thứ tự</TableHead>
                  <TableHead>Trạng thái</TableHead>
                  <TableHead>Ngày tạo</TableHead>
                  <TableHead className="w-[70px]" />
                </TableRow>
              </TableHeader>
              <TableBody>
                {categories.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className="text-muted-foreground h-24 text-center">
                      Không tìm thấy danh mục nào.
                    </TableCell>
                  </TableRow>
                ) : (
                  categories.map((cat) => {
                    const CatIcon = getCategoryIcon(cat.slug);
                    return (
                    <TableRow key={cat.id}>
                      <TableCell>
                        <div className="flex items-center gap-3">
                          <Avatar className="h-9 w-9 shrink-0 rounded-md">
                            <AvatarFallback className="bg-primary/10 text-primary rounded-md text-lg">
                              <CatIcon className="h-4 w-4" />
                            </AvatarFallback>
                          </Avatar>
                          <div className="min-w-0">
                            <p className="truncate text-sm font-medium">
                              {cat.name}
                            </p>
                            {cat.description && (
                              <p className="text-muted-foreground truncate text-xs">
                                {cat.description}
                              </p>
                            )}
                          </div>
                        </div>
                      </TableCell>
                      <TableCell className="text-muted-foreground text-sm">{cat.slug}</TableCell>
                      <TableCell className="text-sm">{cat.sort_order}</TableCell>
                      <TableCell>
                        <Badge
                          variant={cat.is_active ? "default" : "secondary"}
                          className={
                            cat.is_active
                              ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400"
                              : ""
                          }
                        >
                          {cat.is_active ? "Hoạt động" : "Ẩn"}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-muted-foreground text-sm">
                        {formatDate(cat.created_at)}
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
                              <DropdownMenuItem onClick={() => openEdit(cat)}>
                                <Pencil className="mr-2 h-4 w-4" />
                                Chỉnh sửa
                              </DropdownMenuItem>
                            )}
                            {can("content.delete") && (
                              <>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem
                                  onClick={() => setDeleteCategory(cat)}
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
                    );
                  })
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
            <DialogTitle>Tạo danh mục mới</DialogTitle>
            <DialogDescription>Thêm danh mục bài viết mới.</DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Tên danh mục *</Label>
              <Input
                value={formName}
                onChange={(e) => setFormName(e.target.value)}
                placeholder="Nhập tên danh mục"
              />
            </div>
            <div className="space-y-2">
              <Label>Mô tả</Label>
              <Input
                value={formDescription}
                onChange={(e) => setFormDescription(e.target.value)}
                placeholder="Mô tả danh mục"
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Icon</Label>
                <Input
                  value={formIcon}
                  onChange={(e) => setFormIcon(e.target.value)}
                  placeholder="📄"
                />
              </div>
              <div className="space-y-2">
                <Label>Thứ tự</Label>
                <Input
                  type="number"
                  value={formSortOrder}
                  onChange={(e) => setFormSortOrder(Number(e.target.value))}
                />
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Checkbox
                id="create_is_active"
                checked={formIsActive}
                onCheckedChange={(c) => setFormIsActive(c === true)}
              />
              <Label htmlFor="create_is_active">Hoạt động</Label>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowCreateDialog(false)}>
              Hủy
            </Button>
            <Button onClick={handleCreate} disabled={!formName || createCategory.isPending}>
              {createCategory.isPending ? "Đang tạo..." : "Tạo"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={!!editCategory} onOpenChange={(open) => !open && setEditCategory(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Chỉnh sửa danh mục</DialogTitle>
            <DialogDescription>Cập nhật thông tin danh mục.</DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Tên danh mục *</Label>
              <Input value={formName} onChange={(e) => setFormName(e.target.value)} />
            </div>
            <div className="space-y-2">
              <Label>Mô tả</Label>
              <Input value={formDescription} onChange={(e) => setFormDescription(e.target.value)} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Icon</Label>
                <Input value={formIcon} onChange={(e) => setFormIcon(e.target.value)} />
              </div>
              <div className="space-y-2">
                <Label>Thứ tự</Label>
                <Input
                  type="number"
                  value={formSortOrder}
                  onChange={(e) => setFormSortOrder(Number(e.target.value))}
                />
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Checkbox
                id="edit_is_active"
                checked={formIsActive}
                onCheckedChange={(c) => setFormIsActive(c === true)}
              />
              <Label htmlFor="edit_is_active">Hoạt động</Label>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditCategory(null)}>
              Hủy
            </Button>
            <Button onClick={handleUpdate} disabled={!formName || updateCategory.isPending}>
              {updateCategory.isPending ? "Đang cập nhật..." : "Cập nhật"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirm */}
      <ConfirmDialog
        open={!!deleteCategory}
        onOpenChange={(open) => !open && setDeleteCategory(null)}
        title="Xóa danh mục"
        description={`Bạn có chắc chắn muốn xóa danh mục "${deleteCategory?.name}"?`}
        confirmText="Xóa"
        variant="destructive"
        loading={deleteCategoryMut.isPending}
        onConfirm={() => {
          if (deleteCategory) {
            deleteCategoryMut.mutate(deleteCategory.id);
            setDeleteCategory(null);
          }
        }}
      />

    </div>
  );
}
