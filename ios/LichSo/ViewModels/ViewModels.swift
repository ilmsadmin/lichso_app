import Foundation
import Combine
import SwiftUI

// MARK: - Home ViewModel
@MainActor
class HomeViewModel: ObservableObject {
    @Published var dayInfo: DayInfo?
    @Published var upcomingEvents: [UpcomingEvent] = []
    @Published var currentDate: Date = Date()

    init() {
        loadDay(date: Date())
        loadEvents()
    }

    func loadDay(date: Date) {
        let cal = Calendar.current
        let d = cal.component(.day, from: date)
        let m = cal.component(.month, from: date)
        let y = cal.component(.year, from: date)
        dayInfo = LunarCalendarEngine.buildDayInfo(dd: d, mm: m, yy: y)
        currentDate = date
    }

    func prevDay() {
        currentDate = Calendar.current.date(byAdding: .day, value: -1, to: currentDate) ?? currentDate
        loadDay(date: currentDate)
    }

    func nextDay() {
        currentDate = Calendar.current.date(byAdding: .day, value: 1, to: currentDate) ?? currentDate
        loadDay(date: currentDate)
    }

    func goToToday() {
        loadDay(date: Date())
    }

    private func loadEvents() {
        upcomingEvents = [
            UpcomingEvent(title: "Rằm Tháng Giêng", timeLabel: "Ngày 15 tháng Giêng", tag: "Âm lịch", colorType: .gold),
            UpcomingEvent(title: "Giỗ Tổ Hùng Vương", timeLabel: "10/3 Âm lịch", tag: "Lễ lớn", colorType: .teal),
            UpcomingEvent(title: "Tết Trung Thu", timeLabel: "15/8 Âm lịch", tag: "Truyền thống", colorType: .gold),
        ]
    }
}

// MARK: - Calendar ViewModel
@MainActor
class CalendarViewModel: ObservableObject {
    @Published var calendarDays: [CalendarDay] = []
    @Published var currentMonth: Int
    @Published var currentYear: Int
    @Published var selectedDayInfo: DayInfo?
    @Published var showLunarBadge: Bool = true
    @Published var selectedDate: Date = Date()

    init() {
        let cal = Calendar.current
        currentMonth = cal.component(.month, from: Date())
        currentYear = cal.component(.year, from: Date())
        loadCalendar()
        selectToday()
    }

    func loadCalendar() {
        calendarDays = LunarCalendarEngine.calendarDays(month: currentMonth, year: currentYear)
    }

    func prevMonth() {
        if currentMonth == 1 { currentMonth = 12; currentYear -= 1 }
        else { currentMonth -= 1 }
        loadCalendar()
    }

    func nextMonth() {
        if currentMonth == 12 { currentMonth = 1; currentYear += 1 }
        else { currentMonth += 1 }
        loadCalendar()
    }

    func selectDay(_ day: CalendarDay) {
        selectedDayInfo = LunarCalendarEngine.buildDayInfo(dd: day.solarDay, mm: day.solarMonth, yy: day.solarYear)
        var comps = DateComponents(year: day.solarYear, month: day.solarMonth, day: day.solarDay)
        selectedDate = Calendar.current.date(from: comps) ?? Date()
    }

    func selectToday() {
        let cal = Calendar.current
        let d = cal.component(.day, from: Date())
        let m = cal.component(.month, from: Date())
        let y = cal.component(.year, from: Date())
        selectedDayInfo = LunarCalendarEngine.buildDayInfo(dd: d, mm: m, yy: y)
        selectedDate = Date()
    }

    func goToToday() {
        let cal = Calendar.current
        currentMonth = cal.component(.month, from: Date())
        currentYear = cal.component(.year, from: Date())
        loadCalendar()
        selectToday()
    }
}

// MARK: - Tasks ViewModel
@MainActor
class TasksViewModel: ObservableObject {
    @Published var tasks: [TaskItem] = []
    @Published var notes: [NoteItem] = []
    @Published var reminders: [ReminderItem] = []
    @Published var selectedTab: TaskTab = .tasks
    @Published var showAddSheet: Bool = false

    enum TaskTab { case tasks, notes, reminders }

    init() { loadSampleData() }

    private func loadSampleData() {
        let cal = Calendar.current
        tasks = [
            TaskItem(title: "Kiểm tra ngày tốt cho khai trương", isDone: false, priority: 2,
                     deadline: cal.date(byAdding: .day, value: 3, to: Date())),
            TaskItem(title: "Đặt lịch cưới hỏi tháng sau", isDone: false, priority: 2,
                     deadline: cal.date(byAdding: .day, value: 15, to: Date())),
            TaskItem(title: "Mua đồ cúng rằm tháng Giêng", isDone: true, priority: 1, deadline: nil),
        ]
        notes = [
            NoteItem(title: "Ngày xuất hành tốt", content: "Tháng 3: các ngày 3, 8, 15, 20 là ngày Hoàng Đạo, phù hợp xuất hành.", color: "gold"),
            NoteItem(title: "Hướng tốt năm Ất Tỵ", content: "Thần Tài: Đông Bắc\nHỷ Thần: Tây Nam\nHung Thần: Tây Bắc", color: "teal"),
        ]
        reminders = [
            ReminderItem(title: "Cúng rằm mùng 1", time: Date(), isActive: true, note: "Chuẩn bị hương hoa trái cây"),
            ReminderItem(title: "Giỗ Tổ Hùng Vương 10/3 ÂL", time: Date(), isActive: true, note: ""),
        ]
    }

    // MARK: Task
    func addTask(_ title: String, priority: Int = 1, deadline: Date? = nil) {
        guard !title.isEmpty else { return }
        tasks.insert(TaskItem(title: title, isDone: false, priority: priority, deadline: deadline), at: 0)
    }

    func updateTask(_ id: UUID, title: String, priority: Int, deadline: Date?) {
        guard let idx = tasks.firstIndex(where: { $0.id == id }) else { return }
        tasks[idx].title = title
        tasks[idx].priority = priority
        tasks[idx].deadline = deadline
    }

    func toggleTask(_ id: UUID) {
        if let idx = tasks.firstIndex(where: { $0.id == id }) {
            tasks[idx].isDone.toggle()
        }
    }

    func deleteTask(_ id: UUID) {
        tasks.removeAll { $0.id == id }
    }

    // MARK: Note
    func addNote(_ title: String, content: String, color: String = "gold") {
        guard !title.isEmpty else { return }
        notes.insert(NoteItem(title: title, content: content, color: color), at: 0)
    }

    func updateNote(_ id: UUID, title: String, content: String, color: String) {
        guard let idx = notes.firstIndex(where: { $0.id == id }) else { return }
        notes[idx].title = title
        notes[idx].content = content
        notes[idx].color = color
    }

    func deleteNote(_ id: UUID) {
        notes.removeAll { $0.id == id }
    }

    // MARK: Reminder
    func addReminder(_ title: String, time: Date, note: String = "") {
        guard !title.isEmpty else { return }
        reminders.insert(ReminderItem(title: title, time: time, isActive: true, note: note), at: 0)
    }

    func updateReminder(_ id: UUID, title: String, time: Date, note: String) {
        guard let idx = reminders.firstIndex(where: { $0.id == id }) else { return }
        reminders[idx].title = title
        reminders[idx].time = time
        reminders[idx].note = note
    }

    func toggleReminder(_ id: UUID) {
        if let idx = reminders.firstIndex(where: { $0.id == id }) {
            reminders[idx].isActive.toggle()
        }
    }

    func deleteReminder(_ id: UUID) {
        reminders.removeAll { $0.id == id }
    }
}

// MARK: - AI Memory (persistent)
class AIMemory: ObservableObject {
    static let shared = AIMemory()
    private let ud = UserDefaults.standard

    @Published var userName: String {
        didSet { ud.set(userName, forKey: "ai_userName") }
    }
    @Published var aiNickname: String {
        didSet { ud.set(aiNickname, forKey: "ai_nickname") }
    }
    @Published var habits: [String] {
        didSet { ud.set(habits, forKey: "ai_habits") }
    }
    // Today's context (cleared daily)
    @Published var todayContext: String {
        didSet { ud.set(todayContext, forKey: "ai_todayContext") }
    }
    private var todayContextDate: Date {
        get { ud.object(forKey: "ai_todayContextDate") as? Date ?? Date.distantPast }
        set { ud.set(newValue, forKey: "ai_todayContextDate") }
    }

    var isOnboarded: Bool { !userName.isEmpty }
    var displayName: String { aiNickname.isEmpty ? "Lịch Số AI" : aiNickname }

    init() {
        self.userName = ud.string(forKey: "ai_userName") ?? ""
        self.aiNickname = ud.string(forKey: "ai_nickname") ?? ""
        self.habits = ud.stringArray(forKey: "ai_habits") ?? []
        // Reset todayContext if from a different day
        let savedDate = ud.object(forKey: "ai_todayContextDate") as? Date ?? Date.distantPast
        if Calendar.current.isDateInToday(savedDate) {
            self.todayContext = ud.string(forKey: "ai_todayContext") ?? ""
        } else {
            self.todayContext = ""
        }
    }

    func addHabit(_ habit: String) {
        let trimmed = habit.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty, !habits.contains(trimmed) else { return }
        if habits.count >= 20 { habits.removeFirst() }
        habits.append(trimmed)
    }

    func updateTodayContext(_ ctx: String) {
        todayContext = ctx
        todayContextDate = Date()
    }

    func reset() {
        userName = ""; aiNickname = ""; habits = []; todayContext = ""
    }

    /// Build memory block for system prompt
    func systemBlock() -> String {
        var parts: [String] = []
        if !userName.isEmpty { parts.append("Tên người dùng: \(userName).") }
        if !aiNickname.isEmpty { parts.append("Người dùng gọi bạn là '\(aiNickname)'.") }
        if !habits.isEmpty { parts.append("Thói quen/sở thích đã biết: \(habits.joined(separator: "; ")).") }
        if !todayContext.isEmpty { parts.append("Bối cảnh hôm nay: \(todayContext).") }
        return parts.isEmpty ? "" : "📚 BỘ NHỚ:\n" + parts.joined(separator: "\n")
    }

    /// Simple offline response based on memory + day info
    func offlineResponse(for input: String, dayInfo: DayInfo) -> String {
        let lower = input.lowercased()
        let name = userName.isEmpty ? "bạn" : userName
        let ai = displayName

        if lower.contains("tên") && (lower.contains("tôi") || lower.contains("mình") || lower.contains("em")) {
            return "Tôi nhớ bạn tên là \(name) 😊"
        }
        if lower.contains("tên") && lower.contains("bạn") {
            return "Bạn gọi tôi là \(ai) nhé 🌟"
        }
        if lower.contains("hôm nay") || lower.contains("ngày") {
            let lunar = dayInfo.lunar
            return """
            📅 Hôm nay \(dayInfo.dayCanChi) (\(lunar.day)/\(lunar.month) âm lịch).
            ✅ Nên làm: \(dayInfo.activities.nenLam.prefix(3).joined(separator: ", ")).
            ❌ Không nên: \(dayInfo.activities.khongNen.prefix(2).joined(separator: ", ")).
            ⚠️ (Đang ở chế độ offline — không có kết nối AI)
            """
        }
        if lower.contains("giờ") && (lower.contains("tốt") || lower.contains("hoàng đạo")) {
            let gio = dayInfo.gioHoangDao.prefix(3).map { "\($0.name) \($0.time)" }.joined(separator: ", ")
            return "⏰ Giờ Hoàng Đạo hôm nay: \(gio)\n⚠️ (Chế độ offline)"
        }
        if lower.contains("hướng") || lower.contains("xuất hành") {
            return "🧭 Hướng tốt: Thần Tài – \(dayInfo.huong.thanTai), Hỷ Thần – \(dayInfo.huong.hyThan)\n⚠️ (Chế độ offline)"
        }
        return "Xin chào \(name)! Hiện tại tôi đang offline, không thể kết nối AI. Bạn hãy thử lại sau nhé 🙏\n\nTôi có thể trả lời các câu hỏi về ngày hôm nay, giờ hoàng đạo, hướng xuất hành từ dữ liệu cục bộ."
    }
}

// MARK: - Onboarding State
enum OnboardingStep { case askUserName, askAIName, done }

// MARK: - Chat ViewModel
@MainActor
class ChatViewModel: ObservableObject {
    @Published var messages: [ChatMessage] = []
    @Published var isTyping: Bool = false
    @Published var inputText: String = ""
    @Published var onboardingStep: OnboardingStep = .done

    private let geminiService = GeminiAIService()
    let memory = AIMemory.shared

    init() {
        if memory.isOnboarded {
            appendWelcomeBack()
        } else {
            onboardingStep = .askUserName
            messages.append(ChatMessage(
                content: "✨ Xin chào! Tôi là trợ lý phong thuỷ thông minh của Lịch Số.\n\nĐể tôi có thể phục vụ bạn tốt hơn, bạn tên là gì ạ? 😊",
                isUser: false, timestamp: Date()
            ))
        }
    }

    private func appendWelcomeBack() {
        let name = memory.userName
        let ai = memory.displayName
        let today = Date()
        let cal = Calendar.current
        let d = cal.component(.day, from: today)
        let m = cal.component(.month, from: today)
        let y = cal.component(.year, from: today)
        let info = LunarCalendarEngine.buildDayInfo(dd: d, mm: m, yy: y)
        messages.append(ChatMessage(
            content: "✨ Chào \(name)! Tôi là \(ai).\n\nHôm nay \(info.dayCanChi) — \(info.activities.nenLam.first ?? "ngày bình thường") 🌟\n\nBạn cần tôi giúp gì hôm nay?",
            isUser: false, timestamp: Date()
        ))
    }

    func sendMessage(_ text: String) {
        let trimmed = text.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else { return }

        messages.append(ChatMessage(content: trimmed, isUser: true, timestamp: Date()))

        // Onboarding flow
        switch onboardingStep {
        case .askUserName:
            // Nhận biết user đang hỏi câu hỏi thay vì đưa tên
            if looksLikeQuestion(trimmed) {
                // Bỏ qua onboarding, trả lời câu hỏi ngay, đặt tên mặc định
                memory.userName = "bạn"
                onboardingStep = .done
                learnFromMessage(trimmed)
                isTyping = true
                Task {
                    do {
                        let response = try await geminiService.chat(
                            userMessage: trimmed,
                            history: ArraySlice(messages.dropLast()),
                            memory: memory
                        )
                        try await Task.sleep(nanoseconds: 400_000_000)
                        isTyping = false
                        messages.append(ChatMessage(content: response, isUser: false, timestamp: Date()))
                    } catch {
                        isTyping = false
                        let today = Date()
                        let cal = Calendar.current
                        let info = LunarCalendarEngine.buildDayInfo(
                            dd: cal.component(.day, from: today),
                            mm: cal.component(.month, from: today),
                            yy: cal.component(.year, from: today)
                        )
                        messages.append(ChatMessage(content: memory.offlineResponse(for: trimmed, dayInfo: info), isUser: false, timestamp: Date()))
                    }
                }
                return
            }
            // Tên hợp lệ — lưu và hỏi tên AI
            let name = extractName(trimmed)
            memory.userName = name
            onboardingStep = .askAIName
            messages.append(ChatMessage(
                content: "Rất vui được gặp bạn, \(name)! 🎉\n\nBạn muốn gọi tôi bằng tên thân mật nào? (Ví dụ: \"Sao\", \"Ngọc\", hay cứ gọi \"Lịch Số AI\" cũng được)",
                isUser: false, timestamp: Date()
            ))
            return
        case .askAIName:
            let nickname = trimmed.lowercased().contains("lịch số") ? "" : trimmed
            memory.aiNickname = nickname
            onboardingStep = .done
            let ai = memory.displayName
            messages.append(ChatMessage(
                content: "Tuyệt! Từ giờ bạn gọi tôi là \"\(ai)\" nhé 💫\n\nTôi đã ghi nhớ tên bạn và sẽ nhớ những thói quen của bạn theo thời gian.\n\nBây giờ hỏi tôi bất cứ điều gì về phong thuỷ, lịch âm, ngày tốt xấu nhé!",
                isUser: false, timestamp: Date()
            ))
            return
        case .done:
            break
        }

        // Learn habits from user message
        learnFromMessage(trimmed)
        isTyping = true

        Task {
            do {
                let response = try await geminiService.chat(
                    userMessage: trimmed,
                    history: ArraySlice(messages.dropLast()),
                    memory: memory
                )
                try await Task.sleep(nanoseconds: 400_000_000)
                isTyping = false
                messages.append(ChatMessage(content: response, isUser: false, timestamp: Date()))
            } catch {
                isTyping = false
                // Offline fallback
                let today = Date()
                let cal = Calendar.current
                let d = cal.component(.day, from: today)
                let m = cal.component(.month, from: today)
                let y = cal.component(.year, from: today)
                let info = LunarCalendarEngine.buildDayInfo(dd: d, mm: m, yy: y)
                let offline = memory.offlineResponse(for: trimmed, dayInfo: info)
                messages.append(ChatMessage(content: offline, isUser: false, timestamp: Date()))
            }
        }
    }

    /// Nhận biết user đang hỏi câu hỏi/yêu cầu thay vì đưa tên
    private func looksLikeQuestion(_ text: String) -> Bool {
        let lower = text.lowercased()
        // Câu hỏi có dấu hỏi
        if text.contains("?") { return true }
        // Bắt đầu bằng từ hỏi tiếng Việt
        let questionStarters = ["hôm nay", "ngày", "giờ", "tháng", "năm", "khi nào", "làm sao",
                                 "tại sao", "vì sao", "như thế nào", "có nên", "nên", "cho tôi",
                                 "giúp", "tìm", "xem", "kiểm tra", "tra cứu", "hướng", "tuổi",
                                 "mệnh", "động thổ", "khai trương", "cưới", "xuất hành", "sinh",
                                 "phong thuỷ", "âm lịch", "can chi", "ngũ hành", "hoàng đạo"]
        for kw in questionStarters { if lower.hasPrefix(kw) || lower.contains(kw) { return true } }
        // Quá dài để là tên (>30 ký tự thường là câu)
        if text.count > 30 { return true }
        // Có nhiều hơn 3 từ và không phải tên ghép
        let words = text.split(separator: " ")
        if words.count > 4 { return true }
        return false
    }

    /// Trích xuất tên sạch từ câu trả lời (vd: "Tôi là Zin" → "Zin")
    private func extractName(_ text: String) -> String {
        let lower = text.lowercased()
        // "tôi là X", "mình là X", "tên tôi là X", "tên mình là X"
        let prefixes = ["tên tôi là ", "tên mình là ", "tôi là ", "mình là ", "tên là "]
        for prefix in prefixes {
            if lower.hasPrefix(prefix) {
                let extracted = String(text.dropFirst(prefix.count)).trimmingCharacters(in: .whitespaces)
                if !extracted.isEmpty { return extracted }
            }
        }
        // Nếu chỉ 1-2 từ, dùng thẳng
        return text.trimmingCharacters(in: .whitespaces)
    }

    private func learnFromMessage(_ text: String) {
        let lower = text.lowercased()
        // Detect habits/preferences patterns
        let patterns: [(String, String)] = [
            ("thích", "Thích: \(text)"),
            ("hay làm", "Thường: \(text)"),
            ("thường", "Thói quen: \(text)"),
            ("mỗi ngày", "Hàng ngày: \(text)"),
            ("khai trương", "Quan tâm khai trương"),
            ("cưới", "Quan tâm cưới hỏi"),
            ("xuất hành", "Hay xuất hành"),
            ("kinh doanh", "Làm kinh doanh"),
        ]
        for (keyword, habit) in patterns {
            if lower.contains(keyword) && !memory.habits.contains(where: { $0.lowercased().contains(keyword) }) {
                memory.addHabit(habit)
                break
            }
        }
        // Update today context with last user intent
        if text.count > 10 {
            memory.updateTodayContext("Câu hỏi gần nhất: \(text.prefix(80))")
        }
    }

    func clearChat() {
        messages.removeAll()
        let name = memory.userName.isEmpty ? "" : " \(memory.userName)"
        messages.append(ChatMessage(
            content: "✨ Lịch sử chat đã được xoá. Tôi sẵn sàng hỗ trợ bạn\(name)!",
            isUser: false, timestamp: Date()
        ))
    }

    func formatTime(_ date: Date) -> String {
        let f = DateFormatter()
        f.dateFormat = "HH:mm"
        return f.string(from: date)
    }
}

// MARK: - Settings ViewModel
@MainActor
class SettingsViewModel: ObservableObject {

    // ─── Persisted via UserDefaults (@AppStorage-like pattern) ───
    @Published var notifyEnabled: Bool {
        didSet {
            UserDefaults.standard.set(notifyEnabled, forKey: "notifyEnabled")
            handleNotificationToggle(notifyEnabled)
        }
    }
    @Published var lunarBadgeEnabled: Bool {
        didSet { UserDefaults.standard.set(lunarBadgeEnabled, forKey: "lunarBadgeEnabled") }
    }
    @Published var gioDaiCatEnabled: Bool {
        didSet {
            UserDefaults.standard.set(gioDaiCatEnabled, forKey: "gioDaiCatEnabled")
            handleGioDaiCatToggle(gioDaiCatEnabled)
        }
    }
    @Published var darkModeEnabled: Bool {
        didSet { UserDefaults.standard.set(darkModeEnabled, forKey: "darkModeEnabled") }
    }

    // ─── UI State ───
    @Published var showPrivacyPolicy: Bool = false
    @Published var showHelp: Bool = false
    @Published var showShareSheet: Bool = false
    @Published var notificationPermissionDenied: Bool = false
    @Published var toastMessage: String? = nil

    // ─── Computed ───
    var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
    }

    var cacheSize: String {
        let url = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first
        guard let cacheURL = url else { return "N/A" }
        let bytes = (try? FileManager.default.allocatedSizeOfDirectory(at: cacheURL)) ?? 0
        if bytes < 1024 { return "\(bytes) B" }
        if bytes < 1024 * 1024 { return "\(bytes / 1024) KB" }
        return String(format: "%.1f MB", Double(bytes) / (1024.0 * 1024.0))
    }

    init() {
        let ud = UserDefaults.standard
        self.notifyEnabled = ud.object(forKey: "notifyEnabled") as? Bool ?? true
        self.lunarBadgeEnabled = ud.object(forKey: "lunarBadgeEnabled") as? Bool ?? true
        self.gioDaiCatEnabled = ud.object(forKey: "gioDaiCatEnabled") as? Bool ?? false
        self.darkModeEnabled = ud.object(forKey: "darkModeEnabled") as? Bool ?? true
    }

    // MARK: - Notification Logic

    func handleNotificationToggle(_ enabled: Bool) {
        if enabled {
            UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
                Task { @MainActor in
                    if granted {
                        self.toastMessage = "Đã bật thông báo nhắc nhở"
                    } else {
                        // Revert — user denied permission
                        self.notifyEnabled = false
                        UserDefaults.standard.set(false, forKey: "notifyEnabled")
                        self.notificationPermissionDenied = true
                    }
                }
            }
        } else {
            // Cancel all pending non-GioDaiCat notifications
            UNUserNotificationCenter.current().getPendingNotificationRequests { requests in
                let ids = requests
                    .filter { !$0.identifier.hasPrefix("giodaicat") }
                    .map { $0.identifier }
                UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: ids)
            }
            toastMessage = "Đã tắt thông báo nhắc nhở"
        }
    }

    func handleGioDaiCatToggle(_ enabled: Bool) {
        let center = UNUserNotificationCenter.current()
        if enabled {
            center.requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
                Task { @MainActor in
                    if granted {
                        self.scheduleGioDaiCatNotification()
                        self.toastMessage = "Sẽ nhận thông báo giờ hoàng đạo lúc 6h sáng"
                    } else {
                        self.gioDaiCatEnabled = false
                        UserDefaults.standard.set(false, forKey: "gioDaiCatEnabled")
                        self.notificationPermissionDenied = true
                    }
                }
            }
        } else {
            center.removePendingNotificationRequests(withIdentifiers: ["giodaicat_daily"])
            toastMessage = "Đã tắt thông báo giờ hoàng đạo"
        }
    }

    private func scheduleGioDaiCatNotification() {
        let center = UNUserNotificationCenter.current()
        center.removePendingNotificationRequests(withIdentifiers: ["giodaicat_daily"])

        let content = UNMutableNotificationContent()
        content.title = "🌟 Giờ Hoàng Đạo hôm nay"
        content.body = "Mở Lịch Số để xem giờ hoàng đạo và lịch âm hôm nay!"
        content.sound = .default

        var dateComponents = DateComponents()
        dateComponents.hour = 6
        dateComponents.minute = 0
        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)

        let request = UNNotificationRequest(
            identifier: "giodaicat_daily",
            content: content,
            trigger: trigger
        )
        center.add(request)
    }

    // MARK: - Share & Rate

    func shareApp() {
        showShareSheet = true
    }

    func rateApp() {
        // Replace YOUR_APP_ID with real App Store ID when published
        let appStoreId = "YOUR_APP_ID"
        let urlStr = "itms-apps://itunes.apple.com/app/id\(appStoreId)?action=write-review"
        if let url = URL(string: urlStr), UIApplication.shared.canOpenURL(url) {
            UIApplication.shared.open(url)
        } else {
            toastMessage = "Tính năng đánh giá sẽ có khi ứng dụng lên App Store"
        }
    }

    func clearCache() {
        if let cacheURL = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first {
            try? FileManager.default.removeItem(at: cacheURL)
            try? FileManager.default.createDirectory(at: cacheURL, withIntermediateDirectories: true)
        }
        toastMessage = "Đã xoá cache"
    }

    func consumeToast() {
        toastMessage = nil
    }
}

// MARK: - FileManager extension for directory size
extension FileManager {
    func allocatedSizeOfDirectory(at url: URL) throws -> Int {
        var size: Int = 0
        let keys: [URLResourceKey] = [.isRegularFileKey, .fileAllocatedSizeKey, .totalFileAllocatedSizeKey]
        guard let enumerator = self.enumerator(at: url, includingPropertiesForKeys: keys,
                                               options: .skipsHiddenFiles) else { return 0 }
        for case let fileURL as URL in enumerator {
            let rv = try fileURL.resourceValues(forKeys: Set(keys))
            guard rv.isRegularFile == true else { continue }
            size += rv.totalFileAllocatedSize ?? rv.fileAllocatedSize ?? 0
        }
        return size
    }
}

// MARK: - OpenRouter AI Service
class GeminiAIService {
    private let apiKey: String = {
        guard let key = Bundle.main.infoDictionary?["OpenRouterAPIKey"] as? String, !key.isEmpty else {
            assertionFailure("OpenRouterAPIKey not found in Info.plist")
            return ""
        }
        return key
    }()
    private let endpoint = "https://openrouter.ai/api/v1/chat/completions"
    private let model = "google/gemini-2.0-flash-001"

    func chat(userMessage: String, history: ArraySlice<ChatMessage>, memory: AIMemory? = nil) async throws -> String {
        // Build context
        let today = Date()
        let cal = Calendar.current
        let d = cal.component(.day, from: today)
        let m = cal.component(.month, from: today)
        let y = cal.component(.year, from: today)
        let info = LunarCalendarEngine.buildDayInfo(dd: d, mm: m, yy: y)
        let lunar = info.lunar
        let systemPrompt = """
        Bạn là trợ lý phong thuỷ và lịch vạn niên thông minh của app Lịch Số.
        Hôm nay: \(d)/\(m)/\(y) dương lịch = \(lunar.day)/\(lunar.month)/\(lunar.year) âm lịch.
        Ngày \(info.dayCanChi), tháng \(info.monthCanChi), năm \(info.yearCanChi).
        Giờ Hoàng Đạo: \(info.gioHoangDao.map { "\($0.name) (\($0.time))" }.joined(separator: ", ")).
        Nên làm: \(info.activities.nenLam.joined(separator: ", ")).
        Không nên: \(info.activities.khongNen.joined(separator: ", ")).
        Hướng tốt — Thần Tài: \(info.huong.thanTai), Hỷ Thần: \(info.huong.hyThan).
        \(memory?.systemBlock() ?? "")
        Hãy trả lời bằng tiếng Việt, ngắn gọn, thân thiện. Dùng emoji phù hợp. Xưng hô với người dùng theo tên nếu biết.
        """

        // Build messages array (OpenAI-compatible format)
        var messages: [[String: String]] = [
            ["role": "system", "content": systemPrompt]
        ]
        for msg in history.suffix(6) {
            messages.append([
                "role": msg.isUser ? "user" : "assistant",
                "content": msg.content
            ])
        }
        messages.append(["role": "user", "content": userMessage])

        let body: [String: Any] = [
            "model": model,
            "messages": messages,
            "temperature": 0.8,
            "max_tokens": 512
        ]

        guard let url = URL(string: endpoint) else {
            throw URLError(.badURL)
        }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        request.setValue("https://lichso.app", forHTTPHeaderField: "HTTP-Referer")
        request.setValue("Lịch Số iOS", forHTTPHeaderField: "X-Title")
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, response) = try await URLSession.shared.data(for: request)

        // Debug: check HTTP status
        if let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode != 200 {
            let errBody = String(data: data, encoding: .utf8) ?? "unknown"
            throw NSError(domain: "OpenRouter", code: httpResponse.statusCode,
                          userInfo: [NSLocalizedDescriptionKey: errBody])
        }

        if let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
           let choices = json["choices"] as? [[String: Any]],
           let message = choices.first?["message"] as? [String: Any],
           let text = message["content"] as? String {
            return text
        }
        throw URLError(.cannotParseResponse)
    }
}
