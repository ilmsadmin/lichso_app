package handlers

import (
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// PushNotificationHandler handles device token registration and campaign management.
type PushNotificationHandler struct {
	campaignService *services.PushCampaignService
	deviceTokenRepo interface {
		Upsert(*models.DeviceToken) error
		DeactivateByToken(string) error
	}
	logger *zap.Logger
}

func NewPushNotificationHandler(
	campaignService *services.PushCampaignService,
	logger *zap.Logger,
) *PushNotificationHandler {
	return &PushNotificationHandler{
		campaignService: campaignService,
		logger:          logger,
	}
}

// ── Device token (user-facing) ────────────────────────────────────────────

type registerTokenRequest struct {
	Token      string `json:"token" validate:"required,min=10"`
	Platform   string `json:"platform" validate:"required,oneof=android ios web"`
	AppVersion string `json:"app_version"`
	DeviceID   string `json:"device_id"`
}

// RegisterToken handles POST /api/push/register
// Called by the Android app after receiving an FCM token.
func (h *PushNotificationHandler) RegisterToken(c *fiber.Ctx) error {
	var req registerTokenRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	token := &models.DeviceToken{
		Token:      req.Token,
		Platform:   req.Platform,
		AppVersion: req.AppVersion,
		DeviceID:   req.DeviceID,
		IsActive:   true,
		LastSeen:   time.Now(),
	}

	// Attach user ID from JWT claims if authenticated
	if userIDStr, ok := c.Locals("userID").(string); ok && userIDStr != "" {
		if uid, err := uuid.Parse(userIDStr); err == nil {
			token.UserID = &uid
		}
	}

	// Use campaignService's embedded device token repo via the handler
	// We reach it through a stored reference set at wiring time
	if h.deviceTokenRepo != nil {
		if err := h.deviceTokenRepo.Upsert(token); err != nil {
			h.logger.Error("Failed to upsert device token", zap.Error(err))
			return utils.InternalErrorResponse(c)
		}
	}

	return utils.SuccessResponse(c, "Token registered", nil)
}

// UnregisterToken handles DELETE /api/push/register
func (h *PushNotificationHandler) UnregisterToken(c *fiber.Ctx) error {
	token := c.Query("token")
	if token == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "token query param required")
	}
	if h.deviceTokenRepo != nil {
		_ = h.deviceTokenRepo.DeactivateByToken(token)
	}
	return utils.SuccessResponse(c, "Token unregistered", nil)
}

// SetDeviceTokenRepo wires the repository after construction (avoids circular deps).
func (h *PushNotificationHandler) SetDeviceTokenRepo(repo interface {
	Upsert(*models.DeviceToken) error
	DeactivateByToken(string) error
}) {
	h.deviceTokenRepo = repo
}

// ── Admin campaign endpoints ──────────────────────────────────────────────

type createCampaignRequest struct {
	Title       string            `json:"title" validate:"required,min=2,max=255"`
	Body        string            `json:"body" validate:"required,min=2"`
	ImageURL    string            `json:"image_url"`
	ClickAction string            `json:"click_action"`
	DataPayload map[string]string `json:"data_payload"`
	TargetType  string            `json:"target_type" validate:"required,oneof=all users"`
	TargetUsers []string          `json:"target_users"`
	ScheduledAt *time.Time        `json:"scheduled_at"`
}

// AdminListCampaigns handles GET /api/admin/push/campaigns
func (h *PushNotificationHandler) AdminListCampaigns(c *fiber.Ctx) error {
	page := c.QueryInt("page", 1)
	limit := c.QueryInt("limit", 20)
	status := c.Query("status")

	campaigns, total, err := h.campaignService.List(page, limit, status)
	if err != nil {
		return utils.InternalErrorResponse(c)
	}

	return utils.PaginatedResponse(c, "Campaigns", campaigns, page, limit, total)
}

// AdminGetCampaign handles GET /api/admin/push/campaigns/:id
func (h *PushNotificationHandler) AdminGetCampaign(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid campaign ID")
	}

	campaign, err := h.campaignService.GetByID(id)
	if err != nil {
		return utils.NotFoundResponse(c, "Campaign not found")
	}
	return utils.SuccessResponse(c, "Campaign", campaign)
}

// AdminCreateCampaign handles POST /api/admin/push/campaigns
func (h *PushNotificationHandler) AdminCreateCampaign(c *fiber.Ctx) error {
	var req createCampaignRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	var createdBy *uuid.UUID
	if userIDStr, ok := c.Locals("userID").(string); ok {
		if uid, err := uuid.Parse(userIDStr); err == nil {
			createdBy = &uid
		}
	}

	campaign, err := h.campaignService.Create(services.CreateCampaignInput{
		Title:       req.Title,
		Body:        req.Body,
		ImageURL:    req.ImageURL,
		ClickAction: req.ClickAction,
		DataPayload: req.DataPayload,
		TargetType:  req.TargetType,
		TargetUsers: req.TargetUsers,
		ScheduledAt: req.ScheduledAt,
		CreatedBy:   createdBy,
	})
	if err != nil {
		h.logger.Error("Failed to create campaign", zap.Error(err))
		return utils.InternalErrorResponse(c)
	}

	return utils.CreatedResponse(c, "Campaign created", campaign)
}

// AdminUpdateCampaign handles PUT /api/admin/push/campaigns/:id
func (h *PushNotificationHandler) AdminUpdateCampaign(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid campaign ID")
	}

	var req createCampaignRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	campaign, err := h.campaignService.Update(id, services.UpdateCampaignInput{
		Title:       req.Title,
		Body:        req.Body,
		ImageURL:    req.ImageURL,
		ClickAction: req.ClickAction,
		DataPayload: req.DataPayload,
		TargetType:  req.TargetType,
		TargetUsers: req.TargetUsers,
		ScheduledAt: req.ScheduledAt,
	})
	if err != nil {
		h.logger.Error("Failed to update campaign", zap.Error(err))
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Campaign updated", campaign)
}

// AdminDeleteCampaign handles DELETE /api/admin/push/campaigns/:id
func (h *PushNotificationHandler) AdminDeleteCampaign(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid campaign ID")
	}
	if err := h.campaignService.Delete(id); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}
	return utils.NoContentResponse(c)
}

// AdminSendCampaign handles POST /api/admin/push/campaigns/:id/send
func (h *PushNotificationHandler) AdminSendCampaign(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid campaign ID")
	}
	if err := h.campaignService.Send(id); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}
	return utils.SuccessResponse(c, "Campaign is being sent", nil)
}

// AdminGetStats handles GET /api/admin/push/stats
func (h *PushNotificationHandler) AdminGetStats(c *fiber.Ctx) error {
	count, err := h.campaignService.ActiveDeviceCount()
	if err != nil {
		return utils.InternalErrorResponse(c)
	}
	return utils.SuccessResponse(c, "Push stats", fiber.Map{
		"active_devices": count,
	})
}
