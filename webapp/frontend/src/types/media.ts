// ============================================
// Media / File Manager Types
// ============================================

/**
 * Media file from the API
 */
export interface MediaFile {
  id: string;
  filename: string;
  original_name: string;
  url: string;
  mime_type: string;
  size: number;
  extension: string;
  uploaded_by: string;
  uploaded_name?: string;
  alt?: string;
  description?: string;
  folder: string;
  is_public: boolean;
  created_at: string;
  updated_at: string;
}

/**
 * Upload response
 */
export interface UploadResponse {
  uploaded: MediaFile[];
  errors: string[];
  total: number;
  success: number;
  failed: number;
}

/**
 * Media update request
 */
export interface UpdateMediaRequest {
  alt?: string;
  description?: string;
  folder?: string;
}

/**
 * Media stats from the API
 */
export interface MediaStats {
  total_files: number;
  total_size: number;
  max_size: number;
}

/**
 * Filter options for media list
 */
export type MediaTypeFilter = "image" | "document" | "video" | "audio" | "";

// ============================================
// V3 Media Types
// ============================================

/**
 * Image dimensions
 */
export interface MediaDimensions {
  width: number;
  height: number;
}

/**
 * Focal point coordinates for smart cropping
 */
export interface MediaFocalPoint {
  x: number;
  y: number;
}

/**
 * Media variant (thumbnail, WebP, etc.)
 */
export interface MediaVariant {
  id: string;
  media_id: string;
  variant_name: string;
  path: string;
  mime_type: string;
  width: number;
  height: number;
  size: number;
  created_at: string;
}

/**
 * V3 Media file with extended fields
 */
export interface MediaFileV3 extends MediaFile {
  media_type?: string;
  source_type?: string;
  dimensions?: MediaDimensions;
  blur_hash?: string;
  focal_point?: MediaFocalPoint;
  dominant_color?: string;
  duration?: number;
  tags?: string[];
  file_hash?: string;
  usage_count: number;
  is_favorite: boolean;
  caption?: string;
  credit?: string;
  process_status?: string;
  variants?: MediaVariant[];
  deleted_at?: string;
}

/**
 * Media folder
 */
export interface MediaFolder {
  id: string;
  name: string;
  slug: string;
  parent_id?: string;
  path: string;
  media_count: number;
  total_size: number;
  icon?: string;
  color?: string;
  sort_order: number;
  is_system: boolean;
  created_at: string;
  updated_at: string;
  children?: MediaFolder[];
}

/**
 * Media album
 */
export interface MediaAlbum {
  id: string;
  title: string;
  slug: string;
  description?: string;
  cover_media_id?: string;
  media_count: number;
  total_size: number;
  visibility: "public" | "private" | "unlisted";
  entity_type?: string;
  entity_id?: string;
  tags?: string[];
  created_by: string;
  created_at: string;
  updated_at: string;
}

/**
 * Media album item
 */
export interface MediaAlbumItem {
  id: string;
  album_id: string;
  media_id: string;
  sort_order: number;
  caption?: string;
  added_at: string;
}

/**
 * Media attachment (links media to content entities)
 */
export interface MediaAttachment {
  id: string;
  media_id: string;
  entity_type: string;
  entity_id: string;
  attachment_type: string;
  sort_order: number;
  caption?: string;
  credit?: string;
  created_at: string;
}

/**
 * Extended media stats
 */
export interface MediaStatsV3 {
  total_files: number;
  total_size: number;
  max_size: number;
  by_type: Record<string, number>;
  by_mime: Record<string, number>;
  folder_count: number;
  album_count: number;
  favorite_count: number;
  trash_count: number;
}

// ============================================
// V3 Request Types
// ============================================

export interface UpdateMediaV3Request {
  alt?: string;
  description?: string;
  caption?: string;
  credit?: string;
  folder?: string;
  folder_id?: string;
  is_public?: boolean;
  is_favorite?: boolean;
  tags?: string[];
}

export interface SetFocalPointRequest {
  x: number;
  y: number;
}

export interface CropImageRequest {
  x: number;
  y: number;
  width: number;
  height: number;
}

export interface ResizeImageRequest {
  width: number;
  height: number;
  fit?: "cover" | "contain" | "fill";
}

export interface RotateImageRequest {
  angle: 90 | 180 | 270 | -90 | -180 | -270;
}

export interface UploadFromURLRequest {
  url: string;
  folder?: string;
  alt?: string;
  caption?: string;
}

export interface InitChunkUploadRequest {
  filename: string;
  total_size: number;
  total_chunks: number;
  chunk_size: number;
  mime_type: string;
  folder?: string;
}

export interface InitChunkUploadResponse {
  upload_id: string;
  message: string;
}

export interface CompleteChunkUploadResponse {
  media_id: string;
  url: string;
  message: string;
}

export interface CreateFolderRequest {
  name: string;
  parent_id?: string;
  icon?: string;
  color?: string;
}

export interface UpdateFolderRequest {
  name?: string;
  icon?: string;
  color?: string;
}

export interface MoveFolderRequest {
  media_ids: string[];
  folder_id: string;
}

export interface CreateAlbumRequest {
  title: string;
  description?: string;
  visibility?: "public" | "private" | "unlisted";
  entity_type?: string;
  entity_id?: string;
  tags?: string[];
}

export interface UpdateAlbumRequest {
  title?: string;
  description?: string;
  visibility?: "public" | "private" | "unlisted";
  cover_media_id?: string;
  tags?: string[];
}

export interface AlbumMediaRequest {
  media_ids: string[];
}

export interface ReorderAlbumItem {
  media_id: string;
  sort_order: number;
}

export interface ReorderAlbumRequest {
  items: ReorderAlbumItem[];
}

export interface AttachMediaRequest {
  media_id: string;
  entity_type: string;
  entity_id: string;
  attachment_type: string;
  caption?: string;
  credit?: string;
}

export interface DetachMediaRequest {
  media_id: string;
  entity_type: string;
  entity_id: string;
}

/**
 * V3 Media list query params
 */
export interface MediaListParams {
  page?: number;
  limit?: number;
  folder?: string;
  folder_id?: string;
  type?: string;
  media_type?: string;
  search?: string;
  tag?: string;
  favorite?: boolean;
  sort_by?: string;
  sort_order?: "asc" | "desc";
  trash?: boolean;
}
