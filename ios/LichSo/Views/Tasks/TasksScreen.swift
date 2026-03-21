import SwiftUI

// MARK: - Swipe-to-Delete + Double-tap-to-Edit Modifier
struct InteractiveRowModifier: ViewModifier {
    let onEdit: () -> Void
    let onDeleteRequest: () -> Void

    @State private var offset: CGFloat = 0
    @State private var isDragging = false
    private let deleteThreshold: CGFloat = 80
    private let deleteReveal: CGFloat = 72

    func body(content: Content) -> some View {
        ZStack(alignment: .trailing) {
            RoundedRectangle(cornerRadius: 14)
                .fill(Color.red.opacity(min(Double(abs(offset) / deleteReveal), 1) * 0.85))
                .overlay(alignment: .trailing) {
                    Image(systemName: abs(offset) > 20 ? "trash.fill" : "trash")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.white)
                        .opacity(Double(min(abs(offset) / 30, 1)))
                        .padding(.trailing, 18)
                        .scaleEffect(abs(offset) >= deleteThreshold ? 1.2 : 1.0)
                        .animation(.spring(response: 0.2), value: offset)
                }

            content
                .offset(x: offset)
                .simultaneousGesture(
                    TapGesture(count: 2).onEnded { onEdit() }
                )
                .gesture(
                    DragGesture(minimumDistance: 20)
                        .onChanged { val in
                            let x = val.translation.width
                            guard abs(val.translation.height) < abs(x) else { return }
                            if x < 0 {
                                isDragging = true
                                offset = max(x, -deleteReveal - 12)
                            } else if isDragging {
                                offset = min(x + (offset < 0 ? offset : 0), 0)
                            }
                        }
                        .onEnded { val in
                            isDragging = false
                            if offset <= -deleteThreshold {
                                withAnimation(.spring(response: 0.25)) { offset = -deleteReveal }
                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                                    withAnimation(.spring(response: 0.3)) { offset = 0 }
                                    onDeleteRequest()
                                }
                            } else {
                                withAnimation(.spring(response: 0.3)) { offset = 0 }
                            }
                        }
                )
        }
        .clipped()
        .accessibilityAction(named: "Chỉnh sửa") { onEdit() }
        .accessibilityAction(named: "Xoá") { onDeleteRequest() }
    }
}

extension View {
    func interactiveRow(onEdit: @escaping () -> Void, onDeleteRequest: @escaping () -> Void) -> some View {
        self.modifier(InteractiveRowModifier(onEdit: onEdit, onDeleteRequest: onDeleteRequest))
    }
}

// MARK: - Tasks Screen (Modern Redesign)
struct TasksScreen: View {
    @ObservedObject var viewModel: TasksViewModel
    @Environment(\.lichSoColors) var c
    @Environment(\.dismiss) var dismiss

    @State private var showSheet = false
    @State private var editingTask: TaskItem? = nil
    @State private var editingNote: NoteItem? = nil
    @State private var editingReminder: ReminderItem? = nil
    @State private var deleteTarget: UUID? = nil
    @State private var showDeleteConfirm = false
    @State private var deleteTabType: TasksViewModel.TaskTab = .tasks

    var body: some View {
        NavigationView {
            ZStack {
                c.bg.ignoresSafeArea()

                VStack(spacing: 0) {
                    // ─── Stats Row ───
                    HStack(spacing: 8) {
                        ModernStatCard(
                            icon: "checkmark.square",
                            value: "\(viewModel.tasks.filter { !$0.isDone }.count)",
                            label: "Việc cần làm",
                            color: c.cyan
                        )
                        ModernStatCard(
                            icon: "bell.fill",
                            value: "\(viewModel.reminders.filter { $0.isActive }.count)",
                            label: "Nhắc nhở",
                            color: c.gold
                        )
                        ModernStatCard(
                            icon: "note.text",
                            value: "\(viewModel.notes.count)",
                            label: "Ghi chú",
                            color: c.teal
                        )
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 8)
                    .padding(.bottom, 12)

                    // ─── Tabs ───
                    ModernTabBar(selectedTab: $viewModel.selectedTab)
                        .padding(.horizontal, 16)
                        .padding(.bottom, 10)

                    // ─── Content ───
                    ScrollView(showsIndicators: false) {
                        VStack(spacing: 0) {
                            switch viewModel.selectedTab {
                            case .tasks:
                                ModernTaskListContent(
                                    tasks: viewModel.tasks,
                                    viewModel: viewModel,
                                    onEdit: { t in editingTask = t; showSheet = true },
                                    onDeleteRequest: { id in deleteTarget = id; deleteTabType = .tasks; showDeleteConfirm = true }
                                )
                            case .notes:
                                ModernNoteListContent(
                                    notes: viewModel.notes,
                                    onEdit: { n in editingNote = n; showSheet = true },
                                    onDeleteRequest: { id in deleteTarget = id; deleteTabType = .notes; showDeleteConfirm = true }
                                )
                            case .reminders:
                                ModernReminderListContent(
                                    reminders: viewModel.reminders,
                                    viewModel: viewModel,
                                    onEdit: { r in editingReminder = r; showSheet = true },
                                    onDeleteRequest: { id in deleteTarget = id; deleteTabType = .reminders; showDeleteConfirm = true }
                                )
                            }
                            Spacer(minLength: 16)
                        }
                    }

                    // ─── Bottom Quick Action Bar ───
                    QuickActionBar(
                        viewModel: viewModel,
                        onAddTap: { editingTask = nil; editingNote = nil; editingReminder = nil; showSheet = true }
                    )
                }
            }
            .navigationTitle("Ghi chú & Việc làm")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { dismiss() }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 20))
                            .foregroundColor(c.textTertiary)
                            .symbolRenderingMode(.hierarchical)
                    }
                }
            }
            .sheet(isPresented: $showSheet, onDismiss: { editingTask = nil; editingNote = nil; editingReminder = nil }) {
                TaskEditSheet(viewModel: viewModel, editingTask: editingTask, editingNote: editingNote, editingReminder: editingReminder)
                    .environment(\.lichSoColors, c)
            }
            .confirmationDialog("Xác nhận xoá", isPresented: $showDeleteConfirm, titleVisibility: .visible) {
                Button("Xoá", role: .destructive) {
                    guard let id = deleteTarget else { return }
                    withAnimation {
                        switch deleteTabType {
                        case .tasks: viewModel.deleteTask(id)
                        case .notes: viewModel.deleteNote(id)
                        case .reminders: viewModel.deleteReminder(id)
                        }
                    }
                }
                Button("Huỷ", role: .cancel) {}
            } message: {
                Text("Hành động này không thể hoàn tác.")
            }
        }
    }
}

// MARK: - Modern Stat Card
struct ModernStatCard: View {
    let icon: String
    let value: String
    let label: String
    let color: Color
    @Environment(\.lichSoColors) var c

    var body: some View {
        VStack(spacing: 6) {
            HStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundColor(color)
                Text(value)
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(color)
            }
            Text(label)
                .font(.system(size: 10, weight: .medium))
                .foregroundColor(c.textTertiary)
                .lineLimit(1)
                .minimumScaleFactor(0.8)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .background(
            RoundedRectangle(cornerRadius: 14)
                .fill(color.opacity(0.06))
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(color.opacity(0.15), lineWidth: 1)
                )
        )
    }
}

// MARK: - Modern Tab Bar
struct ModernTabBar: View {
    @Binding var selectedTab: TasksViewModel.TaskTab
    @Environment(\.lichSoColors) var c

    var body: some View {
        HStack(spacing: 4) {
            ModernTabItem(title: "Việc làm", icon: "checkmark.square", isSelected: selectedTab == .tasks, color: c.cyan) {
                selectedTab = .tasks
            }
            ModernTabItem(title: "Ghi chú", icon: "note.text", isSelected: selectedTab == .notes, color: c.teal) {
                selectedTab = .notes
            }
            ModernTabItem(title: "Nhắc nhở", icon: "bell", isSelected: selectedTab == .reminders, color: c.gold) {
                selectedTab = .reminders
            }
        }
        .padding(3)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(c.surface.opacity(0.5))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(c.border, lineWidth: 1)
                )
        )
    }
}

struct ModernTabItem: View {
    let title: String
    let icon: String
    let isSelected: Bool
    let color: Color
    let action: () -> Void
    @Environment(\.lichSoColors) var c

    var body: some View {
        Button(action: action) {
            HStack(spacing: 5) {
                Image(systemName: icon)
                    .font(.system(size: 11, weight: .semibold))
                Text(title)
                    .font(.system(size: 12, weight: isSelected ? .bold : .medium))
            }
            .foregroundColor(isSelected ? color : c.textTertiary)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 8)
            .background(
                RoundedRectangle(cornerRadius: 10)
                    .fill(isSelected ? color.opacity(0.12) : Color.clear)
            )
        }
    }
}

// MARK: - Gesture Hint Bar
struct GestureHintBar: View {
    @Environment(\.lichSoColors) var c
    var body: some View {
        HStack(spacing: 16) {
            HStack(spacing: 5) {
                Image(systemName: "hand.tap.fill").font(.system(size: 10))
                Text("Chạm 2 lần để sửa").font(.system(size: 10))
            }
            .foregroundColor(c.teal.opacity(0.7))
            HStack(spacing: 5) {
                Image(systemName: "hand.draw.fill").font(.system(size: 10))
                Text("Kéo trái để xoá").font(.system(size: 10))
            }
            .foregroundColor(c.red2.opacity(0.7))
            Spacer()
        }
        .padding(.horizontal, 4)
        .padding(.bottom, 6)
    }
}

// MARK: - Modern Task List
struct ModernTaskListContent: View {
    let tasks: [TaskItem]
    let viewModel: TasksViewModel
    let onEdit: (TaskItem) -> Void
    let onDeleteRequest: (UUID) -> Void
    @Environment(\.lichSoColors) var c

    var pending: [TaskItem] { tasks.filter { !$0.isDone } }
    var done: [TaskItem] { tasks.filter { $0.isDone } }

    var body: some View {
        VStack(spacing: 8) {
            GestureHintBar()

            if pending.isEmpty && done.isEmpty {
                TasksEmptyState(icon: "checkmark.circle", title: "Chưa có việc nào", subtitle: "Thêm việc cần làm bằng nút + bên dưới")
            }

            ForEach(pending) { t in
                ModernTaskRow(task: t, viewModel: viewModel, onEdit: onEdit, onDeleteRequest: onDeleteRequest)
            }

            if !done.isEmpty && !pending.isEmpty {
                HStack(spacing: 8) {
                    Rectangle().fill(c.borderSubtle).frame(height: 1)
                    Text("Đã xong (\(done.count))")
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(c.textQuaternary)
                        .fixedSize()
                    Rectangle().fill(c.borderSubtle).frame(height: 1)
                }
                .padding(.vertical, 6)
            }

            ForEach(done) { t in
                ModernTaskRow(task: t, viewModel: viewModel, onEdit: onEdit, onDeleteRequest: onDeleteRequest)
            }
        }
        .padding(.horizontal, 16)
    }
}

struct ModernTaskRow: View {
    let task: TaskItem
    let viewModel: TasksViewModel
    let onEdit: (TaskItem) -> Void
    let onDeleteRequest: (UUID) -> Void
    @Environment(\.lichSoColors) var c

    private var priorityInfo: (String, Color, String) {
        switch task.priority {
        case 2: return ("Cao", c.red2, "exclamationmark.triangle.fill")
        case 1: return ("TB", c.gold, "minus.circle.fill")
        default: return ("Thấp", c.textTertiary, "arrow.down.circle")
        }
    }

    private var deadlineText: String? {
        guard let d = task.deadline else { return nil }
        let today = Calendar.current.startOfDay(for: Date())
        let target = Calendar.current.startOfDay(for: d)
        let days = Calendar.current.dateComponents([.day], from: today, to: target).day ?? 0
        if days < 0 { return "Quá hạn \(abs(days))d" }
        if days == 0 { return "Hôm nay" }
        if days == 1 { return "Ngày mai" }
        let f = DateFormatter(); f.dateFormat = "dd/MM"; return f.string(from: d)
    }

    private var isOverdue: Bool { guard let d = task.deadline, !task.isDone else { return false }; return d < Date() }

    var body: some View {
        HStack(spacing: 12) {
            // Checkbox
            Button(action: { withAnimation(.spring(response: 0.3)) { viewModel.toggleTask(task.id) } }) {
                ZStack {
                    RoundedRectangle(cornerRadius: 6)
                        .fill(task.isDone ? c.teal.opacity(0.15) : Color.clear)
                        .frame(width: 24, height: 24)
                        .overlay(
                            RoundedRectangle(cornerRadius: 6)
                                .stroke(task.isDone ? c.teal : c.textQuaternary, lineWidth: 1.5)
                        )
                    if task.isDone {
                        Image(systemName: "checkmark")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(c.teal)
                    }
                }
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(task.title)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(task.isDone ? c.textTertiary : c.textPrimary)
                    .strikethrough(task.isDone, color: c.textTertiary)

                HStack(spacing: 6) {
                    let (pLabel, pColor, pIcon) = priorityInfo
                    HStack(spacing: 3) {
                        Image(systemName: pIcon).font(.system(size: 8))
                        Text(pLabel).font(.system(size: 10, weight: .semibold))
                    }
                    .foregroundColor(pColor)
                    .padding(.horizontal, 7)
                    .padding(.vertical, 3)
                    .background(pColor.opacity(0.1))
                    .clipShape(Capsule())

                    if let dl = deadlineText {
                        HStack(spacing: 3) {
                            Image(systemName: "calendar.badge.clock").font(.system(size: 9))
                            Text(dl).font(.system(size: 10, weight: .medium))
                        }
                        .foregroundColor(isOverdue ? c.red2 : c.textTertiary)
                        .padding(.horizontal, 7)
                        .padding(.vertical, 3)
                        .background((isOverdue ? c.red : c.surface).opacity(0.12))
                        .clipShape(Capsule())
                    }
                }
            }
            Spacer()
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .background(
            RoundedRectangle(cornerRadius: 14)
                .fill(c.panelBg)
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(isOverdue ? c.red.opacity(0.3) : c.border, lineWidth: 1)
                )
        )
        .interactiveRow(onEdit: { onEdit(task) }, onDeleteRequest: { onDeleteRequest(task.id) })
    }
}

// MARK: - Modern Note List
struct ModernNoteListContent: View {
    let notes: [NoteItem]
    let onEdit: (NoteItem) -> Void
    let onDeleteRequest: (UUID) -> Void
    @Environment(\.lichSoColors) var c

    var body: some View {
        VStack(spacing: 8) {
            GestureHintBar()

            if notes.isEmpty {
                TasksEmptyState(icon: "note.text", title: "Chưa có ghi chú", subtitle: "Tạo ghi chú mới bằng nút + bên dưới")
            }

            LazyVGrid(columns: [GridItem(.flexible(), spacing: 8), GridItem(.flexible(), spacing: 8)], spacing: 8) {
                ForEach(notes) { n in
                    ModernNoteCard(note: n, onEdit: { onEdit(n) }, onDeleteRequest: { onDeleteRequest(n.id) })
                }
            }
        }
        .padding(.horizontal, 16)
    }
}

struct ModernNoteCard: View {
    let note: NoteItem
    let onEdit: () -> Void
    let onDeleteRequest: () -> Void
    @Environment(\.lichSoColors) var c

    private var noteColor: Color {
        switch note.color {
        case "gold": return c.noteGold; case "teal": return c.noteTeal; case "orange": return c.noteOrange
        case "purple": return c.notePurple; case "green": return c.noteGreen; case "red": return c.noteRed
        default: return c.noteGold
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Color bar at top
            HStack(spacing: 0) {
                RoundedRectangle(cornerRadius: 2)
                    .fill(noteColor)
                    .frame(width: 30, height: 3)
                Spacer()
                Menu {
                    Button { onEdit() } label: { Label("Sửa", systemImage: "pencil") }
                    Button(role: .destructive) { onDeleteRequest() } label: { Label("Xoá", systemImage: "trash") }
                } label: {
                    Image(systemName: "ellipsis")
                        .font(.system(size: 13))
                        .foregroundColor(c.textTertiary)
                        .frame(width: 24, height: 24)
                }
            }

            Text(note.title)
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(c.textPrimary)
                .lineLimit(2)

            Text(note.content)
                .font(.system(size: 11))
                .foregroundColor(c.textSecondary)
                .lineLimit(4)

            Spacer(minLength: 0)

            Text(note.createdAt.formatted(date: .abbreviated, time: .omitted))
                .font(.system(size: 9, weight: .medium))
                .foregroundColor(c.textQuaternary)
        }
        .padding(12)
        .frame(maxWidth: .infinity, minHeight: 130, alignment: .topLeading)
        .background(
            RoundedRectangle(cornerRadius: 14)
                .fill(c.panelBg)
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(noteColor.opacity(0.25), lineWidth: 1.5)
                )
                .shadow(color: c.isDark ? Color.black.opacity(0.15) : Color.black.opacity(0.04), radius: 6, x: 0, y: 2)
        )
        .interactiveRow(onEdit: { onEdit() }, onDeleteRequest: { onDeleteRequest() })
    }
}

// MARK: - Modern Reminder List
struct ModernReminderListContent: View {
    let reminders: [ReminderItem]
    let viewModel: TasksViewModel
    let onEdit: (ReminderItem) -> Void
    let onDeleteRequest: (UUID) -> Void
    var body: some View {
        VStack(spacing: 8) {
            GestureHintBar()

            if reminders.isEmpty {
                TasksEmptyState(icon: "bell", title: "Chưa có nhắc nhở", subtitle: "Tạo nhắc nhở mới bằng nút + bên dưới")
            }

            ForEach(reminders) { r in
                ModernReminderRow(reminder: r, viewModel: viewModel, onEdit: onEdit, onDeleteRequest: onDeleteRequest)
            }
        }
        .padding(.horizontal, 16)
    }
}

struct ModernReminderRow: View {
    let reminder: ReminderItem
    let viewModel: TasksViewModel
    let onEdit: (ReminderItem) -> Void
    let onDeleteRequest: (UUID) -> Void
    @Environment(\.lichSoColors) var c

    private var timeString: String {
        let f = DateFormatter(); f.dateFormat = "HH:mm · dd/MM/yyyy"; return f.string(from: reminder.time)
    }
    private var isPast: Bool { reminder.time < Date() && reminder.isActive }

    var body: some View {
        HStack(spacing: 12) {
            // Icon
            ZStack {
                RoundedRectangle(cornerRadius: 12)
                    .fill(reminder.isActive ? c.gold.opacity(0.1) : c.surface.opacity(0.5))
                    .frame(width: 42, height: 42)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(reminder.isActive ? c.gold.opacity(0.2) : c.border, lineWidth: 1)
                    )
                Image(systemName: reminder.isActive ? "bell.fill" : "bell.slash")
                    .font(.system(size: 16))
                    .foregroundColor(reminder.isActive ? c.gold : c.textTertiary)
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(reminder.title)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(reminder.isActive ? c.textPrimary : c.textTertiary)
                HStack(spacing: 4) {
                    Image(systemName: "clock").font(.system(size: 10))
                    Text(timeString).font(.system(size: 11, weight: .medium))
                }
                .foregroundColor(isPast ? c.red2 : c.textTertiary)
                if !reminder.note.isEmpty {
                    Text(reminder.note)
                        .font(.system(size: 11))
                        .foregroundColor(c.textTertiary)
                        .lineLimit(1)
                }
            }
            Spacer()
            Toggle("", isOn: .init(get: { reminder.isActive }, set: { _ in viewModel.toggleReminder(reminder.id) }))
                .labelsHidden()
                .tint(c.cyan)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .background(
            RoundedRectangle(cornerRadius: 14)
                .fill(c.panelBg)
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(isPast ? c.red.opacity(0.25) : c.border, lineWidth: 1)
                )
        )
        .interactiveRow(onEdit: { onEdit(reminder) }, onDeleteRequest: { onDeleteRequest(reminder.id) })
    }
}

// MARK: - Quick Action Bar (Bottom)
struct QuickActionBar: View {
    @ObservedObject var viewModel: TasksViewModel
    let onAddTap: () -> Void
    @Environment(\.lichSoColors) var c

    private var pendingCount: Int { viewModel.tasks.filter { !$0.isDone }.count }
    private var doneCount: Int { viewModel.tasks.filter { $0.isDone }.count }
    private var activeReminders: Int { viewModel.reminders.filter { $0.isActive }.count }
    private var overdueCount: Int {
        viewModel.tasks.filter { !$0.isDone && ($0.deadline ?? .distantFuture) < Date() }.count
    }
    private var nextDeadline: TaskItem? {
        viewModel.tasks
            .filter { !$0.isDone && $0.deadline != nil && $0.deadline! > Date() }
            .sorted { ($0.deadline ?? .distantFuture) < ($1.deadline ?? .distantFuture) }
            .first
    }

    private var summaryText: String {
        switch viewModel.selectedTab {
        case .tasks:
            if overdueCount > 0 {
                return "⚠️ \(overdueCount) việc quá hạn cần xử lý!"
            } else if pendingCount == 0 && doneCount > 0 {
                return "🎉 Tuyệt vời! Đã hoàn thành tất cả."
            } else if let next = nextDeadline, let dl = next.deadline {
                let days = Calendar.current.dateComponents([.day], from: Calendar.current.startOfDay(for: Date()), to: Calendar.current.startOfDay(for: dl)).day ?? 0
                if days == 0 { return "⏰ \"\(next.title)\" hết hạn hôm nay!" }
                if days == 1 { return "📌 \"\(next.title)\" hết hạn ngày mai" }
                return "📅 Sắp tới: \"\(next.title)\" – còn \(days) ngày"
            } else if pendingCount > 0 {
                return "📋 Bạn có \(pendingCount) việc cần làm"
            } else {
                return "✨ Thêm việc mới để bắt đầu!"
            }
        case .notes:
            let count = viewModel.notes.count
            if count == 0 { return "📝 Ghi lại ý tưởng, ngày tốt, ghi chú quan trọng" }
            return "📝 \(count) ghi chú · Chạm + để thêm mới"
        case .reminders:
            if activeReminders == 0 { return "🔔 Tạo nhắc nhở để không bỏ lỡ ngày quan trọng" }
            return "🔔 \(activeReminders) nhắc nhở đang hoạt động"
        }
    }

    private var summaryIcon: String {
        switch viewModel.selectedTab {
        case .tasks: return overdueCount > 0 ? "exclamationmark.triangle.fill" : "chart.bar.fill"
        case .notes: return "lightbulb.fill"
        case .reminders: return "bell.badge.fill"
        }
    }

    private var summaryColor: Color {
        switch viewModel.selectedTab {
        case .tasks: return overdueCount > 0 ? c.red2 : c.cyan
        case .notes: return c.teal
        case .reminders: return c.gold
        }
    }

    private var progressValue: Double {
        let total = pendingCount + doneCount
        guard total > 0 else { return 0 }
        return Double(doneCount) / Double(total)
    }

    var body: some View {
        HStack(spacing: 12) {
            // ─── Summary Card ───
            VStack(alignment: .leading, spacing: 8) {
                HStack(spacing: 8) {
                    Image(systemName: summaryIcon)
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(summaryColor)
                    Text(summaryText)
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(c.textPrimary)
                        .lineLimit(2)
                        .fixedSize(horizontal: false, vertical: true)
                }

                // Progress bar for tasks tab
                if viewModel.selectedTab == .tasks && (pendingCount + doneCount) > 0 {
                    HStack(spacing: 8) {
                        GeometryReader { geo in
                            ZStack(alignment: .leading) {
                                RoundedRectangle(cornerRadius: 3)
                                    .fill(c.surface)
                                    .frame(height: 5)
                                RoundedRectangle(cornerRadius: 3)
                                    .fill(
                                        LinearGradient(
                                            colors: [c.teal, c.cyan],
                                            startPoint: .leading,
                                            endPoint: .trailing
                                        )
                                    )
                                    .frame(width: geo.size.width * progressValue, height: 5)
                                    .animation(.spring(response: 0.4), value: progressValue)
                            }
                        }
                        .frame(height: 5)
                        Text("\(doneCount)/\(pendingCount + doneCount)")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(c.textTertiary)
                            .monospacedDigit()
                    }
                }

                // Quick action chips
                HStack(spacing: 6) {
                    QuickChip(icon: quickChipIcon, label: quickChipLabel, color: summaryColor, action: onAddTap)

                    if viewModel.selectedTab == .tasks && doneCount > 0 {
                        QuickChip(
                            icon: "trash.circle",
                            label: "Xoá đã xong",
                            color: c.textTertiary,
                            action: {
                                withAnimation {
                                    let doneIds = viewModel.tasks.filter { $0.isDone }.map { $0.id }
                                    doneIds.forEach { viewModel.deleteTask($0) }
                                }
                            }
                        )
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            // ─── FAB ───
            Button(action: onAddTap) {
                ZStack {
                    Circle()
                        .fill(
                            LinearGradient(colors: [c.cyan, c.cyan2], startPoint: .topLeading, endPoint: .bottomTrailing)
                        )
                        .frame(width: 50, height: 50)
                        .shadow(color: c.cyan.opacity(0.3), radius: 8, x: 0, y: 3)
                    Image(systemName: "plus")
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(.white)
                }
            }
            .accessibilityLabel(quickChipLabel)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(
            Rectangle()
                .fill(c.bg)
                .overlay(alignment: .top) {
                    Rectangle()
                        .fill(c.border.opacity(0.5))
                        .frame(height: 0.5)
                }
                .shadow(color: c.isDark ? Color.black.opacity(0.2) : Color.black.opacity(0.05), radius: 8, x: 0, y: -4)
        )
    }

    private var quickChipIcon: String {
        switch viewModel.selectedTab {
        case .tasks: return "plus.square"
        case .notes: return "plus.rectangle.on.rectangle"
        case .reminders: return "bell.badge.plus"
        }
    }

    private var quickChipLabel: String {
        switch viewModel.selectedTab {
        case .tasks: return "Thêm việc"
        case .notes: return "Ghi chú mới"
        case .reminders: return "Nhắc nhở mới"
        }
    }
}

// MARK: - Quick Chip Button
struct QuickChip: View {
    let icon: String
    let label: String
    let color: Color
    let action: () -> Void
    @Environment(\.lichSoColors) var c

    var body: some View {
        Button(action: action) {
            HStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.system(size: 10, weight: .semibold))
                Text(label)
                    .font(.system(size: 10, weight: .semibold))
            }
            .foregroundColor(color)
            .padding(.horizontal, 10)
            .padding(.vertical, 5)
            .background(
                Capsule()
                    .fill(color.opacity(0.1))
                    .overlay(
                        Capsule()
                            .stroke(color.opacity(0.2), lineWidth: 0.5)
                    )
            )
        }
        .accessibilityLabel(label)
    }
}

// MARK: - Empty State
struct TasksEmptyState: View {
    let icon: String
    let title: String
    let subtitle: String
    @Environment(\.lichSoColors) var c

    var body: some View {
        VStack(spacing: 10) {
            Image(systemName: icon)
                .font(.system(size: 32, weight: .light))
                .foregroundColor(c.textQuaternary)
            Text(title)
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(c.textTertiary)
            Text(subtitle)
                .font(.system(size: 12))
                .foregroundColor(c.textQuaternary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 40)
    }
}

// MARK: - Task Edit Sheet
struct TaskEditSheet: View {
    @ObservedObject var viewModel: TasksViewModel
    @Environment(\.lichSoColors) var c
    @Environment(\.dismiss) var dismiss

    let editingTask: TaskItem?
    let editingNote: NoteItem?
    let editingReminder: ReminderItem?

    @State private var taskTitle = ""
    @State private var taskPriority = 1
    @State private var hasDeadline = false
    @State private var taskDeadline = Calendar.current.date(byAdding: .day, value: 1, to: Date()) ?? Date()

    @State private var noteTitle = ""
    @State private var noteContent = ""
    @State private var noteColor = "gold"

    @State private var reminderTitle = ""
    @State private var reminderTime = Date()
    @State private var reminderNote = ""

    private var isEditing: Bool { editingTask != nil || editingNote != nil || editingReminder != nil }
    private var currentTab: TasksViewModel.TaskTab {
        if editingTask != nil { return .tasks }
        if editingNote != nil { return .notes }
        if editingReminder != nil { return .reminders }
        return viewModel.selectedTab
    }
    private var isValid: Bool {
        switch currentTab {
        case .tasks: return !taskTitle.trimmingCharacters(in: .whitespaces).isEmpty
        case .notes: return !noteTitle.trimmingCharacters(in: .whitespaces).isEmpty
        case .reminders: return !reminderTitle.trimmingCharacters(in: .whitespaces).isEmpty
        }
    }

    let noteColors: [(String, String)] = [
        ("gold","Vàng"), ("teal","Xanh"), ("orange","Cam"),
        ("purple","Tím"), ("green","Lục"), ("red","Đỏ")
    ]

    var body: some View {
        NavigationView {
            ZStack {
                c.bg.ignoresSafeArea()
                ScrollView {
                    VStack(spacing: 20) {
                        switch currentTab {
                        case .tasks: taskForm
                        case .notes: noteForm
                        case .reminders: reminderForm
                        }
                        Spacer(minLength: 40)
                    }
                    .padding(20)
                }
            }
            .navigationTitle(isEditing ? "Chỉnh sửa" : "Thêm mới")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Huỷ") { dismiss() }.foregroundColor(c.textSecondary)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(isEditing ? "Cập nhật" : "Lưu") { save() }
                        .fontWeight(.semibold)
                        .foregroundColor(isValid ? c.cyan : c.textQuaternary)
                        .disabled(!isValid)
                }
            }
            .onAppear { prefillFields() }
        }
    }

    // ─── Task Form ───
    private var taskForm: some View {
        VStack(spacing: 16) {
            FormSection(title: "TIÊU ĐỀ") {
                SheetTextField(text: $taskTitle, placeholder: "Tên việc cần làm...", icon: "checkmark.square")
            }
            FormSection(title: "ĐỘ ƯU TIÊN") {
                HStack(spacing: 8) {
                    ForEach([("Thấp", 0), ("Bình thường", 1), ("Cao", 2)], id: \.0) { label, val in
                        let color: Color = val == 2 ? c.red2 : val == 1 ? c.gold : c.textTertiary
                        Button { taskPriority = val } label: {
                            HStack(spacing: 6) {
                                Circle().fill(color).frame(width: 8, height: 8)
                                Text(label).font(.system(size: 13, weight: taskPriority == val ? .semibold : .regular))
                            }
                            .foregroundColor(taskPriority == val ? color : c.textTertiary)
                            .frame(maxWidth: .infinity).padding(.vertical, 10)
                            .background(taskPriority == val ? color.opacity(0.12) : c.panelBg)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                            .overlay(RoundedRectangle(cornerRadius: 10).stroke(taskPriority == val ? color.opacity(0.4) : c.border, lineWidth: 1))
                        }
                    }
                }
            }
            FormSection(title: "DEADLINE") {
                VStack(spacing: 0) {
                    HStack {
                        Image(systemName: "calendar.badge.clock").font(.system(size: 15)).foregroundColor(c.cyan).frame(width: 24)
                        Text("Đặt deadline").font(.system(size: 14)).foregroundColor(c.textPrimary)
                        Spacer()
                        Toggle("", isOn: $hasDeadline).labelsHidden().tint(c.cyan)
                    }
                    .padding(.horizontal, 14).padding(.vertical, 12)
                    if hasDeadline {
                        Divider().overlay(c.border).padding(.horizontal, 14)
                        DatePicker("", selection: $taskDeadline, in: Date()..., displayedComponents: [.date, .hourAndMinute])
                            .labelsHidden().datePickerStyle(.graphical).tint(c.cyan)
                            .padding(.horizontal, 8).padding(.bottom, 8)
                    }
                }
                .background(c.panelBg).clipShape(RoundedRectangle(cornerRadius: 14))
                .overlay(RoundedRectangle(cornerRadius: 14).stroke(c.border, lineWidth: 1))
            }
        }
    }

    // ─── Note Form ───
    private var noteForm: some View {
        VStack(spacing: 16) {
            FormSection(title: "TIÊU ĐỀ") {
                SheetTextField(text: $noteTitle, placeholder: "Tiêu đề ghi chú...", icon: "note.text")
            }
            FormSection(title: "NỘI DUNG") {
                ZStack(alignment: .topLeading) {
                    if noteContent.isEmpty {
                        Text("Nội dung ghi chú...").font(.system(size: 14)).foregroundColor(c.textQuaternary)
                            .padding(.horizontal, 16).padding(.vertical, 14)
                    }
                    TextEditor(text: $noteContent)
                        .font(.system(size: 14)).foregroundColor(c.textPrimary)
                        .frame(minHeight: 130).padding(.horizontal, 12).padding(.vertical, 8)
                        .scrollContentBackground(.hidden)
                }
                .background(c.panelBg).clipShape(RoundedRectangle(cornerRadius: 14))
                .overlay(RoundedRectangle(cornerRadius: 14).stroke(c.border, lineWidth: 1))
            }
            FormSection(title: "MÀU SẮC") {
                HStack(spacing: 10) {
                    ForEach(noteColors, id: \.0) { key, label in
                        let color = noteColorValue(key)
                        Button { noteColor = key } label: {
                            VStack(spacing: 4) {
                                ZStack {
                                    Circle().fill(color.opacity(0.25)).frame(width: 36, height: 36)
                                    Circle().fill(color).frame(width: 22, height: 22)
                                    if noteColor == key {
                                        Image(systemName: "checkmark").font(.system(size: 11, weight: .bold)).foregroundColor(.white)
                                    }
                                }
                                Text(label).font(.system(size: 9)).foregroundColor(noteColor == key ? color : c.textQuaternary)
                            }
                        }
                        .frame(maxWidth: .infinity)
                    }
                }
                .padding(.horizontal, 14).padding(.vertical, 12)
                .background(c.panelBg).clipShape(RoundedRectangle(cornerRadius: 14))
                .overlay(RoundedRectangle(cornerRadius: 14).stroke(c.border, lineWidth: 1))
            }
        }
    }

    // ─── Reminder Form ───
    private var reminderForm: some View {
        VStack(spacing: 16) {
            FormSection(title: "TIÊU ĐỀ") {
                SheetTextField(text: $reminderTitle, placeholder: "Tên nhắc nhở...", icon: "bell")
            }
            FormSection(title: "GHI CHÚ") {
                SheetTextField(text: $reminderNote, placeholder: "Ghi chú thêm (tuỳ chọn)...", icon: "text.bubble")
            }
            FormSection(title: "THỜI GIAN") {
                DatePicker("", selection: $reminderTime, displayedComponents: [.date, .hourAndMinute])
                    .labelsHidden().datePickerStyle(.graphical).tint(c.cyan)
                    .padding(.horizontal, 8).padding(.vertical, 8)
                    .background(c.panelBg).clipShape(RoundedRectangle(cornerRadius: 14))
                    .overlay(RoundedRectangle(cornerRadius: 14).stroke(c.border, lineWidth: 1))
            }
        }
    }

    private func prefillFields() {
        if let t = editingTask {
            taskTitle = t.title; taskPriority = t.priority
            if let d = t.deadline { hasDeadline = true; taskDeadline = d }
        } else if let n = editingNote {
            noteTitle = n.title; noteContent = n.content; noteColor = n.color
        } else if let r = editingReminder {
            reminderTitle = r.title; reminderTime = r.time; reminderNote = r.note
        }
    }

    private func save() {
        switch currentTab {
        case .tasks:
            let dl: Date? = hasDeadline ? taskDeadline : nil
            if let t = editingTask { viewModel.updateTask(t.id, title: taskTitle, priority: taskPriority, deadline: dl) }
            else { viewModel.addTask(taskTitle, priority: taskPriority, deadline: dl) }
        case .notes:
            if let n = editingNote { viewModel.updateNote(n.id, title: noteTitle, content: noteContent, color: noteColor) }
            else { viewModel.addNote(noteTitle, content: noteContent, color: noteColor) }
        case .reminders:
            if let r = editingReminder { viewModel.updateReminder(r.id, title: reminderTitle, time: reminderTime, note: reminderNote) }
            else { viewModel.addReminder(reminderTitle, time: reminderTime, note: reminderNote) }
        }
        dismiss()
    }

    private func noteColorValue(_ key: String) -> Color {
        switch key {
        case "gold": return c.noteGold; case "teal": return c.noteTeal; case "orange": return c.noteOrange
        case "purple": return c.notePurple; case "green": return c.noteGreen; case "red": return c.noteRed
        default: return c.noteGold
        }
    }
}

// MARK: - Form Section
struct FormSection<Content: View>: View {
    let title: String
    @ViewBuilder let content: () -> Content
    @Environment(\.lichSoColors) var c
    var body: some View {
        VStack(alignment: .leading, spacing: 7) {
            Text(title).font(.system(size: 10.5, weight: .bold)).foregroundColor(c.textTertiary).kerning(1)
            content()
        }
    }
}

// MARK: - Sheet Text Field
struct SheetTextField: View {
    @Binding var text: String
    let placeholder: String
    let icon: String
    @Environment(\.lichSoColors) var c
    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: icon).font(.system(size: 15)).foregroundColor(c.cyan).frame(width: 24)
            TextField(placeholder, text: $text).font(.system(size: 14)).foregroundColor(c.textPrimary)
        }
        .padding(.horizontal, 14).padding(.vertical, 13)
        .background(c.panelBg).clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(c.border, lineWidth: 1))
    }
}
