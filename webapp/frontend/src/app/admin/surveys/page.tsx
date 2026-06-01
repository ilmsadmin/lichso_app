"use client";

import { useState, useCallback } from "react";
import Link from "next/link";
import { Plus, ToggleLeft, ToggleRight, Trash2, Pencil, BarChart2 } from "lucide-react";
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
import { useSurveys, useDeleteSurvey, useToggleSurvey } from "@/hooks/useSurveys";
import { DEFAULT_PAGE_SIZE, ROUTES } from "@/lib/constants";

export default function SurveysPage() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);
  const [search, setSearch] = useState("");

  const { data, isLoading } = useSurveys({
    page,
    limit,
    search: search || undefined,
  });

  const deleteSurvey = useDeleteSurvey();
  const toggleSurvey = useToggleSurvey();

  const surveys = data?.data ?? [];
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

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Quản lý Khảo sát (Survey)</h1>
          <p className="text-muted-foreground">
            Tạo và thu thập khảo sát ý kiến người dùng trên ứng dụng di động Lịch Số.
          </p>
        </div>
        <PermissionGate permission="content.create">
          <Button size="sm" asChild>
            <Link href={`${ROUTES.ADMIN_SURVEYS}/create`}>
              <Plus className="mr-2 h-4 w-4" />
              Thêm khảo sát
            </Link>
          </Button>
        </PermissionGate>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <SearchInput
          value={search}
          onChange={handleSearch}
          placeholder="Tìm kiếm khảo sát..."
          className="w-full sm:max-w-sm"
        />
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <div className="flex flex-col items-center gap-3">
            <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
            <p className="text-muted-foreground text-sm">Đang tải khảo sát...</p>
          </div>
        </div>
      ) : surveys.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16">
          <p className="text-muted-foreground mb-4 text-sm">Chưa có khảo sát nào được tạo.</p>
          <PermissionGate permission="content.create">
            <Button asChild>
              <Link href={`${ROUTES.ADMIN_SURVEYS}/create`}>
                <Plus className="mr-2 h-4 w-4" />
                Tạo khảo sát đầu tiên
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
                  <TableHead>Tiêu đề khảo sát</TableHead>
                  <TableHead>Số câu hỏi</TableHead>
                  <TableHead className="hidden lg:table-cell">Ngày tạo</TableHead>
                  <TableHead>Trạng thái</TableHead>
                  <TableHead className="text-right">Thao tác</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {surveys.map((sv) => (
                  <TableRow key={sv.id}>
                    {/* Title & Description */}
                    <TableCell className="font-medium max-w-[300px]">
                      <div className="flex flex-col">
                        <span className="truncate text-base">{sv.title}</span>
                        {sv.description && (
                          <span className="text-xs text-muted-foreground truncate max-w-[280px]">
                            {sv.description}
                          </span>
                        )}
                      </div>
                    </TableCell>

                    {/* Question Count */}
                    <TableCell>
                      <Badge variant="secondary">
                        {sv.questions?.length ?? 0} câu hỏi
                      </Badge>
                    </TableCell>

                    {/* Dates */}
                    <TableCell className="hidden lg:table-cell text-sm">
                      {formatDate(sv.created_at)}
                    </TableCell>

                    {/* Active Toggle */}
                    <TableCell>
                      <PermissionGate
                        permission="content.update"
                        fallback={
                          <Badge variant={sv.is_active ? "default" : "secondary"}>
                            {sv.is_active ? "Kích hoạt" : "Tắt"}
                          </Badge>
                        }
                      >
                        <Button
                          variant="ghost"
                          size="sm"
                          className={`gap-1.5 ${sv.is_active ? "text-green-600" : "text-muted-foreground"}`}
                          onClick={() => toggleSurvey.mutate(sv.id)}
                          disabled={toggleSurvey.isPending}
                        >
                          {sv.is_active ? (
                            <ToggleRight className="h-4.5 w-4.5" />
                          ) : (
                            <ToggleLeft className="h-4.5 w-4.5" />
                          )}
                          {sv.is_active ? "Bật" : "Tắt"}
                        </Button>
                      </PermissionGate>
                    </TableCell>

                    {/* Actions */}
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        {/* Results Stats Button */}
                        <Button variant="ghost" size="icon" title="Xem kết quả khảo sát" asChild>
                          <Link href={`${ROUTES.ADMIN_SURVEYS}/${sv.id}/responses`}>
                            <BarChart2 className="h-4 w-4 text-primary" />
                          </Link>
                        </Button>

                        <PermissionGate permission="content.update">
                          <Button variant="ghost" size="icon" title="Sửa câu hỏi" asChild>
                            <Link href={`${ROUTES.ADMIN_SURVEYS}/${sv.id}`}>
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
                                <AlertDialogTitle>Xóa cuộc khảo sát?</AlertDialogTitle>
                                <AlertDialogDescription>
                                  Khảo sát &quot;{sv.title}&quot; và tất cả câu phản hồi của người dùng sẽ bị xóa vĩnh viễn khỏi hệ thống.
                                </AlertDialogDescription>
                              </AlertDialogHeader>
                              <AlertDialogFooter>
                                <AlertDialogCancel>Hủy</AlertDialogCancel>
                                <AlertDialogAction
                                  onClick={() => deleteSurvey.mutate(sv.id)}
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
