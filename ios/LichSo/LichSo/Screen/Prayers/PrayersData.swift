import Foundation
import SwiftUI

// ═══════════════════════════════════════════
// Prayers Data — Static prayer content
// All Vietnamese traditional prayers organized by category
// ═══════════════════════════════════════════

enum PrayerCategory: String, CaseIterable, Identifiable {
    case all = "Tất cả"
    case gio = "Cúng giỗ"
    case ram = "Rằm & Mùng 1"
    case tet = "Tết"
    case nhapTrach = "Nhập trạch"
    case kinhDoanh = "Khai trương"
    case chua = "Đi chùa"
    case tangLe = "Tang lễ"

    var id: String { rawValue }

    var icon: String {
        switch self {
        case .all: return "list.bullet"
        case .gio: return "flame.fill"
        case .ram: return "moon.fill"
        case .tet: return "sparkles"
        case .nhapTrach: return "house.fill"
        case .kinhDoanh: return "banknote"
        case .chua: return "building.columns.fill"
        case .tangLe: return "leaf.fill"
        }
    }

    var iconColor: (Color, Color) {
        switch self {
        case .all: return (Color(hex: "B71C1C"), Color(hex: "FFEBEE"))
        case .gio: return (Color(hex: "E65100"), Color(hex: "FFF3E0"))
        case .ram: return (Color(hex: "6A1B9A"), Color(hex: "F3E5F5"))
        case .tet: return (Color(hex: "C62828"), Color(hex: "FFEBEE"))
        case .nhapTrach: return (Color(hex: "1565C0"), Color(hex: "E3F2FD"))
        case .kinhDoanh: return (Color(hex: "2E7D32"), Color(hex: "E8F5E9"))
        case .chua: return (Color(hex: "AD1457"), Color(hex: "FCE4EC"))
        case .tangLe: return (Color(hex: "455A64"), Color(hex: "ECEFF1"))
        }
    }
}

struct Prayer: Identifiable {
    let id = UUID()
    let title: String
    let description: String
    let category: PrayerCategory
    let iconName: String
    let content: String
    let isPopular: Bool
    let tags: [PrayerTag]

    struct PrayerTag {
        let text: String
        let type: TagType

        enum TagType {
            case hot
            case new
            case normal
        }
    }
}

// MARK: - Prayer Database

struct PrayersDatabase {
    static let featured = Prayer(
        title: "Văn khấn Rằm tháng Bảy",
        description: "Bài cúng Vu Lan — Xá tội vong nhân, báo hiếu cha mẹ, cúng chúng sinh",
        category: .ram,
        iconName: "hands.sparkles",
        content: """
        Nam mô A Di Đà Phật! (3 lần)

        Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
        Con kính lạy Đức Địa Tạng Vương Bồ Tát, Đức Mục Kiền Liên Tôn Giả.

        Hôm nay là ngày Rằm tháng Bảy, nhằm tiết Vu Lan báo hiếu.
        Tín chủ (chúng) con là: ……………
        Ngụ tại: ……………

        Thành tâm sửa biện hương hoa lễ vật, trà quả bày lên trước án.
        Chúng con thành tâm kính mời:
        — Các vị Tiên linh, Gia tiên nội ngoại họ ………
        — Các vong linh cô hồn không nơi nương tựa

        Cúi xin chứng giám lòng thành, thụ hưởng lễ vật.
        Phù hộ độ trì cho gia đình chúng con luôn được bình an, mạnh khỏe, hạnh phúc.

        Nam mô A Di Đà Phật! (3 lần)
        """,
        isPopular: true,
        tags: [.init(text: "Phổ biến", type: .hot), .init(text: "Vu Lan", type: .normal)]
    )

    static let allPrayers: [Prayer] = [
        // ═══ CÚNG GIỖ ═══
        Prayer(
            title: "Văn khấn cúng giỗ (ngày giỗ thường)",
            description: "Bài khấn cúng giỗ Ông Bà, Cha Mẹ, người thân đã khuất",
            category: .gio,
            iconName: "flame.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
            Con kính lạy Hoàng Thiên Hậu Thổ, chư vị Tôn thần.
            Con kính lạy ngài Đông Trù Tư mệnh Táo phủ Thần quân.
            Con kính lạy các ngài Thần linh, Thổ địa cai quản trong xứ này.

            Hôm nay là ngày …… tháng …… năm ……
            Là ngày giỗ của ……………

            Tín chủ (chúng) con là: ……………
            Ngụ tại: ……………

            Thành tâm sửa biện hương hoa lễ vật, trà quả và các thứ cúng dâng,
            bày lên trước án kính mời các vị Hương linh gia tiên.

            Trước linh vị của ……………
            Chúng con cúi đầu thành kính nhớ ơn, dâng nén tâm hương.

            Cúi xin chứng giám lòng thành, thụ hưởng lễ vật,
            phù hộ cho toàn gia an khang, vạn sự hanh thông.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: true,
            tags: [.init(text: "Phổ biến", type: .hot), .init(text: "Giỗ", type: .normal)]
        ),
        Prayer(
            title: "Văn khấn cúng 49 ngày",
            description: "Bài khấn cúng 49 ngày cho người mới mất",
            category: .gio,
            iconName: "flame.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
            Con kính lạy Đức Địa Tạng Vương Bồ Tát.
            Con kính lạy Đức Đại Thánh U Minh Giáo Chủ.

            Hôm nay là ngày …… tháng …… năm ……
            Nhằm ngày chung thất (49 ngày) của ……………

            Tín chủ con là: ……………
            Ngụ tại: ……………

            Chúng con thành tâm thiết lễ cúng dường, hương hoa trà quả,
            kính dâng trước linh vị.

            Cúi xin linh hồn ………… được siêu thoát về cõi an lành.
            Nguyện cầu mười phương Chư Phật từ bi tiếp độ.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: false,
            tags: [.init(text: "Tang lễ", type: .normal)]
        ),
        Prayer(
            title: "Văn khấn cúng 100 ngày",
            description: "Bài khấn lễ tốt khốc (100 ngày) cho người mới mất",
            category: .gio,
            iconName: "flame.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
            Con kính lạy Hoàng Thiên Hậu Thổ, chư vị Tôn thần.
            Con kính lạy Đức Đại Thánh U Minh Giáo Chủ.

            Hôm nay là ngày …… tháng …… năm ……
            Nhằm ngày tốt khốc (100 ngày) của ……………

            Tín chủ con là: ……………
            Ngụ tại: ……………

            Chúng con thành tâm thiết lễ cúng dường.
            Kính cẩn dâng nén tâm hương trước linh vị.

            Cúi xin linh hồn ………… thanh thản siêu sinh.
            Phù hộ cho con cháu bình an, mạnh khỏe.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: false,
            tags: [.init(text: "Tang lễ", type: .normal)]
        ),
        Prayer(
            title: "Văn khấn cúng giỗ đầu (tiểu tường)",
            description: "Bài khấn giỗ đầu tiên sau khi mất",
            category: .gio,
            iconName: "flame.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
            Con kính lạy Hoàng Thiên Hậu Thổ, chư vị Tôn thần.

            Hôm nay là ngày …… tháng …… năm ……
            Nhằm ngày tiểu tường (giỗ đầu) của ……………

            Tín chủ con là: ……………
            Ngụ tại: ……………

            Gia đình chúng con một lòng nhớ thương người đã khuất,
            Thành tâm sửa biện hương hoa lễ vật kính dâng.

            Kính mời hương linh ………… giáng lâm, chứng giám lòng thành.
            Cúi xin phù hộ con cháu bình an, gia đạo hưng long.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: false,
            tags: [.init(text: "Giỗ đầu", type: .normal)]
        ),

        // ═══ RẰM & MÙNG 1 ═══
        Prayer(
            title: "Văn khấn cúng Rằm hàng tháng",
            description: "Bài khấn ngày rằm (15 Âm lịch) tại gia",
            category: .ram,
            iconName: "moon.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
            Con kính lạy Hoàng Thiên Hậu Thổ, chư vị Tôn thần.
            Con kính lạy ngài Đông Trù Tư mệnh Táo phủ Thần quân.
            Con kính lạy các ngài Thần linh, Thổ Địa cai quản trong xứ này.

            Hôm nay là ngày Rằm, tháng …… năm ……
            Tín chủ (chúng) con là: ……………
            Ngụ tại: ……………

            Thành tâm sửa biện hương hoa lễ vật kính dâng trước án.
            Cúi xin chư vị Tôn thần chứng giám lòng thành.
            Phù hộ độ trì cho gia đình chúng con:
            — An khang thịnh vượng
            — Mọi sự hanh thông
            — Bệnh tật tiêu trừ

            Chúng con lễ bạc tâm thành, cúi xin chứng giám.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: true,
            tags: [.init(text: "Phổ biến", type: .hot), .init(text: "Rằm", type: .normal)]
        ),
        Prayer(
            title: "Văn khấn cúng Mùng 1 hàng tháng",
            description: "Bài khấn ngày mùng 1 (1 Âm lịch) tại gia",
            category: .ram,
            iconName: "circle.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
            Con kính lạy Hoàng Thiên Hậu Thổ, chư vị Tôn thần.
            Con kính lạy ngài Đông Trù Tư mệnh Táo phủ Thần quân.

            Hôm nay là ngày mùng 1, tháng …… năm ……
            Tín chủ (chúng) con là: ……………
            Ngụ tại: ……………

            Nhân ngày sóc vọng, tín chủ thành tâm sắm lễ,
            hương hoa trà quả kính dâng trước án.

            Cúi xin chư vị Tôn thần chứng giám.
            Ban phước lành cho toàn gia an khang.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: false,
            tags: [.init(text: "Mùng 1", type: .normal)]
        ),

        // ═══ TẾT ═══
        Prayer(
            title: "Văn khấn cúng Giao Thừa (ngoài trời)",
            description: "Bài khấn đêm Giao Thừa — tiễn Ông Hành Khiển cũ",
            category: .tet,
            iconName: "gift.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Kính lạy: Đương niên Hành Khiển Chí đức Tôn thần,
            Đương cảnh Thành Hoàng Chư vị Đại Vương,
            Ngài Đông Trù Tư mệnh Táo phủ Thần quân,
            Các ngài Thần linh Thổ Địa cai quản trong xứ này.

            Nay là phút giao thừa năm cũ …… bước sang năm mới ……

            Tín chủ (chúng) con là: ……………
            Ngụ tại: ……………

            Phút thiêng giao thừa, tín chủ con thành tâm sửa biện hương hoa lễ vật,
            kính cẩn dâng lên trước án.

            Kính tiễn Ông Hành Khiển năm cũ, cung nghinh Ông Hành Khiển năm mới.
            Cầu xin năm mới: gia đình an khang, vạn sự như ý, tài lộc dồi dào.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: true,
            tags: [.init(text: "Phổ biến", type: .hot), .init(text: "Tết", type: .normal)]
        ),
        Prayer(
            title: "Văn khấn cúng Tất Niên (30 Tết)",
            description: "Mời ông bà tổ tiên về ăn Tết cùng con cháu",
            category: .tet,
            iconName: "gift.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
            Con kính lạy Hoàng Thiên Hậu Thổ, chư vị Tôn thần.
            Con kính lạy ngài Đông Trù Tư mệnh Táo phủ Thần quân.

            Hôm nay là ngày 30 tháng Chạp năm ……
            Tín chủ (chúng) con là: ……………
            Ngụ tại: ……………

            Năm cũ sắp qua, năm mới sắp đến.
            Chúng con cúi đầu kính mời:
            — Các cụ Tổ tiên nội ngoại họ ……
            — Cùng chư vị Hương linh

            Giáng lâm hưởng lễ, ăn Tết cùng con cháu.
            Phù hộ cho gia đình năm mới vạn sự tốt lành.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: false,
            tags: [.init(text: "Tết", type: .normal)]
        ),
        Prayer(
            title: "Văn khấn cúng mùng 1 Tết (Tân niên)",
            description: "Cúng ngày mùng 1 Tết Nguyên Đán tại gia",
            category: .tet,
            iconName: "gift.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
            Con kính lạy Hoàng Thiên Hậu Thổ, chư vị Tôn thần.
            Con kính lạy ngài Đông Trù Tư mệnh Táo phủ Thần quân.

            Hôm nay là ngày mùng 1 Tết Nguyên Đán, năm ……
            Tín chủ (chúng) con là: ……………
            Ngụ tại: ……………

            Đầu năm mới, chúng con thành tâm dâng lễ.
            Kính mời: Các cụ Tổ tiên, Ông Bà nội ngoại.
            Giáng lâm chứng giám tấm lòng thành.

            Cầu xin năm mới: phúc lộc song toàn, gia đạo hưng long.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: false,
            tags: [.init(text: "Tết", type: .normal)]
        ),
        Prayer(
            title: "Văn khấn cúng ông Công ông Táo (23 tháng Chạp)",
            description: "Tiễn ông Táo về trời báo cáo Ngọc Hoàng",
            category: .tet,
            iconName: "gift.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Kính lạy ngài Đông Trù Tư mệnh Táo phủ Thần quân.

            Hôm nay là ngày 23 tháng Chạp năm ……
            Tín chủ (chúng) con là: ……………
            Ngụ tại: ……………

            Nay nhân ngày ông Táo chầu Trời, tín chủ con thành tâm sắm lễ,
            kính tiễn ông Táo lên Thiên Đình.

            Kính xin Ngài lên tâu Ngọc Hoàng Thượng Đế những điều tốt đẹp.
            Cầu xin gia đình năm mới bình an, phúc lộc đầy nhà.

            Chúng con lễ bạc tâm thành, cúi xin chứng giám.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: true,
            tags: [.init(text: "Phổ biến", type: .hot), .init(text: "23 Chạp", type: .normal)]
        ),

        // ═══ NHẬP TRẠCH ═══
        Prayer(
            title: "Văn khấn nhập trạch (về nhà mới)",
            description: "Xin phép Thổ Công, Thổ Địa cho gia đình vào ở",
            category: .nhapTrach,
            iconName: "house.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Con kính lạy Hoàng Thiên Hậu Thổ, chư vị Tôn thần.
            Con kính lạy ngài Đông Trù Tư mệnh Táo phủ Thần quân.
            Con kính lạy các ngài Thần linh Thổ Địa cai quản trong xứ này.
            Con kính lạy các ngài Tiền chủ, Hậu chủ tại ngôi nhà này.

            Hôm nay là ngày …… tháng …… năm ……
            Tín chủ (chúng) con là: ……………
            Ngụ tại (địa chỉ mới): ……………

            Nay gia đình chúng con chuyển về nhà mới.
            Thành tâm sắm biện hương hoa lễ vật kính dâng trước án.

            Kính xin các vị Tôn thần cho phép gia đình chúng con được vào ở.
            Cầu xin: gia đạo yên ổn, mọi việc hanh thông, nhà cửa bình yên.

            Chúng con lễ bạc tâm thành, cúi xin chứng giám.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: false,
            tags: [.init(text: "Mới", type: .new), .init(text: "Nhà mới", type: .normal)]
        ),
        Prayer(
            title: "Văn khấn cúng động thổ xây nhà",
            description: "Cúng xin phép động thổ trước khi xây dựng",
            category: .nhapTrach,
            iconName: "house.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Con kính lạy Hoàng Thiên Hậu Thổ, chư vị Tôn thần.
            Con kính lạy Thanh Long, Bạch Hổ, Chu Tước, Huyền Vũ.
            Con kính lạy các ngài Thần linh Thổ Địa cai quản trong xứ này.

            Hôm nay là ngày …… tháng …… năm ……
            Tín chủ (chúng) con là: ……………
            Tại: ……………

            Nay chúng con khởi công xây dựng.
            Kính xin các vị Tôn thần cho phép gia đình động thổ.
            Cầu xin: xây dựng thuận lợi, bình an, không trở ngại.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: false,
            tags: [.init(text: "Xây nhà", type: .normal)]
        ),

        // ═══ KHAI TRƯƠNG ═══
        Prayer(
            title: "Văn khấn khai trương",
            description: "Khai trương cửa hàng, công ty — cầu tài lộc hanh thông",
            category: .kinhDoanh,
            iconName: "banknote",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Con kính lạy Hoàng Thiên Hậu Thổ, chư vị Tôn thần.
            Con kính lạy Ông Thần Tài, Ông Thổ Địa.
            Con kính lạy các ngài Thần linh cai quản trong xứ này.

            Hôm nay là ngày …… tháng …… năm ……
            Tín chủ (chúng) con là: ……………
            Tại cửa hàng / công ty: ……………
            Địa chỉ: ……………

            Nay nhân ngày khai trương, chúng con thành tâm sắm lễ kính dâng.
            Cầu xin: buôn bán phát đạt, khách hàng đông đúc, tài lộc hanh thông.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: false,
            tags: [.init(text: "Kinh doanh", type: .normal)]
        ),
        Prayer(
            title: "Văn khấn cúng Thần Tài mùng 10 tháng Giêng",
            description: "Vía Thần Tài — cầu tài lộc đầu năm",
            category: .kinhDoanh,
            iconName: "banknote",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Con kính lạy Ông Thần Tài, Ông Thổ Địa.
            Con kính lạy Ngũ phương, Ngũ thổ Long Thần.

            Hôm nay là ngày mùng 10 tháng Giêng năm ……
            Nhằm ngày Vía Thần Tài.

            Tín chủ (chúng) con là: ……………
            Ngụ tại: ……………

            Thành tâm kính lễ, cầu xin Ông Thần Tài ban phước tài lộc,
            làm ăn phát đạt, tiền vào như nước.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: true,
            tags: [.init(text: "Phổ biến", type: .hot), .init(text: "Tài lộc", type: .normal)]
        ),

        // ═══ ĐI CHÙA ═══
        Prayer(
            title: "Văn khấn ban Tam Bảo (chùa)",
            description: "Khấn trước ban Tam Bảo chính điện khi đi chùa",
            category: .chua,
            iconName: "building.columns.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Đệ tử con thành tâm lạy Phật, lạy Pháp, lạy Tăng.
            Con kính lạy Đức Phật Thích Ca Mâu Ni.
            Con kính lạy Đức Phật A Di Đà.
            Con kính lạy Đức Quán Thế Âm Bồ Tát.
            Con kính lạy Đức Đại Thế Chí Bồ Tát.
            Con kính lạy Đức Địa Tạng Vương Bồ Tát.

            Đệ tử con là: ……………
            Ngụ tại: ……………

            Hôm nay là ngày …… tháng …… năm ……
            Đệ tử con đến trước Tam Bảo, thành tâm dâng hương, lễ Phật.

            Cầu xin: gia đạo bình an, thân tâm an lạc,
            tai qua nạn khỏi, phúc tuệ song tu.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: true,
            tags: [.init(text: "Phổ biến", type: .hot), .init(text: "Đi chùa", type: .normal)]
        ),
        Prayer(
            title: "Văn khấn Thần Tài — Thổ Địa",
            description: "Cúng Ông Thần Tài, Ông Thổ Địa tại nhà",
            category: .chua,
            iconName: "building.columns.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Con kính lạy Ông Thần Tài.
            Con kính lạy Ông Thổ Địa.
            Con kính lạy Ngũ phương, Ngũ thổ Long Thần.

            Hôm nay là ngày …… tháng …… năm ……
            Tín chủ (chúng) con là: ……………
            Ngụ tại: ……………

            Thành tâm dâng hương hoa lễ vật kính cúng.
            Cúi xin Ông Thần Tài, Ông Thổ Địa:
            — Phù hộ gia đình bình an
            — Làm ăn phát đạt, tiền tài dồi dào
            — Mọi sự hanh thông tốt đẹp

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: false,
            tags: [.init(text: "Tài lộc", type: .normal)]
        ),
        Prayer(
            title: "Văn khấn Đức Ông (chùa)",
            description: "Khấn trước ban Đức Ông khi đi lễ chùa",
            category: .chua,
            iconName: "building.columns.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Con kính lạy Đức Ông Tu Đạt Đa, Đại Thí Chủ.
            Con kính lạy chư vị Hộ Pháp.

            Đệ tử con là: ……………
            Ngụ tại: ……………

            Hôm nay là ngày …… tháng …… năm ……
            Đệ tử con đến trước Đức Ông, thành tâm lễ bái.

            Cầu xin Đức Ông phù hộ:
            — Gia đạo yên vui
            — Sức khỏe dồi dào
            — Công việc thuận lợi

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: false,
            tags: [.init(text: "Đi chùa", type: .normal)]
        ),

        // ═══ TANG LỄ ═══
        Prayer(
            title: "Văn khấn lễ cúng cơm hàng ngày (tang lễ)",
            description: "Cúng cơm cho người mới mất trong thời gian tang lễ",
            category: .tangLe,
            iconName: "leaf.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Hôm nay là ngày …… tháng …… năm ……
            Con cháu thành tâm dâng cơm nước, hương hoa.
            Kính mời hương linh ………… về hưởng lễ.

            Cúi xin linh hồn an nghỉ, sớm được siêu thoát.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: false,
            tags: [.init(text: "Tang lễ", type: .normal)]
        ),
        Prayer(
            title: "Văn khấn lễ chung thất (tuần chung)",
            description: "Bài cúng kết thúc 7 tuần lễ (49 ngày) cho người mất",
            category: .tangLe,
            iconName: "leaf.fill",
            content: """
            Nam mô A Di Đà Phật! (3 lần)

            Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
            Con kính lạy Đức Địa Tạng Vương Bồ Tát.

            Hôm nay là ngày …… tháng …… năm ……
            Nhằm ngày chung thất (49 ngày) của ……………

            Con cháu thành tâm dâng lễ.
            Nguyện cầu hương linh sớm siêu thoát.

            Nam mô A Di Đà Phật! (3 lần)
            """,
            isPopular: false,
            tags: [.init(text: "Tang lễ", type: .normal)]
        ),
    ]

    static func prayers(for category: PrayerCategory) -> [Prayer] {
        if category == .all { return allPrayers }
        return allPrayers.filter { $0.category == category }
    }

    static func search(_ query: String) -> [Prayer] {
        let q = query.lowercased()
        return allPrayers.filter {
            $0.title.lowercased().contains(q) ||
            $0.description.lowercased().contains(q) ||
            $0.category.rawValue.lowercased().contains(q)
        }
    }

    /// Group prayers by category (for display), ordered
    static func groupedPrayers(for category: PrayerCategory) -> [(section: String, icon: String, prayers: [Prayer])] {
        let list = prayers(for: category)

        if category != .all {
            return [(section: category.rawValue, icon: category.icon, prayers: list)]
        }

        var groups: [(String, String, [Prayer])] = []
        let categories: [PrayerCategory] = [.gio, .ram, .tet, .nhapTrach, .kinhDoanh, .chua, .tangLe]
        for cat in categories {
            let items = list.filter { $0.category == cat }
            if !items.isEmpty {
                groups.append((cat.rawValue, cat.icon, items))
            }
        }
        return groups
    }
}
