import SwiftUI

// ═══════════════════════════════════════════
// Profile Screen — Tab "Cá Nhân"
// Shows user profile summary + menu items
// Triggers EditProfileScreen as full-screen modal
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

struct ProfileScreen: View {
    @State private var showEditProfile = false
    @State private var showSettings = false
    @State private var showSearch = false
    @State private var showNotifications = false
    @State private var showFamilySettings = false
    @State private var showPickMember = false

    // Reactive profile data from UserDefaults
    @AppStorage("displayName") private var displayName = "Người dùng"
    @AppStorage("profile_email") private var email = ""
    @AppStorage("gender") private var gender = "Nam"
    @AppStorage("birthDay") private var birthDay = 0
    @AppStorage("birthMonth") private var birthMonth = 0
    @AppStorage("birthYear") private var birthYear = 0
    @AppStorage("birthHour") private var birthHour = -1
    @AppStorage("birthMinute") private var birthMinute = -1
    @AppStorage("profile_avatar_path") private var avatarPath = ""

    var body: some View {
        ScrollView(.vertical, showsIndicators: false) {
            VStack(spacing: 16) {
                Spacer().frame(height: 16)

                // ═══ PROFILE HEADER CARD ═══
                ProfileHeaderCard(
                    displayName: displayName,
                    email: email,
                    avatarPath: avatarPath,
                    onEditTap: { showEditProfile = true }
                )

                // ═══ BIRTH INFO ═══
                if birthYear > 0, birthMonth > 0, birthDay > 0 {
                    BirthInfoCard(
                        day: birthDay,
                        month: birthMonth,
                        year: birthYear,
                        hour: birthHour,
                        gender: gender
                    )
                }

                // ═══ MENU SECTIONS ═══
                PersonalMenuSection(
                    onNotificationsTap: { showNotifications = true },
                    onSearchTap: { showSearch = true }
                )

                FamilyTreeMenuSection(
                    onFamilySettingsTap: { showFamilySettings = true },
                    onPickMemberTap: { showPickMember = true }
                )

                SettingsMenuSection(onSettingsTap: { showSettings = true })

                Spacer().frame(height: 100)
            }
            .padding(.horizontal, 16)
        }
        .background(SurfaceBg)
        .fullScreenCover(isPresented: $showEditProfile) {
            EditProfileScreen()
        }
        .fullScreenCover(isPresented: $showSettings) {
            SettingsScreen()
        }
        .sheet(isPresented: $showNotifications) {
            NavigationStack {
                NotificationsScreen()
            }
        }
        .sheet(isPresented: $showSearch) {
            NavigationStack {
                SearchScreen()
            }
        }
        .sheet(isPresented: $showFamilySettings) {
            NavigationStack {
                FamilySettingsScreen(viewModel: FamilyTreeViewModel())
            }
        }
        .sheet(isPresented: $showPickMember) {
            NavigationStack {
                PickMemberScreen(viewModel: FamilyTreeViewModel(), onSelect: { _ in
                    showPickMember = false
                })
            }
        }
    }
}

// ══════════════════════════════════════════
// PROFILE HEADER CARD
// ══════════════════════════════════════════

private struct ProfileHeaderCard: View {
    let displayName: String
    let email: String
    let avatarPath: String
    let onEditTap: () -> Void

    var body: some View {
        HStack(spacing: 16) {
            // Avatar
            AvatarView(name: displayName, path: avatarPath, size: 72)

            // Name + Email
            VStack(alignment: .leading, spacing: 4) {
                Text(displayName)
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(TextMain)

                if !email.isEmpty {
                    Text(email)
                        .font(.system(size: 13))
                        .foregroundColor(TextDim)
                }
            }

            Spacer()

            // Edit button
            Button(action: onEditTap) {
                Image(systemName: "pencil")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(PrimaryRed)
                    .padding(10)
                    .background(PrimaryContainer.opacity(0.6))
                    .clipShape(Circle())
            }
        }
        .padding(20)
        .background(
            LinearGradient(
                colors: [SurfaceContainer, SurfaceContainerHigh.opacity(0.5)],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(OutlineVariant.opacity(0.5), lineWidth: 1)
        )
    }
}

// ══════════════════════════════════════════
// AVATAR VIEW (reusable)
// ══════════════════════════════════════════

struct AvatarView: View {
    let name: String
    let path: String
    let size: CGFloat

    private var initials: String {
        let parts = name.trimmingCharacters(in: .whitespaces).split(separator: " ").compactMap { $0.first.map(String.init) }
        if parts.count >= 2 { return "\(parts.first!)\(parts.last!)" }
        return parts.first ?? "?"
    }

    var body: some View {
        if !path.isEmpty, FileManager.default.fileExists(atPath: path),
           let img = UIImage(contentsOfFile: path) {
            Image(uiImage: img)
                .resizable()
                .scaledToFill()
                .frame(width: size, height: size)
                .clipShape(Circle())
                .overlay(Circle().stroke(SurfaceContainer, lineWidth: 2))
        } else {
            Circle()
                .fill(
                    LinearGradient(
                        colors: [PrimaryRed, DeepRed],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .frame(width: size, height: size)
                .overlay(
                    Text(initials)
                        .font(.system(size: size * 0.35, weight: .bold, design: .serif))
                        .foregroundColor(.white)
                )
                .overlay(Circle().stroke(SurfaceContainer, lineWidth: 2))
        }
    }
}

// ══════════════════════════════════════════
// BIRTH INFO CARD
// ══════════════════════════════════════════

private struct BirthInfoCard: View {
    let day: Int
    let month: Int
    let year: Int
    let hour: Int
    let gender: String

    var body: some View {
        let info = calculateBirthInfo()

        VStack(alignment: .leading, spacing: 12) {
            // Header
            HStack(spacing: 8) {
                Image(systemName: "sparkles")
                    .font(.system(size: 14))
                    .foregroundColor(GoldAccent)
                Text("Dựa trên ngày sinh \(String(format: "%02d/%02d/%d", day, month, year))")
                    .font(.system(size: 12))
                    .foregroundColor(TextSub)
            }
            .padding(12)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                LinearGradient(
                    colors: [Color(hex: "FFF8E1"), Color(hex: "FFFDE7")],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color(hex: "FFE082"), lineWidth: 1)
            )

            // Info chips
            HStack(spacing: 10) {
                InfoChip(label: "Tuổi", value: info.yearCanChi)
                InfoChip(label: "Mệnh", value: info.menh)
                InfoChip(label: "Con giáp", value: "\(info.conGiapEmoji) \(info.conGiap)")
            }

            HStack(spacing: 10) {
                InfoChip(label: "Cung", value: info.cung)
                InfoChip(label: "Ngũ hành", value: info.nguHanh)
            }
        }
        .padding(16)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(OutlineVariant.opacity(0.5), lineWidth: 1)
        )
    }

    private func calculateBirthInfo() -> (yearCanChi: String, menh: String, nguHanh: String, conGiap: String, conGiapEmoji: String, cung: String) {
        let lunarYear = year
        let yearCanChi = CanChiCalculator.getYearCanChi(lunarYear: lunarYear)

        let chiIndex = ((lunarYear + 8) % 12 + 12) % 12
        let conGiapNames = ["Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"]
        let conGiapEmojis = ["🐭", "🐮", "🐯", "🐱", "🐲", "🐍", "🐴", "🐐", "🐒", "🐔", "🐶", "🐷"]

        // Nap Am
        let napAmList = [
            "Hải Trung Kim", "Hải Trung Kim", "Lư Trung Hỏa", "Lư Trung Hỏa",
            "Đại Lâm Mộc", "Đại Lâm Mộc", "Lộ Bàng Thổ", "Lộ Bàng Thổ",
            "Kiếm Phong Kim", "Kiếm Phong Kim", "Sơn Đầu Hỏa", "Sơn Đầu Hỏa",
            "Giản Hạ Thủy", "Giản Hạ Thủy", "Thành Đầu Thổ", "Thành Đầu Thổ",
            "Bạch Lạp Kim", "Bạch Lạp Kim", "Dương Liễu Mộc", "Dương Liễu Mộc",
            "Tuyền Trung Thủy", "Tuyền Trung Thủy", "Ốc Thượng Thổ", "Ốc Thượng Thổ",
            "Tích Lịch Hỏa", "Tích Lịch Hỏa", "Tùng Bách Mộc", "Tùng Bách Mộc",
            "Trường Lưu Thủy", "Trường Lưu Thủy", "Sa Trung Kim", "Sa Trung Kim",
            "Sơn Hạ Hỏa", "Sơn Hạ Hỏa", "Bình Địa Mộc", "Bình Địa Mộc",
            "Bích Thượng Thổ", "Bích Thượng Thổ", "Kim Bạch Kim", "Kim Bạch Kim",
            "Phúc Đăng Hỏa", "Phúc Đăng Hỏa", "Thiên Hà Thủy", "Thiên Hà Thủy",
            "Đại Dịch Thổ", "Đại Dịch Thổ", "Thoa Xuyến Kim", "Thoa Xuyến Kim",
            "Tang Đố Mộc", "Tang Đố Mộc", "Đại Khê Thủy", "Đại Khê Thủy",
            "Sa Trung Thổ", "Sa Trung Thổ", "Thiên Thượng Hỏa", "Thiên Thượng Hỏa",
            "Thạch Lựu Mộc", "Thạch Lựu Mộc", "Đại Hải Thủy", "Đại Hải Thủy"
        ]
        let napIndex = ((lunarYear - 4) % 60 + 60) % 60
        let nguHanh = napIndex < napAmList.count ? napAmList[napIndex] : ""

        let menh: String
        if nguHanh.contains("Kim") { menh = "Kim" }
        else if nguHanh.contains("Mộc") { menh = "Mộc" }
        else if nguHanh.contains("Thủy") { menh = "Thủy" }
        else if nguHanh.contains("Hỏa") { menh = "Hỏa" }
        else if nguHanh.contains("Thổ") { menh = "Thổ" }
        else { menh = "" }

        // Cung Bát Trạch
        let cungNames = ["Khảm", "Ly", "Cấn", "Đoài", "Càn", "Khôn", "Tốn", "Chấn", "Trung Cung"]
        let sum = digitSum(lunarYear)
        let isMale = gender != "Nữ"
        let cungIndex: Int
        if isMale {
            let v = (11 - sum % 9) % 9
            cungIndex = v == 0 ? 8 : v - 1
        } else {
            let v = (sum + 4) % 9
            cungIndex = v == 0 ? 8 : v - 1
        }
        let cung = cungIndex < cungNames.count ? cungNames[cungIndex] : "Khảm"

        return (yearCanChi, menh, nguHanh, conGiapNames[chiIndex], conGiapEmojis[chiIndex], cung)
    }

    private func digitSum(_ n: Int) -> Int {
        var s = 0; var v = abs(n)
        while v > 0 { s += v % 10; v /= 10 }
        while s >= 10 { var ns = 0; var sv = s; while sv > 0 { ns += sv % 10; sv /= 10 }; s = ns }
        return s
    }
}

private struct InfoChip: View {
    let label: String
    let value: String

    var body: some View {
        VStack(spacing: 2) {
            Text(label)
                .font(.system(size: 10, weight: .medium))
                .foregroundColor(TextDim)
            Text(value)
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(PrimaryRed)
                .lineLimit(1)
                .minimumScaleFactor(0.7)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 10)
        .background(SurfaceBg)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(OutlineVariant, lineWidth: 1)
        )
    }
}

// ══════════════════════════════════════════
// MENU SECTIONS
// ══════════════════════════════════════════

private struct PersonalMenuSection: View {
    var onNotificationsTap: () -> Void = {}
    var onSearchTap: () -> Void = {}

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionHeader(icon: "folder.fill", title: "Cá nhân")

            MenuCard {
                MenuItem(iconBg: Color(hex: "FFEBEE"), iconColor: Color(hex: "C62828"),
                         icon: "bookmark.fill", title: "Ngày đã lưu", desc: "Các ngày quan trọng")
                MenuDivider()
                MenuItem(iconBg: Color(hex: "FFF8E1"), iconColor: Color(hex: "F57F17"),
                         icon: "bell.fill", title: "Thông báo", desc: "Nhắc nhở và thông báo",
                         action: onNotificationsTap)
                MenuDivider()
                MenuItem(iconBg: Color(hex: "E8F5E9"), iconColor: Color(hex: "2E7D32"),
                         icon: "magnifyingglass", title: "Tìm kiếm", desc: "Tra cứu ngày, lễ, âm lịch",
                         action: onSearchTap)
            }
        }
    }
}

private struct FamilyTreeMenuSection: View {
    var onFamilySettingsTap: () -> Void = {}
    var onPickMemberTap: () -> Void = {}

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionHeader(icon: "person.3.fill", title: "Gia phả")

            MenuCard {
                MenuItem(iconBg: Color(hex: "EFEBE9"), iconColor: Color(hex: "5D4037"),
                         icon: "person.3.fill", title: "Thành viên gia phả", desc: "Xem & quản lý thành viên",
                         action: onPickMemberTap)
                MenuDivider()
                MenuItem(iconBg: Color(hex: "FFF8E1"), iconColor: Color(hex: "D4A017"),
                         icon: "gearshape.2.fill", title: "Cài đặt gia phả", desc: "Tên dòng họ, hiển thị, nhắc nhở",
                         action: onFamilySettingsTap)
            }
        }
    }
}

private struct SettingsMenuSection: View {
    var onSettingsTap: () -> Void = {}

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionHeader(icon: "gearshape.fill", title: "Cài đặt")

            MenuCard {
                MenuItem(iconBg: Color(hex: "F3E5F5"), iconColor: Color(hex: "6A1B9A"),
                         icon: "gearshape.fill", title: "Cài đặt ứng dụng", desc: "Giao diện, thông báo, hiển thị",
                         action: onSettingsTap)
                MenuDivider()
                MenuItem(iconBg: Color(hex: "E3F2FD"), iconColor: Color(hex: "1565C0"),
                         icon: "arrow.up.doc.fill", title: "Sao lưu dữ liệu", desc: "Xuất toàn bộ ra file JSON")
                MenuDivider()
                MenuItem(iconBg: Color(hex: "F3E5F5"), iconColor: Color(hex: "6A1B9A"),
                         icon: "arrow.down.doc.fill", title: "Phục hồi dữ liệu", desc: "Khôi phục từ file sao lưu")
                MenuDivider()
                MenuItem(iconBg: Color(hex: "FFF3E0"), iconColor: Color(hex: "E65100"),
                         icon: "info.circle.fill", title: "Về ứng dụng", desc: "Lịch Số v2.0")
            }
        }
    }
}

private struct SectionHeader: View {
    let icon: String
    let title: String

    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundColor(PrimaryRed)
            Text(title)
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(TextMain)
        }
    }
}

private struct MenuCard<Content: View>: View {
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(spacing: 0) {
            content()
        }
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(OutlineVariant.opacity(0.5), lineWidth: 1)
        )
    }
}

private struct MenuItem: View {
    let iconBg: Color
    let iconColor: Color
    let icon: String
    let title: String
    let desc: String
    var action: () -> Void = {}

    var body: some View {
        Button(action: action) {
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.system(size: 16))
                    .foregroundColor(iconColor)
                    .frame(width: 36, height: 36)
                    .background(iconBg)
                    .clipShape(RoundedRectangle(cornerRadius: 10))

                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(TextMain)
                    Text(desc)
                        .font(.system(size: 11))
                        .foregroundColor(TextDim)
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(OutlineVariant)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
        }
    }
}

private struct MenuDivider: View {
    var body: some View {
        Rectangle()
            .fill(OutlineVariant.opacity(0.5))
            .frame(height: 0.5)
            .padding(.leading, 64)
    }
}
