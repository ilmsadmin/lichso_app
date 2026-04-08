import SwiftUI

struct SettingsScreen: View {
    @Environment(\.lichSoColors) private var c
    @EnvironmentObject private var settings: AppSettings
    var onBackClick: () -> Void = {}

    var body: some View {
        VStack(spacing: 0) {
            // Top bar
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(c.textPrimary)
                }
                Spacer()
                Text("CÀI ĐẶT")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(c.textPrimary)
                    .tracking(1)
                Spacer()
                Color.clear.frame(width: 24)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            List {
                // Display section
                Section("Hiển thị") {
                    Picker("Chế độ giao diện", selection: $settings.themeMode) {
                        Text("Hệ thống").tag("system")
                        Text("Sáng").tag("light")
                        Text("Tối").tag("dark")
                    }
                    Toggle("Hiển thị lịch âm", isOn: $settings.lunarBadgeEnabled)
                    Toggle("Hiển thị câu danh ngôn", isOn: $settings.quoteEnabled)
                    Toggle("Hiển thị ngày lễ", isOn: $settings.festivalEnabled)
                    Toggle("Hiển thị giờ đại cát", isOn: $settings.gioDaiCatEnabled)
                }

                // Calendar section
                Section("Lịch") {
                    Picker("Ngày bắt đầu tuần", selection: $settings.weekStart) {
                        Text("Thứ Hai").tag("Thứ Hai")
                        Text("Chủ Nhật").tag("Chủ Nhật")
                    }
                }

                // Weather section
                Section("Thời tiết") {
                    HStack {
                        Text("Vị trí")
                        Spacer()
                        TextField("Thành phố", text: $settings.locationName)
                            .multilineTextAlignment(.trailing)
                            .foregroundColor(c.textSecondary)
                    }
                    Picker("Đơn vị nhiệt độ", selection: $settings.tempUnit) {
                        Text("°C").tag("°C")
                        Text("°F").tag("°F")
                    }
                }

                // Notification section
                Section("Thông báo") {
                    Toggle("Bật thông báo", isOn: $settings.notifyEnabled)
                    Toggle("Nhắc nhở ngày lễ", isOn: $settings.festivalReminderEnabled)
                    HStack {
                        Text("Giờ nhắc nhở")
                        Spacer()
                        Text(String(format: "%02d:%02d", settings.reminderHour, settings.reminderMinute))
                            .foregroundColor(c.textSecondary)
                    }
                }

                // About section
                Section("Thông tin") {
                    HStack {
                        Text("Phiên bản")
                        Spacer()
                        Text("1.0.0")
                            .foregroundColor(c.textTertiary)
                    }
                    Link("Chính sách bảo mật", destination: URL(string: "https://apps.zenix.vn/privacy-policy")!)
                    Link("Đánh giá ứng dụng", destination: URL(string: "https://apps.apple.com/app/lichso")!)
                }
            }
            .listStyle(.insetGrouped)
        }
        .background(c.bg)
    }
}
