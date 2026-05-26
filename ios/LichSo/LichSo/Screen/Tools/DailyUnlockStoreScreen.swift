import SwiftUI

struct StoreItem: Identifiable {
    let id = UUID()
    let title: String
    let price: Int
    let description: String
    let iconName: String
    var isUnlocked: Bool = false
}

struct DailyUnlockStoreScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var items = [
        StoreItem(title: "Chọn ngày đại sự mở rộng", price: 50, description: "Mở rộng khoảng thời gian chấm điểm ngày tốt lên 60 ngày thay vì 15 ngày.", iconName: "calendar.badge.checkmark", isUnlocked: false),
        StoreItem(title: "Tử vi AI chi tiết cả năm", price: 100, description: "Nhận báo cáo phân tích vận mệnh chi tiết của năm nay từ chuyên gia AI.", iconName: "cpu", isUnlocked: false),
        StoreItem(title: "Mở khóa toàn bộ Cây gia phả", price: 80, description: "Quản lý và xuất sơ đồ phả hệ không giới hạn số lượng thành viên.", iconName: "person.3.fill", isUnlocked: true)
    ]
    
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
                        Text("Kho mở khóa tính năng")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Dùng điểm công đức mở khóa tính năng Premium")
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
                        
                        // User points display
                        HStack {
                            Text("Ví điểm công đức:")
                                .font(.system(size: 14))
                                .foregroundColor(LSTheme.textSecondary)
                            Spacer()
                            Text("185 ☘️")
                                .font(.system(size: 18, weight: .black))
                                .foregroundColor(LSTheme.primary)
                        }
                        .padding(.horizontal, 20)
                        .padding(.vertical, 14)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(12)
                        .padding(.horizontal, 16)
                        
                        // Shop items
                        ForEach(Array(items.enumerated()), id: \.element.id) { idx, item in
                            VStack(alignment: .leading, spacing: 10) {
                                HStack(spacing: 12) {
                                    ZStack {
                                        Circle()
                                            .fill(LSTheme.primary.opacity(0.12))
                                            .frame(width: 40, height: 40)
                                        Image(systemName: item.iconName)
                                            .foregroundColor(LSTheme.primary)
                                    }
                                    
                                    VStack(alignment: .leading, spacing: 3) {
                                        Text(item.title)
                                            .font(.system(size: 15, weight: .bold))
                                            .foregroundColor(LSTheme.textPrimary)
                                        Text(item.description)
                                            .font(.system(size: 12))
                                            .foregroundColor(LSTheme.textSecondary)
                                            .lineSpacing(3)
                                    }
                                }
                                
                                HStack {
                                    Spacer()
                                    if item.isUnlocked {
                                        Text("Đã mở khóa")
                                            .font(.system(size: 12, weight: .bold))
                                            .foregroundColor(LSTheme.goodGreen)
                                            .padding(.horizontal, 16)
                                            .padding(.vertical, 6)
                                            .background(LSTheme.goodGreen.opacity(0.12))
                                            .cornerRadius(12)
                                    } else {
                                        Button {
                                            items[idx].isUnlocked = true
                                        } label: {
                                            Text("Mở khóa - \(item.price) ☘️")
                                                .font(.system(size: 12, weight: .bold))
                                                .foregroundColor(.white)
                                                .padding(.horizontal, 16)
                                                .padding(.vertical, 6)
                                                .background(LSTheme.primary)
                                                .cornerRadius(12)
                                        }
                                    }
                                }
                            }
                            .padding(16)
                            .background(LSTheme.surfaceContainer)
                            .cornerRadius(16)
                            .overlay(
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(LSTheme.outlineVariant.opacity(0.3), lineWidth: 1)
                            )
                            .padding(.horizontal, 16)
                        }
                    }
                    .padding(.bottom, 24)
                }
            }
        }
        .navigationBarHidden(true)
    }
}
