import SwiftUI

struct SearchScreen: View {
    @Environment(\.lichSoColors) private var c
    @State private var searchText = ""
    @State private var searchResults: [DayInfo] = []
    var onBackClick: () -> Void = {}
    var onDateSelected: (Int, Int, Int) -> Void = { _, _, _ in }

    var body: some View {
        VStack(spacing: 0) {
            // Top bar
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(c.textPrimary)
                }
                // Search field
                HStack(spacing: 8) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 16))
                        .foregroundColor(c.textTertiary)
                    TextField("Tìm ngày (dd/MM/yyyy)...", text: $searchText)
                        .font(.system(size: 15))
                        .foregroundColor(c.textPrimary)
                        .onSubmit { performSearch() }
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 10)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(c.surface)
                )
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            ScrollView {
                LazyVStack(spacing: 8) {
                    if searchResults.isEmpty && !searchText.isEmpty {
                        VStack(spacing: 12) {
                            Image(systemName: "magnifyingglass")
                                .font(.system(size: 48))
                                .foregroundColor(c.textQuaternary)
                                .padding(.top, 60)
                            Text("Nhập ngày theo định dạng dd/MM/yyyy")
                                .font(.system(size: 14))
                                .foregroundColor(c.textSecondary)
                        }
                    }
                    ForEach(searchResults, id: \.solarDate.day) { info in
                        Button(action: {
                            onDateSelected(info.solarDate.year, info.solarDate.month, info.solarDate.day)
                        }) {
                            HStack(spacing: 12) {
                                VStack(spacing: 2) {
                                    Text("\(info.solarDate.day)")
                                        .font(.system(size: 22, weight: .bold))
                                        .foregroundColor(c.primary)
                                    Text("Tháng \(info.solarDate.month)")
                                        .font(.system(size: 11))
                                        .foregroundColor(c.textTertiary)
                                }
                                .frame(width: 56)

                                VStack(alignment: .leading, spacing: 3) {
                                    Text("Âm lịch: \(info.lunarDate.day)/\(info.lunarDate.month)")
                                        .font(.system(size: 13))
                                        .foregroundColor(c.textSecondary)
                                    Text("\(info.dayCanChi)")
                                        .font(.system(size: 12))
                                        .foregroundColor(c.textTertiary)
                                    if let holiday = info.solarHoliday ?? info.lunarHoliday, !holiday.isEmpty {
                                        Text(holiday)
                                            .font(.system(size: 12, weight: .medium))
                                            .foregroundColor(c.noteOrange)
                                    }
                                }
                                Spacer()
                                Image(systemName: "chevron.right")
                                    .font(.system(size: 12))
                                    .foregroundColor(c.textQuaternary)
                            }
                            .padding(12)
                            .background(
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(c.surface)
                            )
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
            }
        }
        .background(c.bg)
    }

    private func performSearch() {
        let parts = searchText.split(separator: "/").compactMap { Int($0) }
        guard parts.count == 3 else { return }
        let day = parts[0], month = parts[1], year = parts[2]
        let info = DayInfoProvider.shared.getDayInfo(dd: day, mm: month, yy: year)
        searchResults = [info]
    }
}
