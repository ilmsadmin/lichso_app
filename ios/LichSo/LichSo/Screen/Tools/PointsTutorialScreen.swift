import SwiftUI

struct PointsTutorialScreen: View {
    @Environment(\.dismiss) private var dismiss
    
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
                        Text("Hướng dẫn kiếm điểm")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Cách tích lũy điểm công đức ☘️")
                            .font(.system(size: 12))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    
                    Spacer()
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 8)
                .background(LSTheme.surfaceContainer)
                
                ScrollView {
                    VStack(spacing: 16) {
                        Spacer().frame(height: 10)
                        
                        VStack(spacing: 12) {
                            Image(systemName: "leaf.fill")
                                .font(.system(size: 50))
                                .foregroundColor(LSTheme.goodGreen)
                            
                            Text("Điểm công đức là gì?")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(LSTheme.textPrimary)
                            
                            Text("Là đơn vị điểm thưởng được tặng khi bạn thực hiện các hoạt động hữu ích, tích cực trong ứng dụng. Bạn có thể dùng điểm này để mở khóa các tính năng Premium hoặc mua Streak Freeze.")
                                .font(.system(size: 13))
                                .foregroundColor(LSTheme.textSecondary)
                                .multilineTextAlignment(.center)
                                .lineSpacing(4)
                                .padding(.horizontal, 16)
                        }
                        .padding(20)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                        
                        // Tasks list
                        VStack(alignment: .leading, spacing: 14) {
                            Text("Các nhiệm vụ hàng ngày")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(LSTheme.textPrimary)
                            
                            taskRow(title: "Check-in điểm danh mỗi ngày", reward: "+10 ☘️", desc: "Mở app mỗi ngày để tự động điểm danh nhận thưởng.")
                            taskRow(title: "Trả lời đúng câu hỏi đố vui", reward: "+5 ☘️ / câu", desc: "Tham gia thi đấu kiến thức lịch sử, văn hóa tại tab Đố Vui.")
                            taskRow(title: "Rút quẻ Kinh Dịch đầu ngày", reward: "+5 ☘️", desc: "Xem quẻ may mắn đầu ngày tại màn hình Tiện ích.")
                            taskRow(title: "Xem Tử vi AI vận mệnh", reward: "+15 ☘️", desc: "Trò chuyện, đặt câu hỏi về phong thủy với trợ lý AI.")
                        }
                        .padding(18)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                    }
                    .padding(.bottom, 24)
                }
            }
        }
        .navigationBarHidden(true)
    }
    
    private func taskRow(title: String, reward: String, desc: String) -> some View {
        HStack(alignment: .top, spacing: 12) {
            ZStack {
                Circle()
                    .fill(LSTheme.goodGreen.opacity(0.12))
                    .frame(width: 24, height: 24)
                Image(systemName: "checkmark")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(LSTheme.goodGreen)
            }
            
            VStack(alignment: .leading, spacing: 3) {
                HStack {
                    Text(title)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(LSTheme.textPrimary)
                    Spacer()
                    Text(reward)
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(LSTheme.goodGreen)
                }
                
                Text(desc)
                    .font(.system(size: 12))
                    .foregroundColor(LSTheme.textSecondary)
                    .lineSpacing(3)
            }
        }
    }
}
