package utils

import (
	"regexp"
	"strings"
)

// httpHostPattern matches any http(s)://host(:port) prefix
var httpHostPattern = regexp.MustCompile(`https?://[^/]+`)

// stripHost removes the protocol+host+port prefix from a URL, leaving only the path.
// e.g. "http://localhost:8081/api/uploads/foo.png" → "/api/uploads/foo.png"
func stripHost(rawURL string) string {
	return httpHostPattern.ReplaceAllString(rawURL, "")
}

// NormalizeUploadURL converts any upload URL (absolute or relative) into a
// host-independent relative path: /api/uploads/...
// This ensures URLs stored in the database or returned in API responses never
// contain a hardcoded host, so they work regardless of the deployment environment.
func NormalizeUploadURL(rawURL string) string {
	if rawURL == "" {
		return ""
	}

	// Strip any host prefix first → "/api/uploads/..." or "/uploads/..."
	path := stripHost(rawURL)

	// Ensure /api prefix: /uploads/... → /api/uploads/...
	if strings.HasPrefix(path, "/uploads/") {
		return "/api" + path
	}

	// Already correct: /api/uploads/...
	if strings.HasPrefix(path, "/api/uploads/") {
		return path
	}

	// Not an upload URL — return as-is (could be an external URL)
	return rawURL
}

// NormalizeContentURLs fixes all upload URLs inside HTML content to use
// host-independent relative paths: /api/uploads/...
func NormalizeContentURLs(html string) string {
	if html == "" {
		return ""
	}

	// Replace absolute upload URLs (with any host) → relative path
	// e.g. http://localhost:8081/api/uploads/... → /api/uploads/...
	// e.g. http://localhost:8081/uploads/...     → /api/uploads/...
	absUploadPattern := regexp.MustCompile(`https?://[^/]+(/api)?/uploads/`)
	result := absUploadPattern.ReplaceAllString(html, "/api/uploads/")

	// Fix relative /uploads/ (without /api prefix) in src/href attributes
	result = strings.ReplaceAll(result, `src="/uploads/`, `src="/api/uploads/`)
	result = strings.ReplaceAll(result, `href="/uploads/`, `href="/api/uploads/`)

	return result
}
