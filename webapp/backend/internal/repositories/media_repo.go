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

// MediaRepository handles media CRUD operations in MongoDB
type MediaRepository struct {
	collection *mongo.Collection
}

// NewMediaRepository creates a new MediaRepository
func NewMediaRepository(db *mongo.Database) *MediaRepository {
	return &MediaRepository{
		collection: db.Collection("media"),
	}
}

// Create inserts a new media record
func (r *MediaRepository) Create(ctx context.Context, media *models.Media) error {
	result, err := r.collection.InsertOne(ctx, media)
	if err != nil {
		return err
	}
	media.ID = result.InsertedID.(primitive.ObjectID)
	return nil
}

// FindByID finds a media record by ID
func (r *MediaRepository) FindByID(ctx context.Context, id primitive.ObjectID) (*models.Media, error) {
	var media models.Media
	err := r.collection.FindOne(ctx, bson.M{"_id": id}).Decode(&media)
	if err != nil {
		return nil, err
	}
	return &media, nil
}

// FindAll returns all media with pagination and optional filters (V3 enhanced)
func (r *MediaRepository) FindAll(ctx context.Context, page, limit int, folder, mimeFilter, search string) ([]models.Media, int64, error) {
	filter := bson.M{
		"deleted_at": bson.M{"$exists": false}, // exclude soft-deleted by default
	}

	if folder != "" && folder != "/" {
		filter["folder"] = folder
	}

	if mimeFilter != "" {
		switch mimeFilter {
		case "image":
			filter["mime_type"] = bson.M{"$regex": "^image/"}
		case "document":
			filter["mime_type"] = bson.M{"$regex": "^application/"}
		case "video":
			filter["mime_type"] = bson.M{"$regex": "^video/"}
		case "audio":
			filter["mime_type"] = bson.M{"$regex": "^audio/"}
		default:
			filter["mime_type"] = mimeFilter
		}
	}

	if search != "" {
		filter["$or"] = bson.A{
			bson.M{"original_name": bson.M{"$regex": search, "$options": "i"}},
			bson.M{"description": bson.M{"$regex": search, "$options": "i"}},
			bson.M{"tags": bson.M{"$regex": search, "$options": "i"}},
			bson.M{"alt": bson.M{"$regex": search, "$options": "i"}},
		}
	}

	// Count total
	total, err := r.collection.CountDocuments(ctx, filter)
	if err != nil {
		return nil, 0, err
	}

	// Find with pagination
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

	var media []models.Media
	if err := cursor.All(ctx, &media); err != nil {
		return nil, 0, err
	}

	return media, total, nil
}

// Update updates a media record
func (r *MediaRepository) Update(ctx context.Context, id primitive.ObjectID, update bson.M) error {
	_, err := r.collection.UpdateOne(ctx, bson.M{"_id": id}, bson.M{"$set": update})
	return err
}

// Delete removes a media record
func (r *MediaRepository) Delete(ctx context.Context, id primitive.ObjectID) error {
	_, err := r.collection.DeleteOne(ctx, bson.M{"_id": id})
	return err
}

// DeleteMany removes multiple media records
func (r *MediaRepository) DeleteMany(ctx context.Context, ids []primitive.ObjectID) (int64, error) {
	result, err := r.collection.DeleteMany(ctx, bson.M{"_id": bson.M{"$in": ids}})
	if err != nil {
		return 0, err
	}
	return result.DeletedCount, nil
}

// FindByIDs finds multiple media records by IDs
func (r *MediaRepository) FindByIDs(ctx context.Context, ids []primitive.ObjectID) ([]models.Media, error) {
	cursor, err := r.collection.Find(ctx, bson.M{"_id": bson.M{"$in": ids}})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var media []models.Media
	if err := cursor.All(ctx, &media); err != nil {
		return nil, err
	}
	return media, nil
}

// GetFolders returns all distinct folders
func (r *MediaRepository) GetFolders(ctx context.Context) ([]string, error) {
	values, err := r.collection.Distinct(ctx, "folder", bson.M{})
	if err != nil {
		return nil, err
	}

	folders := make([]string, 0, len(values))
	for _, v := range values {
		if s, ok := v.(string); ok {
			folders = append(folders, s)
		}
	}
	return folders, nil
}

// GetStats returns media storage stats
func (r *MediaRepository) GetStats(ctx context.Context) (int64, int64, error) {
	totalCount, err := r.collection.CountDocuments(ctx, bson.M{})
	if err != nil {
		return 0, 0, err
	}

	// Sum total size
	pipeline := mongo.Pipeline{
		{{Key: "$group", Value: bson.M{
			"_id":        nil,
			"total_size": bson.M{"$sum": "$size"},
		}}},
	}

	cursor, err := r.collection.Aggregate(ctx, pipeline)
	if err != nil {
		return totalCount, 0, nil
	}
	defer cursor.Close(ctx)

	var result struct {
		TotalSize int64 `bson:"total_size"`
	}
	if cursor.Next(ctx) {
		if err := cursor.Decode(&result); err != nil {
			return totalCount, 0, nil
		}
	}

	return totalCount, result.TotalSize, nil
}

// ============================================
// V3 Extended Methods
// ============================================

// FindAllV3 returns media with V3 filtering (media_type, tags, favorite, trash, folder_id, sorting)
func (r *MediaRepository) FindAllV3(ctx context.Context, page, limit int, params MediaFilterParams) ([]models.Media, int64, error) {
	filter := bson.M{}

	// Trash mode: show only soft-deleted items
	if params.Trash {
		filter["deleted_at"] = bson.M{"$exists": true, "$ne": nil}
	} else {
		filter["deleted_at"] = bson.M{"$exists": false}
	}

	if params.Folder != "" && params.Folder != "/" {
		filter["folder"] = params.Folder
	}
	if params.FolderID != nil {
		filter["folder_id"] = *params.FolderID
	}
	if params.MediaType != "" {
		filter["media_type"] = params.MediaType
	}
	if params.MimeFilter != "" {
		switch params.MimeFilter {
		case "image":
			filter["mime_type"] = bson.M{"$regex": "^image/"}
		case "document":
			filter["mime_type"] = bson.M{"$regex": "^application/"}
		case "video":
			filter["mime_type"] = bson.M{"$regex": "^video/"}
		case "audio":
			filter["mime_type"] = bson.M{"$regex": "^audio/"}
		default:
			filter["mime_type"] = params.MimeFilter
		}
	}
	if params.Tag != "" {
		filter["tags"] = params.Tag
	}
	if params.Favorite != nil {
		filter["is_favorite"] = *params.Favorite
	}
	if params.Search != "" {
		filter["$or"] = bson.A{
			bson.M{"original_name": bson.M{"$regex": params.Search, "$options": "i"}},
			bson.M{"description": bson.M{"$regex": params.Search, "$options": "i"}},
			bson.M{"tags": bson.M{"$regex": params.Search, "$options": "i"}},
			bson.M{"alt": bson.M{"$regex": params.Search, "$options": "i"}},
			bson.M{"caption": bson.M{"$regex": params.Search, "$options": "i"}},
		}
	}

	total, err := r.collection.CountDocuments(ctx, filter)
	if err != nil {
		return nil, 0, err
	}

	// Sort
	sortField := "created_at"
	sortOrder := -1 // desc
	if params.SortBy != "" {
		sortField = params.SortBy
	}
	if params.SortOrder == "asc" {
		sortOrder = 1
	}

	skip := int64((page - 1) * limit)
	opts := options.Find().
		SetSort(bson.D{{Key: sortField, Value: sortOrder}}).
		SetSkip(skip).
		SetLimit(int64(limit))

	cursor, err := r.collection.Find(ctx, filter, opts)
	if err != nil {
		return nil, 0, err
	}
	defer cursor.Close(ctx)

	var media []models.Media
	if err := cursor.All(ctx, &media); err != nil {
		return nil, 0, err
	}

	return media, total, nil
}

// MediaFilterParams contains V3 query filters
type MediaFilterParams struct {
	Folder     string
	FolderID   *primitive.ObjectID
	MediaType  string
	MimeFilter string
	Search     string
	Tag        string
	Favorite   *bool
	Trash      bool
	SortBy     string
	SortOrder  string
}

// SoftDelete marks a media as soft-deleted
func (r *MediaRepository) SoftDelete(ctx context.Context, id primitive.ObjectID) error {
	now := time.Now()
	_, err := r.collection.UpdateOne(ctx, bson.M{"_id": id}, bson.M{
		"$set": bson.M{"deleted_at": now, "updated_at": now},
	})
	return err
}

// Restore removes the soft-delete mark
func (r *MediaRepository) Restore(ctx context.Context, id primitive.ObjectID) error {
	_, err := r.collection.UpdateOne(ctx, bson.M{"_id": id}, bson.M{
		"$unset": bson.M{"deleted_at": ""},
		"$set":   bson.M{"updated_at": time.Now()},
	})
	return err
}

// FindTrashed returns all soft-deleted media
func (r *MediaRepository) FindTrashed(ctx context.Context, page, limit int) ([]models.Media, int64, error) {
	filter := bson.M{"deleted_at": bson.M{"$exists": true, "$ne": nil}}
	total, err := r.collection.CountDocuments(ctx, filter)
	if err != nil {
		return nil, 0, err
	}
	skip := int64((page - 1) * limit)
	opts := options.Find().SetSort(bson.D{{Key: "deleted_at", Value: -1}}).SetSkip(skip).SetLimit(int64(limit))
	cursor, err := r.collection.Find(ctx, filter, opts)
	if err != nil {
		return nil, 0, err
	}
	defer cursor.Close(ctx)
	var media []models.Media
	if err := cursor.All(ctx, &media); err != nil {
		return nil, 0, err
	}
	return media, total, nil
}

// CountTrashed returns the number of soft-deleted media
func (r *MediaRepository) CountTrashed(ctx context.Context) (int64, error) {
	return r.collection.CountDocuments(ctx, bson.M{"deleted_at": bson.M{"$exists": true, "$ne": nil}})
}

// CountFavorites returns the number of favorited media
func (r *MediaRepository) CountFavorites(ctx context.Context) (int64, error) {
	return r.collection.CountDocuments(ctx, bson.M{"is_favorite": true, "deleted_at": bson.M{"$exists": false}})
}

// FindByHash finds a media record by file hash (for dedup)
func (r *MediaRepository) FindByHash(ctx context.Context, hash string) (*models.Media, error) {
	var media models.Media
	err := r.collection.FindOne(ctx, bson.M{"file_hash": hash, "deleted_at": bson.M{"$exists": false}}).Decode(&media)
	if err != nil {
		return nil, err
	}
	return &media, nil
}

// FindDuplicates finds potential duplicate files
func (r *MediaRepository) FindDuplicates(ctx context.Context) ([]bson.M, error) {
	pipeline := mongo.Pipeline{
		{{Key: "$match", Value: bson.M{
			"file_hash":  bson.M{"$exists": true, "$ne": ""},
			"deleted_at": bson.M{"$exists": false},
		}}},
		{{Key: "$group", Value: bson.M{
			"_id":   "$file_hash",
			"count": bson.M{"$sum": 1},
			"ids":   bson.M{"$push": "$_id"},
			"names": bson.M{"$push": "$original_name"},
			"size":  bson.M{"$first": "$size"},
		}}},
		{{Key: "$match", Value: bson.M{"count": bson.M{"$gt": 1}}}},
		{{Key: "$sort", Value: bson.M{"count": -1}}},
	}
	cursor, err := r.collection.Aggregate(ctx, pipeline)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)
	var results []bson.M
	if err := cursor.All(ctx, &results); err != nil {
		return nil, err
	}
	return results, nil
}

// FindUnused finds media with zero usage_count
func (r *MediaRepository) FindUnused(ctx context.Context, page, limit int) ([]models.Media, int64, error) {
	filter := bson.M{
		"usage_count": bson.M{"$lte": 0},
		"deleted_at":  bson.M{"$exists": false},
	}
	total, err := r.collection.CountDocuments(ctx, filter)
	if err != nil {
		return nil, 0, err
	}
	skip := int64((page - 1) * limit)
	opts := options.Find().SetSort(bson.D{{Key: "created_at", Value: -1}}).SetSkip(skip).SetLimit(int64(limit))
	cursor, err := r.collection.Find(ctx, filter, opts)
	if err != nil {
		return nil, 0, err
	}
	defer cursor.Close(ctx)
	var media []models.Media
	if err := cursor.All(ctx, &media); err != nil {
		return nil, 0, err
	}
	return media, total, nil
}

// IncrementUsageCount updates the usage count
func (r *MediaRepository) IncrementUsageCount(ctx context.Context, id primitive.ObjectID, delta int) error {
	_, err := r.collection.UpdateOne(ctx, bson.M{"_id": id}, bson.M{
		"$inc": bson.M{"usage_count": delta},
		"$set": bson.M{"updated_at": time.Now()},
	})
	return err
}

// GetStatsByType returns counts grouped by media_type
func (r *MediaRepository) GetStatsByType(ctx context.Context) (map[string]int64, error) {
	pipeline := mongo.Pipeline{
		{{Key: "$match", Value: bson.M{"deleted_at": bson.M{"$exists": false}}}},
		{{Key: "$group", Value: bson.M{
			"_id":   "$media_type",
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
		if item.ID != "" {
			result[item.ID] = item.Count
		}
	}
	return result, nil
}

// GetStatsByMime returns counts grouped by mime_type
func (r *MediaRepository) GetStatsByMime(ctx context.Context) (map[string]int64, error) {
	pipeline := mongo.Pipeline{
		{{Key: "$match", Value: bson.M{"deleted_at": bson.M{"$exists": false}}}},
		{{Key: "$group", Value: bson.M{
			"_id":   "$mime_type",
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
		if item.ID != "" {
			result[item.ID] = item.Count
		}
	}
	return result, nil
}

// EmptyTrash permanently deletes all soft-deleted items
func (r *MediaRepository) EmptyTrash(ctx context.Context) ([]models.Media, error) {
	filter := bson.M{"deleted_at": bson.M{"$exists": true, "$ne": nil}}
	// First, fetch all trashed records to get file paths
	cursor, err := r.collection.Find(ctx, filter)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)
	var media []models.Media
	if err := cursor.All(ctx, &media); err != nil {
		return nil, err
	}
	// Then hard-delete them
	_, err = r.collection.DeleteMany(ctx, filter)
	if err != nil {
		return nil, err
	}
	return media, nil
}
