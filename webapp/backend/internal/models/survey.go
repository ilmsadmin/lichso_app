package models

import (
	"database/sql/driver"
	"encoding/json"
	"errors"
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// SurveyQuestion represents a single question in a survey.
type SurveyQuestion struct {
	Title    string   `json:"title"`
	Type     string   `json:"type"` // "single_choice", "multiple_choice", "text"
	Options  []string `json:"options,omitempty"`
	Required bool     `json:"required"`
}

type SurveyQuestions []SurveyQuestion

// Value implements driver.Valuer interface
func (sq SurveyQuestions) Value() (driver.Value, error) {
	return json.Marshal(sq)
}

// Scan implements sql.Scanner interface
func (sq *SurveyQuestions) Scan(value interface{}) error {
	bytes, ok := value.([]byte)
	if !ok {
		return errors.New("type assertion to []byte failed")
	}
	return json.Unmarshal(bytes, sq)
}

// Survey represents a questionnaire created by admin to collect user feedback.
type Survey struct {
	ID          uuid.UUID       `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Title       string          `gorm:"type:varchar(255);not null" json:"title"`
	Description string          `gorm:"type:text" json:"description,omitempty"`
	Questions   SurveyQuestions `gorm:"type:jsonb;not null" json:"questions"`
	IsActive    bool            `gorm:"default:true" json:"is_active"`
	CreatedBy   *uuid.UUID      `gorm:"type:uuid" json:"created_by,omitempty"`
	CreatedAt   time.Time       `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
	UpdatedAt   time.Time       `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
	DeletedAt   gorm.DeletedAt  `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`
}

func (Survey) TableName() string {
	return "surveys"
}

// UserAnswer represents an answer submitted by a user.
type UserAnswer struct {
	QuestionIndex   int      `json:"question_index"`
	SelectedOptions []string `json:"selected_options,omitempty"`
	TextAnswer      string   `json:"text_answer,omitempty"`
}

type UserAnswers []UserAnswer

// Value implements driver.Valuer interface
func (ua UserAnswers) Value() (driver.Value, error) {
	return json.Marshal(ua)
}

// Scan implements sql.Scanner interface
func (ua *UserAnswers) Scan(value interface{}) error {
	bytes, ok := value.([]byte)
	if !ok {
		return errors.New("type assertion to []byte failed")
	}
	return json.Unmarshal(bytes, ua)
}

// SurveyResponse stores answers from a user for a specific survey.
type SurveyResponse struct {
	ID        uuid.UUID   `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	SurveyID  uuid.UUID   `gorm:"type:uuid;not null;index" json:"survey_id"`
	UserID    *uuid.UUID  `gorm:"type:uuid" json:"user_id,omitempty"`
	DeviceID  string      `gorm:"type:varchar(255)" json:"device_id,omitempty"`
	Answers   UserAnswers `gorm:"type:jsonb;not null" json:"answers"`
	CreatedAt time.Time   `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`

	// Relationships
	Survey *Survey `gorm:"foreignKey:SurveyID" json:"survey,omitempty"`
}

func (SurveyResponse) TableName() string {
	return "survey_responses"
}
