import SwiftUI

struct QuizResultScreen: View {
    let result: SessionResult?
    let total: Int
    let correct: Int
    let onClose: () -> Void
    
    var body: some View {
        ZStack {
            LSTheme.bg.ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Top bar
                HStack {
                    Spacer()
                    Text("Kết quả")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(LSTheme.textPrimary)
                    Spacer()
                }
                .padding(.vertical, 16)
                
                ScrollView {
                    VStack(spacing: 20) {
                        // Score Hero
                        VStack(spacing: 8) {
                            Text("Kết quả của bạn")
                                .font(.system(size: 14))
                                .foregroundColor(.white.opacity(0.8))
                            
                            Text("\(correct) / \(total)")
                                .font(.system(size: 52, weight: .bold))
                                .foregroundColor(.white)
                            
                            if let title = result?.sessionTitle, !title.isEmpty {
                                Text(title)
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(LSTheme.gold)
                            } else {
                                Text(correct >= total / 2 ? "Xuất sắc!" : "Cố gắng hơn nhé!")
                                    .font(.system(size: 16, weight: .semibold))
                                    .foregroundColor(LSTheme.gold)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 24)
                        .background(
                            LinearGradient(
                                colors: [LSTheme.primary, LSTheme.deepRed],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .cornerRadius(20)
                        .padding(.horizontal, 16)
                        
                        // Points breakdown
                        if let res = result {
                            VStack(spacing: 10) {
                                HStack(spacing: 10) {
                                    pointChip(label: "\(res.scoreV2) điểm thành tích", color: LSTheme.primary, icon: "star.fill")
                                    pointChip(label: "+\(res.appPointsEarned) App Points", color: LSTheme.gold, icon: "sparkles")
                                }
                                HStack(spacing: 10) {
                                    pointChip(label: "+\(res.xpEarned) XP", color: LSTheme.teal, icon: "bolt.fill")
                                }
                            }
                            .padding(.horizontal, 16)
                        }
                        
                        // Action buttons
                        VStack(spacing: 12) {
                            Button(action: onClose) {
                                Text("Đóng")
                                    .font(.system(size: 15, weight: .bold))
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 14)
                                    .background(LSTheme.primary)
                                    .foregroundColor(.white)
                                    .cornerRadius(12)
                            }
                            
                            Button(action: shareResult) {
                                HStack {
                                    Image(systemName: "square.and.arrow.up")
                                    Text("Chia sẻ kết quả")
                                }
                                .font(.system(size: 15, weight: .semibold))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                                .background(LSTheme.surfaceContainer)
                                .foregroundColor(LSTheme.textPrimary)
                                .cornerRadius(12)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(LSTheme.outlineVariant.opacity(0.3), lineWidth: 1)
                                )
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                    .padding(.vertical, 16)
                }
            }
        }
    }
    
    private func pointChip(label: String, color: Color, icon: String) -> some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 12))
                .foregroundColor(color)
            Text(label)
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(color)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 10)
        .background(color.opacity(0.12))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(color.opacity(0.3), lineWidth: 1)
        )
    }
    
    private func shareResult() {
        let text = """
🏆 KẾT QUẢ THI ĐẤU LỊCH SỐ 🏆
Số câu đúng: \(correct)/\(total)
Điểm thành tích: \(result?.scoreV2 ?? correct * 10)
XP nhận được: +\(result?.xpEarned ?? correct * 10)

Chơi ngay ứng dụng Lịch Số để thi tài kiến thức lịch sử!
"""
        let activityVC = UIActivityViewController(activityItems: [text], applicationActivities: nil)
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = windowScene.windows.first?.rootViewController {
            rootVC.present(activityVC, animated: true)
        }
    }
}
