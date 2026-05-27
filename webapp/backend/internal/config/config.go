package config

import (
	"fmt"
	"strings"
	"time"

	"github.com/spf13/viper"
)

// Config holds all application configuration
type Config struct {
	App       AppConfig
	Database  DatabaseConfig
	MongoDB   MongoDBConfig
	Redis     RedisConfig
	JWT       JWTConfig
	CORS      CORSConfig
	RateLimit RateLimitConfig
	Log       LogConfig
	Upload    UploadConfig
	Google    GoogleConfig
	SMTP      SMTPConfig
	AI        AIConfig
}

// AIConfig holds OpenRouter AI configuration
type AIConfig struct {
	OpenRouterAPIKey      string
	OpenRouterBaseURL     string
	SiteURL               string
	SiteName              string
	DefaultArticleModel   string
	DefaultHoroscopeModel string
	DefaultChatModel      string
	MaxTokensArticle      int
	MaxTokensHoroscope    int
	MaxTokensChat         int
	// Rate limits per day
	HoroscopeRateLimitGuest   int
	HoroscopeRateLimitFree    int
	HoroscopeRateLimitPremium int
	// Monthly budget cap in USD (0 = unlimited)
	MonthlyBudgetCapUSD float64
	// Pexels API key for auto image search
	PexelsAPIKey string
}

// SMTPConfig holds SMTP email configuration
type SMTPConfig struct {
	Host       string
	Port       int
	User       string
	Password   string
	From       string
	FromName   string
	Encryption string // "tls", "ssl", "none"
	Enabled    bool
}

// GoogleConfig holds Google OAuth configuration
type GoogleConfig struct {
	ClientID        string
	AndroidClientID string // Firebase web client ID used by Android app
	IOSClientID     string // iOS OAuth client ID used by native iOS app
}

// AppConfig holds application-level configuration
type AppConfig struct {
	Name   string
	Env    string
	Port   string
	Debug  bool
	URL    string
	Secret string
}

// DatabaseConfig holds PostgreSQL configuration
type DatabaseConfig struct {
	Host               string
	Port               string
	Name               string
	User               string
	Password           string
	SSLMode            string
	MaxConnections     int
	MaxIdleConnections int
	MaxLifetime        time.Duration
}

// MongoDBConfig holds MongoDB configuration
type MongoDBConfig struct {
	URI         string
	Database    string
	MaxPoolSize uint64
	MinPoolSize uint64
}

// RedisConfig holds Redis configuration
type RedisConfig struct {
	Host     string
	Port     string
	Password string
	DB       int
	PoolSize int
}

// JWTConfig holds JWT configuration
type JWTConfig struct {
	Secret             string
	AccessTokenExpiry  time.Duration
	RefreshTokenExpiry time.Duration
	Issuer             string
}

// CORSConfig holds CORS configuration
type CORSConfig struct {
	AllowedOrigins []string
	AllowedMethods []string
	AllowedHeaders []string
	MaxAge         int
}

// RateLimitConfig holds rate limiting configuration
type RateLimitConfig struct {
	Max         int
	Window      int
	LoginMax    int
	LoginWindow int
}

// LogConfig holds logging configuration
type LogConfig struct {
	Level    string
	Format   string
	Output   string
	FilePath string
}

// UploadConfig holds file upload configuration
type UploadConfig struct {
	MaxSize      int64
	AllowedTypes []string
	Path         string
}

// DSN returns the PostgreSQL connection string
func (d *DatabaseConfig) DSN() string {
	return fmt.Sprintf(
		"host=%s port=%s user=%s password=%s dbname=%s sslmode=%s TimeZone=Asia/Ho_Chi_Minh",
		d.Host, d.Port, d.User, d.Password, d.Name, d.SSLMode,
	)
}

// Addr returns the Redis address
func (r *RedisConfig) Addr() string {
	return fmt.Sprintf("%s:%s", r.Host, r.Port)
}

// IsDevelopment returns true if running in development mode
func (c *Config) IsDevelopment() bool {
	return c.App.Env == "development"
}

// IsProduction returns true if running in production mode
func (c *Config) IsProduction() bool {
	return c.App.Env == "production"
}

// LoadConfig loads configuration from .env file and environment variables
func LoadConfig(path ...string) (*Config, error) {
	if len(path) > 0 && path[0] != "" {
		viper.SetConfigFile(path[0])
	} else {
		viper.SetConfigFile(".env")
	}

	viper.AutomaticEnv()

	// Read config file (ignore error if not found, use env vars)
	if err := viper.ReadInConfig(); err != nil {
		if _, ok := err.(viper.ConfigFileNotFoundError); !ok {
			// Only return error if it's not a "file not found" error
			// Environment variables will still work
			fmt.Printf("Warning: config file not found, using environment variables: %v\n", err)
		}
	}

	// Set defaults
	setDefaults()

	// Parse durations
	accessExpiry, err := time.ParseDuration(viper.GetString("JWT_ACCESS_TOKEN_EXPIRY"))
	if err != nil {
		accessExpiry = 15 * time.Minute
	}

	refreshExpiry, err := time.ParseDuration(viper.GetString("JWT_REFRESH_TOKEN_EXPIRY"))
	if err != nil {
		refreshExpiry = 7 * 24 * time.Hour
	}

	config := &Config{
		App: AppConfig{
			Name:   viper.GetString("APP_NAME"),
			Env:    viper.GetString("APP_ENV"),
			Port:   viper.GetString("APP_PORT"),
			Debug:  viper.GetBool("APP_DEBUG"),
			URL:    viper.GetString("APP_URL"),
			Secret: viper.GetString("APP_SECRET"),
		},
		Database: DatabaseConfig{
			Host:               viper.GetString("DB_HOST"),
			Port:               viper.GetString("DB_PORT"),
			Name:               viper.GetString("DB_NAME"),
			User:               viper.GetString("DB_USER"),
			Password:           viper.GetString("DB_PASSWORD"),
			SSLMode:            viper.GetString("DB_SSL_MODE"),
			MaxConnections:     viper.GetInt("DB_MAX_CONNECTIONS"),
			MaxIdleConnections: viper.GetInt("DB_MAX_IDLE_CONNECTIONS"),
			MaxLifetime:        time.Duration(viper.GetInt("DB_MAX_LIFETIME")) * time.Second,
		},
		MongoDB: MongoDBConfig{
			URI:         viper.GetString("MONGODB_URI"),
			Database:    viper.GetString("MONGODB_DATABASE"),
			MaxPoolSize: viper.GetUint64("MONGODB_MAX_POOL_SIZE"),
			MinPoolSize: viper.GetUint64("MONGODB_MIN_POOL_SIZE"),
		},
		Redis: RedisConfig{
			Host:     viper.GetString("REDIS_HOST"),
			Port:     viper.GetString("REDIS_PORT"),
			Password: viper.GetString("REDIS_PASSWORD"),
			DB:       viper.GetInt("REDIS_DB"),
			PoolSize: viper.GetInt("REDIS_POOL_SIZE"),
		},
		JWT: JWTConfig{
			Secret:             viper.GetString("JWT_SECRET"),
			AccessTokenExpiry:  accessExpiry,
			RefreshTokenExpiry: refreshExpiry,
			Issuer:             viper.GetString("JWT_ISSUER"),
		},
		CORS: CORSConfig{
			AllowedOrigins: splitAndTrim(viper.GetString("CORS_ALLOWED_ORIGINS")),
			AllowedMethods: splitAndTrim(viper.GetString("CORS_ALLOWED_METHODS")),
			AllowedHeaders: splitAndTrim(viper.GetString("CORS_ALLOWED_HEADERS")),
			MaxAge:         viper.GetInt("CORS_MAX_AGE"),
		},
		RateLimit: RateLimitConfig{
			Max:         viper.GetInt("RATE_LIMIT_MAX"),
			Window:      viper.GetInt("RATE_LIMIT_WINDOW"),
			LoginMax:    viper.GetInt("RATE_LIMIT_LOGIN_MAX"),
			LoginWindow: viper.GetInt("RATE_LIMIT_LOGIN_WINDOW"),
		},
		Log: LogConfig{
			Level:    viper.GetString("LOG_LEVEL"),
			Format:   viper.GetString("LOG_FORMAT"),
			Output:   viper.GetString("LOG_OUTPUT"),
			FilePath: viper.GetString("LOG_FILE_PATH"),
		},
		Upload: UploadConfig{
			MaxSize:      viper.GetInt64("UPLOAD_MAX_SIZE"),
			AllowedTypes: splitAndTrim(viper.GetString("UPLOAD_ALLOWED_TYPES")),
			Path:         viper.GetString("UPLOAD_PATH"),
		},
		Google: GoogleConfig{
			ClientID:        viper.GetString("GOOGLE_CLIENT_ID"),
			AndroidClientID: viper.GetString("GOOGLE_ANDROID_CLIENT_ID"),
			IOSClientID:     viper.GetString("GOOGLE_IOS_CLIENT_ID"),
		},
		SMTP: SMTPConfig{
			Host:       viper.GetString("SMTP_HOST"),
			Port:       viper.GetInt("SMTP_PORT"),
			User:       viper.GetString("SMTP_USER"),
			Password:   viper.GetString("SMTP_PASSWORD"),
			From:       viper.GetString("SMTP_FROM"),
			FromName:   viper.GetString("SMTP_FROM_NAME"),
			Encryption: viper.GetString("SMTP_ENCRYPTION"),
			Enabled:    viper.GetBool("SMTP_ENABLED"),
		},
		AI: AIConfig{
			OpenRouterAPIKey:          viper.GetString("OPENROUTER_API_KEY"),
			OpenRouterBaseURL:         viper.GetString("OPENROUTER_BASE_URL"),
			SiteURL:                   viper.GetString("SITE_URL"),
			SiteName:                  viper.GetString("SITE_NAME"),
			DefaultArticleModel:       viper.GetString("AI_ARTICLE_MODEL"),
			DefaultHoroscopeModel:     viper.GetString("AI_HOROSCOPE_MODEL"),
			DefaultChatModel:          viper.GetString("AI_CHAT_MODEL"),
			MaxTokensArticle:          viper.GetInt("AI_MAX_TOKENS_ARTICLE"),
			MaxTokensHoroscope:        viper.GetInt("AI_MAX_TOKENS_HOROSCOPE"),
			MaxTokensChat:             viper.GetInt("AI_MAX_TOKENS_CHAT"),
			HoroscopeRateLimitGuest:   viper.GetInt("AI_RATE_HOROSCOPE_GUEST"),
			HoroscopeRateLimitFree:    viper.GetInt("AI_RATE_HOROSCOPE_FREE"),
			HoroscopeRateLimitPremium: viper.GetInt("AI_RATE_HOROSCOPE_PREMIUM"),
			MonthlyBudgetCapUSD:       viper.GetFloat64("AI_MONTHLY_BUDGET_CAP"),
			PexelsAPIKey:              viper.GetString("PEXELS_API_KEY"),
		},
	}

	return config, nil
}

// setDefaults sets default values for configuration
func setDefaults() {
	viper.SetDefault("APP_NAME", "Zplus Base")
	viper.SetDefault("APP_ENV", "development")
	viper.SetDefault("APP_PORT", "8080")
	viper.SetDefault("APP_DEBUG", false)
	viper.SetDefault("APP_URL", "http://localhost:8080")
	viper.SetDefault("APP_SECRET", "change-this-secret")

	viper.SetDefault("DB_HOST", "localhost")
	viper.SetDefault("DB_PORT", "5432")
	viper.SetDefault("DB_NAME", "zplus_db")
	viper.SetDefault("DB_USER", "zplus_user")
	viper.SetDefault("DB_PASSWORD", "zplus_secret")
	viper.SetDefault("DB_SSL_MODE", "disable")
	viper.SetDefault("DB_MAX_CONNECTIONS", 25)
	viper.SetDefault("DB_MAX_IDLE_CONNECTIONS", 5)
	viper.SetDefault("DB_MAX_LIFETIME", 300)

	viper.SetDefault("MONGODB_URI", "mongodb://localhost:27017")
	viper.SetDefault("MONGODB_DATABASE", "zplus_logs")
	viper.SetDefault("MONGODB_MAX_POOL_SIZE", 50)
	viper.SetDefault("MONGODB_MIN_POOL_SIZE", 5)

	viper.SetDefault("REDIS_HOST", "localhost")
	viper.SetDefault("REDIS_PORT", "6379")
	viper.SetDefault("REDIS_PASSWORD", "")
	viper.SetDefault("REDIS_DB", 0)
	viper.SetDefault("REDIS_POOL_SIZE", 10)

	viper.SetDefault("JWT_SECRET", "change-this-jwt-secret-minimum-32-chars")
	viper.SetDefault("JWT_ACCESS_TOKEN_EXPIRY", "15m")
	viper.SetDefault("JWT_REFRESH_TOKEN_EXPIRY", "168h")
	viper.SetDefault("JWT_ISSUER", "zplus-base")

	viper.SetDefault("CORS_ALLOWED_ORIGINS", "http://localhost:3000")
	viper.SetDefault("CORS_ALLOWED_METHODS", "GET,POST,PUT,PATCH,DELETE,OPTIONS")
	viper.SetDefault("CORS_ALLOWED_HEADERS", "Origin,Content-Type,Accept,Authorization")
	viper.SetDefault("CORS_MAX_AGE", 86400)

	viper.SetDefault("RATE_LIMIT_MAX", 100)
	viper.SetDefault("RATE_LIMIT_WINDOW", 60)
	viper.SetDefault("RATE_LIMIT_LOGIN_MAX", 5)
	viper.SetDefault("RATE_LIMIT_LOGIN_WINDOW", 300)

	viper.SetDefault("LOG_LEVEL", "info")
	viper.SetDefault("LOG_FORMAT", "json")
	viper.SetDefault("LOG_OUTPUT", "stdout")
	viper.SetDefault("LOG_FILE_PATH", "./logs/app.log")

	viper.SetDefault("UPLOAD_MAX_SIZE", 10485760)
	viper.SetDefault("UPLOAD_ALLOWED_TYPES", "image/jpeg,image/png,image/gif,image/webp,application/pdf")
	viper.SetDefault("UPLOAD_PATH", "./uploads")

	viper.SetDefault("GOOGLE_CLIENT_ID", "")
	viper.SetDefault("GOOGLE_ANDROID_CLIENT_ID", "")
	viper.SetDefault("GOOGLE_IOS_CLIENT_ID", "")

	viper.SetDefault("SMTP_HOST", "localhost")
	viper.SetDefault("SMTP_PORT", 587)
	viper.SetDefault("SMTP_USER", "")
	viper.SetDefault("SMTP_PASSWORD", "")
	viper.SetDefault("SMTP_FROM", "noreply@example.com")
	viper.SetDefault("SMTP_FROM_NAME", "Zplus Base")
	viper.SetDefault("SMTP_ENCRYPTION", "tls")
	viper.SetDefault("SMTP_ENABLED", false)

	// AI / OpenRouter defaults
	viper.SetDefault("OPENROUTER_API_KEY", "")
	viper.SetDefault("OPENROUTER_BASE_URL", "https://openrouter.ai/api/v1")
	viper.SetDefault("SITE_URL", "https://lichso.vn")
	viper.SetDefault("SITE_NAME", "Lịch Số")
	viper.SetDefault("AI_ARTICLE_MODEL", "deepseek/deepseek-chat")
	viper.SetDefault("AI_HOROSCOPE_MODEL", "anthropic/claude-sonnet-4")
	viper.SetDefault("AI_CHAT_MODEL", "openai/gpt-4o-mini")
	viper.SetDefault("AI_MAX_TOKENS_ARTICLE", 4096)
	viper.SetDefault("AI_MAX_TOKENS_HOROSCOPE", 2048)
	viper.SetDefault("AI_MAX_TOKENS_CHAT", 1024)
	viper.SetDefault("AI_RATE_HOROSCOPE_GUEST", 1)
	viper.SetDefault("AI_RATE_HOROSCOPE_FREE", 3)
	viper.SetDefault("AI_RATE_HOROSCOPE_PREMIUM", 20)
	viper.SetDefault("AI_MONTHLY_BUDGET_CAP", 50.0)
}

// splitAndTrim splits a comma-separated string and trims whitespace
func splitAndTrim(s string) []string {
	parts := strings.Split(s, ",")
	result := make([]string, 0, len(parts))
	for _, p := range parts {
		trimmed := strings.TrimSpace(p)
		if trimmed != "" {
			result = append(result, trimmed)
		}
	}
	return result
}
