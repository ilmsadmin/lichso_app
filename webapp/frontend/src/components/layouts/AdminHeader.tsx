"use client";

import { useTheme } from "next-themes";
import { useRouter } from "next/navigation";
import { Menu, Moon, Sun, Search, User, LogOut, Settings } from "lucide-react";
import { useAuth } from "@/hooks/useAuth";
import { useUIStore } from "@/stores/uiStore";
import { ROUTES } from "@/lib/constants";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { NotificationDropdown } from "@/components/shared/NotificationDropdown";
import { getInitials } from "@/lib/utils";
import { useState, useEffect } from "react";

export function AdminHeader() {
  const router = useRouter();
  const { user, logout, isLoggingOut } = useAuth();
  const { setSidebarMobileOpen, sidebarCollapsed } = useUIStore();
  const { theme, setTheme } = useTheme();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  return (
    <header className="bg-card/95 supports-[backdrop-filter]:bg-card/60 sticky top-0 z-20 flex h-14 items-center gap-4 border-b px-4 backdrop-blur lg:px-6">
      {/* Mobile menu button */}
      <Button
        variant="ghost"
        size="icon"
        className="lg:hidden"
        onClick={() => setSidebarMobileOpen(true)}
      >
        <Menu className="h-5 w-5" />
        <span className="sr-only">Toggle menu</span>
      </Button>

      {/* Search bar */}
      <div className="relative max-w-md flex-1">
        <Search className="text-muted-foreground absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
        <Input type="search" placeholder="Search..." className="bg-background h-9 pl-9" />
      </div>

      <div className="ml-auto flex items-center gap-2">
        {/* Theme toggle */}
        {mounted && (
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
            className="text-muted-foreground"
          >
            {theme === "dark" ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
            <span className="sr-only">Toggle theme</span>
          </Button>
        )}

        {/* Notifications */}
        <NotificationDropdown />

        {/* User dropdown */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className="relative h-8 w-8 rounded-full">
              <Avatar size="sm">
                <AvatarImage src={user?.avatar} alt={user?.full_name ?? "Avatar"} />
                <AvatarFallback className="text-xs">
                  {user?.full_name ? getInitials(user.full_name) : "U"}
                </AvatarFallback>
              </Avatar>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-56">
            <div className="flex items-center gap-2 p-2">
              <Avatar>
                <AvatarImage src={user?.avatar} alt={user?.full_name ?? "Avatar"} />
                <AvatarFallback>
                  {user?.full_name ? getInitials(user.full_name) : "U"}
                </AvatarFallback>
              </Avatar>
              <div className="flex flex-col space-y-0.5">
                <p className="text-sm leading-none font-medium">{user?.full_name ?? "User"}</p>
                <p className="text-muted-foreground text-xs">{user?.email}</p>
              </div>
            </div>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={() => router.push(ROUTES.ADMIN_PROFILE)}>
              <User className="mr-2 h-4 w-4" />
              Profile
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => router.push(ROUTES.ADMIN_SETTINGS)}>
              <Settings className="mr-2 h-4 w-4" />
              Settings
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => router.push(ROUTES.HOME)}>
              <User className="mr-2 h-4 w-4" />
              Trang chủ
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              onClick={() => logout()}
              disabled={isLoggingOut}
              variant="destructive"
            >
              <LogOut className="mr-2 h-4 w-4" />
              {isLoggingOut ? "Logging out..." : "Log out"}
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  );
}
