"use client";

import { useState, useCallback } from "react";
import { Download, Activity, Search, Filter, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
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
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Pagination } from "@/components/shared/Pagination";
import { useActivityLogs, useExportLogs } from "@/hooks/useDashboard";
import { DEFAULT_PAGE_SIZE } from "@/lib/constants";
import { formatDate } from "@/lib/utils";
import type { ActivityEntry, ActivityLogParams } from "@/types/dashboard";

// ============================================
// Constants
// ============================================

const ACTION_OPTIONS = [
  { value: "all", label: "Tất cả hành động" },
  { value: "create", label: "Tạo mới" },
  { value: "update", label: "Cập nhật" },
  { value: "delete", label: "Xóa" },
  { value: "login", label: "Đăng nhập" },
  { value: "logout", label: "Đăng xuất" },
  { value: "export", label: "Xuất dữ liệu" },
  { value: "import", label: "Nhập dữ liệu" },
];

const MODULE_OPTIONS = [
  { value: "all", label: "Tất cả module" },
  { value: "auth", label: "Xác thực" },
  { value: "users", label: "Người dùng" },
  { value: "articles", label: "Bài viết" },
  { value: "events", label: "Sự kiện" },
  { value: "famous_people", label: "Người nổi tiếng" },
  { value: "folk_festivals", label: "Lễ hội" },
  { value: "quotes", label: "Câu nói" },
  { value: "media", label: "Media" },
  { value: "settings", label: "Cài đặt" },
  { value: "roles", label: "Vai trò" },
  { value: "permissions", label: "Quyền hạn" },
];

const STATUS_OPTIONS = [
  { value: "all", label: "Tất cả trạng thái" },
  { value: "success", label: "Thành công" },
  { value: "failure", label: "Thất bại" },
  { value: "error", label: "Lỗi" },
];

function getStatusBadge(status: string) {
  switch (status) {
    case "success":
      return (
        <Badge className="bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400">
          Thành công
        </Badge>
      );
    case "failure":
      return <Badge variant="destructive">Thất bại</Badge>;
    case "error":
      return (
        <Badge
          variant="destructive"
          className="bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400"
        >
          Lỗi
        </Badge>
      );
    default:
      return <Badge variant="outline">{status}</Badge>;
  }
}

function getActionBadge(action: string) {
  switch (action) {
    case "create":
      return (
        <Badge className="bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400">
          Tạo mới
        </Badge>
      );
    case "update":
      return (
        <Badge className="bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400">
          Cập nhật
        </Badge>
      );
    case "delete":
      return (
        <Badge className="bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400">Xóa</Badge>
      );
    case "login":
      return (
        <Badge className="bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400">
          Đăng nhập
        </Badge>
      );
    case "logout":
      return <Badge variant="secondary">Đăng xuất</Badge>;
    case "export":
      return (
        <Badge className="bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400">
          Xuất
        </Badge>
      );
    case "import":
      return (
        <Badge className="bg-indigo-100 text-indigo-800 dark:bg-indigo-900/30 dark:text-indigo-400">
          Nhập
        </Badge>
      );
    default:
      return <Badge variant="outline">{action}</Badge>;
  }
}

// ============================================
// Component
// ============================================

export default function LogsPage() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);
  const [search, setSearch] = useState("");
  const [action, setAction] = useState("all");
  const [module, setModule] = useState("all");
  const [status, setStatus] = useState("all");
  const [selectedLog, setSelectedLog] = useState<ActivityEntry | null>(null);

  const params: ActivityLogParams = {
    page,
    limit,
    search: search || undefined,
    action: action !== "all" ? action : undefined,
    module: module !== "all" ? module : undefined,
    status: status !== "all" ? status : undefined,
  };

  const { data, isLoading, refetch } = useActivityLogs(params);
  const exportLogs = useExportLogs();

  const logs = data?.logs ?? [];
  const meta = data?.meta;

  const handleSearch = useCallback((value: string) => {
    setSearch(value);
    setPage(1);
  }, []);

  const handleExport = () => {
    exportLogs.mutate(params);
  };

  const handleReset = () => {
    setSearch("");
    setAction("all");
    setModule("all");
    setStatus("all");
    setPage(1);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Activity Logs</h1>
          <p className="text-muted-foreground">Theo dõi tất cả hoạt động trong hệ thống.</p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={() => refetch()}>
            <RefreshCw className="mr-2 h-4 w-4" />
            Làm mới
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={handleExport}
            disabled={exportLogs.isPending}
          >
            <Download className="mr-2 h-4 w-4" />
            {exportLogs.isPending ? "Đang xuất..." : "Xuất CSV"}
          </Button>
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader className="pb-3">
          <CardTitle className="flex items-center gap-2 text-base">
            <Filter className="h-4 w-4" />
            Bộ lọc
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            <div className="relative">
              <Search className="text-muted-foreground absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
              <Input
                placeholder="Tìm theo email, mô tả..."
                value={search}
                onChange={(e) => handleSearch(e.target.value)}
                className="pl-9"
              />
            </div>
            <Select
              value={action}
              onValueChange={(v) => {
                setAction(v);
                setPage(1);
              }}
            >
              <SelectTrigger>
                <SelectValue placeholder="Hành động" />
              </SelectTrigger>
              <SelectContent>
                {ACTION_OPTIONS.map((opt) => (
                  <SelectItem key={opt.value} value={opt.value}>
                    {opt.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select
              value={module}
              onValueChange={(v) => {
                setModule(v);
                setPage(1);
              }}
            >
              <SelectTrigger>
                <SelectValue placeholder="Module" />
              </SelectTrigger>
              <SelectContent>
                {MODULE_OPTIONS.map((opt) => (
                  <SelectItem key={opt.value} value={opt.value}>
                    {opt.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select
              value={status}
              onValueChange={(v) => {
                setStatus(v);
                setPage(1);
              }}
            >
              <SelectTrigger>
                <SelectValue placeholder="Trạng thái" />
              </SelectTrigger>
              <SelectContent>
                {STATUS_OPTIONS.map((opt) => (
                  <SelectItem key={opt.value} value={opt.value}>
                    {opt.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          {(search || action !== "all" || module !== "all" || status !== "all") && (
            <div className="mt-3 flex items-center justify-between">
              <p className="text-muted-foreground text-sm">{meta?.total ?? 0} kết quả</p>
              <Button variant="ghost" size="sm" onClick={handleReset}>
                Xóa bộ lọc
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Table */}
      <Card>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-[180px]">Thời gian</TableHead>
                  <TableHead className="w-[200px]">Người dùng</TableHead>
                  <TableHead className="w-[120px]">Hành động</TableHead>
                  <TableHead className="w-[120px]">Module</TableHead>
                  <TableHead>Mô tả</TableHead>
                  <TableHead className="w-[100px]">Trạng thái</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={6} className="h-32 text-center">
                      <div className="flex items-center justify-center">
                        <div className="border-primary h-6 w-6 animate-spin rounded-full border-2 border-t-transparent" />
                        <span className="text-muted-foreground ml-2">Đang tải...</span>
                      </div>
                    </TableCell>
                  </TableRow>
                ) : logs.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className="h-32 text-center">
                      <div className="flex flex-col items-center gap-2">
                        <Activity className="text-muted-foreground/50 h-8 w-8" />
                        <p className="text-muted-foreground">Không có dữ liệu</p>
                      </div>
                    </TableCell>
                  </TableRow>
                ) : (
                  logs.map((log) => (
                    <TableRow
                      key={log.id}
                      className="hover:bg-muted/50 cursor-pointer"
                      onClick={() => setSelectedLog(log)}
                    >
                      <TableCell className="text-muted-foreground text-sm whitespace-nowrap">
                        {formatDate(log.created_at)}
                      </TableCell>
                      <TableCell className="text-sm font-medium">{log.user_email}</TableCell>
                      <TableCell>{getActionBadge(log.action)}</TableCell>
                      <TableCell>
                        <Badge variant="outline" className="capitalize">
                          {log.module}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-muted-foreground max-w-[300px] truncate text-sm">
                        {log.description}
                      </TableCell>
                      <TableCell>{getStatusBadge(log.status)}</TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>

      {/* Pagination */}
      {meta && meta.total_pages > 1 && (
        <Pagination
          page={page}
          limit={limit}
          total={meta.total}
          totalPages={meta.total_pages}
          onPageChange={setPage}
          onLimitChange={setLimit}
        />
      )}

      {/* Detail Dialog */}
      <Dialog open={!!selectedLog} onOpenChange={(open) => !open && setSelectedLog(null)}>
        <DialogContent className="sm:max-w-lg">
          <DialogHeader>
            <DialogTitle>Chi tiết Activity Log</DialogTitle>
          </DialogHeader>
          {selectedLog && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <p className="text-muted-foreground font-medium">Thời gian</p>
                  <p>{formatDate(selectedLog.created_at)}</p>
                </div>
                <div>
                  <p className="text-muted-foreground font-medium">Người dùng</p>
                  <p>{selectedLog.user_email}</p>
                </div>
                <div>
                  <p className="text-muted-foreground font-medium">Hành động</p>
                  {getActionBadge(selectedLog.action)}
                </div>
                <div>
                  <p className="text-muted-foreground font-medium">Module</p>
                  <Badge variant="outline" className="capitalize">
                    {selectedLog.module}
                  </Badge>
                </div>
                <div>
                  <p className="text-muted-foreground font-medium">Trạng thái</p>
                  {getStatusBadge(selectedLog.status)}
                </div>
                {selectedLog.ip_address && (
                  <div>
                    <p className="text-muted-foreground font-medium">IP Address</p>
                    <p className="font-mono text-xs">{selectedLog.ip_address}</p>
                  </div>
                )}
              </div>

              <div>
                <p className="text-muted-foreground mb-1 text-sm font-medium">Mô tả</p>
                <p className="bg-muted/50 rounded-lg p-3 text-sm">{selectedLog.description}</p>
              </div>

              {selectedLog.user_agent && (
                <div>
                  <p className="text-muted-foreground mb-1 text-sm font-medium">User Agent</p>
                  <p className="bg-muted/50 rounded-lg p-3 font-mono text-xs break-all">
                    {selectedLog.user_agent}
                  </p>
                </div>
              )}

              {selectedLog.metadata && Object.keys(selectedLog.metadata).length > 0 && (
                <div>
                  <p className="text-muted-foreground mb-1 text-sm font-medium">Metadata</p>
                  <pre className="bg-muted/50 overflow-x-auto rounded-lg p-3 text-xs">
                    {JSON.stringify(selectedLog.metadata, null, 2)}
                  </pre>
                </div>
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
