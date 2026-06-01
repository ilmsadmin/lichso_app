"use client";

import { useState, useCallback } from "react";
import Link from "next/link";
import { Plus, ToggleLeft, ToggleRight, Trash2, Pencil, GripVertical } from "lucide-react";
import { Button } from "@/components/ui/button";
import { SearchInput } from "@/components/shared/SearchInput";
import { Pagination } from "@/components/shared/Pagination";
import { PermissionGate } from "@/components/auth/PermissionGate";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { useBanners, useDeleteBanner, useToggleBanner } from "@/hooks/useBanners";
import { DEFAULT_PAGE_SIZE, ROUTES } from "@/lib/constants";
import { BANNER_TYPES } from "@/types/banner";

export default function BannersPage() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);
  const [search, setSearch] = useState("");

  const { data, isLoading } = useBanners({
    page,
    limit,
    search: search || undefined,
  });

  const deleteBanner = useDeleteBanner();
  const toggleBanner = useToggleBanner();

  const banners = data?.data ?? [];
  const meta = data?.meta;

  const handleSearch = useCallback((value: string) => {
    setSearch(value);
    setPage(1);
  }, []);

  const handleLimitChange = useCallback((newLimit: number) => {
    setLimit(newLimit);
    setPage(1);
  }, []);

  const getTypeInfo = (type: string) => {
    return BANNER_TYPES.find((t) => t.value === type) || { value: type, label: type, color: "#666" };
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return "—";
    return new Date(dateStr).toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Quản lý Banner</h1>
          <p className="text-muted-foreground">
            Quản lý banner hiển thị trên trang chủ ứng dụng di động Lịch Số.
          </p>
        </div>
        <PermissionGate permission="content.create">
          <Button size="sm" asChild>
            <Link href={`${ROUTES.ADMIN_BANNERS}/create`}>
              <Plus className="mr-2 h-4 w-4" />
              Thêm banner
            </Link>
          </Button>
        </PermissionGate>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <SearchInput
          value={search}
          onChange={handleSearch}
          placeholder="Tìm kiếm banner..."
          className="w-full sm:max-w-sm"
        />
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <div className="flex flex-col items-center gap-3">
            <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
            <p className="text-muted-foreground text-sm">Đang tải banner...</p>
          </div>
        </div>
      ) : banners.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16">
          <p className="text-muted-foreground mb-4 text-sm">Chưa có banner nào.</p>
          <PermissionGate permission="content.create">
            <Button asChild>
              <Link href={`${ROUTES.ADMIN_BANNERS}/create`}>
                <Plus className="mr-2 h-4 w-4" />
                Tạo banner đầu tiên
              </Link>
            </Button>
          </PermissionGate>
        </div>
      ) : (
        <>
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-10">
                    <GripVertical className="h-4 w-4 text-muted-foreground" />
                  </TableHead>
                  <TableHead>Banner</TableHead>
                  <TableHead className="hidden md:table-cell">Loại</TableHead>
                  <TableHead className="hidden lg:table-cell">CTA</TableHead>
                  <TableHead className="hidden lg:table-cell">Thời gian</TableHead>
                  <TableHead>Trạng thái</TableHead>
                  <TableHead className="text-right">Thao tác</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {banners.map((banner) => {
                  const typeInfo = getTypeInfo(banner.type);
                  return (
                    <TableRow key={banner.id}>
                      {/* Sort order */}
                      <TableCell className="text-muted-foreground text-center text-xs font-mono">
                        {banner.sort_order}
                      </TableCell>

                      {/* Title + Preview */}
                      <TableCell>
                        <div className="flex items-center gap-3">
                          {/* Mini color preview */}
                          <div
                            className="h-10 w-10 shrink-0 rounded-lg"
                            style={{
                              background: `linear-gradient(135deg, ${banner.bg_color || typeInfo.color}, ${typeInfo.color}dd)`,
                            }}
                          />
                          <div className="min-w-0">
                            <div className="flex items-center gap-2">
                              <p className="truncate text-sm font-medium">{banner.title}</p>
                              {banner.platform && banner.platform !== "all" && (
                                <Badge variant="secondary" className="shrink-0 text-[10px] uppercase">
                                  {banner.platform}
                                </Badge>
                              )}
                            </div>
                            {banner.subtitle && (
                              <p className="text-muted-foreground truncate text-xs">
                                {banner.subtitle}
                              </p>
                            )}
                          </div>
                        </div>
                      </TableCell>

                      {/* Type */}
                      <TableCell className="hidden md:table-cell">
                        <Badge
                          variant="outline"
                          className="gap-1"
                          style={{ borderColor: typeInfo.color, color: typeInfo.color }}
                        >
                          <span
                            className="inline-block h-2 w-2 rounded-full"
                            style={{ backgroundColor: typeInfo.color }}
                          />
                          {typeInfo.label}
                        </Badge>
                      </TableCell>

                      {/* CTA */}
                      <TableCell className="hidden lg:table-cell">
                        {banner.cta_text ? (
                          <div>
                            <span className="text-sm">{banner.cta_text}</span>
                            {banner.cta_route && (
                              <p className="text-muted-foreground font-mono text-xs">
                                → {banner.cta_route}
                              </p>
                            )}
                          </div>
                        ) : (
                          <span className="text-muted-foreground text-xs">—</span>
                        )}
                      </TableCell>

                      {/* Date range */}
                      <TableCell className="hidden lg:table-cell">
                        <div className="text-xs">
                          <div>{formatDate(banner.start_date)}</div>
                          <div>{formatDate(banner.end_date)}</div>
                        </div>
                      </TableCell>

                      {/* Active status */}
                      <TableCell>
                        <PermissionGate
                          permission="content.update"
                          fallback={
                            <Badge variant={banner.is_active ? "default" : "secondary"}>
                              {banner.is_active ? "Hoạt động" : "Ẩn"}
                            </Badge>
                          }
                        >
                          <Button
                            variant="ghost"
                            size="sm"
                            className={`gap-1.5 ${banner.is_active ? "text-green-600" : "text-muted-foreground"}`}
                            onClick={() => toggleBanner.mutate(banner.id)}
                            disabled={toggleBanner.isPending}
                          >
                            {banner.is_active ? (
                              <ToggleRight className="h-4 w-4" />
                            ) : (
                              <ToggleLeft className="h-4 w-4" />
                            )}
                            {banner.is_active ? "Bật" : "Tắt"}
                          </Button>
                        </PermissionGate>
                      </TableCell>

                      {/* Actions */}
                      <TableCell className="text-right">
                        <div className="flex items-center justify-end gap-1">
                          <PermissionGate permission="content.update">
                            <Button variant="ghost" size="icon" asChild>
                              <Link href={`${ROUTES.ADMIN_BANNERS}/${banner.id}`}>
                                <Pencil className="h-4 w-4" />
                              </Link>
                            </Button>
                          </PermissionGate>

                          <PermissionGate permission="content.delete">
                            <AlertDialog>
                              <AlertDialogTrigger asChild>
                                <Button variant="ghost" size="icon" className="text-destructive">
                                  <Trash2 className="h-4 w-4" />
                                </Button>
                              </AlertDialogTrigger>
                              <AlertDialogContent>
                                <AlertDialogHeader>
                                  <AlertDialogTitle>Xóa banner?</AlertDialogTitle>
                                  <AlertDialogDescription>
                                    Banner &quot;{banner.title}&quot; sẽ bị xóa. Hành động này không thể hoàn tác.
                                  </AlertDialogDescription>
                                </AlertDialogHeader>
                                <AlertDialogFooter>
                                  <AlertDialogCancel>Hủy</AlertDialogCancel>
                                  <AlertDialogAction
                                    onClick={() => deleteBanner.mutate(banner.id)}
                                    className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                                  >
                                    Xóa
                                  </AlertDialogAction>
                                </AlertDialogFooter>
                              </AlertDialogContent>
                            </AlertDialog>
                          </PermissionGate>
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })}
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
              onLimitChange={handleLimitChange}
            />
          )}
        </>
      )}
    </div>
  );
}
