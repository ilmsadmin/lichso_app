import Foundation

// ═══════════════════════════════════════════
// HistoricalEventProvider — "Ngày này năm xưa"
// Port từ Android HistoricalEventProvider.kt
// ═══════════════════════════════════════════

struct HistoricalEvent: Identifiable {
    let id = UUID()
    let year: Int
    let title: String
    let description: String
    let category: HistoryCategory
    let importance: EventImportance
    var hasImage: Bool = false
}

enum HistoryCategory {
    case vietnam, world, science, culture
}

enum EventImportance {
    case major, minor
}

enum HistoricalEventProvider {

    /// Trả về danh sách sự kiện lịch sử theo ngày/tháng.
    /// Sắp xếp theo năm giảm dần (mới nhất trước).
    static func getEvents(day: Int, month: Int) -> [HistoricalEvent] {
        let key = String(format: "%02d/%02d", day, month)
        return (eventsMap[key] ?? []).sorted { $0.year > $1.year }
    }

    /// Kiểm tra có sự kiện nào trong ngày đó không.
    static func hasEvents(day: Int, month: Int) -> Bool {
        let key = String(format: "%02d/%02d", day, month)
        return !(eventsMap[key] ?? []).isEmpty
    }

    // ══════════════════════════════════════════
    // Dữ liệu sự kiện lịch sử theo ngày/tháng
    // ══════════════════════════════════════════
    private static let eventsMap: [String: [HistoricalEvent]] = [

        // ── 5 tháng 4 ──
        "05/04": [
            HistoricalEvent(year: 1975, title: "Chiến dịch Hồ Chí Minh bắt đầu giai đoạn quyết định",
                description: "Quân Giải phóng tiến sát Sài Gòn, chuẩn bị cho trận đánh cuối cùng thống nhất đất nước.",
                category: .vietnam, importance: .major, hasImage: true),
            HistoricalEvent(year: 2009, title: "Hội nghị thượng đỉnh NATO tại Strasbourg",
                description: "Hội nghị kỷ niệm 60 năm thành lập NATO, với sự tham dự của nhiều nguyên thủ quốc gia.",
                category: .world, importance: .major),
            HistoricalEvent(year: 1992, title: "Hiến pháp nước CHXHCN Việt Nam được thông qua",
                description: "Quốc hội khóa VIII thông qua bản Hiến pháp mới, đánh dấu bước ngoặt đổi mới toàn diện.",
                category: .vietnam, importance: .major),
            HistoricalEvent(year: 1951, title: "Thành lập Đảng Lao động Việt Nam",
                description: "Đại hội lần thứ II của Đảng, đổi tên từ Đảng Cộng sản Đông Dương thành Đảng Lao động Việt Nam.",
                category: .vietnam, importance: .minor),
            HistoricalEvent(year: 1722, title: "Hòn đảo Phục Sinh được phát hiện",
                description: "Nhà thám hiểm Hà Lan Jacob Roggeveen phát hiện đảo Phục Sinh (Easter Island) ở Thái Bình Dương.",
                category: .world, importance: .minor),
        ],

        // ── 6 tháng 4 ──
        "06/04": [
            HistoricalEvent(year: 1972, title: "Mỹ bắt đầu chiến dịch Linebacker ném bom miền Bắc Việt Nam",
                description: "Không quân Mỹ tiến hành chiến dịch ném bom quy mô lớn nhằm ngăn chặn cuộc tiến công Xuân - Hè 1972.",
                category: .vietnam, importance: .major, hasImage: true),
            HistoricalEvent(year: 1896, title: "Olympic hiện đại đầu tiên khai mạc tại Athens",
                description: "Thế vận hội Olympic hiện đại lần đầu tiên được tổ chức tại Athens, Hy Lạp với 241 vận động viên từ 14 quốc gia.",
                category: .world, importance: .major),
            HistoricalEvent(year: 1947, title: "Giải thưởng Tony được trao lần đầu tiên",
                description: "Giải Tony (Antoinette Perry Award) vinh danh những thành tựu xuất sắc trong sân khấu Broadway.",
                category: .culture, importance: .minor),
            HistoricalEvent(year: 1830, title: "Giáo hội Các Thánh Hữu Ngày Sau được thành lập",
                description: "Joseph Smith thành lập Giáo hội Các Thánh Hữu Ngày Sau (Mormon) tại Fayette, New York, Hoa Kỳ.",
                category: .world, importance: .minor),
        ],

        // ── 30 tháng 4 ──
        "30/04": [
            HistoricalEvent(year: 1975, title: "Giải phóng miền Nam, thống nhất đất nước",
                description: "Chiến dịch Hồ Chí Minh toàn thắng, xe tăng tiến vào Dinh Độc Lập, kết thúc chiến tranh, thống nhất Việt Nam.",
                category: .vietnam, importance: .major, hasImage: true),
            HistoricalEvent(year: 1945, title: "Adolf Hitler tự sát trong boongke ở Berlin",
                description: "Trùm phát xít Hitler tự sát trong boongke dưới lòng đất khi quân Liên Xô bao vây Berlin.",
                category: .world, importance: .major),
            HistoricalEvent(year: 1789, title: "George Washington nhậm chức Tổng thống Mỹ đầu tiên",
                description: "George Washington tuyên thệ nhậm chức tại Federal Hall, New York, trở thành Tổng thống đầu tiên của Hoa Kỳ.",
                category: .world, importance: .major),
        ],

        // ── 2 tháng 9 ──
        "02/09": [
            HistoricalEvent(year: 1945, title: "Chủ tịch Hồ Chí Minh đọc Tuyên ngôn Độc lập",
                description: "Tại Quảng trường Ba Đình, Chủ tịch Hồ Chí Minh đọc bản Tuyên ngôn Độc lập, khai sinh nước Việt Nam Dân chủ Cộng hòa.",
                category: .vietnam, importance: .major, hasImage: true),
            HistoricalEvent(year: 1969, title: "Chủ tịch Hồ Chí Minh từ trần",
                description: "Chủ tịch Hồ Chí Minh qua đời tại Hà Nội, hưởng thọ 79 tuổi. Cả nước chìm trong niềm tiếc thương vô hạn.",
                category: .vietnam, importance: .major),
            HistoricalEvent(year: 1945, title: "Nhật Bản ký hiệp ước đầu hàng, kết thúc Thế chiến II",
                description: "Lễ ký đầu hàng diễn ra trên tàu USS Missouri tại Vịnh Tokyo, chính thức kết thúc Chiến tranh thế giới thứ hai.",
                category: .world, importance: .major),
        ],

        // ── 7 tháng 5 ──
        "07/05": [
            HistoricalEvent(year: 1954, title: "Chiến thắng Điện Biên Phủ",
                description: "Quân đội nhân dân Việt Nam giành thắng lợi tại Điện Biên Phủ, kết thúc 56 ngày đêm chiến đấu, buộc Pháp phải ký Hiệp định Genève.",
                category: .vietnam, importance: .major, hasImage: true),
            HistoricalEvent(year: 1824, title: "Bản giao hưởng số 9 của Beethoven được trình diễn lần đầu",
                description: "Ludwig van Beethoven chỉ huy buổi ra mắt Giao hưởng số 9 cung Rê thứ tại Vienna, Áo.",
                category: .culture, importance: .minor),
        ],

        // ── 1 tháng 1 ──
        "01/01": [
            HistoricalEvent(year: 1960, title: "Cameroon tuyên bố độc lập",
                description: "Cameroon chính thức tuyên bố độc lập từ Pháp, trở thành quốc gia châu Phi đầu tiên giành độc lập trong năm 1960.",
                category: .world, importance: .minor),
            HistoricalEvent(year: 1804, title: "Haiti tuyên bố độc lập",
                description: "Haiti trở thành quốc gia da đen đầu tiên giành độc lập và là nước thứ hai ở châu Mỹ thoát khỏi ách thực dân.",
                category: .world, importance: .major),
        ],

        // ── 10 tháng 3 ──
        "10/03": [
            HistoricalEvent(year: 1975, title: "Chiến thắng Buôn Ma Thuột",
                description: "Quân Giải phóng tấn công và giải phóng thị xã Buôn Ma Thuột, mở đầu cuộc Tổng tiến công mùa Xuân 1975.",
                category: .vietnam, importance: .major, hasImage: true),
            HistoricalEvent(year: 1876, title: "Alexander Graham Bell thực hiện cuộc gọi điện thoại đầu tiên",
                description: "Bell gọi cho trợ lý Thomas Watson với câu nói nổi tiếng: \"Mr. Watson, come here. I want to see you.\"",
                category: .science, importance: .major),
        ],

        // ── 19 tháng 5 ──
        "19/05": [
            HistoricalEvent(year: 1890, title: "Chủ tịch Hồ Chí Minh ra đời",
                description: "Nguyễn Sinh Cung (sau này là Hồ Chí Minh) chào đời tại làng Hoàng Trù, xã Kim Liên, huyện Nam Đàn, Nghệ An.",
                category: .vietnam, importance: .major, hasImage: true),
            HistoricalEvent(year: 1941, title: "Thành lập Mặt trận Việt Minh",
                description: "Hồ Chí Minh triệu tập Hội nghị Trung ương 8, thành lập Việt Nam Độc lập Đồng minh Hội (Việt Minh).",
                category: .vietnam, importance: .major),
        ],

        // ── 20 tháng 7 ──
        "20/07": [
            HistoricalEvent(year: 1969, title: "Con người đặt chân lên Mặt Trăng",
                description: "Phi hành gia Neil Armstrong trở thành người đầu tiên đặt chân lên Mặt Trăng trong sứ mệnh Apollo 11 của NASA.",
                category: .science, importance: .major, hasImage: true),
            HistoricalEvent(year: 1954, title: "Hiệp định Genève được ký kết",
                description: "Hiệp định Genève về Đông Dương được ký kết, chia cắt Việt Nam tại vĩ tuyến 17.",
                category: .vietnam, importance: .major),
        ],

        // ── 3 tháng 2 ──
        "03/02": [
            HistoricalEvent(year: 1930, title: "Thành lập Đảng Cộng sản Việt Nam",
                description: "Hội nghị hợp nhất các tổ chức cộng sản tại Hương Cảng (Hong Kong), thành lập Đảng Cộng sản Việt Nam do Nguyễn Ái Quốc chủ trì.",
                category: .vietnam, importance: .major, hasImage: true),
        ],

        // ── 8 tháng 3 ──
        "08/03": [
            HistoricalEvent(year: 1975, title: "Ngày Quốc tế Phụ nữ",
                description: "Liên Hợp Quốc chính thức công nhận ngày 8/3 là Ngày Quốc tế Phụ nữ.",
                category: .world, importance: .major),
            HistoricalEvent(year: 1917, title: "Cách mạng tháng Hai bùng nổ tại Nga",
                description: "Phụ nữ công nhân Petrograd biểu tình đòi \"Bánh mì và Hòa bình\", khởi đầu cuộc Cách mạng tháng Hai lật đổ Sa hoàng.",
                category: .world, importance: .major),
        ],

        // ── 15 tháng 4 ──
        "15/04": [
            HistoricalEvent(year: 1912, title: "Tàu Titanic chìm xuống Đại Tây Dương",
                description: "RMS Titanic chìm lúc 2:20 sáng sau khi đâm vào tảng băng trôi, khiến hơn 1.500 người thiệt mạng.",
                category: .world, importance: .major, hasImage: true),
            HistoricalEvent(year: 1452, title: "Leonardo da Vinci ra đời",
                description: "Thiên tài người Ý Leonardo da Vinci — họa sĩ, nhà khoa học, kỹ sư — chào đời tại Vinci, Tuscany.",
                category: .culture, importance: .major),
        ],

        // ── 12 tháng 4 ──
        "12/04": [
            HistoricalEvent(year: 1961, title: "Yuri Gagarin — người đầu tiên bay vào vũ trụ",
                description: "Phi hành gia Liên Xô Yuri Gagarin hoàn thành chuyến bay vòng quanh Trái Đất trên tàu Vostok 1, trở thành người đầu tiên trong không gian.",
                category: .science, importance: .major, hasImage: true),
        ],

        // ── 21 tháng 7 ──
        "21/07": [
            HistoricalEvent(year: 1969, title: "Neil Armstrong bước đi đầu tiên trên Mặt Trăng",
                description: "Neil Armstrong đặt chân trái lên bề mặt Mặt Trăng lúc 02:56 UTC, nói câu bất hủ: \"Một bước nhỏ của con người, một bước tiến vĩ đại của nhân loại.\"",
                category: .science, importance: .major),
        ],

        // ── 25 tháng 12 ──
        "25/12": [
            HistoricalEvent(year: 1991, title: "Liên Xô chính thức giải thể",
                description: "Mikhail Gorbachev từ chức, lá cờ Liên Xô lần cuối hạ xuống trên Điện Kremlin, kết thúc 69 năm tồn tại của Liên bang Xô Viết.",
                category: .world, importance: .major),
            HistoricalEvent(year: 1776, title: "George Washington vượt sông Delaware",
                description: "Trong đêm Giáng Sinh, George Washington dẫn quân vượt sông Delaware trong giá lạnh, giành chiến thắng bất ngờ tại Trenton.",
                category: .world, importance: .major),
        ],

        // ── 14 tháng 7 ──
        "14/07": [
            HistoricalEvent(year: 1789, title: "Phá ngục Bastille — Khởi đầu Cách mạng Pháp",
                description: "Nhân dân Paris phá ngục Bastille, biểu tượng của chế độ chuyên chế, mở đầu cuộc Đại Cách mạng Pháp.",
                category: .world, importance: .major, hasImage: true),
        ],

        // ── 6 tháng 8 ──
        "06/08": [
            HistoricalEvent(year: 1945, title: "Mỹ ném bom nguyên tử xuống Hiroshima",
                description: "Quả bom nguyên tử Little Boy phá hủy thành phố Hiroshima, Nhật Bản, giết chết ngay lập tức 70.000–80.000 người.",
                category: .world, importance: .major, hasImage: true),
        ],

        // ── 9 tháng 8 ──
        "09/08": [
            HistoricalEvent(year: 1945, title: "Mỹ ném bom nguyên tử xuống Nagasaki",
                description: "Quả bom Fat Man phá hủy Nagasaki, giết chết khoảng 40.000 người, buộc Nhật Bản đầu hàng.",
                category: .world, importance: .major),
        ],

        // ── 11 tháng 9 ──
        "11/09": [
            HistoricalEvent(year: 2001, title: "Vụ khủng bố 11/9 tại Hoa Kỳ",
                description: "Nhóm khủng bố Al-Qaeda cướp 4 máy bay, tấn công Tòa tháp đôi và Lầu Năm Góc, khiến gần 3.000 người thiệt mạng.",
                category: .world, importance: .major, hasImage: true),
        ],

        // ── 9 tháng 11 ──
        "09/11": [
            HistoricalEvent(year: 1989, title: "Bức tường Berlin sụp đổ",
                description: "Người dân Đông và Tây Đức phá dỡ Bức tường Berlin sau 28 năm chia cắt, mở đường cho sự thống nhất nước Đức.",
                category: .world, importance: .major, hasImage: true),
        ],

        // ── 28 tháng 7 ──
        "28/07": [
            HistoricalEvent(year: 1914, title: "Thế chiến I bùng nổ",
                description: "Áo-Hung tuyên chiến với Serbia, kéo theo phản ứng dây chuyền, mở đầu Chiến tranh thế giới thứ nhất.",
                category: .world, importance: .major),
        ],

        // ── 1 tháng 9 ──
        "01/09": [
            HistoricalEvent(year: 1939, title: "Thế chiến II bùng nổ — Đức tấn công Ba Lan",
                description: "Phát xít Đức xâm lược Ba Lan lúc 4:45 sáng, khởi đầu Chiến tranh thế giới thứ hai.",
                category: .world, importance: .major, hasImage: true),
            HistoricalEvent(year: 1858, title: "Pháp tấn công Đà Nẵng, mở đầu xâm lược Việt Nam",
                description: "Liên quân Pháp - Tây Ban Nha nổ súng tấn công cửa biển Đà Nẵng, bắt đầu quá trình xâm lược Việt Nam.",
                category: .vietnam, importance: .major),
        ],

        // ── 22 tháng 12 ──
        "22/12": [
            HistoricalEvent(year: 1944, title: "Ngày thành lập Quân đội Nhân dân Việt Nam",
                description: "Đội Việt Nam Tuyên truyền Giải phóng quân được thành lập tại Cao Bằng, tiền thân của Quân đội nhân dân Việt Nam.",
                category: .vietnam, importance: .major, hasImage: true),
        ],

        // ── 20 tháng 11 ──
        "20/11": [
            HistoricalEvent(year: 1989, title: "Công ước Liên Hợp Quốc về Quyền Trẻ em được thông qua",
                description: "Đại hội đồng LHQ thông qua Công ước về Quyền Trẻ em, văn kiện nhân quyền được phê chuẩn rộng rãi nhất lịch sử.",
                category: .world, importance: .major),
        ],

        // ── 8 tháng 9 ──
        "08/09": [
            HistoricalEvent(year: 1945, title: "Ngày xóa nạn mù chữ tại Việt Nam",
                description: "Chủ tịch Hồ Chí Minh ký Sắc lệnh thành lập Nha Bình dân học vụ, phát động phong trào diệt giặc dốt trên toàn quốc.",
                category: .vietnam, importance: .major),
        ],

        // ── 27 tháng 7 ──
        "27/07": [
            HistoricalEvent(year: 1947, title: "Ngày Thương binh Liệt sĩ Việt Nam",
                description: "Ngày 27/7/1947 được Chủ tịch Hồ Chí Minh chọn làm Ngày Thương binh, ghi nhớ và tri ân những người đã hy sinh vì Tổ quốc.",
                category: .vietnam, importance: .major),
        ],

        // ── 1 tháng 5 ──
        "01/05": [
            HistoricalEvent(year: 1886, title: "Ngày Quốc tế Lao động",
                description: "Công nhân Chicago đình công đòi ngày làm việc 8 giờ, mở đầu phong trào lao động quốc tế. LHQ chính thức công nhận ngày 1/5 là Ngày Quốc tế Lao động.",
                category: .world, importance: .major),
        ],

        // ── 20 tháng 10 ──
        "20/10": [
            HistoricalEvent(year: 1930, title: "Thành lập Hội Phụ nữ Việt Nam",
                description: "Hội Liên hiệp Phụ nữ Việt Nam được thành lập, trở thành tổ chức đại diện và bảo vệ quyền lợi phụ nữ Việt Nam.",
                category: .vietnam, importance: .major),
        ],

        // ── 17 tháng 1 ──
        "17/01": [
            HistoricalEvent(year: 1995, title: "Động đất Kobe, Nhật Bản",
                description: "Trận động đất 7,3 độ Richter tàn phá thành phố Kobe, Nhật Bản, khiến hơn 6.400 người thiệt mạng và 300.000 người mất nhà.",
                category: .world, importance: .major),
        ],

        // ── 26 tháng 12 ──
        "26/12": [
            HistoricalEvent(year: 2004, title: "Thảm họa sóng thần Ấn Độ Dương",
                description: "Trận động đất 9,1 độ Richter ngoài khơi Sumatra gây sóng thần khổng lồ, giết chết hơn 230.000 người ở 14 quốc gia.",
                category: .world, importance: .major, hasImage: true),
        ],

        // ── 21 tháng 3 ──
        "21/03": [
            HistoricalEvent(year: 1960, title: "Thảm sát Sharpeville, Nam Phi",
                description: "Cảnh sát Nam Phi nổ súng vào người biểu tình da đen phản đối chế độ Apartheid tại Sharpeville, giết chết 69 người.",
                category: .world, importance: .major),
        ],
    ]
}
