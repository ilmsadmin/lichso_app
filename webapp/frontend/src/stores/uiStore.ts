import { create } from "zustand";
import { persist } from "zustand/middleware";

// ============================================
// UI Store State
// ============================================

interface UIState {
  // Sidebar
  sidebarCollapsed: boolean;
  sidebarMobileOpen: boolean;

  // Actions
  toggleSidebar: () => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  setSidebarMobileOpen: (open: boolean) => void;
  closeMobileSidebar: () => void;
}

// ============================================
// UI Store
// ============================================

export const useUIStore = create<UIState>()(
  persist(
    (set) => ({
      sidebarCollapsed: false,
      sidebarMobileOpen: false,

      toggleSidebar: () => set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed })),

      setSidebarCollapsed: (collapsed: boolean) => set({ sidebarCollapsed: collapsed }),

      setSidebarMobileOpen: (open: boolean) => set({ sidebarMobileOpen: open }),

      closeMobileSidebar: () => set({ sidebarMobileOpen: false }),
    }),
    {
      name: "zplus-ui-store",
      partialize: (state) => ({
        sidebarCollapsed: state.sidebarCollapsed,
      }),
    }
  )
);
