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
  ChevronLeft,
  ChevronRight,
  FolderOpen,
  Newspaper,
  MessageSquareQuote,
  Crown,
  Landmark,
  Sparkles,
  Hash,
  Layers3,
  CalendarClock,
  BarChart3,
  Bot,
  Wand2,
  BrainCircuit,
  Megaphone,
  Bell,
} from "lucide-react";
import { PermissionGate } from "@/components/auth/PermissionGate";
import { useAuth } from "@/hooks/useAuth";
import { useUIStore } from "@/stores/uiStore";
import { cn } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { ScrollArea } from "@/components/ui/scroll-area";

interface NavItem {
  label: string;
  href: string;
  icon: typeof LayoutDashboard;
  permission?: string;
  separator?: boolean;
}

const navItems: NavItem[] = [
  {
    label: "Dashboard",
    href: ROUTES.ADMIN,
    icon: LayoutDashboard,
  },
  {
    label: "Analytics",
    href: "/admin/analytics",
    icon: BarChart3,
    permission: "dashboard.read",
  },
  // Content Management
  {
    label: "Bài viết",
    href: ROUTES.ADMIN_ARTICLES,
    icon: Newspaper,
    permission: "content.read",
    separator: true,
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
  {
    label: "Nội dung ngày",
    href: ROUTES.ADMIN_DAILY_CONTENT,
    icon: CalendarClock,
    permission: "content.read",
  },
  // Mobile App Management
  {
    label: "Đố vui",
    href: ROUTES.ADMIN_QUIZ,
    icon: BrainCircuit,
    permission: "content.read",
    separator: true,
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
  {
    label: "Ảnh nền",
    href: ROUTES.ADMIN_SCREEN_BACKGROUNDS,
    icon: Sparkles, // Or Image
    permission: "settings.read",
  },
  {
    label: "Push Notifications",
    href: ROUTES.ADMIN_PUSH_NOTIFICATIONS,
    icon: Bell,
    permission: "content.read",
  },
  // System Management
  {
    label: "Users",
    href: ROUTES.ADMIN_USERS,
    icon: Users,
    permission: "users.read",
    separator: true,
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
  {
    label: "Files",
    href: ROUTES.ADMIN_FILES,
    icon: FolderOpen,
    permission: "settings.read",
  },
  {
    label: "Media Analytics",
    href: ROUTES.ADMIN_MEDIA_ANALYTICS,
    icon: BarChart3,
    permission: "settings.read",
  },
  // AI Management
  {
    label: "AI Tạo Bài Viết",
    href: ROUTES.ADMIN_AI_ARTICLES,
    icon: Wand2,
    permission: "content.write",
    separator: true,
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

export function AdminSidebar() {
  const pathname = usePathname();
  const { user, logout, isLoggingOut } = useAuth();
  const { sidebarCollapsed, toggleSidebar } = useUIStore();

  const renderNavItem = (item: NavItem, isActive: boolean, collapsed: boolean) => {
    const link = (
      <Link
        href={item.href}
        className={cn(
          "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-all duration-200",
          isActive
            ? "bg-primary/10 text-primary"
            : "text-muted-foreground hover:bg-accent hover:text-foreground",
          collapsed && "justify-center px-2"
        )}
      >
        <item.icon className="h-4 w-4 shrink-0" />
        {!collapsed && <span className="truncate">{item.label}</span>}
      </Link>
    );

    if (collapsed) {
      return (
        <Tooltip key={item.href}>
          <TooltipTrigger asChild>{link}</TooltipTrigger>
          <TooltipContent side="right" sideOffset={10}>
            {item.label}
          </TooltipContent>
        </Tooltip>
      );
    }

    return link;
  };

  return (
    <aside
      className={cn(
        "bg-card fixed inset-y-0 left-0 z-30 flex flex-col border-r transition-all duration-300 ease-in-out",
        sidebarCollapsed ? "w-16" : "w-64"
      )}
    >
      {/* Header */}
      <div
        className={cn(
          "flex h-14 items-center border-b",
          sidebarCollapsed ? "justify-center px-2" : "px-6"
        )}
      >
        <Link
          href={ROUTES.ADMIN}
          className={cn(
            "text-lg font-bold tracking-tight transition-all",
            sidebarCollapsed && "text-sm"
          )}
        >
          {sidebarCollapsed ? "Z+" : "Zplus Base"}
        </Link>
      </div>

      {/* Navigation */}
      <ScrollArea className="flex-1">
        <nav className={cn("space-y-1 p-3", sidebarCollapsed && "p-2")}>
          {navItems.map((item, index) => {
            const isActive =
              item.href === ROUTES.ADMIN
                ? pathname === ROUTES.ADMIN
                : pathname.startsWith(item.href);

            const linkContent = renderNavItem(item, isActive, sidebarCollapsed);

            const separator =
              item.separator && index > 0 ? (
                <Separator key={`sep-${item.href}`} className="my-2" />
              ) : null;

            if (item.permission) {
              return (
                <PermissionGate key={item.href} permission={item.permission}>
                  {separator}
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

        {/* Bottom nav items */}
        <nav className={cn("space-y-1 p-3", sidebarCollapsed && "p-2")}>
          {bottomNavItems.map((item) => {
            const isActive = pathname.startsWith(item.href);
            return <div key={item.href}>{renderNavItem(item, isActive, sidebarCollapsed)}</div>;
          })}
        </nav>

        <Separator />

        {/* User info */}
        <div className={cn("p-3", sidebarCollapsed && "p-2")}>
          {!sidebarCollapsed && (
            <div className="mb-3 flex items-center gap-3 px-3">
              <div className="bg-primary/10 text-primary flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-medium">
                {user?.first_name?.[0] ?? "U"}
              </div>
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium">{user?.full_name ?? "User"}</p>
                <p className="text-muted-foreground truncate text-xs">{user?.email}</p>
              </div>
            </div>
          )}

          {sidebarCollapsed ? (
            <Tooltip>
              <TooltipTrigger asChild>
                <Button
                  variant="ghost"
                  size="sm"
                  className="w-full"
                  onClick={() => logout()}
                  disabled={isLoggingOut}
                >
                  <LogOut className="h-4 w-4" />
                </Button>
              </TooltipTrigger>
              <TooltipContent side="right" sideOffset={10}>
                {isLoggingOut ? "Logging out..." : "Log out"}
              </TooltipContent>
            </Tooltip>
          ) : (
            <Button
              variant="ghost"
              size="sm"
              className="text-muted-foreground w-full justify-start"
              onClick={() => logout()}
              disabled={isLoggingOut}
            >
              <LogOut className="mr-2 h-4 w-4" />
              {isLoggingOut ? "Logging out..." : "Log out"}
            </Button>
          )}
        </div>

        {/* Collapse toggle */}
        <Separator />
        <div className="p-2">
          <Button variant="ghost" size="sm" className="w-full" onClick={toggleSidebar}>
            {sidebarCollapsed ? (
              <ChevronRight className="h-4 w-4" />
            ) : (
              <>
                <ChevronLeft className="mr-2 h-4 w-4" />
                <span className="text-muted-foreground text-xs">Collapse</span>
              </>
            )}
          </Button>
        </div>
      </div>
    </aside>
  );
}
