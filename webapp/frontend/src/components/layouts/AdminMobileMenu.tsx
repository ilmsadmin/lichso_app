"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LogOut } from "lucide-react";
import { useAuth } from "@/hooks/useAuth";
import { usePermission } from "@/hooks/usePermission";
import { useUIStore } from "@/stores/uiStore";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { ROUTES } from "@/lib/constants";
import { adminNavSections, type AdminNavItem } from "@/components/layouts/adminNav";

/**
 * Full-screen, card-based admin menu for mobile. Opened from the bottom
 * navigation bar's "Menu" button (rendered inside a bottom sheet).
 */
export function AdminMobileMenu() {
  const pathname = usePathname();
  const { user, logout, isLoggingOut } = useAuth();
  const { can, isSuperAdmin } = usePermission();
  const { closeMobileSidebar } = useUIStore();

  const isAllowed = (item: AdminNavItem) =>
    !item.permission || isSuperAdmin() || can(item.permission);

  const isActive = (href: string) =>
    href === ROUTES.ADMIN ? pathname === ROUTES.ADMIN : pathname.startsWith(href);

  return (
    <div className="flex h-full min-h-0 flex-col">
      {/* Grab handle — affordance that the sheet scrolls / can be swiped */}
      <div className="flex shrink-0 justify-center pt-2 pb-1">
        <div className="bg-muted-foreground/30 h-1.5 w-10 rounded-full" />
      </div>

      {/* User header (pr-12 leaves room for the sheet's close button) */}
      <div className="flex shrink-0 items-center gap-3 border-b py-3 pr-12 pl-4">
        <div className="bg-primary/10 text-primary flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-sm font-semibold">
          {user?.first_name?.[0] ?? "U"}
        </div>
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-semibold">{user?.full_name ?? "User"}</p>
          <p className="text-muted-foreground truncate text-xs">{user?.email}</p>
        </div>
      </div>

      {/* Card grid — native scroll so swiping up reveals the rest of the menu */}
      <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain">
        <div className="space-y-5 px-4 py-4 pb-[calc(env(safe-area-inset-bottom)+1rem)]">
          {adminNavSections.map((section) => {
            const items = section.items.filter(isAllowed);
            if (items.length === 0) return null;

            return (
              <div key={section.title}>
                <p className="text-muted-foreground mb-2 px-1 text-xs font-semibold tracking-wide uppercase">
                  {section.title}
                </p>
                <div className="grid grid-cols-3 gap-2.5 sm:grid-cols-4">
                  {items.map((item) => {
                    const active = isActive(item.href);
                    return (
                      <Link
                        key={item.href}
                        href={item.href}
                        onClick={closeMobileSidebar}
                        className={cn(
                          "flex flex-col items-center gap-2 rounded-2xl border p-3 text-center transition active:scale-95",
                          active
                            ? "border-primary bg-primary/5"
                            : "border-border bg-card hover:bg-accent"
                        )}
                      >
                        <span
                          className={cn(
                            "flex h-11 w-11 items-center justify-center rounded-xl",
                            section.tint
                          )}
                        >
                          <item.icon className="h-5 w-5" />
                        </span>
                        <span
                          className={cn(
                            "line-clamp-2 text-[11px] leading-tight font-medium",
                            active ? "text-primary" : "text-foreground"
                          )}
                        >
                          {item.label}
                        </span>
                      </Link>
                    );
                  })}
                </div>
              </div>
            );
          })}

          {/* Logout */}
          <Button
            variant="outline"
            className="text-muted-foreground w-full justify-center gap-2"
            onClick={() => {
              closeMobileSidebar();
              logout();
            }}
            disabled={isLoggingOut}
          >
            <LogOut className="h-4 w-4" />
            {isLoggingOut ? "Đang đăng xuất..." : "Đăng xuất"}
          </Button>
        </div>
      </div>
    </div>
  );
}
