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

// ============================================
// MediaVariantRepository
// ============================================

type MediaVariantRepository struct {
	collection *mongo.Collection
}

func NewMediaVariantRepository(db *mongo.Database) *MediaVariantRepository {
	return &MediaVariantRepository{collection: db.Collection("media_variants")}
}

func (r *MediaVariantRepository) Create(ctx context.Context, v *models.MediaVariant) error {
	v.CreatedAt = time.Now()
	result, err := r.collection.InsertOne(ctx, v)
	if err != nil {
		return err
	}
	v.ID = result.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *MediaVariantRepository) CreateMany(ctx context.Context, variants []models.MediaVariant) error {
	docs := make([]interface{}, len(variants))
	for i := range variants {
		variants[i].CreatedAt = time.Now()
		docs[i] = variants[i]
	}
	_, err := r.collection.InsertMany(ctx, docs)
	return err
}

func (r *MediaVariantRepository) FindByMediaID(ctx context.Context, mediaID primitive.ObjectID) ([]models.MediaVariant, error) {
	cursor, err := r.collection.Find(ctx, bson.M{"media_id": mediaID})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)
	var variants []models.MediaVariant
	if err := cursor.All(ctx, &variants); err != nil {
		return nil, err
	}
	return variants, nil
}

func (r *MediaVariantRepository) FindByMediaIDAndName(ctx context.Context, mediaID primitive.ObjectID, name string) (*models.MediaVariant, error) {
	var v models.MediaVariant
	err := r.collection.FindOne(ctx, bson.M{"media_id": mediaID, "variant_name": name}).Decode(&v)
	if err != nil {
		return nil, err
	}
	return &v, nil
}

func (r *MediaVariantRepository) DeleteByMediaID(ctx context.Context, mediaID primitive.ObjectID) (int64, error) {
	result, err := r.collection.DeleteMany(ctx, bson.M{"media_id": mediaID})
	if err != nil {
		return 0, err
	}
	return result.DeletedCount, nil
}

// ============================================
// MediaFolderRepository
// ============================================

type MediaFolderRepository struct {
	collection *mongo.Collection
}

func NewMediaFolderRepository(db *mongo.Database) *MediaFolderRepository {
	return &MediaFolderRepository{collection: db.Collection("media_folders")}
}

func (r *MediaFolderRepository) Create(ctx context.Context, f *models.MediaFolder) error {
	f.CreatedAt = time.Now()
	f.UpdatedAt = time.Now()
	result, err := r.collection.InsertOne(ctx, f)
	if err != nil {
		return err
	}
	f.ID = result.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *MediaFolderRepository) FindByID(ctx context.Context, id primitive.ObjectID) (*models.MediaFolder, error) {
	var f models.MediaFolder
	err := r.collection.FindOne(ctx, bson.M{"_id": id}).Decode(&f)
	if err != nil {
		return nil, err
	}
	return &f, nil
}

func (r *MediaFolderRepository) FindBySlug(ctx context.Context, slug string) (*models.MediaFolder, error) {
	var f models.MediaFolder
	err := r.collection.FindOne(ctx, bson.M{"slug": slug}).Decode(&f)
	if err != nil {
		return nil, err
	}
	return &f, nil
}

func (r *MediaFolderRepository) FindByParentID(ctx context.Context, parentID *primitive.ObjectID) ([]models.MediaFolder, error) {
	filter := bson.M{}
	if parentID != nil {
		filter["parent_id"] = *parentID
	} else {
		filter["parent_id"] = bson.M{"$exists": false}
	}
	opts := options.Find().SetSort(bson.D{{Key: "sort_order", Value: 1}, {Key: "name", Value: 1}})
	cursor, err := r.collection.Find(ctx, filter, opts)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)
	var folders []models.MediaFolder
	if err := cursor.All(ctx, &folders); err != nil {
		return nil, err
	}
	return folders, nil
}

func (r *MediaFolderRepository) FindAll(ctx context.Context) ([]models.MediaFolder, error) {
	opts := options.Find().SetSort(bson.D{{Key: "path", Value: 1}})
	cursor, err := r.collection.Find(ctx, bson.M{}, opts)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)
	var folders []models.MediaFolder
	if err := cursor.All(ctx, &folders); err != nil {
		return nil, err
	}
	return folders, nil
}

func (r *MediaFolderRepository) Update(ctx context.Context, id primitive.ObjectID, update bson.M) error {
	update["updated_at"] = time.Now()
	_, err := r.collection.UpdateOne(ctx, bson.M{"_id": id}, bson.M{"$set": update})
	return err
}

func (r *MediaFolderRepository) Delete(ctx context.Context, id primitive.ObjectID) error {
	_, err := r.collection.DeleteOne(ctx, bson.M{"_id": id})
	return err
}

func (r *MediaFolderRepository) IncrementMediaCount(ctx context.Context, id primitive.ObjectID, countDelta int, sizeDelta int64) error {
	_, err := r.collection.UpdateOne(ctx, bson.M{"_id": id}, bson.M{
		"$inc": bson.M{"media_count": countDelta, "total_size": sizeDelta},
		"$set": bson.M{"updated_at": time.Now()},
	})
	return err
}

func (r *MediaFolderRepository) Count(ctx context.Context) (int64, error) {
	return r.collection.CountDocuments(ctx, bson.M{})
}

// ============================================
// MediaAlbumRepository
// ============================================

type MediaAlbumRepository struct {
	collection     *mongo.Collection
	itemCollection *mongo.Collection
}

func NewMediaAlbumRepository(db *mongo.Database) *MediaAlbumRepository {
	return &MediaAlbumRepository{
		collection:     db.Collection("media_albums"),
		itemCollection: db.Collection("media_album_items"),
	}
}

func (r *MediaAlbumRepository) Create(ctx context.Context, a *models.MediaAlbum) error {
	a.CreatedAt = time.Now()
	a.UpdatedAt = time.Now()
	result, err := r.collection.InsertOne(ctx, a)
	if err != nil {
		return err
	}
	a.ID = result.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *MediaAlbumRepository) FindByID(ctx context.Context, id primitive.ObjectID) (*models.MediaAlbum, error) {
	var a models.MediaAlbum
	err := r.collection.FindOne(ctx, bson.M{"_id": id}).Decode(&a)
	if err != nil {
		return nil, err
	}
	return &a, nil
}

func (r *MediaAlbumRepository) FindBySlug(ctx context.Context, slug string) (*models.MediaAlbum, error) {
	var a models.MediaAlbum
	err := r.collection.FindOne(ctx, bson.M{"slug": slug}).Decode(&a)
	if err != nil {
		return nil, err
	}
	return &a, nil
}

func (r *MediaAlbumRepository) FindAll(ctx context.Context, page, limit int, visibility string) ([]models.MediaAlbum, int64, error) {
	filter := bson.M{}
	if visibility != "" {
		filter["visibility"] = visibility
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
	var albums []models.MediaAlbum
	if err := cursor.All(ctx, &albums); err != nil {
		return nil, 0, err
	}
	return albums, total, nil
}

func (r *MediaAlbumRepository) Update(ctx context.Context, id primitive.ObjectID, update bson.M) error {
	update["updated_at"] = time.Now()
	_, err := r.collection.UpdateOne(ctx, bson.M{"_id": id}, bson.M{"$set": update})
	return err
}

func (r *MediaAlbumRepository) Delete(ctx context.Context, id primitive.ObjectID) error {
	// Delete album items first
	r.itemCollection.DeleteMany(ctx, bson.M{"album_id": id})
	_, err := r.collection.DeleteOne(ctx, bson.M{"_id": id})
	return err
}

func (r *MediaAlbumRepository) Count(ctx context.Context) (int64, error) {
	return r.collection.CountDocuments(ctx, bson.M{})
}

// --- Album Items ---

func (r *MediaAlbumRepository) AddItems(ctx context.Context, items []models.MediaAlbumItem) error {
	docs := make([]interface{}, len(items))
	for i := range items {
		items[i].AddedAt = time.Now()
		docs[i] = items[i]
	}
	_, err := r.itemCollection.InsertMany(ctx, docs)
	return err
}

func (r *MediaAlbumRepository) RemoveItems(ctx context.Context, albumID primitive.ObjectID, mediaIDs []primitive.ObjectID) (int64, error) {
	result, err := r.itemCollection.DeleteMany(ctx, bson.M{
		"album_id": albumID,
		"media_id": bson.M{"$in": mediaIDs},
	})
	if err != nil {
		return 0, err
	}
	return result.DeletedCount, nil
}

func (r *MediaAlbumRepository) FindItems(ctx context.Context, albumID primitive.ObjectID) ([]models.MediaAlbumItem, error) {
	opts := options.Find().SetSort(bson.D{{Key: "sort_order", Value: 1}})
	cursor, err := r.itemCollection.Find(ctx, bson.M{"album_id": albumID}, opts)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)
	var items []models.MediaAlbumItem
	if err := cursor.All(ctx, &items); err != nil {
		return nil, err
	}
	return items, nil
}

func (r *MediaAlbumRepository) UpdateItemOrder(ctx context.Context, itemID primitive.ObjectID, sortOrder int) error {
	_, err := r.itemCollection.UpdateOne(ctx, bson.M{"_id": itemID}, bson.M{
		"$set": bson.M{"sort_order": sortOrder},
	})
	return err
}

func (r *MediaAlbumRepository) CountItems(ctx context.Context, albumID primitive.ObjectID) (int64, error) {
	return r.itemCollection.CountDocuments(ctx, bson.M{"album_id": albumID})
}

// ============================================
// MediaAttachmentRepository
// ============================================

type MediaAttachmentRepository struct {
	collection *mongo.Collection
}

func NewMediaAttachmentRepository(db *mongo.Database) *MediaAttachmentRepository {
	return &MediaAttachmentRepository{collection: db.Collection("media_attachments")}
}

func (r *MediaAttachmentRepository) Create(ctx context.Context, a *models.MediaAttachment) error {
	a.CreatedAt = time.Now()
	result, err := r.collection.InsertOne(ctx, a)
	if err != nil {
		return err
	}
	a.ID = result.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *MediaAttachmentRepository) FindByEntity(ctx context.Context, entityType, entityID string) ([]models.MediaAttachment, error) {
	opts := options.Find().SetSort(bson.D{{Key: "sort_order", Value: 1}})
	cursor, err := r.collection.Find(ctx, bson.M{
		"entity_type": entityType,
		"entity_id":   entityID,
	}, opts)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)
	var attachments []models.MediaAttachment
	if err := cursor.All(ctx, &attachments); err != nil {
		return nil, err
	}
	return attachments, nil
}

func (r *MediaAttachmentRepository) FindByMediaID(ctx context.Context, mediaID primitive.ObjectID) ([]models.MediaAttachment, error) {
	cursor, err := r.collection.Find(ctx, bson.M{"media_id": mediaID})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)
	var attachments []models.MediaAttachment
	if err := cursor.All(ctx, &attachments); err != nil {
		return nil, err
	}
	return attachments, nil
}

func (r *MediaAttachmentRepository) Delete(ctx context.Context, mediaID primitive.ObjectID, entityType, entityID string) error {
	_, err := r.collection.DeleteOne(ctx, bson.M{
		"media_id":    mediaID,
		"entity_type": entityType,
		"entity_id":   entityID,
	})
	return err
}

func (r *MediaAttachmentRepository) DeleteByMediaID(ctx context.Context, mediaID primitive.ObjectID) (int64, error) {
	result, err := r.collection.DeleteMany(ctx, bson.M{"media_id": mediaID})
	if err != nil {
		return 0, err
	}
	return result.DeletedCount, nil
}

func (r *MediaAttachmentRepository) CountByMediaID(ctx context.Context, mediaID primitive.ObjectID) (int64, error) {
	return r.collection.CountDocuments(ctx, bson.M{"media_id": mediaID})
}

// ============================================
// ChunkUploadRepository
// ============================================

type ChunkUploadRepository struct {
	collection *mongo.Collection
}

func NewChunkUploadRepository(db *mongo.Database) *ChunkUploadRepository {
	return &ChunkUploadRepository{collection: db.Collection("chunk_uploads")}
}

func (r *ChunkUploadRepository) Create(ctx context.Context, c *models.ChunkUpload) error {
	c.CreatedAt = time.Now()
	result, err := r.collection.InsertOne(ctx, c)
	if err != nil {
		return err
	}
	c.ID = result.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *ChunkUploadRepository) FindByUploadID(ctx context.Context, uploadID string) (*models.ChunkUpload, error) {
	var c models.ChunkUpload
	err := r.collection.FindOne(ctx, bson.M{"upload_id": uploadID}).Decode(&c)
	if err != nil {
		return nil, err
	}
	return &c, nil
}

func (r *ChunkUploadRepository) AddChunk(ctx context.Context, uploadID string, chunkIndex int) error {
	_, err := r.collection.UpdateOne(ctx, bson.M{"upload_id": uploadID}, bson.M{
		"$addToSet": bson.M{"uploaded_chunks": chunkIndex},
	})
	return err
}

func (r *ChunkUploadRepository) UpdateStatus(ctx context.Context, uploadID, status string) error {
	_, err := r.collection.UpdateOne(ctx, bson.M{"upload_id": uploadID}, bson.M{
		"$set": bson.M{"status": status},
	})
	return err
}

func (r *ChunkUploadRepository) Delete(ctx context.Context, uploadID string) error {
	_, err := r.collection.DeleteOne(ctx, bson.M{"upload_id": uploadID})
	return err
}

func (r *ChunkUploadRepository) DeleteExpired(ctx context.Context) (int64, error) {
	result, err := r.collection.DeleteMany(ctx, bson.M{
		"expires_at": bson.M{"$lt": time.Now()},
		"status":     bson.M{"$ne": "completed"},
	})
	if err != nil {
		return 0, err
	}
	return result.DeletedCount, nil
}

// ============================================
// MediaVersionRepository
// ============================================

type MediaVersionRepository struct {
	collection *mongo.Collection
}

func NewMediaVersionRepository(db *mongo.Database) *MediaVersionRepository {
	return &MediaVersionRepository{collection: db.Collection("media_versions")}
}

func (r *MediaVersionRepository) Create(ctx context.Context, v *models.MediaVersion) error {
	v.CreatedAt = time.Now()
	result, err := r.collection.InsertOne(ctx, v)
	if err != nil {
		return err
	}
	v.ID = result.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *MediaVersionRepository) FindByMediaID(ctx context.Context, mediaID primitive.ObjectID) ([]models.MediaVersion, error) {
	opts := options.Find().SetSort(bson.D{{Key: "version_number", Value: -1}})
	cursor, err := r.collection.Find(ctx, bson.M{"media_id": mediaID}, opts)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)
	var versions []models.MediaVersion
	if err := cursor.All(ctx, &versions); err != nil {
		return nil, err
	}
	return versions, nil
}

func (r *MediaVersionRepository) GetLatestVersion(ctx context.Context, mediaID primitive.ObjectID) (int, error) {
	opts := options.FindOne().SetSort(bson.D{{Key: "version_number", Value: -1}})
	var v models.MediaVersion
	err := r.collection.FindOne(ctx, bson.M{"media_id": mediaID}, opts).Decode(&v)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return 0, nil
		}
		return 0, err
	}
	return v.VersionNumber, nil
}

func (r *MediaVersionRepository) DeleteByMediaID(ctx context.Context, mediaID primitive.ObjectID) (int64, error) {
	result, err := r.collection.DeleteMany(ctx, bson.M{"media_id": mediaID})
	if err != nil {
		return 0, err
	}
	return result.DeletedCount, nil
}
