import Foundation

/// Thư viện ca dao, tục ngữ, thành ngữ Việt Nam và câu nói nổi tiếng.
struct VietnameseQuotes {
    static let all: [(quote: String, author: String)] = [
        // ── Tục ngữ Việt Nam ──
        ("Thuận vợ thuận chồng, tát Biển Đông cũng cạn", "Tục ngữ Việt Nam"),
        ("Có công mài sắt, có ngày nên kim", "Tục ngữ Việt Nam"),
        ("Đất lành chim đậu", "Tục ngữ Việt Nam"),
        ("Uống nước nhớ nguồn", "Tục ngữ Việt Nam"),
        ("Ăn quả nhớ kẻ trồng cây", "Tục ngữ Việt Nam"),
        ("Gần mực thì đen, gần đèn thì sáng", "Tục ngữ Việt Nam"),
        ("Đi một ngày đàng, học một sàng khôn", "Tục ngữ Việt Nam"),
        ("Không thầy đố mày làm nên", "Tục ngữ Việt Nam"),
        ("Học thầy không tày học bạn", "Tục ngữ Việt Nam"),
        ("Tốt gỗ hơn tốt nước sơn", "Tục ngữ Việt Nam"),
        ("Cái nết đánh chết cái đẹp", "Tục ngữ Việt Nam"),
        ("Kiến tha lâu cũng đầy tổ", "Tục ngữ Việt Nam"),
        ("Lời nói chẳng mất tiền mua, lựa lời mà nói cho vừa lòng nhau", "Tục ngữ Việt Nam"),
        ("Một con ngựa đau, cả tàu bỏ cỏ", "Tục ngữ Việt Nam"),
        ("Chớ thấy sóng cả mà ngã tay chèo", "Tục ngữ Việt Nam"),
        ("Có chí thì nên", "Tục ngữ Việt Nam"),
        ("Thất bại là mẹ thành công", "Tục ngữ Việt Nam"),
        ("Ở hiền gặp lành", "Tục ngữ Việt Nam"),
        ("Gieo gió gặt bão", "Tục ngữ Việt Nam"),
        ("Thua keo này, bày keo khác", "Tục ngữ Việt Nam"),
        ("Nhất nghệ tinh, nhất thân vinh", "Tục ngữ Việt Nam"),
        ("Đói cho sạch, rách cho thơm", "Tục ngữ Việt Nam"),
        ("Cha mẹ sinh con, trời sinh tính", "Tục ngữ Việt Nam"),
        ("Cá không ăn muối cá ươn, con cưỡng cha mẹ trăm đường con hư", "Tục ngữ Việt Nam"),
        ("Công cha như núi Thái Sơn, nghĩa mẹ như nước trong nguồn chảy ra", "Tục ngữ Việt Nam"),
        ("Máu chảy ruột mềm", "Tục ngữ Việt Nam"),
        ("Anh em như thể tay chân", "Tục ngữ Việt Nam"),
        ("Bán anh em xa mua láng giềng gần", "Tục ngữ Việt Nam"),
        ("Chị ngã em nâng", "Tục ngữ Việt Nam"),
        ("Khôn ngoan đối đáp người ngoài, gà cùng một mẹ chớ hoài đá nhau", "Tục ngữ Việt Nam"),
        ("Một giọt máu đào hơn ao nước lã", "Tục ngữ Việt Nam"),
        ("Cây có cội, nước có nguồn", "Tục ngữ Việt Nam"),
        ("Con hơn cha là nhà có phúc", "Tục ngữ Việt Nam"),
        ("Có đức mặc sức mà ăn", "Tục ngữ Việt Nam"),
        ("Người sống đống vàng", "Tục ngữ Việt Nam"),
        ("Nhiễu điều phủ lấy giá gương, người trong một nước phải thương nhau cùng", "Tục ngữ Việt Nam"),
        ("Ăn cây nào rào cây ấy", "Tục ngữ Việt Nam"),
        ("Muốn sang thì bắc cầu kiều, muốn con hay chữ thì yêu lấy thầy", "Tục ngữ Việt Nam"),
        ("Trăm hay không bằng tay quen", "Tục ngữ Việt Nam"),
        ("Đường đi hay tối, nói dối hay cùng", "Tục ngữ Việt Nam"),
        ("Ai ơi bưng bát cơm đầy, dẻo thơm một hạt, đắng cay muôn phần", "Tục ngữ Việt Nam"),
        ("Cha mẹ nuôi con biển hồ lai láng, con nuôi cha mẹ tính tháng tính ngày", "Tục ngữ Việt Nam"),
        ("Lá lành đùm lá rách", "Tục ngữ Việt Nam"),
        ("Thương người như thể thương thân", "Tục ngữ Việt Nam"),
        ("Tay làm hàm nhai, tay quai miệng trễ", "Tục ngữ Việt Nam"),
        ("Kiên nhẫn là mẹ thành công", "Tục ngữ Việt Nam"),

        // ── Ca dao Việt Nam ──
        ("Một cây làm chẳng nên non, ba cây chụm lại nên hòn núi cao", "Ca dao Việt Nam"),
        ("Trong đầm gì đẹp bằng sen, lá xanh bông trắng lại chen nhị vàng", "Ca dao Việt Nam"),
        ("Râu tôm nấu với ruột bầu, chồng chan vợ húp gật đầu khen ngon", "Ca dao Việt Nam"),
        ("Công cha nghĩa mẹ ơn thầy, nghĩ sao cho bõ những ngày ước ao", "Ca dao Việt Nam"),
        ("Chiều chiều ra đứng ngõ sau, trông về quê mẹ ruột đau chín chiều", "Ca dao Việt Nam"),
        ("Trên trời mây trắng như bông, ở dưới cánh đồng bông trắng như mây", "Ca dao Việt Nam"),
        ("Ai ơi đừng bỏ ruộng hoang, bao nhiêu tấc đất tấc vàng bấy nhiêu", "Ca dao Việt Nam"),
        ("Bầu ơi thương lấy bí cùng, tuy rằng khác giống nhưng chung một giàn", "Ca dao Việt Nam"),
        ("Con người có tổ có tông, như cây có cội như sông có nguồn", "Ca dao Việt Nam"),
        ("Mẹ già như chuối ba hương, như xôi nếp một như đường mía lau", "Ca dao Việt Nam"),
        ("Đố ai đếm được lá rừng, đố ai đếm được mấy tầng trời cao", "Ca dao Việt Nam"),
        ("Dù ai đi ngược về xuôi, nhớ ngày giỗ Tổ mùng Mười tháng Ba", "Ca dao Việt Nam"),
        ("Tu đâu cho bằng tu nhà, thờ cha kính mẹ mới là chân tu", "Ca dao Việt Nam"),
        ("Chim có tổ, người có tông", "Ca dao Việt Nam"),

        // ── Thành ngữ Việt Nam ──
        ("Đồng cam cộng khổ", "Thành ngữ Việt Nam"),
        ("Tình thâm nghĩa nặng", "Thành ngữ Việt Nam"),
        ("Kề vai sát cánh", "Thành ngữ Việt Nam"),
        ("Một nắng hai sương", "Thành ngữ Việt Nam"),
        ("Ăn ở có trước có sau", "Thành ngữ Việt Nam"),
        ("Uống nước nhớ nguồn, ăn quả nhớ kẻ trồng cây", "Thành ngữ Việt Nam"),

        // ── Câu nói nổi tiếng — Nhân vật lịch sử Việt Nam ──
        ("Không có gì quý hơn độc lập, tự do", "Hồ Chí Minh"),
        ("Vì lợi ích mười năm thì phải trồng cây, vì lợi ích trăm năm thì phải trồng người", "Hồ Chí Minh"),
        ("Dân ta phải biết sử ta, cho tường gốc tích nước nhà Việt Nam", "Hồ Chí Minh"),
        ("Đoàn kết, đoàn kết, đại đoàn kết. Thành công, thành công, đại thành công", "Hồ Chí Minh"),
        ("Một năm khởi đầu từ mùa xuân, một đời khởi đầu từ tuổi trẻ", "Hồ Chí Minh"),
        ("Non sông Việt Nam có trở nên tươi đẹp hay không, dân tộc Việt Nam có bước tới đài vinh quang hay không, chính là nhờ một phần lớn ở công học tập của các em", "Hồ Chí Minh"),
        ("Đánh cho để dài tóc, đánh cho để đen răng", "Quang Trung"),
        ("Ta thà làm quỷ nước Nam, còn hơn làm vương đất Bắc", "Trần Bình Trọng"),
        ("Nếu bệ hạ muốn hàng giặc, xin hãy chém đầu thần trước đã", "Trần Quốc Tuấn"),
        ("Khoan thư sức dân để làm kế sâu rễ bền gốc, đó là thượng sách giữ nước", "Trần Hưng Đạo"),
        ("Sông núi nước Nam vua Nam ở, rành rành phân định tại sách trời", "Nam quốc sơn hà"),

        // ── Câu nói nổi tiếng — Nhà văn, nhà thơ Việt Nam ──
        ("Chữ tâm kia mới bằng ba chữ tài", "Nguyễn Du"),
        ("Trăm năm trong cõi người ta, chữ tài chữ mệnh khéo là ghét nhau", "Nguyễn Du"),
        ("Thiện căn ở tại lòng ta, chữ tâm kia mới bằng ba chữ tài", "Nguyễn Du"),
        ("Rừng vàng biển bạc", "Nguyễn Trãi"),
        ("Đem đại nghĩa để thắng hung tàn, lấy chí nhân để thay cường bạo", "Nguyễn Trãi"),
        ("Việc nhân nghĩa cốt ở yên dân", "Nguyễn Trãi"),

        // ── Câu nói nổi tiếng — Thế giới ──
        ("Cuộc sống là những gì xảy ra khi bạn bận rộn lập kế hoạch khác", "John Lennon"),
        ("Hãy là sự thay đổi mà bạn muốn thấy trên thế giới", "Mahatma Gandhi"),
        ("Giáo dục là vũ khí mạnh nhất mà bạn có thể dùng để thay đổi thế giới", "Nelson Mandela"),
        ("Điều duy nhất chúng ta phải sợ là chính nỗi sợ hãi", "Franklin D. Roosevelt"),
        ("Hành trình ngàn dặm bắt đầu từ một bước chân", "Lão Tử"),
        ("Biết người là khôn, biết mình là sáng", "Lão Tử"),
        ("Học mà không nghĩ là phí công, nghĩ mà không học là nguy hiểm", "Khổng Tử"),
        ("Có bạn từ phương xa đến, chẳng vui lắm sao?", "Khổng Tử"),
        ("Sự bắt đầu là phần quan trọng nhất của công việc", "Plato"),
        ("Tôi tư duy, vậy tôi tồn tại", "René Descartes"),
        ("Thành công là đi từ thất bại này sang thất bại khác mà không mất đi nhiệt huyết", "Winston Churchill"),
        ("Sống như thể ngày mai bạn sẽ chết, học như thể bạn sẽ sống mãi mãi", "Mahatma Gandhi"),
        ("Mọi thứ bạn có thể tưởng tượng đều là thật", "Pablo Picasso"),
        ("Cách tốt nhất để dự đoán tương lai là tạo ra nó", "Abraham Lincoln"),
        ("Hạnh phúc không phải là điều đã có sẵn. Nó đến từ hành động của chính bạn", "Đức Đạt-lai Lạt-ma"),
        ("Đừng đếm ngày tháng, hãy làm cho mỗi ngày đều có ý nghĩa", "Muhammad Ali"),
        ("Điều quan trọng nhất là không ngừng đặt câu hỏi", "Albert Einstein"),
        ("Trí tưởng tượng quan trọng hơn kiến thức", "Albert Einstein"),
        ("Người thực sự mạnh mẽ là người chiến thắng được chính mình", "Lão Tử"),
        ("Đừng sợ tiến chậm, chỉ sợ đứng yên", "Ngạn ngữ Trung Hoa"),
        ("Cây muốn lặng mà gió chẳng dừng, con muốn phụng dưỡng mà cha mẹ không còn", "Ngạn ngữ Trung Hoa"),
        ("Cho con cả rương vàng, không bằng dạy con một cuốn sách", "Ngạn ngữ Trung Hoa"),
        ("Biển học là vô bờ, lấy siêng năng làm bến", "Ngạn ngữ Trung Hoa"),
        ("Kẻ chiến thắng không bao giờ bỏ cuộc, kẻ bỏ cuộc không bao giờ chiến thắng", "Vince Lombardi"),
        ("Mỗi ngày là một khởi đầu mới, hãy nắm lấy nó", "Khuyết danh"),
        ("Gia đình là nơi cuộc sống bắt đầu và tình yêu không bao giờ kết thúc", "Khuyết danh"),
        ("Hạnh phúc gia đình là đỉnh cao của mọi hoài bão", "Khuyết danh"),
        ("Gốc rễ gia đình càng sâu, cành lá càng vươn cao", "Khuyết danh"),
        ("Gia đình không phải là điều quan trọng, mà là tất cả", "Michael J. Fox"),
        ("Tình yêu thương của gia đình là phước lành lớn nhất trên đời", "Khuyết danh"),

        // ── Câu nói về thời gian & ngày tháng ──
        ("Thời gian là vàng bạc", "Tục ngữ Việt Nam"),
        ("Mỗi buổi sáng thức dậy, bạn có thêm 24 giờ để sống", "Khuyết danh"),
        ("Ngày hôm nay là món quà, vì vậy nó được gọi là hiện tại", "Khuyết danh"),
        ("Cuộc đời không phải là chờ đợi bão tan, mà là học cách nhảy múa dưới mưa", "Vivian Greene"),
        ("Hôm nay là ngày đầu tiên của phần đời còn lại", "Khuyết danh"),
    ]

    /// Lấy ngẫu nhiên một câu quote.
    static func random() -> (quote: String, author: String) {
        all.randomElement() ?? all[0]
    }

    /// Lấy quote dựa theo ngày trong năm (deterministic — cùng ngày luôn ra cùng câu).
    static func ofDay(_ dayOfYear: Int) -> (quote: String, author: String) {
        let index = ((dayOfYear - 1) % all.count + all.count) % all.count
        return all[index]
    }
}
