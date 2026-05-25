import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type {
  MediaFileV3,
  MediaFolder,
  MediaAlbum,
  MediaAlbumItem,
  MediaAttachment,
  MediaStatsV3,
  MediaVariant,
  MediaListParams,
  UpdateMediaV3Request,
  SetFocalPointRequest,
  CropImageRequest,
  ResizeImageRequest,
  RotateImageRequest,
  UploadFromURLRequest,
  InitChunkUploadRequest,
  InitChunkUploadResponse,
  CompleteChunkUploadResponse,
  CreateFolderRequest,
  UpdateFolderRequest,
  MoveFolderRequest,
  CreateAlbumRequest,
  UpdateAlbumRequest,
  AlbumMediaRequest,
  ReorderAlbumRequest,
  AttachMediaRequest,
  DetachMediaRequest,
} from "@/types/media";

const V3_BASE = "/admin/v3/media";

// ============================================
// Upload
// ============================================

/** Upload single file via V3 pipeline */
export async function uploadFileV3(file: File, folder?: string): Promise<ApiResponse<MediaFileV3>> {
  const formData = new FormData();
  formData.append("file", file);
  if (folder) formData.append("folder", folder);

  const response = await api.post<ApiResponse<MediaFileV3>>(`${V3_BASE}/upload`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
    timeout: 60000,
  });
  return response.data;
}

/** Upload multiple files via V3 pipeline */
export async function uploadMultipleV3(
  files: File[],
  folder?: string
): Promise<
  ApiResponse<{
    uploaded: MediaFileV3[];
    errors: string[];
    total: number;
    success: number;
    failed: number;
  }>
> {
  const formData = new FormData();
  files.forEach((file) => formData.append("files", file));
  if (folder) formData.append("folder", folder);

  const response = await api.post(`${V3_BASE}/upload-multiple`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
    timeout: 120000,
  });
  return response.data;
}

/** Import media from external URL */
export async function uploadFromURL(data: UploadFromURLRequest): Promise<ApiResponse<MediaFileV3>> {
  const response = await api.post<ApiResponse<MediaFileV3>>(`${V3_BASE}/upload-url`, data);
  return response.data;
}

// ============================================
// Chunk Upload
// ============================================

/** Initialize a chunk upload session */
export async function initChunkUpload(
  data: InitChunkUploadRequest
): Promise<ApiResponse<InitChunkUploadResponse>> {
  const response = await api.post<ApiResponse<InitChunkUploadResponse>>(
    `${V3_BASE}/chunk/init`,
    data
  );
  return response.data;
}

/** Upload a single chunk */
export async function uploadChunk(
  uploadId: string,
  chunkIndex: number,
  chunk: Blob
): Promise<ApiResponse<null>> {
  const formData = new FormData();
  formData.append("chunk", chunk);

  const response = await api.post<ApiResponse<null>>(
    `${V3_BASE}/chunk/${uploadId}/${chunkIndex}`,
    formData,
    { headers: { "Content-Type": "multipart/form-data" }, timeout: 60000 }
  );
  return response.data;
}

/** Complete a chunk upload session */
export async function completeChunkUpload(
  uploadId: string
): Promise<ApiResponse<CompleteChunkUploadResponse>> {
  const response = await api.post<ApiResponse<CompleteChunkUploadResponse>>(
    `${V3_BASE}/chunk/${uploadId}/complete`
  );
  return response.data;
}

// ============================================
// CRUD
// ============================================

/** List media files with V3 filters */
export async function getMediaListV3(
  params: MediaListParams = {}
): Promise<PaginatedResponse<MediaFileV3>> {
  const searchParams = new URLSearchParams();
  if (params.page) searchParams.append("page", String(params.page));
  if (params.limit) searchParams.append("limit", String(params.limit));
  if (params.folder) searchParams.append("folder", params.folder);
  if (params.folder_id) searchParams.append("folder_id", params.folder_id);
  if (params.type) searchParams.append("type", params.type);
  if (params.media_type) searchParams.append("media_type", params.media_type);
  if (params.search) searchParams.append("search", params.search);
  if (params.tag) searchParams.append("tag", params.tag);
  if (params.favorite !== undefined) searchParams.append("favorite", String(params.favorite));
  if (params.sort_by) searchParams.append("sort_by", params.sort_by);
  if (params.sort_order) searchParams.append("sort_order", params.sort_order);
  if (params.trash) searchParams.append("trash", "true");

  const response = await api.get<PaginatedResponse<MediaFileV3>>(
    `${V3_BASE}?${searchParams.toString()}`
  );
  return response.data;
}

/** Get a single media file with variants */
export async function getMediaByIdV3(id: string): Promise<ApiResponse<MediaFileV3>> {
  const response = await api.get<ApiResponse<MediaFileV3>>(`${V3_BASE}/${id}`);
  return response.data;
}

/** Update media metadata */
export async function updateMediaV3(
  id: string,
  data: UpdateMediaV3Request
): Promise<ApiResponse<MediaFileV3>> {
  const response = await api.put<ApiResponse<MediaFileV3>>(`${V3_BASE}/${id}`, data);
  return response.data;
}

/** Soft delete a media file */
export async function softDeleteMedia(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`${V3_BASE}/${id}`);
  return response.data;
}

/** Restore a soft-deleted media file */
export async function restoreMedia(id: string): Promise<ApiResponse<null>> {
  const response = await api.post<ApiResponse<null>>(`${V3_BASE}/${id}/restore`);
  return response.data;
}

/** Permanently delete a media file */
export async function permanentDeleteMedia(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`${V3_BASE}/${id}/permanent`);
  return response.data;
}

// ============================================
// Trash
// ============================================

/** List trashed media files */
export async function getTrashedMedia(
  page = 1,
  limit = 24
): Promise<PaginatedResponse<MediaFileV3>> {
  const response = await api.get<PaginatedResponse<MediaFileV3>>(
    `${V3_BASE}/trash?page=${page}&limit=${limit}`
  );
  return response.data;
}

/** Empty the trash (permanently delete all) */
export async function emptyTrash(): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`${V3_BASE}/trash`);
  return response.data;
}

// ============================================
// Image Processing
// ============================================

/** Crop an image */
export async function cropImage(
  id: string,
  data: CropImageRequest
): Promise<ApiResponse<MediaFileV3>> {
  const response = await api.post<ApiResponse<MediaFileV3>>(`${V3_BASE}/${id}/crop`, data);
  return response.data;
}

/** Resize an image */
export async function resizeImage(
  id: string,
  data: ResizeImageRequest
): Promise<ApiResponse<MediaFileV3>> {
  const response = await api.post<ApiResponse<MediaFileV3>>(`${V3_BASE}/${id}/resize`, data);
  return response.data;
}

/** Rotate an image */
export async function rotateImage(
  id: string,
  data: RotateImageRequest
): Promise<ApiResponse<MediaFileV3>> {
  const response = await api.post<ApiResponse<MediaFileV3>>(`${V3_BASE}/${id}/rotate`, data);
  return response.data;
}

/** Set image focal point */
export async function setFocalPoint(
  id: string,
  data: SetFocalPointRequest
): Promise<ApiResponse<MediaFileV3>> {
  const response = await api.put<ApiResponse<MediaFileV3>>(`${V3_BASE}/${id}/focal-point`, data);
  return response.data;
}

/** Regenerate image variants */
export async function regenerateVariants(id: string): Promise<ApiResponse<null>> {
  const response = await api.post<ApiResponse<null>>(`${V3_BASE}/${id}/regenerate-variants`);
  return response.data;
}

/** Get image variants */
export async function getVariants(id: string): Promise<ApiResponse<MediaVariant[]>> {
  const response = await api.get<ApiResponse<MediaVariant[]>>(`${V3_BASE}/${id}/variants`);
  return response.data;
}

// ============================================
// Folders
// ============================================

/** Get folder tree */
export async function getFolderTree(): Promise<ApiResponse<MediaFolder[]>> {
  const response = await api.get<ApiResponse<MediaFolder[]>>(`${V3_BASE}/folders/tree`);
  return response.data;
}

/** Create a folder */
export async function createFolder(data: CreateFolderRequest): Promise<ApiResponse<MediaFolder>> {
  const response = await api.post<ApiResponse<MediaFolder>>(`${V3_BASE}/folders`, data);
  return response.data;
}

/** Update a folder */
export async function updateFolder(
  id: string,
  data: UpdateFolderRequest
): Promise<ApiResponse<MediaFolder>> {
  const response = await api.put<ApiResponse<MediaFolder>>(`${V3_BASE}/folders/${id}`, data);
  return response.data;
}

/** Delete a folder */
export async function deleteFolder(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`${V3_BASE}/folders/${id}`);
  return response.data;
}

/** Move media to a folder */
export async function moveMediaToFolder(data: MoveFolderRequest): Promise<ApiResponse<null>> {
  const response = await api.post<ApiResponse<null>>(`${V3_BASE}/folders/move`, data);
  return response.data;
}

// ============================================
// Albums
// ============================================

/** List albums */
export async function getAlbums(page = 1, limit = 20): Promise<PaginatedResponse<MediaAlbum>> {
  const response = await api.get<PaginatedResponse<MediaAlbum>>(
    `${V3_BASE}/albums?page=${page}&limit=${limit}`
  );
  return response.data;
}

/** Create an album */
export async function createAlbum(data: CreateAlbumRequest): Promise<ApiResponse<MediaAlbum>> {
  const response = await api.post<ApiResponse<MediaAlbum>>(`${V3_BASE}/albums`, data);
  return response.data;
}

/** Update an album */
export async function updateAlbum(
  id: string,
  data: UpdateAlbumRequest
): Promise<ApiResponse<MediaAlbum>> {
  const response = await api.put<ApiResponse<MediaAlbum>>(`${V3_BASE}/albums/${id}`, data);
  return response.data;
}

/** Delete an album */
export async function deleteAlbum(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`${V3_BASE}/albums/${id}`);
  return response.data;
}

/** Get album media items */
export async function getAlbumMedia(
  albumId: string,
  page = 1,
  limit = 50
): Promise<PaginatedResponse<MediaFileV3>> {
  const response = await api.get<PaginatedResponse<MediaFileV3>>(
    `${V3_BASE}/albums/${albumId}/media?page=${page}&limit=${limit}`
  );
  return response.data;
}

/** Add media to album */
export async function addMediaToAlbum(
  albumId: string,
  data: AlbumMediaRequest
): Promise<ApiResponse<null>> {
  const response = await api.post<ApiResponse<null>>(`${V3_BASE}/albums/${albumId}/media`, data);
  return response.data;
}

/** Remove media from album */
export async function removeMediaFromAlbum(
  albumId: string,
  data: AlbumMediaRequest
): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`${V3_BASE}/albums/${albumId}/media`, {
    data,
  });
  return response.data;
}

/** Reorder album items */
export async function reorderAlbumMedia(
  albumId: string,
  data: ReorderAlbumRequest
): Promise<ApiResponse<null>> {
  const response = await api.put<ApiResponse<null>>(`${V3_BASE}/albums/${albumId}/reorder`, data);
  return response.data;
}

// ============================================
// Attachments
// ============================================

/** Attach media to entity */
export async function attachMedia(data: AttachMediaRequest): Promise<ApiResponse<MediaAttachment>> {
  const response = await api.post<ApiResponse<MediaAttachment>>(`${V3_BASE}/attach`, data);
  return response.data;
}

/** Detach media from entity */
export async function detachMedia(data: DetachMediaRequest): Promise<ApiResponse<null>> {
  const response = await api.post<ApiResponse<null>>(`${V3_BASE}/detach`, data);
  return response.data;
}

/** Get media attachments for an entity */
export async function getEntityAttachments(
  entityType: string,
  entityId: string
): Promise<ApiResponse<MediaAttachment[]>> {
  const response = await api.get<ApiResponse<MediaAttachment[]>>(
    `${V3_BASE}/attachments/${entityType}/${entityId}`
  );
  return response.data;
}

// ============================================
// Analytics
// ============================================

/** Get extended media stats */
export async function getMediaStatsV3(): Promise<ApiResponse<MediaStatsV3>> {
  const response = await api.get<ApiResponse<MediaStatsV3>>(`${V3_BASE}/stats`);
  return response.data;
}

/** Get duplicate files */
export async function getDuplicates(): Promise<ApiResponse<MediaFileV3[]>> {
  const response = await api.get<ApiResponse<MediaFileV3[]>>(`${V3_BASE}/analytics/duplicates`);
  return response.data;
}

/** Get unused media */
export async function getUnused(): Promise<ApiResponse<MediaFileV3[]>> {
  const response = await api.get<ApiResponse<MediaFileV3[]>>(`${V3_BASE}/analytics/unused`);
  return response.data;
}

/** Get media usages */
export async function getMediaUsages(id: string): Promise<ApiResponse<MediaAttachment[]>> {
  const response = await api.get<ApiResponse<MediaAttachment[]>>(`${V3_BASE}/${id}/usages`);
  return response.data;
}

// ============================================
// Favorites toggle
// ============================================

/** Toggle favorite status */
export async function toggleFavorite(
  id: string,
  isFavorite: boolean
): Promise<ApiResponse<MediaFileV3>> {
  return updateMediaV3(id, { is_favorite: isFavorite });
}
