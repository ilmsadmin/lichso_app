import SwiftUI

// ═══════════════════════════════════════════
// LichSo Adaptive Color Theme
//
// Global Color constants that automatically switch
// between dark and light based on UIKit's trait system.
// These respond to:
//   1. System appearance (iOS Settings)
//   2. .preferredColorScheme() override from AppState
//
// Usage in any View:
//   Text("Hello").foregroundColor(LSTheme.textPrimary)
//   .background(LSTheme.bg)
// ═══════════════════════════════════════════

enum LSTheme {
    // MARK: - Backgrounds
    static var bg: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "0F0E0C") : UIColor(hex: "FFFBF5")
        })
    }
    static var bg2: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "181610") : UIColor(hex: "FFF8F0")
        })
    }
    static var surface: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "2E2B23") : UIColor(hex: "FFF8F0")
        })
    }
    static var surfaceContainer: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "2A2720") : UIColor(hex: "FFF8F0")
        })
    }
    static var surfaceContainerHigh: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "363228") : UIColor(hex: "FFF0E8")
        })
    }

    // MARK: - Text
    static var textPrimary: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "F0E8D0") : UIColor(hex: "1C1B1F")
        })
    }
    static var textSecondary: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "B8AA88") : UIColor(hex: "534340")
        })
    }
    static var textTertiary: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "8A7E62") : UIColor(hex: "857371")
        })
    }
    static var textQuaternary: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "4A4435") : UIColor(hex: "D8C2BF")
        })
    }

    // MARK: - Brand / Primary
    static var primary: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "EF5350") : UIColor(hex: "B71C1C")
        })
    }
    static var primaryContainer: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "3D1515") : UIColor(hex: "FFDAD6")
        })
    }
    static var deepRed: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "CF6679") : UIColor(hex: "8B0000")
        })
    }

    // MARK: - Gold
    static var gold: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "E8C84A") : UIColor(hex: "D4A017")
        })
    }
    static var gold2: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "F5D96E") : UIColor(hex: "C6A300")
        })
    }
    static var goldDim: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "E8C84A").withAlphaComponent(0.18)
                : UIColor(hex: "D4A017").withAlphaComponent(0.12)
        })
    }

    // MARK: - Semantic
    static var goodGreen: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "81C784") : UIColor(hex: "2E7D32")
        })
    }
    static var badRed: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "EF5350") : UIColor(hex: "C62828")
        })
    }
    static var teal: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "4ABEAA") : UIColor(hex: "006C4C")
        })
    }

    // MARK: - Category accent colors (fixed brand colors, same in both modes)
    static let noteBlue     = Color(hex: "1565C0")
    static let remindOrange = Color(hex: "E65100")
    static let taskGreen    = Color(hex: "2E7D32")

    // MARK: - Outlines / Borders
    static var outline: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "9E9080") : UIColor(hex: "857371")
        })
    }
    static var outlineVariant: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "5A4F42") : UIColor(hex: "D8C2BF")
        })
    }
    static var border: Color {
        Color(UIColor { t in
            t.userInterfaceStyle == .dark
                ? UIColor(hex: "FFDC64").withAlphaComponent(0.12)
                : UIColor(hex: "D8C2BF").withAlphaComponent(0.13)
        })
    }
}

// MARK: - UIColor hex helper

extension UIColor {
    convenience init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3:
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6:
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8:
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 255, 255, 255)
        }
        self.init(
            red: CGFloat(r) / 255,
            green: CGFloat(g) / 255,
            blue: CGFloat(b) / 255,
            alpha: CGFloat(a) / 255
        )
    }
}
