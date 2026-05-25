"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { Menu, X, LogOut } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import { LogoSeal } from "@/components/lichso/LogoSeal";
import { ThemeToggle } from "@/components/layouts/ThemeToggle";
import { NotificationDropdown } from "@/components/shared/NotificationDropdown";
import { useAuthStore } from "@/stores/authStore";
import { useAuth } from "@/hooks/useAuth";

const navLinks = [
  { label: "Hôm Nay", href: ROUTES.HOME },
  { label: "Tra Cứu", href: "/tra-cuu" },
  { label: "Ngày Tốt", href: "/ngay-tot" },
  { label: "Phong Thủy", href: "/phong-thuy" },
  { label: "Tử Vi AI", href: "/tu-vi-ai" },
  { label: "Bài Viết", href: ROUTES.ARTICLES },
  { label: "Lịch Sử", href: ROUTES.TODAY_IN_HISTORY },
  { label: "Giới Thiệu", href: "/gioi-thieu" },
];

// ── Avatar helper ─────────────────────────────────────────────────────────────
function UserAvatar({ avatar, name, size = 32 }: { avatar?: string; name?: string; size?: number }) {
  const initials = name
    ? name.trim().split(" ").filter(Boolean).slice(-2).map((w) => w[0].toUpperCase()).join("")
    : "?";

  if (avatar) {
    // eslint-disable-next-line @next/next/no-img-element
    return (
      <img
        src={avatar}
        alt={name || "avatar"}
        width={size}
        height={size}
        className="rounded-full object-cover"
        style={{ width: size, height: size, flexShrink: 0 }}
      />
    );
  }

  return (
    <span
      className="flex shrink-0 items-center justify-center rounded-full text-[11px] font-semibold text-white"
      style={{ width: size, height: size, background: "var(--warm-amber)" }}
    >
      {initials}
    </span>
  );
}

export function HomeHeader() {
  const pathname = usePathname();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const { isAuthenticated, isLoading, user } = useAuthStore();
  const { logout } = useAuth();

  const adminRoles = ["super_admin", "admin", "editor"];
  const userAreaHref = user?.roles?.some((r) => adminRoles.includes(r))
    ? ROUTES.ADMIN
    : ROUTES.PROFILE;

  return (
    <header
      className="sticky top-0 z-50 w-full backdrop-blur-md"
      style={{ borderBottom: "1px solid var(--ls-border-warm)" }}
    >
      <div className="mx-auto flex h-[60px] max-w-[1180px] items-center justify-between px-4 sm:h-[72px] sm:px-6 lg:px-7">

        {/* ── Logo ── */}
        <Link href={ROUTES.HOME} className="flex items-center gap-2.5 sm:gap-3.5">
          <LogoSeal className="h-9 w-9 sm:h-12 sm:w-12" />
          {/* Desktop: full title + subtitle */}
          <div className="hidden sm:block">
            <h1 className="text-text-dark text-xl font-[var(--font-lora)] font-semibold tracking-wide">
              Lịch Số
            </h1>
            <p className="text-text-soft mt-0.5 text-[11px] tracking-[2px] uppercase">
              Lịch Vạn Niên Việt Nam
            </p>
          </div>
          {/* Mobile: just the name, no subtitle */}
          <span className="text-text-dark text-lg font-[var(--font-lora)] font-semibold tracking-wide sm:hidden">
            Lịch Số
          </span>
        </Link>

        {/* ── Desktop nav ── */}
        <nav className="hidden items-center gap-1 md:flex">
          {navLinks.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className={cn(
                "rounded-[20px] px-3.5 py-1.5 text-[13px] transition-all",
                "border border-transparent",
                pathname === link.href
                  ? "bg-warm-amber/10 border-warm-amber/30 text-warm-amber font-medium"
                  : "text-text-mid hover:bg-warm-amber/[0.08] hover:border-warm-amber/[0.18] hover:text-warm-amber"
              )}
            >
              {link.label}
            </Link>
          ))}
        </nav>

        {/* ── Desktop actions ── */}
        <div className="hidden items-center gap-2 md:flex">
          <ThemeToggle />
          {isLoading ? (
            <div className="bg-warm-amber/10 h-8 w-8 animate-pulse rounded-full" />
          ) : isAuthenticated && user ? (
            <div className="flex items-center gap-2">
              <NotificationDropdown />
              <Link
                href={userAreaHref}
                className="flex items-center justify-center rounded-full ring-offset-1 transition-all hover:ring-2"
                style={{ "--tw-ring-color": "var(--warm-amber)" } as React.CSSProperties}
                title={user.full_name || user.email}
              >
                <UserAvatar avatar={user.avatar} name={user.full_name || user.first_name} size={34} />
              </Link>
              <Button variant="ghost" size="icon" className="h-8 w-8" onClick={() => logout()} title="Đăng xuất">
                <LogOut className="h-4 w-4" />
              </Button>
            </div>
          ) : (
            <Button variant="ghost" size="sm" asChild>
              <Link href={ROUTES.LOGIN}>Đăng nhập</Link>
            </Button>
          )}
        </div>

        {/* ── Mobile right actions ── */}
        <div className="flex items-center gap-0.5 md:hidden">
          {!isLoading && isAuthenticated && user ? (
            <>
              <NotificationDropdown />
              <Link
                href={userAreaHref}
                className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full"
                title={user.full_name || user.email}
              >
                <UserAvatar avatar={user.avatar} name={user.full_name || user.first_name} size={28} />
              </Link>
            </>
          ) : !isLoading && !isAuthenticated ? (
            <Button variant="ghost" size="sm" className="px-2.5 text-xs" asChild>
              <Link href={ROUTES.LOGIN}>Đăng nhập</Link>
            </Button>
          ) : null}
          {/* Hamburger */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg transition-colors"
            style={{ color: "var(--ls-text-mid)" }}
            aria-label="Toggle menu"
          >
            {mobileMenuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </button>
        </div>
      </div>

      {/* ── Mobile drawer menu ── */}
      {mobileMenuOpen && (
        <div
          className="border-t md:hidden"
          style={{ borderColor: "var(--ls-border-warm)", background: "var(--ls-card-bg-solid-strong, var(--background))" }}
        >
          <div className="mx-auto max-w-[1180px] px-4 py-3 sm:px-6">

            {/* Nav links */}
            <nav className="space-y-0.5">
              {navLinks.map((link) => (
                <Link
                  key={link.href}
                  href={link.href}
                  onClick={() => setMobileMenuOpen(false)}
                  className={cn(
                    "flex items-center rounded-lg px-3 py-2.5 text-[14px] font-medium transition-colors",
                    pathname === link.href
                      ? "bg-warm-amber/10 text-warm-amber"
                      : "text-text-mid hover:bg-warm-amber/[0.06] hover:text-warm-amber"
                  )}
                >
                  {link.label}
                </Link>
              ))}
            </nav>

            {/* Divider */}
            <div className="my-3 h-px" style={{ background: "var(--ls-border-soft)" }} />

            {/* User section + theme */}
            <div className="flex items-center justify-between">
              {isAuthenticated && user ? (
                <div className="flex min-w-0 flex-1 items-center gap-3">
                  <Link
                    href={userAreaHref}
                    onClick={() => setMobileMenuOpen(false)}
                    className="flex min-w-0 items-center gap-2.5"
                  >
                    <UserAvatar avatar={user.avatar} name={user.full_name || user.first_name} size={32} />
                    <div className="min-w-0">
                      <p className="text-text-dark truncate text-[13px] font-semibold">
                        {user.full_name || user.first_name || user.email.split("@")[0]}
                      </p>
                      <p className="text-text-soft truncate text-[11px]">{user.email}</p>
                    </div>
                  </Link>
                </div>
              ) : (
                <Link
                  href={ROUTES.LOGIN}
                  onClick={() => setMobileMenuOpen(false)}
                  className="text-text-mid hover:text-warm-amber text-[14px] font-medium"
                >
                  Đăng nhập
                </Link>
              )}

              <div className="flex shrink-0 items-center gap-1 pl-2">
                <ThemeToggle />
                {isAuthenticated && user && (
                  <button
                    onClick={() => { setMobileMenuOpen(false); logout(); }}
                    className="flex h-9 w-9 items-center justify-center rounded-lg transition-colors"
                    style={{ color: "var(--ls-text-soft)" }}
                    title="Đăng xuất"
                  >
                    <LogOut className="h-4 w-4" />
                  </button>
                )}
              </div>
            </div>

          </div>
        </div>
      )}
    </header>
  );
}

