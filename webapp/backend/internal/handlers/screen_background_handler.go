package handlers

import (
	"fmt"
	"io"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/config"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// ScreenBackgroundHandler handles HTTP requests for screen background management.
type ScreenBackgroundHandler struct {
	repo      *repositories.ScreenBackgroundRepository
	uploadCfg *config.UploadConfig
	appCfg    *config.AppConfig
	logger    *zap.Logger
}

// NewScreenBackgroundHandler creates a new ScreenBackgroundHandler.
func NewScreenBackgroundHandler(
	repo *repositories.ScreenBackgroundRepository,
	uploadCfg *config.UploadConfig,
	appCfg *config.AppConfig,
	logger *zap.Logger,
) *ScreenBackgroundHandler {
	return &ScreenBackgroundHandler{
		repo:      repo,
		uploadCfg: uploadCfg,
		appCfg:    appCfg,
		logger:    logger,
	}
}

// List handles GET /api/admin/screen-backgrounds
// Returns all screen backgrounds (with or without image).
func (h *ScreenBackgroundHandler) List(c *fiber.Ctx) error {
	items, err := h.repo.FindAll()
	if err != nil {
		h.logger.Error("Failed to list screen backgrounds", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	responses := make([]models.ScreenBackgroundResponse, len(items))
	for i, item := range items {
		responses[i] = item.ToResponse()
	}

	return utils.SuccessResponse(c, "Screen backgrounds retrieved successfully", responses)
}

// GetPublic handles GET /api/screen-backgrounds
// Public endpoint — returns only screens that have an active background image set.
func (h *ScreenBackgroundHandler) GetPublic(c *fiber.Ctx) error {
	items, err := h.repo.FindAllActive()
	if err != nil {
		h.logger.Error("Failed to get public screen backgrounds", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	responses := make([]models.ScreenBackgroundResponse, len(items))
	for i, item := range items {
		responses[i] = item.ToResponse()
	}

	return utils.SuccessResponse(c, "Screen backgrounds retrieved successfully", responses)
}

// UploadBackground handles POST /api/admin/screen-backgrounds/:screen_key/upload
// Uploads or replaces the background image for a specific screen.
func (h *ScreenBackgroundHandler) UploadBackground(c *fiber.Ctx) error {
	screenKey := c.Params("screen_key")
	if screenKey == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Screen key is required")
	}

	userEmail, _ := c.Locals("user_email").(string)

	// Get the file from the request
	file, err := c.FormFile("image")
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "No image file provided")
	}

	// Validate file size
	if file.Size > h.uploadCfg.MaxSize {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, fmt.Sprintf(
			"File size exceeds maximum allowed size (%d MB)", h.uploadCfg.MaxSize/(1024*1024),
		))
	}

	// Validate MIME type (only images)
	mimeType := file.Header.Get("Content-Type")
	if !isAllowedImageMime(mimeType) {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, fmt.Sprintf(
			"File type '%s' is not allowed. Only JPEG, PNG, WebP and GIF images are accepted.", mimeType,
		))
	}

	// Find existing record or load by screen_key
	existing, err := h.repo.FindByKey(screenKey)
	if err != nil {
		// If not found, create a new record with sensible defaults.
		// This handles the case where a new screen_key is added that wasn't in the seed.
		h.logger.Warn("Screen key not found in DB, will create new record", zap.String("screen_key", screenKey))
		existing = &models.ScreenBackground{
			ScreenKey:  screenKey,
			ScreenName: screenKey, // fallback name
			IsActive:   true,
		}
	}

	// Delete old image file from disk if it exists
	if existing.ImagePath != "" {
		oldFullPath := filepath.Join(h.uploadCfg.Path, existing.ImagePath)
		if removeErr := os.Remove(oldFullPath); removeErr != nil && !os.IsNotExist(removeErr) {
			h.logger.Warn("Failed to remove old background image",
				zap.String("path", oldFullPath),
				zap.Error(removeErr),
			)
		}
	}

	// Generate unique filename
	ext := strings.ToLower(filepath.Ext(file.Filename))
	uniqueName := fmt.Sprintf("bg_%s_%s%s",
		screenKey,
		time.Now().Format("20060102150405"),
		ext,
	)

	// Save to disk under uploads/screen-backgrounds/
	folderPath := "screen-backgrounds"
	uploadDir := filepath.Join(h.uploadCfg.Path, folderPath)
	if mkErr := os.MkdirAll(uploadDir, 0755); mkErr != nil {
		h.logger.Error("Failed to create screen-backgrounds directory", zap.Error(mkErr))
		return utils.InternalErrorResponse(c)
	}

	relPath := filepath.Join(folderPath, uniqueName)
	fullPath := filepath.Join(h.uploadCfg.Path, relPath)

	src, err := file.Open()
	if err != nil {
		return utils.InternalErrorResponse(c)
	}
	defer src.Close()

	dst, err := os.Create(fullPath)
	if err != nil {
		h.logger.Error("Failed to create background image file", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}
	defer dst.Close()

	if _, err := io.Copy(dst, src); err != nil {
		h.logger.Error("Failed to write background image file", zap.Error(err))
		os.Remove(fullPath)
		return utils.InternalErrorResponse(c)
	}

	// Build public URL
	imageURL := fmt.Sprintf("%s/api/uploads/%s", strings.TrimRight(h.appCfg.URL, "/"), relPath)

	// Update record
	existing.ImageURL = imageURL
	existing.ImagePath = relPath
	existing.IsActive = true
	existing.UpdatedBy = userEmail
	existing.UpdatedAt = time.Now()
	if existing.ID == uuid.Nil {
		existing.ID = uuid.New()
	}

	if err := h.repo.Upsert(existing); err != nil {
		h.logger.Error("Failed to save screen background record", zap.Error(err))
		os.Remove(fullPath)
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Background image uploaded successfully", existing.ToResponse())
}

// DeleteBackground handles DELETE /api/admin/screen-backgrounds/:screen_key
// Removes the background image for a specific screen (resets to default).
func (h *ScreenBackgroundHandler) DeleteBackground(c *fiber.Ctx) error {
	screenKey := c.Params("screen_key")
	if screenKey == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Screen key is required")
	}

	existing, err := h.repo.FindByKey(screenKey)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, "Screen background not found")
	}

	// Delete file from disk
	if existing.ImagePath != "" {
		fullPath := filepath.Join(h.uploadCfg.Path, existing.ImagePath)
		if removeErr := os.Remove(fullPath); removeErr != nil && !os.IsNotExist(removeErr) {
			h.logger.Warn("Failed to remove background image from disk",
				zap.String("path", fullPath),
				zap.Error(removeErr),
			)
		}
	}

	// Clear image fields in DB
	if err := h.repo.ClearImage(screenKey); err != nil {
		h.logger.Error("Failed to clear screen background image in DB", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Background image removed successfully", nil)
}

// isAllowedImageMime checks if the MIME type is an image type accepted for backgrounds.
func isAllowedImageMime(mimeType string) bool {
	allowed := []string{
		"image/jpeg",
		"image/png",
		"image/webp",
		"image/gif",
	}
	lower := strings.ToLower(strings.TrimSpace(mimeType))
	for _, a := range allowed {
		if lower == a {
			return true
		}
	}
	return false
}

// UpdateBackgroundUrl handles PUT /api/admin/screen-backgrounds/:screen_key/url
// Updates the background image URL from a JSON payload (e.g., from Media Manager).
func (h *ScreenBackgroundHandler) UpdateBackgroundUrl(c *fiber.Ctx) error {
	screenKey := c.Params("screen_key")
	if screenKey == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Screen key is required")
	}

	userEmail, _ := c.Locals("user_email").(string)

	var payload struct {
		ImageURL string `json:"image_url"`
	}

	if err := c.BodyParser(&payload); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request payload")
	}

	// Find existing record or load by screen_key
	existing, err := h.repo.FindByKey(screenKey)
	if err != nil {
		h.logger.Warn("Screen key not found in DB, will create new record", zap.String("screen_key", screenKey))
		existing = &models.ScreenBackground{
			ScreenKey:  screenKey,
			ScreenName: screenKey,
			IsActive:   true,
		}
	}

	// Delete old image file from disk if it exists (if it was an uploaded file via the old method)
	if existing.ImagePath != "" {
		oldFullPath := filepath.Join(h.uploadCfg.Path, existing.ImagePath)
		if removeErr := os.Remove(oldFullPath); removeErr != nil && !os.IsNotExist(removeErr) {
			h.logger.Warn("Failed to remove old background image",
				zap.String("path", oldFullPath),
				zap.Error(removeErr),
			)
		}
	}

	existing.ImageURL = payload.ImageURL
	existing.ImagePath = "" // Reset path since it's managed by Media Manager or external URL
	existing.IsActive = true
	existing.UpdatedBy = userEmail
	existing.UpdatedAt = time.Now()
	if existing.ID == uuid.Nil {
		existing.ID = uuid.New()
	}

	if err := h.repo.Upsert(existing); err != nil {
		h.logger.Error("Failed to save screen background record", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Background URL updated successfully", existing.ToResponse())
}
