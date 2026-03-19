# 📱 LỊCH SỐ — Tài Liệu Dự Án Chi Tiết

> **Phiên bản tài liệu:** 1.0  
> **Ngày tạo:** 18/03/2026  
> **Nền tảng:** Android (Native)  
> **Phiên bản ứng dụng mục tiêu:** v1.0  
> **Trạng thái:** Thiết kế UI hoàn tất — Chuẩn bị phát triển

---

## 📋 MỤC LỤC

1. [Tổng quan dự án](#1-tổng-quan-dự-án)
2. [Tầm nhìn & Mục tiêu](#2-tầm-nhìn--mục-tiêu)
3. [Đối tượng người dùng](#3-đối-tượng-người-dùng)
4. [Kiến trúc ứng dụng](#4-kiến-trúc-ứng-dụng)
5. [Cấu trúc màn hình & Navigation](#5-cấu-trúc-màn-hình--navigation)
6. [Chi tiết tính năng](#6-chi-tiết-tính-năng)
7. [Hệ thống Design System](#7-hệ-thống-design-system)
8. [Trợ lý AI — Kiến trúc & Tính năng](#8-trợ-lý-ai--kiến-trúc--tính-năng)
9. [Mô hình dữ liệu (Data Models)](#9-mô-hình-dữ-liệu-data-models)
10. [API & Backend Services](#10-api--backend-services)
11. [Thông báo & Nhắc nhở](#11-thông-báo--nhắc-nhở)
12. [Mô hình kinh doanh (Monetization)](#12-mô-hình-kinh-doanh-monetization)
13. [Yêu cầu phi chức năng](#13-yêu-cầu-phi-chức-năng)
14. [Kế hoạch phát triển (Roadmap)](#14-kế-hoạch-phát-triển-roadmap)
15. [Phụ lục kỹ thuật](#15-phụ-lục-kỹ-thuật)

---

## 1. TỔNG QUAN DỰ ÁN

### 1.1. Giới thiệu

**Lịch Số** là ứng dụng Android kết hợp **Lịch Vạn Niên** truyền thống Việt Nam với **trợ lý AI thông minh**, cung cấp trải nghiệm quản lý thời gian toàn diện bao gồm:

- Lịch dương — âm lịch đồng bộ
- Tra cứu can chi, tiết khí, giờ hoàng đạo, hướng xuất hành
- Quản lý công việc, ghi chú, nhắc nhở
- Trợ lý AI chat tích hợp — tư vấn dựa trên lịch vạn niên
- Hệ thống AI Template cho các tác vụ lặp lại

### 1.2. Định vị sản phẩm

| Thuộc tính | Giá trị |
|---|---|
| **Tên ứng dụng** | Lịch Số — Lịch Vạn Niên |
| **Nền tảng** | Android (API 26+, Android 8.0 Oreo trở lên) |
| **Ngôn ngữ chính** | Tiếng Việt |
| **Phong cách UI** | Premium Dark Theme — Vàng gold + Teal accent |
| **Phân loại** | Productivity / Lifestyle / Calendar |
| **Unique Value** | Lịch vạn niên kết hợp AI — duy nhất trên thị trường Việt Nam |

### 1.3. Tóm tắt tính năng cốt lõi

```
┌────────────────────────────────────────────────────┐
│                    LỊCH SỐ APP                     │
├──────────────┬──────────────┬──────────────────────┤
│  📅 Lịch     │  ✅ Công việc │  🤖 AI Chat         │
│  Vạn Niên    │  Ghi chú     │  Trợ lý thông minh  │
│  Can chi     │  Nhắc nhở    │  Template AI        │
│  Tiết khí    │  Task mgmt   │  Tư vấn can chi     │
│  Hoàng đạo   │  Notes       │  Tạo task tự động   │
└──────────────┴──────────────┴──────────────────────┘
```

---

## 2. TẦM NHÌN & MỤC TIÊU

### 2.1. Tầm nhìn

Trở thành ứng dụng lịch **#1 Việt Nam** — kết hợp giá trị văn hóa truyền thống với công nghệ AI hiện đại, phục vụ hàng triệu người dùng Việt Nam trong cuộc sống hàng ngày.

### 2.2. Mục tiêu kinh doanh

| Chỉ số | Mục tiêu Q2/2026 | Mục tiêu cuối năm 2026 |
|---|---|---|
| Lượt cài đặt | 100,000 | 500,000 |
| DAU (Daily Active Users) | 30,000 | 150,000 |
| Tỷ lệ giữ chân (D7) | 45% | 55% |
| Chuyển đổi Pro | 3% | 5% |
| Đánh giá Google Play | ≥ 4.5⭐ | ≥ 4.6⭐ |

### 2.3. Mục tiêu kỹ thuật

- **Thời gian khởi động (Cold Start):** < 1.5 giây
- **Kích thước APK:** < 25MB
- **Offline-first:** Toàn bộ tính năng lịch hoạt động offline
- **Hiệu năng cuộn lịch:** 60 FPS không drop frame
- **Thời gian phản hồi AI:** < 3 giây cho câu trả lời đầu tiên

---

## 3. ĐỐI TƯỢNG NGƯỜI DÙNG

### 3.1. Persona chính

#### Persona 1: Người dùng truyền thống (40–65 tuổi)
- **Nhu cầu:** Xem lịch âm, tra ngày tốt xấu, xem giờ hoàng đạo
- **Tần suất:** Hàng ngày, đặc biệt vào đầu tháng âm, ngày rằm
- **Kỹ năng công nghệ:** Cơ bản
- **Kỳ vọng:** Giao diện rõ ràng, chữ lớn, thông tin chính xác

#### Persona 2: Người dùng hiện đại (25–40 tuổi)
- **Nhu cầu:** Quản lý công việc kết hợp xem lịch truyền thống
- **Tần suất:** Hàng ngày
- **Kỹ năng công nghệ:** Cao
- **Kỳ vọng:** UI đẹp, tích hợp AI, quản lý task hiệu quả

#### Persona 3: Doanh nhân / Kinh doanh
- **Nhu cầu:** Chọn ngày tốt cho giao dịch, ký hợp đồng, khai trương
- **Tần suất:** Hàng tuần
- **Kỹ năng công nghệ:** Trung bình — Cao
- **Kỳ vọng:** Tư vấn chính xác, nhắc nhở đúng lúc

---

## 4. KIẾN TRÚC ỨNG DỤNG

### 4.1. Kiến trúc tổng quan

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                    │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────────┐ │
│  │ Calendar  │ │  Tasks   │ │ AI Chat  │ │  Settings  │ │
│  │  Screen   │ │  Screen  │ │  Screen  │ │   Screen   │ │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └─────┬──────┘ │
│       │             │            │              │        │
│  ┌────┴─────────────┴────────────┴──────────────┴────┐  │
│  │              VIEWMODEL LAYER (MVVM)               │  │
│  │  CalendarVM │ TaskVM │ AIChatVM │ SettingsVM      │  │
│  └────────────────────┬──────────────────────────────┘  │
├───────────────────────┼─────────────────────────────────┤
│                  DOMAIN LAYER                           │
│  ┌────────────────────┴──────────────────────────────┐  │
│  │                  USE CASES                         │  │
│  │  GetLunarDate │ GetCanChi │ GetTietKhi │ GetGioHD │  │
│  │  ManageTasks  │ ManageNotes│ ManageReminders      │  │
│  │  AIChatUseCase│ TemplateUseCase                   │  │
│  └────────────────────┬──────────────────────────────┘  │
│  ┌────────────────────┴──────────────────────────────┐  │
│  │               REPOSITORIES (Interface)             │  │
│  └────────────────────┬──────────────────────────────┘  │
├───────────────────────┼─────────────────────────────────┤
│                   DATA LAYER                            │
│  ┌──────────────┐ ┌──────────────┐ ┌─────────────────┐ │
│  │  Local DB    │ │  Remote API  │ │  AI Service     │ │
│  │  (Room)      │ │  (Retrofit)  │ │  (OpenAI/Gemini)│ │
│  └──────────────┘ └──────────────┘ └─────────────────┘ │
│  ┌──────────────┐ ┌──────────────┐ ┌─────────────────┐ │
│  │  DataStore   │ │  WorkManager │ │  Firebase       │ │
│  │  (Prefs)     │ │  (Scheduler) │ │  (Auth/Push)    │ │
│  └──────────────┘ └──────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 4.2. Công nghệ đề xuất (Tech Stack)

| Layer | Công nghệ | Lý do chọn |
|---|---|---|
| **Language** | Kotlin | Standard cho Android, Coroutines support |
| **UI Framework** | Jetpack Compose | Modern declarative UI, tối ưu animation |
| **Architecture** | MVVM + Clean Architecture | Tách biệt concern, testable |
| **DI** | Hilt (Dagger) | Google recommend, Compose integration |
| **Local DB** | Room | Offline-first, SQLite wrapper |
| **Preferences** | DataStore | Thay thế SharedPreferences |
| **Networking** | Retrofit + OkHttp | Standard, interceptors |
| **Serialization** | Kotlinx Serialization | Native Kotlin, nhanh hơn Gson |
| **Image** | Coil | Kotlin-first, Compose native |
| **Navigation** | Compose Navigation | Single Activity architecture |
| **Async** | Kotlin Coroutines + Flow | Reactive streams, lifecycle-aware |
| **Background** | WorkManager | Reliable scheduling cho nhắc nhở |
| **AI** | Google Gemini API / OpenAI | LLM cho trợ lý AI |
| **Auth** | Firebase Authentication | Google Sign-in, anonymous |
| **Push** | Firebase Cloud Messaging | Thông báo push |
| **Analytics** | Firebase Analytics + Crashlytics | Tracking & crash report |
| **Testing** | JUnit5 + Mockk + Turbine | Unit & integration test |
| **UI Test** | Compose Testing | UI automation |

### 4.3. Cấu trúc Package

```
com.lichso.app/
├── di/                          # Hilt modules
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── AIModule.kt
├── data/
│   ├── local/
│   │   ├── db/
│   │   │   ├── LichSoDatabase.kt
│   │   │   ├── dao/
│   │   │   │   ├── TaskDao.kt
│   │   │   │   ├── NoteDao.kt
│   │   │   │   ├── ReminderDao.kt
│   │   │   │   ├── EventDao.kt
│   │   │   │   └── ChatHistoryDao.kt
│   │   │   └── entity/
│   │   │       ├── TaskEntity.kt
│   │   │       ├── NoteEntity.kt
│   │   │       ├── ReminderEntity.kt
│   │   │       └── ChatMessageEntity.kt
│   │   └── datastore/
│   │       └── UserPreferences.kt
│   ├── remote/
│   │   ├── api/
│   │   │   ├── AIService.kt
│   │   │   └── SyncService.kt
│   │   └── dto/
│   │       ├── AIRequestDto.kt
│   │       └── AIResponseDto.kt
│   └── repository/
│       ├── CalendarRepositoryImpl.kt
│       ├── TaskRepositoryImpl.kt
│       ├── NoteRepositoryImpl.kt
│       ├── ReminderRepositoryImpl.kt
│       ├── AIRepositoryImpl.kt
│       └── SettingsRepositoryImpl.kt
├── domain/
│   ├── model/
│   │   ├── LunarDate.kt
│   │   ├── CanChi.kt
│   │   ├── TietKhi.kt
│   │   ├── GioHoangDao.kt
│   │   ├── DayInfo.kt
│   │   ├── Task.kt
│   │   ├── Note.kt
│   │   ├── Reminder.kt
│   │   ├── ChatMessage.kt
│   │   └── AITemplate.kt
│   ├── repository/
│   │   ├── CalendarRepository.kt
│   │   ├── TaskRepository.kt
│   │   ├── NoteRepository.kt
│   │   ├── ReminderRepository.kt
│   │   └── AIRepository.kt
│   └── usecase/
│       ├── calendar/
│       │   ├── GetLunarDateUseCase.kt
│       │   ├── GetCanChiUseCase.kt
│       │   ├── GetTietKhiUseCase.kt
│       │   ├── GetGioHoangDaoUseCase.kt
│       │   ├── GetDayActivitiesUseCase.kt
│       │   └── GetMonthCalendarUseCase.kt
│       ├── task/
│       │   ├── GetTasksUseCase.kt
│       │   ├── CreateTaskUseCase.kt
│       │   ├── UpdateTaskUseCase.kt
│       │   └── DeleteTaskUseCase.kt
│       ├── note/
│       │   ├── GetNotesUseCase.kt
│       │   ├── CreateNoteUseCase.kt
│       │   └── DeleteNoteUseCase.kt
│       ├── reminder/
│       │   ├── GetRemindersUseCase.kt
│       │   ├── CreateReminderUseCase.kt
│       │   ├── ToggleReminderUseCase.kt
│       │   └── DeleteReminderUseCase.kt
│       └── ai/
│           ├── SendMessageUseCase.kt
│           ├── GetChatHistoryUseCase.kt
│           ├── GetTemplatesUseCase.kt
│           └── ApplyTemplateUseCase.kt
├── ui/
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   ├── Typography.kt
│   │   └── Shape.kt
│   ├── components/
│   │   ├── BottomNavBar.kt
│   │   ├── CalendarGrid.kt
│   │   ├── DayCell.kt
│   │   ├── LunarHeroCard.kt
│   │   ├── TietKhiBar.kt
│   │   ├── ActivityGrid.kt
│   │   ├── EventCard.kt
│   │   ├── TaskItem.kt
│   │   ├── NoteCard.kt
│   │   ├── ReminderItem.kt
│   │   ├── ChatBubble.kt
│   │   ├── AITemplateCard.kt
│   │   ├── FABRobot.kt
│   │   └── ToggleSwitch.kt
│   ├── screen/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   └── HomeViewModel.kt
│   │   ├── tasks/
│   │   │   ├── TasksScreen.kt
│   │   │   ├── TasksViewModel.kt
│   │   │   └── tabs/
│   │   │       ├── TaskListTab.kt
│   │   │       ├── NotesTab.kt
│   │   │       └── RemindersTab.kt
│   │   ├── ai/
│   │   │   ├── AIChatScreen.kt
│   │   │   └── AIChatViewModel.kt
│   │   ├── templates/
│   │   │   ├── TemplatesScreen.kt
│   │   │   └── TemplatesViewModel.kt
│   │   └── settings/
│   │       ├── SettingsScreen.kt
│   │       └── SettingsViewModel.kt
│   └── navigation/
│       ├── NavGraph.kt
│       └── Screen.kt
├── worker/
│   ├── DailyNotificationWorker.kt
│   ├── ReminderWorker.kt
│   └── LunarEventWorker.kt
├── util/
│   ├── LunarCalendarUtil.kt
│   ├── CanChiCalculator.kt
│   ├── TietKhiCalculator.kt
│   ├── GioHoangDaoCalculator.kt
│   ├── DateFormatter.kt
│   └── MoonPhaseCalculator.kt
└── LichSoApp.kt                 # Application class
```

---

## 5. CẤU TRÚC MÀN HÌNH & NAVIGATION

### 5.1. Sơ đồ Navigation

```
                    ┌──────────────┐
                    │  Splash /    │
                    │  Onboarding  │
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
            ┌───────┤  Main Shell  ├────────┐
            │       │ (Bottom Nav) │        │
            │       └──────┬───────┘        │
            │              │                │
     ┌──────▼──┐   ┌──────▼──┐   ┌────────▼────────┐
     │  HOME   │   │  TASKS  │   │    AI CHAT      │
     │ (Lịch)  │   │(Công    │   │  (Trợ lý AI)    │
     │         │   │ việc)   │   │                  │
     └────┬────┘   └────┬────┘   └────────┬────────┘
          │              │                 │
     ┌────▼────┐   ┌────▼────┐   ┌────────▼────────┐
     │Day      │   │Create   │   │   TEMPLATES     │
     │Detail   │   │Task/    │   │  (Template AI)  │
     │(future) │   │Note     │   │                 │
     └─────────┘   └─────────┘   └─────────────────┘
                                          │
                                  ┌───────▼────────┐
                                  │   SETTINGS     │
                                  │  (Cài đặt)     │
                                  └────────────────┘
```

### 5.2. Bottom Navigation Bar

| # | Tab | Icon | Page ID | Mô tả |
|---|---|---|---|---|
| 1 | **Lịch** | Calendar icon | `home` | Màn hình chính — Lịch Vạn Niên |
| 2 | **Công việc** | Checkbox icon | `tasks` | Quản lý task, ghi chú, nhắc nhở |
| 3 | **AI Chat** | Robot icon | `ai` | Trợ lý AI chat |
| 4 | **Template** | Document icon | `templates` | Kho template AI |

### 5.3. Floating Action Buttons

| FAB | Vị trí | Hành vi |
|---|---|---|
| **FAB Robot** 🤖 | Dưới phải (trên tất cả pages trừ AI) | Mở AI Chat, có animation pulse |
| **FAB Add** ➕ | Dưới phải (chỉ trên Tasks page) | Tạo task/ghi chú/nhắc nhở mới |

---

## 6. CHI TIẾT TÍNH NĂNG

### 6.1. MÀN HÌNH HOME — Lịch Vạn Niên

#### 6.1.1. Hero Card (Thẻ ngày)

Hiển thị thông tin tổng quan ngày hiện tại:

| Thành phần | Mô tả | Ví dụ |
|---|---|---|
| **Số ngày lớn** | Font Noto Serif, 58px, màu gold | `18` |
| **Thứ trong tuần** | Label uppercase, màu teal | `THỨ TƯ` |
| **Tháng · Năm** (dương) | Text mờ | `Tháng 3 · 2026` |
| **Ngày âm lịch** | Badge viền gold | `19 tháng 2 · Bính Ngọ` |
| **Pha mặt trăng** | Icon góc phải trên | 🌙 (waning crescent, etc.) |
| **Thẻ Can Chi** | Chips row | `Năm Bính Ngọ` · `Tháng Canh Dần` · `Ngày Nhâm Tuất` · `Giờ HĐ: Tý–Sửu` |
| **Chip ngày xấu** | Highlight đỏ nếu ngày đặc biệt | Style `.ccp.hi` |

#### 6.1.2. Thanh Tiết Khí

- Hiển thị tiết khí hiện tại/sắp tới
- Có đếm ngược: **"Xuân Phân — 20/3 (còn 2 ngày)"**
- Tap → xem chi tiết 24 tiết khí trong năm

#### 6.1.3. Calendar Grid (Lưới lịch tháng)

| Thuộc tính | Chi tiết |
|---|---|
| **Layout** | Grid 7 cột × 6 hàng |
| **Header** | CN(đỏ) · T2 · T3 · T4 · T5 · T6 · T7(teal) |
| **Ô ngày** | Số dương (14px) + số âm nhỏ (9px) |
| **Ngày hôm nay** | Background gold mờ + chấm gold dưới |
| **Chủ nhật** | Số dương màu đỏ |
| **Thứ 7** | Số dương màu teal |
| **Ngày khác tháng** | Opacity 28% |
| **Ngày có sự kiện** | Chấm teal góc phải trên |
| **Ngày lễ** | Số dương đỏ (override) |
| **Navigation** | Nút ‹ › chuyển tháng |

#### 6.1.4. Thông tin ngày (Activity Grid)

Grid 2×2 hiển thị 4 card thông tin:

| Card | Nội dung | Màu accent |
|---|---|---|
| **Nên làm** ✅ | Danh sách việc tốt nên làm trong ngày | Teal |
| **Không nên** ❌ | Danh sách việc nên tránh | Đỏ |
| **Giờ hoàng đạo** ⭐ | Các giờ tốt (Tý, Sửu, Mão, ...) | Gold |
| **Hướng tốt** 🧭 | Thần tài, Quý nhân, Hung thần | Gold + Đỏ |

#### 6.1.5. Sự kiện sắp tới (Event List)

Danh sách sự kiện gần nhất, mỗi event bao gồm:
- **Dot màu** (phân loại)
- **Tiêu đề sự kiện**
- **Thời gian** (relative: "Hôm nay", "Ngày mai", ngày cụ thể)
- **Tag phân loại**: `Công việc` (gold) | `Âm lịch` (teal) | `Tiết khí` (đỏ)

---

### 6.2. MÀN HÌNH TASKS — Ghi chú & Việc làm

#### 6.2.1. Quick Stats (Thống kê nhanh)

Grid 3 cột hiển thị:

| Stat | Ví dụ | Màu |
|---|---|---|
| Việc hôm nay | `7` | Teal |
| Nhắc nhở | `3` | Gold |
| Ghi chú | `12` | Text mờ |

#### 6.2.2. Tab Navigation

3 tabs chuyển đổi nội dung:

##### Tab 1: **Việc làm** (Tasks)

- Phân nhóm theo thời gian: `Hôm nay`, `Tuần này`, `Tháng này`
- Mỗi task item bao gồm:
  - **Checkbox** (circle → done with animation)
  - **Tiêu đề** (line-through khi done)
  - **Metadata**: Ngày/giờ + Mức ưu tiên
  - **Priority badges**: `Cao` (đỏ) | `Vừa` (gold) | `Thấp` (teal)

##### Tab 2: **Ghi chú** (Notes)

- Grid 2 cột, card màu sắc nổi bật
- Mỗi note card:
  - **Background màu** (gold, teal, orange, purple, green, red)
  - **Tiêu đề** (đậm, chữ đen trên nền màu)
  - **Preview** nội dung (2 dòng)
  - **Timestamp** (relative)

##### Tab 3: **Nhắc nhở** (Reminders)

- Phân nhóm: `Hôm nay`, `Ngày mai & tuần này`
- Mỗi reminder:
  - **Thời gian lớn** (font Serif, 18px) + AM/PM
  - **Divider dọc**
  - **Tiêu đề + lịch lặp** (Hàng tuần, Hàng tháng âm, Một lần)
  - **Toggle on/off** (teal = on, surface = off)

---

### 6.3. MÀN HÌNH AI CHAT — Trợ lý Lịch Số AI

#### 6.3.1. Header

- **Avatar AI**: Gradient teal, icon robot, badge xanh "online"
- **Tên**: "Trợ lý Lịch Số AI"
- **Trạng thái**: "Đang hoạt động"
- **Actions**: Xem lịch sử chat | Reset conversation

#### 6.3.2. Chat Messages

| Loại | Alignment | Style |
|---|---|---|
| **AI message** | Trái | Background dark, border subtle, avatar robot teal |
| **User message** | Phải | Background gradient teal, text trắng, avatar gold |
| **Timestamp** | Trên mỗi bubble | Font 10px, màu mờ |

#### 6.3.3. AI Interactive Cards

AI có thể trả về các card tương tác:

- **Action Cards**: Chọn tính năng (tra ngày tốt, tạo task, tư vấn can chi)
- **Result Cards**: Hiển thị kết quả (nhắc nhở đã tạo, ngày tốt tìm được)
- **Styled content**: Bold, màu teal (tốt) / đỏ (xấu) trong phản hồi

#### 6.3.4. Quick Topics (Chips)

Thanh cuộn ngang phía dưới chat, gợi ý nhanh:

| Chip | Hành vi |
|---|---|
| 📅 Ngày tốt ký kết | Hỏi AI về ngày tốt cho giao dịch |
| ✅ Tạo task nhanh | Bắt đầu flow tạo task qua AI |
| 🔔 Đặt nhắc nhở | Tạo reminder bằng ngôn ngữ tự nhiên |
| ☀️ Tiết khí hôm nay | Xem thông tin tiết khí hiện tại |
| ⭐ Can chi ngày | Phân tích can chi chi tiết |
| 📄 Tóm tắt tuần | Tổng hợp task, sự kiện trong tuần |

#### 6.3.5. Input Area

- **Attach button**: Đính kèm file/ảnh
- **Text input**: Auto-expand textarea, max 100px height
- **Send button**: Gradient teal, icon paper plane

---

### 6.4. MÀN HÌNH TEMPLATES — Template AI

#### 6.4.1. Tab Filters

`Tất cả` | `Lịch` | `Công việc` | `Cá nhân`

#### 6.4.2. Template Cards

Mỗi template bao gồm:

| Thành phần | Chi tiết |
|---|---|
| **Icon** | Themed icon trong ô màu |
| **Tên template** | Bold, 13px |
| **Phân loại** | Text mờ |
| **Mô tả/Prompt** | Italic, border-left, có placeholder `[THÁNG]`, `[SỰ KIỆN]` |
| **Tags** | Chips nhỏ phân loại |

#### 6.4.3. Danh sách Template mặc định

| # | Tên | Loại | Prompt mẫu |
|---|---|---|---|
| 1 | Tra ngày tốt sự kiện | Lịch | "Tìm 3 ngày tốt nhất trong tháng [THÁNG] để [SỰ KIỆN]..." |
| 2 | Lên kế hoạch tuần | Công việc | "Tạo danh sách task tuần này dựa trên mục tiêu [MỤC TIÊU]..." |
| 3 | Nhắc nhở theo âm lịch | Nhắc nhở | "Tạo nhắc nhở hàng tháng vào ngày [NGÀY] âm lịch..." |
| 4 | Phân tích can chi ngày | Tử vi | "Phân tích chi tiết ngày [NGÀY/THÁNG/NĂM]: can chi, giờ HD..." |
| 5 | Tóm tắt ghi chú | Ghi chú | "Tóm tắt và tổ chức lại ghi chú thành đầu việc, ý tưởng..." |
| 6 | Lịch sự kiện gia đình | Cá nhân | "Tìm ngày tốt trong tháng [THÁNG] để tổ chức [SỰ KIỆN GĐ]..." |

---

### 6.5. MÀN HÌNH SETTINGS — Cài đặt

#### 6.5.1. Profile Section

- **Avatar** (gradient gold, user icon)
- **Tên người dùng**
- **Gói dịch vụ** + link nâng cấp Pro
- **Nút chỉnh sửa**

#### 6.5.2. Nhóm cài đặt

**Giao diện & Hiển thị:**

| Setting | Loại | Mặc định |
|---|---|---|
| Chủ đề tối | Toggle | ✅ On |
| Cỡ chữ | Selector | Vừa |
| Ngôn ngữ | Selector | Tiếng Việt |

**Lịch:**

| Setting | Loại | Mặc định |
|---|---|---|
| Hiển thị âm lịch | Toggle | ✅ On |
| Đánh dấu ngày lễ | Toggle | ✅ On |
| Hiển thị tiết khí | Toggle | ✅ On |
| Giờ hoàng đạo | Toggle | ❌ Off |

**Thông báo:**

| Setting | Loại | Mặc định |
|---|---|---|
| Cho phép thông báo | Toggle | ✅ On |
| Thông báo ngày mới | Toggle | ✅ On |
| Nhắc ngày lễ âm lịch | Toggle | ✅ On |

**Trợ lý AI:**

| Setting | Loại | Mặc định |
|---|---|---|
| Gói AI | Info + Nâng cấp | Miễn phí · 50 tin/ngày |
| Quản lý Template | Navigation | 6 template |

**Thông tin ứng dụng:**

| Setting | Loại |
|---|---|
| Phiên bản | Info — v1.0 |
| Đánh giá ứng dụng | Navigation → Google Play |
| Đăng xuất | Action (đỏ) |

---

## 7. HỆ THỐNG DESIGN SYSTEM

### 7.1. Bảng màu (Color Palette)

```
BACKGROUNDS
──────────────────────────────────────────
--bg:       #0f0e0c     ■  Nền chính (gần đen ấm)
--bg2:      #181610     ■  Nền card
--bg3:      #211f1a     ■  Nền hover/inactive
--bg4:      #2a2720     ■  Nền phụ

SURFACES
──────────────────────────────────────────
--surface:  #2e2b23     ■  Surface chính
--surface2: #363228     ■  Surface nổi

ACCENT - GOLD (Primary)
──────────────────────────────────────────
--gold:     #e8c84a     ■  Gold chính
--gold2:    #f5d96e     ■  Gold sáng (highlight)
--gold-dim: rgba(232,200,74,.18)  ■  Gold mờ (background)

ACCENT - TEAL (Secondary)
──────────────────────────────────────────
--teal:     #4abeaa     ■  Teal chính
--teal2:    #62d4c0     ■  Teal sáng
--teal-dim: rgba(74,190,170,.15)  ■  Teal mờ

ACCENT - RED (Warning/Negative)
──────────────────────────────────────────
--red:      #d94f3b     ■  Đỏ chính
--red2:     #e8614e     ■  Đỏ sáng

TEXT
──────────────────────────────────────────
--text:     #f0e8d0     ■  Text chính (kem ấm)
--text2:    #b8aa88     ■  Text phụ
--text3:    #7a6e52     ■  Text mờ (label)
--text4:    #4a4435     ■  Text rất mờ (hint)

BORDER
──────────────────────────────────────────
--border:   rgba(255,220,100,.12)  ■  Viền mờ gold
```

### 7.2. Typography

| Role | Font | Weight | Size | Color |
|---|---|---|---|---|
| **Page title** | Noto Serif | 400 | 22px | `--gold2` |
| **Day number (hero)** | Noto Serif | 700 | 58px | `--gold2` |
| **Section label** | Be Vietnam Pro | 700 | 10.5px | `--text3` (uppercase) |
| **Card header** | Be Vietnam Pro | 700 | 10px | Contextual |
| **Body text** | Be Vietnam Pro | 400 | 13px | `--text` |
| **Body secondary** | Be Vietnam Pro | 400 | 12px | `--text2` |
| **Small text** | Be Vietnam Pro | 400 | 11px | `--text3` |
| **Badge** | Noto Serif | 400 | 11px | `--gold` |
| **Calendar solar** | Be Vietnam Pro | 500 | 14px | `--text` |
| **Calendar lunar** | Be Vietnam Pro | 400 | 9px | `--text3` |
| **Stat number** | Noto Serif | 700 | 22px | Contextual |

**Font Families:**
- **Be Vietnam Pro** (300, 400, 500, 600, 700): UI chính, sans-serif, tối ưu cho tiếng Việt
- **Noto Serif** (400, 700, 400 italic): Tiêu đề, số liệu, tạo cảm giác truyền thống

### 7.3. Spacing & Sizing

| Token | Giá trị | Sử dụng |
|---|---|---|
| `--nav-h` | 72px | Chiều cao bottom navigation |
| `--r` | 16px | Border radius lớn (card) |
| `--rs` | 10px | Border radius nhỏ (item) |
| Page padding | 20px (horizontal) | Lề nội dung chính |
| Card padding | 11–16px | Padding nội dung card |
| Grid gap | 8px | Khoảng cách giữa card |
| Item gap | 6–7px | Khoảng cách giữa list item |

### 7.4. Animation & Transitions

| Element | Property | Duration | Easing |
|---|---|---|---|
| Page transition | opacity + translateX | 320ms | `cubic-bezier(.4,0,.2,1)` |
| Hover state | background, color | 150–200ms | ease |
| Toggle switch | background, position | 200ms | ease |
| FAB Robot | box-shadow pulse | 3s infinite | ease |
| FAB hover | transform scale | 200ms | ease |
| Checkbox done | opacity | 200ms | ease |
| Nav active icon | transform scale(1.1) | 200ms | ease |

### 7.5. Iconography

- **Hệ thống icon:** SVG inline, stroke-based (Feather Icons style)
- **Stroke width:** 1.5–2.2 tùy kích thước
- **Kích thước phổ biến:** 13–26px
- **Style:** `stroke: currentColor`, `fill: none`, `stroke-linecap: round`, `stroke-linejoin: round`

---

## 8. TRỢ LÝ AI — KIẾN TRÚC & TÍNH NĂNG

### 8.1. Sơ đồ luồng AI

```
┌──────────────────────────────────────────────────────┐
│                   USER INPUT                          │
│  Text / Quick Topic / Template                       │
└────────────────────┬─────────────────────────────────┘
                     │
              ┌──────▼──────┐
              │  INTENT     │
              │  DETECTION  │
              │  (Local)    │
              └──────┬──────┘
                     │
         ┌───────────┼───────────┐
         │           │           │
    ┌────▼────┐ ┌────▼────┐ ┌───▼──────┐
    │CALENDAR │ │  TASK   │ │ GENERAL  │
    │ QUERY   │ │ ACTION  │ │ CHAT     │
    └────┬────┘ └────┬────┘ └───┬──────┘
         │           │          │
    ┌────▼────┐ ┌────▼────┐ ┌──▼───────┐
    │ Local   │ │ Local   │ │ LLM API  │
    │ Calc    │ │ DB Ops  │ │ (Gemini/ │
    │ Engine  │ │         │ │  OpenAI) │
    └────┬────┘ └────┬────┘ └──┬───────┘
         │           │          │
         └───────────┼──────────┘
                     │
              ┌──────▼──────┐
              │  RESPONSE   │
              │  FORMATTER  │
              └──────┬──────┘
                     │
              ┌──────▼──────┐
              │  CHAT UI    │
              │  (Render)   │
              └─────────────┘
```

### 8.2. Khả năng AI

| Khả năng | Xử lý | Cần Internet |
|---|---|---|
| Tra cứu ngày tốt/xấu | Local calculation | ❌ |
| Giờ hoàng đạo | Local calculation | ❌ |
| Can chi chi tiết | Local calculation | ❌ |
| Tiết khí | Local calculation | ❌ |
| Tạo task/reminder | Local DB + NLP parsing | ❌ (basic) / ✅ (complex) |
| Tư vấn phong thủy | LLM API | ✅ |
| Tóm tắt ghi chú | LLM API | ✅ |
| Lên kế hoạch tuần | LLM API + Local calendar | ✅ |
| Trả lời câu hỏi chung | LLM API | ✅ |

### 8.3. Template Engine

```
Template Flow:
                                    
User chọn Template → Fill placeholders → Generate prompt → Send to AI → Render response
       │                    │                    │               │              │
  ┌────▼────┐        ┌─────▼─────┐       ┌─────▼─────┐  ┌─────▼─────┐ ┌─────▼─────┐
  │Template │        │ Dynamic   │       │ Compiled  │  │ AI/Local  │ │ Rich      │
  │ Card    │───────>│ Form UI   │──────>│ Prompt    │─>│ Process   │>│ Response  │
  │ Select  │        │ [MONTH]   │       │ String    │  │           │ │ Cards     │
  └─────────┘        │ [EVENT]   │       └───────────┘  └───────────┘ └───────────┘
                     └───────────┘
```

### 8.4. Giới hạn & Gói dịch vụ

| Tính năng | Free | Pro |
|---|---|---|
| Tin nhắn AI/ngày | 50 | Không giới hạn |
| Template mặc định | 6 | 6 + tùy chỉnh |
| Template tùy chỉnh | ❌ | ✅ Không giới hạn |
| Tra cứu lịch offline | ✅ | ✅ |
| Lịch sử chat | 7 ngày | Không giới hạn |
| Mô hình AI | Standard | Premium (GPT-4/Gemini Pro) |

---

## 9. MÔ HÌNH DỮ LIỆU (DATA MODELS)

### 9.1. Entity Relationship Diagram

```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐
│    User      │     │    Task      │     │    Note      │
├─────────────┤     ├──────────────┤     ├──────────────┤
│ id: String   │──┐  │ id: Long     │     │ id: Long     │
│ name: String │  │  │ title: String│     │ title: String│
│ email: String│  │  │ desc: String?│     │ content: Str │
│ plan: Enum   │  ├─>│ userId: Str  │     │ color: String│
│ createdAt    │  │  │ dueDate: Long│     │ userId: Str  │
└─────────────┘  │  │ priority: Enum│  ┌─>│ createdAt    │
                  │  │ isCompleted  │  │  │ updatedAt    │
                  │  │ createdAt    │  │  └──────────────┘
                  │  └──────────────┘  │
                  │                     │  ┌──────────────┐
                  │  ┌──────────────┐  │  │  ChatMessage │
                  │  │   Reminder   │  │  ├──────────────┤
                  │  ├──────────────┤  │  │ id: Long     │
                  │  │ id: Long     │  │  │ role: Enum   │
                  ├─>│ title: String│  │  │ content: Str │
                  │  │ time: Long   │  │  │ userId: Str  │
                  │  │ repeat: Enum │  │  │ timestamp    │
                  │  │ isLunar: Bool│  │  │ cards: Json? │
                  │  │ isEnabled    │  │  └──────────────┘
                  │  │ userId: Str  │  │
                  │  └──────────────┘  │  ┌──────────────┐
                  │                     │  │  AITemplate  │
                  │  ┌──────────────┐  │  ├──────────────┤
                  │  │    Event     │  │  │ id: Long     │
                  │  ├──────────────┤  │  │ name: String │
                  └─>│ id: Long     │  │  │ category: Str│
                     │ title: String│  │  │ prompt: Str  │
                     │ date: Long   │  │  │ icon: String │
                     │ type: Enum   │  │  │ color: String│
                     │ color: String│  │  │ tags: List   │
                     │ userId: Str  │  │  │ isCustom:Bool│
                     └──────────────┘  │  └──────────────┘
                                       │
                                       │  ┌──────────────┐
                                       │  │ UserSettings │
                                       │  ├──────────────┤
                                       └─>│ darkMode:Bool│
                                          │ fontSize:Enum│
                                          │ language:Str │
                                          │ showLunar    │
                                          │ showHolidays │
                                          │ showTietKhi  │
                                          │ showGioHD    │
                                          │ notifEnabled │
                                          │ dailyNotif   │
                                          │ lunarNotif   │
                                          └──────────────┘
```

### 9.2. Kotlin Data Classes

```kotlin
// === ENUMS ===
enum class UserPlan { FREE, PRO }
enum class Priority { HIGH, MEDIUM, LOW }
enum class RepeatType { ONCE, DAILY, WEEKLY, MONTHLY_SOLAR, MONTHLY_LUNAR, YEARLY }
enum class MessageRole { USER, AI }
enum class EventType { WORK, LUNAR, TIET_KHI, HOLIDAY, PERSONAL }
enum class FontSize { SMALL, MEDIUM, LARGE }

// === DOMAIN MODELS ===
data class LunarDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val isLeapMonth: Boolean,
    val monthName: String,      // "tháng 2"
    val dayCanChi: String,      // "Nhâm Tuất"
    val monthCanChi: String,    // "Canh Dần"
    val yearCanChi: String,     // "Bính Ngọ"
)

data class DayInfo(
    val solarDate: LocalDate,
    val lunarDate: LunarDate,
    val tietKhi: TietKhi?,
    val gioHoangDao: List<GioHoangDao>,
    val nenLam: List<String>,
    val khongNen: List<String>,
    val huongTot: Map<String, String>,  // "Thần tài" -> "Đông-Nam"
    val moonPhase: MoonPhase,
)

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dueDate: LocalDateTime?,
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val color: String,          // hex color
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

data class Reminder(
    val id: Long = 0,
    val title: String,
    val time: LocalDateTime,
    val repeatType: RepeatType = RepeatType.ONCE,
    val isLunarBased: Boolean = false,
    val isEnabled: Boolean = true,
)
```

---

## 10. API & BACKEND SERVICES

### 10.1. AI Chat API

```
POST /api/v1/ai/chat
Authorization: Bearer {token}

Request:
{
  "message": "Tuần này ngày nào tốt để ký hợp đồng?",
  "context": {
    "currentDate": "2026-03-18",
    "lunarDate": "02-19-BinhNgo",
    "timezone": "Asia/Ho_Chi_Minh"
  },
  "conversationId": "conv_abc123",
  "templateId": null
}

Response:
{
  "id": "msg_xyz789",
  "role": "ai",
  "content": "Theo can chi tuần 18–22/3...",
  "cards": [
    {
      "type": "reminder_created",
      "data": {
        "title": "Ký hợp đồng",
        "date": "2026-03-20",
        "time": "09:00"
      }
    }
  ],
  "timestamp": "2026-03-18T09:42:00+07:00"
}
```

### 10.2. Sync API (Optional)

```
POST /api/v1/sync
Authorization: Bearer {token}

Request:
{
  "lastSyncAt": "2026-03-17T23:00:00Z",
  "changes": {
    "tasks": [...],
    "notes": [...],
    "reminders": [...]
  }
}
```

### 10.3. Xác thực (Authentication)

- **Firebase Authentication** với các provider:
  - Google Sign-in
  - Anonymous (cho trải nghiệm nhanh)
  - Email/Password (optional)
- Token JWT cho API calls

---

## 11. THÔNG BÁO & NHẮC NHỞ

### 11.1. Loại thông báo

| Loại | Trigger | Channel | Priority |
|---|---|---|---|
| **Thông báo ngày mới** | Hàng ngày 6:00 sáng | `daily_summary` | Default |
| **Nhắc nhở task** | Trước thời hạn task | `task_reminder` | High |
| **Nhắc nhở tùy chỉnh** | Theo thời gian đặt | `custom_reminder` | High |
| **Ngày lễ âm lịch** | Trước 1 ngày (mặc định) | `lunar_events` | Default |
| **Tiết khí** | Ngày bắt đầu tiết khí mới | `tiet_khi` | Low |

### 11.2. Implementation

```
WorkManager Schedule:
┌──────────────────────────────────────────────┐
│ PeriodicWorkRequest (24h)                    │
│ ├── DailyNotificationWorker                  │
│ │   └── Tạo tóm tắt ngày: can chi,          │
│ │       task hôm nay, sự kiện âm lịch       │
│ └── LunarEventWorker                         │
│     └── Kiểm tra ngày lễ âm lịch sắp tới    │
│                                              │
│ OneTimeWorkRequest (per reminder)            │
│ └── ReminderWorker                           │
│     └── Trigger notification tại thời điểm   │
│         đã đặt, hỗ trợ lặp lại             │
└──────────────────────────────────────────────┘
```

### 11.3. Notification Content mẫu

**Thông báo ngày mới:**
```
📅 Thứ Tư 18/03 · Nhâm Tuất
Âm lịch: 19 tháng 2 Bính Ngọ
✅ 7 việc hôm nay · ⭐ Giờ tốt: Tý, Sửu, Mão
Nên: Giao dịch, ký kết | Tránh: An táng
```

---

## 12. MÔ HÌNH KINH DOANH (MONETIZATION)

### 12.1. Gói dịch vụ

| Tính năng | Free | Pro (Monthly) | Pro (Yearly) |
|---|---|---|---|
| **Giá** | 0₫ | 49,000₫/tháng | 399,000₫/năm |
| Lịch vạn niên đầy đủ | ✅ | ✅ | ✅ |
| Can chi, tiết khí, giờ HD | ✅ | ✅ | ✅ |
| Task, Note, Reminder | ✅ | ✅ | ✅ |
| AI Chat | 50 tin/ngày | Không giới hạn | Không giới hạn |
| AI Model | Standard | Premium | Premium |
| Template tùy chỉnh | ❌ | ✅ | ✅ |
| Lịch sử chat | 7 ngày | Không giới hạn | Không giới hạn |
| Widget (tương lai) | Basic | Premium | Premium |
| Quảng cáo | Có (không xâm lấn) | Không | Không |
| Sync đa thiết bị | ❌ | ✅ | ✅ |

### 12.2. Nguồn thu dự kiến

```
Revenue Mix (ước tính khi đạt 500K users):
├── Subscription Pro:  65%   (~₫120M/tháng)
├── Quảng cáo (AdMob): 25%   (~₫45M/tháng)
└── Tips / Donate:     10%   (~₫18M/tháng)
```

---

## 13. YÊU CẦU PHI CHỨC NĂNG

### 13.1. Hiệu năng (Performance)

| Metric | Target |
|---|---|
| Cold start | < 1.5s |
| Warm start | < 500ms |
| Calendar scroll | 60 FPS |
| Memory usage | < 150MB |
| APK size | < 25MB |
| Battery drain | < 2%/giờ active use |
| AI response (first token) | < 3s |

### 13.2. Khả năng tương thích

| Yêu cầu | Chi tiết |
|---|---|
| Android version | API 26+ (Android 8.0 Oreo) |
| Kích thước màn hình | 5" — 7" (phone), tablet (adaptive layout) |
| Orientation | Portrait only (v1), Landscape (v2) |
| Dark/Light mode | Dark (default), Light (Settings toggle) |
| Accessibility | TalkBack support, minimum touch target 48dp |
| Ngôn ngữ | Tiếng Việt (primary), English (planned) |

### 13.3. Bảo mật

| Vấn đề | Giải pháp |
|---|---|
| API keys | Stored in BuildConfig, obfuscated |
| User data | Encrypted with EncryptedSharedPreferences |
| Network | HTTPS only, certificate pinning |
| Auth tokens | Stored in Android Keystore |
| Local DB | Room + SQLCipher (optional) |
| ProGuard | Full obfuscation cho release build |

### 13.4. Offline Support

| Tính năng | Offline | Yêu cầu sync |
|---|---|---|
| Xem lịch dương/âm | ✅ Hoàn toàn offline | Không |
| Can chi, tiết khí | ✅ Hoàn toàn offline | Không |
| Giờ hoàng đạo | ✅ Hoàn toàn offline | Không |
| Quản lý tasks | ✅ Local-first | Sync khi có mạng |
| Ghi chú | ✅ Local-first | Sync khi có mạng |
| Nhắc nhở | ✅ Local-first | Sync khi có mạng |
| AI Chat | ❌ Cần internet | — |
| AI Chat (basic queries) | ✅ Local NLP (v2) | — |

---

## 14. KẾ HOẠCH PHÁT TRIỂN (ROADMAP)

### Phase 1: MVP (8 tuần)

```
Tuần 1–2: Foundation
├── Setup project (Compose + Hilt + Room + Navigation)
├── Design System implementation (Theme, Colors, Typography)
├── Bottom Navigation shell
└── Calendar calculation engine (Lunar, CanChi, TietKhi, GioHD)

Tuần 3–4: Home Screen
├── Hero Card component
├── Calendar Grid (month view)
├── Tiết Khí bar
├── Day info grid (Nên làm, Không nên, Giờ HD, Hướng)
└── Event list

Tuần 5–6: Tasks & Notes
├── Task list (CRUD + checkbox toggle)
├── Notes grid (CRUD + colors)
├── Reminders list (CRUD + toggle + WorkManager)
├── Tab navigation
└── Quick stats

Tuần 7–8: AI Chat & Polish
├── AI Chat UI
├── AI integration (Gemini/OpenAI)
├── Template screen
├── Settings screen
├── FAB Robot
└── Testing & bug fixes
```

### Phase 2: Enhancement (4 tuần)

```
Tuần 9–10:
├── Push notifications (Firebase)
├── Daily summary notification
├── Lunar event reminders
├── Search functionality
└── Widget (basic) — hiển thị ngày + âm lịch

Tuần 11–12:
├── Pro subscription (Google Play Billing)
├── User authentication
├── Data sync (optional)
├── Performance optimization
└── Google Play Store submission
```

### Phase 3: Growth (Ongoing)

```
├── Light theme
├── Tablet adaptive layout
├── Widget premium (lịch tháng, can chi)
├── English language support
├── Tử vi chi tiết theo năm sinh
├── Chia sẻ ngày tốt (social sharing)
├── Tích hợp Google Calendar
├── Wear OS companion
└── iOS version (Kotlin Multiplatform)
```

---

## 15. PHỤ LỤC KỸ THUẬT

### 15.1. Thuật toán chuyển đổi Dương → Âm lịch

Sử dụng thuật toán Hồ Ngọc Đức (chuẩn cho lịch Việt Nam):

```
Input:  Solar date (dd/mm/yyyy)
Output: Lunar date (day, month, year, isLeap)

Các bước chính:
1. Tính Julian Day Number từ ngày dương
2. Tính New Moon gần nhất (thuật toán Jean Meeus)
3. Xác định tháng âm lịch dựa trên Winter Solstice
4. Xử lý tháng nhuận
5. Ánh xạ Can Chi (Thiên Can × Địa Chi)
```

### 15.2. Bảng Can Chi

**10 Thiên Can:** Giáp, Ất, Bính, Đinh, Mậu, Kỷ, Canh, Tân, Nhâm, Quý

**12 Địa Chi:** Tý, Sửu, Dần, Mão, Thìn, Tỵ, Ngọ, Mùi, Thân, Dậu, Tuất, Hợi

**Chu kỳ:** 60 tổ hợp (Lục Thập Hoa Giáp)

### 15.3. 24 Tiết Khí

| # | Tiết khí | Tháng dương | Kinh độ mặt trời |
|---|---|---|---|
| 1 | Lập Xuân | ~4/2 | 315° |
| 2 | Vũ Thủy | ~19/2 | 330° |
| 3 | Kinh Trập | ~6/3 | 345° |
| 4 | **Xuân Phân** | ~21/3 | 0° |
| 5 | Thanh Minh | ~5/4 | 15° |
| 6 | Cốc Vũ | ~20/4 | 30° |
| 7 | Lập Hạ | ~6/5 | 45° |
| 8 | Tiểu Mãn | ~21/5 | 60° |
| 9 | Mang Chủng | ~6/6 | 75° |
| 10 | Hạ Chí | ~21/6 | 90° |
| 11 | Tiểu Thử | ~7/7 | 105° |
| 12 | Đại Thử | ~23/7 | 120° |
| 13 | Lập Thu | ~7/8 | 135° |
| 14 | Xử Thử | ~23/8 | 150° |
| 15 | Bạch Lộ | ~8/9 | 165° |
| 16 | Thu Phân | ~23/9 | 180° |
| 17 | Hàn Lộ | ~8/10 | 195° |
| 18 | Sương Giáng | ~23/10 | 210° |
| 19 | Lập Đông | ~7/11 | 225° |
| 20 | Tiểu Tuyết | ~22/11 | 240° |
| 21 | Đại Tuyết | ~7/12 | 255° |
| 22 | Đông Chí | ~22/12 | 270° |
| 23 | Tiểu Hàn | ~6/1 | 285° |
| 24 | Đại Hàn | ~20/1 | 300° |

### 15.4. Giờ Hoàng Đạo

12 giờ trong ngày tương ứng 12 Địa Chi:

| Giờ | Thời gian | Địa Chi |
|---|---|---|
| Tý | 23:00 – 01:00 | 子 |
| Sửu | 01:00 – 03:00 | 丑 |
| Dần | 03:00 – 05:00 | 寅 |
| Mão | 05:00 – 07:00 | 卯 |
| Thìn | 07:00 – 09:00 | 辰 |
| Tỵ | 09:00 – 11:00 | 巳 |
| Ngọ | 11:00 – 13:00 | 午 |
| Mùi | 13:00 – 15:00 | 未 |
| Thân | 15:00 – 17:00 | 申 |
| Dậu | 17:00 – 19:00 | 酉 |
| Tuất | 19:00 – 21:00 | 戌 |
| Hợi | 21:00 – 23:00 | 亥 |

Giờ Hoàng Đạo được tính dựa trên **Ngày Chi** theo bảng tra Thanh Long, Minh Đường, Kim Quỹ, Thiên Đức, Ngọc Đường, Tư Mệnh.

### 15.5. Dependencies (build.gradle.kts)

```kotlin
// Core
implementation("androidx.core:core-ktx:1.13.1")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
implementation("androidx.activity:activity-compose:1.9.1")

// Compose
implementation(platform("androidx.compose:compose-bom:2024.08.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.navigation:navigation-compose:2.8.0")

// Hilt
implementation("com.google.dagger:hilt-android:2.51.1")
kapt("com.google.dagger:hilt-compiler:2.51.1")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// DataStore
implementation("androidx.datastore:datastore-preferences:1.1.1")

// Network
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

// WorkManager
implementation("androidx.work:work-runtime-ktx:2.9.1")

// Firebase
implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-messaging-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")
implementation("com.google.firebase:firebase-crashlytics-ktx")

// Google AI (Gemini)
implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

// Image
implementation("io.coil-kt:coil-compose:2.7.0")

// Testing
testImplementation("junit:junit:4.13.2")
testImplementation("io.mockk:mockk:1.13.12")
testImplementation("app.cash.turbine:turbine:1.1.0")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
```

---

## 📝 GHI CHÚ CUỐI

### Quy ước đặt tên

| Loại | Quy ước | Ví dụ |
|---|---|---|
| Package | lowercase | `com.lichso.app.ui.screen.home` |
| Class | PascalCase | `HomeViewModel`, `CalendarGrid` |
| Function | camelCase | `getMonthCalendar()`, `toggleCheck()` |
| Constant | SCREAMING_SNAKE | `MAX_AI_MESSAGES_FREE` |
| Resource | snake_case | `ic_calendar`, `str_hello` |
| Composable | PascalCase | `@Composable fun HeroCard()` |
| ViewModel | PascalCase + VM | `HomeViewModel` |
| UseCase | PascalCase + UseCase | `GetLunarDateUseCase` |

### Coding Standards

- **Kotlin style guide** theo [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- **Compose best practices** theo [Jetpack Compose guidelines](https://developer.android.com/jetpack/compose/performance)
- **Git flow**: `main` → `develop` → `feature/*` → `bugfix/*` → `release/*`
- **Commit convention**: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`
- **Code review**: Tối thiểu 1 reviewer trước khi merge

---

> **Tài liệu này là bản thiết kế sống (living document).** Cập nhật liên tục theo tiến độ phát triển.  
> **Liên hệ:** Kiến trúc sư dự án — [Cập nhật thông tin]  
> **Lần cập nhật cuối:** 18/03/2026
