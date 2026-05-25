package services

import (
	"context"
	"sync"
	"time"

	"github.com/zplus/lichso/internal/repositories"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.uber.org/zap"
)

// MediaWorker is a background worker that processes images from the Redis queue
type MediaWorker struct {
	mediaRepo    *repositories.MediaRepository
	variantRepo  *repositories.MediaVariantRepository
	imgService   *ImageProcessService
	videoService *VideoProcessService
	cacheService *MediaCacheService
	logger       *zap.Logger
	stopCh       chan struct{}
	wg           sync.WaitGroup
}

// NewMediaWorker creates a new MediaWorker
func NewMediaWorker(
	mediaRepo *repositories.MediaRepository,
	variantRepo *repositories.MediaVariantRepository,
	imgService *ImageProcessService,
	videoService *VideoProcessService,
	cacheService *MediaCacheService,
	logger *zap.Logger,
) *MediaWorker {
	return &MediaWorker{
		mediaRepo:    mediaRepo,
		variantRepo:  variantRepo,
		imgService:   imgService,
		videoService: videoService,
		cacheService: cacheService,
		logger:       logger,
		stopCh:       make(chan struct{}),
	}
}

// Start begins the worker loop
func (w *MediaWorker) Start() {
	w.wg.Add(1)
	go func() {
		defer w.wg.Done()
		w.logger.Info("📸 Media processing worker started")
		for {
			select {
			case <-w.stopCh:
				w.logger.Info("📸 Media processing worker stopped")
				return
			default:
				w.processNext()
			}
		}
	}()
}

// Stop gracefully stops the worker
func (w *MediaWorker) Stop() {
	close(w.stopCh)
	w.wg.Wait()
}

func (w *MediaWorker) processNext() {
	// Block for up to 5 seconds waiting for a new item
	mediaID, err := w.cacheService.DequeueImageProcess(5 * time.Second)
	if err != nil {
		// Timeout or error — just loop back
		return
	}

	if mediaID == "" {
		return
	}

	w.logger.Info("Processing media", zap.String("media_id", mediaID))

	ctx := context.Background()
	objID, err := primitive.ObjectIDFromHex(mediaID)
	if err != nil {
		w.logger.Error("Invalid media ID in queue", zap.String("media_id", mediaID))
		return
	}

	media, err := w.mediaRepo.FindByID(ctx, objID)
	if err != nil {
		w.logger.Error("Media not found for processing", zap.String("media_id", mediaID), zap.Error(err))
		return
	}

	// Mark as processing
	w.mediaRepo.Update(ctx, objID, bson.M{"process_status": "processing"})

	// Process image metadata if not already done
	if media.IsImage() && media.BlurHash == "" {
		meta, err := w.imgService.ProcessImageMetadata(media)
		if err != nil {
			w.logger.Error("Failed to process image metadata", zap.Error(err))
		} else {
			update := bson.M{}
			if meta.Dimensions != nil {
				update["dimensions"] = meta.Dimensions
			}
			if meta.BlurHash != "" {
				update["blur_hash"] = meta.BlurHash
			}
			if meta.DominantColor != "" {
				update["dominant_color"] = meta.DominantColor
			}
			if meta.FileHash != "" {
				update["file_hash"] = meta.FileHash
				w.cacheService.StoreFileHash(meta.FileHash, mediaID)
			}
			if len(update) > 0 {
				w.mediaRepo.Update(ctx, objID, update)
			}
		}
	}

	// Generate variants if none exist
	if media.IsImage() {
		existing, _ := w.variantRepo.FindByMediaID(ctx, objID)
		if len(existing) == 0 {
			variants, err := w.imgService.GenerateVariants(media)
			if err != nil {
				w.logger.Error("Failed to generate variants", zap.Error(err))
				w.mediaRepo.Update(ctx, objID, bson.M{"process_status": "failed"})
				return
			}
			if len(variants) > 0 {
				w.variantRepo.CreateMany(ctx, variants)
			}
		}
	} else if media.MediaType == "video" && w.videoService != nil && w.videoService.IsFFmpegAvailable() {
		// Video processing: extract metadata + generate thumbnail variants
		if media.Duration == 0 {
			vmeta, err := w.videoService.ExtractMetadata(media.Path)
			if err == nil {
				update := bson.M{}
				if vmeta.Duration > 0 {
					update["duration"] = vmeta.Duration
				}
				if vmeta.Dimensions != nil {
					update["dimensions"] = vmeta.Dimensions
				}
				if len(update) > 0 {
					w.mediaRepo.Update(ctx, objID, update)
				}
			} else {
				w.logger.Warn("Failed to extract video metadata", zap.Error(err))
			}
		}

		existing, _ := w.variantRepo.FindByMediaID(ctx, objID)
		if len(existing) == 0 {
			variants, err := w.videoService.GenerateVideoVariants(media)
			if err != nil {
				w.logger.Error("Failed to generate video variants", zap.Error(err))
				w.mediaRepo.Update(ctx, objID, bson.M{"process_status": "failed"})
				return
			}
			if len(variants) > 0 {
				w.variantRepo.CreateMany(ctx, variants)
			}
		}
	}

	// Mark as completed
	w.mediaRepo.Update(ctx, objID, bson.M{"process_status": "completed"})
	w.cacheService.InvalidateMedia(mediaID)
	w.logger.Info("Media processing completed", zap.String("media_id", mediaID))
}
