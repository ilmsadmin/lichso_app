package database

import (
	"context"
	"fmt"
	"time"

	"github.com/redis/go-redis/v9"
	"github.com/zplus/lichso/internal/config"
	"go.uber.org/zap"
)

// ConnectRedis establishes a connection to Redis
func ConnectRedis(cfg *config.RedisConfig, log *zap.Logger) (*redis.Client, error) {
	log.Info("Connecting to Redis...",
		zap.String("host", cfg.Host),
		zap.String("port", cfg.Port),
		zap.Int("db", cfg.DB),
	)

	client := redis.NewClient(&redis.Options{
		Addr:     cfg.Addr(),
		Password: cfg.Password,
		DB:       cfg.DB,
		PoolSize: cfg.PoolSize,
	})

	// Verify connection with ping
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if _, err := client.Ping(ctx).Result(); err != nil {
		return nil, fmt.Errorf("failed to connect to Redis: %w", err)
	}

	log.Info("✅ Redis connected successfully",
		zap.String("addr", cfg.Addr()),
	)

	return client, nil
}

// CloseRedis closes the Redis connection
func CloseRedis(client *redis.Client) error {
	return client.Close()
}

// HealthCheckRedis checks if Redis is reachable
func HealthCheckRedis(client *redis.Client) (map[string]interface{}, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	start := time.Now()
	result, err := client.Ping(ctx).Result()
	if err != nil {
		return map[string]interface{}{
			"status":  "disconnected",
			"error":   err.Error(),
			"latency": time.Since(start).String(),
		}, err
	}

	// Get Redis info
	info, _ := client.Info(ctx, "memory").Result()
	_ = info // Can parse memory usage if needed

	return map[string]interface{}{
		"status":  "connected",
		"ping":    result,
		"latency": time.Since(start).String(),
	}, nil
}
