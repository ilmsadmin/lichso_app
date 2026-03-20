# Lịch Số — iOS App

Phiên bản iOS của app **Lịch Số — Lịch Vạn Niên**, được xây dựng bằng **SwiftUI** với thiết kế và tính năng tương đương app Android.

## Yêu cầu

- **Xcode 15+**
- **iOS 17+**
- **Swift 5.9+**

## Cấu trúc Project

```
ios/
└── LichSo/
    ├── LichSoApp.swift              # Entry point + Tab navigation
    ├── Info.plist
    ├── Assets.xcassets/
    │
    ├── Theme/
    │   └── LichSoTheme.swift        # Design system (Dark/Light palette)
    │
    ├── Models/
    │   └── DayInfo.swift            # Domain models
    │
    ├── Domain/
    │   └── LunarCalendarEngine.swift # Âm lịch engine (Việt Nam)
    │
    ├── ViewModels/
    │   └── ViewModels.swift         # HomeVM, CalendarVM, TasksVM, ChatVM, SettingsVM
    │
    └── Views/
        ├── Components/
        │   └── SharedComponents.swift  # SectionLabel, ActivityCard, RobotFAB...
        ├── Home/
        │   └── HomeScreen.swift        # Trang chủ — Hero card, đồng hồ, thông tin ngày
        ├── Calendar/
        │   └── CalendarScreen.swift    # Lịch tháng, Calendar grid, Day detail overlay
        ├── Tasks/
        │   └── TasksScreen.swift       # Việc làm / Ghi chú / Nhắc nhở
        ├── Chat/
        │   └── ChatScreen.swift        # AI Chat — Gemini API
        ├── Templates/
        │   └── TemplatesScreen.swift   # Template phong thuỷ
        └── Settings/
            └── SettingsScreen.swift    # Cài đặt
```

## Tính năng

### 🏠 Trang Chủ
- **Hero Card** hiển thị ngày dương + âm lịch cỡ lớn theo phong cách serif sang trọng
- **Can Chi** ngày / tháng / năm dạng chip
- **Đồng hồ sống** với nhận biết Giờ Hoàng Đạo
- **Tiết khí** bar
- **Nên làm / Không nên** grid
- **Giờ Hoàng Đạo + Hướng tốt** cards
- **Điều hướng ngày** (trước/sau)

### 📅 Lịch Tháng
- Grid 7 cột với ngày dương + âm lịch nhỏ
- Highlight ngày hôm nay, Chủ nhật, ngày lễ
- **Day Detail Overlay** (bottom sheet) với thông tin đầy đủ
- Điều hướng tháng

### ✅ Ghi Chú & Việc Làm
- **3 tab**: Việc làm / Ghi chú / Nhắc nhở
- **Swipe-to-delete** từng item
- **Add sheet** thêm nhanh
- **Toggle** bật/tắt nhắc nhở
- Note màu sắc (gold, teal, orange, purple, green, red)

### 🤖 AI Chat (Lịch Số AI)
- Chat với **Google Gemini API**
- Quick topics gợi ý
- Typing indicator với robot icon
- Context ngày hôm nay tự động

### 📋 Template
- 6 category: Lịch Ngày, Cưới Hỏi, Kinh Doanh, Xin Việc, Chuyển Nhà, Xuất Hành
- Template detail sheet với checklist mẫu

### ⚙️ Cài Đặt
- Dark/Light mode toggle
- Thông báo nhắc nhở
- Hiển thị lịch âm
- Chính sách bảo mật

### 🎨 Design System
- **Dark palette**: Nền đen sang trọng, gold/teal accent (y hệt Android)
- **Light palette**: Nền kem ấm, gold/teal mềm mại
- **Robot FAB** có thể kéo thả, mở AI Chat
- Animation: robot blink, antenna bounce, head tilt

## Cài đặt AI

Mở `ViewModels/ViewModels.swift` và thay `YOUR_GEMINI_API_KEY` bằng API key thực:

```swift
private let apiKey = "YOUR_GEMINI_API_KEY"
```

Lấy API key miễn phí tại: https://aistudio.google.com/

## Mở Project

```bash
open ios/LichSo.xcodeproj
```

Sau đó chọn Simulator iPhone và nhấn **Run (⌘R)**.
