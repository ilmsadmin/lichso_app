package handlers

import (
	"context"
	"strconv"

	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// MediaHandler handles file management HTTP requests
type MediaHandler struct {
	mediaService *services.MediaService
	logger       *zap.Logger
}

// NewMediaHandler creates a new MediaHandler
func NewMediaHandler(mediaService *services.MediaService, logger *zap.Logger) *MediaHandler {
	return &MediaHandler{
		mediaService: mediaService,
		logger:       logger,
	}
}

// Upload handles POST /api/admin/media/upload
func (h *MediaHandler) Upload(c *fiber.Ctx) error {
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
	resp, err := h.mediaService.Upload(ctx, file, userID, userEmail, folder)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to upload file", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.CreatedResponse(c, "File uploaded successfully", resp)
}

// UploadMultiple handles POST /api/admin/media/upload-multiple
func (h *MediaHandler) UploadMultiple(c *fiber.Ctx) error {
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
	responses, uploadErrors, err := h.mediaService.UploadMultiple(ctx, files, userID, userEmail, folder)
	if err != nil {
		h.logger.Error("Failed to upload files", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Files processed", fiber.Map{
		"uploaded": responses,
		"errors":   uploadErrors,
		"total":    len(files),
		"success":  len(responses),
		"failed":   len(uploadErrors),
	})
}

// List handles GET /api/admin/media
func (h *MediaHandler) List(c *fiber.Ctx) error {
	page, _ := strconv.Atoi(c.Query("page", "1"))
	limit, _ := strconv.Atoi(c.Query("limit", "24"))
	folder := c.Query("folder")
	mimeFilter := c.Query("type")
	search := c.Query("search")

	if page < 1 {
		page = 1
	}
	if limit < 1 || limit > 100 {
		limit = 24
	}

	ctx := context.Background()
	media, total, err := h.mediaService.List(ctx, page, limit, folder, mimeFilter, search)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to list media", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.PaginatedResponse(c, "Media retrieved successfully", media, page, limit, total)
}

// Get handles GET /api/admin/media/:id
func (h *MediaHandler) Get(c *fiber.Ctx) error {
	id := c.Params("id")

	ctx := context.Background()
	media, err := h.mediaService.GetByID(ctx, id)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to get media", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Media retrieved successfully", media)
}

// Update handles PUT /api/admin/media/:id
func (h *MediaHandler) Update(c *fiber.Ctx) error {
	id := c.Params("id")

	var req struct {
		Alt         string `json:"alt"`
		Description string `json:"description"`
		Folder      string `json:"folder"`
	}

	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	ctx := context.Background()
	media, err := h.mediaService.Update(ctx, id, req.Alt, req.Description, req.Folder)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to update media", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Media updated successfully", media)
}

// Delete handles DELETE /api/admin/media/:id
func (h *MediaHandler) Delete(c *fiber.Ctx) error {
	id := c.Params("id")

	ctx := context.Background()
	if err := h.mediaService.Delete(ctx, id); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to delete media", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Media deleted successfully", nil)
}

// DeleteMultiple handles POST /api/admin/media/delete-multiple
func (h *MediaHandler) DeleteMultiple(c *fiber.Ctx) error {
	var req struct {
		IDs []string `json:"ids"`
	}

	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	if len(req.IDs) == 0 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "No IDs provided")
	}

	ctx := context.Background()
	count, err := h.mediaService.DeleteMultiple(ctx, req.IDs)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Failed to delete media", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Media deleted successfully", fiber.Map{
		"deleted_count": count,
	})
}

// GetFolders handles GET /api/admin/media/folders
func (h *MediaHandler) GetFolders(c *fiber.Ctx) error {
	ctx := context.Background()
	folders, err := h.mediaService.GetFolders(ctx)
	if err != nil {
		h.logger.Error("Failed to get folders", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Folders retrieved successfully", folders)
}

// GetStats handles GET /api/admin/media/stats
func (h *MediaHandler) GetStats(c *fiber.Ctx) error {
	ctx := context.Background()
	stats, err := h.mediaService.GetStats(ctx)
	if err != nil {
		h.logger.Error("Failed to get media stats", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Media stats retrieved successfully", stats)
}
