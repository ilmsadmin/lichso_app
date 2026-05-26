import SwiftUI

enum Season: String, CaseIterable, Identifiable {
    case spring = "Xuân"
    case summer = "Hạ"
    case autumn = "Thu"
    case winter = "Đông"
    
    var id: String { rawValue }
    
    var displayName: String { rawValue }
    
    var emoji: String {
        switch self {
        case .spring: return "🌸"
        case .summer: return "☀️"
        case .autumn: return "🍂"
        case .winter: return "❄️"
        }
    }
    
    var gradientColors: [Color] {
        switch self {
        case .spring: return [Color(hex: "43A047"), Color(hex: "FFB300")]
        case .summer: return [Color(hex: "D84315"), Color(hex: "FF6F00")]
        case .autumn: return [Color(hex: "E65100"), Color(hex: "8D6E63")]
        case .winter: return [Color(hex: "1565C0"), Color(hex: "1A237E")]
        }
    }
}

struct TietKhiDetail: Identifiable {
    let id = UUID()
    let name: String
    let pinyin: String
    let month: Int
    let approxDay: Int
    let season: Season
    let meaning: String
    let recommended: [String]
    let avoid: [String]
    let seasonalFood: String
}

struct TietKhiScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var selectedTerm: TietKhiDetail? = nil
    
    let terms = TietKhiCatalog.all
    
    // Calculate current term today
    private var termInfo: (currentTermName: String?, daysUntilNext: Int, nextTermName: String?, nextTermDate: String?, exactDates: [String: String]) {
        let calendar = Calendar.current
        let today = Date()
        let dd = calendar.component(.day, from: today)
        let mm = calendar.component(.month, from: today)
        let yy = calendar.component(.year, from: today)
        
        let info = TietKhiCalculator.getCurrentSolarTerm(dd: dd, mm: mm, yy: yy)
        let allTermsThisYear = TietKhiCalculator.getAllSolarTerms(year: yy)
        
        var exactDates: [String: String] = [:]
        for term in allTermsThisYear {
            exactDates[term.name] = String(format: "%02d/%02d", term.dd, term.mm)
        }
        
        let nextDateStr = info.next.map { String(format: "%02d/%02d", $0.dd, $0.mm) }
        return (info.current?.name, info.daysUntilNext, info.next?.name, nextDateStr, exactDates)
    }
    
    var body: some View {
        ZStack {
            LSTheme.bg.ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Top bar
                HStack {
                    Button { dismiss() } label: {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundColor(LSTheme.textPrimary)
                            .frame(width: 44, height: 44)
                    }
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text("24 Tiết Khí")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Vòng năm Á Đông")
                            .font(.system(size: 12))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    
                    Spacer()
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 8)
                .background(LSTheme.surfaceContainer)
                
                ScrollView {
                    VStack(spacing: 20) {
                        // Hero card
                        heroCard
                        
                        // 4 Seasons
                        ForEach(Season.allCases) { season in
                            seasonSection(season)
                        }
                    }
                    .padding(16)
                }
            }
        }
        .navigationBarHidden(true)
        .sheet(item: $selectedTerm) { term in
            TietKhiDetailSheet(detail: term)
        }
    }
    
    private var heroCard: some View {
        let info = termInfo
        let currentTerm = info.currentTermName.flatMap { TietKhiCatalog.byName($0) }
        let colors = currentTerm?.season.gradientColors ?? [Color(hex: "FFD54F"), Color(hex: "F57F17")]
        
        return Button {
            if let current = currentTerm {
                selectedTerm = current
            }
        } label: {
            VStack(alignment: .leading, spacing: 10) {
                Text("TIẾT KHÍ HÔM NAY")
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(.white.opacity(0.8))
                
                Text(info.currentTermName ?? "—")
                    .font(.system(size: 32, weight: .bold))
                    .foregroundColor(.white)
                
                if let current = currentTerm {
                    Text("\(current.season.emoji) Mùa \(current.season.displayName) · \(current.pinyin)")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(.white.opacity(0.9))
                    
                    Text(current.meaning)
                        .font(.system(size: 13))
                        .foregroundColor(.white.opacity(0.95))
                        .multilineTextAlignment(.leading)
                        .lineLimit(3)
                        .fixedSize(horizontal: false, vertical: true)
                }
                
                if let nextName = info.nextTermName {
                    Spacer().frame(height: 4)
                    
                    let dateLabel = info.nextTermDate.map { " (\($0))" } ?? ""
                    Text("Còn \(info.daysUntilNext) ngày → \(nextName)\(dateLabel)")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(.white)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(Color.white.opacity(0.2))
                        .cornerRadius(10)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(20)
            .background(LinearGradient(colors: colors, startPoint: .topLeading, endPoint: .bottomTrailing))
            .cornerRadius(20)
        }
        .buttonStyle(.plain)
    }
    
    private func seasonSection(_ season: Season) -> some View {
        let seasonItems = TietKhiCatalog.bySeason(season)
        let info = termInfo
        
        return VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 8) {
                Text(season.emoji)
                    .font(.system(size: 20))
                Text("Mùa \(season.displayName)")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(LSTheme.textPrimary)
            }
            
            VStack(spacing: 0) {
                ForEach(Array(seasonItems.enumerated()), id: \.element.id) { idx, item in
                    let isCurrent = item.name == info.currentTermName
                    let exactDate = info.exactDates[item.name]
                    
                    Button {
                        selectedTerm = item
                    } label: {
                        TietKhiRow(item: item, isCurrent: isCurrent, exactDate: exactDate)
                    }
                    .buttonStyle(.plain)
                    
                    if idx < seasonItems.count - 1 {
                        Divider()
                            .background(LSTheme.outlineVariant)
                    }
                }
            }
            .background(LSTheme.surfaceContainer)
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(LSTheme.outlineVariant.opacity(0.4), lineWidth: 1)
            )
        }
    }
}

struct TietKhiRow: View {
    let item: TietKhiDetail
    let isCurrent: Bool
    let exactDate: String?
    
    var body: some View {
        let accent = item.season.gradientColors.first ?? .green
        
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(accent.opacity(0.15))
                    .frame(width: 38, height: 38)
                    .overlay(
                        Circle()
                            .stroke(isCurrent ? accent : Color.clear, lineWidth: 2)
                    )
                
                let dayStr = exactDate?.components(separatedBy: "/").first.map { String(Int($0) ?? item.approxDay) } ?? "\(item.approxDay)"
                Text(dayStr)
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(accent)
            }
            
            VStack(alignment: .leading, spacing: 3) {
                HStack(spacing: 6) {
                    Text(item.name)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(LSTheme.textPrimary)
                    
                    if isCurrent {
                        Text("Hôm nay")
                            .font(.system(size: 9, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(accent)
                            .cornerRadius(6)
                    }
                }
                
                let dateDisplay = exactDate != nil ? "\(exactDate!) dương · \(item.pinyin)" : "≈ \(item.approxDay)/\(item.month) dương · \(item.pinyin)"
                Text(dateDisplay)
                    .font(.system(size: 11))
                    .foregroundColor(LSTheme.textTertiary)
            }
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .font(.system(size: 14))
                .foregroundColor(LSTheme.textTertiary)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .background(isCurrent ? accent.opacity(0.08) : Color.clear)
    }
}

struct TietKhiDetailSheet: View {
    @Environment(\.dismiss) private var dismiss
    let detail: TietKhiDetail
    
    var body: some View {
        let accent = detail.season.gradientColors.first ?? .green
        
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Header
                HStack(alignment: .center) {
                    ZStack {
                        Circle()
                            .fill(accent.opacity(0.15))
                            .frame(width: 48, height: 48)
                            .overlay(Circle().stroke(accent, lineWidth: 2))
                        
                        Text(detail.season.emoji)
                            .font(.system(size: 22))
                    }
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text(detail.name)
                            .font(.system(size: 22, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("\(detail.pinyin) · ≈ \(detail.approxDay)/\(detail.month) dương")
                            .font(.system(size: 12))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    
                    Spacer()
                    
                    Button { dismiss() } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 24))
                            .foregroundColor(LSTheme.textSecondary)
                    }
                }
                
                // Meaning card
                VStack(alignment: .leading, spacing: 8) {
                    Text(detail.meaning)
                        .font(.system(size: 14))
                        .foregroundColor(LSTheme.textPrimary)
                        .lineSpacing(4)
                }
                .padding(14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(accent.opacity(0.08))
                .cornerRadius(14)
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(accent.opacity(0.3), lineWidth: 1)
                )
                
                // Recommended
                bulletSection(title: "Nên làm", items: detail.recommended, iconName: "checkmark.circle.fill", color: LSTheme.goodGreen)
                
                // Avoid
                bulletSection(title: "Kiêng kỵ", items: detail.avoid, iconName: "xmark.circle.fill", color: LSTheme.badRed)
                
                // Food
                HStack(alignment: .top, spacing: 10) {
                    Image(systemName: "fork.knife.circle.fill")
                        .font(.system(size: 22))
                        .foregroundColor(LSTheme.gold)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Thực phẩm theo mùa")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text(detail.seasonalFood)
                            .font(.system(size: 13))
                            .foregroundColor(LSTheme.textSecondary)
                            .lineSpacing(4)
                    }
                }
                .padding(14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(LSTheme.gold.opacity(0.08))
                .cornerRadius(14)
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(LSTheme.gold.opacity(0.3), lineWidth: 1)
                )
            }
            .padding(20)
        }
        .background(LSTheme.bg)
    }
    
    private func bulletSection(title: String, items: [String], iconName: String, color: Color) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 6) {
                Image(systemName: iconName)
                    .font(.system(size: 18))
                    .foregroundColor(color)
                Text(title)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(LSTheme.textPrimary)
            }
            
            ForEach(items, id: \.self) { item in
                HStack(alignment: .top, spacing: 6) {
                    Text("•")
                        .font(.system(size: 14))
                        .foregroundColor(color)
                    Text(item)
                        .font(.system(size: 13))
                        .foregroundColor(LSTheme.textSecondary)
                        .lineSpacing(4)
                }
                .padding(.leading, 24)
            }
        }
    }
}

// 24 Solar terms static database
struct TietKhiCatalog {
    static let all: [TietKhiDetail] = [
        TietKhiDetail(
            name: "Lập Xuân", pinyin: "Lìchūn", month: 2, approxDay: 4, season: .spring,
            meaning: "Bắt đầu mùa xuân — vạn vật bừng tỉnh sau giấc ngủ đông. Khí dương bắt đầu sinh.",
            recommended: ["Khai trương — đầu tư việc mới", "Trồng cây, gieo hạt", "Tập thể dục buổi sáng", "Mặc đồ ấm phần thân dưới"],
            avoid: ["Ăn quá nhiều đồ chua", "Để cơ thể nhiễm lạnh đột ngột", "Cắt tóc quá ngắn"],
            seasonalFood: "Hành lá, giá đỗ, măng, cải xanh"
        ),
        TietKhiDetail(
            name: "Vũ Thủy", pinyin: "Yǔshuǐ", month: 2, approxDay: 19, season: .spring,
            meaning: "Mưa xuân bắt đầu rơi — tuyết tan, nước về. Đất đai ẩm, thuận lợi gieo trồng.",
            recommended: ["Bón phân ruộng vườn", "Bổ tỳ vị bằng cháo gạo", "Mang ô khi ra ngoài"],
            avoid: ["Ăn lạnh, nước đá", "Để chân ướt lâu"],
            seasonalFood: "Cháo gạo lứt, hạt sen, đậu đỏ"
        ),
        TietKhiDetail(
            name: "Kinh Trập", pinyin: "Jīngzhé", month: 3, approxDay: 6, season: .spring,
            meaning: "Sấm đầu mùa — sâu trùng ngủ đông thức giấc. Vạn vật chuyển động.",
            recommended: ["Dọn dẹp nhà cửa, xua tà khí", "Đi bộ ngoài thiên nhiên", "Bắt sâu, làm cỏ vườn"],
            avoid: ["Ngủ nướng quá 7 giờ", "Cãi vã, nóng giận"],
            seasonalFood: "Tỏi, gừng, lê, mật ong"
        ),
        TietKhiDetail(
            name: "Xuân Phân", pinyin: "Chūnfēn", month: 3, approxDay: 21, season: .spring,
            meaning: "Ngày dài bằng đêm — âm dương cân bằng. Là điểm khởi đầu của xuân giữa.",
            recommended: ["Cúng tổ tiên", "Cân bằng làm việc & nghỉ ngơi", "Trồng cây cảnh trong nhà"],
            avoid: ["Thức quá khuya", "Ăn uống thất thường"],
            seasonalFood: "Rau cải, dâu tây, cà chua"
        ),
        TietKhiDetail(
            name: "Thanh Minh", pinyin: "Qīngmíng", month: 4, approxDay: 5, season: .spring,
            meaning: "Trời quang đãng, sáng sủa. Tiết tảo mộ — tưởng nhớ tổ tiên.",
            recommended: ["Tảo mộ, sửa sang phần mộ", "Du xuân, dã ngoại", "Trồng hoa cúc, hoa mai"],
            avoid: ["Cãi nhau ở mộ phần", "Ăn đồ quá béo"],
            seasonalFood: "Bánh trôi, bánh chay, rau má"
        ),
        TietKhiDetail(
            name: "Cốc Vũ", pinyin: "Gǔyǔ", month: 4, approxDay: 20, season: .spring,
            meaning: "Mưa hạt thóc — mưa nhiều thuận lợi cho hạt giống nảy mầm.",
            recommended: ["Cấy lúa mùa", "Uống trà xanh đầu vụ", "Vệ sinh ẩm mốc"],
            avoid: ["Ăn quá nhiều thịt nóng", "Ở phòng kín, ẩm thấp"],
            seasonalFood: "Trà mạn, măng tây, cá chép"
        ),
        TietKhiDetail(
            name: "Lập Hạ", pinyin: "Lìxià", month: 5, approxDay: 5, season: .summer,
            meaning: "Bắt đầu mùa hè — nhiệt độ tăng nhanh. Khí dương cực thịnh, dưỡng tâm là chính.",
            recommended: ["Dậy sớm, ngủ trễ một chút", "Uống nhiều nước", "Ăn nhạt, ít dầu mỡ"],
            avoid: ["Tập thể dục lúc nắng gắt", "Ăn quá lạnh đột ngột", "Tức giận"],
            seasonalFood: "Đậu xanh, dưa hấu, bí đao"
        ),
        TietKhiDetail(
            name: "Tiểu Mãn", pinyin: "Xiǎomǎn", month: 5, approxDay: 21, season: .summer,
            meaning: "Hạt thóc đầy mẩy nhưng chưa chín hẳn. Trời mưa nhiều, ẩm độ cao.",
            recommended: ["Chống ẩm, phơi quần áo", "Ăn món thanh nhiệt", "Tránh đứng gió lùa"],
            avoid: ["Ăn đồ chiên rán", "Ngồi lâu trong phòng lạnh"],
            seasonalFood: "Mướp đắng, rau dền, cá rô"
        ),
        TietKhiDetail(
            name: "Mang Chủng", pinyin: "Mángzhòng", month: 6, approxDay: 6, season: .summer,
            meaning: "Mùa gặt lúa chiêm — cấy lúa mùa. Bận rộn nhất trong năm với nhà nông.",
            recommended: ["Tăng đạm thực vật", "Tắm nước ấm buổi tối", "Ngủ trưa 30 phút"],
            avoid: ["Lao động quá sức giữa trưa", "Uống bia rượu nhiều"],
            seasonalFood: "Mơ ngâm, vải thiều, khổ qua"
        ),
        TietKhiDetail(
            name: "Hạ Chí", pinyin: "Xiàzhì", month: 6, approxDay: 21, season: .summer,
            meaning: "Ngày dài nhất trong năm — dương cực thịnh. Là điểm chuyển âm dương.",
            recommended: ["Cúng tổ tiên (Đoan Ngọ)", "Hái thuốc nam", "Ăn món thanh nhiệt"],
            avoid: ["Phơi nắng giữa trưa", "Ăn quá no"],
            seasonalFood: "Cơm rượu nếp, mận, vải, chè đỗ đen"
        ),
        TietKhiDetail(
            name: "Tiểu Thử", pinyin: "Xiǎoshǔ", month: 7, approxDay: 7, season: .summer,
            meaning: "Bắt đầu nóng nhẹ — chưa cực điểm. Cần dưỡng tâm, tránh nóng giận.",
            recommended: ["Ngủ trưa", "Uống trà sen, trà cúc", "Đi bơi"],
            avoid: ["Ăn cay nhiều", "Để bàn chân lạnh"],
            seasonalFood: "Sen, mướp, dưa leo"
        ),
        TietKhiDetail(
            name: "Đại Thử", pinyin: "Dàshǔ", month: 7, approxDay: 23, season: .summer,
            meaning: "Nóng cực đỉnh — \"tam phục\" khắc nghiệt nhất. Đề phòng say nắng.",
            recommended: ["Ở nơi mát mẻ buổi trưa", "Tăng cường rau xanh", "Tắm nước mát (không đá)"],
            avoid: ["Ra ngoài 11–14h", "Tập thể thao quá mức", "Ăn đá lạnh khi mới đi nắng về"],
            seasonalFood: "Đậu xanh, sương sáo, rau câu, dừa"
        ),
        TietKhiDetail(
            name: "Lập Thu", pinyin: "Lìqiū", month: 8, approxDay: 7, season: .autumn,
            meaning: "Bắt đầu mùa thu — khí dương suy, khí âm sinh. Thời tiết bắt đầu hanh khô.",
            recommended: ["Bổ phổi bằng món hấp", "Đi bộ chiều mát", "Uống nước ép lê"],
            avoid: ["Ăn quá nhiều đồ cay", "Để da khô nứt nẻ"],
            seasonalFood: "Lê, củ sen, bí đỏ, hạnh nhân"
        ),
        TietKhiDetail(
            name: "Xử Thử", pinyin: "Chǔshǔ", month: 8, approxDay: 23, season: .autumn,
            meaning: "Nóng đã rút lui — sáng mát, trưa còn nóng. Chuyển giao rõ rệt.",
            recommended: ["Tăng giờ ngủ buổi tối", "Mặc áo khoác mỏng sáng/tối", "Bổ âm bằng cháo loãng"],
            avoid: ["Bật quạt thẳng vào người khi ngủ", "Ăn dưa hấu quá nhiều"],
            seasonalFood: "Cháo bí đỏ, mật ong, nấm"
        ),
        TietKhiDetail(
            name: "Bạch Lộ", pinyin: "Báilù", month: 9, approxDay: 8, season: .autumn,
            meaning: "Sương trắng đầu mùa — khí trời lạnh hơn vào ban đêm.",
            recommended: ["Đắp chăn mỏng khi ngủ", "Uống trà hoa cúc", "Dưỡng phổi bằng món hấp"],
            avoid: ["Đi chân đất buổi sáng sớm", "Ăn món sống lạnh"],
            seasonalFood: "Long nhãn, hạt sen, gà ác hầm"
        ),
        TietKhiDetail(
            name: "Thu Phân", pinyin: "Qiūfēn", month: 9, approxDay: 23, season: .autumn,
            meaning: "Ngày bằng đêm lần thứ 2 — âm dương cân bằng. Tiết Trung thu cận kề.",
            recommended: ["Sum họp gia đình (Trung thu)", "Dưỡng âm bổ phổi", "Đi bộ chậm dưới trăng"],
            avoid: ["Buồn rầu, suy nghĩ quá nhiều", "Thức quá khuya"],
            seasonalFood: "Bánh trung thu, hồng đỏ, lựu, nho"
        ),
        TietKhiDetail(
            name: "Hàn Lộ", pinyin: "Hánlù", month: 10, approxDay: 8, season: .autumn,
            meaning: "Sương lạnh — khí trời rõ rệt mát lạnh. Cây cối bắt đầu chuyển vàng.",
            recommended: ["Mang tất khi ngủ", "Uống nước ấm sáng sớm", "Ngâm chân nước gừng"],
            avoid: ["Để vai gáy bị lạnh", "Ăn cua sống"],
            seasonalFood: "Khoai lang, cua, hạt dẻ, táo đỏ"
        ),
        TietKhiDetail(
            name: "Sương Giáng", pinyin: "Shuāngjiàng", month: 10, approxDay: 23, season: .autumn,
            meaning: "Sương sa thành sương muối — báo hiệu mùa đông cận kề.",
            recommended: ["Bổ thận bằng món hầm", "Mặc áo che ấm bụng", "Tập khí công"],
            avoid: ["Tập thể dục sớm khi sương dày", "Ăn quá nhiều đồ chua"],
            seasonalFood: "Hồng giòn, gừng, củ cải trắng"
        ),
        TietKhiDetail(
            name: "Lập Đông", pinyin: "Lìdōng", month: 11, approxDay: 7, season: .winter,
            meaning: "Bắt đầu mùa đông — vạn vật ẩn náu, dưỡng tinh khí. Khí âm cực thịnh sinh.",
            recommended: ["Bổ thận tinh", "Ăn món hầm ấm", "Đi ngủ sớm 22h", "Tắm nước ấm"],
            avoid: ["Tập thể dục quá sớm khi trời tối", "Ra mồ hôi rồi gặp gió"],
            seasonalFood: "Thịt cừu, gừng, quế, đông trùng hạ thảo"
        ),
        TietKhiDetail(
            name: "Tiểu Tuyết", pinyin: "Xiǎoxuě", month: 11, approxDay: 22, season: .winter,
            meaning: "Tuyết nhỏ rơi (ở miền Bắc TQ) — VN: rét đậm bắt đầu. Khí trời u ám.",
            recommended: ["Tăng cường nội tiết", "Uống trà gừng mật ong", "Mặc đồ giữ ấm cổ"],
            avoid: ["Ăn quá nhiều đồ ngọt", "Ngồi lì trong phòng kín"],
            seasonalFood: "Gừng, hành, tỏi, mật ong, hạt óc chó"
        ),
        TietKhiDetail(
            name: "Đại Tuyết", pinyin: "Dàxuě", month: 12, approxDay: 7, season: .winter,
            meaning: "Tuyết lớn — rét đậm rét hại. Cần dưỡng dương khí trong người.",
            recommended: ["Phơi nắng buổi trưa", "Ăn cháo nóng", "Sưởi ấm tay chân"],
            avoid: ["Tắm nước lạnh", "Để đầu ướt khi ra ngoài"],
            seasonalFood: "Cháo bát bảo, hạt dẻ, khoai môn"
        ),
        TietKhiDetail(
            name: "Đông Chí", pinyin: "Dōngzhì", month: 12, approxDay: 22, season: .winter,
            meaning: "Đêm dài nhất trong năm — âm cực thịnh, dương khí bắt đầu sinh trở lại.",
            recommended: ["Cúng gia tiên cuối năm", "Sum họp gia đình", "Bổ tinh dưỡng huyết", "Ăn bánh trôi/bánh chay"],
            avoid: ["Ra ngoài lúc nửa đêm", "Suy nghĩ tiêu cực"],
            seasonalFood: "Bánh trôi, gà ác hầm thuốc bắc, đậu đỏ"
        ),
        TietKhiDetail(
            name: "Tiểu Hàn", pinyin: "Xiǎohán", month: 1, approxDay: 6, season: .winter,
            meaning: "Lạnh nhỏ — báo hiệu giai đoạn lạnh nhất sắp đến.",
            recommended: ["Ngâm chân nước thuốc", "Uống canh xương", "Mặc thêm áo khoác lông"],
            avoid: ["Ra ngoài khi gió mạnh", "Ăn rau sống"],
            seasonalFood: "Xương hầm, cải bó xôi, hành tây"
        ),
        TietKhiDetail(
            name: "Đại Hàn", pinyin: "Dàhán", month: 1, approxDay: 20, season: .winter,
            meaning: "Lạnh cực điểm — chuẩn bị đón Tết. Là tiết khí cuối cùng trong vòng 24 tiết.",
            recommended: ["Dọn dẹp nhà chuẩn bị Tết", "Mua sắm Tất niên", "Tổng kết 1 năm", "Bổ thận bằng món hầm"],
            avoid: ["Tranh cãi cuối năm", "Cho vay tiền cận Tết (theo dân gian)"],
            seasonalFood: "Bánh chưng (cận Tết), giò chả, dưa hành"
        )
    ]
    
    static func byName(_ name: String) -> TietKhiDetail? {
        return all.first { $0.name == name }
    }
    
    static func bySeason(_ season: Season) -> [TietKhiDetail] {
        return all.filter { $0.season == season }
    }
}
