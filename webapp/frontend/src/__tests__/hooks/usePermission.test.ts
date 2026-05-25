import { describe, it, expect, beforeEach } from "vitest";
import { renderHook, act } from "@testing-library/react";
import { useAuthStore } from "@/stores/authStore";
import type { AuthUser } from "@/types/auth";

// Reset store before each test
beforeEach(() => {
  act(() => {
    useAuthStore.getState().clearAuth();
  });
});

const createMockUser = (overrides: Partial<AuthUser> = {}): AuthUser => ({
  id: "user-1",
  email: "test@example.com",
  first_name: "Test",
  last_name: "User",
  full_name: "Test User",
  avatar: "",
  roles: ["viewer"],
  permissions: ["users.read"],
  ...overrides,
});

describe("authStore - permission helpers", () => {
  it("hasRole returns false when no user", () => {
    const { result } = renderHook(() => useAuthStore());
    expect(result.current.hasRole("admin")).toBe(false);
  });

  it("hasRole returns true when user has role", () => {
    const { result } = renderHook(() => useAuthStore());
    act(() => {
      result.current.setUser(createMockUser({ roles: ["admin", "editor"] }));
    });

    expect(result.current.hasRole("admin")).toBe(true);
    expect(result.current.hasRole("editor")).toBe(true);
    expect(result.current.hasRole("super_admin")).toBe(false);
  });

  it("hasPermission returns false when no user", () => {
    const { result } = renderHook(() => useAuthStore());
    expect(result.current.hasPermission("users.read")).toBe(false);
  });

  it("hasPermission returns true when user has permission", () => {
    const { result } = renderHook(() => useAuthStore());
    act(() => {
      result.current.setUser(createMockUser({ permissions: ["users.read", "users.create"] }));
    });

    expect(result.current.hasPermission("users.read")).toBe(true);
    expect(result.current.hasPermission("users.create")).toBe(true);
    expect(result.current.hasPermission("users.delete")).toBe(false);
  });

  it("super admin has all permissions", () => {
    const { result } = renderHook(() => useAuthStore());
    act(() => {
      result.current.setUser(
        createMockUser({
          roles: ["super_admin"],
          permissions: [], // No explicit permissions
        })
      );
    });

    // Super admin should have any permission
    expect(result.current.hasPermission("users.delete")).toBe(true);
    expect(result.current.hasPermission("anything.here")).toBe(true);
  });

  it("hasAnyRole returns true when user has at least one role", () => {
    const { result } = renderHook(() => useAuthStore());
    act(() => {
      result.current.setUser(createMockUser({ roles: ["editor"] }));
    });

    expect(result.current.hasAnyRole(["admin", "editor"])).toBe(true);
    expect(result.current.hasAnyRole(["super_admin", "admin"])).toBe(false);
  });

  it("hasAnyRole returns false when no user", () => {
    const { result } = renderHook(() => useAuthStore());
    expect(result.current.hasAnyRole(["admin"])).toBe(false);
  });

  it("isSuperAdmin returns true for super_admin role", () => {
    const { result } = renderHook(() => useAuthStore());
    act(() => {
      result.current.setUser(createMockUser({ roles: ["super_admin"] }));
    });

    expect(result.current.isSuperAdmin()).toBe(true);
  });

  it("isSuperAdmin returns false for other roles", () => {
    const { result } = renderHook(() => useAuthStore());
    act(() => {
      result.current.setUser(createMockUser({ roles: ["admin"] }));
    });

    expect(result.current.isSuperAdmin()).toBe(false);
  });

  it("isAdmin returns true for admin or super_admin", () => {
    const { result } = renderHook(() => useAuthStore());

    act(() => {
      result.current.setUser(createMockUser({ roles: ["admin"] }));
    });
    expect(result.current.isAdmin()).toBe(true);

    act(() => {
      result.current.setUser(createMockUser({ roles: ["super_admin"] }));
    });
    expect(result.current.isAdmin()).toBe(true);
  });

  it("isAdmin returns false for editor/viewer", () => {
    const { result } = renderHook(() => useAuthStore());
    act(() => {
      result.current.setUser(createMockUser({ roles: ["editor"] }));
    });

    expect(result.current.isAdmin()).toBe(false);
  });
});

describe("authStore - state management", () => {
  it("setUser sets user and isAuthenticated", () => {
    const { result } = renderHook(() => useAuthStore());
    const user = createMockUser();

    act(() => {
      result.current.setUser(user);
    });

    expect(result.current.user).toEqual(user);
    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.isLoading).toBe(false);
  });

  it("clearAuth resets all state", () => {
    const { result } = renderHook(() => useAuthStore());

    act(() => {
      result.current.setUser(createMockUser());
    });
    expect(result.current.isAuthenticated).toBe(true);

    act(() => {
      result.current.clearAuth();
    });
    expect(result.current.user).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.isLoading).toBe(false);
  });

  it("setLoading updates loading state", () => {
    const { result } = renderHook(() => useAuthStore());

    act(() => {
      result.current.setLoading(true);
    });
    expect(result.current.isLoading).toBe(true);

    act(() => {
      result.current.setLoading(false);
    });
    expect(result.current.isLoading).toBe(false);
  });
});
