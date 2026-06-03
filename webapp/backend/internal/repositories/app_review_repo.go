package repositories

import (
	"context"
	"fmt"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// AppReviewRepository handles persistence for mobile app reviews.
type AppReviewRepository struct {
	db *gorm.DB
}

func NewAppReviewRepository(db *gorm.DB) *AppReviewRepository {
	return &AppReviewRepository{db: db}
}

func (r *AppReviewRepository) Create(ctx context.Context, review *models.AppReview) error {
	if err := r.db.WithContext(ctx).Create(review).Error; err != nil {
		return fmt.Errorf("AppReviewRepository.Create: %w", err)
	}
	return nil
}

func (r *AppReviewRepository) GetByID(ctx context.Context, id uuid.UUID) (*models.AppReview, error) {
	var review models.AppReview
	if err := r.db.WithContext(ctx).
		Preload("User").
		Where("id = ?", id).
		First(&review).Error; err != nil {
		return nil, fmt.Errorf("AppReviewRepository.GetByID: %w", err)
	}
	return &review, nil
}

func (r *AppReviewRepository) List(ctx context.Context, params dto.AppReviewListParams) ([]models.AppReview, int64, error) {
	var (
		reviews []models.AppReview
		total   int64
	)

	query := r.db.WithContext(ctx).Model(&models.AppReview{}).Preload("User")

	if params.Search != "" {
		pattern := "%" + params.Search + "%"
		query = query.Joins("LEFT JOIN users ON users.id = app_reviews.user_id").
			Where(
				`app_reviews.review_text ILIKE ?
				OR app_reviews.device_name ILIKE ?
				OR app_reviews.app_version ILIKE ?
				OR users.email ILIKE ?
				OR CONCAT(COALESCE(users.first_name, ''), ' ', COALESCE(users.last_name, '')) ILIKE ?`,
				pattern, pattern, pattern, pattern, pattern,
			)
	}
	if params.Status != "" {
		query = query.Where("app_reviews.status = ?", params.Status)
	}
	if params.Platform != "" {
		query = query.Where("app_reviews.platform = ?", params.Platform)
	}
	if params.Stars > 0 {
		query = query.Where("app_reviews.stars = ?", params.Stars)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, fmt.Errorf("AppReviewRepository.List count: %w", err)
	}

	sortBy := "app_reviews.created_at"
	switch params.SortBy {
	case "updated_at":
		sortBy = "app_reviews.updated_at"
	case "stars":
		sortBy = "app_reviews.stars"
	case "status":
		sortBy = "app_reviews.status"
	}

	sortOrder := "DESC"
	if params.SortOrder == "asc" {
		sortOrder = "ASC"
	}

	offset := (params.Page - 1) * params.Limit
	if err := query.
		Order(sortBy + " " + sortOrder).
		Offset(offset).
		Limit(params.Limit).
		Find(&reviews).Error; err != nil {
		return nil, 0, fmt.Errorf("AppReviewRepository.List: %w", err)
	}

	return reviews, total, nil
}

func (r *AppReviewRepository) Update(ctx context.Context, review *models.AppReview) error {
	if err := r.db.WithContext(ctx).Save(review).Error; err != nil {
		return fmt.Errorf("AppReviewRepository.Update: %w", err)
	}
	return nil
}
