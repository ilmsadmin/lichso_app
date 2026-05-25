package routes

import (
	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/handlers"
	"github.com/zplus/lichso/internal/middleware"
)

// SetupMediaV3Routes configures V3 media management routes
func SetupMediaV3Routes(
	router fiber.Router,
	authMiddleware *middleware.AuthMiddleware,
	permMiddleware *middleware.PermissionMiddleware,
	mediaV3Handler *handlers.MediaV3Handler,
) {
	// V3 Admin media routes (require authentication)
	v3media := router.Group("/admin/v3/media", authMiddleware.Authenticate())

	// Upload & Import
	v3media.Post("/upload", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.Upload)
	v3media.Post("/upload-multiple", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.UploadMultiple)
	v3media.Post("/upload-url", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.UploadFromURL)

	// Chunk Upload
	v3media.Post("/chunk/init", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.InitChunkUpload)
	v3media.Post("/chunk/:upload_id/:chunk_index", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.UploadChunk)
	v3media.Post("/chunk/:upload_id/complete", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.CompleteChunkUpload)

	// Trash
	v3media.Get("/trash", permMiddleware.RequirePermission("settings.read"), mediaV3Handler.GetTrash)
	v3media.Delete("/trash", permMiddleware.RequirePermission("settings.delete"), mediaV3Handler.EmptyTrash)

	// Stats & Analytics
	v3media.Get("/stats", permMiddleware.RequirePermission("settings.read"), mediaV3Handler.GetExtendedStats)
	v3media.Get("/analytics/duplicates", permMiddleware.RequirePermission("settings.read"), mediaV3Handler.GetDuplicates)
	v3media.Get("/analytics/unused", permMiddleware.RequirePermission("settings.read"), mediaV3Handler.GetUnused)

	// Folders
	v3media.Get("/folders/tree", permMiddleware.RequirePermission("settings.read"), mediaV3Handler.GetFolderTree)
	v3media.Post("/folders", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.CreateFolder)
	v3media.Put("/folders/:id", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.UpdateFolder)
	v3media.Delete("/folders/:id", permMiddleware.RequirePermission("settings.delete"), mediaV3Handler.DeleteFolder)
	v3media.Post("/folders/move", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.MoveMedia)

	// Albums
	v3media.Get("/albums", permMiddleware.RequirePermission("settings.read"), mediaV3Handler.ListAlbums)
	v3media.Post("/albums", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.CreateAlbum)
	v3media.Put("/albums/:id", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.UpdateAlbum)
	v3media.Delete("/albums/:id", permMiddleware.RequirePermission("settings.delete"), mediaV3Handler.DeleteAlbum)
	v3media.Get("/albums/:id/media", permMiddleware.RequirePermission("settings.read"), mediaV3Handler.GetAlbumMedia)
	v3media.Post("/albums/:id/media", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.AddMediaToAlbum)
	v3media.Delete("/albums/:id/media", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.RemoveMediaFromAlbum)

	// Attachments
	v3media.Post("/attach", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.AttachMedia)
	v3media.Post("/detach", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.DetachMedia)
	v3media.Get("/attachments/:entity_type/:entity_id", permMiddleware.RequirePermission("settings.read"), mediaV3Handler.GetEntityAttachments)

	// Media CRUD (must come after specific paths)
	v3media.Get("/", permMiddleware.RequirePermission("settings.read"), mediaV3Handler.List)
	v3media.Get("/:id", permMiddleware.RequirePermission("settings.read"), mediaV3Handler.Get)
	v3media.Put("/:id", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.Update)
	v3media.Delete("/:id", permMiddleware.RequirePermission("settings.delete"), mediaV3Handler.SoftDelete)
	v3media.Post("/:id/restore", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.Restore)
	v3media.Delete("/:id/permanent", permMiddleware.RequirePermission("settings.delete"), mediaV3Handler.PermanentDelete)

	// Image Processing
	v3media.Post("/:id/crop", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.CropImage)
	v3media.Post("/:id/resize", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.ResizeImage)
	v3media.Post("/:id/rotate", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.RotateImage)
	v3media.Put("/:id/focal-point", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.SetFocalPoint)
	v3media.Post("/:id/regenerate-variants", permMiddleware.RequirePermission("settings.update"), mediaV3Handler.RegenerateVariants)
	v3media.Get("/:id/variants", permMiddleware.RequirePermission("settings.read"), mediaV3Handler.GetVariants)
	v3media.Get("/:id/usages", permMiddleware.RequirePermission("settings.read"), mediaV3Handler.GetMediaUsages)

	// ============================================
	// Public Media Delivery (no auth)
	// ============================================
	publicMedia := router.Group("/v3/media")
	publicMedia.Get("/:id/variant/:variant_name", func(c *fiber.Ctx) error {
		// Public variant delivery endpoint — placeholder for CDN integration
		return c.SendStatus(501) // TODO: implement public variant serving
	})
}
