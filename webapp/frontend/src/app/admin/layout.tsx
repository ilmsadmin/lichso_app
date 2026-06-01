"use client";

import { AuthGuard } from "@/components/auth/AuthGuard";
import { AdminSidebar } from "@/components/layouts/AdminSidebar";
import { AdminHeader } from "@/components/layouts/AdminHeader";
import { AdminMobileMenu } from "@/components/layouts/AdminMobileMenu";
import { AdminBottomNav } from "@/components/layouts/AdminBottomNav";
import { BreadcrumbNav } from "@/components/shared/BreadcrumbNav";
import { useUIStore } from "@/stores/uiStore";
import { cn } from "@/lib/utils";
import { Sheet, SheetContent, SheetTitle } from "@/components/ui/sheet";
import { TooltipProvider } from "@/components/ui/tooltip";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const { sidebarCollapsed, sidebarMobileOpen, setSidebarMobileOpen } = useUIStore();

  return (
    <AuthGuard requireAdmin>
      <TooltipProvider delayDuration={0}>
        {/* App shell: full viewport height, only <main> scrolls. This keeps the
            mobile bottom nav permanently pinned to the bottom of the screen. */}
        <div className="bg-background flex h-dvh overflow-hidden">
          {/* Desktop Sidebar */}
          <div className="hidden lg:block">
            <AdminSidebar />
          </div>

          {/* Mobile card menu (bottom sheet) */}
          <Sheet open={sidebarMobileOpen} onOpenChange={setSidebarMobileOpen}>
            <SheetContent
              side="bottom"
              className="h-[88vh] rounded-t-2xl p-0 lg:hidden"
            >
              <SheetTitle className="sr-only">Menu quản trị</SheetTitle>
              <AdminMobileMenu />
            </SheetContent>
          </Sheet>

          {/* Main content area */}
          <div
            className={cn(
              "flex min-h-0 min-w-0 flex-1 flex-col transition-all duration-300",
              sidebarCollapsed ? "lg:pl-16" : "lg:pl-64"
            )}
          >
            {/* Header */}
            <AdminHeader />

            {/* Breadcrumb */}
            <div className="border-b px-4 py-2 lg:px-6">
              <BreadcrumbNav />
            </div>

            {/* Page content — the only scrollable region */}
            <main className="flex-1 overflow-y-auto p-4 lg:p-6">{children}</main>

            {/* Mobile bottom navigation bar (in-flow, always pinned at viewport bottom) */}
            <AdminBottomNav />
          </div>
        </div>
      </TooltipProvider>
    </AuthGuard>
  );
}
