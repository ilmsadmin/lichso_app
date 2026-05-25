package routes

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
	"github.com/zplus/lichso/internal/middleware"
)

// SetupAuthRoutes configures authentication routes
func SetupAuthRoutes(router fiber.Router, authHandler *handlers.AuthHandler, authMiddleware *middleware.AuthMiddleware) {
	auth := router.Group("/auth")

	// Public routes (no auth required)
	auth.Post("/register", authHandler.Register)
	auth.Post("/login", authHandler.Login)
	auth.Post("/google", authHandler.GoogleLogin)
	auth.Post("/refresh", authHandler.RefreshToken)
	auth.Post("/forgot-password", authHandler.ForgotPassword)
	auth.Post("/reset-password", authHandler.ResetPassword)

	// Protected routes (auth required)
	auth.Post("/logout", authMiddleware.Authenticate(), authHandler.Logout)
	auth.Get("/me", authMiddleware.Authenticate(), authHandler.GetMe)
	auth.Put("/me", authMiddleware.Authenticate(), authHandler.UpdateProfile)
	auth.Put("/change-password", authMiddleware.Authenticate(), authHandler.ChangePassword)
}
