package services

import (
	"encoding/json"
	"errors"
	"fmt"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"go.uber.org/zap"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

const (
	maxGuestDailyEarnPoints = 50
	maxUserDailyEarnPoints  = 150
)

// GuestTransaction represents a transaction tracked locally by the guest client.
type GuestTransaction struct {
	Amount         int                    `json:"amount"`
	Direction      string                 `json:"direction"`
	Source         string                 `json:"source"`
	SourceID       *string                `json:"source_id,omitempty"`
	IdempotencyKey string                 `json:"idempotency_key"`
	Metadata       map[string]interface{} `json:"metadata,omitempty"`
	CreatedAt      time.Time              `json:"created_at"`
}

// PointsService manages point wallets and transactions.
type PointsService struct {
	db     *gorm.DB
	logger *zap.Logger
}

// NewPointsService creates a new PointsService.
func NewPointsService(db *gorm.DB, logger *zap.Logger) *PointsService {
	return &PointsService{db: db, logger: logger}
}

// GetWallet returns the points wallet for the user, creating it if it doesn't exist.
func (s *PointsService) GetWallet(userID uuid.UUID) (*models.PointWallet, error) {
	var wallet models.PointWallet
	err := s.db.Where("user_id = ?", userID).First(&wallet).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		wallet = models.PointWallet{
			UserID:    userID,
			Balance:   0,
			UpdatedAt: time.Now(),
		}
		if err := s.db.Create(&wallet).Error; err != nil {
			return nil, fmt.Errorf("failed to create wallet: %w", err)
		}
		return &wallet, nil
	}
	if err != nil {
		return nil, fmt.Errorf("failed to get wallet: %w", err)
	}
	return &wallet, nil
}

// CreditPoints adds points to the user's wallet with ledger logging and daily cap checks.
func (s *PointsService) CreditPoints(userID uuid.UUID, amount int, source string, sourceID *string, idempotencyKey *string, metadata map[string]interface{}) (*models.PointWallet, error) {
	if amount <= 0 {
		return nil, errors.New("credit amount must be positive")
	}

	var wallet *models.PointWallet
	err := s.db.Transaction(func(tx *gorm.DB) error {
		// 1. Check idempotency
		if idempotencyKey != nil && *idempotencyKey != "" {
			var exists models.PointTransaction
			err := tx.Where("idempotency_key = ?", *idempotencyKey).First(&exists).Error
			if err == nil {
				// Already processed, fetch and return current wallet
				var w models.PointWallet
				if err := tx.Where("user_id = ?", userID).First(&w).Error; err != nil {
					return err
				}
				wallet = &w
				return nil
			}
			if !errors.Is(err, gorm.ErrRecordNotFound) {
				return err
			}
		}

		// 2. Check daily earning limit
		todayStart := time.Now().Truncate(24 * time.Hour)
		var todayEarned int64
		err := tx.Model(&models.PointTransaction{}).
			Where("user_id = ? AND direction = 'earn' AND created_at >= ?", userID, todayStart).
			Select("COALESCE(SUM(amount), 0)").
			Row().Scan(&todayEarned)
		if err != nil {
			return err
		}

		if todayEarned >= int64(maxUserDailyEarnPoints) {
			s.logger.Warn("User has reached daily earning limit", zap.String("user_id", userID.String()))
			// Fetch current wallet to return
			var w models.PointWallet
			if err := tx.Where("user_id = ?", userID).First(&w).Error; err != nil {
				return err
			}
			wallet = &w
			return nil
		}

		// Cap the amount if it would exceed the limit
		allowedAmount := amount
		if todayEarned+int64(amount) > int64(maxUserDailyEarnPoints) {
			allowedAmount = int(int64(maxUserDailyEarnPoints) - todayEarned)
		}

		if allowedAmount <= 0 {
			var w models.PointWallet
			if err := tx.Where("user_id = ?", userID).First(&w).Error; err != nil {
				return err
			}
			wallet = &w
			return nil
		}

		// 3. Update wallet or create
		var w models.PointWallet
		err = tx.Clauses(clause.Locking{Strength: "UPDATE"}).Where("user_id = ?", userID).First(&w).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			w = models.PointWallet{
				UserID:         userID,
				Balance:        allowedAmount,
				LifetimeEarned: allowedAmount,
				UpdatedAt:      time.Now(),
			}
			if err := tx.Create(&w).Error; err != nil {
				return err
			}
		} else if err != nil {
			return err
		} else {
			w.Balance += allowedAmount
			w.LifetimeEarned += allowedAmount
			w.UpdatedAt = time.Now()
			if err := tx.Save(&w).Error; err != nil {
				return err
			}
		}

		// 4. Log transaction
		metaBytes, _ := json.Marshal(metadata)
		txLog := models.PointTransaction{
			UserID:         userID,
			Amount:         allowedAmount,
			Direction:      "earn",
			Source:         source,
			SourceID:       sourceID,
			IdempotencyKey: idempotencyKey,
			Metadata:       metaBytes,
			CreatedAt:      time.Now(),
		}
		if err := tx.Create(&txLog).Error; err != nil {
			return err
		}

		wallet = &w
		return nil
	})

	if err != nil {
		return nil, err
	}
	return wallet, nil
}

// SpendPoints deducts points from the user's wallet.
func (s *PointsService) SpendPoints(userID uuid.UUID, amount int, source string, sourceID *string, idempotencyKey *string, metadata map[string]interface{}) (*models.PointWallet, error) {
	if amount <= 0 {
		return nil, errors.New("spend amount must be positive")
	}

	var wallet *models.PointWallet
	err := s.db.Transaction(func(tx *gorm.DB) error {
		// 1. Check idempotency
		if idempotencyKey != nil && *idempotencyKey != "" {
			var exists models.PointTransaction
			err := tx.Where("idempotency_key = ?", *idempotencyKey).First(&exists).Error
			if err == nil {
				// Already processed
				var w models.PointWallet
				if err := tx.Where("user_id = ?", userID).First(&w).Error; err != nil {
					return err
				}
				wallet = &w
				return nil
			}
			if !errors.Is(err, gorm.ErrRecordNotFound) {
				return err
			}
		}

		// 2. Fetch and lock wallet
		var w models.PointWallet
		if wErr := tx.Clauses(clause.Locking{Strength: "UPDATE"}).Where("user_id = ?", userID).First(&w).Error; wErr != nil {
			if errors.Is(wErr, gorm.ErrRecordNotFound) {
				return errors.New("points wallet not found; insufficient points")
			}
			return wErr
		}

		if w.Balance < amount {
			return errors.New("insufficient points balance")
		}

		// 3. Deduct
		w.Balance -= amount
		w.LifetimeSpent += amount
		w.UpdatedAt = time.Now()
		if err := tx.Save(&w).Error; err != nil {
			return err
		}

		// If it's a quiz assist, parse metadata and log in quiz_assist_usages
		if strings.HasPrefix(source, "quiz_assist_") {
			var sessID uuid.UUID
			var qID int64
			if mSessID, ok := metadata["session_id"]; ok {
				if sStr, ok := mSessID.(string); ok {
					sessID, _ = uuid.Parse(sStr)
				}
			}
			if mQID, ok := metadata["question_id"]; ok {
				switch val := mQID.(type) {
				case float64:
					qID = int64(val)
				case int64:
					qID = val
				case int:
					qID = int64(val)
				}
			}
			if sessID != uuid.Nil && qID != 0 {
				assistType := strings.TrimPrefix(source, "quiz_assist_")
				assistLog := models.QuizAssistUsage{
					SessionID:     sessID,
					UserID:        userID,
					QuestionID:    qID,
					AssistType:    assistType,
					CostAppPoints: amount,
					CreatedAt:     time.Now(),
				}
				if err := tx.Clauses(clause.OnConflict{DoNothing: true}).Create(&assistLog).Error; err != nil {
					return err
				}
			}
		}

		// 4. Log transaction
		metaBytes, _ := json.Marshal(metadata)
		txLog := models.PointTransaction{
			UserID:         userID,
			Amount:         amount,
			Direction:      "spend",
			Source:         source,
			SourceID:       sourceID,
			IdempotencyKey: idempotencyKey,
			Metadata:       metaBytes,
			CreatedAt:      time.Now(),
		}
		if err := tx.Create(&txLog).Error; err != nil {
			return err
		}

		wallet = &w
		return nil
	})

	if err != nil {
		return nil, err
	}
	return wallet, nil
}

// SyncGuestPoints merges offline guest transactions when they log in.
func (s *PointsService) SyncGuestPoints(userID uuid.UUID, guestTxs []GuestTransaction) (int, error) {
	syncedCount := 0

	err := s.db.Transaction(func(tx *gorm.DB) error {
		// Initialize or load wallet
		var wallet models.PointWallet
		err := tx.Clauses(clause.Locking{Strength: "UPDATE"}).Where("user_id = ?", userID).First(&wallet).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			wallet = models.PointWallet{
				UserID:    userID,
				Balance:   0,
				UpdatedAt: time.Now(),
			}
			if err := tx.Create(&wallet).Error; err != nil {
				return err
			}
		} else if err != nil {
			return err
		}

		for _, gTx := range guestTxs {
			if gTx.IdempotencyKey == "" {
				continue
			}

			// Check duplicate
			var exists models.PointTransaction
			err = tx.Where("idempotency_key = ?", gTx.IdempotencyKey).First(&exists).Error
			if err == nil {
				continue // Already imported
			}
			if !errors.Is(err, gorm.ErrRecordNotFound) {
				return err
			}

			if gTx.Direction == "earn" {
				// Check guest limits for the transaction date
				txDateStart := gTx.CreatedAt.Truncate(24 * time.Hour)
				var dateEarned int64
				err = tx.Model(&models.PointTransaction{}).
					Where("user_id = ? AND direction = 'earn' AND created_at >= ? AND created_at < ?", userID, txDateStart, txDateStart.Add(24*time.Hour)).
					Select("COALESCE(SUM(amount), 0)").
					Row().Scan(&dateEarned)
				if err != nil {
					return err
				}

				if dateEarned >= int64(maxGuestDailyEarnPoints) {
					continue // Earning limit reached for that day
				}

				allowed := gTx.Amount
				if dateEarned+int64(gTx.Amount) > int64(maxGuestDailyEarnPoints) {
					allowed = int(int64(maxGuestDailyEarnPoints) - dateEarned)
				}
				if allowed <= 0 {
					continue
				}

				// Apply to wallet
				wallet.Balance += allowed
				wallet.LifetimeEarned += allowed
				wallet.UpdatedAt = time.Now()

				metaBytes, _ := json.Marshal(gTx.Metadata)
				txLog := models.PointTransaction{
					UserID:         userID,
					Amount:         allowed,
					Direction:      "earn",
					Source:         gTx.Source,
					SourceID:       gTx.SourceID,
					IdempotencyKey: &gTx.IdempotencyKey,
					Metadata:       metaBytes,
					CreatedAt:      gTx.CreatedAt,
				}
				if err := tx.Create(&txLog).Error; err != nil {
					return err
				}
				syncedCount++

			} else if gTx.Direction == "spend" {
				// For spends, we always sync them but keep balance from going negative
				actualSpend := gTx.Amount
				if wallet.Balance < actualSpend {
					// Clamp or allow if necessary? Usually, we deduct up to balance.
					actualSpend = wallet.Balance
				}

				if actualSpend > 0 {
					wallet.Balance -= actualSpend
					wallet.LifetimeSpent += actualSpend
					wallet.UpdatedAt = time.Now()
				}

				metaBytes, _ := json.Marshal(gTx.Metadata)
				txLog := models.PointTransaction{
					UserID:         userID,
					Amount:         gTx.Amount, // record original spend amount for history
					Direction:      "spend",
					Source:         gTx.Source,
					SourceID:       gTx.SourceID,
					IdempotencyKey: &gTx.IdempotencyKey,
					Metadata:       metaBytes,
					CreatedAt:      gTx.CreatedAt,
				}
				if err := tx.Create(&txLog).Error; err != nil {
					return err
				}
				syncedCount++
			}
		}

		// Save wallet state
		if err := tx.Save(&wallet).Error; err != nil {
			return err
		}

		return nil
	})

	if err != nil {
		return 0, err
	}

	return syncedCount, nil
}
