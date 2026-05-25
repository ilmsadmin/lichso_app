package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

// ============================================
// MediaVariant — Resized/converted image variants
// ============================================

// MediaVariant represents a generated variant (thumbnail, WebP, etc.)
type MediaVariant struct {
	ID          primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	MediaID     primitive.ObjectID `bson:"media_id" json:"media_id"`
	VariantName string             `bson:"variant_name" json:"variant_name"` // "thumb_sm","thumb_md","thumb_lg","medium","large","og"
	Path        string             `bson:"path" json:"path"`
	MimeType    string             `bson:"mime_type" json:"mime_type"`
	Width       int                `bson:"width" json:"width"`
	Height      int                `bson:"height" json:"height"`
	Size        int64              `bson:"size" json:"size"`
	CreatedAt   time.Time          `bson:"created_at" json:"created_at"`
}

func (MediaVariant) CollectionName() string { return "media_variants" }

// ============================================
// MediaAttachment — Links media to content entities
// ============================================

// MediaAttachment links a media item to a content entity (article, event, etc.)
type MediaAttachment struct {
	ID             primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	MediaID        primitive.ObjectID `bson:"media_id" json:"media_id"`
	EntityType     string             `bson:"entity_type" json:"entity_type"`         // "article","event","festival","person","quote"
	EntityID       string             `bson:"entity_id" json:"entity_id"`             // UUID or ObjectID of linked content
	AttachmentType string             `bson:"attachment_type" json:"attachment_type"` // "featured_image","og_image","gallery","content_image"
	SortOrder      int                `bson:"sort_order" json:"sort_order"`
	Caption        string             `bson:"caption,omitempty" json:"caption,omitempty"`
	Credit         string             `bson:"credit,omitempty" json:"credit,omitempty"`
	CreatedAt      time.Time          `bson:"created_at" json:"created_at"`
}

func (MediaAttachment) CollectionName() string { return "media_attachments" }

// ============================================
// MediaAlbum — Collections of media
// ============================================

// MediaAlbum represents a curated collection of media items
type MediaAlbum struct {
	ID           primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	Title        string             `bson:"title" json:"title"`
	Slug         string             `bson:"slug" json:"slug"`
	Description  string             `bson:"description,omitempty" json:"description,omitempty"`
	CoverMediaID primitive.ObjectID `bson:"cover_media_id,omitempty" json:"cover_media_id,omitempty"`
	MediaCount   int                `bson:"media_count" json:"media_count"`
	TotalSize    int64              `bson:"total_size" json:"total_size"`
	Visibility   string             `bson:"visibility" json:"visibility"` // "public","private","unlisted"
	EntityType   string             `bson:"entity_type,omitempty" json:"entity_type,omitempty"`
	EntityID     string             `bson:"entity_id,omitempty" json:"entity_id,omitempty"`
	Tags         []string           `bson:"tags,omitempty" json:"tags,omitempty"`
	CreatedBy    string             `bson:"created_by" json:"created_by"`
	CreatedAt    time.Time          `bson:"created_at" json:"created_at"`
	UpdatedAt    time.Time          `bson:"updated_at" json:"updated_at"`
}

func (MediaAlbum) CollectionName() string { return "media_albums" }

// ============================================
// MediaAlbumItem — Items within an album
// ============================================

// MediaAlbumItem represents a single item in an album
type MediaAlbumItem struct {
	ID        primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	AlbumID   primitive.ObjectID `bson:"album_id" json:"album_id"`
	MediaID   primitive.ObjectID `bson:"media_id" json:"media_id"`
	SortOrder int                `bson:"sort_order" json:"sort_order"`
	Caption   string             `bson:"caption,omitempty" json:"caption,omitempty"`
	AddedAt   time.Time          `bson:"added_at" json:"added_at"`
}

func (MediaAlbumItem) CollectionName() string { return "media_album_items" }

// ============================================
// MediaFolder — Folder hierarchy for organizing media
// ============================================

// MediaFolder represents a folder in the media library
type MediaFolder struct {
	ID         primitive.ObjectID  `bson:"_id,omitempty" json:"id"`
	Name       string              `bson:"name" json:"name"`
	Slug       string              `bson:"slug" json:"slug"`
	ParentID   *primitive.ObjectID `bson:"parent_id,omitempty" json:"parent_id,omitempty"` // nil = root
	Path       string              `bson:"path" json:"path"`                               // e.g., "/festivals/2026"
	MediaCount int                 `bson:"media_count" json:"media_count"`
	TotalSize  int64               `bson:"total_size" json:"total_size"`
	Icon       string              `bson:"icon,omitempty" json:"icon,omitempty"`
	Color      string              `bson:"color,omitempty" json:"color,omitempty"`
	SortOrder  int                 `bson:"sort_order" json:"sort_order"`
	IsSystem   bool                `bson:"is_system" json:"is_system"` // true = cannot delete
	CreatedAt  time.Time           `bson:"created_at" json:"created_at"`
	UpdatedAt  time.Time           `bson:"updated_at" json:"updated_at"`
}

func (MediaFolder) CollectionName() string { return "media_folders" }

// ============================================
// ChunkUpload — Tracking multi-part uploads
// ============================================

// ChunkUpload tracks an in-progress chunk upload session
type ChunkUpload struct {
	ID             primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	UploadID       string             `bson:"upload_id" json:"upload_id"` // UUID
	Filename       string             `bson:"filename" json:"filename"`
	TotalChunks    int                `bson:"total_chunks" json:"total_chunks"`
	UploadedChunks []int              `bson:"uploaded_chunks" json:"uploaded_chunks"`
	TotalSize      int64              `bson:"total_size" json:"total_size"`
	ChunkSize      int64              `bson:"chunk_size" json:"chunk_size"`
	MimeType       string             `bson:"mime_type" json:"mime_type"`
	TempPath       string             `bson:"temp_path" json:"temp_path"`
	UploadedBy     string             `bson:"uploaded_by" json:"uploaded_by"`
	Folder         string             `bson:"folder" json:"folder"`
	Status         string             `bson:"status" json:"status"` // "uploading","assembling","completed","failed","expired"
	ExpiresAt      time.Time          `bson:"expires_at" json:"expires_at"`
	CreatedAt      time.Time          `bson:"created_at" json:"created_at"`
}

func (ChunkUpload) CollectionName() string { return "chunk_uploads" }

// ============================================
// MediaVersion — History of edits to a media item
// ============================================

// MediaVersion tracks historical versions of a media file
type MediaVersion struct {
	ID            primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	MediaID       primitive.ObjectID `bson:"media_id" json:"media_id"`
	VersionNumber int                `bson:"version_number" json:"version_number"`
	Path          string             `bson:"path" json:"path"`
	Size          int64              `bson:"size" json:"size"`
	ChangedBy     string             `bson:"changed_by" json:"changed_by"`
	ChangeNote    string             `bson:"change_note,omitempty" json:"change_note,omitempty"`
	CreatedAt     time.Time          `bson:"created_at" json:"created_at"`
}

func (MediaVersion) CollectionName() string { return "media_versions" }
