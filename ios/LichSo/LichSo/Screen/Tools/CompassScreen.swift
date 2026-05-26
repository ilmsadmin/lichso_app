import SwiftUI
import CoreLocation

class HeadingManager: NSObject, ObservableObject, CLLocationManagerDelegate {
    @Published var heading: Double = 0.0
    @Published var authorizationStatus: CLAuthorizationStatus = .notDetermined
    
    private let locationManager = CLLocationManager()
    
    override init() {
        super.init()
        locationManager.delegate = self
        locationManager.headingFilter = 0.5
        locationManager.startUpdatingHeading()
        authorizationStatus = locationManager.authorizationStatus
    }
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        authorizationStatus = manager.authorizationStatus
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
        heading = newHeading.magneticHeading
    }
}

struct CompassScreen: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var headingManager = HeadingManager()
    @State private var manualRotation: Double = 0.0
    
    var onAskAiClick: (String) -> Void = { _ in }
    
    private var todayInfo: DayInfo {
        let calendar = Calendar.current
        let today = Date()
        let dd = calendar.component(.day, from: today)
        let mm = calendar.component(.month, from: today)
        let yy = calendar.component(.year, from: today)
        return DayInfoProvider.shared.getDayInfo(dd: dd, mm: mm, yy: yy)
    }
    
    var body: some View {
        let currentHeading = headingManager.heading != 0 ? headingManager.heading : manualRotation
        let headingLabel = FengShuiCalculators.compassDirectionForDegrees(currentHeading)
        let roundedHeading = Int(currentHeading.rounded())
        
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
                        Text("La bàn phong thủy")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Theo dõi hướng và gợi ý hôm nay")
                            .font(.system(size: 12))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    
                    Spacer()
                    
                    Button {
                        onAskAiClick("Hãy phân tích la bàn phong thủy, hướng nhà và cung phi theo tuổi của tôi.")
                    } label: {
                        Text("AI")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(LSTheme.primary)
                            .padding(.horizontal, 14)
                            .padding(.vertical, 6)
                            .overlay(
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(LSTheme.primary, lineWidth: 1.5)
                            )
                    }
                    .padding(.trailing, 8)
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 8)
                .background(LSTheme.surfaceContainer)
                
                ScrollView {
                    VStack(spacing: 20) {
                        Spacer().frame(height: 20)
                        
                        // Compass dial
                        compassDialView(currentHeading: currentHeading, label: headingLabel)
                        
                        // Simulated slider for simulator / when no device heading
                        if headingManager.heading == 0 {
                            VStack(spacing: 4) {
                                Text("Không tìm thấy la bàn phần cứng. Kéo thanh để xoay thử:")
                                    .font(.system(size: 11))
                                    .foregroundColor(LSTheme.textSecondary)
                                Slider(value: $manualRotation, in: 0...360)
                                    .accentColor(LSTheme.primary)
                            }
                            .padding(.horizontal, 20)
                        }
                        
                        Spacer().frame(height: 10)
                        
                        // Current Heading Info
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Hướng hiện tại")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(LSTheme.textPrimary)
                            Text("\(roundedHeading)° · \(headingLabel)")
                                .font(.system(size: 24, weight: .bold))
                                .foregroundColor(LSTheme.primary)
                            Text("Giữ thiết bị phẳng để hướng đo chính xác nhất. La bàn sẽ cập nhật góc xoay theo thời gian thực.")
                                .font(.system(size: 13))
                                .foregroundColor(LSTheme.textSecondary)
                                .lineSpacing(4)
                        }
                        .padding(18)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 20)
                        
                        // Auspicious directions today
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Hướng tốt hôm nay")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(LSTheme.textPrimary)
                            
                            directionRow(label: "Thần Tài", value: todayInfo.huong.thanTai, color: LSTheme.goodGreen)
                            directionRow(label: "Hỷ Thần", value: todayInfo.huong.hyThan, color: LSTheme.gold)
                            directionRow(label: "Nên tránh", value: todayInfo.huong.hungThan, color: LSTheme.badRed)
                        }
                        .padding(18)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 20)
                        
                        // Usage tips
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Mẹo sử dụng")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(LSTheme.textPrimary)
                            Text("• Giữ máy phẳng để góc quay ổn định.\n• Ưu tiên kiểm tra ở nơi thoáng, tránh đứng sát kim loại lớn.\n• Dùng cùng với Bát trạch để đối chiếu hướng nhà hợp tuổi.")
                                .font(.system(size: 13))
                                .foregroundColor(LSTheme.textSecondary)
                                .lineSpacing(6)
                        }
                        .padding(18)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 20)
                    }
                    .padding(.bottom, 24)
                }
            }
        }
        .navigationBarHidden(true)
    }
    
    private func compassDialView(currentHeading: Double, label: String) -> some View {
        Box {
            ZStack {
                Circle()
                    .fill(LSTheme.surfaceContainer)
                    .frame(width: 290, height: 290)
                
                Circle()
                    .fill(LSTheme.bg)
                    .frame(width: 250, height: 250)
                
                ZStack {
                    // Direction markers rotated
                    Text("N")
                        .font(.system(size: 18, weight: .black))
                        .foregroundColor(Color(hex: "E53935"))
                        .offset(y: -100)
                    
                    Text("E")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(LSTheme.textSecondary)
                        .offset(x: 100)
                    
                    Text("S")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(LSTheme.textSecondary)
                        .offset(y: 100)
                    
                    Text("W")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(LSTheme.textSecondary)
                        .offset(x: -100)
                    
                    // Rotating lines and needle
                    Circle()
                        .stroke(LSTheme.outlineVariant.opacity(0.3), lineWidth: 1)
                        .frame(width: 180, height: 180)
                    
                    // Red center dot
                    Circle()
                        .fill(Color(hex: "E53935"))
                        .frame(width: 12, height: 12)
                }
                .frame(width: 220, height: 220)
                .rotationEffect(.degrees(-currentHeading))
                
                // Readout text overlay in center
                VStack(spacing: 2) {
                    Text(label)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(LSTheme.primary)
                    Text("Đang đo")
                        .font(.system(size: 10))
                        .foregroundColor(LSTheme.textTertiary)
                }
            }
            .frame(width: 290, height: 290)
        }
    }
    
    private func directionRow(label: String, value: String, color: Color) -> some View {
        HStack {
            Text(label)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(LSTheme.textSecondary)
            Spacer()
            Text(value)
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(color)
        }
        .padding(.vertical, 4)
    }
}

// Helper wrapper for layout
private struct Box<Content: View>: View {
    let content: () -> Content
    
    var body: some View {
        HStack {
            Spacer()
            content()
            Spacer()
        }
    }
}
