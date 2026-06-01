import SwiftUI

// ═══════════════════════════════════════════
// Drag-and-Drop Popup Overlay — ported from Android LichSoMainScreen.kt
// Shows one small floating popup image at a time. The user can drag it
// around, tap to trigger its CTA, or close it with the × button.
// ═══════════════════════════════════════════

struct HomePopupOverlay: View {
    let popups: [Popup]
    let onAction: (String) -> Void

    @State private var dismissedIds: Set<String> = []
    @State private var dragOffset: CGSize = .zero
    @State private var accumulatedOffset: CGSize = .zero

    private var currentPopup: Popup? {
        popups.first { !dismissedIds.contains($0.id) }
    }

    var body: some View {
        GeometryReader { _ in
            if let popup = currentPopup {
                let url = normalizeServerMediaUrl(popup.imageUrl).flatMap { URL(string: $0) }

                ZStack(alignment: .topTrailing) {
                    // Popup image — draggable + tappable
                    CachedAsyncImage(url: url) { img in
                        img.resizable().aspectRatio(contentMode: .fit)
                    } placeholder: {
                        Color.clear
                    }
                    .frame(maxWidth: 90, maxHeight: 90)
                    .onTapGesture {
                        onAction(popup.ctaRoute ?? "")
                    }

                    // Close button
                    Button {
                        dismissedIds.insert(popup.id)
                        accumulatedOffset = .zero
                        dragOffset = .zero
                    } label: {
                        ZStack {
                            Circle().fill(Color.black.opacity(0.5)).frame(width: 18, height: 18)
                            Image(systemName: "xmark")
                                .font(.system(size: 9, weight: .bold))
                                .foregroundColor(.white)
                        }
                    }
                    .offset(x: -8, y: 8)
                }
                .offset(
                    x: accumulatedOffset.width + dragOffset.width,
                    y: accumulatedOffset.height + dragOffset.height
                )
                .gesture(
                    DragGesture()
                        .onChanged { value in dragOffset = value.translation }
                        .onEnded { value in
                            accumulatedOffset.width += value.translation.width
                            accumulatedOffset.height += value.translation.height
                            dragOffset = .zero
                        }
                )
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: alignment(for: popup.position))
                .padding(padding(for: popup.position))
                .id(popup.id)
            }
        }
        .allowsHitTesting(currentPopup != nil)
    }

    private func alignment(for position: String?) -> Alignment {
        switch position {
        case "top_left": return .topLeading
        case "top_right": return .topTrailing
        case "bottom_left": return .bottomLeading
        case "bottom_right": return .bottomTrailing
        case "center_left": return .leading
        case "center_right": return .trailing
        default: return .center
        }
    }

    private func padding(for position: String?) -> EdgeInsets {
        switch position {
        case "top_left": return EdgeInsets(top: 96, leading: 16, bottom: 0, trailing: 0)
        case "top_right": return EdgeInsets(top: 96, leading: 0, bottom: 0, trailing: 16)
        case "bottom_left": return EdgeInsets(top: 0, leading: 16, bottom: 96, trailing: 0)
        case "bottom_right": return EdgeInsets(top: 0, leading: 0, bottom: 96, trailing: 16)
        case "center_left": return EdgeInsets(top: 0, leading: 16, bottom: 0, trailing: 0)
        case "center_right": return EdgeInsets(top: 0, leading: 0, bottom: 0, trailing: 16)
        default: return EdgeInsets(top: 16, leading: 16, bottom: 16, trailing: 16)
        }
    }
}
