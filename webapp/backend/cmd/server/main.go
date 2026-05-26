package main

import (
	"context"
	"fmt"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/cors"
	"github.com/gofiber/fiber/v2/middleware/requestid"
	"github.com/redis/go-redis/v9"
	"github.com/zplus/lichso/internal/config"
	"github.com/zplus/lichso/internal/database"
	"github.com/zplus/lichso/internal/handlers"
	"github.com/zplus/lichso/internal/middleware"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/routes"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/services/calendar"
	"github.com/zplus/lichso/internal/utils"
	"github.com/zplus/lichso/internal/validators"
	"go.mongodb.org/mongo-driver/mongo"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

var (
	version   = "1.0.0"
	startTime time.Time
)

func main() {
	startTime = time.Now()

	// ============================================
	// Load Configuration
	// ============================================
	cfg, err := config.LoadConfig()
	if err != nil {
		fmt.Printf("❌ Failed to load config: %v\n", err)
		os.Exit(1)
	}

	// ============================================
	// Initialize Logger
	// ============================================
	logger := initLogger(cfg)
	defer func() {
		_ = logger.Sync()
	}()

	logger.Info("🚀 Starting Zplus Base API Server",
		zap.String("version", version),
		zap.String("env", cfg.App.Env),
		zap.String("port", cfg.App.Port),
	)

	// ============================================
	// Connect to Databases
	// ============================================
	pgDB, mongoClient, mongoDB, redisClient, err := connectDatabases(cfg, logger)
	if err != nil {
		logger.Fatal("❌ Failed to connect to databases", zap.Error(err))
	}

	// Ensure cleanup on exit
	defer func() {
		logger.Info("Closing database connections...")
		if err := database.ClosePostgres(pgDB); err != nil {
			logger.Error("Failed to close PostgreSQL", zap.Error(err))
		}
		if err := database.CloseMongoDB(mongoClient); err != nil {
			logger.Error("Failed to close MongoDB", zap.Error(err))
		}
		if err := database.CloseRedis(redisClient); err != nil {
			logger.Error("Failed to close Redis", zap.Error(err))
		}
	}()

	// ============================================
	// Initialize Fiber App
	// ============================================
	// In production: hide server identity, disable startup messages
	serverHeader := cfg.App.Name
	if cfg.IsProduction() {
		serverHeader = ""
	}

	app := fiber.New(fiber.Config{
		AppName:               cfg.App.Name + " v" + version,
		ServerHeader:          serverHeader,
		BodyLimit:             int(cfg.Upload.MaxSize),
		DisableStartupMessage: cfg.IsProduction(),
		ErrorHandler:          globalErrorHandler(logger),
	})

	// ============================================
	// Global Middleware
	// ============================================
	setupMiddleware(app, cfg, logger, redisClient, mongoDB)

	// ============================================
	// Routes
	// ============================================
	setupRoutes(app, cfg, pgDB, mongoDB, redisClient, logger)

	// ============================================
	// Graceful Shutdown
	// ============================================
	go func() {
		if err := app.Listen(":" + cfg.App.Port); err != nil {
			logger.Fatal("❌ Server failed to start", zap.Error(err))
		}
	}()

	logger.Info(fmt.Sprintf("✅ Server is running on http://localhost:%s", cfg.App.Port))

	// Wait for interrupt signal
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	logger.Info("🛑 Shutting down server...")

	if err := app.ShutdownWithTimeout(10 * time.Second); err != nil {
		logger.Fatal("Server forced to shutdown", zap.Error(err))
	}

	logger.Info("✅ Server exited gracefully")
}

// initLogger creates a new zap logger based on environment
func initLogger(cfg *config.Config) *zap.Logger {
	var logger *zap.Logger
	var err error

	if cfg.IsDevelopment() {
		logger, err = zap.NewDevelopment()
	} else {
		logger, err = zap.NewProduction()
	}

	if err != nil {
		panic(fmt.Sprintf("Failed to initialize logger: %v", err))
	}

	return logger
}

// connectDatabases connects to all databases
func connectDatabases(cfg *config.Config, logger *zap.Logger) (*gorm.DB, *mongo.Client, *mongo.Database, *redis.Client, error) {
	// PostgreSQL
	pgDB, err := database.ConnectPostgres(&cfg.Database, logger)
	if err != nil {
		return nil, nil, nil, nil, fmt.Errorf("PostgreSQL: %w", err)
	}

	// MongoDB
	mongoClient, mongoDB, err := database.ConnectMongoDB(&cfg.MongoDB, logger)
	if err != nil {
		return nil, nil, nil, nil, fmt.Errorf("MongoDB: %w", err)
	}

	// Redis
	redisClient, err := database.ConnectRedis(&cfg.Redis, logger)
	if err != nil {
		return nil, nil, nil, nil, fmt.Errorf("Redis: %w", err)
	}

	return pgDB, mongoClient, mongoDB, redisClient, nil
}

// setupMiddleware configures global middleware
func setupMiddleware(app *fiber.App, cfg *config.Config, logger *zap.Logger, redisClient *redis.Client, mongoDB *mongo.Database) {
	// Custom recovery middleware
	app.Use(middleware.Recovery(logger))

	// Request ID
	app.Use(requestid.New())

	// Security headers (HSTS, CSP, X-Frame-Options, etc.)
	app.Use(middleware.SecurityHeaders(cfg))

	// Request logger
	app.Use(middleware.RequestLogger(logger))

	// Guest mobile daily activity tracker (for DAU analytics)
	app.Use(middleware.MobileGuestActivityTracker(redisClient, mongoDB, logger))

	// CORS - In production, AllowCredentials enables secure cookie handling
	app.Use(cors.New(cors.Config{
		AllowOrigins:     joinStrings(cfg.CORS.AllowedOrigins),
		AllowMethods:     joinStrings(cfg.CORS.AllowedMethods),
		AllowHeaders:     joinStrings(cfg.CORS.AllowedHeaders),
		ExposeHeaders:    "Content-Length",
		AllowCredentials: cfg.IsProduction(),
		MaxAge:           cfg.CORS.MaxAge,
	}))
}

// setupRoutes configures all routes
func setupRoutes(app *fiber.App, cfg *config.Config, pgDB *gorm.DB, mongoDB *mongo.Database, redisClient *redis.Client, logger *zap.Logger) {
	// API group
	api := app.Group("/api")

	// Health check
	api.Get("/health", func(c *fiber.Ctx) error {
		uptime := time.Since(startTime).String()

		// Check database connections
		pgHealth, pgErr := database.HealthCheckPostgres(pgDB)
		mongoHealth, mongoErr := database.HealthCheckMongoDB(mongoDB.Client())
		redisHealth, redisErr := database.HealthCheckRedis(redisClient)

		status := "healthy"
		if pgErr != nil || mongoErr != nil || redisErr != nil {
			status = "degraded"
		}

		return utils.SuccessResponse(c, "OK", fiber.Map{
			"status":  status,
			"version": version,
			"uptime":  uptime,
			"env":     cfg.App.Env,
			"databases": fiber.Map{
				"postgres": pgHealth,
				"mongodb":  mongoHealth,
				"redis":    redisHealth,
			},
		})
	})

	// Ping (lightweight health check)
	api.Get("/ping", func(c *fiber.Ctx) error {
		return c.JSON(fiber.Map{"message": "pong"})
	})

	// ============================================
	// Initialize Dependencies
	// ============================================
	validator := validators.NewValidator()

	// Repositories
	userRepo := repositories.NewUserRepository(pgDB)
	roleRepo := repositories.NewRoleRepository(pgDB)
	permissionRepo := repositories.NewPermissionRepository(pgDB)

	// Services
	cacheService := services.NewCacheService(redisClient, logger)
	authService := services.NewAuthService(userRepo, cacheService, cfg, logger, mongoDB)
	rbacService := services.NewRBACService(userRepo, roleRepo, permissionRepo, cacheService, logger)
	roleService := services.NewRoleService(roleRepo, permissionRepo, rbacService, logger, mongoDB)
	permissionService := services.NewPermissionService(permissionRepo, logger)
	userService := services.NewUserService(userRepo, roleRepo, rbacService, cacheService, logger, mongoDB)

	// Middleware
	authMiddleware := middleware.NewAuthMiddleware(cfg, cacheService, logger)
	permMiddleware := middleware.NewPermissionMiddleware(rbacService, logger)

	// Handlers
	authHandler := handlers.NewAuthHandler(authService, validator, logger)
	roleHandler := handlers.NewRoleHandler(roleService, validator, logger)
	permissionHandler := handlers.NewPermissionHandler(permissionService, logger)
	userHandler := handlers.NewUserHandler(userService, validator, logger)

	// Activity log repository & admin handler
	activityRepo := repositories.NewActivityLogRepository(mongoDB)
	adminHandler := handlers.NewAdminHandler(userRepo, roleRepo, activityRepo, redisClient, logger)

	// Settings
	settingRepo := repositories.NewSettingRepository(mongoDB)
	settingService := services.NewSettingService(settingRepo, mongoDB, logger)
	settingHandler := handlers.NewSettingHandler(settingService, validator, logger)

	// Notifications & WebSocket
	wsHub := services.NewWSHub(logger)
	notifRepo := repositories.NewNotificationRepository(mongoDB)
	notifService := services.NewNotificationService(notifRepo, wsHub, logger)
	notifHandler := handlers.NewNotificationHandler(notifService, logger)

	// Email service
	emailService := services.NewEmailService(&cfg.SMTP, &cfg.App, logger)
	emailHandler := handlers.NewEmailHandler(emailService, logger)

	// Media / File manager
	mediaRepo := repositories.NewMediaRepository(mongoDB)
	mediaService := services.NewMediaService(mediaRepo, &cfg.Upload, &cfg.App, logger)
	mediaHandler := handlers.NewMediaHandler(mediaService, logger)

	// Media V3 Infrastructure
	variantRepo := repositories.NewMediaVariantRepository(mongoDB)
	folderRepo := repositories.NewMediaFolderRepository(mongoDB)
	albumRepo := repositories.NewMediaAlbumRepository(mongoDB)
	attachmentRepo := repositories.NewMediaAttachmentRepository(mongoDB)
	chunkRepo := repositories.NewChunkUploadRepository(mongoDB)
	versionRepo := repositories.NewMediaVersionRepository(mongoDB)
	imgProcessService := services.NewImageProcessService(cfg.Upload.Path, logger)
	videoProcessService := services.NewVideoProcessService(cfg.Upload.Path, logger)
	mediaCacheService := services.NewMediaCacheService(redisClient, logger)
	mediaV3Service := services.NewMediaV3Service(
		mediaRepo, variantRepo, folderRepo, albumRepo, attachmentRepo, chunkRepo, versionRepo,
		imgProcessService, videoProcessService, mediaCacheService,
		cfg.Upload.Path, cfg.App.URL, cfg.Upload.MaxSize, cfg.Upload.AllowedTypes,
		logger,
	)
	mediaV3Handler := handlers.NewMediaV3Handler(mediaV3Service, logger)

	// Wire email service into auth and notification services
	authService.SetEmailService(emailService)
	notifService.SetEmailService(emailService)

	// ============================================
	// Register Routes
	// ============================================
	routes.SetupAuthRoutes(api, authHandler, authMiddleware)
	routes.SetupAdminRoutes(api, authMiddleware, permMiddleware, roleHandler, permissionHandler, userHandler, adminHandler, settingHandler, emailHandler)
	routes.SetupNotificationRoutes(api, authMiddleware, notifHandler)
	routes.SetupMediaRoutes(api, authMiddleware, permMiddleware, mediaHandler, cfg.Upload.Path)
	routes.SetupMediaV3Routes(api, authMiddleware, permMiddleware, mediaV3Handler)

	// Calendar & Feng Shui routes (public — no auth required)
	calService := calendar.NewService()
	calHandler := handlers.NewCalendarHandler(calService, logger)
	routes.SetupCalendarRoutes(api, calHandler)

	// Bookmark & Reminder routes (require authentication)
	bookmarkRepo := repositories.NewBookmarkRepository(pgDB)
	bookmarkService := services.NewBookmarkService(bookmarkRepo, logger)
	bookmarkHandler := handlers.NewBookmarkHandler(bookmarkService, validator, logger)
	routes.SetupBookmarkRoutes(api, authMiddleware, bookmarkHandler)

	reminderRepo := repositories.NewReminderRepository(pgDB)
	reminderService := services.NewReminderService(reminderRepo, logger)
	reminderHandler := handlers.NewReminderHandler(reminderService, validator, logger)
	routes.SetupReminderRoutes(api, authMiddleware, reminderHandler)

	// Start background reminder email notifier
	reminderNotifier := services.NewReminderNotifier(reminderRepo, emailService, notifService, logger)
	go reminderNotifier.Start(context.Background())

	// ============================================
	// V2 Content Infrastructure
	// ============================================
	// Repositories
	articleRepo := repositories.NewArticleRepository(pgDB)
	articleCategoryRepo := repositories.NewArticleCategoryRepository(pgDB)
	articleTagRepo := repositories.NewArticleTagRepository(pgDB)
	quoteRepo := repositories.NewQuoteRepository(pgDB)
	famousPersonRepo := repositories.NewFamousPersonRepository(pgDB)
	eventRepo := repositories.NewEventRepository(pgDB)
	folkFestivalRepo := repositories.NewFolkFestivalRepository(pgDB)

	// Services
	articleService := services.NewArticleService(articleRepo, articleCategoryRepo, articleTagRepo, logger)
	articleCategoryService := services.NewArticleCategoryService(articleCategoryRepo, logger)
	articleTagService := services.NewArticleTagService(articleTagRepo, logger)
	quoteService := services.NewQuoteService(quoteRepo, logger)
	famousPersonService := services.NewFamousPersonService(famousPersonRepo, logger)
	eventService := services.NewEventService(eventRepo, logger)
	folkFestivalService := services.NewFolkFestivalService(folkFestivalRepo, logger)

	// Handlers
	articleHandler := handlers.NewArticleHandler(articleService, validator, logger)
	articleCategoryHandler := handlers.NewArticleCategoryHandler(articleCategoryService, validator, logger)
	articleTagHandler := handlers.NewArticleTagHandler(articleTagService, validator, logger)
	quoteHandler := handlers.NewQuoteHandler(quoteService, validator, logger)
	famousPersonHandler := handlers.NewFamousPersonHandler(famousPersonService, validator, logger)
	eventHandler := handlers.NewEventHandler(eventService, validator, logger)
	folkFestivalHandler := handlers.NewFolkFestivalHandler(folkFestivalService, validator, logger)

	// Public content routes
	routes.SetupContentRoutes(api, articleHandler, articleCategoryHandler, articleTagHandler, quoteHandler, famousPersonHandler, eventHandler, folkFestivalHandler)

	// Admin content routes
	routes.SetupAdminContentRoutes(api, authMiddleware, permMiddleware, articleHandler, articleCategoryHandler, articleTagHandler, quoteHandler, famousPersonHandler, eventHandler, folkFestivalHandler)

	// Export routes (public — no auth required)
	exportService := services.NewExportService(calService, logger)
	exportHandler := handlers.NewExportHandler(exportService, logger)
	routes.SetupExportRoutes(api, exportHandler)

	// ============================================
	// V3 Content Infrastructure
	// ============================================
	// Repositories
	articleRelationRepo := repositories.NewArticleRelationRepository(pgDB)
	dailyContentRepo := repositories.NewDailyContentRepository(pgDB)
	userNoteRepo := repositories.NewUserNoteRepository(pgDB)
	userCountdownRepo := repositories.NewUserCountdownRepository(pgDB)

	// Services
	articleRelationService := services.NewArticleRelationService(articleRelationRepo, articleRepo, logger)
	dailyContentService := services.NewDailyContentService(dailyContentRepo, quoteRepo, eventRepo, articleRepo, famousPersonRepo, folkFestivalRepo, logger)
	userNoteService := services.NewUserNoteService(userNoteRepo, logger)
	userCountdownService := services.NewUserCountdownService(userCountdownRepo, logger)

	// Handlers
	articleRelationHandler := handlers.NewArticleRelationHandler(articleRelationService, validator, logger)
	dailyContentHandler := handlers.NewDailyContentHandler(dailyContentService, validator, logger)
	userNoteHandler := handlers.NewUserNoteHandler(userNoteService, validator, logger)
	userCountdownHandler := handlers.NewUserCountdownHandler(userCountdownService, validator, logger)

	// Phase 23: Horoscope & Good Day services
	horoscopeService := services.NewHoroscopeService(logger)
	goodDayService := services.NewGoodDayService(horoscopeService, logger)
	horoscopeHandler := handlers.NewHoroscopeHandler(horoscopeService, logger)
	goodDayHandler := handlers.NewGoodDayHandler(goodDayService, logger)

	// Phase 24: Newsletter + Streaks & Achievements
	newsletterRepo := repositories.NewNewsletterRepository(pgDB)
	streakRepo := repositories.NewStreakRepository(pgDB)
	achievementRepo := repositories.NewAchievementRepository(pgDB)
	newsletterService := services.NewNewsletterService(newsletterRepo, logger)
	streakAchievementService := services.NewStreakAchievementService(streakRepo, achievementRepo, logger)
	newsletterHandler := handlers.NewNewsletterHandler(newsletterService, logger)
	streakHandler := handlers.NewStreakHandler(streakAchievementService, logger)

	// V4 AI Infrastructure (shared, must be initialised before quiz)
	openRouterService := services.NewOpenRouterService(&cfg.AI, logger)
	openRouterService.SetSettingRepo(settingRepo) // load API key from DB settings at runtime

	// Points unified wallet
	pointsService := services.NewPointsService(pgDB, logger)
	pointsHandler := handlers.NewPointsHandler(pgDB, pointsService, validator, logger)

	// V5 Quiz
	quizRepo := repositories.NewQuizRepository(pgDB)
	quizService := services.NewQuizService(quizRepo, cacheService, logger)
	quizService.SetOpenRouterService(openRouterService)
	quizService.SetPointsService(pointsService)
	quizHandler := handlers.NewQuizHandler(quizService, validator, logger)
	routes.SetupQuizRoutes(api, authMiddleware, permMiddleware, quizHandler)
	routes.SetupPointsRoutes(api, authMiddleware, pointsHandler)

	// V3 Routes
	routes.SetupV3ContentRoutes(api, articleRelationHandler, dailyContentHandler, horoscopeHandler, goodDayHandler)
	routes.SetupV3AdminRoutes(api, authMiddleware, permMiddleware, articleRelationHandler, dailyContentHandler, newsletterHandler)
	routes.SetupV3UserRoutes(api, authMiddleware, userNoteHandler, userCountdownHandler, streakHandler, newsletterHandler)

	// ============================================
	// V4 AI Infrastructure (Phase 25-28)
	// ============================================

	// Repositories
	aiLogRepo := repositories.NewAILogRepository(pgDB)
	aiHoroscopeRepo := repositories.NewAIHoroscopeRepository(pgDB)
	aiQuotaRepo := repositories.NewAIQuotaRepository(pgDB)
	aiChatRepo := repositories.NewAIChatRepository(pgDB)
	aiPromptRepo := repositories.NewAIPromptTemplateRepository(pgDB)

	// Seed default prompt templates (idempotent)
	services.SeedDefaultPromptTemplates(aiPromptRepo, logger)

	// Services
	aiHoroscopeService := services.NewAIHoroscopeService(openRouterService, horoscopeService, aiHoroscopeRepo, aiQuotaRepo, aiLogRepo, aiPromptRepo, cacheService, &cfg.AI, logger)
	aiArticleService := services.NewAIArticleService(openRouterService, articleRepo, articleCategoryRepo, articleTagRepo, aiLogRepo, aiPromptRepo, services.NewImageSearchService(cfg.AI.PexelsAPIKey, cfg.Upload.Path, logger), &cfg.AI, logger)
	aiChatService := services.NewAIChatService(openRouterService, aiChatRepo, aiLogRepo, aiPromptRepo, &cfg.AI, logger)

	// Handlers
	aiHoroscopeHandler := handlers.NewAIHoroscopeHandler(aiHoroscopeService, validator, logger)
	aiArticleHandler := handlers.NewAIArticleHandler(aiArticleService, validator, logger)
	aiChatHandler := handlers.NewAIChatHandler(aiChatService, validator, logger)
	aiAdminHandler := handlers.NewAIAdminHandler(aiLogRepo, aiPromptRepo, validator, logger)

	// V4 Routes
	routes.SetupV4PublicRoutes(api, aiHoroscopeHandler)
	routes.SetupV4AuthRoutes(api, authMiddleware, aiHoroscopeHandler, aiChatHandler)
	routes.SetupV4AdminRoutes(api, authMiddleware, permMiddleware, aiArticleHandler, aiAdminHandler)

	// Rate limiting for auth endpoints
	loginRateLimit := middleware.RateLimitConfig{Max: cfg.RateLimit.LoginMax, Window: cfg.RateLimit.LoginWindow}
	registerRateLimit := middleware.RateLimitConfig{Max: 3, Window: 60}
	forgotPwRateLimit := middleware.RateLimitConfig{Max: 3, Window: 300}

	// Apply rate limiting to specific public auth routes
	authGroup := api.Group("/auth")
	authGroup.Use("/login", middleware.RateLimiter(cacheService, loginRateLimit))
	authGroup.Use("/google", middleware.RateLimiter(cacheService, loginRateLimit))
	authGroup.Use("/register", middleware.RateLimiter(cacheService, registerRateLimit))
	authGroup.Use("/forgot-password", middleware.RateLimiter(cacheService, forgotPwRateLimit))
	authGroup.Use("/reset-password", middleware.RateLimiter(cacheService, forgotPwRateLimit))

	// 404 handler for undefined routes
	app.Use(func(c *fiber.Ctx) error {
		return utils.NotFoundResponse(c, "Route not found")
	})
}

// globalErrorHandler handles all unhandled errors
func globalErrorHandler(logger *zap.Logger) fiber.ErrorHandler {
	return func(c *fiber.Ctx, err error) error {
		// Default to 500
		code := fiber.StatusInternalServerError
		message := "Internal server error"

		// Check if it's a Fiber error
		if e, ok := err.(*fiber.Error); ok {
			code = e.Code
			message = e.Message
		}

		// Check if it's an AppError
		if appErr, ok := utils.IsAppError(err); ok {
			code = appErr.Code
			message = appErr.Message
		}

		logger.Error("Request error",
			zap.Int("status", code),
			zap.String("error", err.Error()),
			zap.String("method", c.Method()),
			zap.String("path", c.Path()),
		)

		return utils.ErrorResponse(c, code, message)
	}
}

// joinStrings joins a string slice with commas
func joinStrings(s []string) string {
	result := ""
	for i, v := range s {
		if i > 0 {
			result += ", "
		}
		result += v
	}
	return result
}
