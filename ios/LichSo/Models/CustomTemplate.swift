import Foundation
import Combine

// MARK: - Custom Template Model
struct CustomTemplate: Identifiable, Codable {
    var id: UUID = UUID()
    var title: String
    var desc: String
    var icon: String
    var color: String       // gold | teal | red | orange | purple | green
    var items: [String]     // checklist items
    var clonedFrom: String? // original template title
    var createdAt: Date = Date()
    var updatedAt: Date = Date()
}

// MARK: - Template Store (UserDefaults persistence)
class TemplateStore: ObservableObject {
    static let shared = TemplateStore()

    @Published var customTemplates: [CustomTemplate] = [] {
        didSet { save() }
    }

    private let key = "customTemplates_v1"

    init() { load() }

    // ── CRUD ──
    func add(_ t: CustomTemplate) {
        customTemplates.insert(t, at: 0)
    }

    func update(_ t: CustomTemplate) {
        guard let idx = customTemplates.firstIndex(where: { $0.id == t.id }) else { return }
        var updated = t
        updated.updatedAt = Date()
        customTemplates[idx] = updated
    }

    func delete(_ id: UUID) {
        customTemplates.removeAll { $0.id == id }
    }

    func clone(from source: TemplateItem) -> CustomTemplate {
        CustomTemplate(
            title: "\(source.title) (Bản sao)",
            desc: source.desc,
            icon: source.icon,
            color: source.color,
            items: defaultItems(for: source.title),
            clonedFrom: source.title
        )
    }

    // ── Persistence ──
    private func save() {
        if let data = try? JSONEncoder().encode(customTemplates) {
            UserDefaults.standard.set(data, forKey: key)
        }
    }

    private func load() {
        guard let data = UserDefaults.standard.data(forKey: key),
              let decoded = try? JSONDecoder().decode([CustomTemplate].self, from: data) else { return }
        customTemplates = decoded
    }

    // ── Default checklist for cloning ──
    func defaultItems(for title: String) -> [String] {
        switch title {
        case "Checklist Ngày Tốt":
            return ["Kiểm tra ngày âm lịch", "Tra cứu giờ Hoàng Đạo", "Xác nhận hướng xuất hành", "Chuẩn bị lễ vật", "Thông báo người thân"]
        case "Nhật Ký Phong Thuỷ":
            return ["Ghi ngày âm dương", "Đánh giá ngày tốt/xấu", "Ghi chú sự kiện quan trọng", "Xem lại Can Chi trong ngày"]
        case "Kế Hoạch Tuần":
            return ["Xem lịch âm tuần tới", "Chọn ngày tốt cho việc lớn", "Tránh ngày xung tuổi", "Lập danh sách việc cần làm"]
        case "Checklist Cưới Hỏi":
            return ["Chọn ngày cưới tốt", "Đặt nhà hàng tiệc cưới", "May áo dài cô dâu chú rể", "Đặt hoa và trang trí", "Gửi thiệp mời khách", "Chuẩn bị sính lễ đám hỏi", "Chụp ảnh cưới", "Đặt xe hoa"]
        case "Danh Sách Khách Mời":
            return ["Lập danh sách họ hàng", "Liệt kê bạn bè thân thiết", "Đồng nghiệp quan trọng", "Xác nhận số lượng khách", "Gửi thiệp theo địa chỉ"]
        case "Chọn Ngày Cưới":
            return ["Xác định năm tháng phù hợp", "Tra ngày hoàng đạo", "Tránh ngày tam nương, sát chủ", "Xem tuổi cô dâu chú rể", "Tham khảo thầy phong thuỷ"]
        case "Khai Trương Cửa Hàng":
            return ["Chọn ngày giờ khai trương tốt", "Chuẩn bị mâm lễ cúng", "Trang trí cửa hàng", "Mời khách hàng quan trọng", "Chuẩn bị khuyến mãi khai trương", "Kiểm tra bố trí phong thuỷ"]
        case "Ký Kết Hợp Đồng":
            return ["Chọn ngày tốt để ký", "Kiểm tra hướng ngồi ký", "Xem giờ hoàng đạo buổi sáng", "Chuẩn bị tài liệu đầy đủ", "Mặc trang phục màu may mắn"]
        case "Ra Mắt Sản Phẩm":
            return ["Chọn ngày hoàng đạo ra mắt", "Chuẩn bị sự kiện launch", "Lên kế hoạch truyền thông", "Cúng khai trương sản phẩm", "Đặt quảng cáo đúng giờ tốt"]
        case "Ngày Nộp Hồ Sơ":
            return ["Chọn ngày tốt để nộp đơn", "Chuẩn bị hồ sơ đầy đủ", "Mặc trang phục phù hợp", "Đi đúng hướng tốt buổi sáng", "Cầu may trước khi đi"]
        case "Chuẩn Bị Phỏng Vấn":
            return ["Tra giờ hoàng đạo ngày phỏng vấn", "Chọn màu sắc trang phục may mắn", "Xem hướng đi đến nơi phỏng vấn", "Chuẩn bị câu trả lời", "Ngủ sớm trước ngày phỏng vấn"]
        case "Checklist Chuyển Nhà":
            return ["Chọn ngày giờ chuyển nhà", "Dọn dẹp nhà cũ sạch sẽ", "Chuyển đồ vật linh thiêng trước", "Mang bếp lửa vào nhà đầu tiên", "Cúng nhập trạch", "Bật đèn sáng toàn nhà", "Nấu bếp ngay trong ngày"]
        case "Nghi Lễ Nhập Trạch":
            return ["Chọn giờ hoàng đạo nhập trạch", "Chuẩn bị mâm lễ (hương, hoa, quả)", "Bật bếp đun nước sôi", "Bật đèn sáng toàn nhà", "Đọc văn khấn nhập trạch", "Đốt pháo (nếu được phép)"]
        case "Bố Trí Nội Thất":
            return ["Xác định hướng nhà", "Bố trí bàn thờ đúng hướng", "Đặt giường ngủ hợp phong thuỷ", "Tránh đầu giường nhìn thẳng cửa", "Đặt bàn làm việc nhìn ra cửa sổ"]
        case "Checklist Du Lịch":
            return ["Chọn ngày xuất hành tốt", "Xác định hướng đi tốt", "Mang theo đồ vật may mắn", "Cúng bái trước khi xuất hành", "Chuẩn bị hành lý đầy đủ"]
        case "Hướng Xuất Hành":
            return ["Tra hướng Thần Tài hôm nay", "Tra hướng Hỷ Thần", "Tránh hướng Hung Thần", "Xuất hành đúng giờ hoàng đạo", "Ghi lại hướng đã đi"]
        default:
            return ["Mục 1", "Mục 2", "Mục 3"]
        }
    }
}
