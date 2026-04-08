import SwiftUI

struct GoodDaysScreen: View {
    @Environment(\.lichSoColors) private var c
    @State private var selectedMonth = Calendar.current.component(.month, from: Date())
    @State private var selectedYear = Calendar.current.component(.year, from: Date())
    @State private var goodDays: [(day: Int, month: Int, year: Int, info: DayInfo)] = []
    @State private var selectedPurpose = 0
    var onBackClick: () -> Void = {}

    private let purposes = [
        "Tất cả", "Khai trương", "Cưới hỏi", "Xây dựng", "Xuất hành",
        "Nhập trạch", "Ký hợp đồng", "Tang lễ"
    ]

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(c.textPrimary)
                }
                Spacer()
                Text("NGÀY TỐT / XẤU")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(c.textPrimary)
                    .tracking(1)
                Spacer()
                Color.clear.frame(width: 24)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            // Month selector
            HStack {
                Button(action: { prevMonth() }) {
                    Image(systemName: "chevron.left")
                        .foregroundColor(c.textSecondary)
                }
                Spacer()
                Text("Tháng \(selectedMonth) / \(String(selectedYear))")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(c.textPrimary)
                Spacer()
                Button(action: { nextMonth() }) {
                    Image(systemName: "chevron.right")
                        .foregroundColor(c.textSecondary)
                }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 8)

            // Purpose filter
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(0..<purposes.count, id: \.self) { i in
                        Button(action: {
                            selectedPurpose = i
                            findGoodDays()
                        }) {
                            Text(purposes[i])
                                .font(.system(size: 12, weight: selectedPurpose == i ? .bold : .medium))
                                .foregroundColor(selectedPurpose == i ? .white : c.textSecondary)
                                .padding(.horizontal, 14)
                                .padding(.vertical, 7)
                                .background(
                                    RoundedRectangle(cornerRadius: 16)
                                        .fill(selectedPurpose == i ? c.primary : c.surface)
                                )
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
            .padding(.bottom, 12)

            // Results
            ScrollView {
                LazyVStack(spacing: 8) {
                    if goodDays.isEmpty {
                        VStack(spacing: 12) {
                            Image(systemName: "calendar.badge.checkmark")
                                .font(.system(size: 48))
                                .foregroundColor(c.textQuaternary)
                                .padding(.top, 40)
                            Text("Đang tìm ngày tốt...")
                                .font(.system(size: 14))
                                .foregroundColor(c.textSecondary)
                        }
                    }
                    ForEach(goodDays, id: \.day) { item in
                        GoodDayCard(day: item.day, month: item.month, year: item.year, info: item.info, c: c)
                    }
                }
                .padding(.horizontal, 16)
            }
        }
        .background(c.bg)
        .onAppear { findGoodDays() }
    }

    private func prevMonth() {
        selectedMonth -= 1
        if selectedMonth < 1 { selectedMonth = 12; selectedYear -= 1 }
        findGoodDays()
    }

    private func nextMonth() {
        selectedMonth += 1
        if selectedMonth > 12 { selectedMonth = 1; selectedYear += 1 }
        findGoodDays()
    }

    private func findGoodDays() {
        var results: [(day: Int, month: Int, year: Int, info: DayInfo)] = []
        let daysInMonth = Calendar.current.range(of: .day, in: .month,
            for: Calendar.current.date(from: DateComponents(year: selectedYear, month: selectedMonth))!)?.count ?? 30

        for day in 1...daysInMonth {
            let info = DayInfoProvider.shared.getDayInfo(dd: day, mm: selectedMonth, yy: selectedYear)
            // Filter by purpose if not "All"
            let isGood: Bool
            if selectedPurpose == 0 {
                isGood = !info.nenLam.isEmpty
            } else {
                let keyword = purposes[selectedPurpose]
                isGood = info.nenLam.contains { $0.localizedCaseInsensitiveContains(keyword) }
            }
            if isGood {
                results.append((day: day, month: selectedMonth, year: selectedYear, info: info))
            }
        }
        goodDays = results
    }
}

struct GoodDayCard: View {
    let day: Int
    let month: Int
    let year: Int
    let info: DayInfo
    let c: LichSoColors

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text("\(day)/\(month)/\(year)")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(c.textPrimary)
                    Text("Âm lịch: \(info.lunarDate.day)/\(info.lunarDate.month)")
                        .font(.system(size: 12))
                        .foregroundColor(c.textSecondary)
                }
                Spacer()
                Text(info.trucNgay)
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(c.gold)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(c.goldDim)
                    .cornerRadius(8)
            }

            // Nên làm
            if !info.nenLam.isEmpty {
                HStack(spacing: 4) {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 12))
                        .foregroundColor(c.goodGreen)
                    Text(info.nenLam.joined(separator: ", "))
                        .font(.system(size: 12))
                        .foregroundColor(c.textSecondary)
                        .lineLimit(2)
                }
            }

            // Giờ hoàng đạo
            if !info.gioHoangDao.isEmpty {
                HStack(spacing: 4) {
                    Image(systemName: "clock.fill")
                        .font(.system(size: 11))
                        .foregroundColor(c.gold)
                    Text(info.gioHoangDao.map { "\($0.name) (\($0.time))" }.joined(separator: " • "))
                        .font(.system(size: 11))
                        .foregroundColor(c.textTertiary)
                }
            }
        }
        .padding(14)
        .background(
            RoundedRectangle(cornerRadius: 14)
                .fill(c.surface)
        )
    }
}
