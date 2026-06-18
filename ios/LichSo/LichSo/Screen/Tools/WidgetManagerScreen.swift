import SwiftUI

struct WidgetManagerScreen: View {
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
                        Text("Quản lý Widget")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Hướng dẫn cài đặt tiện ích màn hình chính")
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
                            Image(systemName: "square.grid.2x2.fill")
                                .font(.system(size: 48))
                                .foregroundColor(LSTheme.primary)
                            Text("Tiện ích màn hình chính")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(LSTheme.textPrimary)
                            Text("Xem lịch âm dương, giờ hoàng đạo và đếm ngược sự kiện trực tiếp từ màn hình nền điện thoại của bạn.")
                                .font(.system(size: 13))
                                .foregroundColor(LSTheme.textSecondary)
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 20)
                        }
                        .padding(20)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                        
                        // Steps
                        VStack(alignment: .leading, spacing: 14) {
                            Text("Cách thêm Widget vào iOS")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(LSTheme.textPrimary)
                            
                            stepRow(number: "1", text: "Tại màn hình chính, chạm và giữ một vùng trống cho đến khi các ứng dụng bắt đầu rung lắc.")
                            stepRow(number: "2", text: "Chạm vào nút Thêm (+) ở góc trên bên trái màn hình.")
                            stepRow(number: "3", text: "Tìm kiếm từ khóa \"Lịch Số\" trong danh sách ứng dụng hiển thị.")
                            stepRow(number: "4", text: "Chọn kích thước Widget bạn mong muốn (Nhỏ, Trung bình, Lớn) rồi nhấn \"Thêm tiện ích\".")
                            stepRow(number: "5", text: "Đặt widget vào vị trí phù hợp rồi nhấn \"Xong\" để hoàn thành.")
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
    
    private func stepRow(number: String, text: String) -> some View {
        HStack(alignment: .top, spacing: 12) {
            ZStack {
                Circle()
                    .fill(LSTheme.primary.opacity(0.15))
                    .frame(width: 24, height: 24)
                Text(number)
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(LSTheme.primary)
            }
            
            Text(text)
                .font(.system(size: 13))
                .foregroundColor(LSTheme.textSecondary)
                .lineSpacing(4)
        }
    }
}
