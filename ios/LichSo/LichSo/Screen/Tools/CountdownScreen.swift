import SwiftUI

struct CountdownEvent: Identifiable, Codable {
    let id: UUID
    let title: String
    let targetDate: Date
    
    var daysRemaining: Int {
        let calendar = Calendar.current
        let today = calendar.startOfDay(for: Date())
        let target = calendar.startOfDay(for: targetDate)
        let diff = calendar.dateComponents([.day], from: today, to: target)
        return diff.day ?? 0
    }
}

struct CountdownScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var events: [CountdownEvent] = []
    
    // Add event dialog state
    @State private var showAddSheet = false
    @State private var newEventTitle = ""
    @State private var newEventDate = Date()
    
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
                        Text("Đếm ngược sự kiện")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Theo dõi các cột mốc quan trọng")
                            .font(.system(size: 12))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    
                    Spacer()
                    
                    Button {
                        showAddSheet = true
                    } label: {
                        Image(systemName: "plus")
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(LSTheme.primary)
                            .frame(width: 44, height: 44)
                    }
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 8)
                .background(LSTheme.surfaceContainer)
                
                ScrollView {
                    VStack(spacing: 12) {
                        Spacer().frame(height: 10)
                        
                        if events.isEmpty {
                            VStack(spacing: 8) {
                                Image(systemName: "hourglass.badge.plus")
                                    .font(.system(size: 44))
                                    .foregroundColor(LSTheme.textTertiary)
                                Text("Chưa có sự kiện nào")
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundColor(LSTheme.textSecondary)
                                Text("Bấm nút '+' góc trên để bắt đầu thêm sự kiện đếm ngược.")
                                    .font(.system(size: 12))
                                    .foregroundColor(LSTheme.textTertiary)
                                    .multilineTextAlignment(.center)
                            }
                            .padding(.vertical, 80)
                        } else {
                            ForEach(events) { ev in
                                eventRow(ev)
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                }
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            loadEvents()
        }
        .sheet(isPresented: $showAddSheet) {
            addEventSheet
        }
    }
    
    private func loadEvents() {
        if let data = UserDefaults.standard.data(forKey: "countdown_events"),
           let decoded = try? JSONDecoder().decode([CountdownEvent].self, from: data) {
            events = decoded
        } else {
            // Default events if empty
            let calendar = Calendar.current
            let nextYear = calendar.component(.year, from: Date()) + 1
            let tetDate = calendar.date(from: DateComponents(year: nextYear, month: 1, day: 29)) ?? Date() // approximate
            
            let defaultEvents = [
                CountdownEvent(id: UUID(), title: "Tết Nguyên Đán năm sau", targetDate: tetDate),
                CountdownEvent(id: UUID(), title: "Tết Dương Lịch", targetDate: calendar.date(from: DateComponents(year: nextYear, month: 1, day: 1)) ?? Date())
            ]
            events = defaultEvents
            saveEvents(defaultEvents)
        }
    }
    
    private func saveEvents(_ list: [CountdownEvent]) {
        if let encoded = try? JSONEncoder().encode(list) {
            UserDefaults.standard.set(encoded, forKey: "countdown_events")
        }
    }
    
    private func deleteEvent(_ event: CountdownEvent) {
        events.removeAll { $0.id == event.id }
        saveEvents(events)
    }
    
    private func eventRow(_ ev: CountdownEvent) -> some View {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        let dateStr = formatter.string(from: ev.targetDate)
        
        let days = ev.daysRemaining
        let isPast = days < 0
        
        return HStack(spacing: 16) {
            VStack(alignment: .leading, spacing: 4) {
                Text(ev.title)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(LSTheme.textPrimary)
                Text(dateStr)
                    .font(.system(size: 12))
                    .foregroundColor(LSTheme.textSecondary)
            }
            
            Spacer()
            
            VStack(alignment: .trailing, spacing: 2) {
                if isPast {
                    Text("Đã qua \(abs(days)) ngày")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(LSTheme.textTertiary)
                } else if days == 0 {
                    Text("Hôm nay!")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(LSTheme.primary)
                } else {
                    Text("\(days)")
                        .font(.system(size: 24, weight: .black))
                        .foregroundColor(LSTheme.primary)
                    Text("ngày nữa")
                        .font(.system(size: 10))
                        .foregroundColor(LSTheme.textSecondary)
                }
            }
            
            Button {
                deleteEvent(ev)
            } label: {
                Image(systemName: "trash")
                    .foregroundColor(LSTheme.badRed.opacity(0.8))
                    .padding(8)
            }
        }
        .padding(14)
        .background(LSTheme.surfaceContainer)
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(LSTheme.outlineVariant.opacity(0.3), lineWidth: 1)
        )
    }
    
    private var addEventSheet: some View {
        NavigationView {
            Form {
                Section(header: Text("Thông tin sự kiện")) {
                    TextField("Tên sự kiện (VD: Sinh nhật con)", text: $newEventTitle)
                    DatePicker("Ngày diễn ra", selection: $newEventDate, displayedComponents: .date)
                }
            }
            .navigationTitle("Thêm đếm ngược")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Hủy") { showAddSheet = false }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Lưu") {
                        if !newEventTitle.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                            let newEvent = CountdownEvent(id: UUID(), title: newEventTitle, targetDate: newEventDate)
                            events.append(newEvent)
                            saveEvents(events)
                            newEventTitle = ""
                            showAddSheet = false
                        }
                    }
                }
            }
        }
    }
}
