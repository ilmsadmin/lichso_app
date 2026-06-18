import UIKit

/// Lightweight haptic feedback helper. Call from the main actor on user interactions.
///
///   Haptics.selection()   // tab / segment switches
///   Haptics.light()       // taps, toggles
///   Haptics.success()     // completed action (quiz correct, purchase ok)
///   Haptics.error()       // failed / wrong action
enum Haptics {
    static func selection() {
        let g = UISelectionFeedbackGenerator()
        g.prepare()
        g.selectionChanged()
    }

    static func light() {
        UIImpactFeedbackGenerator(style: .light).impactOccurred()
    }

    static func medium() {
        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
    }

    static func rigid() {
        UIImpactFeedbackGenerator(style: .rigid).impactOccurred()
    }

    static func success() {
        UINotificationFeedbackGenerator().notificationOccurred(.success)
    }

    static func warning() {
        UINotificationFeedbackGenerator().notificationOccurred(.warning)
    }

    static func error() {
        UINotificationFeedbackGenerator().notificationOccurred(.error)
    }
}
