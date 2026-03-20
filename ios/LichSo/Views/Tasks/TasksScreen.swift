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
            // ── Delete background ──
            RoundedRectangle(cornerRadius: 12)
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

            // ── Main content ──
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
                                // snap fully then trigger confirm
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
    }
}

extension View {
    func interactiveRow(onEdit: @escaping () -> Void, onDeleteRequest: @escaping () -> Void) -> some View {
        self.modifier(InteractiveRowModifier(onEdit: onEdit, onDeleteRequest: onDeleteRequest))
    }
}

// MARK: - Tasks Screen
struct TasksScreen: View {
    @ObservedObject var viewModel: TasksViewModel
    @Environment(\.lichSoColors) var c

    @State private var showSheet = false
    @State private var editingTask: TaskItem? = nil
    @State private var editingNote: NoteItem? = nil
    @State private var editingReminder: ReminderItem? = nil
    @State private var deleteTarget: UUID? = nil
    @State private var showDeleteConfirm = false
    @State private var deleteTabType: TasksViewModel.TaskTab = .tasks

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            VStack(spacing: 0) {
                // ─── Header ───
                HStack {
                    Text("Ghi chú & Việc làm")
                        .font(.system(size: 22, weight: .bold, design: .serif))
                        .foregroundColor(c.gold2)
                    Spacer()
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 14)

                // ─── Stats ───
                HStack(spacing: 8) {
                    StatCard(value: "\(viewModel.tasks.filter { !$0.isDone }.count)", label: "Việc chưa xong", color: c.teal2)
                    StatCard(value: "\(viewModel.reminders.filter { $0.isActive }.count)", label: "Nhắc nhở", color: c.gold2)
                    StatCard(value: "\(viewModel.notes.count)", label: "Ghi chú", color: c.textSecondary)
                }
                .padding(.horizontal, 20)

                Spacer(minLength: 14)

                // ─── Tabs ───
                HStack(spacing: 4) {
                    TabButton(title: "Việc làm", icon: "checkmark.square", isSelected: viewModel.selectedTab == .tasks) { viewModel.selectedTab = .tasks }
                    TabButton(title: "Ghi chú", icon: "note.text", isSelected: viewModel.selectedTab == .notes) { viewModel.selectedTab = .notes }
                    TabButton(title: "Nhắc nhở", icon: "bell", isSelected: viewModel.selectedTab == .reminders) { viewModel.selectedTab = .reminders }
                }
                .padding(.horizontal, 20)

                Spacer(minLength: 10)

                ScrollView {
                    VStack(spacing: 0) {
                        switch viewModel.selectedTab {
                        case .tasks:
                            TaskListContent(tasks: viewModel.tasks, viewModel: viewModel,
                                onEdit: { t in editingTask = t; showSheet = true },
                                onDeleteRequest: { id in deleteTarget = id; deleteTabType = .tasks; showDeleteConfirm = true })
                        case .notes:
                            NoteListContent(notes: viewModel.notes,
                                onEdit: { n in editingNote = n; showSheet = true },
                                onDeleteRequest: { id in deleteTarget = id; deleteTabType = .notes; showDeleteConfirm = true })
                        case .reminders:
                            ReminderListContent(reminders: viewModel.reminders, viewModel: viewModel,
                                onEdit: { r in editingReminder = r; showSheet = true },
                                onDeleteRequest: { id in deleteTarget = id; deleteTabType = .reminders; showDeleteConfirm = true })
                        }
                        Spacer(minLength: 100)
                    }
                }
            }
            .background(c.bg.ignoresSafeArea())

            // ─── FAB ───
            Button(action: { editingTask = nil; editingNote = nil; editingReminder = nil; showSheet = true }) {
                Image(systemName: "plus")
                    .font(.system(size: 22, weight: .semibold))
                    .foregroundColor(c.isDark ? Color(hex: 0x1A1500) : .white)
                    .frame(width: 52, height: 52)
                    .background(c.gold)
                    .clipShape(Circle())
                    .shadow(color: c.gold.opacity(0.4), radius: 8, x: 0, y: 4)
            }
            .padding(.trailing, 20)
            .padding(.bottom, 24)
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

// MARK: - Stat Card
struct StatCard: View {
    let value: String
    let label: String
    let color: Color
    @Environment(\.lichSoColors) var c
    var body: some View {
        VStack(spacing: 2) {
            Text(value).font(.system(size: 22, weight: .bold)).foregroundColor(color)
            Text(label).font(.system(size: 10)).foregroundColor(c.textTertiary).multilineTextAlignment(.center).lineLimit(2).minimumScaleFactor(0.8)
        }
        .frame(maxWidth: .infinity).padding(.vertical, 10)
        .background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 10))
        .overlay(RoundedRectangle(cornerRadius: 10).stroke(c.border, lineWidth: 1))
    }
}

// MARK: - Tab Button
struct TabButton: View {
    let title: String
    let icon: String
    let isSelected: Bool
    let action: () -> Void
    @Environment(\.lichSoColors) var c
    var body: some View {
        Button(action: action) {
            HStack(spacing: 5) {
                Image(systemName: icon).font(.system(size: 11))
                Text(title).font(.system(size: 12, weight: isSelected ? .semibold : .medium))
            }
            .foregroundColor(isSelected ? c.gold2 : c.textTertiary)
            .frame(maxWidth: .infinity).padding(.vertical, 7)
            .background(isSelected ? c.goldDim : c.bg2)
            .clipShape(RoundedRectangle(cornerRadius: 8))
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(isSelected ? c.gold.opacity(0.3) : c.border, lineWidth: 1))
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
        .padding(.bottom, 2)
    }
}

// MARK: - Task List
struct TaskListContent: View {
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
            ForEach(pending) { t in TaskRow(task: t, viewModel: viewModel, onEdit: onEdit, onDeleteRequest: onDeleteRequest) }
            if !done.isEmpty && !pending.isEmpty {
                HStack {
                    Rectangle().fill(c.border).frame(height: 1)
                    Text("Đã xong (\(done.count))").font(.system(size: 11)).foregroundColor(c.textQuaternary).fixedSize()
                    Rectangle().fill(c.border).frame(height: 1)
                }
                .padding(.horizontal, 20).padding(.vertical, 4)
            }
            ForEach(done) { t in TaskRow(task: t, viewModel: viewModel, onEdit: onEdit, onDeleteRequest: onDeleteRequest) }
        }
        .padding(.horizontal, 20)
    }
}

struct TaskRow: View {
    let task: TaskItem
    let viewModel: TasksViewModel
    let onEdit: (TaskItem) -> Void
    let onDeleteRequest: (UUID) -> Void
    @Environment(\.lichSoColors) var c

    private var priorityInfo: (String, Color) {
        switch task.priority {
        case 2: return ("Cao", c.red2)
        case 1: return ("TB", c.gold2)
        default: return ("Thấp", c.textTertiary)
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
        let f = DateFormatter(); f.dateFormat = "dd/MM"; return "HH: \(f.string(from: d))"
    }

    private var isOverdue: Bool { guard let d = task.deadline, !task.isDone else { return false }; return d < Date() }

    var body: some View {
        HStack(spacing: 12) {
            Button(action: { viewModel.toggleTask(task.id) }) {
                Image(systemName: task.isDone ? "checkmark.circle.fill" : "circle")
                    .font(.system(size: 22))
                    .foregroundColor(task.isDone ? c.teal : c.textTertiary)
            }
            VStack(alignment: .leading, spacing: 4) {
                Text(task.title)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(task.isDone ? c.textTertiary : c.textPrimary)
                    .strikethrough(task.isDone, color: c.textTertiary)
                HStack(spacing: 6) {
                    let (pLabel, pColor) = priorityInfo
                    Text(pLabel).font(.system(size: 10, weight: .semibold)).foregroundColor(pColor)
                        .padding(.horizontal, 6).padding(.vertical, 2)
                        .background(pColor.opacity(0.12)).clipShape(Capsule())
                    if let dl = deadlineText {
                        HStack(spacing: 3) {
                            Image(systemName: "calendar.badge.clock").font(.system(size: 9))
                            Text(dl).font(.system(size: 10))
                        }
                        .foregroundColor(isOverdue ? c.red2 : c.textTertiary)
                        .padding(.horizontal, 6).padding(.vertical, 2)
                        .background((isOverdue ? c.red : c.surface).opacity(0.15)).clipShape(Capsule())
                    }
                }
            }
            Spacer()
        }
        .padding(.horizontal, 13).padding(.vertical, 11)
        .background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 10))
        .overlay(RoundedRectangle(cornerRadius: 10).stroke(isOverdue ? c.red.opacity(0.3) : c.border, lineWidth: 1))
        .interactiveRow(onEdit: { onEdit(task) }, onDeleteRequest: { onDeleteRequest(task.id) })
    }
}

// MARK: - Note List
struct NoteListContent: View {
    let notes: [NoteItem]
    let onEdit: (NoteItem) -> Void
    let onDeleteRequest: (UUID) -> Void
    var body: some View {
        VStack(spacing: 8) {
            GestureHintBar()
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                ForEach(notes) { n in
                    NoteCard(note: n, onEdit: { onEdit(n) }, onDeleteRequest: { onDeleteRequest(n.id) })
                }
            }
        }
        .padding(.horizontal, 20)
    }
}

struct NoteCard: View {
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
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Circle().fill(noteColor).frame(width: 8, height: 8)
                Spacer()
                Menu {
                    Button { onEdit() } label: { Label("Sửa", systemImage: "pencil") }
                    Button(role: .destructive) { onDeleteRequest() } label: { Label("Xoá", systemImage: "trash") }
                } label: {
                    Image(systemName: "ellipsis").font(.system(size: 13)).foregroundColor(c.textTertiary).padding(4)
                }
            }
            Text(note.title).font(.system(size: 13, weight: .semibold)).foregroundColor(c.textPrimary).lineLimit(2)
            Text(note.content).font(.system(size: 11)).foregroundColor(c.textSecondary).lineLimit(5)
            Spacer(minLength: 0)
            Text(note.createdAt.formatted(date: .abbreviated, time: .omitted)).font(.system(size: 9)).foregroundColor(c.textQuaternary)
        }
        .padding(12).frame(maxWidth: .infinity, minHeight: 120, alignment: .topLeading)
        .background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(noteColor.opacity(0.35), lineWidth: 1.5))
        .interactiveRow(onEdit: { onEdit() }, onDeleteRequest: { onDeleteRequest() })
    }
}

// MARK: - Reminder List
struct ReminderListContent: View {
    let reminders: [ReminderItem]
    let viewModel: TasksViewModel
    let onEdit: (ReminderItem) -> Void
    let onDeleteRequest: (UUID) -> Void
    var body: some View {
        VStack(spacing: 8) {
            GestureHintBar()
            ForEach(reminders) { r in
                ReminderRow(reminder: r, viewModel: viewModel, onEdit: onEdit, onDeleteRequest: onDeleteRequest)
            }
        }
        .padding(.horizontal, 20)
    }
}

struct ReminderRow: View {
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
            ZStack {
                Circle().fill(reminder.isActive ? c.gold.opacity(0.12) : c.surface).frame(width: 38, height: 38)
                Image(systemName: reminder.isActive ? "bell.fill" : "bell.slash")
                    .font(.system(size: 15)).foregroundColor(reminder.isActive ? c.gold2 : c.textTertiary)
            }
            VStack(alignment: .leading, spacing: 3) {
                Text(reminder.title).font(.system(size: 13, weight: .medium))
                    .foregroundColor(reminder.isActive ? c.textPrimary : c.textTertiary)
                HStack(spacing: 4) {
                    Image(systemName: "clock").font(.system(size: 10))
                    Text(timeString).font(.system(size: 11))
                }
                .foregroundColor(isPast ? c.red2 : c.textTertiary)
                if !reminder.note.isEmpty {
                    Text(reminder.note).font(.system(size: 11)).foregroundColor(c.textTertiary).lineLimit(1)
                }
            }
            Spacer()
            Toggle("", isOn: .init(get: { reminder.isActive }, set: { _ in viewModel.toggleReminder(reminder.id) }))
                .labelsHidden().tint(c.teal)
        }
        .padding(.horizontal, 13).padding(.vertical, 11)
        .background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(isPast ? c.red.opacity(0.25) : c.border, lineWidth: 1))
        .interactiveRow(onEdit: { onEdit(reminder) }, onDeleteRequest: { onDeleteRequest(reminder.id) })
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
                        .foregroundColor(isValid ? c.gold2 : c.textQuaternary)
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
                        let color: Color = val == 2 ? c.red2 : val == 1 ? c.gold2 : c.textTertiary
                        Button { taskPriority = val } label: {
                            HStack(spacing: 6) {
                                Circle().fill(color).frame(width: 8, height: 8)
                                Text(label).font(.system(size: 13, weight: taskPriority == val ? .semibold : .regular))
                            }
                            .foregroundColor(taskPriority == val ? color : c.textTertiary)
                            .frame(maxWidth: .infinity).padding(.vertical, 10)
                            .background(taskPriority == val ? color.opacity(0.12) : c.bg2)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                            .overlay(RoundedRectangle(cornerRadius: 8).stroke(taskPriority == val ? color.opacity(0.4) : c.border, lineWidth: 1))
                        }
                    }
                }
            }
            FormSection(title: "DEADLINE") {
                VStack(spacing: 0) {
                    HStack {
                        Image(systemName: "calendar.badge.clock").font(.system(size: 15)).foregroundColor(c.teal).frame(width: 24)
                        Text("Đặt deadline").font(.system(size: 14)).foregroundColor(c.textPrimary)
                        Spacer()
                        Toggle("", isOn: $hasDeadline).labelsHidden().tint(c.teal)
                    }
                    .padding(.horizontal, 14).padding(.vertical, 12)
                    if hasDeadline {
                        Divider().overlay(c.border).padding(.horizontal, 14)
                        DatePicker("", selection: $taskDeadline, in: Date()..., displayedComponents: [.date, .hourAndMinute])
                            .labelsHidden().datePickerStyle(.graphical).tint(c.teal)
                            .padding(.horizontal, 8).padding(.bottom, 8)
                    }
                }
                .background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(c.border, lineWidth: 1))
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
                .background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(c.border, lineWidth: 1))
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
                .background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(c.border, lineWidth: 1))
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
                    .labelsHidden().datePickerStyle(.graphical).tint(c.teal)
                    .padding(.horizontal, 8).padding(.vertical, 8)
                    .background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(c.border, lineWidth: 1))
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
            Image(systemName: icon).font(.system(size: 15)).foregroundColor(c.teal).frame(width: 24)
            TextField(placeholder, text: $text).font(.system(size: 14)).foregroundColor(c.textPrimary)
        }
        .padding(.horizontal, 14).padding(.vertical, 13)
        .background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(c.border, lineWidth: 1))
    }
}
