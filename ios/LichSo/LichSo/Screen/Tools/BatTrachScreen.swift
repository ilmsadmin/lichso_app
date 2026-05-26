import SwiftUI

struct BatTrachScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var birthYearText: String = "1990"
    @State private var gender: String = "Nam"
    
    var onAskAiClick: (String) -> Void = { _ in }
    
    private var birthYearValue: Int {
        Int(birthYearText) ?? 1990
    }
    
    var body: some View {
        let result = FengShuiCalculators.calculateCungPhi(lunarYear: birthYearValue, gender: gender)
        
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
                        Text("Bát Trạch")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Xem hướng hợp cung phi bản mệnh")
                            .font(.system(size: 12))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    
                    Spacer()
                    
                    Button {
                        onAskAiClick("Hãy phân tích chi tiết Bát trạch phong thủy cho tuổi \(birthYearText) \(gender), cung phi \(result.cungName).")
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
                        
                        // Select inputs
                        VStack(alignment: .leading, spacing: 14) {
                            Text("Thông tin bản mệnh")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(LSTheme.textPrimary)
                            
                            HStack(spacing: 12) {
                                VStack(alignment: .leading, spacing: 6) {
                                    Text("Năm sinh (Âm lịch)")
                                        .font(.system(size: 12))
                                        .foregroundColor(LSTheme.textSecondary)
                                    TextField("1990", text: $birthYearText)
                                        .font(.system(size: 15, weight: .semibold))
                                        .keyboardType(.numberPad)
                                        .padding(10)
                                        .background(LSTheme.bg)
                                        .cornerRadius(10)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 10)
                                                .stroke(LSTheme.outlineVariant, lineWidth: 1)
                                        )
                                        .onChange(of: birthYearText) { _, next in
                                            let filtered = next.filter { $0.isNumber }
                                            if filtered != next {
                                                birthYearText = filtered
                                            }
                                        }
                                }
                                
                                VStack(alignment: .leading, spacing: 6) {
                                    Text("Giới tính")
                                        .font(.system(size: 12))
                                        .foregroundColor(LSTheme.textSecondary)
                                    HStack(spacing: 0) {
                                        genderButton(title: "Nam")
                                        genderButton(title: "Nữ")
                                    }
                                    .background(LSTheme.bg)
                                    .cornerRadius(10)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 10)
                                            .stroke(LSTheme.outlineVariant, lineWidth: 1)
                                    )
                                }
                            }
                        }
                        .padding(18)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                        
                        // Cung Phi Result
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Bản mệnh Cung Phi")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(LSTheme.primary)
                            
                            HStack {
                                Text(result.cungName)
                                    .font(.system(size: 32, weight: .black))
                                    .foregroundColor(LSTheme.textPrimary)
                                
                                Text("(\(result.groupLabel))")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(LSTheme.textSecondary)
                            }
                            
                            Text("Theo Bát trạch tam nguyên cung phi, người sinh năm \(birthYearText) có mệnh quái là cung \(result.cungName), thuộc nhóm \(result.groupLabel).")
                                .font(.system(size: 13))
                                .foregroundColor(LSTheme.textSecondary)
                                .lineSpacing(4)
                        }
                        .padding(18)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                        
                        // Directions lists
                        VStack(alignment: .leading, spacing: 14) {
                            Text("Hướng Cát (Nên chọn)")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(LSTheme.goodGreen)
                            
                            ForEach(result.favorableDirections, id: \.self) { dir in
                                directionRow(dir: dir, isGood: true)
                            }
                            
                            Divider()
                                .background(LSTheme.outlineVariant)
                                .padding(.vertical, 4)
                            
                            Text("Hướng Hung (Nên tránh)")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(LSTheme.badRed)
                            
                            ForEach(result.unfavorableDirections, id: \.self) { dir in
                                directionRow(dir: dir, isGood: false)
                            }
                        }
                        .padding(18)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                        
                        // Usage Notes
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Lời khuyên phong thủy")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(LSTheme.textPrimary)
                            Text(result.note)
                                .font(.system(size: 13))
                                .foregroundColor(LSTheme.textSecondary)
                                .lineSpacing(5)
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
    
    private func genderButton(title: String) -> some View {
        Button {
            gender = title
        } label: {
            Text(title)
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(gender == title ? .white : LSTheme.textPrimary)
                .frame(maxWidth: .infinity)
                .frame(height: 38)
                .background(gender == title ? LSTheme.primary : Color.clear)
                .cornerRadius(8)
        }
    }
    
    private func directionRow(dir: String, isGood: Bool) -> some View {
        HStack {
            Image(systemName: isGood ? "checkmark.circle.fill" : "xmark.circle.fill")
                .foregroundColor(isGood ? LSTheme.goodGreen : LSTheme.badRed)
            Text("Hướng \(dir)")
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(LSTheme.textPrimary)
            Spacer()
            Text(isGood ? "Hợp mệnh" : "Khắc mệnh")
                .font(.system(size: 12))
                .foregroundColor(isGood ? LSTheme.goodGreen : LSTheme.badRed)
        }
    }
}
