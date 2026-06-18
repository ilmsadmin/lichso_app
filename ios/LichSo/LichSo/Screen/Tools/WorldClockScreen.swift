import SwiftUI

struct WorldCityTime: Identifiable {
    let id = UUID()
    let cityName: String
    let flag: String
    let timeZoneId: String
    let description: String
}

struct WorldClockScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var currentTime = Date()
    
    let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()
    
    let cities = [
        WorldCityTime(cityName: "Hà Nội", flag: "🇻🇳", timeZoneId: "Asia/Ho_Chi_Minh", description: "Múi giờ gốc Đông Dương (ICT)"),
        WorldCityTime(cityName: "New York", flag: "🇺🇸", timeZoneId: "America/New_York", description: "Giờ chuẩn miền Đông Mỹ (EST)"),
        WorldCityTime(cityName: "Tokyo", flag: "🇯🇵", timeZoneId: "Asia/Tokyo", description: "Giờ chuẩn Nhật Bản (JST)"),
        WorldCityTime(cityName: "Luân Đôn", flag: "🇬🇧", timeZoneId: "Europe/London", description: "Giờ chuẩn Tây Âu (BST/GMT)"),
        WorldCityTime(cityName: "Paris", flag: "🇫🇷", timeZoneId: "Europe/Paris", description: "Giờ chuẩn Trung Âu (CEST)")
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
                        Text("Múi giờ thế giới")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Đồng hồ so sánh các khu vực")
                            .font(.system(size: 12))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    
                    Spacer()
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 8)
                .background(LSTheme.surfaceContainer)
                
                ScrollView {
                    VStack(spacing: 12) {
                        Spacer().frame(height: 10)
                        
                        // Hero card with Hanoi time
                        hanoiHeroView
                        
                        // Global list
                        ForEach(cities) { city in
                            cityClockCard(city)
                        }
                    }
                    .padding(.horizontal, 16)
                }
            }
        }
        .navigationBarHidden(true)
        .onReceive(timer) { input in
            currentTime = input
        }
    }
    
    private var hanoiHeroView: some View {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm:ss"
        formatter.timeZone = TimeZone(identifier: "Asia/Ho_Chi_Minh")
        let timeStr = formatter.string(from: currentTime)
        
        let dateForm = DateFormatter()
        dateForm.dateFormat = "EEEE, dd/MM/yyyy"
        dateForm.locale = Locale(identifier: "vi_VN")
        dateForm.timeZone = TimeZone(identifier: "Asia/Ho_Chi_Minh")
        let dateStr = dateForm.string(from: currentTime)
        
        return VStack(spacing: 8) {
            Text("GIỜ VIỆT NAM (ICT)")
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(.white.opacity(0.8))
            
            Text(timeStr)
                .font(.system(size: 38, weight: .black))
                .foregroundColor(.white)
                .tracking(2)
            
            Text(dateStr)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(.white.opacity(0.9))
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 24)
        .background(LinearGradient(colors: [LSTheme.primary, Color(hex: "BF360C")], startPoint: .topLeading, endPoint: .bottomTrailing))
        .cornerRadius(20)
    }
    
    private func cityClockCard(_ city: WorldCityTime) -> some View {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        formatter.timeZone = TimeZone(identifier: city.timeZoneId)
        let timeStr = formatter.string(from: currentTime)
        
        let ampm = DateFormatter()
        ampm.dateFormat = "a"
        ampm.timeZone = TimeZone(identifier: city.timeZoneId)
        let ampmStr = ampm.string(from: currentTime)
        
        return HStack(spacing: 16) {
            Text(city.flag)
                .font(.system(size: 32))
            
            VStack(alignment: .leading, spacing: 3) {
                Text(city.cityName)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(LSTheme.textPrimary)
                Text(city.description)
                    .font(.system(size: 11))
                    .foregroundColor(LSTheme.textTertiary)
            }
            
            Spacer()
            
            HStack(alignment: .firstTextBaseline, spacing: 4) {
                Text(timeStr)
                    .font(.system(size: 24, weight: .black))
                    .foregroundColor(LSTheme.textPrimary)
                Text(ampmStr)
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(LSTheme.textSecondary)
            }
        }
        .padding(16)
        .background(LSTheme.surfaceContainer)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(LSTheme.outlineVariant.opacity(0.3), lineWidth: 1)
        )
    }
}
