import SwiftUI

// ═══════════════════════════════════════════
// Onboarding Screen — Port từ Android (1 trang welcome duy nhất).
// Trước đây iOS là flow nhiều trang + thu thập hồ sơ; nay rút gọn còn một trang
// chào mừng giống Android — phần hồ sơ chuyển sang màn Cá nhân, hướng dẫn tính
// năng thay bằng tour ngay trên Home, xin quyền thông báo do hệ thống tự hỏi.
// ═══════════════════════════════════════════

private let PrimaryRed = Color(hex: "B71C1C")
private let DeepRed = Color(hex: "8B0000")
private let GoldAccent = Color(hex: "D4A017")
private let SurfaceBg = Color(hex: "FFFBF5")
private let TextMain = Color(hex: "1C1B1F")
private let TextSub = Color(hex: "534340")

// Một trang welcome gộp các tính năng chính.
private struct FeatureChipData {
    let icon: String
    let label: String
}

private let featureChips: [FeatureChipData] = [
    FeatureChipData(icon: "calendar", label: "Lịch vạn niên"),
    FeatureChipData(icon: "calendar.badge.checkmark", label: "Ngày tốt xấu"),
    FeatureChipData(icon: "hands.sparkles.fill", label: "Văn khấn"),
    FeatureChipData(icon: "person.3.fill", label: "Gia phả"),
]

struct OnboardingScreen: View {
    let onFinish: () -> Void

    var body: some View {
        ZStack(alignment: .bottom) {
            SurfaceBg.ignoresSafeArea()

            // ── Nội dung trang ──
            VStack(spacing: 0) {
                Spacer().frame(height: 60)

                // Icon lớn + vòng trang trí
                ZStack {
                    Circle()
                        .fill(
                            RadialGradient(
                                colors: [PrimaryRed.opacity(0.12), Color.clear],
                                center: .center, startRadius: 0, endRadius: 90
                            )
                        )
                        .frame(width: 180, height: 180)

                    Circle()
                        .fill(LinearGradient(
                            colors: [PrimaryRed, Color(hex: "D32F2F")],
                            startPoint: .topLeading, endPoint: .bottomTrailing
                        ))
                        .frame(width: 120, height: 120)
                        .overlay(
                            Image(systemName: "calendar")
                                .font(.system(size: 50, weight: .medium))
                                .foregroundColor(.white)
                        )
                }

                Spacer().frame(height: 40)

                // Tiêu đề
                Text("Chào mừng đến Lịch Số")
                    .font(.system(size: 28, weight: .bold, design: .serif))
                    .foregroundColor(TextMain)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: 8)

                // Phụ đề
                Text("Lịch vạn niên · Phong thủy · Gia phả · AI")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(PrimaryRed)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: 6)

                // Đường vàng trang trí
                Rectangle()
                    .fill(LinearGradient(
                        colors: [Color.clear, GoldAccent, Color.clear],
                        startPoint: .leading, endPoint: .trailing
                    ))
                    .frame(width: 50, height: 2)

                Spacer().frame(height: 20)

                // Mô tả
                Text("Tất cả trong một ứng dụng: lịch âm dương, ngày tốt xấu, văn khấn, cây gia phả và trợ lý AI tử vi.")
                    .font(.system(size: 15))
                    .foregroundColor(TextSub)
                    .multilineTextAlignment(.center)
                    .lineSpacing(6)
                    .padding(.horizontal, 40)

                Spacer().frame(height: 28)

                // Chips tính năng (2 hàng x 2)
                VStack(spacing: 8) {
                    HStack(spacing: 8) {
                        ForEach(featureChips.prefix(2), id: \.label) { chip in
                            FeatureChip(icon: chip.icon, label: chip.label)
                        }
                    }
                    HStack(spacing: 8) {
                        ForEach(featureChips.suffix(2), id: \.label) { chip in
                            FeatureChip(icon: chip.icon, label: chip.label)
                        }
                    }
                }

                Spacer()
            }
            .padding(.horizontal, 32)

            // ── Nút bắt đầu ──
            Button(action: onFinish) {
                HStack(spacing: 8) {
                    Text("Bắt đầu sử dụng")
                        .font(.system(size: 16, weight: .bold))
                    Image(systemName: "arrow.right")
                        .font(.system(size: 16, weight: .semibold))
                }
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(LinearGradient(
                    colors: [PrimaryRed, DeepRed],
                    startPoint: .leading, endPoint: .trailing
                ))
                .clipShape(RoundedRectangle(cornerRadius: 28))
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
    }
}

private struct FeatureChip: View {
    let icon: String
    let label: String
    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundColor(PrimaryRed)
            Text(label)
                .font(.system(size: 12, weight: .medium))
                .foregroundColor(PrimaryRed)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 8)
        .background(PrimaryRed.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(PrimaryRed.opacity(0.15), lineWidth: 1)
        )
    }
}

#Preview {
    OnboardingScreen(onFinish: {})
}
