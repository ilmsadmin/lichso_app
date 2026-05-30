"use client";

import { useState, useCallback } from "react";
import dynamic from "next/dynamic";
import Link from "next/link";
import { Plus, Download } from "lucide-react";
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
import { useUsers, useDeleteUser, useToggleUserStatus, useExportUsers } from "@/hooks/useUsers";
import { DEFAULT_PAGE_SIZE, ROUTES } from "@/lib/constants";

// Lazy load heavy table component
const UserTable = dynamic(
  () => import("@/components/users/UserTable").then((mod) => ({ default: mod.UserTable })),
  {
    loading: () => (
      <div className="flex items-center justify-center py-12">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    ),
  }
);

export default function UsersPage() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState<string>("all");
  const [role, setRole] = useState<string>("all");
  const [hasDevice, setHasDevice] = useState<string>("all");

  const { data, isLoading } = useUsers({
    page,
    limit,
    search: search || undefined,
    status: status !== "all" ? status : undefined,
    role: role !== "all" ? role : undefined,
    has_device: hasDevice !== "all" ? hasDevice : undefined,
  });

  const deleteUser = useDeleteUser();
  const toggleStatus = useToggleUserStatus();
  const exportUsers = useExportUsers();

  const users = data?.data ?? [];
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
          <h1 className="text-2xl font-bold tracking-tight">Users</h1>
          <p className="text-muted-foreground">Manage user accounts and their access.</p>
        </div>
        <div className="flex items-center gap-2">
          <PermissionGate permission="users.export">
            <Button
              variant="outline"
              size="sm"
              onClick={() =>
                exportUsers.mutate({
                  search: search || undefined,
                  status: status !== "all" ? status : undefined,
                  role: role !== "all" ? role : undefined,
                  has_device: hasDevice !== "all" ? hasDevice : undefined,
                })
              }
              disabled={exportUsers.isPending}
            >
              <Download className="mr-2 h-4 w-4" />
              Export
            </Button>
          </PermissionGate>
          <PermissionGate permission="users.create">
            <Button size="sm" asChild>
              <Link href={`${ROUTES.ADMIN_USERS}/create`}>
                <Plus className="mr-2 h-4 w-4" />
                Add User
              </Link>
            </Button>
          </PermissionGate>
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <SearchInput
          value={search}
          onChange={handleSearch}
          placeholder="Search users by name or email..."
          className="w-full sm:max-w-sm"
        />
        <Select
          value={status}
          onValueChange={(v) => {
            setStatus(v);
            setPage(1);
          }}
        >
          <SelectTrigger className="w-[140px]">
            <SelectValue placeholder="Status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Status</SelectItem>
            <SelectItem value="active">Active</SelectItem>
            <SelectItem value="inactive">Inactive</SelectItem>
          </SelectContent>
        </Select>
        <Select
          value={role}
          onValueChange={(v) => {
            setRole(v);
            setPage(1);
          }}
        >
          <SelectTrigger className="w-[160px]">
            <SelectValue placeholder="Role" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Roles</SelectItem>
            <SelectItem value="super_admin">Super Admin</SelectItem>
            <SelectItem value="admin">Admin</SelectItem>
            <SelectItem value="editor">Editor</SelectItem>
            <SelectItem value="viewer">Viewer</SelectItem>
          </SelectContent>
        </Select>
        <Select
          value={hasDevice}
          onValueChange={(v) => {
            setHasDevice(v);
            setPage(1);
          }}
        >
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Thiết bị" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            <SelectItem value="yes">Có thiết bị đăng ký</SelectItem>
            <SelectItem value="no">Chưa có thiết bị</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Table */}
      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <div className="flex flex-col items-center gap-3">
            <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
            <p className="text-muted-foreground text-sm">Loading users...</p>
          </div>
        </div>
      ) : (
        <>
          <UserTable
            users={users}
            onDelete={(id) => deleteUser.mutate(id)}
            onToggleStatus={(id, isActive) => toggleStatus.mutate({ id, isActive })}
            isDeleting={deleteUser.isPending}
            isToggling={toggleStatus.isPending}
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
