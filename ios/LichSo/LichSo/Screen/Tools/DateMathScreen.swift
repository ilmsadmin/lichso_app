import SwiftUI

struct DateMathScreen: View {
    @Environment(\.dismiss) private var dismiss
    
    // Tab selector: 0 for diff, 1 for math
    @State private var selectedTab = 0
    
    // For diff calculation
    @State private var diffStartDate = Date()
    @State private var diffEndDate = Date()
    @State private var calculatedDiffDays: Int? = nil
    
    // For arithmetic calculation
    @State private var startMathDate = Date()
    @State private var addSubtractDays = 30
    @State private var isAdd = true
    @State private var calculatedMathDate: Date? = nil
    
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
                        Text("Máy tính ngày")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Khoảng cách & Cộng trừ thời gian")
                            .font(.system(size: 12))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    
                    Spacer()
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 8)
                .background(LSTheme.surfaceContainer)
                
                // Tabs
                HStack(spacing: 0) {
                    tabHeaderButton(title: "Khoảng cách ngày", index: 0)
                    tabHeaderButton(title: "Cộng / Trừ ngày", index: 1)
                }
                .background(LSTheme.surfaceContainer)
                
                ScrollView {
                    VStack(spacing: 16) {
                        Spacer().frame(height: 10)
                        
                        if selectedTab == 0 {
                            diffCalculationsView
                        } else {
                            mathCalculationsView
                        }
                    }
                    .padding(.bottom, 24)
                }
            }
        }
        .navigationBarHidden(true)
    }
    
    private func tabHeaderButton(title: String, index: Int) -> some View {
        Button {
            selectedTab = index
        } label: {
            VStack(spacing: 6) {
                Text(title)
                    .font(.system(size: 14, weight: selectedTab == index ? .bold : .medium))
                    .foregroundColor(selectedTab == index ? LSTheme.primary : LSTheme.textSecondary)
                
                Rectangle()
                    .fill(selectedTab == index ? LSTheme.primary : Color.clear)
                    .frame(height: 2)
            }
            .frame(maxWidth: .infinity)
            .padding(.top, 10)
        }
    }
    
    private var diffCalculationsView: some View {
        VStack(spacing: 16) {
            VStack(alignment: .leading, spacing: 14) {
                DatePicker("Ngày bắt đầu", selection: $diffStartDate, displayedComponents: .date)
                    .font(.system(size: 14))
                
                DatePicker("Ngày kết thúc", selection: $diffEndDate, displayedComponents: .date)
                    .font(.system(size: 14))
                
                Button {
                    let calendar = Calendar.current
                    let start = calendar.startOfDay(for: diffStartDate)
                    let end = calendar.startOfDay(for: diffEndDate)
                    let components = calendar.dateComponents([.day], from: start, to: end)
                    calculatedDiffDays = components.day
                } label: {
                    Text("Tính khoảng cách")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 44)
                        .background(LSTheme.primary)
                        .cornerRadius(22)
                }
            }
            .padding(18)
            .background(LSTheme.surfaceContainer)
            .cornerRadius(16)
            .padding(.horizontal, 16)
            
            if let days = calculatedDiffDays {
                VStack(spacing: 8) {
                    Text("Kết quả khoảng cách")
                        .font(.system(size: 13))
                        .foregroundColor(LSTheme.textSecondary)
                    
                    Text("\(abs(days)) ngày")
                        .font(.system(size: 28, weight: .black))
                        .foregroundColor(LSTheme.primary)
                    
                    let relation = days >= 0 ? "sau ngày bắt đầu" : "trước ngày bắt đầu"
                    Text("Ngày kết thúc cách ngày bắt đầu \(abs(days)) ngày \(relation).")
                        .font(.system(size: 12))
                        .foregroundColor(LSTheme.textTertiary)
                }
                .padding(18)
                .frame(maxWidth: .infinity)
                .background(LSTheme.surfaceContainer)
                .cornerRadius(16)
                .padding(.horizontal, 16)
            }
        }
    }
    
    private func formattedDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "EEEE, dd/MM/yyyy"
        formatter.locale = Locale(identifier: "vi_VN")
        return formatter.string(from: date)
    }

    private var mathCalculationsView: some View {
        VStack(spacing: 16) {
            VStack(alignment: .leading, spacing: 14) {
                DatePicker("Ngày bắt đầu", selection: $startMathDate, displayedComponents: .date)
                    .font(.system(size: 14))
                
                HStack {
                    Text("Phép tính")
                        .font(.system(size: 14))
                        .foregroundColor(LSTheme.textSecondary)
                    Spacer()
                    Picker("Phép tính", selection: $isAdd) {
                        Text("Cộng thêm").tag(true)
                        Text("Trừ đi").tag(false)
                    }
                    .pickerStyle(.segmented)
                    .frame(width: 160)
                }
                
                HStack {
                    Text("Số lượng ngày")
                        .font(.system(size: 14))
                        .foregroundColor(LSTheme.textSecondary)
                    Spacer()
                    TextField("Số ngày", value: $addSubtractDays, formatter: NumberFormatter())
                        .font(.system(size: 15, weight: .semibold))
                        .keyboardType(.numberPad)
                        .multilineTextAlignment(.trailing)
                        .frame(width: 80)
                        .padding(8)
                        .background(LSTheme.bg)
                        .cornerRadius(6)
                }
                
                Button {
                    let calendar = Calendar.current
                    let offset = isAdd ? addSubtractDays : -addSubtractDays
                    calculatedMathDate = calendar.date(byAdding: .day, value: offset, to: startMathDate)
                } label: {
                    Text("Tính ngày mới")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 44)
                        .background(LSTheme.primary)
                        .cornerRadius(22)
                }
            }
            .padding(18)
            .background(LSTheme.surfaceContainer)
            .cornerRadius(16)
            .padding(.horizontal, 16)
            
            if let targetDate = calculatedMathDate {
                VStack(spacing: 8) {
                    Text("Kết quả ngày mới")
                        .font(.system(size: 13))
                        .foregroundColor(LSTheme.textSecondary)
                    
                    Text(formattedDate(targetDate))
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(LSTheme.primary)
                        .multilineTextAlignment(.center)
                    
                    let op = isAdd ? "cộng" : "trừ"
                    Text("Sau khi \(op) \(addSubtractDays) ngày.")
                        .font(.system(size: 12))
                        .foregroundColor(LSTheme.textTertiary)
                }
                .padding(18)
                .frame(maxWidth: .infinity)
                .background(LSTheme.surfaceContainer)
                .cornerRadius(16)
                .padding(.horizontal, 16)
            }
        }
    }
}
