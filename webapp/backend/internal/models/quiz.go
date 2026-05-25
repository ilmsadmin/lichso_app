package models

import (
	"time"

	"github.com/google/uuid"
	"github.com/lib/pq"
)

// QuizQuestion represents a single quiz question stored in the database.
type QuizQuestion struct {
	ID          int64      `gorm:"primaryKey;autoIncrement" json:"id"`
	Content     string     `gorm:"type:text;not null" json:"content"`
	OptionA     string     `gorm:"type:varchar(500);not null" json:"option_a"`
	OptionB     string     `gorm:"type:varchar(500);not null" json:"option_b"`
	OptionC     string     `gorm:"type:varchar(500);not null" json:"option_c"`
	OptionD     string     `gorm:"type:varchar(500);not null" json:"option_d"`
	Correct     string     `gorm:"type:char(1);not null" json:"correct"` // only in admin responses
	Explanation string     `gorm:"type:text" json:"explanation,omitempty"`
	Category    string     `gorm:"type:varchar(100);not null;default:'history_vn'" json:"category"`
	Difficulty  string     `gorm:"type:varchar(20);not null;default:'medium'" json:"difficulty"`
	ArticleID   *int64     `gorm:"index" json:"article_id,omitempty"`
	IsActive    bool       `gorm:"default:true" json:"is_active"`
	CreatedBy   *uuid.UUID `gorm:"type:uuid" json:"created_by,omitempty"`
	CreatedAt   time.Time  `json:"created_at"`
	UpdatedAt   time.Time  `json:"updated_at"`
}

func (QuizQuestion) TableName() string { return "quiz_questions" }

// QuizDailySet represents an admin-curated set of questions for a specific date.
type QuizDailySet struct {
	ID          int64         `gorm:"primaryKey;autoIncrement" json:"id"`
	Date        time.Time     `gorm:"type:date;uniqueIndex;not null" json:"date"`
	QuestionIDs pq.Int64Array `gorm:"type:bigint[];not null" json:"question_ids"`
	CreatedBy   *uuid.UUID    `gorm:"type:uuid" json:"created_by,omitempty"`
	CreatedAt   time.Time     `json:"created_at"`
}

func (QuizDailySet) TableName() string { return "quiz_daily_sets" }

// QuizAnswer represents one answer within a session's answers JSONB array.
type QuizAnswer struct {
	QuestionID int64  `json:"question_id"`
	Chosen     string `json:"chosen"`    // "a"|"b"|"c"|"d"
	IsCorrect  bool   `json:"is_correct"`
	TimeMs     int    `json:"time_ms"`
}

// QuizSession represents a user's quiz-playing session.
type QuizSession struct {
	ID          uuid.UUID     `gorm:"type:uuid;primaryKey;default:gen_random_uuid()" json:"id"`
	UserID      uuid.UUID     `gorm:"type:uuid;not null;index" json:"user_id"`
	SessionType string        `gorm:"type:varchar(20);not null;default:'daily'" json:"session_type"`
	Category    string        `gorm:"type:varchar(100)" json:"category,omitempty"`
	QuestionIDs pq.Int64Array `gorm:"type:bigint[];not null" json:"question_ids"`
	Answers     []byte        `gorm:"type:jsonb;not null;default:'[]'" json:"-"`
	Score       int           `gorm:"not null;default:0" json:"score"`
	Total       int           `gorm:"not null;default:0" json:"total"`
	Completed   bool          `gorm:"default:false" json:"completed"`
	StartedAt   time.Time     `gorm:"not null;default:now()" json:"started_at"`
	FinishedAt  *time.Time    `json:"finished_at,omitempty"`
}

func (QuizSession) TableName() string { return "quiz_sessions" }

// QuizScore represents aggregated scores for a user, used by the leaderboard.
type QuizScore struct {
	UserID      uuid.UUID  `gorm:"type:uuid;primaryKey" json:"user_id"`
	DisplayName string     `gorm:"type:varchar(200);not null;default:''" json:"display_name"`
	AvatarURL   string     `gorm:"type:varchar(500)" json:"avatar_url,omitempty"`
	TotalScore  int        `gorm:"not null;default:0" json:"total_score"`
	WeekScore   int        `gorm:"not null;default:0" json:"week_score"`
	MonthScore  int        `gorm:"not null;default:0" json:"month_score"`
	BestStreak  int        `gorm:"not null;default:0" json:"best_streak"`
	CurStreak   int        `gorm:"not null;default:0" json:"cur_streak"`
	LastQuiz    *time.Time `gorm:"type:date" json:"last_quiz,omitempty"`
	UpdatedAt   time.Time  `json:"updated_at"`
}

func (QuizScore) TableName() string { return "quiz_scores" }
