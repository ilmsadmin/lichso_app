import SwiftUI

// ═══════════════════════════════════════════
// ItemEditScreen — trình soạn thảo hợp nhất cho 1 ItemEntity.
// Port từ Android `ItemEditScreen.kt`: title + mô tả + tags, kèm năng lực bật/tắt
// "Việc cần làm" (priority, hạn) và "Nhắc nhở" (ngày giờ, lặp, âm lịch, nhắc trước).
// ═══════════════════════════════════════════

private let repeatLabels = ["Một lần", "Hàng ngày", "Hàng tuần", "Hàng tháng", "Hàng tháng (Âm)", "Hàng năm"]
private let advanceOptions: [(Int, String)] = [(0, "Đúng ngày"), (1, "Trước 1 ngày"), (3, "Trước 3 ngày"), (7, "Trước 7 ngày")]

struct ItemEditScreen: View {
    let initialItem: ItemEntity?
    let onSave: (ItemDraft, ItemEntity?) -> Void
    let onCancel: () -> Void
    let onDelete: (() -> Void)?

    @State private var title: String
    @State private var description: String
    @State private var tags: String
    @State private var isPinned: Bool
    @State private var colorIndex: Int

    @State private var isTask: Bool
    @State private var priority: Int
    @State private var hasDue: Bool
    @State private var dueDate: Date

    @State private var hasReminder: Bool
    @State private var reminderDate: Date
    @State private var repeatType: Int
    @State private var useLunar: Bool
    @State private var advanceDays: Int

    init(
        initialItem: ItemEntity?,
        onSave: @escaping (ItemDraft, ItemEntity?) -> Void,
        onCancel: @escaping () -> Void,
        onDelete: (() -> Void)?
    ) {
        self.initialItem = initialItem
        self.onSave = onSave
        self.onCancel = onCancel
        self.onDelete = onDelete
        _title = State(initialValue: initialItem?.title ?? "")
        _description = State(initialValue: initialItem?.itemDescription ?? "")
        _tags = State(initialValue: initialItem?.tags ?? "")
        _isPinned = State(initialValue: initialItem?.isPinned ?? false)
        _colorIndex = State(initialValue: initialItem?.colorIndex ?? 0)
        _isTask = State(initialValue: initialItem?.isTask ?? false)
        _priority = State(initialValue: initialItem?.priority ?? 1)
        let due = initialItem?.dueDate.map { Date(timeIntervalSince1970: Double($0) / 1000) }
        _hasDue = State(initialValue: due != nil)
        _dueDate = State(initialValue: due ?? Date())
        _hasReminder = State(initialValue: initialItem?.hasReminder ?? false)
        let rem = initialItem?.reminderAt.map { Date(timeIntervalSince1970: Double($0) / 1000) }
        _reminderDate = State(initialValue: rem ?? Date())
        _repeatType = State(initialValue: initialItem?.repeatType ?? 0)
        _useLunar = State(initialValue: initialItem?.useLunar ?? false)
        _advanceDays = State(initialValue: initialItem?.advanceDays ?? 0)
    }

    private var isExisting: Bool { (initialItem?.id ?? 0) != 0 }
    private var canSave: Bool {
        !title.trimmingCharacters(in: .whitespaces).isEmpty ||
        !description.trimmingCharacters(in: .whitespaces).isEmpty
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                field("Tiêu đề", text: $title)
                field("Mô tả", text: $description, multiline: true)
                field("Tags (phân cách bằng dấu phẩy)", text: $tags)

                // Màu & ghim
                HStack(spacing: 8) {
                    ForEach(0..<itemAccentColors.count, id: \.self) { idx in
                        Circle()
                            .fill(itemAccentColors[idx].opacity(idx == 0 ? 0.4 : 1))
                            .frame(width: 28, height: 28)
                            .overlay(Circle().stroke(LSTheme.textPrimary, lineWidth: colorIndex == idx ? 3 : 0))
                            .onTapGesture { colorIndex = idx }
                    }
                    Spacer()
                    Button { isPinned.toggle() } label: {
                        Image(systemName: isPinned ? "pin.fill" : "pin")
                            .font(.system(size: 18))
                            .foregroundColor(isPinned ? LSTheme.primary : LSTheme.textTertiary)
                    }
                }

                // ── Việc cần làm ──
                sectionToggle("Việc cần làm", icon: "checkmark.circle", isOn: $isTask)
                if isTask {
                    VStack(alignment: .leading, spacing: 10) {
                        label("Mức ưu tiên")
                        HStack(spacing: 8) {
                            ForEach([(0, "Thấp"), (1, "Vừa"), (2, "Cao")], id: \.0) { p, lbl in
                                ChoiceChip(label: lbl, selected: priority == p) { priority = p }
                            }
                        }
                        Toggle("Đặt hạn hoàn thành", isOn: $hasDue)
                            .font(.system(size: 14)).tint(LSTheme.primary)
                        if hasDue {
                            DatePicker("Hạn", selection: $dueDate, displayedComponents: .date)
                                .datePickerStyle(.compact)
                                .environment(\.locale, Locale(identifier: "vi_VN"))
                        }
                    }
                }

                // ── Nhắc nhở ──
                sectionToggle("Nhắc nhở", icon: "bell", isOn: $hasReminder)
                if hasReminder {
                    VStack(alignment: .leading, spacing: 10) {
                        DatePicker("Ngày & giờ nhắc", selection: $reminderDate, displayedComponents: [.date, .hourAndMinute])
                            .datePickerStyle(.compact)
                            .environment(\.locale, Locale(identifier: "vi_VN"))
                        label("Lặp lại")
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                ForEach(0..<repeatLabels.count, id: \.self) { idx in
                                    ChoiceChip(label: repeatLabels[idx], selected: repeatType == idx) {
                                        repeatType = idx
                                        if idx == 4 { useLunar = true }
                                    }
                                }
                            }
                        }
                        Toggle("Theo Âm lịch", isOn: $useLunar)
                            .font(.system(size: 14)).tint(LSTheme.primary)
                        label("Nhắc trước")
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                ForEach(advanceOptions, id: \.0) { days, lbl in
                                    ChoiceChip(label: lbl, selected: advanceDays == days) { advanceDays = days }
                                }
                            }
                        }
                    }
                }

                Button(action: saveAction) {
                    HStack {
                        Image(systemName: "checkmark")
                        Text("Lưu")
                    }
                    .font(.system(size: 16, weight: .semibold)).foregroundColor(.white)
                    .frame(maxWidth: .infinity).padding(.vertical, 14)
                    .background(canSave ? LSTheme.primary : LSTheme.outlineVariant)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                }
                .disabled(!canSave)
                .padding(.top, 4)
            }
            .padding(16)
        }
        .background(LSTheme.bg)
        .navigationTitle(isExisting ? "Chỉnh sửa" : "Tạo mục mới")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button("Huỷ", action: onCancel)
            }
            if let onDelete, isExisting {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(role: .destructive, action: onDelete) {
                        Image(systemName: "trash").foregroundColor(LSTheme.badRed)
                    }
                }
            }
        }
    }

    private func saveAction() {
        guard canSave else { return }
        let draft = ItemDraft(
            title: title, description: description, tags: tags,
            isTask: isTask, priority: priority,
            dueDate: (isTask && hasDue) ? dueDate : nil,
            hasReminder: hasReminder, reminderDate: reminderDate,
            repeatType: repeatType, useLunar: useLunar, advanceDays: advanceDays,
            isPinned: isPinned, colorIndex: colorIndex
        )
        onSave(draft, isExisting ? initialItem : nil)
    }

    // ── Subviews ──
    private func field(_ placeholder: String, text: Binding<String>, multiline: Bool = false) -> some View {
        Group {
            if multiline {
                TextField(placeholder, text: text, axis: .vertical)
                    .lineLimit(2...6)
            } else {
                TextField(placeholder, text: text)
            }
        }
        .font(.system(size: 15))
        .padding(12)
        .background(LSTheme.surfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(LSTheme.outlineVariant, lineWidth: 1))
    }

    private func label(_ text: String) -> some View {
        Text(text).font(.system(size: 13, weight: .semibold)).foregroundColor(LSTheme.textSecondary)
    }

    private func sectionToggle(_ title: String, icon: String, isOn: Binding<Bool>) -> some View {
        HStack(spacing: 10) {
            Image(systemName: icon).font(.system(size: 18)).foregroundColor(LSTheme.primary)
            Text(title).font(.system(size: 15, weight: .semibold)).foregroundColor(LSTheme.textPrimary)
            Spacer()
            Toggle("", isOn: isOn).labelsHidden().tint(LSTheme.primary)
        }
        .padding(.horizontal, 12).padding(.vertical, 8)
        .background(LSTheme.surfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

private struct ChoiceChip: View {
    let label: String
    let selected: Bool
    let onTap: () -> Void
    var body: some View {
        Button(action: onTap) {
            Text(label)
                .font(.system(size: 13, weight: selected ? .semibold : .medium))
                .foregroundColor(selected ? LSTheme.primary : LSTheme.textSecondary)
                .padding(.horizontal, 14).padding(.vertical, 8)
                .background(selected ? LSTheme.primaryContainer : LSTheme.surfaceContainerHigh)
                .clipShape(Capsule())
                .overlay(Capsule().stroke(selected ? LSTheme.primary : LSTheme.outlineVariant, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}
