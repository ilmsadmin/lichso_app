"use client";

import { use } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Pencil, Shield, Power, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { PermissionGate } from "@/components/auth/PermissionGate";
import { useUser, useDeleteUser, useToggleUserStatus } from "@/hooks/useUsers";
import { formatDate, getInitials } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import { useState } from "react";

export default function UserDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const { data, isLoading } = useUser(id);
  const deleteUser = useDeleteUser();
  const toggleStatus = useToggleUserStatus();
  const [showDelete, setShowDelete] = useState(false);
  const [showToggle, setShowToggle] = useState(false);

  const user = data?.data;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    );
  }

  if (!user) {
    return (
      <div className="flex flex-col items-center justify-center gap-4 py-24">
        <p className="text-muted-foreground">User not found.</p>
        <Button variant="outline" onClick={() => router.push(ROUTES.ADMIN_USERS)}>
          Back to Users
        </Button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => router.push(ROUTES.ADMIN_USERS)}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h1 className="text-2xl font-bold tracking-tight">User Details</h1>
            <p className="text-muted-foreground">{user.email}</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <PermissionGate permission="users.update">
            <Button variant="outline" size="sm" asChild>
              <Link href={`${ROUTES.ADMIN_USERS}/${user.id}/edit`}>
                <Pencil className="mr-2 h-4 w-4" />
                Edit
              </Link>
            </Button>
            <Button variant="outline" size="sm" onClick={() => setShowToggle(true)}>
              <Power className="mr-2 h-4 w-4" />
              {user.is_active ? "Deactivate" : "Activate"}
            </Button>
          </PermissionGate>
          <PermissionGate permission="users.delete">
            <Button
              variant="outline"
              size="sm"
              className="text-destructive hover:text-destructive"
              onClick={() => setShowDelete(true)}
            >
              <Trash2 className="mr-2 h-4 w-4" />
              Delete
            </Button>
          </PermissionGate>
        </div>
      </div>

      {/* Profile Card */}
      <Card>
        <CardHeader>
          <div className="flex items-center gap-4">
            <Avatar className="h-16 w-16">
              <AvatarFallback className="bg-primary/10 text-primary text-lg">
                {getInitials(user.full_name || user.email)}
              </AvatarFallback>
            </Avatar>
            <div>
              <CardTitle className="text-xl">
                {user.full_name || `${user.first_name} ${user.last_name}`}
              </CardTitle>
              <CardDescription>{user.email}</CardDescription>
            </div>
            <Badge
              className={`ml-auto ${
                user.is_active
                  ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400"
                  : "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400"
              }`}
            >
              {user.is_active ? "Active" : "Inactive"}
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <p className="text-muted-foreground text-sm font-medium">First Name</p>
              <p className="text-sm">{user.first_name}</p>
            </div>
            <div>
              <p className="text-muted-foreground text-sm font-medium">Last Name</p>
              <p className="text-sm">{user.last_name}</p>
            </div>
            <div>
              <p className="text-muted-foreground text-sm font-medium">Phone</p>
              <p className="text-sm">{user.phone || "—"}</p>
            </div>
            <div>
              <p className="text-muted-foreground text-sm font-medium">Last Login</p>
              <p className="text-sm">{user.last_login ? formatDate(user.last_login) : "Never"}</p>
            </div>
            <div>
              <p className="text-muted-foreground text-sm font-medium">Created</p>
              <p className="text-sm">{formatDate(user.created_at)}</p>
            </div>
            <div>
              <p className="text-muted-foreground text-sm font-medium">Updated</p>
              <p className="text-sm">{formatDate(user.updated_at)}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Roles Card */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle className="text-lg">Roles</CardTitle>
            <CardDescription>
              Roles assigned to this user determine their permissions.
            </CardDescription>
          </div>
          <PermissionGate permission="users.update">
            <Button variant="outline" size="sm" asChild>
              <Link href={`${ROUTES.ADMIN_USERS}/${user.id}/edit?tab=roles`}>
                <Shield className="mr-2 h-4 w-4" />
                Manage
              </Link>
            </Button>
          </PermissionGate>
        </CardHeader>
        <Separator />
        <CardContent className="pt-4">
          {user.roles && user.roles.length > 0 ? (
            <div className="flex flex-wrap gap-2">
              {user.roles.map((role) => (
                <Badge
                  key={role.id}
                  variant={
                    role.name === "super_admin"
                      ? "destructive"
                      : role.name === "admin"
                        ? "default"
                        : "secondary"
                  }
                >
                  {role.display_name}
                </Badge>
              ))}
            </div>
          ) : (
            <p className="text-muted-foreground text-sm">No roles assigned.</p>
          )}
        </CardContent>
      </Card>

      {/* Dialogs */}
      <ConfirmDialog
        open={showDelete}
        onOpenChange={setShowDelete}
        title="Delete User"
        description={`Are you sure you want to delete "${user.full_name || user.email}"? This action cannot be undone.`}
        confirmText="Delete"
        variant="destructive"
        loading={deleteUser.isPending}
        onConfirm={() => {
          deleteUser.mutate(user.id, {
            onSuccess: () => {
              router.push(ROUTES.ADMIN_USERS);
            },
          });
        }}
      />

      <ConfirmDialog
        open={showToggle}
        onOpenChange={setShowToggle}
        title={user.is_active ? "Deactivate User" : "Activate User"}
        description={
          user.is_active
            ? `Deactivating "${user.full_name}" will prevent them from logging in.`
            : `Activating "${user.full_name}" will allow them to log in.`
        }
        confirmText={user.is_active ? "Deactivate" : "Activate"}
        variant={user.is_active ? "destructive" : "default"}
        loading={toggleStatus.isPending}
        onConfirm={() => {
          toggleStatus.mutate(
            { id: user.id, isActive: !user.is_active },
            {
              onSuccess: () => setShowToggle(false),
            }
          );
        }}
      />
    </div>
  );
}
