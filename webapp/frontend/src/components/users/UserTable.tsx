"use client";

import { useState } from "react";
import Link from "next/link";
import {
  MoreHorizontal, Pencil, Trash2, Power, Shield, Smartphone,
} from "lucide-react";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem,
  DropdownMenuSeparator, DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { UserDetailDrawer } from "./UserDetailDrawer";
import { usePermission } from "@/hooks/usePermission";
import { formatDate, getInitials } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import type { User } from "@/types/user";
import { formatDistanceToNow } from "date-fns";
import { vi } from "date-fns/locale";

interface UserTableProps {
  users: User[];
  onDelete: (id: string) => void;
  onToggleStatus: (id: string, isActive: boolean) => void;
  isDeleting?: boolean;
  isToggling?: boolean;
}

function ProviderBadge({ provider }: { provider: string }) {
  if (provider === "google")
    return <Badge variant="outline" className="text-xs gap-1 py-0 h-5">🔵 Google</Badge>;
  return <Badge variant="outline" className="text-xs gap-1 py-0 h-5">🔑 Local</Badge>;
}

function DeviceCell({ count, platforms, version }: { count: number; platforms: string; version: string }) {
  if (count === 0) return <span className="text-muted-foreground text-xs">—</span>;
  const platformList = platforms.split(",").filter(Boolean);
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <div className="flex items-center gap-1 cursor-default">
          <Smartphone className="w-3.5 h-3.5 text-muted-foreground" />
          <span className="text-xs font-medium">{count}</span>
          {platformList.map((p) => (
            <span key={p} className="text-xs">{p === "android" ? "🤖" : p === "ios" ? "🍎" : "🌐"}</span>
          ))}
        </div>
      </TooltipTrigger>
      <TooltipContent>
        <p className="text-xs">{count} thiết bị {version ? `• v${version}` : ""}</p>
      </TooltipContent>
    </Tooltip>
  );
}

export function UserTable({ users, onDelete, onToggleStatus, isDeleting, isToggling }: UserTableProps) {
  const { can } = usePermission();
  const [deleteUser, setDeleteUser] = useState<User | null>(null);
  const [toggleUser, setToggleUser] = useState<User | null>(null);
  const [detailUser, setDetailUser] = useState<User | null>(null);

  return (
    <>
      <div className="rounded-md border overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/30">
              <TableHead className="w-[240px]">Người dùng</TableHead>
              <TableHead className="w-[90px]">Provider</TableHead>
              <TableHead className="w-[120px]">Điện thoại</TableHead>
              <TableHead>Roles</TableHead>
              <TableHead className="w-[90px] text-center">Thiết bị</TableHead>
              <TableHead className="w-[80px]">Trạng thái</TableHead>
              <TableHead className="w-[130px]">Đăng nhập cuối</TableHead>
              <TableHead className="w-[100px]">Ngày tạo</TableHead>
              <TableHead className="w-[50px]" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {users.length === 0 ? (
              <TableRow>
                <TableCell colSpan={9} className="text-muted-foreground h-24 text-center">
                  Không tìm thấy người dùng nào.
                </TableCell>
              </TableRow>
            ) : (
              users.map((user) => (
                <TableRow
                  key={user.id}
                  className="cursor-pointer hover:bg-muted/40 transition-colors"
                  onClick={() => setDetailUser(user)}
                >
                  {/* Avatar + tên + email */}
                  <TableCell onClick={(e) => e.stopPropagation()}>
                    <div
                      className="flex items-center gap-2.5 cursor-pointer"
                      onClick={() => setDetailUser(user)}
                    >
                      <Avatar className="h-8 w-8 shrink-0">
                        <AvatarImage src={user.avatar} />
                        <AvatarFallback className="bg-primary/10 text-primary text-xs">
                          {getInitials(user.full_name || user.email)}
                        </AvatarFallback>
                      </Avatar>
                      <div className="min-w-0">
                        <p className="text-sm font-medium truncate leading-tight">
                          {user.full_name || `${user.first_name} ${user.last_name}`.trim() || "—"}
                        </p>
                        <p className="text-xs text-muted-foreground truncate">{user.email}</p>
                        <Tooltip>
                          <TooltipTrigger asChild>
                            <p className="text-[10px] text-muted-foreground/60 font-mono truncate">
                              {user.id.split("-")[0]}…
                            </p>
                          </TooltipTrigger>
                          <TooltipContent>
                            <p className="font-mono text-xs">{user.id}</p>
                          </TooltipContent>
                        </Tooltip>
                      </div>
                    </div>
                  </TableCell>

                  {/* Provider */}
                  <TableCell onClick={(e) => e.stopPropagation()}>
                    <ProviderBadge provider={user.provider} />
                  </TableCell>

                  {/* Điện thoại */}
                  <TableCell>
                    <span className="text-xs font-mono">{user.phone || "—"}</span>
                  </TableCell>

                  {/* Roles */}
                  <TableCell onClick={(e) => e.stopPropagation()}>
                    <div className="flex flex-wrap gap-1">
                      {user.roles?.length > 0 ? (
                        user.roles.map((role) => (
                          <Badge
                            key={role.id}
                            variant={
                              role.name === "super_admin" ? "destructive"
                                : role.name === "admin" ? "default"
                                : "secondary"
                            }
                            className="text-xs py-0 h-5"
                          >
                            {role.display_name}
                          </Badge>
                        ))
                      ) : (
                        <span className="text-muted-foreground text-xs">—</span>
                      )}
                    </div>
                  </TableCell>

                  {/* Thiết bị */}
                  <TableCell className="text-center" onClick={(e) => e.stopPropagation()}>
                    <DeviceCell
                      count={user.device_count ?? 0}
                      platforms={user.platforms ?? ""}
                      version={user.latest_version ?? ""}
                    />
                  </TableCell>

                  {/* Trạng thái */}
                  <TableCell onClick={(e) => e.stopPropagation()}>
                    <Badge
                      variant={user.is_active ? "default" : "secondary"}
                      className={`text-xs py-0 h-5 ${user.is_active
                        ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400"
                        : "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400"}`}
                    >
                      {user.is_active ? "Active" : "Inactive"}
                    </Badge>
                  </TableCell>

                  {/* Đăng nhập cuối */}
                  <TableCell>
                    <span className="text-xs text-muted-foreground">
                      {user.last_login
                        ? formatDistanceToNow(new Date(user.last_login), { addSuffix: true, locale: vi })
                        : "Chưa đăng nhập"}
                    </span>
                  </TableCell>

                  {/* Ngày tạo */}
                  <TableCell>
                    <span className="text-xs text-muted-foreground">{formatDate(user.created_at)}</span>
                  </TableCell>

                  {/* Actions */}
                  <TableCell onClick={(e) => e.stopPropagation()}>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon" className="h-7 w-7">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end" className="text-sm">
                        <DropdownMenuItem onClick={() => setDetailUser(user)}>
                          <Smartphone className="mr-2 h-3.5 w-3.5" />
                          Xem chi tiết
                        </DropdownMenuItem>
                        {can("users.update") && (
                          <>
                            <DropdownMenuItem asChild>
                              <Link href={`${ROUTES.ADMIN_USERS}/${user.id}/edit`}>
                                <Pencil className="mr-2 h-3.5 w-3.5" />
                                Chỉnh sửa
                              </Link>
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => setToggleUser(user)}>
                              <Power className="mr-2 h-3.5 w-3.5" />
                              {user.is_active ? "Vô hiệu hóa" : "Kích hoạt"}
                            </DropdownMenuItem>
                            <DropdownMenuItem asChild>
                              <Link href={`${ROUTES.ADMIN_USERS}/${user.id}/edit?tab=roles`}>
                                <Shield className="mr-2 h-3.5 w-3.5" />
                                Phân quyền
                              </Link>
                            </DropdownMenuItem>
                          </>
                        )}
                        {can("users.delete") && (
                          <>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                              onClick={() => setDeleteUser(user)}
                              className="text-destructive focus:text-destructive"
                            >
                              <Trash2 className="mr-2 h-3.5 w-3.5" />
                              Xóa
                            </DropdownMenuItem>
                          </>
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* Detail drawer */}
      <UserDetailDrawer
        user={detailUser}
        open={!!detailUser}
        onOpenChange={(open) => !open && setDetailUser(null)}
      />

      {/* Delete confirmation */}
      <ConfirmDialog
        open={!!deleteUser}
        onOpenChange={() => setDeleteUser(null)}
        title="Xóa người dùng"
        description={`Bạn có chắc muốn xóa "${deleteUser?.full_name || deleteUser?.email}"? Thao tác này không thể hoàn tác.`}
        confirmText="Xóa"
        variant="destructive"
        loading={isDeleting}
        onConfirm={() => {
          if (deleteUser) { onDelete(deleteUser.id); setDeleteUser(null); }
        }}
      />

      {/* Toggle status confirmation */}
      <ConfirmDialog
        open={!!toggleUser}
        onOpenChange={() => setToggleUser(null)}
        title={toggleUser?.is_active ? "Vô hiệu hóa" : "Kích hoạt"}
        description={
          toggleUser?.is_active
            ? `"${toggleUser?.full_name}" sẽ không thể đăng nhập nữa.`
            : `Kích hoạt lại tài khoản "${toggleUser?.full_name}"?`
        }
        confirmText={toggleUser?.is_active ? "Vô hiệu hóa" : "Kích hoạt"}
        variant={toggleUser?.is_active ? "destructive" : "default"}
        loading={isToggling}
        onConfirm={() => {
          if (toggleUser) { onToggleStatus(toggleUser.id, !toggleUser.is_active); setToggleUser(null); }
        }}
      />
    </>
  );
}
