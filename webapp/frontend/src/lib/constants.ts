// ============================================
// Application Constants
// ============================================

export const APP_NAME = process.env.NEXT_PUBLIC_APP_NAME || "Lịch Số";

export const APP_URL = process.env.NEXT_PUBLIC_APP_URL || "http://localhost:3000";

export const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

// Token storage keys
export const ACCESS_TOKEN_KEY = process.env.NEXT_PUBLIC_ACCESS_TOKEN_KEY || "zplus_access_token";

export const REFRESH_TOKEN_KEY = process.env.NEXT_PUBLIC_REFRESH_TOKEN_KEY || "zplus_refresh_token";

// Pagination defaults
export const DEFAULT_PAGE = 1;
export const DEFAULT_PAGE_SIZE = 20;
export const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];

// Feature flags
export const ENABLE_REGISTRATION = process.env.NEXT_PUBLIC_ENABLE_REGISTRATION === "true";

export const ENABLE_SOCIAL_LOGIN = process.env.NEXT_PUBLIC_ENABLE_SOCIAL_LOGIN === "true";

export const ENABLE_DARK_MODE = process.env.NEXT_PUBLIC_ENABLE_DARK_MODE !== "false";

// Upload limits
export const MAX_UPLOAD_SIZE = Number(process.env.NEXT_PUBLIC_MAX_UPLOAD_SIZE) || 10 * 1024 * 1024; // 10MB

// Roles that can access /admin
export const ADMIN_ROLES = ["super_admin", "admin", "editor"] as const;

// Roles that are regular viewers (no admin access)
export const VIEWER_ROLES = ["viewer"] as const;

// Routes
export const ROUTES = {
  HOME: "/",
  LOGIN: "/login",
  REGISTER: "/register",
  FORGOT_PASSWORD: "/forgot-password",
  RESET_PASSWORD: "/reset-password",
  // User profile (accessible by all authenticated users)
  PROFILE: "/profile",
  PROFILE_NOTES: "/profile/notes",
  PROFILE_COUNTDOWNS: "/profile/countdowns",
  PROFILE_BOOKMARKS: "/profile/bookmarks",
  // Admin area (only for admin roles)
  ADMIN: "/admin",
  ADMIN_USERS: "/admin/users",
  ADMIN_ROLES: "/admin/roles",
  ADMIN_PERMISSIONS: "/admin/permissions",
  ADMIN_SETTINGS: "/admin/settings",
  ADMIN_LOGS: "/admin/logs",
  ADMIN_FILES: "/admin/files",
  ADMIN_PROFILE: "/admin/profile",
  // V2 Content Management
  ADMIN_ARTICLES: "/admin/articles",
  ADMIN_CATEGORIES: "/admin/categories",
  ADMIN_TAGS: "/admin/tags",
  ADMIN_QUOTES: "/admin/quotes",
  ADMIN_FAMOUS_PEOPLE: "/admin/famous-people",
  ADMIN_EVENTS: "/admin/events",
  ADMIN_FESTIVALS: "/admin/festivals",
  // V3 Content Management
  ADMIN_DAILY_CONTENT: "/admin/daily-content",
  ADMIN_MEDIA_ANALYTICS: "/admin/media-analytics",
  // Mobile App Management
  ADMIN_QUIZ: "/admin/quiz",
  ADMIN_BANNERS: "/admin/banners",
  ADMIN_POPUPS: "/admin/popups",
  ADMIN_SURVEYS: "/admin/surveys",
  ADMIN_APP_REVIEWS: "/admin/app-reviews",
  ADMIN_SCREEN_BACKGROUNDS: "/admin/screen-backgrounds",
  // V4 AI Management
  ADMIN_AI_ARTICLES: "/admin/ai-articles",
  ADMIN_AI_DASHBOARD: "/admin/ai-dashboard",
  // Push Notifications
  ADMIN_PUSH_NOTIFICATIONS: "/admin/push-notifications",
  ADMIN_PUSH_TEMPLATES: "/admin/push-notifications/templates",
  ADMIN_PUSH_GROUPS: "/admin/push-notifications/groups",
  // Public content pages
  ARTICLES: "/bai-viet",
  ARTICLE_CATEGORY: "/bai-viet/danh-muc",
  ARTICLE_TAG: "/bai-viet/tag",
  QUOTES: "/cau-noi-noi-tieng",
  FAMOUS_PEOPLE: "/nguoi-noi-tieng",
  EVENTS: "/su-kien",
  FESTIVALS: "/le-hoi",
  TODAY_IN_HISTORY: "/ngay-nay-trong-lich-su",
} as const;
