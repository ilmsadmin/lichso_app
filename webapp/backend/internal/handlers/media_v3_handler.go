package handlers

import (
	"context"
	"strconv"

	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// MediaV3Handler handles V3 media management HTTP requests
type MediaV3Handler struct {
	mediaV3Service *services.MediaV3Service
	logger         *zap.Logger
}

// NewMediaV3Handler creates a new MediaV3Handler
func NewMediaV3Handler(mediaV3Service *services.MediaV3Service, logger *zap.Logger) *MediaV3Handler {
	return &MediaV3Handler{
		mediaV3Service: mediaV3Service,
		logger:         logger,
	}
}

// ============================================
// Upload & Processing
// ============================================

// Upload handles POST /api/admin/v3/media/upload
func (h *MediaV3Handler) Upload(c *fiber.Ctx) error {
	userID, ok := c.Locals("user_id").(string)
	if !ok || userID == "" {
		return utils.UnauthorizedResponse(c, "")
	}
	userEmail, _ := c.Locals("user_email").(string)
	folder := c.FormValue("folder", "/")

	file, err := c.FormFile("file")
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "No file provided")
	}

	ctx := context.Background()
	resp, err := h.mediaV3Service.UploadWithProcessing(ctx, file, userID, userEmail, folder)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to upload file", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.CreatedResponse(c, "File uploaded successfully", resp)
}

// UploadMultiple handles POST /api/admin/v3/media/upload-multiple
func (h *MediaV3Handler) UploadMultiple(c *fiber.Ctx) error {
	userID, ok := c.Locals("user_id").(string)
	if !ok || userID == "" {
		return utils.UnauthorizedResponse(c, "")
	}
	userEmail, _ := c.Locals("user_email").(string)
	folder := c.FormValue("folder", "/")

	form, err := c.MultipartForm()
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid multipart form")
	}

	files := form.File["files"]
	if len(files) == 0 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "No files provided")
	}

	ctx := context.Background()
	var responses []interface{}
	var uploadErrors []string

	for _, file := range files {
		resp, err := h.mediaV3Service.UploadWithProcessing(ctx, file, userID, userEmail, folder)
		if err != nil {
			uploadErrors = append(uploadErrors, file.Filename+": "+err.Error())
			continue
		}
		responses = append(responses, resp)
	}

	return utils.SuccessResponse(c, "Files processed", fiber.Map{
		"uploaded": responses,
		"errors":   uploadErrors,
		"total":    len(files),
		"success":  len(responses),
		"failed":   len(uploadErrors),
	})
}

// UploadFromURL handles POST /api/admin/v3/media/upload-url
func (h *MediaV3Handler) UploadFromURL(c *fiber.Ctx) error {
	userID, ok := c.Locals("user_id").(string)
	if !ok || userID == "" {
		return utils.UnauthorizedResponse(c, "")
	}
	userEmail, _ := c.Locals("user_email").(string)

	var req dto.UploadFromURLRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}
	if req.URL == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "URL is required")
	}

	ctx := context.Background()
	resp, err := h.mediaV3Service.UploadFromURL(ctx, req, userID, userEmail)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to upload from URL", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.CreatedResponse(c, "File imported successfully", resp)
}

// ============================================
// CRUD
// ============================================

// List handles GET /api/admin/v3/media
func (h *MediaV3Handler) List(c *fiber.Ctx) error {
	page, _ := strconv.Atoi(c.Query("page", "1"))
	limit, _ := strconv.Atoi(c.Query("limit", "24"))
	if page < 1 {
		page = 1
	}
	if limit < 1 || limit > 100 {
		limit = 24
	}

	var favorite *bool
	if fav := c.Query("favorite"); fav != "" {
		v := fav == "true" || fav == "1"
		favorite = &v
	}

	params := dto.MediaListParams{
		Page:       page,
		Limit:      limit,
		Folder:     c.Query("folder"),
		FolderID:   c.Query("folder_id"),
		MimeFilter: c.Query("type"),
		MediaType:  c.Query("media_type"),
		Search:     c.Query("search"),
		Tag:        c.Query("tag"),
		Favorite:   favorite,
		SortBy:     c.Query("sort_by"),
		SortOrder:  c.Query("sort_order"),
		Trash:      c.Query("trash") == "true",
	}

	ctx := context.Background()
	media, total, err := h.mediaV3Service.ListV3(ctx, params)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to list media", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.PaginatedResponse(c, "Media retrieved successfully", media, page, limit, total)
}

// Get handles GET /api/admin/v3/media/:id
func (h *MediaV3Handler) Get(c *fiber.Ctx) error {
	id := c.Params("id")
	ctx := context.Background()

	media, err := h.mediaV3Service.GetByIDV3(ctx, id)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to get media", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Media retrieved successfully", media)
}

// Update handles PUT /api/admin/v3/media/:id
func (h *MediaV3Handler) Update(c *fiber.Ctx) error {
	id := c.Params("id")

	var req dto.UpdateMediaRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	ctx := context.Background()
	media, err := h.mediaV3Service.UpdateV3(ctx, id, req)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to update media", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Media updated successfully", media)
}

// SoftDelete handles DELETE /api/admin/v3/media/:id
func (h *MediaV3Handler) SoftDelete(c *fiber.Ctx) error {
	id := c.Params("id")
	ctx := context.Background()

	if err := h.mediaV3Service.SoftDelete(ctx, id); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to soft delete media", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Media moved to trash", nil)
}

// Restore handles POST /api/admin/v3/media/:id/restore
func (h *MediaV3Handler) Restore(c *fiber.Ctx) error {
	id := c.Params("id")
	ctx := context.Background()

	if err := h.mediaV3Service.Restore(ctx, id); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to restore media", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Media restored successfully", nil)
}

// PermanentDelete handles DELETE /api/admin/v3/media/:id/permanent
func (h *MediaV3Handler) PermanentDelete(c *fiber.Ctx) error {
	id := c.Params("id")
	ctx := context.Background()

	if err := h.mediaV3Service.PermanentDelete(ctx, id); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to permanently delete media", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Media permanently deleted", nil)
}

// GetTrash handles GET /api/admin/v3/media/trash
func (h *MediaV3Handler) GetTrash(c *fiber.Ctx) error {
	page, _ := strconv.Atoi(c.Query("page", "1"))
	limit, _ := strconv.Atoi(c.Query("limit", "24"))
	if page < 1 {
		page = 1
	}
	if limit < 1 || limit > 100 {
		limit = 24
	}

	ctx := context.Background()
	media, total, err := h.mediaV3Service.GetTrash(ctx, page, limit)
	if err != nil {
		h.logger.Error("Failed to get trash", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.PaginatedResponse(c, "Trash retrieved successfully", media, page, limit, total)
}

// EmptyTrash handles DELETE /api/admin/v3/media/trash
func (h *MediaV3Handler) EmptyTrash(c *fiber.Ctx) error {
	ctx := context.Background()
	count, err := h.mediaV3Service.EmptyTrash(ctx)
	if err != nil {
		h.logger.Error("Failed to empty trash", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Trash emptied", fiber.Map{"deleted_count": count})
}

// ============================================
// Image Processing
// ============================================

// CropImage handles POST /api/admin/v3/media/:id/crop
func (h *MediaV3Handler) CropImage(c *fiber.Ctx) error {
	id := c.Params("id")
	userID, _ := c.Locals("user_id").(string)

	var req dto.CropImageRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	ctx := context.Background()
	resp, err := h.mediaV3Service.CropImage(ctx, id, req, userID)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to crop image", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Image cropped successfully", resp)
}

// ResizeImage handles POST /api/admin/v3/media/:id/resize
func (h *MediaV3Handler) ResizeImage(c *fiber.Ctx) error {
	id := c.Params("id")
	userID, _ := c.Locals("user_id").(string)

	var req dto.ResizeImageRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	ctx := context.Background()
	resp, err := h.mediaV3Service.ResizeImage(ctx, id, req, userID)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to resize image", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Image resized successfully", resp)
}

// RotateImage handles POST /api/admin/v3/media/:id/rotate
func (h *MediaV3Handler) RotateImage(c *fiber.Ctx) error {
	id := c.Params("id")
	userID, _ := c.Locals("user_id").(string)

	var req dto.RotateImageRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	ctx := context.Background()
	resp, err := h.mediaV3Service.RotateImage(ctx, id, req, userID)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to rotate image", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Image rotated successfully", resp)
}

// SetFocalPoint handles PUT /api/admin/v3/media/:id/focal-point
func (h *MediaV3Handler) SetFocalPoint(c *fiber.Ctx) error {
	id := c.Params("id")

	var req dto.SetFocalPointRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	ctx := context.Background()
	resp, err := h.mediaV3Service.SetFocalPoint(ctx, id, req)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to set focal point", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Focal point updated", resp)
}

// RegenerateVariants handles POST /api/admin/v3/media/:id/regenerate-variants
func (h *MediaV3Handler) RegenerateVariants(c *fiber.Ctx) error {
	id := c.Params("id")
	ctx := context.Background()

	resp, err := h.mediaV3Service.RegenerateVariants(ctx, id)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to regenerate variants", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Variants regenerated", resp)
}

// GetVariants handles GET /api/admin/v3/media/:id/variants
func (h *MediaV3Handler) GetVariants(c *fiber.Ctx) error {
	id := c.Params("id")
	ctx := context.Background()

	variants, err := h.mediaV3Service.GetVariants(ctx, id)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to get variants", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Variants retrieved", variants)
}

// ============================================
// Folders
// ============================================

// GetFolderTree handles GET /api/admin/v3/media/folders/tree
func (h *MediaV3Handler) GetFolderTree(c *fiber.Ctx) error {
	ctx := context.Background()
	folders, err := h.mediaV3Service.GetFolderTree(ctx)
	if err != nil {
		h.logger.Error("Failed to get folder tree", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Folder tree retrieved", folders)
}

// CreateFolder handles POST /api/admin/v3/media/folders
func (h *MediaV3Handler) CreateFolder(c *fiber.Ctx) error {
	var req dto.CreateFolderRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}
	if req.Name == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Name is required")
	}

	ctx := context.Background()
	folder, err := h.mediaV3Service.CreateFolder(ctx, req)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to create folder", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.CreatedResponse(c, "Folder created successfully", folder)
}

// UpdateFolder handles PUT /api/admin/v3/media/folders/:id
func (h *MediaV3Handler) UpdateFolder(c *fiber.Ctx) error {
	id := c.Params("id")

	var req dto.UpdateFolderRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	ctx := context.Background()
	folder, err := h.mediaV3Service.UpdateFolder(ctx, id, req)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to update folder", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Folder updated successfully", folder)
}

// DeleteFolder handles DELETE /api/admin/v3/media/folders/:id
func (h *MediaV3Handler) DeleteFolder(c *fiber.Ctx) error {
	id := c.Params("id")
	ctx := context.Background()

	if err := h.mediaV3Service.DeleteFolder(ctx, id); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to delete folder", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Folder deleted successfully", nil)
}

// MoveMedia handles POST /api/admin/v3/media/folders/move
func (h *MediaV3Handler) MoveMedia(c *fiber.Ctx) error {
	var req dto.MoveFolderRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	ctx := context.Background()
	if err := h.mediaV3Service.MoveMediaToFolder(ctx, req); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to move media", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Media moved successfully", nil)
}

// ============================================
// Albums
// ============================================

// ListAlbums handles GET /api/admin/v3/media/albums
func (h *MediaV3Handler) ListAlbums(c *fiber.Ctx) error {
	page, _ := strconv.Atoi(c.Query("page", "1"))
	limit, _ := strconv.Atoi(c.Query("limit", "20"))
	visibility := c.Query("visibility")

	ctx := context.Background()
	albums, total, err := h.mediaV3Service.ListAlbums(ctx, page, limit, visibility)
	if err != nil {
		h.logger.Error("Failed to list albums", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.PaginatedResponse(c, "Albums retrieved", albums, page, limit, total)
}

// CreateAlbum handles POST /api/admin/v3/media/albums
func (h *MediaV3Handler) CreateAlbum(c *fiber.Ctx) error {
	userID, _ := c.Locals("user_id").(string)

	var req dto.CreateAlbumRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}
	if req.Title == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Title is required")
	}

	ctx := context.Background()
	album, err := h.mediaV3Service.CreateAlbum(ctx, req, userID)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to create album", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.CreatedResponse(c, "Album created successfully", album)
}

// UpdateAlbum handles PUT /api/admin/v3/media/albums/:id
func (h *MediaV3Handler) UpdateAlbum(c *fiber.Ctx) error {
	id := c.Params("id")

	var req dto.UpdateAlbumRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	ctx := context.Background()
	album, err := h.mediaV3Service.UpdateAlbum(ctx, id, req)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to update album", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Album updated successfully", album)
}

// DeleteAlbum handles DELETE /api/admin/v3/media/albums/:id
func (h *MediaV3Handler) DeleteAlbum(c *fiber.Ctx) error {
	id := c.Params("id")
	ctx := context.Background()

	if err := h.mediaV3Service.DeleteAlbum(ctx, id); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to delete album", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Album deleted successfully", nil)
}

// AddMediaToAlbum handles POST /api/admin/v3/media/albums/:id/media
func (h *MediaV3Handler) AddMediaToAlbum(c *fiber.Ctx) error {
	albumID := c.Params("id")

	var req dto.AlbumMediaRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	ctx := context.Background()
	if err := h.mediaV3Service.AddMediaToAlbum(ctx, albumID, req.MediaIDs); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to add media to album", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Media added to album", nil)
}

// RemoveMediaFromAlbum handles DELETE /api/admin/v3/media/albums/:id/media
func (h *MediaV3Handler) RemoveMediaFromAlbum(c *fiber.Ctx) error {
	albumID := c.Params("id")

	var req dto.AlbumMediaRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	ctx := context.Background()
	if err := h.mediaV3Service.RemoveMediaFromAlbum(ctx, albumID, req.MediaIDs); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to remove media from album", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Media removed from album", nil)
}

// GetAlbumMedia handles GET /api/admin/v3/media/albums/:id/media
func (h *MediaV3Handler) GetAlbumMedia(c *fiber.Ctx) error {
	albumID := c.Params("id")
	ctx := context.Background()

	media, err := h.mediaV3Service.GetAlbumMedia(ctx, albumID)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to get album media", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Album media retrieved", media)
}

// ============================================
// Attachments
// ============================================

// AttachMedia handles POST /api/admin/v3/media/attach
func (h *MediaV3Handler) AttachMedia(c *fiber.Ctx) error {
	var req dto.AttachMediaRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	ctx := context.Background()
	att, err := h.mediaV3Service.AttachMedia(ctx, req)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to attach media", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.CreatedResponse(c, "Media attached successfully", att)
}

// DetachMedia handles POST /api/admin/v3/media/detach
func (h *MediaV3Handler) DetachMedia(c *fiber.Ctx) error {
	var req dto.DetachMediaRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	ctx := context.Background()
	if err := h.mediaV3Service.DetachMedia(ctx, req); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to detach media", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Media detached successfully", nil)
}

// GetEntityAttachments handles GET /api/admin/v3/media/attachments/:entity_type/:entity_id
func (h *MediaV3Handler) GetEntityAttachments(c *fiber.Ctx) error {
	entityType := c.Params("entity_type")
	entityID := c.Params("entity_id")

	ctx := context.Background()
	attachments, err := h.mediaV3Service.GetEntityAttachments(ctx, entityType, entityID)
	if err != nil {
		h.logger.Error("Failed to get attachments", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Attachments retrieved", attachments)
}

// GetMediaUsages handles GET /api/admin/v3/media/:id/usages
func (h *MediaV3Handler) GetMediaUsages(c *fiber.Ctx) error {
	id := c.Params("id")
	ctx := context.Background()

	usages, err := h.mediaV3Service.GetMediaUsages(ctx, id)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to get media usages", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Media usages retrieved", usages)
}

// ============================================
// Chunk Upload
// ============================================

// InitChunkUpload handles POST /api/admin/v3/media/chunk/init
func (h *MediaV3Handler) InitChunkUpload(c *fiber.Ctx) error {
	userID, ok := c.Locals("user_id").(string)
	if !ok || userID == "" {
		return utils.UnauthorizedResponse(c, "")
	}

	var req dto.InitChunkUploadRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	ctx := context.Background()
	resp, err := h.mediaV3Service.InitChunkUpload(ctx, req, userID)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to init chunk upload", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.CreatedResponse(c, resp.Message, resp)
}

// UploadChunk handles POST /api/admin/v3/media/chunk/:upload_id/:chunk_index
func (h *MediaV3Handler) UploadChunk(c *fiber.Ctx) error {
	uploadID := c.Params("upload_id")
	chunkIndex, err := strconv.Atoi(c.Params("chunk_index"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid chunk index")
	}

	file, err := c.FormFile("chunk")
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "No chunk data provided")
	}

	src, err := file.Open()
	if err != nil {
		return utils.InternalErrorResponse(c)
	}
	defer src.Close()

	ctx := context.Background()
	if err := h.mediaV3Service.UploadChunk(ctx, uploadID, chunkIndex, src); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to upload chunk", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Chunk uploaded", fiber.Map{"chunk_index": chunkIndex})
}

// CompleteChunkUpload handles POST /api/admin/v3/media/chunk/:upload_id/complete
func (h *MediaV3Handler) CompleteChunkUpload(c *fiber.Ctx) error {
	uploadID := c.Params("upload_id")
	ctx := context.Background()

	resp, err := h.mediaV3Service.CompleteChunkUpload(ctx, uploadID)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to complete chunk upload", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.CreatedResponse(c, resp.Message, resp)
}

// ============================================
// Analytics
// ============================================

// GetExtendedStats handles GET /api/admin/v3/media/stats
func (h *MediaV3Handler) GetExtendedStats(c *fiber.Ctx) error {
	ctx := context.Background()
	stats, err := h.mediaV3Service.GetExtendedStats(ctx)
	if err != nil {
		h.logger.Error("Failed to get media stats", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Media stats retrieved", stats)
}

// GetDuplicates handles GET /api/admin/v3/media/analytics/duplicates
func (h *MediaV3Handler) GetDuplicates(c *fiber.Ctx) error {
	ctx := context.Background()
	dupes, err := h.mediaV3Service.GetDuplicates(ctx)
	if err != nil {
		h.logger.Error("Failed to get duplicates", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Duplicates retrieved", dupes)
}

// GetUnused handles GET /api/admin/v3/media/analytics/unused
func (h *MediaV3Handler) GetUnused(c *fiber.Ctx) error {
	page, _ := strconv.Atoi(c.Query("page", "1"))
	limit, _ := strconv.Atoi(c.Query("limit", "24"))

	ctx := context.Background()
	media, total, err := h.mediaV3Service.GetUnused(ctx, page, limit)
	if err != nil {
		h.logger.Error("Failed to get unused media", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.PaginatedResponse(c, "Unused media retrieved", media, page, limit, total)
}
