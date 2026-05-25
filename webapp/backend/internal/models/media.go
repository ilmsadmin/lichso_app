package models

import (
	"fmt"
	"path/filepath"
	"strings"
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

// Dimensions represents width/height of an image
type Dimensions struct {
	Width  int `bson:"width" json:"width"`
	Height int `bson:"height" json:"height"`
}

// FocalPoint represents the focal point coordinates for smart cropping
type FocalPoint struct {
	X float64 `bson:"x" json:"x"`
	Y float64 `bson:"y" json:"y"`
}

// Media represents a file stored in MongoDB
type Media struct {
	ID           primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	Filename     string             `bson:"filename" json:"filename"`
	OriginalName string             `bson:"original_name" json:"original_name"`
	Path         string             `bson:"path" json:"path"`
	MimeType     string             `bson:"mime_type" json:"mime_type"`
	Size         int64              `bson:"size" json:"size"`
	Extension    string             `bson:"extension" json:"extension"`
	UploadedBy   string             `bson:"uploaded_by" json:"uploaded_by"`
	UploadedName string             `bson:"uploaded_name,omitempty" json:"uploaded_name,omitempty"`
	Alt          string             `bson:"alt,omitempty" json:"alt,omitempty"`
	Description  string             `bson:"description,omitempty" json:"description,omitempty"`
	Folder       string             `bson:"folder" json:"folder"`
	IsPublic     bool               `bson:"is_public" json:"is_public"`
	CreatedAt    time.Time          `bson:"created_at" json:"created_at"`
	UpdatedAt    time.Time          `bson:"updated_at" json:"updated_at"`

	// --- V3 Fields ---
	MediaType     string              `bson:"media_type,omitempty" json:"media_type,omitempty"`         // "image","video","audio","document","archive"
	SourceType    string              `bson:"source_type,omitempty" json:"source_type,omitempty"`       // "upload","url","api","seed"
	Dimensions    *Dimensions         `bson:"dimensions,omitempty" json:"dimensions,omitempty"`         // image dimensions
	ExifData      map[string]any      `bson:"exif_data,omitempty" json:"exif_data,omitempty"`           // EXIF metadata
	BlurHash      string              `bson:"blur_hash,omitempty" json:"blur_hash,omitempty"`           // BlurHash placeholder
	FocalPoint    *FocalPoint         `bson:"focal_point,omitempty" json:"focal_point,omitempty"`       // smart crop focal point
	DominantColor string              `bson:"dominant_color,omitempty" json:"dominant_color,omitempty"` // e.g. "#3a7bd5"
	Duration      float64             `bson:"duration,omitempty" json:"duration,omitempty"`             // for video/audio (seconds)
	SourceURL     string              `bson:"source_url,omitempty" json:"source_url,omitempty"`         // original URL if imported
	Tags          []string            `bson:"tags,omitempty" json:"tags,omitempty"`                     // manual tags
	AutoTags      []string            `bson:"auto_tags,omitempty" json:"auto_tags,omitempty"`           // AI-generated tags
	FileHash      string              `bson:"file_hash,omitempty" json:"file_hash,omitempty"`           // SHA-256 hash for dedup
	FolderID      *primitive.ObjectID `bson:"folder_id,omitempty" json:"folder_id,omitempty"`           // link to media_folders
	UsageCount    int                 `bson:"usage_count" json:"usage_count"`                           // number of entity references
	IsFavorite    bool                `bson:"is_favorite" json:"is_favorite"`
	Caption       string              `bson:"caption,omitempty" json:"caption,omitempty"`
	Credit        string              `bson:"credit,omitempty" json:"credit,omitempty"`
	ProcessStatus string              `bson:"process_status,omitempty" json:"process_status,omitempty"` // "pending","processing","completed","failed"
	DeletedAt     *time.Time          `bson:"deleted_at,omitempty" json:"deleted_at,omitempty"`         // soft delete
}

// CollectionName returns the MongoDB collection name
func (Media) CollectionName() string {
	return "media"
}

// MediaResponse represents the media data returned in API responses
type MediaResponse struct {
	ID            string         `json:"id"`
	Filename      string         `json:"filename"`
	OriginalName  string         `json:"original_name"`
	URL           string         `json:"url"`
	MimeType      string         `json:"mime_type"`
	Size          int64          `json:"size"`
	Extension     string         `json:"extension"`
	UploadedBy    string         `json:"uploaded_by"`
	UploadedName  string         `json:"uploaded_name,omitempty"`
	Alt           string         `json:"alt,omitempty"`
	Description   string         `json:"description,omitempty"`
	Folder        string         `json:"folder"`
	IsPublic      bool           `json:"is_public"`
	CreatedAt     time.Time      `json:"created_at"`
	UpdatedAt     time.Time      `json:"updated_at"`
	MediaType     string         `json:"media_type,omitempty"`
	Dimensions    *Dimensions    `json:"dimensions,omitempty"`
	BlurHash      string         `json:"blur_hash,omitempty"`
	FocalPoint    *FocalPoint    `json:"focal_point,omitempty"`
	DominantColor string         `json:"dominant_color,omitempty"`
	Duration      float64        `json:"duration,omitempty"`
	Tags          []string       `json:"tags,omitempty"`
	FileHash      string         `json:"file_hash,omitempty"`
	UsageCount    int            `json:"usage_count"`
	IsFavorite    bool           `json:"is_favorite"`
	Caption       string         `json:"caption,omitempty"`
	Credit        string         `json:"credit,omitempty"`
	ProcessStatus string         `json:"process_status,omitempty"`
	Variants      []MediaVariant `json:"variants,omitempty"` // populated on detail view
}

// ToResponse converts Media to MediaResponse.
// The URL is always a host-independent relative path: /api/uploads/...
// The frontend is responsible for prepending the correct host when rendering.
func (m *Media) ToResponse(_ string) MediaResponse {
	return MediaResponse{
		ID:            m.ID.Hex(),
		Filename:      m.Filename,
		OriginalName:  m.OriginalName,
		URL:           "/api/uploads/" + m.Path,
		MimeType:      m.MimeType,
		Size:          m.Size,
		Extension:     m.Extension,
		UploadedBy:    m.UploadedBy,
		UploadedName:  m.UploadedName,
		Alt:           m.Alt,
		Description:   m.Description,
		Folder:        m.Folder,
		IsPublic:      m.IsPublic,
		CreatedAt:     m.CreatedAt,
		UpdatedAt:     m.UpdatedAt,
		MediaType:     m.MediaType,
		Dimensions:    m.Dimensions,
		BlurHash:      m.BlurHash,
		FocalPoint:    m.FocalPoint,
		DominantColor: m.DominantColor,
		Duration:      m.Duration,
		Tags:          m.Tags,
		FileHash:      m.FileHash,
		UsageCount:    m.UsageCount,
		IsFavorite:    m.IsFavorite,
		Caption:       m.Caption,
		Credit:        m.Credit,
		ProcessStatus: m.ProcessStatus,
	}
}

// NewMedia creates a new Media instance
func NewMedia(originalName, filename, path, mimeType string, size int64, uploadedBy string) *Media {
	ext := strings.ToLower(strings.TrimPrefix(filepath.Ext(originalName), "."))
	mediaType := DetectMediaType(mimeType)
	return &Media{
		Filename:      filename,
		OriginalName:  originalName,
		Path:          path,
		MimeType:      mimeType,
		Size:          size,
		Extension:     ext,
		UploadedBy:    uploadedBy,
		Folder:        "/",
		IsPublic:      true,
		MediaType:     mediaType,
		SourceType:    "upload",
		ProcessStatus: "pending",
		CreatedAt:     time.Now(),
		UpdatedAt:     time.Now(),
	}
}

// DetectMediaType returns the media type string based on MIME type
func DetectMediaType(mimeType string) string {
	switch {
	case strings.HasPrefix(mimeType, "image/"):
		return "image"
	case strings.HasPrefix(mimeType, "video/"):
		return "video"
	case strings.HasPrefix(mimeType, "audio/"):
		return "audio"
	case strings.Contains(mimeType, "pdf") || strings.Contains(mimeType, "document") ||
		strings.Contains(mimeType, "text") || strings.Contains(mimeType, "spreadsheet") ||
		strings.Contains(mimeType, "presentation"):
		return "document"
	case strings.Contains(mimeType, "zip") || strings.Contains(mimeType, "rar") ||
		strings.Contains(mimeType, "tar") || strings.Contains(mimeType, "gzip"):
		return "archive"
	default:
		return "document"
	}
}

// IsImage returns true if the media file is an image
func (m *Media) IsImage() bool {
	return strings.HasPrefix(m.MimeType, "image/")
}

// HumanSize returns a human-readable file size
func (m *Media) HumanSize() string {
	const unit = 1024
	size := float64(m.Size)
	units := []string{"B", "KB", "MB", "GB", "TB"}
	i := 0
	for size >= unit && i < len(units)-1 {
		size /= unit
		i++
	}
	if i == 0 {
		return fmt.Sprintf("%.0f %s", size, units[i])
	}
	return fmt.Sprintf("%.1f %s", size, units[i])
}
