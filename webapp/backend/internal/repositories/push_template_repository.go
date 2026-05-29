package repositories

import (
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

type PushTemplateRepository struct{ db *gorm.DB }

func NewPushTemplateRepository(db *gorm.DB) *PushTemplateRepository {
	return &PushTemplateRepository{db: db}
}

func (r *PushTemplateRepository) Create(t *models.PushTemplate) error { return r.db.Create(t).Error }

func (r *PushTemplateRepository) GetByID(id uuid.UUID) (*models.PushTemplate, error) {
	var t models.PushTemplate
	err := r.db.Where("id = ? AND deleted_at IS NULL", id).First(&t).Error
	if err != nil {
		return nil, err
	}
	return &t, nil
}

func (r *PushTemplateRepository) List() ([]models.PushTemplate, error) {
	var list []models.PushTemplate
	err := r.db.Where("deleted_at IS NULL").Order("created_at DESC").Find(&list).Error
	return list, err
}

func (r *PushTemplateRepository) Update(t *models.PushTemplate) error { return r.db.Save(t).Error }

func (r *PushTemplateRepository) Delete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.PushTemplate{}).Error
}

// ── User Group ──────────────────────────────────────────────────────────

type UserGroupRepository struct{ db *gorm.DB }

func NewUserGroupRepository(db *gorm.DB) *UserGroupRepository {
	return &UserGroupRepository{db: db}
}

func (r *UserGroupRepository) Create(g *models.UserGroup) error { return r.db.Create(g).Error }

func (r *UserGroupRepository) GetByID(id uuid.UUID) (*models.UserGroup, error) {
	var g models.UserGroup
	err := r.db.Where("id = ? AND deleted_at IS NULL", id).First(&g).Error
	if err != nil {
		return nil, err
	}
	return &g, nil
}

func (r *UserGroupRepository) List() ([]models.UserGroup, error) {
	var list []models.UserGroup
	err := r.db.Where("deleted_at IS NULL").Order("created_at DESC").Find(&list).Error
	return list, err
}

func (r *UserGroupRepository) Update(g *models.UserGroup) error { return r.db.Save(g).Error }

func (r *UserGroupRepository) Delete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.UserGroup{}).Error
}

// AddMember adds a user to a group and bumps member_count.
func (r *UserGroupRepository) AddMember(groupID, userID uuid.UUID) error {
	member := models.UserGroupMember{GroupID: groupID, UserID: userID}
	if err := r.db.Where("group_id = ? AND user_id = ?", groupID, userID).
		FirstOrCreate(&member).Error; err != nil {
		return err
	}
	return r.db.Model(&models.UserGroup{}).Where("id = ?", groupID).
		UpdateColumn("member_count", gorm.Expr("(SELECT COUNT(*) FROM user_group_members WHERE group_id = ?)", groupID)).Error
}

// RemoveMember removes a user from a group.
func (r *UserGroupRepository) RemoveMember(groupID, userID uuid.UUID) error {
	if err := r.db.Where("group_id = ? AND user_id = ?", groupID, userID).
		Delete(&models.UserGroupMember{}).Error; err != nil {
		return err
	}
	return r.db.Model(&models.UserGroup{}).Where("id = ?", groupID).
		UpdateColumn("member_count", gorm.Expr("(SELECT COUNT(*) FROM user_group_members WHERE group_id = ?)", groupID)).Error
}

// GetMembers returns all user IDs in a group.
func (r *UserGroupRepository) GetMemberUserIDs(groupID uuid.UUID) ([]uuid.UUID, error) {
	var ids []uuid.UUID
	err := r.db.Model(&models.UserGroupMember{}).
		Where("group_id = ?", groupID).
		Pluck("user_id", &ids).Error
	return ids, err
}

// GetMembersWithDetails returns group members with basic user info.
type GroupMemberDetail struct {
	UserID    uuid.UUID `json:"user_id"`
	FullName  string    `json:"full_name"`
	Email     string    `json:"email"`
	Avatar    string    `json:"avatar"`
	AddedAt   string    `json:"added_at"`
}

func (r *UserGroupRepository) GetMembersWithDetails(groupID uuid.UUID) ([]GroupMemberDetail, error) {
	var members []GroupMemberDetail
	err := r.db.Raw(`
		SELECT m.user_id,
		       TRIM(COALESCE(u.first_name,'') || ' ' || COALESCE(u.last_name,'')) AS full_name,
		       u.email, u.avatar,
		       m.added_at::text
		FROM user_group_members m
		JOIN users u ON u.id = m.user_id AND u.deleted_at IS NULL
		WHERE m.group_id = ?
		ORDER BY m.added_at DESC
	`, groupID).Scan(&members).Error
	return members, err
}
