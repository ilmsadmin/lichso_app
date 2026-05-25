import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

// Routes that require authentication
const protectedRoutes = ["/admin", "/profile"];

// Routes only accessible when NOT authenticated
const authRoutes = ["/login", "/register", "/forgot-password", "/reset-password"];

// Roles allowed to access /admin
const ADMIN_ALLOWED_ROLES = ["super_admin", "admin", "editor"];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const accessTokenKey = process.env.NEXT_PUBLIC_ACCESS_TOKEN_KEY || "zplus_access_token";
  const token = request.cookies.get(accessTokenKey)?.value;
  const userRolesCookie = request.cookies.get("zplus_user_roles")?.value;

  // Check if accessing protected routes without token
  const isProtectedRoute = protectedRoutes.some((route) => pathname.startsWith(route));

  if (isProtectedRoute && !token) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("redirect", pathname);
    return NextResponse.redirect(loginUrl);
  }

  // Check admin route access - only admin roles allowed
  const isAdminRoute = pathname.startsWith("/admin");
  if (isAdminRoute && token && userRolesCookie) {
    try {
      const roles: string[] = JSON.parse(userRolesCookie);
      const hasAdminAccess = roles.some((role) => ADMIN_ALLOWED_ROLES.includes(role));
      if (!hasAdminAccess) {
        // Viewer trying to access /admin → redirect to /profile
        return NextResponse.redirect(new URL("/profile", request.url));
      }
    } catch {
      // If cookie is malformed, let the client-side guard handle it
    }
  }

  // Check if accessing auth routes with token
  const isAuthRoute = authRoutes.some((route) => pathname === route);

  if (isAuthRoute && token) {
    // Determine where to redirect based on roles
    if (userRolesCookie) {
      try {
        const roles: string[] = JSON.parse(userRolesCookie);
        const hasAdminAccess = roles.some((role) => ADMIN_ALLOWED_ROLES.includes(role));
        if (hasAdminAccess) {
          return NextResponse.redirect(new URL("/admin", request.url));
        } else {
          return NextResponse.redirect(new URL("/profile", request.url));
        }
      } catch {
        return NextResponse.redirect(new URL("/profile", request.url));
      }
    }
    return NextResponse.redirect(new URL("/profile", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except:
     * - api routes
     * - _next/static (static files)
     * - _next/image (image optimization)
     * - favicon.ico
     * - public files
     */
    "/((?!api|_next/static|_next/image|favicon.ico|.*\\..*$).*)",
  ],
};
