package middleware

import (
	"fmt"
	"runtime/debug"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/config"
	"go.uber.org/zap"
)

// ErrorTracker provides structured error reporting for production.
// To integrate with Sentry or similar services, uncomment the Sentry SDK import
// and init in InitErrorTracking, then use CaptureError to send errors.
//
// Install: go get github.com/getsentry/sentry-go
//
// Example initialization in main.go:
//
//	middleware.InitErrorTracking(cfg)
//	defer middleware.FlushErrorTracking()

// InitErrorTracking initializes error tracking service.
// Uncomment the Sentry code below when ready to integrate.
func InitErrorTracking(cfg *config.Config) {
	if !cfg.IsProduction() {
		return
	}

	// Uncomment when Sentry DSN is configured:
	//
	// import sentry "github.com/getsentry/sentry-go"
	//
	// err := sentry.Init(sentry.ClientOptions{
	// 	Dsn:              os.Getenv("SENTRY_DSN"),
	// 	Environment:      cfg.App.Env,
	// 	Release:          "zplus-base@1.0.0",
	// 	TracesSampleRate: 0.2,
	// 	EnableTracing:    true,
	// })
	// if err != nil {
	// 	fmt.Printf("Sentry init failed: %v\n", err)
	// }
	fmt.Println("📊 Error tracking initialized (production mode)")
}

// FlushErrorTracking flushes any pending error reports.
func FlushErrorTracking() {
	// Uncomment when Sentry is integrated:
	// sentry.Flush(2 * time.Second)
	_ = time.Second // placeholder
}

// CaptureError logs an error and sends it to the error tracking service.
func CaptureError(err error, logger *zap.Logger, extras ...map[string]interface{}) {
	if err == nil {
		return
	}

	logger.Error("Captured error",
		zap.Error(err),
		zap.String("stack", string(debug.Stack())),
	)

	// Uncomment when Sentry is integrated:
	// sentry.WithScope(func(scope *sentry.Scope) {
	// 	for _, extra := range extras {
	// 		for k, v := range extra {
	// 			scope.SetExtra(k, v)
	// 		}
	// 	}
	// 	sentry.CaptureException(err)
	// })
}

// ErrorTrackingMiddleware captures unhandled panics and reports them.
func ErrorTrackingMiddleware(logger *zap.Logger) fiber.Handler {
	return func(c *fiber.Ctx) error {
		defer func() {
			if r := recover(); r != nil {
				err, ok := r.(error)
				if !ok {
					err = fmt.Errorf("%v", r)
				}

				CaptureError(err, logger, map[string]interface{}{
					"method": c.Method(),
					"path":   c.Path(),
					"ip":     c.IP(),
				})

				// Re-panic to let the recovery middleware handle the response
				panic(r)
			}
		}()

		return c.Next()
	}
}
