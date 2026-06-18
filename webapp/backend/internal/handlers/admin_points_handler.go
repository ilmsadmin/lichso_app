package handlers

import (
	"strconv"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// ============================================
// Admin points moderation endpoints.
// Registered under /admin/points (see SetupPointsRoutes).
// ============================================

// AdminListUserPoints handles GET /admin/points/users
func (h *PointsHandler) AdminListUserPoints(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	sortBy := c.Query("sort_by", "total")

	rows, total, err := h.pointsService.AdminListUserPoints(pagination.Page, pagination.Limit, pagination.Search, sortBy)
	if err != nil {
		h.logger.Error("AdminListUserPoints failed", zap.Error(err))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách điểm người dùng", rows, pagination.Page, pagination.Limit, total)
}

// AdminGetUserPoints handles GET /admin/points/users/:userId
func (h *PointsHandler) AdminGetUserPoints(c *fiber.Ctx) error {
	userID, err := uuid.Parse(c.Params("userId"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid user ID")
	}

	detail, err := h.pointsService.AdminGetUserPoints(userID)
	if err != nil {
		return utils.NotFoundResponse(c, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết điểm người dùng", detail)
}

// AdminGetUserDailyPoints handles GET /admin/points/users/:userId/daily?days=30
func (h *PointsHandler) AdminGetUserDailyPoints(c *fiber.Ctx) error {
	userID, err := uuid.Parse(c.Params("userId"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid user ID")
	}

	days, _ := strconv.Atoi(c.Query("days", "30"))

	rows, err := h.pointsService.AdminGetUserDailyPoints(userID, days)
	if err != nil {
		h.logger.Error("AdminGetUserDailyPoints failed", zap.Error(err))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Lịch sử điểm hằng ngày", rows)
}

// AdminAdjustUserPoints handles POST /admin/points/users/:userId/adjust
func (h *PointsHandler) AdminAdjustUserPoints(c *fiber.Ctx) error {
	adminID, err := h.getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	userID, err := uuid.Parse(c.Params("userId"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid user ID")
	}

	var req struct {
		WalletDelta    int    `json:"wallet_delta"`
		ResetQuizScore bool   `json:"reset_quiz_score"`
		Reason         string `json:"reason"`
	}
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.pointsService.AdminAdjustUserPoints(adminID, userID, req.WalletDelta, req.ResetQuizScore, req.Reason)
	if err != nil {
		h.logger.Error("AdminAdjustUserPoints failed", zap.Error(err), zap.String("user_id", userID.String()))
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật điểm người dùng", result)
}
