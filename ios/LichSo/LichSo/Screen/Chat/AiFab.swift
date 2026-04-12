import SwiftUI

// ══════════════════════════════════════════
// AI FAB — Shared Floating Action Button
// Opens AI Chat from any tab
// ══════════════════════════════════════════

private var PrimaryRed: Color { LSTheme.primary }
private var GoldAccent: Color { LSTheme.gold }
private var SurfaceBg: Color { LSTheme.bg }

struct AiFab: View {
    var onTap: () -> Void = {}
    var body: some View {
        Button(action: onTap) {
            ZStack(alignment: .topTrailing) {
                Circle()
                    .fill(
                        LinearGradient(
                            colors: [Color(hex: "D32F2F"), PrimaryRed],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 50, height: 50)
                    .shadow(color: PrimaryRed.opacity(0.3), radius: 8, y: 4)
                    .overlay(
                        Image(systemName: "sparkles")
                            .font(.system(size: 22))
                            .foregroundColor(.white)
                    )

                Text("AI")
                    .font(.system(size: 7.5, weight: .heavy))
                    .foregroundColor(.white)
                    .padding(.horizontal, 5)
                    .padding(.vertical, 2)
                    .background(GoldAccent)
                    .clipShape(RoundedRectangle(cornerRadius: 6))
                    .overlay(RoundedRectangle(cornerRadius: 6).stroke(SurfaceBg, lineWidth: 1.5))
                    .offset(x: 5, y: -3)
            }
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    AiFab()
}
