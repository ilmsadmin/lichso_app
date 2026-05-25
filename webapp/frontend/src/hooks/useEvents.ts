"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as eventService from "@/services/eventService";
import type { CreateEventRequest, UpdateEventRequest, EventListParams } from "@/types/event";
import { toast } from "sonner";

// ============================================
// Event Hooks
// ============================================

const EVENTS_KEY = "events";

/** Hook for fetching paginated events */
export function useEvents(params?: EventListParams) {
  return useQuery({
    queryKey: [EVENTS_KEY, params],
    queryFn: () => eventService.getEvents(params),
  });
}

/** Hook for fetching a single event */
export function useEvent(id: string) {
  return useQuery({
    queryKey: [EVENTS_KEY, id],
    queryFn: () => eventService.getEvent(id),
    enabled: !!id,
  });
}

/** Hook for creating an event */
export function useCreateEvent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateEventRequest) => eventService.createEvent(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Tạo sự kiện thành công");
        queryClient.invalidateQueries({ queryKey: [EVENTS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể tạo sự kiện");
    },
  });
}

/** Hook for updating an event */
export function useUpdateEvent(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdateEventRequest) => eventService.updateEvent(id, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Cập nhật sự kiện thành công");
        queryClient.invalidateQueries({ queryKey: [EVENTS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể cập nhật sự kiện");
    },
  });
}

/** Hook for deleting an event */
export function useDeleteEvent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => eventService.deleteEvent(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Xóa sự kiện thành công");
        queryClient.invalidateQueries({ queryKey: [EVENTS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể xóa sự kiện");
    },
  });
}
