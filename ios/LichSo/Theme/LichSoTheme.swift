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
}

// MARK: - Dark Palette
extension LichSoColors {
    static let dark = LichSoColors(
        bg:             Color(hex: 0x0F0E0C),
        bg2:            Color(hex: 0x181610),
        bg3:            Color(hex: 0x211F1A),
        bg4:            Color(hex: 0x2A2720),
        surface:        Color(hex: 0x2E2B23),
        surface2:       Color(hex: 0x363228),
        border:         Color(white: 1.0, opacity: 0.11).blended(with: Color(hex: 0xFFDC64), fraction: 0.5),
        gold:           Color(hex: 0xE8C84A),
        gold2:          Color(hex: 0xF5D96E),
        goldDim:        Color(hex: 0xE8C84A).opacity(0.18),
        teal:           Color(hex: 0x4ABEAA),
        teal2:          Color(hex: 0x62D4C0),
        tealDim:        Color(hex: 0x4ABEAA).opacity(0.15),
        red:            Color(hex: 0xD94F3B),
        red2:           Color(hex: 0xE8614E),
        textPrimary:    Color(hex: 0xF0E8D0),
        textSecondary:  Color(hex: 0xB8AA88),
        textTertiary:   Color(hex: 0x7A6E52),
        textQuaternary: Color(hex: 0x4A4435),
        noteGold:       Color(hex: 0xE8C84A),
        noteTeal:       Color(hex: 0x4ABEAA),
        noteOrange:     Color(hex: 0xE8A06A),
        notePurple:     Color(hex: 0xA084DC),
        noteGreen:      Color(hex: 0x78C47A),
        noteRed:        Color(hex: 0xE87070),
        isDark: true
    )
}

// MARK: - Light Palette
extension LichSoColors {
    static let light = LichSoColors(
        bg:             Color(hex: 0xF8F6F1),
        bg2:            Color(hex: 0xFFFFFF),
        bg3:            Color(hex: 0xF0EDE6),
        bg4:            Color(hex: 0xE8E4DB),
        surface:        Color(hex: 0xEAE6DD),
        surface2:       Color(hex: 0xDDD8CD),
        border:         Color(hex: 0x8B7A4A).opacity(0.10),
        gold:           Color(hex: 0xC4A020),
        gold2:          Color(hex: 0xAA8A10),
        goldDim:        Color(hex: 0xC4A020).opacity(0.12),
        teal:           Color(hex: 0x2E9A88),
        teal2:          Color(hex: 0x1E8070),
        tealDim:        Color(hex: 0x2E9A88).opacity(0.10),
        red:            Color(hex: 0xC43D2B),
        red2:           Color(hex: 0xD04838),
        textPrimary:    Color(hex: 0x1A1710),
        textSecondary:  Color(hex: 0x5C5340),
        textTertiary:   Color(hex: 0x8A7F68),
        textQuaternary: Color(hex: 0xB8AE98),
        noteGold:       Color(hex: 0xC4A020),
        noteTeal:       Color(hex: 0x2E9A88),
        noteOrange:     Color(hex: 0xCC7B3A),
        notePurple:     Color(hex: 0x7B5EB0),
        noteGreen:      Color(hex: 0x4A9C4E),
        noteRed:        Color(hex: 0xCC4040),
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
