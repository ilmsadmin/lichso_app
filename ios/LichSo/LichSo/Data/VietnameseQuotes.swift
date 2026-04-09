import Foundation

enum VietnameseQuotes {
    static let all: [(String, String)] = [
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
        ("Công cha như núi Thái Sơn, nghĩa mẹ như nước trong nguồn chảy ra", "Tục ngữ Việt Nam"),
        ("Máu chảy ruột mềm", "Tục ngữ Việt Nam"),
        ("Anh em như thể tay chân", "Tục ngữ Việt Nam"),
        ("Một giọt máu đào hơn ao nước lã", "Tục ngữ Việt Nam"),
        ("Cây có cội, nước có nguồn", "Tục ngữ Việt Nam"),
        ("Con hơn cha là nhà có phúc", "Tục ngữ Việt Nam"),
        ("Lá lành đùm lá rách", "Tục ngữ Việt Nam"),
        ("Thương người như thể thương thân", "Tục ngữ Việt Nam"),
        ("Một cây làm chẳng nên non, ba cây chụm lại nên hòn núi cao", "Ca dao Việt Nam"),
        ("Con người có tổ có tông, như cây có cội như sông có nguồn", "Ca dao Việt Nam"),
        ("Dù ai đi ngược về xuôi, nhớ ngày giỗ Tổ mùng Mười tháng Ba", "Ca dao Việt Nam"),
        ("Tu đâu cho bằng tu nhà, thờ cha kính mẹ mới là chân tu", "Ca dao Việt Nam"),
        ("Không có gì quý hơn độc lập, tự do", "Hồ Chí Minh"),
        ("Dân ta phải biết sử ta, cho tường gốc tích nước nhà Việt Nam", "Hồ Chí Minh"),
        ("Chữ tâm kia mới bằng ba chữ tài", "Nguyễn Du"),
        ("Đem đại nghĩa để thắng hung tàn, lấy chí nhân để thay cường bạo", "Nguyễn Trãi"),
    ]

    static func ofDay(_ dayOfYear: Int) -> (String, String) {
        let index = dayOfYear % all.count
        return all[index]
    }
}
