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

// NotificationRepository handles MongoDB operations for notifications
type NotificationRepository struct {
	collection *mongo.Collection
}

// NewNotificationRepository creates a new NotificationRepository
func NewNotificationRepository(db *mongo.Database) *NotificationRepository {
	return &NotificationRepository{
		collection: db.Collection(models.Notification{}.CollectionName()),
	}
}

// Create inserts a new notification
func (r *NotificationRepository) Create(ctx context.Context, notif *models.Notification) error {
	notif.CreatedAt = time.Now()
	result, err := r.collection.InsertOne(ctx, notif)
	if err != nil {
		return err
	}
	notif.ID = result.InsertedID.(primitive.ObjectID)
	return nil
}

// FindByUserID returns notifications for a user with pagination
func (r *NotificationRepository) FindByUserID(ctx context.Context, userID string, page, limit int) ([]models.Notification, int64, error) {
	filter := bson.M{"user_id": userID}

	// Count total
	total, err := r.collection.CountDocuments(ctx, filter)
	if err != nil {
		return nil, 0, err
	}

	// Query with pagination, newest first
	skip := int64((page - 1) * limit)
	opts := options.Find().
		SetSort(bson.D{{Key: "created_at", Value: -1}}).
		SetSkip(skip).
		SetLimit(int64(limit))

	cursor, err := r.collection.Find(ctx, filter, opts)
	if err != nil {
		return nil, 0, err
	}
	defer cursor.Close(ctx)

	var notifications []models.Notification
	if err := cursor.All(ctx, &notifications); err != nil {
		return nil, 0, err
	}

	return notifications, total, nil
}

// CountUnread returns the count of unread notifications for a user
func (r *NotificationRepository) CountUnread(ctx context.Context, userID string) (int64, error) {
	filter := bson.M{"user_id": userID, "is_read": false}
	return r.collection.CountDocuments(ctx, filter)
}

// MarkAsRead marks a single notification as read
func (r *NotificationRepository) MarkAsRead(ctx context.Context, id primitive.ObjectID, userID string) error {
	filter := bson.M{"_id": id, "user_id": userID}
	update := bson.M{"$set": bson.M{"is_read": true}}
	_, err := r.collection.UpdateOne(ctx, filter, update)
	return err
}

// MarkAllAsRead marks all notifications as read for a user
func (r *NotificationRepository) MarkAllAsRead(ctx context.Context, userID string) error {
	filter := bson.M{"user_id": userID, "is_read": false}
	update := bson.M{"$set": bson.M{"is_read": true}}
	_, err := r.collection.UpdateMany(ctx, filter, update)
	return err
}

// Delete deletes a notification by ID
func (r *NotificationRepository) Delete(ctx context.Context, id primitive.ObjectID, userID string) error {
	filter := bson.M{"_id": id, "user_id": userID}
	_, err := r.collection.DeleteOne(ctx, filter)
	return err
}

// DeleteAllByUser deletes all notifications for a user
func (r *NotificationRepository) DeleteAllByUser(ctx context.Context, userID string) error {
	filter := bson.M{"user_id": userID}
	_, err := r.collection.DeleteMany(ctx, filter)
	return err
}
