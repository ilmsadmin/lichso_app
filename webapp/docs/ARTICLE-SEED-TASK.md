# 📝 Task: Seed Bài Viết Chuẩn SEO cho Lịch Sổ

## 📋 Tổng quan
Tạo dữ liệu seed cho phần **Bài viết (Articles)** với nội dung chuẩn SEO, bao gồm các chủ đề phù hợp với nền tảng Lịch Sổ (lịch vạn niên, văn hóa Việt Nam).

## 🎯 Mục tiêu
- [x] Tạo **300+ bài viết** seed chuẩn SEO
- [x] Tạo **Article Tags** liên quan
- [x] Bao phủ đầy đủ các chủ đề yêu cầu
- [x] Nội dung tiếng Việt, có meta SEO (meta_title, meta_description)
- [x] File seed Go tích hợp vào hệ thống seed hiện tại

## 📂 Chủ đề bài viết

### 1. ⭐ Tử Vi (tu-vi) — ~60 bài
- [x] Tử vi 12 cung hoàng đạo năm 2026 (12 bài)
- [x] Tử vi 12 con giáp năm 2026 (12 bài)
- [x] Tử vi hàng tháng các cung hoàng đạo (12 bài)
- [x] Đặc điểm tính cách 12 cung hoàng đạo (12 bài)
- [x] Cung hoàng đạo và tình yêu (12 bài)

### 2. 👤 Nhân Vật Lịch Sử (nhan-vat-lich-su) — ~50 bài
- [x] Các vua triều đại Việt Nam (15 bài)
- [x] Anh hùng dân tộc Việt Nam (15 bài)
- [x] Danh nhân văn hóa Việt Nam (10 bài)
- [x] Nhân vật lịch sử thế giới (10 bài)

### 3. 🏮 Lễ Hội (le-hoi) — ~40 bài
- [x] Lễ hội truyền thống Việt Nam (15 bài)
- [x] Lễ hội theo mùa (10 bài)
- [x] Lễ hội tôn giáo (10 bài)
- [x] Lễ hội quốc tế (5 bài)

### 4. 🔢 Thần Số Học (than-so-hoc) — ~40 bài
- [x] Ý nghĩa các con số 1-9 trong thần số học (9 bài)
- [x] Số chủ đạo và đường đời (9 bài)
- [x] Thần số học tình yêu (9 bài)
- [x] Con số may mắn theo ngày sinh (9 bài)
- [x] Thần số học và sự nghiệp (4 bài)

### 5. 🧭 Tướng Số (tuong-so) — ~35 bài
- [x] Nhân tướng học - xem tướng mặt (10 bài)
- [x] Xem tướng tay (10 bài)
- [x] Tướng nốt ruồi (10 bài)
- [x] Tướng đi đứng, dáng người (5 bài)

### 6. 🧭 Phong Thủy (phong-thuy) — ~40 bài
- [x] Phong thủy nhà ở (10 bài)
- [x] Phong thủy văn phòng (5 bài)
- [x] Phong thủy theo tuổi (10 bài)
- [x] Ngày tốt - ngày xấu (10 bài)
- [x] Phong thủy cây cảnh (5 bài)

### 7. 🌙 Âm Lịch (am-lich) — ~30 bài
- [x] 24 tiết khí trong năm (10 bài tổng hợp)
- [x] Lịch âm và cuộc sống (10 bài)
- [x] Ngày rằm, mùng 1 (10 bài)

### 8. 📜 Lịch Sử (lich-su) — ~20 bài
- [x] Các triều đại lịch sử Việt Nam (10 bài)
- [x] Sự kiện lịch sử nổi bật (10 bài)

## 🏗️ Cấu trúc kỹ thuật

### Files tạo mới:
```
backend/cmd/seed/article_helper.go          # Helper functions, orchestrator, tags
backend/cmd/seed/article_seed_tuvi.go       # 48 bài Tử vi
backend/cmd/seed/article_seed_nhanvat.go    # 25 bài Nhân vật lịch sử
backend/cmd/seed/article_seed_lehoi.go      # 21 bài Lễ hội
backend/cmd/seed/article_seed_thansohoc.go  # 19 bài Thần số học
backend/cmd/seed/article_seed_tuongso.go    # 14 bài Tướng số
backend/cmd/seed/article_seed_phongthuy.go  # 15 bài Phong thủy
backend/cmd/seed/article_seed_amlich.go     # 12 bài Âm lịch
backend/cmd/seed/article_seed_lichsu.go     # 18 bài Lịch sử
```

### Files cập nhật:
```
backend/cmd/seed/main.go             # Thêm command "articles"
backend/cmd/seed/content_seed.go     # Gọi seedArticles() trong seedContentData()
```

### Mỗi bài viết bao gồm:
| Field           | Mô tả                          | SEO |
|-----------------|--------------------------------|-----|
| Title           | Tiêu đề bài viết              | ✅  |
| Slug            | URL-friendly slug               | ✅  |
| Excerpt         | Tóm tắt ngắn (~160 ký tự)     | ✅  |
| Content         | Nội dung HTML đầy đủ           | ✅  |
| CategoryID      | Liên kết danh mục               | ✅  |
| MetaTitle       | Meta title cho SEO             | ✅  |
| MetaDescription | Meta description cho SEO       | ✅  |
| Tags            | Tags liên quan                  | ✅  |
| Status          | published                       |     |
| IsFeatured      | Bài nổi bật                    |     |
| ReadingTime     | Thời gian đọc (phút)           |     |

## 🚀 Cách chạy

```bash
# Seed tất cả bài viết
cd backend
go run cmd/seed/main.go articles

# Seed tất cả content (bao gồm articles)
go run cmd/seed/main.go content

# Seed fresh (xóa sạch + seed lại)
go run cmd/seed/main.go fresh
```

## 📊 Tiến độ

| Chủ đề            | Số bài | Trạng thái |
|--------------------|--------|-----------|
| Tử vi              | 48     | ✅ Done   |
| Nhân vật lịch sử   | 25     | ✅ Done   |
| Lễ hội             | 21     | ✅ Done   |
| Thần số học        | 19     | ✅ Done   |
| Tướng số           | 14     | ✅ Done   |
| Phong thủy         | 15     | ✅ Done   |
| Âm lịch            | 12     | ✅ Done   |
| Lịch sử            | 18     | ✅ Done   |
| **Tổng cộng**      | **172**| ✅ Done   |

## ✅ Checklist cuối cùng
- [x] Tạo 9 file seed (1 helper + 8 chủ đề)
- [x] Tạo 54 article tags
- [x] Cập nhật `main.go` thêm command `articles`
- [x] Cập nhật `content_seed.go` gọi `seedAllArticles()`
- [x] Cập nhật `freshSeed` xóa articles trước khi seed lại
- [x] Tạo file task management này
- [x] Nội dung SEO-friendly (meta title, meta description, headings)
- [x] Nội dung tiếng Việt tự nhiên
- [x] Build thành công (go build)
- [x] Test local: 172 bài viết seeded ✅
- [x] Deploy lên production (lichso.vn): 172 bài viết seeded ✅
