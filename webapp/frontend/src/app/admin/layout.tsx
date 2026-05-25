"use client";

import { AuthGuard } from "@/components/auth/AuthGuard";
import { AdminSidebar } from "@/components/layouts/AdminSidebar";
import { AdminHeader } from "@/components/layouts/AdminHeader";
import { AdminSidebarMobile } from "@/components/layouts/AdminSidebarMobile";
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
        <div className="bg-background flex min-h-screen">
          {/* Desktop Sidebar */}
          <div className="hidden lg:block">
            <AdminSidebar />
          </div>

          {/* Mobile Sidebar */}
          <Sheet open={sidebarMobileOpen} onOpenChange={setSidebarMobileOpen}>
            <SheetContent side="left" className="w-64 p-0">
              <SheetTitle className="sr-only">Navigation Menu</SheetTitle>
              <AdminSidebarMobile />
            </SheetContent>
          </Sheet>

          {/* Main content area */}
          <div
            className={cn(
              "flex flex-1 flex-col transition-all duration-300",
              sidebarCollapsed ? "lg:pl-16" : "lg:pl-64"
            )}
          >
            {/* Header */}
            <AdminHeader />

            {/* Breadcrumb */}
            <div className="border-b px-4 py-2 lg:px-6">
              <BreadcrumbNav />
            </div>

            {/* Page content */}
            <main className="flex-1 p-4 lg:p-6">{children}</main>
          </div>
        </div>
      </TooltipProvider>
    </AuthGuard>
  );
}
