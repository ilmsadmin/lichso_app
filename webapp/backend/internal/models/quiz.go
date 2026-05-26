package models

import (
	"time"

	"github.com/google/uuid"
	"github.com/lib/pq"
)

// QuizQuestion represents a single quiz question stored in the database.
type QuizQuestion struct {
	ID           int64      `gorm:"primaryKey;autoIncrement" json:"id"`
	Content      string     `gorm:"type:text;not null" json:"content"`
	OptionA      string     `gorm:"type:varchar(500);not null" json:"option_a"`
	OptionB      string     `gorm:"type:varchar(500);not null" json:"option_b"`
	OptionC      string     `gorm:"type:varchar(500);not null" json:"option_c"`
	OptionD      string     `gorm:"type:varchar(500);not null" json:"option_d"`
	Correct      string     `gorm:"type:char(1);not null" json:"correct"` // only in admin responses
	Hint         string     `gorm:"type:text" json:"hint,omitempty"`
	Explanation  string     `gorm:"type:text" json:"explanation,omitempty"`
	Category     string     `gorm:"type:varchar(100);not null;default:'history_vn'" json:"category"`
	Difficulty   string     `gorm:"type:varchar(20);not null;default:'medium'" json:"difficulty"`
	ArticleID    *int64     `gorm:"index" json:"article_id,omitempty"`
	IsActive     bool       `gorm:"default:true" json:"is_active"`
	CreatedBy    *uuid.UUID `gorm:"type:uuid" json:"created_by,omitempty"`
	CreatedAt    time.Time  `json:"created_at"`
	UpdatedAt    time.Time  `json:"updated_at"`
	SourceTitle  string     `gorm:"type:varchar(200)" json:"source_title,omitempty"`
	SourceURL    string     `gorm:"type:varchar(500)" json:"source_url,omitempty"`
	SourceNote   string     `gorm:"type:text" json:"source_note,omitempty"`
	ReviewStatus string     `gorm:"type:varchar(20);not null;default:'draft'" json:"review_status"`
	ReviewedBy   *uuid.UUID `gorm:"type:uuid" json:"reviewed_by,omitempty"`
	ReviewedAt   *time.Time `json:"reviewed_at,omitempty"`
	PublishedAt  *time.Time `json:"published_at,omitempty"`
	Version      int        `gorm:"default:1" json:"version"`
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
	Chosen     string `json:"chosen"` // "a"|"b"|"c"|"d"
	IsCorrect  bool   `json:"is_correct"`
	TimeMs     int    `json:"time_ms"`
}

// QuizSession represents a user's quiz-playing session.
type QuizSession struct {
	ID              uuid.UUID     `gorm:"type:uuid;primaryKey;default:gen_random_uuid()" json:"id"`
	UserID          uuid.UUID     `gorm:"type:uuid;not null;index" json:"user_id"`
	ClientSessionID *string       `gorm:"type:varchar(100);uniqueIndex" json:"client_session_id,omitempty"`
	SessionType     string        `gorm:"type:varchar(20);not null;default:'daily'" json:"session_type"`
	Category        string        `gorm:"type:varchar(100)" json:"category,omitempty"`
	QuestionIDs     pq.Int64Array `gorm:"type:bigint[];not null" json:"question_ids"`
	Answers         []byte        `gorm:"type:jsonb;not null;default:'[]'" json:"-"`
	Score           int           `gorm:"not null;default:0" json:"score"`
	ScoreV2         int           `gorm:"column:score_v2;not null;default:0" json:"score_v2"`
	Total           int           `gorm:"not null;default:0" json:"total"`
	Completed       bool          `gorm:"default:false" json:"completed"`
	StartedAt       time.Time     `gorm:"not null;default:now()" json:"started_at"`
	FinishedAt      *time.Time    `json:"finished_at,omitempty"`
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
	XP          int        `gorm:"column:xp;not null;default:0" json:"xp"`
}

func (QuizScore) TableName() string { return "quiz_scores" }

// PointWallet represents the unified app points balance for a user.
type PointWallet struct {
	UserID         uuid.UUID `gorm:"type:uuid;primaryKey" json:"user_id"`
	Balance        int       `gorm:"not null;default:0" json:"balance"`
	LifetimeEarned int       `gorm:"not null;default:0" json:"lifetime_earned"`
	LifetimeSpent  int       `gorm:"not null;default:0" json:"lifetime_spent"`
	UpdatedAt      time.Time `json:"updated_at"`
}

func (PointWallet) TableName() string { return "point_wallets" }

// PointTransaction represents a ledger entry for point balance changes.
type PointTransaction struct {
	ID             int64     `gorm:"primaryKey;autoIncrement" json:"id"`
	UserID         uuid.UUID `gorm:"type:uuid;not null;index" json:"user_id"`
	Amount         int       `gorm:"not null" json:"amount"`
	Direction      string    `gorm:"type:varchar(10);not null" json:"direction"` // "earn" | "spend"
	Source         string    `gorm:"type:varchar(50);not null" json:"source"`
	SourceID       *string   `gorm:"type:varchar(100)" json:"source_id,omitempty"`
	IdempotencyKey *string   `gorm:"type:varchar(100);uniqueIndex" json:"idempotency_key,omitempty"`
	Metadata       []byte    `gorm:"type:jsonb" json:"metadata,omitempty"`
	CreatedAt      time.Time `json:"created_at"`
}

func (PointTransaction) TableName() string { return "point_transactions" }

// QuizAssistUsage records when a user uses an assist (hint, 50/50, extra_time) during a quiz question.
type QuizAssistUsage struct {
	ID            int64     `gorm:"primaryKey;autoIncrement" json:"id"`
	SessionID     uuid.UUID `gorm:"type:uuid;not null;index" json:"session_id"`
	UserID        uuid.UUID `gorm:"type:uuid;not null" json:"user_id"`
	QuestionID    int64     `gorm:"not null" json:"question_id"`
	AssistType    string    `gorm:"type:varchar(20);not null" json:"assist_type"` // "fifty_fifty" | "hint" | "extra_time"
	CostAppPoints int       `gorm:"not null" json:"cost_app_points"`
	CreatedAt     time.Time `json:"created_at"`
}

func (QuizAssistUsage) TableName() string { return "quiz_assist_usages" }

// QuizCategoryMastery tracks user progress and title unlocking within a specific category.
type QuizCategoryMastery struct {
	ID             int64      `gorm:"primaryKey;autoIncrement" json:"id"`
	UserID         uuid.UUID  `gorm:"type:uuid;not null;uniqueIndex:idx_user_category" json:"user_id"`
	Category       string     `gorm:"type:varchar(100);not null;uniqueIndex:idx_user_category" json:"category"`
	AnsweredCount  int        `gorm:"not null;default:0" json:"answered_count"`
	CorrectCount   int        `gorm:"not null;default:0" json:"correct_count"`
	CorrectRate    float64    `gorm:"type:numeric(5,2);not null;default:0.00" json:"correct_rate"`
	CurrentStreak  int        `gorm:"not null;default:0" json:"current_streak"`
	BestStreak     int        `gorm:"not null;default:0" json:"best_streak"`
	MasteryLevel   int        `gorm:"not null;default:0" json:"mastery_level"`
	Title          string     `gorm:"type:varchar(100);not null;default:''" json:"title"`
	LastUnlockedAt *time.Time `json:"last_unlocked_at,omitempty"`
	CreatedAt      time.Time  `json:"created_at"`
	UpdatedAt      time.Time  `json:"updated_at"`
}

func (QuizCategoryMastery) TableName() string { return "quiz_category_masteries" }

// UserBadge represents a behavior-based badge unlocked by the user.
type UserBadge struct {
	ID         int64     `gorm:"primaryKey;autoIncrement" json:"id"`
	UserID     uuid.UUID `gorm:"type:uuid;not null;uniqueIndex:idx_user_badge" json:"user_id"`
	BadgeKey   string    `gorm:"type:varchar(100);not null;uniqueIndex:idx_user_badge" json:"badge_key"`
	Source     string    `gorm:"type:varchar(100);not null" json:"source"`
	UnlockedAt time.Time `json:"unlocked_at"`
	Metadata   []byte    `gorm:"type:jsonb" json:"metadata,omitempty"`
}

func (UserBadge) TableName() string { return "user_badges" }
