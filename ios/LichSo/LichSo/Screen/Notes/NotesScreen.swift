import SwiftUI
import SwiftData

// ═══════════════════════════════════════════
// Notes Screen — Tab "Ghi chú"
// 3 sub-tabs: Notes / Tasks / Reminders
// Matches screen-notes.html design
// ═══════════════════════════════════════════

private let PrimaryRed = Color(hex: "B71C1C")
private let SurfaceBg = Color(hex: "FFFBF5")
private let SurfaceContainer = Color(hex: "FFF8F0")
private let SurfaceContainerHigh = Color(hex: "FFF0E8")
private let TextMain = Color(hex: "1C1B1F")
private let TextSub = Color(hex: "534340")
private let TextDim = Color(hex: "857371")
private let OutlineVariant = Color(hex: "D8C2BF")
private let NoteBlue = Color(hex: "1565C0")
private let TaskGreen = Color(hex: "2E7D32")
private let RemindOrange = Color(hex: "E65100")
private let GoldAccent = Color(hex: "D4A017")

struct NotesScreen: View {
    @Environment(\.modelContext) private var modelContext
    @StateObject private var vm = NotesViewModel()
    @State private var showCreateSheet = false

    var body: some View {
        VStack(spacing: 0) {
            // ═══ TOP BAR ═══
            NotesTopBar()

            // ═══ STAT ROW ═══
            HStack(spacing: 8) {
                StatCard(value: vm.noteCount, label: "GHI CHÚ", color: NoteBlue)
                StatCard(value: vm.pendingTaskCount, label: "VIỆC CẦN LÀM", color: TaskGreen)
                StatCard(value: vm.activeReminderCount, label: "NHẮC NHỞ", color: RemindOrange)
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 10)

            // ═══ TAB BAR ═══
            NotesTabBar(selected: $vm.selectedTab)
                .padding(.horizontal, 20)

            // ═══ SEARCH ═══
            SearchRow(text: $vm.searchText)
                .padding(.horizontal, 20)
                .padding(.top, 10)
                .padding(.bottom, 6)

            // ═══ CONTENT ═══
            ScrollView(.vertical, showsIndicators: false) {
                switch vm.selectedTab {
                case .notes:   NotesListView(vm: vm)
                case .tasks:   TasksListView(vm: vm)
                case .reminders: RemindersListView(vm: vm)
                }
                Spacer().frame(height: 100)
            }
            .padding(.horizontal, 16)
        }
        .background(SurfaceBg)
        .overlay(alignment: .bottomTrailing) {
            FabButton(tab: vm.selectedTab) { showCreateSheet = true }
                .padding(.trailing, 20)
                .padding(.bottom, 100)
        }
        .sheet(isPresented: $showCreateSheet) {
            CreateSheet(tab: vm.selectedTab, vm: vm)
                .presentationDetents([.medium, .large])
        }
        .onAppear { vm.setModelContext(modelContext) }
    }
}

// ══════════════════════════════════════════
// TOP BAR
// ══════════════════════════════════════════

private struct NotesTopBar: View {
    var body: some View {
        HStack(spacing: 12) {
            Text("Ghi chú & Việc cần làm")
                .font(.system(size: 22, weight: .bold))
                .foregroundColor(TextMain)
            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.top, 8)
        .padding(.bottom, 10)
    }
}

// ══════════════════════════════════════════
// STAT CARDS
// ══════════════════════════════════════════

private struct StatCard: View {
    let value: Int
    let label: String
    let color: Color

    var body: some View {
        VStack(spacing: 3) {
            Text("\(value)")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(color)
            Text(label)
                .font(.system(size: 9, weight: .medium))
                .foregroundColor(TextDim)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 10)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(OutlineVariant, lineWidth: 1))
    }
}

// ══════════════════════════════════════════
// TAB BAR
// ══════════════════════════════════════════

private struct NotesTabBar: View {
    @Binding var selected: NotesTab

    var body: some View {
        HStack(spacing: 0) {
            ForEach(NotesTab.allCases, id: \.rawValue) { tab in
                Button {
                    withAnimation(.easeInOut(duration: 0.2)) { selected = tab }
                } label: {
                    HStack(spacing: 5) {
                        Image(systemName: tab.icon)
                            .font(.system(size: 14))
                        Text(tab.label)
                            .font(.system(size: 13, weight: .semibold))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .foregroundColor(selected == tab ? .white : TextSub)
                    .background(
                        selected == tab ? tabColor(tab) : Color.clear
                    )
                }
            }
        }
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(OutlineVariant, lineWidth: 1))
    }

    private func tabColor(_ tab: NotesTab) -> Color {
        switch tab { case .notes: return NoteBlue; case .tasks: return TaskGreen; case .reminders: return RemindOrange }
    }
}

// ══════════════════════════════════════════
// SEARCH ROW
// ══════════════════════════════════════════

private struct SearchRow: View {
    @Binding var text: String

    var body: some View {
        HStack(spacing: 8) {
            HStack(spacing: 8) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 14))
                    .foregroundColor(TextDim)
                TextField("Tìm kiếm...", text: $text)
                    .font(.system(size: 13))
                if !text.isEmpty {
                    Button { text = "" } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 14))
                            .foregroundColor(TextDim)
                    }
                }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 8)
            .background(SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(OutlineVariant, lineWidth: 1))
        }
    }
}

// ══════════════════════════════════════════
// NOTES LIST
// ══════════════════════════════════════════

private struct NotesListView: View {
    @ObservedObject var vm: NotesViewModel

    private var pinned: [NoteEntity] { vm.filteredNotes.filter { $0.isPinned } }
    private var others: [NoteEntity] { vm.filteredNotes.filter { !$0.isPinned } }

    var body: some View {
        LazyVStack(spacing: 0) {
            if vm.filteredNotes.isEmpty {
                EmptyHint(icon: "note.text", text: "Chưa có ghi chú nào\nNhấn + để tạo ghi chú mới")
            } else {
                if !pinned.isEmpty {
                    SectionDivider(text: "Đã ghim")
                    ForEach(pinned, id: \.id) { note in
                        NoteCard(note: note, onPin: { vm.togglePin(note) }, onDelete: { vm.deleteNote(note) })
                    }
                }
                if !others.isEmpty {
                    SectionDivider(text: pinned.isEmpty ? "Tất cả ghi chú" : "Khác")
                    ForEach(others, id: \.id) { note in
                        NoteCard(note: note, onPin: { vm.togglePin(note) }, onDelete: { vm.deleteNote(note) })
                    }
                }
            }
        }
    }
}

private struct NoteCard: View {
    let note: NoteEntity
    let onPin: () -> Void
    let onDelete: () -> Void

    private static let stripColors: [Color] = [NoteBlue, GoldAccent, Color(hex: "7B1FA2"), PrimaryRed, TaskGreen]

    var body: some View {
        HStack(spacing: 0) {
            // Left color strip
            Rectangle()
                .fill(Self.stripColors[note.colorIndex % Self.stripColors.count])
                .frame(width: 4)

            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(note.title)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(TextMain)
                        .lineLimit(1)
                    Spacer()
                    if note.isPinned {
                        Image(systemName: "pin.fill")
                            .font(.system(size: 12))
                            .foregroundColor(PrimaryRed)
                    }
                }

                if !note.content.isEmpty {
                    Text(note.content)
                        .font(.system(size: 12))
                        .foregroundColor(TextSub)
                        .lineLimit(2)
                }

                HStack(spacing: 5) {
                    if !note.labels.isEmpty {
                        ForEach(note.labels.components(separatedBy: ",").prefix(2), id: \.self) { label in
                            Text(label.trimmingCharacters(in: .whitespaces))
                                .font(.system(size: 10, weight: .semibold))
                                .padding(.horizontal, 7)
                                .padding(.vertical, 2)
                                .background(NoteBlue.opacity(0.08))
                                .foregroundColor(NoteBlue)
                                .clipShape(RoundedRectangle(cornerRadius: 6))
                        }
                    }
                    Spacer()
                    Text(NotesViewModel.relativeDate(note.updatedAt))
                        .font(.system(size: 10))
                        .foregroundColor(TextDim)
                }
                .padding(.top, 4)
            }
            .padding(14)
        }
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))
        .padding(.bottom, 8)
        .contextMenu {
            Button { onPin() } label: { Label(note.isPinned ? "Bỏ ghim" : "Ghim", systemImage: note.isPinned ? "pin.slash" : "pin") }
            Button(role: .destructive) { onDelete() } label: { Label("Xóa", systemImage: "trash") }
        }
    }
}

// ══════════════════════════════════════════
// TASKS LIST
// ══════════════════════════════════════════

private struct TasksListView: View {
    @ObservedObject var vm: NotesViewModel

    private var pending: [TaskEntity] { vm.filteredTasks.filter { !$0.isDone } }
    private var done: [TaskEntity] { vm.filteredTasks.filter { $0.isDone } }

    var body: some View {
        LazyVStack(spacing: 0) {
            if vm.filteredTasks.isEmpty {
                EmptyHint(icon: "checkmark.circle", text: "Chưa có việc cần làm\nNhấn + để tạo mới")
            } else {
                if !pending.isEmpty {
                    SectionDivider(text: "Đang làm · \(pending.count)")
                    ForEach(pending, id: \.id) { task in
                        TaskCard(task: task, onToggle: { vm.toggleTask(task) }, onDelete: { vm.deleteTask(task) })
                    }
                }
                if !done.isEmpty {
                    SectionDivider(text: "Hoàn thành · \(done.count)")
                    ForEach(done, id: \.id) { task in
                        TaskCard(task: task, onToggle: { vm.toggleTask(task) }, onDelete: { vm.deleteTask(task) })
                    }
                }
            }
        }
    }
}

private struct TaskCard: View {
    let task: TaskEntity
    let onToggle: () -> Void
    let onDelete: () -> Void

    private var isOverdue: Bool {
        guard let due = task.dueDate else { return false }
        return !task.isDone && due < Int64(Date().timeIntervalSince1970 * 1000)
    }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            // Checkbox
            Button(action: onToggle) {
                ZStack {
                    RoundedRectangle(cornerRadius: 7)
                        .fill(task.isDone ? TaskGreen : Color.clear)
                        .frame(width: 22, height: 22)
                        .overlay(
                            RoundedRectangle(cornerRadius: 7)
                                .stroke(task.isDone ? TaskGreen : TaskGreen, lineWidth: 2)
                        )
                    if task.isDone {
                        Image(systemName: "checkmark")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.white)
                    }
                }
            }
            .padding(.top, 1)

            // Info
            VStack(alignment: .leading, spacing: 4) {
                Text(task.title)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(task.isDone ? TextDim : TextMain)
                    .strikethrough(task.isDone)
                    .lineLimit(2)

                HStack(spacing: 5) {
                    if let due = task.dueDate {
                        HStack(spacing: 3) {
                            Image(systemName: "calendar")
                                .font(.system(size: 9))
                            Text(NotesViewModel.formatDate(due))
                                .font(.system(size: 10, weight: .semibold))
                        }
                        .padding(.horizontal, 7)
                        .padding(.vertical, 2)
                        .background(isOverdue ? Color(hex: "C62828").opacity(0.08) : TaskGreen.opacity(0.08))
                        .foregroundColor(isOverdue ? Color(hex: "C62828") : TaskGreen)
                        .clipShape(RoundedRectangle(cornerRadius: 6))
                    }

                    if !task.labels.isEmpty {
                        Text(task.labels)
                            .font(.system(size: 10, weight: .semibold))
                            .padding(.horizontal, 7)
                            .padding(.vertical, 2)
                            .background(TaskGreen.opacity(0.08))
                            .foregroundColor(TaskGreen)
                            .clipShape(RoundedRectangle(cornerRadius: 6))
                    }
                }
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 4) {
                // Priority dot
                Circle()
                    .fill(task.priority >= 3 ? Color(hex: "C62828") : task.priority == 2 ? Color(hex: "F57F17") : TaskGreen)
                    .frame(width: 8, height: 8)
                Text(NotesViewModel.relativeDate(task.createdAt))
                    .font(.system(size: 10))
                    .foregroundColor(TextDim)
            }
        }
        .padding(14)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))
        .opacity(task.isDone ? 0.5 : 1)
        .padding(.bottom, 8)
        .contextMenu {
            Button(role: .destructive) { onDelete() } label: { Label("Xóa", systemImage: "trash") }
        }
    }
}

// ══════════════════════════════════════════
// REMINDERS LIST
// ══════════════════════════════════════════

private struct RemindersListView: View {
    @ObservedObject var vm: NotesViewModel

    var body: some View {
        LazyVStack(spacing: 0) {
            if vm.filteredReminders.isEmpty {
                EmptyHint(icon: "alarm", text: "Chưa có nhắc nhở nào\nNhấn + để tạo nhắc nhở mới")
            } else {
                ForEach(vm.filteredReminders, id: \.id) { reminder in
                    ReminderCard(reminder: reminder, onToggle: { vm.toggleReminder(reminder) }, onDelete: { vm.deleteReminder(reminder) })
                }
            }
        }
    }
}

private struct ReminderCard: View {
    let reminder: ReminderEntity
    let onToggle: () -> Void
    let onDelete: () -> Void

    private var catIdx: Int { min(reminder.category, NotesViewModel.categoryIcons.count - 1) }

    var body: some View {
        HStack(spacing: 12) {
            // Icon
            Image(systemName: NotesViewModel.categoryIcons[catIdx])
                .font(.system(size: 18))
                .foregroundColor(NotesViewModel.categoryColors[catIdx])
                .frame(width: 42, height: 42)
                .background(NotesViewModel.categoryBgs[catIdx])
                .clipShape(RoundedRectangle(cornerRadius: 12))

            // Info
            VStack(alignment: .leading, spacing: 2) {
                Text(reminder.title)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(TextMain)
                    .lineLimit(1)

                HStack(spacing: 4) {
                    Image(systemName: "clock")
                        .font(.system(size: 10))
                    Text("\(NotesViewModel.formatDate(reminder.triggerTime)) · \(NotesViewModel.formatTime(reminder.triggerTime))")
                        .font(.system(size: 11))
                }
                .foregroundColor(TextSub)

                if reminder.repeatType > 0 && reminder.repeatType < NotesViewModel.repeatLabels.count {
                    HStack(spacing: 3) {
                        Image(systemName: "repeat")
                            .font(.system(size: 9))
                        Text(NotesViewModel.repeatLabels[reminder.repeatType])
                            .font(.system(size: 10, weight: .medium))
                    }
                    .foregroundColor(RemindOrange)
                }
            }

            Spacer()

            Toggle("", isOn: Binding(
                get: { reminder.isEnabled },
                set: { _ in onToggle() }
            ))
            .tint(RemindOrange)
            .labelsHidden()
        }
        .padding(14)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))
        .padding(.bottom, 8)
        .contextMenu {
            Button(role: .destructive) { onDelete() } label: { Label("Xóa", systemImage: "trash") }
        }
    }
}

// ══════════════════════════════════════════
// SHARED COMPONENTS
// ══════════════════════════════════════════

private struct SectionDivider: View {
    let text: String
    var body: some View {
        HStack(spacing: 10) {
            Text(text)
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(TextDim)
                .textCase(.uppercase)
                .tracking(0.6)
            Rectangle().fill(OutlineVariant).frame(height: 1)
        }
        .padding(.vertical, 10)
    }
}

private struct EmptyHint: View {
    let icon: String
    let text: String
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 40))
                .foregroundColor(TextDim.opacity(0.4))
            Text(text)
                .font(.system(size: 14))
                .foregroundColor(TextDim)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 60)
    }
}

private struct FabButton: View {
    let tab: NotesTab
    let action: () -> Void

    private var fabColor: Color {
        switch tab { case .notes: return NoteBlue; case .tasks: return TaskGreen; case .reminders: return RemindOrange }
    }
    private var fabIcon: String {
        switch tab { case .notes: return "square.and.pencil"; case .tasks: return "plus.circle"; case .reminders: return "alarm.waves.left.and.right" }
    }

    var body: some View {
        Button(action: action) {
            Image(systemName: fabIcon)
                .font(.system(size: 22, weight: .semibold))
                .foregroundColor(.white)
                .frame(width: 56, height: 56)
                .background(fabColor)
                .clipShape(Circle())
                .shadow(color: fabColor.opacity(0.3), radius: 8, y: 4)
        }
    }
}

// ══════════════════════════════════════════
// CREATE SHEET
// ══════════════════════════════════════════

private struct CreateSheet: View {
    let tab: NotesTab
    @ObservedObject var vm: NotesViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var title = ""
    @State private var content = ""
    @State private var priority = 1
    @State private var dueDate = Date()
    @State private var hasDueDate = false
    @State private var reminderDate = Date().addingTimeInterval(3600)
    @State private var repeatType = 0
    @State private var category = 3 // Personal

    var body: some View {
        NavigationStack {
            Form {
                switch tab {
                case .notes:
                    Section("Ghi chú mới") {
                        TextField("Tiêu đề", text: $title)
                        TextEditor(text: $content)
                            .frame(minHeight: 120)
                    }
                case .tasks:
                    Section("Việc cần làm mới") {
                        TextField("Tiêu đề", text: $title)
                        Picker("Ưu tiên", selection: $priority) {
                            Text("Thấp").tag(1)
                            Text("Trung bình").tag(2)
                            Text("Cao").tag(3)
                        }
                        Toggle("Ngày hạn", isOn: $hasDueDate)
                        if hasDueDate {
                            DatePicker("Hạn", selection: $dueDate, displayedComponents: .date)
                                .environment(\.locale, Locale(identifier: "vi_VN"))
                        }
                    }
                case .reminders:
                    Section("Nhắc nhở mới") {
                        TextField("Tiêu đề", text: $title)
                        DatePicker("Thời gian", selection: $reminderDate, displayedComponents: [.date, .hourAndMinute])
                            .environment(\.locale, Locale(identifier: "vi_VN"))
                        Picker("Lặp lại", selection: $repeatType) {
                            ForEach(0..<NotesViewModel.repeatLabels.count, id: \.self) { i in
                                Text(NotesViewModel.repeatLabels[i]).tag(i)
                            }
                        }
                        Picker("Danh mục", selection: $category) {
                            Text("Ngày lễ").tag(0)
                            Text("Sinh nhật").tag(1)
                            Text("Âm lịch").tag(2)
                            Text("Cá nhân").tag(3)
                            Text("Tưởng niệm").tag(4)
                        }
                    }
                }
            }
            .navigationTitle(tab == .notes ? "Ghi chú mới" : tab == .tasks ? "Việc mới" : "Nhắc nhở mới")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Hủy") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Lưu") {
                        save()
                        dismiss()
                    }
                    .disabled(title.trimmingCharacters(in: .whitespaces).isEmpty)
                    .fontWeight(.semibold)
                }
            }
        }
    }

    private func save() {
        let t = title.trimmingCharacters(in: .whitespaces)
        guard !t.isEmpty else { return }
        switch tab {
        case .notes:
            vm.createNote(title: t, content: content)
        case .tasks:
            vm.createTask(title: t, priority: priority, dueDate: hasDueDate ? dueDate : nil)
        case .reminders:
            vm.createReminder(title: t, triggerTime: reminderDate, repeatType: repeatType, category: category)
        }
    }
}
