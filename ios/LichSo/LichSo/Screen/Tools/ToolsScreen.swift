import SwiftUI

struct ToolItem: Identifiable {
    let id = UUID()
    let action: String
    let title: String
    let subtitle: String
    let icon: String
    let colors: [Color]
    var badge: String? = nil
}

struct ToolSection: Identifiable {
    let id = UUID()
    let bannerType: String
    let title: String
    let subtitle: String
    let accent: Color
    let items: [ToolItem]
}

struct ToolsScreen: View {
    var onMenuClick: () -> Void = {}
    var onBannerAction: (String) -> Void = { _ in }
    
    @State private var activeRoute: String? = nil
    @State private var sectionBanners: [Banner] = []
    
    // We can use NavigationStack triggers or similar
    let sections = [
        ToolSection(
            bannerType: "tools_calendar",
            title: "Lịch & Ngày tốt",
            subtitle: "Chọn ngày · Đổi lịch · Tiết khí · Văn khấn",
            accent: Color(hex: "2E7D32"),
            items: [
                ToolItem(action: "gooddays", title: "Ngày tốt / xấu", subtitle: "Hoàng đạo · Hắc đạo", icon: "sun.max.fill", colors: [Color(hex: "2E7D32"), Color(hex: "66BB6A")]),
                ToolItem(action: "datepicker", title: "Chọn ngày đại sự", subtitle: "Nhập trạch · Khai trương · Cưới hỏi", icon: "calendar.badge.checkmark", colors: [Color(hex: "8B0000"), Color(hex: "D4A017")], badge: "PRO"),
                ToolItem(action: "zodiac", title: "Xem tuổi hợp", subtitle: "Tương sinh · Tương khắc", icon: "heart.fill", colors: [Color(hex: "C2185B"), Color(hex: "E91E63")]),
                ToolItem(action: "lunar", title: "Đổi ngày Âm / Dương", subtitle: "Chuyển đổi nhanh", icon: "arrow.left.and.right", colors: [Color(hex: "512DA8"), Color(hex: "7E57C2")]),
                ToolItem(action: "tiet_khi", title: "24 Tiết Khí", subtitle: "Vòng năm Á Đông · phong thuỷ", icon: "leaf.fill", colors: [Color(hex: "388E3C"), Color(hex: "FFB300")]),
                ToolItem(action: "prayers", title: "Văn khấn", subtitle: "Bài cúng truyền thống", icon: "book.fill", colors: [Color(hex: "BF360C"), Color(hex: "E65100")]),
                ToolItem(action: "templates", title: "Mẫu tra cứu", subtitle: "Cưới hỏi · Động thổ · Khai trương", icon: "doc.text.magnifyingglass", colors: [Color(hex: "00838F"), Color(hex: "26A69A")])
            ]
        ),
        ToolSection(
            bannerType: "tools_feng_shui",
            title: "Phong thủy & Nghi lễ",
            subtitle: "La bàn · Lỗ Ban · Bát trạch · Nghi lễ năm",
            accent: Color(hex: "1565C0"),
            items: [
                ToolItem(action: "compass", title: "La bàn phong thủy", subtitle: "Đo hướng, định cung, xem trạch", icon: "location.north.circle.fill", colors: [Color(hex: "1565C0"), Color(hex: "26A69A")], badge: "NEW"),
                ToolItem(action: "loban", title: "Thước Lỗ Ban", subtitle: "Đo kích thước cát hung", icon: "ruler.fill", colors: [Color(hex: "6D4C41"), Color(hex: "BCAAA4")], badge: "NEW"),
                ToolItem(action: "bat_trach", title: "Bát trạch", subtitle: "Hướng tốt theo tuổi", icon: "house.fill", colors: [Color(hex: "8E24AA"), Color(hex: "BA68C8")], badge: "NEW"),
                ToolItem(action: "xong_dat", title: "Xông đất", subtitle: "Chọn người mở vận đầu năm", icon: "house.circle.fill", colors: [Color(hex: "EF6C00"), Color(hex: "FFB74D")], badge: "AI"),
                ToolItem(action: "xuat_hanh", title: "Xuất hành", subtitle: "Chọn hướng & giờ xuất phát", icon: "figure.walk", colors: [Color(hex: "00897B"), Color(hex: "4DB6AC")], badge: "AI"),
                ToolItem(action: "sao_han", title: "Sao hạn năm", subtitle: "Xem sao chiếu mệnh", icon: "exclamationmark.triangle.fill", colors: [Color(hex: "E53935"), Color(hex: "FFA726")], badge: "AI"),
                ToolItem(action: "phi_tinh", title: "Phi tinh cửu cung", subtitle: "Luồng khí theo từng cung", icon: "grid", colors: [Color(hex: "3949AB"), Color(hex: "5C6BC0")], badge: "AI")
            ]
        ),
        ToolSection(
            bannerType: "tools_utility",
            title: "Công cụ thực dụng",
            subtitle: "Tính ngày · Đếm ngược · Đồng hồ · Gia phả",
            accent: Color(hex: "1565C0"),
            items: [
                ToolItem(action: "datemath", title: "Máy tính ngày", subtitle: "Tuổi · Khoảng cách · Cộng/Trừ", icon: "plus.minus.and.percent", colors: [Color(hex: "1565C0"), Color(hex: "42A5F5")], badge: "NEW"),
                ToolItem(action: "countdown", title: "Đếm ngược sự kiện", subtitle: "Hiển thị Home / Widget", icon: "hourglass", colors: [Color(hex: "2E7D32"), Color(hex: "66BB6A")], badge: "NEW"),
                ToolItem(action: "worldclock", title: "Múi giờ thế giới", subtitle: "Đồng hồ & đổi múi giờ", icon: "globe", colors: [Color(hex: "1565C0"), Color(hex: "5C6BC0")], badge: "NEW"),
                ToolItem(action: "familytree", title: "Cây gia phả", subtitle: "Quản lý gia tộc", icon: "person.3.fill", colors: [Color(hex: "1E88E5"), Color(hex: "26A69A")]),
                ToolItem(action: "history", title: "Ngày này năm xưa", subtitle: "Sự kiện lịch sử", icon: "clock.arrow.2.circlepath", colors: [Color(hex: "E65100"), Color(hex: "F9A825")]),
                ToolItem(action: "birth_planner", title: "Ngày sinh đẹp", subtitle: "Gợi ý quanh ngày dự sinh", icon: "face.smiling", colors: [Color(hex: "E65100"), Color(hex: "FFFFB7")], badge: "PRO"),
                ToolItem(action: "cycle_tracker", title: "Chu kỳ kinh nguyệt", subtitle: "Kỳ tiếp theo · rụng trứng", icon: "app.gift", colors: [Color(hex: "C2185B"), Color(hex: "F06292")], badge: "NEW"),
                ToolItem(action: "widget_manager", title: "Quản lý Widget", subtitle: "Thêm widget vào màn hình", icon: "square.grid.2x2", colors: [Color(hex: "6A1B9A"), Color(hex: "9C27B0")])
            ]
        ),
        ToolSection(
            bannerType: "tools_collection",
            title: "Kho & Khám phá",
            subtitle: "AI · Rút quẻ · Sưu tập · Điểm thưởng",
            accent: Color(hex: "6A1B9A"),
            items: [
                ToolItem(action: "quiz", title: "Quiz Kiến thức", subtitle: "Thi đấu · Bảng xếp hạng", icon: "questionmark.app.fill", colors: [Color(hex: "1565C0"), Color(hex: "7B1FA2")], badge: "NEW"),
                ToolItem(action: "knowledge_feed", title: "Khám Phá", subtitle: "Sự kiện · Nhân vật · Bài viết", icon: "sparkles", colors: [Color(hex: "00838F"), Color(hex: "26A69A")], badge: "NEW"),
                ToolItem(action: "ai_chat", title: "Tử vi AI", subtitle: "Phân tích vận mệnh", icon: "cpu", colors: [Color(hex: "D32F2F"), Color(hex: "7B1FA2")], badge: "AI"),
                ToolItem(action: "oracle_draw", title: "Rút quẻ đầu ngày", subtitle: "Kinh Dịch · 1 lần/ngày", icon: "hands.sparkles.fill", colors: [Color(hex: "8B0000"), Color(hex: "D4A017")], badge: "v2"),
                ToolItem(action: "zodiac_collection", title: "Sưu tập 12 con giáp", subtitle: "Rút thẻ ngày · Đổi điểm ⚡", icon: "pawprint.fill", colors: [Color(hex: "2E7D32"), Color(hex: "D4A017")], badge: "NEW"),
                ToolItem(action: "bookmarks", title: "Ngày đã lưu", subtitle: "Đánh dấu quan trọng", icon: "bookmark.fill", colors: [Color(hex: "00838F"), Color(hex: "26C6DA")]),
                ToolItem(action: "points_ledger", title: "Nhật ký điểm", subtitle: "Lịch sử công đức", icon: "list.bullet.rectangle.portrait", colors: [Color(hex: "5D4037"), Color(hex: "8D6E63")]),
                ToolItem(action: "daily_store", title: "Kho mở khoá", subtitle: "Tiêu ⚡ mở tính năng", icon: "cart.fill", colors: [Color(hex: "D4A017"), Color(hex: "F9A825")]),
                ToolItem(action: "how_to_earn", title: "Hướng dẫn kiếm điểm", subtitle: "+100☘️ khi xem lần đầu", icon: "info.circle.fill", colors: [Color(hex: "D4A017"), Color(hex: "8B0000")], badge: "+100"),
                ToolItem(action: "streak_freeze", title: "Đóng băng streak", subtitle: "Mua vé · tặng bạn bè", icon: "snowflake", colors: [Color(hex: "1565C0"), Color(hex: "26C6DA")], badge: "NEW")
            ]
        )
    ]
    
    var body: some View {
        NavigationStack {
            ZStack {
                LSTheme.bg.ignoresSafeArea()
                
                VStack(spacing: 0) {
                    // Custom App Bar
                    HStack {
                        Button(action: onMenuClick) {
                            Image(systemName: "line.3.horizontal")
                                .font(.system(size: 20, weight: .semibold))
                                .foregroundColor(LSTheme.textPrimary)
                                .frame(width: 44, height: 44)
                        }
                        .accessibilityLabel("Menu")

                        VStack(alignment: .leading, spacing: 2) {
                            Text("Tiện ích")
                                .font(.system(size: 20, weight: .bold))
                                .foregroundColor(LSTheme.textPrimary)
                            Text("Khám phá công cụ hữu ích")
                                .font(.system(size: 12))
                                .foregroundColor(LSTheme.textTertiary)
                        }
                        
                        Spacer()
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    
                    ScrollView {
                        VStack(spacing: 20) {
                            ForEach(sections) { section in
                                sectionView(section)
                            }
                        }
                        .padding(.vertical, 16)
                    }
                }
            }
            .task {
                await loadSectionBanners()
            }
            .navigationDestination(for: String.self) { route in
                if route.hasPrefix("ai_chat:") {
                    let prompt = String(route.dropFirst(8))
                    AIChatScreen(initialMessage: prompt, isEmbeddedInTabBar: true)
                } else {
                    switch route {
                    case "gooddays":
                        GoodDaysScreen()
                    case "datepicker":
                        DatePickerToolScreen()
                    case "zodiac":
                        SearchScreen(initialTool: "zodiac")
                    case "lunar":
                        SearchScreen(initialTool: "lunar")
                    case "tiet_khi":
                        TietKhiScreen()
                    case "prayers":
                        PrayersScreen()
                    case "compass":
                        CompassScreen { prompt in
                            activeRoute = "ai_chat:" + prompt
                        }
                    case "loban":
                        LoBanScreen { prompt in
                            activeRoute = "ai_chat:" + prompt
                        }
                    case "bat_trach":
                        BatTrachScreen { prompt in
                            activeRoute = "ai_chat:" + prompt
                        }
                    case "xong_dat":
                        AIChatScreen(initialMessage: "Hãy gợi ý người xông đất hợp tuổi, hợp mệnh và các lưu ý cần tránh cho năm nay.", isEmbeddedInTabBar: true)
                    case "xuat_hanh":
                        AIChatScreen(initialMessage: "Hãy gợi ý hướng và giờ xuất hành tốt, kèm giải thích ngắn gọn theo phong thuỷ.", isEmbeddedInTabBar: true)
                    case "sao_han":
                        AIChatScreen(initialMessage: "Hãy phân tích sao hạn năm nay và cho lời khuyên hoá giải đơn giản, dễ hiểu.", isEmbeddedInTabBar: true)
                    case "phi_tinh":
                        AIChatScreen(initialMessage: "Hãy giải thích phi tinh cửu cung và gợi ý cách đọc cơ bản cho người mới.", isEmbeddedInTabBar: true)
                    case "datemath":
                        DateMathScreen()
                    case "countdown":
                        CountdownScreen()
                    case "worldclock":
                        WorldClockScreen()
                    case "familytree":
                        FamilyTreeScreen()
                    case "history":
                        ThisDayInHistoryScreen()
                    case "birth_planner":
                        BirthDatePlannerScreen()
                    case "cycle_tracker":
                        CycleTrackerScreen()
                    case "widget_manager":
                        WidgetManagerScreen()
                    case "quiz":
                        QuizHomeScreen()
                    case "knowledge_feed":
                        KnowledgeFeedScreen()
                    case "ai_chat":
                        AIChatScreen(isEmbeddedInTabBar: true)
                    case "oracle_draw":
                        OracleDrawScreen { prompt in
                            activeRoute = "ai_chat:" + prompt
                        }
                    case "zodiac_collection":
                        ZodiacCollectionScreen()
                    case "bookmarks":
                        BookmarksScreen()
                    case "templates":
                        TemplatesScreen()
                    case "points_ledger":
                        LedgerScreen()
                    case "daily_store":
                        DailyUnlockStoreScreen()
                    case "how_to_earn":
                        PointsTutorialScreen()
                    case "streak_freeze":
                        StreakFreezeScreen()
                    default:
                        PlaceholderScreen(title: route.capitalized, icon: "cpu")
                    }
                }
            }
        }
    }
    
    private func sectionView(_ section: ToolSection) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            if let banner = bannerFor(section) {
                sectionBannerView(banner)
                    .padding(.horizontal, 16)
            }

            // Header
            HStack(spacing: 8) {
                RoundedRectangle(cornerRadius: 2)
                    .fill(section.accent)
                    .frame(width: 4, height: 18)
                
                VStack(alignment: .leading, spacing: 1) {
                    Text(section.title)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(LSTheme.textPrimary)
                    Text(section.subtitle)
                        .font(.system(size: 11))
                        .foregroundColor(LSTheme.textTertiary)
                }
            }
            .padding(.horizontal, 16)
            
            // Grid of items
            let columns = [
                GridItem(.flexible(), spacing: 10),
                GridItem(.flexible(), spacing: 10),
                GridItem(.flexible(), spacing: 10)
            ]
            
            LazyVGrid(columns: columns, spacing: 10) {
                ForEach(section.items) { item in
                    NavigationLink(value: item.action) {
                        toolCard(item)
                    }
                    .buttonStyle(PlainButtonStyle())
                }
            }
            .padding(.horizontal, 16)
        }
    }
    
    private func toolCard(_ item: ToolItem) -> some View {
        VStack(spacing: 8) {
            ZStack(alignment: .topTrailing) {
                // Icon wrapper with gradient
                RoundedRectangle(cornerRadius: 12)
                    .fill(LinearGradient(colors: item.colors, startPoint: .topLeading, endPoint: .bottomTrailing))
                    .frame(width: 40, height: 40)
                    .overlay(
                        Image(systemName: item.icon)
                            .font(.system(size: 18))
                            .foregroundColor(.white)
                    )
                
                if let badge = item.badge {
                    Text(badge)
                        .font(.system(size: 7, weight: .bold))
                        .foregroundColor(.black)
                        .padding(.horizontal, 4)
                        .padding(.vertical, 2)
                        .background(LSTheme.gold)
                        .cornerRadius(4)
                        .offset(x: 8, y: -6)
                }
            }
            
            Text(item.title)
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(LSTheme.textPrimary)
                .multilineTextAlignment(.center)
                .lineLimit(2)
                .frame(height: 30)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .padding(.horizontal, 8)
        .background(LSTheme.surfaceContainer)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(LSTheme.outlineVariant.opacity(0.3), lineWidth: 1)
        )
    }

    private func sectionBannerView(_ banner: Banner) -> some View {
        let imageUrl = normalizeServerMediaUrl(banner.imageUrl)
        return Button {
            let route = banner.ctaRoute?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            if !route.isEmpty {
                onBannerAction(route)
            }
        } label: {
            ZStack {
                RoundedRectangle(cornerRadius: 14)
                    .fill(Color(hex: (banner.bgColor ?? "").replacingOccurrences(of: "#", with: "").isEmpty ? "8D1A00" : (banner.bgColor ?? "").replacingOccurrences(of: "#", with: "")))
                    .frame(height: 84)

                if let imageUrl, let url = URL(string: imageUrl) {
                    CachedAsyncImage(url: url) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        Color.clear
                    }
                    .frame(height: 84)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                }

                RoundedRectangle(cornerRadius: 14)
                    .fill(
                        LinearGradient(
                            colors: [
                                Color.black.opacity(0.45),
                                Color.black.opacity(0.15),
                            ],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )

                VStack(alignment: .leading, spacing: 2) {
                    Text(banner.title)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.white)
                        .lineLimit(1)
                    if let subtitle = banner.subtitle, !subtitle.isEmpty {
                        Text(subtitle)
                            .font(.system(size: 11))
                            .foregroundColor(.white.opacity(0.9))
                            .lineLimit(1)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 14)
            }
        }
        .buttonStyle(.plain)
    }

    /// Khớp banner cho 1 section theo "location key" (giống Android: banner.locations chứa key).
    /// Vẫn fallback theo `type` để tương thích banner cấu hình kiểu cũ.
    private func bannerFor(_ section: ToolSection) -> Banner? {
        let key = section.bannerType.lowercased()
        return sectionBanners.first { banner in
            if let locs = banner.locations, locs.contains(where: { $0.lowercased() == key }) {
                return true
            }
            return banner.type?.lowercased() == key
        }
    }

    @MainActor
    private func loadSectionBanners() async {
        do {
            // Lấy đúng phạm vi tiện ích (server lọc theo locations + platform).
            let list = try await QuizService.shared.fetchBanners(
                location: "tools,tools_calendar,tools_feng_shui,tools_utility,tools_collection"
            )
            sectionBanners = list.filter { $0.active }.sorted { $0.sortOrder < $1.sortOrder }
        } catch {
            sectionBanners = []
        }
    }
}
