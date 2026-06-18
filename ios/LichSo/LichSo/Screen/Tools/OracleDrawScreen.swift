import SwiftUI

struct OracleStick: Identifiable {
    let id = UUID()
    let title: String
    let rating: String
    let meaning: String
}

struct OracleDrawScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var isDrawing = false
    @State private var drawnStick: OracleStick? = nil
    
    var onAskAiClick: (String) -> Void = { _ in }
    
    let sticks = [
        OracleStick(title: "Quẻ Số 1: Thuần Càn (Trời)", rating: "Đại Cát", meaning: "Tượng trưng cho sự khởi đầu rực rỡ, sức mạnh phi thường. Hãy tự tin triển khai các dự án mới."),
        OracleStick(title: "Quẻ Số 2: Thuần Khôn (Đất)", rating: "Cát", meaning: "Tương trưng cho sự bao dung, thuận theo tự nhiên. Cần kiên nhẫn tích lũy thay vì nóng vội phát triển."),
        OracleStick(title: "Quẻ Số 11: Địa Thiên Thái", rating: "Đại Cát", meaning: "Thời kỳ âm dương hòa hợp, mọi sự hanh thông. Thích hợp mưu cầu công danh sự nghiệp."),
        OracleStick(title: "Quẻ Số 12: Thiên Địa Bĩ", rating: "Hung", meaning: "Âm dương bế tắc, thời vận khó khăn. Nên phòng thủ, giữ vững vị trí cũ hơn là mở mang.")
    ]
    
    var body: some View {
        ZStack {
            LSTheme.bg.ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Top bar
                HStack {
                    Button { dismiss() } label: {
                        Image(systemName: "chevron.left")
                        .accessibilityLabel("Quay lại")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundColor(LSTheme.textPrimary)
                            .frame(width: 44, height: 44)
                    }
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Rút quẻ Kinh Dịch")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Gợi ý điềm cát hung đầu ngày")
                            .font(.system(size: 12))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    
                    Spacer()
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 8)
                .background(LSTheme.surfaceContainer)
                
                ScrollView {
                    VStack(spacing: 24) {
                        Spacer().frame(height: 10)
                        
                        if let stick = drawnStick {
                            // Show results
                            resultCard(stick)
                        } else {
                            // Show drawing view
                            drawingBoxView
                        }
                    }
                    .padding(.horizontal, 16)
                }
            }
        }
        .navigationBarHidden(true)
    }
    
    private var drawingBoxView: some View {
        VStack(spacing: 24) {
            ZStack {
                RoundedRectangle(cornerRadius: 20)
                    .fill(LSTheme.surfaceContainer)
                    .frame(height: 320)
                    .overlay(
                        VStack(spacing: 16) {
                            Image(systemName: "hand.sparkles.fill")
                                .font(.system(size: 60))
                                .foregroundColor(LSTheme.gold)
                                .rotationEffect(.degrees(isDrawing ? -15 : 15))
                                .animation(isDrawing ? Animation.linear(duration: 0.15).repeatForever(autoreverses: true) : .default, value: isDrawing)
                            
                            Text("Ống Quẻ Kinh Dịch")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(LSTheme.textPrimary)
                            
                            Text("Giữ tâm tịnh, suy nghĩ về việc cần hỏi trong ngày và nhấn nút rút quẻ.")
                                .font(.system(size: 13))
                                .foregroundColor(LSTheme.textSecondary)
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 24)
                        }
                    )
            }
            
            Button {
                triggerDrawing()
            } label: {
                Text(isDrawing ? "Đang gieo quẻ..." : "Bắt đầu rút quẻ")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 48)
                    .background(LSTheme.primary)
                    .cornerRadius(24)
            }
            .disabled(isDrawing)
        }
    }
    
    private func triggerDrawing() {
        isDrawing = true
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            isDrawing = false
            drawnStick = sticks.randomElement()
        }
    }
    
    private func resultCard(_ stick: OracleStick) -> some View {
        let isGood = stick.rating.contains("Cát")
        
        return VStack(spacing: 20) {
            VStack(spacing: 12) {
                Text("QUẺ ĐÃ RÚT ĐƯỢC")
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(LSTheme.textTertiary)
                
                Text(stick.title)
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(LSTheme.textPrimary)
                
                Text(stick.rating)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(isGood ? LSTheme.goodGreen : LSTheme.badRed)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 6)
                    .background(isGood ? LSTheme.goodGreen.opacity(0.12) : LSTheme.badRed.opacity(0.12))
                    .cornerRadius(12)
                
                Text(stick.meaning)
                    .font(.system(size: 14))
                    .foregroundColor(LSTheme.textSecondary)
                    .multilineTextAlignment(.center)
                    .lineSpacing(4)
                    .padding(.horizontal, 16)
            }
            .padding(.vertical, 24)
            .frame(maxWidth: .infinity)
            .background(LSTheme.surfaceContainer)
            .cornerRadius(20)
            
            HStack(spacing: 12) {
                Button {
                    drawnStick = nil
                } label: {
                    Text("Rút lại")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(LSTheme.textPrimary)
                        .frame(maxWidth: .infinity)
                        .frame(height: 44)
                        .background(LSTheme.surfaceContainerHigh)
                        .cornerRadius(22)
                }
                
                Button {
                    onAskAiClick("Hãy giải thích ý nghĩa của \(stick.title) (\(stick.rating)) chi tiết hơn cho việc làm trong ngày.")
                } label: {
                    Text("Hỏi Tử vi AI")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 44)
                        .background(LSTheme.primary)
                        .cornerRadius(22)
                }
            }
        }
    }
}
