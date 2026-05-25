"use client";

import dynamic from "next/dynamic";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useCreateUser } from "@/hooks/useUsers";
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

export default function CreateUserPage() {
  const router = useRouter();
  const createUser = useCreateUser();

  const { data: rolesData } = useQuery({
    queryKey: ["roles"],
    queryFn: getRoles,
  });

  const roles = rolesData?.data ?? [];

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => router.push(ROUTES.ADMIN_USERS)}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Create User</h1>
          <p className="text-muted-foreground">Add a new user account to the system.</p>
        </div>
      </div>

      <UserForm
        roles={roles}
        isSubmitting={createUser.isPending}
        onCancel={() => router.push(ROUTES.ADMIN_USERS)}
        onSubmit={(data) => {
          createUser.mutate(
            {
              email: data.email,
              password: data.password || "",
              first_name: data.first_name,
              last_name: data.last_name,
              phone: data.phone,
              is_active: data.is_active,
              role_ids: data.role_ids,
            },
            {
              onSuccess: (response) => {
                if (response.success) {
                  router.push(ROUTES.ADMIN_USERS);
                }
              },
            }
          );
        }}
      />
    </div>
  );
}
