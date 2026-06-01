package repositories

import (
	"errors"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// SurveyRepository handles survey database operations.
type SurveyRepository struct {
	db *gorm.DB
}

// NewSurveyRepository creates a new SurveyRepository.
func NewSurveyRepository(db *gorm.DB) *SurveyRepository {
	return &SurveyRepository{db: db}
}

// Create creates a new survey.
func (r *SurveyRepository) Create(survey *models.Survey) error {
	return r.db.Create(survey).Error
}

// GetByID returns a survey by ID.
func (r *SurveyRepository) GetByID(id uuid.UUID) (*models.Survey, error) {
	var survey models.Survey
	err := r.db.Where("id = ?", id).First(&survey).Error
	return &survey, err
}

// GetLatestActive returns the latest active survey.
func (r *SurveyRepository) GetLatestActive() (*models.Survey, error) {
	var survey models.Survey
	err := r.db.Where("is_active = ?", true).
		Order("created_at DESC").
		First(&survey).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, nil
	}
	return &survey, err
}

// ListAll returns all surveys with pagination and search.
func (r *SurveyRepository) ListAll(page, pageSize int, search string) ([]models.Survey, int64, error) {
	var surveys []models.Survey
	var total int64

	query := r.db.Model(&models.Survey{})
	if search != "" {
		searchPattern := "%" + search + "%"
		query = query.Where("(title ILIKE ? OR description ILIKE ?)", searchPattern, searchPattern)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Order("created_at DESC").
		Offset(offset).Limit(pageSize).Find(&surveys).Error

	return surveys, total, err
}

// Update updates a survey.
func (r *SurveyRepository) Update(survey *models.Survey) error {
	return r.db.Save(survey).Error
}

// Delete soft-deletes a survey.
func (r *SurveyRepository) Delete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.Survey{}).Error
}

// CreateResponse saves a user's survey response.
func (r *SurveyRepository) CreateResponse(resp *models.SurveyResponse) error {
	return r.db.Create(resp).Error
}

// HasSubmitted checks if a user or device has already submitted a response for a survey.
func (r *SurveyRepository) HasSubmitted(surveyID uuid.UUID, userID *uuid.UUID, deviceID string) (bool, error) {
	var count int64
	query := r.db.Model(&models.SurveyResponse{}).Where("survey_id = ?", surveyID)

	if userID != nil && deviceID != "" {
		query = query.Where("(user_id = ? OR device_id = ?)", userID, deviceID)
	} else if userID != nil {
		query = query.Where("user_id = ?", userID)
	} else if deviceID != "" {
		query = query.Where("device_id = ?", deviceID)
	} else {
		return false, nil
	}

	err := query.Count(&count).Error
	return count > 0, err
}

// ListResponsesBySurveyID returns all responses for a survey.
func (r *SurveyRepository) ListResponsesBySurveyID(surveyID uuid.UUID) ([]models.SurveyResponse, error) {
	var responses []models.SurveyResponse
	err := r.db.Where("survey_id = ?", surveyID).Order("created_at DESC").Find(&responses).Error
	return responses, err
}
