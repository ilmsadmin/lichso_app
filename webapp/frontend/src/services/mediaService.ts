import api from "@/lib/api";
import type { ApiResponse, PaginationMeta } from "@/types/api";
import type { MediaFile, UpdateMediaRequest, MediaStats } from "@/types/media";

// ============================================
// Media API Service
// ============================================

/**
 * Upload a single file
 */
export async function uploadFile(file: File, folder?: string): Promise<ApiResponse<MediaFile>> {
  const formData = new FormData();
  formData.append("file", file);
  if (folder) {
    formData.append("folder", folder);
  }

  const response = await api.post<ApiResponse<MediaFile>>("/admin/media/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
    timeout: 60000,
  });
  return response.data;
}

/**
 * Upload multiple files
 */
export async function uploadMultipleFiles(
  files: File[],
  folder?: string
): Promise<
  ApiResponse<{
    uploaded: MediaFile[];
    errors: string[];
    total: number;
    success: number;
    failed: number;
  }>
> {
  const formData = new FormData();
  files.forEach((file) => formData.append("files", file));
  if (folder) {
    formData.append("folder", folder);
  }

  const response = await api.post("/admin/media/upload-multiple", formData, {
    headers: { "Content-Type": "multipart/form-data" },
    timeout: 120000,
  });
  return response.data;
}

/**
 * List media files
 */
export async function getMediaFiles(
  page: number = 1,
  limit: number = 24,
  options?: {
    folder?: string;
    type?: string;
    search?: string;
  }
): Promise<ApiResponse<MediaFile[]> & { meta: PaginationMeta }> {
  const params = new URLSearchParams();
  params.append("page", String(page));
  params.append("limit", String(limit));
  if (options?.folder) params.append("folder", options.folder);
  if (options?.type) params.append("type", options.type);
  if (options?.search) params.append("search", options.search);

  const response = await api.get(`/admin/media?${params.toString()}`);
  return response.data;
}

/**
 * Get a single media file
 */
export async function getMediaFile(id: string): Promise<ApiResponse<MediaFile>> {
  const response = await api.get(`/admin/media/${id}`);
  return response.data;
}

/**
 * Update media metadata
 */
export async function updateMedia(
  id: string,
  data: UpdateMediaRequest
): Promise<ApiResponse<MediaFile>> {
  const response = await api.put(`/admin/media/${id}`, data);
  return response.data;
}

/**
 * Delete a single media file
 */
export async function deleteMedia(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete(`/admin/media/${id}`);
  return response.data;
}

/**
 * Delete multiple media files
 */
export async function deleteMultipleMedia(
  ids: string[]
): Promise<ApiResponse<{ deleted_count: number }>> {
  const response = await api.post("/admin/media/delete-multiple", { ids });
  return response.data;
}

/**
 * Get all folders
 */
export async function getFolders(): Promise<ApiResponse<string[]>> {
  const response = await api.get("/admin/media/folders");
  return response.data;
}

/**
 * Get media statistics
 */
export async function getMediaStats(): Promise<ApiResponse<MediaStats>> {
  const response = await api.get("/admin/media/stats");
  return response.data;
}

// ============================================
// Email API Service
// ============================================

/**
 * Send a test email
 */
export async function sendTestEmail(to: string): Promise<ApiResponse<null>> {
  const response = await api.post("/admin/email/test", { to });
  return response.data;
}

/**
 * Get email configuration status
 */
export async function getEmailStatus(): Promise<ApiResponse<{ enabled: boolean }>> {
  const response = await api.get("/admin/email/status");
  return response.data;
}
