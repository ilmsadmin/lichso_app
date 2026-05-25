package services

import (
	"encoding/json"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"

	"github.com/zplus/lichso/internal/models"
	"go.uber.org/zap"
)

// VideoMetadata holds extracted metadata from a video file
type VideoMetadata struct {
	Duration   float64            `json:"duration"`    // seconds
	Width      int                `json:"width"`       // pixels
	Height     int                `json:"height"`      // pixels
	Codec      string             `json:"codec"`       // e.g. "h264"
	Bitrate    int64              `json:"bitrate"`     // bits per second
	FPS        float64            `json:"fps"`         // frames per second
	AudioCodec string             `json:"audio_codec"` // e.g. "aac"
	Dimensions *models.Dimensions `json:"dimensions"`
}

// ffprobeOutput maps the JSON output from ffprobe
type ffprobeOutput struct {
	Streams []ffprobeStream `json:"streams"`
	Format  ffprobeFormat   `json:"format"`
}

type ffprobeStream struct {
	CodecType    string `json:"codec_type"`
	CodecName    string `json:"codec_name"`
	Width        int    `json:"width"`
	Height       int    `json:"height"`
	RFrameRate   string `json:"r_frame_rate"`
	AvgFrameRate string `json:"avg_frame_rate"`
	Duration     string `json:"duration"`
}

type ffprobeFormat struct {
	Duration string `json:"duration"`
	BitRate  string `json:"bit_rate"`
	Size     string `json:"size"`
}

// VideoProcessService handles video thumbnail extraction and metadata parsing via FFmpeg/FFprobe
type VideoProcessService struct {
	uploadPath  string
	ffmpegPath  string
	ffprobePath string
	logger      *zap.Logger
}

// NewVideoProcessService creates a new VideoProcessService
func NewVideoProcessService(uploadPath string, logger *zap.Logger) *VideoProcessService {
	ffmpegPath, _ := exec.LookPath("ffmpeg")
	if ffmpegPath == "" {
		ffmpegPath = "ffmpeg" // fallback, let exec handle PATH lookup
	}
	ffprobePath, _ := exec.LookPath("ffprobe")
	if ffprobePath == "" {
		ffprobePath = "ffprobe"
	}

	return &VideoProcessService{
		uploadPath:  uploadPath,
		ffmpegPath:  ffmpegPath,
		ffprobePath: ffprobePath,
		logger:      logger,
	}
}

// IsFFmpegAvailable checks if FFmpeg and FFprobe are installed
func (s *VideoProcessService) IsFFmpegAvailable() bool {
	_, err := exec.LookPath("ffmpeg")
	if err != nil {
		return false
	}
	_, err = exec.LookPath("ffprobe")
	return err == nil
}

// ============================================
// Video Metadata Extraction
// ============================================

// ExtractMetadata uses ffprobe to extract video metadata (duration, resolution, codec, bitrate, fps)
func (s *VideoProcessService) ExtractMetadata(mediaPath string) (*VideoMetadata, error) {
	fullPath := filepath.Join(s.uploadPath, mediaPath)

	// Run ffprobe with JSON output
	cmd := exec.Command(s.ffprobePath,
		"-v", "quiet",
		"-print_format", "json",
		"-show_format",
		"-show_streams",
		fullPath,
	)

	output, err := cmd.Output()
	if err != nil {
		s.logger.Error("ffprobe failed", zap.String("path", fullPath), zap.Error(err))
		return nil, fmt.Errorf("ffprobe failed: %w", err)
	}

	var probe ffprobeOutput
	if err := json.Unmarshal(output, &probe); err != nil {
		s.logger.Error("Failed to parse ffprobe output", zap.Error(err))
		return nil, fmt.Errorf("failed to parse ffprobe output: %w", err)
	}

	meta := &VideoMetadata{}

	// Parse format-level metadata
	if probe.Format.Duration != "" {
		if d, err := strconv.ParseFloat(probe.Format.Duration, 64); err == nil {
			meta.Duration = d
		}
	}
	if probe.Format.BitRate != "" {
		if br, err := strconv.ParseInt(probe.Format.BitRate, 10, 64); err == nil {
			meta.Bitrate = br
		}
	}

	// Parse stream-level metadata
	for _, stream := range probe.Streams {
		switch stream.CodecType {
		case "video":
			meta.Codec = stream.CodecName
			meta.Width = stream.Width
			meta.Height = stream.Height
			meta.Dimensions = &models.Dimensions{Width: stream.Width, Height: stream.Height}

			// Parse FPS from r_frame_rate (format: "30/1" or "30000/1001")
			meta.FPS = parseFraction(stream.RFrameRate)
			if meta.FPS == 0 {
				meta.FPS = parseFraction(stream.AvgFrameRate)
			}

			// Fallback duration from stream if format didn't have it
			if meta.Duration == 0 && stream.Duration != "" {
				if d, err := strconv.ParseFloat(stream.Duration, 64); err == nil {
					meta.Duration = d
				}
			}

		case "audio":
			meta.AudioCodec = stream.CodecName
		}
	}

	return meta, nil
}

// ============================================
// Video Thumbnail Extraction
// ============================================

// ExtractThumbnail extracts a thumbnail from a video at the given timestamp (seconds).
// Returns the relative path of the generated thumbnail image.
func (s *VideoProcessService) ExtractThumbnail(media *models.Media, timestampSec float64) (string, *models.Dimensions, error) {
	srcPath := filepath.Join(s.uploadPath, media.Path)

	// Generate thumbnail filename
	dir := filepath.Dir(media.Path)
	baseName := strings.TrimSuffix(media.Filename, filepath.Ext(media.Filename))
	thumbFilename := fmt.Sprintf("%s_thumb.jpg", baseName)
	thumbRelPath := filepath.Join(dir, thumbFilename)
	thumbFullPath := filepath.Join(s.uploadPath, thumbRelPath)

	// Ensure directory exists
	if err := os.MkdirAll(filepath.Dir(thumbFullPath), 0755); err != nil {
		return "", nil, fmt.Errorf("failed to create directory: %w", err)
	}

	// Format timestamp as HH:MM:SS.mmm
	ts := formatTimestamp(timestampSec)

	// Run FFmpeg to extract thumbnail
	cmd := exec.Command(s.ffmpegPath,
		"-y",          // overwrite output
		"-i", srcPath, // input file
		"-ss", ts, // seek to timestamp
		"-vframes", "1", // extract 1 frame
		"-q:v", "2", // high quality JPEG
		"-vf", "scale='min(1200,iw)':-2", // max width 1200, maintain aspect ratio
		thumbFullPath,
	)

	if output, err := cmd.CombinedOutput(); err != nil {
		s.logger.Error("FFmpeg thumbnail extraction failed",
			zap.String("path", srcPath),
			zap.String("output", string(output)),
			zap.Error(err),
		)
		return "", nil, fmt.Errorf("ffmpeg thumbnail extraction failed: %w", err)
	}

	// Get thumbnail dimensions by re-reading with ffprobe
	dims, err := s.getImageDimensions(thumbFullPath)
	if err != nil {
		s.logger.Warn("Failed to get thumbnail dimensions", zap.Error(err))
		// Non-fatal: still return the path
		return thumbRelPath, nil, nil
	}

	s.logger.Info("Video thumbnail extracted",
		zap.String("media_id", media.ID.Hex()),
		zap.String("thumb_path", thumbRelPath),
		zap.Int("width", dims.Width),
		zap.Int("height", dims.Height),
	)

	return thumbRelPath, dims, nil
}

// ExtractThumbnailAuto extracts a thumbnail from the best position in the video.
// Strategy: extract at 10% of video duration (avoids black intro frames).
func (s *VideoProcessService) ExtractThumbnailAuto(media *models.Media) (string, *models.Dimensions, error) {
	// First get the video duration
	meta, err := s.ExtractMetadata(media.Path)
	if err != nil {
		// Fallback: extract at 1 second
		return s.ExtractThumbnail(media, 1.0)
	}

	// Extract at 10% of duration, minimum 1 second
	ts := meta.Duration * 0.10
	if ts < 1.0 {
		ts = 1.0
	}
	if ts > meta.Duration {
		ts = 0.0 // very short video
	}

	return s.ExtractThumbnail(media, ts)
}

// GenerateVideoVariants creates thumbnail variants for a video (small, medium, large thumbnails)
func (s *VideoProcessService) GenerateVideoVariants(media *models.Media) ([]models.MediaVariant, error) {
	// Extract a high-quality source thumbnail first
	thumbPath, _, err := s.ExtractThumbnailAuto(media)
	if err != nil {
		return nil, fmt.Errorf("failed to extract source thumbnail: %w", err)
	}

	thumbFullPath := filepath.Join(s.uploadPath, thumbPath)

	// Now generate resized variants from the source thumbnail using FFmpeg
	dir := filepath.Dir(media.Path)
	baseName := strings.TrimSuffix(media.Filename, filepath.Ext(media.Filename))

	variantConfigs := []struct {
		Name   string
		Width  int
		Height int
	}{
		{"thumb_sm", 150, 150},
		{"thumb_md", 300, 300},
		{"thumb_lg", 600, 600},
	}

	var variants []models.MediaVariant

	for _, vc := range variantConfigs {
		variantFilename := fmt.Sprintf("%s_%s.jpg", baseName, vc.Name)
		variantRelPath := filepath.Join(dir, variantFilename)
		variantFullPath := filepath.Join(s.uploadPath, variantRelPath)

		// Use FFmpeg to resize the source thumbnail
		vf := fmt.Sprintf("scale=%d:%d:force_original_aspect_ratio=increase,crop=%d:%d", vc.Width, vc.Height, vc.Width, vc.Height)
		cmd := exec.Command(s.ffmpegPath,
			"-y",
			"-i", thumbFullPath,
			"-vf", vf,
			"-q:v", "3",
			variantFullPath,
		)

		if output, err := cmd.CombinedOutput(); err != nil {
			s.logger.Error("Failed to generate video variant",
				zap.String("variant", vc.Name),
				zap.String("output", string(output)),
				zap.Error(err),
			)
			continue
		}

		// Get file info
		fi, err := os.Stat(variantFullPath)
		if err != nil {
			continue
		}

		variants = append(variants, models.MediaVariant{
			MediaID:     media.ID,
			VariantName: vc.Name,
			Path:        variantRelPath,
			MimeType:    "image/jpeg",
			Width:       vc.Width,
			Height:      vc.Height,
			Size:        fi.Size(),
		})
	}

	return variants, nil
}

// ============================================
// Helpers
// ============================================

// getImageDimensions uses ffprobe to get image dimensions
func (s *VideoProcessService) getImageDimensions(fullPath string) (*models.Dimensions, error) {
	cmd := exec.Command(s.ffprobePath,
		"-v", "quiet",
		"-print_format", "json",
		"-show_streams",
		fullPath,
	)

	output, err := cmd.Output()
	if err != nil {
		return nil, err
	}

	var probe ffprobeOutput
	if err := json.Unmarshal(output, &probe); err != nil {
		return nil, err
	}

	for _, stream := range probe.Streams {
		if stream.Width > 0 && stream.Height > 0 {
			return &models.Dimensions{Width: stream.Width, Height: stream.Height}, nil
		}
	}

	return nil, fmt.Errorf("no dimensions found")
}

// parseFraction parses a fraction string like "30/1" or "30000/1001" to a float64
func parseFraction(s string) float64 {
	if s == "" || s == "0/0" {
		return 0
	}
	parts := strings.Split(s, "/")
	if len(parts) != 2 {
		f, _ := strconv.ParseFloat(s, 64)
		return f
	}
	num, _ := strconv.ParseFloat(parts[0], 64)
	den, _ := strconv.ParseFloat(parts[1], 64)
	if den == 0 {
		return 0
	}
	return num / den
}

// formatTimestamp converts seconds to HH:MM:SS.mmm format
func formatTimestamp(seconds float64) string {
	h := int(seconds) / 3600
	m := (int(seconds) % 3600) / 60
	s := seconds - float64(h*3600+m*60)
	return fmt.Sprintf("%02d:%02d:%06.3f", h, m, s)
}
