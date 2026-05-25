import { create } from "zustand";
import Cookies from "js-cookie";
import { ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY, ADMIN_ROLES } from "@/lib/constants";
import type { AuthUser } from "@/types/auth";

// ============================================
// Auth Store State
// ============================================

interface AuthState {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  // Actions
  setUser: (user: AuthUser) => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
  clearAuth: () => void;
  setLoading: (loading: boolean) => void;

  // Permission helpers
  hasRole: (role: string) => boolean;
  hasPermission: (permission: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
  isSuperAdmin: () => boolean;
  isAdmin: () => boolean;
  /** Returns true if user has admin/editor/super_admin role (can access /admin) */
  hasAdminAccess: () => boolean;
  /** Returns the appropriate home route based on user role */
  getHomeRoute: () => string;
}

// ============================================
// Auth Store
// ============================================

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  isAuthenticated: false,
  isLoading: true,

  setUser: (user: AuthUser) => {
    set({ user, isAuthenticated: true, isLoading: false });
    // Store roles in a cookie so middleware can check them
    Cookies.set("zplus_user_roles", JSON.stringify(user.roles || []), {
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
    });
  },

  setTokens: (accessToken: string, refreshToken: string) => {
    Cookies.set(ACCESS_TOKEN_KEY, accessToken, {
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
    });
    Cookies.set(REFRESH_TOKEN_KEY, refreshToken, {
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
    });
  },

  clearAuth: () => {
    Cookies.remove(ACCESS_TOKEN_KEY);
    Cookies.remove(REFRESH_TOKEN_KEY);
    Cookies.remove("zplus_user_roles");
    set({ user: null, isAuthenticated: false, isLoading: false });
  },

  setLoading: (loading: boolean) => {
    set({ isLoading: loading });
  },

  hasRole: (role: string) => {
    const { user } = get();
    return user?.roles?.includes(role as AuthUser["roles"][number]) ?? false;
  },

  hasPermission: (permission: string) => {
    const { user } = get();
    if (!user) return false;
    // Super admin has all permissions
    if (user.roles?.includes("super_admin")) return true;
    return user.permissions?.includes(permission) ?? false;
  },

  hasAnyRole: (roles: string[]) => {
    const { user } = get();
    if (!user) return false;
    return roles.some((role) => user.roles?.includes(role as AuthUser["roles"][number]));
  },

  isSuperAdmin: () => {
    return get().hasRole("super_admin");
  },

  isAdmin: () => {
    const { user } = get();
    return user?.roles?.some((r) => r === "super_admin" || r === "admin") ?? false;
  },

  hasAdminAccess: () => {
    const { user } = get();
    if (!user) return false;
    return user.roles?.some((r) => (ADMIN_ROLES as readonly string[]).includes(r)) ?? false;
  },

  getHomeRoute: () => {
    const { hasAdminAccess } = get();
    return hasAdminAccess() ? "/admin" : "/profile";
  },
}));
