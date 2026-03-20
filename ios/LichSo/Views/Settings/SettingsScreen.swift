import SwiftUI
import UserNotifications

// MARK: - Settings Screen
struct SettingsScreen: View {
    @EnvironmentObject var viewModel: SettingsViewModel
    @Environment(\.lichSoColors) var c

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                // Header
                Text("Cài đặt")
                    .font(.system(size: 22, weight: .bold, design: .serif))
                    .foregroundColor(c.gold2)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 14)

                // ═══ General Settings ═══
                SettingsGroup(label: "Cài đặt chung") {
                    SettingsToggleRow(
                        title: "Thông báo nhắc nhở",
                        subtitle: "Nhận nhắc nhở công việc đúng giờ",
                        icon: "bell",
                        isOn: $viewModel.notifyEnabled
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        title: "Hiển thị lịch âm",
                        subtitle: "Ngày âm lịch trên màn hình chính",
                        icon: "moon.circle",
                        isOn: $viewModel.lunarBadgeEnabled
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        title: "Thông báo giờ đại cát",
                        subtitle: "Nhắc nhở lúc 6h sáng mỗi ngày",
                        icon: "clock.badge.checkmark",
                        isOn: $viewModel.gioDaiCatEnabled
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        title: "Chế độ tối",
                        subtitle: "Giao diện tối bảo vệ mắt",
                        icon: "moon.fill",
                        isOn: $viewModel.darkModeEnabled
                    )
                }

                Spacer(minLength: 12)

                // ═══ About ═══
                SettingsGroup(label: "Thông tin") {
                    SettingsArrowRow(title: "Hướng dẫn sử dụng", icon: "questionmark.circle") {
                        viewModel.showHelp = true
                    }
                    SettingsDivider()
                    SettingsArrowRow(title: "Đánh giá ứng dụng", icon: "star") {
                        viewModel.rateApp()
                    }
                    SettingsDivider()
                    SettingsArrowRow(title: "Chia sẻ ứng dụng", icon: "square.and.arrow.up") {
                        viewModel.shareApp()
                    }
                    SettingsDivider()
                    SettingsArrowRow(title: "Chính sách bảo mật", icon: "lock.shield") {
                        viewModel.showPrivacyPolicy = true
                    }
                }

                Spacer(minLength: 12)

                // ═══ Data ═══
                SettingsGroup(label: "Dữ liệu") {
                    SettingsArrowRow(
                        title: "Xoá cache",
                        icon: "trash",
                        value: viewModel.cacheSize
                    ) {
                        viewModel.clearCache()
                    }
                }

                Spacer(minLength: 18)

                // Version info
                VStack(spacing: 3) {
                    Text("Lịch Số — Lịch Vạn Niên")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(c.textTertiary)
                    Text("Phiên bản \(viewModel.appVersion)")
                        .font(.system(size: 11))
                        .foregroundColor(c.textQuaternary)
                    Text("Made with ♥ by Lịch Số Team")
                        .font(.system(size: 10))
                        .foregroundColor(c.textQuaternary)
                }
                .frame(maxWidth: .infinity)

                Spacer(minLength: 96)
            }
        }
        .background(c.bg.ignoresSafeArea())
        // ─── Sheets ───
        .sheet(isPresented: $viewModel.showPrivacyPolicy) {
            PrivacyPolicyView()
                .environment(\.lichSoColors, c)
        }
        .sheet(isPresented: $viewModel.showHelp) {
            HelpGuideView()
                .environment(\.lichSoColors, c)
        }
        .sheet(isPresented: $viewModel.showShareSheet) {
            ShareSheetView(items: [
                "📅 Lịch Số — Lịch Vạn Niên\nỨng dụng lịch âm dương, phong thuỷ & AI chat thông minh!\nhttps://apps.apple.com/app/lich-so"
            ])
        }
        // ─── Notification denied alert ───
        .alert("Quyền thông báo bị từ chối", isPresented: $viewModel.notificationPermissionDenied) {
            Button("Mở Cài đặt") {
                if let url = URL(string: UIApplication.openSettingsURLString) {
                    UIApplication.shared.open(url)
                }
            }
            Button("Huỷ", role: .cancel) {}
        } message: {
            Text("Vui lòng vào Cài đặt > Lịch Số để bật quyền thông báo.")
        }
        // ─── Toast ───
        .overlay(alignment: .bottom) {
            if let msg = viewModel.toastMessage {
                ToastBanner(message: msg)
                    .padding(.bottom, 100)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .onAppear {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                            withAnimation { viewModel.consumeToast() }
                        }
                    }
            }
        }
        .animation(.easeInOut(duration: 0.3), value: viewModel.toastMessage)
    }
}

// MARK: - Settings Group
struct SettingsGroup<Content: View>: View {
    let label: String
    @ViewBuilder let content: () -> Content
    @Environment(\.lichSoColors) var c

    var body: some View {
        VStack(alignment: .leading, spacing: 7) {
            Text(label.uppercased())
                .font(.system(size: 10.5, weight: .bold))
                .foregroundColor(c.textTertiary)
                .kerning(1)
                .padding(.horizontal, 20)

            VStack(spacing: 0) {
                content()
            }
            .background(c.bg2)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(c.border, lineWidth: 1))
            .padding(.horizontal, 20)
        }
    }
}

// MARK: - Settings Toggle Row
struct SettingsToggleRow: View {
    let title: String
    var subtitle: String? = nil
    let icon: String
    @Binding var isOn: Bool
    @Environment(\.lichSoColors) var c

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundColor(c.teal)
                .frame(width: 24)
            VStack(alignment: .leading, spacing: 1) {
                Text(title)
                    .font(.system(size: 14))
                    .foregroundColor(c.textPrimary)
                if let sub = subtitle {
                    Text(sub)
                        .font(.system(size: 11))
                        .foregroundColor(c.textTertiary)
                }
            }
            Spacer()
            Toggle("", isOn: $isOn)
                .labelsHidden()
                .tint(c.teal)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 11)
    }
}

// MARK: - Settings Arrow Row
struct SettingsArrowRow: View {
    let title: String
    let icon: String
    var value: String? = nil
    let action: () -> Void
    @Environment(\.lichSoColors) var c

    var body: some View {
        Button(action: action) {
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.system(size: 16))
                    .foregroundColor(c.teal)
                    .frame(width: 24)
                Text(title)
                    .font(.system(size: 14))
                    .foregroundColor(c.textPrimary)
                Spacer()
                if let val = value {
                    Text(val)
                        .font(.system(size: 12))
                        .foregroundColor(c.textTertiary)
                }
                Image(systemName: "chevron.right")
                    .font(.system(size: 12))
                    .foregroundColor(c.textTertiary)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 12)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Settings Divider
struct SettingsDivider: View {
    @Environment(\.lichSoColors) var c

    var body: some View {
        Divider()
            .overlay(c.border)
            .padding(.leading, 50)
    }
}

// MARK: - Share Sheet (UIActivityViewController wrapper)
struct ShareSheetView: UIViewControllerRepresentable {
    let items: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

// MARK: - Toast Banner
struct ToastBanner: View {
    let message: String
    @Environment(\.lichSoColors) var c

    var body: some View {
        Text(message)
            .font(.system(size: 13, weight: .medium))
            .foregroundColor(.white)
            .padding(.horizontal, 18)
            .padding(.vertical, 10)
            .background(Color.black.opacity(0.78))
            .clipShape(Capsule())
            .shadow(color: .black.opacity(0.2), radius: 8, x: 0, y: 4)
    }
}

// MARK: - Help Guide View
struct HelpGuideView: View {
    @Environment(\.lichSoColors) var c
    @Environment(\.dismiss) var dismiss

    struct HelpSection: Identifiable {
        let id = UUID()
        let icon: String
        let title: String
        let color: Color
        let items: [String]
    }

    var sections: [HelpSection] {[
        HelpSection(
            icon: "house.fill",
            title: "Màn hình chính",
            color: .orange,
            items: [
                "📅 Xem ngày âm dương đầy đủ với Can Chi, Tiết Khí",
                "🌟 Danh sách Giờ Hoàng Đạo trong ngày",
                "✅ Việc nên làm và không nên làm hôm nay",
                "🧭 Hướng Thần Tài, Hỷ Thần xuất hành tốt",
                "Vuốt ngang để chuyển ngày trước/sau"
            ]
        ),
        HelpSection(
            icon: "calendar",
            title: "Lịch tháng",
            color: .green,
            items: [
                "📆 Xem lịch âm dương theo tháng dạng lưới",
                "Số nhỏ bên dưới mỗi ô là ngày âm lịch",
                "Chấm vàng đánh dấu hôm nay",
                "Chấm xanh đánh dấu ngày có công việc",
                "Nhấn vào ngày bất kỳ để xem chi tiết đầy đủ"
            ]
        ),
        HelpSection(
            icon: "checklist",
            title: "Công việc & Nhắc nhở",
            color: .blue,
            items: [
                "📝 Ba tab: Việc cần làm / Ghi chú / Nhắc nhở",
                "Nhấn nút + để thêm mục mới",
                "Vuốt trái để xoá mục",
                "Nhấn vào checkbox để đánh dấu hoàn thành",
                "Nhắc nhở sẽ gửi thông báo đúng thời gian đã chọn"
            ]
        ),
        HelpSection(
            icon: "sparkles",
            title: "AI Phong Thuỷ",
            color: .purple,
            items: [
                "🤖 Chat với AI để hỏi về phong thuỷ, ngày tốt/xấu",
                "AI tự động biết ngày âm lịch và giờ hoàng đạo hôm nay",
                "Chủ đề gợi ý: nhấn vào để hỏi nhanh",
                "Nhấn robot FAB (nút vàng) để mở chat bất kỳ lúc nào",
                "Kéo thả nút robot đến vị trí bạn muốn"
            ]
        ),
        HelpSection(
            icon: "gearshape.fill",
            title: "Cài đặt",
            color: .gray,
            items: [
                "🔔 Bật/tắt thông báo nhắc nhở công việc",
                "🌙 Bật nhận thông báo giờ hoàng đạo lúc 6h sáng",
                "🌓 Chuyển chế độ tối/sáng theo ý thích",
                "Hiển thị lịch âm: bật để thấy ngày âm trên màn hình chính"
            ]
        )
    ]}

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Intro
                    HStack(spacing: 12) {
                        Text("📅")
                            .font(.system(size: 36))
                        VStack(alignment: .leading, spacing: 3) {
                            Text("Lịch Số")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(c.gold2)
                            Text("Lịch Vạn Niên & Phong Thuỷ AI")
                                .font(.system(size: 13))
                                .foregroundColor(c.textSecondary)
                        }
                    }
                    .padding(16)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(c.bg2)
                    .clipShape(RoundedRectangle(cornerRadius: 12))

                    ForEach(sections) { section in
                        VStack(alignment: .leading, spacing: 10) {
                            // Section header
                            HStack(spacing: 8) {
                                Image(systemName: section.icon)
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundColor(section.color)
                                    .frame(width: 28, height: 28)
                                    .background(section.color.opacity(0.15))
                                    .clipShape(RoundedRectangle(cornerRadius: 7))
                                Text(section.title)
                                    .font(.system(size: 15, weight: .semibold))
                                    .foregroundColor(c.textPrimary)
                            }

                            VStack(alignment: .leading, spacing: 7) {
                                ForEach(section.items, id: \.self) { item in
                                    Text(item)
                                        .font(.system(size: 13))
                                        .foregroundColor(c.textSecondary)
                                        .padding(.leading, 4)
                                }
                            }
                            .padding(14)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(c.bg2)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                        }
                    }

                    // Contact
                    VStack(spacing: 6) {
                        Text("Cần hỗ trợ thêm?")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(c.textTertiary)
                        Text("support@lichso.app")
                            .font(.system(size: 12))
                            .foregroundColor(c.teal)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.top, 4)
                    .padding(.bottom, 16)
                }
                .padding(16)
            }
            .background(c.bg.ignoresSafeArea())
            .navigationTitle("Hướng dẫn sử dụng")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Đóng") { dismiss() }
                }
            }
        }
    }
}

// MARK: - Privacy Policy
struct PrivacyPolicyView: View {
    @Environment(\.lichSoColors) var c
    @Environment(\.dismiss) var dismiss

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    Text(privacyText)
                        .font(.system(size: 13))
                        .foregroundColor(c.textSecondary)
                        .padding(20)
                }
            }
            .background(c.bg.ignoresSafeArea())
            .navigationTitle("Chính sách bảo mật")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Đóng") { dismiss() }
                }
            }
        }
    }

    let privacyText = """
    CHÍNH SÁCH BẢO MẬT — LỊCH SỐ

    1. Thông tin thu thập
    Ứng dụng Lịch Số không thu thập thông tin cá nhân của bạn. Tất cả dữ liệu (ghi chú, nhắc nhở, việc làm) được lưu trữ cục bộ trên thiết bị của bạn.

    2. Quyền truy cập
    Ứng dụng có thể yêu cầu quyền gửi thông báo để nhắc nhở lịch âm và giờ hoàng đạo. Chúng tôi không bao giờ đọc lịch hệ thống hay danh bạ của bạn.

    3. AI Chat
    Tin nhắn chat với AI được xử lý qua Google Gemini API. Vui lòng không chia sẻ thông tin cá nhân nhạy cảm trong chat. Xem thêm: https://policies.google.com/privacy

    4. Dữ liệu lưu trữ
    Tất cả công việc, ghi chú và nhắc nhở chỉ được lưu trong bộ nhớ máy. Chúng tôi không có máy chủ thu thập dữ liệu của bạn.

    5. Quyền trẻ em
    Ứng dụng không hướng đến trẻ em dưới 13 tuổi và không thu thập thông tin của trẻ em.

    6. Liên hệ
    Mọi thắc mắc vui lòng liên hệ: support@lichso.app

    Cập nhật lần cuối: 20/03/2026
    """
}
