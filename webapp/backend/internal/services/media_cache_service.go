package services

import (
	"context"
	"encoding/json"
	"fmt"
	"time"

	"github.com/redis/go-redis/v9"
	"github.com/zplus/lichso/internal/models"
	"go.uber.org/zap"
)

const (
	mediaCachePrefix        = "cache:media:"
	mediaVariantCachePrefix = "cache:media:variants:"
	mediaHashPrefix         = "hash:media:file:"
	imageProcessQueue       = "queue:image:process"
	mediaCacheTTL           = 30 * time.Minute
	variantCacheTTL         = 1 * time.Hour
)

// MediaCacheService handles Redis caching for media operations
type MediaCacheService struct {
	client *redis.Client
	logger *zap.Logger
	ctx    context.Context
}

// NewMediaCacheService creates a new MediaCacheService
func NewMediaCacheService(client *redis.Client, logger *zap.Logger) *MediaCacheService {
	return &MediaCacheService{
		client: client,
		logger: logger,
		ctx:    context.Background(),
	}
}

// ============================================
// Media Cache
// ============================================

// CacheMedia stores a media response in Redis
func (s *MediaCacheService) CacheMedia(mediaID string, resp *models.MediaResponse) error {
	key := mediaCachePrefix + mediaID
	data, err := json.Marshal(resp)
	if err != nil {
		return fmt.Errorf("failed to marshal media: %w", err)
	}
	return s.client.Set(s.ctx, key, data, mediaCacheTTL).Err()
}

// GetCachedMedia retrieves a cached media response
func (s *MediaCacheService) GetCachedMedia(mediaID string) (*models.MediaResponse, error) {
	key := mediaCachePrefix + mediaID
	data, err := s.client.Get(s.ctx, key).Bytes()
	if err != nil {
		return nil, err
	}
	var resp models.MediaResponse
	if err := json.Unmarshal(data, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

// InvalidateMedia removes a media entry from cache
func (s *MediaCacheService) InvalidateMedia(mediaID string) error {
	keys := []string{
		mediaCachePrefix + mediaID,
		mediaVariantCachePrefix + mediaID,
	}
	return s.client.Del(s.ctx, keys...).Err()
}

// ============================================
// Variant Cache
// ============================================

// CacheVariants stores media variants in Redis
func (s *MediaCacheService) CacheVariants(mediaID string, variants []models.MediaVariant) error {
	key := mediaVariantCachePrefix + mediaID
	data, err := json.Marshal(variants)
	if err != nil {
		return fmt.Errorf("failed to marshal variants: %w", err)
	}
	return s.client.Set(s.ctx, key, data, variantCacheTTL).Err()
}

// GetCachedVariants retrieves cached variants
func (s *MediaCacheService) GetCachedVariants(mediaID string) ([]models.MediaVariant, error) {
	key := mediaVariantCachePrefix + mediaID
	data, err := s.client.Get(s.ctx, key).Bytes()
	if err != nil {
		return nil, err
	}
	var variants []models.MediaVariant
	if err := json.Unmarshal(data, &variants); err != nil {
		return nil, err
	}
	return variants, nil
}

// ============================================
// File Hash (Dedup)
// ============================================

// StoreFileHash maps a SHA-256 hash to a media ID (permanent, for dedup)
func (s *MediaCacheService) StoreFileHash(hash, mediaID string) error {
	key := mediaHashPrefix + hash
	return s.client.Set(s.ctx, key, mediaID, 0).Err() // no expiration
}

// GetMediaIDByHash looks up media ID by file hash
func (s *MediaCacheService) GetMediaIDByHash(hash string) (string, error) {
	key := mediaHashPrefix + hash
	return s.client.Get(s.ctx, key).Result()
}

// DeleteFileHash removes a hash mapping
func (s *MediaCacheService) DeleteFileHash(hash string) error {
	key := mediaHashPrefix + hash
	return s.client.Del(s.ctx, key).Err()
}

// ============================================
// Image Processing Queue
// ============================================

// EnqueueImageProcess adds a media ID to the image processing queue
func (s *MediaCacheService) EnqueueImageProcess(mediaID string) error {
	return s.client.LPush(s.ctx, imageProcessQueue, mediaID).Err()
}

// DequeueImageProcess retrieves the next media ID from the processing queue (blocking)
func (s *MediaCacheService) DequeueImageProcess(timeout time.Duration) (string, error) {
	result, err := s.client.BRPop(s.ctx, timeout, imageProcessQueue).Result()
	if err != nil {
		return "", err
	}
	if len(result) < 2 {
		return "", fmt.Errorf("empty queue result")
	}
	return result[1], nil
}

// GetProcessQueueLength returns the number of items in the processing queue
func (s *MediaCacheService) GetProcessQueueLength() (int64, error) {
	return s.client.LLen(s.ctx, imageProcessQueue).Result()
}
