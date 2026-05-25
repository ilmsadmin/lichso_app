"use client";

import { use } from "react";
import dynamic from "next/dynamic";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useUser, useUpdateUser, useSetUserRoles } from "@/hooks/useUsers";
import { getRoles } from "@/services/roleService";
import { ROUTES } from "@/lib/constants";

// Lazy load heavy form component
const UserForm = dynamic(
  () => import("@/components/users/UserForm").then((mod) => ({ default: mod.UserForm })),
  {
    loading: () => (
      <div className="flex items-center justify-center py-12">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    ),
  }
);

export default function EditUserPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const { data: userData, isLoading } = useUser(id);
  const updateUser = useUpdateUser(id);
  const setUserRoles = useSetUserRoles(id);

  const { data: rolesData } = useQuery({
    queryKey: ["roles"],
    queryFn: getRoles,
  });

  const user = userData?.data;
  const roles = rolesData?.data ?? [];

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
      <div className="flex items-center gap-4">
        <Button
          variant="ghost"
          size="icon"
          onClick={() => router.push(`${ROUTES.ADMIN_USERS}/${id}`)}
        >
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Edit User</h1>
          <p className="text-muted-foreground">
            Update details for {user.full_name || user.email}.
          </p>
        </div>
      </div>

      <UserForm
        user={user}
        roles={roles}
        isSubmitting={updateUser.isPending || setUserRoles.isPending}
        onCancel={() => router.push(`${ROUTES.ADMIN_USERS}/${id}`)}
        onSubmit={(data) => {
          // Build update payload (only include non-empty fields)
          const updateData: Record<string, unknown> = {
            email: data.email,
            first_name: data.first_name,
            last_name: data.last_name,
            phone: data.phone || "",
            is_active: data.is_active,
          };

          if (data.password) {
            updateData.password = data.password;
          }

          // Update user details
          updateUser.mutate(updateData, {
            onSuccess: (response) => {
              if (response.success) {
                // Also update roles if changed
                const currentRoleIds = user.roles?.map((r) => r.id).sort() ?? [];
                const newRoleIds = (data.role_ids ?? []).sort();
                const rolesChanged = JSON.stringify(currentRoleIds) !== JSON.stringify(newRoleIds);

                if (rolesChanged) {
                  setUserRoles.mutate(
                    { role_ids: data.role_ids ?? [] },
                    {
                      onSuccess: () => {
                        router.push(`${ROUTES.ADMIN_USERS}/${id}`);
                      },
                    }
                  );
                } else {
                  router.push(`${ROUTES.ADMIN_USERS}/${id}`);
                }
              }
            },
          });
        }}
      />
    </div>
  );
}
