"use client";

import dynamic from "next/dynamic";
import Link from "next/link";
import { Plus } from "lucide-react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { PermissionGate } from "@/components/auth/PermissionGate";
import { getRoles, deleteRole } from "@/services/roleService";
import { ROUTES } from "@/lib/constants";
import { toast } from "sonner";

// Lazy load heavy table component
const RoleTable = dynamic(
  () => import("@/components/roles/RoleTable").then((mod) => ({ default: mod.RoleTable })),
  {
    loading: () => (
      <div className="flex items-center justify-center py-12">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    ),
  }
);

export default function RolesPage() {
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ["roles"],
    queryFn: getRoles,
  });

  const deleteMutation = useMutation({
    mutationFn: deleteRole,
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Role deleted successfully");
        queryClient.invalidateQueries({ queryKey: ["roles"] });
      }
    },
    onError: () => {
      toast.error("Failed to delete role");
    },
  });

  const roles = data?.data ?? [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Roles</h1>
          <p className="text-muted-foreground">Manage roles and their permissions.</p>
        </div>
        <PermissionGate permission="roles.create">
          <Button size="sm" asChild>
            <Link href={`${ROUTES.ADMIN_ROLES}/create`}>
              <Plus className="mr-2 h-4 w-4" />
              Add Role
            </Link>
          </Button>
        </PermissionGate>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <div className="flex flex-col items-center gap-3">
            <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
            <p className="text-muted-foreground text-sm">Loading roles...</p>
          </div>
        </div>
      ) : (
        <RoleTable
          roles={roles}
          onDelete={(id) => deleteMutation.mutate(id)}
          isDeleting={deleteMutation.isPending}
        />
      )}
    </div>
  );
}
