"use client";

import { useState, useCallback } from "react";
import dynamic from "next/dynamic";
import Link from "next/link";
import { Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { SearchInput } from "@/components/shared/SearchInput";
import { Pagination } from "@/components/shared/Pagination";
import { PermissionGate } from "@/components/auth/PermissionGate";
import { useArticles, useDeleteArticle, useCategories } from "@/hooks/useArticles";
import { DEFAULT_PAGE_SIZE, ROUTES } from "@/lib/constants";

const ArticleTable = dynamic(
  () =>
    import("@/components/articles/ArticleTable").then((mod) => ({
      default: mod.ArticleTable,
    })),
  {
    loading: () => (
      <div className="flex items-center justify-center py-12">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    ),
  }
);

export default function ArticlesPage() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState<string>("all");
  const [categoryId, setCategoryId] = useState<string>("all");

  const { data: categoriesData } = useCategories();
  const categories = categoriesData?.data ?? [];

  const { data, isLoading } = useArticles({
    page,
    limit,
    search: search || undefined,
    status: status !== "all" ? status : undefined,
    category_id: categoryId !== "all" ? categoryId : undefined,
  });

  const deleteArticle = useDeleteArticle();

  const articles = data?.data ?? [];
  const meta = data?.meta;

  const handleSearch = useCallback((value: string) => {
    setSearch(value);
    setPage(1);
  }, []);

  const handleLimitChange = useCallback((newLimit: number) => {
    setLimit(newLimit);
    setPage(1);
  }, []);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Bài viết</h1>
          <p className="text-muted-foreground">Quản lý bài viết và nội dung trên hệ thống.</p>
        </div>
        <PermissionGate permission="content.create">
          <Button size="sm" asChild>
            <Link href={`${ROUTES.ADMIN_ARTICLES}/create`}>
              <Plus className="mr-2 h-4 w-4" />
              Thêm bài viết
            </Link>
          </Button>
        </PermissionGate>
      </div>

      {/* Filters */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <SearchInput
          value={search}
          onChange={handleSearch}
          placeholder="Tìm kiếm bài viết..."
          className="w-full sm:max-w-sm"
        />
        <Select
          value={status}
          onValueChange={(v) => {
            setStatus(v);
            setPage(1);
          }}
        >
          <SelectTrigger className="w-[160px]">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            <SelectItem value="published">Đã xuất bản</SelectItem>
            <SelectItem value="draft">Bản nháp</SelectItem>
            <SelectItem value="archived">Lưu trữ</SelectItem>
          </SelectContent>
        </Select>
        <Select
          value={categoryId}
          onValueChange={(v) => {
            setCategoryId(v);
            setPage(1);
          }}
        >
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Danh mục" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả danh mục</SelectItem>
            {categories.map((cat) => (
              <SelectItem key={cat.id} value={cat.id}>
                {cat.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {/* Table */}
      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <div className="flex flex-col items-center gap-3">
            <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
            <p className="text-muted-foreground text-sm">Đang tải bài viết...</p>
          </div>
        </div>
      ) : (
        <>
          <ArticleTable
            articles={articles}
            onDelete={(id) => deleteArticle.mutate(id)}
            isDeleting={deleteArticle.isPending}
          />

          {meta && (
            <Pagination
              page={meta.page}
              limit={meta.limit}
              total={meta.total}
              totalPages={meta.total_pages}
              onPageChange={setPage}
              onLimitChange={handleLimitChange}
            />
          )}
        </>
      )}
    </div>
  );
}
