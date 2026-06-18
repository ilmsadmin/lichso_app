import SwiftUI

struct LedgerTransaction: Identifiable {
    let id = UUID()
    let title: String
    let amount: Int
    let dateStr: String
}

struct LedgerScreen: View {
    @Environment(\.dismiss) private var dismiss
    
    let list = [
        LedgerTransaction(title: "Check-in điểm danh ngày mới", amount: 10, dateStr: "Hôm nay"),
        LedgerTransaction(title: "Hoàn thành Đố vui Kiến thức", amount: 20, dateStr: "Hôm nay"),
        LedgerTransaction(title: "Rút quẻ Kinh Dịch đầu ngày", amount: 5, dateStr: "Hôm qua"),
        LedgerTransaction(title: "Chia sẻ app nhận quà", amount: 50, dateStr: "3 ngày trước")
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
                        Text("Nhật ký điểm công đức")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Xem lịch sử biến động điểm công đức")
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
                        
                        // Points wallet card
                        VStack(spacing: 6) {
                            Text("ĐIỂM CÔNG ĐỨC HIỆN TẠI")
                                .font(.system(size: 11, weight: .semibold))
                                .foregroundColor(LSTheme.textTertiary)
                            
                            Text("185 ☘️")
                                .font(.system(size: 34, weight: .black))
                                .foregroundColor(LSTheme.primary)
                            
                            Text("Hạng hiện tại: Cư Sĩ")
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(LSTheme.textSecondary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(20)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                        
                        // Transaction list
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Lịch sử giao dịch")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(LSTheme.textPrimary)
                                .padding(.horizontal, 16)
                            
                            ForEach(list) { tx in
                                HStack {
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text(tx.title)
                                            .font(.system(size: 14, weight: .bold))
                                            .foregroundColor(LSTheme.textPrimary)
                                        Text(tx.dateStr)
                                            .font(.system(size: 11))
                                            .foregroundColor(LSTheme.textTertiary)
                                    }
                                    
                                    Spacer()
                                    
                                    Text(tx.amount > 0 ? "+\(tx.amount)" : "\(tx.amount)")
                                        .font(.system(size: 15, weight: .bold))
                                        .foregroundColor(tx.amount > 0 ? LSTheme.goodGreen : LSTheme.badRed)
                                }
                                .padding(14)
                                .background(LSTheme.surfaceContainer)
                                .cornerRadius(12)
                                .padding(.horizontal, 16)
                            }
                        }
                    }
                    .padding(.bottom, 24)
                }
            }
        }
        .navigationBarHidden(true)
    }
}
