package services

import (
	"context"
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/redis/go-redis/v9"
	"go.uber.org/zap"
)

// CacheService handles Redis caching operations
type CacheService struct {
	client *redis.Client
	logger *zap.Logger
	ctx    context.Context
}

// NewCacheService creates a new CacheService
func NewCacheService(client *redis.Client, logger *zap.Logger) *CacheService {
	return &CacheService{
		client: client,
		logger: logger,
		ctx:    context.Background(),
	}
}

// ============================================
// Permission Cache
// ============================================

const permissionKeyPrefix = "permissions:"
const permissionCacheTTL = 15 * time.Minute

// CacheUserPermissions caches user permissions in Redis as a SET
func (s *CacheService) CacheUserPermissions(userID uuid.UUID, permissions []string) error {
	key := permissionKeyPrefix + userID.String()

	// Delete existing set
	s.client.Del(s.ctx, key)

	if len(permissions) == 0 {
		return nil
	}

	// Add all permissions
	members := make([]interface{}, len(permissions))
	for i, p := range permissions {
		members[i] = p
	}

	pipe := s.client.Pipeline()
	pipe.SAdd(s.ctx, key, members...)
	pipe.Expire(s.ctx, key, permissionCacheTTL)
	_, err := pipe.Exec(s.ctx)

	if err != nil {
		s.logger.Error("Failed to cache user permissions",
			zap.String("user_id", userID.String()),
			zap.Error(err),
		)
	}
	return err
}

// GetUserPermissions retrieves cached user permissions from Redis
func (s *CacheService) GetUserPermissions(userID uuid.UUID) ([]string, error) {
	key := permissionKeyPrefix + userID.String()
	permissions, err := s.client.SMembers(s.ctx, key).Result()
	if err != nil {
		return nil, err
	}
	return permissions, nil
}

// HasPermission checks if a user has a specific permission (O(1) lookup)
func (s *CacheService) HasPermission(userID uuid.UUID, permission string) (bool, error) {
	key := permissionKeyPrefix + userID.String()
	return s.client.SIsMember(s.ctx, key, permission).Result()
}

// InvalidatePermissionCache removes the permission cache for a user
func (s *CacheService) InvalidatePermissionCache(userID uuid.UUID) error {
	key := permissionKeyPrefix + userID.String()
	return s.client.Del(s.ctx, key).Err()
}

// ============================================
// Token Blacklist
// ============================================

const blacklistKeyPrefix = "blacklist:"

// BlacklistToken adds a token's JTI to the blacklist with TTL
func (s *CacheService) BlacklistToken(jti string, ttl time.Duration) error {
	key := blacklistKeyPrefix + jti
	return s.client.Set(s.ctx, key, "1", ttl).Err()
}

// IsTokenBlacklisted checks if a token's JTI is blacklisted
func (s *CacheService) IsTokenBlacklisted(jti string) (bool, error) {
	key := blacklistKeyPrefix + jti
	result, err := s.client.Exists(s.ctx, key).Result()
	if err != nil {
		return false, err
	}
	return result > 0, nil
}

// ============================================
// Password Reset Token
// ============================================

const passwordResetKeyPrefix = "password_reset:"
const passwordResetTTL = 1 * time.Hour

// StorePasswordResetToken stores a password reset token mapped to a user ID
func (s *CacheService) StorePasswordResetToken(token string, userID uuid.UUID) error {
	key := passwordResetKeyPrefix + token
	return s.client.Set(s.ctx, key, userID.String(), passwordResetTTL).Err()
}

// GetPasswordResetUserID retrieves the user ID for a password reset token
func (s *CacheService) GetPasswordResetUserID(token string) (uuid.UUID, error) {
	key := passwordResetKeyPrefix + token
	result, err := s.client.Get(s.ctx, key).Result()
	if err != nil {
		return uuid.Nil, err
	}
	return uuid.Parse(result)
}

// DeletePasswordResetToken deletes a password reset token
func (s *CacheService) DeletePasswordResetToken(token string) error {
	key := passwordResetKeyPrefix + token
	return s.client.Del(s.ctx, key).Err()
}

// ============================================
// Session Cache
// ============================================

const sessionKeyPrefix = "session:"

// ClearUserSession clears all cached data for a user
func (s *CacheService) ClearUserSession(userID uuid.UUID) error {
	keys := []string{
		permissionKeyPrefix + userID.String(),
		sessionKeyPrefix + userID.String(),
	}
	return s.client.Del(s.ctx, keys...).Err()
}

// ============================================
// Rate Limiting
// ============================================

const rateLimitKeyPrefix = "ratelimit:"

// IncrementRateLimit increments a rate limit counter and returns the current count
func (s *CacheService) IncrementRateLimit(identifier string, window time.Duration) (int64, error) {
	key := fmt.Sprintf("%s%s", rateLimitKeyPrefix, identifier)

	pipe := s.client.Pipeline()
	incr := pipe.Incr(s.ctx, key)
	pipe.Expire(s.ctx, key, window)
	_, err := pipe.Exec(s.ctx)
	if err != nil {
		return 0, err
	}

	return incr.Val(), nil
}

// GetRateLimitCount gets the current rate limit count
func (s *CacheService) GetRateLimitCount(identifier string) (int64, error) {
	key := fmt.Sprintf("%s%s", rateLimitKeyPrefix, identifier)
	count, err := s.client.Get(s.ctx, key).Int64()
	if err == redis.Nil {
		return 0, nil
	}
	return count, err
}

// ============================================
// Generic Key/Value Cache
// ============================================

// GetString retrieves a string value from Redis by key
func (s *CacheService) GetString(ctx context.Context, key string) (string, error) {
	return s.client.Get(ctx, key).Result()
}

// SetString stores a string value in Redis with a TTL
func (s *CacheService) SetString(ctx context.Context, key, value string, ttl time.Duration) error {
	return s.client.Set(ctx, key, value, ttl).Err()
}

// DeleteKey removes a key from Redis
func (s *CacheService) DeleteKey(ctx context.Context, key string) error {
	return s.client.Del(ctx, key).Err()
}
