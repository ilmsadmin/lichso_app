package services

import (
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"go.uber.org/zap"
)

// StreakAchievementService handles streaks and achievements business logic
type StreakAchievementService struct {
	streakRepo      *repositories.StreakRepository
	achievementRepo *repositories.AchievementRepository
	logger          *zap.Logger
}

// NewStreakAchievementService creates a new StreakAchievementService
func NewStreakAchievementService(
	streakRepo *repositories.StreakRepository,
	achievementRepo *repositories.AchievementRepository,
	logger *zap.Logger,
) *StreakAchievementService {
	return &StreakAchievementService{
		streakRepo:      streakRepo,
		achievementRepo: achievementRepo,
		logger:          logger,
	}
}

// RecordVisit records a daily visit, updates streak, and checks streak achievements
func (s *StreakAchievementService) RecordVisit(userID uuid.UUID) (*models.UserStreak, error) {
	streak, err := s.streakRepo.RecordVisit(userID)
	if err != nil {
		return nil, err
	}

	// Check streak-related achievements
	if streak.CurrentStreak >= 7 {
		s.achievementRepo.IncrementProgress(userID, "lich_su_gia", 0) // Just ensure it's created
		ach, _ := s.achievementRepo.FindByUserAndKey(userID, "lich_su_gia")
		if ach != nil && !ach.Unlocked {
			ach.Progress = streak.CurrentStreak
			if ach.Progress >= ach.Target {
				s.achievementRepo.Upsert(ach)
			}
		}
	}

	if streak.CurrentStreak >= 30 {
		s.achievementRepo.IncrementProgress(userID, "streak_30", 0)
		ach, _ := s.achievementRepo.FindByUserAndKey(userID, "streak_30")
		if ach != nil && !ach.Unlocked {
			ach.Progress = streak.CurrentStreak
			s.achievementRepo.Upsert(ach)
		}
	}

	return streak, nil
}

// GetStreak returns the streak data for a user
func (s *StreakAchievementService) GetStreak(userID uuid.UUID) (*models.UserStreak, error) {
	return s.streakRepo.FindByUserID(userID)
}

// GetAchievements returns all achievements for a user
func (s *StreakAchievementService) GetAchievements(userID uuid.UUID) ([]models.UserAchievement, error) {
	achievements, err := s.achievementRepo.FindByUserID(userID)
	if err != nil || len(achievements) == 0 {
		// Initialize if not found
		if err := s.achievementRepo.InitUserAchievements(userID); err != nil {
			return nil, err
		}
		return s.achievementRepo.FindByUserID(userID)
	}
	return achievements, nil
}

// IncrementAchievement increments progress for a specific achievement
func (s *StreakAchievementService) IncrementAchievement(userID uuid.UUID, key string, amount int) (*models.UserAchievement, error) {
	return s.achievementRepo.IncrementProgress(userID, key, amount)
}

// GetLeaderboard returns top streaks
func (s *StreakAchievementService) GetLeaderboard(limit int) ([]models.UserStreak, error) {
	return s.streakRepo.GetTopStreaks(limit)
}

// GetUserProgress returns combined streak + achievement data
func (s *StreakAchievementService) GetUserProgress(userID uuid.UUID) (map[string]interface{}, error) {
	streak, _ := s.streakRepo.FindByUserID(userID)
	achievements, _ := s.GetAchievements(userID)

	unlockedCount := 0
	for _, a := range achievements {
		if a.Unlocked {
			unlockedCount++
		}
	}

	result := map[string]interface{}{
		"streak":             streak,
		"achievements":       achievements,
		"total_achievements": len(models.DefaultAchievements),
		"unlocked_count":     unlockedCount,
	}

	return result, nil
}
