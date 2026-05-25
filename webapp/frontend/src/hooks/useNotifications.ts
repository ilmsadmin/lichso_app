"use client";

import { useEffect, useRef, useCallback } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import Cookies from "js-cookie";
import { toast } from "sonner";
import { useNotificationStore } from "@/stores/notificationStore";
import { useAuthStore } from "@/stores/authStore";
import * as notificationService from "@/services/notificationService";
import { ACCESS_TOKEN_KEY, API_URL } from "@/lib/constants";
import type { Notification, WSMessage } from "@/types/notification";

// ============================================
// useNotifications Hook
// ============================================

export function useNotifications() {
  const queryClient = useQueryClient();
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const reconnectAttemptsRef = useRef(0);
  const maxReconnectAttempts = 10;

  const { isAuthenticated } = useAuthStore();

  const {
    notifications,
    unreadCount,
    isConnected,
    setNotifications,
    addNotification,
    setUnreadCount,
    markAsRead: markAsReadInStore,
    markAllAsRead: markAllAsReadInStore,
    removeNotification,
    clearAll,
    setConnected,
  } = useNotificationStore();

  // ============================================
  // WebSocket Connection
  // ============================================
  const connectWebSocket = useCallback(() => {
    if (!isAuthenticated) return;

    const token = Cookies.get(ACCESS_TOKEN_KEY);
    if (!token) return;

    // Build WebSocket URL from API URL
    const wsBaseUrl = API_URL.replace(/^http/, "ws");
    const wsUrl = `${wsBaseUrl}/ws?token=${encodeURIComponent(token)}`;

    // Close existing connection
    if (wsRef.current) {
      wsRef.current.close();
    }

    const ws = new WebSocket(wsUrl);

    ws.onopen = () => {
      setConnected(true);
      reconnectAttemptsRef.current = 0;
    };

    ws.onmessage = (event) => {
      try {
        const msg: WSMessage = JSON.parse(event.data);

        switch (msg.type) {
          case "notification": {
            const notif = msg.data as Notification;
            addNotification(notif);

            // Show a toast for the incoming notification
            const toastFn =
              notif.type === "success"
                ? toast.success
                : notif.type === "error"
                  ? toast.error
                  : notif.type === "warning"
                    ? toast.warning
                    : toast.info;

            toastFn(notif.title, {
              description: notif.message,
              duration: 6000,
              icon:
                notif.ref_type === "reminder"
                  ? "🔔"
                  : notif.ref_type === "bookmark"
                    ? "🔖"
                    : undefined,
            });
            break;
          }
          case "notification_count": {
            const data = msg.data as { unread_count: number };
            setUnreadCount(data.unread_count);
            break;
          }
          case "notification_read_all": {
            markAllAsReadInStore();
            break;
          }
        }
      } catch {
        // Ignore parse errors
      }
    };

    ws.onclose = () => {
      setConnected(false);
      wsRef.current = null;

      // Reconnect with exponential backoff
      if (isAuthenticated && reconnectAttemptsRef.current < maxReconnectAttempts) {
        const delay = Math.min(1000 * Math.pow(2, reconnectAttemptsRef.current), 30000);
        reconnectAttemptsRef.current++;
        reconnectTimeoutRef.current = setTimeout(connectWebSocket, delay);
      }
    };

    ws.onerror = () => {
      // Error will trigger onclose, which handles reconnection
    };

    wsRef.current = ws;
  }, [isAuthenticated, setConnected, addNotification, setUnreadCount, markAllAsReadInStore]);

  // Connect/disconnect WebSocket based on auth state
  useEffect(() => {
    if (isAuthenticated) {
      connectWebSocket();
    }

    return () => {
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
      if (wsRef.current) {
        wsRef.current.close();
        wsRef.current = null;
      }
      reconnectAttemptsRef.current = 0;
    };
  }, [isAuthenticated, connectWebSocket]);

  // ============================================
  // REST API Queries
  // ============================================

  // Fetch notifications list
  const {
    data: notificationsData,
    isLoading: isLoadingNotifications,
    refetch: refetchNotifications,
  } = useQuery({
    queryKey: ["notifications"],
    queryFn: async () => {
      const response = await notificationService.getNotifications(1, 20);
      if (response.success && response.data) {
        setNotifications(response.data.notifications ?? []);
        return response.data;
      }
      return null;
    },
    enabled: isAuthenticated,
    staleTime: 30 * 1000,
  });

  // Fetch unread count
  useQuery({
    queryKey: ["notifications", "unread-count"],
    queryFn: async () => {
      const response = await notificationService.getUnreadCount();
      if (response.success && response.data) {
        setUnreadCount(response.data.unread_count);
        return response.data.unread_count;
      }
      return 0;
    },
    enabled: isAuthenticated,
    staleTime: 30 * 1000,
  });

  // ============================================
  // Mutations
  // ============================================

  const markAsReadMutation = useMutation({
    mutationFn: (id: string) => notificationService.markAsRead(id),
    onSuccess: (_, id) => {
      markAsReadInStore(id);
      queryClient.invalidateQueries({
        queryKey: ["notifications", "unread-count"],
      });
    },
  });

  const markAllAsReadMutation = useMutation({
    mutationFn: () => notificationService.markAllAsRead(),
    onSuccess: () => {
      markAllAsReadInStore();
      queryClient.invalidateQueries({
        queryKey: ["notifications", "unread-count"],
      });
    },
  });

  const deleteNotificationMutation = useMutation({
    mutationFn: (id: string) => notificationService.deleteNotification(id),
    onSuccess: (_, id) => {
      removeNotification(id);
      queryClient.invalidateQueries({
        queryKey: ["notifications", "unread-count"],
      });
    },
  });

  const deleteAllMutation = useMutation({
    mutationFn: () => notificationService.deleteAllNotifications(),
    onSuccess: () => {
      clearAll();
      queryClient.invalidateQueries({
        queryKey: ["notifications", "unread-count"],
      });
    },
  });

  return {
    // State
    notifications,
    unreadCount,
    isConnected,
    isLoadingNotifications,
    meta: notificationsData?.meta,

    // Actions
    markAsRead: markAsReadMutation.mutate,
    markAllAsRead: markAllAsReadMutation.mutate,
    deleteNotification: deleteNotificationMutation.mutate,
    deleteAll: deleteAllMutation.mutate,
    refetchNotifications,

    // Loading states
    isMarkingRead: markAsReadMutation.isPending,
    isMarkingAllRead: markAllAsReadMutation.isPending,
    isDeletingAll: deleteAllMutation.isPending,
  };
}
