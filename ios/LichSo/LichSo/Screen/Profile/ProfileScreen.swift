import SwiftUI
import SwiftData
import UIKit
import UniformTypeIdentifiers

// ═══════════════════════════════════════════
// Profile Screen — Tab "Cá Nhân"
// Designed to match Android version:
// - Red gradient header with avatar, name, meta chips, action buttons
// - Stats row (bookmarks, reminders, notes)
// - Birth info cards (can chi, mệnh, cung...)
// - Bookmarks horizontal preview
// - Menu sections
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
    @State private var showFamilyTree = false
    @State private var showFamilySettings = false
    @State private var showPickMember = false
    @State private var showAddBookmark = false
    @State private var showAbout = false
    @State private var isBackingUp = false
    @State private var isRestoring = false
    @State private var showBackupSuccess = false
    @State private var showRestoreConfirm = false
    @State private var showRestoreSuccess = false
    @State private var showRestoreError = false
    @State private var restoreSummary = ""
    @State private var pendingRestoreData: AppBackupManager.AppBackupData?
    @State private var showDocumentPicker = false
    @State private var backupMessage = ""
    @StateObject private var familyTreeVM = FamilyTreeViewModel()

    @AppStorage("displayName")         private var displayName = "Người dùng"
    @AppStorage("profile_email")       private var email       = ""
    @AppStorage("gender")              private var gender      = "Nam"
    @AppStorage("birthDay")            private var birthDay    = 0
    @AppStorage("birthMonth")          private var birthMonth  = 0
    @AppStorage("birthYear")           private var birthYear   = 0
    @AppStorage("birthHour")           private var birthHour   = -1
    @AppStorage("birthMinute")         private var birthMinute = -1
    @AppStorage("profile_avatar_path") private var avatarPath  = ""

    @Query private var allBookmarks: [BookmarkEntity]
    @Query private var allReminders: [ReminderEntity]
    @Query private var allNotes:     [NoteEntity]
    @Environment(\.modelContext) private var modelContext

    var activeReminderCount: Int { allReminders.filter { $0.isEnabled }.count }

    var body: some View {
        ZStack(alignment: .top) {
            SurfaceBg.ignoresSafeArea()
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 0) {
                    // ═══ RED HEADER ═══
                    ProfileHeader(
                        displayName: displayName,
                        avatarPath: avatarPath,
                        birthYear: birthYear,
                        gender: gender,
                        onSettings: { showSettings = true },
                        onEditProfile: { showEditProfile = true },
                        onFamilyTree: { showFamilyTree = true }
                    )

                    // ═══ CONTENT ═══
                    VStack(spacing: 16) {
                        // Stats row
                        ProfileStatsRow(
                            bookmarkCount: allBookmarks.count,
                            reminderCount: activeReminderCount,
                            noteCount: allNotes.count
                        )

                        // Birth info
                        if birthYear > 0, birthMonth > 0, birthDay > 0 {
                            ProfileBirthInfoSection(
                                day: birthDay, month: birthMonth, year: birthYear,
                                hour: birthHour, gender: gender
                            )
                        }

                        // Bookmarks preview
                        ProfileBookmarksSection(
                            bookmarks: allBookmarks,
                            onViewAll: { showSearch = true },
                            onAdd: { showAddBookmark = true },
                            onRemove: { bm in modelContext.delete(bm) }
                        )

                        // Menu sections
                        ProfileMenuSection(
                            noteCount: allNotes.count,
                            reminderCount: activeReminderCount,
                            bookmarkCount: allBookmarks.count,
                            onNotificationsTap: { showNotifications = true },
                            onSearchTap: { showSearch = true },
                            onFamilySettingsTap: { showFamilySettings = true },
                            onPickMemberTap: { showPickMember = true },
                            onSettingsTap: { showSettings = true },
                            onBackupTap: { performBackup() },
                            onRestoreTap: { showDocumentPicker = true },
                            onAboutTap: { showAbout = true },
                            isBackingUp: isBackingUp,
                            isRestoring: isRestoring
                        )

                        Spacer().frame(height: 100)
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 16)
                }
            }
        }
        .ignoresSafeArea(edges: .top)
        .fullScreenCover(isPresented: $showEditProfile) { EditProfileScreen() }
        .fullScreenCover(isPresented: $showSettings)    { SettingsScreen() }
        .sheet(isPresented: $showNotifications) { NavigationStack { NotificationsScreen() } }
        .sheet(isPresented: $showSearch)        { NavigationStack { SearchScreen() } }
        .sheet(isPresented: $showFamilyTree) {
            NavigationStack { FamilyTreeScreen() }
        }
        .sheet(isPresented: $showFamilySettings) {
            NavigationStack { FamilySettingsScreen(viewModel: familyTreeVM) }
        }
        .sheet(isPresented: $showPickMember) {
            NavigationStack {
                PickMemberScreen(viewModel: familyTreeVM, onSelect: { _ in showPickMember = false })
            }
        }
        .sheet(isPresented: $showAddBookmark) {
            AddBookmarkSheet { d, m, y, lbl, nt in
                let bm = BookmarkEntity(
                    id: Int64(Date().timeIntervalSince1970 * 1000),
                    solarDay: d, solarMonth: m, solarYear: y, label: lbl, note: nt
                )
                modelContext.insert(bm)
                showAddBookmark = false
            }
        }
        .sheet(isPresented: $showAbout) {
            ProfileAboutSheet()
                .presentationDetents([.medium])
                .presentationDragIndicator(.visible)
        }
        .sheet(isPresented: $showDocumentPicker) {
            BackupDocumentPicker { url in
                handleRestoreFile(url: url)
            }
        }
        .alert("Sao lưu thành công", isPresented: $showBackupSuccess) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(backupMessage)
        }
        .alert("Phục hồi dữ liệu?", isPresented: $showRestoreConfirm) {
            Button("Phục hồi", role: .destructive) { performRestore() }
            Button("Huỷ", role: .cancel) { pendingRestoreData = nil }
        } message: {
            Text("Dữ liệu hiện tại sẽ bị ghi đè.\n\n\(restoreSummary)")
        }
        .alert("Phục hồi thành công", isPresented: $showRestoreSuccess) {
            Button("OK", role: .cancel) {}
        } message: {
            Text("✅ Đã phục hồi dữ liệu thành công!\nVui lòng khởi động lại ứng dụng để áp dụng.")
        }
        .alert("Lỗi phục hồi", isPresented: $showRestoreError) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(backupMessage)
        }
    }

    // MARK: - Backup

    private func performBackup() {
        isBackingUp = true
        DispatchQueue.global(qos: .userInitiated).async {
            let json = AppBackupManager.buildBackupJSON(context: modelContext)
            DispatchQueue.main.async {
                isBackingUp = false
                guard let json = json else {
                    backupMessage = "Lỗi tạo dữ liệu sao lưu."
                    showRestoreError = true
                    return
                }
                let fileName = AppBackupManager.generateFileName()
                let tmpDir = FileManager.default.temporaryDirectory
                let tmpURL = tmpDir.appendingPathComponent(fileName)
                do {
                    try json.write(to: tmpURL, atomically: true, encoding: .utf8)
                    presentShareSheet(items: [tmpURL])
                    let data = AppBackupManager.buildBackupData(context: modelContext)
                    let summary = AppBackupManager.getBackupSummary(data)
                    backupMessage = "✅ Đã xuất file sao lưu thành công!\n\n\(summary)\n\nFile: \(fileName)"
                    showBackupSuccess = true
                } catch {
                    backupMessage = "Lỗi ghi file: \(error.localizedDescription)"
                    showRestoreError = true
                }
            }
        }
    }

    private func presentShareSheet(items: [Any]) {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootVC = windowScene.windows.first?.rootViewController else { return }
        let av = UIActivityViewController(activityItems: items, applicationActivities: nil)
        if let popover = av.popoverPresentationController {
            popover.sourceView = rootVC.view
            popover.sourceRect = CGRect(x: rootVC.view.bounds.midX, y: rootVC.view.bounds.midY, width: 0, height: 0)
        }
        // Find the topmost presented VC
        var topVC = rootVC
        while let presented = topVC.presentedViewController { topVC = presented }
        topVC.present(av, animated: true)
    }

    // MARK: - Restore

    private func handleRestoreFile(url: URL) {
        guard url.startAccessingSecurityScopedResource() else {
            backupMessage = "Không thể truy cập file."
            showRestoreError = true
            return
        }
        defer { url.stopAccessingSecurityScopedResource() }

        do {
            let json = try String(contentsOf: url, encoding: .utf8)
            guard let data = AppBackupManager.parseBackupJSON(json) else {
                backupMessage = "File không phải bản sao lưu Lịch Số hợp lệ."
                showRestoreError = true
                return
            }
            if data.type != "full_backup" || data.appId != "com.lichso.app" {
                backupMessage = "File không phải bản sao lưu Lịch Số."
                showRestoreError = true
                return
            }
            pendingRestoreData = data
            restoreSummary = AppBackupManager.getBackupSummary(data)
            showRestoreConfirm = true
        } catch {
            backupMessage = "Không thể đọc file: \(error.localizedDescription)"
            showRestoreError = true
        }
    }

    private func performRestore() {
        guard let data = pendingRestoreData else { return }
        isRestoring = true
        DispatchQueue.global(qos: .userInitiated).async {
            AppBackupManager.restoreFromBackup(data, context: modelContext)
            DispatchQueue.main.async {
                isRestoring = false
                pendingRestoreData = nil
                showRestoreSuccess = true
            }
        }
    }
}

// ══════════════════════════════════════════
// PROFILE HEADER — Red gradient
// ══════════════════════════════════════════

private struct ProfileHeader: View {
    let displayName: String
    let avatarPath: String
    let birthYear: Int
    let gender: String
    let onSettings: () -> Void
    let onEditProfile: () -> Void
    let onFamilyTree: () -> Void

    private var headerBirthInfo: (yearCanChi: String, menh: String)? {
        guard birthYear > 0 else { return nil }
        let canChi = CanChiCalculator.getYearCanChi(lunarYear: birthYear)
        let napAmList = [
            "Hải Trung Kim","Hải Trung Kim","Lư Trung Hỏa","Lư Trung Hỏa",
            "Đại Lâm Mộc","Đại Lâm Mộc","Lộ Bàng Thổ","Lộ Bàng Thổ",
            "Kiếm Phong Kim","Kiếm Phong Kim","Sơn Đầu Hỏa","Sơn Đầu Hỏa",
            "Giản Hạ Thủy","Giản Hạ Thủy","Thành Đầu Thổ","Thành Đầu Thổ",
            "Bạch Lạp Kim","Bạch Lạp Kim","Dương Liễu Mộc","Dương Liễu Mộc",
            "Tuyền Trung Thủy","Tuyền Trung Thủy","Ốc Thượng Thổ","Ốc Thượng Thổ",
            "Tích Lịch Hỏa","Tích Lịch Hỏa","Tùng Bách Mộc","Tùng Bách Mộc",
            "Trường Lưu Thủy","Trường Lưu Thủy","Sa Trung Kim","Sa Trung Kim",
            "Sơn Hạ Hỏa","Sơn Hạ Hỏa","Bình Địa Mộc","Bình Địa Mộc",
            "Bích Thượng Thổ","Bích Thượng Thổ","Kim Bạch Kim","Kim Bạch Kim",
            "Phúc Đăng Hỏa","Phúc Đăng Hỏa","Thiên Hà Thủy","Thiên Hà Thủy",
            "Đại Dịch Thổ","Đại Dịch Thổ","Thoa Xuyến Kim","Thoa Xuyến Kim",
            "Tang Đố Mộc","Tang Đố Mộc","Đại Khê Thủy","Đại Khê Thủy",
            "Sa Trung Thổ","Sa Trung Thổ","Thiên Thượng Hỏa","Thiên Thượng Hỏa",
            "Thạch Lựu Mộc","Thạch Lựu Mộc","Đại Hải Thủy","Đại Hải Thủy"
        ]
        let napIndex = ((birthYear - 4) % 60 + 60) % 60
        let nguHanh = napIndex < napAmList.count ? napAmList[napIndex] : ""
        var menh = ""
        if nguHanh.contains("Kim")       { menh = "Kim" }
        else if nguHanh.contains("Mộc")  { menh = "Mộc" }
        else if nguHanh.contains("Thủy") { menh = "Thủy" }
        else if nguHanh.contains("Hỏa")  { menh = "Hỏa" }
        else if nguHanh.contains("Thổ")  { menh = "Thổ" }
        return (canChi, menh)
    }

    var body: some View {
        ZStack(alignment: .top) {
            LinearGradient(colors: [Color(red: 0.773, green: 0.157, blue: 0.157),
                                    Color(red: 0.545, green: 0, blue: 0)],
                           startPoint: .top, endPoint: .bottom)
                .ignoresSafeArea(edges: .top)

            VStack(spacing: 0) {
                Color.clear.frame(height: safeAreaTop())

                // Top bar
                HStack {
                    Image(systemName: "line.3.horizontal")
                        .font(.system(size: 20, weight: .medium))
                        .foregroundColor(.white)
                        .frame(width: 40, height: 40)
                        .background(Color.white.opacity(0.15))
                        .clipShape(Circle())
                    Spacer()
                    Text("Hồ sơ")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.white)
                    Spacer()
                    Button(action: onSettings) {
                        Image(systemName: "gearshape")
                            .font(.system(size: 20, weight: .medium))
                            .foregroundColor(.white)
                            .frame(width: 40, height: 40)
                            .background(Color.white.opacity(0.15))
                            .clipShape(Circle())
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)

                Spacer().frame(height: 16)

                AvatarView(name: displayName, path: avatarPath, size: 80)
                    .overlay(Circle().stroke(Color.white.opacity(0.3), lineWidth: 2))

                Spacer().frame(height: 12)

                Text(displayName)
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.white)

                if let info = headerBirthInfo, !info.yearCanChi.isEmpty {
                    HStack(spacing: 8) {
                        ProfileMetaChip(icon: "🎂", text: "\(info.yearCanChi) \(birthYear)")
                        ProfileMetaChip(icon: "⭐", text: "Mệnh \(info.menh)")
                    }
                    .padding(.top, 10)
                }

                Spacer().frame(height: 14)

                HStack(spacing: 10) {
                    ProfileActionButton(icon: "pencil",        label: "Sửa hồ sơ", action: onEditProfile)
                    ProfileActionButton(icon: "person.3.fill", label: "Gia phả",   action: onFamilyTree)
                }

                Spacer().frame(height: 24)
            }
        }
        .fixedSize(horizontal: false, vertical: true)
    }

    private func safeAreaTop() -> CGFloat {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first?.windows.first?.safeAreaInsets.top ?? 44
    }
}

private struct ProfileMetaChip: View {
    let icon: String
    let text: String
    var body: some View {
        HStack(spacing: 5) {
            Text(icon).font(.system(size: 13))
            Text(text).font(.system(size: 12, weight: .medium)).foregroundColor(.white)
        }
        .padding(.horizontal, 12).padding(.vertical, 6)
        .background(Color.white.opacity(0.2))
        .clipShape(Capsule())
        .overlay(Capsule().stroke(Color.white.opacity(0.3), lineWidth: 1))
    }
}

private struct ProfileActionButton: View {
    let icon: String
    let label: String
    let action: () -> Void
    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                Image(systemName: icon).font(.system(size: 13, weight: .semibold))
                Text(label).font(.system(size: 13, weight: .semibold))
            }
            .foregroundColor(.white)
            .padding(.horizontal, 20).padding(.vertical, 9)
            .background(Color.white.opacity(0.15))
            .overlay(RoundedRectangle(cornerRadius: 20).stroke(Color.white.opacity(0.3), lineWidth: 1))
            .clipShape(RoundedRectangle(cornerRadius: 20))
        }
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
        let parts = name.trimmingCharacters(in: .whitespaces)
            .split(separator: " ").compactMap { $0.first.map(String.init) }
        if parts.count >= 2 { return "\(parts.first!)\(parts.last!)" }
        return parts.first ?? "?"
    }

    var body: some View {
        Group {
            if !path.isEmpty, FileManager.default.fileExists(atPath: path),
               let img = UIImage(contentsOfFile: path) {
                Image(uiImage: img).resizable().scaledToFill()
            } else {
                LinearGradient(colors: [PrimaryRed, DeepRed],
                               startPoint: .topLeading, endPoint: .bottomTrailing)
                    .overlay(
                        Text(initials)
                            .font(.system(size: size * 0.35, weight: .bold, design: .serif))
                            .foregroundColor(.white)
                    )
            }
        }
        .frame(width: size, height: size)
        .clipShape(Circle())
    }
}

// ══════════════════════════════════════════
// STATS ROW
// ══════════════════════════════════════════

private struct ProfileStatsRow: View {
    let bookmarkCount: Int
    let reminderCount: Int
    let noteCount: Int
    var body: some View {
        HStack(spacing: 10) {
            StatCard(value: bookmarkCount, label: "Ngày đã\nlưu")
            StatCard(value: reminderCount, label: "Nhắc nhở")
            StatCard(value: noteCount,     label: "Ghi chú")
        }
    }
}

private struct StatCard: View {
    let value: Int
    let label: String
    var body: some View {
        VStack(spacing: 4) {
            Text("\(value)")
                .font(.system(size: 26, weight: .bold)).foregroundColor(PrimaryRed)
            Text(label)
                .font(.system(size: 12)).foregroundColor(TextDim).multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity).padding(.vertical, 14)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant.opacity(0.5), lineWidth: 1))
    }
}

// ══════════════════════════════════════════
// BIRTH INFO SECTION
// ══════════════════════════════════════════

private struct ProfileBirthInfoSection: View {
    let day: Int
    let month: Int
    let year: Int
    let hour: Int
    let gender: String

    var body: some View {
        let info = calculateBirthInfo()
        VStack(spacing: 10) {
            // Yellow hint
            HStack(alignment: .top, spacing: 10) {
                Text("✨").font(.system(size: 16))
                Text("Dựa trên ngày sinh \(String(format: "%02d/%02d/%d", day, month, year)), hệ thống tự động tính:")
                    .font(.system(size: 12)).foregroundColor(TextSub)
                    .fixedSize(horizontal: false, vertical: true)
            }
            .padding(14).frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(red: 1, green: 0.973, blue: 0.882))
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(RoundedRectangle(cornerRadius: 14)
                .stroke(Color(red: 1, green: 0.878, blue: 0.51), lineWidth: 1))

            // Row 1: Tuổi, Mệnh, Con giáp
            HStack(spacing: 10) {
                CalcCard(label: "Tuổi",     value: info.yearCanChi)
                CalcCard(label: "Mệnh",     value: info.menh)
                CalcCard(label: "Con giáp", value: "\(info.conGiapEmoji) \(info.conGiap)")
            }
            // Row 2: Cung, Ngũ hành
            HStack(spacing: 10) {
                CalcCard(label: "Cung",     value: info.cung)
                CalcCard(label: "Ngũ hành", value: info.nguHanh)
            }
        }
    }

    private func calculateBirthInfo() -> (yearCanChi: String, menh: String, nguHanh: String,
                                          conGiap: String, conGiapEmoji: String, cung: String) {
        let lunarYear = year
        let yearCanChi = CanChiCalculator.getYearCanChi(lunarYear: lunarYear)
        let chiIndex = ((lunarYear + 8) % 12 + 12) % 12
        let conGiapNames  = ["Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi"]
        let conGiapEmojis = ["🐭","🐮","🐯","🐱","🐲","🐍","🐴","🐐","🐒","🐔","🐶","🐷"]
        let napAmList = [
            "Hải Trung Kim","Hải Trung Kim","Lư Trung Hỏa","Lư Trung Hỏa",
            "Đại Lâm Mộc","Đại Lâm Mộc","Lộ Bàng Thổ","Lộ Bàng Thổ",
            "Kiếm Phong Kim","Kiếm Phong Kim","Sơn Đầu Hỏa","Sơn Đầu Hỏa",
            "Giản Hạ Thủy","Giản Hạ Thủy","Thành Đầu Thổ","Thành Đầu Thổ",
            "Bạch Lạp Kim","Bạch Lạp Kim","Dương Liễu Mộc","Dương Liễu Mộc",
            "Tuyền Trung Thủy","Tuyền Trung Thủy","Ốc Thượng Thổ","Ốc Thượng Thổ",
            "Tích Lịch Hỏa","Tích Lịch Hỏa","Tùng Bách Mộc","Tùng Bách Mộc",
            "Trường Lưu Thủy","Trường Lưu Thủy","Sa Trung Kim","Sa Trung Kim",
            "Sơn Hạ Hỏa","Sơn Hạ Hỏa","Bình Địa Mộc","Bình Địa Mộc",
            "Bích Thượng Thổ","Bích Thượng Thổ","Kim Bạch Kim","Kim Bạch Kim",
            "Phúc Đăng Hỏa","Phúc Đăng Hỏa","Thiên Hà Thủy","Thiên Hà Thủy",
            "Đại Dịch Thổ","Đại Dịch Thổ","Thoa Xuyến Kim","Thoa Xuyến Kim",
            "Tang Đố Mộc","Tang Đố Mộc","Đại Khê Thủy","Đại Khê Thủy",
            "Sa Trung Thổ","Sa Trung Thổ","Thiên Thượng Hỏa","Thiên Thượng Hỏa",
            "Thạch Lựu Mộc","Thạch Lựu Mộc","Đại Hải Thủy","Đại Hải Thủy"
        ]
        let napIndex = ((lunarYear - 4) % 60 + 60) % 60
        let nguHanh = napIndex < napAmList.count ? napAmList[napIndex] : ""
        var menh = ""
        if nguHanh.contains("Kim")       { menh = "Kim" }
        else if nguHanh.contains("Mộc")  { menh = "Mộc" }
        else if nguHanh.contains("Thủy") { menh = "Thủy" }
        else if nguHanh.contains("Hỏa")  { menh = "Hỏa" }
        else if nguHanh.contains("Thổ")  { menh = "Thổ" }

        let cungNames = ["Khảm","Ly","Cấn","Đoài","Càn","Khôn","Tốn","Chấn","Trung Cung"]
        let sum = digitSum(lunarYear)
        let isMale = gender != "Nữ"
        let cungIndex: Int
        if isMale { let v = (11 - sum % 9) % 9; cungIndex = v == 0 ? 8 : v - 1 }
        else       { let v = (sum + 4) % 9;      cungIndex = v == 0 ? 8 : v - 1 }
        let cungName = cungIndex < cungNames.count ? cungNames[cungIndex] : "Khảm"
        let cung = "\(cungName) (\(isMale ? "Nam" : "Nữ"))"
        return (yearCanChi, menh, nguHanh, conGiapNames[chiIndex], conGiapEmojis[chiIndex], cung)
    }

    private func digitSum(_ n: Int) -> Int {
        var s = 0; var v = abs(n)
        while v > 0 { s += v % 10; v /= 10 }
        while s >= 10 { var ns = 0; var sv = s; while sv > 0 { ns += sv % 10; sv /= 10 }; s = ns }
        return s
    }
}

private struct CalcCard: View {
    let label: String
    let value: String
    var body: some View {
        VStack(spacing: 3) {
            Text(label).font(.system(size: 10, weight: .medium)).foregroundColor(TextDim)
            Text(value).font(.system(size: 14, weight: .bold)).foregroundColor(PrimaryRed)
                .multilineTextAlignment(.center).lineLimit(2).minimumScaleFactor(0.7)
        }
        .frame(maxWidth: .infinity).padding(.vertical, 12).padding(.horizontal, 8)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(OutlineVariant, lineWidth: 1))
    }
}

// ══════════════════════════════════════════
// BOOKMARKS SECTION
// ══════════════════════════════════════════

private struct ProfileBookmarksSection: View {
    let bookmarks: [BookmarkEntity]
    let onViewAll: () -> Void
    let onAdd: () -> Void
    let onRemove: (BookmarkEntity) -> Void

    private var sortedBookmarks: [BookmarkEntity] {
        Array(bookmarks.sorted {
            ($0.solarYear * 10000 + $0.solarMonth * 100 + $0.solarDay) <
            ($1.solarYear * 10000 + $1.solarMonth * 100 + $1.solarDay)
        }.prefix(10))
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Image(systemName: "bookmark.fill").font(.system(size: 14)).foregroundColor(PrimaryRed)
                Text("Ngày đã lưu").font(.system(size: 15, weight: .bold)).foregroundColor(TextMain)
                Spacer()
                if !bookmarks.isEmpty {
                    Button(action: onViewAll) {
                        Image(systemName: "arrow.up.right.square").font(.system(size: 16)).foregroundColor(TextDim)
                    }
                }
                Button(action: onAdd) {
                    Image(systemName: "plus").font(.system(size: 16, weight: .semibold)).foregroundColor(PrimaryRed)
                }
            }

            if bookmarks.isEmpty {
                Text("Chưa có ngày nào được lưu")
                    .font(.system(size: 13)).foregroundColor(TextDim)
                    .frame(maxWidth: .infinity).padding(.vertical, 20)
                    .background(SurfaceContainer)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 10) {
                        ForEach(sortedBookmarks) { bm in
                            BookmarkCard(bookmark: bm, onRemove: { onRemove(bm) })
                        }
                    }
                    .padding(.horizontal, 2).padding(.vertical, 4)
                }
            }
        }
    }
}

private struct BookmarkCard: View {
    let bookmark: BookmarkEntity
    let onRemove: () -> Void
    @State private var showRemoveConfirm = false

    private let cardColors: [Color] = [
        Color(red: 1, green: 0.922, blue: 0.933),
        Color(red: 1, green: 0.973, blue: 0.882),
        Color(red: 0.910, green: 0.961, blue: 0.914),
        Color(red: 0.890, green: 0.949, blue: 0.992),
        Color(red: 0.953, green: 0.898, blue: 0.961),
        Color(red: 1, green: 0.953, blue: 0.878)
    ]
    private let textColors: [Color] = [
        Color(red: 0.776, green: 0.157, blue: 0.157),
        Color(red: 0.961, green: 0.498, blue: 0.090),
        Color(red: 0.180, green: 0.490, blue: 0.196),
        Color(red: 0.086, green: 0.396, blue: 0.753),
        Color(red: 0.416, green: 0.106, blue: 0.604),
        Color(red: 0.902, green: 0.318, blue: 0)
    ]
    private let monthNames = [
        "Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
        "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"
    ]

    var body: some View {
        let idx = bookmark.colorIndex % cardColors.count
        let mName = bookmark.solarMonth >= 1 && bookmark.solarMonth <= 12
            ? monthNames[bookmark.solarMonth - 1] : "T\(bookmark.solarMonth)"

        VStack(alignment: .leading, spacing: 4) {
            Text("\(bookmark.solarDay)")
                .font(.system(size: 28, weight: .bold)).foregroundColor(textColors[idx])
            Text(mName)
                .font(.system(size: 10, weight: .medium)).foregroundColor(textColors[idx].opacity(0.8))
            Text(String(format: "Ngày %02d/%02d/\n%d", bookmark.solarDay, bookmark.solarMonth, bookmark.solarYear))
                .font(.system(size: 9)).foregroundColor(TextDim).lineLimit(2)
            if !bookmark.label.isEmpty {
                Text(bookmark.label)
                    .font(.system(size: 9, weight: .medium)).foregroundColor(textColors[idx])
                    .lineLimit(1).padding(.horizontal, 6).padding(.vertical, 2)
                    .background(textColors[idx].opacity(0.12)).clipShape(Capsule())
            }
        }
        .padding(12).frame(width: 110, alignment: .leading)
        .background(cardColors[idx])
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(textColors[idx].opacity(0.2), lineWidth: 1))
        .onLongPressGesture { showRemoveConfirm = true }
        .confirmationDialog("Xoá ngày đã lưu?", isPresented: $showRemoveConfirm, titleVisibility: .visible) {
            Button("Xoá", role: .destructive, action: onRemove)
            Button("Huỷ", role: .cancel) {}
        }
    }
}

// ══════════════════════════════════════════
// MENU SECTIONS
// ══════════════════════════════════════════

private struct ProfileMenuSection: View {
    let noteCount: Int
    let reminderCount: Int
    let bookmarkCount: Int
    let onNotificationsTap: () -> Void
    let onSearchTap: () -> Void
    let onFamilySettingsTap: () -> Void
    let onPickMemberTap: () -> Void
    let onSettingsTap: () -> Void
    var onBackupTap: () -> Void = {}
    var onRestoreTap: () -> Void = {}
    var onAboutTap: () -> Void = {}
    var isBackingUp: Bool = false
    var isRestoring: Bool = false

    var body: some View {
        VStack(spacing: 16) {
            // Cá nhân
            VStack(alignment: .leading, spacing: 10) {
                PMenuSectionHeader(icon: "folder.fill", title: "Cá nhân")
                PMenuCard {
                    PMenuItem(iconBg: Color(red: 1, green: 0.922, blue: 0.933),
                              iconColor: Color(red: 0.776, green: 0.157, blue: 0.157),
                              icon: "bookmark.fill", title: "Ngày đã lưu",
                              desc: "\(bookmarkCount) ngày quan trọng",
                              badge: bookmarkCount > 0 ? "\(bookmarkCount)" : nil, badgeColor: PrimaryRed)
                    PMenuDivider()
                    PMenuItem(iconBg: Color(red: 1, green: 0.973, blue: 0.882),
                              iconColor: Color(red: 0.961, green: 0.498, blue: 0.090),
                              icon: "bell.fill", title: "Nhắc nhở",
                              desc: "\(reminderCount) nhắc nhở đang hoạt động",
                              badge: reminderCount > 0 ? "\(reminderCount)" : nil, badgeColor: GoldAccent,
                              action: onNotificationsTap)
                    PMenuDivider()
                    PMenuItem(iconBg: Color(red: 0.910, green: 0.961, blue: 0.914),
                              iconColor: Color(red: 0.180, green: 0.490, blue: 0.196),
                              icon: "note.text", title: "Ghi chú",
                              desc: "\(noteCount) ghi chú theo ngày", action: onSearchTap)
                }
            }

            // Gia phả
            VStack(alignment: .leading, spacing: 10) {
                PMenuSectionHeader(icon: "person.3.fill", title: "Gia phả")
                PMenuCard {
                    PMenuItem(iconBg: Color(red: 0.937, green: 0.922, blue: 0.914),
                              iconColor: Color(red: 0.365, green: 0.251, blue: 0.216),
                              icon: "person.3.fill", title: "Thành viên gia phả",
                              desc: "Xem & quản lý thành viên", action: onPickMemberTap)
                    PMenuDivider()
                    PMenuItem(iconBg: Color(red: 1, green: 0.973, blue: 0.882),
                              iconColor: Color(red: 0.831, green: 0.627, blue: 0.090),
                              icon: "gearshape.2.fill", title: "Cài đặt gia phả",
                              desc: "Tên dòng họ, hiển thị, nhắc nhở", action: onFamilySettingsTap)
                }
            }

            // Cài đặt
            VStack(alignment: .leading, spacing: 10) {
                PMenuSectionHeader(icon: "gearshape.fill", title: "Cài đặt")
                PMenuCard {
                    PMenuItem(iconBg: Color(red: 0.910, green: 0.961, blue: 0.914),
                              iconColor: Color(red: 0.180, green: 0.490, blue: 0.196),
                              icon: "arrow.up.doc.fill", title: "Sao lưu dữ liệu",
                              desc: "Xuất toàn bộ dữ liệu ra file JSON",
                              action: onBackupTap)
                    PMenuDivider()
                    PMenuItem(iconBg: Color(red: 0.890, green: 0.945, blue: 0.992),
                              iconColor: Color(red: 0.086, green: 0.396, blue: 0.753),
                              icon: "arrow.down.doc.fill", title: "Phục hồi dữ liệu",
                              desc: "Khôi phục từ file sao lưu JSON",
                              action: onRestoreTap)
                    PMenuDivider()
                    PMenuItem(iconBg: Color(red: 0.953, green: 0.898, blue: 0.961),
                              iconColor: Color(red: 0.416, green: 0.106, blue: 0.604),
                              icon: "gearshape.fill", title: "Cài đặt ứng dụng",
                              desc: "Giao diện, thông báo, hiển thị", action: onSettingsTap)
                    PMenuDivider()
                    PMenuItem(iconBg: Color(red: 1, green: 0.953, blue: 0.878),
                              iconColor: Color(red: 0.902, green: 0.318, blue: 0),
                              icon: "info.circle.fill", title: "Về ứng dụng", desc: "Lịch Số",
                              action: onAboutTap)
                }

                // Loading indicator for backup/restore
                if isBackingUp || isRestoring {
                    HStack(spacing: 10) {
                        ProgressView()
                            .tint(PrimaryRed)
                        Text(isBackingUp ? "Đang sao lưu dữ liệu..." : "Đang phục hồi dữ liệu...")
                            .font(.system(size: 13))
                            .foregroundColor(TextSub)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(14)
                    .background(Color(red: 0.890, green: 0.945, blue: 0.992))
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                }
            }
        }
    }
}

private struct PMenuSectionHeader: View {
    let icon: String
    let title: String
    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: icon).font(.system(size: 13)).foregroundColor(PrimaryRed)
            Text(title).font(.system(size: 14, weight: .bold)).foregroundColor(TextMain)
        }
    }
}

private struct PMenuCard<Content: View>: View {
    @ViewBuilder let content: () -> Content
    var body: some View {
        VStack(spacing: 0) { content() }
            .background(SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant.opacity(0.5), lineWidth: 1))
    }
}

private struct PMenuItem: View {
    let iconBg: Color
    let iconColor: Color
    let icon: String
    let title: String
    let desc: String
    var badge: String? = nil
    var badgeColor: Color = PrimaryRed
    var action: () -> Void = {}

    var body: some View {
        Button(action: action) {
            HStack(spacing: 12) {
                Image(systemName: icon).font(.system(size: 16)).foregroundColor(iconColor)
                    .frame(width: 36, height: 36).background(iconBg)
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                VStack(alignment: .leading, spacing: 2) {
                    HStack(spacing: 6) {
                        Text(title).font(.system(size: 14, weight: .semibold)).foregroundColor(TextMain)
                        if let b = badge {
                            Text(b).font(.system(size: 10, weight: .bold)).foregroundColor(.white)
                                .padding(.horizontal, 6).padding(.vertical, 2)
                                .background(badgeColor).clipShape(Capsule())
                        }
                    }
                    Text(desc).font(.system(size: 11)).foregroundColor(TextDim)
                }
                Spacer()
                Image(systemName: "chevron.right").font(.system(size: 12, weight: .semibold))
                    .foregroundColor(OutlineVariant)
            }
            .padding(.horizontal, 16).padding(.vertical, 12)
        }
        .buttonStyle(.plain)
    }
}

private struct PMenuDivider: View {
    var body: some View {
        Rectangle().fill(OutlineVariant.opacity(0.4)).frame(height: 0.5).padding(.leading, 64)
    }
}

// ══════════════════════════════════════════
// ADD BOOKMARK SHEET
// ══════════════════════════════════════════

private struct AddBookmarkSheet: View {
    let onAdd: (Int, Int, Int, String, String) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var day   = ""
    @State private var month = ""
    @State private var year  = Calendar.current.component(.year, from: Date()).description
    @State private var label = ""
    @State private var note  = ""

    var body: some View {
        NavigationStack {
            Form {
                Section("Ngày (Dương lịch)") {
                    HStack {
                        TextField("Ngày",  text: $day)
                            .keyboardType(.numberPad)
                            .frame(maxWidth: 60)
                        Text("/")
                        TextField("Tháng", text: $month)
                            .keyboardType(.numberPad)
                            .frame(maxWidth: 60)
                        Text("/")
                        TextField("Năm",   text: $year)
                            .keyboardType(.numberPad)
                    }
                }
                Section("Nhãn & Ghi chú") {
                    TextField("Tên sự kiện (tuỳ chọn)", text: $label)
                    TextField("Ghi chú (tuỳ chọn)",     text: $note)
                }
            }
            .navigationTitle("Thêm ngày đã lưu")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Huỷ") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Lưu") {
                        guard let d = Int(day), let m = Int(month), let y = Int(year),
                              d >= 1, d <= 31, m >= 1, m <= 12 else { return }
                        onAdd(d, m, y, label, note)
                    }.fontWeight(.bold)
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// ABOUT SHEET (Profile)
// ══════════════════════════════════════════

private struct ProfileAboutSheet: View {
    @Environment(\.dismiss) private var dismiss

    private var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0"
    }
    private var buildNumber: String {
        Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "1"
    }

    var body: some View {
        VStack(spacing: 0) {
            // Close button
            HStack {
                Spacer()
                Button { dismiss() } label: {
                    Image(systemName: "xmark.circle.fill")
                        .font(.system(size: 24))
                        .foregroundColor(TextDim)
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 20)

            ScrollView {
                VStack(spacing: 16) {
                    // App icon
                    Image("AppLogo")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 64, height: 64)
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                        .padding(.top, 8)

                    VStack(spacing: 6) {
                        Text("Lịch Số")
                            .font(.system(size: 24, weight: .bold))
                            .foregroundColor(TextMain)
                        Text("Lịch Vạn Niên Việt Nam")
                            .font(.system(size: 15))
                            .foregroundColor(TextSub)
                        Text("Phiên bản \(appVersion) (\(buildNumber))")
                            .font(.system(size: 13))
                            .foregroundColor(TextDim)
                            .padding(.top, 2)
                    }

                    // Divider
                    Rectangle()
                        .fill(OutlineVariant.opacity(0.4))
                        .frame(height: 1)
                        .padding(.horizontal, 40)
                        .padding(.vertical, 4)

                    // Features
                    VStack(alignment: .leading, spacing: 12) {
                        AboutFeatureRow(icon: "calendar.badge.clock", text: "Lịch âm dương chính xác")
                        AboutFeatureRow(icon: "sparkles",            text: "Phong thủy & Ngày hoàng đạo")
                        AboutFeatureRow(icon: "brain.head.profile",  text: "Trợ lý AI thông minh")
                        AboutFeatureRow(icon: "bell.badge.fill",     text: "Nhắc nhở thông minh")
                        AboutFeatureRow(icon: "note.text",           text: "Ghi chú & Công việc")
                        AboutFeatureRow(icon: "person.2.fill",       text: "Gia phả & Kỷ niệm gia đình")
                    }
                    .padding(.horizontal, 32)

                    // Links
                    VStack(spacing: 8) {
                        Button {
                            SmartRatingManager.shared.triggerManually()
                        } label: {
                            HStack(spacing: 6) {
                                Image(systemName: "star.fill").font(.system(size: 13))
                                Text("Đánh giá trên App Store").font(.system(size: 13, weight: .medium))
                            }
                            .foregroundColor(PrimaryRed)
                        }

                        Button {
                            if let url = URL(string: "https://apps.zenix.vn/privacy-policy") {
                                UIApplication.shared.open(url)
                            }
                        } label: {
                            HStack(spacing: 6) {
                                Image(systemName: "lock.shield.fill").font(.system(size: 13))
                                Text("Chính sách bảo mật").font(.system(size: 13, weight: .medium))
                            }
                            .foregroundColor(TextSub)
                        }
                    }
                    .padding(.top, 8)

                    Spacer().frame(height: 12)

                    Text("© 2024 Lịch Số. All rights reserved.")
                        .font(.system(size: 11))
                        .foregroundColor(TextDim)
                        .padding(.bottom, 8)
                }
            }
        }
        .background(SurfaceBg.ignoresSafeArea())
    }
}

private struct AboutFeatureRow: View {
    let icon: String
    let text: String

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: icon)
                .font(.system(size: 17))
                .foregroundColor(PrimaryRed)
                .frame(width: 26)
            Text(text)
                .font(.system(size: 14))
                .foregroundColor(TextMain)
        }
    }
}

// ══════════════════════════════════════════
// DOCUMENT PICKER (for restore)
// ══════════════════════════════════════════

private struct BackupDocumentPicker: UIViewControllerRepresentable {
    let onPick: (URL) -> Void

    func makeUIViewController(context: Context) -> UIDocumentPickerViewController {
        let picker = UIDocumentPickerViewController(forOpeningContentTypes: [.json, .data])
        picker.delegate = context.coordinator
        picker.allowsMultipleSelection = false
        return picker
    }

    func updateUIViewController(_ uiViewController: UIDocumentPickerViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(onPick: onPick)
    }

    class Coordinator: NSObject, UIDocumentPickerDelegate {
        let onPick: (URL) -> Void
        init(onPick: @escaping (URL) -> Void) { self.onPick = onPick }

        func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
            guard let url = urls.first else { return }
            onPick(url)
        }
    }
}