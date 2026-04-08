import SwiftUI

struct ThisDayInHistoryScreen: View {
    @Environment(\.lichSoColors) private var c
    @State private var events: [HistoricalEvent] = []
    var onBackClick: () -> Void = {}

    var body: some View {
        VStack(spacing: 0) {
            // Top bar
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(c.textPrimary)
                }
                Spacer()
                Text("NGÀY NÀY NĂM XƯA")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(c.textPrimary)
                    .tracking(1)
                Spacer()
                Color.clear.frame(width: 24)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            let today = Date()
            let cal = Calendar.current
            let day = cal.component(.day, from: today)
            let month = cal.component(.month, from: today)

            // Date display
            Text("Ngày \(day) tháng \(month)")
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(c.primary)
                .padding(.bottom, 12)

            ScrollView {
                LazyVStack(spacing: 12) {
                    if events.isEmpty {
                        VStack(spacing: 12) {
                            Image(systemName: "clock.arrow.circlepath")
                                .font(.system(size: 48))
                                .foregroundColor(c.textQuaternary)
                                .padding(.top, 60)
                            Text("Chưa có dữ liệu cho ngày này")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(c.textSecondary)
                        }
                    }
                    ForEach(events, id: \.title) { event in
                        HistoryEventCard(event: event, c: c)
                    }
                }
                .padding(.horizontal, 16)
            }
        }
        .background(c.bg)
        .onAppear {
            let cal = Calendar.current
            let day = cal.component(.day, from: Date())
            let month = cal.component(.month, from: Date())
            events = HistoricalEventProvider.getEvents(day: day, month: month)
        }
    }
}

struct HistoryEventCard: View {
    let event: HistoricalEvent
    let c: LichSoColors

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            // Year badge
            Text("\(event.year)")
                .font(.system(size: 13, weight: .bold))
                .foregroundColor(.white)
                .padding(.horizontal, 10)
                .padding(.vertical, 6)
                .background(categoryColor)
                .cornerRadius(8)

            VStack(alignment: .leading, spacing: 4) {
                // Category tag
                Text(categoryLabel)
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundColor(categoryColor)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2)
                    .background(categoryColor.opacity(0.15))
                    .cornerRadius(6)

                Text(event.title)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(c.textPrimary)
                    .fixedSize(horizontal: false, vertical: true)

                Text(event.description)
                    .font(.system(size: 13))
                    .foregroundColor(c.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
        .padding(14)
        .background(
            RoundedRectangle(cornerRadius: 14)
                .fill(c.surface)
        )
    }

    private var categoryColor: Color {
        switch event.category {
        case .vietnam: return c.red
        case .world: return c.teal
        case .culture: return c.gold
        case .science: return c.notePurple
        }
    }

    private var categoryLabel: String {
        switch event.category {
        case .vietnam: return "Việt Nam"
        case .world: return "Thế giới"
        case .culture: return "Văn hóa"
        case .science: return "Khoa học"
        }
    }
}
