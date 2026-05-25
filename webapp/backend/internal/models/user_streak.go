package models

import (
	"time"

	"github.com/google/uuid"
)

// UserStreak tracks daily visit streaks for gamification
type UserStreak struct {
	ID            uuid.UUID  `gorm:"type:uuid;primaryKey;default:gen_random_uuid()" json:"id"`
	UserID        uuid.UUID  `gorm:"type:uuid;uniqueIndex;not null" json:"user_id"`
	CurrentStreak int        `gorm:"not null;default:0" json:"current_streak"`
	LongestStreak int        `gorm:"not null;default:0" json:"longest_streak"`
	LastVisitDate *time.Time `gorm:"type:date" json:"last_visit_date,omitempty"`
	TotalVisits   int        `gorm:"not null;default:0" json:"total_visits"`
	CreatedAt     time.Time  `gorm:"not null;default:now()" json:"created_at"`
	UpdatedAt     time.Time  `gorm:"not null;default:now()" json:"updated_at"`
}

// TableName returns the table name
func (UserStreak) TableName() string {
	return "user_streaks"
}

// UserAchievement tracks user achievement progress
type UserAchievement struct {
	ID              uuid.UUID  `gorm:"type:uuid;primaryKey;default:gen_random_uuid()" json:"id"`
	UserID          uuid.UUID  `gorm:"type:uuid;not null" json:"user_id"`
	AchievementKey  string     `gorm:"type:varchar(50);not null" json:"achievement_key"`
	AchievementName string     `gorm:"type:varchar(200);not null" json:"achievement_name"`
	Description     string     `gorm:"type:text" json:"description,omitempty"`
	Badge           string     `gorm:"type:varchar(10)" json:"badge"`
	Progress        int        `gorm:"not null;default:0" json:"progress"`
	Target          int        `gorm:"not null;default:1" json:"target"`
	Unlocked        bool       `gorm:"not null;default:false" json:"unlocked"`
	UnlockedAt      *time.Time `json:"unlocked_at,omitempty"`
	CreatedAt       time.Time  `gorm:"not null;default:now()" json:"created_at"`
	UpdatedAt       time.Time  `gorm:"not null;default:now()" json:"updated_at"`
}

// TableName returns the table name
func (UserAchievement) TableName() string {
	return "user_achievements"
}

// AchievementDefinition defines an achievement template
type AchievementDefinition struct {
	Key         string `json:"key"`
	Name        string `json:"name"`
	Description string `json:"description"`
	Badge       string `json:"badge"`
	Target      int    `json:"target"`
}

// DefaultAchievements is the list of all available achievements
var DefaultAchievements = []AchievementDefinition{
	{Key: "lich_su_gia", Name: "Lịch sử gia", Description: "Xem lịch 7 ngày liên tiếp", Badge: "📅", Target: 7},
	{Key: "nha_van_hoa", Name: "Nhà văn hoá", Description: "Đọc 10 bài viết", Badge: "📚", Target: 10},
	{Key: "ghi_chu_sieng", Name: "Ghi chú siêng", Description: "Ghi chú 30 ngày", Badge: "✍️", Target: 30},
	{Key: "kham_pha", Name: "Khám phá", Description: "Xem 12 tháng khác nhau", Badge: "🗺️", Target: 12},
	{Key: "chia_se", Name: "Chia sẻ", Description: "Share 5 lần", Badge: "📤", Target: 5},
	{Key: "bookworm", Name: "Bookworm", Description: "Đọc 50 bài viết", Badge: "🐛", Target: 50},
	{Key: "streak_30", Name: "Streak 30", Description: "Truy cập 30 ngày liên tiếp", Badge: "🔥", Target: 30},
	{Key: "phong_thuy_master", Name: "Phong thuỷ master", Description: "Xem phong thuỷ 100 ngày", Badge: "🧭", Target: 100},
}
