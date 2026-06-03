import SwiftUI
import SwiftData

// ═══════════════════════════════════════════
// ItemsScreen — màn "Sổ tay" hợp nhất (Ghi chú + Việc cần làm + Nhắc nhở).
// Port từ Android `ItemsScreen.kt`: chip lọc + danh sách item + thanh lệnh AI ở đáy.
// ═══════════════════════════════════════════

/// Bảng màu cho item (index 0..5) — dùng chung list/editor.
let itemAccentColors: [Color] = [
    LSTheme.textTertiary, LSTheme.gold, LSTheme.teal,
    LSTheme.remindOrange, Color(hex: "7B1FA2"), LSTheme.goodGreen
]

struct ItemsScreen: View {
    var onMenuClick: () -> Void = {}

    @Environment(\.modelContext) private var modelContext
    @StateObject private var vm = ItemsViewModel()
    @StateObject private var keyboard = KeyboardObserver()
    @State private var editing = false
    @State private var editingItem: ItemEntity?

    // MainTabView bọc nội dung trong GeometryReader → tự-né-bàn-phím của SwiftUI bị vô
    // hiệu. Phải nâng thanh AI thủ công lên trên bàn phím (giống Android xử lý IME).
    private var safeAreaBottom: CGFloat {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first { $0.isKeyWindow }?
            .safeAreaInsets.bottom ?? 0
    }
    /// Khoảng cần nâng thanh AI = chiều cao bàn phím − phần đã chừa cho thanh tab dưới
    /// (+ một khoảng hở nhỏ để thanh nhập không dính sát mép bàn phím).
    private var keyboardLift: CGFloat {
        guard keyboard.height > 0 else { return 0 }
        let tabBarReserve: CGFloat = 60 + safeAreaBottom   // khớp contentBottom ở MainTabView
        let gapAboveKeyboard: CGFloat = 10
        return max(0, keyboard.height - tabBarReserve + gapAboveKeyboard)
    }
    /// Khoảng đệm đáy tối thiểu để thanh AI vượt khỏi thanh tab nâng cao (nút "Hôm nay"
    /// có vòng tròn 56px khiến thanh tab cao hơn phần MainTabView chừa sẵn ~28px).
    private let barBottomGap: CGFloat = 28

    var body: some View {
        VStack(spacing: 0) {
            topBar

            // Tìm kiếm
            searchField
                .padding(.horizontal, 14)
                .padding(.vertical, 8)

            // Chip lọc
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(ItemFilter.allCases, id: \.self) { f in
                        FilterChip(label: f.label, selected: vm.filter == f) { vm.filter = f }
                    }
                }
                .padding(.horizontal, 14)
            }

            // Thông báo AI
            if let msg = vm.aiMessage {
                AiCard(text: msg, isError: false) { vm.dismissAiMessage() }
            }
            if let err = vm.aiError {
                AiCard(text: err, isError: true) { vm.dismissAiMessage() }
            }

            // Danh sách
            if vm.visibleItems.isEmpty {
                emptyState
                    .dismissKeyboardOnTapAndDrag()   // chạm/vuốt vùng trống → ẩn bàn phím
            } else {
                ScrollView(.vertical, showsIndicators: false) {
                    LazyVStack(spacing: 10) {
                        ForEach(vm.visibleItems, id: \.id) { item in
                            ItemRow(
                                item: item, vm: vm,
                                onTap: { editingItem = item; editing = true },
                                onToggleDone: { vm.toggleDone(item) }
                            )
                            .contextMenu {
                                Button { vm.togglePin(item) } label: {
                                    Label(item.isPinned ? "Bỏ ghim" : "Ghim", systemImage: item.isPinned ? "pin.slash" : "pin")
                                }
                                Button(role: .destructive) { vm.requestDelete(item) } label: {
                                    Label("Xoá", systemImage: "trash")
                                }
                            }
                        }
                    }
                    .padding(.horizontal, 14)
                    .padding(.top, 8)
                    .padding(.bottom, 16)
                }
                .scrollDismissesKeyboard(.interactively)   // vuốt danh sách → ẩn
                .dismissKeyboardOnTap()                     // chạm vùng trống/mục → ẩn
            }

            // Thanh lệnh AI — luôn cách thanh tab một khoảng; khi bàn phím hiện thì nâng
            // lên trên bàn phím.
            SharedAiCommandBar(
                placeholder: "Hỏi AI tạo nhanh...",
                isProcessing: vm.isAiProcessing
            ) { text in
                vm.processAiCommand(text)
            }
                .padding(.bottom, max(barBottomGap, keyboardLift))
                .animation(.easeOut(duration: 0.25), value: keyboardLift)
        }
        .background(LSTheme.bg)
        .onAppear { vm.setModelContext(modelContext) }
        .fullScreenCover(isPresented: $editing) {
            NavigationStack {
                ItemEditScreen(
                    initialItem: editingItem,
                    onSave: { draft, existing in vm.save(draft, existing: existing); editing = false },
                    onCancel: { editing = false },
                    onDelete: editingItem.map { item in { vm.requestDelete(item); editing = false } }
                )
            }
        }
        .alert("Xoá mục này?", isPresented: Binding(
            get: { vm.deletingItem != nil },
            set: { if !$0 { vm.cancelDelete() } }
        )) {
            Button("Xoá", role: .destructive) { vm.confirmDelete() }
            Button("Huỷ", role: .cancel) { vm.cancelDelete() }
        } message: {
            Text("“\(vm.deletingItem?.title ?? "")” sẽ bị xoá vĩnh viễn.")
        }
    }

    // ── Top bar ──
    private var topBar: some View {
        HStack(spacing: 12) {
            Button(action: onMenuClick) {
                Image(systemName: "line.3.horizontal")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(width: 38, height: 38)
                    .background(Color.white.opacity(0.18))
                    .clipShape(Circle())
            }

            VStack(alignment: .leading, spacing: 2) {
                Text("Sổ tay")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.white)
                Text("\(vm.pendingTaskCount) việc · \(vm.reminderCount) nhắc · \(vm.totalCount) mục")
                    .font(.system(size: 11))
                    .foregroundColor(.white.opacity(0.85))
            }
            Spacer()
            Button {
                editingItem = nil; editing = true
            } label: {
                Image(systemName: "plus")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(width: 38, height: 38)
                    .background(Color.white.opacity(0.18))
                    .clipShape(Circle())
            }
        }
        .padding(.horizontal, 20)
        .padding(.top, 8)
        .padding(.bottom, 14)
        .background(
            LinearGradient(
                colors: [Color(red: 0.773, green: 0.157, blue: 0.157),
                         Color(red: 0.545, green: 0, blue: 0)],
                startPoint: .topLeading, endPoint: .bottomTrailing
            )
            .ignoresSafeArea(edges: .top)
        )
    }

    private var searchField: some View {
        HStack(spacing: 8) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 14)).foregroundColor(LSTheme.textTertiary)
            TextField("Tìm theo tiêu đề, mô tả, tag…", text: $vm.query)
                .font(.system(size: 13))
            if !vm.query.isEmpty {
                Button { vm.query = "" } label: {
                    Image(systemName: "xmark.circle.fill")
                        .font(.system(size: 14)).foregroundColor(LSTheme.textTertiary)
                }
            }
        }
        .padding(.horizontal, 14).padding(.vertical, 10)
        .background(LSTheme.surfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(LSTheme.outlineVariant, lineWidth: 1))
    }

    private var emptyState: some View {
        VStack(spacing: 12) {
            Spacer()
            Image(systemName: "note.text.badge.plus")
                .font(.system(size: 44)).foregroundColor(LSTheme.textTertiary.opacity(0.4))
            Text("Chưa có mục nào")
                .font(.system(size: 16, weight: .semibold)).foregroundColor(LSTheme.textPrimary)
            Text("Tạo ghi chú, việc cần làm hoặc nhắc nhở — tất cả trong một.")
                .font(.system(size: 13)).foregroundColor(LSTheme.textSecondary)
                .multilineTextAlignment(.center).padding(.horizontal, 40)
            Button { editingItem = nil; editing = true } label: {
                Text("Tạo mục mới")
                    .font(.system(size: 14, weight: .semibold)).foregroundColor(.white)
                    .padding(.horizontal, 20).padding(.vertical, 10)
                    .background(LSTheme.primary).clipShape(Capsule())
            }
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

// ══════════════════════════════════════════
// ITEM ROW
// ══════════════════════════════════════════

private struct ItemRow: View {
    let item: ItemEntity
    @ObservedObject var vm: ItemsViewModel
    let onTap: () -> Void
    let onToggleDone: () -> Void

    private var accent: Color { itemAccentColors[min(max(item.colorIndex, 0), itemAccentColors.count - 1)] }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            // Marker: checkbox khi là việc, chấm màu khi không
            if item.isTask {
                Button(action: onToggleDone) {
                    Image(systemName: item.isDone ? "checkmark.circle.fill" : "circle")
                        .font(.system(size: 22))
                        .foregroundColor(item.isDone ? LSTheme.goodGreen : accent)
                }
                .buttonStyle(.plain)
            } else {
                Circle().fill(accent).frame(width: 12, height: 12).padding(.top, 5)
            }

            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(item.title)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(LSTheme.textPrimary)
                        .strikethrough(item.isDone)
                        .lineLimit(2)
                    Spacer()
                    if item.isPinned {
                        Image(systemName: "pin.fill")
                            .font(.system(size: 12)).foregroundColor(LSTheme.primary)
                    }
                }
                if !item.itemDescription.isEmpty {
                    Text(item.itemDescription)
                        .font(.system(size: 13)).foregroundColor(LSTheme.textSecondary)
                        .lineLimit(2)
                }
                badges
            }
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(LSTheme.surfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(LSTheme.outlineVariant, lineWidth: 1))
        .contentShape(Rectangle())
        .onTapGesture(perform: onTap)
    }

    @ViewBuilder private var badges: some View {
        let tags = item.tags.split(separator: ",").map { $0.trimmingCharacters(in: .whitespaces) }.filter { !$0.isEmpty }.prefix(2)
        let hasAny = item.isTask || item.hasReminder || !tags.isEmpty
        if hasAny {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    if item.isTask { Badge(icon: "flag", label: vm.priorityLabel(item.priority)) }
                    if item.isTask, let due = item.dueDate { Badge(icon: "calendar", label: vm.formatDate(due)) }
                    if item.hasReminder, let at = item.reminderAt { Badge(icon: "bell", label: vm.formatDateTime(at)) }
                    ForEach(Array(tags), id: \.self) { tag in Badge(icon: "tag", label: tag) }
                }
            }
            .padding(.top, 2)
        }
    }
}

private struct Badge: View {
    let icon: String
    let label: String
    var body: some View {
        HStack(spacing: 3) {
            Image(systemName: icon).font(.system(size: 10))
            Text(label).font(.system(size: 11)).lineLimit(1)
        }
        .foregroundColor(LSTheme.textSecondary)
        .padding(.horizontal, 6).padding(.vertical, 3)
        .background(LSTheme.surfaceContainerHigh)
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

// ══════════════════════════════════════════
// FILTER CHIP
// ══════════════════════════════════════════

private struct FilterChip: View {
    let label: String
    let selected: Bool
    let onTap: () -> Void
    var body: some View {
        Button(action: onTap) {
            Text(label)
                .font(.system(size: 13, weight: selected ? .semibold : .medium))
                .foregroundColor(selected ? .white : LSTheme.textSecondary)
                .padding(.horizontal, 14).padding(.vertical, 8)
                .background(selected ? LSTheme.primary : LSTheme.surfaceContainerHigh)
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }
}

// ══════════════════════════════════════════
// AI COMMAND BAR + CARD
// ══════════════════════════════════════════

struct SharedAiCommandBar: View {
    var placeholder: String
    let isProcessing: Bool
    var focused: FocusState<Bool>.Binding?
    let onSend: (String) -> Void

    @State private var text = ""

    private var canSend: Bool { !text.trimmingCharacters(in: .whitespaces).isEmpty && !isProcessing }

    init(
        placeholder: String,
        isProcessing: Bool,
        focused: FocusState<Bool>.Binding? = nil,
        onSend: @escaping (String) -> Void
    ) {
        self.placeholder = placeholder
        self.isProcessing = isProcessing
        self.focused = focused
        self.onSend = onSend
    }

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: "sparkles").font(.system(size: 16)).foregroundColor(LSTheme.gold)
            inputField
            Button(action: send) {
                Group {
                    if isProcessing {
                        ProgressView().tint(.white).scaleEffect(0.7)
                    } else {
                        Image(systemName: "arrow.up").font(.system(size: 16, weight: .bold)).foregroundColor(.white)
                    }
                }
                .frame(width: 34, height: 34)
                .background(canSend ? LSTheme.primary : LSTheme.outlineVariant)
                .clipShape(Circle())
            }
            .buttonStyle(.plain)
            .disabled(!canSend)
        }
        .padding(.horizontal, 14).padding(.vertical, 6)
        .background(LSTheme.surfaceContainerHigh)
        .clipShape(RoundedRectangle(cornerRadius: 22))
        .overlay(RoundedRectangle(cornerRadius: 22).stroke(LSTheme.outlineVariant, lineWidth: 1))
        .padding(.horizontal, 12).padding(.vertical, 6)
    }

    private func send() {
        guard canSend else { return }
        onSend(text)
        text = ""
    }

    @ViewBuilder
    private var inputField: some View {
        if let focused {
            TextField(placeholder, text: $text)
                .font(.system(size: 14))
                .submitLabel(.send)
                .focused(focused)
                .onSubmit { send() }
        } else {
            TextField(placeholder, text: $text)
                .font(.system(size: 14))
                .submitLabel(.send)
                .onSubmit { send() }
        }
    }
}

private struct AiCard: View {
    let text: String
    let isError: Bool
    let onDismiss: () -> Void
    var body: some View {
        HStack(alignment: .top, spacing: 8) {
            Image(systemName: isError ? "exclamationmark.triangle" : "sparkles")
                .font(.system(size: 16)).foregroundColor(isError ? LSTheme.badRed : LSTheme.primary)
            Text(text).font(.system(size: 13)).foregroundColor(LSTheme.textPrimary)
                .frame(maxWidth: .infinity, alignment: .leading)
            Button(action: onDismiss) {
                Image(systemName: "xmark").font(.system(size: 14)).foregroundColor(LSTheme.textTertiary)
            }
            .buttonStyle(.plain)
        }
        .padding(12)
        .background(isError ? LSTheme.badRed.opacity(0.12) : LSTheme.primaryContainer)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .padding(.horizontal, 14).padding(.vertical, 4)
    }
}

// ══════════════════════════════════════════
// KEYBOARD OBSERVER — theo dõi chiều cao bàn phím (vì GeometryReader ở MainTabView
// làm hỏng cơ chế tự né bàn phím của SwiftUI).
// ══════════════════════════════════════════
final class KeyboardObserver: ObservableObject {
    @Published var height: CGFloat = 0

    init() {
        let nc = NotificationCenter.default
        nc.addObserver(self, selector: #selector(onChange(_:)),
                       name: UIResponder.keyboardWillChangeFrameNotification, object: nil)
        nc.addObserver(self, selector: #selector(onChange(_:)),
                       name: UIResponder.keyboardWillShowNotification, object: nil)
        nc.addObserver(self, selector: #selector(onHide),
                       name: UIResponder.keyboardWillHideNotification, object: nil)
    }

    @objc private func onChange(_ note: Notification) {
        guard let frame = note.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect else { return }
        let screenHeight = UIScreen.main.bounds.height
        height = max(0, screenHeight - frame.origin.y)
    }

    @objc private func onHide() { height = 0 }

    deinit { NotificationCenter.default.removeObserver(self) }
}
