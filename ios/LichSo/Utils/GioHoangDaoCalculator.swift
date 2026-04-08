import Foundation

// MARK: - Tính Giờ Hoàng Đạo dựa trên Chi của ngày

struct GioHoangDao: Identifiable {
    let id = UUID()
    let name: String
    let time: String
}

enum GioHoangDaoCalculator {
    private static let GIO_LABELS: [(String, String)] = [
        ("Tý", "23h–1h"), ("Sửu", "1h–3h"),
        ("Dần", "3h–5h"), ("Mão", "5h–7h"),
        ("Thìn", "7h–9h"), ("Tỵ", "9h–11h"),
        ("Ngọ", "11h–13h"), ("Mùi", "13h–15h"),
        ("Thân", "15h–17h"), ("Dậu", "17h–19h"),
        ("Tuất", "19h–21h"), ("Hợi", "21h–23h")
    ]

    // Bảng tra giờ hoàng đạo theo Chi ngày
    private static let HOANG_DAO: [Int: [Int]] = [
        0: [0, 1, 4, 5, 8, 9],     // Ngày Tý
        1: [2, 3, 6, 7, 10, 11],   // Ngày Sửu
        2: [0, 1, 4, 5, 8, 9],     // Ngày Dần
        3: [2, 3, 6, 7, 10, 11],   // Ngày Mão
        4: [0, 1, 4, 5, 8, 9],     // Ngày Thìn
        5: [2, 3, 6, 7, 10, 11],   // Ngày Tỵ
        6: [0, 1, 4, 5, 8, 9],     // Ngày Ngọ
        7: [2, 3, 6, 7, 10, 11],   // Ngày Mùi
        8: [0, 1, 4, 5, 8, 9],     // Ngày Thân
        9: [2, 3, 6, 7, 10, 11],   // Ngày Dậu
        10: [0, 1, 4, 5, 8, 9],    // Ngày Tuất
        11: [2, 3, 6, 7, 10, 11]   // Ngày Hợi
    ]

    static func getGioHoangDao(jd: Int) -> [GioHoangDao] {
        let chiOfDay = ((jd + 1) % 12 + 12) % 12
        let gioTot = HOANG_DAO[chiOfDay] ?? [0, 1, 4, 5, 8, 9]
        return gioTot.map { idx in
            let (name, time) = GIO_LABELS[idx]
            return GioHoangDao(name: name, time: time)
        }
    }
}
