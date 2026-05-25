# 📜 Lịch Số — Tài Liệu Chương Trình

> **Lịch Số (曆數)** — Ứng dụng Lịch Vạn Niên Việt Nam, xây dựng trên nền tảng Zplus Base Code

---

## 📋 Mục Lục

1. [Tổng Quan Dự Án](#1-tổng-quan-dự-án)
2. [Kiến Trúc Hệ Thống](#2-kiến-trúc-hệ-thống)
3. [Thiết Kế Giao Diện Trang Chủ](#3-thiết-kế-giao-diện-trang-chủ)
4. [Tính Năng Chi Tiết](#4-tính-năng-chi-tiết)
5. [Design System & UI Guidelines](#5-design-system--ui-guidelines)
6. [Cấu Trúc Dữ Liệu Lịch](#6-cấu-trúc-dữ-liệu-lịch)
7. [Tech Stack](#7-tech-stack)
8. [Cấu Trúc Mã Nguồn](#8-cấu-trúc-mã-nguồn)
9. [API Endpoints (Planned)](#9-api-endpoints-planned)
10. [Triển Khai & Vận Hành](#10-triển-khai--vận-hành)
11. [Roadmap](#11-roadmap)

---

## 1. Tổng Quan Dự Án

### 1.1 Giới Thiệu

**Lịch Số** là ứng dụng web tra cứu Lịch Vạn Niên Việt Nam, kết hợp giữa lịch Dương (Solar) và lịch Âm (Lunar), cung cấp thông tin phong thuỷ, tiết khí, ngày tốt/xấu và các thông tin truyền thống Việt Nam.

Dự án được phát triển dựa trên **Zplus Base Code** — một boilerplate fullstack sử dụng Go Fiber + Next.js + PostgreSQL + MongoDB + Redis với kiến trúc Clean Architecture.

### 1.2 Mục Tiêu

| Mục tiêu | Mô tả |
|-----------|--------|
| **Tra cứu Âm Dương** | Chuyển đổi và tra cứu ngày Âm lịch ↔ Dương lịch chính xác |
| **Thông tin Phong Thuỷ** | Hướng xuất hành tốt, giờ hoàng đạo, sao chiếu mệnh |
| **Ngày Tốt/Xấu** | Đánh giá chỉ số ngày, việc nên làm / không nên làm |
| **Tiết Khí** | Hiển thị 24 tiết khí trong năm, tiến trình tiết khí hiện tại |
| **Trải nghiệm đẹp** | Giao diện mang phong cách Á Đông, ấm áp, tinh tế |

### 1.3 Đối Tượng Người Dùng

- Người Việt Nam cần tra cứu ngày Âm lịch hàng ngày
- Người quan tâm đến phong thuỷ, ngày tốt/xấu cho các sự kiện (cưới hỏi, khai trương, xây nhà...)
- Người tìm hiểu về văn hoá truyền thống Việt Nam
- Nhà nghiên cứu, học sinh cần tra cứu lịch Vạn Niên

---

## 2. Kiến Trúc Hệ Thống

### 2.1 Kiến Trúc Tổng Quan

```
┌─────────────────────────────────────────────────────────┐
│                      NGINX (Reverse Proxy)              │
│                      Port: 80 / 443                     │
└─────────────┬──────────────────────┬────────────────────┘
              │                      │
    ┌─────────▼─────────┐  ┌────────▼────────┐
    │   Frontend (Web)  │  │  Backend (API)  │
    │   Next.js 16      │  │  Go Fiber v2    │
    │   Port: 3000      │  │  Port: 8080     │
    │                   │  │                 │
    │  ┌─────────────┐  │  │ ┌─────────────┐│
    │  │ index.html  │  │  │ │  Handlers   ││
    │  │ (Landing)   │  │  │ │  Services   ││
    │  │             │  │  │ │  Repos      ││
    │  └─────────────┘  │  │ └──────┬──────┘│
    └───────────────────┘  └────────┼───────┘
                                    │
              ┌─────────────────────┼─────────────────┐
              │                     │                  │
    ┌─────────▼──────┐  ┌──────────▼──────┐  ┌───────▼──────┐
    │  PostgreSQL 16 │  │   MongoDB 7.0   │  │   Redis 7    │
    │  (Main Data)   │  │ (Logs/Activity) │  │  (Cache/     │
    │                │  │                 │  │   Sessions)  │
    └────────────────┘  └─────────────────┘  └──────────────┘
```

### 2.2 Mối Quan Hệ Với Zplus Base

Lịch Số kế thừa từ Zplus Base các thành phần:

| Thành phần | Từ Zplus Base | Mở rộng cho Lịch Số |
|------------|---------------|---------------------|
| **Authentication** | JWT + Refresh Token | ✅ Giữ nguyên |
| **RBAC** | Role-Based Access Control | ✅ Giữ nguyên |
| **User Management** | CRUD Users, Roles, Permissions | ✅ Giữ nguyên |
| **Admin Dashboard** | Next.js Admin Panel | ✅ Giữ nguyên |
| **Media** | Upload/Manage files | ✅ Giữ nguyên |
| **Notification** | WebSocket real-time | ✅ Giữ nguyên |
| **Landing Page** | ❌ Chưa có | 🆕 `index.html` — Trang chủ Lịch Số |
| **Calendar Engine** | ❌ Chưa có | 🆕 Thuật toán chuyển đổi Âm-Dương |
| **Phong Thuỷ** | ❌ Chưa có | 🆕 Tính toán ngày tốt, hướng, giờ hoàng đạo |
| **Tiết Khí** | ❌ Chưa có | 🆕 24 tiết khí theo thiên văn |

### 2.3 Clean Architecture Pattern (Backend)

```
cmd/                    → Entry points (server, migrate, seed)
internal/
├── config/             → Application configuration
├── database/           → Database connections (PostgreSQL, MongoDB, Redis)
├── dto/                → Data Transfer Objects (Request/Response)
├── handlers/           → HTTP Handlers (Controllers)
├── middleware/         → Auth, Logger, Rate Limit, Security
├── models/            → Domain Models (GORM entities)
├── repositories/      → Data Access Layer
├── routes/            → Route definitions
├── services/          → Business Logic Layer
├── utils/             → Shared utilities
└── validators/        → Input validation
```

---

## 3. Thiết Kế Giao Diện Trang Chủ

### 3.1 Bố Cục Tổng Quan (Layout)

Trang chủ `index.html` là **Single Page** tĩnh, chứa toàn bộ HTML/CSS/JS inline, với bố cục:

```
┌─────────────────────────────────────────────────────────┐
│  HEADER                                                 │
│  [Logo Seal + Lịch Số] ·············· [Nav: Hôm Nay |  │
│                                     Tra Cứu | Ngày Tốt │
│                                   | Phong Thủy | Tử Vi] │
├─────────────────────────────────────────────────────────┤
│  SEARCH BAR                                             │
│  [Tra cứu ngày âm dương, xem ngày tốt xấu...]  [▸]    │
├─────────────────────────────────────────────────────────┤
│  TABS                                                   │
│  [Lịch Tháng | Tháng Âm | Ngày Tốt | 24 Tiết | Đổi..] │
├──────────────────────────────┬──────────────────────────┤
│  HERO — MAIN DATE CARD      │  INFO PANEL (330px)      │
│                              │                          │
│  ── Dương Lịch ──            │  ┌────────────────────┐  │
│      04                      │  │ Trực Ngày & Giờ    │  │
│  Tháng 3 · 2026 · Thứ Tư    │  │ Hoàng Đạo          │  │
│                              │  │ [Tý] [Sửu] [Ngọ]  │  │
│      ── ✦ ──                 │  └────────────────────┘  │
│                              │  ┌────────────────────┐  │
│  ── Âm Lịch ──               │  │ 🧭 Hướng Xuất Hành │  │
│  Mồng 5 · Tháng Hai         │  └────────────────────┘  │
│  🌒 Trăng lưỡi liềm         │  ┌────────────────────┐  │
│                              │  │ ⚠ Kiêng Kỵ         │  │
│  [Năm Ất Tỵ] [Tháng Bính..] │  └────────────────────┘  │
│  [Ngày Nhâm Ngọ] [Giờ...]   │  ┌────────────────────┐  │
│                              │  │ 🌿 Tiết Khí · Sao   │  │
│  Chỉ số ngày: ████████ 78%  │  └────────────────────┘  │
├──────────────────────────────┴──────────────────────────┤
│  CALENDAR — Lịch Tháng                                  │
│  [‹] ····· Tháng 3 · 2026 ····· [›]                    │
│  ┌────────────────────────────────────────────────────┐  │
│  │ CN  T2  T3  T4  T5  T6  T7                        │  │
│  │  1   2   3  [4]  5   6   7  ← today highlighted   │  │
│  │  8   9  10  11  12  13  14                         │  │
│  │ ···                                                │  │
│  └────────────────────────────────────────────────────┘  │
├─────────────┬─────────────────┬─────────────────────────┤
│  BOTTOM 3-COL                                           │
│  ┌─────────┐ ┌─────────────┐ ┌───────────────────────┐  │
│  │ 🌱 Tiết  │ │ 🧭 La Bàn   │ │ 📋 Việc Nên / Không  │  │
│  │ Khí     │ │ Hướng Tốt   │ │                       │  │
│  │ Vũ Thủy │ │  [Compass]  │ │ ● Xuất hành           │  │
│  │ ████░░  │ │  ĐB · Nam   │ │ ● Giao thương         │  │
│  └─────────┘ └─────────────┘ │ ○ Khai trương          │  │
│                               └───────────────────────┘  │
└─────────────────────────────────────────────────────────┘
│  CLOCK WIDGET (fixed bottom-right)                      │
│  ┌──────────────┐                                       │
│  │  Giờ Hiện Tại │                                      │
│  │   14:25:30    │                                      │
│  │ Giờ Mùi ✦    │                                      │
│  └──────────────┘                                       │
```

### 3.2 Các Thành Phần UI (Components)

#### 3.2.1 Header
- **Logo Seal**: SVG phức tạp với gradient Á Đông (amber/teal), mê cung, xoắn ốc Fibonacci, trăng + sao
- **Logo Text**: "Lịch Số · 曆數" — font Lora serif, phụ đề "Lịch Vạn Niên Việt Nam"
- **Navigation**: 5 mục — Hôm Nay, Tra Cứu, Ngày Tốt, Phong Thủy, Tử Vi
- **Hover effect**: pill-shaped với border amber

#### 3.2.2 Search Bar
- **Input**: Glassmorphism (backdrop-filter blur), border warm
- **Button**: Gradient amber → gold, shadow lift on hover
- **Placeholder**: Gợi ý tra cứu đa năng

#### 3.2.3 Tab Navigation
- **5 tabs**: Lịch Tháng, Tháng Âm Lịch, Ngày Tốt Tháng, 24 Tiết Khí, Đổi Lịch
- **Active indicator**: Underline amber, font-weight 500

#### 3.2.4 Main Date Card (Hero Left)
- **Dương lịch**: Số ngày cỡ lớn (96px), màu amber
- **Âm lịch**: Tên ngày Hán-Việt + thông tin tháng/năm Can Chi
- **Moon phase**: Emoji + mô tả pha trăng
- **Can Chi badges**: 4 badge — Năm/Tháng/Ngày/Giờ với màu riêng biệt
- **Goodness bar**: Progress bar chỉ số ngày tốt/xấu (jade → gold gradient)
- **Watermark**: Chữ Hán "蓮" (Lotus) mờ góc phải dưới

#### 3.2.5 Info Panel (Hero Right)
- **4 info cards** xếp dọc:
  1. Trực Ngày & Giờ Hoàng Đạo (grid tags)
  2. Hướng Xuất Hành Tốt (Tài thần, Hỷ thần)
  3. Kiêng Kỵ Hôm Nay (màu đỏ)
  4. Tiết Khí & Sao Chiếu Mệnh
- **Hover effect**: translateX(3px), border highlight

#### 3.2.6 Calendar Grid
- **Month navigation**: Prev/Next buttons + label
- **Week header**: CN → T7 (CN đỏ, T7 xanh)
- **Day cells**: Solar day (Lora serif) + Lunar day (Noto Serif SC)
- **Today**: Amber top-border gradient + background tint
- **Dots**: Green = ngày tốt, Gold = có sự kiện

#### 3.2.7 Bottom 3-Column Grid
- **Tiết Khí card**: Tên + Hán tự, mô tả thơ, progress bar, tiết khí tiếp theo
- **La Bàn card**: SVG compass với mũi tên hướng tốt (xanh) và hướng xấu (đỏ nét đứt)
- **Việc Nên/Không card**: Danh sách bullet với dots xanh (tốt) / đỏ (xấu)

#### 3.2.8 Clock Widget (Fixed)
- **Position**: Bottom-right, fixed
- **Content**: Giờ real-time (tabular-nums), giờ Can Chi + Hoàng Đạo/Hắc Đạo
- **Style**: Glassmorphism, blur 20px

### 3.3 Background & Decorations

| Layer | Mô tả |
|-------|--------|
| `.bg-layer` | Gradient chính: teal-blue ← → warm cream → peach |
| `.bg-layer::before` | Vầng sáng đèn lồng (top-right), radial gradient vàng cam |
| `.bg-layer::after` | Ambient teal nhẹ (bottom-left) |
| `.bg-svg` | SVG overlay cố định — hoa sen, khung cửa, đèn lồng, mây |

#### SVG Decorations Chi Tiết:
- **Hoa sen trái** (opacity 0.5): 3 bông sen với cánh ellipse, cuống thẳng đứng
- **Khung cửa trái** (opacity 0.18): 2 hình chữ nhật lồng — gợi cửa sổ Á Đông
- **Vòng đèn lồng phải**: 2 circle lớn (r=260, r=220) — vầng trăng/đèn lồng
- **Lưới lồng đèn**: Grid lines ngang + dọc (amber mờ) — gợi khung đèn
- **Đám mây phải** (opacity 0.35): 8 ellipses chồng lên nhau — mây ngũ sắc
- **Hoa sen trên phải** (opacity 0.35): 2 bông sen nhỏ gần vùng đèn lồng

---

## 4. Tính Năng Chi Tiết

### 4.1 Trang Chủ (Đã Triển Khai — `index.html`)

| Tính năng | Trạng thái | Mô tả |
|-----------|------------|--------|
| Hiển thị ngày Dương lịch | ✅ Tĩnh | Ngày/tháng/năm, thứ trong tuần |
| Hiển thị ngày Âm lịch | ✅ Tĩnh | Ngày Mồng/Rằm, tháng, Can Chi |
| Pha trăng | ✅ Tĩnh | Emoji + mô tả |
| Can Chi 4 trụ | ✅ Tĩnh | Năm/Tháng/Ngày/Giờ |
| Chỉ số ngày tốt | ✅ Animated | Progress bar với animation CSS |
| Giờ Hoàng Đạo | ✅ Tĩnh | Grid tags, highlight giờ tốt |
| Hướng xuất hành | ✅ Tĩnh | Tài thần, Hỷ thần |
| Kiêng kỵ | ✅ Tĩnh | Hướng xấu, sao xấu |
| Tiết khí | ✅ Tĩnh | Tên + Hán tự, progress, next |
| Lịch tháng (Calendar) | ✅ Dynamic | JS render, prev/next month |
| La bàn hướng | ✅ SVG | Compass tĩnh |
| Việc nên/không nên | ✅ Tĩnh | Danh sách bullet |
| Đồng hồ real-time | ✅ Dynamic | Cập nhật giây, giờ Can Chi live |
| Tab navigation | ✅ Interactive | Active state toggle (chưa switch content) |
| Search | ✅ UI only | Placeholder, chưa có logic |

### 4.2 Đồng Hồ & Giờ Hoàng Đạo (Logic)

```
Giờ Can Chi:  Tý → Sửu → Dần → Mão → Thìn → Tỵ → Ngọ → Mùi → Thân → Dậu → Tuất → Hợi
Giờ Dương:    23-1  1-3   3-5   5-7   7-9   9-11 11-13 13-15 15-17 17-19 19-21 21-23
Hoàng Đạo:    ✦     ✦     —     —     ✦     —     ✦     ✦     —     ✦     —     —
```

**Công thức tính giờ Can Chi:**
```javascript
const idx = Math.floor(((hours + 1) % 24) / 2);
// idx = 0 → Tý, 1 → Sửu, ..., 11 → Hợi
```

### 4.3 Lịch Tháng (Calendar Logic)

- Tính ngày đầu tháng (`getDay()`) để xác định offset
- Hiển thị 42 ô (6 hàng × 7 cột)
- Ngày tháng trước/sau mờ (class `other`)
- Ngày hôm nay highlight amber
- Ngày Âm lịch hiển thị dưới ngày Dương (giả lập offset `(num+23)%30`)
- Dots: xanh = ngày tốt, vàng = sự kiện

---

## 5. Design System & UI Guidelines

### 5.1 Color Palette

```
┌─────────────────────────────────────────────────────────┐
│  SKY & WATER (Background tones)                        │
│  ┌──────┐ ┌──────┐                                      │
│  │C8DDE0│ │E4EFF1│  Sky Mist / Sky Pale                 │
│  └──────┘ └──────┘                                      │
│                                                         │
│  WARM EARTH (Primary accent tones)                      │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐                   │
│  │F5E8CC│ │F0D4A8│ │D4956A│ │C4783A│                   │
│  │Cream │ │Peach │ │Gold  │ │Amber │ ← Primary         │
│  └──────┘ └──────┘ └──────┘ └──────┘                   │
│                                                         │
│  JADE & TEAL (Positive / Nature)                        │
│  ┌──────┐ ┌──────┐                                      │
│  │4A8B7F│ │6BA898│  Jade Teal / Jade Soft               │
│  └──────┘ └──────┘                                      │
│                                                         │
│  TEXT HIERARCHY                                          │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐                   │
│  │3D2E1A│ │6B5240│ │9A7B62│ │B8A090│                   │
│  │Dark  │ │Mid   │ │Soft  │ │Muted │                   │
│  └──────┘ └──────┘ └──────┘ └──────┘                   │
│                                                         │
│  SEMANTIC                                               │
│  ┌──────┐                                               │
│  │C06060│  Danger / Bad / Sunday                        │
│  └──────┘                                               │
│  ┌──────┐                                               │
│  │5080A0│  Info / Saturday                              │
│  └──────┘                                               │
└─────────────────────────────────────────────────────────┘
```

### 5.2 CSS Custom Properties (Variables)

| Variable | Value | Sử dụng |
|----------|-------|---------|
| `--warm-amber` | `#C4783A` | Primary accent, active states, CTA |
| `--warm-gold` | `#D4956A` | Secondary accent, gradients |
| `--jade-teal` | `#4A8B7F` | Positive indicators, tốt |
| `--jade-soft` | `#6BA898` | Soft positive, progress fills |
| `--text-dark` | `#3D2E1A` | Headings, primary text |
| `--text-mid` | `#6B5240` | Body text |
| `--text-soft` | `#9A7B62` | Secondary text |
| `--text-muted` | `#B8A090` | Labels, captions |
| `--card-bg` | `rgba(255,252,248,0.72)` | Card backgrounds (glassmorphism) |
| `--card-bg-strong` | `rgba(255,252,248,0.88)` | Hover/focus card |
| `--border-warm` | `rgba(196,120,58,0.18)` | Primary borders |
| `--border-soft` | `rgba(196,120,58,0.10)` | Subtle borders |
| `--shadow-warm` | `rgba(180,120,60,0.12)` | Box shadows |

### 5.3 Typography

| Element | Font | Size | Weight | Spacing |
|---------|------|------|--------|---------|
| Body text | Be Vietnam Pro | 13-14px | 300-400 | 0.3px |
| Headings | Lora | 16-20px | 600 | 0.5-1px |
| Solar Day (big) | Lora | 96px → 60px (mobile) | 600 | -3px |
| Chinese chars | Noto Serif SC | 11-26px | 400-700 | 1-2px |
| Labels | Be Vietnam Pro | 10-11px | 600 | 2-3px uppercase |
| Clock time | Lora | 26px | 400 | 1.5px tabular |

### 5.4 Design Principles

1. **Glassmorphism (Kính mờ)**: Cards sử dụng `backdrop-filter: blur()` + semi-transparent backgrounds
2. **Warm Gradient**: Gradient amber-gold cho CTA, progress bars, dividers
3. **Á Đông Aesthetic**: Hoa sen SVG, chữ Hán watermark, la bàn phong thuỷ
4. **Subtle Motion**: `fadeUp` animation cho sections, smooth transitions 0.2-0.35s
5. **Hierarchy rõ ràng**: 4 cấp text color (dark → muted), semantic colors cho tốt/xấu

### 5.5 Responsive Breakpoints

| Breakpoint | Thay đổi |
|------------|----------|
| `> 960px` | Full layout: Hero 2-col, Bottom 3-col |
| `≤ 960px` | Hero 1-col, Bottom 2-col, Solar Day 76px, Info Panel wrap horizontal |
| `≤ 640px` | Bottom 1-col, Nav ẩn, Solar Day 60px, Clock thu nhỏ |

---

## 6. Cấu Trúc Dữ Liệu Lịch

### 6.1 Dữ Liệu Ngày (Day Data Model)

```typescript
interface LichNgay {
  // === Dương Lịch ===
  solarDay: number;          // 4
  solarMonth: number;        // 3
  solarYear: number;         // 2026
  dayOfWeek: string;         // "Thứ Tư"
  
  // === Âm Lịch ===
  lunarDay: number;          // 5
  lunarDayName: string;      // "Mồng 5"
  lunarMonth: number;        // 2
  lunarMonthName: string;    // "Tháng Hai"
  lunarYear: number;         // 2025 (Âm lịch)
  isLeapMonth: boolean;      // false
  
  // === Can Chi (Tứ Trụ) ===
  yearCanChi: string;        // "Ất Tỵ"
  monthCanChi: string;       // "Bính Dần"  
  dayCanChi: string;         // "Nhâm Ngọ"
  hourCanChi: string;        // "Canh Thìn" (tại giờ hiện tại)
  
  // === Phong Thuỷ ===
  trucNgay: string;          // "Trực Nguy"
  trucDanhGia: string;       // "Ngày Bình Thường"
  huongTot: string[];        // ["Đông Bắc", "Nam"]
  huongXau: string[];        // ["Tây"]
  taiThan: string;           // "Đông"
  hyThan: string;            // "Đông Nam"
  saoChieuMenh: string;     // "Thái Dương"
  
  // === Giờ Hoàng Đạo ===
  gioHoangDao: GioCanChi[];  // 12 giờ, đánh dấu hoàng đạo
  
  // === Tiết Khí ===
  tietKhi: string;           // "Vũ Thủy"
  tietKhiHanTu: string;     // "雨水"
  tietKhiProgress: number;  // 0.65 (65%)
  nextTietKhi: string;      // "Kinh Trập"
  
  // === Đánh Giá ===
  chiSoNgay: number;        // 78 (%)
  danhGia: string;          // "Tốt"
  viecNen: string[];        // ["Xuất hành", "Giao thương", ...]
  viecKhong: string[];      // ["Khai trương", "Động thổ", ...]
  
  // === Pha Trăng ===
  moonPhase: string;        // "waxing_crescent"
  moonEmoji: string;        // "🌒"
  moonDesc: string;         // "Trăng lưỡi liềm đầu tháng"
}

interface GioCanChi {
  name: string;             // "Tý"
  range: string;            // "23–1h"
  isHoangDao: boolean;      // true
}
```

### 6.2 Hệ Thống Can Chi

#### Thiên Can (10):
| # | Tên | Ngũ Hành | Âm/Dương |
|---|-----|----------|----------|
| 1 | Giáp | Mộc | Dương |
| 2 | Ất | Mộc | Âm |
| 3 | Bính | Hoả | Dương |
| 4 | Đinh | Hoả | Âm |
| 5 | Mậu | Thổ | Dương |
| 6 | Kỷ | Thổ | Âm |
| 7 | Canh | Kim | Dương |
| 8 | Tân | Kim | Âm |
| 9 | Nhâm | Thuỷ | Dương |
| 10 | Quý | Thuỷ | Âm |

#### Địa Chi (12):
| # | Tên | Con giáp | Giờ |
|---|-----|----------|-----|
| 1 | Tý | Chuột | 23–1h |
| 2 | Sửu | Trâu | 1–3h |
| 3 | Dần | Hổ | 3–5h |
| 4 | Mão | Mèo | 5–7h |
| 5 | Thìn | Rồng | 7–9h |
| 6 | Tỵ | Rắn | 9–11h |
| 7 | Ngọ | Ngựa | 11–13h |
| 8 | Mùi | Dê | 13–15h |
| 9 | Thân | Khỉ | 15–17h |
| 10 | Dậu | Gà | 17–19h |
| 11 | Tuất | Chó | 19–21h |
| 12 | Hợi | Heo | 21–23h |

### 6.3 24 Tiết Khí

| # | Tên | Hán Tự | Khoảng Dương lịch |
|---|-----|--------|-------------------|
| 1 | Tiểu Hàn | 小寒 | ~6/1 |
| 2 | Đại Hàn | 大寒 | ~20/1 |
| 3 | Lập Xuân | 立春 | ~4/2 |
| 4 | Vũ Thủy | 雨水 | ~19/2 |
| 5 | Kinh Trập | 驚蟄 | ~6/3 |
| 6 | Xuân Phân | 春分 | ~21/3 |
| 7 | Thanh Minh | 清明 | ~5/4 |
| 8 | Cốc Vũ | 穀雨 | ~20/4 |
| 9 | Lập Hạ | 立夏 | ~6/5 |
| 10 | Tiểu Mãn | 小滿 | ~21/5 |
| 11 | Mang Chủng | 芒種 | ~6/6 |
| 12 | Hạ Chí | 夏至 | ~21/6 |
| 13 | Tiểu Thử | 小暑 | ~7/7 |
| 14 | Đại Thử | 大暑 | ~23/7 |
| 15 | Lập Thu | 立秋 | ~7/8 |
| 16 | Xử Thử | 處暑 | ~23/8 |
| 17 | Bạch Lộ | 白露 | ~8/9 |
| 18 | Thu Phân | 秋分 | ~23/9 |
| 19 | Hàn Lộ | 寒露 | ~8/10 |
| 20 | Sương Giáng | 霜降 | ~23/10 |
| 21 | Lập Đông | 立冬 | ~7/11 |
| 22 | Tiểu Tuyết | 小雪 | ~22/11 |
| 23 | Đại Tuyết | 大雪 | ~7/12 |
| 24 | Đông Chí | 冬至 | ~22/12 |

---

## 7. Tech Stack

### 7.1 Frontend — Trang Chủ (`index.html`)

| Công nghệ | Mục đích |
|------------|----------|
| **HTML5** | Semantic markup |
| **CSS3** | Custom Properties, Grid, Flexbox, Glassmorphism, Animations |
| **Vanilla JavaScript** | Calendar rendering, Clock widget, Tab/Nav interaction |
| **Google Fonts** | Be Vietnam Pro, Lora, Noto Serif SC |
| **SVG** | Logo seal, background decorations, compass |

### 7.2 Frontend — Admin & App (`frontend/`)

| Công nghệ | Version | Mục đích |
|------------|---------|----------|
| **Next.js** | 16.1.6 | React framework, SSR/SSG |
| **React** | 19.2.3 | UI library |
| **TypeScript** | 5.x | Type safety |
| **Tailwind CSS** | 4.x | Utility-first CSS |
| **shadcn/ui** | 3.8.5 | Component library (Radix UI based) |
| **Zustand** | 5.0.11 | State management |
| **TanStack React Query** | 5.90 | Server state / data fetching |
| **Axios** | 1.13.6 | HTTP client |
| **React Hook Form** | 7.71 | Form management |
| **Zod** | 4.3 | Schema validation |
| **date-fns** | 4.1 | Date utilities |
| **Sonner** | 2.0 | Toast notifications |
| **Lucide React** | 0.576 | Icon library |
| **Vitest** | 4.0 | Unit testing |

### 7.3 Backend (`backend/`)

| Công nghệ | Version | Mục đích |
|------------|---------|----------|
| **Go** | 1.24.5 | Language |
| **Go Fiber** | v2.52 | HTTP framework |
| **GORM** | 1.31 | ORM for PostgreSQL |
| **JWT (golang-jwt)** | v5.3 | Authentication tokens |
| **Zap** | 1.27 | Structured logging |
| **Viper** | 1.21 | Configuration management |
| **Validator** | v10.30 | Input validation |
| **pgx** | v5.6 | PostgreSQL driver |
| **go-redis** | v9.18 | Redis client |
| **mongo-driver** | 1.17 | MongoDB driver |

### 7.4 Infrastructure

| Service | Image/Version | Port | Mục đích |
|---------|---------------|------|----------|
| **Nginx** | 1.25-alpine | 80/443 | Reverse proxy, SSL termination |
| **PostgreSQL** | 16-alpine | 5432 | Primary database (users, roles, permissions) |
| **MongoDB** | 7.0 | 27017 | Activity logs, analytics |
| **Redis** | 7-alpine | 6379 | Cache, sessions, rate limiting |

---

## 8. Cấu Trúc Mã Nguồn

### 8.1 Cấu Trúc Toàn Dự Án

```
Zplus_Lichso/
│
├── index.html                  # 🆕 Trang chủ Lịch Số (Landing Page)
│
├── backend/                    # Go Fiber API Server
│   ├── cmd/
│   │   ├── server/main.go      # HTTP server entry point
│   │   ├── migrate/main.go     # Database migration runner
│   │   └── seed/main.go        # Data seeder
│   ├── internal/
│   │   ├── config/             # App configuration (Viper)
│   │   ├── database/           # DB connections (PostgreSQL, MongoDB, Redis)
│   │   ├── dto/                # Request/Response DTOs
│   │   │   ├── auth_dto.go
│   │   │   ├── user_dto.go
│   │   │   ├── role_dto.go
│   │   │   ├── setting_dto.go
│   │   │   └── pagination_dto.go
│   │   ├── handlers/           # HTTP Handlers (Controllers)
│   │   │   ├── auth_handler.go
│   │   │   ├── user_handler.go
│   │   │   ├── role_handler.go
│   │   │   ├── admin_handler.go
│   │   │   ├── media_handler.go
│   │   │   ├── notification_handler.go
│   │   │   ├── permission_handler.go
│   │   │   ├── setting_handler.go
│   │   │   └── email_handler.go
│   │   ├── middleware/         # HTTP Middleware
│   │   │   ├── auth.go            # JWT authentication
│   │   │   ├── permission.go      # RBAC permission check
│   │   │   ├── rate_limit.go      # Rate limiting (Redis)
│   │   │   ├── logger.go          # Request logging
│   │   │   ├── security.go        # Security headers
│   │   │   ├── recovery.go        # Panic recovery
│   │   │   └── error_tracking.go  # Error tracking
│   │   ├── models/            # GORM Domain Models
│   │   │   ├── user.go
│   │   │   ├── role.go
│   │   │   ├── permission.go
│   │   │   ├── user_role.go
│   │   │   ├── role_permission.go
│   │   │   ├── refresh_token.go
│   │   │   ├── media.go
│   │   │   ├── notification.go
│   │   │   ├── setting.go
│   │   │   └── activity_log.go
│   │   ├── repositories/      # Data Access Layer
│   │   │   ├── user_repo.go
│   │   │   ├── role_repo.go
│   │   │   ├── permission_repo.go
│   │   │   ├── media_repo.go
│   │   │   ├── notification_repo.go
│   │   │   ├── setting_repo.go
│   │   │   └── activity_log_repo.go
│   │   ├── routes/            # Route Definitions
│   │   │   ├── auth_routes.go
│   │   │   ├── admin_routes.go
│   │   │   ├── media_routes.go
│   │   │   └── notification_routes.go
│   │   ├── services/          # Business Logic
│   │   │   ├── auth_service.go
│   │   │   ├── user_service.go
│   │   │   ├── role_service.go
│   │   │   ├── permission_service.go
│   │   │   ├── rbac_service.go
│   │   │   ├── cache_service.go
│   │   │   ├── media_service.go
│   │   │   ├── notification_service.go
│   │   │   ├── email_service.go
│   │   │   ├── email_templates.go
│   │   │   ├── setting_service.go
│   │   │   └── websocket_hub.go
│   │   ├── utils/             # Shared Utilities
│   │   └── validators/        # Custom Validators
│   ├── migrations/            # SQL Migration Files
│   ├── uploads/               # Uploaded files storage
│   ├── Dockerfile
│   ├── go.mod
│   └── go.sum
│
├── frontend/                   # Next.js Admin Application
│   ├── src/
│   │   ├── middleware.ts       # Next.js middleware (auth guard)
│   │   ├── app/
│   │   │   ├── (auth)/         # Auth pages (login, register)
│   │   │   ├── (home)/         # Protected home pages
│   │   │   ├── admin/          # Admin dashboard
│   │   │   ├── layout.tsx      # Root layout
│   │   │   ├── globals.css     # Global styles
│   │   │   ├── loading.tsx     # Loading UI
│   │   │   ├── error.tsx       # Error boundary
│   │   │   └── not-found.tsx   # 404 page
│   │   ├── components/
│   │   │   ├── ui/             # shadcn/ui base components
│   │   │   ├── shared/         # Shared/common components
│   │   │   ├── layouts/        # Layout components
│   │   │   ├── auth/           # Auth-related components
│   │   │   ├── users/          # User management components
│   │   │   ├── roles/          # Role management components
│   │   │   └── media/          # Media management components
│   │   ├── hooks/              # Custom React Hooks
│   │   │   ├── useAuth.ts
│   │   │   ├── useDashboard.ts
│   │   │   ├── useMedia.ts
│   │   │   ├── useNotifications.ts
│   │   │   ├── usePermission.ts
│   │   │   └── useUsers.ts
│   │   ├── services/           # API Service Layer
│   │   │   ├── authService.ts
│   │   │   ├── userService.ts
│   │   │   ├── roleService.ts
│   │   │   ├── adminService.ts
│   │   │   ├── mediaService.ts
│   │   │   ├── notificationService.ts
│   │   │   └── permissionService.ts
│   │   ├── stores/             # Zustand Stores
│   │   │   ├── authStore.ts
│   │   │   ├── notificationStore.ts
│   │   │   └── uiStore.ts
│   │   ├── types/              # TypeScript Type Definitions
│   │   │   ├── api.ts
│   │   │   ├── auth.ts
│   │   │   ├── user.ts
│   │   │   ├── role.ts
│   │   │   ├── permission.ts
│   │   │   ├── media.ts
│   │   │   ├── notification.ts
│   │   │   ├── dashboard.ts
│   │   │   └── settings.ts
│   │   ├── lib/                # Utilities
│   │   │   ├── api.ts          # Axios instance
│   │   │   ├── constants.ts
│   │   │   └── utils.ts
│   │   └── __tests__/          # Test files
│   ├── public/                 # Static assets
│   ├── package.json
│   ├── tsconfig.json
│   ├── vitest.config.ts
│   └── Dockerfile
│
├── docker/                     # Docker Configurations
│   ├── nginx/                  # Nginx reverse proxy config
│   ├── postgres/init.sql       # PostgreSQL init script
│   ├── mongodb/init.js         # MongoDB init script
│   └── redis/redis.conf        # Redis configuration
│
├── scripts/                    # Utility Scripts
│   ├── setup.sh                # Project setup
│   ├── backup.sh               # Database backup
│   ├── restore.sh              # Database restore
│   └── health-check.sh         # Service health check
│
├── docs/                       # Documentation (this folder)
├── docker-compose.yml          # Development orchestration
├── docker-compose.prod.yml     # Production orchestration
├── Makefile                    # Command shortcuts
└── README.md
```

### 8.2 Database Schema (PostgreSQL)

```sql
-- Migrations đã có:
-- 000001: users              — Bảng người dùng
-- 000002: roles              — Bảng vai trò
-- 000003: permissions        — Bảng quyền
-- 000004: user_roles         — Quan hệ N-N user ↔ role
-- 000005: role_permissions   — Quan hệ N-N role ↔ permission
-- 000006: refresh_tokens     — JWT refresh tokens
-- 000007: performance_indexes — Index tối ưu
-- 000008: social_login_fields — Google OAuth fields
```

---

## 9. API Endpoints (Planned)

### 9.1 Endpoints Hiện Có (Từ Zplus Base)

| Method | Endpoint | Mô tả |
|--------|----------|--------|
| POST | `/api/auth/register` | Đăng ký |
| POST | `/api/auth/login` | Đăng nhập |
| POST | `/api/auth/refresh` | Refresh token |
| POST | `/api/auth/logout` | Đăng xuất |
| GET | `/api/users/me` | Profile |
| GET | `/api/admin/users` | List users |
| CRUD | `/api/admin/roles` | Manage roles |
| CRUD | `/api/admin/permissions` | Manage permissions |
| CRUD | `/api/media` | Upload/manage media |
| GET | `/api/notifications` | Get notifications |

### 9.2 Endpoints Lịch Số (Cần Phát Triển)

| Method | Endpoint | Mô tả |
|--------|----------|--------|
| GET | `/api/calendar/today` | Thông tin ngày hôm nay (Âm + Dương + Phong Thuỷ) |
| GET | `/api/calendar/date/:date` | Tra cứu ngày cụ thể (format: YYYY-MM-DD) |
| GET | `/api/calendar/month/:year/:month` | Lịch tháng với Âm lịch |
| GET | `/api/calendar/convert` | Đổi Âm ↔ Dương |
| GET | `/api/calendar/good-days` | Ngày tốt trong tháng |
| GET | `/api/calendar/solar-terms/:year` | 24 tiết khí trong năm |
| GET | `/api/fengshui/direction/:date` | Hướng xuất hành tốt |
| GET | `/api/fengshui/hours/:date` | Giờ hoàng đạo |
| GET | `/api/fengshui/activities/:date` | Việc nên / không nên |
| GET | `/api/horoscope/zodiac/:year` | Tử vi theo năm sinh |
| GET | `/api/search` | Tìm kiếm ngày/sự kiện |

---

## 10. Triển Khai & Vận Hành

### 10.1 Development

```bash
# Setup ban đầu
make setup              # Copy .env files
make up                 # Start Docker services
make migrate            # Run DB migrations
make seed               # Seed default data

# Development
make dev-api            # Backend (Go + Air hot reload)
make dev-web            # Frontend (Next.js dev server)

# Mở index.html trực tiếp trong browser cho Landing Page
```

### 10.2 Testing

```bash
make test-api           # Backend Go tests
make test-web           # Frontend Vitest tests
make ci                 # Full CI pipeline (lint + test)
```

### 10.3 Production

```bash
make build-prod         # Build production images
make deploy-prod        # Deploy production
make backup             # Backup databases
make health             # Check service health
```

### 10.4 Service URLs

| Môi trường | Service | URL |
|------------|---------|-----|
| Development | Frontend | http://localhost:3000 |
| Development | Backend API | http://localhost:8080 |
| Development | Nginx Proxy | http://localhost |
| Development | Landing Page | Mở `index.html` trực tiếp |

---

## 11. Roadmap

### Phase 1 — Landing Page ✅ (Hiện tại)
- [x] Thiết kế giao diện trang chủ (`index.html`)
- [x] Calendar tháng (JavaScript vanilla)
- [x] Đồng hồ real-time + Giờ Can Chi
- [x] Background SVG Á Đông (hoa sen, mây, đèn lồng)
- [x] Logo seal SVG
- [x] Responsive design (3 breakpoints)

### Phase 2 — Calendar Engine ✅
- [x] Thuật toán chuyển đổi Âm lịch ↔ Dương lịch chính xác
- [x] Tính Can Chi 4 trụ (Năm/Tháng/Ngày/Giờ)
- [x] Tính 24 tiết khí theo thiên văn
- [x] Xác định pha trăng
- [x] API endpoints cho calendar

### Phase 3 — Phong Thuỷ Engine ✅
- [x] Tính trực ngày (12 trực)
- [x] Xác định sao chiếu mệnh (28 sao)
- [x] Giờ hoàng đạo / hắc đạo
- [x] Hướng xuất hành (Tài thần, Hỷ thần, Hung thần)
- [x] Việc nên / không nên theo ngày
- [x] Chỉ số ngày tốt/xấu

### Phase 4 — Frontend Integration ✅
- [x] Chuyển Landing Page sang Next.js component
- [x] Kết nối API cho dữ liệu real-time
- [x] Tra cứu ngày (search functionality)
- [x] Tab content switching (Lịch Tháng, Tháng Âm, Ngày Tốt, 24 Tiết, Đổi lịch)
- [x] Chi tiết ngày khi click vào calendar cell

### Phase 5 — Advanced Features 🔄
- [x] Tử vi giản lược
- [x] Ngày lễ / sự kiện Việt Nam
- [x] Bookmark ngày quan trọng (cần đăng nhập)
- [x] Nhắc nhở ngày lễ / giỗ
- [x] Xuất lịch (PDF, iCal)
- [x] Progressive Web App (PWA)
- [x] Dark mode / theme switching

---

## 📎 Phụ Lục

### A. Fonts Sử Dụng

| Font | Nguồn | Mục đích |
|------|-------|----------|
| [Be Vietnam Pro](https://fonts.google.com/specimen/Be+Vietnam+Pro) | Google Fonts | Body text — Font Việt tối ưu |
| [Lora](https://fonts.google.com/specimen/Lora) | Google Fonts | Headings, số lớn — Serif thanh lịch |
| [Noto Serif SC](https://fonts.google.com/specimen/Noto+Serif+SC) | Google Fonts | Chữ Hán, nhãn Âm lịch |

### B. Animations

| Animation | Duration | Easing | Delay |
|-----------|----------|--------|-------|
| `fadeUp` | 0.65-0.85s | ease-out | 0-0.1s |
| Progress bars | 1.6s | cubic-bezier(0.16,1,0.3,1) | 0.3-0.4s |
| Hover transitions | 0.2-0.35s | ease | — |

### C. Tham Khảo

- [Thuật toán Âm lịch Việt Nam](https://www.informatik.uni-leipzig.de/~duc/amlich/)
- [24 Tiết Khí](https://vi.wikipedia.org/wiki/Ti%E1%BA%BFt_kh%C3%AD)
- [Zplus Base Code](https://github.com/zplus) — Nền tảng gốc

---

> 📅 Cập nhật lần cuối: 05/03/2026  
> 📝 Tác giả: Zplus Team  
> 🏷️ Version: 0.5.0 (Advanced Features Phase)
