import SwiftUI

// ═══════════════════════════════════════════
// Onboarding Screen — Port from Android
// ═══════════════════════════════════════════

private let PrimaryRed = Color(hex: "B71C1C")
private let DeepRed = Color(hex: "8B0000")
private let GoldAccent = Color(hex: "D4A017")
private let GoldLight = Color(hex: "E8C84A")
private let SurfaceBg = Color(hex: "FFFBF5")
private let TextMain = Color(hex: "1C1B1F")
private let TextSub = Color(hex: "534340")
private let TextDim = Color(hex: "857371")
private let Outline = Color(hex: "D8C2BF")

// ── Onboarding page data ──

struct OnboardingPageData: Identifiable {
    let id = UUID()
    let icon: String  // SF Symbol name
    let iconColors: [Color]
    let accentColor: Color
    let title: String
    let subtitle: String
    let description: String
    let featureChips: [(String, String)] // (SF Symbol, label)
}

private let onboardingPages = [
    OnboardingPageData(
        icon: "calendar",
        iconColors: [PrimaryRed, Color(hex: "D32F2F")],
        accentColor: PrimaryRed,
        title: "Lịch Vạn Niên",
        subtitle: "Âm lịch · Dương lịch · Can Chi",
        description: "Xem lịch âm dương chính xác, ngày giờ hoàng đạo, tiết khí và thông tin can chi đầy đủ cho mỗi ngày.",
        featureChips: [
            ("calendar", "Lịch âm dương"),
            ("moon.fill", "Tuần trăng"),
            ("clock.fill", "Giờ hoàng đạo"),
            ("sparkles", "Can chi ngày")
        ]
    ),
    OnboardingPageData(
        icon: "checkmark.circle.fill",
        iconColors: [Color(hex: "2E7D32"), Color(hex: "43A047")],
        accentColor: Color(hex: "2E7D32"),
        title: "Ngày Tốt · Ngày Xấu",
        subtitle: "Chọn ngày theo phong thủy",
        description: "Tra cứu ngày tốt xấu cho mọi việc: cưới hỏi, khai trương, xây nhà, xuất hành... theo lịch vạn niên truyền thống.",
        featureChips: [
            ("heart.fill", "Cưới hỏi"),
            ("storefront.fill", "Khai trương"),
            ("house.fill", "Xây nhà"),
            ("airplane.departure", "Xuất hành")
        ]
    ),
    OnboardingPageData(
        icon: "book.fill",
        iconColors: [GoldAccent, GoldLight],
        accentColor: GoldAccent,
        title: "Văn Khấn · Cúng Lễ",
        subtitle: "Trọn bộ văn khấn truyền thống",
        description: "Hơn 100 bài văn khấn cho mọi dịp: cúng gia tiên, khai trương, động thổ, cầu an, giải hạn...",
        featureChips: [
            ("building.columns.fill", "Cúng gia tiên"),
            ("party.popper.fill", "Ngày lễ tết"),
            ("hands.clap.fill", "Cầu an"),
            ("bookmark.fill", "Lưu & chia sẻ")
        ]
    ),
    OnboardingPageData(
        icon: "figure.2.and.child.holdinghands",
        iconColors: [Color(hex: "6A1B9A"), Color(hex: "AB47BC")],
        accentColor: Color(hex: "7B1FA2"),
        title: "Gia Phả Gia Đình",
        subtitle: "Lưu giữ cội nguồn · Kết nối thế hệ",
        description: "Xây dựng cây gia phả trực quan, lưu giữ thông tin từng thành viên, ngày giỗ và kỷ niệm quan trọng của dòng họ.",
        featureChips: [
            ("person.3.fill", "Cây gia phả"),
            ("person.fill", "Hồ sơ thành viên"),
            ("birthday.cake.fill", "Ngày giỗ · Sinh nhật"),
            ("square.and.arrow.up.fill", "Chia sẻ gia phả")
        ]
    ),
    OnboardingPageData(
        icon: "sparkles",
        iconColors: [Color(hex: "1565C0"), Color(hex: "42A5F5")],
        accentColor: Color(hex: "1565C0"),
        title: "Trợ Lý AI Thông Minh",
        subtitle: "Hỏi đáp phong thủy bằng AI",
        description: "Hỏi bất cứ điều gì về phong thủy, ngày giờ tốt, văn khấn... AI sẽ trả lời chính xác và nhanh chóng.",
        featureChips: [
            ("message.fill", "Hỏi đáp AI"),
            ("newspaper.fill", "Ngày này năm xưa"),
            ("bell.fill", "Nhắc nhở thông minh"),
            ("lightbulb.fill", "Gợi ý thông minh")
        ]
    )
]

struct OnboardingScreen: View {
    let onFinish: () -> Void

    @State private var currentPage = 0
    private let permissionsPageIndex = onboardingPages.count       // page 5
    private let profilePageIndex = onboardingPages.count + 1       // page 6
    private var totalPages: Int { onboardingPages.count + 2 }

    // Profile form state
    @State private var inputName = ""
    @State private var inputBirthDay = ""
    @State private var inputBirthMonth = ""
    @State private var inputBirthYear = ""
    @State private var inputBirthHour = ""
    @State private var inputBirthMinute = ""
    @State private var inputGender = "Nam"
    @State private var nameError = false
    @State private var yearError = false

    var body: some View {
        ZStack(alignment: .bottom) {
            SurfaceBg.ignoresSafeArea()

            // ── Pager ──
            TabView(selection: $currentPage) {
                ForEach(0..<onboardingPages.count, id: \.self) { index in
                    OnboardingPageContent(page: onboardingPages[index])
                        .tag(index)
                }

                PermissionsSetupPage(
                    onSkip: { currentPage = profilePageIndex },
                    onNext: { currentPage = profilePageIndex },
                    totalPages: totalPages,
                    currentPage: $currentPage
                )
                .tag(permissionsPageIndex)

                ProfileSetupPage(
                    inputName: $inputName,
                    inputBirthDay: $inputBirthDay,
                    inputBirthMonth: $inputBirthMonth,
                    inputBirthYear: $inputBirthYear,
                    inputBirthHour: $inputBirthHour,
                    inputBirthMinute: $inputBirthMinute,
                    inputGender: $inputGender,
                    nameError: $nameError,
                    yearError: $yearError,
                    onSkip: { onFinish() },
                    onFinish: { saveProfileAndFinish() },
                    totalPages: totalPages,
                    currentPageBinding: $currentPage
                )
                .tag(profilePageIndex)
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
            .animation(.easeInOut(duration: 0.3), value: currentPage)

            // ── Bottom controls (feature pages only) ──
            if currentPage < onboardingPages.count {
                VStack(spacing: 28) {
                    // Page dots
                    HStack(spacing: 8) {
                        ForEach(0..<totalPages, id: \.self) { index in
                            Capsule()
                                .fill(currentPage == index ? accentColorFor(index: index) : Outline)
                                .frame(width: currentPage == index ? 28 : 8, height: 8)
                                .animation(.spring(response: 0.3, dampingFraction: 0.7), value: currentPage)
                        }
                    }

                    // Action buttons
                    HStack {
                        Button("Bỏ qua") {
                            withAnimation { currentPage = permissionsPageIndex }
                        }
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(TextDim)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 10)

                        Spacer()

                        Button {
                            withAnimation { currentPage += 1 }
                        } label: {
                            HStack(spacing: 8) {
                                Text("Tiếp theo")
                                    .font(.system(size: 15, weight: .bold))
                                Image(systemName: "arrow.right")
                                    .font(.system(size: 14, weight: .bold))
                            }
                            .foregroundColor(.white)
                            .padding(.horizontal, 24)
                            .padding(.vertical, 14)
                            .background(
                                LinearGradient(
                                    colors: [accentColorFor(index: currentPage), accentColorFor(index: currentPage).opacity(0.85)],
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .clipShape(Capsule())
                        }
                    }
                    .padding(.horizontal, 24)
                }
                .padding(.bottom, 32)
            }
        }
    }

    private func accentColorFor(index: Int) -> Color {
        if index < onboardingPages.count {
            return onboardingPages[index].accentColor
        } else if index == permissionsPageIndex {
            return Color(hex: "1565C0")
        }
        return PrimaryRed
    }

    private func saveProfileAndFinish() {
        let nameTrimmed = inputName.trimmingCharacters(in: .whitespaces)
        let yearInt = Int(inputBirthYear) ?? 0

        if !nameTrimmed.isEmpty && yearInt > 0 {
            if yearInt < 1900 || yearInt > 2100 {
                yearError = true
                return
            }
        }

        // Save to UserDefaults
        let defaults = UserDefaults.standard
        if !nameTrimmed.isEmpty {
            defaults.set(nameTrimmed, forKey: "displayName")
        }
        if yearInt > 0 {
            defaults.set(yearInt, forKey: "birthYear")
        }
        defaults.set(Int(inputBirthDay) ?? 0, forKey: "birthDay")
        defaults.set(Int(inputBirthMonth) ?? 0, forKey: "birthMonth")
        defaults.set(Int(inputBirthHour) ?? -1, forKey: "birthHour")
        defaults.set(Int(inputBirthMinute) ?? -1, forKey: "birthMinute")
        defaults.set(inputGender, forKey: "gender")

        onFinish()
    }
}

// ═══════════════════════════════════════════
// SINGLE PAGE CONTENT
// ═══════════════════════════════════════════

struct OnboardingPageContent: View {
    let page: OnboardingPageData

    var body: some View {
        VStack(spacing: 0) {
            Spacer().frame(height: 60)

            // ── Big Icon ──
            ZStack {
                Circle()
                    .fill(
                        RadialGradient(
                            colors: [page.accentColor, Color.clear],
                            center: .center,
                            startRadius: 0,
                            endRadius: 90
                        )
                    )
                    .frame(width: 180, height: 180)
                    .opacity(0.12)

                Circle()
                    .fill(
                        LinearGradient(
                            colors: page.iconColors,
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 120, height: 120)
                    .overlay(
                        Image(systemName: page.icon)
                            .font(.system(size: 52))
                            .foregroundColor(.white)
                    )
            }

            Spacer().frame(height: 40)

            // ── Title ──
            Text(page.title)
                .font(.system(size: 28, weight: .bold, design: .serif))
                .foregroundColor(TextMain)
                .multilineTextAlignment(.center)

            Spacer().frame(height: 8)

            // ── Subtitle ──
            Text(page.subtitle)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(page.accentColor)
                .multilineTextAlignment(.center)

            Spacer().frame(height: 6)

            // ── Gold decorative line ──
            Rectangle()
                .fill(
                    LinearGradient(
                        colors: [Color.clear, GoldAccent, Color.clear],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .frame(width: 50, height: 2)

            Spacer().frame(height: 20)

            // ── Description ──
            Text(page.description)
                .font(.system(size: 15))
                .foregroundColor(TextSub)
                .multilineTextAlignment(.center)
                .lineSpacing(6)
                .padding(.horizontal, 40)

            Spacer().frame(height: 28)

            // ── Feature Chips ──
            VStack(spacing: 8) {
                HStack(spacing: 8) {
                    ForEach(0..<min(2, page.featureChips.count), id: \.self) { i in
                        FeatureChipView(
                            icon: page.featureChips[i].0,
                            label: page.featureChips[i].1,
                            accentColor: page.accentColor
                        )
                    }
                }
                HStack(spacing: 8) {
                    ForEach(2..<page.featureChips.count, id: \.self) { i in
                        FeatureChipView(
                            icon: page.featureChips[i].0,
                            label: page.featureChips[i].1,
                            accentColor: page.accentColor
                        )
                    }
                }
            }

            Spacer()
        }
        .padding(.horizontal, 32)
    }
}

// ═══════════════════════════════════════════
// FEATURE CHIP
// ═══════════════════════════════════════════

struct FeatureChipView: View {
    let icon: String
    let label: String
    let accentColor: Color

    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundColor(accentColor)
            Text(label)
                .font(.system(size: 12, weight: .medium))
                .foregroundColor(accentColor)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 8)
        .background(accentColor.opacity(0.08))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(accentColor.opacity(0.15), lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

// ═══════════════════════════════════════════
// PERMISSIONS PAGE
// ═══════════════════════════════════════════

struct PermissionsSetupPage: View {
    let onSkip: () -> Void
    let onNext: () -> Void
    let totalPages: Int
    @Binding var currentPage: Int

    @State private var notificationGranted = false

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Spacer().frame(height: 48)

                // Icon
                ZStack {
                    Circle()
                        .fill(
                            RadialGradient(
                                colors: [Color(hex: "1565C0"), Color.clear],
                                center: .center,
                                startRadius: 0,
                                endRadius: 90
                            )
                        )
                        .frame(width: 180, height: 180)
                        .opacity(0.12)

                    Circle()
                        .fill(
                            LinearGradient(
                                colors: [Color(hex: "1565C0"), Color(hex: "42A5F5")],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 100, height: 100)
                        .overlay(
                            Image(systemName: notificationGranted ? "checkmark.shield.fill" : "bell.badge.fill")
                                .font(.system(size: 48))
                                .foregroundColor(.white)
                        )
                }

                Spacer().frame(height: 24)

                Text("Cấp quyền ứng dụng")
                    .font(.system(size: 26, weight: .bold, design: .serif))
                    .foregroundColor(TextMain)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: 6)

                Text("Để nhắc nhở và thông báo hoạt động tốt, ứng dụng cần quyền thông báo")
                    .font(.system(size: 13))
                    .foregroundColor(TextSub)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 16)

                Spacer().frame(height: 6)

                // Gold line
                Rectangle()
                    .fill(LinearGradient(colors: [Color.clear, GoldAccent, Color.clear], startPoint: .leading, endPoint: .trailing))
                    .frame(width: 50, height: 2)

                Spacer().frame(height: 28)

                // Permission card
                PermissionItemCard(
                    icon: "bell.fill",
                    iconColor: Color(hex: "E65100"),
                    title: "Thông báo",
                    description: "Nhận nhắc nhở tờ lịch hàng ngày, ngày lễ âm lịch, giờ hoàng đạo và nhắc nhở cá nhân đúng giờ.",
                    isGranted: notificationGranted,
                    onRequest: {
                        requestNotificationPermission()
                    }
                )

                Spacer().frame(height: 20)

                // Tip
                HStack(alignment: .top, spacing: 8) {
                    Image(systemName: "lightbulb.fill")
                        .foregroundColor(GoldAccent)
                        .font(.system(size: 16))
                    Text("Các quyền này giúp ứng dụng nhắc nhở bạn về ngày giỗ, lễ tết, giờ hoàng đạo và nhắc nhở cá nhân.")
                        .font(.system(size: 12))
                        .foregroundColor(TextSub)
                        .lineSpacing(4)
                }
                .padding(12)
                .background(GoldAccent.opacity(0.08))
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(GoldAccent.opacity(0.2), lineWidth: 1))
                .clipShape(RoundedRectangle(cornerRadius: 12))

                Spacer().frame(height: 28)

                // Page dots
                HStack(spacing: 8) {
                    ForEach(0..<totalPages, id: \.self) { index in
                        Capsule()
                            .fill(currentPage == index ? accentColorFor(index) : Outline)
                            .frame(width: currentPage == index ? 28 : 8, height: 8)
                    }
                }

                Spacer().frame(height: 16)

                // Buttons
                HStack {
                    Button("Để sau") { onSkip() }
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(TextDim)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 10)

                    Spacer()

                    Button {
                        onNext()
                    } label: {
                        HStack(spacing: 8) {
                            Text("Tiếp tục")
                                .font(.system(size: 15, weight: .bold))
                            Image(systemName: "arrow.right")
                                .font(.system(size: 14, weight: .bold))
                        }
                        .foregroundColor(.white)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 14)
                        .background(
                            LinearGradient(colors: [Color(hex: "1565C0"), Color(hex: "1976D2")], startPoint: .leading, endPoint: .trailing)
                        )
                        .clipShape(Capsule())
                    }
                }
            }
            .padding(.horizontal, 32)
            .padding(.bottom, 32)
        }
    }

    private func accentColorFor(_ index: Int) -> Color {
        if index < onboardingPages.count { return onboardingPages[index].accentColor }
        if index == onboardingPages.count { return Color(hex: "1565C0") }
        return PrimaryRed
    }

    private func requestNotificationPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
            DispatchQueue.main.async {
                notificationGranted = granted
            }
        }
    }
}

// ═══════════════════════════════════════════
// PERMISSION ITEM CARD
// ═══════════════════════════════════════════

struct PermissionItemCard: View {
    let icon: String
    let iconColor: Color
    let title: String
    let description: String
    let isGranted: Bool
    let onRequest: () -> Void

    var body: some View {
        HStack(alignment: .top, spacing: 14) {
            Circle()
                .fill(isGranted ? Color(hex: "C8E6C9") : iconColor.opacity(0.12))
                .frame(width: 44, height: 44)
                .overlay(
                    Image(systemName: isGranted ? "checkmark" : icon)
                        .font(.system(size: 20))
                        .foregroundColor(isGranted ? Color(hex: "388E3C") : iconColor)
                )

            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 8) {
                    Text(title)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(isGranted ? Color(hex: "2E7D32") : TextMain)
                    if isGranted {
                        Text("✓ Đã cấp")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(Color(hex: "388E3C"))
                    }
                }
                Text(description)
                    .font(.system(size: 12))
                    .foregroundColor(isGranted ? Color(hex: "33691E") : TextSub)
                    .lineSpacing(4)

                if !isGranted {
                    Button("Cấp quyền") { onRequest() }
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(iconColor)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(iconColor.opacity(0.1))
                        .overlay(RoundedRectangle(cornerRadius: 10).stroke(iconColor.opacity(0.3), lineWidth: 1))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                        .padding(.top, 6)
                }
            }
        }
        .padding(16)
        .background(isGranted ? Color(hex: "F1F8E9") : Color.white)
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(isGranted ? Color(hex: "A5D6A7") : Outline, lineWidth: 1))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

// ═══════════════════════════════════════════
// PROFILE SETUP PAGE
// ═══════════════════════════════════════════

struct ProfileSetupPage: View {
    @Binding var inputName: String
    @Binding var inputBirthDay: String
    @Binding var inputBirthMonth: String
    @Binding var inputBirthYear: String
    @Binding var inputBirthHour: String
    @Binding var inputBirthMinute: String
    @Binding var inputGender: String
    @Binding var nameError: Bool
    @Binding var yearError: Bool
    let onSkip: () -> Void
    let onFinish: () -> Void
    let totalPages: Int
    @Binding var currentPageBinding: Int

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Spacer().frame(height: 48)

                // Icon
                ZStack {
                    Circle()
                        .fill(RadialGradient(colors: [PrimaryRed, Color.clear], center: .center, startRadius: 0, endRadius: 90))
                        .frame(width: 180, height: 180)
                        .opacity(0.12)
                    Circle()
                        .fill(LinearGradient(colors: [PrimaryRed, Color(hex: "D32F2F")], startPoint: .topLeading, endPoint: .bottomTrailing))
                        .frame(width: 100, height: 100)
                        .overlay(
                            Image(systemName: "person.fill")
                                .font(.system(size: 48))
                                .foregroundColor(.white)
                        )
                }

                Spacer().frame(height: 24)

                Text("Thông tin cá nhân")
                    .font(.system(size: 26, weight: .bold, design: .serif))
                    .foregroundColor(TextMain)

                Spacer().frame(height: 6)

                Text("Giúp AI phân tích phong thủy chính xác theo tuổi & mệnh")
                    .font(.system(size: 13))
                    .foregroundColor(TextSub)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 16)

                Spacer().frame(height: 6)

                Rectangle()
                    .fill(LinearGradient(colors: [Color.clear, GoldAccent, Color.clear], startPoint: .leading, endPoint: .trailing))
                    .frame(width: 50, height: 2)

                Spacer().frame(height: 28)

                // ═══ FORM ═══
                VStack(spacing: 16) {
                    // Name
                    FormFieldView(
                        value: $inputName,
                        label: "Tên hiển thị *",
                        placeholder: "Nhập tên của bạn",
                        icon: "person.fill",
                        isError: nameError,
                        errorText: nameError ? "Vui lòng nhập tên" : nil
                    )

                    // Year + Gender
                    HStack(spacing: 12) {
                        FormFieldView(
                            value: $inputBirthYear,
                            label: "Năm sinh *",
                            placeholder: "VD: 1995",
                            icon: "calendar",
                            keyboardType: .numberPad,
                            isError: yearError,
                            errorText: yearError ? "Năm không hợp lệ" : nil
                        )

                        VStack(alignment: .leading, spacing: 6) {
                            Text("Giới tính")
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(TextSub)

                            HStack(spacing: 4) {
                                ForEach(["Nam", "Nữ", "Khác"], id: \.self) { g in
                                    Text(g)
                                        .font(.system(size: 12, weight: inputGender == g ? .bold : .medium))
                                        .foregroundColor(inputGender == g ? PrimaryRed : TextSub)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 10)
                                        .background(inputGender == g ? PrimaryRed.opacity(0.12) : Color.clear)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 10)
                                                .stroke(inputGender == g ? PrimaryRed : Outline, lineWidth: 1)
                                        )
                                        .clipShape(RoundedRectangle(cornerRadius: 10))
                                        .onTapGesture { inputGender = g }
                                }
                            }
                        }
                    }

                    // Optional fields
                    Text("Thông tin thêm (không bắt buộc)")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(TextDim)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    HStack(spacing: 12) {
                        FormFieldView(value: $inputBirthDay, label: "Ngày sinh", placeholder: "DD", keyboardType: .numberPad)
                        FormFieldView(value: $inputBirthMonth, label: "Tháng sinh", placeholder: "MM", keyboardType: .numberPad)
                    }

                    HStack(spacing: 12) {
                        FormFieldView(value: $inputBirthHour, label: "Giờ sinh", placeholder: "HH", icon: "clock.fill", keyboardType: .numberPad)
                        FormFieldView(value: $inputBirthMinute, label: "Phút", placeholder: "MM", keyboardType: .numberPad)
                    }
                }

                Spacer().frame(height: 20)

                // Privacy notice
                HStack(alignment: .top, spacing: 8) {
                    Image(systemName: "lock.fill")
                        .foregroundColor(Color(hex: "388E3C"))
                        .font(.system(size: 16))
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Bảo mật tuyệt đối")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(Color(hex: "2E7D32"))
                        Text("Chúng tôi không thu thập bất kỳ thông tin cá nhân nào. Tất cả dữ liệu chỉ được lưu trữ trên điện thoại của bạn.")
                            .font(.system(size: 12))
                            .foregroundColor(Color(hex: "33691E"))
                            .lineSpacing(4)
                    }
                }
                .padding(12)
                .background(Color(hex: "E8F5E9"))
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(hex: "81C784").opacity(0.4), lineWidth: 1))
                .clipShape(RoundedRectangle(cornerRadius: 12))

                Spacer().frame(height: 28)

                // Page dots
                HStack(spacing: 8) {
                    ForEach(0..<totalPages, id: \.self) { index in
                        Capsule()
                            .fill(currentPageBinding == index ? accentColorFor(index) : Outline)
                            .frame(width: currentPageBinding == index ? 28 : 8, height: 8)
                    }
                }

                Spacer().frame(height: 16)

                // Action buttons
                HStack {
                    Button("Bỏ qua") { onSkip() }
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(TextDim)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 10)

                    Spacer()

                    Button {
                        onFinish()
                    } label: {
                        HStack(spacing: 8) {
                            Text("Bắt đầu sử dụng")
                                .font(.system(size: 15, weight: .bold))
                            Image(systemName: "checkmark")
                                .font(.system(size: 14, weight: .bold))
                        }
                        .foregroundColor(.white)
                        .padding(.horizontal, 32)
                        .padding(.vertical, 14)
                        .background(
                            LinearGradient(colors: [PrimaryRed, DeepRed], startPoint: .leading, endPoint: .trailing)
                        )
                        .clipShape(Capsule())
                    }
                }
            }
            .padding(.horizontal, 32)
            .padding(.bottom, 32)
        }
    }

    private func accentColorFor(_ index: Int) -> Color {
        if index < onboardingPages.count { return onboardingPages[index].accentColor }
        if index == onboardingPages.count { return Color(hex: "1565C0") }
        return PrimaryRed
    }
}

// ═══════════════════════════════════════════
// REUSABLE FORM FIELD
// ═══════════════════════════════════════════

struct FormFieldView: View {
    @Binding var value: String
    let label: String
    var placeholder: String = ""
    var icon: String? = nil
    var keyboardType: UIKeyboardType = .default
    var isError: Bool = false
    var errorText: String? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(isError ? Color(hex: "B71C1C") : TextSub)

            HStack(spacing: 8) {
                if let icon = icon {
                    Image(systemName: icon)
                        .font(.system(size: 16))
                        .foregroundColor(isError ? Color(hex: "B71C1C") : TextDim)
                }
                TextField(placeholder, text: $value)
                    .font(.system(size: 14))
                    .foregroundColor(TextMain)
                    .keyboardType(keyboardType)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 12)
            .background(isError ? Color(hex: "FFF0F0") : Color.white)
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(isError ? Color(hex: "B71C1C") : Outline, lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 14))

            if let errorText = errorText {
                Text(errorText)
                    .font(.system(size: 11))
                    .foregroundColor(Color(hex: "B71C1C"))
            }
        }
    }
}

#Preview {
    OnboardingScreen(onFinish: {})
}
