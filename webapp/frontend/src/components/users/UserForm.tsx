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
import type { User } from "@/types/user";
import type { Role } from "@/types/role";

// ============================================
// Schemas
// ============================================

const userFormSchema = z.object({
  email: z.string().email("Invalid email address"),
  password: z.string().optional(),
  first_name: z.string().min(1, "First name is required"),
  last_name: z.string().min(1, "Last name is required"),
  phone: z.string().optional(),
  is_active: z.boolean(),
  role_ids: z.array(z.string()),
});

type UserFormData = z.infer<typeof userFormSchema>;

// ============================================
// Component
// ============================================

interface UserFormProps {
  user?: User;
  roles?: Role[];
  onSubmit: (data: UserFormData) => void;
  isSubmitting?: boolean;
  onCancel: () => void;
}

export function UserForm({ user, roles = [], onSubmit, isSubmitting, onCancel }: UserFormProps) {
  const isEditing = !!user;

  const form = useForm<UserFormData>({
    resolver: zodResolver(userFormSchema),
    defaultValues: {
      email: user?.email ?? "",
      password: "",
      first_name: user?.first_name ?? "",
      last_name: user?.last_name ?? "",
      phone: user?.phone ?? "",
      is_active: user?.is_active ?? true,
      role_ids: user?.roles?.map((r) => r.id) ?? [],
    },
  });

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = form;

  const selectedRoles = watch("role_ids") ?? [];

  // Keep form in sync if user changes
  useEffect(() => {
    if (user) {
      setValue("email", user.email);
      setValue("first_name", user.first_name);
      setValue("last_name", user.last_name);
      setValue("phone", user.phone || "");
      setValue("is_active", user.is_active);
      setValue("role_ids", user.roles?.map((r) => r.id) ?? []);
    }
  }, [user, setValue]);

  const toggleRole = (roleId: string) => {
    const current = selectedRoles;
    const updated = current.includes(roleId)
      ? current.filter((id) => id !== roleId)
      : [...current, roleId];
    setValue("role_ids", updated, { shouldDirty: true });
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      {/* Basic Info */}
      <Card>
        <CardHeader>
          <CardTitle>Basic Information</CardTitle>
          <CardDescription>
            {isEditing ? "Update user account details." : "Create a new user account."}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="first_name">First Name *</Label>
              <Input id="first_name" {...register("first_name")} placeholder="John" />
              {errors.first_name && (
                <p className="text-destructive text-xs">{errors.first_name.message}</p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="last_name">Last Name *</Label>
              <Input id="last_name" {...register("last_name")} placeholder="Doe" />
              {errors.last_name && (
                <p className="text-destructive text-xs">{errors.last_name.message}</p>
              )}
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="email">Email *</Label>
            <Input id="email" type="email" {...register("email")} placeholder="john@example.com" />
            {errors.email && <p className="text-destructive text-xs">{errors.email.message}</p>}
          </div>

          <div className="space-y-2">
            <Label htmlFor="phone">Phone</Label>
            <Input id="phone" type="tel" {...register("phone")} placeholder="+84 xxx xxx xxx" />
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">{isEditing ? "New Password" : "Password *"}</Label>
            <Input
              id="password"
              type="password"
              {...register("password")}
              placeholder={isEditing ? "Leave blank to keep current" : "Min. 8 characters"}
            />
            {errors.password && (
              <p className="text-destructive text-xs">{errors.password.message}</p>
            )}
          </div>

          <div className="flex items-center gap-2">
            <Checkbox
              id="is_active"
              checked={watch("is_active")}
              onCheckedChange={(checked) => setValue("is_active", checked === true)}
            />
            <Label htmlFor="is_active" className="cursor-pointer">
              Active account
            </Label>
          </div>
        </CardContent>
      </Card>

      {/* Roles */}
      {roles.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Roles</CardTitle>
            <CardDescription>
              Assign roles to this user. Roles determine what permissions the user has.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {roles.map((role) => (
                <label
                  key={role.id}
                  className="hover:bg-accent flex cursor-pointer items-center gap-3 rounded-lg border p-3 transition-colors"
                >
                  <Checkbox
                    checked={selectedRoles.includes(role.id)}
                    onCheckedChange={() => toggleRole(role.id)}
                  />
                  <div>
                    <p className="text-sm font-medium">{role.display_name}</p>
                    {role.description && (
                      <p className="text-muted-foreground text-xs">{role.description}</p>
                    )}
                  </div>
                </label>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Actions */}
      <Separator />
      <div className="flex items-center justify-end gap-3">
        <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting}>
          Cancel
        </Button>
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Saving..." : isEditing ? "Update User" : "Create User"}
        </Button>
      </div>
    </form>
  );
}
