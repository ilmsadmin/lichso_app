import SwiftUI

// ═══════════════════════════════════════════
// Home Banner Carousel — ported from Android HomeScreen.kt
// Auto-scrolling carousel shown in the red top section.
// ═══════════════════════════════════════════

// ── Default banners shown when server returns nothing ──
let DEFAULT_BANNERS: [Banner] = [
    Banner(
        id: "local_1",
        title: "Xem ngày tốt hôm nay",
        subtitle: "Gợi ý việc nên làm và giờ đẹp trong ngày",
        iconKey: "sun",
        ctaText: "Xem ngay",
        ctaType: "route",
        ctaRoute: "gooddays",
        type: "feature"
    ),
    Banner(
        id: "local_2",
        title: "Khám phá kiến thức lịch sử",
        subtitle: "Bài viết hay về văn hóa và truyền thống Việt Nam",
        iconKey: "article",
        ctaText: "Đọc ngay",
        ctaType: "route",
        ctaRoute: "knowledge_feed",
        type: "content"
    ),
    Banner(
        id: "local_3",
        title: "Đố vui lịch sử",
        subtitle: "Thử thách kiến thức với hàng trăm câu hỏi thú vị",
        iconKey: "quiz",
        ctaText: "Chơi ngay",
        ctaType: "route",
        ctaRoute: "quiz_home",
        type: "quiz"
    ),
    Banner(
        id: "local_4",
        title: "Tử vi & Phong thuỷ AI",
        subtitle: "Giải đáp mọi thắc mắc về phong thuỷ, vận mệnh",
        iconKey: "ai",
        ctaText: "Hỏi AI",
        ctaType: "route",
        ctaRoute: "chat",
        type: "ai"
    ),
]

// ── Normalize relative media URLs returned by the server ──
func normalizeServerMediaUrl(_ rawUrl: String?) -> String? {
    let value = rawUrl?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
    if value.isEmpty { return nil }
    if value.hasPrefix("http://") || value.hasPrefix("https://") { return value }

    let cleaned = value.hasPrefix("/") ? String(value.dropFirst()) : value
    if cleaned.hasPrefix("api/uploads/") {
        return "https://lichso.vn/" + cleaned
    } else if cleaned.hasPrefix("uploads/") {
        return "https://lichso.vn/api/" + cleaned
    }
    return "https://lichso.vn/" + cleaned
}

// ── Map iconKey / type → SF Symbol ──
private func bannerPresetIcon(_ iconKey: String?, _ bannerType: String?) -> String {
    switch iconKey {
    case "calendar": return "calendar"
    case "article":  return "book"
    case "quiz":     return "brain.head.profile"
    case "ai":       return "sparkles"
    case "gift":     return "gift"
    case "star":     return "star.fill"
    case "sun":      return "sun.max.fill"
    case "moon":     return "moon.stars.fill"
    case "clock":    return "clock"
    case "compass":  return "safari"
    case "scroll":   return "doc.text"
    case "bell":     return "bell.fill"
    case "trophy":   return "trophy.fill"
    case "users":    return "person.3.fill"
    case "heart":    return "heart.fill"
    case "home":     return "house.fill"
    case "shop":     return "bag.fill"
    case "wallet":   return "wallet.pass.fill"
    case "map":      return "mappin.and.ellipse"
    case "shield":   return "checkmark.shield.fill"
    default:
        switch bannerType {
        case "content": return "book"
        case "quiz":    return "brain.head.profile"
        case "ai":      return "sparkles"
        case "promo":   return "tag.fill"
        default:        return "calendar"
        }
    }
}

// ── Gradient colors per banner type / bgColor ──
private func bannerGradient(_ banner: Banner) -> [Color] {
    if let colorStr = banner.bgColor, !colorStr.isEmpty {
        let parsed = Color(hex: colorStr.hasPrefix("#") ? String(colorStr.dropFirst()) : colorStr)
        return [parsed, parsed]
    }
    switch banner.type {
    case "content": return [Color(hex: "1565C0"), Color(hex: "0D47A1")]
    case "quiz":    return [Color(hex: "2E7D32"), Color(hex: "1B5E20")]
    case "ai":      return [Color(hex: "4A148C"), Color(hex: "311B92")]
    default:        return [Color(hex: "BF360C"), Color(hex: "8D1A00")]
    }
}

// MARK: - Banner Carousel

struct BannerCarousel: View {
    let banners: [Banner]
    let onBannerAction: (String) -> Void

    @State private var currentPage = 0
    private let timer = Timer.publish(every: 4, on: .main, in: .common).autoconnect()

    var body: some View {
        VStack(spacing: 7) {
            TabView(selection: $currentPage) {
                ForEach(Array(banners.enumerated()), id: \.element.id) { index, banner in
                    BannerCard(banner: banner) { route in
                        if let route = route, !route.isEmpty { onBannerAction(route) }
                    }
                    .tag(index)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
            .frame(height: 68)
            .onReceive(timer) { _ in
                guard banners.count > 1 else { return }
                withAnimation { currentPage = (currentPage + 1) % banners.count }
            }

            // Pagination dots
            if banners.count > 1 {
                HStack(spacing: 4) {
                    ForEach(0..<banners.count, id: \.self) { index in
                        let isSelected = currentPage == index
                        Capsule()
                            .fill(isSelected ? Color.white : Color.white.opacity(0.4))
                            .frame(width: isSelected ? 16 : 5, height: 5)
                            .animation(.easeInOut(duration: 0.3), value: currentPage)
                            .onTapGesture { withAnimation { currentPage = index } }
                    }
                }
            }
        }
    }
}

// MARK: - Banner Card

private struct BannerCard: View {
    let banner: Banner
    let onCtaClick: (String?) -> Void

    var body: some View {
        let gradient = bannerGradient(banner)
        let imageUrl = normalizeServerMediaUrl(banner.imageUrl)
        let iconUrl = normalizeServerMediaUrl(banner.iconUrl)

        Button {
            onCtaClick(banner.ctaRoute)
        } label: {
            ZStack {
                LinearGradient(colors: gradient, startPoint: .topLeading, endPoint: .bottomTrailing)

                // Server background image (if any)
                if let imageUrl = imageUrl, let url = URL(string: imageUrl) {
                    CachedAsyncImage(url: url) { img in
                        img.resizable().aspectRatio(contentMode: .fill).opacity(0.9)
                    } placeholder: {
                        Color.clear
                    }
                    LinearGradient(
                        colors: [
                            Color.black.opacity(0.48),
                            Color.black.opacity(0.28),
                            Color.black.opacity(0.12),
                        ],
                        startPoint: .leading, endPoint: .trailing
                    )
                }

                HStack(spacing: 12) {
                    // Left icon
                    ZStack {
                        RoundedRectangle(cornerRadius: 10)
                            .fill(Color.white.opacity(0.15))
                            .overlay(
                                RoundedRectangle(cornerRadius: 10)
                                    .stroke(Color.white.opacity(0.2), lineWidth: 1)
                            )
                            .frame(width: 42, height: 42)
                        if let iconUrl = iconUrl, let url = URL(string: iconUrl) {
                            CachedAsyncImage(url: url) { img in
                                img.resizable().aspectRatio(contentMode: .fit).frame(width: 28, height: 28)
                            } placeholder: {
                                Image(systemName: bannerPresetIcon(banner.iconKey, banner.type))
                                    .font(.system(size: 22)).foregroundColor(.white)
                            }
                        } else {
                            Image(systemName: bannerPresetIcon(banner.iconKey, banner.type))
                                .font(.system(size: 22)).foregroundColor(.white)
                        }
                    }

                    // Title + subtitle
                    VStack(alignment: .leading, spacing: 1) {
                        Text(banner.title)
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(.white)
                            .lineLimit(1)
                        if let subtitle = banner.subtitle, !subtitle.isEmpty {
                            Text(subtitle)
                                .font(.system(size: 10))
                                .foregroundColor(.white.opacity(0.8))
                                .lineLimit(1)
                        }
                    }
                    Spacer(minLength: 8)

                    // CTA pill
                    if let ctaText = banner.ctaText, !ctaText.isEmpty {
                        HStack(spacing: 2) {
                            Text(ctaText)
                                .font(.system(size: 10, weight: .semibold))
                                .foregroundColor(Color(hex: "8B2500"))
                            Image(systemName: "chevron.right")
                                .font(.system(size: 9, weight: .bold))
                                .foregroundColor(Color(hex: "8B2500"))
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(Color(hex: "F5E6C8"))
                        .clipShape(Capsule())
                    }
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
            }
            .frame(height: 68)
            .clipShape(RoundedRectangle(cornerRadius: 14))
        }
        .buttonStyle(.plain)
    }
}
