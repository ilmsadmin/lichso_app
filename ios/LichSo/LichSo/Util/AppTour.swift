import SwiftUI

// ══════════════════════════════════════════
// App Tour — spotlight / coachmark onboarding.
// Ported from Android feature/tour/AppTour.kt.
//
// Usage:
//   1. Create a @StateObject TourController with steps + onFinished.
//   2. Tag highlight targets with `.tourTarget("key")` (key == step.key).
//   3. Put `TourOverlay(controller:)` at the top of the view tree.
//   4. Call controller.start() on first launch.
// ══════════════════════════════════════════

struct TourStep: Identifiable {
    let key: String
    let title: String
    let text: String
    var id: String { key }
}

final class TourController: ObservableObject {
    let steps: [TourStep]
    private let onFinished: () -> Void
    private var finishedOnce = false

    @Published private(set) var currentIndex: Int = -1

    init(steps: [TourStep], onFinished: @escaping () -> Void) {
        self.steps = steps
        self.onFinished = onFinished
    }

    var isActive: Bool { steps.indices.contains(currentIndex) }
    var currentStep: TourStep? { steps.indices.contains(currentIndex) ? steps[currentIndex] : nil }
    var isLastStep: Bool { currentIndex == steps.count - 1 }

    func start() {
        if !finishedOnce && !steps.isEmpty { currentIndex = 0 }
    }

    func next() {
        if currentIndex < steps.count - 1 { currentIndex += 1 } else { finish() }
    }

    func finish() {
        currentIndex = -1
        if !finishedOnce {
            finishedOnce = true
            onFinished()
        }
    }
}

// MARK: - Target registration

struct TourAnchorKey: PreferenceKey {
    static var defaultValue: [String: Anchor<CGRect>] = [:]
    static func reduce(value: inout [String: Anchor<CGRect>], nextValue: () -> [String: Anchor<CGRect>]) {
        value.merge(nextValue()) { _, new in new }
    }
}

extension View {
    /// Registers this view as a tour highlight target. Layout-neutral.
    func tourTarget(_ key: String) -> some View {
        anchorPreference(key: TourAnchorKey.self, value: .bounds) { [key: $0] }
    }
}

// MARK: - Spotlight shape (full screen minus a rounded hole)

private struct SpotlightShape: Shape {
    let hole: CGRect
    func path(in rect: CGRect) -> Path {
        var p = Path(rect)
        p.addRoundedRect(in: hole, cornerSize: CGSize(width: 16, height: 16))
        return p
    }
}

// MARK: - Overlay

struct TourOverlay: View {
    @ObservedObject var controller: TourController
    let anchors: [String: Anchor<CGRect>]

    var body: some View {
        GeometryReader { proxy in
            if let step = controller.currentStep, let anchor = anchors[step.key] {
                let target = proxy[anchor]
                let hole = target.insetBy(dx: -8, dy: -8)
                let size = proxy.size
                let placeBelow = target.midY < size.height / 2

                ZStack(alignment: .topLeading) {
                    // Dim layer with spotlight cutout. Tap anywhere advances.
                    SpotlightShape(hole: hole)
                        .fill(Color.black.opacity(0.72), style: FillStyle(eoFill: true))
                        .contentShape(Rectangle())
                        .onTapGesture { withAnimation { controller.next() } }

                    // Highlight ring
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(LSTheme.gold, lineWidth: 2)
                        .frame(width: hole.width, height: hole.height)
                        .position(x: hole.midX, y: hole.midY)
                        .allowsHitTesting(false)

                    tooltip(step: step, target: target, screenSize: size, placeBelow: placeBelow)
                }
                .ignoresSafeArea()
            }
        }
        .animation(.easeInOut(duration: 0.25), value: controller.currentIndex)
    }

    private func tooltip(step: TourStep, target: CGRect, screenSize: CGSize, placeBelow: Bool) -> some View {
        let cardWidth = min(screenSize.width - 40, 320)
        let gap: CGFloat = 16
        let cardX = min(max(cardWidth / 2 + 20, target.midX), screenSize.width - cardWidth / 2 - 20)

        return VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(step.title)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
                Spacer()
                Text("\(controller.currentIndex + 1)/\(controller.steps.count)")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(LSTheme.gold)
            }
            Text(step.text)
                .font(.system(size: 14))
                .foregroundColor(.white.opacity(0.9))
                .fixedSize(horizontal: false, vertical: true)
            HStack {
                Button("Bỏ qua") { withAnimation { controller.finish() } }
                    .font(.system(size: 13))
                    .foregroundColor(.white.opacity(0.7))
                Spacer()
                Button(controller.isLastStep ? "Xong" : "Tiếp") { withAnimation { controller.next() } }
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(LSTheme.primary)
                    .padding(.horizontal, 18)
                    .padding(.vertical, 8)
                    .background(LSTheme.gold)
                    .clipShape(Capsule())
            }
            .padding(.top, 2)
        }
        .padding(16)
        .frame(width: cardWidth, alignment: .leading)
        .background(Color(hex: "1C1410"))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(LSTheme.gold.opacity(0.4), lineWidth: 1))
        .position(
            x: cardX,
            y: placeBelow ? target.maxY + gap + 80 : max(120, target.minY - gap - 80)
        )
    }
}
