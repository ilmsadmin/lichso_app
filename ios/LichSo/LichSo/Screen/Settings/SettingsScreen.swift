import SwiftUI

// ═══════════════════════════════════════════
// Settings Screen — matches screen-settings.html
// 5 sections: Chung, Hiển thị, Thông báo,
// Vị trí & Thời tiết, Thông tin
// ═══════════════════════════════════════════

private let PrimaryRed = Color(hex: "B71C1C")
private let SurfaceBg = Color(hex: "FFFBF5")
private let SurfaceContainer = Color(hex: "FFF8F0")
private let SurfaceVariant = Color(hex: "F5DDD8")
private let TextMain = Color(hex: "1C1B1F")
private let TextSub = Color(hex: "534340")
private let TextDim = Color(hex: "857371")
private let OutlineVariant = Color(hex: "D8C2BF")

struct SettingsScreen: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var vm = SettingsViewModel()

    // Dialogs
    @State private var showWeekStartPicker = false
    @State private var showThemePicker = false
    @State private var showTimePicker = false
    @State private var showTempUnitPicker = false
    @State private var showAbout = false

    var body: some View {
        VStack(spacing: 0) {
            // ═══ TOP BAR ═══
            TopBar(onBack: { dismiss() })

            // ═══ CONTENT ═══
            ScrollView(.vertical, showsIndicators: false) {
                VStack(alignment: .leading, spacing: 0) {

                    // ── CHUNG ──
                    SectionTitle("Chung")

                    SettingsGroup {
                        ArrowItem(
                            iconBg: Color(hex: "FFEBEE"), iconColor: Color(hex: "C62828"),
                            icon: "calendar", title: "Ngày bắt đầu tuần",
                            desc: "Ngày đầu tiên của tuần",
                            value: vm.weekStart
                        ) { showWeekStartPicker = true }

                        SettingDivider()

                        ArrowItem(
                            iconBg: Color(hex: "FFF8E1"), iconColor: Color(hex: "F57F17"),
                            icon: "globe", title: "Ngôn ngữ",
                            desc: "Ngôn ngữ hiển thị",
                            value: vm.language
                        ) { }

                        SettingDivider()

                        ArrowItem(
                            iconBg: Color(hex: "F3E5F5"), iconColor: Color(hex: "7B1FA2"),
                            icon: "paintpalette.fill", title: "Giao diện",
                            desc: "Sáng / Tối / Theo hệ thống",
                            value: vm.theme
                        ) { showThemePicker = true }
                    }

                    // ── HIỂN THỊ ──
                    SectionTitle("Hiển thị")

                    SettingsGroup {
                        ToggleItem(
                            iconBg: Color(hex: "E0F2F1"), iconColor: Color(hex: "00695C"),
                            icon: "moon.fill", title: "Hiển thị âm lịch",
                            desc: "Hiện ngày âm trên lịch tháng",
                            isOn: $vm.showLunar
                        )

                        SettingDivider()

                        ToggleItem(
                            iconBg: Color(hex: "E8F5E9"), iconColor: Color(hex: "2E7D32"),
                            icon: "checkmark.seal.fill", title: "Ngày hoàng đạo",
                            desc: "Đánh dấu ngày tốt/xấu trên lịch",
                            isOn: $vm.showHoangDao
                        )

                        SettingDivider()

                        ToggleItem(
                            iconBg: Color(hex: "FFF3E0"), iconColor: Color(hex: "E65100"),
                            icon: "party.popper.fill", title: "Ngày lễ / sự kiện",
                            desc: "Hiển thị ngày lễ Việt Nam & quốc tế",
                            isOn: $vm.showFestivals
                        )

                        SettingDivider()

                        ToggleItem(
                            iconBg: Color(hex: "E3F2FD"), iconColor: Color(hex: "1565C0"),
                            icon: "text.quote", title: "Câu danh ngôn",
                            desc: "Hiện câu nói hay mỗi ngày",
                            isOn: $vm.showQuote
                        )
                    }

                    // ── THÔNG BÁO ──
                    SectionTitle("Thông báo")

                    SettingsGroup {
                        ToggleItem(
                            iconBg: Color(hex: "FFF8E1"), iconColor: Color(hex: "F57F17"),
                            icon: "bell.fill", title: "Nhắc nhở hàng ngày",
                            desc: "Thông báo thông tin ngày mới",
                            isOn: $vm.dailyReminder
                        )

                        SettingDivider()

                        ArrowItem(
                            iconBg: Color(hex: "FFEBEE"), iconColor: Color(hex: "C62828"),
                            icon: "clock.fill", title: "Giờ nhắc nhở",
                            desc: "Thời gian nhận thông báo",
                            value: vm.reminderTimeString
                        ) { showTimePicker = true }

                        SettingDivider()

                        ToggleItem(
                            iconBg: Color(hex: "E8F5E9"), iconColor: Color(hex: "2E7D32"),
                            icon: "party.popper.fill", title: "Nhắc ngày lễ",
                            desc: "Báo trước 1 ngày trước lễ",
                            isOn: $vm.festivalReminder
                        )
                    }

                    // ── VỊ TRÍ & THỜI TIẾT ──
                    SectionTitle("Vị trí & Thời tiết")

                    SettingsGroup {
                        ArrowItem(
                            iconBg: Color(hex: "E3F2FD"), iconColor: Color(hex: "1565C0"),
                            icon: "location.fill", title: "Vị trí",
                            desc: "Dùng cho thời tiết & giờ mặt trời",
                            value: vm.location
                        ) { }

                        SettingDivider()

                        ArrowItem(
                            iconBg: Color(hex: "E0F2F1"), iconColor: Color(hex: "00695C"),
                            icon: "thermometer.medium", title: "Đơn vị nhiệt độ",
                            desc: "Celsius hoặc Fahrenheit",
                            value: vm.tempUnit
                        ) { showTempUnitPicker = true }
                    }

                    // ── THÔNG TIN ──
                    SectionTitle("Thông tin")

                    SettingsGroup {
                        ArrowItem(
                            iconBg: Color(hex: "F5F5F5"), iconColor: Color(hex: "616161"),
                            icon: "info.circle.fill", title: "Về ứng dụng"
                        ) { showAbout = true }

                        SettingDivider()

                        ArrowItem(
                            iconBg: Color(hex: "F5F5F5"), iconColor: Color(hex: "616161"),
                            icon: "star.fill", title: "Đánh giá ứng dụng"
                        ) {
                            if let url = URL(string: "https://apps.apple.com/app/id0000000000") {
                                UIApplication.shared.open(url)
                            }
                        }

                        SettingDivider()

                        ArrowItem(
                            iconBg: Color(hex: "F5F5F5"), iconColor: Color(hex: "616161"),
                            icon: "lock.shield.fill", title: "Chính sách bảo mật"
                        ) {
                            if let url = URL(string: "https://lichso.com/privacy") {
                                UIApplication.shared.open(url)
                            }
                        }
                    }

                    // ── VERSION ──
                    VStack(spacing: 4) {
                        Text("🗓️")
                            .font(.system(size: 28))
                        Text("Lịch Vạn Niên Việt Nam v2.1.0")
                            .font(.system(size: 12))
                            .foregroundColor(TextDim)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 24)

                    Spacer().frame(height: 32)
                }
                .padding(.horizontal, 16)
            }
        }
        .background(SurfaceBg)
        .navigationBarHidden(true)

        // ═══ SHEETS / DIALOGS ═══
        .sheet(isPresented: $showWeekStartPicker) {
            OptionPickerSheet(
                title: "Ngày bắt đầu tuần",
                options: vm.weekStartOptions,
                selected: vm.weekStart
            ) { vm.weekStart = $0 }
                .presentationDetents([.height(220)])
        }
        .sheet(isPresented: $showThemePicker) {
            OptionPickerSheet(
                title: "Giao diện",
                options: vm.themeOptions,
                selected: vm.theme
            ) { vm.theme = $0 }
                .presentationDetents([.height(260)])
        }
        .sheet(isPresented: $showTempUnitPicker) {
            OptionPickerSheet(
                title: "Đơn vị nhiệt độ",
                options: vm.tempUnitOptions,
                selected: vm.tempUnit
            ) { vm.tempUnit = $0 }
                .presentationDetents([.height(200)])
        }
        .sheet(isPresented: $showTimePicker) {
            TimePickerSheet(hour: $vm.reminderHour, minute: $vm.reminderMinute)
                .presentationDetents([.height(300)])
        }
        .sheet(isPresented: $showAbout) {
            AboutSheet()
                .presentationDetents([.medium])
        }
    }
}

// ══════════════════════════════════════════
// TOP BAR
// ══════════════════════════════════════════

private struct TopBar: View {
    let onBack: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Button(action: onBack) {
                Image(systemName: "arrow.left")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(TextMain)
                    .frame(width: 40, height: 40)
                    .contentShape(Rectangle())
            }

            Text("Cài đặt")
                .font(.system(size: 22, weight: .bold))
                .foregroundColor(TextMain)

            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.top, 8)
        .padding(.bottom, 16)
    }
}

// ══════════════════════════════════════════
// SECTION TITLE
// ══════════════════════════════════════════

private struct SectionTitle: View {
    let text: String
    init(_ text: String) { self.text = text }

    var body: some View {
        Text(text.uppercased())
            .font(.system(size: 12, weight: .bold))
            .foregroundColor(PrimaryRed)
            .tracking(0.8)
            .padding(.top, 20)
            .padding(.bottom, 10)
            .padding(.leading, 4)
    }
}

// ══════════════════════════════════════════
// SETTINGS GROUP
// ══════════════════════════════════════════

private struct SettingsGroup<Content: View>: View {
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(spacing: 0) {
            content()
        }
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(OutlineVariant, lineWidth: 1)
        )
        .padding(.bottom, 12)
    }
}

private struct SettingDivider: View {
    var body: some View {
        Rectangle()
            .fill(OutlineVariant)
            .frame(height: 0.5)
            .padding(.leading, 68)
    }
}

// ══════════════════════════════════════════
// SETTING ITEMS
// ══════════════════════════════════════════

private struct ToggleItem: View {
    let iconBg: Color
    let iconColor: Color
    let icon: String
    let title: String
    let desc: String
    @Binding var isOn: Bool

    var body: some View {
        HStack(spacing: 14) {
            IconWrap(bg: iconBg, fg: iconColor, icon: icon)

            VStack(alignment: .leading, spacing: 1) {
                Text(title)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(TextMain)
                Text(desc)
                    .font(.system(size: 11))
                    .foregroundColor(TextDim)
            }

            Spacer()

            Toggle("", isOn: $isOn)
                .tint(PrimaryRed)
                .labelsHidden()
        }
        .padding(.horizontal, 18)
        .padding(.vertical, 16)
    }
}

private struct ArrowItem: View {
    let iconBg: Color
    let iconColor: Color
    let icon: String
    let title: String
    var desc: String? = nil
    var value: String? = nil
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 14) {
                IconWrap(bg: iconBg, fg: iconColor, icon: icon)

                VStack(alignment: .leading, spacing: 1) {
                    Text(title)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(TextMain)
                    if let desc = desc {
                        Text(desc)
                            .font(.system(size: 11))
                            .foregroundColor(TextDim)
                    }
                }

                Spacer()

                if let value = value {
                    Text(value)
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(PrimaryRed)
                }

                Image(systemName: "chevron.right")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(OutlineVariant)
            }
            .padding(.horizontal, 18)
            .padding(.vertical, 16)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

private struct IconWrap: View {
    let bg: Color
    let fg: Color
    let icon: String

    var body: some View {
        Image(systemName: icon)
            .font(.system(size: 16))
            .foregroundColor(fg)
            .frame(width: 36, height: 36)
            .background(bg)
            .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}

// ══════════════════════════════════════════
// OPTION PICKER SHEET
// ══════════════════════════════════════════

private struct OptionPickerSheet: View {
    @Environment(\.dismiss) private var dismiss
    let title: String
    let options: [String]
    let selected: String
    let onSelect: (String) -> Void

    var body: some View {
        VStack(spacing: 0) {
            // Drag handle
            Capsule()
                .fill(Color.gray.opacity(0.3))
                .frame(width: 36, height: 4)
                .padding(.top, 10)

            Text(title)
                .font(.system(size: 17, weight: .bold))
                .foregroundColor(TextMain)
                .padding(.top, 16)
                .padding(.bottom, 12)

            VStack(spacing: 0) {
                ForEach(options, id: \.self) { option in
                    Button {
                        onSelect(option)
                        dismiss()
                    } label: {
                        HStack {
                            Text(option)
                                .font(.system(size: 15))
                                .foregroundColor(TextMain)
                            Spacer()
                            if option == selected {
                                Image(systemName: "checkmark.circle.fill")
                                    .font(.system(size: 20))
                                    .foregroundColor(PrimaryRed)
                            }
                        }
                        .padding(.horizontal, 24)
                        .padding(.vertical, 14)
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)

                    if option != options.last {
                        Divider().padding(.leading, 24)
                    }
                }
            }

            Spacer()
        }
        .background(SurfaceBg)
    }
}

// ══════════════════════════════════════════
// TIME PICKER SHEET
// ══════════════════════════════════════════

private struct TimePickerSheet: View {
    @Environment(\.dismiss) private var dismiss
    @Binding var hour: Int
    @Binding var minute: Int
    @State private var date = Date()

    var body: some View {
        VStack(spacing: 0) {
            Capsule()
                .fill(Color.gray.opacity(0.3))
                .frame(width: 36, height: 4)
                .padding(.top, 10)

            Text("Giờ nhắc nhở")
                .font(.system(size: 17, weight: .bold))
                .foregroundColor(TextMain)
                .padding(.top, 16)

            DatePicker("", selection: $date, displayedComponents: .hourAndMinute)
                .datePickerStyle(.wheel)
                .labelsHidden()
                .environment(\.locale, Locale(identifier: "vi_VN"))
                .padding(.horizontal)

            Button {
                let components = Calendar.current.dateComponents([.hour, .minute], from: date)
                hour = components.hour ?? 7
                minute = components.minute ?? 0
                dismiss()
            } label: {
                Text("Xác nhận")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(PrimaryRed)
                    .cornerRadius(12)
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 16)
        }
        .background(SurfaceBg)
        .onAppear {
            var components = DateComponents()
            components.hour = hour
            components.minute = minute
            date = Calendar.current.date(from: components) ?? Date()
        }
    }
}

// ══════════════════════════════════════════
// ABOUT SHEET
// ══════════════════════════════════════════

private struct AboutSheet: View {
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(spacing: 16) {
            Capsule()
                .fill(Color.gray.opacity(0.3))
                .frame(width: 36, height: 4)
                .padding(.top, 10)

            Text("🗓️")
                .font(.system(size: 56))
                .padding(.top, 8)

            Text("Lịch Vạn Niên Việt Nam")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(TextMain)

            Text("Phiên bản 2.1.0")
                .font(.system(size: 14))
                .foregroundColor(TextDim)

            VStack(alignment: .leading, spacing: 8) {
                AboutRow(icon: "calendar.badge.clock", text: "Lịch âm dương chính xác")
                AboutRow(icon: "sparkles", text: "Phong thủy & Hoàng đạo")
                AboutRow(icon: "brain.head.profile", text: "Trợ lý AI thông minh")
                AboutRow(icon: "bell.badge.fill", text: "Nhắc nhở thông minh")
                AboutRow(icon: "note.text", text: "Ghi chú & Công việc")
            }
            .padding(.horizontal, 24)
            .padding(.top, 8)

            Spacer()

            Text("© 2024 Lịch Số. All rights reserved.")
                .font(.system(size: 11))
                .foregroundColor(TextDim)
                .padding(.bottom, 16)
        }
        .background(SurfaceBg)
    }
}

private struct AboutRow: View {
    let icon: String
    let text: String

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundColor(PrimaryRed)
                .frame(width: 24)
            Text(text)
                .font(.system(size: 14))
                .foregroundColor(TextMain)
        }
    }
}

#Preview {
    NavigationStack {
        SettingsScreen()
    }
}
