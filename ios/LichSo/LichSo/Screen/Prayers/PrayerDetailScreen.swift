import SwiftUI

// ═══════════════════════════════════════════
// Prayer Detail Screen — Full prayer text display
// Shows prayer content with share & copy actions
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

struct PrayerDetailScreen: View {
    @Environment(\.dismiss) private var dismiss
    let prayer: Prayer

    @State private var fontSize: CGFloat = 16
    @State private var showCopiedToast = false

    var body: some View {
        VStack(spacing: 0) {
            // ═══ HEADER — Navigation bar only ═══
            DetailNavBar(
                onBack: { dismiss() },
                onShare: { sharePrayer() }
            )

            // ═══ CONTENT ═══
            ScrollView(.vertical, showsIndicators: true) {
                VStack(alignment: .leading, spacing: 16) {
                    // Prayer title section (moved from header)
                    HStack(spacing: 12) {
                        let colors = prayer.category.iconColor
                        Image(systemName: prayer.iconName)
                            .font(.system(size: 22))
                            .foregroundColor(colors.0)
                            .frame(width: 48, height: 48)
                            .background(colors.1)
                            .clipShape(RoundedRectangle(cornerRadius: 14))

                        VStack(alignment: .leading, spacing: 4) {
                            Text(prayer.title)
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(TextMain)
                                .lineLimit(2)

                            // Category badge
                            HStack(spacing: 4) {
                                Image(systemName: prayer.category.icon)
                                    .font(.system(size: 10))
                                    .foregroundColor(PrimaryRed)
                                Text(prayer.category.rawValue)
                                    .font(.system(size: 12, weight: .semibold))
                                    .foregroundColor(PrimaryRed)
                            }
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(Color(hex: "FFEBEE"))
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                        }
                    }

                    // Description
                    Text(prayer.description)
                        .font(.system(size: 14))
                        .foregroundColor(TextSub)
                        .lineSpacing(4)

                    Divider().foregroundColor(OutlineVar)

                    // Font size control
                    HStack {
                        Text("Cỡ chữ")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(TextDim)
                        Spacer()
                        HStack(spacing: 12) {
                            Button { fontSize = max(12, fontSize - 1) } label: {
                                Image(systemName: "textformat.size.smaller")
                                    .font(.system(size: 16))
                                    .foregroundColor(TextSub)
                                    .frame(width: 32, height: 32)
                                    .background(SurfaceContainer)
                                    .clipShape(Circle())
                                    .overlay(Circle().stroke(OutlineVar, lineWidth: 1))
                            }
                            Text("\(Int(fontSize))")
                                .font(.system(size: 13, weight: .medium))
                                .foregroundColor(TextMain)
                                .frame(width: 28)
                            Button { fontSize = min(24, fontSize + 1) } label: {
                                Image(systemName: "textformat.size.larger")
                                    .font(.system(size: 16))
                                    .foregroundColor(TextSub)
                                    .frame(width: 32, height: 32)
                                    .background(SurfaceContainer)
                                    .clipShape(Circle())
                                    .overlay(Circle().stroke(OutlineVar, lineWidth: 1))
                            }
                        }
                    }

                    // Prayer content
                    VStack(alignment: .leading, spacing: 0) {
                        Text(prayer.content)
                            .font(.system(size: fontSize, weight: .regular, design: .serif))
                            .foregroundColor(TextMain)
                            .lineSpacing(fontSize * 0.6)
                            .textSelection(.enabled)
                    }
                    .padding(20)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(SurfaceContainerHigh)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(OutlineVar, lineWidth: 1)
                    )

                    // Tip
                    HStack(spacing: 8) {
                        Image(systemName: "lightbulb.fill")
                            .font(.system(size: 14))
                            .foregroundColor(GoldAccent)
                        Text("Gợi ý: Thay …… bằng thông tin thực tế của gia đình bạn khi đọc bài khấn.")
                            .font(.system(size: 12))
                            .foregroundColor(TextSub)
                            .lineSpacing(3)
                    }
                    .padding(14)
                    .background(Color(hex: "FFF8E1"))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(Color(hex: "FFE082"), lineWidth: 1)
                    )

                    Spacer().frame(height: 16)
                }
                .padding(.horizontal, 20)
                .padding(.top, 16)
            }

            // ═══ BOTTOM ACTION BAR ═══
            BottomActionBar(
                onCopy: { copyPrayer() },
                onShare: { sharePrayer() }
            )
        }
        .background(SurfaceBg)
        .overlay {
            if showCopiedToast {
                VStack {
                    Spacer()
                    Text("Đã sao chép ✓")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.white)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 10)
                        .background(Color.black.opacity(0.75))
                        .clipShape(RoundedRectangle(cornerRadius: 20))
                        .padding(.bottom, 80)
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                }
                .animation(.spring(response: 0.3), value: showCopiedToast)
            }
        }
    }

    private func copyPrayer() {
        UIPasteboard.general.string = "\(prayer.title)\n\n\(prayer.content)"
        showCopiedToast = true
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            showCopiedToast = false
        }
    }

    private func sharePrayer() {
        let text = "\(prayer.title)\n\n\(prayer.content)\n\n— Lịch Số App"
        let activityVC = UIActivityViewController(activityItems: [text], applicationActivities: nil)
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = windowScene.windows.first?.rootViewController {
            rootVC.present(activityVC, animated: true)
        }
    }
}

// ══════════════════════════════════════════
// NAVIGATION BAR — Simple top bar
// ══════════════════════════════════════════

private struct DetailNavBar: View {
    let onBack: () -> Void
    let onShare: () -> Void

    var body: some View {
        HStack {
            Button(action: onBack) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(width: 40, height: 40)
                    .background(.white.opacity(0.12))
                    .clipShape(Circle())
            }

            Spacer()

            Text("Văn Khấn")
                .font(.system(size: 17, weight: .bold))
                .foregroundColor(.white)

            Spacer()

            Button(action: onShare) {
                Image(systemName: "square.and.arrow.up")
                    .font(.system(size: 16))
                    .foregroundColor(.white)
                    .frame(width: 40, height: 40)
                    .background(.white.opacity(0.12))
                    .clipShape(Circle())
            }
        }
        .padding(.horizontal, 20)
        .padding(.top, 12)
        .padding(.bottom, 12)
        .background(
            LinearGradient(
                colors: [DeepRed, PrimaryRed, Color(hex: "D32F2F")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
    }
}

// ══════════════════════════════════════════
// BOTTOM ACTION BAR
// ══════════════════════════════════════════

private struct BottomActionBar: View {
    let onCopy: () -> Void
    let onShare: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Button(action: onCopy) {
                HStack(spacing: 6) {
                    Image(systemName: "doc.on.doc")
                        .font(.system(size: 16))
                    Text("Sao chép")
                        .font(.system(size: 14, weight: .semibold))
                }
                .foregroundColor(PrimaryRed)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(Color(hex: "5D1212"))
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }

            Button(action: onShare) {
                HStack(spacing: 6) {
                    Image(systemName: "square.and.arrow.up")
                        .font(.system(size: 16))
                    Text("Chia sẻ")
                        .font(.system(size: 14, weight: .semibold))
                }
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(PrimaryRed)
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
        .background(SurfaceBg)
        .overlay(alignment: .top) {
            Divider().foregroundColor(OutlineVar)
        }
    }
}

// ══════════════════════════════════════════
// PREVIEW
// ══════════════════════════════════════════

#Preview {
    PrayerDetailScreen(prayer: PrayersDatabase.featured)
}
