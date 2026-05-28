"use client";

import { useState, useCallback } from "react";
import Link from "next/link";
import { Plus, ToggleLeft, ToggleRight, Trash2, Pencil } from "lucide-react";
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
import { usePopups, useDeletePopup, useTogglePopup } from "@/hooks/usePopups";
import { DEFAULT_PAGE_SIZE, ROUTES } from "@/lib/constants";
import { getImageUrl } from "@/lib/utils";
import Image from "next/image";

export default function PopupsPage() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);
  const [search, setSearch] = useState("");

  const { data, isLoading } = usePopups({
    page,
    limit,
    search: search || undefined,
  });

  const deletePopup = useDeletePopup();
  const togglePopup = useTogglePopup();

  const popups = data?.data ?? [];
  const meta = data?.meta;

  const handleSearch = useCallback((value: string) => {
    setSearch(value);
    setPage(1);
  }, []);

  const handleLimitChange = useCallback((newLimit: number) => {
    setLimit(newLimit);
    setPage(1);
  }, []);

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

  const getPositionLabel = (pos: string) => {
    switch (pos) {
      case "center_left": return "Giữa bên trái";
      case "center_right": return "Giữa bên phải";
      case "top_left": return "Góc trên bên trái";
      case "top_right": return "Góc trên bên phải";
      case "bottom_left": return "Góc dưới bên trái";
      case "bottom_right": return "Góc dưới bên phải";
      default: return pos;
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Quản lý Popup nổi</h1>
          <p className="text-muted-foreground">
            Cấu hình popup overlay đè trên màn hình ứng dụng di động Lịch Số.
          </p>
        </div>
        <PermissionGate permission="content.create">
          <Button size="sm" asChild>
            <Link href={`${ROUTES.ADMIN_POPUPS}/create`}>
              <Plus className="mr-2 h-4 w-4" />
              Thêm popup
            </Link>
          </Button>
        </PermissionGate>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <SearchInput
          value={search}
          onChange={handleSearch}
          placeholder="Tìm kiếm popup..."
          className="w-full sm:max-w-sm"
        />
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <div className="flex flex-col items-center gap-3">
            <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
            <p className="text-muted-foreground text-sm">Đang tải popup...</p>
          </div>
        </div>
      ) : popups.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16">
          <p className="text-muted-foreground mb-4 text-sm">Chưa có popup nào được tạo.</p>
          <PermissionGate permission="content.create">
            <Button asChild>
              <Link href={`${ROUTES.ADMIN_POPUPS}/create`}>
                <Plus className="mr-2 h-4 w-4" />
                Tạo popup đầu tiên
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
                  <TableHead>Hình ảnh</TableHead>
                  <TableHead>Tiêu đề</TableHead>
                  <TableHead>Vị trí ban đầu</TableHead>
                  <TableHead className="hidden lg:table-cell">Đích đến CTA</TableHead>
                  <TableHead className="hidden lg:table-cell">Hiển thị từ - đến</TableHead>
                  <TableHead>Trạng thái</TableHead>
                  <TableHead className="text-right">Thao tác</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {popups.map((popup) => (
                  <TableRow key={popup.id}>
                    {/* Image Preview */}
                    <TableCell>
                      <div className="relative h-12 w-12 overflow-hidden rounded bg-muted/20 border flex items-center justify-center">
                        <Image
                          src={getImageUrl(popup.image_url)}
                          alt=""
                          fill
                          sizes="48px"
                          className="object-contain"
                        />
                      </div>
                    </TableCell>

                    {/* Title */}
                    <TableCell className="font-medium max-w-[200px] truncate">
                      {popup.title}
                    </TableCell>

                    {/* Position */}
                    <TableCell>
                      <Badge variant="secondary">
                        {getPositionLabel(popup.position)}
                      </Badge>
                    </TableCell>

                    {/* CTA */}
                    <TableCell className="hidden lg:table-cell max-w-[220px] truncate">
                      {popup.cta_route ? (
                        <div>
                          <Badge variant="outline" className="text-xs">
                            {popup.cta_type === "url" ? "WEB" : "APP"}
                          </Badge>
                          <span className="ml-1.5 text-xs font-mono">{popup.cta_route}</span>
                        </div>
                      ) : (
                        <span className="text-muted-foreground text-xs">—</span>
                      )}
                    </TableCell>

                    {/* Dates */}
                    <TableCell className="hidden lg:table-cell text-xs">
                      <div>Bắt đầu: {formatDate(popup.start_date)}</div>
                      <div>Hết hạn: {formatDate(popup.end_date)}</div>
                    </TableCell>

                    {/* Active */}
                    <TableCell>
                      <PermissionGate
                        permission="content.update"
                        fallback={
                          <Badge variant={popup.is_active ? "default" : "secondary"}>
                            {popup.is_active ? "Kích hoạt" : "Tắt"}
                          </Badge>
                        }
                      >
                        <Button
                          variant="ghost"
                          size="sm"
                          className={`gap-1.5 ${popup.is_active ? "text-green-600" : "text-muted-foreground"}`}
                          onClick={() => togglePopup.mutate(popup.id)}
                          disabled={togglePopup.isPending}
                        >
                          {popup.is_active ? (
                            <ToggleRight className="h-4 w-4" />
                          ) : (
                            <ToggleLeft className="h-4 w-4" />
                          )}
                          {popup.is_active ? "Bật" : "Tắt"}
                        </Button>
                      </PermissionGate>
                    </TableCell>

                    {/* Actions */}
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        <PermissionGate permission="content.update">
                          <Button variant="ghost" size="icon" asChild>
                            <Link href={`${ROUTES.ADMIN_POPUPS}/${popup.id}`}>
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
                                <AlertDialogTitle>Xóa popup?</AlertDialogTitle>
                                <AlertDialogDescription>
                                  Popup &quot;{popup.title}&quot; sẽ bị xóa vĩnh viễn khỏi hệ thống.
                                </AlertDialogDescription>
                              </AlertDialogHeader>
                              <AlertDialogFooter>
                                <AlertDialogCancel>Hủy</AlertDialogCancel>
                                <AlertDialogAction
                                  onClick={() => deletePopup.mutate(popup.id)}
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
                ))}
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
