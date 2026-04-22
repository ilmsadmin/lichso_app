import SwiftUI
import UserNotifications

// ═══════════════════════════════════════════
// Settings Screen — Cài đặt
// Sections: Chung | Hiển thị | Thông báo
//           Đồng bộ & Lưu trữ | Thông tin
// ═══════════════════════════════════════════

struct SettingsScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme
    @StateObject private var vm = SettingsViewModel()

    // Sheet states
    @State private var showWeekStartPicker = false
    @State private var showThemePicker     = false
    @State private var showTimePicker      = false
    @State private var showTempUnitPicker  = false
    @State private var showLocationPicker  = false
    @State private var showAbout           = false
    @State private var showNotifDenied     = false

    private var cs: SettingsColorScheme { SettingsColorScheme(isDark: colorScheme == .dark) }

    var body: some View {
        VStack(spacing: 0) {

            // ── TOP BAR ──
            SettingsTopBar(cs: cs, onBack: { dismiss() })

            // ── CONTENT ──
            ScrollView(.vertical, showsIndicators: false) {
                VStack(alignment: .leading, spacing: 0) {

                    // ────────────────────────────────────
                    // CHUNG
                    // ────────────────────────────────────
                    SectionLabel("Chung", cs: cs)

                    SettingsCard(cs: cs) {
                        ArrowRow(cs: cs,
                                 iconBg: Color(hex: "FFEBEE"), iconFg: Color(hex: "C62828"),
                                 icon: "calendar",
                                 title: "Ngày bắt đầu tuần",
                                 subtitle: "Ngày đầu tiên của tuần",
                                 value: vm.weekStart) {
                            showWeekStartPicker = true
                        }

                        RowDivider(cs: cs)

                        ArrowRow(cs: cs,
                                 iconBg: Color(hex: "F3E5F5"), iconFg: Color(hex: "7B1FA2"),
                                 icon: "paintpalette.fill",
                                 title: "Giao diện",
                                 subtitle: "Sáng / Tối / Theo hệ thống",
                                 value: vm.theme) {
                            showThemePicker = true
                        }
                    }

                    // ────────────────────────────────────
                    // HIỂN THỊ
                    // ────────────────────────────────────
                    SectionLabel("Hiển thị", cs: cs)

                    SettingsCard(cs: cs) {
                        ToggleRow(cs: cs,
                                  iconBg: Color(hex: "E0F2F1"), iconFg: Color(hex: "00695C"),
                                  icon: "moon.fill",
                                  title: "Hiển thị âm lịch",
                                  subtitle: "Hiện ngày âm trên lịch tháng",
                                  isOn: $vm.showLunar)

                        RowDivider(cs: cs)

                        ToggleRow(cs: cs,
                                  iconBg: Color(hex: "E8F5E9"), iconFg: Color(hex: "2E7D32"),
                                  icon: "checkmark.seal.fill",
                                  title: "Ngày hoàng đạo",
                                  subtitle: "Đánh dấu ngày tốt/xấu trên lịch",
                                  isOn: $vm.showHoangDao)

                        RowDivider(cs: cs)

                        ToggleRow(cs: cs,
                                  iconBg: Color(hex: "FFF3E0"), iconFg: Color(hex: "E65100"),
                                  icon: "party.popper.fill",
                                  title: "Ngày lễ / sự kiện",
                                  subtitle: "Hiển thị ngày lễ Việt Nam & quốc tế",
                                  isOn: $vm.showFestivals)

                        RowDivider(cs: cs)

                        ToggleRow(cs: cs,
                                  iconBg: Color(hex: "E3F2FD"), iconFg: Color(hex: "1565C0"),
                                  icon: "text.quote",
                                  title: "Câu danh ngôn",
                                  subtitle: "Hiện câu nói hay mỗi ngày",
                                  isOn: $vm.showQuote)
                    }

                    // ────────────────────────────────────
                    // THÔNG BÁO
                    // ────────────────────────────────────
                    SectionLabel("Thông báo", cs: cs)

                    // Permission banner
                    if vm.notificationPermission == .denied {
                        NotifPermissionBanner(cs: cs) { showNotifDenied = true }
                    }

                    SettingsCard(cs: cs) {
                        ToggleRow(cs: cs,
                                  iconBg: Color(hex: "FFF8E1"), iconFg: Color(hex: "F57F17"),
                                  icon: "bell.fill",
                                  title: "Nhắc nhở hàng ngày",
                                  subtitle: "Thông báo thông tin ngày mới",
                                  isOn: $vm.dailyReminder,
                                  disabled: vm.notificationPermission == .denied) {
                            if vm.notificationPermission == .notDetermined {
                                Task { _ = await vm.requestNotificationPermission() }
                            }
                        }

                        RowDivider(cs: cs)

                        ArrowRow(cs: cs,
                                 iconBg: Color(hex: "FFEBEE"), iconFg: Color(hex: "C62828"),
                                 icon: "clock.fill",
                                 title: "Giờ nhắc nhở",
                                 subtitle: "Thời gian nhận thông báo",
                                 value: vm.reminderTimeString,
                                 dimmed: !vm.dailyReminder || vm.notificationPermission == .denied) {
                            if vm.dailyReminder && vm.notificationPermission == .authorized {
                                showTimePicker = true
                            }
                        }

                        RowDivider(cs: cs)

                        ToggleRow(cs: cs,
                                  iconBg: Color(hex: "E8F5E9"), iconFg: Color(hex: "2E7D32"),
                                  icon: "party.popper.fill",
                                  title: "Nhắc ngày lễ",
                                  subtitle: "Báo trước 1 ngày trước lễ",
                                  isOn: $vm.festivalReminder,
                                  disabled: vm.notificationPermission == .denied)
                    }

                    // ────────────────────────────────────
                    // ĐỒNG BỘ & LƯU TRỮ
                    // ────────────────────────────────────
                    SectionLabel("Đồng bộ & Lưu trữ", cs: cs)

                    SettingsCard(cs: cs) {
                        ToggleRow(cs: cs,
                                  iconBg: Color(hex: "E3F2FD"), iconFg: Color(hex: "1565C0"),
                                  icon: "icloud.fill",
                                  title: "Đồng bộ iCloud",
                                  subtitle: vm.iCloudAvailable
                                      ? "Sao lưu cài đặt lên iCloud"
                                      : "iCloud chưa đăng nhập",
                                  isOn: $vm.iCloudSync,
                                  disabled: !vm.iCloudAvailable)

                        RowDivider(cs: cs)

                        ArrowRow(cs: cs,
                                 iconBg: Color(hex: "E3F2FD"), iconFg: Color(hex: "1565C0"),
                                 icon: "location.fill",
                                 title: "Vị trí",
                                 subtitle: "Dùng cho thời tiết & giờ mặt trời",
                                 value: vm.location) {
                            showLocationPicker = true
                        }

                        RowDivider(cs: cs)

                        ArrowRow(cs: cs,
                                 iconBg: Color(hex: "E0F2F1"), iconFg: Color(hex: "00695C"),
                                 icon: "thermometer.medium",
                                 title: "Đơn vị nhiệt độ",
                                 subtitle: "Celsius hoặc Fahrenheit",
                                 value: vm.tempUnit) {
                            showTempUnitPicker = true
                        }
                    }

                    // ────────────────────────────────────
                    // THÔNG TIN
                    // ────────────────────────────────────
                    SectionLabel("Thông tin", cs: cs)

                    SettingsCard(cs: cs) {
                        ArrowRow(cs: cs,
                                 iconBg: Color(hex: "F5F5F5"), iconFg: Color(hex: "616161"),
                                 icon: "info.circle.fill",
                                 title: "Về ứng dụng") {
                            showAbout = true
                        }

                        RowDivider(cs: cs)

                        ArrowRow(cs: cs,
                                 iconBg: Color(hex: "FFF8E1"), iconFg: Color(hex: "F57F17"),
                                 icon: "star.fill",
                                 title: "Đánh giá trên App Store") {
                            SmartRatingManager.shared.triggerManually()
                        }

                        RowDivider(cs: cs)

                        ArrowRow(cs: cs,
                                 iconBg: Color(hex: "E3F2FD"), iconFg: Color(hex: "1565C0"),
                                 icon: "lock.shield.fill",
                                 title: "Chính sách bảo mật") {
                            if let url = URL(string: "https://apps.zenix.vn/privacy-policy") {
                                UIApplication.shared.open(url)
                            }
                        }

                        RowDivider(cs: cs)

                        ArrowRow(cs: cs,
                                 iconBg: Color(hex: "FFEBEE"), iconFg: Color(hex: "C62828"),
                                 icon: "envelope.fill",
                                 title: "Liên hệ hỗ trợ") {
                            if let url = URL(string: "mailto:zenixhq.com@gmail.com") {
                                UIApplication.shared.open(url)
                            }
                        }
                    }

                    // ── VERSION FOOTER ──
                    VersionFooter(version: vm.appVersion, cs: cs)

                    Spacer().frame(height: 40)
                }
                .padding(.horizontal, 16)
            }
        }
        .background(cs.bg.ignoresSafeArea())
        .navigationBarHidden(true)
        .task { await vm.checkNotificationPermission() }
        // Push to iCloud whenever any setting changes
        .onChange(of: vm.weekStart)      { _, _ in vm.pushToICloud() }
        .onChange(of: vm.theme)          { _, _ in vm.pushToICloud() }
        .onChange(of: vm.showLunar)      { _, _ in vm.pushToICloud() }
        .onChange(of: vm.showHoangDao)   { _, _ in vm.pushToICloud() }
        .onChange(of: vm.showFestivals)  { _, _ in vm.pushToICloud() }
        .onChange(of: vm.showQuote)      { _, _ in vm.pushToICloud() }
        .onChange(of: vm.location)       { _, _ in vm.pushToICloud() }
        .onChange(of: vm.tempUnit)       { _, _ in vm.pushToICloud() }
        .onChange(of: vm.reminderHour)   { _, _ in vm.rescheduleNotifications(); vm.pushToICloud() }
        .onChange(of: vm.reminderMinute) { _, _ in vm.rescheduleNotifications(); vm.pushToICloud() }

        // ══════ SHEETS ══════

        .sheet(isPresented: $showWeekStartPicker) {
            OptionPickerSheet(
                title: "Ngày bắt đầu tuần",
                options: vm.weekStartOptions,
                selected: vm.weekStart,
                cs: cs
            ) { vm.weekStart = $0 }
            .presentationDetents([.height(200)])
            .presentationDragIndicator(.visible)
        }

        .sheet(isPresented: $showThemePicker) {
            OptionPickerSheet(
                title: "Giao diện",
                options: vm.themeOptions,
                selected: vm.theme,
                cs: cs
            ) { vm.theme = $0 }
            .presentationDetents([.height(240)])
            .presentationDragIndicator(.visible)
        }

        .sheet(isPresented: $showTempUnitPicker) {
            OptionPickerSheet(
                title: "Đơn vị nhiệt độ",
                options: vm.tempUnitOptions,
                selected: vm.tempUnit,
                cs: cs
            ) { vm.tempUnit = $0 }
            .presentationDetents([.height(190)])
            .presentationDragIndicator(.visible)
        }

        .sheet(isPresented: $showTimePicker) {
            TimePickerSheet(
                hour: $vm.reminderHour,
                minute: $vm.reminderMinute,
                cs: cs
            ) { vm.rescheduleNotifications() }
            .presentationDetents([.height(310)])
            .presentationDragIndicator(.visible)
        }

        .sheet(isPresented: $showLocationPicker) {
            LocationPickerSheet(
                options: vm.locationOptions,
                selected: vm.location,
                cs: cs
            ) { vm.location = $0 }
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.visible)
        }

        .sheet(isPresented: $showAbout) {
            AboutSheet(version: vm.appVersion, cs: cs)
                .presentationDetents([.medium])
                .presentationDragIndicator(.visible)
        }

        .alert("Thông báo bị tắt", isPresented: $showNotifDenied) {
            Button("Mở Cài đặt") {
                if let url = URL(string: UIApplication.openSettingsURLString) {
                    UIApplication.shared.open(url)
                }
            }
            Button("Bỏ qua", role: .cancel) {}
        } message: {
            Text("Vui lòng mở Cài đặt > Lịch Số và bật Thông báo để nhận nhắc nhở.")
        }
    }
}

// ══════════════════════════════════════════
// COLOR SCHEME HELPER
// ══════════════════════════════════════════

private struct SettingsColorScheme {
    let isDark: Bool

    var bg: Color { isDark ? Color(hex: "0F0E0C") : Color(hex: "FFFBF5") }
    var surface: Color { isDark ? Color(hex: "1A1814") : Color(hex: "FFF8F0") }
    var surfaceCard: Color { isDark ? Color(hex: "1A1814") : Color(hex: "FFF8F0") }
    var primary: Color { isDark ? Color(hex: "EF5350") : Color(hex: "B71C1C") }
    var textMain: Color { isDark ? Color(hex: "F0E8D0") : Color(hex: "1C1B1F") }
    var textSub: Color { isDark ? Color(hex: "B8AA88") : Color(hex: "534340") }
    var textDim: Color { isDark ? Color(hex: "8A7E62") : Color(hex: "857371") }
    var divider: Color { isDark ? Color(hex: "3A3428") : Color(hex: "D8C2BF") }
    var cardBorder: Color { isDark ? Color(hex: "3A3428") : Color(hex: "D8C2BF").opacity(0.5) }
    var toggleTint: Color { isDark ? Color(hex: "EF5350") : Color(hex: "B71C1C") }
    var iconBgFallback: Color { isDark ? Color(hex: "2A2720") : Color(hex: "F5F5F5") }
}

// ══════════════════════════════════════════
// TOP BAR
// ══════════════════════════════════════════

private struct SettingsTopBar: View {
    let cs: SettingsColorScheme
    let onBack: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Button(action: onBack) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(.white)
                    .frame(width: 40, height: 40)
                    .background(Color.white.opacity(0.15))
                    .clipShape(Circle())
            }
            Text("Cài đặt")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.white)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.top, 8)
        .padding(.bottom, 16)
        .background(
            LinearGradient(
                colors: [Color(red: 0.773, green: 0.157, blue: 0.157),
                         Color(red: 0.545, green: 0, blue: 0)],
                startPoint: .topLeading, endPoint: .bottomTrailing
            )
            .ignoresSafeArea(edges: .top)
        )
    }
}

// ══════════════════════════════════════════
// SECTION LABEL
// ══════════════════════════════════════════

private struct SectionLabel: View {
    let text: String
    let cs: SettingsColorScheme
    init(_ text: String, cs: SettingsColorScheme) {
        self.text = text
        self.cs = cs
    }

    var body: some View {
        Text(text.uppercased())
            .font(.system(size: 12, weight: .bold))
            .foregroundColor(cs.primary)
            .tracking(0.8)
            .padding(.top, 20)
            .padding(.bottom, 10)
            .padding(.leading, 4)
    }
}

// ══════════════════════════════════════════
// SETTINGS CARD
// ══════════════════════════════════════════

private struct SettingsCard<Content: View>: View {
    let cs: SettingsColorScheme
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(spacing: 0) {
            content()
        }
        .background(cs.surfaceCard)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(cs.cardBorder, lineWidth: 1)
        )
        .padding(.bottom, 8)
    }
}

// ══════════════════════════════════════════
// ROW DIVIDER
// ══════════════════════════════════════════

private struct RowDivider: View {
    let cs: SettingsColorScheme
    var body: some View {
        Rectangle()
            .fill(cs.divider.opacity(0.5))
            .frame(height: 0.5)
            .padding(.leading, 68)
    }
}

// ══════════════════════════════════════════
// ICON WRAP
// ══════════════════════════════════════════

private struct IconWrap: View {
    let bg: Color
    let fg: Color
    let icon: String

    var body: some View {
        Image(systemName: icon)
            .font(.system(size: 17))
            .foregroundColor(fg)
            .frame(width: 36, height: 36)
            .background(bg)
            .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}

// ══════════════════════════════════════════
// TOGGLE ROW
// ══════════════════════════════════════════

private struct ToggleRow: View {
    let cs: SettingsColorScheme
    let iconBg: Color
    let iconFg: Color
    let icon: String
    let title: String
    let subtitle: String
    @Binding var isOn: Bool
    var disabled: Bool = false
    var onBeforeToggle: (() -> Void)? = nil

    var body: some View {
        HStack(spacing: 14) {
            IconWrap(bg: iconBg, fg: iconFg, icon: icon)
                .opacity(disabled ? 0.45 : 1)

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(disabled ? cs.textDim : cs.textMain)
                Text(subtitle)
                    .font(.system(size: 11))
                    .foregroundColor(cs.textDim)
            }

            Spacer()

            Toggle("", isOn: $isOn)
                .tint(cs.toggleTint)
                .labelsHidden()
                .disabled(disabled)
                .opacity(disabled ? 0.45 : 1)
                .onChange(of: isOn) { _, _ in
                    onBeforeToggle?()
                }
        }
        .padding(.horizontal, 18)
        .padding(.vertical, 15)
    }
}

// ══════════════════════════════════════════
// ARROW ROW
// ══════════════════════════════════════════

private struct ArrowRow: View {
    let cs: SettingsColorScheme
    let iconBg: Color
    let iconFg: Color
    let icon: String
    let title: String
    var subtitle: String? = nil
    var value: String? = nil
    var dimmed: Bool = false
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 14) {
                IconWrap(bg: iconBg, fg: iconFg, icon: icon)
                    .opacity(dimmed ? 0.45 : 1)

                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(dimmed ? cs.textDim : cs.textMain)
                    if let subtitle {
                        Text(subtitle)
                            .font(.system(size: 11))
                            .foregroundColor(cs.textDim)
                    }
                }

                Spacer()

                if let value {
                    Text(value)
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(dimmed ? cs.textDim : cs.primary)
                }

                Image(systemName: "chevron.right")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(cs.divider)
            }
            .padding(.horizontal, 18)
            .padding(.vertical, 15)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

// ══════════════════════════════════════════
// NOTIFICATION PERMISSION BANNER
// ══════════════════════════════════════════

private struct NotifPermissionBanner: View {
    let cs: SettingsColorScheme
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                Image(systemName: "bell.slash.fill")
                    .font(.system(size: 16))
                    .foregroundColor(.white)

                VStack(alignment: .leading, spacing: 2) {
                    Text("Thông báo chưa được bật")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(.white)
                    Text("Nhấn để mở Cài đặt và bật thông báo")
                        .font(.system(size: 11))
                        .foregroundColor(.white.opacity(0.85))
                }

                Spacer()

                Image(systemName: "arrow.up.right")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(.white.opacity(0.7))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(Color(hex: "B71C1C").opacity(0.88))
            .clipShape(RoundedRectangle(cornerRadius: 14))
        }
        .buttonStyle(.plain)
        .padding(.bottom, 8)
    }
}

// ══════════════════════════════════════════
// VERSION FOOTER
// ══════════════════════════════════════════

private struct VersionFooter: View {
    let version: String
    let cs: SettingsColorScheme

    var body: some View {
        VStack(spacing: 6) {
            Image("AppLogo")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 30, height: 30)
            Text("Lịch Số · Lịch Vạn Niên Việt Nam")
                .font(.system(size: 13, weight: .medium))
                .foregroundColor(cs.textDim)
            Text("Phiên bản \(version)")
                .font(.system(size: 12))
                .foregroundColor(cs.textDim.opacity(0.7))
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 28)
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
    let cs: SettingsColorScheme
    let onSelect: (String) -> Void

    var body: some View {
        VStack(spacing: 0) {
            Text(title)
                .font(.system(size: 17, weight: .bold))
                .foregroundColor(cs.textMain)
                .padding(.top, 24)
                .padding(.bottom, 16)

            VStack(spacing: 0) {
                ForEach(options, id: \.self) { option in
                    Button {
                        onSelect(option)
                        dismiss()
                    } label: {
                        HStack {
                            Text(option)
                                .font(.system(size: 15))
                                .foregroundColor(cs.textMain)
                            Spacer()
                            if option == selected {
                                Image(systemName: "checkmark.circle.fill")
                                    .font(.system(size: 20))
                                    .foregroundColor(cs.primary)
                            }
                        }
                        .padding(.horizontal, 24)
                        .padding(.vertical, 15)
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)

                    if option != options.last {
                        Divider()
                            .overlay(cs.divider.opacity(0.5))
                            .padding(.leading, 24)
                    }
                }
            }
            .background(cs.surfaceCard)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(cs.cardBorder, lineWidth: 1)
            )
            .padding(.horizontal, 20)

            Spacer()
        }
        .background(cs.bg.ignoresSafeArea())
    }
}

// ══════════════════════════════════════════
// TIME PICKER SHEET
// ══════════════════════════════════════════

private struct TimePickerSheet: View {
    @Environment(\.dismiss) private var dismiss
    @Binding var hour: Int
    @Binding var minute: Int
    let cs: SettingsColorScheme
    let onConfirm: () -> Void
    @State private var date = Date()

    var body: some View {
        VStack(spacing: 0) {
            Text("Giờ nhắc nhở")
                .font(.system(size: 17, weight: .bold))
                .foregroundColor(cs.textMain)
                .padding(.top, 24)
                .padding(.bottom, 4)

            DatePicker("", selection: $date, displayedComponents: .hourAndMinute)
                .datePickerStyle(.wheel)
                .labelsHidden()
                .environment(\.locale, Locale(identifier: "vi_VN"))
                .colorScheme(cs.isDark ? .dark : .light)
                .padding(.horizontal, 20)

            Button {
                let comps = Calendar.current.dateComponents([.hour, .minute], from: date)
                hour   = comps.hour   ?? 7
                minute = comps.minute ?? 0
                onConfirm()
                dismiss()
            } label: {
                Text("Xác nhận")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(cs.primary)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 20)
        }
        .background(cs.bg.ignoresSafeArea())
        .onAppear {
            var comps = DateComponents()
            comps.hour   = hour
            comps.minute = minute
            date = Calendar.current.date(from: comps) ?? Date()
        }
    }
}

// ══════════════════════════════════════════
// LOCATION PICKER SHEET
// ══════════════════════════════════════════

private struct LocationPickerSheet: View {
    @Environment(\.dismiss) private var dismiss
    let options: [String]
    let selected: String
    let cs: SettingsColorScheme
    let onSelect: (String) -> Void

    @State private var searchText = ""

    private var filtered: [String] {
        searchText.isEmpty ? options : options.filter {
            $0.localizedCaseInsensitiveContains(searchText)
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Text("Chọn vị trí")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundColor(cs.textMain)
                Spacer()
                Button {
                    dismiss()
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .font(.system(size: 22))
                        .foregroundColor(cs.textDim)
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 24)
            .padding(.bottom, 12)

            // Search bar
            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 15))
                    .foregroundColor(cs.textDim)
                TextField("Tìm thành phố...", text: $searchText)
                    .font(.system(size: 14))
                    .foregroundColor(cs.textMain)
                    .autocorrectionDisabled()
                if !searchText.isEmpty {
                    Button { searchText = "" } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(cs.textDim)
                    }
                }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background(cs.surfaceCard)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(cs.cardBorder, lineWidth: 1)
            )
            .padding(.horizontal, 20)
            .padding(.bottom, 12)

            // List
            ScrollView {
                LazyVStack(spacing: 0) {
                    ForEach(filtered, id: \.self) { city in
                        Button {
                            onSelect(city)
                            dismiss()
                        } label: {
                            HStack {
                                Image(systemName: "location.fill")
                                    .font(.system(size: 13))
                                    .foregroundColor(cs.primary.opacity(0.7))
                                    .frame(width: 20)

                                Text(city)
                                    .font(.system(size: 15))
                                    .foregroundColor(cs.textMain)

                                Spacer()

                                if city == selected {
                                    Image(systemName: "checkmark.circle.fill")
                                        .font(.system(size: 18))
                                        .foregroundColor(cs.primary)
                                }
                            }
                            .padding(.horizontal, 20)
                            .padding(.vertical, 14)
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)

                        if city != filtered.last {
                            Divider()
                                .overlay(cs.divider.opacity(0.4))
                                .padding(.leading, 52)
                        }
                    }
                }
            }
            .background(cs.bg)
        }
        .background(cs.bg.ignoresSafeArea())
    }
}

// ══════════════════════════════════════════
// ABOUT SHEET
// ══════════════════════════════════════════

private struct AboutSheet: View {
    @Environment(\.dismiss) private var dismiss
    let version: String
    let cs: SettingsColorScheme

    var body: some View {
        VStack(spacing: 0) {
            // Close button
            HStack {
                Spacer()
                Button { dismiss() } label: {
                    Image(systemName: "xmark.circle.fill")
                        .font(.system(size: 24))
                        .foregroundColor(cs.textDim)
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 20)

            ScrollView {
                VStack(spacing: 16) {
                    // App icon
                    Text("🗓️")
                        .font(.system(size: 60))
                        .padding(.top, 8)

                    VStack(spacing: 6) {
                        Text("Lịch Số")
                            .font(.system(size: 24, weight: .bold))
                            .foregroundColor(cs.textMain)
                        Text("Lịch Vạn Niên Việt Nam")
                            .font(.system(size: 15))
                            .foregroundColor(cs.textSub)
                        Text("Phiên bản \(version)")
                            .font(.system(size: 13))
                            .foregroundColor(cs.textDim)
                            .padding(.top, 2)
                    }

                    // Divider
                    Rectangle()
                        .fill(cs.divider.opacity(0.4))
                        .frame(height: 1)
                        .padding(.horizontal, 40)
                        .padding(.vertical, 4)

                    // Features
                    VStack(alignment: .leading, spacing: 12) {
                        AboutFeatureRow(cs: cs, icon: "calendar.badge.clock",
                                        text: "Lịch âm dương chính xác")
                        AboutFeatureRow(cs: cs, icon: "sparkles",
                                        text: "Phong thủy & Ngày hoàng đạo")
                        AboutFeatureRow(cs: cs, icon: "brain.head.profile",
                                        text: "Trợ lý AI thông minh")
                        AboutFeatureRow(cs: cs, icon: "bell.badge.fill",
                                        text: "Nhắc nhở thông minh")
                        AboutFeatureRow(cs: cs, icon: "note.text",
                                        text: "Ghi chú & Công việc")
                        AboutFeatureRow(cs: cs, icon: "person.2.fill",
                                        text: "Gia phả & Kỷ niệm gia đình")
                    }
                    .padding(.horizontal, 32)

                    Spacer().frame(height: 12)

                    Text("© 2024–2026 Lịch Số. All rights reserved.")
                        .font(.system(size: 11))
                        .foregroundColor(cs.textDim)
                        .padding(.bottom, 8)
                }
            }
        }
        .background(cs.bg.ignoresSafeArea())
    }
}

private struct AboutFeatureRow: View {
    let cs: SettingsColorScheme
    let icon: String
    let text: String

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: icon)
                .font(.system(size: 17))
                .foregroundColor(cs.primary)
                .frame(width: 26)
            Text(text)
                .font(.system(size: 14))
                .foregroundColor(cs.textMain)
        }
    }
}

// ══════════════════════════════════════════
// PREVIEW
// ══════════════════════════════════════════

#Preview("Light") {
    SettingsScreen()
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    SettingsScreen()
        .preferredColorScheme(.dark)
}

