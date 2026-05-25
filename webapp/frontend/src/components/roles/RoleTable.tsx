"use client";

import { useState } from "react";
import Link from "next/link";
import { MoreHorizontal, Pencil, Trash2, Shield } from "lucide-react";
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
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { usePermission } from "@/hooks/usePermission";
import { formatDate } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import type { Role } from "@/types/role";

interface RoleTableProps {
  roles: Role[];
  onDelete: (id: string) => void;
  isDeleting?: boolean;
}

export function RoleTable({ roles, onDelete, isDeleting }: RoleTableProps) {
  const { can } = usePermission();
  const [deleteRole, setDeleteRole] = useState<Role | null>(null);

  return (
    <>
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Role</TableHead>
              <TableHead>Level</TableHead>
              <TableHead>Permissions</TableHead>
              <TableHead>Users</TableHead>
              <TableHead>Created</TableHead>
              <TableHead className="w-[70px]" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {roles.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-muted-foreground h-24 text-center">
                  No roles found.
                </TableCell>
              </TableRow>
            ) : (
              roles.map((role) => (
                <TableRow key={role.id}>
                  <TableCell>
                    <div>
                      <div className="flex items-center gap-2">
                        <p className="text-sm font-medium">{role.display_name}</p>
                        {role.is_system && (
                          <Badge variant="outline" className="text-xs">
                            System
                          </Badge>
                        )}
                      </div>
                      <p className="text-muted-foreground text-xs">{role.name}</p>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant="secondary">{role.level}</Badge>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1.5">
                      <Shield className="text-muted-foreground h-3.5 w-3.5" />
                      <span className="text-sm">{role.permissions?.length ?? 0}</span>
                    </div>
                  </TableCell>
                  <TableCell className="text-sm">{role.user_count ?? 0}</TableCell>
                  <TableCell className="text-muted-foreground text-sm">
                    {formatDate(role.created_at)}
                  </TableCell>
                  <TableCell>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon" className="h-8 w-8">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        {can("roles.update") && !role.is_system && (
                          <DropdownMenuItem asChild>
                            <Link href={`${ROUTES.ADMIN_ROLES}/${role.id}/edit`}>
                              <Pencil className="mr-2 h-4 w-4" />
                              Edit
                            </Link>
                          </DropdownMenuItem>
                        )}
                        {can("roles.delete") && !role.is_system && (
                          <>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                              onClick={() => setDeleteRole(role)}
                              className="text-destructive focus:text-destructive"
                            >
                              <Trash2 className="mr-2 h-4 w-4" />
                              Delete
                            </DropdownMenuItem>
                          </>
                        )}
                        {role.is_system && (
                          <DropdownMenuItem disabled>
                            <Shield className="mr-2 h-4 w-4" />
                            System role (read-only)
                          </DropdownMenuItem>
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

      <ConfirmDialog
        open={!!deleteRole}
        onOpenChange={() => setDeleteRole(null)}
        title="Delete Role"
        description={`Are you sure you want to delete the role "${deleteRole?.display_name}"? Users assigned this role will lose its permissions.`}
        confirmText="Delete"
        variant="destructive"
        loading={isDeleting}
        onConfirm={() => {
          if (deleteRole) {
            onDelete(deleteRole.id);
            setDeleteRole(null);
          }
        }}
      />
    </>
  );
}
