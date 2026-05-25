"use client";

import { useCallback, useMemo } from "react";
import { useAuthStore } from "@/stores/authStore";

// ============================================
// usePermission Hook
// ============================================

/**
 * Hook for checking user permissions in components.
 * Provides helpers to check permissions and roles for conditional rendering.
 */
export function usePermission() {
  const { user, hasPermission, hasRole, hasAnyRole, isSuperAdmin, isAdmin, hasAdminAccess } =
    useAuthStore();

  /**
   * Check if user has a specific permission.
   * Super admins bypass all permission checks.
   */
  const can = useCallback(
    (permission: string): boolean => {
      return hasPermission(permission);
    },
    [hasPermission]
  );

  /**
   * Check if user has ALL of the given permissions.
   */
  const canAll = useCallback(
    (permissions: string[]): boolean => {
      if (!user) return false;
      if (isSuperAdmin()) return true;
      return permissions.every((p) => hasPermission(p));
    },
    [user, hasPermission, isSuperAdmin]
  );

  /**
   * Check if user has ANY of the given permissions.
   */
  const canAny = useCallback(
    (permissions: string[]): boolean => {
      if (!user) return false;
      if (isSuperAdmin()) return true;
      return permissions.some((p) => hasPermission(p));
    },
    [user, hasPermission, isSuperAdmin]
  );

  /**
   * Get the list of user's current permissions.
   */
  const permissions = useMemo(() => {
    return user?.permissions ?? [];
  }, [user]);

  /**
   * Get the list of user's current roles.
   */
  const roles = useMemo(() => {
    return user?.roles ?? [];
  }, [user]);

  return {
    // Permission checks
    can,
    canAll,
    canAny,

    // Role checks
    hasRole,
    hasAnyRole,
    isSuperAdmin,
    isAdmin,
    hasAdminAccess,

    // Data
    permissions,
    roles,
    user,
  };
}
