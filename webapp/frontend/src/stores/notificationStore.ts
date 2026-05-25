import { create } from "zustand";
import type { Notification } from "@/types/notification";

// ============================================
// Notification Store State
// ============================================

interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  isConnected: boolean;

  // Actions
  setNotifications: (notifications: Notification[]) => void;
  addNotification: (notification: Notification) => void;
  setUnreadCount: (count: number) => void;
  markAsRead: (id: string) => void;
  markAllAsRead: () => void;
  removeNotification: (id: string) => void;
  clearAll: () => void;
  setConnected: (connected: boolean) => void;
}

// ============================================
// Notification Store
// ============================================

export const useNotificationStore = create<NotificationState>((set) => ({
  notifications: [],
  unreadCount: 0,
  isConnected: false,

  setNotifications: (notifications: Notification[]) => {
    set({ notifications });
  },

  addNotification: (notification: Notification) => {
    set((state) => ({
      notifications: [notification, ...state.notifications],
    }));
  },

  setUnreadCount: (count: number) => {
    set({ unreadCount: count });
  },

  markAsRead: (id: string) => {
    set((state) => ({
      notifications: state.notifications.map((n) => (n.id === id ? { ...n, is_read: true } : n)),
    }));
  },

  markAllAsRead: () => {
    set((state) => ({
      notifications: state.notifications.map((n) => ({ ...n, is_read: true })),
      unreadCount: 0,
    }));
  },

  removeNotification: (id: string) => {
    set((state) => ({
      notifications: state.notifications.filter((n) => n.id !== id),
    }));
  },

  clearAll: () => {
    set({ notifications: [], unreadCount: 0 });
  },

  setConnected: (connected: boolean) => {
    set({ isConnected: connected });
  },
}));
