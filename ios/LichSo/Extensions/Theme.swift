import SwiftUI

// ══════════════════════════════════════════
// LỊCH SỐ — Design System Colors v2
// Vietnamese Red & Gold — iOS
// ══════════════════════════════════════════

struct LichSoColors {
    let bg: Color
    let bg2: Color
    let bg3: Color
    let bg4: Color
    let surface: Color
    let surface2: Color
    let border: Color
    let gold: Color
    let gold2: Color
    let goldDim: Color
    let teal: Color
    let teal2: Color
    let tealDim: Color
    let red: Color
    let red2: Color
    let textPrimary: Color
    let textSecondary: Color
    let textTertiary: Color
    let textQuaternary: Color
    let noteGold: Color
    let noteTeal: Color
    let noteOrange: Color
    let notePurple: Color
    let noteGreen: Color
    let noteRed: Color
    let isDark: Bool
    let primary: Color
    let onPrimary: Color
    let primaryContainer: Color
    let onPrimaryContainer: Color
    let surfaceContainer: Color
    let surfaceContainerHigh: Color
    let outline: Color
    let outlineVariant: Color
    let deepRed: Color
    let goodGreen: Color
    let badRed: Color
    let neutralAmber: Color
}

// ── Dark palette ──
let DarkColors = LichSoColors(
    bg: Color(hex: 0x0F0E0C),
    bg2: Color(hex: 0x181610),
    bg3: Color(hex: 0x211F1A),
    bg4: Color(hex: 0x2A2720),
    surface: Color(hex: 0x2E2B23),
    surface2: Color(hex: 0x363228),
    border: Color(hex: 0xFFDC64).opacity(0.12),
    gold: Color(hex: 0xE8C84A),
    gold2: Color(hex: 0xF5D96E),
    goldDim: Color(hex: 0xE8C84A).opacity(0.18),
    teal: Color(hex: 0x4ABEAA),
    teal2: Color(hex: 0x62D4C0),
    tealDim: Color(hex: 0x4ABEAA).opacity(0.15),
    red: Color(hex: 0xEF5350),
    red2: Color(hex: 0xE57373),
    textPrimary: Color(hex: 0xF0E8D0),
    textSecondary: Color(hex: 0xB8AA88),
    textTertiary: Color(hex: 0x8A7E62),
    textQuaternary: Color(hex: 0x4A4435),
    noteGold: Color(hex: 0xE8C84A),
    noteTeal: Color(hex: 0x4ABEAA),
    noteOrange: Color(hex: 0xE8A06A),
    notePurple: Color(hex: 0xA084DC),
    noteGreen: Color(hex: 0x78C47A),
    noteRed: Color(hex: 0xE87070),
    isDark: true,
    primary: Color(hex: 0xEF5350),
    onPrimary: .white,
    primaryContainer: Color(hex: 0x3D1515),
    onPrimaryContainer: Color(hex: 0xFFDAD6),
    surfaceContainer: Color(hex: 0x2A2720),
    surfaceContainerHigh: Color(hex: 0x363228),
    outline: Color(hex: 0x9E9080),
    outlineVariant: Color(hex: 0x5A4F42),
    deepRed: Color(hex: 0xCF6679),
    goodGreen: Color(hex: 0x81C784),
    badRed: Color(hex: 0xEF5350),
    neutralAmber: Color(hex: 0xFFD54F)
)

// ── Light palette ──
let LightLichSoColors = LichSoColors(
    bg: Color(hex: 0xFFFBF5),
    bg2: Color(hex: 0xFFF8F0),
    bg3: Color(hex: 0xF5DDD8),
    bg4: Color(hex: 0xFFF0E8),
    surface: Color(hex: 0xFFF8F0),
    surface2: Color(hex: 0xF5DDD8),
    border: Color(hex: 0xD8C2BF).opacity(0.13),
    gold: Color(hex: 0xD4A017),
    gold2: Color(hex: 0xC6A300),
    goldDim: Color(hex: 0xD4A017).opacity(0.12),
    teal: Color(hex: 0x006C4C),
    teal2: Color(hex: 0x006C4C),
    tealDim: Color(hex: 0x006C4C).opacity(0.1),
    red: Color(hex: 0xB71C1C),
    red2: Color(hex: 0xC62828),
    textPrimary: Color(hex: 0x1C1B1F),
    textSecondary: Color(hex: 0x534340),
    textTertiary: Color(hex: 0x857371),
    textQuaternary: Color(hex: 0xD8C2BF),
    noteGold: Color(hex: 0xD4A017),
    noteTeal: Color(hex: 0x006C4C),
    noteOrange: Color(hex: 0xE65100),
    notePurple: Color(hex: 0x7B1FA2),
    noteGreen: Color(hex: 0x2E7D32),
    noteRed: Color(hex: 0xC62828),
    isDark: false,
    primary: Color(hex: 0xB71C1C),
    onPrimary: .white,
    primaryContainer: Color(hex: 0xFFDAD6),
    onPrimaryContainer: Color(hex: 0x410002),
    surfaceContainer: Color(hex: 0xFFF8F0),
    surfaceContainerHigh: Color(hex: 0xFFF0E8),
    outline: Color(hex: 0x857371),
    outlineVariant: Color(hex: 0xD8C2BF),
    deepRed: Color(hex: 0x8B0000),
    goodGreen: Color(hex: 0x2E7D32),
    badRed: Color(hex: 0xC62828),
    neutralAmber: Color(hex: 0xF57F17)
)

// ── Color hex initializer ──
extension Color {
    init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0,
            opacity: alpha
        )
    }
}

// ── Environment key for theme colors ──
private struct LichSoColorsKey: EnvironmentKey {
    static let defaultValue: LichSoColors = LightLichSoColors
}

extension EnvironmentValues {
    var lichSoColors: LichSoColors {
        get { self[LichSoColorsKey.self] }
        set { self[LichSoColorsKey.self] = newValue }
    }
}

// ── Convenience: access as @Environment(\.lichSoColors) ──
extension View {
    func lichSoTheme(isDark: Bool) -> some View {
        self.environment(\.lichSoColors, isDark ? DarkColors : LightLichSoColors)
    }
}

// ── Gradient helpers ──
extension LichSoColors {
    var headerGradient: LinearGradient {
        if isDark {
            LinearGradient(
                colors: [Color(hex: 0x5D1212), Color(hex: 0x4A1010)],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        } else {
            LinearGradient(
                colors: [primary, deepRed],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        }
    }

    var fabGradient: LinearGradient {
        if isDark {
            LinearGradient(
                colors: [Color(hex: 0x7F1D1D), Color(hex: 0x5D1212)],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        } else {
            LinearGradient(
                colors: [primary, Color(hex: 0xC62828)],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        }
    }
}
