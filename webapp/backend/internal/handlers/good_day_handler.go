package handlers

import (
	"strconv"

	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// GoodDayHandler handles enhanced good day HTTP requests
type GoodDayHandler struct {
	goodDayService *services.GoodDayService
	logger         *zap.Logger
}

// NewGoodDayHandler creates a new GoodDayHandler
func NewGoodDayHandler(goodDayService *services.GoodDayService, logger *zap.Logger) *GoodDayHandler {
	return &GoodDayHandler{
		goodDayService: goodDayService,
		logger:         logger,
	}
}

// GetPurposes returns all available purpose types
// GET /api/good-days/purposes
func (h *GoodDayHandler) GetPurposes(c *fiber.Ctx) error {
	result := h.goodDayService.GetPurposes()
	return utils.SuccessResponse(c, "Danh sách mục đích xem ngày", result)
}

// GetGoodDaysForPurpose returns good days for a specific purpose in a given month
// GET /api/good-days/:year/:month/:purpose
func (h *GoodDayHandler) GetGoodDaysForPurpose(c *fiber.Ctx) error {
	year, err := strconv.Atoi(c.Params("year"))
	if err != nil || year < 1900 || year > 2100 {
		return utils.ErrorResponse(c, 400, "Invalid year")
	}
	month, err := strconv.Atoi(c.Params("month"))
	if err != nil || month < 1 || month > 12 {
		return utils.ErrorResponse(c, 400, "Invalid month")
	}
	purpose := c.Params("purpose")
	if purpose == "" {
		return utils.ErrorResponse(c, 400, "Purpose is required")
	}

	birthYear, _ := strconv.Atoi(c.Query("birth_year", "0"))
	spouseYear, _ := strconv.Atoi(c.Query("spouse_year", "0"))

	result := h.goodDayService.GetGoodDaysForPurpose(year, month, purpose, birthYear, spouseYear)
	return utils.SuccessResponse(c, "Ngày tốt cho "+result.PurposeName, result)
}
