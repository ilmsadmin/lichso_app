package handlers

import (
	"fmt"
	"strings"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"github.com/zplus/lichso/internal/validators"
	"go.uber.org/zap"
)

func buildClientUserAgent(c *fiber.Ctx) string {
	baseUA := strings.TrimSpace(c.Get("User-Agent"))
	platform := strings.TrimSpace(c.Get("X-Client-Platform"))
	appVersion := strings.TrimSpace(c.Get("X-App-Version"))
	deviceName := strings.TrimSpace(c.Get("X-Device-Name"))
	osVersion := strings.TrimSpace(c.Get("X-OS-Version"))

	if platform == "" && appVersion == "" && deviceName == "" && osVersion == "" {
		return baseUA
	}

	return fmt.Sprintf("%s | client platform=%s;app=%s;device=%s;os=%s",
		baseUA, platform, appVersion, deviceName, osVersion)
}

// AuthHandler handles authentication HTTP requests
type AuthHandler struct {
	authService *services.AuthService
	validator   *validators.Validator
	logger      *zap.Logger
}

// NewAuthHandler creates a new AuthHandler
func NewAuthHandler(authService *services.AuthService, validator *validators.Validator, logger *zap.Logger) *AuthHandler {
	return &AuthHandler{
		authService: authService,
		validator:   validator,
		logger:      logger,
	}
}

// Login handles POST /api/auth/login
func (h *AuthHandler) Login(c *fiber.Ctx) error {
	var req dto.LoginRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	// Validate request
	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	// Get client info
	ipAddress := c.IP()
	userAgent := buildClientUserAgent(c)

	// Call service
	result, err := h.authService.Login(&req, ipAddress, userAgent)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Login failed", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Login successful", result)
}

// Register handles POST /api/auth/register
func (h *AuthHandler) Register(c *fiber.Ctx) error {
	var req dto.RegisterRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	// Validate request
	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	// Get client info
	ipAddress := c.IP()
	userAgent := buildClientUserAgent(c)

	// Call service
	result, err := h.authService.Register(&req, ipAddress, userAgent)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Registration failed", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.CreatedResponse(c, "Registration successful", fiber.Map{
		"user": result,
	})
}

// RefreshToken handles POST /api/auth/refresh
func (h *AuthHandler) RefreshToken(c *fiber.Ctx) error {
	var req dto.RefreshTokenRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	// Validate request
	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	// Get client info
	ipAddress := c.IP()
	userAgent := buildClientUserAgent(c)

	// Call service
	result, err := h.authService.RefreshToken(&req, ipAddress, userAgent)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Token refresh failed", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Token refreshed", result)
}

// Logout handles POST /api/auth/logout
func (h *AuthHandler) Logout(c *fiber.Ctx) error {
	// Get user ID from context (set by auth middleware)
	userIDStr, ok := c.Locals("user_id").(string)
	if !ok || userIDStr == "" {
		return utils.UnauthorizedResponse(c, "")
	}

	userID, err := uuid.Parse(userIDStr)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user ID")
	}

	// Get the access token JTI and remaining TTL for blacklisting
	tokenJTI, _ := c.Locals("token_jti").(string)
	tokenTTL, _ := c.Locals("token_ttl").(int64)

	// Call service
	if err := h.authService.Logout(userID, tokenJTI, java2GoDuration(tokenTTL)); err != nil {
		h.logger.Error("Logout failed", zap.Error(err))
	}

	return utils.SuccessResponse(c, "Logged out successfully", nil)
}

// ForgotPassword handles POST /api/auth/forgot-password
func (h *AuthHandler) ForgotPassword(c *fiber.Ctx) error {
	var req dto.ForgotPasswordRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	// Validate request
	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	// Call service (always returns success to not reveal email existence)
	_ = h.authService.ForgotPassword(&req)

	return utils.SuccessResponse(c, "If the email exists, a password reset link has been sent", nil)
}

// ResetPassword handles POST /api/auth/reset-password
func (h *AuthHandler) ResetPassword(c *fiber.Ctx) error {
	var req dto.ResetPasswordRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	// Validate request
	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	// Get client info
	ipAddress := c.IP()
	userAgent := buildClientUserAgent(c)

	// Call service
	if err := h.authService.ResetPassword(&req, ipAddress, userAgent); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Password reset failed", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Password reset successfully", nil)
}

// ChangePassword handles PUT /api/auth/change-password
func (h *AuthHandler) ChangePassword(c *fiber.Ctx) error {
	// Get user ID from context
	userIDStr, ok := c.Locals("user_id").(string)
	if !ok || userIDStr == "" {
		return utils.UnauthorizedResponse(c, "")
	}

	userID, err := uuid.Parse(userIDStr)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user ID")
	}

	var req dto.ChangePasswordRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	// Validate request
	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	// Get client info
	ipAddress := c.IP()
	userAgent := buildClientUserAgent(c)

	// Call service
	if err := h.authService.ChangePassword(userID, &req, ipAddress, userAgent); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Password change failed", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Password changed successfully", nil)
}

// GoogleLogin handles POST /api/auth/google
func (h *AuthHandler) GoogleLogin(c *fiber.Ctx) error {
	var req dto.GoogleLoginRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	// Validate request
	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	// Get client info
	ipAddress := c.IP()
	userAgent := buildClientUserAgent(c)

	// Call service
	result, err := h.authService.GoogleLogin(&req, ipAddress, userAgent)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Google login failed", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Google login successful", result)
}

// AppleLogin handles POST /api/auth/apple
func (h *AuthHandler) AppleLogin(c *fiber.Ctx) error {
	var req dto.AppleLoginRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	// Validate request
	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	// Get client info
	ipAddress := c.IP()
	userAgent := buildClientUserAgent(c)

	// Call service
	result, err := h.authService.AppleLogin(&req, ipAddress, userAgent)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Apple login failed", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Apple login successful", result)
}

// GuestLogin handles POST /api/auth/guest
// Creates or resumes an anonymous guest session keyed by device id so every app
// user (guest included) has a row in the users table.
func (h *AuthHandler) GuestLogin(c *fiber.Ctx) error {
	var req dto.GuestLoginRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	ipAddress := c.IP()
	userAgent := buildClientUserAgent(c)

	result, err := h.authService.GuestLogin(&req, ipAddress, userAgent)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("Guest login failed", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Guest session started", result)
}

// GetMe handles GET /api/auth/me
func (h *AuthHandler) GetMe(c *fiber.Ctx) error {
	// Get user ID from context
	userIDStr, ok := c.Locals("user_id").(string)
	if !ok || userIDStr == "" {
		return utils.UnauthorizedResponse(c, "")
	}

	userID, err := uuid.Parse(userIDStr)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user ID")
	}

	// Call service
	result, err := h.authService.GetMe(userID)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("GetMe failed", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "User profile retrieved", result)
}

// UpdateProfile handles PUT /api/auth/me
func (h *AuthHandler) UpdateProfile(c *fiber.Ctx) error {
	userIDStr, ok := c.Locals("user_id").(string)
	if !ok || userIDStr == "" {
		return utils.UnauthorizedResponse(c, "")
	}

	userID, err := uuid.Parse(userIDStr)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user ID")
	}

	var req dto.UpdateProfileRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	result, err := h.authService.UpdateProfile(userID, &req)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("UpdateProfile failed", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Profile updated successfully", result)
}

// DeleteMe handles DELETE /api/auth/me
func (h *AuthHandler) DeleteMe(c *fiber.Ctx) error {
	userIDStr, ok := c.Locals("user_id").(string)
	if !ok || userIDStr == "" {
		return utils.UnauthorizedResponse(c, "")
	}

	userID, err := uuid.Parse(userIDStr)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user ID")
	}

	tokenJTI, _ := c.Locals("token_jti").(string)
	tokenTTL, _ := c.Locals("token_ttl").(int64)

	ipAddress := c.IP()
	userAgent := buildClientUserAgent(c)

	if err := h.authService.DeleteSelf(userID, tokenJTI, java2GoDuration(tokenTTL), ipAddress, userAgent); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		h.logger.Error("DeleteMe failed", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.SuccessResponse(c, "Account deleted successfully", nil)
}

// java2GoDuration converts seconds (int64) to time.Duration
func java2GoDuration(seconds int64) time.Duration {
	return time.Duration(seconds) * time.Second
}
