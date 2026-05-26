package handlers

import (
	"errors"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"github.com/zplus/lichso/internal/validators"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

// PointsHandler handles point-wallet requests.
type PointsHandler struct {
	db            *gorm.DB
	pointsService *services.PointsService
	validator     *validators.Validator
	logger        *zap.Logger
}

// NewPointsHandler creates a new PointsHandler.
func NewPointsHandler(db *gorm.DB, pointsService *services.PointsService, validator *validators.Validator, logger *zap.Logger) *PointsHandler {
	return &PointsHandler{
		db:            db,
		pointsService: pointsService,
		validator:     validator,
		logger:        logger,
	}
}

// GetWallet handles GET /points/wallet
func (h *PointsHandler) GetWallet(c *fiber.Ctx) error {
	userID, err := h.getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	wallet, err := h.pointsService.GetWallet(userID)
	if err != nil {
		h.logger.Error("GetWallet failed", zap.Error(err))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Ví điểm", wallet)
}

// GetTransactions handles GET /points/transactions
func (h *PointsHandler) GetTransactions(c *fiber.Ctx) error {
	userID, err := h.getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var txs []models.PointTransaction
	err = h.db.Where("user_id = ?", userID).Order("created_at DESC").Limit(50).Find(&txs).Error
	if err != nil {
		h.logger.Error("GetTransactions failed", zap.Error(err))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Lịch sử giao dịch điểm", txs)
}

// SpendPoints handles POST /points/spend
func (h *PointsHandler) SpendPoints(c *fiber.Ctx) error {
	userID, err := h.getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var req struct {
		Amount         int                    `json:"amount"`
		Source         string                 `json:"source"`
		SourceID       *string                `json:"source_id"`
		IdempotencyKey *string                `json:"idempotency_key"`
		Metadata       map[string]interface{} `json:"metadata"`
	}

	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	if req.Amount <= 0 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Amount must be positive")
	}
	if req.Source == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Source is required")
	}

	wallet, err := h.pointsService.SpendPoints(userID, req.Amount, req.Source, req.SourceID, req.IdempotencyKey, req.Metadata)
	if err != nil {
		h.logger.Error("SpendPoints failed", zap.Error(err), zap.String("user_id", userID.String()))
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiêu điểm thành công", wallet)
}

// SyncGuestPoints handles POST /points/sync-guest
func (h *PointsHandler) SyncGuestPoints(c *fiber.Ctx) error {
	userID, err := h.getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var req struct {
		Transactions []services.GuestTransaction `json:"transactions"`
	}

	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	synced, err := h.pointsService.SyncGuestPoints(userID, req.Transactions)
	if err != nil {
		h.logger.Error("SyncGuestPoints failed", zap.Error(err), zap.String("user_id", userID.String()))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	wallet, _ := h.pointsService.GetWallet(userID)

	return utils.SuccessResponse(c, "Đồng bộ điểm thành công", fiber.Map{
		"synced_count": synced,
		"wallet":       wallet,
	})
}

// Helper to get authenticated user ID
func (h *PointsHandler) getUserID(c *fiber.Ctx) (uuid.UUID, error) {
	val := c.Locals("userID")
	if val == nil {
		return uuid.Nil, errors.New("missing user ID")
	}
	idStr, ok := val.(string)
	if !ok {
		return uuid.Nil, errors.New("invalid user ID format")
	}
	return uuid.Parse(idStr)
}
