package handlers

import (
	"fmt"
	"strconv"

	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// ExportHandler handles calendar export HTTP requests.
type ExportHandler struct {
	exportService *services.ExportService
	logger        *zap.Logger
}

// NewExportHandler creates a new ExportHandler.
func NewExportHandler(exportService *services.ExportService, logger *zap.Logger) *ExportHandler {
	return &ExportHandler{exportService: exportService, logger: logger}
}

// ExportICal handles GET /api/export/ical
// Query params: year, month
func (h *ExportHandler) ExportICal(c *fiber.Ctx) error {
	year, month, err := parseYearMonth(c)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	data, err := h.exportService.GenerateICal(year, month)
	if err != nil {
		h.logger.Error("Failed to generate iCal", zap.Error(err))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Failed to generate calendar file")
	}

	filename := fmt.Sprintf("lichso-%d-%02d.ics", year, month)
	c.Set("Content-Type", "text/calendar; charset=utf-8")
	c.Set("Content-Disposition", fmt.Sprintf("attachment; filename=\"%s\"", filename))

	return c.Send(data)
}

// ExportText handles GET /api/export/text
// Query params: year, month — returns a plain text representation of the month
func (h *ExportHandler) ExportText(c *fiber.Ctx) error {
	year, month, err := parseYearMonth(c)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	text := h.exportService.GenerateMonthText(year, month)

	filename := fmt.Sprintf("lichso-%d-%02d.txt", year, month)
	c.Set("Content-Type", "text/plain; charset=utf-8")
	c.Set("Content-Disposition", fmt.Sprintf("attachment; filename=\"%s\"", filename))

	return c.SendString(text)
}

func parseYearMonth(c *fiber.Ctx) (int, int, error) {
	yearStr := c.Query("year")
	monthStr := c.Query("month")

	if yearStr == "" || monthStr == "" {
		return 0, 0, fmt.Errorf("year and month are required")
	}

	year, err := strconv.Atoi(yearStr)
	if err != nil || year < 1900 || year > 2100 {
		return 0, 0, fmt.Errorf("year must be between 1900 and 2100")
	}

	month, err := strconv.Atoi(monthStr)
	if err != nil || month < 1 || month > 12 {
		return 0, 0, fmt.Errorf("month must be between 1 and 12")
	}

	return year, month, nil
}
