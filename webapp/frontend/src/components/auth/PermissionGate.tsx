"use client";

import { usePermission } from "@/hooks/usePermission";

// ============================================
// PermissionGate Component
// ============================================

interface PermissionGateProps {
  /** Single permission to check */
  permission?: string;
  /** Multiple permissions - user must have ALL (AND logic) */
  permissions?: string[];
  /** Multiple permissions - user must have ANY (OR logic) */
  anyPermission?: string[];
  /** Required role(s) - user must have at least one */
  roles?: string[];
  /** Content to show when user has access */
  children: React.ReactNode;
  /** Optional fallback content when user doesn't have access */
  fallback?: React.ReactNode;
}

/**
 * PermissionGate conditionally renders children based on the user's
 * permissions and roles. Super admins bypass all checks.
 *
 * @example
 * // Single permission
 * <PermissionGate permission="users.create">
 *   <CreateUserButton />
 * </PermissionGate>
 *
 * @example
 * // Multiple permissions (AND)
 * <PermissionGate permissions={["users.create", "users.update"]}>
 *   <UserForm />
 * </PermissionGate>
 *
 * @example
 * // Any permission (OR)
 * <PermissionGate anyPermission={["users.read", "users.create"]}>
 *   <UserSection />
 * </PermissionGate>
 *
 * @example
 * // Role check
 * <PermissionGate roles={["admin", "super_admin"]}>
 *   <AdminPanel />
 * </PermissionGate>
 *
 * @example
 * // With fallback
 * <PermissionGate permission="users.delete" fallback={<p>No access</p>}>
 *   <DeleteButton />
 * </PermissionGate>
 */
export function PermissionGate({
  permission,
  permissions,
  anyPermission,
  roles,
  children,
  fallback = null,
}: PermissionGateProps) {
  const { can, canAll, canAny, hasAnyRole, isSuperAdmin } = usePermission();

  // Super admin bypasses all checks
  if (isSuperAdmin()) {
    return <>{children}</>;
  }

  // Check single permission
  if (permission && !can(permission)) {
    return <>{fallback}</>;
  }

  // Check all permissions (AND)
  if (permissions && permissions.length > 0 && !canAll(permissions)) {
    return <>{fallback}</>;
  }

  // Check any permission (OR)
  if (anyPermission && anyPermission.length > 0 && !canAny(anyPermission)) {
    return <>{fallback}</>;
  }

  // Check roles
  if (roles && roles.length > 0 && !hasAnyRole(roles)) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}
