package database

import (
	"context"
	"fmt"
	"time"

	"github.com/zplus/lichso/internal/config"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"go.mongodb.org/mongo-driver/mongo/readpref"
	"go.uber.org/zap"
)

// ConnectMongoDB establishes a connection to MongoDB
func ConnectMongoDB(cfg *config.MongoDBConfig, log *zap.Logger) (*mongo.Client, *mongo.Database, error) {
	log.Info("Connecting to MongoDB...",
		zap.String("database", cfg.Database),
	)

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	// Configure client options
	clientOpts := options.Client().
		ApplyURI(cfg.URI).
		SetMaxPoolSize(cfg.MaxPoolSize).
		SetMinPoolSize(cfg.MinPoolSize)

	// Connect to MongoDB
	client, err := mongo.Connect(ctx, clientOpts)
	if err != nil {
		return nil, nil, fmt.Errorf("failed to connect to MongoDB: %w", err)
	}

	// Verify connection with ping
	if err := client.Ping(ctx, readpref.Primary()); err != nil {
		return nil, nil, fmt.Errorf("failed to ping MongoDB: %w", err)
	}

	db := client.Database(cfg.Database)

	log.Info("✅ MongoDB connected successfully",
		zap.String("database", cfg.Database),
	)

	return client, db, nil
}

// CloseMongoDB closes the MongoDB connection
func CloseMongoDB(client *mongo.Client) error {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	return client.Disconnect(ctx)
}

// HealthCheckMongoDB checks if MongoDB is reachable
func HealthCheckMongoDB(client *mongo.Client) (map[string]interface{}, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	start := time.Now()
	if err := client.Ping(ctx, readpref.Primary()); err != nil {
		return map[string]interface{}{
			"status":  "disconnected",
			"error":   err.Error(),
			"latency": time.Since(start).String(),
		}, err
	}

	return map[string]interface{}{
		"status":  "connected",
		"latency": time.Since(start).String(),
	}, nil
}
