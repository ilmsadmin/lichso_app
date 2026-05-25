package handlers

import (
	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// NewsletterHandler handles newsletter HTTP requests
type NewsletterHandler struct {
	newsletterService *services.NewsletterService
	logger            *zap.Logger
}

// NewNewsletterHandler creates a new NewsletterHandler
func NewNewsletterHandler(newsletterService *services.NewsletterService, logger *zap.Logger) *NewsletterHandler {
	return &NewsletterHandler{
		newsletterService: newsletterService,
		logger:            logger,
	}
}

// Subscribe handles public newsletter subscription
// POST /api/newsletter/subscribe
func (h *NewsletterHandler) Subscribe(c *fiber.Ctx) error {
	var body struct {
		Email     string `json:"email" validate:"required,email"`
		Name      string `json:"name"`
		Frequency string `json:"frequency"`
	}

	if err := c.BodyParser(&body); err != nil {
		return utils.ErrorResponse(c, 400, "Invalid request body")
	}

	if body.Email == "" {
		return utils.ErrorResponse(c, 400, "Email is required")
	}

	// Check if user is authenticated (optional)
	var userID *uuid.UUID
	if uid := c.Locals("user_id"); uid != nil {
		if uidStr, ok := uid.(string); ok {
			if parsed, err := uuid.Parse(uidStr); err == nil {
				userID = &parsed
			}
		}
	}

	sub, err := h.newsletterService.Subscribe(body.Email, body.Name, body.Frequency, userID)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		return utils.ErrorResponse(c, 500, "Failed to subscribe")
	}

	return utils.SuccessResponse(c, "Đăng ký newsletter thành công", sub)
}

// Unsubscribe handles newsletter unsubscription
// POST /api/newsletter/unsubscribe
func (h *NewsletterHandler) Unsubscribe(c *fiber.Ctx) error {
	var body struct {
		Email string `json:"email" validate:"required,email"`
	}

	if err := c.BodyParser(&body); err != nil {
		return utils.ErrorResponse(c, 400, "Invalid request body")
	}

	if err := h.newsletterService.Unsubscribe(body.Email); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		return utils.ErrorResponse(c, 500, "Failed to unsubscribe")
	}

	return utils.SuccessResponse(c, "Huỷ đăng ký newsletter thành công", nil)
}

// GetSubscription returns subscription info
// GET /api/newsletter/status?email=xxx
func (h *NewsletterHandler) GetSubscription(c *fiber.Ctx) error {
	email := c.Query("email")
	if email == "" {
		return utils.ErrorResponse(c, 400, "Email is required")
	}

	sub, err := h.newsletterService.GetByEmail(email)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		return utils.ErrorResponse(c, 500, "Failed to get subscription")
	}

	return utils.SuccessResponse(c, "Newsletter subscription", sub)
}

// UpdatePreferences updates subscription preferences
// PUT /api/newsletter/preferences
func (h *NewsletterHandler) UpdatePreferences(c *fiber.Ctx) error {
	var body struct {
		Email       string       `json:"email" validate:"required,email"`
		Frequency   string       `json:"frequency"`
		Preferences models.JSONB `json:"preferences"`
	}

	if err := c.BodyParser(&body); err != nil {
		return utils.ErrorResponse(c, 400, "Invalid request body")
	}

	sub, err := h.newsletterService.UpdatePreferences(body.Email, body.Frequency, body.Preferences)
	if err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		return utils.ErrorResponse(c, 500, "Failed to update preferences")
	}

	return utils.SuccessResponse(c, "Cập nhật tuỳ chọn thành công", sub)
}

// AdminList returns all subscribers for admin
// GET /api/admin/newsletter?page=1&limit=20&search=xxx
func (h *NewsletterHandler) AdminList(c *fiber.Ctx) error {
	page := c.QueryInt("page", 1)
	limit := c.QueryInt("limit", 20)
	search := c.Query("search")

	subs, total, err := h.newsletterService.ListAll(page, limit, search)
	if err != nil {
		return utils.ErrorResponse(c, 500, "Failed to list subscribers")
	}

	return utils.PaginatedResponse(c, "Newsletter subscribers", subs, page, limit, total)
}

// AdminGetStats returns newsletter statistics
// GET /api/admin/newsletter/stats
func (h *NewsletterHandler) AdminGetStats(c *fiber.Ctx) error {
	stats, err := h.newsletterService.GetStats()
	if err != nil {
		return utils.ErrorResponse(c, 500, "Failed to get stats")
	}

	return utils.SuccessResponse(c, "Newsletter stats", stats)
}

// AdminDelete deletes a subscriber
// DELETE /api/admin/newsletter/:id
func (h *NewsletterHandler) AdminDelete(c *fiber.Ctx) error {
	id := c.Params("id")
	if err := h.newsletterService.Delete(id); err != nil {
		if appErr, ok := utils.IsAppError(err); ok {
			return utils.ErrorResponse(c, appErr.Code, appErr.Message)
		}
		return utils.ErrorResponse(c, 500, "Failed to delete subscriber")
	}

	return utils.SuccessResponse(c, "Subscriber deleted", nil)
}
