# 📜 Lịch Số v3.0 — Tài Liệu Chương Trình

> **Lịch Số (曆數) v3.0** — Nền tảng nội dung văn hoá & tri thức truyền thống, nâng cấp hệ thống Media Management & mở rộng tính năng

---

## 📋 Mục Lục

1. [Tổng Quan Version 3.0](#1-tổng-quan-version-30)
2. [Hệ Thống Quản Lý Media Nâng Cao](#2-hệ-thống-quản-lý-media-nâng-cao)
3. [Tính Năng Mới v3.0 — Media & AI](#3-tính-năng-mới-v30--media--ai)
4. [Thiết Kế Cơ Sở Dữ Liệu v3.0](#4-thiết-kế-cơ-sở-dữ-liệu-v30)
5. [API Endpoints Mới v3.0](#5-api-endpoints-mới-v30)
6. [Thiết Kế Giao Diện Media Manager](#6-thiết-kế-giao-diện-media-manager)
7. [Trang Quản Trị Mở Rộng](#7-trang-quản-trị-mở-rộng)
8. [Bài Viết Liên Quan & Content Recommendation](#8-bài-viết-liên-quan--content-recommendation)
9. [Hệ Thống Lịch Nội Dung Theo Ngày (Daily Content Calendar)](#9-hệ-thống-lịch-nội-dung-theo-ngày-daily-content-calendar)
10. [Lịch Chi Tiết Nâng Cao (Enhanced Day Detail)](#10-lịch-chi-tiết-nâng-cao-enhanced-day-detail)
11. [Tính Năng Lịch Mở Rộng (Extended Calendar Features)](#11-tính-năng-lịch-mở-rộng-extended-calendar-features)
12. [Cấu Trúc Mã Nguồn Mới](#12-cấu-trúc-mã-nguồn-mới)
13. [Chiến Lược Kỹ Thuật & Performance](#13-chiến-lược-kỹ-thuật--performance)
14. [Kế Hoạch Triển Khai & Roadmap](#14-kế-hoạch-triển-khai--roadmap)

---

## 1. Tổng Quan Version 3.0

### 1.1 Tầm Nhìn

**Lịch Số v3.0** nâng cấp từ nền tảng nội dung v2.0 thành một **hệ sinh thái nội dung toàn diện**, trong đó:

- 🖼️ **Media Management nâng cao** — Quản lý ảnh, video, file chuyên nghiệp phục vụ bài viết & nội dung
- 📸 **Gallery & Album** — Tổ chức ảnh theo album, gắn với bài viết/sự kiện/lễ hội
- 🎬 **Video Management** — Hỗ trợ video embed (YouTube, Vimeo) & video upload trực tiếp
- ✂️ **Image Processing** — Resize, crop, watermark, tạo thumbnail tự động
- 🔗 **Media–Content Linking** — Liên kết media với bài viết, sự kiện, lễ hội, nhân vật
- 📊 **Media Analytics** — Thống kê dung lượng, sử dụng, media chưa dùng
- 🌐 **CDN Integration** — Tối ưu tốc độ tải ảnh/video
- 🤖 **AI-Powered Features** — Gợi ý nội dung, auto-tagging ảnh, tóm tắt bài viết
- 📖 **Bài Viết Liên Quan** — Gợi ý bài viết liên quan theo chủ đề, tags, AI embedding
- 📅 **Daily Content Calendar** — Admin gán danh ngôn, sự kiện, bài viết theo ngày cụ thể
- 🗓️ **Enhanced Day Detail** — Lịch chi tiết hiển thị nội dung phong phú: quote, sự kiện, bài viết theo ngày
- 📝 **Ghi Chú Cá Nhân** — Người dùng ghi chú, nhật ký theo ngày
- 🔔 **Nhắc Nhở Thông Minh** — Thông báo sự kiện, ngày lễ, nhắc nhở cá nhân
- 🖨️ **In Lịch & Chia Sẻ** — In lịch theo tháng/năm, chia sẻ ngày đẹp lên MXH

### 1.2 So Sánh v2.0 vs v3.0

| Khía cạnh | v2.0 (Hiện tại) | v3.0 (Mới) |
|-----------|-----------------|-------------|
| **Media** | Upload, list, delete cơ bản | Gallery, albums, image processing, video, CDN |
| **Ảnh** | Upload & lưu file gốc | + Thumbnail, resize, crop, watermark, lazy load |
| **Video** | Không hỗ trợ | Upload video + embed YouTube/Vimeo |
| **File** | Upload chung 1 folder | Folder tree, tags, metadata EXIF, phân loại tự động |
| **Tổ chức** | Folder đơn giản | Albums, collections, smart folders, tags |
| **Liên kết** | URL thủ công | Media picker tích hợp, liên kết 2 chiều |
| **SEO ảnh** | Alt text cơ bản | + Caption, credit, EXIF, schema.org ImageObject |
| **Performance** | Serve file gốc | + WebP auto-convert, CDN, responsive images |
| **Admin UX** | Grid cơ bản | Drag & drop, bulk operations, inline edit, preview |
| **AI** | Không có | Auto-tagging, image description, content suggestion |
| **Bài viết** | Liệt kê bài viết cơ bản | + Bài viết liên quan, recommendation engine |
| **Lịch chi tiết** | Chỉ ngày, can chi, phong thuỷ | + Quote, sự kiện lịch sử, bài viết theo ngày |
| **Admin nội dung** | CRUD cơ bản | + Lịch nội dung, gán nội dung theo ngày |
| **Cá nhân hoá** | Không có | Ghi chú, bookmark, nhắc nhở, in lịch |

### 1.3 Mục Tiêu v3.0

| Mục tiêu | KPI |
|-----------|-----|
| **Media management chuyên nghiệp** | Hỗ trợ 10,000+ files, 50GB+ storage |
| **Image loading speed** | < 500ms cho ảnh thumbnail, < 2s cho full-size |
| **Upload UX** | Drag & drop, batch upload < 3s/file |
| **Video integration** | 100+ embedded videos, 50+ uploaded videos |
| **Media–Content linking** | 100% bài viết có media gắn kèm |
| **Storage optimization** | Giảm 40% dung lượng qua WebP + resize |
| **AI features** | Auto-tag 80%+ ảnh chính xác |
| **User engagement** | Thời gian trung bình > 5 phút/session |
| **Bài viết liên quan** | CTR > 15% từ gợi ý bài viết liên quan |
| **Daily content coverage** | 365 ngày/năm đều có nội dung (quote + sự kiện) |
| **Lịch chi tiết engagement** | > 30% người dùng tương tác với nội dung ngày |

---

## 2. Hệ Thống Quản Lý Media Nâng Cao

### 2.1 Kiến Trúc Media System v3.0

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        MEDIA SYSTEM v3.0                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────┐    ┌───────────────┐    ┌──────────────────┐          │
│  │   UPLOAD      │    │  PROCESSING   │    │    STORAGE       │          │
│  │   LAYER       │    │  PIPELINE     │    │    LAYER         │          │
│  ├──────────────┤    ├───────────────┤    ├──────────────────┤          │
│  │ • Single     │    │ • Resize      │    │ • Local FS       │          │
│  │ • Multiple   │───▶│ • Crop        │───▶│ • S3 (Optional)  │          │
│  │ • Drag&Drop  │    │ • WebP Conv.  │    │ • CDN            │          │
│  │ • Paste      │    │ • Thumbnail   │    │ • Backup         │          │
│  │ • URL Import │    │ • Watermark   │    │                  │          │
│  │ • Chunk Upl. │    │ • EXIF Extract│    │                  │          │
│  └──────────────┘    │ • AI Tagging  │    └──────────────────┘          │
│                       └───────────────┘                                  │
│                                                                         │
│  ┌──────────────┐    ┌───────────────┐    ┌──────────────────┐          │
│  │   ORGANIZE    │    │   LINKING      │    │   DELIVERY       │          │
│  ├──────────────┤    ├───────────────┤    ├──────────────────┤          │
│  │ • Folders    │    │ • Articles    │    │ • Responsive     │          │
│  │ • Albums     │    │ • Events      │    │ • Lazy Loading   │          │
│  │ • Tags       │    │ • Festivals   │    │ • CDN Cache      │          │
│  │ • Smart Flt. │    │ • People      │    │ • WebP Fallback  │          │
│  │ • Search     │    │ • Quotes      │    │ • Srcset         │          │
│  │ • Favorites  │    │ • User Avatar │    │ • Blur Placeholder│         │
│  └──────────────┘    └───────────────┘    └──────────────────┘          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.2 📸 Quản Lý Ảnh (Image Management)

#### Hiện trạng v2.0
- Upload file gốc, lưu vào `uploads/{year}/{month}/{filename}`
- Metadata cơ bản: filename, mime_type, size, extension, alt, description
- Folder đơn giản (string)
- Không resize, không thumbnail, không WebP convert

#### Nâng cấp v3.0

| Tính năng | Mô tả |
|-----------|--------|
| **Auto-thumbnail** | Tự động tạo thumbnail (150x150, 300x300, 600x400) khi upload |
| **Responsive variants** | Tạo nhiều kích thước: small (320w), medium (768w), large (1200w), original |
| **WebP auto-convert** | Tự động convert sang WebP (giảm ~30% dung lượng), giữ file gốc |
| **Image crop** | Crop ảnh online (tỷ lệ 1:1, 16:9, 4:3, custom) |
| **Watermark** | Đóng watermark logo "Lịch Số" cho ảnh public |
| **EXIF extraction** | Đọc metadata EXIF: camera, date taken, GPS, dimensions |
| **Blur placeholder** | Tạo BlurHash/Base64 tiny preview cho lazy loading |
| **Focal point** | Chọn điểm focus của ảnh để crop thông minh |
| **Image editor** | Rotate, flip, brightness, contrast cơ bản |

#### Kích thước ảnh tự động tạo

| Tên variant | Width | Height | Sử dụng |
|-------------|-------|--------|---------|
| `thumb_sm` | 150 | 150 | Grid thumbnail, admin list |
| `thumb_md` | 300 | 300 | Card image, preview |
| `thumb_lg` | 600 | 400 | Article card, featured grid |
| `medium` | 768 | auto | Article content, tablet |
| `large` | 1200 | auto | Full-width article, desktop |
| `og` | 1200 | 630 | Open Graph / Social share |
| `original` | - | - | File gốc, download |

#### Dữ liệu mẫu — Image variants

```json
{
  "id": "media-uuid-001",
  "original_name": "den-hung-panorama.jpg",
  "mime_type": "image/jpeg",
  "size": 2458624,
  "dimensions": { "width": 4032, "height": 3024 },
  "exif": {
    "camera": "iPhone 15 Pro",
    "date_taken": "2026-03-10T08:30:00Z",
    "gps": { "lat": 21.1283, "lng": 105.2856 },
    "iso": 100,
    "aperture": "f/1.78"
  },
  "variants": {
    "thumb_sm": { "url": "/media/2026/03/xxx_150x150.webp", "size": 8420 },
    "thumb_md": { "url": "/media/2026/03/xxx_300x300.webp", "size": 24580 },
    "thumb_lg": { "url": "/media/2026/03/xxx_600x400.webp", "size": 62340 },
    "medium": { "url": "/media/2026/03/xxx_768.webp", "size": 98200 },
    "large": { "url": "/media/2026/03/xxx_1200.webp", "size": 185600 },
    "og": { "url": "/media/2026/03/xxx_1200x630.webp", "size": 142000 },
    "original": { "url": "/media/2026/03/xxx_original.jpg", "size": 2458624 }
  },
  "blur_hash": "L6PZfSi_.AyE_3t7t7R**0o#DgR4",
  "focal_point": { "x": 0.5, "y": 0.3 },
  "dominant_color": "#2d5a1e"
}
```

### 2.3 🎬 Quản Lý Video (Video Management)

#### Tính năng chi tiết

| Tính năng | Mô tả |
|-----------|--------|
| **Video embed** | Nhúng video từ YouTube, Vimeo qua URL (không cần upload) |
| **Video upload** | Upload video trực tiếp (MP4, WebM, tối đa 500MB) |
| **Chunk upload** | Upload video lớn theo từng phần (chunk upload 5MB/chunk) |
| **Auto thumbnail** | Tự động tạo poster/thumbnail từ frame đầu tiên |
| **Video metadata** | Duration, resolution, bitrate, codec |
| **Transcoding** | Convert sang MP4 H.264 (web-friendly) — Phase sau |
| **Subtitle** | Hỗ trợ file phụ đề VTT/SRT (Phase sau) |

#### Loại video hỗ trợ

| Nguồn | Format | Max Size | Ghi chú |
|-------|--------|----------|---------|
| **YouTube** | Embed (iframe) | — | Chỉ lưu URL + metadata |
| **Vimeo** | Embed (iframe) | — | Chỉ lưu URL + metadata |
| **Upload trực tiếp** | MP4, WebM, MOV | 500MB | Chunk upload |
| **Tiktok** (Phase sau) | Embed | — | URL embed |

#### Dữ liệu mẫu — Video

```json
{
  "id": "media-uuid-002",
  "media_type": "video",
  "source_type": "youtube",
  "original_name": "Le-hoi-Den-Hung-2026.mp4",
  "source_url": "https://www.youtube.com/watch?v=abc123",
  "embed_code": "<iframe src='https://www.youtube.com/embed/abc123'></iframe>",
  "thumbnail_url": "/media/videos/thumbs/abc123.webp",
  "duration": 324,
  "resolution": "1920x1080",
  "title": "Lễ hội Đền Hùng 2026 — Giỗ Tổ Hùng Vương",
  "description": "Video tổng hợp lễ hội Đền Hùng...",
  "tags": ["đền hùng", "lễ hội", "phú thọ"]
}
```

### 2.4 📁 Quản Lý File & Tài Liệu (Document Management)

#### Nâng cấp từ v2.0

| Tính năng | v2.0 | v3.0 |
|-----------|------|------|
| **File types** | Image, PDF | + Video, Audio, Document (DOC, XLSX, PPTX), Archive (ZIP) |
| **Folder** | String đơn giản | Folder tree lồng nhau, virtual folders |
| **Search** | Theo tên file | + Full-text search nội dung PDF, metadata |
| **Preview** | Không | PDF viewer, image lightbox, video player |
| **Versioning** | Không | Lưu history phiên bản file (replace) |
| **Trash** | Xoá vĩnh viễn | Thùng rác (soft delete, khôi phục 30 ngày) |
| **Duplicate** | Không phát hiện | Phát hiện file trùng lặp bằng hash MD5/SHA256 |

#### Loại file hỗ trợ

| Nhóm | Extensions | Max Size | MIME Types |
|------|-----------|----------|------------|
| **Ảnh** | jpg, jpeg, png, gif, webp, svg, bmp, tiff | 20MB | image/* |
| **Video** | mp4, webm, mov, avi | 500MB | video/* |
| **Audio** | mp3, wav, ogg, aac | 50MB | audio/* |
| **Tài liệu** | pdf, doc, docx, xls, xlsx, pptx, txt, md | 30MB | application/* |
| **Nén** | zip, rar, 7z, tar.gz | 100MB | application/* |

### 2.5 🏷️ Hệ Thống Tags & Smart Folders

#### Media Tags
Mỗi media file có thể được gắn nhiều tags để dễ tìm kiếm:

```json
{
  "tags": ["lễ hội", "đền hùng", "phú thọ", "2026", "panorama"],
  "auto_tags": ["outdoor", "temple", "crowd", "daytime"],
  "colors": ["#2d5a1e", "#8b4513", "#f5deb3"]
}
```

#### Smart Folders (Virtual Folders)
Tự động nhóm media theo điều kiện:

| Smart Folder | Điều kiện tự động |
|-------------|-------------------|
| 📸 Tất cả ảnh | `mime_type LIKE 'image/%'` |
| 🎬 Tất cả video | `mime_type LIKE 'video/%'` |
| 📄 Tài liệu | `mime_type LIKE 'application/%'` |
| ⭐ Yêu thích | `is_favorite = true` |
| 🗑️ Thùng rác | `deleted_at IS NOT NULL` |
| 📅 Hôm nay | `created_at >= today` |
| 📅 Tuần này | `created_at >= this_week_start` |
| ⚠️ Chưa sử dụng | `usage_count = 0` |
| 🖼️ Chưa có alt text | `alt IS NULL OR alt = ''` |
| 📏 File lớn | `size > 5MB` |

### 2.6 🔗 Media–Content Linking System

Hệ thống liên kết 2 chiều giữa media và nội dung:

```
┌─────────────┐         ┌─────────────────────┐         ┌─────────────┐
│   MEDIA     │         │  media_attachments   │         │  CONTENT    │
│             │    1:N   │                     │    N:1   │             │
│ • Image     │◄────────│ • media_id (FK)     │────────▶│ • Article   │
│ • Video     │         │ • entity_type       │         │ • Event     │
│ • Document  │         │ • entity_id         │         │ • Festival  │
│             │         │ • attachment_type    │         │ • Person    │
│             │         │   (featured, gallery,│         │ • Quote     │
│             │         │    content, og)      │         │             │
│             │         │ • sort_order        │         │             │
│             │         │ • caption           │         │             │
│             │         │ • credit            │         │             │
└─────────────┘         └─────────────────────┘         └─────────────┘
```

#### Attachment Types

| Type | Mô tả | Ví dụ |
|------|--------|-------|
| `featured_image` | Ảnh bìa chính | Ảnh đại diện bài viết |
| `og_image` | Ảnh Open Graph | Ảnh hiển thị khi share lên Facebook |
| `gallery` | Gallery ảnh | Album ảnh lễ hội |
| `content_image` | Ảnh trong nội dung | Ảnh chèn vào body bài viết |
| `content_video` | Video trong nội dung | Video nhúng trong bài viết |
| `avatar` | Ảnh đại diện | Avatar nhân vật nổi tiếng |
| `document` | File đính kèm | PDF tài liệu tham khảo |
| `thumbnail` | Ảnh thu nhỏ | Thumbnail cho video |

### 2.7 📸 Album & Collections

#### Albums
Nhóm nhiều media lại thành album có chủ đề:

```json
{
  "id": "album-uuid-001",
  "title": "Lễ Hội Đền Hùng 2026",
  "slug": "le-hoi-den-hung-2026",
  "description": "Bộ sưu tập ảnh lễ hội Giỗ Tổ Hùng Vương năm 2026",
  "cover_media_id": "media-uuid-001",
  "media_count": 48,
  "total_size": 125829120,
  "visibility": "public",
  "entity_type": "festival",
  "entity_id": "festival-uuid-001",
  "tags": ["đền hùng", "lễ hội", "2026"],
  "created_by": "user-uuid-001",
  "created_at": "2026-04-15T10:00:00Z"
}
```

#### Collections (Bộ sưu tập)
Nhóm nhiều albums lại theo chủ đề lớn:

| Collection | Albums | Mô tả |
|-----------|--------|-------|
| 🏛️ Lễ hội Việt Nam | Album: Đền Hùng, Chùa Hương, Hội Lim... | Ảnh các lễ hội |
| 👤 Nhân vật lịch sử | Album: Thời Trần, Thời Lê, Cận đại... | Chân dung nhân vật |
| 📅 Sự kiện 2026 | Album: Tháng 1, Tháng 2, Tháng 3... | Ảnh sự kiện theo tháng |
| 🎨 Infographic | Album: Tử vi, Phong thuỷ, Tiết khí... | Ảnh infographic |

---

## 3. Tính Năng Mới v3.0 — Media & AI

### 3.1 🤖 AI-Powered Features

| Tính năng | Mô tả | Công nghệ |
|-----------|--------|-----------|
| **Auto Image Tagging** | Tự động gắn tag cho ảnh dựa trên nội dung | Google Vision API / OpenAI |
| **Image Description** | Tự động tạo alt text, mô tả ảnh | GPT-4 Vision / Claude |
| **Content Suggestion** | Gợi ý ảnh phù hợp khi viết bài | Semantic search + embedding |
| **Auto Thumbnail Crop** | Crop thông minh dựa trên focal point | Face detection + saliency |
| **Duplicate Detection** | Phát hiện ảnh giống nhau (perceptual hash) | pHash / dHash algorithm |
| **OCR** | Đọc text từ ảnh (screenshot, poster) | Google Vision / Tesseract |
| **Article Summary** | Tóm tắt bài viết tự động | GPT-4 / Claude |
| **Related Content** | Gợi ý nội dung liên quan thông minh hơn | Vector embedding search |

### 3.2 📊 Media Analytics Dashboard

| Metric | Mô tả |
|--------|--------|
| **Storage usage** | Tổng dung lượng theo loại file, theo tháng |
| **Upload trends** | Biểu đồ upload theo ngày/tuần/tháng |
| **Most viewed media** | Ảnh/video được xem nhiều nhất |
| **Unused media** | File upload nhưng chưa gắn với nội dung nào |
| **Broken links** | Media đã xoá nhưng vẫn còn reference |
| **Storage optimization** | Tiết kiệm bao nhiêu dung lượng nhờ WebP |
| **Top uploaders** | Admin/editor upload nhiều nhất |
| **Format distribution** | Phân bổ theo JPEG, PNG, WebP, PDF, MP4... |

### 3.3 🔍 Media Search Nâng Cao

| Tìm kiếm theo | Ví dụ |
|---------------|-------|
| **Tên file** | "den-hung" |
| **Tags** | tag:lễ-hội AND tag:phú-thọ |
| **Loại file** | type:image, type:video |
| **Kích thước** | size:>5MB |
| **Ngày upload** | date:2026-03, date:last-week |
| **Người upload** | uploader:admin@lichso.vn |
| **Dimensions** | width:>1920, ratio:16:9 |
| **Sử dụng** | used:true, used:false |
| **Album** | album:"den-hung-2026" |
| **AI tags** | ai:outdoor AND ai:temple |
| **Color** | color:#2d5a1e (tìm theo màu chủ đạo) |
| **EXIF** | camera:"iPhone 15 Pro" |

### 3.4 🖊️ Media Picker tích hợp

Media Picker là component dùng chung, tích hợp vào mọi form tạo/sửa nội dung:

```
┌─────────────────────────────────────────────────────────────────┐
│  🖼️ Chọn Media                                    [✕ Đóng]     │
├──────────────────┬──────────────────────────────────────────────┤
│  SIDEBAR          │  CONTENT                                     │
│                   │                                              │
│  📁 Thư mục       │  🔍 Tìm kiếm...   [Ảnh] [Video] [File]     │
│  ├─ /             │                                              │
│  ├─ /articles     │  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐  │
│  ├─ /events       │  │ 🖼️  │ │ 🖼️  │ │ 🖼️  │ │ 🎬  │ │ 🖼️  │  │
│  ├─ /festivals    │  │ ✓   │ │     │ │     │ │     │ │     │  │
│  ├─ /people       │  └─────┘ └─────┘ └─────┘ └─────┘ └─────┘  │
│  └─ /quotes       │  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐  │
│                   │  │ 📄  │ │ 🖼️  │ │ 🖼️  │ │ 🖼️  │ │ 🖼️  │  │
│  📸 Albums        │  │     │ │     │ │     │ │     │ │     │  │
│  ├─ Đền Hùng     │  └─────┘ └─────┘ └─────┘ └─────┘ └─────┘  │
│  ├─ Trung Thu    │                                              │
│  └─ Infographic  │  Hiển thị 1-10/248  [← 1 2 3 ... →]        │
│                   │                                              │
│  🏷️ Smart Filters│  ─────────────────────────────────────────── │
│  ├─ ⭐ Yêu thích  │  📤 UPLOAD MỚI                               │
│  ├─ ⚠️ Chưa dùng │  ┌──────────────────────────────────────┐    │
│  └─ 📅 Hôm nay   │  │  Kéo & thả files vào đây             │    │
│                   │  │  hoặc [Chọn file] [Paste URL]        │    │
│                   │  └──────────────────────────────────────┘    │
├──────────────────┴──────────────────────────────────────────────┤
│  Đã chọn: 1 file (den-hung-panorama.jpg)  [Huỷ] [Chọn media]  │
└─────────────────────────────────────────────────────────────────┘
```

### 3.5 ✏️ Inline Image Editor

Chỉnh sửa ảnh nhanh ngay trong admin:

```
┌─────────────────────────────────────────────────────────────────┐
│  ✏️ Chỉnh Sửa Ảnh: den-hung-panorama.jpg        [Huỷ] [Lưu]  │
├─────────────────────────────────────────────────────────────────┤
│  TOOLBAR                                                        │
│  [✂️ Crop] [↻ Rotate] [↔️ Flip] [🔲 Resize] [💧 Watermark]      │
│  [☀️ Brightness] [🔲 Contrast] [🎨 Filter]                      │
├──────────────────────────────┬──────────────────────────────────┤
│                              │  📐 CROP OPTIONS                 │
│                              │  (●) Tự do                       │
│      [Image Preview]         │  (○) 1:1 (Square)               │
│                              │  (○) 16:9 (Widescreen)          │
│      ┌──────────────┐       │  (○) 4:3 (Standard)             │
│      │ ╔══════════╗ │       │  (○) 3:2 (Photo)                │
│      │ ║  Crop    ║ │       │  (○) OG Image (1200x630)        │
│      │ ║  Area    ║ │       │                                  │
│      │ ╚══════════╝ │       │  📏 DIMENSIONS                   │
│      └──────────────┘       │  W: [1200] × H: [630]           │
│                              │                                  │
│                              │  🎯 FOCAL POINT                  │
│                              │  [Click on image to set]        │
│                              │                                  │
└──────────────────────────────┴──────────────────────────────────┘
```

### 3.6 📦 Chunk Upload cho File Lớn

Hỗ trợ upload file lớn (video, archive) theo từng chunk:

```
Upload Progress:
┌─────────────────────────────────────────────────────────────────┐
│  📦 le-hoi-den-hung-4k.mp4                                      │
│  ████████████████████████░░░░░░░░░░░  68%  (340MB / 500MB)     │
│  Chunk 68/100 • Tốc độ: 8.5 MB/s • Còn ~19 giây              │
│  [⏸ Tạm dừng]  [✕ Huỷ]                                        │
├─────────────────────────────────────────────────────────────────┤
│  📦 anh-le-hoi.zip                                               │
│  ██████████████████████████████████████████████████  100% ✅     │
│  Hoàn thành • 85.3 MB • 10 giây                                │
└─────────────────────────────────────────────────────────────────┘
```

#### Cơ chế Chunk Upload:
1. Frontend chia file thành chunks 5MB
2. Upload từng chunk kèm metadata (chunk index, total chunks, upload_id)
3. Backend lưu chunks tạm, assemble khi hoàn tất
4. Hỗ trợ resume nếu mất kết nối
5. Cleanup chunks tạm sau 24h

### 3.7 🌍 CDN Integration (Tùy chọn)

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client   │────▶│  CDN     │────▶│  Origin  │────▶│  Storage │
│  Browser  │     │ (CF/AWS) │     │  Server  │     │  (Disk)  │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
                  Cache Hit ✓      Cache Miss ✗      Source

CDN URL Pattern:
- Production:  https://cdn.lichso.vn/media/2026/03/xxx_768.webp
- Development: http://localhost:8080/api/uploads/2026/03/xxx_768.webp

Hỗ trợ:
- Cloudflare (Free tier — ưu tiên)
- AWS CloudFront (Optional)
- BunnyCDN (Giá rẻ)
```

### 3.8 🖥️ Responsive Image Delivery

Frontend sử dụng `<picture>` element với srcset:

```html
<!-- Auto-generated responsive image tag -->
<picture>
  <source
    type="image/webp"
    srcset="
      /media/2026/03/xxx_320.webp 320w,
      /media/2026/03/xxx_768.webp 768w,
      /media/2026/03/xxx_1200.webp 1200w
    "
    sizes="(max-width: 768px) 100vw, (max-width: 1200px) 768px, 1200px"
  />
  <img
    src="/media/2026/03/xxx_768.jpg"
    alt="Lễ hội Đền Hùng 2026"
    width="1200"
    height="630"
    loading="lazy"
    style="background: #2d5a1e"
    data-blurhash="L6PZfSi_.AyE_3t7t7R**0o#DgR4"
  />
</picture>
```

---

## 4. Thiết Kế Cơ Sở Dữ Liệu v3.0

### 4.1 Sơ Đồ ERD — Bảng Media Mới

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        LICHSO v3.0 — MEDIA TABLES                        │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────────────┐     ┌───────────────────────┐                  │
│  │   media (UPDATED)    │     │   media_variants       │                  │
│  ├─────────────────────┤     ├───────────────────────┤                  │
│  │ _id (PK)            │──┐  │ _id (PK)              │                  │
│  │ filename            │  │  │ media_id (FK) ────────│◄─────────────┐   │
│  │ original_name       │  │  │ variant_name          │              │   │
│  │ path                │  │  │ path                  │              │   │
│  │ mime_type           │  │  │ mime_type             │              │   │
│  │ size                │  │  │ width                 │              │   │
│  │ extension           │  │  │ height                │              │   │
│  │ media_type ← NEW    │  │  │ size                  │              │   │
│  │ source_type ← NEW   │  │  │ created_at            │              │   │
│  │ dimensions ← NEW    │  │  └───────────────────────┘              │   │
│  │ exif_data ← NEW     │  │                                         │   │
│  │ blur_hash ← NEW     │  │  ┌───────────────────────┐              │   │
│  │ focal_point ← NEW   │  │  │  media_tags            │              │   │
│  │ dominant_color ← NEW│  │  ├───────────────────────┤              │   │
│  │ duration ← NEW      │  └─▶│ media_id (FK)         │              │   │
│  │ source_url ← NEW    │     │ tag (string)           │              │   │
│  │ embed_code ← NEW    │     └───────────────────────┘              │   │
│  │ tags ← NEW          │                                             │   │
│  │ auto_tags ← NEW     │     ┌───────────────────────┐              │   │
│  │ file_hash ← NEW     │     │  media_attachments     │              │   │
│  │ usage_count ← NEW   │     ├───────────────────────┤              │   │
│  │ is_favorite ← NEW   │     │ _id (PK)              │              │   │
│  │ uploaded_by          │     │ media_id (FK) ────────│──────────────┘   │
│  │ uploaded_name        │     │ entity_type           │                  │
│  │ alt                  │     │ entity_id             │                  │
│  │ description          │     │ attachment_type       │                  │
│  │ caption ← NEW        │     │ sort_order            │                  │
│  │ credit ← NEW         │     │ caption               │                  │
│  │ folder               │     │ credit                │                  │
│  │ is_public            │     │ created_at            │                  │
│  │ deleted_at ← NEW     │     └───────────────────────┘                  │
│  │ created_at           │                                                │
│  │ updated_at           │     ┌───────────────────────┐                  │
│  └─────────────────────┘     │  media_albums          │                  │
│                               ├───────────────────────┤                  │
│                               │ _id (PK)              │                  │
│  ┌─────────────────────┐     │ title                 │                  │
│  │  media_album_items   │     │ slug                  │                  │
│  ├─────────────────────┤     │ description           │                  │
│  │ _id (PK)            │     │ cover_media_id (FK)   │                  │
│  │ album_id (FK) ──────│────▶│ media_count           │                  │
│  │ media_id (FK) ──────│──┐  │ total_size            │                  │
│  │ sort_order           │  │  │ visibility            │                  │
│  │ caption              │  │  │ entity_type           │                  │
│  │ added_at             │  │  │ entity_id             │                  │
│  └─────────────────────┘  │  │ tags                  │                  │
│                            │  │ created_by            │                  │
│                            │  │ created_at            │                  │
│                            │  │ updated_at            │                  │
│                            │  └───────────────────────┘                  │
│                            │                                             │
│  ┌─────────────────────┐  │  ┌───────────────────────┐                  │
│  │  media_folders ← NEW │  │  │  chunk_uploads ← NEW  │                  │
│  ├─────────────────────┤  │  ├───────────────────────┤                  │
│  │ _id (PK)            │  │  │ _id (PK)              │                  │
│  │ name                │  │  │ upload_id             │                  │
│  │ slug                │  │  │ filename              │                  │
│  │ parent_id (FK,self) │  │  │ total_chunks          │                  │
│  │ path (full path)    │  │  │ uploaded_chunks       │                  │
│  │ media_count         │  │  │ total_size            │                  │
│  │ total_size          │  │  │ mime_type             │                  │
│  │ icon                │  │  │ temp_path             │                  │
│  │ color               │  │  │ uploaded_by           │                  │
│  │ sort_order          │  │  │ status                │                  │
│  │ is_system           │  │  │ expires_at            │                  │
│  │ created_at          │  │  │ created_at            │                  │
│  │ updated_at          │  │  └───────────────────────┘                  │
│  └─────────────────────┘  │                                             │
│                            │  ┌───────────────────────┐                  │
│                            │  │  media_versions ← NEW │                  │
│                            │  ├───────────────────────┤                  │
│                            └─▶│ _id (PK)              │                  │
│                               │ media_id (FK)         │                  │
│                               │ version_number        │                  │
│                               │ path                  │                  │
│                               │ size                  │                  │
│                               │ changed_by            │                  │
│                               │ change_note           │                  │
│                               │ created_at            │                  │
│                               └───────────────────────┘                  │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

### 4.2 MongoDB Schema — Media (Updated)

```javascript
// Collection: media (UPDATED từ v2.0)
{
  _id: ObjectId,
  filename: "20260310_a1b2c3d4.jpg",
  original_name: "den-hung-panorama.jpg",
  path: "2026/03/20260310_a1b2c3d4.jpg",
  mime_type: "image/jpeg",
  size: 2458624,                           // bytes
  extension: "jpg",

  // ═══ NEW v3.0 Fields ═══
  media_type: "image",                      // "image", "video", "audio", "document", "archive"
  source_type: "upload",                    // "upload", "youtube", "vimeo", "url_import"

  // Image-specific
  dimensions: { width: 4032, height: 3024 },
  exif_data: {
    camera: "iPhone 15 Pro",
    date_taken: ISODate("2026-03-10T08:30:00Z"),
    gps: { lat: 21.1283, lng: 105.2856 },
    iso: 100,
    aperture: "f/1.78",
    focal_length: "24mm"
  },
  blur_hash: "L6PZfSi_.AyE_3t7t7R**0o#DgR4",
  focal_point: { x: 0.5, y: 0.3 },
  dominant_color: "#2d5a1e",

  // Video-specific
  duration: null,                           // seconds (for video/audio)
  source_url: null,                         // YouTube/Vimeo URL
  embed_code: null,                         // iframe embed code

  // Organization
  tags: ["lễ hội", "đền hùng", "phú thọ"],
  auto_tags: ["outdoor", "temple", "crowd"],  // AI-generated
  file_hash: "sha256:abc123...",             // For duplicate detection
  folder: "/festivals",                      // Folder path
  folder_id: ObjectId("..."),                // Reference to media_folders

  // Usage tracking
  usage_count: 5,                           // Number of content references
  is_favorite: false,
  is_public: true,

  // SEO & metadata
  uploaded_by: "user-uuid-001",
  uploaded_name: "Admin",
  alt: "Toàn cảnh lễ hội Đền Hùng 2026",
  description: "Ảnh chụp toàn cảnh lễ hội...",
  caption: "Lễ hội Đền Hùng — Giỗ Tổ Hùng Vương 2026",  // NEW
  credit: "Ảnh: Nguyễn Văn A / Lịch Số",                 // NEW

  // Soft delete
  deleted_at: null,                          // NEW — null = active, date = trashed

  created_at: ISODate("2026-03-10T10:00:00Z"),
  updated_at: ISODate("2026-03-10T10:00:00Z")
}
```

```javascript
// Collection: media_variants (NEW v3.0)
{
  _id: ObjectId,
  media_id: ObjectId("..."),                // Reference to parent media
  variant_name: "thumb_lg",                 // "thumb_sm", "thumb_md", "thumb_lg", "medium", "large", "og"
  path: "2026/03/20260310_a1b2c3d4_600x400.webp",
  mime_type: "image/webp",
  width: 600,
  height: 400,
  size: 62340,
  created_at: ISODate("2026-03-10T10:00:05Z")
}
```

```javascript
// Collection: media_attachments (NEW v3.0)
{
  _id: ObjectId,
  media_id: ObjectId("..."),
  entity_type: "article",                   // "article", "event", "festival", "person", "quote"
  entity_id: "uuid-or-objectid",            // ID of the linked content
  attachment_type: "featured_image",         // "featured_image", "og_image", "gallery", "content_image", etc.
  sort_order: 0,
  caption: "Toàn cảnh lễ hội",
  credit: "Ảnh: Lịch Số",
  created_at: ISODate("2026-03-10T10:00:00Z")
}
```

```javascript
// Collection: media_albums (NEW v3.0)
{
  _id: ObjectId,
  title: "Lễ Hội Đền Hùng 2026",
  slug: "le-hoi-den-hung-2026",
  description: "Bộ sưu tập ảnh lễ hội...",
  cover_media_id: ObjectId("..."),
  media_count: 48,
  total_size: 125829120,
  visibility: "public",                     // "public", "private", "unlisted"
  entity_type: "festival",                  // Optional: link to content
  entity_id: "festival-uuid-001",
  tags: ["đền hùng", "lễ hội"],
  created_by: "user-uuid-001",
  created_at: ISODate("2026-04-15T10:00:00Z"),
  updated_at: ISODate("2026-04-15T10:00:00Z")
}
```

```javascript
// Collection: media_album_items (NEW v3.0)
{
  _id: ObjectId,
  album_id: ObjectId("..."),
  media_id: ObjectId("..."),
  sort_order: 1,
  caption: "Đoàn rước kiệu...",
  added_at: ISODate("2026-04-15T10:05:00Z")
}
```

```javascript
// Collection: media_folders (NEW v3.0)
{
  _id: ObjectId,
  name: "Lễ hội",
  slug: "le-hoi",
  parent_id: null,                          // null = root folder
  path: "/le-hoi",                          // Full computed path
  media_count: 256,
  total_size: 524288000,
  icon: "🎎",
  color: "#FF6B35",
  sort_order: 3,
  is_system: false,                         // true = cannot delete (e.g., /articles, /avatars)
  created_at: ISODate("2026-01-01T00:00:00Z"),
  updated_at: ISODate("2026-03-10T10:00:00Z")
}
```

```javascript
// Collection: chunk_uploads (NEW v3.0)
{
  _id: ObjectId,
  upload_id: "upload-uuid-001",             // Unique upload session ID
  filename: "le-hoi-den-hung-4k.mp4",
  total_chunks: 100,
  uploaded_chunks: [0, 1, 2, ..., 67],      // Array of completed chunk indices
  total_size: 524288000,                    // Expected total size
  mime_type: "video/mp4",
  temp_path: "/tmp/uploads/upload-uuid-001/",
  uploaded_by: "user-uuid-001",
  folder: "/videos",
  status: "uploading",                      // "uploading", "assembling", "completed", "failed", "expired"
  expires_at: ISODate("2026-03-11T10:00:00Z"),  // Auto-cleanup after 24h
  created_at: ISODate("2026-03-10T10:00:00Z")
}
```

```javascript
// Collection: media_versions (NEW v3.0)
{
  _id: ObjectId,
  media_id: ObjectId("..."),
  version_number: 2,
  path: "2026/03/20260310_a1b2c3d4_v2.jpg",
  size: 2345678,
  changed_by: "user-uuid-001",
  change_note: "Crop lại ảnh tỷ lệ 16:9",
  created_at: ISODate("2026-03-10T14:30:00Z")
}
```

### 4.3 Redis Keys (Bổ sung v3.0)

```
# Media cache
cache:media:{id}                        → JSON object (TTL: 30m)
cache:media:{id}:variants              → JSON array of variants (TTL: 1h)
cache:media:folder:{path}:list:{page}  → JSON array (TTL: 5m)
cache:media:album:{slug}               → JSON object (TTL: 30m)
cache:media:stats                       → JSON object (TTL: 5m)

# Chunk upload tracking
chunk:upload:{upload_id}               → JSON object (TTL: 24h)
chunk:upload:{upload_id}:chunks        → Bitmap of completed chunks (TTL: 24h)

# Image processing queue
queue:image:process                    → List of media IDs to process
queue:image:process:status:{id}        → "pending" | "processing" | "done" | "failed"

# Media search cache
cache:media:search:{queryHash}         → JSON array (TTL: 5m)

# Duplicate detection
hash:media:file:{sha256}              → media_id (permanent)
hash:media:phash:{perceptual_hash}    → media_id (permanent)
```

---

## 5. API Endpoints Mới v3.0

### 5.1 Media API — Nâng cấp

#### Upload & Processing

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| POST | `/api/v3/admin/media/upload` | Upload file (nâng cấp: auto-process) | ✅ Admin |
| POST | `/api/v3/admin/media/upload-multiple` | Upload nhiều files | ✅ Admin |
| POST | `/api/v3/admin/media/upload-url` | Import từ URL | ✅ Admin |
| POST | `/api/v3/admin/media/upload-chunk/init` | Khởi tạo chunk upload | ✅ Admin |
| POST | `/api/v3/admin/media/upload-chunk/:uploadId` | Upload 1 chunk | ✅ Admin |
| POST | `/api/v3/admin/media/upload-chunk/:uploadId/complete` | Hoàn thành chunk upload | ✅ Admin |
| DELETE | `/api/v3/admin/media/upload-chunk/:uploadId` | Huỷ chunk upload | ✅ Admin |

#### Media CRUD (Nâng cấp)

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| GET | `/api/v3/admin/media` | List media (nâng cấp: advanced search) | ✅ Admin |
| GET | `/api/v3/admin/media/:id` | Chi tiết media (kèm variants) | ✅ Admin |
| PUT | `/api/v3/admin/media/:id` | Update metadata (nâng cấp: thêm fields) | ✅ Admin |
| DELETE | `/api/v3/admin/media/:id` | Soft delete (vào thùng rác) | ✅ Admin |
| POST | `/api/v3/admin/media/:id/restore` | Khôi phục từ thùng rác | ✅ Admin |
| DELETE | `/api/v3/admin/media/:id/permanent` | Xoá vĩnh viễn | ✅ Admin |
| POST | `/api/v3/admin/media/delete-multiple` | Soft delete nhiều files | ✅ Admin |
| GET | `/api/v3/admin/media/trash` | List media trong thùng rác | ✅ Admin |
| POST | `/api/v3/admin/media/trash/empty` | Dọn sạch thùng rác | ✅ Admin |

#### Image Processing

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| POST | `/api/v3/admin/media/:id/crop` | Crop ảnh (tỷ lệ, toạ độ) | ✅ Admin |
| POST | `/api/v3/admin/media/:id/resize` | Resize ảnh | ✅ Admin |
| POST | `/api/v3/admin/media/:id/rotate` | Rotate ảnh (90, 180, 270) | ✅ Admin |
| POST | `/api/v3/admin/media/:id/watermark` | Thêm watermark | ✅ Admin |
| POST | `/api/v3/admin/media/:id/regenerate-variants` | Tạo lại variants | ✅ Admin |
| PUT | `/api/v3/admin/media/:id/focal-point` | Cập nhật focal point | ✅ Admin |

#### Variants & Delivery

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| GET | `/api/v3/media/:id/variant/:name` | Lấy variant ảnh | ❌ Public |
| GET | `/api/v3/media/:id/download` | Download file gốc | ❌ Public |
| GET | `/api/v3/media/:id/embed` | Embed code (video) | ❌ Public |
| GET | `/api/v3/media/:id/oembed` | oEmbed metadata | ❌ Public |

#### Folders

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| GET | `/api/v3/admin/media/folders` | List folder tree | ✅ Admin |
| POST | `/api/v3/admin/media/folders` | Tạo folder | ✅ Admin |
| PUT | `/api/v3/admin/media/folders/:id` | Update folder | ✅ Admin |
| DELETE | `/api/v3/admin/media/folders/:id` | Xoá folder (chuyển files về /) | ✅ Admin |
| POST | `/api/v3/admin/media/:id/move` | Di chuyển file sang folder khác | ✅ Admin |
| POST | `/api/v3/admin/media/move-multiple` | Di chuyển nhiều files | ✅ Admin |

#### Albums

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| GET | `/api/v3/admin/media/albums` | List albums | ✅ Admin |
| POST | `/api/v3/admin/media/albums` | Tạo album | ✅ Admin |
| GET | `/api/v3/admin/media/albums/:id` | Chi tiết album (kèm media) | ✅ Admin |
| PUT | `/api/v3/admin/media/albums/:id` | Update album | ✅ Admin |
| DELETE | `/api/v3/admin/media/albums/:id` | Xoá album (không xoá media) | ✅ Admin |
| POST | `/api/v3/admin/media/albums/:id/add` | Thêm media vào album | ✅ Admin |
| POST | `/api/v3/admin/media/albums/:id/remove` | Xoá media khỏi album | ✅ Admin |
| PUT | `/api/v3/admin/media/albums/:id/reorder` | Sắp xếp lại media trong album | ✅ Admin |
| GET | `/api/v3/albums/:slug` | Public album (gallery page) | ❌ Public |

#### Media–Content Linking

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| POST | `/api/v3/admin/media/attach` | Gắn media vào content | ✅ Admin |
| DELETE | `/api/v3/admin/media/detach` | Gỡ media khỏi content | ✅ Admin |
| GET | `/api/v3/admin/media/attachments/:entityType/:entityId` | List media của 1 content | ✅ Admin |
| GET | `/api/v3/admin/media/:id/usages` | Xem media đang dùng ở đâu | ✅ Admin |

#### Analytics & Stats

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| GET | `/api/v3/admin/media/stats` | Thống kê tổng quan (nâng cấp) | ✅ Admin |
| GET | `/api/v3/admin/media/stats/storage` | Phân bổ dung lượng theo loại | ✅ Admin |
| GET | `/api/v3/admin/media/stats/upload-trends` | Xu hướng upload | ✅ Admin |
| GET | `/api/v3/admin/media/stats/unused` | List media chưa dùng | ✅ Admin |
| GET | `/api/v3/admin/media/stats/duplicates` | List media trùng lặp | ✅ Admin |

#### AI Features

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| POST | `/api/v3/admin/media/:id/ai/auto-tag` | AI auto-tag ảnh | ✅ Admin |
| POST | `/api/v3/admin/media/:id/ai/describe` | AI tạo mô tả ảnh | ✅ Admin |
| POST | `/api/v3/admin/media/:id/ai/generate-alt` | AI tạo alt text | ✅ Admin |
| GET | `/api/v3/admin/media/ai/suggest` | Gợi ý media cho bài viết | ✅ Admin |

### 5.2 Request/Response Mẫu

#### Upload với auto-processing

```
POST /api/v3/admin/media/upload
Content-Type: multipart/form-data

Body:
- file: (binary)
- folder: "/festivals"
- auto_process: true        ← NEW: auto resize + WebP convert
- generate_variants: true   ← NEW: create all size variants
- extract_exif: true        ← NEW: extract EXIF metadata
- detect_duplicates: true   ← NEW: check for duplicate files

Response 201:
{
  "success": true,
  "message": "File uploaded and processed successfully",
  "data": {
    "id": "media-id-001",
    "filename": "20260310_a1b2c3d4.jpg",
    "original_name": "den-hung-panorama.jpg",
    "url": "https://lichso.vn/api/uploads/2026/03/20260310_a1b2c3d4.jpg",
    "mime_type": "image/jpeg",
    "media_type": "image",
    "size": 2458624,
    "dimensions": { "width": 4032, "height": 3024 },
    "blur_hash": "L6PZfSi_.AyE_3t7t7R**0o#DgR4",
    "dominant_color": "#2d5a1e",
    "variants": {
      "thumb_sm": { "url": ".../_150x150.webp", "size": 8420 },
      "thumb_md": { "url": ".../_300x300.webp", "size": 24580 },
      "thumb_lg": { "url": ".../_600x400.webp", "size": 62340 },
      "medium": { "url": ".../_768.webp", "size": 98200 },
      "large": { "url": ".../_1200.webp", "size": 185600 },
      "og": { "url": ".../_1200x630.webp", "size": 142000 }
    },
    "exif_data": {
      "camera": "iPhone 15 Pro",
      "date_taken": "2026-03-10T08:30:00Z"
    },
    "processing_status": "completed",
    "folder": "/festivals",
    "is_duplicate": false,
    "created_at": "2026-03-10T10:00:00Z"
  }
}
```

#### Chunk Upload Flow

```
# Step 1: Init
POST /api/v3/admin/media/upload-chunk/init
{
  "filename": "le-hoi-4k.mp4",
  "total_size": 524288000,
  "mime_type": "video/mp4",
  "chunk_size": 5242880,
  "folder": "/videos"
}

Response 200:
{
  "success": true,
  "data": {
    "upload_id": "upload-uuid-001",
    "total_chunks": 100,
    "chunk_size": 5242880,
    "expires_at": "2026-03-11T10:00:00Z"
  }
}

# Step 2: Upload each chunk
POST /api/v3/admin/media/upload-chunk/upload-uuid-001
Content-Type: multipart/form-data
- chunk: (binary, 5MB)
- chunk_index: 0

Response 200:
{
  "success": true,
  "data": {
    "chunk_index": 0,
    "uploaded_chunks": 1,
    "total_chunks": 100,
    "progress": 1
  }
}

# Step 3: Complete
POST /api/v3/admin/media/upload-chunk/upload-uuid-001/complete

Response 201:
{
  "success": true,
  "message": "Video uploaded successfully",
  "data": {
    "id": "media-id-002",
    "media_type": "video",
    "duration": 324,
    "resolution": "3840x2160",
    "thumbnail_url": "/media/videos/thumbs/media-id-002.webp"
  }
}
```

#### Advanced Search

```
GET /api/v3/admin/media?type=image&tags=lễ+hội,đền+hùng&size_min=1048576&width_min=1920&date_from=2026-01-01&sort=size_desc&page=1&limit=24

Response 200:
{
  "success": true,
  "data": [ ... ],
  "meta": {
    "page": 1,
    "limit": 24,
    "total": 156,
    "total_pages": 7
  },
  "aggregations": {
    "total_size": 824500000,
    "type_counts": { "image": 120, "video": 25, "document": 11 },
    "folder_counts": { "/festivals": 45, "/events": 32, "/articles": 79 }
  }
}
```

---

## 6. Thiết Kế Giao Diện Media Manager

### 6.1 Media Manager Chính (`/admin/media`)

```
┌─────────────────────────────────────────────────────────────────┐
│  📁 Quản Lý Media                              [📤 Upload]     │
├──────────────────┬──────────────────────────────────────────────┤
│  SIDEBAR (20%)    │  CONTENT (80%)                               │
│                   │                                              │
│  📁 THƯ MỤC       │  TOOLBAR                                     │
│  ├─ 📁 / (root)   │  🔍 Tìm kiếm...  [Grid] [List] [Detail]    │
│  ├─ 📁 articles   │  Lọc: [Tất cả ▼] [Ảnh] [Video] [File]     │
│  ├─ 📁 events     │       [Tags ▼] [Kích thước ▼] [Ngày ▼]     │
│  ├─ 📁 festivals  │                                              │
│  │  ├─ 📁 2025    │  ═══════════════════════════════════════════ │
│  │  └─ 📁 2026    │                                              │
│  ├─ 📁 people     │  DRAG & DROP ZONE (khi kéo file vào)        │
│  ├─ 📁 quotes     │  ┌──────────────────────────────────────┐   │
│  └─ 📁 videos     │  │  📤 Kéo & thả files vào đây           │   │
│                   │  │     hoặc click để chọn file          │   │
│  [+ Thêm thư mục]│  └──────────────────────────────────────┘   │
│                   │                                              │
│  ───────────────  │  MEDIA GRID                                  │
│  📸 ALBUMS        │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐       │
│  ├─ Đền Hùng (48)│  │ 🖼️   │ │ 🖼️   │ │ 🎬   │ │ 🖼️   │       │
│  ├─ Trung Thu (32)│  │ ☐    │ │ ☐    │ │ ☐    │ │ ☐    │       │
│  └─ Infographic  │  │ 1.2MB│ │ 856KB│ │ 45MB │ │ 2.1MB│       │
│      (15)        │  └──────┘ └──────┘ └──────┘ └──────┘       │
│                   │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐       │
│  ───────────────  │  │ 📄   │ │ 🖼️   │ │ 🖼️   │ │ 🎵   │       │
│  🏷️ SMART FILTERS│  │ ☐    │ │ ☐    │ │ ☐    │ │ ☐    │       │
│  ├─ ⭐ Yêu thích  │  │ 320KB│ │ 1.8MB│ │ 945KB│ │ 4.2MB│       │
│  ├─ ⚠️ Chưa dùng │  └──────┘ └──────┘ └──────┘ └──────┘       │
│  ├─ 🗑️ Thùng rác │                                              │
│  └─ 📅 Hôm nay   │  ─────────────────────────────────────────── │
│                   │  Hiển thị 1-24/1,248  [← 1 2 3 ... 52 →]   │
│  ───────────────  │                                              │
│  📊 STORAGE       │  BULK ACTIONS (khi chọn nhiều)               │
│  Đã dùng:        │  ☑ Đã chọn 3 files                           │
│  ████████░░ 8.2GB│  [📁 Di chuyển] [🗑️ Xoá] [🏷️ Gắn tag]       │
│  / 20GB          │  [📸 Thêm vào album] [⬇️ Download]           │
└──────────────────┴──────────────────────────────────────────────┘
```

### 6.2 Media Detail Panel (Slide-over)

```
┌─────────────────────────────────────────────────────────────────┐
│  📄 Chi Tiết Media                                    [✕ Đóng] │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────┐                    │
│  │                                         │                    │
│  │           [Image Preview]               │                    │
│  │                                         │                    │
│  │     ┌─ Focal Point ●───┐               │                    │
│  │     └──────────────────┘               │                    │
│  │                                         │                    │
│  └─────────────────────────────────────────┘                    │
│  [✏️ Edit] [✂️ Crop] [↻ Rotate] [📋 Copy URL] [⬇️ Download]     │
│                                                                 │
│  ─── THÔNG TIN ─────────────────────────────────────────────── │
│  📛 Tên: den-hung-panorama.jpg                                  │
│  📦 Kích thước: 2.3 MB (4032 × 3024)                           │
│  📁 Thư mục: /festivals                                        │
│  🏷️ Loại: image/jpeg                                            │
│  📅 Upload: 10/03/2026 by Admin                                │
│  #️⃣ Hash: sha256:abc123...                                      │
│                                                                 │
│  ─── METADATA ──────────────────────────────────────────────── │
│  Alt text *                                                     │
│  [Toàn cảnh lễ hội Đền Hùng 2026                          ]   │
│                                                                 │
│  Caption                                                        │
│  [Lễ hội Đền Hùng — Giỗ Tổ Hùng Vương 2026               ]   │
│                                                                 │
│  Credit / Nguồn                                                 │
│  [Ảnh: Nguyễn Văn A / Lịch Số                             ]   │
│                                                                 │
│  Description                                                    │
│  [Ảnh chụp toàn cảnh lễ hội Đền Hùng tại Phú Thọ...      ]   │
│                                                                 │
│  Tags                                                           │
│  [lễ hội] [đền hùng] [phú thọ] [+ thêm]                      │
│                                                                 │
│  ☑ Yêu thích  ☑ Public                                         │
│                                                                 │
│  ─── VARIANTS ──────────────────────────────────────────────── │
│  thumb_sm (150×150)   — 8.2 KB   [📋]                          │
│  thumb_md (300×300)   — 24.0 KB  [📋]                          │
│  thumb_lg (600×400)   — 60.8 KB  [📋]                          │
│  medium (768w)        — 95.9 KB  [📋]                          │
│  large (1200w)        — 181.3 KB [📋]                          │
│  og (1200×630)        — 138.7 KB [📋]                          │
│  original (4032×3024) — 2.3 MB   [📋]                          │
│                                                                 │
│  ─── EXIF DATA ─────────────────────────────────────────────── │
│  📷 Camera: iPhone 15 Pro                                       │
│  📅 Chụp: 10/03/2026 08:30                                     │
│  📍 GPS: 21.1283, 105.2856 (Phú Thọ)                          │
│  ISO: 100 | f/1.78 | 24mm                                      │
│                                                                 │
│  ─── SỬ DỤNG (5 nơi) ──────────────────────────────────────── │
│  📝 Bài viết: "Lễ hội Đền Hùng 2026..." (featured_image)      │
│  📅 Sự kiện: "Giỗ Tổ Hùng Vương" (content_image)              │
│  🎎 Lễ hội: "Lễ hội Đền Hùng" (gallery)                       │
│  📸 Album: "Đền Hùng 2026" (item #12)                          │
│  📝 Bài viết: "10 lễ hội nổi tiếng..." (content_image)        │
│                                                                 │
│  ─── PHIÊN BẢN ────────────────────────────────────────────── │
│  v2 — 10/03/2026 14:30 — Crop 16:9 (by Admin)                 │
│  v1 — 10/03/2026 10:00 — Upload gốc (by Admin) [Khôi phục]   │
│                                                                 │
│  [💾 Lưu thay đổi]                    [🗑️ Xoá vào thùng rác]  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 6.3 Album Manager (`/admin/media/albums`)

```
┌─────────────────────────────────────────────────────────────────┐
│  📸 Quản Lý Albums                         [+ Tạo Album Mới]   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ALBUM GRID                                                     │
│  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐      │
│  │ ┌────────────┐ │ │ ┌────────────┐ │ │ ┌────────────┐ │      │
│  │ │            │ │ │ │            │ │ │ │            │ │      │
│  │ │  [Cover]   │ │ │ │  [Cover]   │ │ │ │  [Cover]   │ │      │
│  │ │            │ │ │ │            │ │ │ │            │ │      │
│  │ └────────────┘ │ │ └────────────┘ │ │ └────────────┘ │      │
│  │ 🏛️ Đền Hùng    │ │ 🎑 Trung Thu   │ │ 📊 Infographic │      │
│  │ 2026           │ │ 2025           │ │                │      │
│  │ 📷 48 ảnh      │ │ 📷 32 ảnh      │ │ 📷 15 ảnh      │      │
│  │ 💾 120 MB      │ │ 💾 85 MB       │ │ 💾 25 MB       │      │
│  │ 🌐 Public      │ │ 🌐 Public      │ │ 🔒 Private     │      │
│  └────────────────┘ └────────────────┘ └────────────────┘      │
│                                                                 │
│  ┌────────────────┐ ┌────────────────┐                          │
│  │ ┌────────────┐ │ │ ┌────────────┐ │                          │
│  │ │  + Tạo     │ │ │ │            │ │                          │
│  │ │  Album     │ │ │ │  [Cover]   │ │                          │
│  │ │  Mới       │ │ │ │            │ │                          │
│  │ └────────────┘ │ │ └────────────┘ │                          │
│  │                │ │ 👤 Nhân vật    │                          │
│  │                │ │ Lịch sử        │                          │
│  │                │ │ 📷 28 ảnh      │                          │
│  └────────────────┘ └────────────────┘                          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 6.4 Media Analytics (`/admin/media/analytics`)

```
┌─────────────────────────────────────────────────────────────────┐
│  📊 Media Analytics                                             │
├──────────┬──────────┬──────────┬──────────┬──────────┬──────────┤
│ 📁 1,248  │ 📸 980   │ 🎬 45    │ 📄 123   │ 💾 8.2GB │ 🔄 62%   │
│ Tổng file│ Ảnh      │ Video    │ Tài liệu │ Đã dùng  │ Đã dùng  │
│ +15 tuần │ +12 tuần │ +3 tuần  │ +5 tuần  │ / 20GB   │ (nội dung)│
├──────────┴──────────┴──────────┴──────────┴──────────┴──────────┤
│                                                                 │
│  📈 Xu Hướng Upload (30 ngày)      │  📊 Phân Bổ Dung Lượng    │
│  ┌──────────────────────────┐      │  ┌──────────────────┐      │
│  │     ╱╲    ╱╲             │      │  │ 🖼️ Ảnh     5.2 GB│      │
│  │    ╱  ╲  ╱  ╲     ╱╲    │      │  │ ████████████░░░  │      │
│  │ ──╱    ╲╱    ╲───╱  ╲── │      │  │ 🎬 Video   2.1 GB│      │
│  │ W1  W2  W3  W4  W5  W6  │      │  │ ████████░░░░░░░  │      │
│  └──────────────────────────┘      │  │ 📄 Docs    0.9 GB│      │
│                                     │  │ ███░░░░░░░░░░░░  │      │
│  ⚠️ Media Cần Chú Ý                │  └──────────────────┘      │
│  ├─ 476 files chưa sử dụng (38%)  │                             │
│  ├─ 128 files chưa có alt text    │  🏆 Top Uploaders           │
│  ├─ 12 files trùng lặp            │  1. Admin (856 files)      │
│  └─ 3 broken references           │  2. Editor01 (245 files)   │
│                                     │  3. Editor02 (147 files)   │
│  📁 Top Folders (theo dung lượng)  │                             │
│  1. /festivals — 2.8 GB (450 files)│                             │
│  2. /articles  — 2.1 GB (380 files)│                             │
│  3. /events    — 1.5 GB (220 files)│                             │
│  4. /people    — 0.9 GB (150 files)│                             │
│  5. /videos    — 0.6 GB (28 files) │                             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7. Trang Quản Trị Mở Rộng

### 7.1 Sidebar Navigation v3.0

```
┌──────────────────────────┐
│  🏠 LỊCH SỐ ADMIN v3     │
│  ─────────────────────── │
│  📊 Dashboard             │
│  ─── QUẢN LÝ CHUNG ───  │
│  👤 Người dùng            │
│  🔑 Vai trò & Quyền      │
│  ⚙️ Cài đặt               │
│  ─── MEDIA v3.0 ──────  │  ← MỚI
│  📁 Quản lý Media         │  ← NÂNG CẤP
│  │  ├─ 📸 Tất cả files   │  ← MỚI
│  │  ├─ 📁 Thư mục        │  ← MỚI
│  │  ├─ 🏷️ Albums          │  ← MỚI
│  │  ├─ 🗑️ Thùng rác      │  ← MỚI
│  │  └─ 📊 Analytics       │  ← MỚI
│  ─── NỘI DUNG v2.0 ───  │
│  📝 Bài viết (Articles)   │
│  📅 Sự kiện (Events)      │
│  🎎 Lễ hội (Festivals)    │
│  👤 Nhân vật (People)     │
│  💬 Châm ngôn (Quotes)    │
│  📂 Danh mục (Categories) │
│  🏷️ Tags                  │
│  ─── LỊCH NỘI DUNG ───  │  ← MỚI
│  📅 Lịch nội dung theo    │  ← MỚI (Daily Content Calendar)
│  │  ngày                  │
│  │  ├─ 📆 Calendar View  │  ← MỚI
│  │  ├─ 🔄 Auto-fill      │  ← MỚI
│  │  └─ 📊 Coverage Stats │  ← MỚI
│  ─── THỐNG KÊ ─────────  │
│  📈 Thống kê nội dung     │
│  📊 Thống kê media        │  ← MỚI
│  🔍 Tìm kiếm phổ biến    │
│  📧 Newsletter            │  ← MỚI
└──────────────────────────┘
```

### 7.2 RBAC Permissions Mới v3.0

| Permission | Mô tả | Roles |
|------------|--------|-------|
| `media.view` | Xem media, folders, albums | Admin, Editor |
| `media.upload` | Upload files | Admin, Editor |
| `media.edit` | Sửa metadata, crop, resize | Admin, Editor |
| `media.delete` | Soft delete (vào thùng rác) | Admin, Editor |
| `media.delete_permanent` | Xoá vĩnh viễn | Admin |
| `media.manage_folders` | CRUD folders | Admin |
| `media.manage_albums` | CRUD albums | Admin, Editor |
| `media.manage_trash` | Quản lý thùng rác, dọn sạch | Admin |
| `media.analytics` | Xem media analytics | Admin |
| `media.ai` | Sử dụng AI features | Admin |
| `media.bulk_operations` | Thao tác hàng loạt | Admin |
| `daily_content.view` | Xem lịch nội dung | Admin, Editor |
| `daily_content.manage` | CRUD nội dung theo ngày | Admin, Editor |
| `daily_content.auto_fill` | Tự động điền nội dung | Admin |
| `article_relation.manage` | Quản lý bài viết liên quan | Admin, Editor |
| `newsletter.manage` | Quản lý newsletter subscribers | Admin |

---

## 8. Bài Viết Liên Quan & Content Recommendation

### 8.1 Tổng Quan

Hệ thống gợi ý bài viết liên quan giúp người dùng khám phá thêm nội dung, tăng thời gian ở lại trang và cải thiện SEO internal linking.

#### Chiến lược gợi ý bài viết liên quan

```
┌─────────────────────────────────────────────────────────────────┐
│                 RELATED ARTICLES ENGINE                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────┐    ┌────────────────────┐                 │
│  │  RULE-BASED       │    │  AI-POWERED          │                 │
│  │  (Phase 1)        │    │  (Phase 2 — Optional) │                 │
│  ├──────────────────┤    ├────────────────────┤                 │
│  │ 1. Same Category │    │ 1. Vector Embedding│                 │
│  │ 2. Shared Tags   │    │    (OpenAI/Cohere) │                 │
│  │ 3. Same Author   │    │ 2. Semantic Search │                 │
│  │ 4. Manual Pick   │    │ 3. User Behavior   │                 │
│  │    (admin chọn)  │    │    (collaborative   │                 │
│  │ 5. Popular/       │    │     filtering)      │                 │
│  │    Trending       │    │ 4. Content-based    │                 │
│  │ 6. Same Time     │    │    Similarity        │                 │
│  │    Period         │    │                     │                 │
│  └──────────────────┘    └────────────────────┘                 │
│                                                                  │
│  ┌──────────────────────────────────────────┐                   │
│  │  OUTPUT: Sorted list of related articles  │                   │
│  │  Score = 0.4×category + 0.3×tags          │                   │
│  │        + 0.2×popularity + 0.1×recency     │                   │
│  └──────────────────────────────────────────┘                   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 8.2 Các Loại Gợi Ý Bài Viết

| Loại | Mô tả | Vị trí hiển thị | Số lượng |
|------|--------|------------------|----------|
| **Related by Category** | Cùng danh mục (VD: cùng "Lịch Sử") | Cuối bài viết | 4-6 bài |
| **Related by Tags** | Có ≥1 tag chung | Cuối bài viết | 3-5 bài |
| **Manual Related** | Admin chọn thủ công | Cuối bài viết (ưu tiên cao nhất) | 2-4 bài |
| **Popular** | Nhiều lượt xem nhất (7 ngày) | Sidebar | 5-10 bài |
| **Trending** | Tăng trưởng view nhanh nhất | Homepage, Sidebar | 5 bài |
| **Same Author** | Cùng tác giả | Cuối bài viết | 3 bài |
| **Same Period** | Cùng giai đoạn lịch sử (nếu có) | Cuối bài viết | 3 bài |
| **Random Discovery** | Ngẫu nhiên (tránh filter bubble) | Calendar Detail, Footer | 2-3 bài |

### 8.3 Database Schema — Related Articles

#### Bảng `article_relations` (PostgreSQL — MỚI)

```sql
CREATE TABLE article_relations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    related_article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    relation_type VARCHAR(30) NOT NULL DEFAULT 'manual',
        -- 'manual' (admin chọn), 'auto_category', 'auto_tag', 'auto_ai'
    relevance_score DECIMAL(3,2) DEFAULT 0.50,
        -- 0.00–1.00, dùng để sắp xếp
    sort_order INT DEFAULT 0,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_article_relation UNIQUE (article_id, related_article_id),
    CONSTRAINT chk_not_self_related CHECK (article_id <> related_article_id)
);

CREATE INDEX idx_article_relations_article ON article_relations(article_id);
CREATE INDEX idx_article_relations_related ON article_relations(related_article_id);
CREATE INDEX idx_article_relations_type ON article_relations(relation_type);
```

#### Thêm fields vào bảng `articles` (PostgreSQL)

```sql
-- Thêm field cho article embedding (AI-powered related articles)
ALTER TABLE articles ADD COLUMN embedding_vector VECTOR(1536);
    -- Dùng pgvector extension, 1536 dimensions (OpenAI ada-002)
ALTER TABLE articles ADD COLUMN embedding_updated_at TIMESTAMPTZ;

CREATE INDEX idx_articles_embedding ON articles
    USING ivfflat (embedding_vector vector_cosine_ops) WITH (lists = 100);
```

### 8.4 API Endpoints — Related Articles

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| GET | `/api/articles/:id/related` | Lấy bài viết liên quan (auto + manual) | ❌ Public |
| GET | `/api/articles/:id/related?type=manual` | Chỉ lấy bài manual | ❌ Public |
| GET | `/api/articles/popular` | Bài viết phổ biến nhất | ❌ Public |
| GET | `/api/articles/trending` | Bài viết trending | ❌ Public |
| GET | `/api/articles/random?limit=3` | Bài viết ngẫu nhiên | ❌ Public |
| POST | `/api/admin/articles/:id/related` | Admin thêm bài liên quan thủ công | ✅ Admin |
| DELETE | `/api/admin/articles/:id/related/:relatedId` | Xoá liên kết | ✅ Admin |
| PUT | `/api/admin/articles/:id/related/reorder` | Sắp xếp lại thứ tự | ✅ Admin |
| POST | `/api/admin/articles/:id/related/auto-generate` | Tự động tìm bài liên quan | ✅ Admin |

#### Response mẫu — GET `/api/articles/:id/related`

```json
{
  "success": true,
  "data": {
    "manual": [
      {
        "id": "uuid-001",
        "title": "Lễ hội Đền Hùng — Nguồn gốc và ý nghĩa",
        "slug": "le-hoi-den-hung-nguon-goc",
        "excerpt": "Tìm hiểu về lịch sử lâu đời...",
        "featured_image": "/uploads/2026/03/den-hung.webp",
        "category": { "id": "cat-01", "name": "Lịch sử" },
        "reading_time": 8,
        "view_count": 1250,
        "published_at": "2026-03-10T00:00:00Z",
        "relation_type": "manual",
        "relevance_score": 0.95
      }
    ],
    "auto": [
      {
        "id": "uuid-002",
        "title": "Vua Hùng và thời đại Hồng Bàng",
        "slug": "vua-hung-thoi-dai-hong-bang",
        "excerpt": "Khám phá lịch sử thời kỳ...",
        "featured_image": "/uploads/2026/02/hong-bang.webp",
        "category": { "id": "cat-01", "name": "Lịch sử" },
        "reading_time": 12,
        "view_count": 890,
        "published_at": "2026-02-15T00:00:00Z",
        "relation_type": "auto_category",
        "relevance_score": 0.78
      }
    ],
    "total_manual": 2,
    "total_auto": 4
  }
}
```

### 8.5 Giao Diện — Related Articles

#### 8.5.1 Cuối bài viết (Article Detail Page)

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                  │
│  [... Nội dung bài viết ...]                                    │
│                                                                  │
│  ═══════════════════════════════════════════════════════════════ │
│                                                                  │
│  📖 BÀI VIẾT LIÊN QUAN                                         │
│                                                                  │
│  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐      │
│  │ ┌────────────┐ │ │ ┌────────────┐ │ │ ┌────────────┐ │      │
│  │ │  [Image]   │ │ │ │  [Image]   │ │ │ │  [Image]   │ │      │
│  │ └────────────┘ │ │ └────────────┘ │ │ └────────────┘ │      │
│  │ 📂 Lịch sử    │ │ 📂 Lễ hội      │ │ 📂 Văn hoá     │      │
│  │ Lễ hội Đền    │ │ Tết Nguyên     │ │ Phong tục      │      │
│  │ Hùng — Nguồn  │ │ Đán qua các   │ │ cúng giỗ tổ    │      │
│  │ gốc & ý nghĩa │ │ thời kỳ       │ │ tiên           │      │
│  │ ⏱ 8 phút      │ │ ⏱ 12 phút     │ │ ⏱ 6 phút      │      │
│  │ 👁 1,250       │ │ 👁 2,340       │ │ 👁 890         │      │
│  └────────────────┘ └────────────────┘ └────────────────┘      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

#### 8.5.2 Admin — Quản lý bài viết liên quan

```
┌─────────────────────────────────────────────────────────────────┐
│  ✏️ Chỉnh Sửa Bài Viết: "Giỗ Tổ Hùng Vương"                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  [... Các tab khác: Nội dung | SEO | Media ...]                │
│                                                                  │
│  ═══ BÀI VIẾT LIÊN QUAN ═══                                    │
│                                                                  │
│  📌 Chọn thủ công (Manual)                                      │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ 🔍 Tìm bài viết...                                      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ☰ 1. Lễ hội Đền Hùng — Nguồn gốc    [📂 Lịch sử] [✕ Xoá]   │
│  ☰ 2. Vua Hùng thời đại Hồng Bàng    [📂 Lịch sử] [✕ Xoá]   │
│  ☰ 3. Tết Nguyên Đán qua các thời kỳ [📂 Lễ hội]  [✕ Xoá]   │
│  (Kéo thả để sắp xếp thứ tự)                                   │
│                                                                  │
│  ─── Gợi ý tự động ───                                         │
│  [🔄 Tạo gợi ý tự động]                                        │
│                                                                  │
│  ✅ Phong tục cúng giỗ tổ tiên     Score: 0.82  [+ Thêm]      │
│  ✅ Các lễ hội mùa xuân Việt Nam   Score: 0.75  [+ Thêm]      │
│  ✅ Lịch sử dân tộc Lạc Việt      Score: 0.71  [+ Thêm]      │
│  ☐ Phong thuỷ ngày giỗ            Score: 0.55  [+ Thêm]      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 8.6 Thuật Toán Gợi Ý (Rule-based — Phase 1)

```
Input: article A (current article)
Output: List of related articles, sorted by relevance_score

Algorithm:
1. Manual picks (admin đã chọn) → score = 1.00, lấy hết
2. Same category articles (category_id == A.category_id) → score base = 0.40
   - Boost +0.15 nếu có ≥2 tags chung
   - Boost +0.10 nếu published trong 30 ngày gần đây
   - Boost +0.05 nếu view_count > average
3. Shared tags (≥1 tag chung, khác category) → score base = 0.30
   - Score tăng theo số tags chung: +0.10/tag (max +0.30)
4. Same author → score base = 0.20
5. Popular articles (top views, 7 ngày) → score base = 0.10
6. Random (tránh filter bubble) → 1-2 bài, score = 0.05

Dedup: Loại bỏ trùng lặp
Sort: Theo relevance_score giảm dần
Limit: Top 6 bài viết
Cache: Redis TTL 1 giờ, invalidate khi article update
```

---

## 9. Hệ Thống Lịch Nội Dung Theo Ngày (Daily Content Calendar)

### 9.1 Tổng Quan

Hệ thống **Daily Content Calendar** cho phép Admin gán nội dung (danh ngôn, sự kiện lịch sử, bài viết, lễ hội) vào ngày cụ thể. Mỗi ngày trong năm sẽ có nội dung phong phú hiển thị trong phần chi tiết lịch.

```
┌─────────────────────────────────────────────────────────────────┐
│                    DAILY CONTENT CALENDAR                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ADMIN                              USER                         │
│  ┌──────────────────┐              ┌──────────────────┐         │
│  │ Gán nội dung     │              │ Xem lịch chi tiết│         │
│  │ cho ngày cụ thể  │─────────────▶│ + nội dung theo  │         │
│  │                   │              │   ngày            │         │
│  │ • Danh ngôn       │              │                   │         │
│  │ • Sự kiện lịch sử│              │ • Quote ngày      │         │
│  │ • Bài viết        │              │ • Sự kiện lịch sử│         │
│  │ • Lễ hội          │              │ • Bài viết gợi ý │         │
│  │ • Custom content │              │ • Lễ hội          │         │
│  └──────────────────┘              │ • Ngày kỷ niệm   │         │
│                                     │ • Sinh nhật       │         │
│  ┌──────────────────┐              │   người nổi tiếng │         │
│  │ Auto-assign      │              └──────────────────┘         │
│  │ (Hệ thống tự    │                                            │
│  │  gán dựa trên   │              ┌──────────────────┐         │
│  │  dữ liệu có sẵn)│─────────────▶│ Nội dung tự động │         │
│  │                   │              │ từ database       │         │
│  │ • Quote by        │              │ (fallback khi     │         │
│  │   day_of_year    │              │  admin chưa gán)  │         │
│  │ • Events by      │              └──────────────────┘         │
│  │   event_day/month│                                            │
│  │ • Famous people  │                                            │
│  │   by birth_day   │                                            │
│  │ • Festivals by   │                                            │
│  │   lunar/solar    │                                            │
│  └──────────────────┘                                            │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 9.2 Chiến Lược Nội Dung Theo Ngày

Có 2 nguồn nội dung cho mỗi ngày:

#### Nguồn 1: Tự Động (Auto-assign — từ dữ liệu v2.0 có sẵn)

| Loại nội dung | Cách lấy tự động | Ví dụ |
|---------------|-------------------|-------|
| **Danh ngôn (Quote)** | Lấy theo `day_of_year` (1-366) | Quote #75 → Ngày 16/3 |
| **Sự kiện lịch sử** | Lấy theo `event_day` + `event_month` | event_day=10, event_month=3 → "Ngày 10/3 trong lịch sử" |
| **Người nổi tiếng** | Sinh nhật theo `birth_day` + `birth_month` | birth_day=26, birth_month=5 → "Hồ Chí Minh sinh ngày 19/5" |
| **Lễ hội** | Theo `lunar_day/lunar_month` hoặc `solar_day/solar_month` | lunar_day=15, lunar_month=8 → Tết Trung Thu |
| **Bài viết ngẫu nhiên** | Random bài viết `published`, đổi mỗi 24h | Mỗi ngày hiển thị 2-3 bài viết ngẫu nhiên |

#### Nguồn 2: Thủ Công (Manual — Admin gán)

Admin có thể:
- **Override** nội dung tự động (VD: thay quote khác cho ngày 10/3)
- **Thêm nội dung bổ sung** ngoài auto (VD: bài viết đặc biệt cho ngày lễ)
- **Lên lịch trước** nội dung cho nhiều ngày (VD: chuẩn bị nội dung Tết cả tuần)
- **Gán theo ngày dương hoặc ngày âm lịch**
- **Đặt nội dung recurring** (lặp lại hàng năm)

### 9.3 Database Schema — Daily Content

#### Bảng `daily_content_schedule` (PostgreSQL — MỚI)

```sql
CREATE TABLE daily_content_schedule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Ngày hiển thị
    display_day INT NOT NULL,                -- 1-31
    display_month INT NOT NULL,              -- 1-12
    display_year INT,                        -- NULL = recurring (mỗi năm), có giá trị = chỉ năm đó
    calendar_type VARCHAR(10) NOT NULL DEFAULT 'solar',
        -- 'solar' (dương lịch), 'lunar' (âm lịch)

    -- Nội dung liên kết
    content_type VARCHAR(30) NOT NULL,
        -- 'quote', 'event', 'article', 'festival', 'famous_person', 'custom'
    content_id UUID,                         -- ID của nội dung (reference tới bảng tương ứng)
    
    -- Custom content (khi content_type = 'custom')
    custom_title VARCHAR(500),
    custom_body TEXT,
    custom_image_url VARCHAR(500),
    custom_link_url VARCHAR(500),
    custom_link_text VARCHAR(200),

    -- Display settings
    display_priority INT NOT NULL DEFAULT 0,
        -- 0 = bình thường, 1-10 = ưu tiên cao hơn hiển thị trước
    display_section VARCHAR(30) NOT NULL DEFAULT 'main',
        -- 'hero' (nổi bật nhất), 'main' (chính), 'sidebar' (cột bên)
    is_featured BOOLEAN DEFAULT FALSE,       -- Nổi bật (hiển thị lớn hơn)
    is_recurring BOOLEAN DEFAULT TRUE,       -- Lặp lại hàng năm
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Admin
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

-- Indexes
CREATE INDEX idx_daily_content_date ON daily_content_schedule(display_month, display_day);
CREATE INDEX idx_daily_content_year ON daily_content_schedule(display_year) WHERE display_year IS NOT NULL;
CREATE INDEX idx_daily_content_type ON daily_content_schedule(content_type);
CREATE INDEX idx_daily_content_calendar ON daily_content_schedule(calendar_type);
CREATE INDEX idx_daily_content_active ON daily_content_schedule(is_active) WHERE is_active = true;
CREATE UNIQUE INDEX idx_daily_content_unique ON daily_content_schedule(
    display_day, display_month, COALESCE(display_year, 0), calendar_type, content_type, COALESCE(content_id, '00000000-0000-0000-0000-000000000000'::uuid)
);
```

#### Go Model — `DailyContentSchedule`

```go
type DailyContentSchedule struct {
    ID              uuid.UUID      `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
    
    // Ngày hiển thị
    DisplayDay      int            `gorm:"type:int;not null" json:"display_day"`
    DisplayMonth    int            `gorm:"type:int;not null" json:"display_month"`
    DisplayYear     *int           `gorm:"type:int" json:"display_year,omitempty"`
    CalendarType    string         `gorm:"type:varchar(10);not null;default:'solar'" json:"calendar_type"`
    
    // Nội dung
    ContentType     string         `gorm:"type:varchar(30);not null" json:"content_type"`
    ContentID       *uuid.UUID     `gorm:"type:uuid" json:"content_id,omitempty"`
    
    // Custom content
    CustomTitle     string         `gorm:"type:varchar(500)" json:"custom_title,omitempty"`
    CustomBody      string         `gorm:"type:text" json:"custom_body,omitempty"`
    CustomImageURL  string         `gorm:"type:varchar(500)" json:"custom_image_url,omitempty"`
    CustomLinkURL   string         `gorm:"type:varchar(500)" json:"custom_link_url,omitempty"`
    CustomLinkText  string         `gorm:"type:varchar(200)" json:"custom_link_text,omitempty"`
    
    // Display
    DisplayPriority int            `gorm:"type:int;not null;default:0" json:"display_priority"`
    DisplaySection  string         `gorm:"type:varchar(30);not null;default:'main'" json:"display_section"`
    IsFeatured      bool           `gorm:"default:false" json:"is_featured"`
    IsRecurring     bool           `gorm:"default:true" json:"is_recurring"`
    IsActive        bool           `gorm:"default:true" json:"is_active"`
    
    // Admin
    CreatedBy       *uuid.UUID     `gorm:"type:uuid" json:"created_by,omitempty"`
    UpdatedBy       *uuid.UUID     `gorm:"type:uuid" json:"updated_by,omitempty"`
    CreatedAt       time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"created_at"`
    UpdatedAt       time.Time      `gorm:"type:timestamptz;not null;default:now()" json:"updated_at"`
    DeletedAt       gorm.DeletedAt `gorm:"type:timestamptz;index" json:"deleted_at,omitempty"`
    
    // Associations (loaded dynamically based on content_type)
    Quote           *Quote         `gorm:"-" json:"quote,omitempty"`
    Event           *Event         `gorm:"-" json:"event,omitempty"`
    Article         *Article       `gorm:"-" json:"article,omitempty"`
    Festival        *FolkFestival  `gorm:"-" json:"festival,omitempty"`
    FamousPerson    *FamousPerson  `gorm:"-" json:"famous_person,omitempty"`
}

func (DailyContentSchedule) TableName() string {
    return "daily_content_schedule"
}
```

### 9.4 API Endpoints — Daily Content

#### Public API

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| GET | `/api/calendar/date/:date/content` | Lấy tất cả nội dung cho 1 ngày | ❌ Public |
| GET | `/api/calendar/today/content` | Nội dung cho hôm nay | ❌ Public |
| GET | `/api/calendar/month/:year/:month/content-summary` | Tóm tắt nội dung cả tháng (có nội dung hay không) | ❌ Public |

#### Admin API

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| GET | `/api/admin/daily-content` | List tất cả nội dung đã schedule | ✅ Admin |
| GET | `/api/admin/daily-content/date/:date` | Nội dung cho 1 ngày cụ thể | ✅ Admin |
| GET | `/api/admin/daily-content/month/:year/:month` | Nội dung cả tháng (calendar view) | ✅ Admin |
| POST | `/api/admin/daily-content` | Thêm nội dung cho ngày | ✅ Admin |
| PUT | `/api/admin/daily-content/:id` | Sửa nội dung | ✅ Admin |
| DELETE | `/api/admin/daily-content/:id` | Xoá nội dung | ✅ Admin |
| POST | `/api/admin/daily-content/bulk` | Thêm nhiều nội dung cùng lúc | ✅ Admin |
| POST | `/api/admin/daily-content/auto-fill` | Tự động điền nội dung cho khoảng ngày | ✅ Admin |
| GET | `/api/admin/daily-content/stats` | Thống kê coverage (bao nhiêu ngày có nội dung) | ✅ Admin |

#### Response mẫu — GET `/api/calendar/date/2026-03-10/content`

```json
{
  "success": true,
  "data": {
    "date": "2026-03-10",
    "solar": { "day": 10, "month": 3, "year": 2026 },
    "lunar": { "day": 16, "month": 2, "year": 2026 },
    
    "daily_quote": {
      "id": "quote-uuid-001",
      "quote": "Dân ta phải biết sử ta, cho tường gốc tích nước nhà Việt Nam.",
      "author": "Hồ Chí Minh",
      "author_image_url": "/uploads/people/ho-chi-minh.webp",
      "source": "scheduled",
      "schedule_id": "schedule-uuid-001"
    },
    
    "historical_events": [
      {
        "id": "event-uuid-001",
        "title": "Trận Bạch Đằng năm 938",
        "short_description": "Ngô Quyền đánh bại quân Nam Hán...",
        "event_year": 938,
        "importance": "high",
        "image_url": "/uploads/events/bach-dang.webp",
        "article_id": "article-uuid-001",
        "source": "auto"
      },
      {
        "id": "event-uuid-002",
        "title": "Ngày Quốc tế Phụ nữ",
        "short_description": "Kỷ niệm ngày Quốc tế Phụ nữ...",
        "event_type": "world_day",
        "importance": "medium",
        "source": "auto"
      }
    ],
    
    "featured_articles": [
      {
        "id": "article-uuid-010",
        "title": "Phong tục ngày Tết Nguyên Đán",
        "slug": "phong-tuc-ngay-tet-nguyen-dan",
        "excerpt": "Tìm hiểu về các phong tục truyền thống...",
        "featured_image": "/uploads/articles/tet.webp",
        "category": { "name": "Văn hoá" },
        "reading_time": 8,
        "source": "scheduled"
      }
    ],
    
    "random_articles": [
      {
        "id": "article-uuid-020",
        "title": "Ý nghĩa các chòm sao trong tử vi",
        "slug": "y-nghia-cac-chom-sao-tu-vi",
        "excerpt": "Mỗi chòm sao mang một ý nghĩa...",
        "featured_image": "/uploads/articles/tu-vi.webp",
        "category": { "name": "Tử vi" },
        "reading_time": 5,
        "source": "random"
      },
      {
        "id": "article-uuid-021",
        "title": "10 lễ hội mùa xuân nổi tiếng nhất",
        "slug": "10-le-hoi-mua-xuan",
        "excerpt": "Khám phá những lễ hội...",
        "featured_image": "/uploads/articles/le-hoi-xuan.webp",
        "category": { "name": "Lễ hội" },
        "reading_time": 10,
        "source": "random"
      }
    ],
    
    "festivals": [
      {
        "id": "festival-uuid-001",
        "name": "Lễ hội Chùa Hương",
        "calendar_type": "lunar",
        "lunar_day": 16,
        "lunar_month": 2,
        "duration_days": 3,
        "short_description": "Lễ hội diễn ra tại...",
        "source": "auto"
      }
    ],
    
    "famous_birthdays": [
      {
        "id": "person-uuid-001",
        "name": "Nguyễn Trãi",
        "birth_year": 1380,
        "category": "van_hoc",
        "short_bio": "Nhà chính trị, nhà thơ...",
        "image_url": "/uploads/people/nguyen-trai.webp",
        "source": "auto"
      }
    ],
    
    "custom_content": [
      {
        "id": "schedule-uuid-005",
        "custom_title": "Mẹo phong thuỷ ngày 10/3",
        "custom_body": "Hôm nay là ngày tốt để...",
        "custom_image_url": "/uploads/custom/phong-thuy-tips.webp",
        "display_section": "sidebar",
        "source": "scheduled"
      }
    ],

    "content_stats": {
      "total_items": 8,
      "has_quote": true,
      "has_events": true,
      "has_articles": true,
      "has_festivals": true,
      "has_birthdays": true,
      "has_custom": true
    }
  }
}
```

### 9.5 Giao Diện Admin — Daily Content Calendar

#### 9.5.1 Calendar View — Quản Lý Nội Dung Theo Tháng

```
┌─────────────────────────────────────────────────────────────────┐
│  📅 Lịch Nội Dung                    Tháng 3/2026  [◀ ▶]      │
│  ─────────────────────────────────────────────────────────────  │
│  [+ Thêm nội dung] [🔄 Auto-fill tháng] [📊 Thống kê]        │
├────────┬────────┬────────┬────────┬────────┬────────┬────────┤
│  T2    │  T3    │  T4    │  T5    │  T6    │  T7    │  CN    │
├────────┼────────┼────────┼────────┼────────┼────────┼────────┤
│        │        │        │        │        │        │   1    │
│        │        │        │        │        │        │ 💬 📅  │
├────────┼────────┼────────┼────────┼────────┼────────┼────────┤
│   2    │   3    │   4    │   5    │   6    │   7    │   8    │
│ 💬     │ 💬 📅  │ 💬     │ 💬 📝  │ 💬 📅  │ 💬     │ 💬 🎎 📅│
│        │        │        │ 👤     │        │        │ 🌟     │
├────────┼────────┼────────┼────────┼────────┼────────┼────────┤
│   9    │  10    │  11    │  12    │  13    │  14    │  15    │
│ 💬     │ 💬 📅  │ 💬     │ 💬     │ 💬 📝  │ 💬     │ 💬 🎎  │
│        │ 🌟 📝  │        │        │        │        │ 🌟     │
│        │ 👤     │        │        │        │        │        │
├────────┴────────┴────────┴────────┴────────┴────────┴────────┤
│  💬 = Quote  📅 = Sự kiện  📝 = Bài viết  🎎 = Lễ hội        │
│  👤 = Sinh nhật  🌟 = Nổi bật  ⬜ = Trống (cần thêm)         │
│                                                                 │
│  📊 Coverage: 28/31 ngày có nội dung (90.3%)                   │
│  ⚠️ 3 ngày còn trống: 16, 22, 29                               │
└─────────────────────────────────────────────────────────────────┘
```

#### 9.5.2 Dialog — Thêm Nội Dung Cho Ngày

```
┌─────────────────────────────────────────────────────────────────┐
│  📅 Thêm Nội Dung — Ngày 10/03/2026                [✕ Đóng]   │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Loại nội dung *                                                 │
│  [▼ Chọn loại nội dung                              ]           │
│  ├─ 💬 Danh ngôn (Quote)                                        │
│  ├─ 📅 Sự kiện lịch sử (Event)                                  │
│  ├─ 📝 Bài viết (Article)                                       │
│  ├─ 🎎 Lễ hội (Festival)                                        │
│  ├─ 👤 Người nổi tiếng (Famous Person)                           │
│  └─ 📋 Nội dung tuỳ chỉnh (Custom)                              │
│                                                                  │
│  ─── KHI CHỌN "DANH NGÔN" ───                                  │
│                                                                  │
│  Chọn danh ngôn *                                                │
│  🔍 [Tìm kiếm danh ngôn...                         ]           │
│                                                                  │
│  ○ "Dân ta phải biết sử ta..." — Hồ Chí Minh                  │
│  ● "Một dân tộc dốt là..." — Hồ Chí Minh      ← Đã chọn      │
│  ○ "Không có gì quý hơn..." — Hồ Chí Minh                     │
│                                                                  │
│  ─── HOẶC KHI CHỌN "TUỲ CHỈNH" ───                            │
│                                                                  │
│  Tiêu đề *                                                      │
│  [Mẹo phong thuỷ ngày 10/3                         ]           │
│                                                                  │
│  Nội dung                                                        │
│  [Hôm nay là ngày tốt để khởi công xây dựng...    ]           │
│  [...                                                ]           │
│                                                                  │
│  Hình ảnh  [📁 Chọn media]                                      │
│                                                                  │
│  Link      [https://lichso.vn/phong-thuy/...        ]           │
│                                                                  │
│  ─── CÀI ĐẶT HIỂN THỊ ───                                     │
│                                                                  │
│  Loại lịch        [● Dương lịch  ○ Âm lịch]                    │
│  Vị trí hiển thị  [▼ Chính (main)           ]                   │
│  Độ ưu tiên       [▼ Bình thường (0)        ]                   │
│  ☐ Nổi bật (featured)                                           │
│  ☑ Lặp lại hàng năm (recurring)                                │
│                                                                  │
│  [Huỷ]                                           [💾 Lưu]      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

#### 9.5.3 Auto-Fill — Tự Động Điền Nội Dung

```
┌─────────────────────────────────────────────────────────────────┐
│  🔄 Tự Động Điền Nội Dung                          [✕ Đóng]   │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Tự động gán nội dung từ database cho các ngày còn trống.      │
│                                                                  │
│  Khoảng ngày *                                                   │
│  Từ: [01/03/2026]  Đến: [31/03/2026]                           │
│                                                                  │
│  Nội dung tự động gán:                                          │
│  ☑ 💬 Danh ngôn (theo day_of_year)                              │
│  ☑ 📅 Sự kiện lịch sử (theo ngày/tháng)                        │
│  ☑ 👤 Sinh nhật người nổi tiếng (theo ngày/tháng)              │
│  ☑ 🎎 Lễ hội (theo ngày âm/dương)                              │
│  ☑ 📝 Bài viết ngẫu nhiên (2 bài/ngày)                        │
│                                                                  │
│  Chế độ:                                                         │
│  ● Chỉ điền ngày trống (không ghi đè)                           │
│  ○ Ghi đè tất cả (thay thế nội dung đã có)                     │
│                                                                  │
│  Preview: Sẽ tạo ~155 entries cho 31 ngày                       │
│  - 31 quotes (1/ngày)                                           │
│  - ~45 sự kiện lịch sử                                          │
│  - ~12 sinh nhật người nổi tiếng                                │
│  - ~8 lễ hội                                                     │
│  - ~62 bài viết ngẫu nhiên (2/ngày)                            │
│                                                                  │
│  [Huỷ]                                      [🔄 Thực hiện]     │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 9.6 Caching Strategy — Daily Content

```
Redis Keys:
cache:daily_content:{date}                    → JSON (TTL: 1h)
cache:daily_content:{date}:quotes             → JSON (TTL: 1h)
cache:daily_content:{date}:events             → JSON (TTL: 1h)
cache:daily_content:{date}:articles           → JSON (TTL: 1h)
cache:daily_content:random_articles:{date}    → JSON (TTL: 24h, reset hàng ngày)
cache:daily_content:month:{year}:{month}      → JSON summary (TTL: 5m)
cache:daily_content:stats                     → JSON (TTL: 5m)

Invalidation:
- Khi admin thêm/sửa/xoá daily_content → clear cache:{date}
- Khi admin update quote/event/article → clear related date caches
- Random articles → reset lúc 00:00 hàng ngày (cron job)
```

---

## 10. Lịch Chi Tiết Nâng Cao (Enhanced Day Detail)

### 10.1 Tổng Quan

Nâng cấp `DayDetailModal` hiện tại (chỉ hiển thị thông tin lịch cơ bản) thành một trang chi tiết ngày phong phú, bao gồm nội dung văn hoá, bài viết, danh ngôn, sự kiện lịch sử.

#### So Sánh DayDetailModal v2.0 vs v3.0

| Thành phần | v2.0 (Hiện tại) | v3.0 (Nâng cấp) |
|------------|-----------------|------------------|
| **Ngày tháng** | Solar + Lunar + Can Chi | ✅ Giữ nguyên |
| **Sự kiện/ngày lễ** | CalendarEvent[] (lễ quốc gia, quốc tế) | ✅ Giữ nguyên + nâng cấp hiển thị |
| **Đánh giá ngày** | Điểm 0-10, Trực ngày, Sao chiếu | ✅ Giữ nguyên |
| **Giờ hoàng đạo** | Danh sách giờ tốt | ✅ Giữ nguyên |
| **Việc nên/không nên** | List activities | ✅ Giữ nguyên |
| **Danh ngôn ngày** | ❌ Không có | 🆕 Quote of the day |
| **Sự kiện lịch sử** | ❌ Không có | 🆕 "Ngày này trong lịch sử" |
| **Bài viết gợi ý** | ❌ Không có | 🆕 2-3 bài viết ngẫu nhiên (brief card) |
| **Lễ hội** | ❌ Không có | 🆕 Lễ hội diễn ra hôm nay |
| **Sinh nhật người nổi tiếng** | ❌ Không có | 🆕 "Sinh nhật hôm nay" |
| **Nội dung admin gán** | ❌ Không có | 🆕 Custom content từ admin |
| **Ghi chú cá nhân** | ❌ Không có | 🆕 User notes (đăng nhập) |
| **Chia sẻ** | ❌ Không có | 🆕 Share ngày đẹp lên MXH |
| **In lịch** | ❌ Không có | 🆕 In thông tin ngày |

### 10.2 Giao Diện DayDetailModal v3.0

```
┌─────────────────────────────────────────────────────────────────┐
│  📅 Chi Tiết Ngày                                    [✕ Đóng]  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────┐                │
│  │        THỨ BA — 10 THÁNG 3 NĂM 2026        │                │
│  │        ☽ 16 tháng 2 năm Bính Ngọ           │                │
│  │        Ngày Nhâm Thìn • Giờ Canh Tý        │                │
│  └─────────────────────────────────────────────┘                │
│                                                                  │
│  ─── 💬 DANH NGÔN NGÀY ──────────────────────────────────────  │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  ❝ Dân ta phải biết sử ta, cho tường gốc tích          │    │
│  │    nước nhà Việt Nam. ❞                                  │    │
│  │                                                          │    │
│  │    — Hồ Chí Minh 🇻🇳                                    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ─── 📅 NGÀY NÀY TRONG LỊCH SỬ ─────────────────────────────  │
│                                                                  │
│  🔴 938  — Ngô Quyền đánh bại quân Nam Hán tại sông Bạch      │
│            Đằng, chấm dứt 1000 năm Bắc thuộc                   │
│            [📖 Xem chi tiết →]                                   │
│                                                                  │
│  🟡 1975 — Chiến dịch Tây Nguyên bắt đầu, mở màn cho         │
│            chiến dịch Hồ Chí Minh                               │
│            [📖 Xem chi tiết →]                                   │
│                                                                  │
│  🔵 2020 — WHO tuyên bố COVID-19 là đại dịch toàn cầu         │
│                                                                  │
│  ─── 🎎 LỄ HỘI ─────────────────────────────────────────────  │
│                                                                  │
│  🏮 Lễ hội Chùa Hương (ngày 16/2 Âm lịch)                    │
│     Hà Nội • Diễn ra trong 3 ngày                              │
│     [📖 Xem chi tiết →]                                         │
│                                                                  │
│  ─── 🎂 SINH NHẬT HÔM NAY ──────────────────────────────────  │
│                                                                  │
│  👤 Nguyễn Trãi (1380) — Nhà chính trị, nhà thơ lớn          │
│  👤 Chuck Norris (1940) — Diễn viên võ thuật Mỹ 🇺🇸           │
│                                                                  │
│  ─── 📖 BÀI VIẾT GỢI Ý ─────────────────────────────────────  │
│                                                                  │
│  ┌─────────────────────────┐ ┌─────────────────────────┐       │
│  │ ┌─────────────────────┐ │ │ ┌─────────────────────┐ │       │
│  │ │    [Image]          │ │ │ │    [Image]          │ │       │
│  │ └─────────────────────┘ │ │ └─────────────────────┘ │       │
│  │ 📂 Văn hoá              │ │ 📂 Tử vi                │       │
│  │ Phong tục ngày Tết      │ │ Ý nghĩa chòm sao       │       │
│  │ Nguyên Đán               │ │ Song Ngư                │       │
│  │ ⏱ 8 phút • 👁 1,250    │ │ ⏱ 5 phút • 👁 890     │       │
│  │ [Đọc ngay →]           │ │ [Đọc ngay →]           │       │
│  └─────────────────────────┘ └─────────────────────────┘       │
│                                                                  │
│  ═══════════════════════════════════════════════════════════════ │
│                                                                  │
│  ─── ⭐ ĐÁNH GIÁ NGÀY ──────────────────────────────────────  │
│                                                                  │
│  Điểm: ████████░░ 8/10 — Ngày Tốt                              │
│  Trực: Khai 開  |  Sao: Giác 角                                 │
│                                                                  │
│  ─── 🧭 HƯỚNG TỐT ──────────────────────────────────────────  │
│  🟢 Hỷ thần: Đông Nam  |  🟢 Tài thần: Đông Bắc              │
│  🟡 Hạc thần: Tây      |  🔴 Tránh: Tây Nam                   │
│                                                                  │
│  ─── 🌿 TIẾT KHÍ ────────────────────────────────────────────  │
│  Kinh Trập (惊蛰) — Côn trùng tỉnh giấc                       │
│                                                                  │
│  ─── ⏰ GIỜ HOÀNG ĐẠO ──────────────────────────────────────  │
│  🟢 Tý (23-1h) | 🟢 Sửu (1-3h) | 🔴 Dần (3-5h)             │
│  🟢 Mão (5-7h) | 🔴 Thìn (7-9h) | 🟢 Tị (9-11h)            │
│  🔴 Ngọ (11-13h)| 🟢 Mùi (13-15h)| 🔴 Thân (15-17h)         │
│  🟢 Dậu (17-19h)| 🔴 Tuất (19-21h)| 🟢 Hợi (21-23h)         │
│                                                                  │
│  ─── ✅ VIỆC NÊN LÀM ───────────────────────────────────────  │
│  ✅ Cúng tế  ✅ Cầu phúc  ✅ Xuất hành  ✅ Khai trương       │
│  ✅ Nhập trạch  ✅ Cưới hỏi  ✅ An táng                        │
│                                                                  │
│  ─── ❌ VIỆC KHÔNG NÊN ──────────────────────────────────────  │
│  ❌ Phá thổ  ❌ Kiện tụng  ❌ Đào giếng                        │
│                                                                  │
│  ─── 📝 GHI CHÚ CÁ NHÂN ────────────────────────────────────  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ Viết ghi chú cho ngày này...                             │    │
│  │                                                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│  [💾 Lưu ghi chú]                   [🔒 Cần đăng nhập]        │
│                                                                  │
│  ═══════════════════════════════════════════════════════════════ │
│  [🖨️ In]  [📤 Chia sẻ]  [🔖 Đánh dấu]  [🔔 Nhắc nhở]        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 10.3 Frontend Types — Enhanced DayResponse

```typescript
// types/calendar.ts — UPDATED for v3.0

// Nội dung ngày (mới v3.0)
interface DailyQuote {
  id: string;
  quote: string;
  original_quote?: string;
  author: string;
  author_image_url?: string;
  author_nationality?: string;
  source: 'scheduled' | 'auto' | 'random';
}

interface DailyEvent {
  id: string;
  title: string;
  slug: string;
  short_description?: string;
  event_year?: number;
  event_type: string;
  importance: 'low' | 'medium' | 'high';
  image_url?: string;
  article_id?: string;
  source: 'scheduled' | 'auto';
}

interface DailyArticle {
  id: string;
  title: string;
  slug: string;
  excerpt: string;
  featured_image?: string;
  category?: { id: string; name: string };
  reading_time: number;
  view_count: number;
  source: 'scheduled' | 'random';
}

interface DailyFestival {
  id: string;
  name: string;
  slug: string;
  calendar_type: 'lunar' | 'solar';
  short_description?: string;
  duration_days: number;
  region?: string;
  image_url?: string;
  source: 'auto';
}

interface DailyBirthday {
  id: string;
  name: string;
  birth_year?: number;
  category: string;
  short_bio?: string;
  image_url?: string;
  nationality?: string;
  source: 'auto';
}

interface DailyCustomContent {
  id: string;
  custom_title: string;
  custom_body?: string;
  custom_image_url?: string;
  custom_link_url?: string;
  custom_link_text?: string;
  display_section: 'hero' | 'main' | 'sidebar';
  is_featured: boolean;
}

// Content cho 1 ngày
interface DayContent {
  daily_quote?: DailyQuote;
  historical_events: DailyEvent[];
  featured_articles: DailyArticle[];
  random_articles: DailyArticle[];
  festivals: DailyFestival[];
  famous_birthdays: DailyBirthday[];
  custom_content: DailyCustomContent[];
  content_stats: {
    total_items: number;
    has_quote: boolean;
    has_events: boolean;
    has_articles: boolean;
    has_festivals: boolean;
    has_birthdays: boolean;
    has_custom: boolean;
  };
}

// DayResponse mở rộng (v3.0)
interface DayResponseV3 extends DayResponse {
  content?: DayContent;       // Nội dung phong phú theo ngày
  user_notes?: UserNote[];    // Ghi chú cá nhân (nếu đăng nhập)
  user_bookmarks?: boolean;   // Đã bookmark ngày này chưa
}
```

### 10.4 Bài Viết Ngẫu Nhiên Trong Lịch Chi Tiết

Tính năng hiển thị 2-3 bài viết ngẫu nhiên trong phần chi tiết ngày, giúp người dùng khám phá nội dung:

#### Cơ chế hoạt động

```
1. Mỗi ngày, backend random 2-3 bài viết published (khác nhau mỗi ngày)
2. Cache kết quả trong Redis TTL 24h (reset lúc 00:00)
3. Frontend hiển thị dạng brief card (ảnh + title + excerpt ngắn + reading time)
4. Click vào card → chuyển tới trang bài viết chi tiết
5. Nút "🔄 Xem bài khác" → gọi API random mới (bypass cache)
```

#### Random Strategy

```sql
-- Strategy 1: Weighted random (ưu tiên bài chất lượng)
SELECT * FROM articles 
WHERE status = 'published' 
  AND deleted_at IS NULL
ORDER BY 
  -- Ưu tiên bài có ảnh, nhiều view, mới hơn
  (CASE WHEN featured_image IS NOT NULL THEN 0.3 ELSE 0 END)
  + (CASE WHEN view_count > 100 THEN 0.2 ELSE 0 END)
  + (CASE WHEN published_at > NOW() - INTERVAL '90 days' THEN 0.2 ELSE 0 END)
  + RANDOM() * 0.3
  DESC
LIMIT 3;

-- Strategy 2: Category-diverse random (mỗi bài khác category)
-- Lấy 1 bài mỗi category, random trong category
WITH ranked AS (
  SELECT *, ROW_NUMBER() OVER (PARTITION BY category_id ORDER BY RANDOM()) as rn
  FROM articles WHERE status = 'published'
)
SELECT * FROM ranked WHERE rn = 1 LIMIT 3;
```

#### Component Brief Card

```
┌─────────────────────────┐
│ ┌─────────────────────┐ │
│ │                     │ │   ← Featured image (thumb_lg 600×400)
│ │    [Image]          │ │      Fallback: gradient + category icon
│ │                     │ │
│ └─────────────────────┘ │
│ 📂 Văn hoá              │   ← Category badge
│ Phong tục ngày Tết      │   ← Title (max 2 lines, truncate)
│ Nguyên Đán               │
│ ⏱ 8 phút • 👁 1,250    │   ← Reading time + view count
│ [Đọc ngay →]           │   ← CTA link → /bai-viet/:slug
└─────────────────────────┘
```

### 10.5 Calendar Grid — Content Indicators

Trên Calendar Grid (lịch tháng), hiển thị indicator cho ngày có nội dung:

```
┌────────────────────────────────────────────────────────────────┐
│         THÁNG 3 NĂM 2026            [◀]  [▶]                  │
├────────┬────────┬────────┬────────┬────────┬────────┬────────┤
│  T2    │  T3    │  T4    │  T5    │  T6    │  T7    │  CN    │
├────────┼────────┼────────┼────────┼────────┼────────┼────────┤
│   2    │   3    │   4    │   5    │   6    │   7    │   8    │
│  🟢    │  🟢    │  🟢    │  🟡    │  🟢    │  🟢    │  🔴    │
│  ●●    │  ●●●   │  ●     │  ●●    │  ●●    │  ●     │  ●●●● │
├────────┼────────┼────────┼────────┼────────┼────────┼────────┤

●  = Có nội dung (mỗi dot = 1 loại: quote, event, article, festival)
🟢 = Ngày tốt  🟡 = Trung bình  🔴 = Ngày xấu
```

#### API cho Calendar Grid Content Summary

```json
// GET /api/calendar/month/2026/3/content-summary
{
  "success": true,
  "data": {
    "2026-03-01": { "has_content": true, "types": ["quote", "event"], "count": 2 },
    "2026-03-02": { "has_content": true, "types": ["quote", "article"], "count": 3 },
    "2026-03-08": { "has_content": true, "types": ["quote", "event", "festival", "article"], "count": 6 },
    "2026-03-10": { "has_content": true, "types": ["quote", "event", "article", "festival", "birthday"], "count": 8 },
    // ...
  }
}
```

---

## 11. Tính Năng Lịch Mở Rộng (Extended Calendar Features)

### 11.1 Tổng Quan Tính Năng Đề Xuất

Ngoài các tính năng nội dung, v3.0 bổ sung thêm các tính năng tiện ích giúp Lịch Số trở thành **bộ lịch hữu ích nhất** cho người dùng Việt Nam:

```
┌─────────────────────────────────────────────────────────────────┐
│                EXTENDED CALENDAR FEATURES v3.0                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  🔰 CÁ NHÂN HÓA                     📅 LỊCH NÂNG CAO           │
│  ├── 📝 Ghi chú / Nhật ký           ├── 🔔 Nhắc nhở thông minh │
│  ├── 🔖 Bookmark ngày đẹp           ├── ⏳ Đếm ngược sự kiện   │
│  ├── 📋 Sự kiện cá nhân             ├── 🖨️ In lịch tháng/năm   │
│  └── 👨‍👩‍👧‍👦 Lịch gia đình             ├── 📤 Chia sẻ lên MXH      │
│                                      └── 📱 Widget mobile        │
│                                                                  │
│  🌟 NỘI DUNG GIÁ TRỊ               🔮 TỬ VI & PHONG THUỶ      │
│  ├── 💬 Danh ngôn hàng ngày         ├── 🐉 Tử vi hàng ngày     │
│  ├── 📅 Ngày này trong lịch sử      ├── 🏠 Phong thuỷ ngày      │
│  ├── 🎂 Sinh nhật người nổi tiếng   ├── 💍 Xem ngày cưới hỏi   │
│  ├── 🎎 Lễ hội hôm nay             ├── 🏗️ Xem ngày động thổ    │
│  └── 📖 Bài viết ngẫu nhiên        ├── 🚗 Xem ngày xuất hành   │
│                                      └── 📊 Tuổi âm lịch         │
│                                                                  │
│  📊 THỐNG KÊ & ANALYTICS            🌐 TÍCH HỢP                │
│  ├── 📈 Thống kê cá nhân            ├── 🌤️ Thời tiết            │
│  ├── 📅 Lịch sử xem                 ├── 📅 Google Calendar sync  │
│  └── 🏆 Streaks / Achievements      └── 📧 Email digest hàng ngày│
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 11.2 📝 Ghi Chú Cá Nhân (Personal Notes / Diary)

Người dùng đăng nhập có thể ghi chú cho mỗi ngày:

#### Database Schema

```sql
CREATE TABLE user_notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    note_date DATE NOT NULL,
    title VARCHAR(200),
    content TEXT NOT NULL,
    mood VARCHAR(20),              -- 'happy', 'neutral', 'sad', 'excited', 'tired'
    mood_emoji VARCHAR(10),        -- '😊', '😐', '😢', '🤩', '😴'
    tags TEXT[] DEFAULT '{}',
    is_private BOOLEAN DEFAULT TRUE,
    color VARCHAR(7),              -- HEX color for display
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,

    CONSTRAINT uq_user_note_date UNIQUE (user_id, note_date, title)
);

CREATE INDEX idx_user_notes_user_date ON user_notes(user_id, note_date);
CREATE INDEX idx_user_notes_mood ON user_notes(mood) WHERE mood IS NOT NULL;
```

#### API Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| GET | `/api/notes` | List ghi chú của user (phân trang) | ✅ User |
| GET | `/api/notes/date/:date` | Ghi chú cho ngày cụ thể | ✅ User |
| GET | `/api/notes/month/:year/:month` | Ghi chú cả tháng | ✅ User |
| POST | `/api/notes` | Tạo ghi chú mới | ✅ User |
| PUT | `/api/notes/:id` | Sửa ghi chú | ✅ User |
| DELETE | `/api/notes/:id` | Xoá ghi chú | ✅ User |
| GET | `/api/notes/search?q=keyword` | Tìm kiếm ghi chú | ✅ User |
| GET | `/api/notes/stats` | Thống kê (số ngày ghi, mood chart) | ✅ User |

### 11.3 ⏳ Đếm Ngược Sự Kiện (Event Countdown)

Hiển thị đếm ngược đến các sự kiện quan trọng:

#### Sự kiện hệ thống (auto)

| Sự kiện | Loại | Tính toán |
|---------|------|-----------|
| Tết Nguyên Đán | Lunar | Tính từ âm lịch |
| Tết Trung Thu | Lunar | 15/8 âm lịch |
| Giỗ Tổ Hùng Vương | Lunar | 10/3 âm lịch |
| Quốc Khánh 2/9 | Solar | 02/09 |
| Giáng Sinh | Solar | 25/12 |
| Tết Dương Lịch | Solar | 01/01 |

#### Sự kiện cá nhân (user tạo)

```sql
CREATE TABLE user_countdowns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    target_date DATE NOT NULL,
    calendar_type VARCHAR(10) DEFAULT 'solar',
    is_recurring BOOLEAN DEFAULT FALSE,
    icon VARCHAR(10),              -- emoji icon
    color VARCHAR(7),              -- HEX color
    notify_before_days INT[],      -- [7, 3, 1, 0] = thông báo trước 7, 3, 1, 0 ngày
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

#### Giao diện Countdown Widget

```
┌─────────────────────────────────────────────────────────────────┐
│  ⏳ ĐẾM NGƯỢC                                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  🏮 Tết Nguyên Đán 2027                                        │
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐                               │
│  │ 298 │ │  12 │ │  05 │ │  33 │                               │
│  │ ngày│ │ giờ │ │phút │ │giây │                               │
│  └─────┘ └─────┘ └─────┘ └─────┘                               │
│                                                                  │
│  🎂 Sinh nhật bạn Minh                    còn 15 ngày          │
│  🏛️ Giỗ Tổ Hùng Vương (10/3 ÂL)         còn 45 ngày          │
│  🎄 Giáng Sinh                            còn 290 ngày         │
│  📝 Deadline dự án ABC                    còn 3 ngày ⚠️        │
│                                                                  │
│  [+ Thêm sự kiện]                                               │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 11.4 🖨️ In Lịch (Print Calendar)

Cho phép người dùng in lịch tháng/năm có thông tin phong thuỷ:

#### Mẫu in lịch tháng

```
┌─────────────────────────────────────────────────────────────────┐
│                     📅 LỊCH SỐ — THÁNG 3/2026                  │
│                    Tiết khí: Kinh Trập → Xuân Phân              │
├────────┬────────┬────────┬────────┬────────┬────────┬────────┤
│  T2    │  T3    │  T4    │  T5    │  T6    │  T7    │  CN    │
├────────┼────────┼────────┼────────┼────────┼────────┼────────┤
│        │        │        │        │        │        │   1    │
│        │        │        │        │        │        │  11/2  │
│        │        │        │        │        │        │  ★★★   │
├────────┼────────┼────────┼────────┼────────┼────────┼────────┤
│   2    │   3    │   4    │   5    │   6    │   7    │   8    │
│  12/2  │  13/2  │  14/2  │  15/2  │  16/2  │  17/2  │  18/2  │
│  ★★    │  ★★★★ │  ★★★   │  ★★★★ │  ★★    │  ★★★   │ QT Phụ │
│        │        │        │        │        │        │  Nữ    │
├────────┴────────┴────────┴────────┴────────┴────────┴────────┤
│  ★★★★★ = Ngày rất tốt  ★★★ = Tốt  ★★ = Trung bình  ★ = Xấu │
│  Lịch Số — lichso.vn                    Printed: 10/03/2026   │
└─────────────────────────────────────────────────────────────────┘
```

#### Các mẫu in

| Mẫu | Kích thước | Nội dung |
|-----|-----------|----------|
| **Lịch tháng mini** | A4 ngang | Ngày + âm lịch + đánh giá |
| **Lịch tháng chi tiết** | A4 dọc | + Sự kiện, lễ hội, giờ hoàng đạo |
| **Lịch năm** | A3 | 12 tháng tổng quan |
| **Thẻ ngày** | 10×15cm | Thông tin 1 ngày (để bàn) |
| **Lịch tuần** | A4 dọc | 7 ngày chi tiết + ghi chú |

### 11.5 📤 Chia Sẻ Lên Mạng Xã Hội

Tạo ảnh đẹp từ thông tin ngày để chia sẻ:

#### Share Card Template

```
┌─────────────────────────────────────────┐
│                                         │
│   📅 THỨ BA — 10/03/2026               │
│   ☽ 16 tháng 2 năm Bính Ngọ           │
│                                         │
│   ⭐ Ngày Tốt (8/10)                   │
│   Trực: Khai 開 | Sao: Giác 角         │
│                                         │
│   ✅ Nên: Cúng tế, Xuất hành, Khai trương│
│   ❌ Tránh: Phá thổ, Kiện tụng         │
│                                         │
│   ❝ Dân ta phải biết sử ta ❞          │
│     — Hồ Chí Minh                       │
│                                         │
│   🌐 lichso.vn                          │
│                                         │
└─────────────────────────────────────────┘
```

Platforms hỗ trợ:
- Facebook (1200×630 OG image)
- Zalo (share link + preview)
- Twitter/X (1200×675)
- Copy link
- Download image (PNG)

### 11.6 🐉 Tử Vi Hàng Ngày (Daily Horoscope)

Hiển thị tử vi tóm tắt cho 12 con giáp dựa vào ngày:

| Con giáp | Năm sinh mẫu | Tính cách |
|----------|-------------|-----------|
| 🐀 Tý | 1984, 1996, 2008 | Thông minh, nhanh nhẹn |
| 🐂 Sửu | 1985, 1997, 2009 | Kiên nhẫn, chăm chỉ |
| 🐅 Dần | 1986, 1998, 2010 | Dũng cảm, mạnh mẽ |
| 🐇 Mão | 1987, 1999, 2011 | Hiền hoà, khéo léo |
| 🐉 Thìn | 1988, 2000, 2012 | Quyền lực, may mắn |
| 🐍 Tị | 1989, 2001, 2013 | Khôn ngoan, bí ẩn |
| 🐴 Ngọ | 1990, 2002, 2014 | Năng động, tự do |
| 🐐 Mùi | 1991, 2003, 2015 | Hiền lành, nghệ sĩ |
| 🐒 Thân | 1992, 2004, 2016 | Thông minh, linh hoạt |
| 🐓 Dậu | 1993, 2005, 2017 | Chăm chỉ, trung thực |
| 🐕 Tuất | 1994, 2006, 2018 | Trung thành, chính trực |
| 🐖 Hợi | 1995, 2007, 2019 | Rộng lượng, chân thành |

#### Nguồn dữ liệu tử vi

```
Cách tính tử vi ngày cho mỗi con giáp:
1. Dựa trên Can Chi ngày (Thiên Can + Địa Chi)
2. Xem tương hợp/tương khắc với con giáp
3. Kết hợp Trực ngày + Sao chiếu
4. Tạo nội dung: Tổng quan, Tài lộc, Tình cảm, Sức khoẻ, Sự nghiệp
5. Đánh giá sao: ★★★★★ (1-5)
```

#### Giao diện tử vi ngày

```
┌─────────────────────────────────────────────────────────────────┐
│  🐉 TỬ VI NGÀY 10/03/2026 — TUỔI THÌN                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Tổng quan: ★★★★☆ (4/5) — Ngày khá thuận lợi                  │
│                                                                  │
│  💰 Tài lộc:    ★★★★★  Cơ hội tài chính tốt, nên đầu tư      │
│  💕 Tình cảm:   ★★★★☆  Quan hệ hài hoà, tránh cãi vã         │
│  💪 Sức khoẻ:   ★★★☆☆  Chú ý giấc ngủ, tập thể dục           │
│  💼 Sự nghiệp:  ★★★★☆  Được cấp trên ủng hộ                  │
│                                                                  │
│  🍀 Màu may mắn: Vàng, Đỏ                                     │
│  🔢 Số may mắn: 3, 8, 12                                       │
│  🧭 Hướng tốt: Đông Nam                                        │
│  ⏰ Giờ đẹp: Tý (23-1h), Mão (5-7h)                           │
│                                                                  │
│  💡 Lời khuyên: Hôm nay thích hợp cho việc ký kết hợp đồng,   │
│  bắt đầu dự án mới. Tránh tranh cãi vào buổi chiều.           │
│                                                                  │
│  [📅 Xem tuổi khác ▼]  [📖 Xem chi tiết tử vi tuần →]        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 11.7 💍 Xem Ngày Cưới Hỏi / Động Thổ / Xuất Hành

Nâng cấp tính năng "Ngày Tốt" hiện có:

#### Các loại xem ngày

| Loại | Tiêu chí đánh giá | Priority |
|------|-------------------|----------|
| 💍 **Cưới hỏi** | Trực ngày (Thành, Khai, Mãn), Sao tốt, Can Chi hợp tuổi 2 người | P0 |
| 🏗️ **Động thổ / Xây dựng** | Trực (Khai, Kiến), Hướng nhà hợp, Tháng tốt | P0 |
| 🚗 **Xuất hành** | Giờ hoàng đạo, Hướng xuất hành, Trực (Khai, Thành) | P1 |
| 🏠 **Nhập trạch** | Trực (Mãn, Thành), Tuổi hợp, Hướng nhà | P1 |
| 🏪 **Khai trương** | Trực (Khai), Sao tốt, Hướng quầy hợp | P1 |
| 📋 **Ký hợp đồng** | Trực (Thành, Bình), Giờ tốt | P2 |
| ✈️ **Du lịch** | Trực (Khai), Tiết khí, Hướng tốt | P2 |

#### Giao diện xem ngày cưới

```
┌─────────────────────────────────────────────────────────────────┐
│  💍 XEM NGÀY CƯỚI HỎI                                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Thông tin hai người:                                           │
│  🤵 Chú rể:  Năm sinh [1996 ▼]  Con giáp: 🐀 Tý              │
│  👰 Cô dâu:  Năm sinh [1998 ▼]  Con giáp: 🐅 Dần              │
│                                                                  │
│  Khoảng thời gian muốn cưới:                                   │
│  Từ: [03/2026]  Đến: [06/2026]                                 │
│                                                                  │
│  [🔍 Tìm ngày đẹp]                                             │
│                                                                  │
│  ═══ KẾT QUẢ: 8 NGÀY ĐẸP ═══                                  │
│                                                                  │
│  🏆 #1 — THỨ BẢY 15/03/2026                                    │
│  ☽ 21/2 Bính Ngọ | Trực: Thành | Sao: Đẩu                     │
│  ⭐ Điểm: 9.5/10 — TUYỆT VỜI                                  │
│  ✅ Hợp tuổi cả hai  ✅ Trực Thành (tốt cho cưới hỏi)         │
│  ✅ Không phạm tam sát  ✅ Giờ đẹp: Mão (5-7h), Tị (9-11h)    │
│  [📋 Xem chi tiết] [📤 Chia sẻ]                                │
│                                                                  │
│  #2 — THỨ TƯ 25/03/2026                                        │
│  ☽ 1/3 Bính Ngọ | Trực: Khai | Sao: Giác                       │
│  ⭐ Điểm: 8.8/10 — RẤT TỐT                                    │
│  ✅ Hợp tuổi  ✅ Trực Khai  ⚠️ Giờ đẹp hạn chế                │
│  [📋 Xem chi tiết] [📤 Chia sẻ]                                │
│                                                                  │
│  #3 — ... (tiếp)                                                │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 11.8 📊 Tuổi Âm Lịch (Lunar Age Calculator)

Tính tuổi âm lịch chính xác (khác tuổi dương lịch):

```
┌─────────────────────────────────────────────────────────────────┐
│  📊 TÍNH TUỔI ÂM LỊCH                                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Ngày sinh dương lịch:  [15/06/1996]                            │
│                                                                  │
│  [Tính tuổi]                                                    │
│                                                                  │
│  ═══ KẾT QUẢ ═══                                                │
│                                                                  │
│  📅 Ngày sinh dương: 15/06/1996                                 │
│  ☽ Ngày sinh âm:    01/05/Bính Tý                              │
│                                                                  │
│  🔢 Tuổi dương lịch: 30 tuổi (tính đến 2026)                   │
│  🔢 Tuổi âm lịch:   31 tuổi (tính theo âm lịch)              │
│  🔢 Tuổi mụ:        32 tuổi (tính từ khi thụ thai)            │
│                                                                  │
│  🐀 Con giáp: Tý (Chuột)                                       │
│  🌊 Ngũ hành: Thuỷ (Giản hạ thuỷ — Nước dưới khe)            │
│  🔥 Mệnh: Thuỷ                                                  │
│  🌳 Nạp âm: Giản Hạ Thủy                                       │
│                                                                  │
│  💡 Năm 2026 (Bính Ngọ):                                       │
│  Ngũ hành năm: Hoả | Quan hệ: Thuỷ khắc Hoả ⚠️               │
│  → Cần cẩn thận trong năm nay, tránh mạo hiểm                 │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 11.9 🌤️ Tích Hợp Thời Tiết (Weather Integration — Optional)

Hiển thị thời tiết cơ bản cho ngày hiện tại:

```
┌─────────────────────────────┐
│  🌤️ Hà Nội — 10/03/2026    │
│  ☀️ 28°C / Nắng nhẹ        │
│  💧 Độ ẩm: 72%              │
│  🌬️ Gió: 12 km/h           │
│  Ngày mai: 🌧️ 25°C — Mưa  │
└─────────────────────────────┘
```

API: Sử dụng OpenWeatherMap Free tier (1000 calls/day) hoặc WeatherAPI.

### 11.10 📧 Email Digest Hàng Ngày (Newsletter)

Gửi email tóm tắt hàng ngày cho subscriber:

```
Subject: 📅 Lịch Số — Ngày 10/03/2026 | Ngày Tốt ⭐ 8/10

─────────────────────────────────────────

📅 THỨ BA — 10/03/2026
☽ 16 tháng 2 năm Bính Ngọ
⭐ Ngày Tốt (8/10) | Trực: Khai | Sao: Giác

─────────────────────────────────────────

💬 DANH NGÔN NGÀY
❝ Dân ta phải biết sử ta, cho tường gốc tích 
  nước nhà Việt Nam. ❞
  — Hồ Chí Minh

─────────────────────────────────────────

📅 NGÀY NÀY TRONG LỊCH SỬ
• 938 — Trận Bạch Đằng, Ngô Quyền đánh bại quân Nam Hán
• 1975 — Chiến dịch Tây Nguyên bắt đầu

─────────────────────────────────────────

✅ VIỆC NÊN: Cúng tế, Xuất hành, Khai trương
❌ VIỆC TRÁNH: Phá thổ, Kiện tụng

⏰ GIỜ HOÀNG ĐẠO: Tý (23-1h), Sửu (1-3h), Mão (5-7h)

─────────────────────────────────────────

📖 BÀI VIẾT HAY HÔM NAY
1. Phong tục ngày Tết Nguyên Đán (8 phút đọc)
2. Ý nghĩa các chòm sao trong tử vi (5 phút đọc)

→ Xem chi tiết tại lichso.vn

─────────────────────────────────────────
🌐 Lịch Số — lichso.vn
[Huỷ đăng ký] | [Xem trên web]
```

#### Database Schema — Newsletter

```sql
CREATE TABLE newsletter_subscribers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(200),
    user_id UUID REFERENCES users(id),
    frequency VARCHAR(20) DEFAULT 'daily',    -- 'daily', 'weekly', 'monthly'
    preferences JSONB DEFAULT '{}',           -- {"include_horoscope": true, "zodiac": "ty"}
    is_active BOOLEAN DEFAULT TRUE,
    confirmed_at TIMESTAMPTZ,
    unsubscribed_at TIMESTAMPTZ,
    last_sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

### 11.11 🏆 Streaks & Achievements (Gamification)

Khuyến khích người dùng quay lại hàng ngày:

| Achievement | Điều kiện | Badge |
|------------|-----------|-------|
| **Lịch sử gia** | Xem lịch 7 ngày liên tiếp | 📅 |
| **Nhà văn hoá** | Đọc 10 bài viết | 📚 |
| **Ghi chú siêng** | Ghi chú 30 ngày | ✍️ |
| **Khám phá** | Xem 12 tháng khác nhau | 🗺️ |
| **Chia sẻ** | Share 5 lần | 📤 |
| **Bookworm** | Đọc 50 bài viết | 🐛 |
| **Streak 30** | Truy cập 30 ngày liên tiếp | 🔥 |
| **Phong thuỷ master** | Xem phong thuỷ 100 ngày | 🧭 |

### 11.12 Tổng Hợp API Endpoints — Extended Features

| Group | Endpoints | Ghi chú |
|-------|-----------|---------|
| **Notes** | 7 | CRUD + search + stats |
| **Countdowns** | 5 | CRUD + system events |
| **Print** | 3 | Generate PDF/Image |
| **Share** | 3 | Generate share card image + OG |
| **Horoscope** | 4 | Daily + weekly + by zodiac |
| **Wedding/Good Days** | 3 | Nâng cấp existing + thêm loại |
| **Lunar Age** | 1 | Calculator |
| **Weather** | 2 | Current + forecast |
| **Newsletter** | 5 | Subscribe + manage + send |
| **Achievements** | 3 | List + progress + unlock |
| **Daily Content** | 9 | Public + Admin scheduling |
| **Related Articles** | 9 | Auto + manual + admin |
| **Total** | **54** | |

---

## 12. Cấu Trúc Mã Nguồn Mới

### 12.1 Backend — Các File Mới & Cập Nhật

```
backend/internal/
├── models/
│   ├── media.go                    🔄 UPDATED — thêm fields mới
│   ├── media_variant.go            🆕 Model cho image variants
│   ├── media_attachment.go         🆕 Model cho media–content linking
│   ├── media_album.go              🆕 Model cho albums
│   ├── media_album_item.go         🆕 Model cho album items
│   ├── media_folder.go             🆕 Model cho folder tree
│   ├── chunk_upload.go             🆕 Model cho chunk upload
│   ├── media_version.go            🆕 Model cho file versioning
│   ├── article_relation.go         🆕 Model cho bài viết liên quan
│   ├── daily_content_schedule.go   🆕 Model cho lịch nội dung theo ngày
│   ├── user_note.go                🆕 Model cho ghi chú cá nhân
│   ├── user_countdown.go           🆕 Model cho đếm ngược sự kiện
│   └── newsletter_subscriber.go    🆕 Model cho newsletter
├── handlers/
│   ├── media_handler.go            🔄 UPDATED — thêm endpoints mới
│   ├── media_album_handler.go      🆕 HTTP handlers cho albums
│   ├── media_folder_handler.go     🆕 HTTP handlers cho folders
│   ├── media_process_handler.go    🆕 HTTP handlers cho image processing
│   ├── media_chunk_handler.go      🆕 HTTP handlers cho chunk upload
│   ├── media_ai_handler.go         🆕 HTTP handlers cho AI features
│   ├── article_handler.go          🔄 UPDATED — thêm related articles endpoints
│   ├── daily_content_handler.go    🆕 HTTP handlers cho daily content
│   ├── calendar_handler.go         🔄 UPDATED — thêm content endpoint
│   ├── user_note_handler.go        🆕 HTTP handlers cho ghi chú
│   ├── user_countdown_handler.go   🆕 HTTP handlers cho đếm ngược
│   ├── horoscope_handler.go        🆕 HTTP handlers cho tử vi ngày
│   ├── share_handler.go            🆕 HTTP handlers cho share card
│   └── print_handler.go            🆕 HTTP handlers cho in lịch
├── services/
│   ├── media_service.go            🔄 UPDATED — advanced upload, search
│   ├── image_process_service.go    🆕 Resize, crop, WebP, watermark, EXIF
│   ├── video_process_service.go    🆕 Video thumbnail, metadata extraction
│   ├── media_album_service.go      🆕 Business logic cho albums
│   ├── media_folder_service.go     🆕 Business logic cho folders
│   ├── media_attachment_service.go 🆕 Business logic cho media–content link
│   ├── media_analytics_service.go  🆕 Thống kê, báo cáo media
│   ├── chunk_upload_service.go     🆕 Business logic cho chunk upload
│   ├── media_ai_service.go         🆕 AI tagging, description
│   ├── media_cleanup_service.go    🆕 Cleanup trash, expired chunks, orphans
│   ├── article_relation_service.go 🆕 Related articles engine
│   ├── daily_content_service.go    🆕 Daily content scheduling & retrieval
│   ├── user_note_service.go        🆕 Business logic cho ghi chú
│   ├── user_countdown_service.go   🆕 Business logic cho đếm ngược
│   ├── horoscope_service.go        🆕 Tử vi ngày tính theo can chi
│   ├── share_card_service.go       🆕 Generate share card images
│   └── print_service.go            🆕 Generate printable calendar PDF/PNG
├── repositories/
│   ├── media_repo.go               🔄 UPDATED — advanced queries, aggregation
│   ├── media_variant_repo.go       🆕 Data access cho variants
│   ├── media_attachment_repo.go    🆕 Data access cho attachments
│   ├── media_album_repo.go         🆕 Data access cho albums
│   ├── media_folder_repo.go        🆕 Data access cho folders
│   ├── chunk_upload_repo.go        🆕 Data access cho chunk uploads
│   ├── article_relation_repo.go    🆕 Data access cho article relations
│   ├── daily_content_repo.go       🆕 Data access cho daily content
│   ├── user_note_repo.go           🆕 Data access cho ghi chú
│   └── user_countdown_repo.go      🆕 Data access cho đếm ngược
├── routes/
│   ├── media_routes.go             🔄 UPDATED — thêm routes mới
│   ├── media_admin_routes.go       🆕 Tách routes admin media riêng
│   ├── daily_content_routes.go     🆕 Routes cho daily content
│   └── extended_feature_routes.go  🆕 Routes cho notes, countdown, share, print
├── dto/
│   ├── media_dto.go                🔄 UPDATED — thêm request/response mới
│   ├── media_album_dto.go          🆕 DTOs cho albums
│   ├── media_folder_dto.go         🆕 DTOs cho folders
│   ├── media_process_dto.go        🆕 DTOs cho image processing
│   ├── daily_content_dto.go        🆕 DTOs cho daily content
│   ├── article_relation_dto.go     🆕 DTOs cho related articles
│   └── user_note_dto.go            🆕 DTOs cho ghi chú
├── utils/
│   ├── image_utils.go              🆕 Image processing utilities
│   ├── video_utils.go              🆕 Video processing utilities
│   ├── hash_utils.go               🆕 File hashing (MD5, SHA256, pHash)
│   ├── exif_utils.go               🆕 EXIF extraction utilities
│   └── share_card_utils.go         🆕 Share card image generation
└── workers/
    ├── image_worker.go             🆕 Background image processing worker
    ├── cleanup_worker.go           🆕 Scheduled cleanup (trash, chunks)
    ├── analytics_worker.go         🆕 Scheduled analytics aggregation
    ├── daily_content_worker.go     🆕 Daily: reset random articles, auto-assign
    └── newsletter_worker.go        🆕 Daily: send email digests

backend/config/
└── config.go                       🔄 UPDATED — thêm media v3 config
```

### 12.2 Frontend — Các File Mới & Cập Nhật

```
frontend/src/
├── app/admin/
│   ├── media/
│   │   ├── page.tsx                🔄 UPDATED — Media manager nâng cấp
│   │   ├── albums/
│   │   │   ├── page.tsx            🆕 Album list page
│   │   │   ├── create/
│   │   │   │   └── page.tsx        🆕 Create album
│   │   │   └── [id]/
│   │   │       └── page.tsx        🆕 Album detail / edit
│   │   ├── folders/
│   │   │   └── page.tsx            🆕 Folder manager page
│   │   ├── trash/
│   │   │   └── page.tsx            🆕 Trash page
│   │   └── analytics/
│   │       └── page.tsx            🆕 Media analytics dashboard
│   ├── daily-content/
│   │   ├── page.tsx                🆕 Daily Content Calendar view
│   │   └── [date]/
│   │       └── page.tsx            🆕 Day content detail/edit
├── app/(home)/
│   ├── bai-viet/
│   │   └── [slug]/
│   │       └── page.tsx            🔄 UPDATED — thêm Related Articles section
│   ├── tu-vi/
│   │   └── hang-ngay/
│   │       └── page.tsx            🆕 Tử vi hàng ngày
│   ├── xem-ngay/
│   │   ├── cuoi-hoi/
│   │   │   └── page.tsx            🆕 Xem ngày cưới hỏi
│   │   ├── dong-tho/
│   │   │   └── page.tsx            🆕 Xem ngày động thổ
│   │   └── xuat-hanh/
│   │       └── page.tsx            🆕 Xem ngày xuất hành
│   ├── tinh-tuoi/
│   │   └── page.tsx                🆕 Tính tuổi âm lịch
│   └── ghi-chu/
│       └── page.tsx                🆕 Ghi chú cá nhân
├── components/
│   ├── media/
│   │   ├── MediaManager.tsx        🔄 UPDATED — Full media manager
│   │   ├── MediaGrid.tsx           🔄 UPDATED — Grid with variants
│   │   ├── MediaList.tsx           🆕 List view
│   │   ├── MediaDetailPanel.tsx    🆕 Slide-over detail panel
│   │   ├── MediaPicker.tsx         🆕 Reusable media picker dialog
│   │   ├── MediaUploader.tsx       🆕 Drag & drop uploader component
│   │   ├── ChunkUploader.tsx       🆕 Chunk upload with progress
│   │   ├── ImageEditor.tsx         🆕 Inline image crop/rotate/resize
│   │   ├── ImageCropper.tsx        🆕 Crop tool (react-image-crop)
│   │   ├── MediaFolderTree.tsx     🆕 Folder tree sidebar
│   │   ├── MediaAlbumGrid.tsx      🆕 Album grid component
│   │   ├── MediaAlbumDetail.tsx    🆕 Album detail with drag-reorder
│   │   ├── MediaTagInput.tsx       🆕 Tag input for media
│   │   ├── MediaBulkActions.tsx    🆕 Bulk operations toolbar
│   │   ├── MediaSearchBar.tsx      🆕 Advanced search bar
│   │   ├── MediaStatsCards.tsx     🆕 Stats overview cards
│   │   ├── MediaAnalytics.tsx      🆕 Charts & analytics dashboard
│   │   ├── ResponsiveImage.tsx     🆕 <picture> component with srcset
│   │   ├── VideoPlayer.tsx         🆕 Video player (upload + embed)
│   │   ├── VideoEmbed.tsx          🆕 YouTube/Vimeo embed component
│   │   └── LightboxGallery.tsx     🆕 Image lightbox / gallery viewer
│   ├── lichso/
│   │   ├── DayDetailModal.tsx      🔄 UPDATED — thêm content sections
│   │   ├── DailyQuoteCard.tsx      🆕 Hiển thị danh ngôn ngày
│   │   ├── HistoricalEvents.tsx    🆕 Timeline sự kiện lịch sử
│   │   ├── ArticleBriefCard.tsx    🆕 Brief card bài viết (click → detail)
│   │   ├── FestivalCard.tsx        🆕 Lễ hội hôm nay card
│   │   ├── BirthdayBadge.tsx       🆕 Sinh nhật người nổi tiếng
│   │   ├── ContentIndicators.tsx   🆕 Dots indicator trên calendar grid
│   │   ├── CountdownWidget.tsx     🆕 Đếm ngược sự kiện
│   │   ├── ShareCard.tsx           🆕 Share card component + buttons
│   │   ├── PrintCalendar.tsx       🆕 In lịch component
│   │   ├── DailyHoroscope.tsx      🆕 Tử vi ngày component
│   │   ├── LunarAgeCalc.tsx        🆕 Tính tuổi âm lịch
│   │   ├── WeddingDateFinder.tsx   🆕 Xem ngày cưới hỏi
│   │   └── UserNoteEditor.tsx      🆕 Ghi chú cá nhân inline
│   ├── content/
│   │   ├── ImageUpload.tsx         🔄 UPDATED — uses MediaPicker
│   │   ├── RichTextEditor.tsx      🔄 UPDATED — media insert button
│   │   ├── GalleryField.tsx        🆕 Gallery field for forms
│   │   ├── RelatedArticles.tsx     🆕 Component bài viết liên quan
│   │   ├── RelatedArticlesPicker.tsx 🆕 Admin: chọn bài viết liên quan
│   │   └── DailyContentCalendar.tsx  🆕 Admin: calendar view nội dung
├── hooks/
│   ├── useMedia.ts                 🔄 UPDATED — thêm hooks mới
│   ├── useMediaUpload.ts           🆕 Upload hooks (single, multiple, chunk)
│   ├── useMediaAlbums.ts           🆕 Album hooks
│   ├── useMediaFolders.ts          🆕 Folder hooks
│   ├── useImageEditor.ts           🆕 Image editor hooks
│   ├── useMediaAnalytics.ts        🆕 Analytics hooks
│   ├── useRelatedArticles.ts       🆕 Related articles hooks
│   ├── useDailyContent.ts          🆕 Daily content hooks
│   ├── useUserNotes.ts             🆕 User notes hooks
│   ├── useCountdowns.ts            🆕 Countdown hooks
│   └── useHoroscope.ts             🆕 Horoscope hooks
├── services/
│   ├── mediaService.ts             🔄 UPDATED — thêm API calls mới
│   ├── mediaAlbumService.ts        🆕 Album API service
│   ├── mediaFolderService.ts       🆕 Folder API service
│   ├── mediaProcessService.ts      🆕 Image processing API service
│   ├── mediaChunkService.ts        🆕 Chunk upload API service
│   ├── articleRelationService.ts   🆕 Related articles API service
│   ├── dailyContentService.ts      🆕 Daily content API service
│   ├── userNoteService.ts          🆕 User notes API service
│   ├── countdownService.ts         🆕 Countdown API service
│   └── horoscopeService.ts         🆕 Horoscope API service
├── types/
│   ├── media.ts                    🔄 UPDATED — thêm types mới
│   ├── mediaAlbum.ts               🆕 Album types
│   ├── mediaFolder.ts              🆕 Folder types
│   ├── mediaProcess.ts             🆕 Image processing types
│   ├── calendar.ts                 🔄 UPDATED — thêm DayContent, DayResponseV3
│   ├── article.ts                  🔄 UPDATED — thêm ArticleRelation
│   ├── dailyContent.ts             🆕 Daily content types
│   ├── userNote.ts                 🆕 User note types
│   └── horoscope.ts               🆕 Horoscope types
└── lib/
    ├── image-utils.ts              🆕 Client-side image utilities
    ├── video-utils.ts              🆕 Video URL parser (YouTube, Vimeo)
    ├── chunk-upload.ts             🆕 Chunk upload helper
    └── share-utils.ts              🆕 Social share utilities
```

### 12.3 Go Packages Mới

```go
// go.mod additions for v3.0

// Image processing
"github.com/disintegration/imaging"         // Resize, crop, rotate, flip
"github.com/kolesa-team/go-webp"            // WebP encoding
"github.com/rwcarlsen/goexif/exif"          // EXIF extraction
"github.com/corona10/goimagehash"           // Perceptual hash for duplicates
"github.com/buckket/go-blurhash"            // BlurHash generation
"github.com/EdlinOrg/prominern"             // Dominant color extraction

// Video processing
"github.com/u2takey/ffmpeg-go"              // FFmpeg bindings (thumbnail, metadata)

// AI (Optional — external API)
// Google Vision API / OpenAI API — via HTTP client

// File utilities
"crypto/sha256"                             // Go stdlib — file hash
"github.com/h2non/filetype"                // Better MIME type detection
```

### 12.4 NPM Packages Mới (Frontend)

```json
{
  "dependencies": {
    "react-image-crop": "^11.x",            // Image cropping component
    "react-dropzone": "^14.x",              // Drag & drop upload
    "react-easy-crop": "^5.x",              // Alternative image cropper
    "blurhash": "^2.x",                     // BlurHash decode for placeholders
    "react-photo-album": "^3.x",            // Photo gallery layout
    "yet-another-react-lightbox": "^3.x",   // Lightbox gallery viewer
    "react-player": "^2.x",                 // Video player (YouTube, Vimeo, file)
    "recharts": "^2.x",                     // Charts for analytics
    "@dnd-kit/core": "^6.x",               // Drag & drop reorder (albums)
    "@dnd-kit/sortable": "^8.x",           // Sortable lists
    "file-saver": "^2.x",                  // File download utility
    "browser-image-compression": "^2.x"     // Client-side image compression
  }
}
```

---

## 13. Chiến Lược Kỹ Thuật & Performance

### 13.1 Image Processing Pipeline

```
┌──────────────┐
│  File Upload  │
└──────┬───────┘
       │
       ▼
┌──────────────┐     ┌─────────────────┐
│  Validate     │────▶│ Reject invalid   │
│  (type, size) │     │ files            │
└──────┬───────┘     └─────────────────┘
       │
       ▼
┌──────────────┐
│  Save Original│
│  to Disk      │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  Extract EXIF │ (camera, GPS, date)
│  & Metadata   │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  Calculate    │ (SHA256, pHash)
│  File Hash    │
└──────┬───────┘
       │
       ▼
┌──────────────┐     ┌─────────────────┐
│  Check        │────▶│ Flag as duplicate│
│  Duplicates   │     │ (warn user)      │
└──────┬───────┘     └─────────────────┘
       │
       ▼
┌──────────────┐
│  Generate     │ (thumb_sm, thumb_md, thumb_lg, medium, large, og)
│  Variants     │ → WebP format
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  Generate     │ (BlurHash)
│  Blur Hash    │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  Extract      │ (dominant color)
│  Colors       │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  AI Auto-Tag  │ (Optional, async)
│  (Background) │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  Save to DB   │ (media + variants records)
│  Return URL   │
└──────────────┘
```

### 13.2 Background Workers

| Worker | Schedule | Nhiệm vụ |
|--------|----------|-----------|
| `ImageProcessWorker` | On upload (async) | Tạo variants, blurhash, EXIF |
| `CleanupWorker` | Hourly | Dọn trash (>30 ngày), expired chunks (>24h) |
| `AnalyticsWorker` | Daily | Aggregate media stats |
| `OrphanDetector` | Weekly | Tìm media không reference, broken links |
| `StorageOptimizer` | Weekly | Tìm file chưa convert WebP, ảnh quá lớn |
| `AITaggingWorker` | On upload (async) | Auto-tag ảnh (nếu bật) |

### 13.3 Caching Strategy v3.0

```
Layer 1: Nginx / CDN
├─ Image variants: Cache 1 year (fingerprinted URL)
├─ Original files: Cache 1 hour
└─ Upload endpoint: No cache

Layer 2: Redis
├─ Media metadata: TTL 30 minutes
├─ Media variants list: TTL 1 hour
├─ Folder tree: TTL 5 minutes
├─ Album list: TTL 5 minutes
├─ Media search results: TTL 5 minutes
├─ Media stats: TTL 5 minutes
├─ Chunk upload state: TTL 24 hours
└─ Duplicate hash index: Permanent

Layer 3: Next.js
├─ Media picker data: SWR with 60s revalidate
├─ Album gallery pages: ISR 5 minutes
└─ Image blur placeholders: Build-time
```

### 13.4 Storage Strategy

```
uploads/                              ← UPLOAD_PATH
├── 2026/
│   ├── 01/
│   │   ├── 20260115_abc123.jpg       ← Original
│   │   ├── 20260115_abc123_150x150.webp  ← thumb_sm
│   │   ├── 20260115_abc123_300x300.webp  ← thumb_md
│   │   ├── 20260115_abc123_600x400.webp  ← thumb_lg
│   │   ├── 20260115_abc123_768.webp      ← medium
│   │   ├── 20260115_abc123_1200.webp     ← large
│   │   └── 20260115_abc123_1200x630.webp ← og
│   ├── 02/
│   └── 03/
├── videos/
│   ├── thumbs/                       ← Video thumbnails
│   └── uploads/                      ← Uploaded videos
├── tmp/
│   └── chunks/                       ← Temporary chunk storage
│       └── upload-uuid-001/
│           ├── chunk_000
│           ├── chunk_001
│           └── ...
└── watermarks/
    └── lichso-watermark.png          ← Watermark template
```

### 13.5 Environment Variables Mới v3.0

```env
# ═══ Media v3.0 Settings ═══

# Upload limits
UPLOAD_MAX_SIZE=20971520              # 20MB for images
UPLOAD_VIDEO_MAX_SIZE=524288000       # 500MB for videos
UPLOAD_CHUNK_SIZE=5242880             # 5MB per chunk
UPLOAD_ALLOWED_TYPES=image/jpeg,image/png,image/gif,image/webp,image/svg+xml,video/mp4,video/webm,video/quicktime,application/pdf,audio/mpeg,audio/ogg

# Image processing
IMAGE_PROCESS_ASYNC=true              # Process variants in background
IMAGE_GENERATE_WEBP=true              # Auto-convert to WebP
IMAGE_GENERATE_BLURHASH=true          # Generate BlurHash
IMAGE_EXTRACT_EXIF=true               # Extract EXIF data
IMAGE_WATERMARK_ENABLED=false         # Enable watermark
IMAGE_WATERMARK_PATH=./uploads/watermarks/lichso-watermark.png
IMAGE_MAX_DIMENSION=4096              # Max width/height for processing

# Video processing
VIDEO_EXTRACT_THUMBNAIL=true          # Extract thumbnail from video
VIDEO_FFMPEG_PATH=/usr/bin/ffmpeg     # FFmpeg binary path

# Duplicate detection
MEDIA_DETECT_DUPLICATES=true
MEDIA_DUPLICATE_HASH_ALGO=sha256      # sha256, md5, phash

# Trash & cleanup
MEDIA_TRASH_RETENTION_DAYS=30         # Days before permanent delete
MEDIA_CHUNK_EXPIRY_HOURS=24           # Hours before chunk cleanup

# CDN (Optional)
CDN_ENABLED=false
CDN_BASE_URL=https://cdn.lichso.vn
CDN_PROVIDER=cloudflare               # cloudflare, aws, bunny

# AI Features (Optional)
AI_ENABLED=false
AI_PROVIDER=openai                    # openai, google_vision
AI_API_KEY=sk-xxx
AI_AUTO_TAG_ON_UPLOAD=false
```

---

## 14. Kế Hoạch Triển Khai & Roadmap

> � **Nội dung chi tiết đã được tách sang file riêng:** [Roadmap.md](./Roadmap.md)
>
> Bao gồm: Roadmap các Phase 11–24, Timeline, Ưu Tiên Phát Triển, và Tổng Kết Tiến Độ.

---

## 📎 Phụ Lục

### A. So Sánh Kiến Trúc Media v2.0 vs v3.0

| Component | v2.0 | v3.0 |
|-----------|------|------|
| **Model** | 1 model (Media) | 7 models (Media, Variant, Attachment, Album, AlbumItem, Folder, ChunkUpload, Version) |
| **Handler** | 1 file (media_handler.go) | 6 files (media, album, folder, process, chunk, ai) |
| **Service** | 1 file (media_service.go) | 9 files (media, image_process, video_process, album, folder, attachment, analytics, chunk, cleanup) |
| **Repository** | 1 file (media_repo.go) | 6 files (media, variant, attachment, album, folder, chunk) |
| **Routes** | 8 endpoints | 50+ endpoints |
| **Frontend components** | ~5 components | 20+ components |
| **Storage** | Flat file storage | Organized variants + CDN ready |
| **Content features** | CRUD cơ bản | + Related articles, Daily content, Enhanced Day Detail |
| **Calendar** | Thông tin ngày cơ bản | + Quote, Events, Articles, Festivals, Birthdays per day |
| **User features** | Bookmark, Reminder | + Notes, Countdown, Print, Share, Horoscope |

### B. Migration Plan v2.0 → v3.0

```
1. Backup toàn bộ database & uploads
2. Run MongoDB migration: thêm fields mới vào collection media
3. Run migration: tạo collections mới (media_variants, media_attachments, etc.)
4. Run PostgreSQL migration: tạo bảng article_relations
5. Run PostgreSQL migration: tạo bảng daily_content_schedule
6. Run PostgreSQL migration: tạo bảng user_notes, user_countdowns
7. Run PostgreSQL migration: tạo bảng newsletter_subscribers
8. Run one-time script: generate variants cho existing media
9. Run one-time script: calculate file hashes cho duplicate detection
10. Run one-time script: auto-fill daily content cho 365 ngày (quotes, events, birthdays)
11. Run one-time script: auto-generate related articles cho existing articles
12. Update upload config (.env)
13. Deploy updated backend
14. Deploy updated frontend
15. Verify all existing media accessible
16. Verify daily content for current month
17. Run cleanup: remove orphaned files
```

### C. Tải Kỹ Thuật Ước Tính

| Metric | Value |
|--------|-------|
| Existing media files | ~500 |
| Expected v3.0 media files | 10,000+ |
| Average original size | 2MB |
| Average total (all variants) | 3.5MB |
| Total storage (10K files) | ~35GB |
| Savings from WebP | ~30% = ~10.5GB saved |
| Actual storage needed | ~25GB |
| Image processing time | ~2-5s per image |
| Variant generation | 6 variants × 10K = 60K files |

### D. API Tổng Hợp — Quick Reference

| Group | Endpoints | New in v3 |
|-------|-----------|-----------|
| Upload | 7 | +4 (chunk, URL import) |
| CRUD | 9 | +5 (restore, trash, permanent delete) |
| Processing | 6 | +6 (all new) |
| Delivery | 4 | +4 (all new) |
| Folders | 6 | +6 (all new) |
| Albums | 8 | +8 (all new) |
| Attachments | 4 | +4 (all new) |
| Analytics | 5 | +4 (expanded) |
| AI | 4 | +4 (all new) |
| Related Articles | 9 | +9 (all new) |
| Daily Content | 12 | +12 (all new) |
| User Notes | 7 | +7 (all new) |
| Countdowns | 5 | +5 (all new) |
| Print/Share | 6 | +6 (all new) |
| Horoscope | 4 | +4 (all new) |
| Good Days (Nâng cấp) | 3 | +2 (new types) |
| Newsletter | 5 | +5 (all new) |
| **Total** | **~107** | **+99 new** |

---

> 📅 Cập nhật lần cuối: 09/03/2026  
> 📝 Tác giả: Zplus Team  
> 🏷️ Version: 3.0.0 (Media Management + Content Calendar + Extended Features)  
> 📜 Document trước: [LICHSO-V2-DOCUMENT.md](./LICHSO-V2-DOCUMENT.md) (v2.0.0)
