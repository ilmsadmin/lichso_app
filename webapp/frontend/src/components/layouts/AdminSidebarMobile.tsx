"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  Users,
  Shield,
  Lock,
  LogOut,
  Activity,
  Settings,
  User,
  Newspaper,
  MessageSquareQuote,
  Crown,
  Landmark,
  Sparkles,
  Hash,
  Layers3,
  Bot,
  Wand2,
  BrainCircuit,
  Megaphone,
} from "lucide-react";
import { PermissionGate } from "@/components/auth/PermissionGate";
import { useAuth } from "@/hooks/useAuth";
import { useUIStore } from "@/stores/uiStore";
import { cn } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { ScrollArea } from "@/components/ui/scroll-area";

interface NavItem {
  label: string;
  href: string;
  icon: typeof LayoutDashboard;
  permission?: string;
}

const navItems: NavItem[] = [
  {
    label: "Dashboard",
    href: ROUTES.ADMIN,
    icon: LayoutDashboard,
  },
  // Content Management
  {
    label: "Bài viết",
    href: ROUTES.ADMIN_ARTICLES,
    icon: Newspaper,
    permission: "content.read",
  },
  {
    label: "Danh mục",
    href: ROUTES.ADMIN_CATEGORIES,
    icon: Layers3,
    permission: "content.read",
  },
  {
    label: "Tags",
    href: ROUTES.ADMIN_TAGS,
    icon: Hash,
    permission: "content.read",
  },
  {
    label: "Danh ngôn",
    href: ROUTES.ADMIN_QUOTES,
    icon: MessageSquareQuote,
    permission: "content.read",
  },
  {
    label: "Nhân vật",
    href: ROUTES.ADMIN_FAMOUS_PEOPLE,
    icon: Crown,
    permission: "content.read",
  },
  {
    label: "Sự kiện",
    href: ROUTES.ADMIN_EVENTS,
    icon: Landmark,
    permission: "content.read",
  },
  {
    label: "Lễ hội",
    href: ROUTES.ADMIN_FESTIVALS,
    icon: Sparkles,
    permission: "content.read",
  },
  // Mobile App
  {
    label: "Đố vui",
    href: ROUTES.ADMIN_QUIZ,
    icon: BrainCircuit,
    permission: "content.read",
  },
  {
    label: "Banners",
    href: ROUTES.ADMIN_BANNERS,
    icon: Megaphone,
    permission: "content.read",
  },
  {
    label: "Popups",
    href: ROUTES.ADMIN_POPUPS,
    icon: Layers3,
    permission: "content.read",
  },
  // System Management
  {
    label: "Users",
    href: ROUTES.ADMIN_USERS,
    icon: Users,
    permission: "users.read",
  },
  {
    label: "Roles",
    href: ROUTES.ADMIN_ROLES,
    icon: Shield,
    permission: "roles.read",
  },
  {
    label: "Permissions",
    href: ROUTES.ADMIN_PERMISSIONS,
    icon: Lock,
    permission: "permissions.read",
  },
  {
    label: "Activity Logs",
    href: ROUTES.ADMIN_LOGS,
    icon: Activity,
    permission: "logs.read",
  },
  // AI Management
  {
    label: "AI Tạo Bài Viết",
    href: ROUTES.ADMIN_AI_ARTICLES,
    icon: Wand2,
    permission: "content.write",
  },
  {
    label: "AI Dashboard",
    href: ROUTES.ADMIN_AI_DASHBOARD,
    icon: Bot,
    permission: "settings.read",
  },
  {
    label: "Settings",
    href: ROUTES.ADMIN_SETTINGS,
    icon: Settings,
    permission: "settings.read",
  },
];

const bottomNavItems: NavItem[] = [
  {
    label: "Profile",
    href: ROUTES.ADMIN_PROFILE,
    icon: User,
  },
];

export function AdminSidebarMobile() {
  const pathname = usePathname();
  const { user, logout, isLoggingOut } = useAuth();
  const { closeMobileSidebar } = useUIStore();

  const renderNavItem = (item: NavItem, isActive: boolean) => (
    <Link
      href={item.href}
      onClick={closeMobileSidebar}
      className={cn(
        "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
        isActive
          ? "bg-primary/10 text-primary"
          : "text-muted-foreground hover:bg-accent hover:text-foreground"
      )}
    >
      <item.icon className="h-4 w-4 shrink-0" />
      <span className="truncate">{item.label}</span>
    </Link>
  );

  return (
    <div className="flex h-full flex-col">
      {/* Header */}
      <div className="flex h-14 items-center border-b px-6">
        <Link
          href={ROUTES.ADMIN}
          onClick={closeMobileSidebar}
          className="text-lg font-bold tracking-tight"
        >
          Zplus Base
        </Link>
      </div>

      {/* Navigation */}
      <ScrollArea className="flex-1">
        <nav className="space-y-1 p-3">
          {navItems.map((item) => {
            const isActive =
              item.href === ROUTES.ADMIN
                ? pathname === ROUTES.ADMIN
                : pathname.startsWith(item.href);

            const linkContent = renderNavItem(item, isActive);

            if (item.permission) {
              return (
                <PermissionGate key={item.href} permission={item.permission}>
                  {linkContent}
                </PermissionGate>
              );
            }

            return <div key={item.href}>{linkContent}</div>;
          })}
        </nav>
      </ScrollArea>

      {/* Bottom section */}
      <div className="mt-auto">
        <Separator />

        <nav className="space-y-1 p-3">
          {bottomNavItems.map((item) => {
            const isActive = pathname.startsWith(item.href);
            return <div key={item.href}>{renderNavItem(item, isActive)}</div>;
          })}
        </nav>

        <Separator />

        <div className="p-3">
          <div className="mb-3 flex items-center gap-3 px-3">
            <div className="bg-primary/10 text-primary flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-medium">
              {user?.first_name?.[0] ?? "U"}
            </div>
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-medium">{user?.full_name ?? "User"}</p>
              <p className="text-muted-foreground truncate text-xs">{user?.email}</p>
            </div>
          </div>

          <Button
            variant="ghost"
            size="sm"
            className="text-muted-foreground w-full justify-start"
            onClick={() => {
              closeMobileSidebar();
              logout();
            }}
            disabled={isLoggingOut}
          >
            <LogOut className="mr-2 h-4 w-4" />
            {isLoggingOut ? "Logging out..." : "Log out"}
          </Button>
        </div>
      </div>
    </div>
  );
}
