import SwiftUI
import PhotosUI

// ═══════════════════════════════════════════
// Edit Profile ViewModel
// Port from Android ProfileViewModel — dùng UserDefaults (tương đương DataStore)
// Tính Can Chi, Mệnh, Cung từ thuật toán thật
// ═══════════════════════════════════════════

// MARK: - Birth Info (computed from date)

struct BirthInfo: Equatable {
    var yearCanChi: String = ""
    var menh: String = ""           // "Kim", "Mộc", "Thủy", "Hỏa", "Thổ"
    var nguHanh: String = ""        // Ngũ Hành Nạp Âm full name
    var conGiap: String = ""        // "Tý", "Sửu"...
    var conGiapEmoji: String = ""   // 🐭, 🐮...
    var cung: String = ""           // "Càn (Nam)"
    var gioSinhCanChi: String = ""  // "Giờ Mùi (13:00 — 15:00)"
}

// MARK: - ViewModel

final class EditProfileViewModel: ObservableObject {
    // ── Form fields ──
    @Published var displayName: String = ""
    @Published var email: String = ""
    @Published var gender: String = "Nam"

    // Ngày sinh (Âm lịch — như mock HTML)
    @Published var birthDay: String = ""
    @Published var birthMonth: String = ""
    @Published var birthYear: String = ""

    // Giờ sinh
    @Published var birthHour: String = ""
    @Published var birthMinute: String = ""
    @Published var unknownBirthTime: Bool = false

    // Avatar
    @Published var avatarPath: String = ""
    @Published var avatarImage: UIImage? = nil

    // Computed birth info
    @Published var birthInfo: BirthInfo = BirthInfo()

    // UI state
    @Published var toastMessage: String? = nil
    @Published var showImagePicker: Bool = false
    @Published var isSaving: Bool = false

    // Photo picker
    @Published var selectedPhotoItem: PhotosPickerItem? = nil {
        didSet { handlePhotoSelection() }
    }

    // ── UserDefaults keys (same as Onboarding + Android DataStore) ──
    private enum Keys {
        static let displayName = "displayName"
        static let email = "profile_email"
        static let avatarPath = "profile_avatar_path"
        static let birthDay = "birthDay"
        static let birthMonth = "birthMonth"
        static let birthYear = "birthYear"
        static let birthHour = "birthHour"
        static let birthMinute = "birthMinute"
        static let gender = "gender"
    }

    // ═══ Init: Load from UserDefaults ═══

    init() {
        loadProfile()
    }

    func loadProfile() {
        let defaults = UserDefaults.standard
        displayName = defaults.string(forKey: Keys.displayName) ?? ""
        email = defaults.string(forKey: Keys.email) ?? ""
        avatarPath = defaults.string(forKey: Keys.avatarPath) ?? ""
        gender = defaults.string(forKey: Keys.gender) ?? "Nam"

        let bDay = defaults.integer(forKey: Keys.birthDay)
        let bMonth = defaults.integer(forKey: Keys.birthMonth)
        let bYear = defaults.integer(forKey: Keys.birthYear)
        let bHour = defaults.integer(forKey: Keys.birthHour)
        let bMin = defaults.integer(forKey: Keys.birthMinute)

        birthDay = bDay > 0 ? String(bDay) : ""
        birthMonth = bMonth > 0 ? String(bMonth) : ""
        birthYear = bYear > 0 ? String(bYear) : ""

        // -1 hoặc 0 = chưa nhập
        if bHour > 0 || (bHour == 0 && bMin >= 0 && defaults.object(forKey: Keys.birthHour) != nil) {
            birthHour = String(bHour)
            birthMinute = bMin >= 0 ? String(bMin) : ""
            unknownBirthTime = false
        } else {
            birthHour = ""
            birthMinute = ""
            unknownBirthTime = true
        }

        // Load avatar image
        loadAvatarImage()

        // Calculate birth info
        recalculateBirthInfo()
    }

    // ═══ Save to UserDefaults ═══

    func saveProfile() {
        isSaving = true

        let defaults = UserDefaults.standard
        let name = displayName.trimmingCharacters(in: .whitespaces)

        defaults.set(name.isEmpty ? "Người dùng" : name, forKey: Keys.displayName)
        defaults.set(email, forKey: Keys.email)
        defaults.set(gender, forKey: Keys.gender)

        let day = Int(birthDay) ?? 0
        let month = Int(birthMonth) ?? 0
        let year = Int(birthYear) ?? 0
        defaults.set(day, forKey: Keys.birthDay)
        defaults.set(month, forKey: Keys.birthMonth)
        defaults.set(year, forKey: Keys.birthYear)

        if unknownBirthTime {
            defaults.set(-1, forKey: Keys.birthHour)
            defaults.set(-1, forKey: Keys.birthMinute)
        } else {
            defaults.set(Int(birthHour) ?? -1, forKey: Keys.birthHour)
            defaults.set(Int(birthMinute) ?? -1, forKey: Keys.birthMinute)
        }

        defaults.set(avatarPath, forKey: Keys.avatarPath)

        isSaving = false
        toastMessage = "Đã lưu hồ sơ"
    }

    // ═══ Avatar handling ═══

    private func loadAvatarImage() {
        guard !avatarPath.isEmpty else {
            avatarImage = nil
            return
        }
        if FileManager.default.fileExists(atPath: avatarPath) {
            avatarImage = UIImage(contentsOfFile: avatarPath)
        } else {
            avatarImage = nil
        }
    }

    private func handlePhotoSelection() {
        guard let item = selectedPhotoItem else { return }
        item.loadTransferable(type: Data.self) { [weak self] result in
            DispatchQueue.main.async {
                switch result {
                case .success(let data):
                    guard let data = data, let image = UIImage(data: data) else {
                        self?.toastMessage = "Không thể đọc ảnh"
                        return
                    }
                    self?.saveAvatarImage(image)
                case .failure:
                    self?.toastMessage = "Không thể tải ảnh"
                }
            }
        }
    }

    private func saveAvatarImage(_ image: UIImage) {
        let fm = FileManager.default
        guard let docsDir = fm.urls(for: .documentDirectory, in: .userDomainMask).first else { return }

        let avatarDir = docsDir.appendingPathComponent("avatars", isDirectory: true)
        try? fm.createDirectory(at: avatarDir, withIntermediateDirectories: true)

        // Delete old avatar files
        if let files = try? fm.contentsOfDirectory(at: avatarDir, includingPropertiesForKeys: nil) {
            for file in files where file.lastPathComponent.hasPrefix("profile_avatar") {
                try? fm.removeItem(at: file)
            }
        }

        let ts = Int(Date().timeIntervalSince1970 * 1000)
        let destURL = avatarDir.appendingPathComponent("profile_avatar_\(ts).jpg")

        guard let jpegData = image.jpegData(compressionQuality: 0.85) else { return }
        try? jpegData.write(to: destURL)

        avatarPath = destURL.path
        avatarImage = image
        UserDefaults.standard.set(avatarPath, forKey: Keys.avatarPath)
        toastMessage = "Đã cập nhật ảnh đại diện"
    }

    func removeAvatar() {
        let fm = FileManager.default
        if !avatarPath.isEmpty {
            try? fm.removeItem(atPath: avatarPath)
        }
        avatarPath = ""
        avatarImage = nil
        UserDefaults.standard.removeObject(forKey: Keys.avatarPath)
        toastMessage = "Đã xóa ảnh đại diện"
    }

    // ═══ Birth Info Calculation — Port from Android ═══

    func recalculateBirthInfo() {
        let day = Int(birthDay) ?? 0
        let month = Int(birthMonth) ?? 0
        let year = Int(birthYear) ?? 0

        guard day > 0, month > 0, year > 1900, year < 2100 else {
            birthInfo = BirthInfo()
            return
        }

        // Ngày sinh nhập theo Âm lịch → lunarYear = year
        let lunarYear = year

        // Can Chi năm
        let yearCanChi = CanChiCalculator.getYearCanChi(lunarYear: lunarYear)

        // Con giáp
        let chiIndex = ((lunarYear + 8) % 12 + 12) % 12
        let conGiapNames = ["Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"]
        let conGiapEmojis = ["🐭", "🐮", "🐯", "🐱", "🐲", "🐍", "🐴", "🐐", "🐒", "🐔", "🐶", "🐷"]
        let conGiap = conGiapNames[chiIndex]
        let conGiapEmoji = conGiapEmojis[chiIndex]

        // Ngũ Hành Nạp Âm (60-year cycle)
        let nguHanh = getNguHanhNapAm(lunarYear: lunarYear)

        // Mệnh from Ngũ Hành
        let menh = getMenhFromNguHanh(nguHanh)

        // Cung (Bát Trạch)
        let cung = calculateCung(lunarYear: lunarYear, gender: gender)

        // Giờ sinh Can Chi
        var gioSinhCanChi = ""
        if !unknownBirthTime, let hour = Int(birthHour), hour >= 0, hour < 24 {
            gioSinhCanChi = getGioCanChi(hour: hour)
        }

        birthInfo = BirthInfo(
            yearCanChi: yearCanChi,
            menh: menh,
            nguHanh: nguHanh,
            conGiap: conGiap,
            conGiapEmoji: conGiapEmoji,
            cung: cung,
            gioSinhCanChi: gioSinhCanChi
        )
    }

    // ── Ngũ Hành Nạp Âm — 60-year cycle (chính xác từ Android) ──

    private func getNguHanhNapAm(lunarYear: Int) -> String {
        let napAmList = [
            "Hải Trung Kim", "Hải Trung Kim",
            "Lư Trung Hỏa", "Lư Trung Hỏa",
            "Đại Lâm Mộc", "Đại Lâm Mộc",
            "Lộ Bàng Thổ", "Lộ Bàng Thổ",
            "Kiếm Phong Kim", "Kiếm Phong Kim",
            "Sơn Đầu Hỏa", "Sơn Đầu Hỏa",
            "Giản Hạ Thủy", "Giản Hạ Thủy",
            "Thành Đầu Thổ", "Thành Đầu Thổ",
            "Bạch Lạp Kim", "Bạch Lạp Kim",
            "Dương Liễu Mộc", "Dương Liễu Mộc",
            "Tuyền Trung Thủy", "Tuyền Trung Thủy",
            "Ốc Thượng Thổ", "Ốc Thượng Thổ",
            "Tích Lịch Hỏa", "Tích Lịch Hỏa",
            "Tùng Bách Mộc", "Tùng Bách Mộc",
            "Trường Lưu Thủy", "Trường Lưu Thủy",
            "Sa Trung Kim", "Sa Trung Kim",
            "Sơn Hạ Hỏa", "Sơn Hạ Hỏa",
            "Bình Địa Mộc", "Bình Địa Mộc",
            "Bích Thượng Thổ", "Bích Thượng Thổ",
            "Kim Bạch Kim", "Kim Bạch Kim",
            "Phúc Đăng Hỏa", "Phúc Đăng Hỏa",
            "Thiên Hà Thủy", "Thiên Hà Thủy",
            "Đại Dịch Thổ", "Đại Dịch Thổ",
            "Thoa Xuyến Kim", "Thoa Xuyến Kim",
            "Tang Đố Mộc", "Tang Đố Mộc",
            "Đại Khê Thủy", "Đại Khê Thủy",
            "Sa Trung Thổ", "Sa Trung Thổ",
            "Thiên Thượng Hỏa", "Thiên Thượng Hỏa",
            "Thạch Lựu Mộc", "Thạch Lựu Mộc",
            "Đại Hải Thủy", "Đại Hải Thủy"
        ]
        let index = ((lunarYear - 4) % 60 + 60) % 60
        return index < napAmList.count ? napAmList[index] : "Không rõ"
    }

    private func getMenhFromNguHanh(_ nguHanh: String) -> String {
        if nguHanh.contains("Kim") { return "Kim" }
        if nguHanh.contains("Mộc") { return "Mộc" }
        if nguHanh.contains("Thủy") { return "Thủy" }
        if nguHanh.contains("Hỏa") { return "Hỏa" }
        if nguHanh.contains("Thổ") { return "Thổ" }
        return "Không rõ"
    }

    // ── Cung Bát Trạch — chính xác từ Android ──

    private func calculateCung(lunarYear: Int, gender: String) -> String {
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

        let genderLabel = isMale ? "Nam" : "Nữ"
        let cungName = cungIndex < cungNames.count ? cungNames[cungIndex] : "Khảm"
        return "\(cungName) (\(genderLabel))"
    }

    private func digitSum(_ n: Int) -> Int {
        var s = 0
        var v = abs(n)
        while v > 0 {
            s += v % 10
            v /= 10
        }
        while s >= 10 {
            var ns = 0
            var sv = s
            while sv > 0 {
                ns += sv % 10
                sv /= 10
            }
            s = ns
        }
        return s
    }

    // ── Giờ sinh → Can Chi ──

    private func getGioCanChi(hour: Int) -> String {
        let gioChiNames = ["Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"]
        let gioRanges = [
            "23:00 — 01:00", "01:00 — 03:00", "03:00 — 05:00",
            "05:00 — 07:00", "07:00 — 09:00", "09:00 — 11:00",
            "11:00 — 13:00", "13:00 — 15:00", "15:00 — 17:00",
            "17:00 — 19:00", "19:00 — 21:00", "21:00 — 23:00"
        ]

        let index: Int
        if hour == 23 {
            index = 0
        } else {
            index = (hour + 1) / 2
        }
        let safeIndex = index % 12
        return "Giờ \(gioChiNames[safeIndex]) (\(gioRanges[safeIndex]))"
    }

    // ── Mệnh emoji ──

    var menhEmoji: String {
        switch birthInfo.menh {
        case "Kim": return "🥇"
        case "Mộc": return "🌳"
        case "Thủy": return "💧"
        case "Hỏa": return "🔥"
        case "Thổ": return "🏔️"
        default: return "✨"
        }
    }

    // ── Initials for avatar placeholder ──

    var initials: String {
        let parts = displayName
            .trimmingCharacters(in: .whitespaces)
            .split(separator: " ")
            .compactMap { $0.first.map(String.init) }
        if parts.count >= 2 {
            return "\(parts.first!)\(parts.last!)"
        } else if let first = parts.first {
            return first
        }
        return "?"
    }
}
