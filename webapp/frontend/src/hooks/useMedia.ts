"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import * as mediaService from "@/services/mediaService";
import type { UpdateMediaRequest } from "@/types/media";
import { toast } from "sonner";

// ============================================
// useMedia Hook
// ============================================

export function useMedia(
  page: number = 1,
  limit: number = 24,
  options?: {
    folder?: string;
    type?: string;
    search?: string;
  }
) {
  const queryClient = useQueryClient();

  // List media
  const {
    data: mediaData,
    isLoading,
    refetch,
  } = useQuery({
    queryKey: ["media", page, limit, options?.folder, options?.type, options?.search],
    queryFn: () => mediaService.getMediaFiles(page, limit, options),
  });

  // Upload single file
  const uploadMutation = useMutation({
    mutationFn: ({ file, folder }: { file: File; folder?: string }) =>
      mediaService.uploadFile(file, folder),
    onSuccess: () => {
      toast.success("File uploaded successfully");
      queryClient.invalidateQueries({ queryKey: ["media"] });
    },
    onError: () => {
      toast.error("Failed to upload file");
    },
  });

  // Upload multiple files
  const uploadMultipleMutation = useMutation({
    mutationFn: ({ files, folder }: { files: File[]; folder?: string }) =>
      mediaService.uploadMultipleFiles(files, folder),
    onSuccess: (data) => {
      const result = data.data;
      if (result) {
        toast.success(`${result.success} of ${result.total} files uploaded`);
        if (result.errors?.length > 0) {
          result.errors.forEach((err: string) => toast.error(err));
        }
      }
      queryClient.invalidateQueries({ queryKey: ["media"] });
    },
    onError: () => {
      toast.error("Failed to upload files");
    },
  });

  // Update media
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateMediaRequest }) =>
      mediaService.updateMedia(id, data),
    onSuccess: () => {
      toast.success("File updated successfully");
      queryClient.invalidateQueries({ queryKey: ["media"] });
    },
    onError: () => {
      toast.error("Failed to update file");
    },
  });

  // Delete single media
  const deleteMutation = useMutation({
    mutationFn: (id: string) => mediaService.deleteMedia(id),
    onSuccess: () => {
      toast.success("File deleted successfully");
      queryClient.invalidateQueries({ queryKey: ["media"] });
    },
    onError: () => {
      toast.error("Failed to delete file");
    },
  });

  // Delete multiple media
  const deleteMultipleMutation = useMutation({
    mutationFn: (ids: string[]) => mediaService.deleteMultipleMedia(ids),
    onSuccess: (data) => {
      toast.success(`${data.data?.deleted_count ?? 0} files deleted`);
      queryClient.invalidateQueries({ queryKey: ["media"] });
    },
    onError: () => {
      toast.error("Failed to delete files");
    },
  });

  // Get folders
  const { data: foldersData } = useQuery({
    queryKey: ["media", "folders"],
    queryFn: mediaService.getFolders,
  });

  // Get stats
  const { data: statsData } = useQuery({
    queryKey: ["media", "stats"],
    queryFn: mediaService.getMediaStats,
  });

  return {
    // Data
    files: mediaData?.data ?? [],
    meta: mediaData?.meta,
    folders: foldersData?.data ?? [],
    stats: statsData?.data,
    isLoading,

    // Actions
    upload: uploadMutation.mutate,
    uploadMultiple: uploadMultipleMutation.mutate,
    updateFile: updateMutation.mutate,
    deleteFile: deleteMutation.mutate,
    deleteMultiple: deleteMultipleMutation.mutate,
    refetch,

    // Loading states
    isUploading: uploadMutation.isPending || uploadMultipleMutation.isPending,
    isDeleting: deleteMutation.isPending || deleteMultipleMutation.isPending,
    isUpdating: updateMutation.isPending,
  };
}
