"use client";

import { useState, useCallback } from "react";
import dynamic from "next/dynamic";
import Link from "next/link";
import { Plus, Upload } from "lucide-react";
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
import {
  BulkImportDialog,
  type CsvColumn,
  type ImportResult,
} from "@/components/shared/BulkImportDialog";
import { useQuotes, useDeleteQuote, useCreateQuote } from "@/hooks/useQuotes";
import { DEFAULT_PAGE_SIZE, ROUTES } from "@/lib/constants";

const QuoteTable = dynamic(
  () =>
    import("@/components/quotes/QuoteTable").then((mod) => ({
      default: mod.QuoteTable,
    })),
  {
    loading: () => (
      <div className="flex items-center justify-center py-12">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    ),
  }
);

export default function QuotesPage() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);
  const [search, setSearch] = useState("");
  const [isActive, setIsActive] = useState<string>("all");
  const [bulkImportOpen, setBulkImportOpen] = useState(false);

  const { data, isLoading } = useQuotes({
    page,
    limit,
    search: search || undefined,
    is_active: isActive !== "all" ? isActive === "true" : undefined,
  });

  const deleteQuote = useDeleteQuote();
  const createQuote = useCreateQuote();

  const quotes = data?.data ?? [];
  const meta = data?.meta;

  const handleSearch = useCallback((value: string) => {
    setSearch(value);
    setPage(1);
  }, []);

  const handleLimitChange = useCallback((newLimit: number) => {
    setLimit(newLimit);
    setPage(1);
  }, []);

  // Bulk import CSV columns
  const quoteColumns: CsvColumn[] = [
    { key: "quote", label: "Nội dung", required: true },
    { key: "author", label: "Tác giả", required: true },
    { key: "original_quote", label: "Nội dung gốc" },
    { key: "author_title", label: "Chức danh" },
    { key: "source", label: "Nguồn" },
    { key: "day_of_year", label: "Ngày trong năm" },
    { key: "tags", label: "Tags" },
  ];

  const quoteSampleData = [
    {
      quote: "Học, học nữa, học mãi",
      author: "Lê-nin",
      original_quote: "",
      author_title: "Lãnh tụ cách mạng",
      source: "",
      day_of_year: "1",
      tags: "học tập, giáo dục",
    },
    {
      quote: "Cuộc sống là điều xảy ra khi bạn đang bận rộn làm kế hoạch khác",
      author: "John Lennon",
      original_quote: "Life is what happens when you are busy making other plans",
      author_title: "Nhạc sĩ",
      source: "Beautiful Boy",
      day_of_year: "0",
      tags: "cuộc sống, triết học",
    },
  ];

  const handleBulkImport = async (records: Record<string, string>[]): Promise<ImportResult> => {
    let success = 0;
    let failed = 0;
    const errors: string[] = [];

    for (const record of records) {
      try {
        const tagsArray = record.tags
          ? record.tags
              .split(",")
              .map((t) => t.trim())
              .filter(Boolean)
          : undefined;

        await createQuote.mutateAsync({
          quote: record.quote,
          author: record.author,
          original_quote: record.original_quote || undefined,
          author_bio: record.author_title || undefined,
          day_of_year: record.day_of_year ? parseInt(record.day_of_year, 10) : undefined,
          tags: tagsArray,
        });
        success++;
      } catch {
        failed++;
        errors.push(`"${record.quote?.slice(0, 40)}..." - Lỗi khi tạo`);
      }
    }

    return { total: records.length, success, failed, errors };
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Danh ngôn</h1>
          <p className="text-muted-foreground">
            Quản lý danh ngôn nổi tiếng hiển thị trên hệ thống.
          </p>
        </div>
        <PermissionGate permission="content.create">
          <div className="flex items-center gap-2">
            <Button size="sm" variant="outline" onClick={() => setBulkImportOpen(true)}>
              <Upload className="mr-2 h-4 w-4" />
              Import CSV
            </Button>
            <Button size="sm" asChild>
              <Link href={`${ROUTES.ADMIN_QUOTES}/create`}>
                <Plus className="mr-2 h-4 w-4" />
                Thêm danh ngôn
              </Link>
            </Button>
          </div>
        </PermissionGate>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <SearchInput
          value={search}
          onChange={handleSearch}
          placeholder="Tìm kiếm danh ngôn..."
          className="w-full sm:max-w-sm"
        />
        <Select
          value={isActive}
          onValueChange={(v) => {
            setIsActive(v);
            setPage(1);
          }}
        >
          <SelectTrigger className="w-[160px]">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            <SelectItem value="true">Hoạt động</SelectItem>
            <SelectItem value="false">Ẩn</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <div className="flex flex-col items-center gap-3">
            <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
            <p className="text-muted-foreground text-sm">Đang tải danh ngôn...</p>
          </div>
        </div>
      ) : (
        <>
          <QuoteTable
            quotes={quotes}
            onDelete={(id) => deleteQuote.mutate(id)}
            isDeleting={deleteQuote.isPending}
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

      {/* Bulk Import Dialog */}
      <BulkImportDialog
        open={bulkImportOpen}
        onOpenChange={setBulkImportOpen}
        title="Import danh ngôn từ CSV"
        description="Upload file CSV chứa danh sách danh ngôn để thêm hàng loạt."
        columns={quoteColumns}
        sampleData={quoteSampleData}
        onImport={handleBulkImport}
      />
    </div>
  );
}
