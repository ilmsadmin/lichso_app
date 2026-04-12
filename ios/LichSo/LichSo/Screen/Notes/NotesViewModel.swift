import SwiftUI
import SwiftData

// ═══════════════════════════════════════════
// NotesViewModel — Manages Notes, Tasks, Reminders
// Uses SwiftData for real persistence
// ═══════════════════════════════════════════

enum NotesTab: Int, CaseIterable {
    case notes = 0, tasks = 1, reminders = 2
    var label: String {
        switch self { case .notes: return "Ghi chú"; case .tasks: return "Việc cần làm"; case .reminders: return "Nhắc nhở" }
    }
    var icon: String {
        switch self { case .notes: return "note.text"; case .tasks: return "checkmark.circle"; case .reminders: return "alarm" }
    }
}

@MainActor
class NotesViewModel: ObservableObject {
    @Published var selectedTab: NotesTab = .notes
    @Published var searchText = ""
    @Published var notes: [NoteEntity] = []
    @Published var tasks: [TaskEntity] = []
    @Published var reminders: [ReminderEntity] = []

    private var modelContext: ModelContext?

    func setModelContext(_ context: ModelContext) {
        self.modelContext = context
        loadAll()
    }

    func loadAll() {
        loadNotes(); loadTasks(); loadReminders()
    }

    // ── Notes ──
    func loadNotes() {
        guard let ctx = modelContext else { return }
        let descriptor = FetchDescriptor<NoteEntity>(sortBy: [
            SortDescriptor(\.updatedAt, order: .reverse)
        ])
        do {
            let fetched = try ctx.fetch(descriptor)
            // Sort pinned first, then by updatedAt desc
            notes = fetched.sorted { a, b in
                if a.isPinned != b.isPinned { return a.isPinned }
                return a.updatedAt > b.updatedAt
            }
        } catch { print("Notes fetch error: \(error)") }
    }

    func createNote(title: String, content: String, colorIndex: Int = 0) {
        guard let ctx = modelContext else { return }
        let id = Int64(Date().timeIntervalSince1970 * 1000)
        let note = NoteEntity(id: id, title: title, content: content, colorIndex: colorIndex)
        ctx.insert(note)
        try? ctx.save()
        loadNotes()
    }

    func updateNote(_ note: NoteEntity, title: String, content: String, colorIndex: Int, isPinned: Bool, labels: String) {
        note.title = title
        note.content = content
        note.colorIndex = colorIndex
        note.isPinned = isPinned
        note.labels = labels
        note.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
        try? modelContext?.save()
        loadNotes()
    }

    func deleteNote(_ note: NoteEntity) {
        guard let ctx = modelContext else { return }
        ctx.delete(note)
        try? ctx.save()
        loadNotes()
    }

    func togglePin(_ note: NoteEntity) {
        note.isPinned.toggle()
        note.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
        try? modelContext?.save()
        loadNotes()
    }

    // ── Tasks ──
    func loadTasks() {
        guard let ctx = modelContext else { return }
        let descriptor = FetchDescriptor<TaskEntity>(sortBy: [
            SortDescriptor(\.priority, order: .reverse),
            SortDescriptor(\.createdAt, order: .reverse)
        ])
        do {
            let fetched = try ctx.fetch(descriptor)
            // Sort: pending first, then by priority desc, then createdAt desc
            tasks = fetched.sorted { a, b in
                if a.isDone != b.isDone { return !a.isDone }
                if a.priority != b.priority { return a.priority > b.priority }
                return a.createdAt > b.createdAt
            }
        } catch { print("Tasks fetch error: \(error)") }
    }

    func createTask(title: String, description: String = "", priority: Int = 1, dueDate: Date? = nil) {
        guard let ctx = modelContext else { return }
        let id = Int64(Date().timeIntervalSince1970 * 1000)
        let task = TaskEntity(
            id: id, title: title, taskDescription: description,
            dueDate: dueDate.map { Int64($0.timeIntervalSince1970 * 1000) },
            priority: priority
        )
        ctx.insert(task)
        try? ctx.save()
        loadTasks()
    }

    func toggleTask(_ task: TaskEntity) {
        task.isDone.toggle()
        task.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
        try? modelContext?.save()
        loadTasks()
    }

    func updateTask(_ task: TaskEntity, title: String, description: String, priority: Int, dueDate: Date?, hasReminder: Bool, labels: String) {
        task.title = title
        task.taskDescription = description
        task.priority = priority
        task.dueDate = dueDate.map { Int64($0.timeIntervalSince1970 * 1000) }
        task.hasReminder = hasReminder
        task.labels = labels
        task.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
        try? modelContext?.save()
        loadTasks()
    }

    func deleteTask(_ task: TaskEntity) {
        guard let ctx = modelContext else { return }
        ctx.delete(task)
        try? ctx.save()
        loadTasks()
    }

    // ── Reminders ──
    func loadReminders() {
        guard let ctx = modelContext else { return }
        let descriptor = FetchDescriptor<ReminderEntity>(sortBy: [
            SortDescriptor(\.triggerTime, order: .forward)
        ])
        do { reminders = try ctx.fetch(descriptor) } catch { print("Reminders fetch error: \(error)") }
    }

    func createReminder(title: String, subtitle: String = "", triggerTime: Date, repeatType: Int = 0, category: Int = 3) {
        guard let ctx = modelContext else { return }
        let id = Int64(Date().timeIntervalSince1970 * 1000)
        let reminder = ReminderEntity(
            id: id, title: title, subtitle: subtitle,
            triggerTime: Int64(triggerTime.timeIntervalSince1970 * 1000),
            repeatType: repeatType, category: category
        )
        ctx.insert(reminder)
        try? ctx.save()
        loadReminders()
    }

    func toggleReminder(_ reminder: ReminderEntity) {
        reminder.isEnabled.toggle()
        try? modelContext?.save()
        loadReminders()
    }

    func updateReminder(_ reminder: ReminderEntity, title: String, subtitle: String, triggerTime: Date, repeatType: Int, useLunar: Bool, advanceDays: Int, category: Int, isEnabled: Bool, labels: String) {
        reminder.title = title
        reminder.subtitle = subtitle
        reminder.triggerTime = Int64(triggerTime.timeIntervalSince1970 * 1000)
        reminder.repeatType = repeatType
        reminder.useLunar = useLunar
        reminder.advanceDays = advanceDays
        reminder.category = category
        reminder.isEnabled = isEnabled
        reminder.labels = labels
        try? modelContext?.save()
        loadReminders()
    }

    func deleteReminder(_ reminder: ReminderEntity) {
        guard let ctx = modelContext else { return }
        ctx.delete(reminder)
        try? ctx.save()
        loadReminders()
    }

    // ── Filtered ──
    var filteredNotes: [NoteEntity] {
        guard !searchText.isEmpty else { return notes }
        let q = searchText.lowercased()
        return notes.filter { $0.title.lowercased().contains(q) || $0.content.lowercased().contains(q) }
    }
    var filteredTasks: [TaskEntity] {
        guard !searchText.isEmpty else { return tasks }
        let q = searchText.lowercased()
        return tasks.filter { $0.title.lowercased().contains(q) || $0.taskDescription.lowercased().contains(q) }
    }
    var filteredReminders: [ReminderEntity] {
        guard !searchText.isEmpty else { return reminders }
        let q = searchText.lowercased()
        return reminders.filter { $0.title.lowercased().contains(q) || $0.subtitle.lowercased().contains(q) }
    }

    // ── Stats ──
    var noteCount: Int { notes.count }
    var pendingTaskCount: Int { tasks.filter { !$0.isDone }.count }
    var activeReminderCount: Int { reminders.filter { $0.isEnabled }.count }

    // ── Helpers ──
    static func formatDate(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(timestamp) / 1000)
        let f = DateFormatter()
        f.locale = Locale(identifier: "vi_VN")
        f.dateFormat = "dd/MM/yyyy"
        return f.string(from: date)
    }

    static func formatTime(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(timestamp) / 1000)
        let f = DateFormatter()
        f.dateFormat = "HH:mm"
        return f.string(from: date)
    }

    static func relativeDate(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(timestamp) / 1000)
        let diff = Calendar.current.dateComponents([.day], from: date, to: Date())
        if let days = diff.day {
            if days == 0 { return "Hôm nay" }
            if days == 1 { return "Hôm qua" }
            if days < 7 { return "\(days) ngày trước" }
        }
        return formatDate(timestamp)
    }

    static let repeatLabels = ["Một lần", "Hàng ngày", "Hàng tuần", "Hàng tháng", "Âm lịch hàng tháng", "Hàng năm"]
    static let categoryIcons = ["party.popper.fill", "birthday.cake.fill", "moon.fill", "person.fill", "flame.fill"]
    static let categoryColors: [Color] = [Color(hex: "E65100"), Color(hex: "C62828"), Color(hex: "F57F17"), Color(hex: "1565C0"), Color(hex: "7B1FA2")]
    static let categoryBgs: [Color] = [Color(hex: "FFF3E0"), Color(hex: "FFEBEE"), Color(hex: "FFF8E1"), Color(hex: "E3F2FD"), Color(hex: "F3E5F5")]
}
