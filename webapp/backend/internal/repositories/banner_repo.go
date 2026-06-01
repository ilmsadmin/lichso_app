package repositories

import (
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// BannerRepository handles banner database operations.
type BannerRepository struct {
	db *gorm.DB
}

// NewBannerRepository creates a new BannerRepository.
func NewBannerRepository(db *gorm.DB) *BannerRepository {
	return &BannerRepository{db: db}
}

// Create creates a new banner.
func (r *BannerRepository) Create(banner *models.Banner) error {
	return r.db.Create(banner).Error
}

// GetByID returns a banner by ID.
func (r *BannerRepository) GetByID(id uuid.UUID) (*models.Banner, error) {
	var banner models.Banner
	err := r.db.Where("id = ?", id).First(&banner).Error
	return &banner, err
}

// ListActive returns active banners within valid date range, ordered by sort_order.
// When platform is "android" or "ios", only banners targeting that platform (or "all")
// are returned. An empty platform returns only platform-agnostic ("all") banners.
func (r *BannerRepository) ListActive(platform string) ([]models.Banner, error) {
	var banners []models.Banner
	now := time.Now()
	query := r.db.Where("is_active = ?", true).
		Where("(start_date IS NULL OR start_date <= ?)", now).
		Where("(end_date IS NULL OR end_date >= ?)", now)
	if platform == models.PlatformAndroid || platform == models.PlatformIOS {
		query = query.Where("platform = ? OR platform = ?", models.PlatformAll, platform)
	} else {
		query = query.Where("platform = ?", models.PlatformAll)
	}
	err := query.Order("sort_order ASC, created_at DESC").Find(&banners).Error
	return banners, err
}

// ListAll returns all banners (for admin) with pagination and search.
func (r *BannerRepository) ListAll(page, pageSize int, search string) ([]models.Banner, int64, error) {
	var banners []models.Banner
	var total int64

	query := r.db.Model(&models.Banner{})
	if search != "" {
		searchPattern := "%" + search + "%"
		query = query.Where("(title ILIKE ? OR subtitle ILIKE ? OR type ILIKE ?)", searchPattern, searchPattern, searchPattern)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Order("sort_order ASC, created_at DESC").
		Offset(offset).Limit(pageSize).Find(&banners).Error

	return banners, total, err
}

// Update updates a banner.
func (r *BannerRepository) Update(banner *models.Banner) error {
	return r.db.Save(banner).Error
}

// Delete soft-deletes a banner.
func (r *BannerRepository) Delete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.Banner{}).Error
}

// UpdateSortOrders updates the sort_order of multiple banners in a single transaction.
func (r *BannerRepository) UpdateSortOrders(orders map[uuid.UUID]int) error {
	return r.db.Transaction(func(tx *gorm.DB) error {
		for id, sortOrder := range orders {
			if err := tx.Model(&models.Banner{}).Where("id = ?", id).Update("sort_order", sortOrder).Error; err != nil {
				return err
			}
		}
		return nil
	})
}
