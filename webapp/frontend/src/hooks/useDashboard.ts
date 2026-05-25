"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import * as adminService from "@/services/adminService";
import type { ActivityLogParams } from "@/types/dashboard";
import type { UpdateSettingsGroupRequest } from "@/types/settings";

// ============================================
// useDashboard Hook
// ============================================

/**
 * Hook for fetching dashboard statistics
 */
export function useDashboardStats() {
  return useQuery({
    queryKey: ["admin", "dashboard", "stats"],
    queryFn: async () => {
      const response = await adminService.getDashboardStats();
      return response.data;
    },
    staleTime: 30 * 1000, // 30 seconds
  });
}

/**
 * Hook for fetching paginated activity logs
 */
export function useActivityLogs(params?: ActivityLogParams) {
  return useQuery({
    queryKey: ["admin", "logs", params],
    queryFn: async () => {
      const response = await adminService.getActivityLogs(params);
      return {
        logs: response.data ?? [],
        meta: response.meta,
      };
    },
    staleTime: 15 * 1000, // 15 seconds
  });
}

/**
 * Hook for fetching a single activity log
 */
export function useActivityLog(id: string) {
  return useQuery({
    queryKey: ["admin", "logs", id],
    queryFn: async () => {
      const response = await adminService.getActivityLog(id);
      return response.data;
    },
    enabled: !!id,
  });
}

/**
 * Hook for exporting activity logs
 */
export function useExportLogs() {
  return useMutation({
    mutationFn: async (params?: ActivityLogParams) => {
      const blob = await adminService.exportActivityLogs(params);
      // Trigger download
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `activity_logs_${new Date().toISOString().slice(0, 10)}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    },
  });
}

/**
 * Hook for fetching grouped settings
 */
export function useGroupedSettings() {
  return useQuery({
    queryKey: ["admin", "settings", "grouped"],
    queryFn: async () => {
      const response = await adminService.getGroupedSettings();
      return response.data;
    },
    staleTime: 60 * 1000, // 1 minute
  });
}

/**
 * Hook for fetching settings by group
 */
export function useSettingsByGroup(group: string) {
  return useQuery({
    queryKey: ["admin", "settings", group],
    queryFn: async () => {
      const response = await adminService.getSettingsByGroup(group);
      return response.data;
    },
    enabled: !!group,
    staleTime: 60 * 1000,
  });
}

/**
 * Hook for updating a group of settings
 */
export function useUpdateSettingsGroup() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ group, data }: { group: string; data: UpdateSettingsGroupRequest }) => {
      const response = await adminService.updateSettingsGroup(group, data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "settings"] });
    },
  });
}

/**
 * Hook for updating a single setting
 */
export function useUpdateSetting() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ key, value }: { key: string; value: unknown }) => {
      const response = await adminService.updateSetting(key, value);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "settings"] });
    },
  });
}
