import SwiftUI
import SwiftData

// ═══════════════════════════════════════════
// Note Detail Screen — View a note in full
// Tappable from list → shows full content
// ═══════════════════════════════════════════

private var PrimaryRed: Color { LSTheme.primary }
private var SurfaceBg: Color { LSTheme.bg }
private var SurfaceC: Color { LSTheme.surfaceContainer }
private var TextMain: Color { LSTheme.textPrimary }
private var TextSub: Color { LSTheme.textSecondary }
private var TextDim: Color { LSTheme.textTertiary }
private var OutlineVar: Color { LSTheme.outlineVariant }
private var NoteBlue     : Color { LSTheme.noteBlue }
private var TaskGreen: Color { LSTheme.goodGreen }
private var RemindOrange : Color { LSTheme.remindOrange }
private var GoldAccent: Color { LSTheme.gold }

// ═══ Note background colors (same order as edit screen) ═══
private let noteBgColors: [Color] = [
    Color(hex: "1A1814"), Color(hex: "FFE082"), Color(hex: "EF9A9A"),
    Color(hex: "90CAF9"), Color(hex: "A5D6A7"), Color(hex: "CE93D8"), Color(hex: "FFCC80")
]

// ═══ Strip colors for note cards ═══
private let stripColors: [Color] = [
    Color(hex: "1565C0"), Color(hex: "D4A017"), Color(hex: "7B1FA2"),
    Color(hex: "B71C1C"), Color(hex: "2E7D32")
]

// ══════════════════════════════════════════
// MARK: — NoteDetailScreen
// ══════════════════════════════════════════

struct NoteDetailScreen: View {
    @Environment(\.dismiss) private var dismiss
    let note: NoteEntity
    @ObservedObject var vm: NotesViewModel
    @State private var showEdit = false

    private var bgColor: Color {
        let idx = note.colorIndex % noteBgColors.count
        return noteBgColors[idx]
    }

    var body: some View {
        VStack(spacing: 0) {
            // Top bar
            HStack {
                Button { dismiss() } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(TextMain)
                        .frame(width: 40, height: 40)
                }

                Spacer()

                if note.isPinned {
                    Image(systemName: "pin.fill")
                        .font(.system(size: 14))
                        .foregroundColor(PrimaryRed)
                }

                Button { vm.togglePin(note) } label: {
                    Image(systemName: note.isPinned ? "pin.slash" : "pin")
                        .font(.system(size: 16))
                        .foregroundColor(TextSub)
                        .frame(width: 38, height: 38)
                }

                Button { showEdit = true } label: {
                    Image(systemName: "pencil")
                        .font(.system(size: 16))
                        .foregroundColor(TextSub)
                        .frame(width: 38, height: 38)
                }

                Menu {
                    Button { vm.togglePin(note) } label: {
                        Label(note.isPinned ? "Bỏ ghim" : "Ghim", systemImage: note.isPinned ? "pin.slash" : "pin")
                    }
                    Button(role: .destructive) {
                        vm.deleteNote(note)
                        dismiss()
                    } label: {
                        Label("Xóa", systemImage: "trash")
                    }
                } label: {
                    Image(systemName: "ellipsis")
                        .font(.system(size: 16))
                        .foregroundColor(TextSub)
                        .frame(width: 38, height: 38)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)

            ScrollView(.vertical, showsIndicators: false) {
                VStack(alignment: .leading, spacing: 12) {
                    // Title
                    Text(note.title)
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(TextMain)

                    // Labels
                    if !note.labels.isEmpty {
                        HStack(spacing: 6) {
                            ForEach(note.labels.components(separatedBy: ","), id: \.self) { label in
                                let l = label.trimmingCharacters(in: .whitespaces)
                                if !l.isEmpty {
                                    Text(l)
                                        .font(.system(size: 11, weight: .semibold))
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 4)
                                        .background(NoteBlue.opacity(0.08))
                                        .foregroundColor(NoteBlue)
                                        .clipShape(RoundedRectangle(cornerRadius: 8))
                                }
                            }
                        }
                    }

                    // Date info
                    HStack(spacing: 8) {
                        Image(systemName: "clock")
                            .font(.system(size: 11))
                        Text("Cập nhật: \(NotesViewModel.formatDate(note.updatedAt)) lúc \(NotesViewModel.formatTime(note.updatedAt))")
                            .font(.system(size: 11))
                    }
                    .foregroundColor(TextDim)

                    Divider()

                    // Content — parse checklist items
                    let lines = note.content.components(separatedBy: "\n")
                    ForEach(Array(lines.enumerated()), id: \.offset) { _, line in
                        let trimmed = line.trimmingCharacters(in: .whitespaces)
                        if trimmed.hasPrefix("[x] ") || trimmed.hasPrefix("[X] ") {
                            HStack(alignment: .top, spacing: 10) {
                                Image(systemName: "checkmark.square.fill")
                                    .font(.system(size: 16))
                                    .foregroundColor(TaskGreen)
                                Text(String(trimmed.dropFirst(4)))
                                    .font(.system(size: 15))
                                    .foregroundColor(TextDim)
                                    .strikethrough()
                            }
                            .padding(.vertical, 2)
                        } else if trimmed.hasPrefix("[ ] ") {
                            HStack(alignment: .top, spacing: 10) {
                                Image(systemName: "square")
                                    .font(.system(size: 16))
                                    .foregroundColor(TaskGreen)
                                Text(String(trimmed.dropFirst(4)))
                                    .font(.system(size: 15))
                                    .foregroundColor(TextMain)
                            }
                            .padding(.vertical, 2)
                        } else if !trimmed.isEmpty {
                            Text(line)
                                .font(.system(size: 15))
                                .foregroundColor(TextMain)
                                .lineSpacing(6)
                        }
                    }
                }
                .padding(20)
                .padding(.bottom, 40)
            }
        }
        .background(bgColor)
        .navigationBarHidden(true)
        .fullScreenCover(isPresented: $showEdit) {
            NavigationStack {
                NoteEditScreen(existingNote: note, initialType: .note, vm: vm)
            }
        }
    }
}

// ══════════════════════════════════════════
// MARK: — TaskDetailScreen
// ══════════════════════════════════════════

struct TaskDetailScreen: View {
    @Environment(\.dismiss) private var dismiss
    let task: TaskEntity
    @ObservedObject var vm: NotesViewModel
    @State private var showEdit = false

    private var isOverdue: Bool {
        guard let due = task.dueDate else { return false }
        return !task.isDone && due < Int64(Date().timeIntervalSince1970 * 1000)
    }

    var body: some View {
        VStack(spacing: 0) {
            // Top bar
            HStack {
                Button { dismiss() } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(TextMain)
                        .frame(width: 40, height: 40)
                }

                Text("Chi tiết công việc")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(TextMain)

                Spacer()

                Button { showEdit = true } label: {
                    Image(systemName: "pencil")
                        .font(.system(size: 16))
                        .foregroundColor(TextSub)
                        .frame(width: 38, height: 38)
                }

                Menu {
                    Button { vm.toggleTask(task); dismiss() } label: {
                        Label(task.isDone ? "Đánh dấu chưa xong" : "Hoàn thành", systemImage: task.isDone ? "arrow.uturn.backward" : "checkmark.circle")
                    }
                    Button(role: .destructive) { vm.deleteTask(task); dismiss() } label: {
                        Label("Xóa", systemImage: "trash")
                    }
                } label: {
                    Image(systemName: "ellipsis")
                        .font(.system(size: 16))
                        .foregroundColor(TextSub)
                        .frame(width: 38, height: 38)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)

            ScrollView(.vertical, showsIndicators: false) {
                VStack(alignment: .leading, spacing: 16) {
                    // Status badge
                    HStack(spacing: 8) {
                        Image(systemName: task.isDone ? "checkmark.circle.fill" : "circle")
                            .font(.system(size: 20))
                            .foregroundColor(task.isDone ? TaskGreen : TextDim)

                        Text(task.title)
                            .font(.system(size: 22, weight: .bold))
                            .foregroundColor(task.isDone ? TextDim : TextMain)
                            .strikethrough(task.isDone)
                    }

                    // Priority + Due date chips
                    HStack(spacing: 8) {
                        // Priority
                        HStack(spacing: 4) {
                            Circle()
                                .fill(task.priority >= 3 ? Color(hex: "C62828") : task.priority == 2 ? Color(hex: "F57F17") : TaskGreen)
                                .frame(width: 8, height: 8)
                            Text(task.priority >= 3 ? "Ưu tiên cao" : task.priority == 2 ? "Trung bình" : "Thấp")
                                .font(.system(size: 11, weight: .semibold))
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(
                            (task.priority >= 3 ? Color(hex: "C62828") : task.priority == 2 ? Color(hex: "F57F17") : TaskGreen).opacity(0.08)
                        )
                        .foregroundColor(task.priority >= 3 ? Color(hex: "C62828") : task.priority == 2 ? Color(hex: "F57F17") : TaskGreen)
                        .clipShape(RoundedRectangle(cornerRadius: 8))

                        if let due = task.dueDate {
                            HStack(spacing: 4) {
                                Image(systemName: "calendar")
                                    .font(.system(size: 10))
                                Text(NotesViewModel.formatDate(due))
                                    .font(.system(size: 11, weight: .semibold))
                            }
                            .padding(.horizontal, 10)
                            .padding(.vertical, 5)
                            .background(isOverdue ? Color(hex: "C62828").opacity(0.08) : TaskGreen.opacity(0.08))
                            .foregroundColor(isOverdue ? Color(hex: "C62828") : TaskGreen)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                        }
                    }

                    Divider()

                    // Checklist from description
                    if !task.taskDescription.isEmpty {
                        let lines = task.taskDescription.components(separatedBy: "\n")
                        let hasChecklist = lines.contains { $0.trimmingCharacters(in: .whitespaces).hasPrefix("[") }

                        if hasChecklist {
                            Text("Các bước thực hiện")
                                .font(.system(size: 13, weight: .bold))
                                .foregroundColor(TextSub)

                            ForEach(Array(lines.enumerated()), id: \.offset) { _, line in
                                let trimmed = line.trimmingCharacters(in: .whitespaces)
                                if trimmed.hasPrefix("[x] ") || trimmed.hasPrefix("[X] ") {
                                    HStack(alignment: .top, spacing: 10) {
                                        Image(systemName: "checkmark.square.fill")
                                            .font(.system(size: 16))
                                            .foregroundColor(TaskGreen)
                                        Text(String(trimmed.dropFirst(4)))
                                            .font(.system(size: 14))
                                            .foregroundColor(TextDim)
                                            .strikethrough()
                                    }
                                    .padding(.vertical, 3)
                                } else if trimmed.hasPrefix("[ ] ") {
                                    HStack(alignment: .top, spacing: 10) {
                                        Image(systemName: "square")
                                            .font(.system(size: 16))
                                            .foregroundColor(TaskGreen)
                                        Text(String(trimmed.dropFirst(4)))
                                            .font(.system(size: 14))
                                            .foregroundColor(TextMain)
                                    }
                                    .padding(.vertical, 3)
                                } else if !trimmed.isEmpty {
                                    Text(line)
                                        .font(.system(size: 14))
                                        .foregroundColor(TextMain)
                                }
                            }

                            // Progress bar
                            let total = lines.filter { $0.contains("[ ] ") || $0.contains("[x] ") || $0.contains("[X] ") }.count
                            let completed = lines.filter { $0.contains("[x] ") || $0.contains("[X] ") }.count
                            if total > 0 {
                                HStack(spacing: 8) {
                                    ProgressView(value: Double(completed), total: Double(total))
                                        .tint(TaskGreen)
                                    Text("\(completed)/\(total)")
                                        .font(.system(size: 11, weight: .semibold))
                                        .foregroundColor(TaskGreen)
                                }
                                .padding(.top, 4)
                            }
                        } else {
                            Text(task.taskDescription)
                                .font(.system(size: 15))
                                .foregroundColor(TextMain)
                                .lineSpacing(6)
                        }
                    }

                    // Info rows
                    VStack(spacing: 0) {
                        detailRow(icon: "calendar", label: "Tạo ngày", value: NotesViewModel.formatDate(task.createdAt))
                        detailRow(icon: "clock.arrow.circlepath", label: "Cập nhật", value: NotesViewModel.formatDate(task.updatedAt))
                        if task.hasReminder {
                            detailRow(icon: "alarm", label: "Nhắc nhở", value: "Đã bật")
                        }
                    }
                    .padding(.top, 8)
                }
                .padding(20)
                .padding(.bottom, 40)
            }

            // Bottom action
            HStack(spacing: 12) {
                Button {
                    vm.toggleTask(task)
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: task.isDone ? "arrow.uturn.backward" : "checkmark.circle.fill")
                            .font(.system(size: 16))
                        Text(task.isDone ? "Đánh dấu chưa xong" : "Hoàn thành")
                            .font(.system(size: 14, weight: .semibold))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(task.isDone ? SurfaceC : TaskGreen)
                    .foregroundColor(task.isDone ? TextMain : .white)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 30)
        }
        .background(SurfaceBg)
        .navigationBarHidden(true)
        .fullScreenCover(isPresented: $showEdit) {
            NavigationStack {
                NoteEditScreen(existingTask: task, initialType: .task, vm: vm)
            }
        }
    }

    private func detailRow(icon: String, label: String, value: String) -> some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundColor(PrimaryRed)
                .frame(width: 32, height: 32)
                .background(SurfaceC)
                .clipShape(RoundedRectangle(cornerRadius: 8))
            VStack(alignment: .leading, spacing: 1) {
                Text(label)
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(TextDim)
                Text(value)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(TextMain)
            }
            Spacer()
        }
        .padding(.vertical, 8)
    }
}

// ══════════════════════════════════════════
// MARK: — ReminderDetailScreen
// ══════════════════════════════════════════

struct ReminderDetailScreen: View {
    @Environment(\.dismiss) private var dismiss
    let reminder: ReminderEntity
    @ObservedObject var vm: NotesViewModel
    @State private var showEdit = false

    private var catIdx: Int { min(reminder.category, NotesViewModel.categoryIcons.count - 1) }

    var body: some View {
        VStack(spacing: 0) {
            // Header (orange gradient like mock)
            VStack(spacing: 0) {
                HStack {
                    Button { dismiss() } label: {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundColor(.white)
                            .frame(width: 40, height: 40)
                            .background(.white.opacity(0.15))
                            .clipShape(Circle())
                    }

                    Text("Chi tiết nhắc nhở")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.white)

                    Spacer()

                    Button { showEdit = true } label: {
                        Image(systemName: "pencil")
                            .font(.system(size: 16))
                            .foregroundColor(.white)
                            .frame(width: 40, height: 40)
                            .background(.white.opacity(0.15))
                            .clipShape(Circle())
                    }

                    Menu {
                        Button { vm.toggleReminder(reminder) } label: {
                            Label(reminder.isEnabled ? "Tắt" : "Bật", systemImage: reminder.isEnabled ? "bell.slash" : "bell")
                        }
                        Button(role: .destructive) { vm.deleteReminder(reminder); dismiss() } label: {
                            Label("Xóa", systemImage: "trash")
                        }
                    } label: {
                        Image(systemName: "ellipsis")
                            .font(.system(size: 16))
                            .foregroundColor(.white)
                            .frame(width: 40, height: 40)
                            .background(.white.opacity(0.15))
                            .clipShape(Circle())
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)

                // Big icon + title
                VStack(spacing: 8) {
                    Image(systemName: NotesViewModel.categoryIcons[catIdx])
                        .font(.system(size: 36))
                        .foregroundColor(.white)

                    Text(reminder.title)
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(.white)
                        .multilineTextAlignment(.center)

                    if !reminder.subtitle.isEmpty {
                        Text(reminder.subtitle)
                            .font(.system(size: 13))
                            .foregroundColor(.white.opacity(0.8))
                    }
                }
                .padding(.vertical, 20)
            }
            .background(
                LinearGradient(colors: [Color(hex: "E65100"), Color(hex: "EF6C00"), Color(hex: "F57C00")], startPoint: .topLeading, endPoint: .bottomTrailing)
            )

            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 12) {
                    // Info cards
                    infoCard(icon: "calendar", label: "Ngày nhắc", value: NotesViewModel.formatDate(reminder.triggerTime))
                    infoCard(icon: "clock", label: "Giờ nhắc", value: NotesViewModel.formatTime(reminder.triggerTime))

                    if reminder.repeatType > 0 && reminder.repeatType < NotesViewModel.repeatLabels.count {
                        infoCard(icon: "repeat", label: "Lặp lại", value: NotesViewModel.repeatLabels[reminder.repeatType])
                    }

                    if reminder.advanceDays > 0 {
                        infoCard(icon: "bell.badge", label: "Nhắc trước", value: "\(reminder.advanceDays) ngày")
                    }

                    if reminder.useLunar {
                        infoCard(icon: "moon.fill", label: "Theo Âm lịch", value: "Có")
                    }

                    // Category chip
                    let catNames = ["🎉 Ngày lễ", "🎂 Sinh nhật", "🌙 Âm lịch", "📝 Cá nhân", "🕯 Tưởng niệm"]
                    let catName = reminder.category < catNames.count ? catNames[reminder.category] : "Cá nhân"
                    infoCard(icon: "square.grid.2x2", label: "Loại", value: catName)

                    // Toggle
                    HStack(spacing: 12) {
                        Image(systemName: "bell.fill")
                            .font(.system(size: 18))
                            .foregroundColor(RemindOrange)
                            .frame(width: 40, height: 40)
                            .background(RemindOrange.opacity(0.1))
                            .clipShape(RoundedRectangle(cornerRadius: 10))

                        Text("Trạng thái")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(TextMain)

                        Spacer()

                        Toggle("", isOn: Binding(
                            get: { reminder.isEnabled },
                            set: { _ in vm.toggleReminder(reminder) }
                        ))
                        .tint(RemindOrange)
                        .labelsHidden()
                    }
                    .padding(16)
                    .background(SurfaceC)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVar, lineWidth: 1))
                }
                .padding(20)
                .padding(.bottom, 40)
            }
        }
        .background(SurfaceBg)
        .navigationBarHidden(true)
        .fullScreenCover(isPresented: $showEdit) {
            NavigationStack {
                NoteEditScreen(existingReminder: reminder, initialType: .remind, vm: vm)
            }
        }
    }

    private func infoCard(icon: String, label: String, value: String) -> some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundColor(RemindOrange)
                .frame(width: 40, height: 40)
                .background(RemindOrange.opacity(0.1))
                .clipShape(RoundedRectangle(cornerRadius: 10))

            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(TextDim)
                Text(value)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(TextMain)
            }

            Spacer()
        }
        .padding(16)
        .background(SurfaceC)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVar, lineWidth: 1))
    }
}
