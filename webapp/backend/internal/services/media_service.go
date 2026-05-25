package services

import (
	"context"
	"fmt"
	"io"
	"mime/multipart"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/config"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/utils"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.uber.org/zap"
)

// MediaService handles file upload and management business logic
type MediaService struct {
	mediaRepo *repositories.MediaRepository
	cfg       *config.UploadConfig
	appCfg    *config.AppConfig
	logger    *zap.Logger
}

// NewMediaService creates a new MediaService
func NewMediaService(
	mediaRepo *repositories.MediaRepository,
	cfg *config.UploadConfig,
	appCfg *config.AppConfig,
	logger *zap.Logger,
) *MediaService {
	return &MediaService{
		mediaRepo: mediaRepo,
		cfg:       cfg,
		appCfg:    appCfg,
		logger:    logger,
	}
}

// Upload handles file upload
func (s *MediaService) Upload(ctx context.Context, file *multipart.FileHeader, userID, userName, folder string) (*models.MediaResponse, error) {
	// Validate file size
	if file.Size > s.cfg.MaxSize {
		return nil, utils.NewAppError(400, fmt.Sprintf("File size exceeds maximum allowed size (%d MB)", s.cfg.MaxSize/(1024*1024)))
	}

	// Validate MIME type
	mimeType := file.Header.Get("Content-Type")
	if !s.isAllowedMimeType(mimeType) {
		return nil, utils.NewAppError(400, fmt.Sprintf("File type '%s' is not allowed", mimeType))
	}

	// Generate unique filename
	ext := filepath.Ext(file.Filename)
	uniqueName := fmt.Sprintf("%s_%s%s", time.Now().Format("20060102150405"), uuid.New().String()[:8], ext)

	// Determine folder path
	if folder == "" {
		folder = "/"
	}
	folderPath := time.Now().Format("2006/01")
	if folder != "/" {
		folderPath = filepath.Join(strings.TrimPrefix(folder, "/"), folderPath)
	}

	// Create directory structure
	uploadDir := filepath.Join(s.cfg.Path, folderPath)
	if err := os.MkdirAll(uploadDir, 0755); err != nil {
		s.logger.Error("Failed to create upload directory", zap.Error(err))
		return nil, utils.ErrInternal
	}

	// Save file to disk
	filePath := filepath.Join(folderPath, uniqueName)
	fullPath := filepath.Join(s.cfg.Path, filePath)

	src, err := file.Open()
	if err != nil {
		return nil, utils.ErrInternal
	}
	defer src.Close()

	dst, err := os.Create(fullPath)
	if err != nil {
		s.logger.Error("Failed to create file", zap.Error(err))
		return nil, utils.ErrInternal
	}
	defer dst.Close()

	if _, err := io.Copy(dst, src); err != nil {
		s.logger.Error("Failed to write file", zap.Error(err))
		// Clean up on error
		os.Remove(fullPath)
		return nil, utils.ErrInternal
	}

	// Create media record
	media := models.NewMedia(file.Filename, uniqueName, filePath, mimeType, file.Size, userID)
	media.UploadedName = userName
	media.Folder = folder

	if err := s.mediaRepo.Create(ctx, media); err != nil {
		s.logger.Error("Failed to save media record", zap.Error(err))
		// Clean up file on DB error
		os.Remove(fullPath)
		return nil, utils.ErrDatabaseFail
	}

	resp := media.ToResponse(s.appCfg.URL)
	return &resp, nil
}

// UploadMultiple handles multiple file uploads
func (s *MediaService) UploadMultiple(ctx context.Context, files []*multipart.FileHeader, userID, userName, folder string) ([]models.MediaResponse, []string, error) {
	var responses []models.MediaResponse
	var errors []string

	for _, file := range files {
		resp, err := s.Upload(ctx, file, userID, userName, folder)
		if err != nil {
			errors = append(errors, fmt.Sprintf("%s: %s", file.Filename, err.Error()))
			continue
		}
		responses = append(responses, *resp)
	}

	return responses, errors, nil
}

// List returns paginated media files
func (s *MediaService) List(ctx context.Context, page, limit int, folder, mimeFilter, search string) ([]models.MediaResponse, int64, error) {
	media, total, err := s.mediaRepo.FindAll(ctx, page, limit, folder, mimeFilter, search)
	if err != nil {
		return nil, 0, utils.ErrDatabaseFail
	}

	responses := make([]models.MediaResponse, len(media))
	for i, m := range media {
		responses[i] = m.ToResponse(s.appCfg.URL)
	}

	return responses, total, nil
}

// GetByID returns a media file by ID
func (s *MediaService) GetByID(ctx context.Context, id string) (*models.MediaResponse, error) {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, utils.NewAppError(400, "Invalid media ID")
	}

	media, err := s.mediaRepo.FindByID(ctx, objID)
	if err != nil {
		return nil, utils.ErrResourceNotFound
	}

	resp := media.ToResponse(s.appCfg.URL)
	return &resp, nil
}

// Update updates media metadata
func (s *MediaService) Update(ctx context.Context, id string, alt, description, folder string) (*models.MediaResponse, error) {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, utils.NewAppError(400, "Invalid media ID")
	}

	update := bson.M{
		"updated_at": time.Now(),
	}
	if alt != "" {
		update["alt"] = alt
	}
	if description != "" {
		update["description"] = description
	}
	if folder != "" {
		update["folder"] = folder
	}

	if err := s.mediaRepo.Update(ctx, objID, update); err != nil {
		return nil, utils.ErrDatabaseFail
	}

	media, err := s.mediaRepo.FindByID(ctx, objID)
	if err != nil {
		return nil, utils.ErrDatabaseFail
	}

	resp := media.ToResponse(s.appCfg.URL)
	return &resp, nil
}

// Delete removes a media file
func (s *MediaService) Delete(ctx context.Context, id string) error {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return utils.NewAppError(400, "Invalid media ID")
	}

	media, err := s.mediaRepo.FindByID(ctx, objID)
	if err != nil {
		return utils.ErrResourceNotFound
	}

	// Delete file from disk
	fullPath := filepath.Join(s.cfg.Path, media.Path)
	if err := os.Remove(fullPath); err != nil && !os.IsNotExist(err) {
		s.logger.Warn("Failed to delete file from disk",
			zap.String("path", fullPath),
			zap.Error(err),
		)
	}

	// Delete record from DB
	if err := s.mediaRepo.Delete(ctx, objID); err != nil {
		return utils.ErrDatabaseFail
	}

	return nil
}

// DeleteMultiple removes multiple media files
func (s *MediaService) DeleteMultiple(ctx context.Context, ids []string) (int64, error) {
	objIDs := make([]primitive.ObjectID, 0, len(ids))
	for _, id := range ids {
		objID, err := primitive.ObjectIDFromHex(id)
		if err != nil {
			continue
		}
		objIDs = append(objIDs, objID)
	}

	if len(objIDs) == 0 {
		return 0, utils.NewAppError(400, "No valid IDs provided")
	}

	// Get media records to delete files
	mediaList, err := s.mediaRepo.FindByIDs(ctx, objIDs)
	if err != nil {
		return 0, utils.ErrDatabaseFail
	}

	// Delete files from disk
	for _, m := range mediaList {
		fullPath := filepath.Join(s.cfg.Path, m.Path)
		if err := os.Remove(fullPath); err != nil && !os.IsNotExist(err) {
			s.logger.Warn("Failed to delete file from disk",
				zap.String("path", fullPath),
				zap.Error(err),
			)
		}
	}

	// Delete records from DB
	count, err := s.mediaRepo.DeleteMany(ctx, objIDs)
	if err != nil {
		return 0, utils.ErrDatabaseFail
	}

	return count, nil
}

// GetFolders returns all distinct folders
func (s *MediaService) GetFolders(ctx context.Context) ([]string, error) {
	return s.mediaRepo.GetFolders(ctx)
}

// GetStats returns media storage statistics
func (s *MediaService) GetStats(ctx context.Context) (map[string]interface{}, error) {
	totalCount, totalSize, err := s.mediaRepo.GetStats(ctx)
	if err != nil {
		return nil, utils.ErrDatabaseFail
	}

	return map[string]interface{}{
		"total_files": totalCount,
		"total_size":  totalSize,
		"max_size":    s.cfg.MaxSize,
	}, nil
}

// isAllowedMimeType checks if the MIME type is in the allowed list
func (s *MediaService) isAllowedMimeType(mimeType string) bool {
	for _, allowed := range s.cfg.AllowedTypes {
		if strings.EqualFold(allowed, mimeType) {
			return true
		}
		// Support wildcard like "image/*"
		if strings.HasSuffix(allowed, "/*") {
			prefix := strings.TrimSuffix(allowed, "/*")
			if strings.HasPrefix(mimeType, prefix+"/") {
				return true
			}
		}
	}
	return false
}
