import SwiftUI
import PhotosUI

// ═══════════════════════════════════════════
// Edit Profile Screen — Port from Android + iOS HTML mock
// Full-screen modal with real data persistence
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
private var OutlineColor: Color { LSTheme.textTertiary }
private var OutlineVariant: Color { LSTheme.outlineVariant }

struct EditProfileScreen: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var viewModel = EditProfileViewModel()

    var body: some View {
        VStack(spacing: 0) {
            // ═══ HEADER ═══
            FormHeader(
                onClose: { dismiss() },
                onSave: {
                    viewModel.saveProfile()
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                        dismiss()
                    }
                }
            )

            // ═══ FORM CONTENT ═══
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 0) {
                    // ── Avatar ──
                    AvatarEditSection(viewModel: viewModel)

                    // ── Họ và tên ──
                    FieldGroup(label: "HỌ VÀ TÊN") {
                        ProfileTextField(
                            text: $viewModel.displayName,
                            placeholder: "Nhập họ tên",
                            isFilled: !viewModel.displayName.isEmpty
                        )
                    }

                    // ── Email ──
                    FieldGroup(label: "EMAIL") {
                        ProfileTextField(
                            text: $viewModel.email,
                            placeholder: "Nhập email",
                            keyboardType: .emailAddress,
                            isFilled: !viewModel.email.isEmpty
                        )
                    }

                    // ── Giới tính ──
                    FieldGroup(label: "GIỚI TÍNH") {
                        GenderSelector(selected: $viewModel.gender)
                    }
                    .onChange(of: viewModel.gender) { _ in
                        viewModel.recalculateBirthInfo()
                    }

                    // ── Ngày sinh (Âm lịch) ──
                    FieldGroup(label: "NGÀY SINH (ÂM LỊCH)") {
                        VStack(spacing: 8) {
                            // Date row
                            DateFieldRow(
                                day: $viewModel.birthDay,
                                month: $viewModel.birthMonth,
                                year: $viewModel.birthYear
                            )
                            .onChange(of: viewModel.birthDay) { _ in viewModel.recalculateBirthInfo() }
                            .onChange(of: viewModel.birthMonth) { _ in viewModel.recalculateBirthInfo() }
                            .onChange(of: viewModel.birthYear) { _ in viewModel.recalculateBirthInfo() }

                            // Time row
                            TimeFieldRow(
                                hour: $viewModel.birthHour,
                                minute: $viewModel.birthMinute,
                                unknownTime: $viewModel.unknownBirthTime
                            )
                            .onChange(of: viewModel.birthHour) { _ in viewModel.recalculateBirthInfo() }
                            .onChange(of: viewModel.unknownBirthTime) { _ in viewModel.recalculateBirthInfo() }
                        }
                    }

                    // ── Can Chi Result ──
                    if !viewModel.birthInfo.yearCanChi.isEmpty {
                        CanChiResultCard(viewModel: viewModel)
                            .padding(.horizontal, 16)
                            .padding(.top, 12)
                    }

                    Spacer().frame(height: 40)
                }
                .padding(.top, 20)
                .padding(.bottom, 32)
            }
            .background(SurfaceBg)
        }
        .background(SurfaceBg)
        .overlay(alignment: .bottom) {
            // Toast
            if let msg = viewModel.toastMessage {
                ToastView(message: msg)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .onAppear {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                            withAnimation { viewModel.toastMessage = nil }
                        }
                    }
                    .padding(.bottom, 40)
            }
        }
        .animation(.easeInOut(duration: 0.25), value: viewModel.toastMessage)
    }
}

// ══════════════════════════════════════════
// HEADER
// ══════════════════════════════════════════

private struct FormHeader: View {
    let onClose: () -> Void
    let onSave: () -> Void

    var body: some View {
        HStack {
            HStack(spacing: 8) {
                Button(action: onClose) {
                    Circle()
                        .fill(SurfaceContainer)
                        .frame(width: 36, height: 36)
                        .overlay(
                            Image(systemName: "xmark")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(TextMain)
                        )
                }

                Text("Sửa hồ sơ")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(TextMain)
            }

            Spacer()

            Button(action: onSave) {
                Text("Lưu")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 8)
                    .background(PrimaryRed)
                    .clipShape(Capsule())
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 14)
        .padding(.bottom, 14)
        .background(SurfaceBg)
        .overlay(alignment: .bottom) {
            Divider().foregroundColor(OutlineVariant)
        }
    }
}

// ══════════════════════════════════════════
// AVATAR EDIT
// ══════════════════════════════════════════

private struct AvatarEditSection: View {
    @ObservedObject var viewModel: EditProfileViewModel

    var body: some View {
        VStack(spacing: 0) {
            // Avatar circle with camera badge
            PhotosPicker(
                selection: $viewModel.selectedPhotoItem,
                matching: .images,
                photoLibrary: .shared()
            ) {
                ZStack(alignment: .bottomTrailing) {
                    // Avatar circle
                    if let img = viewModel.avatarImage {
                        Image(uiImage: img)
                            .resizable()
                            .scaledToFill()
                            .frame(width: 100, height: 100)
                            .clipShape(Circle())
                            .overlay(Circle().stroke(SurfaceContainer, lineWidth: 3))
                    } else {
                        Circle()
                            .fill(
                                LinearGradient(
                                    colors: [PrimaryRed, DeepRed],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                            .frame(width: 100, height: 100)
                            .overlay(
                                Text(viewModel.initials)
                                    .font(.system(size: 36, weight: .bold, design: .serif))
                                    .foregroundColor(.white)
                            )
                            .overlay(Circle().stroke(SurfaceContainer, lineWidth: 3))
                    }

                    // Camera badge
                    Circle()
                        .fill(PrimaryRed)
                        .frame(width: 32, height: 32)
                        .overlay(
                            Image(systemName: "camera.fill")
                                .font(.system(size: 14))
                                .foregroundColor(.white)
                        )
                        .overlay(Circle().stroke(SurfaceBg, lineWidth: 3))
                }
            }

            Spacer().frame(height: 8)

            // "Đổi ảnh đại diện" link
            PhotosPicker(
                selection: $viewModel.selectedPhotoItem,
                matching: .images,
                photoLibrary: .shared()
            ) {
                Text("Đổi ảnh đại diện")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(PrimaryRed)
            }

            // "Xóa ảnh" link (if avatar exists)
            if viewModel.avatarImage != nil {
                Button {
                    viewModel.removeAvatar()
                } label: {
                    Text("Xóa ảnh")
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(.red.opacity(0.7))
                }
                .padding(.top, 4)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.bottom, 28)
    }
}

// ══════════════════════════════════════════
// FIELD GROUP
// ══════════════════════════════════════════

private struct FieldGroup<Content: View>: View {
    let label: String
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(PrimaryRed)
                .tracking(0.5)

            content()
        }
        .padding(.horizontal, 16)
        .padding(.bottom, 18)
    }
}

// ══════════════════════════════════════════
// TEXT FIELD
// ══════════════════════════════════════════

private struct ProfileTextField: View {
    @Binding var text: String
    var placeholder: String = ""
    var keyboardType: UIKeyboardType = .default
    var isFilled: Bool = false

    var body: some View {
        TextField(placeholder, text: $text)
            .font(.system(size: 15))
            .foregroundColor(TextMain)
            .keyboardType(keyboardType)
            .autocorrectionDisabled()
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(isFilled ? OutlineColor : OutlineVariant, lineWidth: 1.5)
            )
    }
}

// ══════════════════════════════════════════
// GENDER SELECTOR
// ══════════════════════════════════════════

private struct GenderSelector: View {
    @Binding var selected: String

    var body: some View {
        HStack(spacing: 8) {
            GenderOption(emoji: "👨", label: "Nam", isActive: selected == "Nam") {
                selected = "Nam"
            }
            GenderOption(emoji: "👩", label: "Nữ", isActive: selected == "Nữ") {
                selected = "Nữ"
            }
        }
    }
}

private struct GenderOption: View {
    let emoji: String
    let label: String
    let isActive: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 4) {
                Text(emoji)
                    .font(.system(size: 24))
                Text(label)
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(TextMain)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(isActive ? PrimaryContainer : SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(isActive ? PrimaryRed : OutlineVariant, lineWidth: 1.5)
            )
        }
        .animation(.easeInOut(duration: 0.2), value: isActive)
    }
}

// ══════════════════════════════════════════
// DATE FIELD ROW
// ══════════════════════════════════════════

private struct DateFieldRow: View {
    @Binding var day: String
    @Binding var month: String
    @Binding var year: String

    var body: some View {
        HStack(spacing: 8) {
            DateFieldCell(label: "NGÀY", value: $day, placeholder: "DD")
            DateFieldCell(label: "THÁNG", value: $month, placeholder: "MM")
            DateFieldCell(label: "NĂM", value: $year, placeholder: "YYYY")
        }
    }
}

private struct DateFieldCell: View {
    let label: String
    @Binding var value: String
    let placeholder: String

    var body: some View {
        VStack(spacing: 4) {
            Text(label)
                .font(.system(size: 9, weight: .semibold))
                .foregroundColor(OutlineColor)
                .tracking(0.3)

            TextField(placeholder, text: $value)
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(TextMain)
                .multilineTextAlignment(.center)
                .keyboardType(.numberPad)
                .frame(maxWidth: .infinity)
        }
        .padding(.vertical, 12)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(OutlineVariant, lineWidth: 1.5)
        )
    }
}

// ══════════════════════════════════════════
// TIME FIELD ROW
// ══════════════════════════════════════════

private struct TimeFieldRow: View {
    @Binding var hour: String
    @Binding var minute: String
    @Binding var unknownTime: Bool

    var body: some View {
        HStack(spacing: 8) {
            if !unknownTime {
                TimeFieldCell(label: "GIỜ SINH", value: $hour, placeholder: "HH")
                TimeFieldCell(label: "PHÚT", value: $minute, placeholder: "mm")
            }

            // "Không rõ giờ sinh" toggle
            Button {
                withAnimation(.easeInOut(duration: 0.2)) {
                    unknownTime.toggle()
                    if unknownTime {
                        hour = ""
                        minute = ""
                    }
                }
            } label: {
                HStack(spacing: 6) {
                    Image(systemName: unknownTime ? "checkmark.circle.fill" : "questionmark.circle")
                        .font(.system(size: 16))
                        .foregroundColor(unknownTime ? PrimaryRed : OutlineColor)
                    Text("Không rõ giờ sinh")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(unknownTime ? PrimaryRed : OutlineColor)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(unknownTime ? PrimaryContainer.opacity(0.5) : SurfaceContainer)
                .clipShape(RoundedRectangle(cornerRadius: 14))
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(
                            unknownTime ? PrimaryRed.opacity(0.5) : OutlineVariant,
                            style: StrokeStyle(lineWidth: 1.5, dash: unknownTime ? [] : [6, 4])
                        )
                )
            }
        }
    }
}

private struct TimeFieldCell: View {
    let label: String
    @Binding var value: String
    let placeholder: String

    var body: some View {
        VStack(spacing: 4) {
            Text(label)
                .font(.system(size: 9, weight: .semibold))
                .foregroundColor(OutlineColor)
                .tracking(0.3)

            TextField(placeholder, text: $value)
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(TextMain)
                .multilineTextAlignment(.center)
                .keyboardType(.numberPad)
                .frame(maxWidth: .infinity)
        }
        .padding(.vertical, 12)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(OutlineVariant, lineWidth: 1.5)
        )
    }
}

// ══════════════════════════════════════════
// CAN CHI RESULT CARD
// ══════════════════════════════════════════

private struct CanChiResultCard: View {
    @ObservedObject var viewModel: EditProfileViewModel
    let info: BirthInfo

    init(viewModel: EditProfileViewModel) {
        self.viewModel = viewModel
        self.info = viewModel.birthInfo
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            // Header
            HStack(spacing: 6) {
                Image(systemName: "sparkles")
                    .font(.system(size: 14))
                    .foregroundColor(GoldAccent)
                Text("THÔNG TIN TỰ ĐỘNG TÍNH")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(PrimaryRed)
                    .tracking(0.5)
            }

            // Grid 2x2
            LazyVGrid(columns: [
                GridItem(.flexible(), spacing: 8),
                GridItem(.flexible(), spacing: 8)
            ], spacing: 8) {
                // Can Chi năm (highlight)
                CanChiCell(
                    label: "CAN CHI NĂM",
                    value: info.yearCanChi,
                    subtitle: info.nguHanh,
                    isHighlight: true
                )

                // Mệnh
                CanChiCell(
                    label: "MỆNH",
                    value: "\(viewModel.menhEmoji) \(info.menh)",
                    subtitle: menhSubtitle
                )

                // Cung
                CanChiCell(
                    label: "CUNG",
                    value: info.cung.components(separatedBy: " (").first ?? info.cung,
                    subtitle: "Mệnh \(info.menh)"
                )

                // Tuổi (con giáp)
                CanChiCell(
                    label: "TUỔI",
                    value: "\(info.conGiapEmoji) \(info.conGiap)",
                    subtitle: zodiacAnimal
                )
            }

            // Giờ sinh (full width)
            if !info.gioSinhCanChi.isEmpty {
                CanChiCellFull(
                    label: "GIỜ SINH",
                    value: info.gioSinhCanChi,
                    badge: "Tự động"
                )
            }
        }
        .padding(16)
        .background(
            LinearGradient(
                colors: [SurfaceContainerHigh, PrimaryContainer.opacity(0.4)],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(OutlineVariant, lineWidth: 1)
        )
    }

    private var menhSubtitle: String {
        switch info.menh {
        case "Kim": return "Thuộc Kim"
        case "Mộc": return "Thuộc Mộc"
        case "Thủy": return "Thuộc Thủy"
        case "Hỏa": return "Thuộc Hỏa"
        case "Thổ": return "Thuộc Thổ"
        default: return ""
        }
    }

    private var zodiacAnimal: String {
        switch info.conGiap {
        case "Tý": return "Con Chuột"
        case "Sửu": return "Con Trâu"
        case "Dần": return "Con Hổ"
        case "Mão": return "Con Mèo"
        case "Thìn": return "Con Rồng"
        case "Tỵ": return "Con Rắn"
        case "Ngọ": return "Con Ngựa"
        case "Mùi": return "Con Dê"
        case "Thân": return "Con Khỉ"
        case "Dậu": return "Con Gà"
        case "Tuất": return "Con Chó"
        case "Hợi": return "Con Lợn"
        default: return ""
        }
    }
}

private struct CanChiCell: View {
    let label: String
    let value: String
    var subtitle: String = ""
    var isHighlight: Bool = false

    var body: some View {
        VStack(spacing: 2) {
            Text(label)
                .font(.system(size: 9, weight: .semibold))
                .foregroundColor(OutlineColor)
                .tracking(0.3)

            Text(value)
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(TextMain)

            if !subtitle.isEmpty {
                Text(subtitle)
                    .font(.system(size: 10))
                    .foregroundColor(OutlineColor)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(12)
        .background(
            isHighlight ?
            AnyShapeStyle(
                LinearGradient(
                    colors: [GoldAccent.opacity(0.1), GoldAccent.opacity(0.05)],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            ) :
            AnyShapeStyle(SurfaceBg)
        )
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(isHighlight ? GoldAccent.opacity(0.3) : Color.clear, lineWidth: 1)
        )
    }
}

private struct CanChiCellFull: View {
    let label: String
    let value: String
    var badge: String? = nil

    var body: some View {
        VStack(spacing: 4) {
            Text(label)
                .font(.system(size: 9, weight: .semibold))
                .foregroundColor(OutlineColor)
                .tracking(0.3)

            Text(value)
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(TextMain)

            if let badge = badge {
                Text(badge)
                    .font(.system(size: 9, weight: .semibold))
                    .foregroundColor(OutlineColor)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2)
                    .background(SurfaceContainer)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            }
        }
        .frame(maxWidth: .infinity)
        .padding(12)
        .background(SurfaceBg)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

// ══════════════════════════════════════════
// TOAST
// ══════════════════════════════════════════

private struct ToastView: View {
    let message: String

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 16))
                .foregroundColor(.green)
            Text(message)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(.white)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
        .background(.black.opacity(0.8))
        .clipShape(Capsule())
    }
}

// ══════════════════════════════════════════
// PREVIEW
// ══════════════════════════════════════════

#Preview {
    EditProfileScreen()
}
