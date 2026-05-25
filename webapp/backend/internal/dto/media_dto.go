package dto

// ============================================
// Media V3 DTOs
// ============================================

// UpdateMediaRequest — update media metadata (V3 extended)
type UpdateMediaRequest struct {
	Alt         *string  `json:"alt,omitempty"`
	Description *string  `json:"description,omitempty"`
	Caption     *string  `json:"caption,omitempty"`
	Credit      *string  `json:"credit,omitempty"`
	Folder      *string  `json:"folder,omitempty"`
	FolderID    *string  `json:"folder_id,omitempty"`
	IsPublic    *bool    `json:"is_public,omitempty"`
	IsFavorite  *bool    `json:"is_favorite,omitempty"`
	Tags        []string `json:"tags,omitempty"`
}

// SetFocalPointRequest — set image focal point for smart cropping
type SetFocalPointRequest struct {
	X float64 `json:"x" validate:"required,min=0,max=1"`
	Y float64 `json:"y" validate:"required,min=0,max=1"`
}

// CropImageRequest — crop an image
type CropImageRequest struct {
	X      int `json:"x" validate:"required,min=0"`
	Y      int `json:"y" validate:"required,min=0"`
	Width  int `json:"width" validate:"required,min=1"`
	Height int `json:"height" validate:"required,min=1"`
}

// ResizeImageRequest — resize an image
type ResizeImageRequest struct {
	Width  int    `json:"width" validate:"required,min=1,max=10000"`
	Height int    `json:"height" validate:"required,min=1,max=10000"`
	Fit    string `json:"fit" validate:"omitempty,oneof=cover contain fill"` // default: cover
}

// RotateImageRequest — rotate an image
type RotateImageRequest struct {
	Angle int `json:"angle" validate:"required,oneof=90 180 270 -90 -180 -270"`
}

// WatermarkRequest — apply watermark to image
type WatermarkRequest struct {
	Text     string  `json:"text,omitempty"`
	ImageURL string  `json:"image_url,omitempty"`
	Position string  `json:"position" validate:"omitempty,oneof=top-left top-right bottom-left bottom-right center"` // default: bottom-right
	Opacity  float64 `json:"opacity" validate:"omitempty,min=0,max=1"`                                               // default: 0.5
}

// UploadFromURLRequest — import media from external URL
type UploadFromURLRequest struct {
	URL     string `json:"url" validate:"required,url"`
	Folder  string `json:"folder,omitempty"`
	Alt     string `json:"alt,omitempty"`
	Caption string `json:"caption,omitempty"`
}

// ============================================
// Chunk Upload DTOs
// ============================================

// InitChunkUploadRequest — start a chunk upload session
type InitChunkUploadRequest struct {
	Filename    string `json:"filename" validate:"required"`
	TotalSize   int64  `json:"total_size" validate:"required,min=1"`
	TotalChunks int    `json:"total_chunks" validate:"required,min=1"`
	ChunkSize   int64  `json:"chunk_size" validate:"required,min=1"`
	MimeType    string `json:"mime_type" validate:"required"`
	Folder      string `json:"folder,omitempty"`
}

// InitChunkUploadResponse — response after starting chunk upload
type InitChunkUploadResponse struct {
	UploadID string `json:"upload_id"`
	Message  string `json:"message"`
}

// CompleteChunkUploadResponse — response after completing chunk upload
type CompleteChunkUploadResponse struct {
	MediaID string `json:"media_id"`
	URL     string `json:"url"`
	Message string `json:"message"`
}

// ============================================
// Folder DTOs
// ============================================

// CreateFolderRequest — create a new media folder
type CreateFolderRequest struct {
	Name     string  `json:"name" validate:"required,min=1,max=100"`
	ParentID *string `json:"parent_id,omitempty"` // nil = root folder
	Icon     string  `json:"icon,omitempty"`
	Color    string  `json:"color,omitempty"`
}

// UpdateFolderRequest — update a media folder
type UpdateFolderRequest struct {
	Name  *string `json:"name,omitempty" validate:"omitempty,min=1,max=100"`
	Icon  *string `json:"icon,omitempty"`
	Color *string `json:"color,omitempty"`
}

// MoveFolderRequest — move media to a different folder
type MoveFolderRequest struct {
	MediaIDs []string `json:"media_ids" validate:"required,min=1"`
	FolderID string   `json:"folder_id" validate:"required"`
}

// ============================================
// Album DTOs
// ============================================

// CreateAlbumRequest — create a new media album
type CreateAlbumRequest struct {
	Title       string   `json:"title" validate:"required,min=1,max=200"`
	Description string   `json:"description,omitempty"`
	Visibility  string   `json:"visibility" validate:"omitempty,oneof=public private unlisted"` // default: private
	EntityType  string   `json:"entity_type,omitempty"`
	EntityID    string   `json:"entity_id,omitempty"`
	Tags        []string `json:"tags,omitempty"`
}

// UpdateAlbumRequest — update an album
type UpdateAlbumRequest struct {
	Title        *string  `json:"title,omitempty" validate:"omitempty,min=1,max=200"`
	Description  *string  `json:"description,omitempty"`
	Visibility   *string  `json:"visibility,omitempty" validate:"omitempty,oneof=public private unlisted"`
	CoverMediaID *string  `json:"cover_media_id,omitempty"`
	Tags         []string `json:"tags,omitempty"`
}

// AlbumMediaRequest — add/remove media to/from album
type AlbumMediaRequest struct {
	MediaIDs []string `json:"media_ids" validate:"required,min=1"`
}

// ReorderAlbumRequest — reorder media within an album
type ReorderAlbumRequest struct {
	Items []ReorderItem `json:"items" validate:"required,min=1"`
}

// ReorderItem — a single reorder operation
type ReorderItem struct {
	MediaID   string `json:"media_id" validate:"required"`
	SortOrder int    `json:"sort_order" validate:"min=0"`
}

// ============================================
// Attachment DTOs
// ============================================

// AttachMediaRequest — attach media to a content entity
type AttachMediaRequest struct {
	MediaID        string `json:"media_id" validate:"required"`
	EntityType     string `json:"entity_type" validate:"required,oneof=article event festival person quote"`
	EntityID       string `json:"entity_id" validate:"required"`
	AttachmentType string `json:"attachment_type" validate:"required,oneof=featured_image og_image gallery content_image"`
	Caption        string `json:"caption,omitempty"`
	Credit         string `json:"credit,omitempty"`
}

// DetachMediaRequest — remove media attachment from entity
type DetachMediaRequest struct {
	MediaID    string `json:"media_id" validate:"required"`
	EntityType string `json:"entity_type" validate:"required"`
	EntityID   string `json:"entity_id" validate:"required"`
}

// ============================================
// Analytics DTOs
// ============================================

// MediaStatsResponse — extended media stats
type MediaStatsResponse struct {
	TotalFiles    int64            `json:"total_files"`
	TotalSize     int64            `json:"total_size"`
	MaxSize       int64            `json:"max_size"`
	ByType        map[string]int64 `json:"by_type"`
	ByMime        map[string]int64 `json:"by_mime"`
	FolderCount   int64            `json:"folder_count"`
	AlbumCount    int64            `json:"album_count"`
	FavoriteCount int64            `json:"favorite_count"`
	TrashCount    int64            `json:"trash_count"`
}

// MediaListParams — query parameters for listing media
type MediaListParams struct {
	Page       int    `query:"page"`
	Limit      int    `query:"limit"`
	Folder     string `query:"folder"`
	FolderID   string `query:"folder_id"`
	MimeFilter string `query:"type"`
	MediaType  string `query:"media_type"`
	Search     string `query:"search"`
	Tag        string `query:"tag"`
	Favorite   *bool  `query:"favorite"`
	SortBy     string `query:"sort_by"`    // "created_at","size","name"
	SortOrder  string `query:"sort_order"` // "asc","desc"
	Trash      bool   `query:"trash"`
}
