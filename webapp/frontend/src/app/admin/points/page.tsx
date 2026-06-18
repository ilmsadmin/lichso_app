"use client";

import { useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import { Coins, AlertTriangle } from "lucide-react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { SearchInput } from "@/components/shared/SearchInput";
import { Pagination } from "@/components/shared/Pagination";
import { useUserPointsList } from "@/hooks/usePointsAdmin";
import { DEFAULT_PAGE_SIZE, ROUTES } from "@/lib/constants";
import type { AdminPointsListParams } from "@/types/points";

// Sessions/day above this hints at past farming (matches backend maxScoringSessionsPerDay).
const SUSPICIOUS_SESSIONS_TODAY = 15;

const SORT_OPTIONS: { value: NonNullable<AdminPointsListParams["sort_by"]>; label: string }[] = [
  { value: "total", label: "Điểm tổng (quiz)" },
  { value: "week", label: "Điểm tuần" },
  { value: "month", label: "Điểm tháng" },
  { value: "balance", label: "Số dư ví" },
  { value: "lifetime", label: "Tích luỹ" },
  { value: "earned_today", label: "Kiếm hôm nay" },
  { value: "sessions_today", label: "Lượt chơi hôm nay" },
];

function fmt(n: number): string {
  return n.toLocaleString("vi-VN");
}

export default function AdminPointsPage() {
  const router = useRouter();
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);
  const [search, setSearch] = useState("");
  const [sortBy, setSortBy] = useState<NonNullable<AdminPointsListParams["sort_by"]>>("total");

  const { data, isLoading } = useUserPointsList({
    page,
    limit,
    search: search || undefined,
    sort_by: sortBy,
  });

  const rows = data?.data ?? [];
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
          <h1 className="flex items-center gap-2 text-2xl font-bold tracking-tight">
            <Coins className="h-6 w-6 text-amber-500" />
            Quản lý điểm thưởng
          </h1>
          <p className="text-muted-foreground">
            Theo dõi ví điểm, điểm quiz và phát hiện tài khoản cày điểm bất thường.
          </p>
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <SearchInput
          value={search}
          onChange={handleSearch}
          placeholder="Tìm theo tên hoặc email..."
          className="w-full sm:max-w-sm"
        />
        <Select
          value={sortBy}
          onValueChange={(v) => {
            setSortBy(v as NonNullable<AdminPointsListParams["sort_by"]>);
            setPage(1);
          }}
        >
          <SelectTrigger className="w-[200px]">
            <SelectValue placeholder="Sắp xếp" />
          </SelectTrigger>
          <SelectContent>
            {SORT_OPTIONS.map((o) => (
              <SelectItem key={o.value} value={o.value}>
                {o.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {/* Table */}
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
                  <TableHead>Người dùng</TableHead>
                  <TableHead className="text-right">Số dư ví</TableHead>
                  <TableHead className="text-right">Điểm tổng</TableHead>
                  <TableHead className="text-right">Tuần</TableHead>
                  <TableHead className="text-right">Tháng</TableHead>
                  <TableHead className="text-right">Streak</TableHead>
                  <TableHead className="text-right">Hôm nay</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {rows.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} className="text-muted-foreground py-10 text-center">
                      Không có dữ liệu
                    </TableCell>
                  </TableRow>
                ) : (
                  rows.map((r) => {
                    const suspicious = r.sessions_today >= SUSPICIOUS_SESSIONS_TODAY;
                    return (
                      <TableRow
                        key={r.user_id}
                        className="hover:bg-muted/50 cursor-pointer"
                        onClick={() => router.push(`${ROUTES.ADMIN_POINTS}/${r.user_id}`)}
                      >
                        <TableCell>
                          <div className="flex flex-col">
                            <span className="font-medium">
                              {r.display_name?.trim() || "(Không tên)"}
                            </span>
                            <span className="text-muted-foreground text-xs">{r.email}</span>
                          </div>
                        </TableCell>
                        <TableCell className="text-right font-medium">{fmt(r.balance)}</TableCell>
                        <TableCell className="text-right">{fmt(r.quiz_total_score)}</TableCell>
                        <TableCell className="text-right">{fmt(r.quiz_week_score)}</TableCell>
                        <TableCell className="text-right">{fmt(r.quiz_month_score)}</TableCell>
                        <TableCell className="text-right">{r.cur_streak}</TableCell>
                        <TableCell className="text-right">
                          <div className="flex items-center justify-end gap-2">
                            <span className="text-muted-foreground text-xs">
                              +{fmt(r.earned_today)}đ
                            </span>
                            {suspicious ? (
                              <Badge variant="destructive" className="gap-1">
                                <AlertTriangle className="h-3 w-3" />
                                {r.sessions_today} lượt
                              </Badge>
                            ) : (
                              <Badge variant="secondary">{r.sessions_today} lượt</Badge>
                            )}
                          </div>
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
              onLimitChange={handleLimitChange}
            />
          )}
        </>
      )}
    </div>
  );
}
