import SwiftUI
import SwiftData

// ═══════════════════════════════════════════
// Family Settings Screen — Matches screen-family-settings.html
// Full CRUD for family settings, display, notifications,
// share/export, sync, danger zone.
// Uses REAL data from SwiftData via FamilyTreeViewModel
// ═══════════════════════════════════════════

private var PrimaryRed: Color { LSTheme.primary }
private var DeepRed: Color { LSTheme.deepRed }
private var GoldAccent: Color { LSTheme.gold }
private var SurfaceBg: Color { LSTheme.bg }
private var SurfaceContainer: Color { LSTheme.surfaceContainer }
private var SurfaceContainerHigh: Color { LSTheme.surfaceContainerHigh }
private var PrimaryContainer: Color { LSTheme.primaryContainer }
private var TextMain: Color { LSTheme.textPrimary }
private var TextSub: Color { LSTheme.textSecondary }
private var TextDim: Color { LSTheme.textTertiary }
private var OutlineVariant: Color { LSTheme.outlineVariant }

struct FamilySettingsScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @ObservedObject var viewModel: FamilyTreeViewModel

    @State private var showEditNameAlert = false
    @State private var showEditCrestAlert = false
    @State private var showEditHometownAlert = false
    @State private var showDeleteAllAlert = false
    @State private var showTreeModePicker = false
    @State private var showDaysBeforePicker = false

    @State private var editText = ""

    var body: some View {
        VStack(spacing: 0) {
            // ═══ HEADER ═══
            HStack(spacing: 12) {
                Button { dismiss() } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(width: 40, height: 40)
                        .background(Color.white.opacity(0.15))
                        .clipShape(Circle())
                }
                Text("Cài đặt gia phả")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.white)
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)
            .padding(.bottom, 16)
            .background(
                LinearGradient(
                    colors: [Color(red: 0.773, green: 0.157, blue: 0.157),
                             Color(red: 0.545, green: 0, blue: 0)],
                    startPoint: .topLeading, endPoint: .bottomTrailing
                )
            )

            ScrollView(.vertical, showsIndicators: false) {
                VStack(alignment: .leading, spacing: 0) {
                    // ═══ FAMILY BANNER ═══
                    FamilyBanner(settings: viewModel.settings)
                        .padding(.horizontal, 16)
                        .padding(.top, 16)
                        .padding(.bottom, 16)

                    // ═══ STAT ROW ═══
                    StatRow(
                        generations: viewModel.totalGenerations,
                        members: viewModel.totalMembers,
                        memorials: viewModel.totalMemorials
                    )
                    .padding(.horizontal, 16)
                    .padding(.bottom, 12)

                    // ═══ EDIT SECTION ═══
                    SectionLabel(icon: "square.and.pencil", text: "CHỈNH SỬA")
                        .padding(.horizontal, 16)

                    SettingsGroup {
                        SettingsRow(
                            icon: "person.text.rectangle.fill",
                            iconBg: Color(hex: "EFEBE9"),
                            iconFg: Color(hex: "5D4037"),
                            title: "Tên dòng họ",
                            desc: "Đổi tên hiển thị gia phả",
                            value: viewModel.settings?.familyName
                        ) {
                            editText = viewModel.settings?.familyName ?? ""
                            showEditNameAlert = true
                        }

                        Divider().padding(.leading, 64)

                        SettingsRow(
                            icon: "shield.fill",
                            iconBg: Color(hex: "FFF8E1"),
                            iconFg: Color(hex: "F57F17"),
                            title: "Biểu tượng dòng họ",
                            desc: "Chỉnh sửa ký hiệu Family Crest",
                            value: viewModel.settings?.familyCrest
                        ) {
                            editText = viewModel.settings?.familyCrest ?? ""
                            showEditCrestAlert = true
                        }

                        Divider().padding(.leading, 64)

                        SettingsRow(
                            icon: "mappin.and.ellipse",
                            iconBg: Color(hex: "F3E5F5"),
                            iconFg: Color(hex: "7B1FA2"),
                            title: "Quê quán gốc",
                            desc: "Quê gốc của dòng họ",
                            value: viewModel.settings?.hometown.isEmpty == false ? viewModel.settings?.hometown : "Chưa cài"
                        ) {
                            editText = viewModel.settings?.hometown ?? ""
                            showEditHometownAlert = true
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 12)

                    // ═══ DISPLAY SECTION ═══
                    SectionLabel(icon: "eye", text: "HIỂN THỊ")
                        .padding(.horizontal, 16)

                    SettingsGroup {
                        SettingsRow(
                            icon: "arrow.triangle.branch",
                            iconBg: Color(hex: "E3F2FD"),
                            iconFg: Color(hex: "1565C0"),
                            title: "Kiểu hiển thị cây",
                            desc: "Dọc, ngang hoặc hình quạt",
                            value: treeDisplayModeLabel
                        ) {
                            showTreeModePicker = true
                        }

                        Divider().padding(.leading, 64)

                        SettingsToggle(
                            icon: "photo.fill",
                            iconBg: Color(hex: "E8F5E9"),
                            iconFg: Color(hex: "2E7D32"),
                            title: "Hiện ảnh đại diện",
                            desc: "Hiển thị ảnh trên cây gia phả",
                            isOn: Binding(
                                get: { viewModel.settings?.showAvatar ?? true },
                                set: { viewModel.settings?.showAvatar = $0; viewModel.updateSettings() }
                            )
                        )

                        Divider().padding(.leading, 64)

                        SettingsToggle(
                            icon: "calendar",
                            iconBg: Color(hex: "FFF8E1"),
                            iconFg: Color(hex: "F57F17"),
                            title: "Hiện năm sinh/mất",
                            desc: "Hiển thị năm trên node",
                            isOn: Binding(
                                get: { viewModel.settings?.showYears ?? true },
                                set: { viewModel.settings?.showYears = $0; viewModel.updateSettings() }
                            )
                        )
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 12)

                    // ═══ NOTIFICATIONS SECTION ═══
                    SectionLabel(icon: "bell.fill", text: "THÔNG BÁO")
                        .padding(.horizontal, 16)

                    SettingsGroup {
                        SettingsToggle(
                            icon: "flame.fill",
                            iconBg: Color(hex: "FFEBEE"),
                            iconFg: Color(hex: "C62828"),
                            title: "Nhắc ngày giỗ",
                            desc: "Nhắc nhở trước ngày giỗ",
                            isOn: Binding(
                                get: { viewModel.settings?.remindMemorial ?? true },
                                set: { viewModel.settings?.remindMemorial = $0; viewModel.updateSettings() }
                            )
                        )

                        Divider().padding(.leading, 64)

                        SettingsToggle(
                            icon: "gift.fill",
                            iconBg: Color(hex: "E3F2FD"),
                            iconFg: Color(hex: "1565C0"),
                            title: "Nhắc sinh nhật",
                            desc: "Nhắc sinh nhật thành viên",
                            isOn: Binding(
                                get: { viewModel.settings?.remindBirthday ?? true },
                                set: { viewModel.settings?.remindBirthday = $0; viewModel.updateSettings() }
                            )
                        )

                        Divider().padding(.leading, 64)

                        SettingsRow(
                            icon: "clock.fill",
                            iconBg: Color(hex: "FFF8E1"),
                            iconFg: Color(hex: "F57F17"),
                            title: "Thời gian nhắc trước",
                            desc: "Nhắc trước ngày giỗ bao lâu",
                            value: "\(viewModel.settings?.remindDaysBefore ?? 3) ngày"
                        ) {
                            showDaysBeforePicker = true
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 12)

                    // ═══ SHARE & EXPORT ═══
                    SectionLabel(icon: "square.and.arrow.up", text: "CHIA SẺ & XUẤT")
                        .padding(.horizontal, 16)

                    ShareExportGrid()
                        .padding(.horizontal, 16)
                        .padding(.bottom, 12)

                    // ═══ SYNC ═══
                    SectionLabel(icon: "arrow.triangle.2.circlepath", text: "ĐỒNG BỘ")
                        .padding(.horizontal, 16)

                    SettingsGroup {
                        SettingsRow(
                            icon: "icloud.and.arrow.up.fill",
                            iconBg: Color(hex: "E8F5E9"),
                            iconFg: Color(hex: "2E7D32"),
                            title: "Sao lưu lên iCloud",
                            desc: "Sao lưu dữ liệu gia phả"
                        ) {
                            // TODO: iCloud backup
                        }

                        Divider().padding(.leading, 64)

                        SettingsRow(
                            icon: "icloud.and.arrow.down.fill",
                            iconBg: Color(hex: "E3F2FD"),
                            iconFg: Color(hex: "1565C0"),
                            title: "Khôi phục từ iCloud",
                            desc: "Tải bản sao lưu trước đó"
                        ) {
                            // TODO: iCloud restore
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 12)

                    // ═══ DANGER ZONE ═══
                    SectionLabel(icon: "exclamationmark.triangle.fill", text: "NGUY HIỂM")
                        .padding(.horizontal, 16)

                    Button { showDeleteAllAlert = true } label: {
                        HStack(spacing: 8) {
                            Image(systemName: "trash.fill")
                                .font(.system(size: 18))
                            Text("Xoá toàn bộ gia phả")
                                .font(.system(size: 14, weight: .semibold))
                        }
                        .foregroundColor(Color(hex: "C62828"))
                        .frame(maxWidth: .infinity)
                        .padding(14)
                        .background(Color(hex: "FFF5F5"))
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color(hex: "FFCDD2"), lineWidth: 1.5))
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 8)

                    Spacer().frame(height: 32)
                }
            }
        }
        .background(SurfaceBg)
        .navigationBarHidden(true)
        .onAppear {
            viewModel.setup(context: modelContext)
        }
        // ── ALERTS ──
        .alert("Tên dòng họ", isPresented: $showEditNameAlert) {
            TextField("Nhập tên", text: $editText)
            Button("Huỷ", role: .cancel) {}
            Button("Lưu") {
                viewModel.settings?.familyName = editText
                viewModel.updateSettings()
            }
        }
        .alert("Biểu tượng", isPresented: $showEditCrestAlert) {
            TextField("VD: Ng, Tr, Lê...", text: $editText)
            Button("Huỷ", role: .cancel) {}
            Button("Lưu") {
                viewModel.settings?.familyCrest = editText
                viewModel.updateSettings()
            }
        }
        .alert("Quê quán gốc", isPresented: $showEditHometownAlert) {
            TextField("VD: Hà Nam, Nghệ An...", text: $editText)
            Button("Huỷ", role: .cancel) {}
            Button("Lưu") {
                viewModel.settings?.hometown = editText
                viewModel.updateSettings()
            }
        }
        .alert("Xoá toàn bộ gia phả", isPresented: $showDeleteAllAlert) {
            Button("Huỷ", role: .cancel) {}
            Button("Xoá", role: .destructive) {
                viewModel.deleteAllFamilyData()
            }
        } message: {
            Text("Hành động này sẽ xoá tất cả dữ liệu gia phả và không thể khôi phục. Bạn chắc chắn?")
        }
        .confirmationDialog("Kiểu hiển thị cây", isPresented: $showTreeModePicker) {
            Button("Dọc (Vertical)") {
                viewModel.settings?.treeDisplayMode = "vertical"
                viewModel.updateSettings()
            }
            Button("Ngang (Horizontal)") {
                viewModel.settings?.treeDisplayMode = "horizontal"
                viewModel.updateSettings()
            }
            Button("Hình quạt (Fan)") {
                viewModel.settings?.treeDisplayMode = "fan"
                viewModel.updateSettings()
            }
            Button("Huỷ", role: .cancel) {}
        }
        .confirmationDialog("Nhắc trước bao lâu", isPresented: $showDaysBeforePicker) {
            ForEach([1, 2, 3, 5, 7], id: \.self) { days in
                Button("\(days) ngày") {
                    viewModel.settings?.remindDaysBefore = days
                    viewModel.updateSettings()
                }
            }
            Button("Huỷ", role: .cancel) {}
        }
    }

    private var treeDisplayModeLabel: String {
        switch viewModel.settings?.treeDisplayMode {
        case "vertical": return "Dọc"
        case "horizontal": return "Ngang"
        case "fan": return "Hình quạt"
        default: return "Dọc"
        }
    }
}

// ══════════════════════════════════════════
// SECTION LABEL
// ══════════════════════════════════════════

private struct SectionLabel: View {
    let icon: String
    let text: String

    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundColor(PrimaryRed)
            Text(text)
                .font(.system(size: 12, weight: .bold))
                .foregroundColor(PrimaryRed)
                .tracking(0.5)
        }
        .padding(.top, 20)
        .padding(.bottom, 10)
    }
}

// ══════════════════════════════════════════
// FAMILY BANNER
// ══════════════════════════════════════════

private struct FamilyBanner: View {
    let settings: FamilySettingsEntity?

    var body: some View {
        ZStack(alignment: .topTrailing) {
            Circle()
                .fill(RadialGradient(colors: [Color(hex: "D4A017").opacity(0.12), .clear], center: .center, startRadius: 0, endRadius: 70))
                .frame(width: 140, height: 140)
                .offset(x: 20, y: -30)

            HStack(spacing: 14) {
                // Crest
                Circle()
                    .fill(LinearGradient(colors: [GoldAccent, Color(hex: "B8860B")], startPoint: .topLeading, endPoint: .bottomTrailing))
                    .frame(width: 56, height: 56)
                    .overlay(
                        Text(settings?.familyCrest ?? "GP")
                            .font(.system(size: 24, weight: .bold, design: .serif))
                            .foregroundColor(.white)
                    )
                    .overlay(Circle().stroke(.white.opacity(0.3), lineWidth: 2))

                VStack(alignment: .leading, spacing: 2) {
                    Text(settings?.familyName ?? "Gia phả của tôi")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.white)

                    let created = settings != nil ? formatDate(settings!.createdAt) : ""
                    if !created.isEmpty {
                        Text("Tạo ngày \(created)")
                            .font(.system(size: 12))
                            .foregroundColor(.white.opacity(0.6))
                    }
                }

                Spacer()
            }
            .padding(20)
        }
        .background(
            LinearGradient(
                colors: [Color(hex: "3E2723"), Color(hex: "4E342E"), Color(hex: "5D4037")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .clipShape(RoundedRectangle(cornerRadius: 20))
    }

    private func formatDate(_ ts: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(ts) / 1000)
        let f = DateFormatter()
        f.dateFormat = "dd/MM/yyyy"
        return f.string(from: date)
    }
}

// ══════════════════════════════════════════
// STAT ROW
// ══════════════════════════════════════════

private struct StatRow: View {
    let generations: Int
    let members: Int
    let memorials: Int

    var body: some View {
        HStack(spacing: 8) {
            StatBox(value: generations, label: "Thế hệ")
            StatBox(value: members, label: "Thành viên")
            StatBox(value: memorials, label: "Ngày giỗ")
        }
    }
}

private struct StatBox: View {
    let value: Int
    let label: String

    var body: some View {
        VStack(spacing: 2) {
            Text("\(value)")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(PrimaryRed)
            Text(label)
                .font(.system(size: 10, weight: .medium))
                .foregroundColor(TextDim)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 14)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))
    }
}

// ══════════════════════════════════════════
// SETTINGS GROUP / ROW / TOGGLE
// ══════════════════════════════════════════

private struct SettingsGroup<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        VStack(spacing: 0) { content }
            .background(SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 20))
            .overlay(RoundedRectangle(cornerRadius: 20).stroke(OutlineVariant, lineWidth: 1))
    }
}

private struct SettingsRow: View {
    let icon: String
    let iconBg: Color
    let iconFg: Color
    let title: String
    let desc: String
    var value: String? = nil
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .foregroundColor(iconFg)
                    .frame(width: 36, height: 36)
                    .background(iconBg)
                    .clipShape(RoundedRectangle(cornerRadius: 10))

                VStack(alignment: .leading, spacing: 1) {
                    Text(title)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(TextMain)
                    Text(desc)
                        .font(.system(size: 11))
                        .foregroundColor(TextDim)
                }

                Spacer()

                if let v = value {
                    Text(v)
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(TextDim)
                }

                Image(systemName: "chevron.right")
                    .font(.system(size: 14))
                    .foregroundColor(OutlineVariant)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
        }
        .buttonStyle(.plain)
    }
}

private struct SettingsToggle: View {
    let icon: String
    let iconBg: Color
    let iconFg: Color
    let title: String
    let desc: String
    @Binding var isOn: Bool

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 18))
                .foregroundColor(iconFg)
                .frame(width: 36, height: 36)
                .background(iconBg)
                .clipShape(RoundedRectangle(cornerRadius: 10))

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
                .labelsHidden()
                .tint(Color(hex: "4CAF50"))
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
    }
}

// ══════════════════════════════════════════
// SHARE & EXPORT GRID
// ══════════════════════════════════════════

private struct ShareExportGrid: View {
    private let columns = [GridItem(.flexible(), spacing: 8), GridItem(.flexible(), spacing: 8)]

    var body: some View {
        LazyVGrid(columns: columns, spacing: 8) {
            ShareButton(icon: "doc.richtext", iconBg: Color(hex: "FFEBEE"), iconFg: Color(hex: "C62828"), title: "Xuất PDF", desc: "File in ấn")
            ShareButton(icon: "photo", iconBg: Color(hex: "E8F5E9"), iconFg: Color(hex: "2E7D32"), title: "Xuất Ảnh", desc: "PNG chất lượng cao")
            ShareButton(icon: "link", iconBg: Color(hex: "E3F2FD"), iconFg: Color(hex: "1565C0"), title: "Chia sẻ link", desc: "Gửi cho gia đình")
            ShareButton(icon: "qrcode", iconBg: Color(hex: "F3E5F5"), iconFg: Color(hex: "7B1FA2"), title: "Mã QR", desc: "Quét để xem")
        }
    }
}

private struct ShareButton: View {
    let icon: String
    let iconBg: Color
    let iconFg: Color
    let title: String
    let desc: String

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: icon)
                .font(.system(size: 18))
                .foregroundColor(iconFg)
                .frame(width: 36, height: 36)
                .background(iconBg)
                .clipShape(RoundedRectangle(cornerRadius: 10))

            VStack(alignment: .leading, spacing: 0) {
                Text(title)
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(TextMain)
                Text(desc)
                    .font(.system(size: 10))
                    .foregroundColor(TextDim)
            }

            Spacer(minLength: 0)
        }
        .padding(14)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))
    }
}
