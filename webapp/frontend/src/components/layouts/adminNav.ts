import {
  LayoutDashboard,
  Users,
  Shield,
  Lock,
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
  CalendarClock,
  BarChart3,
  Bot,
  Wand2,
  BrainCircuit,
  Megaphone,
  Bell,
  ClipboardList,
  FolderOpen,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { ROUTES } from "@/lib/constants";

export interface AdminNavItem {
  label: string;
  href: string;
  icon: LucideIcon;
  permission?: string;
}

export interface AdminNavSection {
  title: string;
  /** Tailwind classes for the icon tile (bg + text) — gives each group a subtle color. */
  tint: string;
  items: AdminNavItem[];
}

// Grouped navigation shared by the mobile card menu (and available for reuse).
export const adminNavSections: AdminNavSection[] = [
  {
    title: "Tổng quan",
    tint: "bg-sky-500/10 text-sky-600 dark:text-sky-400",
    items: [
      { label: "Dashboard", href: ROUTES.ADMIN, icon: LayoutDashboard },
      { label: "Analytics", href: "/admin/analytics", icon: BarChart3, permission: "dashboard.read" },
    ],
  },
  {
    title: "Nội dung",
    tint: "bg-violet-500/10 text-violet-600 dark:text-violet-400",
    items: [
      { label: "Bài viết", href: ROUTES.ADMIN_ARTICLES, icon: Newspaper, permission: "content.read" },
      { label: "Danh mục", href: ROUTES.ADMIN_CATEGORIES, icon: Layers3, permission: "content.read" },
      { label: "Tags", href: ROUTES.ADMIN_TAGS, icon: Hash, permission: "content.read" },
      { label: "Danh ngôn", href: ROUTES.ADMIN_QUOTES, icon: MessageSquareQuote, permission: "content.read" },
      { label: "Nhân vật", href: ROUTES.ADMIN_FAMOUS_PEOPLE, icon: Crown, permission: "content.read" },
      { label: "Sự kiện", href: ROUTES.ADMIN_EVENTS, icon: Landmark, permission: "content.read" },
      { label: "Lễ hội", href: ROUTES.ADMIN_FESTIVALS, icon: Sparkles, permission: "content.read" },
      { label: "Nội dung ngày", href: ROUTES.ADMIN_DAILY_CONTENT, icon: CalendarClock, permission: "content.read" },
    ],
  },
  {
    title: "Ứng dụng",
    tint: "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400",
    items: [
      { label: "Đố vui", href: ROUTES.ADMIN_QUIZ, icon: BrainCircuit, permission: "content.read" },
      { label: "Banners", href: ROUTES.ADMIN_BANNERS, icon: Megaphone, permission: "content.read" },
      { label: "Popups", href: ROUTES.ADMIN_POPUPS, icon: Layers3, permission: "content.read" },
      { label: "Khảo sát", href: ROUTES.ADMIN_SURVEYS, icon: ClipboardList, permission: "content.read" },
      { label: "Ảnh nền", href: ROUTES.ADMIN_SCREEN_BACKGROUNDS, icon: Sparkles, permission: "settings.read" },
      { label: "Push Notifications", href: ROUTES.ADMIN_PUSH_NOTIFICATIONS, icon: Bell, permission: "content.read" },
    ],
  },
  {
    title: "Hệ thống",
    tint: "bg-amber-500/10 text-amber-600 dark:text-amber-400",
    items: [
      { label: "Users", href: ROUTES.ADMIN_USERS, icon: Users, permission: "users.read" },
      { label: "Roles", href: ROUTES.ADMIN_ROLES, icon: Shield, permission: "roles.read" },
      { label: "Permissions", href: ROUTES.ADMIN_PERMISSIONS, icon: Lock, permission: "permissions.read" },
      { label: "Activity Logs", href: ROUTES.ADMIN_LOGS, icon: Activity, permission: "logs.read" },
      { label: "Files", href: ROUTES.ADMIN_FILES, icon: FolderOpen, permission: "settings.read" },
      { label: "Media Analytics", href: ROUTES.ADMIN_MEDIA_ANALYTICS, icon: BarChart3, permission: "settings.read" },
    ],
  },
  {
    title: "AI",
    tint: "bg-fuchsia-500/10 text-fuchsia-600 dark:text-fuchsia-400",
    items: [
      { label: "AI Tạo Bài Viết", href: ROUTES.ADMIN_AI_ARTICLES, icon: Wand2, permission: "content.write" },
      { label: "AI Dashboard", href: ROUTES.ADMIN_AI_DASHBOARD, icon: Bot, permission: "settings.read" },
    ],
  },
  {
    title: "Khác",
    tint: "bg-slate-500/10 text-slate-600 dark:text-slate-400",
    items: [
      { label: "Settings", href: ROUTES.ADMIN_SETTINGS, icon: Settings, permission: "settings.read" },
      { label: "Hồ sơ", href: ROUTES.ADMIN_PROFILE, icon: User },
    ],
  },
];

// Items pinned to the mobile bottom navigation bar (quick access).
// The 5th slot is a "Menu" button rendered separately.
export const adminBottomNavItems: AdminNavItem[] = [
  { label: "Tổng quan", href: ROUTES.ADMIN, icon: LayoutDashboard },
  { label: "Bài viết", href: ROUTES.ADMIN_ARTICLES, icon: Newspaper, permission: "content.read" },
  { label: "Thông báo", href: ROUTES.ADMIN_PUSH_NOTIFICATIONS, icon: Bell, permission: "content.read" },
  { label: "Người dùng", href: ROUTES.ADMIN_USERS, icon: Users, permission: "users.read" },
];

export interface AdminQuickItem extends AdminNavItem {
  /** Tailwind classes for the icon tile (bg + text). */
  tint: string;
}

// Frequently used destinations shown as quick-access cards on the dashboard.
export const adminQuickAccessItems: AdminQuickItem[] = [
  { label: "Bài viết", href: ROUTES.ADMIN_ARTICLES, icon: Newspaper, permission: "content.read", tint: "bg-violet-500/10 text-violet-600 dark:text-violet-400" },
  { label: "Đố vui", href: ROUTES.ADMIN_QUIZ, icon: BrainCircuit, permission: "content.read", tint: "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400" },
  { label: "Thông báo", href: ROUTES.ADMIN_PUSH_NOTIFICATIONS, icon: Bell, permission: "content.read", tint: "bg-rose-500/10 text-rose-600 dark:text-rose-400" },
  { label: "Người dùng", href: ROUTES.ADMIN_USERS, icon: Users, permission: "users.read", tint: "bg-sky-500/10 text-sky-600 dark:text-sky-400" },
  { label: "Banners", href: ROUTES.ADMIN_BANNERS, icon: Megaphone, permission: "content.read", tint: "bg-amber-500/10 text-amber-600 dark:text-amber-400" },
  { label: "Danh mục", href: ROUTES.ADMIN_CATEGORIES, icon: Layers3, permission: "content.read", tint: "bg-teal-500/10 text-teal-600 dark:text-teal-400" },
  { label: "AI Bài viết", href: ROUTES.ADMIN_AI_ARTICLES, icon: Wand2, permission: "content.write", tint: "bg-fuchsia-500/10 text-fuchsia-600 dark:text-fuchsia-400" },
  { label: "Cài đặt", href: ROUTES.ADMIN_SETTINGS, icon: Settings, permission: "settings.read", tint: "bg-slate-500/10 text-slate-600 dark:text-slate-400" },
];
