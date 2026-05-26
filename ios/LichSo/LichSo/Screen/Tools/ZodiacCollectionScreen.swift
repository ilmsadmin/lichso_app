import SwiftUI

struct ZodiacCard: Identifiable {
    let id = UUID()
    let name: String
    let emoji: String
    let title: String
    let description: String
}

struct ZodiacCollectionScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var collectedIds: Set<String> = ["Tý", "Sửu", "Dần"]
    @State private var showDrawResult = false
    @State private var lastDrawn: ZodiacCard? = nil
    
    let allCards = [
        ZodiacCard(name: "Tý", emoji: "🐭", title: "Chuột Vàng", description: "Thông minh, nhanh nhẹn, luôn tiên phong."),
        ZodiacCard(name: "Sửu", emoji: "🐮", title: "Trâu Cát", description: "Cần cù, chăm chỉ, bền bỉ bền vững."),
        ZodiacCard(name: "Dần", emoji: "🐯", title: "Hổ Uy", description: "Dũng cảm, quyết đoán, khí phách phi thường."),
        ZodiacCard(name: "Mão", emoji: "🐱", title: "Mèo Lộc", description: "Ôn hòa, khéo léo, chiêu tài đón lộc."),
        ZodiacCard(name: "Thìn", emoji: "🐲", title: "Rồng Thiêng", description: "Quyền lực, may mắn, mây mưa thuận hòa."),
        ZodiacCard(name: "Tỵ", emoji: "🐍", title: "Rắn Trí", description: "Sâu sắc, kiên định, trực giác nhạy bén."),
        ZodiacCard(name: "Ngọ", emoji: "🐴", title: "Ngựa Phi", description: "Tự do, nhiệt huyết, mã đáo thành công."),
        ZodiacCard(name: "Mùi", emoji: "🐑", title: "Dê Lành", description: "Hiền hòa, hiếu thảo, an nhàn tự tại."),
        ZodiacCard(name: "Thân", emoji: "🐵", title: "Khỉ Linh", description: "Linh hoạt, tài trí, ứng biến nhanh chóng."),
        ZodiacCard(name: "Dậu", emoji: "🐔", title: "Gà Minh", description: "Đúng giờ, trung thành, tiếng gáy báo sáng."),
        ZodiacCard(name: "Tuất", emoji: "🐶", title: "Chó Nghĩa", description: "Trung nghĩa, bảo vệ, gia đình bình an."),
        ZodiacCard(name: "Hợi", emoji: "🐷", title: "Lợn Phúc", description: "Sung túc, an nhàn, tài lộc dồi dào.")
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
                        Text("Sưu tập 12 con giáp")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Rút thẻ ngày & Đổi điểm lấy ⚡")
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
                        
                        // Collection counter card
                        VStack(spacing: 10) {
                            Text("BỘ SƯU TẬP CỦA BẠN")
                                .font(.system(size: 11, weight: .semibold))
                                .foregroundColor(LSTheme.textTertiary)
                            
                            Text("\(collectedIds.count) / 12 con giáp")
                                .font(.system(size: 24, weight: .black))
                                .foregroundColor(LSTheme.primary)
                            
                            Button {
                                drawCard()
                            } label: {
                                Text("Rút thẻ miễn phí (1 lần/ngày)")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 20)
                                    .padding(.vertical, 8)
                                    .background(LSTheme.primary)
                                    .cornerRadius(20)
                            }
                            .padding(.top, 4)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(20)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                        
                        // Grid of 12 zodiacs
                        let columns = [
                            GridItem(.flexible(), spacing: 12),
                            GridItem(.flexible(), spacing: 12),
                            GridItem(.flexible(), spacing: 12)
                        ]
                        
                        LazyVGrid(columns: columns, spacing: 12) {
                            ForEach(allCards) { card in
                                let isCollected = collectedIds.contains(card.name)
                                VStack(spacing: 6) {
                                    Text(card.emoji)
                                        .font(.system(size: 36))
                                        .grayscale(isCollected ? 0 : 1)
                                        .opacity(isCollected ? 1 : 0.4)
                                    
                                    Text(card.name)
                                        .font(.system(size: 13, weight: .semibold))
                                        .foregroundColor(isCollected ? LSTheme.textPrimary : LSTheme.textSecondary)
                                    
                                    Text(isCollected ? "Đã có" : "Chưa có")
                                        .font(.system(size: 10))
                                        .foregroundColor(isCollected ? LSTheme.goodGreen : LSTheme.textTertiary)
                                }
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                                .background(LSTheme.surfaceContainer)
                                .cornerRadius(12)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(isCollected ? LSTheme.primary : LSTheme.outlineVariant.opacity(0.3), lineWidth: isCollected ? 1.5 : 1)
                                )
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                    .padding(.bottom, 24)
                }
            }
        }
        .navigationBarHidden(true)
        .sheet(isPresented: $showDrawResult) {
            if let card = lastDrawn {
                drawResultSheet(card)
            }
        }
    }
    
    private func drawCard() {
        let randomCard = allCards.randomElement()!
        lastDrawn = randomCard
        collectedIds.insert(randomCard.name)
        showDrawResult = true
    }
    
    private func drawResultSheet(_ card: ZodiacCard) -> some View {
        VStack(spacing: 20) {
            Text("THẺ VỪA RÚT ĐƯỢC")
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(LSTheme.textTertiary)
            
            Text(card.emoji)
                .font(.system(size: 80))
            
            Text(card.title)
                .font(.system(size: 22, weight: .black))
                .foregroundColor(LSTheme.primary)
            
            Text(card.description)
                .font(.system(size: 14))
                .foregroundColor(LSTheme.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 30)
            
            Button {
                showDrawResult = false
            } label: {
                Text("Tuyệt vời")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 44)
                    .background(LSTheme.primary)
                    .cornerRadius(22)
            }
            .padding(.horizontal, 30)
        }
        .padding(.vertical, 40)
        .frame(maxWidth: .infinity)
        .background(LSTheme.bg)
    }
}
