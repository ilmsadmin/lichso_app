"use client";

import dynamic from "next/dynamic";
import { useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { createRole } from "@/services/roleService";
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

export default function CreateRolePage() {
  const router = useRouter();
  const queryClient = useQueryClient();

  const { data: permissionsData } = useQuery({
    queryKey: ["permissions", "grouped"],
    queryFn: getPermissionsGrouped,
  });

  const mutation = useMutation({
    mutationFn: createRole,
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Role created successfully");
        queryClient.invalidateQueries({ queryKey: ["roles"] });
        router.push(ROUTES.ADMIN_ROLES);
      }
    },
    onError: () => {
      toast.error("Failed to create role");
    },
  });

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => router.push(ROUTES.ADMIN_ROLES)}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Create Role</h1>
          <p className="text-muted-foreground">Define a new role with specific permissions.</p>
        </div>
      </div>

      <RoleForm
        groupedPermissions={permissionsData?.data ?? []}
        isSubmitting={mutation.isPending}
        onCancel={() => router.push(ROUTES.ADMIN_ROLES)}
        onSubmit={(data) => {
          mutation.mutate({
            name: data.name,
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
