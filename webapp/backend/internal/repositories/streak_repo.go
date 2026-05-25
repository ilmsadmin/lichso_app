package repositories

import (
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// StreakRepository handles data access for user streaks
type StreakRepository struct {
	db *gorm.DB
}

// NewStreakRepository creates a new StreakRepository
func NewStreakRepository(db *gorm.DB) *StreakRepository {
	return &StreakRepository{db: db}
}

// FindByUserID finds a streak record by user ID
func (r *StreakRepository) FindByUserID(userID uuid.UUID) (*models.UserStreak, error) {
	var streak models.UserStreak
	err := r.db.Where("user_id = ?", userID).First(&streak).Error
	if err != nil {
		return nil, err
	}
	return &streak, nil
}

// Upsert creates or updates a streak record
func (r *StreakRepository) Upsert(streak *models.UserStreak) error {
	return r.db.Save(streak).Error
}

// RecordVisit records a daily visit and updates the streak
func (r *StreakRepository) RecordVisit(userID uuid.UUID) (*models.UserStreak, error) {
	streak, err := r.FindByUserID(userID)
	if err != nil {
		// Create new streak
		now := time.Now()
		today := time.Date(now.Year(), now.Month(), now.Day(), 0, 0, 0, 0, time.UTC)
		streak = &models.UserStreak{
			UserID:        userID,
			CurrentStreak: 1,
			LongestStreak: 1,
			LastVisitDate: &today,
			TotalVisits:   1,
		}
		err = r.db.Create(streak).Error
		return streak, err
	}

	now := time.Now()
	today := time.Date(now.Year(), now.Month(), now.Day(), 0, 0, 0, 0, time.UTC)

	if streak.LastVisitDate != nil {
		lastVisit := time.Date(streak.LastVisitDate.Year(), streak.LastVisitDate.Month(), streak.LastVisitDate.Day(), 0, 0, 0, 0, time.UTC)
		diff := today.Sub(lastVisit).Hours() / 24

		if diff == 0 {
			// Already visited today
			return streak, nil
		} else if diff == 1 {
			// Consecutive day
			streak.CurrentStreak++
		} else {
			// Streak broken
			streak.CurrentStreak = 1
		}
	} else {
		streak.CurrentStreak = 1
	}

	if streak.CurrentStreak > streak.LongestStreak {
		streak.LongestStreak = streak.CurrentStreak
	}
	streak.LastVisitDate = &today
	streak.TotalVisits++
	streak.UpdatedAt = now

	err = r.db.Save(streak).Error
	return streak, err
}

// GetTopStreaks returns top N users by current streak
func (r *StreakRepository) GetTopStreaks(limit int) ([]models.UserStreak, error) {
	var streaks []models.UserStreak
	err := r.db.Order("current_streak DESC").Limit(limit).Find(&streaks).Error
	return streaks, err
}

// ============================================
// Achievement Repository
// ============================================

// AchievementRepository handles data access for user achievements
type AchievementRepository struct {
	db *gorm.DB
}

// NewAchievementRepository creates a new AchievementRepository
func NewAchievementRepository(db *gorm.DB) *AchievementRepository {
	return &AchievementRepository{db: db}
}

// FindByUserID returns all achievements for a user
func (r *AchievementRepository) FindByUserID(userID uuid.UUID) ([]models.UserAchievement, error) {
	var achievements []models.UserAchievement
	err := r.db.Where("user_id = ?", userID).Order("created_at ASC").Find(&achievements).Error
	return achievements, err
}

// FindByUserAndKey finds a specific achievement
func (r *AchievementRepository) FindByUserAndKey(userID uuid.UUID, key string) (*models.UserAchievement, error) {
	var achievement models.UserAchievement
	err := r.db.Where("user_id = ? AND achievement_key = ?", userID, key).First(&achievement).Error
	if err != nil {
		return nil, err
	}
	return &achievement, nil
}

// Upsert creates or updates an achievement
func (r *AchievementRepository) Upsert(achievement *models.UserAchievement) error {
	return r.db.Save(achievement).Error
}

// IncrementProgress increments the progress of an achievement and checks if it's unlocked
func (r *AchievementRepository) IncrementProgress(userID uuid.UUID, key string, amount int) (*models.UserAchievement, error) {
	achievement, err := r.FindByUserAndKey(userID, key)
	if err != nil {
		// Create with defaults from definition
		var def *models.AchievementDefinition
		for _, d := range models.DefaultAchievements {
			if d.Key == key {
				def = &d
				break
			}
		}
		if def == nil {
			return nil, err
		}
		now := time.Now()
		achievement = &models.UserAchievement{
			UserID:          userID,
			AchievementKey:  key,
			AchievementName: def.Name,
			Description:     def.Description,
			Badge:           def.Badge,
			Progress:        amount,
			Target:          def.Target,
		}
		if achievement.Progress >= achievement.Target {
			achievement.Unlocked = true
			achievement.UnlockedAt = &now
		}
		err = r.db.Create(achievement).Error
		return achievement, err
	}

	if achievement.Unlocked {
		return achievement, nil // Already unlocked
	}

	achievement.Progress += amount
	now := time.Now()
	if achievement.Progress >= achievement.Target {
		achievement.Unlocked = true
		achievement.UnlockedAt = &now
	}
	achievement.UpdatedAt = now

	err = r.db.Save(achievement).Error
	return achievement, err
}

// InitUserAchievements creates all achievement records for a new user
func (r *AchievementRepository) InitUserAchievements(userID uuid.UUID) error {
	for _, def := range models.DefaultAchievements {
		existing, _ := r.FindByUserAndKey(userID, def.Key)
		if existing != nil {
			continue
		}
		achievement := &models.UserAchievement{
			UserID:          userID,
			AchievementKey:  def.Key,
			AchievementName: def.Name,
			Description:     def.Description,
			Badge:           def.Badge,
			Target:          def.Target,
		}
		if err := r.db.Create(achievement).Error; err != nil {
			return err
		}
	}
	return nil
}
