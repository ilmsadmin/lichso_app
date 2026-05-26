import SwiftUI

struct LoBanScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var selectedMode: LoBanMode = .doGiaDung
    @State private var lengthText: String = "81.0"
    
    var onAskAiClick: (String) -> Void = { _ in }
    
    private var lengthValue: Double {
        Double(lengthText.replacingOccurrences(of: ",", with: ".")) ?? 0.0
    }
    
    var body: some View {
        let result = FengShuiCalculators.evaluateLoBan(lengthCm: lengthValue, mode: selectedMode)
        
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
                        Text("Thước Lỗ Ban")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Đo kích thước cát hung phong thủy")
                            .font(.system(size: 12))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    
                    Spacer()
                    
                    Button {
                        onAskAiClick("Hãy giải thích thước Lỗ Ban và gợi ý kích thước cát lợi cho cửa chính, bếp, bàn thờ và giường ngủ.")
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
                    VStack(spacing: 16) {
                        Spacer().frame(height: 10)
                        
                        // Select Ruler Type
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Chọn loại thước")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(LSTheme.textPrimary)
                            
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 8) {
                                    ForEach(LoBanMode.allCases) { mode in
                                        Button {
                                            selectedMode = mode
                                        } label: {
                                            Text(mode.title)
                                                .font(.system(size: 13, weight: .medium))
                                                .foregroundColor(selectedMode == mode ? .white : LSTheme.textPrimary)
                                                .padding(.horizontal, 14)
                                                .padding(.vertical, 8)
                                                .background(selectedMode == mode ? LSTheme.primary : LSTheme.surfaceContainerHigh)
                                                .cornerRadius(20)
                                        }
                                    }
                                }
                            }
                            
                            // Visual preview of the selected scale
                            loBanRulerPreview(result: result)
                        }
                        .padding(18)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                        
                        // Input Value
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Nhập số đo")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(LSTheme.textPrimary)
                            
                            HStack {
                                TextField("Chiều dài", text: $lengthText)
                                    .font(.system(size: 16, weight: .semibold))
                                    .keyboardType(.decimalPad)
                                    .padding(12)
                                    .background(LSTheme.bg)
                                    .cornerRadius(10)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 10)
                                            .stroke(LSTheme.outlineVariant, lineWidth: 1)
                                    )
                                    .onChange(of: lengthText) { _, next in
                                        let filtered = next.filter { $0.isNumber || $0 == "." || $0 == "," }
                                        if filtered != next {
                                            lengthText = filtered
                                        }
                                    }
                                
                                Text("cm")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(LSTheme.textSecondary)
                            }
                            
                            Slider(value: Binding(
                                get: { min(max(0.0, lengthValue), 200.0) },
                                set: { lengthText = String(format: "%.1f", $0) }
                            ), in: 0...200)
                            .accentColor(LSTheme.primary)
                            
                            Text("Gợi ý: cửa chính, bàn thờ, giường ngủ thường cần kiểm tra nhiều lớp số đo khác nhau.")
                                .font(.system(size: 11))
                                .foregroundColor(LSTheme.textTertiary)
                        }
                        .padding(18)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                        
                        // Result Evaluation
                        let ratingColor = result.isAuspicious ? LSTheme.goodGreen : LSTheme.badRed
                        VStack(alignment: .leading, spacing: 8) {
                            Text(result.isAuspicious ? "Kết quả cát lợi" : "Kết quả tham khảo")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(ratingColor)
                            
                            Text("\(String(format: "%.1f", result.rawLengthCm)) cm · \(result.mode.title)")
                                .font(.system(size: 22, weight: .black))
                                .foregroundColor(LSTheme.textPrimary)
                            
                            Text("Rơi vào cung \(result.zoneName)")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(ratingColor)
                            
                            Text(result.hint)
                                .font(.system(size: 13))
                                .foregroundColor(LSTheme.textSecondary)
                                .lineSpacing(4)
                        }
                        .padding(18)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(ratingColor.opacity(0.08))
                        .cornerRadius(16)
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(ratingColor.opacity(0.3), lineWidth: 1.5)
                        )
                        .padding(.horizontal, 16)
                        
                        // Auxiliary details
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Thông số kỹ thuật")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(LSTheme.textPrimary)
                            
                            infoRow(label: "Chu kỳ thước", value: result.mode.title)
                            infoRow(label: "Vị trí trong chu kỳ", value: "\(String(format: "%.1f", result.normalizedCm)) cm")
                            infoRow(label: "Mốc cung", value: "Cung #\(result.zoneIndex + 1) / 8")
                        }
                        .padding(18)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                        
                        // Tips
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Mốc dùng nhanh")
                                .font(.system(size: 15, weight: .semibold))
                                .foregroundColor(LSTheme.textPrimary)
                            Text("• Cửa chính: đo phủ bì theo khung thực tế.\n• Bàn thờ: ưu tiên số đo gọn, cân đối.\n• Giường ngủ: nên đo kích thước sử dụng thực tế, không chỉ phần khung.")
                                .font(.system(size: 13))
                                .foregroundColor(LSTheme.textSecondary)
                                .lineSpacing(6)
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
    
    private func loBanRulerPreview(result: LoBanResult) -> some View {
        let zoneNames = ["Tài Lộc", "Bệnh Tật", "Ly Tán", "Nghĩa Đức", "Quan Lộc", "Kiếp Sát", "Họa Hại", "Bản Mệnh"]
        let goodZones: Set<Int> = [0, 3, 4, 7]
        
        return VStack(alignment: .leading, spacing: 4) {
            Text(selectedMode.subtitle)
                .font(.system(size: 12, weight: .medium))
                .foregroundColor(LSTheme.textTertiary)
                .padding(.bottom, 4)
            
            VStack(spacing: 8) {
                // We show 5 cycles around the input length
                ForEach(0..<5) { cycleIdx in
                    let isCurrentCycle = cycleIdx == 2
                    HStack(spacing: 3) {
                        ForEach(0..<8) { idx in
                            let isGood = goodZones.contains(idx)
                            let isCurrentZone = isCurrentCycle && idx == result.zoneIndex
                            
                            let containerColor: Color = {
                                if isCurrentZone {
                                    return result.isAuspicious ? LSTheme.goodGreen.opacity(0.3) : LSTheme.badRed.opacity(0.25)
                                }
                                return isGood ? LSTheme.goodGreen.opacity(0.12) : LSTheme.badRed.opacity(0.06)
                            }()
                            
                            let borderColor = isCurrentZone ? LSTheme.primary : LSTheme.outlineVariant.opacity(0.4)
                            
                            Text(zoneNames[idx])
                                .font(.system(size: 8, weight: isCurrentZone ? .bold : .regular))
                                .foregroundColor(isCurrentZone ? LSTheme.textPrimary : LSTheme.textSecondary)
                                .frame(maxWidth: .infinity)
                                .frame(height: 40)
                                .background(containerColor)
                                .cornerRadius(6)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 6)
                                        .stroke(borderColor, lineWidth: isCurrentZone ? 1.5 : 0.8)
                                )
                        }
                    }
                }
            }
        }
    }
    
    private func infoRow(label: String, value: String) -> some View {
        HStack {
            Text(label)
                .font(.system(size: 13))
                .foregroundColor(LSTheme.textSecondary)
            Spacer()
            Text(value)
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(LSTheme.textPrimary)
        }
        .padding(.vertical, 4)
    }
}
