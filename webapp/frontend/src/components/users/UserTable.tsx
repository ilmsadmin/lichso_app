"use client";

import { useState } from "react";
import Link from "next/link";
import { MoreHorizontal, Pencil, Trash2, Eye, Shield, Power } from "lucide-react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { usePermission } from "@/hooks/usePermission";
import { formatDate, getInitials } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import type { User } from "@/types/user";

interface UserTableProps {
  users: User[];
  onDelete: (id: string) => void;
  onToggleStatus: (id: string, isActive: boolean) => void;
  isDeleting?: boolean;
  isToggling?: boolean;
}

export function UserTable({
  users,
  onDelete,
  onToggleStatus,
  isDeleting,
  isToggling,
}: UserTableProps) {
  const { can } = usePermission();
  const [deleteUser, setDeleteUser] = useState<User | null>(null);
  const [toggleUser, setToggleUser] = useState<User | null>(null);

  return (
    <>
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[300px]">User</TableHead>
              <TableHead>Roles</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Last Login</TableHead>
              <TableHead>Created</TableHead>
              <TableHead className="w-[70px]" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {users.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-muted-foreground h-24 text-center">
                  No users found.
                </TableCell>
              </TableRow>
            ) : (
              users.map((user) => (
                <TableRow key={user.id}>
                  <TableCell>
                    <div className="flex items-center gap-3">
                      <Avatar className="h-9 w-9">
                        <AvatarFallback className="bg-primary/10 text-primary text-xs">
                          {getInitials(user.full_name || user.email)}
                        </AvatarFallback>
                      </Avatar>
                      <div className="min-w-0">
                        <p className="truncate text-sm font-medium">
                          {user.full_name || `${user.first_name} ${user.last_name}`}
                        </p>
                        <p className="text-muted-foreground truncate text-xs">{user.email}</p>
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex flex-wrap gap-1">
                      {user.roles?.map((role) => (
                        <Badge
                          key={role.id}
                          variant={
                            role.name === "super_admin"
                              ? "destructive"
                              : role.name === "admin"
                                ? "default"
                                : "secondary"
                          }
                          className="text-xs"
                        >
                          {role.display_name}
                        </Badge>
                      ))}
                      {(!user.roles || user.roles.length === 0) && (
                        <span className="text-muted-foreground text-xs">No roles</span>
                      )}
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge
                      variant={user.is_active ? "default" : "secondary"}
                      className={
                        user.is_active
                          ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400"
                          : "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400"
                      }
                    >
                      {user.is_active ? "Active" : "Inactive"}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-muted-foreground text-sm">
                    {user.last_login ? formatDate(user.last_login) : "Never"}
                  </TableCell>
                  <TableCell className="text-muted-foreground text-sm">
                    {formatDate(user.created_at)}
                  </TableCell>
                  <TableCell>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon" className="h-8 w-8">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        {can("users.read") && (
                          <DropdownMenuItem asChild>
                            <Link href={`${ROUTES.ADMIN_USERS}/${user.id}`}>
                              <Eye className="mr-2 h-4 w-4" />
                              View Details
                            </Link>
                          </DropdownMenuItem>
                        )}
                        {can("users.update") && (
                          <>
                            <DropdownMenuItem asChild>
                              <Link href={`${ROUTES.ADMIN_USERS}/${user.id}/edit`}>
                                <Pencil className="mr-2 h-4 w-4" />
                                Edit
                              </Link>
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => setToggleUser(user)}>
                              <Power className="mr-2 h-4 w-4" />
                              {user.is_active ? "Deactivate" : "Activate"}
                            </DropdownMenuItem>
                            <DropdownMenuItem asChild>
                              <Link href={`${ROUTES.ADMIN_USERS}/${user.id}/edit?tab=roles`}>
                                <Shield className="mr-2 h-4 w-4" />
                                Manage Roles
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
                              <Trash2 className="mr-2 h-4 w-4" />
                              Delete
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

      {/* Delete confirmation */}
      <ConfirmDialog
        open={!!deleteUser}
        onOpenChange={() => setDeleteUser(null)}
        title="Delete User"
        description={`Are you sure you want to delete "${deleteUser?.full_name || deleteUser?.email}"? This action cannot be undone.`}
        confirmText="Delete"
        variant="destructive"
        loading={isDeleting}
        onConfirm={() => {
          if (deleteUser) {
            onDelete(deleteUser.id);
            setDeleteUser(null);
          }
        }}
      />

      {/* Toggle status confirmation */}
      <ConfirmDialog
        open={!!toggleUser}
        onOpenChange={() => setToggleUser(null)}
        title={toggleUser?.is_active ? "Deactivate User" : "Activate User"}
        description={
          toggleUser?.is_active
            ? `Are you sure you want to deactivate "${toggleUser?.full_name}"? They will no longer be able to log in.`
            : `Are you sure you want to activate "${toggleUser?.full_name}"?`
        }
        confirmText={toggleUser?.is_active ? "Deactivate" : "Activate"}
        variant={toggleUser?.is_active ? "destructive" : "default"}
        loading={isToggling}
        onConfirm={() => {
          if (toggleUser) {
            onToggleStatus(toggleUser.id, !toggleUser.is_active);
            setToggleUser(null);
          }
        }}
      />
    </>
  );
}
