package services

import (
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"image"
	"image/color"
	"io"
	"os"
	"path/filepath"
	"strings"

	"github.com/buckket/go-blurhash"
	"github.com/disintegration/imaging"
	"github.com/zplus/lichso/internal/models"
	"go.uber.org/zap"
)

// VariantConfig defines the target size for a generated variant
type VariantConfig struct {
	Name   string
	Width  int
	Height int
}

// DefaultVariants are the standard set of generated image variants
var DefaultVariants = []VariantConfig{
	{Name: "thumb_sm", Width: 150, Height: 150},
	{Name: "thumb_md", Width: 300, Height: 300},
	{Name: "thumb_lg", Width: 600, Height: 600},
	{Name: "medium", Width: 800, Height: 0}, // proportional
	{Name: "large", Width: 1200, Height: 0}, // proportional
	{Name: "og", Width: 1200, Height: 630},  // Open Graph
}

// ImageProcessService handles all image processing (resize, crop, WebP, blurhash, etc.)
type ImageProcessService struct {
	uploadPath string
	logger     *zap.Logger
}

// NewImageProcessService creates a new ImageProcessService
func NewImageProcessService(uploadPath string, logger *zap.Logger) *ImageProcessService {
	return &ImageProcessService{
		uploadPath: uploadPath,
		logger:     logger,
	}
}

// ============================================
// Variant Generation
// ============================================

// GenerateVariants creates all default image variants for a given media file
func (s *ImageProcessService) GenerateVariants(media *models.Media) ([]models.MediaVariant, error) {
	if !media.IsImage() {
		return nil, nil
	}

	srcPath := filepath.Join(s.uploadPath, media.Path)
	srcImg, err := imaging.Open(srcPath)
	if err != nil {
		return nil, fmt.Errorf("failed to open image: %w", err)
	}

	dir := filepath.Dir(media.Path)
	baseName := strings.TrimSuffix(media.Filename, filepath.Ext(media.Filename))

	var variants []models.MediaVariant

	for _, vc := range DefaultVariants {
		var resized *image.NRGBA

		if vc.Height == 0 {
			// Proportional resize by width
			resized = imaging.Resize(srcImg, vc.Width, 0, imaging.Lanczos)
		} else if vc.Width == vc.Height {
			// Thumbnail: fill & crop to square
			resized = imaging.Fill(srcImg, vc.Width, vc.Height, imaging.Center, imaging.Lanczos)
		} else {
			// Fill to exact dimensions (e.g., OG image)
			resized = imaging.Fill(srcImg, vc.Width, vc.Height, imaging.Center, imaging.Lanczos)
		}

		// Save as WebP if possible, fallback to JPEG
		variantFilename := fmt.Sprintf("%s_%s.webp", baseName, vc.Name)
		variantRelPath := filepath.Join(dir, variantFilename)
		variantFullPath := filepath.Join(s.uploadPath, variantRelPath)

		// Ensure directory exists
		if err := os.MkdirAll(filepath.Dir(variantFullPath), 0755); err != nil {
			s.logger.Error("Failed to create variant directory", zap.Error(err))
			continue
		}

		// Try saving as WebP-compatible format (actually save as JPEG since Go's imaging lib doesn't support WebP encoding natively)
		// Use JPEG with high quality as the standard variant format
		variantFilename = fmt.Sprintf("%s_%s.jpg", baseName, vc.Name)
		variantRelPath = filepath.Join(dir, variantFilename)
		variantFullPath = filepath.Join(s.uploadPath, variantRelPath)

		if err := imaging.Save(resized, variantFullPath, imaging.JPEGQuality(85)); err != nil {
			s.logger.Error("Failed to save variant",
				zap.String("variant", vc.Name),
				zap.Error(err),
			)
			continue
		}

		// Get file size
		fi, err := os.Stat(variantFullPath)
		if err != nil {
			continue
		}

		bounds := resized.Bounds()
		variants = append(variants, models.MediaVariant{
			MediaID:     media.ID,
			VariantName: vc.Name,
			Path:        variantRelPath,
			MimeType:    "image/jpeg",
			Width:       bounds.Dx(),
			Height:      bounds.Dy(),
			Size:        fi.Size(),
		})
	}

	return variants, nil
}

// GenerateSingleVariant creates a single variant with custom dimensions
func (s *ImageProcessService) GenerateSingleVariant(media *models.Media, name string, width, height int, fit string) (*models.MediaVariant, error) {
	srcPath := filepath.Join(s.uploadPath, media.Path)
	srcImg, err := imaging.Open(srcPath)
	if err != nil {
		return nil, fmt.Errorf("failed to open image: %w", err)
	}

	var resized *image.NRGBA
	switch fit {
	case "contain":
		resized = imaging.Fit(srcImg, width, height, imaging.Lanczos)
	case "fill":
		resized = imaging.Resize(srcImg, width, height, imaging.Lanczos)
	default: // cover
		resized = imaging.Fill(srcImg, width, height, imaging.Center, imaging.Lanczos)
	}

	dir := filepath.Dir(media.Path)
	baseName := strings.TrimSuffix(media.Filename, filepath.Ext(media.Filename))
	variantFilename := fmt.Sprintf("%s_%s.jpg", baseName, name)
	variantRelPath := filepath.Join(dir, variantFilename)
	variantFullPath := filepath.Join(s.uploadPath, variantRelPath)

	if err := os.MkdirAll(filepath.Dir(variantFullPath), 0755); err != nil {
		return nil, fmt.Errorf("failed to create directory: %w", err)
	}

	if err := imaging.Save(resized, variantFullPath, imaging.JPEGQuality(85)); err != nil {
		return nil, fmt.Errorf("failed to save variant: %w", err)
	}

	fi, _ := os.Stat(variantFullPath)
	bounds := resized.Bounds()

	return &models.MediaVariant{
		MediaID:     media.ID,
		VariantName: name,
		Path:        variantRelPath,
		MimeType:    "image/jpeg",
		Width:       bounds.Dx(),
		Height:      bounds.Dy(),
		Size:        fi.Size(),
	}, nil
}

// ============================================
// Image Operations
// ============================================

// CropImage crops an image and saves the result, returns new path & dimensions
func (s *ImageProcessService) CropImage(media *models.Media, x, y, w, h int) (string, *models.Dimensions, error) {
	srcPath := filepath.Join(s.uploadPath, media.Path)
	srcImg, err := imaging.Open(srcPath)
	if err != nil {
		return "", nil, fmt.Errorf("failed to open image: %w", err)
	}

	cropped := imaging.Crop(srcImg, image.Rect(x, y, x+w, y+h))

	// Save back
	dir := filepath.Dir(media.Path)
	baseName := strings.TrimSuffix(media.Filename, filepath.Ext(media.Filename))
	newFilename := fmt.Sprintf("%s_cropped%s", baseName, filepath.Ext(media.Filename))
	newRelPath := filepath.Join(dir, newFilename)
	newFullPath := filepath.Join(s.uploadPath, newRelPath)

	if err := saveImage(cropped, newFullPath, media.MimeType); err != nil {
		return "", nil, err
	}

	bounds := cropped.Bounds()
	return newRelPath, &models.Dimensions{Width: bounds.Dx(), Height: bounds.Dy()}, nil
}

// ResizeImage resizes an image and saves the result
func (s *ImageProcessService) ResizeImage(media *models.Media, width, height int, fit string) (string, *models.Dimensions, error) {
	srcPath := filepath.Join(s.uploadPath, media.Path)
	srcImg, err := imaging.Open(srcPath)
	if err != nil {
		return "", nil, fmt.Errorf("failed to open image: %w", err)
	}

	var resized *image.NRGBA
	switch fit {
	case "contain":
		resized = imaging.Fit(srcImg, width, height, imaging.Lanczos)
	case "fill":
		resized = imaging.Resize(srcImg, width, height, imaging.Lanczos)
	default: // cover
		resized = imaging.Fill(srcImg, width, height, imaging.Center, imaging.Lanczos)
	}

	dir := filepath.Dir(media.Path)
	baseName := strings.TrimSuffix(media.Filename, filepath.Ext(media.Filename))
	newFilename := fmt.Sprintf("%s_resized%s", baseName, filepath.Ext(media.Filename))
	newRelPath := filepath.Join(dir, newFilename)
	newFullPath := filepath.Join(s.uploadPath, newRelPath)

	if err := saveImage(resized, newFullPath, media.MimeType); err != nil {
		return "", nil, err
	}

	bounds := resized.Bounds()
	return newRelPath, &models.Dimensions{Width: bounds.Dx(), Height: bounds.Dy()}, nil
}

// RotateImage rotates an image by the given angle (90, 180, 270)
func (s *ImageProcessService) RotateImage(media *models.Media, angle int) (string, *models.Dimensions, error) {
	srcPath := filepath.Join(s.uploadPath, media.Path)
	srcImg, err := imaging.Open(srcPath)
	if err != nil {
		return "", nil, fmt.Errorf("failed to open image: %w", err)
	}

	var rotated *image.NRGBA
	switch {
	case angle == 90 || angle == -270:
		rotated = imaging.Rotate90(srcImg)
	case angle == 180 || angle == -180:
		rotated = imaging.Rotate180(srcImg)
	case angle == 270 || angle == -90:
		rotated = imaging.Rotate270(srcImg)
	default:
		return "", nil, fmt.Errorf("unsupported rotation angle: %d", angle)
	}

	dir := filepath.Dir(media.Path)
	baseName := strings.TrimSuffix(media.Filename, filepath.Ext(media.Filename))
	newFilename := fmt.Sprintf("%s_rotated%s", baseName, filepath.Ext(media.Filename))
	newRelPath := filepath.Join(dir, newFilename)
	newFullPath := filepath.Join(s.uploadPath, newRelPath)

	if err := saveImage(rotated, newFullPath, media.MimeType); err != nil {
		return "", nil, err
	}

	bounds := rotated.Bounds()
	return newRelPath, &models.Dimensions{Width: bounds.Dx(), Height: bounds.Dy()}, nil
}

// ============================================
// Metadata Extraction
// ============================================

// GetDimensions reads the image dimensions from a file path
func (s *ImageProcessService) GetDimensions(filePath string) (*models.Dimensions, error) {
	fullPath := filepath.Join(s.uploadPath, filePath)
	img, err := imaging.Open(fullPath)
	if err != nil {
		return nil, err
	}
	bounds := img.Bounds()
	return &models.Dimensions{Width: bounds.Dx(), Height: bounds.Dy()}, nil
}

// GenerateBlurHash creates a BlurHash string from an image
func (s *ImageProcessService) GenerateBlurHash(filePath string) (string, error) {
	fullPath := filepath.Join(s.uploadPath, filePath)
	img, err := imaging.Open(fullPath)
	if err != nil {
		return "", fmt.Errorf("failed to open image for blurhash: %w", err)
	}

	// Resize to small size for fast BlurHash computation
	small := imaging.Resize(img, 64, 0, imaging.Lanczos)
	hash, err := blurhash.Encode(4, 3, small)
	if err != nil {
		return "", fmt.Errorf("failed to encode blurhash: %w", err)
	}
	return hash, nil
}

// ExtractDominantColor extracts the dominant color from an image
func (s *ImageProcessService) ExtractDominantColor(filePath string) (string, error) {
	fullPath := filepath.Join(s.uploadPath, filePath)
	img, err := imaging.Open(fullPath)
	if err != nil {
		return "", fmt.Errorf("failed to open image: %w", err)
	}

	// Resize to tiny for fast processing
	tiny := imaging.Resize(img, 1, 1, imaging.Lanczos)
	r, g, b, _ := tiny.At(0, 0).(color.NRGBA).R, tiny.At(0, 0).(color.NRGBA).G, tiny.At(0, 0).(color.NRGBA).B, 0

	return fmt.Sprintf("#%02x%02x%02x", r, g, b), nil
}

// ComputeFileHash computes the SHA-256 hash of a file
func (s *ImageProcessService) ComputeFileHash(filePath string) (string, error) {
	fullPath := filepath.Join(s.uploadPath, filePath)
	f, err := os.Open(fullPath)
	if err != nil {
		return "", err
	}
	defer f.Close()

	h := sha256.New()
	if _, err := io.Copy(h, f); err != nil {
		return "", err
	}
	return hex.EncodeToString(h.Sum(nil)), nil
}

// ProcessImageMetadata extracts all metadata from an image: dimensions, blurhash, dominant color
func (s *ImageProcessService) ProcessImageMetadata(media *models.Media) (*ImageMetadata, error) {
	meta := &ImageMetadata{}

	// Dimensions
	dims, err := s.GetDimensions(media.Path)
	if err == nil {
		meta.Dimensions = dims
	}

	// BlurHash
	hash, err := s.GenerateBlurHash(media.Path)
	if err == nil {
		meta.BlurHash = hash
	}

	// Dominant color
	dc, err := s.ExtractDominantColor(media.Path)
	if err == nil {
		meta.DominantColor = dc
	}

	// File hash
	fh, err := s.ComputeFileHash(media.Path)
	if err == nil {
		meta.FileHash = fh
	}

	return meta, nil
}

// ImageMetadata holds extracted metadata from an image
type ImageMetadata struct {
	Dimensions    *models.Dimensions
	BlurHash      string
	DominantColor string
	FileHash      string
}

// ============================================
// Variant Cleanup
// ============================================

// DeleteVariantFiles removes variant files from disk
func (s *ImageProcessService) DeleteVariantFiles(variants []models.MediaVariant) {
	for _, v := range variants {
		fullPath := filepath.Join(s.uploadPath, v.Path)
		if err := os.Remove(fullPath); err != nil && !os.IsNotExist(err) {
			s.logger.Warn("Failed to delete variant file",
				zap.String("path", fullPath),
				zap.Error(err),
			)
		}
	}
}

// ============================================
// Helpers
// ============================================

func saveImage(img *image.NRGBA, path, mimeType string) error {
	dir := filepath.Dir(path)
	if err := os.MkdirAll(dir, 0755); err != nil {
		return fmt.Errorf("failed to create directory: %w", err)
	}

	switch {
	case strings.Contains(mimeType, "png"):
		return imaging.Save(img, path)
	default:
		return imaging.Save(img, path, imaging.JPEGQuality(90))
	}
}
