import SwiftUI

// ═══════════════════════════════════════════
// Main Tab View — iOS Tab Bar (5 tabs)
// Matches iOS HTML mock tab bar design
// ═══════════════════════════════════════════

private let PrimaryRed = Color(hex: "B71C1C")
private let SurfaceBg = Color(hex: "FFFBF5")
private let TabInactive = Color(hex: "857371")

enum MainTab: Int, CaseIterable {
    case calendar = 0
    case notes = 1
    case today = 2
    case prayers = 3
    case profile = 4

    var label: String {
        switch self {
        case .calendar: return "Lịch tháng"
        case .notes: return "Ghi chú"
        case .today: return "Hôm nay"
        case .prayers: return "Văn Khấn"
        case .profile: return "Cá nhân"
        }
    }

    var icon: String {
        switch self {
        case .calendar: return "calendar"
        case .notes: return "note.text"
        case .today: return "sun.min.fill"
        case .prayers: return "book.fill"
        case .profile: return "person"
        }
    }

    var iconFilled: String {
        switch self {
        case .calendar: return "calendar"
        case .notes: return "note.text"
        case .today: return "sun.min.fill"
        case .prayers: return "book.fill"
        case .profile: return "person.fill"
        }
    }
}

struct MainTabView: View {
    @State private var selectedTab: MainTab = .today

    var body: some View {
        ZStack(alignment: .bottom) {
            // Content
            Group {
                switch selectedTab {
                case .calendar:
                    PlaceholderScreen(title: "Lịch Tháng", icon: "calendar")
                case .notes:
                    NotesScreen()
                case .today:
                    HomeScreen()
                case .prayers:
                    PlaceholderScreen(title: "Văn Khấn", icon: "book.fill")
                case .profile:
                    ProfileScreen()
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            // Custom Tab Bar
            CustomTabBar(selectedTab: $selectedTab)
        }
        .edgesIgnoringSafeArea(.bottom)
    }
}

// ══════════════════════════════════════════
// CUSTOM TAB BAR (matching iOS mock)
// ══════════════════════════════════════════

struct CustomTabBar: View {
    @Binding var selectedTab: MainTab

    var body: some View {
        HStack(spacing: 0) {
            ForEach(MainTab.allCases, id: \.rawValue) { tab in
                if tab == .today {
                    // Center raised circle button
                    CenterTabButton(
                        isSelected: selectedTab == .today,
                        onTap: { selectedTab = .today }
                    )
                } else {
                    // Regular tab
                    RegularTabButton(
                        tab: tab,
                        isSelected: selectedTab == tab,
                        onTap: { selectedTab = tab }
                    )
                }
            }
        }
        .padding(.horizontal, 8)
        .padding(.top, 6)
        .padding(.bottom, 30) // home indicator padding
        .background(
            SurfaceBg.opacity(0.95)
                .background(.ultraThinMaterial)
                .shadow(color: .black.opacity(0.06), radius: 1, y: -0.5)
        )
    }
}

private struct RegularTabButton: View {
    let tab: MainTab
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 2) {
                Image(systemName: isSelected ? tab.iconFilled : tab.icon)
                    .font(.system(size: 22))
                    .foregroundColor(isSelected ? PrimaryRed : TabInactive)

                Text(tab.label)
                    .font(.system(size: 10, weight: isSelected ? .semibold : .medium))
                    .foregroundColor(isSelected ? PrimaryRed : TabInactive)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 4)
        }
        .buttonStyle(.plain)
    }
}

private struct CenterTabButton: View {
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 0) {
                // Raised circle with today's date
                ZStack {
                    Circle()
                        .fill(
                            LinearGradient(
                                colors: [Color(hex: "D32F2F"), PrimaryRed],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 56, height: 56)
                        .shadow(color: PrimaryRed.opacity(0.25), radius: 8, y: 4)
                        .overlay(
                            Circle()
                                .stroke(SurfaceBg, lineWidth: 3)
                        )

                    let today = Calendar.current.component(.day, from: Date())
                    Text("\(today)")
                        .font(.system(size: 23, weight: .heavy))
                        .foregroundColor(.white)
                        .monospacedDigit()
                }
                .offset(y: -20)

                Text("Hôm nay")
                    .font(.system(size: 10, weight: isSelected ? .semibold : .medium))
                    .foregroundColor(isSelected ? PrimaryRed : TabInactive)
                    .offset(y: -16)
            }
            .frame(maxWidth: .infinity)
        }
        .buttonStyle(.plain)
    }
}

// ══════════════════════════════════════════
// PLACEHOLDER (for tabs not yet implemented)
// ══════════════════════════════════════════

struct PlaceholderScreen: View {
    let title: String
    let icon: String

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: icon)
                .font(.system(size: 48))
                .foregroundColor(PrimaryRed.opacity(0.4))
            Text(title)
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(Color(hex: "1C1B1F"))
            Text("Đang phát triển...")
                .font(.system(size: 14))
                .foregroundColor(Color(hex: "857371"))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(SurfaceBg)
    }
}

#Preview {
    MainTabView()
}
