"use client";

import Link from "next/link";
import { useTheme } from "next-themes";
import { useRouter, usePathname } from "next/navigation";
import {
  Moon,
  Sun,
  User,
  LogOut,
  Home,
  BookmarkIcon,
  Settings,
  StickyNote,
  Timer,
  Menu,
  X,
} from "lucide-react";
import { useAuth } from "@/hooks/useAuth";
import { ROUTES } from "@/lib/constants";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { getInitials } from "@/lib/utils";
import { useState, useEffect } from "react";
import { cn } from "@/lib/utils";

const NAV_LINKS = [
  { href: ROUTES.HOME, label: "Trang chủ", icon: Home },
  { href: ROUTES.PROFILE, label: "Hồ sơ", icon: User },
  { href: "/profile/bookmarks", label: "Bookmarks", icon: BookmarkIcon },
  { href: "/profile/notes", label: "Ghi chú", icon: StickyNote },
  { href: "/profile/countdowns", label: "Đếm ngược", icon: Timer },
];

export function ProfileHeader() {
  const router = useRouter();
  const pathname = usePathname();
  const { user, logout, isLoggingOut, hasAdminAccess } = useAuth();
  const { theme, setTheme } = useTheme();
  const [mounted, setMounted] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  // Close mobile menu on route change
  useEffect(() => {
    setMobileMenuOpen(false);
  }, [pathname]);

  return (
    <header className="bg-card/95 supports-[backdrop-filter]:bg-card/60 sticky top-0 z-20 border-b backdrop-blur">
      <div className="flex h-14 items-center gap-4 px-4 lg:px-6">
        {/* Mobile menu toggle */}
        <Button
          variant="ghost"
          size="icon"
          className="sm:hidden"
          onClick={() => setMobileMenuOpen((o) => !o)}
          aria-label="Mở menu"
        >
          {mobileMenuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
        </Button>

        {/* Left side - Logo + Desktop nav */}
        <div className="flex items-center gap-4">
          <Link
            href={ROUTES.HOME}
            className="hover:text-primary text-lg font-bold tracking-tight transition-colors"
          >
            Lịch Số
          </Link>
          <nav className="hidden items-center gap-1 sm:flex">
            {NAV_LINKS.map(({ href, label, icon: Icon }) => (
              <Link
                key={href}
                href={href}
                className={cn(
                  "flex items-center gap-1.5 rounded-md px-3 py-1.5 text-sm transition-colors",
                  pathname === href
                    ? "bg-accent text-foreground"
                    : "text-muted-foreground hover:bg-accent hover:text-foreground",
                )}
              >
                <Icon className="h-4 w-4" />
                {label}
              </Link>
            ))}
          </nav>
        </div>

        <div className="ml-auto flex items-center gap-2">
          {/* Admin link (only for admin roles) */}
          {hasAdminAccess() && (
            <Button variant="outline" size="sm" asChild>
              <Link href={ROUTES.ADMIN} className="flex items-center gap-1.5">
                <Settings className="h-4 w-4" />
                Quản trị
              </Link>
            </Button>
          )}

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
              <DropdownMenuItem onClick={() => router.push(ROUTES.PROFILE)}>
                <User className="mr-2 h-4 w-4" />
                Hồ sơ cá nhân
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => router.push(ROUTES.HOME)}>
                <Home className="mr-2 h-4 w-4" />
                Trang chủ
              </DropdownMenuItem>
              {hasAdminAccess() && (
                <>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={() => router.push(ROUTES.ADMIN)}>
                    <Settings className="mr-2 h-4 w-4" />
                    Quản trị
                  </DropdownMenuItem>
                </>
              )}
              <DropdownMenuSeparator />
              <DropdownMenuItem
                onClick={() => logout()}
                disabled={isLoggingOut}
                variant="destructive"
              >
                <LogOut className="mr-2 h-4 w-4" />
                {isLoggingOut ? "Đang đăng xuất..." : "Đăng xuất"}
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Mobile nav drawer */}
      {mobileMenuOpen && (
        <nav className="border-t px-4 py-3 sm:hidden">
          <div className="flex flex-col gap-1">
            {NAV_LINKS.map(({ href, label, icon: Icon }) => (
              <Link
                key={href}
                href={href}
                className={cn(
                  "flex items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium transition-colors",
                  pathname === href
                    ? "bg-accent text-foreground"
                    : "text-muted-foreground hover:bg-accent hover:text-foreground",
                )}
              >
                <Icon className="h-4 w-4" />
                {label}
              </Link>
            ))}
            {hasAdminAccess() && (
              <Link
                href={ROUTES.ADMIN}
                className="text-muted-foreground hover:bg-accent hover:text-foreground flex items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium transition-colors"
              >
                <Settings className="h-4 w-4" />
                Quản trị
              </Link>
            )}
          </div>
        </nav>
      )}
    </header>
  );
}
