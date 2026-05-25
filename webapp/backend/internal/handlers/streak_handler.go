package handlers

import (
	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// StreakHandler handles streak and achievement HTTP requests
type StreakHandler struct {
	streakService *services.StreakAchievementService
	logger        *zap.Logger
}

// NewStreakHandler creates a new StreakHandler
func NewStreakHandler(streakService *services.StreakAchievementService, logger *zap.Logger) *StreakHandler {
	return &StreakHandler{
		streakService: streakService,
		logger:        logger,
	}
}

// RecordVisit records a daily visit and returns updated streak
// POST /api/user/streak/visit
func (h *StreakHandler) RecordVisit(c *fiber.Ctx) error {
	userID, err := parseUserID(c)
	if err != nil {
		return utils.ErrorResponse(c, 401, "Unauthorized")
	}

	streak, err := h.streakService.RecordVisit(userID)
	if err != nil {
		return utils.ErrorResponse(c, 500, "Failed to record visit")
	}

	return utils.SuccessResponse(c, "Visit recorded", streak)
}

// GetStreak returns the current streak info
// GET /api/user/streak
func (h *StreakHandler) GetStreak(c *fiber.Ctx) error {
	userID, err := parseUserID(c)
	if err != nil {
		return utils.ErrorResponse(c, 401, "Unauthorized")
	}

	streak, err := h.streakService.GetStreak(userID)
	if err != nil {
		// Return empty streak
		return utils.SuccessResponse(c, "Streak info", map[string]interface{}{
			"current_streak": 0,
			"longest_streak": 0,
			"total_visits":   0,
		})
	}

	return utils.SuccessResponse(c, "Streak info", streak)
}

// GetAchievements returns all achievements for the user
// GET /api/user/achievements
func (h *StreakHandler) GetAchievements(c *fiber.Ctx) error {
	userID, err := parseUserID(c)
	if err != nil {
		return utils.ErrorResponse(c, 401, "Unauthorized")
	}

	achievements, err := h.streakService.GetAchievements(userID)
	if err != nil {
		return utils.ErrorResponse(c, 500, "Failed to get achievements")
	}

	return utils.SuccessResponse(c, "Achievements", achievements)
}

// GetProgress returns combined streak + achievement data
// GET /api/user/progress
func (h *StreakHandler) GetProgress(c *fiber.Ctx) error {
	userID, err := parseUserID(c)
	if err != nil {
		return utils.ErrorResponse(c, 401, "Unauthorized")
	}

	progress, err := h.streakService.GetUserProgress(userID)
	if err != nil {
		return utils.ErrorResponse(c, 500, "Failed to get progress")
	}

	return utils.SuccessResponse(c, "User progress", progress)
}

// GetLeaderboard returns top streaks
// GET /api/streak/leaderboard
func (h *StreakHandler) GetLeaderboard(c *fiber.Ctx) error {
	limit := c.QueryInt("limit", 10)
	if limit > 50 {
		limit = 50
	}

	streaks, err := h.streakService.GetLeaderboard(limit)
	if err != nil {
		return utils.ErrorResponse(c, 500, "Failed to get leaderboard")
	}

	return utils.SuccessResponse(c, "Leaderboard", streaks)
}

// parseUserID extracts user UUID from context
func parseUserID(c *fiber.Ctx) (uuid.UUID, error) {
	uid := c.Locals("user_id")
	if uid == nil {
		return uuid.UUID{}, fiber.NewError(401, "Unauthorized")
	}
	uidStr, ok := uid.(string)
	if !ok {
		return uuid.UUID{}, fiber.NewError(401, "Invalid user ID")
	}
	return uuid.Parse(uidStr)
}
