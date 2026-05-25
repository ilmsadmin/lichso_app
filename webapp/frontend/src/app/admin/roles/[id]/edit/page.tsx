"use client";

import { use } from "react";
import dynamic from "next/dynamic";
import { useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { getRole, updateRole } from "@/services/roleService";
import { getPermissionsGrouped } from "@/services/permissionService";
import { ROUTES } from "@/lib/constants";
import { toast } from "sonner";

// Lazy load heavy form component with permission matrix
const RoleForm = dynamic(
  () => import("@/components/roles/RoleForm").then((mod) => ({ default: mod.RoleForm })),
  {
    loading: () => (
      <div className="flex items-center justify-center py-12">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    ),
  }
);

export default function EditRolePage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const queryClient = useQueryClient();

  const { data: roleData, isLoading } = useQuery({
    queryKey: ["roles", id],
    queryFn: () => getRole(id),
    enabled: !!id,
  });

  const { data: permissionsData } = useQuery({
    queryKey: ["permissions", "grouped"],
    queryFn: getPermissionsGrouped,
  });

  const mutation = useMutation({
    mutationFn: (data: Parameters<typeof updateRole>[1]) => updateRole(id, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Role updated successfully");
        queryClient.invalidateQueries({ queryKey: ["roles"] });
        router.push(ROUTES.ADMIN_ROLES);
      }
    },
    onError: () => {
      toast.error("Failed to update role");
    },
  });

  const role = roleData?.data;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    );
  }

  if (!role) {
    return (
      <div className="flex flex-col items-center justify-center gap-4 py-24">
        <p className="text-muted-foreground">Role not found.</p>
        <Button variant="outline" onClick={() => router.push(ROUTES.ADMIN_ROLES)}>
          Back to Roles
        </Button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => router.push(ROUTES.ADMIN_ROLES)}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Edit Role</h1>
          <p className="text-muted-foreground">
            Update {role.display_name} role settings and permissions.
          </p>
        </div>
      </div>

      <RoleForm
        role={role}
        groupedPermissions={permissionsData?.data ?? []}
        isSubmitting={mutation.isPending}
        onCancel={() => router.push(ROUTES.ADMIN_ROLES)}
        onSubmit={(data) => {
          mutation.mutate({
            display_name: data.display_name,
            description: data.description,
            level: data.level,
            permission_ids: data.permission_ids,
          });
        }}
      />
    </div>
  );
}
