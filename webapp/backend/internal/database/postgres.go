package database

import (
	"fmt"
	"time"

	"github.com/zplus/lichso/internal/config"
	"go.uber.org/zap"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

// ConnectPostgres establishes a connection to PostgreSQL using GORM
func ConnectPostgres(cfg *config.DatabaseConfig, log *zap.Logger) (*gorm.DB, error) {
	log.Info("Connecting to PostgreSQL...",
		zap.String("host", cfg.Host),
		zap.String("port", cfg.Port),
		zap.String("database", cfg.Name),
	)

	// Set GORM logger level - silent in production, info in development
	gormLogLevel := logger.Silent
	if cfg.SSLMode == "disable" {
		gormLogLevel = logger.Info
	}

	db, err := gorm.Open(postgres.Open(cfg.DSN()), &gorm.Config{
		Logger:                                   logger.Default.LogMode(gormLogLevel),
		DisableForeignKeyConstraintWhenMigrating: true,
		PrepareStmt:                              true,
		SkipDefaultTransaction:                   true,
		QueryFields:                              true,
	})
	if err != nil {
		return nil, fmt.Errorf("failed to connect to PostgreSQL: %w", err)
	}

	// Get underlying sql.DB to configure connection pool
	sqlDB, err := db.DB()
	if err != nil {
		return nil, fmt.Errorf("failed to get underlying sql.DB: %w", err)
	}

	// Configure connection pool
	sqlDB.SetMaxOpenConns(cfg.MaxConnections)
	sqlDB.SetMaxIdleConns(cfg.MaxIdleConnections)
	sqlDB.SetConnMaxLifetime(cfg.MaxLifetime)
	sqlDB.SetConnMaxIdleTime(5 * time.Minute)

	// Verify connection
	if err := sqlDB.Ping(); err != nil {
		return nil, fmt.Errorf("failed to ping PostgreSQL: %w", err)
	}

	log.Info("✅ PostgreSQL connected successfully",
		zap.String("database", cfg.Name),
	)

	return db, nil
}

// ClosePostgres closes the PostgreSQL connection
func ClosePostgres(db *gorm.DB) error {
	sqlDB, err := db.DB()
	if err != nil {
		return err
	}
	return sqlDB.Close()
}

// HealthCheckPostgres checks if PostgreSQL is reachable
func HealthCheckPostgres(db *gorm.DB) (map[string]interface{}, error) {
	sqlDB, err := db.DB()
	if err != nil {
		return nil, err
	}

	start := time.Now()
	if err := sqlDB.Ping(); err != nil {
		return map[string]interface{}{
			"status":  "disconnected",
			"error":   err.Error(),
			"latency": time.Since(start).String(),
		}, err
	}

	stats := sqlDB.Stats()
	return map[string]interface{}{
		"status":           "connected",
		"latency":          time.Since(start).String(),
		"open_connections": stats.OpenConnections,
		"in_use":           stats.InUse,
		"idle":             stats.Idle,
	}, nil
}
