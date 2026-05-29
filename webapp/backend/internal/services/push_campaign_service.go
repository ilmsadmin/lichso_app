package services

import (
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"go.uber.org/zap"
)

const fcmBatchSize = 100 // tokens per batch call

// PushCampaignService manages push notification campaigns.
type PushCampaignService struct {
	campaignRepo    *repositories.PushCampaignRepository
	deviceTokenRepo *repositories.DeviceTokenRepository
	fcm             *FCMService // nil when FCM is disabled
	logger          *zap.Logger
}

func NewPushCampaignService(
	campaignRepo *repositories.PushCampaignRepository,
	deviceTokenRepo *repositories.DeviceTokenRepository,
	fcm *FCMService,
	logger *zap.Logger,
) *PushCampaignService {
	return &PushCampaignService{
		campaignRepo:    campaignRepo,
		deviceTokenRepo: deviceTokenRepo,
		fcm:             fcm,
		logger:          logger,
	}
}

// ── Campaign CRUD ─────────────────────────────────────────────────────────

type CreateCampaignInput struct {
	Title       string
	Body        string
	ImageURL    string
	ClickAction string
	DataPayload map[string]string
	TargetType  string
	TargetUsers []string // user IDs when target_type = "users"
	ScheduledAt *time.Time
	CreatedBy   *uuid.UUID
}

func (s *PushCampaignService) Create(input CreateCampaignInput) (*models.PushCampaign, error) {
	dataJSON := "{}"
	if len(input.DataPayload) > 0 {
		b, _ := json.Marshal(input.DataPayload)
		dataJSON = string(b)
	}

	targetType := input.TargetType
	if targetType == "" {
		targetType = models.CampaignTargetAll
	}

	status := models.CampaignStatusDraft
	if input.ScheduledAt != nil {
		status = models.CampaignStatusScheduled
	}

	campaign := &models.PushCampaign{
		Title:       input.Title,
		Body:        input.Body,
		ImageURL:    input.ImageURL,
		ClickAction: input.ClickAction,
		DataPayload: dataJSON,
		TargetType:  targetType,
		TargetUsers: strings.Join(input.TargetUsers, ","),
		Status:      status,
		ScheduledAt: input.ScheduledAt,
		CreatedBy:   input.CreatedBy,
	}

	if err := s.campaignRepo.Create(campaign); err != nil {
		return nil, err
	}
	return campaign, nil
}

func (s *PushCampaignService) GetByID(id uuid.UUID) (*models.PushCampaign, error) {
	return s.campaignRepo.GetByID(id)
}

func (s *PushCampaignService) List(page, limit int, status string) ([]models.PushCampaign, int64, error) {
	if page < 1 {
		page = 1
	}
	if limit < 1 || limit > 100 {
		limit = 20
	}
	return s.campaignRepo.List(page, limit, status)
}

type UpdateCampaignInput struct {
	Title       string
	Body        string
	ImageURL    string
	ClickAction string
	DataPayload map[string]string
	TargetType  string
	TargetUsers []string
	ScheduledAt *time.Time
}

func (s *PushCampaignService) Update(id uuid.UUID, input UpdateCampaignInput) (*models.PushCampaign, error) {
	campaign, err := s.campaignRepo.GetByID(id)
	if err != nil {
		return nil, err
	}
	if campaign.Status == models.CampaignStatusSent || campaign.Status == models.CampaignStatusSending {
		return nil, fmt.Errorf("cannot edit a campaign that has already been sent")
	}

	if input.Title != "" {
		campaign.Title = input.Title
	}
	if input.Body != "" {
		campaign.Body = input.Body
	}
	campaign.ImageURL = input.ImageURL
	campaign.ClickAction = input.ClickAction

	if len(input.DataPayload) > 0 {
		b, _ := json.Marshal(input.DataPayload)
		campaign.DataPayload = string(b)
	}
	if input.TargetType != "" {
		campaign.TargetType = input.TargetType
	}
	campaign.TargetUsers = strings.Join(input.TargetUsers, ",")
	campaign.ScheduledAt = input.ScheduledAt

	if input.ScheduledAt != nil {
		campaign.Status = models.CampaignStatusScheduled
	} else if campaign.Status == models.CampaignStatusScheduled {
		campaign.Status = models.CampaignStatusDraft
	}

	if err := s.campaignRepo.Update(campaign); err != nil {
		return nil, err
	}
	return campaign, nil
}

func (s *PushCampaignService) Delete(id uuid.UUID) error {
	campaign, err := s.campaignRepo.GetByID(id)
	if err != nil {
		return err
	}
	if campaign.Status == models.CampaignStatusSending {
		return fmt.Errorf("cannot delete a campaign that is currently sending")
	}
	return s.campaignRepo.Delete(id)
}

// ── Send ──────────────────────────────────────────────────────────────────

// Send dispatches a campaign immediately in a background goroutine.
// Returns quickly — actual delivery happens asynchronously.
func (s *PushCampaignService) Send(id uuid.UUID) error {
	campaign, err := s.campaignRepo.GetByID(id)
	if err != nil {
		return err
	}
	if campaign.Status == models.CampaignStatusSent || campaign.Status == models.CampaignStatusSending {
		return fmt.Errorf("campaign already sent or currently sending")
	}

	// Mark as sending immediately
	if err := s.campaignRepo.UpdateStatus(id, models.CampaignStatusSending, 0, 0, nil); err != nil {
		return err
	}

	go s.dispatchCampaign(campaign)
	return nil
}

func (s *PushCampaignService) dispatchCampaign(campaign *models.PushCampaign) {
	logger := s.logger.With(zap.String("campaign_id", campaign.ID.String()))
	logger.Info("Dispatching push campaign", zap.String("title", campaign.Title))

	if s.fcm == nil {
		logger.Warn("FCM not configured — campaign dispatch skipped")
		_ = s.campaignRepo.UpdateStatus(campaign.ID, models.CampaignStatusFailed, 0, 0, nil)
		return
	}

	tokens, err := s.resolveTokens(campaign)
	if err != nil {
		logger.Error("Failed to resolve tokens", zap.Error(err))
		_ = s.campaignRepo.UpdateStatus(campaign.ID, models.CampaignStatusFailed, 0, 0, nil)
		return
	}
	if len(tokens) == 0 {
		logger.Warn("No active device tokens found for campaign")
		now := time.Now()
		_ = s.campaignRepo.UpdateStatus(campaign.ID, models.CampaignStatusSent, 0, 0, &now)
		return
	}

	var extraData map[string]string
	if campaign.DataPayload != "{}" && campaign.DataPayload != "" {
		_ = json.Unmarshal([]byte(campaign.DataPayload), &extraData)
	}

	var totalSent, totalFailed int

	// Send in batches
	for i := 0; i < len(tokens); i += fcmBatchSize {
		end := i + fcmBatchSize
		if end > len(tokens) {
			end = len(tokens)
		}
		batch := tokens[i:end]

		results, err := s.fcm.SendToTokens(batch, campaign.Title, campaign.Body, campaign.ImageURL, campaign.ClickAction, extraData)
		if err != nil {
			logger.Error("FCM batch send error", zap.Int("batch_start", i), zap.Error(err))
			totalFailed += len(batch)
			continue
		}
		for _, r := range results {
			if r.Success {
				totalSent++
			} else {
				totalFailed++
			}
		}
	}

	now := time.Now()
	finalStatus := models.CampaignStatusSent
	if totalSent == 0 && totalFailed > 0 {
		finalStatus = models.CampaignStatusFailed
	}

	_ = s.campaignRepo.UpdateStatus(campaign.ID, finalStatus, totalSent, totalFailed, &now)
	logger.Info("Campaign dispatched",
		zap.Int("sent", totalSent),
		zap.Int("failed", totalFailed),
		zap.String("status", finalStatus),
	)
}

func (s *PushCampaignService) resolveTokens(campaign *models.PushCampaign) ([]string, error) {
	switch campaign.TargetType {
	case models.CampaignTargetAll:
		// Collect all active tokens in pages to avoid loading millions into memory
		var allTokens []string
		offset := 0
		for {
			batch, err := s.deviceTokenRepo.GetActiveTokens("android", 1000, offset)
			if err != nil {
				return nil, err
			}
			allTokens = append(allTokens, batch...)
			if len(batch) < 1000 {
				break
			}
			offset += 1000
		}
		return allTokens, nil

	case models.CampaignTargetUsers:
		if campaign.TargetUsers == "" {
			return nil, nil
		}
		userIDStrs := strings.Split(campaign.TargetUsers, ",")
		userIDs := make([]uuid.UUID, 0, len(userIDStrs))
		for _, s := range userIDStrs {
			id, err := uuid.Parse(strings.TrimSpace(s))
			if err == nil {
				userIDs = append(userIDs, id)
			}
		}
		return s.deviceTokenRepo.GetActiveTokensByUserIDs(userIDs)

	default:
		return nil, fmt.Errorf("unknown target_type: %s", campaign.TargetType)
	}
}

// ActiveDeviceCount returns the number of registered active devices.
func (s *PushCampaignService) ActiveDeviceCount() (int64, error) {
	return s.deviceTokenRepo.CountActive()
}
