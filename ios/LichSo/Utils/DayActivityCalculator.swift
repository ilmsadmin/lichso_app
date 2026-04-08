import Foundation

// MARK: - Ngày tốt/xấu — Nên làm / Không nên — Hướng tốt

struct DayActivities {
    let nenLam: [String]
    let khongNen: [String]
    let isXauDay: Bool
    let isNguyetKy: Bool
    let isTamNuong: Bool
}

struct HuongTot {
    let thanTai: String
    let hyThan: String
    let hungThan: String
}

enum DayActivityCalculator {
    private static let NEN_LAM_LIST: [[String]] = [
        ["Giao dịch, ký kết", "Xuất hành", "Cưới hỏi", "Khai trương"],
        ["Cầu tài, cầu phúc", "Nhập học", "An sàng", "Dựng cột"],
        ["Động thổ, xây dựng", "Nhập trạch", "Giao dịch", "Cầu tài"],
        ["Cầu an, giải hạn", "Tu sửa", "Nhập học", "Cưới hỏi"],
        ["Xuất hành", "Khai trương", "Giao dịch", "Ký kết"],
        ["Cầu phúc, cúng tế", "An táng", "Nhập trạch", "Tu sửa"],
        ["Giao dịch, ký kết", "Xuất hành", "Khai trương", "Nhập học"],
        ["Cầu tài", "An sàng", "Tu sửa", "Nhập trạch"],
        ["Động thổ", "Dựng cột", "Giao dịch", "Xuất hành"],
        ["Cầu an", "Tu sửa", "Nhập học", "Cầu tài"],
        ["Cưới hỏi", "Xuất hành", "Ký kết", "Khai trương"],
        ["Cầu phúc", "An sàng", "Nhập trạch", "Giao dịch"]
    ]

    private static let KHONG_NEN_LIST: [[String]] = [
        ["An táng", "Kiện cáo"],
        ["Xuất hành xa", "Phá thổ"],
        ["Cưới hỏi", "Kiện cáo"],
        ["Khai trương", "An táng"],
        ["An táng", "Tu sửa"],
        ["Xuất hành", "Kiện cáo"],
        ["An táng", "Phá thổ"],
        ["Khai trương", "Kiện cáo"],
        ["Cưới hỏi", "An táng"],
        ["Xuất hành xa", "Phá thổ"],
        ["An táng", "Kiện cáo"],
        ["Xuất hành", "Tu sửa"]
    ]

    private static let THAN_TAI = [
        "Đông-Nam", "Đông", "Bắc", "Bắc", "Đông-Bắc",
        "Đông", "Tây-Nam", "Tây", "Bắc", "Nam"
    ]

    private static let HY_THAN = [
        "Đông-Bắc", "Tây-Bắc", "Tây-Nam", "Nam", "Đông-Nam",
        "Đông-Bắc", "Tây-Bắc", "Tây-Nam", "Nam", "Đông-Nam"
    ]

    private static let HUNG_THAN = [
        "Nam", "Đông", "Bắc", "Tây", "Nam", "Đông",
        "Bắc", "Tây", "Nam", "Đông", "Bắc", "Tây"
    ]

    static func getDayActivities(jd: Int, lunarDay: Int, lunarMonth: Int) -> DayActivities {
        let chiIdx = ((jd + 1) % 12 + 12) % 12

        let isNguyetKy = lunarDay == 5 || lunarDay == 14 || lunarDay == 23
        let isTamNuong = [3, 7, 13, 18, 22, 27].contains(lunarDay)

        let nenLam = chiIdx < NEN_LAM_LIST.count ? NEN_LAM_LIST[chiIdx] : ["Giao dịch", "Xuất hành"]
        var khongNen = chiIdx < KHONG_NEN_LIST.count ? KHONG_NEN_LIST[chiIdx] : ["An táng"]

        let isXauDay = isNguyetKy || isTamNuong
        if isNguyetKy {
            khongNen.insert("Nguyệt kỵ — kiêng mọi việc lớn", at: 0)
        }
        if isTamNuong {
            khongNen.insert("Tam nương sát — không cưới hỏi", at: 0)
        }

        return DayActivities(nenLam: nenLam, khongNen: khongNen, isXauDay: isXauDay, isNguyetKy: isNguyetKy, isTamNuong: isTamNuong)
    }

    static func getHuongTot(jd: Int) -> HuongTot {
        let canIdx = ((jd + 9) % 10 + 10) % 10
        let chiIdx = ((jd + 1) % 12 + 12) % 12
        return HuongTot(
            thanTai: THAN_TAI[canIdx],
            hyThan: HY_THAN[canIdx],
            hungThan: HUNG_THAN[chiIdx]
        )
    }
}
