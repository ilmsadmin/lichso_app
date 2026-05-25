package utils

import (
	"regexp"
	"strings"
	"unicode"

	"golang.org/x/text/runes"
	"golang.org/x/text/transform"
	"golang.org/x/text/unicode/norm"
)

var (
	slugRegexp     = regexp.MustCompile(`[^a-z0-9]+`)
	slugTrimRegexp = regexp.MustCompile(`^-+|-+$`)
)

// vietnameseReplacements maps Vietnamese diacritical characters to ASCII equivalents.
var vietnameseReplacements = map[rune]string{
	'á': "a", 'à': "a", 'ả': "a", 'ã': "a", 'ạ': "a",
	'ă': "a", 'ắ': "a", 'ằ': "a", 'ẳ': "a", 'ẵ': "a", 'ặ': "a",
	'â': "a", 'ấ': "a", 'ầ': "a", 'ẩ': "a", 'ẫ': "a", 'ậ': "a",
	'đ': "d",
	'é': "e", 'è': "e", 'ẻ': "e", 'ẽ': "e", 'ẹ': "e",
	'ê': "e", 'ế': "e", 'ề': "e", 'ể': "e", 'ễ': "e", 'ệ': "e",
	'í': "i", 'ì': "i", 'ỉ': "i", 'ĩ': "i", 'ị': "i",
	'ó': "o", 'ò': "o", 'ỏ': "o", 'õ': "o", 'ọ': "o",
	'ô': "o", 'ố': "o", 'ồ': "o", 'ổ': "o", 'ỗ': "o", 'ộ': "o",
	'ơ': "o", 'ớ': "o", 'ờ': "o", 'ở': "o", 'ỡ': "o", 'ợ': "o",
	'ú': "u", 'ù': "u", 'ủ': "u", 'ũ': "u", 'ụ': "u",
	'ư': "u", 'ứ': "u", 'ừ': "u", 'ử': "u", 'ữ': "u", 'ự': "u",
	'ý': "y", 'ỳ': "y", 'ỷ': "y", 'ỹ': "y", 'ỵ': "y",
	'Á': "a", 'À': "a", 'Ả': "a", 'Ã': "a", 'Ạ': "a",
	'Ă': "a", 'Ắ': "a", 'Ằ': "a", 'Ẳ': "a", 'Ẵ': "a", 'Ặ': "a",
	'Â': "a", 'Ấ': "a", 'Ầ': "a", 'Ẩ': "a", 'Ẫ': "a", 'Ậ': "a",
	'Đ': "d",
	'É': "e", 'È': "e", 'Ẻ': "e", 'Ẽ': "e", 'Ẹ': "e",
	'Ê': "e", 'Ế': "e", 'Ề': "e", 'Ể': "e", 'Ễ': "e", 'Ệ': "e",
	'Í': "i", 'Ì': "i", 'Ỉ': "i", 'Ĩ': "i", 'Ị': "i",
	'Ó': "o", 'Ò': "o", 'Ỏ': "o", 'Õ': "o", 'Ọ': "o",
	'Ô': "o", 'Ố': "o", 'Ồ': "o", 'Ổ': "o", 'Ỗ': "o", 'Ộ': "o",
	'Ơ': "o", 'Ớ': "o", 'Ờ': "o", 'Ở': "o", 'Ỡ': "o", 'Ợ': "o",
	'Ú': "u", 'Ù': "u", 'Ủ': "u", 'Ũ': "u", 'Ụ': "u",
	'Ư': "u", 'Ứ': "u", 'Ừ': "u", 'Ử': "u", 'Ữ': "u", 'Ự': "u",
	'Ý': "y", 'Ỳ': "y", 'Ỷ': "y", 'Ỹ': "y", 'Ỵ': "y",
}

// removeVietnameseDiacritics removes Vietnamese diacritical marks.
func removeVietnameseDiacritics(s string) string {
	var builder strings.Builder
	builder.Grow(len(s))

	for _, r := range s {
		if replacement, ok := vietnameseReplacements[r]; ok {
			builder.WriteString(replacement)
		} else {
			builder.WriteRune(r)
		}
	}
	return builder.String()
}

// removeDiacritics removes any remaining Unicode diacritics using NFD normalization.
func removeDiacritics(s string) string {
	t := transform.Chain(norm.NFD, runes.Remove(runes.In(unicode.Mn)), norm.NFC)
	result, _, _ := transform.String(t, s)
	return result
}

// GenerateSlug generates a URL-friendly slug from a Vietnamese or Unicode string.
func GenerateSlug(s string) string {
	// Step 1: Lowercase
	s = strings.ToLower(s)

	// Step 2: Remove Vietnamese diacritics first (more accurate)
	s = removeVietnameseDiacritics(s)

	// Step 3: Remove any remaining Unicode diacritics
	s = removeDiacritics(s)

	// Step 4: Replace non-alphanumeric characters with hyphens
	s = slugRegexp.ReplaceAllString(s, "-")

	// Step 5: Trim leading/trailing hyphens
	s = slugTrimRegexp.ReplaceAllString(s, "")

	return s
}

// CalculateReadingTime estimates reading time in minutes based on content length.
// Average reading speed: ~200 words per minute for Vietnamese.
func CalculateReadingTime(content string) int {
	words := len(strings.Fields(content))
	minutes := words / 200
	if minutes < 1 {
		minutes = 1
	}
	return minutes
}
