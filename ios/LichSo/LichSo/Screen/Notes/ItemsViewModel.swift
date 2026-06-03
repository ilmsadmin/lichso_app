import SwiftUI
import SwiftData

// ═══════════════════════════════════════════
// ItemsViewModel — quản lý danh sách Item hợp nhất (ghi chú + việc + nhắc).
// Port từ Android `ItemsViewModel.kt`. Dùng SwiftData (ModelContext) để lưu thật.
// ═══════════════════════════════════════════

/// Bộ lọc cho danh sách hợp nhất.
enum ItemFilter: CaseIterable {
    case all, tasks, reminders, notes, pinned
    var label: String {
        switch self {
        case .all: return "Tất cả"
        case .tasks: return "Cần làm"
        case .reminders: return "Có nhắc"
        case .notes: return "Ghi chú"
        case .pinned: return "Đã ghim"
        }
    }
}

/// Giá trị soạn thảo từ ItemEditScreen (tránh thao tác trực tiếp managed object khi tạo mới).
struct ItemDraft {
    var title: String = ""
    var description: String = ""
    var tags: String = ""
    var isTask: Bool = false
    var priority: Int = 1
    var dueDate: Date? = nil
    var hasReminder: Bool = false
    var reminderDate: Date = Date()
    var repeatType: Int = 0
    var useLunar: Bool = false
    var advanceDays: Int = 0
    var isPinned: Bool = false
    var colorIndex: Int = 0
}

@MainActor
final class ItemsViewModel: ObservableObject {
    @Published var items: [ItemEntity] = []
    @Published var filter: ItemFilter = .all
    @Published var query: String = ""
    @Published var deletingItem: ItemEntity?

    // AI
    @Published var isAiProcessing = false
    @Published var aiMessage: String?
    @Published var aiError: String?

    private var modelContext: ModelContext?

    func setModelContext(_ context: ModelContext) {
        self.modelContext = context
        load()
    }

    func load() {
        guard let ctx = modelContext else { return }
        let descriptor = FetchDescriptor<ItemEntity>(sortBy: [SortDescriptor(\.updatedAt, order: .reverse)])
        let fetched = (try? ctx.fetch(descriptor)) ?? []
        // Ghim lên đầu → việc chưa xong trước → mới cập nhật trước
        items = fetched.sorted { a, b in
            if a.isPinned != b.isPinned { return a.isPinned }
            if a.isDone != b.isDone { return !a.isDone }
            return a.updatedAt > b.updatedAt
        }
    }

    // ── Lọc / tìm kiếm ──
    var visibleItems: [ItemEntity] {
        let q = query.trimmingCharacters(in: .whitespaces).lowercased()
        return items.filter { item in
            let matchFilter: Bool
            switch filter {
            case .all: matchFilter = true
            case .tasks: matchFilter = item.isTask
            case .reminders: matchFilter = item.hasReminder
            case .notes: matchFilter = !item.isTask && !item.hasReminder
            case .pinned: matchFilter = item.isPinned
            }
            let matchQuery = q.isEmpty
                || item.title.lowercased().contains(q)
                || item.itemDescription.lowercased().contains(q)
                || item.tags.lowercased().contains(q)
            return matchFilter && matchQuery
        }
    }

    var pendingTaskCount: Int { items.filter { $0.isTask && !$0.isDone }.count }
    var reminderCount: Int { items.filter { $0.hasReminder && $0.reminderEnabled }.count }
    var totalCount: Int { items.count }

    // ── CRUD ──
    private func reminderAtMillis(_ draft: ItemDraft) -> Int64? {
        guard draft.hasReminder else { return nil }
        return Int64(draft.reminderDate.timeIntervalSince1970 * 1000)
    }

    /// Tạo mới (existing==nil) hoặc cập nhật. Tự lên lịch / huỷ nhắc nhở.
    func save(_ draft: ItemDraft, existing: ItemEntity?) {
        guard let ctx = modelContext else { return }
        var title = draft.title.trimmingCharacters(in: .whitespacesAndNewlines)
        let desc = draft.description.trimmingCharacters(in: .whitespacesAndNewlines)
        if title.isEmpty && desc.isEmpty { return }
        if title.isEmpty { title = String(desc.prefix(50)) }
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let dueMillis = draft.isTask ? draft.dueDate.map { Int64($0.timeIntervalSince1970 * 1000) } : nil

        let item: ItemEntity
        if let existing {
            item = existing
            item.title = title
            item.itemDescription = desc
            item.tags = draft.tags.trimmingCharacters(in: .whitespaces)
            item.isTask = draft.isTask
            item.priority = draft.priority
            item.dueDate = dueMillis
            item.hasReminder = draft.hasReminder
            item.reminderAt = reminderAtMillis(draft)
            item.repeatType = draft.repeatType
            item.useLunar = draft.useLunar
            item.advanceDays = draft.advanceDays
            item.reminderEnabled = draft.hasReminder ? item.reminderEnabled : true
            item.isPinned = draft.isPinned
            item.colorIndex = draft.colorIndex
            item.updatedAt = now
        } else {
            item = ItemEntity(
                id: now,
                title: title,
                itemDescription: desc,
                tags: draft.tags.trimmingCharacters(in: .whitespaces),
                isTask: draft.isTask,
                priority: draft.priority,
                dueDate: dueMillis,
                hasReminder: draft.hasReminder,
                reminderAt: reminderAtMillis(draft),
                repeatType: draft.repeatType,
                useLunar: draft.useLunar,
                advanceDays: draft.advanceDays,
                isPinned: draft.isPinned,
                colorIndex: draft.colorIndex
            )
            ctx.insert(item)
        }
        try? ctx.save()
        ItemReminderScheduler.schedule(item)
        load()
    }

    func toggleDone(_ item: ItemEntity) {
        item.isDone.toggle()
        item.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
        try? modelContext?.save()
        load()
    }

    func togglePin(_ item: ItemEntity) {
        item.isPinned.toggle()
        item.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
        try? modelContext?.save()
        load()
    }

    func toggleReminder(_ item: ItemEntity) {
        item.reminderEnabled.toggle()
        try? modelContext?.save()
        ItemReminderScheduler.schedule(item)
        load()
    }

    func requestDelete(_ item: ItemEntity) { deletingItem = item }
    func cancelDelete() { deletingItem = nil }

    func confirmDelete() {
        guard let item = deletingItem, let ctx = modelContext else { return }
        ItemReminderScheduler.cancel(item.id)
        ctx.delete(item)
        try? ctx.save()
        deletingItem = nil
        load()
    }

    // ── Helpers ──
    func formatDateTime(_ millis: Int64) -> String { Self.df("dd/MM · HH:mm").string(from: date(millis)) }
    func formatDate(_ millis: Int64) -> String { Self.df("dd/MM/yyyy").string(from: date(millis)) }
    func priorityLabel(_ p: Int) -> String { p == 2 ? "Cao" : (p == 1 ? "Vừa" : "Thấp") }

    private func date(_ millis: Int64) -> Date { Date(timeIntervalSince1970: Double(millis) / 1000) }
    private static func df(_ format: String) -> DateFormatter {
        let f = DateFormatter(); f.locale = Locale(identifier: "vi_VN"); f.dateFormat = format; return f
    }

    // ════════════════════════════════════════════
    // AI — map AiActionResult → thao tác DB
    // ════════════════════════════════════════════

    func processAiCommand(_ input: String) {
        let trimmed = input.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        isAiProcessing = true; aiMessage = nil; aiError = nil
        Task {
            let result = await AiTaskService.processCommand(trimmed, existingContext: buildContext())
            await MainActor.run { apply(result) }
        }
    }

    private func apply(_ result: AiActionResult) {
        guard let ctx = modelContext else { isAiProcessing = false; return }
        switch result.action {
        case "create_task", "bulk_create", "create_note", "create_reminder":
            for ai in result.items {
                let entity = aiItemToEntity(ai)
                ctx.insert(entity)
                if entity.hasReminder { ItemReminderScheduler.schedule(entity) }
            }
        case "delete_task", "delete_note", "delete_reminder":
            for ai in result.items {
                let kw = ai.title.lowercased()
                if let found = items.first(where: { $0.title.lowercased().contains(kw) }) {
                    ItemReminderScheduler.cancel(found.id)
                    ctx.delete(found)
                }
            }
        case "edit_task", "edit_note":
            for ai in result.items {
                let old = ai.description.replacingOccurrences(of: "old_title:", with: "").lowercased()
                if let found = items.first(where: { $0.title.lowercased().contains(old) }) {
                    found.title = ai.title
                    found.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
                }
            }
        case "edit_task_priority":
            for ai in result.items {
                let kw = ai.title.lowercased()
                if let found = items.first(where: { $0.title.lowercased().contains(kw) }) {
                    found.priority = ai.priority
                    found.isTask = true
                    found.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
                }
            }
        case "mark_done":
            for ai in result.items {
                let kw = ai.title.lowercased()
                if let found = items.first(where: { $0.title.lowercased().contains(kw) && !$0.isDone }) {
                    found.isDone = true
                    found.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
                }
            }
        case "stats_all", "stats_tasks", "stats_notes", "stats_reminders":
            isAiProcessing = false
            aiMessage = buildStats(result.action)
            return
        default:
            break
        }
        try? ctx.save()
        load()
        isAiProcessing = false
        aiMessage = result.message
    }

    private func aiItemToEntity(_ ai: AiItem) -> ItemEntity {
        switch ai.type {
        case "reminder":
            return ItemEntity(
                title: ai.title, itemDescription: ai.description,
                hasReminder: true, reminderAt: parseTimeToMillis(ai.time), repeatType: ai.repeatType
            )
        case "task":
            return ItemEntity(
                title: ai.title, itemDescription: ai.description,
                isTask: true, priority: ai.priority,
                dueDate: ai.dueDate.flatMap(parseDateToMillis)
            )
        default:
            return ItemEntity(title: ai.title, itemDescription: ai.description, colorIndex: ai.colorIndex)
        }
    }

    private func buildContext() -> String {
        guard !items.isEmpty else { return "" }
        var sb = "Các mục hiện tại (\(items.count)):\n"
        for i in items.prefix(15) {
            let kind = i.isTask ? (i.isDone ? "việc-xong" : "việc") : (i.hasReminder ? "nhắc" : "ghi-chú")
            sb += "- [id:\(i.id)] \(i.title) (\(kind))\n"
        }
        return sb
    }

    private func buildStats(_ action: String) -> String {
        let tasks = items.filter { $0.isTask }
        let reminders = items.filter { $0.hasReminder }
        let notes = items.filter { !$0.isTask && !$0.hasReminder }
        var lines: [String] = []
        if action == "stats_tasks" || action == "stats_all" {
            let done = tasks.filter { $0.isDone }.count
            lines.append("✦ CÔNG VIỆC — Tổng \(tasks.count) · ✅ \(done) · ⬜ \(tasks.count - done)")
        }
        if action == "stats_notes" || action == "stats_all" {
            lines.append("◇ GHI CHÚ — \(notes.count) mục")
        }
        if action == "stats_reminders" || action == "stats_all" {
            let on = reminders.filter { $0.reminderEnabled }.count
            lines.append("◷ NHẮC NHỞ — Tổng \(reminders.count) · ✅ Bật \(on)")
        }
        if action == "stats_all" { lines.append("\n⊛ Tổng cộng: \(items.count) mục") }
        return lines.joined(separator: "\n")
    }

    func dismissAiMessage() { aiMessage = nil; aiError = nil }

    private func parseDateToMillis(_ s: String) -> Int64? {
        let f = DateFormatter(); f.dateFormat = "yyyy-MM-dd"
        return f.date(from: s).map { Int64($0.timeIntervalSince1970 * 1000) }
    }

    private func parseTimeToMillis(_ s: String?) -> Int64 {
        var comps = Calendar.current.dateComponents([.year, .month, .day], from: Date())
        comps.second = 0
        if let s, !s.isEmpty {
            let parts = s.split(separator: ":")
            comps.hour = min(max(Int(parts.first ?? "9") ?? 9, 0), 23)
            comps.minute = parts.count > 1 ? min(max(Int(parts[1]) ?? 0, 0), 59) : 0
        }
        let date = Calendar.current.date(from: comps) ?? Date()
        return Int64(date.timeIntervalSince1970 * 1000)
    }
}
