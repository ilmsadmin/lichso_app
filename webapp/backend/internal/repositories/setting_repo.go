package repositories

import (
	"context"
	"time"

	"github.com/zplus/lichso/internal/models"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

// SettingRepository handles database operations for app settings (MongoDB)
type SettingRepository struct {
	collection *mongo.Collection
}

// NewSettingRepository creates a new SettingRepository
func NewSettingRepository(db *mongo.Database) *SettingRepository {
	return &SettingRepository{
		collection: db.Collection("app_settings"),
	}
}

// FindAll returns all settings
func (r *SettingRepository) FindAll(ctx context.Context) ([]models.Setting, error) {
	opts := options.Find().SetSort(bson.D{{Key: "group", Value: 1}, {Key: "key", Value: 1}})

	cursor, err := r.collection.Find(ctx, bson.M{}, opts)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var settings []models.Setting
	if err := cursor.All(ctx, &settings); err != nil {
		return nil, err
	}

	return settings, nil
}

// FindByGroup returns all settings for a specific group
func (r *SettingRepository) FindByGroup(ctx context.Context, group string) ([]models.Setting, error) {
	opts := options.Find().SetSort(bson.D{{Key: "key", Value: 1}})

	cursor, err := r.collection.Find(ctx, bson.M{"group": group}, opts)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var settings []models.Setting
	if err := cursor.All(ctx, &settings); err != nil {
		return nil, err
	}

	return settings, nil
}

// FindByKey returns a single setting by key
func (r *SettingRepository) FindByKey(ctx context.Context, key string) (*models.Setting, error) {
	var setting models.Setting
	err := r.collection.FindOne(ctx, bson.M{"key": key}).Decode(&setting)
	if err != nil {
		return nil, err
	}
	return &setting, nil
}

// FindByID returns a single setting by ID
func (r *SettingRepository) FindByID(ctx context.Context, id string) (*models.Setting, error) {
	objectID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, err
	}

	var setting models.Setting
	err = r.collection.FindOne(ctx, bson.M{"_id": objectID}).Decode(&setting)
	if err != nil {
		return nil, err
	}

	return &setting, nil
}

// Upsert creates or updates a setting by key
func (r *SettingRepository) Upsert(ctx context.Context, setting *models.Setting) error {
	filter := bson.M{"key": setting.Key}
	update := bson.M{
		"$set": bson.M{
			"value":       setting.Value,
			"group":       setting.Group,
			"description": setting.Description,
			"updated_by":  setting.UpdatedBy,
			"updated_at":  time.Now(),
		},
	}
	opts := options.Update().SetUpsert(true)

	_, err := r.collection.UpdateOne(ctx, filter, update, opts)
	return err
}

// BulkUpsert creates or updates multiple settings at once
func (r *SettingRepository) BulkUpsert(ctx context.Context, settings []models.Setting) error {
	if len(settings) == 0 {
		return nil
	}

	var operations []mongo.WriteModel
	for _, s := range settings {
		filter := bson.M{"key": s.Key}
		update := bson.M{
			"$set": bson.M{
				"value":       s.Value,
				"group":       s.Group,
				"description": s.Description,
				"updated_by":  s.UpdatedBy,
				"updated_at":  time.Now(),
			},
		}
		op := mongo.NewUpdateOneModel().SetFilter(filter).SetUpdate(update).SetUpsert(true)
		operations = append(operations, op)
	}

	_, err := r.collection.BulkWrite(ctx, operations)
	return err
}

// Delete removes a setting by key
func (r *SettingRepository) Delete(ctx context.Context, key string) error {
	_, err := r.collection.DeleteOne(ctx, bson.M{"key": key})
	return err
}

// GetGrouped returns settings grouped by their group field
func (r *SettingRepository) GetGrouped(ctx context.Context) (map[string][]models.Setting, error) {
	settings, err := r.FindAll(ctx)
	if err != nil {
		return nil, err
	}

	grouped := make(map[string][]models.Setting)
	for _, s := range settings {
		grouped[s.Group] = append(grouped[s.Group], s)
	}

	return grouped, nil
}
