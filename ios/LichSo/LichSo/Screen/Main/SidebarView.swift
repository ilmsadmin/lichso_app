import SwiftUI

// ═══════════════════════════════════════════
// Sidebar / Drawer Menu
// Matches Android drawer design: Red header → nav items → explore → info → footer
// ═══════════════════════════════════════════

private var SurfaceBg: Color { LSTheme.bg }
private var SurfaceCard: Color { LSTheme.surfaceContainerHigh }
private var TextPrimary: Color { LSTheme.textPrimary }
private var TextSecondary: Color { LSTheme.textSecondary }
private var TextTertiary: Color { LSTheme.textTertiary }
private var PrimaryRed: Color { LSTheme.primary }
private var OutlineColor: Color { LSTheme.outlineVariant }
private var SelectedBg: Color { LSTheme.primaryContainer }
private var SelectedFg: Color { LSTheme.primary }

private let HeaderGradient: [Color] = [
    Color(hex: "5D1212"),
    Color(hex: "4A1010")
]

// MARK: - Menu Item Model

private struct MenuItem: Identifiable {
    let id = UUID()
    let icon: String
    let iconFilled: String
    let title: String
    let route: String

    init(icon: String, iconFilled: String? = nil, title: String, route: String) {
        self.icon = icon
        self.iconFilled = iconFilled ?? icon
        self.title = title
        self.route = route
    }
}

// MARK: - Sidebar View

struct SidebarView: View {
    @Binding var isOpen: Bool
    var onNavigate: (String) -> Void

    @State private var dragOffset: CGFloat = 0

    private let drawerWidth: CGFloat = 300

    var body: some View {
        ZStack(alignment: .leading) {
            // Dimmed backdrop
            if isOpen {
                Color.black
                    .opacity(0.5 * Double(1 - dragOffset / drawerWidth))
                    .ignoresSafeArea()
                    .onTapGesture { close() }
                    .transition(.opacity)
            }

            // Drawer panel
            HStack(spacing: 0) {
                drawerContent
                    .frame(width: drawerWidth)
                    .background(SurfaceBg)
                    .offset(x: isOpen ? dragOffset : -drawerWidth)

                Spacer()
            }
            .gesture(
                DragGesture()
                    .onChanged { value in
                        let offset = value.translation.width
                        if offset < 0 {
                            dragOffset = offset
                        }
                    }
                    .onEnded { value in
                        if value.translation.width < -80 {
                            close()
                        } else {
                            withAnimation(.easeOut(duration: 0.2)) {
                                dragOffset = 0
                            }
                        }
                    }
            )
        }
        .animation(.easeInOut(duration: 0.28), value: isOpen)
        .onChange(of: isOpen) {
            if isOpen { dragOffset = 0 }
        }
    }

    private func close() {
        withAnimation(.easeInOut(duration: 0.25)) {
            isOpen = false
            dragOffset = 0
        }
    }

    // MARK: - Drawer Content

    private var drawerContent: some View {
        VStack(spacing: 0) {
            // ═══ Header (Red Gradient) ═══
            drawerHeader

            // ═══ Scrollable Menu Items ═══
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 0) {
                    // Main navigation
                    ForEach(mainItems) { item in
                        menuRow(item: item)
                    }

                    divider

                    // Section: Khám phá
                    sectionTitle("KHÁM PHÁ")

                    ForEach(exploreItems) { item in
                        menuRow(item: item)
                    }

                    divider

                    // Settings
                    ForEach(bottomItems) { item in
                        menuRow(item: item)
                    }

                    divider

                    // Section: Thông tin
                    sectionTitle("THÔNG TIN")

                    actionRow(icon: "star", title: "Đánh giá ứng dụng") {
                        openURL("https://apps.apple.com/app/id6740048518")
                    }
                    actionRow(icon: "square.and.arrow.up", title: "Chia sẻ ứng dụng") {
                        shareApp()
                    }
                    actionRow(icon: "shield.checkered", title: "Chính sách bảo mật") {
                        openURL("https://apps.zenix.vn/privacy-policy")
                    }
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
            }

            // ═══ Footer ═══
            drawerFooter
        }
    }

    // MARK: - Header

    private var drawerHeader: some View {
        VStack(spacing: 0) {
            Spacer().frame(height: 0) // status bar spacing handled by safearea

            HStack(spacing: 16) {
                // App icon
                Image("AppLogo")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 52, height: 52)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .overlay(
                        RoundedRectangle(cornerRadius: 14)
                            .stroke(Color.white.opacity(0.15), lineWidth: 1)
                    )
                    .shadow(color: .black.opacity(0.3), radius: 8)

                VStack(alignment: .leading, spacing: 3) {
                    Text("Lịch Số")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(.white)
                    Text("Lịch vạn niên số 1 Việt Nam")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(.white.opacity(0.8))
                }

                Spacer()
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 24)
        }
        .background(
            LinearGradient(
                colors: HeaderGradient,
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
    }

    // MARK: - Menu Row

    private func menuRow(item: MenuItem) -> some View {
        Button {
            onNavigate(item.route)
            close()
        } label: {
            HStack(spacing: 16) {
                Image(systemName: item.icon)
                    .font(.system(size: 20))
                    .foregroundColor(TextPrimary)
                    .frame(width: 24)

                Text(item.title)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(TextPrimary)

                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(
                RoundedRectangle(cornerRadius: 28)
                    .fill(Color.clear)
            )
        }
        .buttonStyle(.plain)
    }

    // MARK: - Action Row (external links)

    private func actionRow(icon: String, title: String, action: @escaping () -> Void) -> some View {
        Button {
            action()
        } label: {
            HStack(spacing: 16) {
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .foregroundColor(TextSecondary)
                    .frame(width: 24)

                Text(title)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(TextSecondary)

                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
        }
        .buttonStyle(.plain)
    }

    // MARK: - Section Title

    private func sectionTitle(_ text: String) -> some View {
        HStack {
            Text(text)
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(TextTertiary)
                .tracking(1)
            Spacer()
        }
        .padding(.leading, 16)
        .padding(.top, 8)
        .padding(.bottom, 8)
    }

    // MARK: - Divider

    private var divider: some View {
        Rectangle()
            .fill(OutlineColor)
            .frame(height: 0.5)
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
    }

    // MARK: - Footer

    private var drawerFooter: some View {
        VStack(spacing: 6) {
            Rectangle()
                .fill(OutlineColor)
                .frame(height: 0.5)
                .padding(.horizontal, 24)

            Spacer().frame(height: 8)

            // Zenix logo placeholder
            HStack(spacing: 4) {
                Image(systemName: "sparkle")
                    .font(.system(size: 14))
                    .foregroundStyle(
                        LinearGradient(
                            colors: [Color(hex: "42A5F5"), Color(hex: "26A69A")],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                Text("Zenix Labs")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundStyle(
                        LinearGradient(
                            colors: [Color(hex: "42A5F5"), Color(hex: "26A69A"), Color(hex: "009688")],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
            }

            Text("Lịch Số v\(appVersion)")
                .font(.system(size: 11, weight: .medium))
                .foregroundColor(TextTertiary)

            HStack(spacing: 3) {
                Text("Phát triển bởi")
                    .font(.system(size: 10))
                    .foregroundColor(TextTertiary.opacity(0.7))
                Text("Zenix Labs")
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundStyle(
                        LinearGradient(
                            colors: [Color(hex: "42A5F5"), Color(hex: "26A69A"), Color(hex: "009688")],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
            }

            Spacer().frame(height: 16)
        }
    }

    // MARK: - Data

    private var mainItems: [MenuItem] {
        [
            MenuItem(icon: "sun.max", iconFilled: "sun.max.fill", title: "Trang chủ", route: "home"),
            MenuItem(icon: "calendar", title: "Lịch tháng", route: "calendar"),
            MenuItem(icon: "checkmark.circle", iconFilled: "checkmark.circle.fill", title: "Ngày tốt / xấu", route: "gooddays"),
            MenuItem(icon: "bookmark", iconFilled: "bookmark.fill", title: "Ngày đã lưu", route: "bookmarks"),
        ]
    }

    private var exploreItems: [MenuItem] {
        [
            MenuItem(icon: "clock.arrow.circlepath", title: "Ngày này năm xưa", route: "history"),
            MenuItem(icon: "person.3", iconFilled: "person.3.fill", title: "Cây gia phả", route: "familytree"),
            MenuItem(icon: "book", iconFilled: "book.fill", title: "Các bài văn khấn", route: "prayers"),
        ]
    }

    private var bottomItems: [MenuItem] {
        [
            MenuItem(icon: "gearshape", iconFilled: "gearshape.fill", title: "Cài đặt", route: "settings"),
        ]
    }

    // MARK: - Helpers

    private var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
    }

    private func openURL(_ urlString: String) {
        guard let url = URL(string: urlString) else { return }
        UIApplication.shared.open(url)
    }

    private func shareApp() {
        let text = """
🗓️ Lịch Số – Lịch Vạn Niên, Phong Thuỷ & AI Tử Vi

Ứng dụng lịch âm dương đầy đủ nhất:
✅ Lịch vạn niên – âm dương, can chi, giờ hoàng đạo
✅ Ngày tốt xấu theo phong thuỷ truyền thống
✅ Văn khấn cúng lễ – hơn 100 bài đầy đủ
✅ Gia phả gia đình – lưu giữ cội nguồn
✅ Trợ lý AI phong thuỷ & tử vi thông minh
✅ Widget lịch ngay trên màn hình chính

📲 Tải miễn phí tại:
https://apps.apple.com/app/id6740048518
"""
        let activityVC = UIActivityViewController(activityItems: [text], applicationActivities: nil)
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = windowScene.windows.first?.rootViewController {
            rootVC.present(activityVC, animated: true)
        }
    }
}

// MARK: - Preview

#Preview {
    ZStack {
        Color(hex: "0F0E0C").ignoresSafeArea()
        SidebarView(isOpen: .constant(true)) { route in
            print("Navigate to: \(route)")
        }
    }
}
