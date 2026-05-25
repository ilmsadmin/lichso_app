"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/hooks/useAuth";
import { ROUTES } from "@/lib/constants";

interface AuthGuardProps {
  children: React.ReactNode;
  /** If true, only users with admin-level roles can access */
  requireAdmin?: boolean;
  requiredRoles?: string[];
  requiredPermission?: string;
  fallback?: React.ReactNode;
}

/**
 * AuthGuard protects routes requiring authentication.
 * Optionally checks for required roles or permissions.
 * When requireAdmin is true, viewers are redirected to /profile.
 */
export function AuthGuard({
  children,
  requireAdmin = false,
  requiredRoles,
  requiredPermission,
  fallback,
}: AuthGuardProps) {
  const router = useRouter();
  const { isAuthenticated, isLoading, initAuth, hasAnyRole, hasPermission, hasAdminAccess } =
    useAuth();

  useEffect(() => {
    initAuth();
  }, [initAuth]);

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push(ROUTES.LOGIN);
    }
  }, [isLoading, isAuthenticated, router]);

  // After auth is loaded, check admin access
  useEffect(() => {
    if (!isLoading && isAuthenticated && requireAdmin && !hasAdminAccess()) {
      router.push(ROUTES.PROFILE);
    }
  }, [isLoading, isAuthenticated, requireAdmin, hasAdminAccess, router]);

  // Loading state
  if (isLoading) {
    return (
      fallback || (
        <div className="flex min-h-screen items-center justify-center">
          <div className="flex flex-col items-center gap-4">
            <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
            <p className="text-muted-foreground text-sm">Loading...</p>
          </div>
        </div>
      )
    );
  }

  // Not authenticated
  if (!isAuthenticated) {
    return null;
  }

  // Check admin access
  if (requireAdmin && !hasAdminAccess()) {
    return null; // Will redirect via useEffect above
  }

  // Check role-based access
  if (requiredRoles && requiredRoles.length > 0 && !hasAnyRole(requiredRoles)) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <h1 className="text-destructive text-2xl font-bold">403</h1>
          <p className="text-muted-foreground mt-2">Bạn không có quyền truy cập trang này.</p>
          <button
            onClick={() => router.push(ROUTES.PROFILE)}
            className="bg-primary text-primary-foreground hover:bg-primary/90 mt-4 rounded-md px-4 py-2 text-sm"
          >
            Về trang cá nhân
          </button>
        </div>
      </div>
    );
  }

  // Check permission-based access
  if (requiredPermission && !hasPermission(requiredPermission)) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <h1 className="text-destructive text-2xl font-bold">403</h1>
          <p className="text-muted-foreground mt-2">Bạn không có quyền thực hiện thao tác này.</p>
          <button
            onClick={() => router.push(ROUTES.PROFILE)}
            className="bg-primary text-primary-foreground hover:bg-primary/90 mt-4 rounded-md px-4 py-2 text-sm"
          >
            Về trang cá nhân
          </button>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}
