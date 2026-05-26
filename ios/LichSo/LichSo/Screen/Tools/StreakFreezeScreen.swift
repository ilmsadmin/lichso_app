import SwiftUI

struct StreakFreezeScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var freezesOwned = 1
    
    var body: some View {
        ZStack {
            LSTheme.bg.ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Top bar
                HStack {
                    Button { dismiss() } label: {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundColor(LSTheme.textPrimary)
                            .frame(width: 44, height: 44)
                    }
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Đóng băng streak")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Giữ chuỗi ngày điểm danh liên tục")
                            .font(.system(size: 12))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    
                    Spacer()
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 8)
                .background(LSTheme.surfaceContainer)
                
                ScrollView {
                    VStack(spacing: 20) {
                        Spacer().frame(height: 10)
                        
                        // Current status card
                        VStack(spacing: 12) {
                            Image(systemName: "snowflake")
                                .font(.system(size: 64))
                                .foregroundColor(LSTheme.primary)
                            
                            Text("Số lượng khiên sở hữu")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(LSTheme.textSecondary)
                            
                            Text("\(freezesOwned) khiên")
                                .font(.system(size: 28, weight: .black))
                                .foregroundColor(LSTheme.primary)
                            
                            Text("Khiên đóng băng sẽ tự động được kích hoạt nếu bạn quên check-in điểm danh trong ngày, giúp bảo toàn chuỗi streak điểm danh liên tiếp.")
                                .font(.system(size: 12))
                                .foregroundColor(LSTheme.textTertiary)
                                .multilineTextAlignment(.center)
                                .lineSpacing(4)
                                .padding(.horizontal, 16)
                        }
                        .padding(24)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                        
                        // Shop card
                        VStack(alignment: .leading, spacing: 14) {
                            Text("Cửa hàng mua khiên")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(LSTheme.textPrimary)
                            
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text("Mua 1 Khiên đóng băng")
                                        .font(.system(size: 14, weight: .bold))
                                        .foregroundColor(LSTheme.textPrimary)
                                    Text("Giá: 30 ☘️")
                                        .font(.system(size: 13, weight: .bold))
                                        .foregroundColor(LSTheme.primary)
                                }
                                
                                Spacer()
                                
                                Button {
                                    freezesOwned += 1
                                } label: {
                                    Text("Mua ngay")
                                        .font(.system(size: 13, weight: .bold))
                                        .foregroundColor(.white)
                                        .padding(.horizontal, 16)
                                        .padding(.vertical, 8)
                                        .background(LSTheme.primary)
                                        .cornerRadius(16)
                                }
                            }
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
}
