"use client";

import { useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { usePathname } from "next/navigation";
import { Moon, Sun, LogOut, ChevronDown } from "lucide-react";
import { useTheme } from "next-themes";
import { cn } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import { useAuthStore } from "@/stores/authStore";
import { useAuth } from "@/hooks/useAuth";
import { NotificationDropdown } from "@/components/shared/NotificationDropdown";

const mainNav = [
  { label: "Hôm Nay", href: ROUTES.HOME, icon: "fas fa-calendar-day" },
  { label: "Ngày Tốt", href: "/ngay-tot", icon: "fas fa-star" },
  { label: "Phong Thuỷ", href: "/phong-thuy", icon: "fas fa-yin-yang" },
  { label: "Tử Vi AI", href: "/tu-vi-ai", icon: "fas fa-robot", badge: true },
];

const articleDropdown = [
  { label: "Lịch sử Việt Nam", href: `${ROUTES.TODAY_IN_HISTORY}`, icon: "fas fa-landmark" },
  { label: "Văn hoá dân gian", href: `${ROUTES.ARTICLES}?category=van-hoa`, icon: "fas fa-masks-theater" },
  { label: "Nhân vật lịch sử", href: ROUTES.FAMOUS_PEOPLE, icon: "fas fa-user-tie" },
  { label: "Lễ hội truyền thống", href: ROUTES.FESTIVALS, icon: "fas fa-dragon" },
  { label: "Châm ngôn nổi tiếng", href: ROUTES.QUOTES, icon: "fas fa-scroll" },
];

const extraNav = [
  { label: "Giới Thiệu", href: "/gioi-thieu", icon: "fas fa-info-circle" },
];

function UserAvatar({ avatar, name, size = 32 }: { avatar?: string; name?: string; size?: number }) {
  const initials = name
    ? name.trim().split(" ").filter(Boolean).slice(-2).map((w) => w[0].toUpperCase()).join("")
    : "?";

  if (avatar) {
    return (
      // eslint-disable-next-line @next/next/no-img-element
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
      className="flex shrink-0 items-center justify-center rounded-full text-[11px] font-semibold"
      style={{ width: size, height: size, background: "var(--v2-bg-gold)", color: "#1A0F0A" }}
    >
      {initials}
    </span>
  );
}

export function V2Header() {
  const pathname = usePathname();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const { isAuthenticated, isLoading, user } = useAuthStore();
  const { logout } = useAuth();
  const { theme, setTheme } = useTheme();

  const adminRoles = ["super_admin", "admin", "editor"];
  const userAreaHref = user?.roles?.some((r) => adminRoles.includes(r))
    ? ROUTES.ADMIN
    : ROUTES.PROFILE;

  return (
    <header className="v2-header sticky top-0 z-50" style={{ background: "var(--v2-bg-header)" }}>
      <div className="mx-auto flex h-16 max-w-[1400px] items-center justify-between px-4 sm:px-8">
        {/* Logo */}
        <Link href={ROUTES.HOME} className="flex items-center gap-3">
          <Image
            src="/logo-v2.png"
            alt="Lịch Số"
            width={44}
            height={44}
            className="rounded-lg"
            style={{ filter: "drop-shadow(0 2px 8px rgba(200, 168, 78, 0.3))" }}
          />
          <div className="flex flex-col leading-tight">
            <span
              className="font-playfair text-xl font-bold"
              style={{ color: "var(--v2-bg-gold)", letterSpacing: "0.5px" }}
            >
              Lịch Số
            </span>
            <span
              className="text-[10px] font-medium uppercase"
              style={{ color: "rgba(200, 168, 78, 0.5)", letterSpacing: "2px" }}
            >
              Vạn Niên Số 1 Việt Nam
            </span>
          </div>
        </Link>

        {/* Desktop Nav */}
        <nav className="hidden items-center gap-0.5 lg:flex">
          {mainNav.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className={cn(
                "relative flex items-center gap-1.5 rounded-lg px-3.5 py-2 text-[13px] font-medium transition-all",
                pathname === link.href
                  ? "text-[var(--v2-bg-gold)]"
                  : "text-white/65 hover:bg-white/[0.06] hover:text-white/95"
              )}
              style={pathname === link.href ? { background: "rgba(200, 168, 78, 0.08)" } : {}}
            >
              {link.label}
              {link.badge && (
                <span className="absolute top-1.5 right-2 h-1.5 w-1.5 rounded-full bg-[#E85A6E]" />
              )}
            </Link>
          ))}

          {/* Dropdown: Bài Viết */}
          <div
            className="relative"
            onMouseEnter={() => setDropdownOpen(true)}
            onMouseLeave={() => setDropdownOpen(false)}
          >
            <button
              className="flex items-center gap-1 rounded-lg px-3.5 py-2 text-[13px] font-medium text-white/65 transition-all hover:bg-white/[0.06] hover:text-white/95"
            >
              Khám Phá
              <ChevronDown className="h-3 w-3" />
            </button>
            {dropdownOpen && (
              <div
                className="absolute top-full left-1/2 z-50 mt-2 min-w-[200px] -translate-x-1/2 rounded-xl p-1.5"
                style={{
                  background: "var(--v2-bg-card)",
                  border: "1px solid var(--v2-border-primary)",
                  boxShadow: "var(--v2-shadow-lg)",
                }}
              >
                {articleDropdown.map((item) => (
                  <Link
                    key={item.href}
                    href={item.href}
                    onClick={() => setDropdownOpen(false)}
                    className="flex items-center gap-2.5 rounded-lg px-3 py-2.5 text-[13px] transition-all"
                    style={{ color: "var(--v2-text-secondary)" }}
                    onMouseEnter={(e) => {
                      (e.currentTarget as HTMLElement).style.background = "var(--v2-bg-accent-soft)";
                      (e.currentTarget as HTMLElement).style.color = "var(--v2-text-accent)";
                    }}
                    onMouseLeave={(e) => {
                      (e.currentTarget as HTMLElement).style.background = "transparent";
                      (e.currentTarget as HTMLElement).style.color = "var(--v2-text-secondary)";
                    }}
                  >
                    {item.label}
                  </Link>
                ))}
              </div>
            )}
          </div>

          {extraNav.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className={cn(
                "flex items-center gap-1.5 rounded-lg px-3.5 py-2 text-[13px] font-medium transition-all",
                pathname === link.href
                  ? "text-[var(--v2-bg-gold)]"
                  : "text-white/65 hover:bg-white/[0.06] hover:text-white/95"
              )}
            >
              {link.label}
            </Link>
          ))}
        </nav>

        {/* Header Actions */}
        <div className="flex items-center gap-2">
          {/* Theme Toggle */}
          <button
            onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
            className="flex h-9 w-9 items-center justify-center rounded-lg border transition-all"
            style={{
              borderColor: "rgba(255,255,255,0.1)",
              background: "rgba(255,255,255,0.05)",
              color: "rgba(255,255,255,0.6)",
            }}
            onMouseEnter={(e) => {
              (e.currentTarget as HTMLElement).style.color = "var(--v2-bg-gold)";
              (e.currentTarget as HTMLElement).style.borderColor = "rgba(200, 168, 78, 0.3)";
            }}
            onMouseLeave={(e) => {
              (e.currentTarget as HTMLElement).style.color = "rgba(255,255,255,0.6)";
              (e.currentTarget as HTMLElement).style.borderColor = "rgba(255,255,255,0.1)";
            }}
          >
            {theme === "dark" ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
          </button>

          {/* Auth */}
          <div className="hidden items-center gap-2 md:flex">
            {isLoading ? (
              <div className="h-8 w-8 animate-pulse rounded-full" style={{ background: "rgba(200,168,78,0.2)" }} />
            ) : isAuthenticated && user ? (
              <div className="flex items-center gap-2">
                <NotificationDropdown />
                <Link
                  href={userAreaHref}
                  className="flex items-center justify-center rounded-full transition-all"
                  title={user.full_name || user.email}
                >
                  <UserAvatar avatar={user.avatar} name={user.full_name || user.first_name} size={34} />
                </Link>
                <button
                  onClick={() => logout()}
                  className="flex h-9 w-9 items-center justify-center rounded-lg border transition-all"
                  style={{
                    borderColor: "rgba(255,255,255,0.1)",
                    background: "rgba(255,255,255,0.05)",
                    color: "rgba(255,255,255,0.6)",
                  }}
                  title="Đăng xuất"
                >
                  <LogOut className="h-4 w-4" />
                </button>
              </div>
            ) : (
              <Link
                href={ROUTES.LOGIN}
                className="flex items-center gap-1.5 rounded-full border px-5 py-2 text-[13px] font-medium whitespace-nowrap transition-all"
                style={{
                  borderColor: "rgba(200, 168, 78, 0.3)",
                  background: "rgba(200, 168, 78, 0.08)",
                  color: "var(--v2-bg-gold)",
                }}
                onMouseEnter={(e) => {
                  (e.currentTarget as HTMLElement).style.background = "var(--v2-bg-gold)";
                  (e.currentTarget as HTMLElement).style.color = "#1A0F0A";
                  (e.currentTarget as HTMLElement).style.borderColor = "var(--v2-bg-gold)";
                }}
                onMouseLeave={(e) => {
                  (e.currentTarget as HTMLElement).style.background = "rgba(200, 168, 78, 0.08)";
                  (e.currentTarget as HTMLElement).style.color = "var(--v2-bg-gold)";
                  (e.currentTarget as HTMLElement).style.borderColor = "rgba(200, 168, 78, 0.3)";
                }}
              >
                Đăng nhập
              </Link>
            )}
          </div>

          {/* Mobile hamburger */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="flex h-9 w-9 items-center justify-center rounded-lg lg:hidden"
            style={{ background: "rgba(255,255,255,0.08)", color: "var(--v2-bg-gold)" }}
          >
            {mobileMenuOpen ? (
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
            ) : (
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" /></svg>
            )}
          </button>
        </div>
      </div>

      {/* Gold accent line */}
      <div
        className="h-px"
        style={{
          background: "linear-gradient(90deg, transparent 0%, var(--v2-bg-gold) 50%, transparent 100%)",
          opacity: 0.3,
        }}
      />

      {/* Mobile Menu */}
      {mobileMenuOpen && (
        <div
          className="border-t lg:hidden"
          style={{
            borderColor: "var(--v2-border-primary)",
            background: "var(--v2-bg-card)",
          }}
        >
          <div className="mx-auto max-w-[1400px] px-4 py-3">
            <nav className="space-y-0.5">
              {[...mainNav, ...articleDropdown, ...extraNav].map((link) => (
                <Link
                  key={link.href}
                  href={link.href}
                  onClick={() => setMobileMenuOpen(false)}
                  className={cn(
                    "flex items-center rounded-lg px-3 py-2.5 text-[14px] font-medium transition-colors",
                    pathname === link.href
                      ? "text-[var(--v2-text-accent)]"
                      : "hover:text-[var(--v2-text-accent)]"
                  )}
                  style={{
                    color: pathname === link.href ? "var(--v2-text-accent)" : "var(--v2-text-secondary)",
                    background: pathname === link.href ? "var(--v2-bg-accent-soft)" : "transparent",
                  }}
                >
                  {link.label}
                </Link>
              ))}
            </nav>
            <div className="my-3 h-px" style={{ background: "var(--v2-border-light)" }} />
            <div className="flex items-center justify-between">
              {isAuthenticated && user ? (
                <div className="flex items-center gap-2.5">
                  <UserAvatar avatar={user.avatar} name={user.full_name || user.first_name} size={32} />
                  <div>
                    <p className="text-[13px] font-semibold" style={{ color: "var(--v2-text-primary)" }}>
                      {user.full_name || user.first_name || user.email.split("@")[0]}
                    </p>
                    <p className="text-[11px]" style={{ color: "var(--v2-text-muted)" }}>
                      {user.email}
                    </p>
                  </div>
                </div>
              ) : (
                <Link
                  href={ROUTES.LOGIN}
                  onClick={() => setMobileMenuOpen(false)}
                  className="text-[14px] font-medium"
                  style={{ color: "var(--v2-text-accent)" }}
                >
                  Đăng nhập
                </Link>
              )}
            </div>
          </div>
        </div>
      )}
    </header>
  );
}
