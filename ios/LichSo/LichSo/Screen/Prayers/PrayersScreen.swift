import SwiftUI

// ═══════════════════════════════════════════
// Prayers Screen — Văn Khấn
// Matches screen-prayers.html mock design
// Lists all Vietnamese traditional prayers by category
// ═══════════════════════════════════════════

private var PrimaryRed: Color { LSTheme.primary }
private var DeepRed: Color { LSTheme.deepRed }
private var GoldAccent: Color { LSTheme.gold }
private var SurfaceBg: Color { LSTheme.bg }
private var SurfaceContainer: Color { LSTheme.surfaceContainer }
private var SurfaceContainerHigh: Color { LSTheme.surfaceContainerHigh }
private var TextMain: Color { LSTheme.textPrimary }
private var TextSub: Color { LSTheme.textSecondary }
private var TextDim: Color { LSTheme.textTertiary }
private var OutlineVar: Color { LSTheme.outlineVariant }

struct PrayersScreen: View {
    @State private var selectedCategory: PrayerCategory = .all
    @State private var searchText = ""
    @State private var selectedPrayer: Prayer?
    @State private var showBookmarks = false

    var body: some View {
        VStack(spacing: 0) {
            // ═══ HEADER ═══
            PrayersHeader(onBookmarksTap: { showBookmarks = true })

            // ═══ SEARCH BAR ═══
            SearchBar(text: $searchText)

            // ═══ CATEGORY CHIPS ═══
            CategoryChips(selected: $selectedCategory)

            // ═══ CONTENT ═══
            ScrollView(.vertical, showsIndicators: false) {
                VStack(alignment: .leading, spacing: 0) {
                    // Featured card (only when showing all & no search)
                    if selectedCategory == .all && searchText.isEmpty {
                        FeaturedPrayerCard(prayer: PrayersDatabase.featured) {
                            selectedPrayer = PrayersDatabase.featured
                        }
                    }

                    // Prayer list
                    if searchText.isEmpty {
                        let groups = PrayersDatabase.groupedPrayers(for: selectedCategory)
                        ForEach(Array(groups.enumerated()), id: \.offset) { _, group in
                            PrayerSectionTitle(title: group.section, icon: group.icon)
                            ForEach(group.prayers) { prayer in
                                PrayerCard(prayer: prayer) {
                                    selectedPrayer = prayer
                                }
                            }
                        }
                    } else {
                        let results = PrayersDatabase.search(searchText)
                        if results.isEmpty {
                            EmptySearchView(query: searchText)
                        } else {
                            ForEach(results) { prayer in
                                PrayerCard(prayer: prayer) {
                                    selectedPrayer = prayer
                                }
                            }
                        }
                    }

                    Spacer().frame(height: 24)
                }
                .padding(.horizontal, 16)
            }
        }
        .background(SurfaceBg)
        .fullScreenCover(item: $selectedPrayer) { prayer in
            PrayerDetailScreen(prayer: prayer)
        }
    }
}

// ══════════════════════════════════════════
// HEADER
// ══════════════════════════════════════════

private struct PrayersHeader: View {
    let onBookmarksTap: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack {
                Text("Văn Khấn")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.white)
                Spacer()
                Button(action: onBookmarksTap) {
                    Image(systemName: "bookmark")
                        .font(.system(size: 18))
                        .foregroundColor(.white)
                        .frame(width: 40, height: 40)
                        .background(.white.opacity(0.12))
                        .clipShape(Circle())
                }
            }

            Text("Tuyển tập các bài văn khấn truyền thống Việt Nam")
                .font(.system(size: 12))
                .foregroundColor(.white.opacity(0.6))
                .padding(.top, 2)
        }
        .padding(.horizontal, 24)
        .padding(.top, 8)
        .padding(.bottom, 20)
        .background(
            LinearGradient(
                colors: [DeepRed, PrimaryRed, Color(hex: "D32F2F")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea(edges: .top)
        )
        .overlay(alignment: .topTrailing) {
            Image(systemName: "sun.max.fill")
                .font(.system(size: 50))
                .foregroundColor(.white.opacity(0.06))
                .offset(x: -10, y: 5)
        }
    }
}

// ══════════════════════════════════════════
// SEARCH BAR
// ══════════════════════════════════════════

private struct SearchBar: View {
    @Binding var text: String

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 18))
                .foregroundColor(TextDim)
            TextField("Tìm bài văn khấn...", text: $text)
                .font(.system(size: 14))
                .foregroundColor(TextMain)

            if !text.isEmpty {
                Button {
                    text = ""
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .font(.system(size: 16))
                        .foregroundColor(TextDim)
                }
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 11)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 28))
        .overlay(
            RoundedRectangle(cornerRadius: 28)
                .stroke(OutlineVar, lineWidth: 1.5)
        )
        .padding(.horizontal, 16)
        .padding(.top, 12)
    }
}

// ══════════════════════════════════════════
// CATEGORY CHIPS
// ══════════════════════════════════════════

private struct CategoryChips: View {
    @Binding var selected: PrayerCategory

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(PrayerCategory.allCases) { cat in
                    CategoryChip(
                        iconName: cat.icon,
                        title: cat.rawValue,
                        isActive: selected == cat
                    ) {
                        selected = cat
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
        }
    }
}

private struct CategoryChip: View {
    let iconName: String
    let title: String
    let isActive: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 5) {
                Image(systemName: iconName)
                    .font(.system(size: 12))
                Text(title)
                    .font(.system(size: 12, weight: .semibold))
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 7)
            .background(isActive ? PrimaryRed : SurfaceBg)
            .foregroundColor(isActive ? .white : TextSub)
            .clipShape(RoundedRectangle(cornerRadius: 20))
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(isActive ? PrimaryRed : OutlineVar, lineWidth: 1.5)
            )
        }
        .buttonStyle(.plain)
    }
}

// ══════════════════════════════════════════
// FEATURED PRAYER CARD
// ══════════════════════════════════════════

private struct FeaturedPrayerCard: View {
    let prayer: Prayer
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 14) {
                // Icon
                Image(systemName: prayer.iconName)
                    .font(.system(size: 24))
                    .foregroundColor(.white)
                    .frame(width: 56, height: 56)
                    .background(
                        LinearGradient(
                            colors: [GoldAccent, Color(hex: "B8860B")],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                VStack(alignment: .leading, spacing: 2) {
                    Text("HAY DÙNG NHẤT")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(Color(hex: "E65100"))
                        .tracking(0.5)

                    Text(prayer.title)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(TextMain)
                        .lineLimit(2)

                    Text(prayer.description)
                        .font(.system(size: 12))
                        .foregroundColor(TextSub)
                        .lineLimit(2)
                }

                Spacer()
            }
            .padding(16)
            .background(
                LinearGradient(
                    colors: [Color(hex: "FFF8E1"), Color(hex: "FFECB3")],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            )
            .clipShape(RoundedRectangle(cornerRadius: 20))
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(Color(hex: "FFE082"), lineWidth: 1.5)
            )
            .padding(.top, 4)
            .padding(.bottom, 14)
        }
        .buttonStyle(.plain)
    }
}

// ══════════════════════════════════════════
// SECTION TITLE
// ══════════════════════════════════════════

private struct PrayerSectionTitle: View {
    let title: String
    let icon: String

    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundColor(PrimaryRed)
            Text(title)
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(TextMain)
        }
        .padding(.top, 14)
        .padding(.bottom, 8)
    }
}

// ══════════════════════════════════════════
// PRAYER CARD
// ══════════════════════════════════════════

private struct PrayerCard: View {
    let prayer: Prayer
    let onTap: () -> Void

    private var iconBackground: Color {
        prayer.category.iconColor.1
    }

    private var iconForeground: Color {
        prayer.category.iconColor.0
    }

    var body: some View {
        Button(action: onTap) {
            HStack(alignment: .top, spacing: 12) {
                // Icon
                Image(systemName: prayer.iconName)
                    .font(.system(size: 20))
                    .foregroundColor(iconForeground)
                    .frame(width: 44, height: 44)
                    .background(iconBackground)
                    .clipShape(RoundedRectangle(cornerRadius: 12))

                // Info
                VStack(alignment: .leading, spacing: 2) {
                    Text(prayer.title)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(TextMain)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)

                    Text(prayer.description)
                        .font(.system(size: 11))
                        .foregroundColor(TextSub)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)

                    // Tags
                    HStack(spacing: 4) {
                        ForEach(Array(prayer.tags.enumerated()), id: \.offset) { _, tag in
                            HStack(spacing: 3) {
                                if tag.type == .hot {
                                    Image(systemName: "flame.fill")
                                        .font(.system(size: 7))
                                } else if tag.type == .new {
                                    Image(systemName: "sparkles")
                                        .font(.system(size: 7))
                                }
                                Text(tag.text)
                                    .font(.system(size: 9, weight: .semibold))
                            }
                            .foregroundColor(tagColor(tag.type))
                                .padding(.horizontal, 8)
                                .padding(.vertical, 2)
                                .background(tagBg(tag.type))
                                .clipShape(RoundedRectangle(cornerRadius: 6))
                        }
                    }
                    .padding(.top, 4)
                }

                Spacer()

                // Arrow
                Image(systemName: "chevron.right")
                    .font(.system(size: 14))
                    .foregroundColor(OutlineVar)
                    .padding(.top, 14)
            }
            .padding(14)
            .background(SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(OutlineVar, lineWidth: prayer.isPopular ? 0 : 1)
            )
            .overlay(alignment: .leading) {
                if prayer.isPopular {
                    Rectangle()
                        .fill(PrimaryRed)
                        .frame(width: 3)
                        .clipShape(RoundedRectangle(cornerRadius: 2))
                }
            }
            .padding(.bottom, 8)
        }
        .buttonStyle(.plain)
    }

    private func tagColor(_ type: Prayer.PrayerTag.TagType) -> Color {
        switch type {
        case .hot:
            return Color(UIColor { t in
                t.userInterfaceStyle == .dark
                    ? UIColor(hex: "EF9A9A") : UIColor(hex: "C62828")
            })
        case .new:
            return Color(UIColor { t in
                t.userInterfaceStyle == .dark
                    ? UIColor(hex: "A5D6A7") : UIColor(hex: "2E7D32")
            })
        case .normal: return TextSub
        }
    }

    private func tagBg(_ type: Prayer.PrayerTag.TagType) -> Color {
        switch type {
        case .hot:
            return Color(UIColor { t in
                t.userInterfaceStyle == .dark
                    ? UIColor(hex: "3A1B1B") : UIColor(hex: "FFEBEE")
            })
        case .new:
            return Color(UIColor { t in
                t.userInterfaceStyle == .dark
                    ? UIColor(hex: "1B3A2F") : UIColor(hex: "E8F5E9")
            })
        case .normal: return SurfaceContainerHigh
        }
    }
}

// ══════════════════════════════════════════
// EMPTY SEARCH
// ══════════════════════════════════════════

private struct EmptySearchView: View {
    let query: String

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 36))
                .foregroundColor(TextDim.opacity(0.5))
            Text("Không tìm thấy kết quả")
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(TextMain)
            Text("Thử tìm kiếm với từ khóa khác")
                .font(.system(size: 13))
                .foregroundColor(TextDim)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 60)
    }
}

// ══════════════════════════════════════════
// PREVIEW
// ══════════════════════════════════════════

#Preview {
    PrayersScreen()
}
