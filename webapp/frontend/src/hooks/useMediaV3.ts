"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as mediaV3 from "@/services/mediaV3Service";
import type {
  MediaListParams,
  UpdateMediaV3Request,
  SetFocalPointRequest,
  CropImageRequest,
  ResizeImageRequest,
  RotateImageRequest,
  UploadFromURLRequest,
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
import { toast } from "sonner";

// ============================================
// Query Keys
// ============================================
const MEDIA_V3_KEY = "media-v3";
const MEDIA_V3_DETAIL_KEY = "media-v3-detail";
const MEDIA_V3_TRASH_KEY = "media-v3-trash";
const FOLDERS_KEY = "media-folders";
const ALBUMS_KEY = "media-albums";
const ALBUM_MEDIA_KEY = "album-media";
const MEDIA_STATS_V3_KEY = "media-stats-v3";
const MEDIA_VARIANTS_KEY = "media-variants";
const MEDIA_USAGES_KEY = "media-usages";

// ============================================
// List / Detail hooks
// ============================================

/** Paginated media list with V3 filters */
export function useMediaListV3(params: MediaListParams = {}) {
  return useQuery({
    queryKey: [MEDIA_V3_KEY, params],
    queryFn: () => mediaV3.getMediaListV3(params),
  });
}

/** Single media detail with variants */
export function useMediaDetailV3(id: string | null) {
  return useQuery({
    queryKey: [MEDIA_V3_DETAIL_KEY, id],
    queryFn: () => mediaV3.getMediaByIdV3(id!),
    enabled: !!id,
  });
}

/** Trashed media */
export function useTrashedMedia(page = 1, limit = 24) {
  return useQuery({
    queryKey: [MEDIA_V3_TRASH_KEY, page, limit],
    queryFn: () => mediaV3.getTrashedMedia(page, limit),
  });
}

/** Media variants */
export function useMediaVariants(id: string | null) {
  return useQuery({
    queryKey: [MEDIA_VARIANTS_KEY, id],
    queryFn: () => mediaV3.getVariants(id!),
    enabled: !!id,
  });
}

/** Media usages */
export function useMediaUsages(id: string | null) {
  return useQuery({
    queryKey: [MEDIA_USAGES_KEY, id],
    queryFn: () => mediaV3.getMediaUsages(id!),
    enabled: !!id,
  });
}

// ============================================
// Upload mutations
// ============================================

export function useUploadV3() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ file, folder }: { file: File; folder?: string }) =>
      mediaV3.uploadFileV3(file, folder),
    onSuccess: () => {
      toast.success("Tải lên thành công");
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_STATS_V3_KEY] });
    },
    onError: () => toast.error("Tải lên thất bại"),
  });
}

export function useUploadMultipleV3() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ files, folder }: { files: File[]; folder?: string }) =>
      mediaV3.uploadMultipleV3(files, folder),
    onSuccess: (data) => {
      const result = data.data;
      if (result) {
        toast.success(`${result.success}/${result.total} files uploaded`);
        result.errors?.forEach((e) => toast.error(e));
      }
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_STATS_V3_KEY] });
    },
    onError: () => toast.error("Tải lên thất bại"),
  });
}

export function useUploadFromURL() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UploadFromURLRequest) => mediaV3.uploadFromURL(data),
    onSuccess: () => {
      toast.success("Đã import từ URL");
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_STATS_V3_KEY] });
    },
    onError: () => toast.error("Import URL thất bại"),
  });
}

// ============================================
// CRUD mutations
// ============================================

export function useUpdateMediaV3() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateMediaV3Request }) =>
      mediaV3.updateMediaV3(id, data),
    onSuccess: () => {
      toast.success("Đã cập nhật");
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_DETAIL_KEY] });
    },
    onError: () => toast.error("Cập nhật thất bại"),
  });
}

export function useSoftDeleteMedia() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => mediaV3.softDeleteMedia(id),
    onSuccess: () => {
      toast.success("Đã chuyển vào thùng rác");
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_TRASH_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_STATS_V3_KEY] });
    },
    onError: () => toast.error("Xóa thất bại"),
  });
}

export function useRestoreMedia() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => mediaV3.restoreMedia(id),
    onSuccess: () => {
      toast.success("Đã khôi phục");
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_TRASH_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_STATS_V3_KEY] });
    },
    onError: () => toast.error("Khôi phục thất bại"),
  });
}

export function usePermanentDeleteMedia() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => mediaV3.permanentDeleteMedia(id),
    onSuccess: () => {
      toast.success("Đã xóa vĩnh viễn");
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_TRASH_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_STATS_V3_KEY] });
    },
    onError: () => toast.error("Xóa thất bại"),
  });
}

export function useEmptyTrash() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => mediaV3.emptyTrash(),
    onSuccess: () => {
      toast.success("Đã dọn sạch thùng rác");
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_TRASH_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_STATS_V3_KEY] });
    },
    onError: () => toast.error("Dọn thùng rác thất bại"),
  });
}

export function useToggleFavorite() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, isFavorite }: { id: string; isFavorite: boolean }) =>
      mediaV3.toggleFavorite(id, isFavorite),
    onSuccess: (_, variables) => {
      toast.success(variables.isFavorite ? "Đã thêm vào yêu thích" : "Đã bỏ yêu thích");
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_DETAIL_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_STATS_V3_KEY] });
    },
    onError: () => toast.error("Thao tác thất bại"),
  });
}

// ============================================
// Image Processing mutations
// ============================================

export function useCropImage() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: CropImageRequest }) =>
      mediaV3.cropImage(id, data),
    onSuccess: () => {
      toast.success("Đã cắt ảnh");
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_DETAIL_KEY] });
    },
    onError: () => toast.error("Cắt ảnh thất bại"),
  });
}

export function useResizeImage() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: ResizeImageRequest }) =>
      mediaV3.resizeImage(id, data),
    onSuccess: () => {
      toast.success("Đã resize ảnh");
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_DETAIL_KEY] });
    },
    onError: () => toast.error("Resize thất bại"),
  });
}

export function useRotateImage() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: RotateImageRequest }) =>
      mediaV3.rotateImage(id, data),
    onSuccess: () => {
      toast.success("Đã xoay ảnh");
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_DETAIL_KEY] });
    },
    onError: () => toast.error("Xoay ảnh thất bại"),
  });
}

export function useSetFocalPoint() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: SetFocalPointRequest }) =>
      mediaV3.setFocalPoint(id, data),
    onSuccess: () => {
      toast.success("Đã cập nhật focal point");
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_DETAIL_KEY] });
    },
    onError: () => toast.error("Cập nhật focal point thất bại"),
  });
}

export function useRegenerateVariants() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => mediaV3.regenerateVariants(id),
    onSuccess: () => {
      toast.success("Đang tạo lại variants...");
      queryClient.invalidateQueries({ queryKey: [MEDIA_VARIANTS_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_DETAIL_KEY] });
    },
    onError: () => toast.error("Tạo lại variants thất bại"),
  });
}

// ============================================
// Folder hooks
// ============================================

export function useFolderTree() {
  return useQuery({
    queryKey: [FOLDERS_KEY, "tree"],
    queryFn: () => mediaV3.getFolderTree(),
  });
}

export function useCreateFolder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateFolderRequest) => mediaV3.createFolder(data),
    onSuccess: () => {
      toast.success("Đã tạo thư mục");
      queryClient.invalidateQueries({ queryKey: [FOLDERS_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_STATS_V3_KEY] });
    },
    onError: () => toast.error("Tạo thư mục thất bại"),
  });
}

export function useUpdateFolder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateFolderRequest }) =>
      mediaV3.updateFolder(id, data),
    onSuccess: () => {
      toast.success("Đã cập nhật thư mục");
      queryClient.invalidateQueries({ queryKey: [FOLDERS_KEY] });
    },
    onError: () => toast.error("Cập nhật thư mục thất bại"),
  });
}

export function useDeleteFolder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => mediaV3.deleteFolder(id),
    onSuccess: () => {
      toast.success("Đã xóa thư mục");
      queryClient.invalidateQueries({ queryKey: [FOLDERS_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_STATS_V3_KEY] });
    },
    onError: () => toast.error("Xóa thư mục thất bại"),
  });
}

export function useMoveMediaToFolder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: MoveFolderRequest) => mediaV3.moveMediaToFolder(data),
    onSuccess: () => {
      toast.success("Đã di chuyển vào thư mục");
      queryClient.invalidateQueries({ queryKey: [MEDIA_V3_KEY] });
      queryClient.invalidateQueries({ queryKey: [FOLDERS_KEY] });
    },
    onError: () => toast.error("Di chuyển thất bại"),
  });
}

// ============================================
// Album hooks
// ============================================

export function useAlbums(page = 1, limit = 20) {
  return useQuery({
    queryKey: [ALBUMS_KEY, page, limit],
    queryFn: () => mediaV3.getAlbums(page, limit),
  });
}

export function useAlbumMedia(albumId: string | null, page = 1, limit = 50) {
  return useQuery({
    queryKey: [ALBUM_MEDIA_KEY, albumId, page, limit],
    queryFn: () => mediaV3.getAlbumMedia(albumId!, page, limit),
    enabled: !!albumId,
  });
}

export function useCreateAlbum() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateAlbumRequest) => mediaV3.createAlbum(data),
    onSuccess: () => {
      toast.success("Đã tạo album");
      queryClient.invalidateQueries({ queryKey: [ALBUMS_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_STATS_V3_KEY] });
    },
    onError: () => toast.error("Tạo album thất bại"),
  });
}

export function useUpdateAlbum() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateAlbumRequest }) =>
      mediaV3.updateAlbum(id, data),
    onSuccess: () => {
      toast.success("Đã cập nhật album");
      queryClient.invalidateQueries({ queryKey: [ALBUMS_KEY] });
    },
    onError: () => toast.error("Cập nhật album thất bại"),
  });
}

export function useDeleteAlbum() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => mediaV3.deleteAlbum(id),
    onSuccess: () => {
      toast.success("Đã xóa album");
      queryClient.invalidateQueries({ queryKey: [ALBUMS_KEY] });
      queryClient.invalidateQueries({ queryKey: [MEDIA_STATS_V3_KEY] });
    },
    onError: () => toast.error("Xóa album thất bại"),
  });
}

export function useAddMediaToAlbum() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ albumId, data }: { albumId: string; data: AlbumMediaRequest }) =>
      mediaV3.addMediaToAlbum(albumId, data),
    onSuccess: () => {
      toast.success("Đã thêm vào album");
      queryClient.invalidateQueries({ queryKey: [ALBUM_MEDIA_KEY] });
      queryClient.invalidateQueries({ queryKey: [ALBUMS_KEY] });
    },
    onError: () => toast.error("Thêm vào album thất bại"),
  });
}

export function useRemoveMediaFromAlbum() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ albumId, data }: { albumId: string; data: AlbumMediaRequest }) =>
      mediaV3.removeMediaFromAlbum(albumId, data),
    onSuccess: () => {
      toast.success("Đã xóa khỏi album");
      queryClient.invalidateQueries({ queryKey: [ALBUM_MEDIA_KEY] });
      queryClient.invalidateQueries({ queryKey: [ALBUMS_KEY] });
    },
    onError: () => toast.error("Xóa khỏi album thất bại"),
  });
}

export function useReorderAlbumMedia() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ albumId, data }: { albumId: string; data: ReorderAlbumRequest }) =>
      mediaV3.reorderAlbumMedia(albumId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [ALBUM_MEDIA_KEY] });
    },
    onError: () => toast.error("Sắp xếp thất bại"),
  });
}

// ============================================
// Attachment hooks
// ============================================

export function useAttachMedia() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: AttachMediaRequest) => mediaV3.attachMedia(data),
    onSuccess: () => {
      toast.success("Đã đính kèm media");
      queryClient.invalidateQueries({ queryKey: [MEDIA_USAGES_KEY] });
    },
    onError: () => toast.error("Đính kèm thất bại"),
  });
}

export function useDetachMedia() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: DetachMediaRequest) => mediaV3.detachMedia(data),
    onSuccess: () => {
      toast.success("Đã gỡ đính kèm");
      queryClient.invalidateQueries({ queryKey: [MEDIA_USAGES_KEY] });
    },
    onError: () => toast.error("Gỡ đính kèm thất bại"),
  });
}

export function useEntityAttachments(entityType: string, entityId: string) {
  return useQuery({
    queryKey: ["entity-attachments", entityType, entityId],
    queryFn: () => mediaV3.getEntityAttachments(entityType, entityId),
    enabled: !!entityType && !!entityId,
  });
}

// ============================================
// Analytics hooks
// ============================================

export function useMediaStatsV3() {
  return useQuery({
    queryKey: [MEDIA_STATS_V3_KEY],
    queryFn: () => mediaV3.getMediaStatsV3(),
  });
}

export function useDuplicateMedia() {
  return useQuery({
    queryKey: ["media-duplicates"],
    queryFn: () => mediaV3.getDuplicates(),
  });
}

export function useUnusedMedia() {
  return useQuery({
    queryKey: ["media-unused"],
    queryFn: () => mediaV3.getUnused(),
  });
}
