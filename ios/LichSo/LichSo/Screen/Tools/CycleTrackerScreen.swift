import SwiftUI

struct CycleRecord: Codable, Identifiable {
    let id: UUID
    let startDate: Date
    let cycleLength: Int
    let periodLength: Int
}

struct CycleTrackerScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var records: [CycleRecord] = []
    
    // Config parameters
    @State private var cycleLength = 28
    @State private var periodLength = 5
    @State private var showLogSheet = false
    @State private var logStartDate = Date()
    
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
                        Text("Chu kỳ kinh nguyệt")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Theo dõi sức khỏe & dự đoán ngày rụng trứng")
                            .font(.system(size: 12))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    
                    Spacer()
                    
                    Button {
                        showLogSheet = true
                    } label: {
                        Image(systemName: "plus.circle.fill")
                            .font(.system(size: 22))
                            .foregroundColor(LSTheme.primary)
                            .frame(width: 44, height: 44)
                    }
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 8)
                .background(LSTheme.surfaceContainer)
                
                ScrollView {
                    VStack(spacing: 16) {
                        Spacer().frame(height: 10)
                        
                        // Prediction Hero card
                        predictionHeroView
                        
                        // Config settings Card
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Thiết lập chu kỳ trung bình")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(LSTheme.textPrimary)
                            
                            Stepper("Chiều dài chu kỳ: \(cycleLength) ngày", value: $cycleLength, in: 20...45)
                                .font(.system(size: 14))
                            
                            Stepper("Số ngày kinh nguyệt: \(periodLength) ngày", value: $periodLength, in: 3...10)
                                .font(.system(size: 14))
                        }
                        .padding(18)
                        .background(LSTheme.surfaceContainer)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                        
                        // Log history list
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Lịch sử chu kỳ")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(LSTheme.textPrimary)
                                .padding(.horizontal, 16)
                            
                            if records.isEmpty {
                                Text("Chưa ghi nhận kỳ nào. Nhấn '+' để bắt đầu nhập ngày.")
                                    .font(.system(size: 13))
                                    .foregroundColor(LSTheme.textTertiary)
                                    .padding(.horizontal, 20)
                                    .padding(.vertical, 20)
                            } else {
                                ForEach(records) { rec in
                                    recordRow(rec)
                                }
                            }
                        }
                    }
                    .padding(.bottom, 24)
                }
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            loadRecords()
        }
        .sheet(isPresented: $showLogSheet) {
            logCycleSheetView
        }
    }
    
    private var predictionHeroView: some View {
        let calendar = Calendar.current
        let nextPeriodDate: Date? = {
            guard let last = records.sorted(by: { $0.startDate < $1.startDate }).last else { return nil }
            return calendar.date(byAdding: .day, value: cycleLength, to: last.startDate)
        }()
        
        let ovulatingDate: Date? = {
            guard let next = nextPeriodDate else { return nil }
            return calendar.date(byAdding: .day, value: -14, to: next)
        }()
        
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        
        return VStack(spacing: 12) {
            Text("DỰ ĐOÁN KỲ TIẾP THEO")
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(.white.opacity(0.8))
            
            if let next = nextPeriodDate {
                Text(formatter.string(from: next))
                    .font(.system(size: 28, weight: .black))
                    .foregroundColor(.white)
                
                if let ovul = ovulatingDate {
                    Text("Ngày rụng trứng dự kiến: \(formatter.string(from: ovul))")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(.white.opacity(0.9))
                }
            } else {
                Text("Chưa có dữ liệu")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(.white)
                Text("Vui lòng ghi nhận kỳ kinh nguyệt gần nhất để dự đoán.")
                    .font(.system(size: 12))
                    .foregroundColor(.white.opacity(0.8))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 20)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 24)
        .background(LinearGradient(colors: [Color(hex: "C2185B"), Color(hex: "F06292")], startPoint: .topLeading, endPoint: .bottomTrailing))
        .cornerRadius(20)
        .padding(.horizontal, 16)
    }
    
    private func recordRow(_ rec: CycleRecord) -> some View {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        
        return HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text("Ngày bắt đầu: \(formatter.string(from: rec.startDate))")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(LSTheme.textPrimary)
                Text("Kéo dài: \(rec.periodLength) ngày · Chu kỳ: \(rec.cycleLength) ngày")
                    .font(.system(size: 12))
                    .foregroundColor(LSTheme.textSecondary)
            }
            Spacer()
            Button {
                deleteRecord(rec)
            } label: {
                Image(systemName: "trash")
                    .foregroundColor(LSTheme.badRed.opacity(0.8))
            }
        }
        .padding(14)
        .background(LSTheme.surfaceContainer)
        .cornerRadius(12)
        .padding(.horizontal, 16)
    }
    
    private func loadRecords() {
        if let data = UserDefaults.standard.data(forKey: "cycle_records"),
           let decoded = try? JSONDecoder().decode([CycleRecord].self, from: data) {
            records = decoded
        }
    }
    
    private func saveRecords(_ list: [CycleRecord]) {
        if let encoded = try? JSONEncoder().encode(list) {
            UserDefaults.standard.set(encoded, forKey: "cycle_records")
        }
    }
    
    private func deleteRecord(_ record: CycleRecord) {
        records.removeAll { $0.id == record.id }
        saveRecords(records)
    }
    
    private var logCycleSheetView: some View {
        NavigationView {
            Form {
                DatePicker("Ngày bắt đầu kỳ kinh", selection: $logStartDate, displayedComponents: .date)
            }
            .navigationTitle("Ghi nhận chu kỳ")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Hủy") { showLogSheet = false }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Lưu") {
                        let newRec = CycleRecord(id: UUID(), startDate: logStartDate, cycleLength: cycleLength, periodLength: periodLength)
                        records.append(newRec)
                        saveRecords(records)
                        showLogSheet = false
                    }
                }
            }
        }
    }
}
