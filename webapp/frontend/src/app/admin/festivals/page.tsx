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
import { useFolkFestivals, useDeleteFolkFestival } from "@/hooks/useFolkFestivals";
import { DEFAULT_PAGE_SIZE, ROUTES } from "@/lib/constants";

const FolkFestivalTable = dynamic(
  () =>
    import("@/components/festivals/FolkFestivalTable").then((mod) => ({
      default: mod.FolkFestivalTable,
    })),
  {
    loading: () => (
      <div className="flex items-center justify-center py-12">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    ),
  }
);

export default function FestivalsPage() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);
  const [search, setSearch] = useState("");
  const [festivalType, setFestivalType] = useState<string>("all");

  const { data, isLoading } = useFolkFestivals({
    page,
    limit,
    search: search || undefined,
    festival_type: festivalType !== "all" ? festivalType : undefined,
  });

  const deleteFestival = useDeleteFolkFestival();

  const festivals = data?.data ?? [];
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
          <h1 className="text-2xl font-bold tracking-tight">Lễ hội dân gian</h1>
          <p className="text-muted-foreground">
            Quản lý lễ hội dân gian và phong tục truyền thống.
          </p>
        </div>
        <PermissionGate permission="content.create">
          <Button size="sm" asChild>
            <Link href={`${ROUTES.ADMIN_FESTIVALS}/create`}>
              <Plus className="mr-2 h-4 w-4" />
              Thêm lễ hội
            </Link>
          </Button>
        </PermissionGate>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <SearchInput
          value={search}
          onChange={handleSearch}
          placeholder="Tìm kiếm lễ hội..."
          className="w-full sm:max-w-sm"
        />
        <Select
          value={festivalType}
          onValueChange={(v) => {
            setFestivalType(v);
            setPage(1);
          }}
        >
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Loại lễ hội" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            <SelectItem value="traditional">Truyền thống</SelectItem>
            <SelectItem value="religious">Tôn giáo</SelectItem>
            <SelectItem value="cultural">Văn hóa</SelectItem>
            <SelectItem value="seasonal">Mùa vụ</SelectItem>
            <SelectItem value="regional">Vùng miền</SelectItem>
            <SelectItem value="other">Khác</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <div className="flex flex-col items-center gap-3">
            <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
            <p className="text-muted-foreground text-sm">Đang tải lễ hội...</p>
          </div>
        </div>
      ) : (
        <>
          <FolkFestivalTable
            festivals={festivals}
            onDelete={(id) => deleteFestival.mutate(id)}
            isDeleting={deleteFestival.isPending}
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
