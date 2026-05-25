"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import type { Role } from "@/types/role";
import type { GroupedPermissions } from "@/types/permission";

const roleFormSchema = z.object({
  name: z
    .string()
    .min(2, "Name must be at least 2 characters")
    .regex(/^[a-z_]+$/, "Name must be lowercase with underscores only"),
  display_name: z.string().min(2, "Display name is required"),
  description: z.string().optional(),
  level: z.number().min(0).max(100),
  permission_ids: z.array(z.string()),
});

type RoleFormData = z.infer<typeof roleFormSchema>;

interface RoleFormProps {
  role?: Role;
  groupedPermissions?: GroupedPermissions[];
  onSubmit: (data: RoleFormData) => void;
  isSubmitting?: boolean;
  onCancel: () => void;
}

export function RoleForm({
  role,
  groupedPermissions = [],
  onSubmit,
  isSubmitting,
  onCancel,
}: RoleFormProps) {
  const isEditing = !!role;

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<RoleFormData>({
    resolver: zodResolver(roleFormSchema),
    defaultValues: {
      name: role?.name ?? "",
      display_name: role?.display_name ?? "",
      description: role?.description ?? "",
      level: role?.level ?? 0,
      permission_ids: role?.permissions?.map((p) => p.id) ?? [],
    },
  });

  const selectedPermissions = watch("permission_ids") ?? [];

  useEffect(() => {
    if (role) {
      setValue("name", role.name);
      setValue("display_name", role.display_name);
      setValue("description", role.description || "");
      setValue("level", role.level);
      setValue("permission_ids", role.permissions?.map((p) => p.id) ?? []);
    }
  }, [role, setValue]);

  const togglePermission = (permId: string) => {
    const updated = selectedPermissions.includes(permId)
      ? selectedPermissions.filter((id) => id !== permId)
      : [...selectedPermissions, permId];
    setValue("permission_ids", updated, { shouldDirty: true });
  };

  const toggleModule = (group: GroupedPermissions) => {
    const modulePermIds = group.permissions.map((p) => p.id);
    const allSelected = modulePermIds.every((id) => selectedPermissions.includes(id));

    let updated: string[];
    if (allSelected) {
      updated = selectedPermissions.filter((id) => !modulePermIds.includes(id));
    } else {
      updated = [...new Set([...selectedPermissions, ...modulePermIds])];
    }
    setValue("permission_ids", updated, { shouldDirty: true });
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Role Information</CardTitle>
          <CardDescription>
            {isEditing ? "Update role details." : "Define a new role."}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="name">System Name *</Label>
              <Input
                id="name"
                {...register("name")}
                placeholder="custom_role"
                disabled={isEditing}
              />
              {errors.name && <p className="text-destructive text-xs">{errors.name.message}</p>}
            </div>
            <div className="space-y-2">
              <Label htmlFor="display_name">Display Name *</Label>
              <Input id="display_name" {...register("display_name")} placeholder="Custom Role" />
              {errors.display_name && (
                <p className="text-destructive text-xs">{errors.display_name.message}</p>
              )}
            </div>
          </div>
          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Input
              id="description"
              {...register("description")}
              placeholder="What this role is for..."
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="level">Level (0-100)</Label>
            <Input
              id="level"
              type="number"
              {...register("level")}
              min={0}
              max={100}
              className="w-32"
            />
            <p className="text-muted-foreground text-xs">
              Higher levels have broader access. System roles use 100, 90, 50, 10.
            </p>
          </div>
        </CardContent>
      </Card>

      {/* Permissions */}
      {groupedPermissions.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Permissions</CardTitle>
            <CardDescription>Select which permissions this role should have.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            {groupedPermissions.map((group) => {
              const modulePermIds = group.permissions.map((p) => p.id);
              const allSelected = modulePermIds.every((id) => selectedPermissions.includes(id));
              const someSelected =
                !allSelected && modulePermIds.some((id) => selectedPermissions.includes(id));

              return (
                <div key={group.module} className="space-y-3">
                  <div className="flex items-center gap-2">
                    <Checkbox
                      checked={allSelected}
                      ref={undefined}
                      className={someSelected ? "opacity-60" : ""}
                      onCheckedChange={() => toggleModule(group)}
                    />
                    <Label className="cursor-pointer text-sm font-semibold capitalize">
                      {group.module}
                    </Label>
                    <span className="text-muted-foreground text-xs">({modulePermIds.length})</span>
                  </div>
                  <div className="ml-6 grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
                    {group.permissions.map((perm) => (
                      <label
                        key={perm.id}
                        className="hover:bg-accent flex cursor-pointer items-center gap-2 rounded border p-2 text-sm transition-colors"
                      >
                        <Checkbox
                          checked={selectedPermissions.includes(perm.id)}
                          onCheckedChange={() => togglePermission(perm.id)}
                        />
                        <span>{perm.display_name}</span>
                      </label>
                    ))}
                  </div>
                </div>
              );
            })}
          </CardContent>
        </Card>
      )}

      <Separator />
      <div className="flex items-center justify-end gap-3">
        <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting}>
          Cancel
        </Button>
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Saving..." : isEditing ? "Update Role" : "Create Role"}
        </Button>
      </div>
    </form>
  );
}
