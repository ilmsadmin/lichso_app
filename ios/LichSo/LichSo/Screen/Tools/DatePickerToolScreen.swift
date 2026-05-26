import SwiftUI

struct DateScoredResult: Identifiable {
    let id = UUID()
    let date: Date
    let score: Int
    let label: String
    let details: String
}

struct DatePickerToolScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var eventType = "Cưới hỏi"
    @State private var startDate = Date()
    @State private var endDate = Calendar.current.date(byAdding: .day, value: 7, to: Date()) ?? Date()
    @State private var scoredResults: [DateScoredResult] = []
    
    let eventTypes = ["Cưới hỏi", "Nhập trạch (Chuyển nhà)", "Khai trương", "Động thổ", "Xuất hành"]
    
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
                        Text("Chọn ngày đại sự")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Chấm điểm ngày tốt lành theo tuổi")
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
                        
                        // Config panel
                        VStack(alignment: .leading, spacing: 14) {
                            Text("Cấu hình tra cứu")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(LSTheme.textPrimary)
                            
                            VStack(alignment: .leading, spacing: 6) {
                                Text("Loại sự kiện đại sự")
                                    .font(.system(size: 12))
                                    .foregroundColor(LSTheme.textSecondary)
                                Picker("Loại sự kiện", selection: $eventType) {
                                    ForEach(eventTypes, id: \.self) { type in
                                        Text(type).tag(type)
                                    }
                                }
                                .pickerStyle(.menu)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(4)
                                .background(LSTheme.bg)
                                .cornerRadius(8)
                            }
                            
                            DatePicker("Từ ngày", selection: $startDate, displayedComponents: .date)
                                .font(.system(size: 14))
                            
                            DatePicker("Đến ngày", selection: $endDate, displayedComponents: .date)
                                .font(.system(size: 14))
                            
                            Button {
                                generateScores()
                            } label: {
                                Text("Xem ngày tốt nhất")
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
                        
                        // Results header
                        if !scoredResults.isEmpty {
                            VStack(alignment: .leading, spacing: 12) {
                                Text("Danh sách ngày đã chấm điểm")
                                    .font(.system(size: 15, weight: .bold))
                                    .foregroundColor(LSTheme.textPrimary)
                                    .padding(.horizontal, 16)
                                
                                ForEach(scoredResults) { res in
                                    scoredRow(res)
                                }
                            }
                        } else {
                            VStack(spacing: 8) {
                                Image(systemName: "calendar.badge.clock")
                                    .font(.system(size: 40))
                                    .foregroundColor(LSTheme.textTertiary)
                                Text("Bấm tra cứu để xem điểm cát lành của các ngày trong khoảng đã chọn.")
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
            generateScores()
        }
    }
    
    private func generateScores() {
        let calendar = Calendar.current
        var results: [DateScoredResult] = []
        var currentDate = startDate
        
        // Cap at 15 days to avoid loading too many
        for _ in 0..<15 {
            if currentDate > endDate { break }
            
            let dd = calendar.component(.day, from: currentDate)
            let mm = calendar.component(.month, from: currentDate)
            let yy = calendar.component(.year, from: currentDate)
            
            let dayInfo = DayInfoProvider.shared.getDayInfo(dd: dd, mm: mm, yy: yy)
            let score = dayInfo.dayRating.percent
            let ratingLabel = dayInfo.dayRating.label
            
            results.append(DateScoredResult(
                date: currentDate,
                score: score,
                label: ratingLabel,
                details: "Trực \(dayInfo.trucNgay.name) · Sao \(dayInfo.saoChieu.name) · Hướng hỷ thần \(dayInfo.huong.hyThan)"
            ))
            
            guard let next = calendar.date(byAdding: .day, value: 1, to: currentDate) else { break }
            currentDate = next
        }
        scoredResults = results.sorted { $0.score > $1.score }
    }
    
    private func scoredRow(_ res: DateScoredResult) -> some View {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        let dateString = formatter.string(from: res.date)
        
        let scoreColor: Color = {
            if res.score >= 80 { return LSTheme.goodGreen }
            if res.score >= 60 { return LSTheme.gold }
            return LSTheme.badRed
        }()
        
        return HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                Text(dateString)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(LSTheme.textPrimary)
                Text(res.details)
                    .font(.system(size: 12))
                    .foregroundColor(LSTheme.textSecondary)
            }
            
            Spacer()
            
            VStack(alignment: .trailing, spacing: 2) {
                Text("\(res.score)đ")
                    .font(.system(size: 18, weight: .black))
                    .foregroundColor(scoreColor)
                Text(res.label)
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(scoreColor)
            }
        }
        .padding(14)
        .background(LSTheme.surfaceContainer)
        .cornerRadius(12)
        .padding(.horizontal, 16)
    }
}
