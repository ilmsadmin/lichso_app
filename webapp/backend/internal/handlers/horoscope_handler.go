package handlers

import (
	"strconv"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// HoroscopeHandler handles horoscope-related HTTP requests
type HoroscopeHandler struct {
	horoscopeService *services.HoroscopeService
	logger           *zap.Logger
}

// NewHoroscopeHandler creates a new HoroscopeHandler
func NewHoroscopeHandler(horoscopeService *services.HoroscopeService, logger *zap.Logger) *HoroscopeHandler {
	return &HoroscopeHandler{
		horoscopeService: horoscopeService,
		logger:           logger,
	}
}

// GetDailyAll returns horoscopes for all 12 zodiac signs for a given date
// GET /api/horoscope/daily/:year/:month/:day
func (h *HoroscopeHandler) GetDailyAll(c *fiber.Ctx) error {
	year, err := strconv.Atoi(c.Params("year"))
	if err != nil || year < 1900 || year > 2100 {
		return utils.ErrorResponse(c, 400, "Invalid year")
	}
	month, err := strconv.Atoi(c.Params("month"))
	if err != nil || month < 1 || month > 12 {
		return utils.ErrorResponse(c, 400, "Invalid month")
	}
	day, err := strconv.Atoi(c.Params("day"))
	if err != nil || day < 1 || day > 31 {
		return utils.ErrorResponse(c, 400, "Invalid day")
	}

	result := h.horoscopeService.GetAllZodiacHoroscope(day, month, year)
	return utils.SuccessResponse(c, "Tử vi ngày", result)
}

// GetDailyByZodiac returns horoscope for a specific zodiac sign on a given date
// GET /api/horoscope/daily/:year/:month/:day/:zodiac
func (h *HoroscopeHandler) GetDailyByZodiac(c *fiber.Ctx) error {
	year, err := strconv.Atoi(c.Params("year"))
	if err != nil || year < 1900 || year > 2100 {
		return utils.ErrorResponse(c, 400, "Invalid year")
	}
	month, err := strconv.Atoi(c.Params("month"))
	if err != nil || month < 1 || month > 12 {
		return utils.ErrorResponse(c, 400, "Invalid month")
	}
	day, err := strconv.Atoi(c.Params("day"))
	if err != nil || day < 1 || day > 31 {
		return utils.ErrorResponse(c, 400, "Invalid day")
	}
	zodiac, err := strconv.Atoi(c.Params("zodiac"))
	if err != nil || zodiac < 0 || zodiac > 11 {
		return utils.ErrorResponse(c, 400, "Invalid zodiac index (0-11)")
	}

	result := h.horoscopeService.GetDailyHoroscope(day, month, year, zodiac)
	return utils.SuccessResponse(c, "Tử vi ngày", result)
}

// GetByBirthYear returns horoscope for today based on birth year
// GET /api/horoscope/birth-year/:year
func (h *HoroscopeHandler) GetByBirthYear(c *fiber.Ctx) error {
	birthYear, err := strconv.Atoi(c.Params("year"))
	if err != nil || birthYear < 1900 || birthYear > 2100 {
		return utils.ErrorResponse(c, 400, "Invalid birth year")
	}

	zodiacIdx := h.horoscopeService.GetZodiacFromYear(birthYear)

	now := time.Now()
	dd, mm, yy := now.Day(), int(now.Month()), now.Year()

	result := h.horoscopeService.GetDailyHoroscope(dd, mm, yy, zodiacIdx)
	return utils.SuccessResponse(c, "Tử vi ngày theo năm sinh", result)
}

// CalculateLunarAge calculates lunar age and related info
// GET /api/horoscope/lunar-age/:year
func (h *HoroscopeHandler) CalculateLunarAge(c *fiber.Ctx) error {
	birthYear, err := strconv.Atoi(c.Params("year"))
	if err != nil || birthYear < 1900 || birthYear > 2100 {
		return utils.ErrorResponse(c, 400, "Invalid birth year")
	}

	result := h.horoscopeService.CalculateLunarAge(birthYear)
	return utils.SuccessResponse(c, "Tuổi âm lịch", result)
}
