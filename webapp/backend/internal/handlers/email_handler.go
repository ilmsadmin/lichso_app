package handlers

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// EmailHandler handles email-related HTTP requests
type EmailHandler struct {
	emailService *services.EmailService
	logger       *zap.Logger
}

// NewEmailHandler creates a new EmailHandler
func NewEmailHandler(emailService *services.EmailService, logger *zap.Logger) *EmailHandler {
	return &EmailHandler{
		emailService: emailService,
		logger:       logger,
	}
}

// SendTestEmail handles POST /api/admin/email/test
func (h *EmailHandler) SendTestEmail(c *fiber.Ctx) error {
	var req struct {
		To string `json:"to" validate:"required,email"`
	}

	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid JSON format")
	}

	if req.To == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Email address is required")
	}

	if !h.emailService.IsEnabled() {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Email sending is not configured. Please update SMTP settings.")
	}

	if err := h.emailService.SendTestEmail(req.To); err != nil {
		h.logger.Error("Failed to send test email", zap.Error(err))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Failed to send test email: "+err.Error())
	}

	return utils.SuccessResponse(c, "Test email sent successfully", nil)
}

// GetEmailStatus handles GET /api/admin/email/status
func (h *EmailHandler) GetEmailStatus(c *fiber.Ctx) error {
	return utils.SuccessResponse(c, "Email status retrieved", fiber.Map{
		"enabled": h.emailService.IsEnabled(),
	})
}
