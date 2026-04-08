import Foundation

// MARK: - Tính Trực của ngày (Kiến Trừ 12 Trực)

struct TrucNgayInfo {
    let name: String
    let rating: String // "Tốt", "Xấu", "Trung bình"
}

enum TrucNgayCalculator {
    private static let TRUC_NAMES = [
        "Kiến", "Trừ", "Mãn", "Bình", "Định", "Chấp",
        "Phá", "Nguy", "Thành", "Thu", "Khai", "Bế"
    ]

    private static let TRUC_RATING: [String: String] = [
        "Kiến": "Trung bình",
        "Trừ": "Tốt",
        "Mãn": "Tốt",
        "Bình": "Tốt",
        "Định": "Tốt",
        "Chấp": "Trung bình",
        "Phá": "Xấu",
        "Nguy": "Xấu",
        "Thành": "Tốt",
        "Thu": "Trung bình",
        "Khai": "Tốt",
        "Bế": "Xấu"
    ]

    /// Tính trực của ngày dựa trên Chi ngày và tháng âm lịch
    static func getTrucNgay(jd: Int, lunarMonth: Int) -> TrucNgayInfo {
        let chiOfDay = ((jd + 1) % 12 + 12) % 12
        let trucIdx = ((chiOfDay - (lunarMonth - 1) + 12) % 12)
        let name = TRUC_NAMES[trucIdx]
        let rating = TRUC_RATING[name] ?? "Trung bình"
        return TrucNgayInfo(name: name, rating: rating)
    }
}
