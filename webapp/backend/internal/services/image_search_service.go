package services

import (
	"encoding/json"
	"fmt"
	"io"
	"math/rand"
	"net/http"
	"net/url"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/google/uuid"
	"go.uber.org/zap"
)

// viToEnKeywords maps Vietnamese keywords commonly used on lichso.vn to
// English search terms that produce relevant stock photos on Pexels.
// Keys are lowercase, diacritics-stripped Vietnamese words/phrases.
var viToEnKeywords = map[string]string{
	// Phong thủy
	"phong thuy":   "feng shui",
	"phong thủy":   "feng shui",
	"phong thuỷ":   "feng shui",
	"nha o":        "house interior",
	"nhà ở":        "house interior",
	"phong ngu":    "bedroom interior",
	"phòng ngủ":    "bedroom interior",
	"phong khach":  "living room",
	"phòng khách":  "living room",
	"phong bep":    "kitchen interior",
	"phòng bếp":    "kitchen interior",
	"nha bep":      "kitchen",
	"nhà bếp":      "kitchen",
	"ban lam viec": "office desk",
	"bàn làm việc": "office desk workspace",
	"van phong":    "office",
	"văn phòng":    "office",
	"cua chinh":    "front door house",
	"cửa chính":    "front door house",
	"cau thang":    "staircase",
	"cầu thang":    "staircase",
	"san vuon":     "garden",
	"sân vườn":     "garden",
	"ho ca":        "koi pond",
	"hồ cá":        "koi pond",
	"be ca":        "fish aquarium",
	"bể cá":        "fish aquarium",
	"ban tho":      "altar incense",
	"bàn thờ":      "altar incense",
	"tuong phat":   "buddha statue",
	"tượng phật":   "buddha statue",

	// Tử vi / Cung hoàng đạo
	"tu vi":          "horoscope zodiac",
	"tử vi":          "horoscope zodiac",
	"cung hoang dao": "zodiac signs",
	"cung hoàng đạo": "zodiac signs",
	"bach duong":     "aries zodiac",
	"bạch dương":     "aries zodiac",
	"kim nguu":       "taurus zodiac",
	"kim ngưu":       "taurus zodiac",
	"song tu":        "gemini zodiac",
	"song tử":        "gemini zodiac",
	"cu giai":        "cancer zodiac",
	"cự giải":        "cancer zodiac",
	"su tu":          "leo zodiac",
	"sư tử":          "leo zodiac",
	"xu nu":          "virgo zodiac",
	"xử nữ":          "virgo zodiac",
	"thien binh":     "libra zodiac",
	"thiên bình":     "libra zodiac",
	"thien yet":      "scorpio zodiac",
	"thiên yết":      "scorpio zodiac",
	"nhan ma":        "sagittarius zodiac",
	"nhân mã":        "sagittarius zodiac",
	"ma ket":         "capricorn zodiac",
	"ma kết":         "capricorn zodiac",
	"bao binh":       "aquarius zodiac",
	"bảo bình":       "aquarius zodiac",
	"song ngu":       "pisces zodiac",
	"song ngư":       "pisces zodiac",

	// 12 Con giáp
	"con giap":  "chinese zodiac",
	"con giáp":  "chinese zodiac",
	"tuoi ty":   "rat chinese zodiac",
	"tuổi tý":   "rat chinese zodiac",
	"tuoi suu":  "ox chinese zodiac",
	"tuổi sửu":  "ox chinese zodiac",
	"tuoi dan":  "tiger chinese zodiac",
	"tuổi dần":  "tiger chinese zodiac",
	"tuoi mao":  "cat rabbit",
	"tuổi mão":  "cat rabbit",
	"tuoi thin": "dragon chinese zodiac",
	"tuổi thìn": "dragon chinese zodiac",
	"tuoi ti":   "snake",
	"tuổi tỵ":   "snake",
	"tuoi ngo":  "horse",
	"tuổi ngọ":  "horse",
	"tuoi mui":  "goat",
	"tuổi mùi":  "goat",
	"tuoi than": "monkey",
	"tuổi thân": "monkey",
	"tuoi dau":  "rooster chicken",
	"tuổi dậu":  "rooster chicken",
	"tuoi tuat": "dog",
	"tuổi tuất": "dog",
	"tuoi hoi":  "pig",
	"tuổi hợi":  "pig",

	// Ngũ hành
	"ngu hanh":  "five elements nature",
	"ngũ hành":  "five elements nature",
	"hanh kim":  "gold metal",
	"hành kim":  "gold metal",
	"hanh moc":  "green forest trees",
	"hành mộc":  "green forest trees",
	"hanh thuy": "water ocean",
	"hành thủy": "water ocean",
	"hanh hoa":  "fire flames",
	"hành hỏa":  "fire flames",
	"hanh tho":  "earth soil nature",
	"hành thổ":  "earth soil nature",

	// Tâm linh / Lễ hội
	"tam linh":       "spiritual meditation",
	"tâm linh":       "spiritual meditation",
	"chua":           "buddhist temple",
	"chùa":           "buddhist temple",
	"dinh":           "vietnamese temple",
	"đình":           "vietnamese temple",
	"den":            "shrine temple",
	"đền":            "shrine temple",
	"le hoi":         "vietnamese festival",
	"lễ hội":         "vietnamese festival",
	"tet":            "lunar new year",
	"tết":            "lunar new year",
	"tet nguyen dan": "lunar new year celebration",
	"tết nguyên đán": "lunar new year celebration",
	"trung thu":      "mid autumn festival lantern",
	"vu lan":         "buddhist ceremony",
	"ram thang bay":  "ghost festival",
	"rằm tháng bảy":  "ghost festival",
	"gio to":         "ancestor worship",
	"giỗ tổ":         "ancestor worship",

	// Lịch / Thời gian
	"lich":          "calendar",
	"lịch":          "calendar",
	"lich van nien": "calendar almanac",
	"lịch vạn niên": "calendar almanac",
	"lich am":       "lunar calendar moon",
	"lịch âm":       "lunar calendar moon",
	"ngay tot":      "auspicious lucky day",
	"ngày tốt":      "auspicious lucky day",
	"ngay xau":      "calendar planning",
	"ngày xấu":      "calendar planning",
	"gio tot":       "clock time",
	"giờ tốt":       "clock time",
	"xem ngay":      "calendar date planning",
	"xem ngày":      "calendar date planning",

	// Đời sống
	"suc khoe":  "health wellness",
	"sức khỏe":  "health wellness",
	"tinh yeu":  "love couple",
	"tình yêu":  "love couple",
	"hon nhan":  "wedding marriage",
	"hôn nhân":  "wedding marriage",
	"tai chinh": "finance money",
	"tài chính": "finance money",
	"tien bac":  "money wealth",
	"tiền bạc":  "money wealth",
	"su nghiep": "career business",
	"sự nghiệp": "career business",
	"cong viec": "work office",
	"công việc": "work office",
	"hoc hanh":  "education study",
	"học hành":  "education study",
	"gia dinh":  "family home",
	"gia đình":  "family home",

	// Thiên văn
	"trang":       "moon night",
	"trăng":       "moon night",
	"mat trang":   "moon",
	"mặt trăng":   "moon",
	"mat troi":    "sun sunrise",
	"mặt trời":    "sun sunrise",
	"sao":         "stars night sky",
	"nhat thuc":   "solar eclipse",
	"nhật thực":   "solar eclipse",
	"nguyet thuc": "lunar eclipse",
	"nguyệt thực": "lunar eclipse",

	// Chung
	"mau sac":     "colors palette",
	"màu sắc":     "colors palette",
	"so hoc":      "numerology numbers",
	"số học":      "numerology numbers",
	"dat ten":     "baby naming",
	"đặt tên":     "baby naming",
	"xong dat":    "new year house",
	"xông đất":    "new year house",
	"khai truong": "grand opening business",
	"khai trương": "grand opening business",
	"dong tho":    "construction groundbreaking",
	"động thổ":    "construction groundbreaking",
	"xay nha":     "house construction building",
	"xây nhà":     "house construction building",
	"chuyen nha":  "moving house",
	"chuyển nhà":  "moving house",
	"mua xe":      "car buying",
	"cuoi hoi":    "wedding ceremony",
	"cưới hỏi":    "wedding ceremony",
}

// ImageSearchService searches for stock images on Pexels and downloads them locally.
type ImageSearchService struct {
	apiKey     string
	uploadPath string
	logger     *zap.Logger
	client     *http.Client
}

// NewImageSearchService creates a new ImageSearchService.
// apiKey: Pexels API key, uploadPath: base upload directory (e.g. "./uploads").
func NewImageSearchService(apiKey, uploadPath string, logger *zap.Logger) *ImageSearchService {
	return &ImageSearchService{
		apiKey:     apiKey,
		uploadPath: uploadPath,
		logger:     logger,
		client: &http.Client{
			Timeout: 30 * time.Second,
		},
	}
}

// IsAvailable returns true if a Pexels API key is configured.
func (s *ImageSearchService) IsAvailable() bool {
	return s.apiKey != ""
}

// pexelsSearchResponse represents the Pexels /search API response.
type pexelsSearchResponse struct {
	Photos []pexelsPhoto `json:"photos"`
}

type pexelsPhoto struct {
	ID           int            `json:"id"`
	Alt          string         `json:"alt"`
	Photographer string         `json:"photographer"`
	Src          pexelsPhotoSrc `json:"src"`
}

type pexelsPhotoSrc struct {
	Original  string `json:"original"`
	Large2x   string `json:"large2x"`
	Large     string `json:"large"`
	Medium    string `json:"medium"`
	Small     string `json:"small"`
	Landscape string `json:"landscape"`
}

// SearchAndDownload searches Pexels for an image matching the query,
// downloads the best result, saves it to the uploads directory, and
// returns the relative URL path (e.g. "/api/uploads/2026/01/ai_xxxxx.jpg").
// The query (typically a Vietnamese article title) is translated to English
// keywords before calling Pexels for better results.
// Returns empty string if no image is found or any error occurs — never blocks the caller.
func (s *ImageSearchService) SearchAndDownload(query string) string {
	if !s.IsAvailable() {
		return ""
	}

	englishQuery := s.translateToEnglishKeywords(query)
	s.logger.Info("Pexels image search",
		zap.String("original", query),
		zap.String("translated", englishQuery),
	)

	imageURL := s.searchPexels(englishQuery)
	if imageURL == "" {
		return ""
	}

	localPath, err := s.downloadImage(imageURL)
	if err != nil {
		s.logger.Warn("Failed to download image from Pexels",
			zap.String("url", imageURL),
			zap.Error(err),
		)
		return ""
	}

	return "/api/uploads/" + localPath
}

// translateToEnglishKeywords converts a Vietnamese article title/topic into
// English search keywords suitable for Pexels stock photo search.
// It scans the input for known Vietnamese phrases and maps them to English.
func (s *ImageSearchService) translateToEnglishKeywords(viText string) string {
	lower := strings.ToLower(strings.TrimSpace(viText))

	// Collect all matched English keywords, longest match first
	var matched []string
	remaining := lower

	// Try longest phrases first (sort by descending key length)
	type kv struct {
		k string
		v string
	}
	var sortedKV []kv
	for k, v := range viToEnKeywords {
		sortedKV = append(sortedKV, kv{k, v})
	}
	// Sort descending by key length for longest-match-first
	for i := 0; i < len(sortedKV); i++ {
		for j := i + 1; j < len(sortedKV); j++ {
			if len(sortedKV[j].k) > len(sortedKV[i].k) {
				sortedKV[i], sortedKV[j] = sortedKV[j], sortedKV[i]
			}
		}
	}

	seen := map[string]bool{}
	for _, pair := range sortedKV {
		if strings.Contains(remaining, pair.k) {
			// Avoid duplicate English terms
			for _, word := range strings.Fields(pair.v) {
				if !seen[word] {
					seen[word] = true
				}
			}
			matched = append(matched, pair.v)
			// Remove matched part to avoid double-matching
			remaining = strings.Replace(remaining, pair.k, " ", 1)
		}
	}

	if len(matched) > 0 {
		// Combine unique matched terms, limit to ~4 keywords for best Pexels results
		result := strings.Join(matched, " ")
		words := strings.Fields(result)
		// Deduplicate
		var unique []string
		dedupSeen := map[string]bool{}
		for _, w := range words {
			if !dedupSeen[w] {
				dedupSeen[w] = true
				unique = append(unique, w)
			}
		}
		if len(unique) > 5 {
			unique = unique[:5]
		}
		return strings.Join(unique, " ")
	}

	// Fallback: if no Vietnamese keywords matched, use original query
	// (might work for already-English queries or generic terms)
	return viText
}

// searchPexels calls Pexels API and returns the URL of the best matching image.
func (s *ImageSearchService) searchPexels(query string) string {
	// Use landscape orientation and large size suitable for featured images
	reqURL := fmt.Sprintf(
		"https://api.pexels.com/v1/search?query=%s&per_page=5&orientation=landscape&size=large",
		url.QueryEscape(query),
	)

	req, err := http.NewRequest("GET", reqURL, nil)
	if err != nil {
		s.logger.Warn("Failed to create Pexels request", zap.Error(err))
		return ""
	}
	req.Header.Set("Authorization", s.apiKey)

	resp, err := s.client.Do(req)
	if err != nil {
		s.logger.Warn("Pexels API request failed", zap.Error(err))
		return ""
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		s.logger.Warn("Pexels API returned non-200",
			zap.Int("status", resp.StatusCode),
			zap.String("query", query),
		)
		return ""
	}

	var result pexelsSearchResponse
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		s.logger.Warn("Failed to decode Pexels response", zap.Error(err))
		return ""
	}

	if len(result.Photos) == 0 {
		s.logger.Info("No Pexels images found", zap.String("query", query))
		return ""
	}

	// Pick a random photo from results for variety across articles
	idx := rand.Intn(len(result.Photos))
	photo := result.Photos[idx]
	imgURL := photo.Src.Landscape
	if imgURL == "" {
		imgURL = photo.Src.Large
	}
	if imgURL == "" {
		imgURL = photo.Src.Original
	}

	s.logger.Info("Pexels image selected",
		zap.Int("photo_id", photo.ID),
		zap.String("alt", photo.Alt),
		zap.String("photographer", photo.Photographer),
	)

	return imgURL
}

// downloadImage fetches the image from the URL and saves it to the uploads directory.
// Returns the relative path within the uploads directory (e.g. "2026/01/ai_xxxx.jpg").
func (s *ImageSearchService) downloadImage(imageURL string) (string, error) {
	resp, err := s.client.Get(imageURL)
	if err != nil {
		return "", fmt.Errorf("download image: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("download image: status %d", resp.StatusCode)
	}

	// Determine extension from URL or Content-Type
	ext := s.detectExtension(imageURL, resp.Header.Get("Content-Type"))

	// Create folder structure: YYYY/MM
	folderPath := time.Now().Format("2006/01")
	uploadDir := filepath.Join(s.uploadPath, folderPath)
	if err := os.MkdirAll(uploadDir, 0755); err != nil {
		return "", fmt.Errorf("create upload dir: %w", err)
	}

	// Generate unique filename
	filename := fmt.Sprintf("ai_%s_%s%s",
		time.Now().Format("20060102150405"),
		uuid.New().String()[:8],
		ext,
	)

	fullPath := filepath.Join(uploadDir, filename)
	out, err := os.Create(fullPath)
	if err != nil {
		return "", fmt.Errorf("create file: %w", err)
	}
	defer out.Close()

	if _, err := io.Copy(out, resp.Body); err != nil {
		os.Remove(fullPath)
		return "", fmt.Errorf("write file: %w", err)
	}

	// Return relative path
	return filepath.Join(folderPath, filename), nil
}

// detectExtension tries to determine file extension from the URL path or Content-Type.
func (s *ImageSearchService) detectExtension(imageURL, contentType string) string {
	// Try from URL path first
	parsed, err := url.Parse(imageURL)
	if err == nil {
		ext := filepath.Ext(parsed.Path)
		if ext != "" && len(ext) <= 5 {
			return ext
		}
	}

	// Fallback to Content-Type
	switch {
	case strings.Contains(contentType, "png"):
		return ".png"
	case strings.Contains(contentType, "webp"):
		return ".webp"
	case strings.Contains(contentType, "gif"):
		return ".gif"
	default:
		return ".jpg"
	}
}
