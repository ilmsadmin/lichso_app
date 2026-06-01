"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LayoutGrid } from "lucide-react";
import { usePermission } from "@/hooks/usePermission";
import { useUIStore } from "@/stores/uiStore";
import { cn } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import { adminBottomNavItems, type AdminNavItem } from "@/components/layouts/adminNav";

/**
 * Mobile bottom navigation bar (hidden on lg+). Rendered as the last in-flow child
 * of the app-shell content column, so it is always pinned to the bottom of the
 * viewport regardless of page height (the page itself never scrolls — only the
 * <main> area does).
 */
export function AdminBottomNav() {
  const pathname = usePathname();
  const { can, isSuperAdmin } = usePermission();
  const { sidebarMobileOpen, setSidebarMobileOpen } = useUIStore();

  const isActive = (href: string) =>
    href === ROUTES.ADMIN ? pathname === ROUTES.ADMIN : pathname.startsWith(href);

  const items = adminBottomNavItems.filter(
    (item: AdminNavItem) => !item.permission || isSuperAdmin() || can(item.permission)
  );

  const cellClass =
    "flex flex-1 flex-col items-center justify-center gap-0.5 py-2 text-[10px] font-medium transition-colors";

  return (
    <nav className="bg-card/95 supports-[backdrop-filter]:bg-card/80 z-40 flex shrink-0 items-stretch border-t pb-[env(safe-area-inset-bottom)] backdrop-blur lg:hidden">
      {items.map((item) => {
        const active = isActive(item.href);
        return (
          <Link
            key={item.href}
            href={item.href}
            className={cn(cellClass, active ? "text-primary" : "text-muted-foreground")}
          >
            <item.icon className={cn("h-5 w-5", active && "fill-primary/10")} />
            <span className="max-w-full truncate px-0.5">{item.label}</span>
          </Link>
        );
      })}

      {/* Menu button — opens the card menu sheet */}
      <button
        type="button"
        onClick={() => setSidebarMobileOpen(true)}
        className={cn(cellClass, sidebarMobileOpen ? "text-primary" : "text-muted-foreground")}
      >
        <LayoutGrid className="h-5 w-5" />
        <span>Menu</span>
      </button>
    </nav>
  );
}
