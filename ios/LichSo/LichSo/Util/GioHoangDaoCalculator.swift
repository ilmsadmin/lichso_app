import Foundation

// ═══════════════════════════════════════════
// Tính Giờ Hoàng Đạo dựa trên Chi của ngày
// Port chính xác từ Android Kotlin
// KHÔNG ĐƯỢC THAY ĐỔI THUẬT TOÁN
// ═══════════════════════════════════════════

struct GioHoangDao {
    let name: String
    let time: String
}

enum GioHoangDaoCalculator {

    private static let GIO_LABELS = [
        GioHoangDao(name: "Tý", time: "23h–1h"),   GioHoangDao(name: "Sửu", time: "1h–3h"),
        GioHoangDao(name: "Dần", time: "3h–5h"),    GioHoangDao(name: "Mão", time: "5h–7h"),
        GioHoangDao(name: "Thìn", time: "7h–9h"),   GioHoangDao(name: "Tỵ", time: "9h–11h"),
        GioHoangDao(name: "Ngọ", time: "11h–13h"),  GioHoangDao(name: "Mùi", time: "13h–15h"),
        GioHoangDao(name: "Thân", time: "15h–17h"), GioHoangDao(name: "Dậu", time: "17h–19h"),
        GioHoangDao(name: "Tuất", time: "19h–21h"), GioHoangDao(name: "Hợi", time: "21h–23h")
    ]

    // Bảng tra giờ hoàng đạo theo Chi ngày
    private static let HOANG_DAO: [Int: [Int]] = [
        0:  [0, 1, 4, 5, 8, 9],    // Ngày Tý
        1:  [2, 3, 6, 7, 10, 11],  // Ngày Sửu
        2:  [0, 1, 4, 5, 8, 9],    // Ngày Dần
        3:  [2, 3, 6, 7, 10, 11],  // Ngày Mão
        4:  [0, 1, 4, 5, 8, 9],    // Ngày Thìn
        5:  [2, 3, 6, 7, 10, 11],  // Ngày Tỵ
        6:  [0, 1, 4, 5, 8, 9],    // Ngày Ngọ
        7:  [2, 3, 6, 7, 10, 11],  // Ngày Mùi
        8:  [0, 1, 4, 5, 8, 9],    // Ngày Thân
        9:  [2, 3, 6, 7, 10, 11],  // Ngày Dậu
        10: [0, 1, 4, 5, 8, 9],    // Ngày Tuất
        11: [2, 3, 6, 7, 10, 11]   // Ngày Hợi
    ]

    static func getGioHoangDao(jd: Int) -> [GioHoangDao] {
        let chiOfDay = (jd + 1) % 12
        let gioTot = HOANG_DAO[chiOfDay] ?? [0, 1, 4, 5, 8, 9]
        return gioTot.map { GIO_LABELS[$0] }
    }
}
