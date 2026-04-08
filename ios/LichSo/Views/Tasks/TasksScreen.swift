import SwiftUI
import SwiftData

struct TasksScreen: View {
    @Environment(\.lichSoColors) private var c
    @Environment(\.modelContext) private var modelContext
    @State private var tasks: [TaskEntity] = []
    @State private var notes: [NoteEntity] = []
    @State private var selectedTab = 0 // 0 = tasks, 1 = notes
    @State private var showAddSheet = false
    @State private var editingTask: TaskEntity? = nil
    @State private var editingNote: NoteEntity? = nil
    var onBackClick: () -> Void = {}
    var onMenuClick: () -> Void = {}

    var body: some View {
        VStack(spacing: 0) {
            // Top bar
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(c.textPrimary)
                }
                Spacer()
                Text("GHI CHÚ & CÔNG VIỆC")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(c.textPrimary)
                    .tracking(1)
                Spacer()
                Button(action: { showAddSheet = true }) {
                    Image(systemName: "plus.circle.fill")
                        .font(.system(size: 22))
                        .foregroundColor(c.primary)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            // Tab selector
            HStack(spacing: 0) {
                tabButton(title: "Công việc", index: 0)
                tabButton(title: "Ghi chú", index: 1)
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 8)

            // Content
            ScrollView {
                if selectedTab == 0 {
                    tasksContent
                } else {
                    notesContent
                }
            }
        }
        .background(c.bg)
        .onAppear { loadData() }
        .sheet(isPresented: $showAddSheet) {
            if selectedTab == 0 {
                TaskEditSheet(task: nil, c: c, onSave: { title, desc, dueDate, priority in
                    addTask(title: title, description: desc, dueDate: dueDate, priority: priority)
                })
            } else {
                NoteEditSheet(note: nil, c: c, onSave: { title, content, color in
                    addNote(title: title, content: content, color: color)
                })
            }
        }
    }

    @ViewBuilder
    private func tabButton(title: String, index: Int) -> some View {
        Button(action: { selectedTab = index }) {
            Text(title)
                .font(.system(size: 14, weight: selectedTab == index ? .bold : .medium))
                .foregroundColor(selectedTab == index ? c.primary : c.textTertiary)
                .padding(.vertical, 8)
                .frame(maxWidth: .infinity)
                .overlay(alignment: .bottom) {
                    if selectedTab == index {
                        Rectangle()
                            .fill(c.primary)
                            .frame(height: 2)
                    }
                }
        }
    }

    // MARK: - Tasks Content
    private var tasksContent: some View {
        LazyVStack(spacing: 8) {
            if tasks.isEmpty {
                emptyState(icon: "checkmark.circle", title: "Chưa có công việc", subtitle: "Nhấn + để thêm công việc mới")
            }
            ForEach(tasks, id: \.id) { task in
                TaskRow(task: task, c: c, onToggle: {
                    task.isDone.toggle()
                    try? modelContext.save()
                    loadData()
                }, onDelete: {
                    modelContext.delete(task)
                    try? modelContext.save()
                    loadData()
                })
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 8)
    }

    // MARK: - Notes Content
    private var notesContent: some View {
        LazyVStack(spacing: 8) {
            if notes.isEmpty {
                emptyState(icon: "note.text", title: "Chưa có ghi chú", subtitle: "Nhấn + để thêm ghi chú mới")
            }
            ForEach(notes, id: \.id) { note in
                NoteRow(note: note, c: c, onDelete: {
                    modelContext.delete(note)
                    try? modelContext.save()
                    loadData()
                })
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 8)
    }

    @ViewBuilder
    private func emptyState(icon: String, title: String, subtitle: String) -> some View {
        VStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 48))
                .foregroundColor(c.textQuaternary)
            Text(title)
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(c.textSecondary)
            Text(subtitle)
                .font(.system(size: 13))
                .foregroundColor(c.textTertiary)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 60)
    }

    private func loadData() {
        let taskDescriptor = FetchDescriptor<TaskEntity>(sortBy: [SortDescriptor(\.createdAt, order: .reverse)])
        let noteDescriptor = FetchDescriptor<NoteEntity>(sortBy: [SortDescriptor(\.createdAt, order: .reverse)])
        tasks = (try? modelContext.fetch(taskDescriptor)) ?? []
        notes = (try? modelContext.fetch(noteDescriptor)) ?? []
    }

    private func addTask(title: String, description: String, dueDate: Date?, priority: Int) {
        let task = TaskEntity(title: title, taskDescription: description, dueDate: dueDate, priority: priority)
        modelContext.insert(task)
        try? modelContext.save()
        loadData()
    }

    private func addNote(title: String, content: String, color: String) {
        let note = NoteEntity(title: title, content: content)
        modelContext.insert(note)
        try? modelContext.save()
        loadData()
    }
}

// MARK: - Task Row
struct TaskRow: View {
    let task: TaskEntity
    let c: LichSoColors
    let onToggle: () -> Void
    let onDelete: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Button(action: onToggle) {
                Image(systemName: task.isDone ? "checkmark.circle.fill" : "circle")
                    .font(.system(size: 22))
                    .foregroundColor(task.isDone ? c.goodGreen : c.outline)
            }
            VStack(alignment: .leading, spacing: 3) {
                Text(task.title)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(task.isDone ? c.textTertiary : c.textPrimary)
                    .strikethrough(task.isDone)
                if !task.taskDescription.isEmpty {
                    Text(task.taskDescription)
                        .font(.system(size: 12))
                        .foregroundColor(c.textTertiary)
                        .lineLimit(2)
                }
                if let dueDate = task.dueDate {
                    Text(formatDate(dueDate))
                        .font(.system(size: 11))
                        .foregroundColor(c.noteOrange)
                }
            }
            Spacer()
            Button(action: onDelete) {
                Image(systemName: "trash")
                    .font(.system(size: 14))
                    .foregroundColor(c.textTertiary)
            }
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(c.surface)
        )
    }

    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        return formatter.string(from: date)
    }
}

// MARK: - Note Row
struct NoteRow: View {
    let note: NoteEntity
    let c: LichSoColors
    let onDelete: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(note.title.isEmpty ? "Không có tiêu đề" : note.title)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(c.textPrimary)
                Spacer()
                Button(action: onDelete) {
                    Image(systemName: "trash")
                        .font(.system(size: 14))
                        .foregroundColor(c.textTertiary)
                }
            }
            if !note.content.isEmpty {
                Text(note.content)
                    .font(.system(size: 13))
                    .foregroundColor(c.textSecondary)
                    .lineLimit(4)
            }
            Text(formatDate(note.createdAt))
                .font(.system(size: 11))
                .foregroundColor(c.textTertiary)
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(c.surface)
        )
    }

    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy HH:mm"
        return formatter.string(from: date)
    }
}

// MARK: - Task Edit Sheet
struct TaskEditSheet: View {
    let task: TaskEntity?
    let c: LichSoColors
    let onSave: (String, String, Date?, Int) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var title = ""
    @State private var description = ""
    @State private var dueDate = Date()
    @State private var hasDueDate = false
    @State private var priority = 0

    var body: some View {
        NavigationView {
            Form {
                Section("Thông tin") {
                    TextField("Tiêu đề", text: $title)
                    TextField("Mô tả", text: $description, axis: .vertical)
                        .lineLimit(3...6)
                }
                Section("Thời hạn") {
                    Toggle("Đặt thời hạn", isOn: $hasDueDate)
                    if hasDueDate {
                        DatePicker("Ngày", selection: $dueDate, displayedComponents: [.date, .hourAndMinute])
                    }
                }
                Section("Mức ưu tiên") {
                    Picker("Ưu tiên", selection: $priority) {
                        Text("Bình thường").tag(0)
                        Text("Quan trọng").tag(1)
                        Text("Khẩn cấp").tag(2)
                    }
                    .pickerStyle(.segmented)
                }
            }
            .navigationTitle("Thêm công việc")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Hủy") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Lưu") {
                        onSave(title, description, hasDueDate ? dueDate : nil, priority)
                        dismiss()
                    }
                    .disabled(title.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
        .onAppear {
            if let task = task {
                title = task.title
                description = task.taskDescription
                if let dd = task.dueDate {
                    dueDate = dd
                    hasDueDate = true
                }
                priority = task.priority
            }
        }
    }
}

// MARK: - Note Edit Sheet
struct NoteEditSheet: View {
    let note: NoteEntity?
    let c: LichSoColors
    let onSave: (String, String, String) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var title = ""
    @State private var content = ""
    @State private var color = "gold"

    private let colors = ["gold", "teal", "orange", "purple", "green", "red"]

    var body: some View {
        NavigationView {
            Form {
                Section("Thông tin") {
                    TextField("Tiêu đề", text: $title)
                    TextEditor(text: $content)
                        .frame(minHeight: 120)
                }
                Section("Màu sắc") {
                    HStack(spacing: 12) {
                        ForEach(colors, id: \.self) { colorName in
                            Circle()
                                .fill(noteColor(colorName))
                                .frame(width: 30, height: 30)
                                .overlay(
                                    Circle().stroke(.white, lineWidth: color == colorName ? 2 : 0)
                                )
                                .onTapGesture { color = colorName }
                        }
                    }
                }
            }
            .navigationTitle("Thêm ghi chú")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Hủy") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Lưu") {
                        onSave(title, content, color)
                        dismiss()
                    }
                }
            }
        }
        .onAppear {
            if let note = note {
                title = note.title
                content = note.content
                let colorNames = ["gold", "teal", "orange", "purple", "green", "red"]
                color = note.colorIndex >= 0 && note.colorIndex < colorNames.count ? colorNames[note.colorIndex] : "gold"
            }
        }
    }

    private func noteColor(_ name: String) -> Color {
        switch name {
        case "gold": return c.noteGold
        case "teal": return c.noteTeal
        case "orange": return c.noteOrange
        case "purple": return c.notePurple
        case "green": return c.noteGreen
        case "red": return c.noteRed
        default: return c.noteGold
        }
    }
}
