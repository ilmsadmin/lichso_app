import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import { PermissionGate } from "@/components/auth/PermissionGate";

// Mock the usePermission hook
const mockCan = vi.fn();
const mockCanAll = vi.fn();
const mockCanAny = vi.fn();
const mockHasAnyRole = vi.fn();
const mockIsSuperAdmin = vi.fn();

vi.mock("@/hooks/usePermission", () => ({
  usePermission: () => ({
    can: mockCan,
    canAll: mockCanAll,
    canAny: mockCanAny,
    hasAnyRole: mockHasAnyRole,
    isSuperAdmin: mockIsSuperAdmin,
  }),
}));

describe("PermissionGate", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockCan.mockReturnValue(false);
    mockCanAll.mockReturnValue(false);
    mockCanAny.mockReturnValue(false);
    mockHasAnyRole.mockReturnValue(false);
    mockIsSuperAdmin.mockReturnValue(false);
  });

  it("renders children when super admin", () => {
    mockIsSuperAdmin.mockReturnValue(true);

    render(
      <PermissionGate permission="users.read">
        <div>Protected Content</div>
      </PermissionGate>
    );

    expect(screen.getByText("Protected Content")).toBeInTheDocument();
  });

  it("renders children when user has the required permission", () => {
    mockCan.mockReturnValue(true);

    render(
      <PermissionGate permission="users.read">
        <div>Protected Content</div>
      </PermissionGate>
    );

    expect(screen.getByText("Protected Content")).toBeInTheDocument();
  });

  it("renders fallback when user lacks the required permission", () => {
    mockCan.mockReturnValue(false);

    render(
      <PermissionGate permission="users.delete" fallback={<div>No Access</div>}>
        <div>Protected Content</div>
      </PermissionGate>
    );

    expect(screen.queryByText("Protected Content")).not.toBeInTheDocument();
    expect(screen.getByText("No Access")).toBeInTheDocument();
  });

  it("renders nothing (null fallback) when user lacks permission and no fallback", () => {
    mockCan.mockReturnValue(false);

    const { container } = render(
      <PermissionGate permission="users.delete">
        <div>Protected Content</div>
      </PermissionGate>
    );

    expect(screen.queryByText("Protected Content")).not.toBeInTheDocument();
    expect(container.innerHTML).toBe("");
  });

  it("checks all permissions (AND logic) with permissions prop", () => {
    mockCanAll.mockReturnValue(true);

    render(
      <PermissionGate permissions={["users.read", "users.create"]}>
        <div>Protected Content</div>
      </PermissionGate>
    );

    expect(screen.getByText("Protected Content")).toBeInTheDocument();
  });

  it("blocks when not all permissions are met (AND logic)", () => {
    mockCanAll.mockReturnValue(false);

    render(
      <PermissionGate permissions={["users.read", "users.create"]}>
        <div>Protected Content</div>
      </PermissionGate>
    );

    expect(screen.queryByText("Protected Content")).not.toBeInTheDocument();
  });

  it("checks any permission (OR logic) with anyPermission prop", () => {
    mockCanAny.mockReturnValue(true);

    render(
      <PermissionGate anyPermission={["users.read", "roles.read"]}>
        <div>Protected Content</div>
      </PermissionGate>
    );

    expect(screen.getByText("Protected Content")).toBeInTheDocument();
  });

  it("blocks when no permissions match (OR logic)", () => {
    mockCanAny.mockReturnValue(false);

    render(
      <PermissionGate anyPermission={["users.delete", "roles.delete"]}>
        <div>Protected Content</div>
      </PermissionGate>
    );

    expect(screen.queryByText("Protected Content")).not.toBeInTheDocument();
  });

  it("checks roles with roles prop", () => {
    mockHasAnyRole.mockReturnValue(true);

    render(
      <PermissionGate roles={["admin", "super_admin"]}>
        <div>Admin Content</div>
      </PermissionGate>
    );

    expect(screen.getByText("Admin Content")).toBeInTheDocument();
  });

  it("blocks when user lacks the required role", () => {
    mockHasAnyRole.mockReturnValue(false);

    render(
      <PermissionGate roles={["admin"]}>
        <div>Admin Content</div>
      </PermissionGate>
    );

    expect(screen.queryByText("Admin Content")).not.toBeInTheDocument();
  });

  it("renders children with no permission checks specified", () => {
    render(
      <PermissionGate>
        <div>No Checks Content</div>
      </PermissionGate>
    );

    expect(screen.getByText("No Checks Content")).toBeInTheDocument();
  });

  it("super admin bypasses all checks including roles", () => {
    mockIsSuperAdmin.mockReturnValue(true);
    mockHasAnyRole.mockReturnValue(false);

    render(
      <PermissionGate roles={["admin"]} permission="users.delete">
        <div>Super Admin Access</div>
      </PermissionGate>
    );

    expect(screen.getByText("Super Admin Access")).toBeInTheDocument();
  });
});
