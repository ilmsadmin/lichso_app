import SwiftUI

// MARK: - LichSo Design System Colors

struct LichSoColors {
    let bg: Color
    let bg2: Color
    let bg3: Color
    let bg4: Color
    let surface: Color
    let surface2: Color
    let border: Color
    let borderSubtle: Color
    let gold: Color
    let gold2: Color
    let goldDim: Color
    let cyan: Color
    let cyan2: Color
    let cyanDim: Color
    let green: Color
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
    let panelBg: Color
    let shineTop: Color
    let speechBg: Color
    let inputBg: Color
    let isDark: Bool

    // Gradient for brand title
    var brandGradient: LinearGradient {
        LinearGradient(colors: [cyan, cyan2], startPoint: .leading, endPoint: .trailing)
    }
}

// MARK: - Dark Palette (matching mock: #0d0f14 base)
extension LichSoColors {
    static let dark = LichSoColors(
        bg:             Color(hex: 0x0D0F14),
        bg2:            Color(hex: 0x14182C).opacity(0.96),
        bg3:            Color(hex: 0x1A1E2C).opacity(0.92),
        bg4:            Color(hex: 0x222636),
        surface:        Color(hex: 0x2E2B23),
        surface2:       Color(hex: 0x363228),
        border:         Color.white.opacity(0.075),
        borderSubtle:   Color.white.opacity(0.042),
        gold:           Color(hex: 0xF5C842),
        gold2:          Color(hex: 0xF5C842),
        goldDim:        Color(hex: 0xF5C842).opacity(0.12),
        cyan:           Color(hex: 0x4ECDC4),
        cyan2:          Color(hex: 0x38BDF8),
        cyanDim:        Color(hex: 0x4ECDC4).opacity(0.12),
        green:          Color(hex: 0x34D399),
        teal:           Color(hex: 0x4ECDC4),
        teal2:          Color(hex: 0x38BDF8),
        tealDim:        Color(hex: 0x4ECDC4).opacity(0.15),
        red:            Color(hex: 0xD94F3B),
        red2:           Color(hex: 0xE8614E),
        textPrimary:    Color(hex: 0xEDF0F5),
        textSecondary:  Color(hex: 0x8590A4),
        textTertiary:   Color(hex: 0x434D60),
        textQuaternary: Color(hex: 0x2A3040),
        noteGold:       Color(hex: 0xF5C842),
        noteTeal:       Color(hex: 0x4ECDC4),
        noteOrange:     Color(hex: 0xE8A06A),
        notePurple:     Color(hex: 0xA084DC),
        noteGreen:      Color(hex: 0x34D399),
        noteRed:        Color(hex: 0xE87070),
        panelBg:        Color(hex: 0x141824).opacity(0.96),
        shineTop:       Color.white.opacity(0.05),
        speechBg:       Color(hex: 0x1A1E2C).opacity(0.95),
        inputBg:        Color(hex: 0x141824).opacity(0.96),
        isDark: true
    )
}

// MARK: - Light Palette (matching mock light theme)
extension LichSoColors {
    static let light = LichSoColors(
        bg:             Color(hex: 0xE8ECF2),
        bg2:            Color.white.opacity(0.88),
        bg3:            Color(hex: 0xF5F7FC).opacity(0.92),
        bg4:            Color(hex: 0xE8E4DB),
        surface:        Color(hex: 0xEAE6DD),
        surface2:       Color(hex: 0xDDD8CD),
        border:         Color.black.opacity(0.07),
        borderSubtle:   Color.black.opacity(0.05),
        gold:           Color(hex: 0xC99A00),
        gold2:          Color(hex: 0xC99A00),
        goldDim:        Color(hex: 0xC99A00).opacity(0.10),
        cyan:           Color(hex: 0x0AADA4),
        cyan2:          Color(hex: 0x0A8FD4),
        cyanDim:        Color(hex: 0x0AADA4).opacity(0.10),
        green:          Color(hex: 0x1AAB74),
        teal:           Color(hex: 0x0AADA4),
        teal2:          Color(hex: 0x0A8FD4),
        tealDim:        Color(hex: 0x0AADA4).opacity(0.10),
        red:            Color(hex: 0xC43D2B),
        red2:           Color(hex: 0xD04838),
        textPrimary:    Color(hex: 0x111318),
        textSecondary:  Color(hex: 0x5A6272),
        textTertiary:   Color(hex: 0x9AA0AE),
        textQuaternary: Color(hex: 0xB8AE98),
        noteGold:       Color(hex: 0xC99A00),
        noteTeal:       Color(hex: 0x0AADA4),
        noteOrange:     Color(hex: 0xCC7B3A),
        notePurple:     Color(hex: 0x7B5EB0),
        noteGreen:      Color(hex: 0x1AAB74),
        noteRed:        Color(hex: 0xCC4040),
        panelBg:        Color.white.opacity(0.88),
        shineTop:       Color.white.opacity(0.9),
        speechBg:       Color(hex: 0xE6EAF2).opacity(0.97),
        inputBg:        Color.white.opacity(0.88),
        isDark: false
    )
}

// MARK: - Environment Key
private struct LichSoColorsKey: EnvironmentKey {
    static let defaultValue: LichSoColors = .dark
}

extension EnvironmentValues {
    var lichSoColors: LichSoColors {
        get { self[LichSoColorsKey.self] }
        set { self[LichSoColorsKey.self] = newValue }
    }
}

// MARK: - Color Extension
extension Color {
    init(hex: UInt32) {
        let r = Double((hex >> 16) & 0xFF) / 255.0
        let g = Double((hex >> 8) & 0xFF) / 255.0
        let b = Double(hex & 0xFF) / 255.0
        self.init(red: r, green: g, blue: b)
    }

    func blended(with other: Color, fraction: Double) -> Color {
        // fallback
        return self
    }
}
