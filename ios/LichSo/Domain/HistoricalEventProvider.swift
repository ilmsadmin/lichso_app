import Foundation

/// Provider cung cấp dữ liệu "Ngày này năm xưa" (This Day in History).
struct HistoricalEventProvider {

    /// Trả về danh sách sự kiện lịch sử theo ngày/tháng.
    /// Sắp xếp theo năm giảm dần (mới nhất trước).
    static func getEvents(day: Int, month: Int) -> [HistoricalEvent] {
        let key = String(format: "%02d/%02d", day, month)
        return (eventsMap[key] ?? []).sorted { $0.year > $1.year }
    }

    // ══════════════════════════════════════════
    // DỮ LIỆU MẪU
    // ══════════════════════════════════════════
    private static let eventsMap: [String: [HistoricalEvent]] = [
        "05/04": [
            HistoricalEvent(year: 1975, title: "Chiến dịch Hồ Chí Minh bắt đầu giai đoạn quyết định",
                description: "Quân Giải phóng tiến sát Sài Gòn, chuẩn bị cho trận đánh cuối cùng thống nhất đất nước.",
                category: .vietnam, importance: .major),
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
        "06/04": [
            HistoricalEvent(year: 1972, title: "Mỹ bắt đầu chiến dịch Linebacker ném bom miền Bắc Việt Nam",
                description: "Không quân Mỹ tiến hành chiến dịch ném bom quy mô lớn nhằm ngăn chặn cuộc tiến công Xuân - Hè 1972.",
                category: .vietnam, importance: .major),
            HistoricalEvent(year: 1896, title: "Olympic hiện đại đầu tiên khai mạc tại Athens",
                description: "Thế vận hội Olympic hiện đại lần đầu tiên được tổ chức tại Athens, Hy Lạp với 241 vận động viên từ 14 quốc gia.",
                category: .world, importance: .major),
            HistoricalEvent(year: 1947, title: "Giải thưởng Tony được trao lần đầu tiên",
                description: "Giải Tony (Antoinette Perry Award) vinh danh những thành tựu xuất sắc trong sân khấu Broadway.",
                category: .culture, importance: .minor),
        ],
        "30/04": [
            HistoricalEvent(year: 1975, title: "Giải phóng miền Nam, thống nhất đất nước",
                description: "Chiến dịch Hồ Chí Minh toàn thắng, xe tăng tiến vào Dinh Độc Lập, kết thúc chiến tranh, thống nhất Việt Nam.",
                category: .vietnam, importance: .major),
            HistoricalEvent(year: 1945, title: "Adolf Hitler tự sát trong boongke ở Berlin",
                description: "Trùm phát xít Hitler tự sát trong boongke dưới lòng đất khi quân Liên Xô bao vây Berlin.",
                category: .world, importance: .major),
            HistoricalEvent(year: 1789, title: "George Washington nhậm chức Tổng thống Mỹ đầu tiên",
                description: "George Washington tuyên thệ nhậm chức tại Federal Hall, New York, trở thành Tổng thống đầu tiên của Hoa Kỳ.",
                category: .world, importance: .major),
        ],
        "02/09": [
            HistoricalEvent(year: 1945, title: "Chủ tịch Hồ Chí Minh đọc Tuyên ngôn Độc lập",
                description: "Tại Quảng trường Ba Đình, Chủ tịch Hồ Chí Minh đọc bản Tuyên ngôn Độc lập, khai sinh nước Việt Nam Dân chủ Cộng hòa.",
                category: .vietnam, importance: .major),
            HistoricalEvent(year: 1969, title: "Chủ tịch Hồ Chí Minh từ trần",
                description: "Chủ tịch Hồ Chí Minh qua đời tại Hà Nội, hưởng thọ 79 tuổi. Cả nước chìm trong niềm tiếc thương vô hạn.",
                category: .vietnam, importance: .major),
            HistoricalEvent(year: 1945, title: "Nhật Bản ký hiệp ước đầu hàng, kết thúc Thế chiến II",
                description: "Lễ ký đầu hàng diễn ra trên tàu USS Missouri tại Vịnh Tokyo, chính thức kết thúc Chiến tranh thế giới thứ hai.",
                category: .world, importance: .major),
        ],
        "07/05": [
            HistoricalEvent(year: 1954, title: "Chiến thắng Điện Biên Phủ",
                description: "Quân đội nhân dân Việt Nam giành thắng lợi tại Điện Biên Phủ, kết thúc 56 ngày đêm chiến đấu, buộc Pháp phải ký Hiệp định Genève.",
                category: .vietnam, importance: .major),
            HistoricalEvent(year: 1824, title: "Bản giao hưởng số 9 của Beethoven được trình diễn lần đầu",
                description: "Ludwig van Beethoven chỉ huy buổi ra mắt Giao hưởng số 9 cung Rê thứ tại Vienna, Áo.",
                category: .culture, importance: .minor),
        ],
        "01/01": [
            HistoricalEvent(year: 1960, title: "Cameroon tuyên bố độc lập",
                description: "Cameroon chính thức tuyên bố độc lập từ Pháp, trở thành quốc gia châu Phi đầu tiên giành độc lập trong năm 1960.",
                category: .world, importance: .minor),
            HistoricalEvent(year: 1804, title: "Haiti tuyên bố độc lập",
                description: "Haiti trở thành quốc gia da đen đầu tiên giành độc lập và là nước thứ hai ở châu Mỹ thoát khỏi ách thực dân.",
                category: .world, importance: .major),
        ],
        "10/03": [
            HistoricalEvent(year: 1975, title: "Chiến thắng Buôn Ma Thuột",
                description: "Quân Giải phóng tấn công và giải phóng thị xã Buôn Ma Thuột, mở đầu cuộc Tổng tiến công mùa Xuân 1975.",
                category: .vietnam, importance: .major),
            HistoricalEvent(year: 1876, title: "Alexander Graham Bell thực hiện cuộc gọi điện thoại đầu tiên",
                description: "Bell gọi cho trợ lý Thomas Watson với câu nói nổi tiếng: \"Mr. Watson, come here. I want to see you.\"",
                category: .science, importance: .major),
        ],
        "19/05": [
            HistoricalEvent(year: 1890, title: "Chủ tịch Hồ Chí Minh ra đời",
                description: "Nguyễn Sinh Cung (sau này là Hồ Chí Minh) chào đời tại làng Hoàng Trù, xã Kim Liên, huyện Nam Đàn, Nghệ An.",
                category: .vietnam, importance: .major),
            HistoricalEvent(year: 1941, title: "Thành lập Mặt trận Việt Minh",
                description: "Hồ Chí Minh triệu tập Hội nghị Trung ương 8, thành lập Việt Nam Độc lập Đồng minh Hội (Việt Minh).",
                category: .vietnam, importance: .major),
        ],
        "20/07": [
            HistoricalEvent(year: 1969, title: "Con người đặt chân lên Mặt Trăng",
                description: "Phi hành gia Neil Armstrong trở thành người đầu tiên đặt chân lên Mặt Trăng trong sứ mệnh Apollo 11 của NASA.",
                category: .science, importance: .major),
            HistoricalEvent(year: 1954, title: "Hiệp định Genève được ký kết",
                description: "Hiệp định Genève về Đông Dương được ký kết, chia cắt Việt Nam tại vĩ tuyến 17.",
                category: .vietnam, importance: .major),
        ],
        "03/02": [
            HistoricalEvent(year: 1930, title: "Thành lập Đảng Cộng sản Việt Nam",
                description: "Hội nghị hợp nhất các tổ chức cộng sản tại Hương Cảng (Hong Kong), thành lập Đảng Cộng sản Việt Nam do Nguyễn Ái Quốc chủ trì.",
                category: .vietnam, importance: .major),
        ],
        "08/03": [
            HistoricalEvent(year: 1975, title: "Ngày Quốc tế Phụ nữ",
                description: "Liên Hợp Quốc chính thức công nhận ngày 8/3 là Ngày Quốc tế Phụ nữ.",
                category: .world, importance: .major),
            HistoricalEvent(year: 1917, title: "Cách mạng tháng Hai bùng nổ tại Nga",
                description: "Phụ nữ công nhân Petrograd biểu tình đòi \"Bánh mì và Hòa bình\", khởi đầu cuộc Cách mạng tháng Hai lật đổ Sa hoàng.",
                category: .world, importance: .major),
        ],
    ]
}
