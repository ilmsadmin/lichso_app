import SwiftUI

// ═══════════════════════════════════════════
// LỊCH SỐ — Design System Colors
// Vietnamese Red & Gold
// Port from Android LichSoColors
// ═══════════════════════════════════════════

struct LichSoColors {
    let bg: Color
    let bg2: Color
    let surface: Color
    let surfaceContainer: Color
    let surfaceContainerHigh: Color
    let border: Color
    let gold: Color
    let gold2: Color
    let goldDim: Color
    let teal: Color
    let primary: Color
    let onPrimary: Color
    let primaryContainer: Color
    let deepRed: Color
    let outline: Color
    let outlineVariant: Color
    let textPrimary: Color
    let textSecondary: Color
    let textTertiary: Color
    let textQuaternary: Color
    let goodGreen: Color
    let badRed: Color
    let neutralAmber: Color
    let isDark: Bool
}

extension LichSoColors {
    static let light = LichSoColors(
        bg: Color(hex: "FFFBF5"),
        bg2: Color(hex: "FFF8F0"),
        surface: Color(hex: "FFF8F0"),
        surfaceContainer: Color(hex: "FFF8F0"),
        surfaceContainerHigh: Color(hex: "FFF0E8"),
        border: Color(hex: "D8C2BF").opacity(0.13),
        gold: Color(hex: "D4A017"),
        gold2: Color(hex: "C6A300"),
        goldDim: Color(hex: "D4A017").opacity(0.12),
        teal: Color(hex: "006C4C"),
        primary: Color(hex: "B71C1C"),
        onPrimary: .white,
        primaryContainer: Color(hex: "FFDAD6"),
        deepRed: Color(hex: "8B0000"),
        outline: Color(hex: "857371"),
        outlineVariant: Color(hex: "D8C2BF"),
        textPrimary: Color(hex: "1C1B1F"),
        textSecondary: Color(hex: "534340"),
        textTertiary: Color(hex: "857371"),
        textQuaternary: Color(hex: "D8C2BF"),
        goodGreen: Color(hex: "2E7D32"),
        badRed: Color(hex: "C62828"),
        neutralAmber: Color(hex: "F57F17"),
        isDark: false
    )

    static let dark = LichSoColors(
        bg: Color(hex: "0F0E0C"),
        bg2: Color(hex: "181610"),
        surface: Color(hex: "2E2B23"),
        surfaceContainer: Color(hex: "2A2720"),
        surfaceContainerHigh: Color(hex: "363228"),
        border: Color(hex: "FFDC64").opacity(0.12),
        gold: Color(hex: "E8C84A"),
        gold2: Color(hex: "F5D96E"),
        goldDim: Color(hex: "E8C84A").opacity(0.18),
        teal: Color(hex: "4ABEAA"),
        primary: Color(hex: "EF5350"),
        onPrimary: .white,
        primaryContainer: Color(hex: "3D1515"),
        deepRed: Color(hex: "CF6679"),
        outline: Color(hex: "9E9080"),
        outlineVariant: Color(hex: "5A4F42"),
        textPrimary: Color(hex: "F0E8D0"),
        textSecondary: Color(hex: "B8AA88"),
        textTertiary: Color(hex: "8A7E62"),
        textQuaternary: Color(hex: "4A4435"),
        goodGreen: Color(hex: "81C784"),
        badRed: Color(hex: "EF5350"),
        neutralAmber: Color(hex: "FFD54F"),
        isDark: true
    )
}

// MARK: - Color hex extension

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (1, 1, 1, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

// MARK: - Environment key

private struct LichSoColorsKey: EnvironmentKey {
    static let defaultValue = LichSoColors.light
}

extension EnvironmentValues {
    var lichSoColors: LichSoColors {
        get { self[LichSoColorsKey.self] }
        set { self[LichSoColorsKey.self] = newValue }
    }
}
