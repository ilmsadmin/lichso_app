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

// ActivityLogRepository handles database operations for activity logs (MongoDB)
type ActivityLogRepository struct {
	collection *mongo.Collection
}

// NewActivityLogRepository creates a new ActivityLogRepository
func NewActivityLogRepository(db *mongo.Database) *ActivityLogRepository {
	return &ActivityLogRepository{
		collection: db.Collection("activity_logs"),
	}
}

// ActivityLogQuery represents query parameters for listing activity logs
type ActivityLogQuery struct {
	Page      int
	Limit     int
	UserID    string
	Action    string
	Module    string
	Status    string
	StartDate *time.Time
	EndDate   *time.Time
	Search    string
	SortBy    string
	SortOrder string
}

// FindAllPaginated returns paginated activity logs with filters
func (r *ActivityLogRepository) FindAllPaginated(ctx context.Context, query ActivityLogQuery) ([]models.ActivityLog, int64, error) {
	filter := r.buildFilter(query)

	// Count total
	total, err := r.collection.CountDocuments(ctx, filter)
	if err != nil {
		return nil, 0, err
	}

	// Sort direction
	sortDir := -1 // descending by default
	if query.SortOrder == "asc" {
		sortDir = 1
	}

	sortField := "created_at"
	if query.SortBy != "" {
		sortField = query.SortBy
	}

	// Find with pagination
	skip := int64((query.Page - 1) * query.Limit)
	opts := options.Find().
		SetSort(bson.D{{Key: sortField, Value: sortDir}}).
		SetSkip(skip).
		SetLimit(int64(query.Limit))

	cursor, err := r.collection.Find(ctx, filter, opts)
	if err != nil {
		return nil, 0, err
	}
	defer cursor.Close(ctx)

	var logs []models.ActivityLog
	if err := cursor.All(ctx, &logs); err != nil {
		return nil, 0, err
	}

	return logs, total, nil
}

// FindByID returns a single activity log by ID
func (r *ActivityLogRepository) FindByID(ctx context.Context, id string) (*models.ActivityLog, error) {
	objectID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, err
	}

	var log models.ActivityLog
	err = r.collection.FindOne(ctx, bson.M{"_id": objectID}).Decode(&log)
	if err != nil {
		return nil, err
	}

	return &log, nil
}

// CountByDateRange returns the number of logs within a date range
func (r *ActivityLogRepository) CountByDateRange(ctx context.Context, start, end time.Time) (int64, error) {
	filter := bson.M{
		"created_at": bson.M{
			"$gte": start,
			"$lte": end,
		},
	}
	return r.collection.CountDocuments(ctx, filter)
}

// CountByAction returns the number of logs for a specific action
func (r *ActivityLogRepository) CountByAction(ctx context.Context, action string) (int64, error) {
	return r.collection.CountDocuments(ctx, bson.M{"action": action})
}

// GetRecentLogs returns the most recent activity logs
func (r *ActivityLogRepository) GetRecentLogs(ctx context.Context, limit int) ([]models.ActivityLog, error) {
	opts := options.Find().
		SetSort(bson.D{{Key: "created_at", Value: -1}}).
		SetLimit(int64(limit))

	cursor, err := r.collection.Find(ctx, bson.M{}, opts)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var logs []models.ActivityLog
	if err := cursor.All(ctx, &logs); err != nil {
		return nil, err
	}

	return logs, nil
}

// GetActionCounts returns counts of each action type within a date range
func (r *ActivityLogRepository) GetActionCounts(ctx context.Context, start, end time.Time) (map[string]int64, error) {
	pipeline := mongo.Pipeline{
		{{Key: "$match", Value: bson.M{
			"created_at": bson.M{
				"$gte": start,
				"$lte": end,
			},
		}}},
		{{Key: "$group", Value: bson.M{
			"_id":   "$action",
			"count": bson.M{"$sum": 1},
		}}},
	}

	cursor, err := r.collection.Aggregate(ctx, pipeline)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	result := make(map[string]int64)
	for cursor.Next(ctx) {
		var item struct {
			ID    string `bson:"_id"`
			Count int64  `bson:"count"`
		}
		if err := cursor.Decode(&item); err != nil {
			continue
		}
		result[item.ID] = item.Count
	}

	return result, nil
}

// GetModuleCounts returns counts grouped by module within a date range
func (r *ActivityLogRepository) GetModuleCounts(ctx context.Context, start, end time.Time) (map[string]int64, error) {
	pipeline := mongo.Pipeline{
		{{Key: "$match", Value: bson.M{
			"created_at": bson.M{
				"$gte": start,
				"$lte": end,
			},
		}}},
		{{Key: "$group", Value: bson.M{
			"_id":   "$module",
			"count": bson.M{"$sum": 1},
		}}},
	}

	cursor, err := r.collection.Aggregate(ctx, pipeline)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	result := make(map[string]int64)
	for cursor.Next(ctx) {
		var item struct {
			ID    string `bson:"_id"`
			Count int64  `bson:"count"`
		}
		if err := cursor.Decode(&item); err != nil {
			continue
		}
		result[item.ID] = item.Count
	}

	return result, nil
}

// buildFilter creates a MongoDB filter from query parameters
func (r *ActivityLogRepository) buildFilter(query ActivityLogQuery) bson.M {
	filter := bson.M{}

	if query.UserID != "" {
		filter["user_id"] = query.UserID
	}

	if query.Action != "" {
		filter["action"] = query.Action
	}

	if query.Module != "" {
		filter["module"] = query.Module
	}

	if query.Status != "" {
		filter["status"] = query.Status
	}

	if query.Search != "" {
		filter["$or"] = bson.A{
			bson.M{"user_email": bson.M{"$regex": query.Search, "$options": "i"}},
			bson.M{"description": bson.M{"$regex": query.Search, "$options": "i"}},
			bson.M{"action": bson.M{"$regex": query.Search, "$options": "i"}},
		}
	}

	dateFilter := bson.M{}
	if query.StartDate != nil {
		dateFilter["$gte"] = *query.StartDate
	}
	if query.EndDate != nil {
		dateFilter["$lte"] = *query.EndDate
	}
	if len(dateFilter) > 0 {
		filter["created_at"] = dateFilter
	}

	return filter
}
