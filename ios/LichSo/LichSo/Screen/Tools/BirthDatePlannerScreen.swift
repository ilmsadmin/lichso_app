import SwiftUI

struct BirthDateSuggestion: Identifiable {
    let id = UUID()
    let date: Date
    let score: Int
    let label: String
    let comment: String
}

struct BirthDatePlannerScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var dueDate = Date()
    @State private var suggestions: [BirthDateSuggestion] = []
    
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
                        Text("Kế hoạch ngày sinh đẹp")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Phân tích ngày sinh quanh ngày dự sinh")
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
                        
                        // Inputs
                        VStack(alignment: .leading, spacing: 14) {
                            Text("Nhập ngày dự sinh")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(LSTheme.textPrimary)
                            
                            DatePicker("Ngày dự sinh", selection: $dueDate, displayedComponents: .date)
                                .font(.system(size: 14))
                            
                            Button {
                                calculateSuggestions()
                            } label: {
                                Text("Gợi ý ngày sinh tốt nhất")
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 44)
                                    .background(LSTheme.primary)
                                    .cornerRadius(22)
                            }
                            .padding(.top, 6)
                        }
                        .padding(18)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                        
                        // Suggestions list
                        if !suggestions.isEmpty {
                            VStack(alignment: .leading, spacing: 12) {
                                Text("Các ngày gợi ý (khoảng ±7 ngày)")
                                    .font(.system(size: 15, weight: .bold))
                                    .foregroundColor(LSTheme.textPrimary)
                                    .padding(.horizontal, 16)
                                
                                ForEach(suggestions) { sug in
                                    suggestionRow(sug)
                                }
                            }
                        } else {
                            VStack(spacing: 8) {
                                Image(systemName: "sparkles")
                                    .font(.system(size: 40))
                                    .foregroundColor(LSTheme.textTertiary)
                                Text("Hãy bấm tính toán để nhận danh sách ngày lành tốt nhất quanh khoảng ngày dự sinh.")
                                    .font(.system(size: 13))
                                    .foregroundColor(LSTheme.textSecondary)
                                    .multilineTextAlignment(.center)
                                    .padding(.horizontal, 40)
                            }
                            .padding(.vertical, 40)
                        }
                    }
                    .padding(.bottom, 24)
                }
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            calculateSuggestions()
        }
    }
    
    private func calculateSuggestions() {
        let calendar = Calendar.current
        var list: [BirthDateSuggestion] = []
        
        // Check days from due date -7 to due date +7
        for offset in -7...7 {
            guard let checkDate = calendar.date(byAdding: .day, value: offset, to: dueDate) else { continue }
            
            let dd = calendar.component(.day, from: checkDate)
            let mm = calendar.component(.month, from: checkDate)
            let yy = calendar.component(.year, from: checkDate)
            
            let dayInfo = DayInfoProvider.shared.getDayInfo(dd: dd, mm: mm, yy: yy)
            
            let rawScore = dayInfo.dayRating.percent
            let ratingLabel = dayInfo.dayRating.label
            
            var comment = "Ngày lành thích hợp."
            if offset == 0 {
                comment += " (Chính xác ngày dự sinh)"
            } else if offset < 0 {
                comment += " (Sớm \(abs(offset)) ngày)"
            } else {
                comment += " (Muộn \(offset) ngày)"
            }
            
            list.append(BirthDateSuggestion(
                date: checkDate,
                score: rawScore,
                label: ratingLabel,
                comment: comment
            ))
        }
        
        // Sort by score
        suggestions = list.sorted { $0.score > $1.score }
    }
    
    private func suggestionRow(_ sug: BirthDateSuggestion) -> some View {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        let dateStr = formatter.string(from: sug.date)
        
        let scoreColor: Color = {
            if sug.score >= 80 { return LSTheme.goodGreen }
            if sug.score >= 60 { return LSTheme.gold }
            return LSTheme.badRed
        }()
        
        return HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                Text(dateStr)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(LSTheme.textPrimary)
                Text(sug.comment)
                    .font(.system(size: 12))
                    .foregroundColor(LSTheme.textSecondary)
            }
            
            Spacer()
            
            VStack(alignment: .trailing, spacing: 2) {
                Text("\(sug.score)đ")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(scoreColor)
                Text(sug.label)
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundColor(scoreColor)
            }
        }
        .padding(14)
        .background(LSTheme.surfaceContainer)
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(LSTheme.outlineVariant.opacity(0.3), lineWidth: 1)
        )
        .padding(.horizontal, 16)
    }
}
