"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as userService from "@/services/userService";
import type {
  CreateUserRequest,
  UpdateUserRequest,
  SetUserRolesRequest,
  UserListParams,
} from "@/types/user";
import { toast } from "sonner";

// ============================================
// useUsers Hook - List & CRUD operations
// ============================================

const USERS_KEY = "users";
const USER_DETAIL_KEY = "user-detail";

/**
 * Hook for fetching paginated users
 */
export function useUsers(params?: UserListParams) {
  return useQuery({
    queryKey: [USERS_KEY, params],
    queryFn: () => userService.getUsers(params),
  });
}

/**
 * Hook for fetching a single user
 */
export function useUser(id: string) {
  return useQuery({
    queryKey: [USERS_KEY, id],
    queryFn: () => userService.getUser(id),
    enabled: !!id,
  });
}

/**
 * Hook for fetching full enriched user detail (admin view)
 */
export function useUserDetail(id: string) {
  return useQuery({
    queryKey: [USER_DETAIL_KEY, id],
    queryFn: () => userService.getUserDetail(id),
    enabled: !!id,
    staleTime: 30_000,
  });
}

/**
 * Hook for creating a user
 */
export function useCreateUser() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateUserRequest) => userService.createUser(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("User created successfully");
        queryClient.invalidateQueries({ queryKey: [USERS_KEY] });
      }
    },
    onError: () => {
      toast.error("Failed to create user");
    },
  });
}

/**
 * Hook for updating a user
 */
export function useUpdateUser(id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: UpdateUserRequest) => userService.updateUser(id, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("User updated successfully");
        queryClient.invalidateQueries({ queryKey: [USERS_KEY] });
      }
    },
    onError: () => {
      toast.error("Failed to update user");
    },
  });
}

/**
 * Hook for deleting a user
 */
export function useDeleteUser() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => userService.deleteUser(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("User deleted successfully");
        queryClient.invalidateQueries({ queryKey: [USERS_KEY] });
      }
    },
    onError: () => {
      toast.error("Failed to delete user");
    },
  });
}

/**
 * Hook for toggling user status
 */
export function useToggleUserStatus() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, isActive }: { id: string; isActive: boolean }) =>
      userService.toggleUserStatus(id, { is_active: isActive }),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("User status updated");
        queryClient.invalidateQueries({ queryKey: [USERS_KEY] });
      }
    },
    onError: () => {
      toast.error("Failed to update user status");
    },
  });
}

/**
 * Hook for setting user roles
 */
export function useSetUserRoles(id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: SetUserRolesRequest) => userService.setUserRoles(id, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("User roles updated");
        queryClient.invalidateQueries({ queryKey: [USERS_KEY] });
      }
    },
    onError: () => {
      toast.error("Failed to update user roles");
    },
  });
}

/**
 * Hook for exporting users to CSV
 */
export function useExportUsers() {
  return useMutation({
    mutationFn: (params?: UserListParams) => userService.exportUsers(params),
    onSuccess: (blob) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `users-${new Date().toISOString().split("T")[0]}.csv`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      toast.success("Users exported successfully");
    },
    onError: () => {
      toast.error("Failed to export users");
    },
  });
}
