package services

import (
	"context"
	"fmt"
	"strings"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"go.uber.org/zap"
)

// AppReviewService contains business logic for client ratings and admin handling.
type AppReviewService struct {
	repo   *repositories.AppReviewRepository
	logger *zap.Logger
}

func NewAppReviewService(repo *repositories.AppReviewRepository, logger *zap.Logger) *AppReviewService {
	return &AppReviewService{repo: repo, logger: logger}
}

func (s *AppReviewService) Submit(
	ctx context.Context,
	req *dto.SubmitAppReviewRequest,
	userID *uuid.UUID,
	platform string,
	appVersion string,
	deviceID string,
	deviceName string,
	osVersion string,
) (*dto.AppReviewResponse, error) {
	platform = models.NormalizePlatform(platform)
	if platform == "" {
		return nil, fmt.Errorf("nền tảng thiết bị không hợp lệ")
	}

	reviewFlow := models.NormalizeAppReviewFlow(req.ReviewFlow)
	if reviewFlow == "" {
		return nil, fmt.Errorf("luồng đánh giá không hợp lệ")
	}

	review := &models.AppReview{
		UserID:       userID,
		Platform:     platform,
		AppVersion:   strings.TrimSpace(appVersion),
		DeviceID:     strings.TrimSpace(deviceID),
		DeviceName:   strings.TrimSpace(deviceName),
		OSVersion:    strings.TrimSpace(osVersion),
		Stars:        req.Stars,
		ReviewText:   strings.TrimSpace(req.ReviewText),
		ReviewFlow:   reviewFlow,
		ReviewSource: strings.TrimSpace(req.ReviewSource),
		Status:       models.AppReviewStatusNew,
	}

	if err := s.repo.Create(ctx, review); err != nil {
		s.logger.Error("Failed to submit app review", zap.Error(err))
		return nil, fmt.Errorf("không thể lưu đánh giá ứng dụng")
	}

	saved, err := s.repo.GetByID(ctx, review.ID)
	if err != nil {
		return nil, fmt.Errorf("không thể tải lại đánh giá vừa lưu")
	}
	return toAppReviewResponse(saved), nil
}

func (s *AppReviewService) List(ctx context.Context, params dto.AppReviewListParams) ([]dto.AppReviewResponse, int64, error) {
	reviews, total, err := s.repo.List(ctx, params)
	if err != nil {
		return nil, 0, fmt.Errorf("không thể tải danh sách đánh giá")
	}

	result := make([]dto.AppReviewResponse, len(reviews))
	for i := range reviews {
		result[i] = *toAppReviewResponse(&reviews[i])
	}
	return result, total, nil
}

func (s *AppReviewService) GetByID(ctx context.Context, id uuid.UUID) (*dto.AppReviewResponse, error) {
	review, err := s.repo.GetByID(ctx, id)
	if err != nil {
		return nil, fmt.Errorf("không tìm thấy đánh giá")
	}
	return toAppReviewResponse(review), nil
}

func (s *AppReviewService) Update(ctx context.Context, id uuid.UUID, req *dto.UpdateAppReviewRequest) (*dto.AppReviewResponse, error) {
	review, err := s.repo.GetByID(ctx, id)
	if err != nil {
		return nil, fmt.Errorf("không tìm thấy đánh giá")
	}

	if req.Status != nil {
		status := models.NormalizeAppReviewStatus(*req.Status)
		if status == "" {
			return nil, fmt.Errorf("trạng thái đánh giá không hợp lệ")
		}
		review.Status = status
	}
	if req.AdminNote != nil {
		review.AdminNote = strings.TrimSpace(*req.AdminNote)
	}

	if err := s.repo.Update(ctx, review); err != nil {
		s.logger.Error("Failed to update app review", zap.Error(err), zap.String("review_id", id.String()))
		return nil, fmt.Errorf("không thể cập nhật đánh giá")
	}

	updated, err := s.repo.GetByID(ctx, id)
	if err != nil {
		return nil, fmt.Errorf("không thể tải đánh giá sau khi cập nhật")
	}
	return toAppReviewResponse(updated), nil
}

func toAppReviewResponse(review *models.AppReview) *dto.AppReviewResponse {
	var userID *string
	if review.UserID != nil {
		value := review.UserID.String()
		userID = &value
	}

	var user *dto.AppReviewUserDTO
	if review.User != nil {
		user = &dto.AppReviewUserDTO{
			ID:       review.User.ID.String(),
			Email:    review.User.Email,
			FullName: strings.TrimSpace(review.User.FullName()),
			Provider: review.User.Provider,
		}
	}

	return &dto.AppReviewResponse{
		ID:           review.ID.String(),
		UserID:       userID,
		Platform:     review.Platform,
		AppVersion:   review.AppVersion,
		DeviceID:     review.DeviceID,
		DeviceName:   review.DeviceName,
		OSVersion:    review.OSVersion,
		Stars:        review.Stars,
		ReviewText:   review.ReviewText,
		ReviewFlow:   review.ReviewFlow,
		ReviewSource: review.ReviewSource,
		Status:       review.Status,
		AdminNote:    review.AdminNote,
		CreatedAt:    review.CreatedAt,
		UpdatedAt:    review.UpdatedAt,
		User:         user,
	}
}
