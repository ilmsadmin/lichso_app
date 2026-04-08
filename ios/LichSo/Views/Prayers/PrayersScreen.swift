import SwiftUI

// MARK: - Prayer data
struct Prayer: Identifiable {
    let id: Int
    let title: String
    let category: String
    let icon: String
    let content: String
}

private let samplePrayers: [Prayer] = [
    Prayer(id: 1, title: "Văn khấn thổ công", category: "Gia tiên", icon: "🏠",
           content: """
Nam mô A Di Đà Phật! (3 lần)
Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Đông Trù Tư Mệnh Táo Phủ Thần Quân.
Con kính lạy các ngài Thần linh, Thổ địa cai quản trong xứ này.
Tín chủ con là: ...
Ngụ tại: ...
Hôm nay là ngày ... tháng ... năm ...
Tín chủ con thành tâm sắm lễ, hương hoa trà quả, thắp nén hương thơm dâng trước án, kính mời các vị Thần linh, Thổ địa, Thổ Công chứng giám.
Cúi xin các Ngài phù hộ độ trì cho gia đình con được mạnh khỏe, bình an, vạn sự như ý.
Nam mô A Di Đà Phật! (3 lần)
"""),
    Prayer(id: 2, title: "Văn khấn gia tiên", category: "Gia tiên", icon: "🙏",
           content: """
Nam mô A Di Đà Phật! (3 lần)
Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
Con kính lạy Đức Đương cảnh Thành Hoàng Chư vị Đại Vương.
Con kính lạy ngài Đông Trù Tư Mệnh Táo Phủ Thần Quân.
Con kính lạy các ngài Thổ Địa, Long Mạch, Tôn Thần.
Con kính lạy Tổ tiên nội ngoại họ ...
Tín chủ con là: ...
Hôm nay là ngày ... tháng ... năm ...
Nhân ngày ... tín chủ con thành tâm sắm lễ, hương hoa trà quả, dâng lên trước án.
Kính mời các cụ Tổ tiên nội ngoại, chư vị Hương linh gia tiên họ ... về thụ hưởng lễ vật.
Cúi xin các cụ phù hộ độ trì cho con cháu được mạnh khỏe, bình an, công việc hanh thông, vạn sự như ý.
Nam mô A Di Đà Phật! (3 lần)
"""),
    Prayer(id: 3, title: "Văn khấn mùng 1, ngày rằm", category: "Hàng tháng", icon: "🌕",
           content: """
Nam mô A Di Đà Phật! (3 lần)
Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Hôm nay là ngày mùng Một (hoặc Rằm) tháng ... năm ...
Tín chủ con là: ...
Ngụ tại: ...
Thành tâm sắm lễ, hương hoa trà quả, thắp nén hương thơm dâng trước án.
Kính mời chư vị Thần linh, Thổ Công, Gia tiên về chứng giám lòng thành.
Cúi xin phù hộ cho gia đình con an khang, thịnh vượng, mọi việc thuận lợi.
Nam mô A Di Đà Phật! (3 lần)
"""),
    Prayer(id: 4, title: "Văn khấn cúng giao thừa", category: "Tết", icon: "🎆",
           content: """
Nam mô A Di Đà Phật! (3 lần)
Kính lạy Đức Đương Niên Hành Khiển Thái Tuế Chí Đức Tôn Thần.
Kính lạy các ngài Bản Cảnh Thành Hoàng Chư vị Đại Vương.
Kính lạy ngài Bản Xứ Thần Linh Thổ Địa.
Kính lạy các ngài Ngũ Phương, Ngũ Thổ, Long Mạch, Tài Thần.
Tín chủ con là: ...
Ngụ tại: ...
Nay gặp tiết giao thừa năm ... sang năm ...
Tín chủ con thành tâm sắm lễ, hương hoa trà quả, dâng trước linh vị, cung thỉnh các Ngài giáng lâm chứng giám.
Kính mong các Ngài phù hộ độ trì cho gia đình con năm mới an khang, thịnh vượng, vạn sự như ý, gặp nhiều may mắn.
Nam mô A Di Đà Phật! (3 lần)
"""),
    Prayer(id: 5, title: "Văn khấn cúng Rằm tháng Giêng", category: "Tết", icon: "🏮",
           content: """
Nam mô A Di Đà Phật! (3 lần)
Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
Con kính lạy Đức Phật Thiên Quan Tứ Phúc.
Hôm nay là ngày Rằm tháng Giêng năm ...
Tín chủ con là: ...
Ngụ tại: ...
Thành tâm sắm lễ, hương hoa trà quả dâng lên trước Phật.
Kính cầu Chư Phật, Chư Bồ Tát, Chư Hiền Thánh từ bi gia hộ.
Nguyện cho gia đình con được phúc lộc song toàn, gia đạo hưng long, bình an mạnh khỏe.
Nam mô A Di Đà Phật! (3 lần)
"""),
    Prayer(id: 6, title: "Văn khấn giỗ đầu", category: "Gia tiên", icon: "🕯️",
           content: """
Nam mô A Di Đà Phật! (3 lần)
Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
Con kính lạy ngài Bản Cảnh Thành Hoàng chư vị Đại Vương.
Con kính lạy ngài Bản Xứ Thần Linh Thổ Địa.
Con kính lạy Tổ tiên nội ngoại họ ...
Hôm nay là ngày giỗ đầu của ...
Tín chủ con thành tâm sắm lễ, hương hoa trà quả, thắp nén hương thơm dâng trước án.
Kính mời hương linh ... về hưởng lễ vật. Cúi xin phù hộ con cháu.
Nam mô A Di Đà Phật! (3 lần)
"""),
    Prayer(id: 7, title: "Văn khấn nhập trạch", category: "Hàng tháng", icon: "🏡",
           content: """
Nam mô A Di Đà Phật! (3 lần)
Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Hôm nay là ngày ... tháng ... năm ... nhằm ngày lành tháng tốt.
Tín chủ con là: ...
Nguyên quán tại: ...
Nay chuyển đến cư ngụ tại địa chỉ mới: ...
Thành tâm sắm lễ, kính mời chư vị Thần linh chứng giám.
Cúi xin được phù hộ cho gia đình bình an, vạn sự tốt lành.
Nam mô A Di Đà Phật! (3 lần)
"""),
    Prayer(id: 8, title: "Văn khấn khai trương", category: "Hàng tháng", icon: "🎊",
           content: """
Nam mô A Di Đà Phật! (3 lần)
Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Thần Tài vị tiền.
Hôm nay ngày ... tháng ... năm ... là ngày khai trương.
Tín chủ con là: ...
Thành tâm sắm lễ, hương hoa trà quả, thắp nén hương thơm.
Kính mời chư vị Thần Tài, Thổ Địa giáng lâm chứng giám.
Cúi xin phù hộ cho công việc làm ăn phát đạt, tài lộc dồi dào.
Nam mô A Di Đà Phật! (3 lần)
"""),
]

// MARK: - PrayersScreen
struct PrayersScreen: View {
    @Environment(\.lichSoColors) private var c
    @State private var selectedPrayer: Prayer? = nil
    @State private var selectedCategory = "Tất cả"
    var onBackClick: () -> Void = {}
    var onMenuClick: () -> Void = {}

    private let categories = ["Tất cả", "Gia tiên", "Hàng tháng", "Tết"]

    private var filteredPrayers: [Prayer] {
        if selectedCategory == "Tất cả" { return samplePrayers }
        return samplePrayers.filter { $0.category == selectedCategory }
    }

    var body: some View {
        VStack(spacing: 0) {
            // ── Top Bar ──
            topBar
            // ── Category Chips ──
            categoryChips
            // ── Prayer List ──
            prayerList
        }
        .background(c.bg)
        .sheet(item: $selectedPrayer) { prayer in
            PrayerDetailView(prayer: prayer, c: c)
        }
    }

    // MARK: - Top Bar
    private var topBar: some View {
        HStack {
            Button(action: onBackClick) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(c.textPrimary)
            }
            Spacer()
            Text("VĂN KHẤN")
                .font(.system(size: 15, weight: .bold))
                .foregroundColor(c.textPrimary)
                .tracking(1)
            Spacer()
            Button(action: onMenuClick) {
                Image(systemName: "line.3.horizontal")
                    .font(.system(size: 18))
                    .foregroundColor(c.textPrimary)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }

    // MARK: - Category Chips
    private var categoryChips: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(categories, id: \.self) { cat in
                    Button(action: { withAnimation(.easeInOut(duration: 0.2)) { selectedCategory = cat } }) {
                        Text(cat)
                            .font(.system(size: 13, weight: selectedCategory == cat ? .bold : .medium))
                            .foregroundColor(selectedCategory == cat ? .white : c.textSecondary)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(
                                Capsule()
                                    .fill(selectedCategory == cat ? c.primary : c.surface)
                            )
                    }
                }
            }
            .padding(.horizontal, 16)
        }
        .padding(.bottom, 16)
    }

    // MARK: - Prayer List
    private var prayerList: some View {
        ScrollView {
            LazyVStack(spacing: 10) {
                ForEach(filteredPrayers) { prayer in
                    prayerCard(prayer)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 16)
        }
    }

    // MARK: - Prayer Card
    @ViewBuilder
    private func prayerCard(_ prayer: Prayer) -> some View {
        Button(action: { selectedPrayer = prayer }) {
            HStack(spacing: 14) {
                // Icon
                Text(prayer.icon)
                    .font(.system(size: 28))
                    .frame(width: 48, height: 48)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(c.goldDim)
                    )

                // Text
                VStack(alignment: .leading, spacing: 3) {
                    Text(prayer.title)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(c.textPrimary)
                        .lineLimit(1)
                    Text(prayer.category)
                        .font(.system(size: 12))
                        .foregroundColor(c.textTertiary)
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(c.textQuaternary)
            }
            .padding(14)
            .background(
                RoundedRectangle(cornerRadius: 14)
                    .fill(c.surface)
            )
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Prayer Detail View
struct PrayerDetailView: View {
    let prayer: Prayer
    let c: LichSoColors
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    // Header
                    HStack(spacing: 12) {
                        Text(prayer.icon)
                            .font(.system(size: 36))
                        VStack(alignment: .leading, spacing: 4) {
                            Text(prayer.title)
                                .font(.system(size: 20, weight: .bold))
                                .foregroundColor(c.textPrimary)
                            Text(prayer.category)
                                .font(.system(size: 13, weight: .medium))
                                .foregroundColor(c.gold)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 4)
                                .background(c.goldDim)
                                .cornerRadius(8)
                        }
                    }

                    Divider()

                    // Content
                    Text(prayer.content)
                        .font(.system(size: 16))
                        .foregroundColor(c.textPrimary)
                        .lineSpacing(8)
                }
                .padding(20)
            }
            .background(c.bg)
            .navigationTitle("Chi tiết văn khấn")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Đóng") { dismiss() }
                }
            }
        }
    }
}
