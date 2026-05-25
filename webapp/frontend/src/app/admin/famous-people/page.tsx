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
import { useFamousPeople, useDeleteFamousPerson } from "@/hooks/useFamousPeople";
import { DEFAULT_PAGE_SIZE, ROUTES } from "@/lib/constants";

const FamousPersonTable = dynamic(
  () =>
    import("@/components/famous-people/FamousPersonTable").then((mod) => ({
      default: mod.FamousPersonTable,
    })),
  {
    loading: () => (
      <div className="flex items-center justify-center py-12">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    ),
  }
);

export default function FamousPeoplePage() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);
  const [search, setSearch] = useState("");
  const [isVietnamese, setIsVietnamese] = useState<string>("all");

  const { data, isLoading } = useFamousPeople({
    page,
    limit,
    search: search || undefined,
    is_vietnamese: isVietnamese !== "all" ? isVietnamese === "true" : undefined,
  });

  const deletePerson = useDeleteFamousPerson();

  const people = data?.data ?? [];
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
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Nhân vật nổi tiếng</h1>
          <p className="text-muted-foreground">
            Quản lý danh sách nhân vật nổi tiếng và sinh nhật.
          </p>
        </div>
        <PermissionGate permission="content.create">
          <Button size="sm" asChild>
            <Link href={`${ROUTES.ADMIN_FAMOUS_PEOPLE}/create`}>
              <Plus className="mr-2 h-4 w-4" />
              Thêm nhân vật
            </Link>
          </Button>
        </PermissionGate>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <SearchInput
          value={search}
          onChange={handleSearch}
          placeholder="Tìm kiếm nhân vật..."
          className="w-full sm:max-w-sm"
        />
        <Select
          value={isVietnamese}
          onValueChange={(v) => {
            setIsVietnamese(v);
            setPage(1);
          }}
        >
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Quốc tịch" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            <SelectItem value="true">Việt Nam</SelectItem>
            <SelectItem value="false">Quốc tế</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <div className="flex flex-col items-center gap-3">
            <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
            <p className="text-muted-foreground text-sm">Đang tải danh sách...</p>
          </div>
        </div>
      ) : (
        <>
          <FamousPersonTable
            people={people}
            onDelete={(id) => deletePerson.mutate(id)}
            isDeleting={deletePerson.isPending}
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
