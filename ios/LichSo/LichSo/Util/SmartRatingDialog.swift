import SwiftUI
import StoreKit
import UIKit

// ═══════════════════════════════════════════
// SmartRatingDialog — Dialog xin đánh giá thông minh
//
// 3 nhánh:
//   [emotion] → hỏi cảm xúc chung
//       → "Rất hài lòng" → [stars] chọn 1–5 sao
//           → 4–5 sao → StoreKit In-App Review
//           → 1–3 sao → [feedback] form gửi mail
//       → "Chưa hài lòng" → [feedback] form gửi mail
//   [feedback] → nhập góp ý → gửi email
//   [thanks]   → cảm ơn, tự đóng sau 2.5s
// ═══════════════════════════════════════════

// MARK: - Colors

private let PrimaryRed  = Color(hex: "B71C1C")
private let GoldAccent  = Color(hex: "D4A017")
private let SurfaceBg   = Color(hex: "FFFBF5")
private let TextMain    = Color(hex: "1C1B1F")
private let TextSub     = Color(hex: "534340")
private let TextDim     = Color(hex: "857371")
private let OutlineClr  = Color(hex: "D8C2BF")

struct SmartRatingDialog: View {
    @ObservedObject var ratingManager: SmartRatingManager
    @Environment(\.requestReview) private var requestReview

    @State private var step: String = "emotion"
    @State private var feedbackText: String = ""
    @State private var selectedStars: Int = 0

    var body: some View {
        if ratingManager.shouldShow {
            ZStack {
                // Dimmed backdrop
                Color.black.opacity(0.45)
                    .ignoresSafeArea()
                    .onTapGesture { dismiss() }

                // Dialog content
                dialogContent
                    .padding(.horizontal, 20)
                    .transition(.scale(scale: 0.9).combined(with: .opacity))
            }
            .animation(.spring(response: 0.35, dampingFraction: 0.8), value: step)
            .onAppear {
                ratingManager.recordShown()
            }
        }
    }

    @ViewBuilder
    private var dialogContent: some View {
        switch step {
        case "emotion":
            EmotionStepView(
                onHappy: { step = "stars" },
                onUnhappy: { step = "feedback" },
                onDismiss: { dismiss() }
            )
        case "stars":
            StarsStepView(
                selectedStars: $selectedStars,
                onConfirm: { stars in
                    if stars >= 4 {
                        ratingManager.recordRated()
                        requestReview()
                    } else {
                        step = "feedback"
                    }
                },
                onDismiss: { dismiss() }
            )
        case "feedback":
            FeedbackStepView(
                feedbackText: $feedbackText,
                selectedStars: selectedStars,
                onSend: { step = "thanks" },
                onSkip: { dismiss() }
            )
        case "thanks":
            ThanksStepView(onDismiss: { dismiss() })
        default:
            EmptyView()
        }
    }

    private func dismiss() {
        ratingManager.dismiss()
        // Reset state for next time
        step = "emotion"
        feedbackText = ""
        selectedStars = 0
    }
}

// ══════════════════════════════════════════
// STEP 1 — Hỏi cảm xúc
// ══════════════════════════════════════════

private struct EmotionStepView: View {
    let onHappy: () -> Void
    let onUnhappy: () -> Void
    let onDismiss: () -> Void

    @State private var starPhase: CGFloat = 0

    var body: some View {
        VStack(spacing: 0) {
            // Dismiss X
            HStack {
                Spacer()
                Button(action: onDismiss) {
                    Image(systemName: "xmark")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(TextDim)
                        .frame(width: 32, height: 32)
                }
            }

            // Animated stars row
            HStack(spacing: 4) {
                ForEach(0..<5, id: \.self) { i in
                    Image(systemName: "star.fill")
                        .font(.system(size: 28))
                        .foregroundColor(GoldAccent)
                        .scaleEffect(1.0 + 0.12 * sin(starPhase + Double(i) * 0.6))
                }
            }
            .onAppear {
                withAnimation(.linear(duration: 2.5).repeatForever(autoreverses: false)) {
                    starPhase = .pi * 2
                }
            }

            Spacer().frame(height: 16)

            Text("Bạn có hài lòng\nvới Lịch Số không?")
                .font(.system(size: 22, weight: .bold, design: .serif))
                .foregroundColor(TextMain)
                .multilineTextAlignment(.center)
                .lineSpacing(4)

            Spacer().frame(height: 8)

            Text("Chỉ mất 10 giây — đánh giá của bạn giúp chúng tôi cải thiện ứng dụng tốt hơn mỗi ngày 🙏")
                .font(.system(size: 13))
                .foregroundColor(TextSub)
                .multilineTextAlignment(.center)
                .lineSpacing(4)
                .padding(.horizontal, 8)

            Spacer().frame(height: 28)

            // 2 buttons
            HStack(spacing: 12) {
                // Chưa hài lòng
                Button(action: onUnhappy) {
                    VStack(spacing: 2) {
                        Text("😕").font(.system(size: 20))
                        Text("Chưa hài lòng")
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(TextSub)
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 52)
                    .background(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(OutlineClr, lineWidth: 1)
                    )
                }
                .buttonStyle(.plain)

                // Rất hài lòng
                Button(action: onHappy) {
                    VStack(spacing: 2) {
                        Text("😍").font(.system(size: 20))
                        Text("Rất hài lòng!")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(Color(hex: "5D3A00"))
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 52)
                    .background(
                        LinearGradient(
                            colors: [GoldAccent, Color(hex: "F5CC3A")],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                }
                .buttonStyle(.plain)
            }

            Spacer().frame(height: 12)

            Button("Bỏ qua") { onDismiss() }
                .font(.system(size: 12))
                .foregroundColor(TextDim)
                .padding(.horizontal, 16)
                .padding(.vertical, 6)
        }
        .padding(28)
        .background(
            RoundedRectangle(cornerRadius: 24)
                .fill(SurfaceBg)
                .shadow(color: .black.opacity(0.15), radius: 12, y: 6)
        )
    }
}

// ══════════════════════════════════════════
// STEP 2 — Chọn số sao (1–5)
// ══════════════════════════════════════════

private struct StarsStepView: View {
    @Binding var selectedStars: Int
    let onConfirm: (Int) -> Void
    let onDismiss: () -> Void

    private let starLabels = ["Rất tệ", "Không tốt", "Tạm được", "Khá tốt", "Xuất sắc"]
    private let starColors: [Color] = [
        Color(hex: "E53935"),  // 1★ – đỏ
        Color(hex: "FF7043"),  // 2★ – cam đỏ
        Color(hex: "FFB300"),  // 3★ – vàng tối
        Color(hex: "7CB342"),  // 4★ – xanh lá
        Color(hex: "43A047"),  // 5★ – xanh lá đậm
    ]

    var body: some View {
        VStack(spacing: 0) {
            // Dismiss X
            HStack {
                Spacer()
                Button(action: onDismiss) {
                    Image(systemName: "xmark")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(TextDim)
                        .frame(width: 32, height: 32)
                }
            }

            Text("⭐").font(.system(size: 48))

            Spacer().frame(height: 12)

            Text("Bạn đánh giá Lịch Số\nbao nhiêu sao?")
                .font(.system(size: 21, weight: .bold, design: .serif))
                .foregroundColor(TextMain)
                .multilineTextAlignment(.center)
                .lineSpacing(4)

            Spacer().frame(height: 6)

            Text(subtitleText)
                .font(.system(size: 13))
                .foregroundColor(TextSub)
                .multilineTextAlignment(.center)
                .lineSpacing(4)
                .padding(.horizontal, 8)

            Spacer().frame(height: 24)

            // Star row
            HStack(spacing: 8) {
                ForEach(1...5, id: \.self) { star in
                    let isFilled = star <= selectedStars
                    let color = isFilled && selectedStars > 0
                        ? starColors[selectedStars - 1]
                        : Color(hex: "D0C4C0")

                    Image(systemName: "star.fill")
                        .font(.system(size: 38))
                        .foregroundColor(color)
                        .scaleEffect(isFilled ? 1.25 : 1.0)
                        .animation(.spring(response: 0.3, dampingFraction: 0.5), value: selectedStars)
                        .onTapGesture { selectedStars = star }
                }
            }

            // Star label
            if selectedStars > 0 {
                Spacer().frame(height: 8)
                Text(starLabels[selectedStars - 1])
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(starColors[selectedStars - 1])
                    .transition(.opacity)
            }

            Spacer().frame(height: 24)

            // Confirm button
            Button(action: { if selectedStars > 0 { onConfirm(selectedStars) } }) {
                Text(confirmButtonText)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(
                        RoundedRectangle(cornerRadius: 16)
                            .fill(selectedStars >= 4
                                  ? Color(hex: "43A047")
                                  : (selectedStars > 0 ? PrimaryRed : OutlineClr))
                    )
            }
            .buttonStyle(.plain)
            .disabled(selectedStars == 0)

            Spacer().frame(height: 8)

            Button("Bỏ qua") { onDismiss() }
                .font(.system(size: 12))
                .foregroundColor(TextDim)
                .padding(.horizontal, 16)
                .padding(.vertical, 6)
        }
        .padding(28)
        .background(
            RoundedRectangle(cornerRadius: 24)
                .fill(SurfaceBg)
                .shadow(color: .black.opacity(0.15), radius: 12, y: 6)
        )
    }

    private var subtitleText: String {
        if selectedStars >= 4 {
            return "Cảm ơn bạn! Đánh giá của bạn trên App Store sẽ giúp nhiều người khám phá Lịch Số 🙏"
        } else if selectedStars >= 1 {
            return "Chúng tôi muốn lắng nghe để cải thiện tốt hơn cho bạn 💬"
        } else {
            return "Chạm vào ngôi sao để chọn mức đánh giá của bạn"
        }
    }

    private var confirmButtonText: String {
        if selectedStars == 0 {
            return "Chọn số sao để tiếp tục"
        } else if selectedStars >= 4 {
            return "⭐ Đánh giá ngay"
        } else {
            return "💬 Gửi phản hồi cho chúng tôi"
        }
    }
}

// ══════════════════════════════════════════
// STEP 3 — Form phản hồi
// ══════════════════════════════════════════

private struct FeedbackStepView: View {
    @Binding var feedbackText: String
    let selectedStars: Int
    let onSend: () -> Void
    let onSkip: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            // Icon
            ZStack {
                Circle()
                    .fill(Color(hex: "FFF3E0"))
                    .frame(width: 60, height: 60)
                Image(systemName: "exclamationmark.bubble.fill")
                    .font(.system(size: 26))
                    .foregroundColor(Color(hex: "E65100"))
            }

            Spacer().frame(height: 16)

            Text("Hãy cho chúng tôi\nbiết vấn đề của bạn")
                .font(.system(size: 20, weight: .bold, design: .serif))
                .foregroundColor(TextMain)
                .multilineTextAlignment(.center)
                .lineSpacing(4)

            Spacer().frame(height: 6)

            Text("Phản hồi của bạn sẽ được gửi thẳng đến đội phát triển và được xử lý trong vòng 24 giờ")
                .font(.system(size: 12))
                .foregroundColor(TextSub)
                .multilineTextAlignment(.center)
                .lineSpacing(4)
                .padding(.horizontal, 4)

            Spacer().frame(height: 20)

            // Text field
            ZStack(alignment: .topLeading) {
                RoundedRectangle(cornerRadius: 14)
                    .stroke(OutlineClr, lineWidth: 1)
                    .frame(height: 140)

                if feedbackText.isEmpty {
                    Text("Bạn gặp khó khăn gì? Tính năng nào chưa tốt? Bạn mong muốn điều gì?")
                        .font(.system(size: 13))
                        .foregroundColor(TextDim)
                        .lineSpacing(4)
                        .padding(14)
                }

                TextEditor(text: $feedbackText)
                    .font(.system(size: 14))
                    .foregroundColor(TextMain)
                    .scrollContentBackground(.hidden)
                    .frame(height: 140)
                    .padding(8)
            }

            Spacer().frame(height: 6)

            // Email hint
            HStack(spacing: 4) {
                Image(systemName: "envelope.fill")
                    .font(.system(size: 12))
                    .foregroundColor(TextDim)
                Text("Phản hồi gửi tới: zenixhq.com@gmail.com")
                    .font(.system(size: 11))
                    .foregroundColor(TextDim)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            Spacer().frame(height: 20)

            // Buttons
            HStack(spacing: 12) {
                Button(action: onSkip) {
                    Text("Bỏ qua")
                        .font(.system(size: 14))
                        .foregroundColor(TextSub)
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(
                            RoundedRectangle(cornerRadius: 14)
                                .stroke(OutlineClr, lineWidth: 1)
                        )
                }
                .buttonStyle(.plain)

                Button(action: {
                    sendFeedbackEmail(feedback: feedbackText, stars: selectedStars)
                    onSend()
                }) {
                    HStack(spacing: 5) {
                        Image(systemName: "paperplane.fill")
                            .font(.system(size: 13))
                        Text("Gửi phản hồi")
                            .font(.system(size: 13, weight: .bold))
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 48)
                    .background(
                        RoundedRectangle(cornerRadius: 14)
                            .fill(feedbackText.trimmingCharacters(in: .whitespaces).isEmpty
                                  ? OutlineClr : PrimaryRed)
                    )
                }
                .buttonStyle(.plain)
                .disabled(feedbackText.trimmingCharacters(in: .whitespaces).isEmpty)
            }
        }
        .padding(28)
        .background(
            RoundedRectangle(cornerRadius: 24)
                .fill(SurfaceBg)
                .shadow(color: .black.opacity(0.15), radius: 12, y: 6)
        )
    }
}

// ══════════════════════════════════════════
// STEP 4 — Cảm ơn
// ══════════════════════════════════════════

private struct ThanksStepView: View {
    let onDismiss: () -> Void
    @State private var heartScale: CGFloat = 0.9

    var body: some View {
        VStack(spacing: 0) {
            Text("🙏")
                .font(.system(size: 52))
                .scaleEffect(heartScale)
                .onAppear {
                    withAnimation(.easeInOut(duration: 0.7).repeatForever(autoreverses: true)) {
                        heartScale = 1.1
                    }
                }

            Spacer().frame(height: 16)

            Text("Cảm ơn bạn rất nhiều!")
                .font(.system(size: 22, weight: .bold, design: .serif))
                .foregroundColor(TextMain)
                .multilineTextAlignment(.center)

            Spacer().frame(height: 8)

            Text("Phản hồi của bạn đã được gửi. Chúng tôi sẽ nỗ lực cải thiện để mang lại trải nghiệm tốt nhất.")
                .font(.system(size: 13))
                .foregroundColor(TextSub)
                .multilineTextAlignment(.center)
                .lineSpacing(4)
                .padding(.horizontal, 8)

            Spacer().frame(height: 20)

            Button("Đóng") { onDismiss() }
                .font(.system(size: 13))
                .foregroundColor(TextDim)
        }
        .padding(36)
        .background(
            RoundedRectangle(cornerRadius: 24)
                .fill(SurfaceBg)
                .shadow(color: .black.opacity(0.15), radius: 12, y: 6)
        )
        .onAppear {
            // Auto-dismiss sau 2.5 giây
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                onDismiss()
            }
        }
    }
}

// ══════════════════════════════════════════
// Helpers
// ══════════════════════════════════════════

private func sendFeedbackEmail(feedback: String, stars: Int) {
    let starText = stars > 0
        ? "\(String(repeating: "⭐", count: stars)) (\(stars)/5 sao)"
        : "Không chọn"
    let subject = "[Lịch Số] Phản hồi – \(starText)"

    let device = UIDevice.current
    let body = """
    \(feedback)
    
    ---
    Đánh giá: \(starText)
    Thiết bị: \(device.model) (\(device.systemName) \(device.systemVersion))
    App: Lịch Số \(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "?") (\(Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "?"))
    """

    let to = "zenixhq.com@gmail.com"
    guard let encodedSubject = subject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
          let encodedBody = body.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
          let url = URL(string: "mailto:\(to)?subject=\(encodedSubject)&body=\(encodedBody)") else {
        return
    }

    UIApplication.shared.open(url)
}
