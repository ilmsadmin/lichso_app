import SwiftUI
import SwiftData

// ═══════════════════════════════════════════
// Note / Task / Reminder — Edit & Create Screen
// Matches screen-note-task-edit.html mock
// 3 type-chips to switch between Note / Task / Remind
// ═══════════════════════════════════════════

private var PrimaryRed: Color { LSTheme.primary }
private var SurfaceBg: Color { LSTheme.bg }
private var SurfaceC: Color { LSTheme.surfaceContainer }
private var SurfaceCHigh: Color { LSTheme.surfaceContainerHigh }
private var TextMain: Color { LSTheme.textPrimary }
private var TextSub: Color { LSTheme.textSecondary }
private var TextDim: Color { LSTheme.textTertiary }
private var OutlineVar: Color { LSTheme.outlineVariant }
private var NoteBlue      : Color { LSTheme.noteBlue }
private var TaskGreen: Color { LSTheme.goodGreen }
private var RemindOrange  : Color { LSTheme.remindOrange }
private var GoldAccent: Color { LSTheme.gold }

// ── Item type ──────────────────────────────
enum EditItemType: Int, CaseIterable {
    case note = 0, task = 1, remind = 2

    var label: String {
        switch self { case .note: return "Ghi chú"; case .task: return "Công việc"; case .remind: return "Nhắc nhở" }
    }
    var icon: String {
        switch self { case .note: return "note.text"; case .task: return "checklist"; case .remind: return "alarm" }
    }
    var color: Color {
        switch self { case .note: return NoteBlue; case .task: return TaskGreen; case .remind: return RemindOrange }
    }
}

// ── Note background colors ─────────────────
private let noteColors: [(name: String, color: Color)] = [
    ("Mặc định", Color(hex: "1A1814")),
    ("Vàng",     Color(hex: "FFE082")),
    ("Đỏ",      Color(hex: "EF9A9A")),
    ("Xanh",    Color(hex: "90CAF9")),
    ("Lục",     Color(hex: "A5D6A7")),
    ("Tím",     Color(hex: "CE93D8")),
    ("Cam",     Color(hex: "FFCC80")),
]

// ── Checklist item ─────────────────────────
struct ChecklistItem: Identifiable {
    let id = UUID()
    var text: String
    var isDone: Bool
}

// ════════════════════════════════════════════
// MARK: — NoteEditScreen
// ════════════════════════════════════════════

struct NoteEditScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext

    // ── Existing items (nil = create new) ──
    var existingNote: NoteEntity?
    var existingTask: TaskEntity?
    var existingReminder: ReminderEntity?

    /// Which tab the user came from (determines initial type)
    var initialType: EditItemType

    @ObservedObject var vm: NotesViewModel

    // ── State ──────────────────────────────
    @State private var itemType: EditItemType = .note
    @State private var title = ""
    @State private var bodyText = ""
    @State private var isPinned = false
    @State private var colorIndex = 0
    @State private var labels: [String] = []

    // Task specifics
    @State private var checklistItems: [ChecklistItem] = []
    @State private var dueDate = Date()
    @State private var hasDueDate = false
    @State private var priority = 1 // 1=Low, 2=Medium, 3=High
    @State private var hasTaskReminder = false

    // Reminder specifics
    @State private var reminderDate = Date()
    @State private var reminderTime = Date()
    @State private var advanceDays = 1 // 0=same day, 1, 3, 7
    @State private var repeatType = 0 // 0=none,1=daily,2=weekly,3=monthly,5=yearly
    @State private var useLunar = false
    @State private var reminderCategory = 0 // 0=holiday,1=birthday,2=lunar,3=personal
    @State private var isReminderEnabled = true

    // Attached date card
    @State private var showDateAttach = false
    @State private var attachedDate = Date()

    private var isEditing: Bool {
        existingNote != nil || existingTask != nil || existingReminder != nil
    }

    private var screenTitle: String {
        if isEditing {
            switch itemType {
            case .note: return "Sửa ghi chú"
            case .task: return "Sửa công việc"
            case .remind: return "Sửa nhắc nhở"
            }
        } else {
            switch itemType {
            case .note: return "Ghi chú mới"
            case .task: return "Công việc mới"
            case .remind: return "Nhắc nhở mới"
            }
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            // ═══ TOP BAR ═══
            topBar

            // ═══ TYPE ROW ═══
            typeChipsRow

            // ═══ DATE ATTACH CARD ═══
            if showDateAttach {
                dateAttachCard
            }

            // ═══ EDIT CONTENT ═══
            ScrollView(.vertical, showsIndicators: false) {
                VStack(alignment: .leading, spacing: 0) {
                    // Title
                    TextField("Tiêu đề...", text: $title)
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(TextMain)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 4)

                    // Labels row
                    labelsRow

                    // Type-specific panel
                    switch itemType {
                    case .note:   notePanel
                    case .task:   taskPanel
                    case .remind: remindPanel
                    }
                }
                .padding(.bottom, 40)
            }

            // ═══ BOTTOM TOOLBAR (note only) ═══
            if itemType == .note {
                noteToolbar
            }
        }
        .background(SurfaceBg)
        .navigationBarHidden(true)
        .onAppear(perform: loadExistingData)
    }

    // ──────────────────────────────────────
    // MARK: — Top Bar
    // ──────────────────────────────────────
    private var topBar: some View {
        HStack(spacing: 2) {
            Button { dismiss() } label: {
                Image(systemName: "chevron.left")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(TextMain)
                    .frame(width: 40, height: 40)
            }

            Text(screenTitle)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(TextMain)

            Spacer()

            // Undo / Redo (placeholder)
            Button {} label: {
                Image(systemName: "arrow.uturn.backward")
                    .font(.system(size: 16))
                    .foregroundColor(TextSub)
                    .frame(width: 38, height: 38)
            }
            Button {} label: {
                Image(systemName: "arrow.uturn.forward")
                    .font(.system(size: 16))
                    .foregroundColor(TextSub)
                    .frame(width: 38, height: 38)
            }

            // Pin button (note only)
            if itemType == .note {
                Button { isPinned.toggle() } label: {
                    Image(systemName: isPinned ? "pin.fill" : "pin")
                        .font(.system(size: 16))
                        .foregroundColor(isPinned ? PrimaryRed : TextSub)
                        .frame(width: 38, height: 38)
                }
            }

            // Save button
            Button(action: save) {
                Text("Lưu")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 18)
                    .padding(.vertical, 7)
                    .background(PrimaryRed)
                    .clipShape(Capsule())
            }
            .disabled(title.trimmingCharacters(in: .whitespaces).isEmpty)
            .opacity(title.trimmingCharacters(in: .whitespaces).isEmpty ? 0.5 : 1)
        }
        .padding(.horizontal, 16)
        .padding(.top, 8)
        .padding(.bottom, 8)
    }

    // ──────────────────────────────────────
    // MARK: — Type Chips Row
    // ──────────────────────────────────────
    private var typeChipsRow: some View {
        HStack(spacing: 6) {
            ForEach(EditItemType.allCases, id: \.rawValue) { type in
                Button {
                    withAnimation(.easeInOut(duration: 0.2)) { itemType = type }
                } label: {
                    HStack(spacing: 4) {
                        Image(systemName: type.icon)
                            .font(.system(size: 13))
                        Text(type.label)
                            .font(.system(size: 12, weight: .semibold))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                    .background(itemType == type ? type.color : Color.clear)
                    .foregroundColor(itemType == type ? .white : TextSub)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(itemType == type ? type.color : OutlineVar, lineWidth: 1.5)
                    )
                }
            }
        }
        .padding(.horizontal, 20)
        .padding(.bottom, 8)
    }

    // ──────────────────────────────────────
    // MARK: — Date Attach Card
    // ──────────────────────────────────────
    private var dateAttachCard: some View {
        HStack(spacing: 12) {
            // Calendar icon
            VStack(spacing: 0) {
                Text(dayString(attachedDate))
                    .font(.system(size: 17, weight: .bold))
                    .foregroundColor(.white)
                Text(monthShort(attachedDate))
                    .font(.system(size: 8, weight: .semibold))
                    .foregroundColor(.white.opacity(0.8))
                    .textCase(.uppercase)
            }
            .frame(width: 42, height: 42)
            .background(
                LinearGradient(colors: [PrimaryRed, Color(hex: "C62828")], startPoint: .topLeading, endPoint: .bottomTrailing)
            )
            .clipShape(RoundedRectangle(cornerRadius: 12))

            VStack(alignment: .leading, spacing: 1) {
                Text(fullDateString(attachedDate))
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(TextMain)
                Text(lunarDateHint(attachedDate))
                    .font(.system(size: 11))
                    .foregroundColor(TextSub)
            }

            Spacer()

            Button { showDateAttach = false } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 12))
                    .foregroundColor(TextDim)
                    .frame(width: 28, height: 28)
                    .background(Color.black.opacity(0.05))
                    .clipShape(Circle())
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .background(
            LinearGradient(colors: [Color(hex: "1A1814"), SurfaceCHigh], startPoint: .topLeading, endPoint: .bottomTrailing)
        )
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(OutlineVar, lineWidth: 1))
        .padding(.horizontal, 20)
        .padding(.bottom, 8)
    }

    // ──────────────────────────────────────
    // MARK: — Labels Row
    // ──────────────────────────────────────
    private var labelsRow: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 6) {
                ForEach(labels, id: \.self) { label in
                    HStack(spacing: 4) {
                        Image(systemName: labelIcon(label))
                            .font(.system(size: 11))
                        Text(label)
                            .font(.system(size: 11, weight: .semibold))
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(labelBg(label))
                    .foregroundColor(labelFg(label))
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(labelFg(label).opacity(0.2), lineWidth: 1))
                    .onTapGesture {
                        labels.removeAll { $0 == label }
                    }
                }

                // Add label button
                Button {
                    addDefaultLabel()
                } label: {
                    HStack(spacing: 4) {
                        Image(systemName: "plus")
                            .font(.system(size: 11))
                        Text("Thêm nhãn")
                            .font(.system(size: 11, weight: .semibold))
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .foregroundColor(TextSub)
                    .overlay(
                        RoundedRectangle(cornerRadius: 10)
                            .strokeBorder(style: StrokeStyle(lineWidth: 1, dash: [4]))
                            .foregroundColor(OutlineVar)
                    )
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 12)
        }
    }

    // ════════════════════════════════════════
    // MARK: — NOTE PANEL
    // ════════════════════════════════════════
    private var notePanel: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Body text
            TextEditor(text: $bodyText)
                .font(.system(size: 15))
                .foregroundColor(TextMain)
                .scrollContentBackground(.hidden)
                .frame(minHeight: 120)
                .padding(.horizontal, 16)

            // Inline checklist
            if !checklistItems.isEmpty {
                VStack(spacing: 0) {
                    ForEach($checklistItems) { $item in
                        HStack(alignment: .top, spacing: 10) {
                            Button {
                                item.isDone.toggle()
                            } label: {
                                ZStack {
                                    RoundedRectangle(cornerRadius: 7)
                                        .fill(item.isDone ? TaskGreen : Color.clear)
                                        .frame(width: 22, height: 22)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 7)
                                                .stroke(TaskGreen, lineWidth: 2)
                                        )
                                    if item.isDone {
                                        Image(systemName: "checkmark")
                                            .font(.system(size: 12, weight: .bold))
                                            .foregroundColor(.white)
                                    }
                                }
                            }
                            .padding(.top, 1)

                            TextField("Mục mới...", text: $item.text)
                                .font(.system(size: 14))
                                .foregroundColor(item.isDone ? TextDim : TextMain)
                                .strikethrough(item.isDone)
                        }
                        .padding(.vertical, 7)
                    }
                }
                .padding(.horizontal, 20)
            }

            // Add checklist item
            Button {
                checklistItems.append(ChecklistItem(text: "", isDone: false))
            } label: {
                HStack(spacing: 8) {
                    Image(systemName: "plus.circle.fill")
                        .font(.system(size: 16))
                        .foregroundColor(TaskGreen)
                    Text("Thêm mục")
                        .font(.system(size: 13))
                        .foregroundColor(TextDim)
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 6)
        }
    }

    // ════════════════════════════════════════
    // MARK: — TASK PANEL
    // ════════════════════════════════════════
    private var taskPanel: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Checklist
            sectionTitle(icon: "checklist", text: "Các bước thực hiện")
                .padding(.horizontal, 20)

            VStack(spacing: 0) {
                ForEach($checklistItems) { $item in
                    HStack(alignment: .top, spacing: 10) {
                        Button { item.isDone.toggle() } label: {
                            ZStack {
                                RoundedRectangle(cornerRadius: 7)
                                    .fill(item.isDone ? TaskGreen : Color.clear)
                                    .frame(width: 22, height: 22)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 7)
                                            .stroke(TaskGreen, lineWidth: 2)
                                    )
                                if item.isDone {
                                    Image(systemName: "checkmark")
                                        .font(.system(size: 12, weight: .bold))
                                        .foregroundColor(.white)
                                }
                            }
                        }
                        .padding(.top, 1)

                        TextField("Bước mới...", text: $item.text)
                            .font(.system(size: 14))
                            .foregroundColor(item.isDone ? TextDim : TextMain)
                            .strikethrough(item.isDone)
                    }
                    .padding(.vertical, 7)
                }
            }
            .padding(.horizontal, 20)

            Button {
                checklistItems.append(ChecklistItem(text: "", isDone: false))
            } label: {
                HStack(spacing: 8) {
                    Image(systemName: "plus.circle.fill")
                        .font(.system(size: 16))
                        .foregroundColor(TaskGreen)
                    Text("Thêm bước")
                        .font(.system(size: 13))
                        .foregroundColor(TextDim)
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 6)

            // Task detail section
            sectionTitle(icon: "slider.horizontal.3", text: "Chi tiết công việc")
                .padding(.horizontal, 20)

            VStack(spacing: 0) {
                // Due date
                formRow(icon: "calendar", label: "Hạn chót") {
                    VStack(alignment: .leading, spacing: 4) {
                        Toggle("", isOn: $hasDueDate).labelsHidden().tint(RemindOrange)
                        if hasDueDate {
                            DatePicker("", selection: $dueDate, displayedComponents: [.date, .hourAndMinute])
                                .labelsHidden()
                                .environment(\.locale, Locale(identifier: "vi_VN"))
                        }
                    }
                }

                // Priority
                formRow(icon: "flag.fill", label: "Ưu tiên") {
                    EmptyView()
                } below: {
                    HStack(spacing: 6) {
                        priorityChip("Cao", value: 3, isHigh: true)
                        priorityChip("Trung bình", value: 2, isHigh: false)
                        priorityChip("Thấp", value: 1, isHigh: false)
                    }
                }

                // Reminder toggle
                formRow(icon: "alarm", label: "Nhắc nhở") {
                    Toggle("", isOn: $hasTaskReminder)
                        .labelsHidden()
                        .tint(RemindOrange)
                }
            }
            .padding(.horizontal, 20)
        }
    }

    // ════════════════════════════════════════
    // MARK: — REMIND PANEL
    // ════════════════════════════════════════
    private var remindPanel: some View {
        VStack(alignment: .leading, spacing: 0) {
            sectionTitle(icon: "alarm", text: "Cài đặt nhắc nhở")
                .padding(.horizontal, 20)

            VStack(spacing: 0) {
                // Date
                formRow(icon: "calendar", label: "Ngày nhắc") {
                    DatePicker("", selection: $reminderDate, displayedComponents: .date)
                        .labelsHidden()
                        .environment(\.locale, Locale(identifier: "vi_VN"))
                }

                // Time
                formRow(icon: "clock", label: "Giờ nhắc") {
                    DatePicker("", selection: $reminderTime, displayedComponents: .hourAndMinute)
                        .labelsHidden()
                        .environment(\.locale, Locale(identifier: "vi_VN"))
                }

                // Advance days
                formRow(icon: "bell.badge", label: "Nhắc trước") {
                    EmptyView()
                } below: {
                    HStack(spacing: 6) {
                        advanceChip("Đúng ngày", value: 0)
                        advanceChip("1 ngày", value: 1)
                        advanceChip("3 ngày", value: 3)
                        advanceChip("1 tuần", value: 7)
                    }
                }

                // Repeat
                formRow(icon: "repeat", label: "Lặp lại") {
                    EmptyView()
                } below: {
                    HStack(spacing: 6) {
                        repeatChip("Không", value: 0)
                        repeatChip("Hàng năm", value: 5)
                        repeatChip("Hàng tháng", value: 3)
                        repeatChip("Hàng tuần", value: 2)
                    }
                }

                // Lunar toggle
                formRow(icon: "moon.fill", label: "Theo Âm lịch") {
                    Toggle("", isOn: $useLunar)
                        .labelsHidden()
                        .tint(RemindOrange)
                } below: {
                    Text("Nhắc theo ngày Âm lịch thay vì Dương lịch")
                        .font(.system(size: 11))
                        .foregroundColor(TextSub)
                }

                // Category
                formRow(icon: "square.grid.2x2", label: "Loại nhắc nhở") {
                    EmptyView()
                } below: {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 6) {
                            categoryChip("🎉 Ngày lễ", value: 0)
                            categoryChip("🎂 Sinh nhật", value: 1)
                            categoryChip("🌙 Âm lịch", value: 2)
                            categoryChip("📝 Cá nhân", value: 3)
                        }
                    }
                }
            }
            .padding(.horizontal, 20)
        }
    }

    // ════════════════════════════════════════
    // MARK: — NOTE TOOLBAR (bottom)
    // ════════════════════════════════════════
    private var noteToolbar: some View {
        VStack(spacing: 0) {
            Divider()

            // Color row
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 4) {
                    Text("Màu nền")
                        .font(.system(size: 10, weight: .semibold))
                        .foregroundColor(TextDim)
                        .padding(.trailing, 6)

                    ForEach(0..<noteColors.count, id: \.self) { i in
                        Button {
                            colorIndex = i
                        } label: {
                            Circle()
                                .fill(noteColors[i].color)
                                .frame(width: 24, height: 24)
                                .overlay(
                                    Circle()
                                        .stroke(colorIndex == i ? TextMain : (i == 0 ? OutlineVar : Color.clear), lineWidth: colorIndex == i ? 2.5 : 1)
                                )
                                .scaleEffect(colorIndex == i ? 1.1 : 1.0)
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 10)
            }

            Divider()

            // Tool row
            HStack(spacing: 2) {
                toolBtn("checkmark.square", isActive: !checklistItems.isEmpty) {
                    if checklistItems.isEmpty {
                        checklistItems.append(ChecklistItem(text: "", isDone: false))
                    }
                }
                toolBtn("photo", isActive: false) {}
                toolBtn("calendar", isActive: showDateAttach) {
                    showDateAttach.toggle()
                    attachedDate = Date()
                }
                toolBtn("bold", isActive: false) {}
                toolBtn("list.bullet", isActive: false) {}

                Spacer()

                VStack(alignment: .trailing, spacing: 2) {
                    Text(isEditing ? "Sửa lúc \(timeNowString())" : "Mới tạo")
                        .font(.system(size: 10))
                        .foregroundColor(TextDim)
                    Text("\(wordCount()) từ")
                        .font(.system(size: 10))
                        .foregroundColor(TextDim)
                }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 6)
            .padding(.bottom, 20)
        }
        .background(SurfaceBg)
    }

    // ════════════════════════════════════════
    // MARK: — Helper Components
    // ════════════════════════════════════════

    private func sectionTitle(icon: String, text: String) -> some View {
        HStack(spacing: 5) {
            Image(systemName: icon)
                .font(.system(size: 13))
                .foregroundColor(PrimaryRed)
            Text(text)
                .font(.system(size: 12, weight: .bold))
                .foregroundColor(TextSub)
        }
        .padding(.top, 12)
        .padding(.bottom, 8)
    }

    private func formRow<Trailing: View>(icon: String, label: String, @ViewBuilder trailing: () -> Trailing) -> some View {
        formRow(icon: icon, label: label, trailing: trailing, below: { EmptyView() })
    }

    private func formRow<Trailing: View, Below: View>(
        icon: String, label: String,
        @ViewBuilder trailing: () -> Trailing,
        @ViewBuilder below: () -> Below
    ) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.system(size: 16))
                    .foregroundColor(PrimaryRed)
                    .frame(width: 36, height: 36)
                    .background(SurfaceC)
                    .clipShape(RoundedRectangle(cornerRadius: 10))

                VStack(alignment: .leading, spacing: 0) {
                    Text(label)
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(TextSub)
                }

                Spacer()

                trailing()
            }

            below()
                .padding(.leading, 48)
        }
        .padding(.vertical, 12)
        .overlay(alignment: .bottom) {
            Divider().padding(.leading, 48)
        }
    }

    private func priorityChip(_ text: String, value: Int, isHigh: Bool) -> some View {
        Button {
            priority = value
        } label: {
            Text(text)
                .font(.system(size: 11, weight: .semibold))
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(
                    priority == value
                        ? (isHigh ? Color(hex: "C62828").opacity(0.08) : PrimaryRed.opacity(0.06))
                        : Color.clear
                )
                .foregroundColor(
                    priority == value
                        ? (isHigh ? Color(hex: "C62828") : PrimaryRed)
                        : TextSub
                )
                .clipShape(RoundedRectangle(cornerRadius: 8))
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(priority == value ? (isHigh ? Color(hex: "C62828") : PrimaryRed) : OutlineVar, lineWidth: 1)
                )
        }
    }

    private func advanceChip(_ text: String, value: Int) -> some View {
        Button { advanceDays = value } label: {
            Text(text)
                .font(.system(size: 11, weight: advanceDays == value ? .semibold : .regular))
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(advanceDays == value ? RemindOrange.opacity(0.08) : Color.clear)
                .foregroundColor(advanceDays == value ? RemindOrange : TextSub)
                .clipShape(RoundedRectangle(cornerRadius: 8))
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(advanceDays == value ? RemindOrange : OutlineVar, lineWidth: 1)
                )
        }
    }

    private func repeatChip(_ text: String, value: Int) -> some View {
        Button { repeatType = value } label: {
            Text(text)
                .font(.system(size: 11, weight: repeatType == value ? .semibold : .regular))
                .padding(.horizontal, 11)
                .padding(.vertical, 5)
                .background(repeatType == value ? RemindOrange.opacity(0.08) : Color.clear)
                .foregroundColor(repeatType == value ? RemindOrange : TextSub)
                .clipShape(RoundedRectangle(cornerRadius: 10))
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(repeatType == value ? RemindOrange : OutlineVar, lineWidth: 1)
                )
        }
    }

    private func categoryChip(_ text: String, value: Int) -> some View {
        Button { reminderCategory = value } label: {
            Text(text)
                .font(.system(size: 11, weight: reminderCategory == value ? .semibold : .regular))
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(reminderCategory == value ? PrimaryRed.opacity(0.06) : Color.clear)
                .foregroundColor(reminderCategory == value ? PrimaryRed : TextSub)
                .clipShape(RoundedRectangle(cornerRadius: 8))
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(reminderCategory == value ? PrimaryRed : OutlineVar, lineWidth: 1)
                )
        }
    }

    private func toolBtn(_ icon: String, isActive: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: icon)
                .font(.system(size: 18))
                .foregroundColor(isActive ? PrimaryRed : TextSub)
                .frame(width: 40, height: 40)
                .background(isActive ? Color(hex: "5D1212") : Color.clear)
                .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    // ════════════════════════════════════════
    // MARK: — Data Helpers
    // ════════════════════════════════════════

    private func loadExistingData() {
        itemType = initialType

        if let note = existingNote {
            title = note.title
            bodyText = note.content
            isPinned = note.isPinned
            colorIndex = note.colorIndex
            if !note.labels.isEmpty {
                labels = note.labels.components(separatedBy: ",").map { $0.trimmingCharacters(in: .whitespaces) }
            }
        }
        if let task = existingTask {
            title = task.title
            bodyText = task.taskDescription
            priority = task.priority
            if let due = task.dueDate {
                hasDueDate = true
                dueDate = Date(timeIntervalSince1970: Double(due) / 1000)
            }
            hasTaskReminder = task.hasReminder
            if !task.labels.isEmpty {
                labels = task.labels.components(separatedBy: ",").map { $0.trimmingCharacters(in: .whitespaces) }
            }
            // Parse checklist from description
            parseChecklistFromDescription(task.taskDescription)
        }
        if let rem = existingReminder {
            title = rem.title
            bodyText = rem.subtitle
            repeatType = rem.repeatType
            useLunar = rem.useLunar
            advanceDays = rem.advanceDays
            reminderCategory = rem.category
            isReminderEnabled = rem.isEnabled
            let trigDate = Date(timeIntervalSince1970: Double(rem.triggerTime) / 1000)
            reminderDate = trigDate
            reminderTime = trigDate
            if !rem.labels.isEmpty {
                labels = rem.labels.components(separatedBy: ",").map { $0.trimmingCharacters(in: .whitespaces) }
            }
        }
    }

    private func parseChecklistFromDescription(_ desc: String) {
        let lines = desc.components(separatedBy: "\n")
        for line in lines {
            let trimmed = line.trimmingCharacters(in: .whitespaces)
            if trimmed.hasPrefix("[x] ") || trimmed.hasPrefix("[X] ") {
                checklistItems.append(ChecklistItem(text: String(trimmed.dropFirst(4)), isDone: true))
            } else if trimmed.hasPrefix("[ ] ") {
                checklistItems.append(ChecklistItem(text: String(trimmed.dropFirst(4)), isDone: false))
            }
        }
    }

    private func buildChecklistString() -> String {
        checklistItems.map { item in
            "\(item.isDone ? "[x]" : "[ ]") \(item.text)"
        }.joined(separator: "\n")
    }

    private func save() {
        let t = title.trimmingCharacters(in: .whitespaces)
        guard !t.isEmpty else { return }

        switch itemType {
        case .note:
            var fullContent = bodyText
            if !checklistItems.isEmpty {
                if !fullContent.isEmpty { fullContent += "\n" }
                fullContent += buildChecklistString()
            }
            if let note = existingNote {
                vm.updateNote(note, title: t, content: fullContent, colorIndex: colorIndex, isPinned: isPinned, labels: labels.joined(separator: ","))
            } else {
                vm.createNote(title: t, content: fullContent, colorIndex: colorIndex)
            }

        case .task:
            let desc = buildChecklistString()
            if let task = existingTask {
                vm.updateTask(task, title: t, description: desc, priority: priority, dueDate: hasDueDate ? dueDate : nil, hasReminder: hasTaskReminder, labels: labels.joined(separator: ","))
            } else {
                vm.createTask(title: t, description: desc, priority: priority, dueDate: hasDueDate ? dueDate : nil)
            }

        case .remind:
            // Combine date + time
            let cal = Calendar.current
            var comps = cal.dateComponents([.year, .month, .day], from: reminderDate)
            let timeComps = cal.dateComponents([.hour, .minute], from: reminderTime)
            comps.hour = timeComps.hour
            comps.minute = timeComps.minute
            let triggerDate = cal.date(from: comps) ?? reminderDate

            if let rem = existingReminder {
                vm.updateReminder(rem, title: t, subtitle: bodyText, triggerTime: triggerDate, repeatType: repeatType, useLunar: useLunar, advanceDays: advanceDays, category: reminderCategory, isEnabled: isReminderEnabled, labels: labels.joined(separator: ","))
            } else {
                vm.createReminder(title: t, subtitle: bodyText, triggerTime: triggerDate, repeatType: repeatType, category: reminderCategory)
            }
        }

        dismiss()
    }

    // ── Label helpers ──
    private func labelIcon(_ label: String) -> String {
        switch label.lowercased() {
        case "gia đình": return "person.2"
        case "quan trọng": return "exclamationmark.triangle"
        case "công việc": return "briefcase"
        default: return "tag"
        }
    }

    private func labelBg(_ label: String) -> Color {
        switch label.lowercased() {
        case "gia đình": return Color(hex: "7B1FA2").opacity(0.08)
        case "quan trọng": return PrimaryRed.opacity(0.08)
        default: return NoteBlue.opacity(0.08)
        }
    }

    private func labelFg(_ label: String) -> Color {
        switch label.lowercased() {
        case "gia đình": return Color(hex: "7B1FA2")
        case "quan trọng": return PrimaryRed
        default: return NoteBlue
        }
    }

    private func addDefaultLabel() {
        let available = ["Gia đình", "Quan trọng", "Công việc", "Cá nhân", "Sức khỏe"]
        for l in available {
            if !labels.contains(l) {
                labels.append(l)
                return
            }
        }
    }

    // ── Date helpers ──
    private func dayString(_ date: Date) -> String {
        let f = DateFormatter(); f.dateFormat = "d"; return f.string(from: date)
    }
    private func monthShort(_ date: Date) -> String {
        let f = DateFormatter(); f.locale = Locale(identifier: "vi_VN"); f.dateFormat = "MMM"; return f.string(from: date)
    }
    private func fullDateString(_ date: Date) -> String {
        let f = DateFormatter(); f.locale = Locale(identifier: "vi_VN"); f.dateFormat = "EEEE, dd/MM/yyyy"; return f.string(from: date)
    }
    private func lunarDateHint(_ date: Date) -> String {
        // Placeholder — integrate with LunarCalendarUtil for real lunar date
        return "Ngày Âm lịch"
    }
    private func timeNowString() -> String {
        let f = DateFormatter(); f.dateFormat = "H:mm"; return f.string(from: Date())
    }
    private func wordCount() -> Int {
        let text = title + " " + bodyText
        return text.split(separator: " ").count
    }
}
