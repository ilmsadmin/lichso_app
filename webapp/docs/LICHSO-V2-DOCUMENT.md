# 📜 Lịch Số v2.0 — Tài Liệu Chương Trình

> **Lịch Số (曆數) v2.0** — Ứng dụng Lịch Vạn Niên Việt Nam, nền tảng nội dung văn hoá & tri thức truyền thống

---

## 📋 Mục Lục

1. [Tổng Quan Version 2.0](#1-tổng-quan-version-20)
2. [Tính Năng Mới v2.0](#2-tính-năng-mới-v20)
3. [Thiết Kế Cơ Sở Dữ Liệu](#3-thiết-kế-cơ-sở-dữ-liệu)
4. [API Endpoints Mới](#4-api-endpoints-mới)
5. [Thiết Kế Giao Diện Người Dùng](#5-thiết-kế-giao-diện-người-dùng)
6. [Trang Quản Trị (Admin Panel)](#6-trang-quản-trị-admin-panel)
7. [Cấu Trúc Mã Nguồn Mới](#7-cấu-trúc-mã-nguồn-mới)
8. [Chiến Lược Dữ Liệu & SEO](#8-chiến-lược-dữ-liệu--seo)
9. [Kế Hoạch Triển Khai](#9-kế-hoạch-triển-khai)
10. [Roadmap v2.0](#10-roadmap-v20)

---

## 1. Tổng Quan Version 2.0

### 1.1 Tầm Nhìn

**Lịch Số v2.0** chuyển mình từ một ứng dụng **tra cứu lịch** thuần tuý thành một **nền tảng nội dung văn hoá & tri thức** (Content Platform), nơi người dùng không chỉ xem lịch mà còn được truyền cảm hứng mỗi ngày thông qua:

- 🏛️ Câu chuyện về **nhân vật lịch sử** nổi tiếng thế giới & Việt Nam
- 📅 **Sự kiện lịch sử**, ngày kỷ niệm, quốc khánh các nước
- 🎎 **Lễ hội dân gian** truyền thống Việt Nam & thế giới
- 💬 **Châm ngôn nổi tiếng** truyền cảm hứng mỗi ngày
- 📝 **Bài viết chuyên sâu** về nhân vật, sự kiện, lễ hội, tử vi, phong thuỷ

### 1.2 So Sánh v1.x vs v2.0

| Khía cạnh | v1.x (Hiện tại) | v2.0 (Mới) |
|-----------|-----------------|-------------|
| **Bản chất** | Công cụ tra cứu lịch | Nền tảng nội dung + Lịch |
| **Nội dung** | Dữ liệu tính toán (Âm Dương, Can Chi) | + Bài viết, sự kiện, nhân vật |
| **Mỗi ngày** | Thông tin phong thuỷ | + Châm ngôn, sinh nhật nhân vật, sự kiện ngày này |
| **Admin** | Quản lý users, roles, media | + Quản lý bài viết, sự kiện, nhân vật, châm ngôn |
| **SEO** | Trang đơn | Hàng nghìn trang nội dung (articles, events) |
| **Engagement** | Xem rồi đi | Đọc bài → khám phá → quay lại |

### 1.3 Mục Tiêu v2.0

| Mục tiêu | KPI |
|-----------|-----|
| **Nội dung phong phú** | 500+ bài viết, 1000+ sự kiện, 365+ châm ngôn |
| **SEO ranking** | Top 3 cho "lịch vạn niên", "ngày hôm nay âm lịch" |
| **User engagement** | Thời gian trung bình > 3 phút/session |
| **Quay lại hàng ngày** | > 30% returning visitors |
| **Admin hiệu quả** | Thêm nội dung < 2 phút/bài viết |

---

## 2. Tính Năng Mới v2.0

### 2.1 💬 Câu Châm Ngôn Nổi Tiếng (Quotes)

#### Mô tả
Mỗi ngày hiển thị một câu châm ngôn truyền cảm hứng từ các nhân vật nổi tiếng thế giới và Việt Nam. Châm ngôn được gắn theo ngày trong năm (day of year) hoặc theo chủ đề ngẫu nhiên.

#### Tính năng chi tiết

| Tính năng | Mô tả |
|-----------|--------|
| **Châm ngôn ngày** | Mỗi ngày có 1 câu châm ngôn chính hiển thị trên trang chủ |
| **Nguồn gốc** | Tên tác giả, năm sinh/mất, quốc tịch |
| **Đa ngôn ngữ** | Câu gốc (tiếng Anh/Hán/...) + bản dịch tiếng Việt |
| **Phân loại** | Tags: triết học, tình yêu, cuộc sống, thành công, giáo dục... |
| **Chia sẻ** | Nút chia sẻ lên Facebook, Twitter, copy text |
| **Yêu thích** | Bookmark câu châm ngôn hay (cần đăng nhập) |
| **Random** | Nút "Xem câu khác" để lấy ngẫu nhiên |

#### Dữ liệu mẫu

```json
{
  "quote": "Hãy là sự thay đổi mà bạn muốn nhìn thấy trong thế giới.",
  "originalQuote": "Be the change you wish to see in the world.",
  "originalLanguage": "en",
  "author": "Mahatma Gandhi",
  "authorBio": "Nhà lãnh đạo phong trào độc lập Ấn Độ",
  "authorBirthYear": 1869,
  "authorDeathYear": 1948,
  "authorNationality": "Ấn Độ",
  "authorImageUrl": "/uploads/authors/gandhi.jpg",
  "tags": ["cuộc sống", "thay đổi", "triết học"],
  "dayOfYear": 66,
  "isActive": true
}
```

---

### 2.2 🎂 Ngày Sinh Nhân Vật Nổi Tiếng (Famous Birthdays)

#### Mô tả
Hiển thị danh sách nhân vật nổi tiếng sinh vào ngày hôm nay (theo Dương lịch). Bao gồm nhân vật lịch sử Việt Nam, thế giới, khoa học, nghệ thuật, thể thao, chính trị...

#### Tính năng chi tiết

| Tính năng | Mô tả |
|-----------|--------|
| **Sinh nhật hôm nay** | Card nhân vật nổi tiếng sinh cùng ngày |
| **Thông tin cơ bản** | Tên, năm sinh/mất, quốc tịch, nghề nghiệp |
| **Ảnh đại diện** | Avatar nhân vật |
| **Mô tả ngắn** | 1-2 câu tóm tắt đóng góp/thành tựu |
| **Liên kết bài viết** | Link đến bài viết chi tiết (nếu có) |
| **Phân loại** | Danh mục: Chính trị, Khoa học, Nghệ thuật, Thể thao, Văn học, Quân sự... |
| **Lọc theo quốc gia** | Ưu tiên nhân vật Việt Nam, có thể lọc theo nước |
| **Timeline sinh nhật** | Xem sinh nhật trong tháng này |

#### Dữ liệu mẫu

```json
{
  "name": "Hồ Chí Minh",
  "originalName": "Nguyễn Sinh Cung",
  "birthDate": "1890-05-19",
  "deathDate": "1969-09-02",
  "nationality": "Việt Nam",
  "occupation": "Nhà cách mạng, Chính trị gia",
  "category": "chinh_tri",
  "shortBio": "Chủ tịch nước Việt Nam Dân chủ Cộng hòa, lãnh tụ phong trào giải phóng dân tộc Việt Nam.",
  "imageUrl": "/uploads/people/ho-chi-minh.jpg",
  "articleSlug": "ho-chi-minh-cuoc-doi-va-su-nghiep",
  "isVietnamese": true,
  "tags": ["lãnh tụ", "cách mạng", "Việt Nam"],
  "isActive": true
}
```

---

### 2.3 📅 Ngày Kỷ Niệm, Sự Kiện & Quốc Khánh (Events & Holidays)

#### Mô tả
Hiển thị các sự kiện lịch sử nổi tiếng, ngày kỷ niệm quốc tế, quốc khánh các nước xảy ra vào ngày hôm nay.

#### Phân loại sự kiện

| Loại | Mô tả | Ví dụ |
|------|--------|-------|
| `historical_event` | Sự kiện lịch sử | Chiến thắng Điện Biên Phủ (07/05/1954) |
| `national_day` | Quốc khánh các nước | Quốc khánh Việt Nam (02/09) |
| `international_day` | Ngày quốc tế | Ngày Quốc tế Phụ nữ (08/03) |
| `memorial_day` | Ngày tưởng niệm | Ngày Thương binh Liệt sĩ (27/07) |
| `cultural_event` | Sự kiện văn hoá | Ngày Nhà giáo Việt Nam (20/11) |
| `scientific_event` | Sự kiện khoa học | Neil Armstrong đặt chân lên Mặt Trăng (20/07/1969) |

#### Tính năng chi tiết

| Tính năng | Mô tả |
|-----------|--------|
| **Sự kiện hôm nay** | Danh sách sự kiện xảy ra ngày này trong lịch sử |
| **Quốc khánh** | Quốc khánh các nước rơi vào ngày hôm nay, có quốc kỳ |
| **Ngày quốc tế** | Ngày do UN/UNESCO công nhận |
| **Countdown** | Đếm ngược đến ngày kỷ niệm sắp tới |
| **Lọc & tìm kiếm** | Theo năm, quốc gia, loại sự kiện |
| **Liên kết bài viết** | Link đến bài viết chi tiết |
| **Lịch sự kiện** | Calendar view đánh dấu ngày có sự kiện |

#### Dữ liệu mẫu

```json
{
  "title": "Chiến thắng Điện Biên Phủ",
  "slug": "chien-thang-dien-bien-phu",
  "eventDate": "1954-05-07",
  "eventDay": 7,
  "eventMonth": 5,
  "eventYear": 1954,
  "isRecurring": true,
  "eventType": "historical_event",
  "country": "Việt Nam",
  "countryCode": "VN",
  "shortDescription": "Chiến thắng lịch sử kết thúc 9 năm kháng chiến chống thực dân Pháp.",
  "imageUrl": "/uploads/events/dien-bien-phu.jpg",
  "articleSlug": "chien-thang-dien-bien-phu-1954",
  "importance": "high",
  "tags": ["lịch sử", "chiến tranh", "Việt Nam", "Pháp"],
  "isActive": true
}
```

---

### 2.4 🎎 Ngày Lễ Hội Dân Gian (Folk Festivals)

#### Mô tả
Hiển thị thông tin về các lễ hội dân gian truyền thống Việt Nam và một số nước Đông Á, bao gồm cả lễ hội theo Âm lịch và Dương lịch.

#### Phân loại lễ hội

| Loại | Mô tả | Ví dụ |
|------|--------|-------|
| `tet_holiday` | Lễ Tết | Tết Nguyên Đán, Tết Trung Thu |
| `folk_festival` | Lễ hội dân gian | Hội Lim, Lễ hội Đền Hùng |
| `religious_festival` | Lễ hội tôn giáo | Lễ Vu Lan, Lễ Phật Đản |
| `regional_festival` | Lễ hội vùng miền | Lễ hội Kate (Ninh Thuận), Lễ hội Bà Chúa Xứ |
| `east_asian_festival` | Lễ hội Đông Á | Lễ Thất Tịch, Tiết Thanh Minh |
| `worship_day` | Ngày cúng/giỗ | Ngày Rằm, Mùng Một, Giỗ Tổ Hùng Vương |

#### Tính năng chi tiết

| Tính năng | Mô tả |
|-----------|--------|
| **Lễ hội hôm nay** | Lễ hội diễn ra hôm nay (Âm hoặc Dương) |
| **Lễ hội sắp tới** | Danh sách lễ hội sắp diễn ra, countdown |
| **Hỗ trợ Âm lịch** | Xác định ngày lễ theo Âm lịch (vd: Rằm tháng Giêng) |
| **Thông tin chi tiết** | Nguồn gốc, ý nghĩa, phong tục, địa điểm |
| **Hình ảnh** | Gallery ảnh minh hoạ lễ hội |
| **Vùng miền** | Tag vùng miền: Bắc, Trung, Nam |
| **Liên kết bài viết** | Link đến bài viết chi tiết |

#### Dữ liệu mẫu

```json
{
  "name": "Tết Trung Thu",
  "slug": "tet-trung-thu",
  "alternateName": "Tết Thiếu Nhi, Tết Trông Trăng",
  "calendarType": "lunar",
  "lunarDay": 15,
  "lunarMonth": 8,
  "solarDay": null,
  "solarMonth": null,
  "festivalType": "tet_holiday",
  "region": "Toàn quốc",
  "country": "Việt Nam",
  "shortDescription": "Lễ hội truyền thống dành cho thiếu nhi, rước đèn, phá cỗ, ngắm trăng.",
  "traditions": ["Rước đèn", "Phá cỗ", "Múa lân", "Làm bánh Trung Thu"],
  "imageUrl": "/uploads/festivals/trung-thu.jpg",
  "galleryUrls": ["/uploads/festivals/trung-thu-1.jpg", "/uploads/festivals/trung-thu-2.jpg"],
  "articleSlug": "tet-trung-thu-nguon-goc-va-y-nghia",
  "importance": "high",
  "tags": ["Tết", "Trung Thu", "thiếu nhi", "truyền thống"],
  "isActive": true
}
```

---

### 2.5 📝 Bài Viết Chi Tiết (Articles / Blog)

#### Mô tả
Hệ thống bài viết chuyên sâu, giới thiệu chi tiết về nhân vật lịch sử, sự kiện, lễ hội dân gian, tử vi, phong thuỷ, và các chủ đề liên quan đến văn hoá truyền thống.

#### Phân loại bài viết

| Danh mục | Slug | Mô tả | Ví dụ |
|----------|------|--------|-------|
| **Nhân vật lịch sử** | `nhan-vat` | Tiểu sử, đóng góp nhân vật nổi tiếng | "Trần Hưng Đạo — Vị anh hùng dân tộc" |
| **Sự kiện lịch sử** | `su-kien` | Diễn biến, ý nghĩa sự kiện | "Cách mạng Tháng Tám 1945" |
| **Lễ hội dân gian** | `le-hoi` | Nguồn gốc, phong tục, ý nghĩa | "Lễ hội Đền Hùng — Giỗ Tổ Hùng Vương" |
| **Tử vi** | `tu-vi` | Tử vi 12 con giáp, tử vi hàng ngày/tháng/năm | "Tử vi tuổi Thìn năm 2026" |
| **Phong thuỷ** | `phong-thuy` | Hướng dẫn phong thuỷ nhà cửa, công việc | "Phong thuỷ bàn làm việc năm 2026" |
| **Ngày tốt** | `ngay-tot` | Hướng dẫn chọn ngày tốt cho sự kiện | "Ngày tốt cưới hỏi tháng 3/2026" |
| **Văn hoá** | `van-hoa` | Bài viết văn hoá truyền thống | "24 Tiết Khí — Ý nghĩa và nguồn gốc" |
| **Kiến thức** | `kien-thuc` | Bài viết chia sẻ kiến thức tổng hợp | "Cách tính ngày Âm lịch chính xác" |

#### Tính năng chi tiết

| Tính năng | Mô tả |
|-----------|--------|
| **Rich text editor** | Soạn thảo bài viết với WYSIWYG editor (TipTap/Quill) |
| **SEO fields** | Meta title, meta description, canonical URL, OG image |
| **Slug tự động** | Tạo slug từ tiêu đề, có thể chỉnh sửa |
| **Ảnh bìa** | Featured image cho bài viết |
| **Tags & Categories** | Đa danh mục, đa tag |
| **Bài liên quan** | Tự động gợi ý bài viết liên quan |
| **Trạng thái** | Draft → Review → Published → Archived |
| **Lên lịch đăng** | Hẹn giờ publish bài viết |
| **Lượt xem** | Đếm view, bài viết phổ biến |
| **Chia sẻ** | Social sharing buttons |
| **Mục lục tự động** | Table of contents từ headings |
| **Thời gian đọc** | Tính tự động từ word count |
| **Tác giả** | Liên kết với user (admin/editor) |

#### Dữ liệu mẫu

```json
{
  "title": "Tết Trung Thu — Nguồn Gốc và Ý Nghĩa Truyền Thống",
  "slug": "tet-trung-thu-nguon-goc-va-y-nghia",
  "excerpt": "Tìm hiểu nguồn gốc, ý nghĩa và phong tục truyền thống của Tết Trung Thu trong văn hoá Việt Nam.",
  "content": "<h2>Nguồn gốc Tết Trung Thu</h2><p>Tết Trung Thu có nguồn gốc từ...</p>...",
  "featuredImage": "/uploads/articles/trung-thu-cover.jpg",
  "category": "le-hoi",
  "tags": ["Trung Thu", "lễ hội", "truyền thống", "thiếu nhi"],
  "authorId": "user-uuid-001",
  "status": "published",
  "publishedAt": "2026-09-15T08:00:00Z",
  "scheduledAt": null,
  "metaTitle": "Tết Trung Thu — Nguồn Gốc, Ý Nghĩa & Phong Tục | Lịch Số",
  "metaDescription": "Khám phá nguồn gốc và ý nghĩa Tết Trung Thu, lễ hội truyền thống...",
  "ogImage": "/uploads/articles/trung-thu-og.jpg",
  "viewCount": 12450,
  "readingTime": 8,
  "relatedArticles": ["le-hoi-den-hung", "tet-nguyen-dan-phong-tuc"],
  "isActive": true
}
```

---

## 3. Thiết Kế Cơ Sở Dữ Liệu

### 3.1 Sơ Đồ Quan Hệ (ERD) — Các Bảng Mới

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        LICHSO v2.0 — NEW TABLES                         │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────┐     ┌─────────────────┐     ┌──────────────────┐       │
│  │   quotes     │     │  famous_people   │     │     events       │       │
│  ├─────────────┤     ├─────────────────┤     ├──────────────────┤       │
│  │ id (PK)     │     │ id (PK)         │     │ id (PK)          │       │
│  │ quote       │     │ name            │     │ title            │       │
│  │ original_   │     │ original_name   │     │ slug             │       │
│  │   quote     │     │ birth_date      │     │ event_date       │       │
│  │ original_   │     │ death_date      │     │ event_day        │       │
│  │   language  │     │ nationality     │     │ event_month      │       │
│  │ author      │     │ occupation      │     │ event_year       │       │
│  │ author_bio  │     │ category        │     │ is_recurring     │       │
│  │ tags        │     │ short_bio       │     │ event_type       │       │
│  │ day_of_year │     │ image_url       │     │ country          │       │
│  │ is_active   │     │ article_id (FK) │──┐  │ country_code     │       │
│  │ created_at  │     │ is_vietnamese   │  │  │ short_desc       │       │
│  │ updated_at  │     │ tags            │  │  │ image_url        │       │
│  └─────────────┘     │ is_active       │  │  │ article_id (FK)  │──┐    │
│                       │ created_at      │  │  │ importance       │  │    │
│                       │ updated_at      │  │  │ tags             │  │    │
│                       └─────────────────┘  │  │ is_active        │  │    │
│                                             │  │ created_at       │  │    │
│  ┌──────────────────┐                      │  │ updated_at       │  │    │
│  │  folk_festivals   │                      │  └──────────────────┘  │    │
│  ├──────────────────┤                      │                         │    │
│  │ id (PK)          │                      │                         │    │
│  │ name             │                      │  ┌──────────────────┐   │    │
│  │ slug             │                      │  │    articles       │   │    │
│  │ alternate_name   │                      │  ├──────────────────┤   │    │
│  │ calendar_type    │                      │  │ id (PK)          │◄──┘    │
│  │ lunar_day        │                      │  │ title            │◄───    │
│  │ lunar_month      │                      │  │ slug (UNIQUE)    │        │
│  │ solar_day        │                      │  │ excerpt          │        │
│  │ solar_month      │                      │  │ content (TEXT)   │        │
│  │ festival_type    │                      │  │ featured_image   │        │
│  │ region           │                      │  │ category         │        │
│  │ country          │                      │  │ tags             │        │
│  │ short_desc       │                      │  │ author_id (FK)   │→ users │
│  │ traditions       │                      │  │ status           │        │
│  │ image_url        │                      │  │ published_at     │        │
│  │ gallery_urls     │                      │  │ scheduled_at     │        │
│  │ article_id (FK)  │──────────────────────┘  │ meta_title       │        │
│  │ importance       │                         │ meta_description │        │
│  │ tags             │                         │ og_image         │        │
│  │ is_active        │                         │ view_count       │        │
│  │ created_at       │                         │ reading_time     │        │
│  │ updated_at       │                         │ is_active        │        │
│  └──────────────────┘                         │ created_at       │        │
│                                                │ updated_at       │        │
│                                                └──────────────────┘        │
│                                                                            │
│  ┌────────────────────┐     ┌──────────────────────┐                      │
│  │  article_tags       │     │  article_categories   │                      │
│  ├────────────────────┤     ├──────────────────────┤                      │
│  │ id (PK)            │     │ id (PK)              │                      │
│  │ name               │     │ name                 │                      │
│  │ slug (UNIQUE)      │     │ slug (UNIQUE)        │                      │
│  │ description        │     │ description          │                      │
│  │ article_count      │     │ parent_id (FK, self) │                      │
│  │ created_at         │     │ icon                 │                      │
│  │ updated_at         │     │ sort_order           │                      │
│  └────────────────────┘     │ is_active            │                      │
│                              │ created_at           │                      │
│                              │ updated_at           │                      │
│                              └──────────────────────┘                      │
└──────────────────────────────────────────────────────────────────────────┘
```

### 3.2 SQL Migrations

#### Migration 011: Bảng `quotes` — Câu Châm Ngôn

```sql
-- 000011_create_quotes_table.up.sql

CREATE TABLE IF NOT EXISTS quotes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quote TEXT NOT NULL,
    original_quote TEXT,
    original_language VARCHAR(10) DEFAULT 'vi',
    author VARCHAR(255) NOT NULL,
    author_bio TEXT,
    author_birth_year INT,
    author_death_year INT,
    author_nationality VARCHAR(100),
    author_image_url VARCHAR(500),
    tags TEXT[] DEFAULT '{}',
    day_of_year INT,                          -- 1-366, null = random pool
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_quotes_day_of_year ON quotes(day_of_year) WHERE is_active = true;
CREATE INDEX idx_quotes_author ON quotes(author);
CREATE INDEX idx_quotes_tags ON quotes USING GIN(tags);
```

#### Migration 012: Bảng `famous_people` — Nhân Vật Nổi Tiếng

```sql
-- 000012_create_famous_people_table.up.sql

CREATE TYPE person_category AS ENUM (
    'chinh_tri', 'khoa_hoc', 'nghe_thuat', 'van_hoc', 
    'the_thao', 'quan_su', 'ton_giao', 'kinh_doanh',
    'am_nhac', 'dien_anh', 'giao_duc', 'y_hoc', 'khac'
);

CREATE TABLE IF NOT EXISTS famous_people (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255),
    birth_date DATE,
    birth_day INT,                             -- 1-31
    birth_month INT,                           -- 1-12
    birth_year INT,
    death_date DATE,
    nationality VARCHAR(100),
    occupation VARCHAR(500),
    category person_category DEFAULT 'khac',
    short_bio TEXT,
    image_url VARCHAR(500),
    article_id UUID REFERENCES articles(id) ON DELETE SET NULL,
    is_vietnamese BOOLEAN DEFAULT false,
    tags TEXT[] DEFAULT '{}',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_famous_people_birthday ON famous_people(birth_month, birth_day) WHERE is_active = true;
CREATE INDEX idx_famous_people_category ON famous_people(category);
CREATE INDEX idx_famous_people_nationality ON famous_people(nationality);
CREATE INDEX idx_famous_people_vietnamese ON famous_people(is_vietnamese) WHERE is_vietnamese = true;
CREATE INDEX idx_famous_people_tags ON famous_people USING GIN(tags);
```

#### Migration 013: Bảng `events` — Sự Kiện & Kỷ Niệm

```sql
-- 000013_create_events_table.up.sql

CREATE TYPE event_type AS ENUM (
    'historical_event', 'national_day', 'international_day',
    'memorial_day', 'cultural_event', 'scientific_event'
);

CREATE TYPE event_importance AS ENUM ('low', 'medium', 'high', 'critical');

CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    slug VARCHAR(500) NOT NULL UNIQUE,
    event_date DATE,                           -- Ngày gốc (nếu biết chính xác)
    event_day INT NOT NULL,                    -- 1-31
    event_month INT NOT NULL,                  -- 1-12
    event_year INT,                            -- null = recurring annually
    is_recurring BOOLEAN DEFAULT true,
    event_type event_type NOT NULL,
    country VARCHAR(100),
    country_code VARCHAR(5),
    flag_emoji VARCHAR(10),
    short_description TEXT,
    image_url VARCHAR(500),
    article_id UUID REFERENCES articles(id) ON DELETE SET NULL,
    importance event_importance DEFAULT 'medium',
    tags TEXT[] DEFAULT '{}',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_events_date ON events(event_month, event_day) WHERE is_active = true;
CREATE INDEX idx_events_type ON events(event_type);
CREATE INDEX idx_events_country ON events(country_code);
CREATE INDEX idx_events_importance ON events(importance);
CREATE INDEX idx_events_slug ON events(slug);
CREATE INDEX idx_events_tags ON events USING GIN(tags);
```

#### Migration 014: Bảng `folk_festivals` — Lễ Hội Dân Gian

```sql
-- 000014_create_folk_festivals_table.up.sql

CREATE TYPE calendar_type AS ENUM ('lunar', 'solar', 'both');

CREATE TYPE festival_type AS ENUM (
    'tet_holiday', 'folk_festival', 'religious_festival',
    'regional_festival', 'east_asian_festival', 'worship_day'
);

CREATE TABLE IF NOT EXISTS folk_festivals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(500) NOT NULL,
    slug VARCHAR(500) NOT NULL UNIQUE,
    alternate_name VARCHAR(500),
    calendar_type calendar_type NOT NULL DEFAULT 'lunar',
    lunar_day INT,                             -- 1-30 (Âm lịch)
    lunar_month INT,                           -- 1-12 (Âm lịch)
    solar_day INT,                             -- 1-31 (Dương lịch)
    solar_month INT,                           -- 1-12 (Dương lịch)
    duration_days INT DEFAULT 1,               -- Số ngày diễn ra
    festival_type festival_type NOT NULL,
    region VARCHAR(255),                       -- "Toàn quốc", "Bắc Bộ", "Ninh Thuận"
    country VARCHAR(100) DEFAULT 'Việt Nam',
    short_description TEXT,
    traditions TEXT[] DEFAULT '{}',             -- Phong tục
    image_url VARCHAR(500),
    gallery_urls TEXT[] DEFAULT '{}',
    article_id UUID REFERENCES articles(id) ON DELETE SET NULL,
    importance event_importance DEFAULT 'medium',
    tags TEXT[] DEFAULT '{}',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_festivals_lunar ON folk_festivals(lunar_month, lunar_day) WHERE calendar_type IN ('lunar', 'both') AND is_active = true;
CREATE INDEX idx_festivals_solar ON folk_festivals(solar_month, solar_day) WHERE calendar_type IN ('solar', 'both') AND is_active = true;
CREATE INDEX idx_festivals_type ON folk_festivals(festival_type);
CREATE INDEX idx_festivals_slug ON folk_festivals(slug);
CREATE INDEX idx_festivals_tags ON folk_festivals USING GIN(tags);
```

#### Migration 015: Bảng `article_categories` — Danh Mục Bài Viết

```sql
-- 000015_create_article_categories_table.up.sql

CREATE TABLE IF NOT EXISTS article_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parent_id UUID REFERENCES article_categories(id) ON DELETE SET NULL,
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_article_categories_slug ON article_categories(slug);
CREATE INDEX idx_article_categories_parent ON article_categories(parent_id);
```

#### Migration 016: Bảng `article_tags` — Tags Bài Viết

```sql
-- 000016_create_article_tags_table.up.sql

CREATE TABLE IF NOT EXISTS article_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    article_count INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_article_tags_slug ON article_tags(slug);
```

#### Migration 017: Bảng `articles` — Bài Viết

```sql
-- 000017_create_articles_table.up.sql

CREATE TYPE article_status AS ENUM ('draft', 'review', 'published', 'archived');

CREATE TABLE IF NOT EXISTS articles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    slug VARCHAR(500) NOT NULL UNIQUE,
    excerpt TEXT,
    content TEXT NOT NULL,
    featured_image VARCHAR(500),
    category_id UUID REFERENCES article_categories(id) ON DELETE SET NULL,
    tag_ids UUID[] DEFAULT '{}',
    author_id UUID REFERENCES users(id) ON DELETE SET NULL,
    status article_status DEFAULT 'draft',
    published_at TIMESTAMPTZ,
    scheduled_at TIMESTAMPTZ,
    meta_title VARCHAR(255),
    meta_description VARCHAR(500),
    og_image VARCHAR(500),
    view_count INT DEFAULT 0,
    reading_time INT DEFAULT 0,               -- phút
    is_featured BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_articles_slug ON articles(slug);
CREATE INDEX idx_articles_status ON articles(status) WHERE is_active = true;
CREATE INDEX idx_articles_category ON articles(category_id);
CREATE INDEX idx_articles_author ON articles(author_id);
CREATE INDEX idx_articles_published ON articles(published_at DESC) WHERE status = 'published';
CREATE INDEX idx_articles_featured ON articles(is_featured) WHERE is_featured = true AND status = 'published';
CREATE INDEX idx_articles_view_count ON articles(view_count DESC);

-- Full-text search index
CREATE INDEX idx_articles_search ON articles USING GIN(
    to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(excerpt, '') || ' ' || coalesce(content, ''))
);
```

#### Migration 018: Bảng liên kết `article_tag_relations`

```sql
-- 000018_create_article_tag_relations_table.up.sql

CREATE TABLE IF NOT EXISTS article_tag_relations (
    article_id UUID REFERENCES articles(id) ON DELETE CASCADE,
    tag_id UUID REFERENCES article_tags(id) ON DELETE CASCADE,
    PRIMARY KEY (article_id, tag_id)
);

CREATE INDEX idx_article_tag_article ON article_tag_relations(article_id);
CREATE INDEX idx_article_tag_tag ON article_tag_relations(tag_id);
```

### 3.3 MongoDB Collections (Bổ sung)

```javascript
// Collection: article_views — Tracking chi tiết lượt xem
{
  articleId: ObjectId,
  userId: ObjectId | null,       // null = anonymous
  ipAddress: "xxx.xxx.xxx.xxx",
  userAgent: "...",
  referrer: "https://google.com",
  viewedAt: ISODate("2026-03-06T10:00:00Z"),
  readDuration: 120              // seconds
}

// Collection: content_analytics — Thống kê nội dung
{
  date: "2026-03-06",
  totalViews: 5420,
  topArticles: [
    { articleId: "...", views: 340, title: "..." }
  ],
  topCategories: [
    { category: "le-hoi", views: 1200 }
  ],
  newArticles: 3,
  searchQueries: [
    { query: "tết trung thu", count: 45 }
  ]
}
```

### 3.4 Redis Keys (Bổ sung)

```
# Cache bài viết phổ biến
cache:articles:popular              → JSON array (TTL: 1h)

# Cache châm ngôn ngày
cache:quote:day:{dayOfYear}         → JSON object (TTL: 24h)

# Cache sự kiện ngày
cache:events:date:{month}:{day}     → JSON array (TTL: 24h)

# Cache sinh nhật ngày
cache:birthdays:date:{month}:{day}  → JSON array (TTL: 24h)

# Cache lễ hội tháng
cache:festivals:month:{month}       → JSON array (TTL: 24h)

# Cache bài viết theo slug
cache:article:{slug}                → JSON object (TTL: 30m)

# View counter (batch update to PostgreSQL)
counter:article:views:{articleId}   → INT (flush every 5min)

# Full-text search cache
cache:search:{queryHash}            → JSON array (TTL: 15m)
```

---

## 4. API Endpoints Mới

### 4.1 Quotes API — Châm Ngôn

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| GET | `/api/v2/quotes/today` | Châm ngôn ngày hôm nay | ❌ |
| GET | `/api/v2/quotes/random` | Châm ngôn ngẫu nhiên | ❌ |
| GET | `/api/v2/quotes?tag=triết-học&page=1` | Danh sách châm ngôn (có filter) | ❌ |
| GET | `/api/v2/quotes/:id` | Chi tiết châm ngôn | ❌ |
| POST | `/api/v2/admin/quotes` | Tạo châm ngôn mới | ✅ Admin |
| PUT | `/api/v2/admin/quotes/:id` | Cập nhật châm ngôn | ✅ Admin |
| DELETE | `/api/v2/admin/quotes/:id` | Xoá châm ngôn | ✅ Admin |
| POST | `/api/v2/admin/quotes/import` | Import CSV/JSON hàng loạt | ✅ Admin |

#### Request/Response mẫu

```
GET /api/v2/quotes/today

Response 200:
{
  "success": true,
  "data": {
    "id": "uuid-001",
    "quote": "Hãy là sự thay đổi mà bạn muốn nhìn thấy trong thế giới.",
    "originalQuote": "Be the change you wish to see in the world.",
    "originalLanguage": "en",
    "author": "Mahatma Gandhi",
    "authorBio": "Nhà lãnh đạo phong trào độc lập Ấn Độ (1869–1948)",
    "authorImageUrl": "/uploads/authors/gandhi.jpg",
    "tags": ["cuộc sống", "thay đổi"]
  }
}
```

### 4.2 Famous People API — Nhân Vật Nổi Tiếng

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| GET | `/api/v2/people/birthdays/today` | Sinh nhật hôm nay | ❌ |
| GET | `/api/v2/people/birthdays/:month/:day` | Sinh nhật ngày cụ thể | ❌ |
| GET | `/api/v2/people?category=khoa_hoc&nationality=Việt+Nam` | Danh sách (filter) | ❌ |
| GET | `/api/v2/people/:id` | Chi tiết nhân vật | ❌ |
| POST | `/api/v2/admin/people` | Tạo nhân vật mới | ✅ Admin |
| PUT | `/api/v2/admin/people/:id` | Cập nhật nhân vật | ✅ Admin |
| DELETE | `/api/v2/admin/people/:id` | Xoá nhân vật | ✅ Admin |
| POST | `/api/v2/admin/people/import` | Import hàng loạt | ✅ Admin |

### 4.3 Events API — Sự Kiện & Kỷ Niệm

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| GET | `/api/v2/events/today` | Sự kiện ngày hôm nay | ❌ |
| GET | `/api/v2/events/date/:month/:day` | Sự kiện ngày cụ thể | ❌ |
| GET | `/api/v2/events/month/:year/:month` | Sự kiện trong tháng | ❌ |
| GET | `/api/v2/events?type=national_day&country=VN` | Danh sách (filter) | ❌ |
| GET | `/api/v2/events/:slug` | Chi tiết sự kiện | ❌ |
| GET | `/api/v2/events/upcoming?limit=10` | Sự kiện sắp tới | ❌ |
| POST | `/api/v2/admin/events` | Tạo sự kiện | ✅ Admin |
| PUT | `/api/v2/admin/events/:id` | Cập nhật sự kiện | ✅ Admin |
| DELETE | `/api/v2/admin/events/:id` | Xoá sự kiện | ✅ Admin |
| POST | `/api/v2/admin/events/import` | Import hàng loạt | ✅ Admin |

### 4.4 Folk Festivals API — Lễ Hội Dân Gian

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| GET | `/api/v2/festivals/today` | Lễ hội hôm nay | ❌ |
| GET | `/api/v2/festivals/upcoming?limit=10` | Lễ hội sắp tới | ❌ |
| GET | `/api/v2/festivals/month/:month` | Lễ hội trong tháng (Âm/Dương) | ❌ |
| GET | `/api/v2/festivals?type=tet_holiday&region=Bắc` | Danh sách (filter) | ❌ |
| GET | `/api/v2/festivals/:slug` | Chi tiết lễ hội | ❌ |
| POST | `/api/v2/admin/festivals` | Tạo lễ hội | ✅ Admin |
| PUT | `/api/v2/admin/festivals/:id` | Cập nhật lễ hội | ✅ Admin |
| DELETE | `/api/v2/admin/festivals/:id` | Xoá lễ hội | ✅ Admin |

### 4.5 Articles API — Bài Viết

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| GET | `/api/v2/articles?category=le-hoi&page=1&limit=10` | Danh sách bài viết | ❌ |
| GET | `/api/v2/articles/featured` | Bài viết nổi bật | ❌ |
| GET | `/api/v2/articles/popular?limit=10` | Bài viết phổ biến | ❌ |
| GET | `/api/v2/articles/latest?limit=10` | Bài viết mới nhất | ❌ |
| GET | `/api/v2/articles/:slug` | Chi tiết bài viết (tăng view) | ❌ |
| GET | `/api/v2/articles/:slug/related?limit=5` | Bài viết liên quan | ❌ |
| GET | `/api/v2/articles/search?q=trung+thu` | Tìm kiếm full-text | ❌ |
| POST | `/api/v2/admin/articles` | Tạo bài viết | ✅ Admin/Editor |
| PUT | `/api/v2/admin/articles/:id` | Cập nhật bài viết | ✅ Admin/Editor |
| PATCH | `/api/v2/admin/articles/:id/status` | Đổi trạng thái | ✅ Admin |
| DELETE | `/api/v2/admin/articles/:id` | Xoá bài viết | ✅ Admin |

### 4.6 Categories & Tags API

| Method | Endpoint | Mô tả | Auth |
|--------|----------|--------|------|
| GET | `/api/v2/categories` | Danh sách danh mục | ❌ |
| GET | `/api/v2/categories/:slug/articles` | Bài viết theo danh mục | ❌ |
| GET | `/api/v2/tags` | Danh sách tags | ❌ |
| GET | `/api/v2/tags/:slug/articles` | Bài viết theo tag | ❌ |
| POST | `/api/v2/admin/categories` | Tạo danh mục | ✅ Admin |
| PUT | `/api/v2/admin/categories/:id` | Cập nhật danh mục | ✅ Admin |
| DELETE | `/api/v2/admin/categories/:id` | Xoá danh mục | ✅ Admin |
| POST | `/api/v2/admin/tags` | Tạo tag | ✅ Admin |

### 4.7 Aggregated "Today" API — Tổng hợp dữ liệu ngày

```
GET /api/v2/today

Response 200:
{
  "success": true,
  "data": {
    "date": "2026-03-06",
    "calendar": { ...existing calendar data... },
    "quote": {
      "quote": "...",
      "author": "...",
      "authorImageUrl": "..."
    },
    "birthdays": [
      { "name": "Michelangelo", "birthYear": 1475, "occupation": "Hoạ sĩ, Nhà điêu khắc", "imageUrl": "..." }
    ],
    "events": [
      { "title": "Ngày thành lập Đảng Nhân dân Cách mạng Lào", "eventType": "national_day", "flagEmoji": "🇱🇦" }
    ],
    "festivals": [
      { "name": "Lễ Rước Đèn Hoa", "festivalType": "folk_festival", "region": "Hội An" }
    ],
    "featuredArticles": [
      { "title": "...", "slug": "...", "excerpt": "...", "featuredImage": "..." }
    ]
  }
}
```

---

## 5. Thiết Kế Giao Diện Người Dùng

### 5.1 Bố Cục Trang Chủ Mới (v2.0)

```
┌─────────────────────────────────────────────────────────┐
│  HEADER (giữ nguyên v1)                                │
├─────────────────────────────────────────────────────────┤
│  SEARCH BAR (giữ nguyên v1)                            │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  💬 CHÂM NGÔN NGÀY — Quote of the Day (MỚI)            │
│  ┌─────────────────────────────────────────────────────┐│
│  │  "Hãy là sự thay đổi mà bạn muốn nhìn thấy       ││
│  │   trong thế giới."                                  ││
│  │                        — Mahatma Gandhi (1869–1948) ││
│  │                              [🔄 Xem câu khác]      ││
│  └─────────────────────────────────────────────────────┘│
│                                                         │
├─────────────────────────────────────────────────────────┤
│  TABS (mở rộng)                                        │
│  [Lịch | Tháng Âm | Ngày Tốt | 24 Tiết | Đổi Lịch    │
│   | Sự Kiện | Lễ Hội | Bài Viết]      ← 3 tabs mới    │
├──────────────────────────────┬──────────────────────────┤
│  HERO — MAIN DATE CARD      │  INFO PANEL              │
│  (giữ nguyên v1)            │  (giữ nguyên v1)         │
│                              │                          │
│                              │  + ┌────────────────┐   │
│                              │    │ 🎂 Sinh Nhật    │   │
│                              │    │ Hôm Nay         │   │
│                              │    │ • Michelangelo  │   │
│                              │    │ • Gabriel García│   │
│                              │    │   Márquez       │   │
│                              │    └────────────────┘   │
├──────────────────────────────┴──────────────────────────┤
│  CALENDAR (giữ nguyên v1)                              │
│  + Dots mới: 🔵 sự kiện, 🟡 lễ hội, 🟢 ngày tốt       │
├─────────────────────────────────────────────────────────┤
│  📅 SỰ KIỆN NGÀY NÀY TRONG LỊCH SỬ (MỚI)             │
│  ┌──────────────────────────────────────────────┐      │
│  │ 🏛️ 1836 — Trận Alamo kết thúc (Mỹ)          │      │
│  │ 🇬🇭 1957 — Ghana giành độc lập               │      │
│  │ 🔬 1869 — Mendeleev công bố bảng tuần hoàn   │      │
│  │                          [Xem tất cả →]       │      │
│  └──────────────────────────────────────────────┘      │
├─────────────────────────────────────────────────────────┤
│  🎎 LỄ HỘI SẮP TỚI (MỚI)                              │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐          │
│  │  🏮 Tết     │ │  🎋 Thanh   │ │ 🐲 Lễ hội  │          │
│  │  Hàn Thực  │ │  Minh      │ │  Đền Hùng  │          │
│  │  3/3 Âm    │ │  5/4 Dương │ │  10/3 Âm   │          │
│  │  còn 15ng  │ │  còn 30ng  │ │  còn 42ng  │          │
│  └────────────┘ └────────────┘ └────────────┘          │
├─────────────────────────────────────────────────────────┤
│  BOTTOM 3-COL (giữ nguyên v1)                          │
├─────────────────────────────────────────────────────────┤
│  📝 BÀI VIẾT NỔI BẬT (MỚI)                             │
│  ┌───────────────────┐ ┌───────────────────┐            │
│  │  [Ảnh bìa]        │ │  [Ảnh bìa]        │            │
│  │  Tử vi tuổi Thìn  │ │  24 Tiết Khí —    │            │
│  │  năm 2026         │ │  Ý nghĩa & nguồn  │            │
│  │  ⏱ 8 phút đọc     │ │  gốc               │            │
│  │  👁 12,450 views   │ │  ⏱ 5 phút đọc     │            │
│  └───────────────────┘ └───────────────────┘            │
│  ┌───────────────────┐ ┌───────────────────┐            │
│  │  Phong thuỷ bàn   │ │  Lễ hội Đền Hùng  │            │
│  │  làm việc 2026    │ │  — Giỗ Tổ ...     │            │
│  └───────────────────┘ └───────────────────┘            │
│                          [Xem tất cả bài viết →]        │
├─────────────────────────────────────────────────────────┤
│  FOOTER (giữ nguyên v1) + Links mới                    │
│  Châm Ngôn | Nhân Vật | Sự Kiện | Lễ Hội | Bài Viết   │
└─────────────────────────────────────────────────────────┘
```

### 5.2 Trang Bài Viết Chi Tiết (`/bai-viet/:slug`)

```
┌─────────────────────────────────────────────────────────┐
│  HEADER                                                 │
├─────────────────────────────────────────────────────────┤
│  BREADCRUMB                                             │
│  Trang chủ > Lễ Hội > Tết Trung Thu — Nguồn Gốc...    │
├──────────────────────────────┬──────────────────────────┤
│  ARTICLE CONTENT (70%)       │  SIDEBAR (30%)           │
│                              │                          │
│  [Featured Image]            │  📋 MỤC LỤC              │
│                              │  1. Nguồn gốc            │
│  Tết Trung Thu —             │  2. Ý nghĩa              │
│  Nguồn Gốc và Ý Nghĩa      │  3. Phong tục            │
│                              │  4. Các hoạt động        │
│  📂 Lễ hội · ⏱ 8 phút       │                          │
│  ✍ Zplus Team · 15/09/2026  │  📊 BÀI VIẾT PHỔ BIẾN    │
│                              │  • Tử vi tuổi Thìn      │
│  <article content>           │  • Phong thuỷ 2026       │
│  ...                         │  • Lễ hội Đền Hùng      │
│                              │                          │
│  TAGS                        │  🏷️ TAGS                  │
│  #TrungThu #LễHội           │  Trung Thu (45)          │
│                              │  Lễ Hội (120)           │
│  SHARE BUTTONS               │  Tử Vi (89)             │
│  [Facebook] [Twitter] [Copy] │                          │
├──────────────────────────────┴──────────────────────────┤
│  BÀI VIẾT LIÊN QUAN                                    │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐           │
│  │ Bài 1  │ │ Bài 2  │ │ Bài 3  │ │ Bài 4  │           │
│  └────────┘ └────────┘ └────────┘ └────────┘           │
├─────────────────────────────────────────────────────────┤
│  FOOTER                                                 │
└─────────────────────────────────────────────────────────┘
```

### 5.3 Trang Danh Sách Bài Viết (`/bai-viet`)

```
┌─────────────────────────────────────────────────────────┐
│  HEADER                                                 │
├─────────────────────────────────────────────────────────┤
│  PAGE TITLE: 📝 Bài Viết — Lịch Số                      │
├─────────────────────────────────────────────────────────┤
│  FILTER BAR                                             │
│  [Tất cả] [Nhân vật] [Sự kiện] [Lễ hội] [Tử vi]       │
│  [Phong thuỷ] [Ngày tốt] [Văn hoá] [Kiến thức]        │
│  🔍 Tìm kiếm bài viết...                               │
├─────────────────────────────────────────────────────────┤
│  FEATURED ARTICLE (full-width)                          │
│  ┌─────────────────────────────────────────────────┐    │
│  │ [Large Image]  Tử Vi 12 Con Giáp Năm 2026       │    │
│  │                ⏱ 12 phút · 👁 24,500 views       │    │
│  └─────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────┤
│  ARTICLE GRID (3 columns)                               │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                │
│  │ [Image]  │ │ [Image]  │ │ [Image]  │                │
│  │ Title    │ │ Title    │ │ Title    │                │
│  │ Excerpt  │ │ Excerpt  │ │ Excerpt  │                │
│  │ ⏱ · 👁   │ │ ⏱ · 👁   │ │ ⏱ · 👁   │                │
│  └──────────┘ └──────────┘ └──────────┘                │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                │
│  │ ...      │ │ ...      │ │ ...      │                │
│  └──────────┘ └──────────┘ └──────────┘                │
├─────────────────────────────────────────────────────────┤
│  PAGINATION                                             │
│  [← Trước] [1] [2] [3] ... [10] [Sau →]               │
└─────────────────────────────────────────────────────────┘
```

### 5.4 Trang Sự Kiện Ngày Này (`/su-kien`)

```
┌─────────────────────────────────────────────────────────┐
│  HEADER                                                 │
├─────────────────────────────────────────────────────────┤
│  📅 NGÀY NÀY TRONG LỊCH SỬ — 6 tháng 3                 │
│  [← Ngày trước]  [Chọn ngày ▼]  [Ngày sau →]          │
├─────────────────────────────────────────────────────────┤
│  🏛️ SỰ KIỆN LỊCH SỬ                                    │
│  ┌─ 1836 ─────────────────────────────────────────┐    │
│  │ Trận Alamo kết thúc — Texas, Hoa Kỳ 🇺🇸         │    │
│  └────────────────────────────────────────────────┘    │
│  ┌─ 1957 ─────────────────────────────────────────┐    │
│  │ Ghana trở thành quốc gia châu Phi đầu tiên 🇬🇭   │    │
│  │ giành độc lập từ thực dân                        │    │
│  └────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────┤
│  🎂 SINH NHẬT HÔM NAY                                   │
│  ┌────────┐ ┌────────┐ ┌────────┐                      │
│  │ [Ảnh]  │ │ [Ảnh]  │ │ [Ảnh]  │                      │
│  │ Michel-│ │ Gabriel │ │ Shaquil│                      │
│  │ angelo │ │ García  │ │ le     │                      │
│  │ 1475   │ │ Márquez │ │ O'Neal │                      │
│  │ Hoạ sĩ │ │ 1927   │ │ 1972   │                      │
│  └────────┘ └────────┘ └────────┘                      │
├─────────────────────────────────────────────────────────┤
│  🇺🇳 NGÀY QUỐC TẾ & QUỐC KHÁNH                         │
│  • 🇬🇭 Ngày Độc Lập Ghana (1957)                        │
│  • Ngày Nha Sĩ Thế Giới (World Dentist's Day)         │
├─────────────────────────────────────────────────────────┤
│  FOOTER                                                 │
└─────────────────────────────────────────────────────────┘
```

### 5.5 Các Routes Mới (Next.js)

| Route | Trang | Mô tả |
|-------|-------|--------|
| `/` | Trang chủ | Trang chủ Lịch Số (mở rộng v2) |
| `/bai-viet` | Danh sách bài viết | Grid bài viết, filter theo category |
| `/bai-viet/:slug` | Chi tiết bài viết | Article page, SEO optimized |
| `/su-kien` | Sự kiện ngày này | Timeline sự kiện hôm nay |
| `/su-kien/:slug` | Chi tiết sự kiện | Event detail page |
| `/le-hoi` | Danh sách lễ hội | Lễ hội sắp tới, calendar view |
| `/le-hoi/:slug` | Chi tiết lễ hội | Festival detail page |
| `/nhan-vat` | Nhân vật nổi tiếng | Sinh nhật hôm nay, directory |
| `/nhan-vat/:id` | Chi tiết nhân vật | Person profile page |
| `/cham-ngon` | Châm ngôn | Browse, search châm ngôn |
| `/admin/articles` | 🔒 Quản lý bài viết | CRUD bài viết |
| `/admin/events` | 🔒 Quản lý sự kiện | CRUD sự kiện |
| `/admin/festivals` | 🔒 Quản lý lễ hội | CRUD lễ hội |
| `/admin/people` | 🔒 Quản lý nhân vật | CRUD nhân vật |
| `/admin/quotes` | 🔒 Quản lý châm ngôn | CRUD châm ngôn |
| `/admin/categories` | 🔒 Quản lý danh mục | CRUD categories & tags |

---

## 6. Trang Quản Trị (Admin Panel)

### 6.1 Sidebar Navigation Mới

```
┌──────────────────────────┐
│  🏠 LỊCH SỐ ADMIN        │
│  ─────────────────────── │
│  📊 Dashboard             │
│  ─── QUẢN LÝ CHUNG ───  │
│  👤 Người dùng            │
│  🔑 Vai trò & Quyền      │
│  📁 Tệp tin (Media)      │
│  ⚙️ Cài đặt               │
│  ─── NỘI DUNG v2.0 ───  │  ← MỚI
│  📝 Bài viết (Articles)   │  ← MỚI
│  📅 Sự kiện (Events)      │  ← MỚI
│  🎎 Lễ hội (Festivals)    │  ← MỚI
│  👤 Nhân vật (People)     │  ← MỚI
│  💬 Châm ngôn (Quotes)    │  ← MỚI
│  📂 Danh mục (Categories) │  ← MỚI
│  🏷️ Tags                  │  ← MỚI
│  ─── THỐNG KÊ ─────────  │
│  📈 Thống kê nội dung     │  ← MỚI
│  🔍 Tìm kiếm phổ biến    │  ← MỚI
└──────────────────────────┘
```

### 6.2 Trang Quản Lý Bài Viết (`/admin/articles`)

#### Danh sách bài viết

```
┌─────────────────────────────────────────────────────────────────┐
│  📝 Quản Lý Bài Viết                    [+ Thêm Bài Viết Mới] │
├─────────────────────────────────────────────────────────────────┤
│  Lọc: [Tất cả ▼] [Danh mục ▼] [Trạng thái ▼]  🔍 Tìm kiếm..│
├─────┬───────────────────┬──────────┬────────┬───────┬──────────┤
│  #  │ Tiêu đề           │ Danh mục │ T.thái │ Views │ Hành động│
├─────┼───────────────────┼──────────┼────────┼───────┼──────────┤
│  1  │ Tử vi tuổi Thìn   │ Tử vi    │ ✅ Pub  │ 24.5K │ ✏️ 🗑️    │
│  2  │ Lễ hội Đền Hùng   │ Lễ hội   │ ✅ Pub  │ 12.3K │ ✏️ 🗑️    │
│  3  │ Phong thuỷ 2026   │ P.thuỷ   │ 📝 Draft│ —     │ ✏️ 🗑️    │
│  4  │ 24 Tiết Khí       │ Văn hoá  │ 👀 Rev  │ —     │ ✏️ 🗑️    │
├─────┴───────────────────┴──────────┴────────┴───────┴──────────┤
│  Hiển thị 1-10 / 45 bài viết    [← 1 2 3 4 5 →]              │
└─────────────────────────────────────────────────────────────────┘
```

#### Form thêm/sửa bài viết

```
┌─────────────────────────────────────────────────────────────────┐
│  📝 Thêm Bài Viết Mới                          [Lưu nháp] [Đăng]│
├──────────────────────────────────┬──────────────────────────────┤
│  EDITOR (70%)                    │  SIDEBAR (30%)               │
│                                  │                              │
│  Tiêu đề *                       │  📊 TRẠNG THÁI               │
│  [                            ]  │  [Draft ▼]                   │
│                                  │  Ngày đăng: [Auto]           │
│  Slug (tự động)                  │  Lên lịch: [Chọn ngày]      │
│  [tu-vi-tuoi-thin-2026       ]  │                              │
│                                  │  📂 DANH MỤC                 │
│  Tóm tắt                        │  [Tử vi ▼]                   │
│  [                            ]  │                              │
│                                  │  🏷️ TAGS                     │
│  Nội dung *                      │  [tử vi, tuổi Thìn, 2026]   │
│  ┌───────────────────────────┐   │  [+ Thêm tag]               │
│  │ B I U  H1 H2  📷  🔗  📋  │   │                              │
│  │───────────────────────────│   │  🖼️ ẢNH BÌA                  │
│  │                           │   │  ┌──────────────┐            │
│  │  <WYSIWYG Editor>        │   │  │  [Upload]     │            │
│  │                           │   │  │  hoặc kéo thả │            │
│  │                           │   │  └──────────────┘            │
│  │                           │   │                              │
│  └───────────────────────────┘   │  🔍 SEO                      │
│                                  │  Meta title                   │
│                                  │  [                         ]  │
│                                  │  Meta description             │
│                                  │  [                         ]  │
│                                  │  OG Image                     │
│                                  │  [Upload]                     │
│                                  │                              │
│                                  │  ⚙️ TUỲ CHỌN                  │
│                                  │  ☐ Bài viết nổi bật          │
│                                  │  ☐ Cho phép bình luận        │
└──────────────────────────────────┴──────────────────────────────┘
```

### 6.3 Trang Quản Lý Sự Kiện (`/admin/events`)

#### Form thêm sự kiện

```
┌─────────────────────────────────────────────────────────────────┐
│  📅 Thêm Sự Kiện Mới                              [Lưu] [Huỷ] │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Tên sự kiện *                                                  │
│  [Chiến thắng Điện Biên Phủ                                ]   │
│                                                                 │
│  Slug (tự động)                                                 │
│  [chien-thang-dien-bien-phu                                 ]   │
│                                                                 │
│  ┌──────────────────────────────────────────────────────┐       │
│  │ Ngày: [07] Tháng: [05] Năm: [1954] (trống = hàng năm) │     │
│  └──────────────────────────────────────────────────────┘       │
│                                                                 │
│  Loại sự kiện *           │  Mức độ quan trọng                  │
│  [Sự kiện lịch sử ▼]     │  [Cao ▼]                            │
│                                                                 │
│  Quốc gia                 │  Mã quốc gia                        │
│  [Việt Nam            ]   │  [VN]                                │
│                                                                 │
│  ☑ Sự kiện lặp lại hàng năm                                    │
│                                                                 │
│  Mô tả ngắn                                                     │
│  [Chiến thắng lịch sử kết thúc 9 năm kháng chiến...        ]  │
│                                                                 │
│  Ảnh minh hoạ        │  Liên kết bài viết                       │
│  [Upload]            │  [Chọn bài viết ▼]                       │
│                                                                 │
│  Tags                                                           │
│  [lịch sử, chiến tranh, Việt Nam, Pháp]                       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 6.4 Trang Quản Lý Lễ Hội (`/admin/festivals`)

#### Form thêm lễ hội

```
┌─────────────────────────────────────────────────────────────────┐
│  🎎 Thêm Lễ Hội Mới                               [Lưu] [Huỷ] │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Tên lễ hội *              │  Tên khác (nếu có)                 │
│  [Tết Trung Thu        ]   │  [Tết Thiếu Nhi, Tết Trông Trăng] │
│                                                                 │
│  Loại lịch *: (●) Âm lịch  (○) Dương lịch  (○) Cả hai         │
│                                                                 │
│  ┌─ ÂM LỊCH ──────────────┐  ┌─ DƯƠNG LỊCH ─────────────────┐ │
│  │ Ngày: [15] Tháng: [08] │  │ Ngày: [__] Tháng: [__]       │ │
│  └─────────────────────────┘  └───────────────────────────────┘ │
│                                                                 │
│  Loại lễ hội *            │  Số ngày diễn ra                    │
│  [Lễ Tết ▼]              │  [1]                                │
│                                                                 │
│  Vùng miền               │  Quốc gia                            │
│  [Toàn quốc          ]   │  [Việt Nam]                          │
│                                                                 │
│  Mô tả ngắn                                                     │
│  [Lễ hội truyền thống dành cho thiếu nhi...                 ]   │
│                                                                 │
│  Phong tục truyền thống (mỗi dòng 1 phong tục)                 │
│  ┌──────────────────────────────────────────────┐               │
│  │ Rước đèn                                     │               │
│  │ Phá cỗ                                       │               │
│  │ Múa lân                                      │               │
│  │ Làm bánh Trung Thu                           │               │
│  └──────────────────────────────────────────────┘               │
│                                                                 │
│  Ảnh chính           │  Gallery (nhiều ảnh)                     │
│  [Upload]            │  [Upload nhiều] [Kéo thả]                │
│                                                                 │
│  Liên kết bài viết   │  Mức độ quan trọng                      │
│  [Chọn bài viết ▼]  │  [Cao ▼]                                │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 6.5 Trang Quản Lý Nhân Vật (`/admin/people`)

#### Form thêm nhân vật

```
┌─────────────────────────────────────────────────────────────────┐
│  👤 Thêm Nhân Vật Nổi Tiếng                       [Lưu] [Huỷ] │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Tên nhân vật *            │  Tên gốc (nếu khác)               │
│  [Hồ Chí Minh          ]  │  [Nguyễn Sinh Cung            ]   │
│                                                                 │
│  Ngày sinh                 │  Ngày mất (nếu có)                │
│  [19/05/1890]              │  [02/09/1969]                      │
│                                                                 │
│  Quốc tịch               │  Nghề nghiệp                        │
│  [Việt Nam            ]   │  [Nhà cách mạng, Chính trị gia ]   │
│                                                                 │
│  Danh mục *               │  ☑ Nhân vật Việt Nam                │
│  [Chính trị ▼]           │                                      │
│                                                                 │
│  Tiểu sử ngắn                                                   │
│  [Chủ tịch nước Việt Nam Dân chủ Cộng hòa, lãnh tụ...     ]   │
│                                                                 │
│  Ảnh đại diện            │  Liên kết bài viết                   │
│  [Upload]                │  [Chọn bài viết ▼]                   │
│                                                                 │
│  Tags                                                           │
│  [lãnh tụ, cách mạng, Việt Nam]                               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 6.6 Trang Quản Lý Châm Ngôn (`/admin/quotes`)

#### Form thêm châm ngôn

```
┌─────────────────────────────────────────────────────────────────┐
│  💬 Thêm Châm Ngôn                                [Lưu] [Huỷ] │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Câu châm ngôn (tiếng Việt) *                                   │
│  [Hãy là sự thay đổi mà bạn muốn nhìn thấy trong thế giới.]  │
│                                                                 │
│  Câu gốc (ngôn ngữ gốc)                                        │
│  [Be the change you wish to see in the world.              ]   │
│                                                                 │
│  Ngôn ngữ gốc: [Tiếng Anh ▼]                                  │
│                                                                 │
│  Tác giả *                │  Giới thiệu tác giả                │
│  [Mahatma Gandhi       ]  │  [Nhà lãnh đạo Ấn Độ          ]   │
│                                                                 │
│  Năm sinh    │  Năm mất    │  Quốc tịch                        │
│  [1869]      │  [1948]      │  [Ấn Độ]                          │
│                                                                 │
│  Ảnh tác giả             │  Ngày hiển thị (day of year)         │
│  [Upload]                │  [66] (trống = random pool)          │
│                                                                 │
│  Tags                                                           │
│  [cuộc sống, thay đổi, triết học]                              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 6.7 Import Hàng Loạt (Batch Import)

Tất cả các module đều hỗ trợ import dữ liệu hàng loạt qua CSV/JSON:

```
┌─────────────────────────────────────────────────────────────────┐
│  📥 Import Dữ Liệu Hàng Loạt                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Loại dữ liệu: [Châm ngôn ▼]                                  │
│                                                                 │
│  Định dạng: (●) CSV  (○) JSON                                  │
│                                                                 │
│  ┌──────────────────────────────────────────┐                   │
│  │                                          │                   │
│  │    📁 Kéo thả file CSV/JSON vào đây      │                   │
│  │       hoặc [Chọn file]                   │                   │
│  │                                          │                   │
│  └──────────────────────────────────────────┘                   │
│                                                                 │
│  📋 Xem trước (Preview): 5 / 150 dòng                          │
│  ┌──────┬──────────────────────────┬────────────┬─────────┐     │
│  │  #   │ Quote                    │ Author     │ Tags    │     │
│  ├──────┼──────────────────────────┼────────────┼─────────┤     │
│  │  1   │ Hãy là sự thay đổi...   │ Gandhi     │ 3 tags  │     │
│  │  2   │ Tôi nghĩ, vậy tôi...    │ Descartes  │ 2 tags  │     │
│  │  ... │ ...                      │ ...        │ ...     │     │
│  └──────┴──────────────────────────┴────────────┴─────────┘     │
│                                                                 │
│  ✅ 148 dòng hợp lệ  ⚠️ 2 dòng lỗi                             │
│                                                                 │
│  [📥 Import 148 dòng]  [❌ Huỷ]                                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 6.8 Dashboard Thống Kê Nội Dung (`/admin` — v2.0)

```
┌─────────────────────────────────────────────────────────────────┐
│  📊 Dashboard — Lịch Số v2.0                                    │
├──────────┬──────────┬──────────┬──────────┬──────────┬──────────┤
│ 📝 120   │ 📅 450   │ 🎎 85    │ 👤 320   │ 💬 365   │ 👁 45.2K │
│ Bài viết │ Sự kiện  │ Lễ hội   │ Nhân vật │ Châm ngôn│ Lượt xem │
│ +5 tuần  │ +12 tuần │ +3 tuần  │ +8 tuần  │ +15 tuần │ hôm nay  │
├──────────┴──────────┴──────────┴──────────┴──────────┴──────────┤
│                                                                 │
│  📈 Lượt Xem 7 Ngày Qua          │  🔥 Bài Viết Phổ Biến       │
│  ┌──────────────────────────┐     │  1. Tử vi tuổi Thìn (24.5K)│
│  │     ╱╲    ╱╲             │     │  2. Phong thuỷ 2026 (18.2K)│
│  │    ╱  ╲  ╱  ╲     ╱╲    │     │  3. Lễ hội Đền Hùng (12.3K)│
│  │ ──╱    ╲╱    ╲───╱  ╲── │     │  4. 24 Tiết Khí (9.8K)     │
│  │ T2  T3  T4  T5  T6  T7  │     │  5. Ngày cưới tốt (8.1K)   │
│  └──────────────────────────┘     │                              │
│                                                                 │
│  📝 Bài Viết Gần Đây              │  🔍 Tìm Kiếm Phổ Biến      │
│  • Tử vi tháng 4/2026 (Draft)    │  1. "tử vi 2026" (450)     │
│  • Lễ Thanh Minh (Published)     │  2. "ngày tốt cưới" (320)  │
│  • Phong thuỷ phòng ngủ (Review) │  3. "lễ hội đền hùng" (280)│
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7. Cấu Trúc Mã Nguồn Mới

### 7.1 Backend — Các File Mới

```
backend/internal/
├── dto/
│   ├── ...existing...
│   ├── quote_dto.go              🆕 Request/Response cho Quotes
│   ├── famous_people_dto.go      🆕 Request/Response cho Famous People
│   ├── event_dto.go              🆕 Request/Response cho Events
│   ├── festival_dto.go           🆕 Request/Response cho Festivals
│   └── article_dto.go            🆕 Request/Response cho Articles
├── handlers/
│   ├── ...existing...
│   ├── quote_handler.go          🆕 HTTP handlers cho Quotes
│   ├── famous_people_handler.go  🆕 HTTP handlers cho Famous People
│   ├── event_handler.go          🆕 HTTP handlers cho Events
│   ├── festival_handler.go       🆕 HTTP handlers cho Festivals
│   └── article_handler.go        🆕 HTTP handlers cho Articles
├── models/
│   ├── ...existing...
│   ├── quote.go                  🆕 GORM model Quote
│   ├── famous_person.go          🆕 GORM model FamousPerson
│   ├── event.go                  🆕 GORM model Event
│   ├── folk_festival.go          🆕 GORM model FolkFestival
│   ├── article.go                🆕 GORM model Article
│   ├── article_category.go       🆕 GORM model ArticleCategory
│   └── article_tag.go            🆕 GORM model ArticleTag
├── repositories/
│   ├── ...existing...
│   ├── quote_repo.go             🆕 Data access cho Quotes
│   ├── famous_people_repo.go     🆕 Data access cho Famous People
│   ├── event_repo.go             🆕 Data access cho Events
│   ├── festival_repo.go          🆕 Data access cho Festivals
│   └── article_repo.go           🆕 Data access cho Articles
├── services/
│   ├── ...existing...
│   ├── quote_service.go          🆕 Business logic cho Quotes
│   ├── famous_people_service.go  🆕 Business logic cho Famous People
│   ├── event_service.go          🆕 Business logic cho Events
│   ├── festival_service.go       🆕 Business logic cho Festivals
│   ├── article_service.go        🆕 Business logic cho Articles
│   └── content_analytics_service.go 🆕 Thống kê nội dung
├── routes/
│   ├── ...existing...
│   ├── content_routes.go         🆕 Routes cho tất cả content APIs
│   └── admin_content_routes.go   🆕 Routes cho admin content APIs
└── validators/
    ├── ...existing...
    └── content_validators.go     🆕 Validation cho content inputs

backend/migrations/
├── ...existing 000001-000010...
├── 000011_create_quotes_table.up.sql               🆕
├── 000011_create_quotes_table.down.sql             🆕
├── 000012_create_famous_people_table.up.sql        🆕
├── 000012_create_famous_people_table.down.sql      🆕
├── 000013_create_events_table.up.sql               🆕
├── 000013_create_events_table.down.sql             🆕
├── 000014_create_folk_festivals_table.up.sql       🆕
├── 000014_create_folk_festivals_table.down.sql     🆕
├── 000015_create_article_categories_table.up.sql   🆕
├── 000015_create_article_categories_table.down.sql 🆕
├── 000016_create_article_tags_table.up.sql         🆕
├── 000016_create_article_tags_table.down.sql       🆕
├── 000017_create_articles_table.up.sql             🆕
├── 000017_create_articles_table.down.sql           🆕
├── 000018_create_article_tag_relations_table.up.sql     🆕
└── 000018_create_article_tag_relations_table.down.sql   🆕
```

### 7.2 Frontend — Các File Mới

```
frontend/src/
├── app/
│   ├── ...existing...
│   ├── (home)/
│   │   ├── ...existing...
│   │   ├── bai-viet/                        🆕 Trang danh sách bài viết
│   │   │   ├── page.tsx
│   │   │   └── [slug]/
│   │   │       └── page.tsx                 🆕 Trang chi tiết bài viết
│   │   ├── su-kien/                         🆕 Trang sự kiện
│   │   │   ├── page.tsx
│   │   │   └── [slug]/
│   │   │       └── page.tsx
│   │   ├── le-hoi/                          🆕 Trang lễ hội
│   │   │   ├── page.tsx
│   │   │   └── [slug]/
│   │   │       └── page.tsx
│   │   ├── nhan-vat/                        🆕 Trang nhân vật
│   │   │   ├── page.tsx
│   │   │   └── [id]/
│   │   │       └── page.tsx
│   │   └── cham-ngon/                       🆕 Trang châm ngôn
│   │       └── page.tsx
│   ├── admin/
│   │   ├── ...existing...
│   │   ├── articles/                        🆕 Quản lý bài viết
│   │   │   ├── page.tsx                     (Danh sách)
│   │   │   ├── create/
│   │   │   │   └── page.tsx                 (Form tạo mới)
│   │   │   └── [id]/
│   │   │       └── edit/
│   │   │           └── page.tsx             (Form chỉnh sửa)
│   │   ├── events/                          🆕 Quản lý sự kiện
│   │   │   ├── page.tsx
│   │   │   ├── create/
│   │   │   │   └── page.tsx
│   │   │   └── [id]/
│   │   │       └── edit/
│   │   │           └── page.tsx
│   │   ├── festivals/                       🆕 Quản lý lễ hội
│   │   │   ├── page.tsx
│   │   │   ├── create/
│   │   │   │   └── page.tsx
│   │   │   └── [id]/
│   │   │       └── edit/
│   │   │           └── page.tsx
│   │   ├── people/                          🆕 Quản lý nhân vật
│   │   │   ├── page.tsx
│   │   │   ├── create/
│   │   │   │   └── page.tsx
│   │   │   └── [id]/
│   │   │       └── edit/
│   │   │           └── page.tsx
│   │   ├── quotes/                          🆕 Quản lý châm ngôn
│   │   │   ├── page.tsx
│   │   │   ├── create/
│   │   │   │   └── page.tsx
│   │   │   └── [id]/
│   │   │       └── edit/
│   │   │           └── page.tsx
│   │   ├── categories/                      🆕 Quản lý danh mục
│   │   │   └── page.tsx
│   │   └── content-analytics/               🆕 Thống kê nội dung
│   │       └── page.tsx
├── components/
│   ├── ...existing...
│   ├── articles/                            🆕
│   │   ├── ArticleCard.tsx                  (Card bài viết)
│   │   ├── ArticleList.tsx                  (Grid bài viết)
│   │   ├── ArticleDetail.tsx                (Chi tiết bài viết)
│   │   ├── ArticleEditor.tsx                (WYSIWYG Editor)
│   │   ├── ArticleForm.tsx                  (Form tạo/sửa)
│   │   ├── ArticleSidebar.tsx               (Sidebar bài viết)
│   │   ├── TableOfContents.tsx              (Mục lục tự động)
│   │   └── RelatedArticles.tsx              (Bài liên quan)
│   ├── events/                              🆕
│   │   ├── EventCard.tsx
│   │   ├── EventTimeline.tsx                (Timeline sự kiện)
│   │   ├── EventForm.tsx
│   │   └── EventCalendar.tsx                (Calendar với events)
│   ├── festivals/                           🆕
│   │   ├── FestivalCard.tsx
│   │   ├── FestivalCountdown.tsx            (Đếm ngược)
│   │   ├── FestivalForm.tsx
│   │   └── FestivalGallery.tsx              (Gallery ảnh)
│   ├── people/                              🆕
│   │   ├── PersonCard.tsx
│   │   ├── BirthdayToday.tsx                (Widget sinh nhật)
│   │   ├── PersonForm.tsx
│   │   └── PersonProfile.tsx                (Profile page)
│   ├── quotes/                              🆕
│   │   ├── QuoteOfTheDay.tsx                (Widget châm ngôn ngày)
│   │   ├── QuoteCard.tsx
│   │   ├── QuoteForm.tsx
│   │   └── QuoteShare.tsx                   (Share buttons)
│   ├── content/                             🆕
│   │   ├── ImportDialog.tsx                 (Import CSV/JSON)
│   │   ├── RichTextEditor.tsx               (TipTap WYSIWYG)
│   │   ├── SEOFields.tsx                    (SEO input fields)
│   │   ├── SlugInput.tsx                    (Auto-slug input)
│   │   ├── TagInput.tsx                     (Tag input with autocomplete)
│   │   ├── ImageUpload.tsx                  (Image upload with preview)
│   │   └── CategorySelect.tsx              (Category dropdown)
│   └── home/                                🆕
│       ├── TodayEvents.tsx                  (Section sự kiện ngày)
│       ├── UpcomingFestivals.tsx             (Section lễ hội sắp tới)
│       └── FeaturedArticles.tsx             (Section bài viết nổi bật)
├── hooks/
│   ├── ...existing...
│   ├── useArticles.ts                       🆕
│   ├── useEvents.ts                         🆕
│   ├── useFestivals.ts                      🆕
│   ├── usePeople.ts                         🆕
│   ├── useQuotes.ts                         🆕
│   └── useContentAnalytics.ts               🆕
├── services/
│   ├── ...existing...
│   ├── articleService.ts                    🆕
│   ├── eventService.ts                      🆕
│   ├── festivalService.ts                   🆕
│   ├── peopleService.ts                     🆕
│   └── quoteService.ts                      🆕
├── types/
│   ├── ...existing...
│   ├── article.ts                           🆕
│   ├── event.ts                             🆕
│   ├── festival.ts                          🆕
│   ├── person.ts                            🆕
│   └── quote.ts                             🆕
└── lib/
    ├── ...existing...
    └── slug.ts                              🆕 Utility tạo slug tiếng Việt
```

### 7.3 GORM Models Mẫu (Go)

```go
// models/quote.go
type Quote struct {
    ID                uuid.UUID       `gorm:"type:uuid;primaryKey;default:gen_random_uuid()" json:"id"`
    Quote             string          `gorm:"type:text;not null" json:"quote"`
    OriginalQuote     *string         `gorm:"type:text" json:"originalQuote,omitempty"`
    OriginalLanguage  string          `gorm:"type:varchar(10);default:'vi'" json:"originalLanguage"`
    Author            string          `gorm:"type:varchar(255);not null" json:"author"`
    AuthorBio         *string         `gorm:"type:text" json:"authorBio,omitempty"`
    AuthorBirthYear   *int            `json:"authorBirthYear,omitempty"`
    AuthorDeathYear   *int            `json:"authorDeathYear,omitempty"`
    AuthorNationality *string         `gorm:"type:varchar(100)" json:"authorNationality,omitempty"`
    AuthorImageURL    *string         `gorm:"type:varchar(500)" json:"authorImageUrl,omitempty"`
    Tags              pq.StringArray  `gorm:"type:text[]" json:"tags"`
    DayOfYear         *int            `json:"dayOfYear,omitempty"`
    IsActive          bool            `gorm:"default:true" json:"isActive"`
    CreatedAt         time.Time       `json:"createdAt"`
    UpdatedAt         time.Time       `json:"updatedAt"`
}

// models/article.go
type ArticleStatus string

const (
    ArticleStatusDraft     ArticleStatus = "draft"
    ArticleStatusReview    ArticleStatus = "review"
    ArticleStatusPublished ArticleStatus = "published"
    ArticleStatusArchived  ArticleStatus = "archived"
)

type Article struct {
    ID              uuid.UUID       `gorm:"type:uuid;primaryKey;default:gen_random_uuid()" json:"id"`
    Title           string          `gorm:"type:varchar(500);not null" json:"title"`
    Slug            string          `gorm:"type:varchar(500);not null;uniqueIndex" json:"slug"`
    Excerpt         *string         `gorm:"type:text" json:"excerpt,omitempty"`
    Content         string          `gorm:"type:text;not null" json:"content"`
    FeaturedImage   *string         `gorm:"type:varchar(500)" json:"featuredImage,omitempty"`
    CategoryID      *uuid.UUID      `gorm:"type:uuid" json:"categoryId,omitempty"`
    Category        *ArticleCategory `gorm:"foreignKey:CategoryID" json:"category,omitempty"`
    AuthorID        *uuid.UUID      `gorm:"type:uuid" json:"authorId,omitempty"`
    Author          *User           `gorm:"foreignKey:AuthorID" json:"author,omitempty"`
    Tags            []ArticleTag    `gorm:"many2many:article_tag_relations" json:"tags,omitempty"`
    Status          ArticleStatus   `gorm:"type:article_status;default:'draft'" json:"status"`
    PublishedAt     *time.Time      `json:"publishedAt,omitempty"`
    ScheduledAt     *time.Time      `json:"scheduledAt,omitempty"`
    MetaTitle       *string         `gorm:"type:varchar(255)" json:"metaTitle,omitempty"`
    MetaDescription *string         `gorm:"type:varchar(500)" json:"metaDescription,omitempty"`
    OGImage         *string         `gorm:"type:varchar(500)" json:"ogImage,omitempty"`
    ViewCount       int             `gorm:"default:0" json:"viewCount"`
    ReadingTime     int             `gorm:"default:0" json:"readingTime"`
    IsFeatured      bool            `gorm:"default:false" json:"isFeatured"`
    IsActive        bool            `gorm:"default:true" json:"isActive"`
    CreatedAt       time.Time       `json:"createdAt"`
    UpdatedAt       time.Time       `json:"updatedAt"`
}
```

### 7.4 TypeScript Types Mẫu (Frontend)

```typescript
// types/quote.ts
export interface Quote {
  id: string;
  quote: string;
  originalQuote?: string;
  originalLanguage: string;
  author: string;
  authorBio?: string;
  authorBirthYear?: number;
  authorDeathYear?: number;
  authorNationality?: string;
  authorImageUrl?: string;
  tags: string[];
  dayOfYear?: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// types/article.ts
export type ArticleStatus = 'draft' | 'review' | 'published' | 'archived';

export interface Article {
  id: string;
  title: string;
  slug: string;
  excerpt?: string;
  content: string;
  featuredImage?: string;
  categoryId?: string;
  category?: ArticleCategory;
  authorId?: string;
  author?: User;
  tags: ArticleTag[];
  status: ArticleStatus;
  publishedAt?: string;
  scheduledAt?: string;
  metaTitle?: string;
  metaDescription?: string;
  ogImage?: string;
  viewCount: number;
  readingTime: number;
  isFeatured: boolean;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// types/event.ts
export type EventType = 
  | 'historical_event' | 'national_day' | 'international_day'
  | 'memorial_day' | 'cultural_event' | 'scientific_event';

export type EventImportance = 'low' | 'medium' | 'high' | 'critical';

export interface HistoricalEvent {
  id: string;
  title: string;
  slug: string;
  eventDate?: string;
  eventDay: number;
  eventMonth: number;
  eventYear?: number;
  isRecurring: boolean;
  eventType: EventType;
  country?: string;
  countryCode?: string;
  flagEmoji?: string;
  shortDescription?: string;
  imageUrl?: string;
  articleId?: string;
  article?: Article;
  importance: EventImportance;
  tags: string[];
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// types/festival.ts
export type CalendarType = 'lunar' | 'solar' | 'both';

export type FestivalType = 
  | 'tet_holiday' | 'folk_festival' | 'religious_festival'
  | 'regional_festival' | 'east_asian_festival' | 'worship_day';

export interface FolkFestival {
  id: string;
  name: string;
  slug: string;
  alternateName?: string;
  calendarType: CalendarType;
  lunarDay?: number;
  lunarMonth?: number;
  solarDay?: number;
  solarMonth?: number;
  durationDays: number;
  festivalType: FestivalType;
  region?: string;
  country: string;
  shortDescription?: string;
  traditions: string[];
  imageUrl?: string;
  galleryUrls: string[];
  articleId?: string;
  article?: Article;
  importance: EventImportance;
  tags: string[];
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// types/person.ts
export type PersonCategory = 
  | 'chinh_tri' | 'khoa_hoc' | 'nghe_thuat' | 'van_hoc'
  | 'the_thao' | 'quan_su' | 'ton_giao' | 'kinh_doanh'
  | 'am_nhac' | 'dien_anh' | 'giao_duc' | 'y_hoc' | 'khac';

export interface FamousPerson {
  id: string;
  name: string;
  originalName?: string;
  birthDate?: string;
  birthDay?: number;
  birthMonth?: number;
  birthYear?: number;
  deathDate?: string;
  nationality?: string;
  occupation?: string;
  category: PersonCategory;
  shortBio?: string;
  imageUrl?: string;
  articleId?: string;
  article?: Article;
  isVietnamese: boolean;
  tags: string[];
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}
```

---

## 8. Chiến Lược Dữ Liệu & SEO

### 8.1 Kế Hoạch Seed Data Ban Đầu

| Loại dữ liệu | Số lượng ban đầu | Nguồn |
|---------------|-------------------|-------|
| Châm ngôn | 365+ (1 câu/ngày) | Tổng hợp từ sách, internet |
| Nhân vật nổi tiếng | 500+ | Wikipedia, Bách khoa toàn thư |
| Sự kiện lịch sử | 1000+ | Wikipedia, sách lịch sử |
| Quốc khánh các nước | 195 nước | UN, Wikipedia |
| Ngày quốc tế | 150+ | UN, UNESCO |
| Lễ hội dân gian VN | 100+ | Sách văn hoá VN |
| Lễ hội Đông Á | 50+ | Tổng hợp |
| Bài viết | 50+ bài seed | Viết tay + AI assisted |
| Danh mục | 8 danh mục chính | Định nghĩa trước |

### 8.2 Chiến Lược SEO

#### URL Structure (SEO-friendly)

```
lichso.vn/                               → Trang chủ
lichso.vn/bai-viet                       → Danh sách bài viết
lichso.vn/bai-viet/tu-vi-tuoi-thin-2026  → Chi tiết bài viết
lichso.vn/su-kien                        → Sự kiện ngày này
lichso.vn/su-kien/chien-thang-dien-bien-phu → Chi tiết sự kiện
lichso.vn/le-hoi                         → Danh sách lễ hội
lichso.vn/le-hoi/tet-trung-thu           → Chi tiết lễ hội
lichso.vn/nhan-vat                       → Nhân vật nổi tiếng
lichso.vn/cham-ngon                      → Châm ngôn
```

#### Meta Tags Template

```html
<!-- Trang bài viết -->
<title>{article.metaTitle || article.title} | Lịch Số</title>
<meta name="description" content="{article.metaDescription || article.excerpt}" />
<meta property="og:title" content="{article.title}" />
<meta property="og:description" content="{article.excerpt}" />
<meta property="og:image" content="{article.ogImage || article.featuredImage}" />
<meta property="og:type" content="article" />
<meta property="article:published_time" content="{article.publishedAt}" />
<meta property="article:author" content="{article.author.name}" />
<meta property="article:tag" content="{article.tags.join(',')}" />
<link rel="canonical" href="https://lichso.vn/bai-viet/{article.slug}" />

<!-- Schema.org Structured Data -->
<script type="application/ld+json">
{
  "@context": "https://schema.org",
  "@type": "Article",
  "headline": "{article.title}",
  "image": "{article.featuredImage}",
  "datePublished": "{article.publishedAt}",
  "author": {
    "@type": "Person",
    "name": "{article.author.name}"
  },
  "publisher": {
    "@type": "Organization",
    "name": "Lịch Số",
    "logo": "https://lichso.vn/favicon.svg"
  }
}
</script>
```

#### Sitemap Strategy

```xml
<!-- sitemap.xml - Generated dynamically -->
<urlset>
  <!-- Static pages -->
  <url><loc>https://lichso.vn/</loc><priority>1.0</priority></url>
  <url><loc>https://lichso.vn/bai-viet</loc><priority>0.9</priority></url>
  <url><loc>https://lichso.vn/su-kien</loc><priority>0.8</priority></url>
  <url><loc>https://lichso.vn/le-hoi</loc><priority>0.8</priority></url>
  
  <!-- Dynamic article pages -->
  <url><loc>https://lichso.vn/bai-viet/tu-vi-tuoi-thin-2026</loc><lastmod>2026-03-01</lastmod></url>
  <url><loc>https://lichso.vn/bai-viet/tet-trung-thu-nguon-goc</loc><lastmod>2026-02-15</lastmod></url>
  <!-- ... -->
  
  <!-- Event pages -->
  <url><loc>https://lichso.vn/su-kien/chien-thang-dien-bien-phu</loc></url>
  <!-- ... -->
  
  <!-- Festival pages -->
  <url><loc>https://lichso.vn/le-hoi/tet-trung-thu</loc></url>
  <!-- ... -->
</urlset>
```

### 8.3 Caching Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                    CACHING LAYERS                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Layer 1: CDN / Nginx Cache                                 │
│  ├─ Static assets: 1 year (immutable)                       │
│  ├─ Article pages (SSG): 1 hour                             │
│  └─ API responses: 5 minutes                                │
│                                                             │
│  Layer 2: Redis Cache                                       │
│  ├─ Quote of the day: 24 hours                              │
│  ├─ Today's events/birthdays: 24 hours                      │
│  ├─ Article by slug: 30 minutes                             │
│  ├─ Popular articles: 1 hour                                │
│  ├─ Search results: 15 minutes                              │
│  └─ View counters: flush every 5 minutes to PostgreSQL      │
│                                                             │
│  Layer 3: Next.js ISR (Incremental Static Regeneration)     │
│  ├─ Article pages: revalidate every 60 seconds              │
│  ├─ Event pages: revalidate every 3600 seconds              │
│  ├─ Festival pages: revalidate every 3600 seconds           │
│  └─ Home page: revalidate every 60 seconds                  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 9. Kế Hoạch Triển Khai

### 9.1 Packages Mới Cần Cài

#### Frontend

| Package | Mục đích |
|---------|----------|
| `@tiptap/react` + extensions | WYSIWYG rich text editor |
| `@tiptap/starter-kit` | Bộ extension cơ bản |
| `@tiptap/extension-image` | Chèn ảnh vào editor |
| `@tiptap/extension-link` | Chèn link |
| `@tiptap/extension-table` | Bảng |
| `papaparse` | Parse CSV cho import |
| `reading-time` | Tính thời gian đọc |
| `react-share` | Social share buttons |
| `next-sitemap` | Tạo sitemap tự động |

#### Backend

| Package | Mục đích |
|---------|----------|
| `github.com/gosimple/slug` | Tạo slug (hỗ trợ Unicode/tiếng Việt) |
| `encoding/csv` | Parse CSV import (Go stdlib) |
| `github.com/microcosm-cc/bluemonday` | Sanitize HTML content |

### 9.2 Environment Variables Mới

```env
# .env additions for v2.0

# Content settings
ARTICLE_DEFAULT_PAGE_SIZE=12
ARTICLE_MAX_PAGE_SIZE=50
ARTICLE_CACHE_TTL=1800          # 30 minutes
QUOTE_CACHE_TTL=86400           # 24 hours
EVENT_CACHE_TTL=86400           # 24 hours

# Upload settings for articles
ARTICLE_IMAGE_MAX_SIZE=5242880  # 5MB
ARTICLE_ALLOWED_IMAGE_TYPES=jpg,jpeg,png,webp,svg

# SEO
SITE_URL=https://lichso.vn
SITE_NAME=Lịch Số
SITE_DESCRIPTION=Lịch Vạn Niên Việt Nam — Tra cứu Âm Dương, Phong Thuỷ, Ngày Tốt
```

### 9.3 RBAC Permissions Mới

| Permission | Mô tả | Roles |
|------------|--------|-------|
| `articles.view` | Xem danh sách bài viết (admin) | Admin, Editor |
| `articles.create` | Tạo bài viết mới | Admin, Editor |
| `articles.edit` | Sửa bài viết | Admin, Editor |
| `articles.delete` | Xoá bài viết | Admin |
| `articles.publish` | Đổi trạng thái publish | Admin |
| `events.manage` | CRUD sự kiện | Admin, Editor |
| `festivals.manage` | CRUD lễ hội | Admin, Editor |
| `people.manage` | CRUD nhân vật | Admin, Editor |
| `quotes.manage` | CRUD châm ngôn | Admin, Editor |
| `categories.manage` | CRUD categories & tags | Admin |
| `content.import` | Import dữ liệu hàng loạt | Admin |
| `analytics.view` | Xem thống kê nội dung | Admin, Editor |

### 9.4 New Role: Editor

```json
{
  "name": "Editor",
  "slug": "editor",
  "description": "Biên tập viên nội dung — quản lý bài viết, sự kiện, lễ hội",
  "permissions": [
    "articles.view", "articles.create", "articles.edit",
    "events.manage", "festivals.manage", "people.manage",
    "quotes.manage", "analytics.view"
  ]
}
```

---

## 10. Roadmap v2.0

### Phase 6 — Content Infrastructure 🔜

- [ ] Database migrations cho bảng mới (quotes, events, festivals, people, articles, categories, tags)
- [ ] GORM models + repositories cho tất cả entities mới
- [ ] Service layer với business logic
- [ ] API handlers + routes (public + admin)
- [ ] Redis caching cho content APIs
- [ ] Seed data script (châm ngôn, sự kiện, lễ hội cơ bản)
- [ ] RBAC permissions mới + Editor role

### Phase 7 — Admin Content Management 🔜

- [ ] Admin sidebar mở rộng
- [ ] CRUD trang cho Bài viết (với WYSIWYG editor TipTap)
- [ ] CRUD trang cho Sự kiện
- [ ] CRUD trang cho Lễ hội
- [ ] CRUD trang cho Nhân vật
- [ ] CRUD trang cho Châm ngôn
- [ ] Quản lý Danh mục & Tags
- [ ] Import hàng loạt (CSV/JSON)
- [ ] Dashboard thống kê nội dung

### Phase 8 — Public Content Pages 🔜

- [ ] Trang châm ngôn ngày (QuoteOfTheDay widget)
- [ ] Trang sự kiện ngày này trong lịch sử
- [ ] Trang lễ hội sắp tới
- [ ] Trang nhân vật nổi tiếng / sinh nhật hôm nay
- [ ] Trang danh sách bài viết (filter, pagination)
- [ ] Trang chi tiết bài viết (SEO optimized, TOC, related)
- [ ] Tích hợp nội dung mới vào trang chủ
- [ ] Full-text search cho bài viết

### Phase 9 — SEO & Performance 🔜

- [ ] Sitemap tự động (next-sitemap)
- [ ] Schema.org structured data cho articles, events, people
- [ ] Open Graph meta tags
- [ ] ISR cho trang bài viết
- [ ] Image optimization (next/image, WebP)
- [ ] Lazy loading cho article content
- [ ] Performance monitoring

### Phase 10 — Analytics & Optimization 📋

- [ ] View tracking (MongoDB)
- [ ] Content analytics dashboard
- [ ] Popular search queries tracking
- [ ] A/B testing cho tiêu đề bài viết
- [ ] RSS feed cho bài viết
- [ ] Newsletter integration
- [ ] Bình luận bài viết (optional)

---

## 📎 Phụ Lục

### A. Dữ Liệu Mẫu — Quốc Khánh Các Nước (Top 20)

| Quốc gia | Ngày | Emoji | Tên sự kiện |
|----------|------|-------|-------------|
| Việt Nam | 02/09 | 🇻🇳 | Quốc khánh Việt Nam |
| Mỹ | 04/07 | 🇺🇸 | Independence Day |
| Pháp | 14/07 | 🇫🇷 | Bastille Day |
| Trung Quốc | 01/10 | 🇨🇳 | Quốc khánh Trung Quốc |
| Nhật Bản | 11/02 | 🇯🇵 | National Foundation Day |
| Hàn Quốc | 15/08 | 🇰🇷 | Liberation Day |
| Ấn Độ | 15/08 | 🇮🇳 | Independence Day |
| Nga | 12/06 | 🇷🇺 | Russia Day |
| Đức | 03/10 | 🇩🇪 | German Unity Day |
| Anh | — | 🇬🇧 | (Không có quốc khánh chính thức) |
| Úc | 26/01 | 🇦🇺 | Australia Day |
| Canada | 01/07 | 🇨🇦 | Canada Day |
| Brazil | 07/09 | 🇧🇷 | Independence Day |
| Mexico | 16/09 | 🇲🇽 | Independence Day |
| Indonesia | 17/08 | 🇮🇩 | Independence Day |
| Thái Lan | 05/12 | 🇹🇭 | National Day |
| Philippines | 12/06 | 🇵🇭 | Independence Day |
| Malaysia | 31/08 | 🇲🇾 | Merdeka Day |
| Singapore | 09/08 | 🇸🇬 | National Day |
| Campuchia | 09/11 | 🇰🇭 | Independence Day |

### B. Dữ Liệu Mẫu — Lễ Hội Dân Gian Việt Nam (Top 20)

| Lễ hội | Loại lịch | Ngày | Vùng |
|--------|-----------|------|------|
| Tết Nguyên Đán | Âm | 1/1 Âm | Toàn quốc |
| Tết Trung Thu | Âm | 15/8 Âm | Toàn quốc |
| Giỗ Tổ Hùng Vương | Âm | 10/3 Âm | Toàn quốc |
| Lễ Vu Lan | Âm | 15/7 Âm | Toàn quốc |
| Tết Đoan Ngọ | Âm | 5/5 Âm | Toàn quốc |
| Tết Hàn Thực | Âm | 3/3 Âm | Bắc Bộ |
| Tết Thượng Nguyên | Âm | 15/1 Âm | Toàn quốc |
| Tết Thanh Minh | Dương | ~5/4 | Toàn quốc |
| Lễ hội Đền Hùng | Âm | 10/3 Âm | Phú Thọ |
| Hội Lim | Âm | 13/1 Âm | Bắc Ninh |
| Lễ hội Chùa Hương | Âm | 6/1-18/3 Âm | Hà Nội |
| Lễ hội Yên Tử | Âm | 10/1 Âm | Quảng Ninh |
| Lễ hội Kate | Dương | 1/7 Chăm | Ninh Thuận |
| Lễ hội Bà Chúa Xứ | Âm | 23-27/4 Âm | An Giang |
| Lễ hội Cổ Loa | Âm | 6/1 Âm | Hà Nội |
| Festival Huế | Dương | Tháng 4 | Huế |
| Lễ hội Gióng | Âm | 6-12/1 Âm | Hà Nội |
| Hội Bài Chòi | Dương | Tết | Trung Bộ |
| Lễ Ok Om Bok | Âm | 15/10 Âm | Sóc Trăng |
| Lễ hội Đua Ghe Ngo | Âm | 14-15/10 Âm | Sóc Trăng |

### C. Dữ Liệu Mẫu — Châm Ngôn (15 câu đầu)

| # | Câu châm ngôn | Tác giả | Quốc tịch |
|---|---------------|---------|-----------|
| 1 | "Hãy là sự thay đổi mà bạn muốn nhìn thấy trong thế giới." | Mahatma Gandhi | Ấn Độ |
| 2 | "Tôi nghĩ, vậy tôi tồn tại." | René Descartes | Pháp |
| 3 | "Cuộc sống là những gì xảy ra khi bạn đang bận lập kế hoạch khác." | John Lennon | Anh |
| 4 | "Không có gì là không thể, chỉ có những điều chúng ta chưa dám làm." | Hồ Chí Minh | Việt Nam |
| 5 | "Giáo dục là vũ khí mạnh nhất để thay đổi thế giới." | Nelson Mandela | Nam Phi |
| 6 | "Hành trình ngàn dặm bắt đầu từ một bước chân." | Lão Tử | Trung Quốc |
| 7 | "Sự thật là con đường duy nhất dẫn đến sự tự do." | Trần Hưng Đạo | Việt Nam |
| 8 | "Trí tưởng tượng quan trọng hơn kiến thức." | Albert Einstein | Đức |
| 9 | "Biết mình biết người, trăm trận trăm thắng." | Tôn Tử | Trung Quốc |
| 10 | "Thành công không phải đích đến, mà là hành trình." | Zig Ziglar | Mỹ |
| 11 | "Một dân tộc dốt là một dân tộc yếu." | Hồ Chí Minh | Việt Nam |
| 12 | "Đừng sợ bước đi chậm, chỉ sợ đứng yên." | Tục ngữ Trung Quốc | Trung Quốc |
| 13 | "Tự do không phải là làm điều mình muốn, mà là không phải làm điều mình không muốn." | Jean-Jacques Rousseau | Pháp |
| 14 | "Người thầy trung bình kể, người thầy giỏi giải thích, người thầy vĩ đại truyền cảm hứng." | William Arthur Ward | Mỹ |
| 15 | "Cây muốn lặng mà gió chẳng dừng." | Tục ngữ Việt Nam | Việt Nam |

### D. Thư Viện Bên Thứ Ba (Dependencies)

#### Frontend (npm packages)

```json
{
  "dependencies": {
    "@tiptap/react": "^2.x",
    "@tiptap/starter-kit": "^2.x",
    "@tiptap/extension-image": "^2.x",
    "@tiptap/extension-link": "^2.x",
    "@tiptap/extension-table": "^2.x",
    "@tiptap/extension-table-row": "^2.x",
    "@tiptap/extension-table-cell": "^2.x",
    "@tiptap/extension-table-header": "^2.x",
    "@tiptap/extension-heading": "^2.x",
    "@tiptap/extension-placeholder": "^2.x",
    "papaparse": "^5.x",
    "reading-time": "^1.x",
    "react-share": "^5.x",
    "next-sitemap": "^4.x"
  }
}
```

#### Backend (Go modules)

```
github.com/gosimple/slug v1.14+
github.com/microcosm-cc/bluemonday v1.0+
```

---

> 📅 Cập nhật lần cuối: 06/03/2026  
> 📝 Tác giả: Zplus Team  
> 🏷️ Version: 2.0.0 (Content Platform)  
> 📜 Document trước: [LICHSO-DOCUMENT.md](./LICHSO-DOCUMENT.md) (v0.5.0)
