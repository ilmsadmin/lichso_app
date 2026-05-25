package routes

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
	"github.com/zplus/lichso/internal/middleware"
)

// SetupAdminRoutes configures admin routes with authentication and permission middleware
func SetupAdminRoutes(
	router fiber.Router,
	authMiddleware *middleware.AuthMiddleware,
	permMiddleware *middleware.PermissionMiddleware,
	roleHandler *handlers.RoleHandler,
	permissionHandler *handlers.PermissionHandler,
	userHandler *handlers.UserHandler,
	adminHandler *handlers.AdminHandler,
	settingHandler *handlers.SettingHandler,
	emailHandler *handlers.EmailHandler,
) {
	// All admin routes require authentication
	admin := router.Group("/admin", authMiddleware.Authenticate())

	// ============================================
	// Dashboard Routes
	// ============================================
	dashboard := admin.Group("/dashboard")
	dashboard.Get("/stats", permMiddleware.RequirePermission("dashboard.read"), adminHandler.GetDashboardStats)

	// ============================================
	// Activity Logs Routes
	// ============================================
	logs := admin.Group("/logs")
	logs.Get("/export", permMiddleware.RequirePermission("logs.export"), adminHandler.ExportActivityLogs)
	logs.Get("/", permMiddleware.RequirePermission("logs.read"), adminHandler.GetActivityLogs)
	logs.Get("/:id", permMiddleware.RequirePermission("logs.read"), adminHandler.GetActivityLog)

	// ============================================
	// User Management Routes
	// ============================================
	users := admin.Group("/users")
	users.Get("/export", permMiddleware.RequirePermission("users.export"), userHandler.Export)
	users.Get("/", permMiddleware.RequirePermission("users.read"), userHandler.List)
	users.Get("/:id", permMiddleware.RequirePermission("users.read"), userHandler.Get)
	users.Post("/", permMiddleware.RequirePermission("users.create"), userHandler.Create)
	users.Put("/:id", permMiddleware.RequirePermission("users.update"), userHandler.Update)
	users.Delete("/:id", permMiddleware.RequirePermission("users.delete"), userHandler.Delete)
	users.Patch("/:id/status", permMiddleware.RequirePermission("users.update"), userHandler.ToggleStatus)
	users.Put("/:id/roles", permMiddleware.RequirePermission("users.update"), userHandler.SetRoles)

	// ============================================
	// Role Management Routes
	// ============================================
	roles := admin.Group("/roles")
	roles.Get("/", permMiddleware.RequirePermission("roles.read"), roleHandler.List)
	roles.Get("/:id", permMiddleware.RequirePermission("roles.read"), roleHandler.Get)
	roles.Post("/", permMiddleware.RequirePermission("roles.create"), roleHandler.Create)
	roles.Put("/:id", permMiddleware.RequirePermission("roles.update"), roleHandler.Update)
	roles.Delete("/:id", permMiddleware.RequirePermission("roles.delete"), roleHandler.Delete)
	roles.Post("/assign", permMiddleware.RequirePermission("roles.assign"), roleHandler.AssignRole)
	roles.Post("/unassign", permMiddleware.RequirePermission("roles.assign"), roleHandler.UnassignRole)

	// ============================================
	// Permission Management Routes
	// ============================================
	permissions := admin.Group("/permissions")
	permissions.Get("/", permMiddleware.RequirePermission("permissions.read"), permissionHandler.List)
	permissions.Get("/grouped", permMiddleware.RequirePermission("permissions.read"), permissionHandler.ListGrouped)
	permissions.Get("/modules", permMiddleware.RequirePermission("permissions.read"), permissionHandler.Modules)

	// ============================================
	// Settings Routes
	// ============================================
	settings := admin.Group("/settings")
	settings.Get("/", permMiddleware.RequirePermission("settings.read"), settingHandler.List)
	settings.Get("/grouped", permMiddleware.RequirePermission("settings.read"), settingHandler.ListGrouped)
	settings.Get("/group/:group", permMiddleware.RequirePermission("settings.read"), settingHandler.GetByGroup)
	settings.Put("/group/:group", permMiddleware.RequirePermission("settings.update"), settingHandler.UpdateGroup)
	settings.Post("/", permMiddleware.RequirePermission("settings.create"), settingHandler.Create)
	settings.Get("/:key", permMiddleware.RequirePermission("settings.read"), settingHandler.Get)
	settings.Put("/:key", permMiddleware.RequirePermission("settings.update"), settingHandler.Update)
	settings.Delete("/:key", permMiddleware.RequirePermission("settings.delete"), settingHandler.Delete)

	// ============================================
	// Cache Monitoring Routes
	// ============================================
	cache := admin.Group("/cache")
	cache.Get("/stats", permMiddleware.RequirePermission("settings.read"), adminHandler.GetCacheStats)

	// ============================================
	// Email Routes
	// ============================================
	email := admin.Group("/email")
	email.Get("/status", permMiddleware.RequirePermission("settings.read"), emailHandler.GetEmailStatus)
	email.Post("/test", permMiddleware.RequirePermission("settings.update"), emailHandler.SendTestEmail)
}
