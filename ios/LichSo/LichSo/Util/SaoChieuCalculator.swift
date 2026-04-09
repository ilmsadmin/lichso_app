import Foundation

// ═══════════════════════════════════════════
// Tính Sao chiếu (28 sao - Nhị thập bát tú)
// Port chính xác từ Android Kotlin
// KHÔNG ĐƯỢC THAY ĐỔI THUẬT TOÁN
// ═══════════════════════════════════════════

struct SaoChieuInfo {
    let name: String
    let rating: String // "Tốt", "Xấu", "Trung bình"
}

enum SaoChieuCalculator {

    private static let SAO_NAMES = [
        "Giác", "Cang", "Đê", "Phòng", "Tâm", "Vĩ", "Cơ",
        "Đẩu", "Ngưu", "Nữ", "Hư", "Nguy", "Thất", "Bích",
        "Khuê", "Lâu", "Vị", "Mão", "Tất", "Chủy", "Sâm",
        "Tỉnh", "Quỷ", "Liễu", "Tinh", "Trương", "Dực", "Chẩn"
    ]

    private static let SAO_RATING: [String: String] = [
        "Giác": "Tốt", "Cang": "Xấu", "Đê": "Xấu", "Phòng": "Tốt",
        "Tâm": "Xấu", "Vĩ": "Tốt", "Cơ": "Tốt",
        "Đẩu": "Tốt", "Ngưu": "Xấu", "Nữ": "Xấu", "Hư": "Xấu",
        "Nguy": "Trung bình", "Thất": "Tốt", "Bích": "Tốt",
        "Khuê": "Xấu", "Lâu": "Tốt", "Vị": "Tốt", "Mão": "Xấu",
        "Tất": "Tốt", "Chủy": "Xấu", "Sâm": "Tốt",
        "Tỉnh": "Tốt", "Quỷ": "Xấu", "Liễu": "Xấu",
        "Tinh": "Tốt", "Trương": "Tốt", "Dực": "Tốt", "Chẩn": "Tốt"
    ]

    static func getSaoChieu(jd: Int) -> SaoChieuInfo {
        let saoIdx = (jd + 15) % 28
        let clampedIdx = max(0, min(saoIdx, 27))
        let name = SAO_NAMES[clampedIdx]
        let rating = SAO_RATING[name] ?? "Trung bình"
        return SaoChieuInfo(name: name, rating: rating)
    }
}
