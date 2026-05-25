package services

import (
	"context"
	"fmt"
	"io"
	"mime/multipart"
	"net/http"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/utils"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.uber.org/zap"
)

// MediaV3Service extends media management with V3 features:
// auto-processing pipeline, variants, folders, albums, attachments, chunk uploads, analytics
type MediaV3Service struct {
	mediaRepo      *repositories.MediaRepository
	variantRepo    *repositories.MediaVariantRepository
	folderRepo     *repositories.MediaFolderRepository
	albumRepo      *repositories.MediaAlbumRepository
	attachmentRepo *repositories.MediaAttachmentRepository
	chunkRepo      *repositories.ChunkUploadRepository
	versionRepo    *repositories.MediaVersionRepository
	imgService     *ImageProcessService
	videoService   *VideoProcessService
	cacheService   *MediaCacheService
	uploadPath     string
	appURL         string
	maxSize        int64
	allowedTypes   []string
	logger         *zap.Logger
}

// NewMediaV3Service creates a new MediaV3Service
func NewMediaV3Service(
	mediaRepo *repositories.MediaRepository,
	variantRepo *repositories.MediaVariantRepository,
	folderRepo *repositories.MediaFolderRepository,
	albumRepo *repositories.MediaAlbumRepository,
	attachmentRepo *repositories.MediaAttachmentRepository,
	chunkRepo *repositories.ChunkUploadRepository,
	versionRepo *repositories.MediaVersionRepository,
	imgService *ImageProcessService,
	videoService *VideoProcessService,
	cacheService *MediaCacheService,
	uploadPath string,
	appURL string,
	maxSize int64,
	allowedTypes []string,
	logger *zap.Logger,
) *MediaV3Service {
	return &MediaV3Service{
		mediaRepo:      mediaRepo,
		variantRepo:    variantRepo,
		folderRepo:     folderRepo,
		albumRepo:      albumRepo,
		attachmentRepo: attachmentRepo,
		chunkRepo:      chunkRepo,
		versionRepo:    versionRepo,
		imgService:     imgService,
		videoService:   videoService,
		cacheService:   cacheService,
		uploadPath:     uploadPath,
		appURL:         appURL,
		maxSize:        maxSize,
		allowedTypes:   allowedTypes,
		logger:         logger,
	}
}

// ============================================
// Upload with Auto-Processing Pipeline
// ============================================

// UploadWithProcessing uploads a file and triggers image processing pipeline
func (s *MediaV3Service) UploadWithProcessing(ctx context.Context, file *multipart.FileHeader, userID, userName, folder string) (*models.MediaResponse, error) {
	// Validate file size
	if file.Size > s.maxSize {
		return nil, utils.NewAppError(400, fmt.Sprintf("File size exceeds maximum allowed size (%d MB)", s.maxSize/(1024*1024)))
	}

	mimeType := file.Header.Get("Content-Type")
	if !s.isAllowedMimeType(mimeType) {
		return nil, utils.NewAppError(400, fmt.Sprintf("File type '%s' is not allowed", mimeType))
	}

	// Generate unique filename
	ext := filepath.Ext(file.Filename)
	uniqueName := fmt.Sprintf("%s_%s%s", time.Now().Format("20060102150405"), uuid.New().String()[:8], ext)

	if folder == "" {
		folder = "/"
	}
	folderPath := time.Now().Format("2006/01")
	if folder != "/" {
		folderPath = filepath.Join(strings.TrimPrefix(folder, "/"), folderPath)
	}

	uploadDir := filepath.Join(s.uploadPath, folderPath)
	if err := os.MkdirAll(uploadDir, 0755); err != nil {
		s.logger.Error("Failed to create upload directory", zap.Error(err))
		return nil, utils.ErrInternal
	}

	filePath := filepath.Join(folderPath, uniqueName)
	fullPath := filepath.Join(s.uploadPath, filePath)

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
		os.Remove(fullPath)
		return nil, utils.ErrInternal
	}

	// Create media record
	media := models.NewMedia(file.Filename, uniqueName, filePath, mimeType, file.Size, userID)
	media.UploadedName = userName
	media.Folder = folder

	// ===== AUTO-PROCESSING PIPELINE =====

	// 1. Compute file hash (dedup check)
	fileHash, err := s.imgService.ComputeFileHash(filePath)
	if err == nil {
		media.FileHash = fileHash

		// Check for duplicates
		existing, err := s.mediaRepo.FindByHash(ctx, fileHash)
		if err == nil && existing != nil {
			// Duplicate found — remove uploaded file, return existing
			os.Remove(fullPath)
			resp := existing.ToResponse(s.appURL)
			return &resp, nil
		}

		// Store hash in Redis for quick lookup
		s.cacheService.StoreFileHash(fileHash, "pending") // will update with real ID after save
	}

	// 2. Image-specific processing
	if media.IsImage() {
		meta, err := s.imgService.ProcessImageMetadata(media)
		if err == nil {
			if meta.Dimensions != nil {
				media.Dimensions = meta.Dimensions
			}
			if meta.BlurHash != "" {
				media.BlurHash = meta.BlurHash
			}
			if meta.DominantColor != "" {
				media.DominantColor = meta.DominantColor
			}
		}
		media.ProcessStatus = "processing"
	} else if media.MediaType == "video" && s.videoService != nil && s.videoService.IsFFmpegAvailable() {
		// 2b. Video-specific processing — extract metadata
		vmeta, err := s.videoService.ExtractMetadata(media.Path)
		if err == nil {
			media.Duration = vmeta.Duration
			media.Dimensions = vmeta.Dimensions
		} else {
			s.logger.Warn("Failed to extract video metadata", zap.Error(err))
		}
		media.ProcessStatus = "processing"
	} else {
		media.ProcessStatus = "completed"
	}

	// 3. Save media record
	if err := s.mediaRepo.Create(ctx, media); err != nil {
		s.logger.Error("Failed to save media record", zap.Error(err))
		os.Remove(fullPath)
		return nil, utils.ErrDatabaseFail
	}

	// 4. Update file hash in Redis with real ID
	if media.FileHash != "" {
		s.cacheService.StoreFileHash(media.FileHash, media.ID.Hex())
	}

	// 5. Generate variants for images (synchronously for now)
	if media.IsImage() {
		variants, err := s.imgService.GenerateVariants(media)
		if err != nil {
			s.logger.Error("Failed to generate variants", zap.Error(err))
		} else if len(variants) > 0 {
			if err := s.variantRepo.CreateMany(ctx, variants); err != nil {
				s.logger.Error("Failed to save variants", zap.Error(err))
			}
		}

		// Mark as completed
		s.mediaRepo.Update(ctx, media.ID, bson.M{"process_status": "completed"})
		media.ProcessStatus = "completed"
	} else if media.MediaType == "video" && s.videoService != nil && s.videoService.IsFFmpegAvailable() {
		// 5b. Generate thumbnail variants for video
		variants, err := s.videoService.GenerateVideoVariants(media)
		if err != nil {
			s.logger.Error("Failed to generate video variants", zap.Error(err))
		} else if len(variants) > 0 {
			if err := s.variantRepo.CreateMany(ctx, variants); err != nil {
				s.logger.Error("Failed to save video variants", zap.Error(err))
			}
		}

		// Mark as completed
		s.mediaRepo.Update(ctx, media.ID, bson.M{"process_status": "completed"})
		media.ProcessStatus = "completed"
	}

	resp := media.ToResponse(s.appURL)

	// Cache the response
	s.cacheService.CacheMedia(media.ID.Hex(), &resp)

	return &resp, nil
}

// UploadFromURL imports a media file from an external URL
func (s *MediaV3Service) UploadFromURL(ctx context.Context, req dto.UploadFromURLRequest, userID, userName string) (*models.MediaResponse, error) {
	// Download the file
	httpResp, err := http.Get(req.URL)
	if err != nil {
		return nil, utils.NewAppError(400, "Failed to download file from URL")
	}
	defer httpResp.Body.Close()

	if httpResp.StatusCode != http.StatusOK {
		return nil, utils.NewAppError(400, fmt.Sprintf("URL returned status %d", httpResp.StatusCode))
	}

	contentType := httpResp.Header.Get("Content-Type")
	if contentType == "" {
		contentType = "application/octet-stream"
	}

	// Extract filename from URL
	urlParts := strings.Split(req.URL, "/")
	filename := urlParts[len(urlParts)-1]
	if idx := strings.Index(filename, "?"); idx > 0 {
		filename = filename[:idx]
	}
	if filename == "" {
		filename = fmt.Sprintf("import_%s", uuid.New().String()[:8])
	}

	ext := filepath.Ext(filename)
	uniqueName := fmt.Sprintf("%s_%s%s", time.Now().Format("20060102150405"), uuid.New().String()[:8], ext)

	folder := req.Folder
	if folder == "" {
		folder = "/"
	}
	folderPath := time.Now().Format("2006/01")
	uploadDir := filepath.Join(s.uploadPath, folderPath)
	if err := os.MkdirAll(uploadDir, 0755); err != nil {
		return nil, utils.ErrInternal
	}

	filePath := filepath.Join(folderPath, uniqueName)
	fullPath := filepath.Join(s.uploadPath, filePath)

	dst, err := os.Create(fullPath)
	if err != nil {
		return nil, utils.ErrInternal
	}
	defer dst.Close()

	written, err := io.Copy(dst, httpResp.Body)
	if err != nil {
		os.Remove(fullPath)
		return nil, utils.ErrInternal
	}

	media := models.NewMedia(filename, uniqueName, filePath, contentType, written, userID)
	media.UploadedName = userName
	media.Folder = folder
	media.SourceType = "url"
	media.SourceURL = req.URL
	if req.Alt != "" {
		media.Alt = req.Alt
	}
	if req.Caption != "" {
		media.Caption = req.Caption
	}

	// Image processing
	if media.IsImage() {
		meta, err := s.imgService.ProcessImageMetadata(media)
		if err == nil {
			if meta.Dimensions != nil {
				media.Dimensions = meta.Dimensions
			}
			if meta.BlurHash != "" {
				media.BlurHash = meta.BlurHash
			}
			if meta.DominantColor != "" {
				media.DominantColor = meta.DominantColor
			}
			if meta.FileHash != "" {
				media.FileHash = meta.FileHash
			}
		}
	}

	media.ProcessStatus = "completed"

	if err := s.mediaRepo.Create(ctx, media); err != nil {
		os.Remove(fullPath)
		return nil, utils.ErrDatabaseFail
	}

	// Generate variants for images
	if media.IsImage() {
		variants, err := s.imgService.GenerateVariants(media)
		if err == nil && len(variants) > 0 {
			s.variantRepo.CreateMany(ctx, variants)
		}
	}

	if media.FileHash != "" {
		s.cacheService.StoreFileHash(media.FileHash, media.ID.Hex())
	}

	resp := media.ToResponse(s.appURL)
	s.cacheService.CacheMedia(media.ID.Hex(), &resp)
	return &resp, nil
}

// ============================================
// V3 CRUD
// ============================================

// GetByIDV3 returns a media with variants
func (s *MediaV3Service) GetByIDV3(ctx context.Context, id string) (*models.MediaResponse, error) {
	// Check cache first
	cached, err := s.cacheService.GetCachedMedia(id)
	if err == nil && cached != nil {
		return cached, nil
	}

	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, utils.NewAppError(400, "Invalid media ID")
	}

	media, err := s.mediaRepo.FindByID(ctx, objID)
	if err != nil {
		return nil, utils.ErrResourceNotFound
	}

	resp := media.ToResponse(s.appURL)

	// Load variants
	variants, err := s.variantRepo.FindByMediaID(ctx, objID)
	if err == nil {
		resp.Variants = variants
	}

	// Cache
	s.cacheService.CacheMedia(id, &resp)

	return &resp, nil
}

// ListV3 returns paginated media with V3 filters
func (s *MediaV3Service) ListV3(ctx context.Context, params dto.MediaListParams) ([]models.MediaResponse, int64, error) {
	page := params.Page
	limit := params.Limit
	if page < 1 {
		page = 1
	}
	if limit < 1 || limit > 100 {
		limit = 24
	}

	var folderOID *primitive.ObjectID
	if params.FolderID != "" {
		oid, err := primitive.ObjectIDFromHex(params.FolderID)
		if err == nil {
			folderOID = &oid
		}
	}

	repoParams := repositories.MediaFilterParams{
		Folder:     params.Folder,
		FolderID:   folderOID,
		MediaType:  params.MediaType,
		MimeFilter: params.MimeFilter,
		Search:     params.Search,
		Tag:        params.Tag,
		Favorite:   params.Favorite,
		Trash:      params.Trash,
		SortBy:     params.SortBy,
		SortOrder:  params.SortOrder,
	}

	media, total, err := s.mediaRepo.FindAllV3(ctx, page, limit, repoParams)
	if err != nil {
		return nil, 0, utils.ErrDatabaseFail
	}

	responses := make([]models.MediaResponse, len(media))
	for i, m := range media {
		responses[i] = m.ToResponse(s.appURL)
	}

	return responses, total, nil
}

// UpdateV3 updates media metadata with V3 fields
func (s *MediaV3Service) UpdateV3(ctx context.Context, id string, req dto.UpdateMediaRequest) (*models.MediaResponse, error) {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, utils.NewAppError(400, "Invalid media ID")
	}

	update := bson.M{"updated_at": time.Now()}

	if req.Alt != nil {
		update["alt"] = *req.Alt
	}
	if req.Description != nil {
		update["description"] = *req.Description
	}
	if req.Caption != nil {
		update["caption"] = *req.Caption
	}
	if req.Credit != nil {
		update["credit"] = *req.Credit
	}
	if req.Folder != nil {
		update["folder"] = *req.Folder
	}
	if req.FolderID != nil {
		if *req.FolderID == "" {
			update["folder_id"] = nil
		} else {
			oid, err := primitive.ObjectIDFromHex(*req.FolderID)
			if err == nil {
				update["folder_id"] = oid
			}
		}
	}
	if req.IsPublic != nil {
		update["is_public"] = *req.IsPublic
	}
	if req.IsFavorite != nil {
		update["is_favorite"] = *req.IsFavorite
	}
	if req.Tags != nil {
		update["tags"] = req.Tags
	}

	if err := s.mediaRepo.Update(ctx, objID, update); err != nil {
		return nil, utils.ErrDatabaseFail
	}

	// Invalidate cache
	s.cacheService.InvalidateMedia(id)

	return s.GetByIDV3(ctx, id)
}

// SoftDelete marks a media item as deleted
func (s *MediaV3Service) SoftDelete(ctx context.Context, id string) error {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return utils.NewAppError(400, "Invalid media ID")
	}

	if err := s.mediaRepo.SoftDelete(ctx, objID); err != nil {
		return utils.ErrDatabaseFail
	}

	s.cacheService.InvalidateMedia(id)
	return nil
}

// Restore restores a soft-deleted media item
func (s *MediaV3Service) Restore(ctx context.Context, id string) error {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return utils.NewAppError(400, "Invalid media ID")
	}

	if err := s.mediaRepo.Restore(ctx, objID); err != nil {
		return utils.ErrDatabaseFail
	}

	s.cacheService.InvalidateMedia(id)
	return nil
}

// PermanentDelete permanently removes a media item, its variants, and files
func (s *MediaV3Service) PermanentDelete(ctx context.Context, id string) error {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return utils.NewAppError(400, "Invalid media ID")
	}

	media, err := s.mediaRepo.FindByID(ctx, objID)
	if err != nil {
		return utils.ErrResourceNotFound
	}

	// Delete variants from disk
	variants, _ := s.variantRepo.FindByMediaID(ctx, objID)
	s.imgService.DeleteVariantFiles(variants)

	// Delete variant records
	s.variantRepo.DeleteByMediaID(ctx, objID)

	// Delete attachments
	s.attachmentRepo.DeleteByMediaID(ctx, objID)

	// Delete versions
	s.versionRepo.DeleteByMediaID(ctx, objID)

	// Delete original file
	fullPath := filepath.Join(s.uploadPath, media.Path)
	os.Remove(fullPath)

	// Delete media record
	if err := s.mediaRepo.Delete(ctx, objID); err != nil {
		return utils.ErrDatabaseFail
	}

	// Remove hash mapping
	if media.FileHash != "" {
		s.cacheService.DeleteFileHash(media.FileHash)
	}

	s.cacheService.InvalidateMedia(id)
	return nil
}

// GetTrash returns soft-deleted media items
func (s *MediaV3Service) GetTrash(ctx context.Context, page, limit int) ([]models.MediaResponse, int64, error) {
	media, total, err := s.mediaRepo.FindTrashed(ctx, page, limit)
	if err != nil {
		return nil, 0, utils.ErrDatabaseFail
	}
	responses := make([]models.MediaResponse, len(media))
	for i, m := range media {
		responses[i] = m.ToResponse(s.appURL)
	}
	return responses, total, nil
}

// EmptyTrash permanently deletes all trashed media
func (s *MediaV3Service) EmptyTrash(ctx context.Context) (int64, error) {
	trashed, err := s.mediaRepo.EmptyTrash(ctx)
	if err != nil {
		return 0, utils.ErrDatabaseFail
	}
	// Cleanup files
	for _, m := range trashed {
		fullPath := filepath.Join(s.uploadPath, m.Path)
		os.Remove(fullPath)
		// Delete variants
		variants, _ := s.variantRepo.FindByMediaID(ctx, m.ID)
		s.imgService.DeleteVariantFiles(variants)
		s.variantRepo.DeleteByMediaID(ctx, m.ID)
		s.cacheService.InvalidateMedia(m.ID.Hex())
		if m.FileHash != "" {
			s.cacheService.DeleteFileHash(m.FileHash)
		}
	}
	return int64(len(trashed)), nil
}

// ============================================
// Image Processing Operations
// ============================================

// CropImage crops a media image and creates a version
func (s *MediaV3Service) CropImage(ctx context.Context, id string, req dto.CropImageRequest, userID string) (*models.MediaResponse, error) {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, utils.NewAppError(400, "Invalid media ID")
	}

	media, err := s.mediaRepo.FindByID(ctx, objID)
	if err != nil {
		return nil, utils.ErrResourceNotFound
	}
	if !media.IsImage() {
		return nil, utils.NewAppError(400, "Media is not an image")
	}

	// Save current version
	s.saveVersion(ctx, media, userID, "Before crop")

	// Perform crop
	newPath, dims, err := s.imgService.CropImage(media, req.X, req.Y, req.Width, req.Height)
	if err != nil {
		return nil, utils.NewAppError(500, "Failed to crop image")
	}

	// Update media record
	fi, _ := os.Stat(filepath.Join(s.uploadPath, newPath))
	update := bson.M{
		"path":       newPath,
		"dimensions": dims,
		"size":       fi.Size(),
		"updated_at": time.Now(),
	}
	s.mediaRepo.Update(ctx, objID, update)

	// Regenerate variants
	s.regenerateVariants(ctx, media)

	s.cacheService.InvalidateMedia(id)
	return s.GetByIDV3(ctx, id)
}

// ResizeImage resizes a media image
func (s *MediaV3Service) ResizeImage(ctx context.Context, id string, req dto.ResizeImageRequest, userID string) (*models.MediaResponse, error) {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, utils.NewAppError(400, "Invalid media ID")
	}

	media, err := s.mediaRepo.FindByID(ctx, objID)
	if err != nil {
		return nil, utils.ErrResourceNotFound
	}
	if !media.IsImage() {
		return nil, utils.NewAppError(400, "Media is not an image")
	}

	s.saveVersion(ctx, media, userID, "Before resize")

	fit := req.Fit
	if fit == "" {
		fit = "cover"
	}

	newPath, dims, err := s.imgService.ResizeImage(media, req.Width, req.Height, fit)
	if err != nil {
		return nil, utils.NewAppError(500, "Failed to resize image")
	}

	fi, _ := os.Stat(filepath.Join(s.uploadPath, newPath))
	update := bson.M{
		"path":       newPath,
		"dimensions": dims,
		"size":       fi.Size(),
		"updated_at": time.Now(),
	}
	s.mediaRepo.Update(ctx, objID, update)
	s.regenerateVariants(ctx, media)
	s.cacheService.InvalidateMedia(id)
	return s.GetByIDV3(ctx, id)
}

// RotateImage rotates a media image
func (s *MediaV3Service) RotateImage(ctx context.Context, id string, req dto.RotateImageRequest, userID string) (*models.MediaResponse, error) {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, utils.NewAppError(400, "Invalid media ID")
	}

	media, err := s.mediaRepo.FindByID(ctx, objID)
	if err != nil {
		return nil, utils.ErrResourceNotFound
	}
	if !media.IsImage() {
		return nil, utils.NewAppError(400, "Media is not an image")
	}

	s.saveVersion(ctx, media, userID, "Before rotate")

	newPath, dims, err := s.imgService.RotateImage(media, req.Angle)
	if err != nil {
		return nil, utils.NewAppError(500, "Failed to rotate image")
	}

	fi, _ := os.Stat(filepath.Join(s.uploadPath, newPath))
	update := bson.M{
		"path":       newPath,
		"dimensions": dims,
		"size":       fi.Size(),
		"updated_at": time.Now(),
	}
	s.mediaRepo.Update(ctx, objID, update)
	s.regenerateVariants(ctx, media)
	s.cacheService.InvalidateMedia(id)
	return s.GetByIDV3(ctx, id)
}

// SetFocalPoint sets the focal point for smart cropping
func (s *MediaV3Service) SetFocalPoint(ctx context.Context, id string, req dto.SetFocalPointRequest) (*models.MediaResponse, error) {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, utils.NewAppError(400, "Invalid media ID")
	}

	update := bson.M{
		"focal_point": &models.FocalPoint{X: req.X, Y: req.Y},
		"updated_at":  time.Now(),
	}
	if err := s.mediaRepo.Update(ctx, objID, update); err != nil {
		return nil, utils.ErrDatabaseFail
	}

	s.cacheService.InvalidateMedia(id)
	return s.GetByIDV3(ctx, id)
}

// RegenerateVariants regenerates all variants for a media item
func (s *MediaV3Service) RegenerateVariants(ctx context.Context, id string) (*models.MediaResponse, error) {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, utils.NewAppError(400, "Invalid media ID")
	}

	media, err := s.mediaRepo.FindByID(ctx, objID)
	if err != nil {
		return nil, utils.ErrResourceNotFound
	}

	s.regenerateVariants(ctx, media)
	s.cacheService.InvalidateMedia(id)
	return s.GetByIDV3(ctx, id)
}

// ============================================
// Folder Management
// ============================================

// CreateFolder creates a new media folder
func (s *MediaV3Service) CreateFolder(ctx context.Context, req dto.CreateFolderRequest) (*models.MediaFolder, error) {
	slug := utils.GenerateSlug(req.Name)

	folder := &models.MediaFolder{
		Name:      req.Name,
		Slug:      slug,
		Path:      "/" + slug,
		Icon:      req.Icon,
		Color:     req.Color,
		SortOrder: 0,
	}

	if req.ParentID != nil {
		parentOID, err := primitive.ObjectIDFromHex(*req.ParentID)
		if err != nil {
			return nil, utils.NewAppError(400, "Invalid parent folder ID")
		}
		parent, err := s.folderRepo.FindByID(ctx, parentOID)
		if err != nil {
			return nil, utils.NewAppError(404, "Parent folder not found")
		}
		folder.ParentID = &parentOID
		folder.Path = parent.Path + "/" + slug
	}

	if err := s.folderRepo.Create(ctx, folder); err != nil {
		return nil, utils.ErrDatabaseFail
	}
	return folder, nil
}

// UpdateFolder updates a media folder
func (s *MediaV3Service) UpdateFolder(ctx context.Context, id string, req dto.UpdateFolderRequest) (*models.MediaFolder, error) {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, utils.NewAppError(400, "Invalid folder ID")
	}

	update := bson.M{}
	if req.Name != nil {
		update["name"] = *req.Name
		update["slug"] = utils.GenerateSlug(*req.Name)
	}
	if req.Icon != nil {
		update["icon"] = *req.Icon
	}
	if req.Color != nil {
		update["color"] = *req.Color
	}

	if err := s.folderRepo.Update(ctx, objID, update); err != nil {
		return nil, utils.ErrDatabaseFail
	}

	return s.folderRepo.FindByID(ctx, objID)
}

// DeleteFolder deletes a media folder
func (s *MediaV3Service) DeleteFolder(ctx context.Context, id string) error {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return utils.NewAppError(400, "Invalid folder ID")
	}

	folder, err := s.folderRepo.FindByID(ctx, objID)
	if err != nil {
		return utils.ErrResourceNotFound
	}
	if folder.IsSystem {
		return utils.NewAppError(403, "Cannot delete system folder")
	}

	return s.folderRepo.Delete(ctx, objID)
}

// GetFolderTree returns the folder hierarchy
func (s *MediaV3Service) GetFolderTree(ctx context.Context) ([]models.MediaFolder, error) {
	return s.folderRepo.FindAll(ctx)
}

// MoveMediaToFolder moves media items to a target folder
func (s *MediaV3Service) MoveMediaToFolder(ctx context.Context, req dto.MoveFolderRequest) error {
	folderOID, err := primitive.ObjectIDFromHex(req.FolderID)
	if err != nil {
		return utils.NewAppError(400, "Invalid folder ID")
	}

	for _, mid := range req.MediaIDs {
		objID, err := primitive.ObjectIDFromHex(mid)
		if err != nil {
			continue
		}
		s.mediaRepo.Update(ctx, objID, bson.M{"folder_id": folderOID, "updated_at": time.Now()})
		s.cacheService.InvalidateMedia(mid)
	}
	return nil
}

// ============================================
// Album Management
// ============================================

// CreateAlbum creates a new media album
func (s *MediaV3Service) CreateAlbum(ctx context.Context, req dto.CreateAlbumRequest, userID string) (*models.MediaAlbum, error) {
	slug := utils.GenerateSlug(req.Title)
	visibility := req.Visibility
	if visibility == "" {
		visibility = "private"
	}

	album := &models.MediaAlbum{
		Title:       req.Title,
		Slug:        slug,
		Description: req.Description,
		Visibility:  visibility,
		EntityType:  req.EntityType,
		Tags:        req.Tags,
		CreatedBy:   userID,
	}

	if err := s.albumRepo.Create(ctx, album); err != nil {
		return nil, utils.ErrDatabaseFail
	}
	return album, nil
}

// UpdateAlbum updates an album
func (s *MediaV3Service) UpdateAlbum(ctx context.Context, id string, req dto.UpdateAlbumRequest) (*models.MediaAlbum, error) {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, utils.NewAppError(400, "Invalid album ID")
	}

	update := bson.M{}
	if req.Title != nil {
		update["title"] = *req.Title
		update["slug"] = utils.GenerateSlug(*req.Title)
	}
	if req.Description != nil {
		update["description"] = *req.Description
	}
	if req.Visibility != nil {
		update["visibility"] = *req.Visibility
	}
	if req.CoverMediaID != nil {
		covOID, err := primitive.ObjectIDFromHex(*req.CoverMediaID)
		if err == nil {
			update["cover_media_id"] = covOID
		}
	}
	if req.Tags != nil {
		update["tags"] = req.Tags
	}

	if err := s.albumRepo.Update(ctx, objID, update); err != nil {
		return nil, utils.ErrDatabaseFail
	}

	return s.albumRepo.FindByID(ctx, objID)
}

// DeleteAlbum deletes an album and its items
func (s *MediaV3Service) DeleteAlbum(ctx context.Context, id string) error {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return utils.NewAppError(400, "Invalid album ID")
	}
	return s.albumRepo.Delete(ctx, objID)
}

// ListAlbums returns paginated albums
func (s *MediaV3Service) ListAlbums(ctx context.Context, page, limit int, visibility string) ([]models.MediaAlbum, int64, error) {
	return s.albumRepo.FindAll(ctx, page, limit, visibility)
}

// AddMediaToAlbum adds media items to an album
func (s *MediaV3Service) AddMediaToAlbum(ctx context.Context, albumID string, mediaIDs []string) error {
	albumOID, err := primitive.ObjectIDFromHex(albumID)
	if err != nil {
		return utils.NewAppError(400, "Invalid album ID")
	}

	// Get current count for sort order
	count, _ := s.albumRepo.CountItems(ctx, albumOID)

	items := make([]models.MediaAlbumItem, 0, len(mediaIDs))
	for i, mid := range mediaIDs {
		mediaOID, err := primitive.ObjectIDFromHex(mid)
		if err != nil {
			continue
		}
		items = append(items, models.MediaAlbumItem{
			AlbumID:   albumOID,
			MediaID:   mediaOID,
			SortOrder: int(count) + i,
		})
	}

	if len(items) == 0 {
		return utils.NewAppError(400, "No valid media IDs")
	}

	if err := s.albumRepo.AddItems(ctx, items); err != nil {
		return utils.ErrDatabaseFail
	}

	// Update media count
	s.albumRepo.Update(ctx, albumOID, bson.M{"media_count": int(count) + len(items)})
	return nil
}

// RemoveMediaFromAlbum removes media items from an album
func (s *MediaV3Service) RemoveMediaFromAlbum(ctx context.Context, albumID string, mediaIDs []string) error {
	albumOID, err := primitive.ObjectIDFromHex(albumID)
	if err != nil {
		return utils.NewAppError(400, "Invalid album ID")
	}

	mediaOIDs := make([]primitive.ObjectID, 0, len(mediaIDs))
	for _, mid := range mediaIDs {
		oid, err := primitive.ObjectIDFromHex(mid)
		if err != nil {
			continue
		}
		mediaOIDs = append(mediaOIDs, oid)
	}

	removed, err := s.albumRepo.RemoveItems(ctx, albumOID, mediaOIDs)
	if err != nil {
		return utils.ErrDatabaseFail
	}

	// Update media count
	count, _ := s.albumRepo.CountItems(ctx, albumOID)
	s.albumRepo.Update(ctx, albumOID, bson.M{"media_count": int(count) - int(removed)})
	return nil
}

// GetAlbumMedia returns all media items in an album
func (s *MediaV3Service) GetAlbumMedia(ctx context.Context, albumID string) ([]models.MediaResponse, error) {
	albumOID, err := primitive.ObjectIDFromHex(albumID)
	if err != nil {
		return nil, utils.NewAppError(400, "Invalid album ID")
	}

	items, err := s.albumRepo.FindItems(ctx, albumOID)
	if err != nil {
		return nil, utils.ErrDatabaseFail
	}

	// Collect media IDs
	mediaIDs := make([]primitive.ObjectID, len(items))
	for i, item := range items {
		mediaIDs[i] = item.MediaID
	}

	media, err := s.mediaRepo.FindByIDs(ctx, mediaIDs)
	if err != nil {
		return nil, utils.ErrDatabaseFail
	}

	responses := make([]models.MediaResponse, len(media))
	for i, m := range media {
		responses[i] = m.ToResponse(s.appURL)
	}
	return responses, nil
}

// ============================================
// Attachment Management
// ============================================

// AttachMedia attaches a media item to a content entity
func (s *MediaV3Service) AttachMedia(ctx context.Context, req dto.AttachMediaRequest) (*models.MediaAttachment, error) {
	mediaOID, err := primitive.ObjectIDFromHex(req.MediaID)
	if err != nil {
		return nil, utils.NewAppError(400, "Invalid media ID")
	}

	att := &models.MediaAttachment{
		MediaID:        mediaOID,
		EntityType:     req.EntityType,
		EntityID:       req.EntityID,
		AttachmentType: req.AttachmentType,
		Caption:        req.Caption,
		Credit:         req.Credit,
	}

	if err := s.attachmentRepo.Create(ctx, att); err != nil {
		return nil, utils.ErrDatabaseFail
	}

	// Increment usage count
	s.mediaRepo.IncrementUsageCount(ctx, mediaOID, 1)

	return att, nil
}

// DetachMedia removes a media attachment from a content entity
func (s *MediaV3Service) DetachMedia(ctx context.Context, req dto.DetachMediaRequest) error {
	mediaOID, err := primitive.ObjectIDFromHex(req.MediaID)
	if err != nil {
		return utils.NewAppError(400, "Invalid media ID")
	}

	if err := s.attachmentRepo.Delete(ctx, mediaOID, req.EntityType, req.EntityID); err != nil {
		return utils.ErrDatabaseFail
	}

	// Decrement usage count
	s.mediaRepo.IncrementUsageCount(ctx, mediaOID, -1)

	return nil
}

// GetEntityAttachments returns all media attached to an entity
func (s *MediaV3Service) GetEntityAttachments(ctx context.Context, entityType, entityID string) ([]models.MediaAttachment, error) {
	return s.attachmentRepo.FindByEntity(ctx, entityType, entityID)
}

// GetMediaUsages returns all entities a media is attached to
func (s *MediaV3Service) GetMediaUsages(ctx context.Context, mediaID string) ([]models.MediaAttachment, error) {
	objID, err := primitive.ObjectIDFromHex(mediaID)
	if err != nil {
		return nil, utils.NewAppError(400, "Invalid media ID")
	}
	return s.attachmentRepo.FindByMediaID(ctx, objID)
}

// ============================================
// Chunk Upload
// ============================================

// InitChunkUpload starts a new chunk upload session
func (s *MediaV3Service) InitChunkUpload(ctx context.Context, req dto.InitChunkUploadRequest, userID string) (*dto.InitChunkUploadResponse, error) {
	uploadID := uuid.New().String()

	tempDir := filepath.Join(s.uploadPath, "chunks", uploadID)
	if err := os.MkdirAll(tempDir, 0755); err != nil {
		return nil, utils.ErrInternal
	}

	chunk := &models.ChunkUpload{
		UploadID:       uploadID,
		Filename:       req.Filename,
		TotalChunks:    req.TotalChunks,
		UploadedChunks: []int{},
		TotalSize:      req.TotalSize,
		ChunkSize:      req.ChunkSize,
		MimeType:       req.MimeType,
		TempPath:       tempDir,
		UploadedBy:     userID,
		Folder:         req.Folder,
		Status:         "uploading",
		ExpiresAt:      time.Now().Add(24 * time.Hour),
	}

	if err := s.chunkRepo.Create(ctx, chunk); err != nil {
		return nil, utils.ErrDatabaseFail
	}

	return &dto.InitChunkUploadResponse{
		UploadID: uploadID,
		Message:  "Chunk upload session created",
	}, nil
}

// UploadChunk uploads a single chunk
func (s *MediaV3Service) UploadChunk(ctx context.Context, uploadID string, chunkIndex int, data io.Reader) error {
	upload, err := s.chunkRepo.FindByUploadID(ctx, uploadID)
	if err != nil {
		return utils.NewAppError(404, "Upload session not found")
	}
	if upload.Status != "uploading" {
		return utils.NewAppError(400, "Upload session is not in uploading state")
	}

	chunkPath := filepath.Join(upload.TempPath, fmt.Sprintf("chunk_%d", chunkIndex))
	f, err := os.Create(chunkPath)
	if err != nil {
		return utils.ErrInternal
	}
	defer f.Close()

	if _, err := io.Copy(f, data); err != nil {
		os.Remove(chunkPath)
		return utils.ErrInternal
	}

	return s.chunkRepo.AddChunk(ctx, uploadID, chunkIndex)
}

// CompleteChunkUpload assembles chunks and creates the media record
func (s *MediaV3Service) CompleteChunkUpload(ctx context.Context, uploadID string) (*dto.CompleteChunkUploadResponse, error) {
	upload, err := s.chunkRepo.FindByUploadID(ctx, uploadID)
	if err != nil {
		return nil, utils.NewAppError(404, "Upload session not found")
	}

	if len(upload.UploadedChunks) != upload.TotalChunks {
		return nil, utils.NewAppError(400, fmt.Sprintf("Missing chunks: uploaded %d of %d", len(upload.UploadedChunks), upload.TotalChunks))
	}

	s.chunkRepo.UpdateStatus(ctx, uploadID, "assembling")

	// Assemble chunks
	ext := filepath.Ext(upload.Filename)
	uniqueName := fmt.Sprintf("%s_%s%s", time.Now().Format("20060102150405"), uuid.New().String()[:8], ext)
	folderPath := time.Now().Format("2006/01")
	uploadDir := filepath.Join(s.uploadPath, folderPath)
	os.MkdirAll(uploadDir, 0755)

	filePath := filepath.Join(folderPath, uniqueName)
	fullPath := filepath.Join(s.uploadPath, filePath)

	dst, err := os.Create(fullPath)
	if err != nil {
		s.chunkRepo.UpdateStatus(ctx, uploadID, "failed")
		return nil, utils.ErrInternal
	}
	defer dst.Close()

	for i := 0; i < upload.TotalChunks; i++ {
		chunkPath := filepath.Join(upload.TempPath, fmt.Sprintf("chunk_%d", i))
		src, err := os.Open(chunkPath)
		if err != nil {
			s.chunkRepo.UpdateStatus(ctx, uploadID, "failed")
			return nil, utils.NewAppError(500, fmt.Sprintf("Failed to read chunk %d", i))
		}
		io.Copy(dst, src)
		src.Close()
	}

	// Get actual file size
	fi, _ := os.Stat(fullPath)

	// Create media record
	media := models.NewMedia(upload.Filename, uniqueName, filePath, upload.MimeType, fi.Size(), upload.UploadedBy)
	media.Folder = upload.Folder

	// Process image
	if media.IsImage() {
		meta, err := s.imgService.ProcessImageMetadata(media)
		if err == nil {
			media.Dimensions = meta.Dimensions
			media.BlurHash = meta.BlurHash
			media.DominantColor = meta.DominantColor
			media.FileHash = meta.FileHash
		}
	}

	media.ProcessStatus = "completed"

	if err := s.mediaRepo.Create(ctx, media); err != nil {
		return nil, utils.ErrDatabaseFail
	}

	// Generate variants
	if media.IsImage() {
		variants, err := s.imgService.GenerateVariants(media)
		if err == nil && len(variants) > 0 {
			s.variantRepo.CreateMany(ctx, variants)
		}
	}

	// Cleanup temp chunks
	os.RemoveAll(upload.TempPath)
	s.chunkRepo.UpdateStatus(ctx, uploadID, "completed")

	resp := media.ToResponse(s.appURL)
	return &dto.CompleteChunkUploadResponse{
		MediaID: media.ID.Hex(),
		URL:     resp.URL,
		Message: "Upload completed successfully",
	}, nil
}

// ============================================
// Analytics
// ============================================

// GetExtendedStats returns comprehensive media statistics
func (s *MediaV3Service) GetExtendedStats(ctx context.Context) (*dto.MediaStatsResponse, error) {
	totalCount, totalSize, err := s.mediaRepo.GetStats(ctx)
	if err != nil {
		return nil, utils.ErrDatabaseFail
	}

	byType, _ := s.mediaRepo.GetStatsByType(ctx)
	byMime, _ := s.mediaRepo.GetStatsByMime(ctx)
	folderCount, _ := s.folderRepo.Count(ctx)
	albumCount, _ := s.albumRepo.Count(ctx)
	favoriteCount, _ := s.mediaRepo.CountFavorites(ctx)
	trashCount, _ := s.mediaRepo.CountTrashed(ctx)

	return &dto.MediaStatsResponse{
		TotalFiles:    totalCount,
		TotalSize:     totalSize,
		MaxSize:       s.maxSize,
		ByType:        byType,
		ByMime:        byMime,
		FolderCount:   folderCount,
		AlbumCount:    albumCount,
		FavoriteCount: favoriteCount,
		TrashCount:    trashCount,
	}, nil
}

// GetDuplicates returns potentially duplicate files
func (s *MediaV3Service) GetDuplicates(ctx context.Context) ([]bson.M, error) {
	return s.mediaRepo.FindDuplicates(ctx)
}

// GetUnused returns unused media
func (s *MediaV3Service) GetUnused(ctx context.Context, page, limit int) ([]models.MediaResponse, int64, error) {
	media, total, err := s.mediaRepo.FindUnused(ctx, page, limit)
	if err != nil {
		return nil, 0, utils.ErrDatabaseFail
	}
	responses := make([]models.MediaResponse, len(media))
	for i, m := range media {
		responses[i] = m.ToResponse(s.appURL)
	}
	return responses, total, nil
}

// GetVariants returns all variants for a media item
func (s *MediaV3Service) GetVariants(ctx context.Context, mediaID string) ([]models.MediaVariant, error) {
	// Check cache
	cached, err := s.cacheService.GetCachedVariants(mediaID)
	if err == nil && cached != nil {
		return cached, nil
	}

	objID, err := primitive.ObjectIDFromHex(mediaID)
	if err != nil {
		return nil, utils.NewAppError(400, "Invalid media ID")
	}

	variants, err := s.variantRepo.FindByMediaID(ctx, objID)
	if err != nil {
		return nil, utils.ErrDatabaseFail
	}

	// Cache variants
	s.cacheService.CacheVariants(mediaID, variants)

	return variants, nil
}

// ============================================
// Private helpers
// ============================================

func (s *MediaV3Service) saveVersion(ctx context.Context, media *models.Media, userID, note string) {
	latestVer, _ := s.versionRepo.GetLatestVersion(ctx, media.ID)
	version := &models.MediaVersion{
		MediaID:       media.ID,
		VersionNumber: latestVer + 1,
		Path:          media.Path,
		Size:          media.Size,
		ChangedBy:     userID,
		ChangeNote:    note,
	}
	s.versionRepo.Create(ctx, version)
}

func (s *MediaV3Service) regenerateVariants(ctx context.Context, media *models.Media) {
	// Delete old variants
	oldVariants, _ := s.variantRepo.FindByMediaID(ctx, media.ID)
	s.imgService.DeleteVariantFiles(oldVariants)
	s.variantRepo.DeleteByMediaID(ctx, media.ID)

	// Re-read updated media
	updated, err := s.mediaRepo.FindByID(ctx, media.ID)
	if err != nil {
		return
	}

	// Generate new variants
	newVariants, err := s.imgService.GenerateVariants(updated)
	if err != nil {
		s.logger.Error("Failed to regenerate variants", zap.Error(err))
		return
	}
	if len(newVariants) > 0 {
		s.variantRepo.CreateMany(ctx, newVariants)
	}

	// Invalidate variant cache
	s.cacheService.InvalidateMedia(media.ID.Hex())
}

func (s *MediaV3Service) isAllowedMimeType(mimeType string) bool {
	for _, allowed := range s.allowedTypes {
		if strings.EqualFold(allowed, mimeType) {
			return true
		}
		if strings.HasSuffix(allowed, "/*") {
			prefix := strings.TrimSuffix(allowed, "/*")
			if strings.HasPrefix(mimeType, prefix+"/") {
				return true
			}
		}
	}
	return false
}
