import SwiftUI

// MARK: - Templates Screen
struct TemplatesScreen: View {
    @Environment(\.lichSoColors) var c
    @EnvironmentObject var tasksVM: TasksViewModel
    @StateObject private var store = TemplateStore.shared
    @State private var selectedCategory: TemplateCategory = .lichNgay
    @State private var toastMessage: String? = nil
    @State private var editingCustom: CustomTemplate? = nil
    @State private var showCreateNew = false

    enum TemplateCategory: String, CaseIterable {
        case cuaToi   = "Của Tôi"
        case lichNgay = "Lịch Ngày"
        case cưới     = "Cưới Hỏi"
        case kinhDoanh = "Kinh Doanh"
        case xinViệc  = "Xin Việc"
        case chuyenNha = "Chuyển Nhà"
        case xuatHanh = "Xuất Hành"

        var icon: String {
            switch self {
            case .cuaToi:    return "person.crop.rectangle.stack.fill"
            case .lichNgay:  return "calendar"
            case .cưới:      return "heart.fill"
            case .kinhDoanh: return "chart.line.uptrend.xyaxis"
            case .xinViệc:   return "briefcase"
            case .chuyenNha: return "house.fill"
            case .xuatHanh:  return "airplane.departure"
            }
        }
    }

    var builtInTemplates: [TemplateItem] {
        switch selectedCategory {
        case .cuaToi: return []
        case .lichNgay:
            return [
                TemplateItem(title: "Checklist Ngày Tốt", desc: "Danh sách công việc phù hợp với ngày hoàng đạo", icon: "checkmark.seal", color: "gold"),
                TemplateItem(title: "Nhật Ký Phong Thuỷ", desc: "Ghi lại những ngày tốt, xấu trong tháng", icon: "book", color: "teal"),
                TemplateItem(title: "Kế Hoạch Tuần", desc: "Lịch làm việc theo Can Chi trong tuần", icon: "calendar.badge.clock", color: "gold"),
            ]
        case .cưới:
            return [
                TemplateItem(title: "Checklist Cưới Hỏi", desc: "Các đầu việc cần chuẩn bị cho ngày cưới", icon: "heart.text.square", color: "red"),
                TemplateItem(title: "Danh Sách Khách Mời", desc: "Quản lý danh sách khách mời và xác nhận", icon: "person.2", color: "gold"),
                TemplateItem(title: "Chọn Ngày Cưới", desc: "Tìm ngày tốt phù hợp cho đôi uyên ương", icon: "calendar.badge.checkmark", color: "teal"),
            ]
        case .kinhDoanh:
            return [
                TemplateItem(title: "Khai Trương Cửa Hàng", desc: "Checklist chuẩn bị khai trương theo phong thuỷ", icon: "storefront", color: "gold"),
                TemplateItem(title: "Ký Kết Hợp Đồng", desc: "Ngày tốt và giờ tốt để ký hợp đồng", icon: "signature", color: "teal"),
                TemplateItem(title: "Ra Mắt Sản Phẩm", desc: "Lịch trình ra mắt theo ngày hoàng đạo", icon: "star.circle", color: "gold"),
            ]
        case .xinViệc:
            return [
                TemplateItem(title: "Ngày Nộp Hồ Sơ", desc: "Chọn ngày tốt để nộp đơn xin việc", icon: "doc.badge.plus", color: "teal"),
                TemplateItem(title: "Chuẩn Bị Phỏng Vấn", desc: "Checklist chuẩn bị theo phong thuỷ", icon: "person.crop.rectangle", color: "gold"),
            ]
        case .chuyenNha:
            return [
                TemplateItem(title: "Checklist Chuyển Nhà", desc: "Danh sách việc cần làm trước ngày chuyển", icon: "house.and.flag", color: "gold"),
                TemplateItem(title: "Nghi Lễ Nhập Trạch", desc: "Hướng dẫn nghi thức nhập trạch theo phong thuỷ", icon: "flame", color: "red"),
                TemplateItem(title: "Bố Trí Nội Thất", desc: "Sắp xếp đồ đạc theo hướng phong thuỷ", icon: "square.split.2x2", color: "teal"),
            ]
        case .xuatHanh:
            return [
                TemplateItem(title: "Checklist Du Lịch", desc: "Danh sách chuẩn bị theo ngày xuất hành tốt", icon: "airplane", color: "teal"),
                TemplateItem(title: "Hướng Xuất Hành", desc: "Chọn hướng đi theo Thần Tài và Hỷ Thần", icon: "location.north.line", color: "gold"),
            ]
        }
    }

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            VStack(spacing: 0) {
                // Header
                HStack {
                    Text("Template")
                        .font(.system(size: 22, weight: .bold, design: .serif))
                        .foregroundColor(c.gold2)
                    Spacer()
                    if selectedCategory == .cuaToi {
                        Button(action: { showCreateNew = true }) {
                            HStack(spacing: 4) {
                                Image(systemName: "plus").font(.system(size: 13, weight: .semibold))
                                Text("Tạo mới").font(.system(size: 13, weight: .semibold))
                            }
                            .foregroundColor(c.isDark ? Color(hex: 0x1A1500) : .white)
                            .padding(.horizontal, 12).padding(.vertical, 6)
                            .background(c.gold).clipShape(Capsule())
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 14)

                // Category Tabs
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(TemplateCategory.allCases, id: \.self) { cat in
                            CategoryChip(
                                title: cat.rawValue,
                                icon: cat.icon,
                                isSelected: selectedCategory == cat,
                                badge: cat == .cuaToi && !store.customTemplates.isEmpty ? store.customTemplates.count : nil
                            )
                            .onTapGesture { selectedCategory = cat }
                        }
                    }
                    .padding(.horizontal, 20).padding(.vertical, 4)
                }

                Spacer(minLength: 12)

                ScrollView {
                    VStack(spacing: 10) {
                        if selectedCategory == .cuaToi {
                            customTemplatesSection
                        } else {
                            ForEach(builtInTemplates) { template in
                                TemplateCard(
                                    template: template, tasksVM: tasksVM,
                                    onApplied: showToast,
                                    onClone: {
                                        var clone = store.clone(from: template)
                                        store.add(clone)
                                        selectedCategory = .cuaToi
                                        editingCustom = store.customTemplates.first { $0.id == clone.id }
                                        showToast("📋 Đã clone! Bạn có thể chỉnh sửa bên \"Của Tôi\"")
                                    }
                                )
                            }
                        }
                        Spacer(minLength: 100)
                    }
                    .padding(.horizontal, 20).padding(.bottom, 32)
                }
            }
            .background(c.bg.ignoresSafeArea())
        }
        .overlay(alignment: .bottom) {
            if let msg = toastMessage {
                Text(msg)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(c.isDark ? Color(hex: 0x1A1500) : .white)
                    .padding(.horizontal, 18).padding(.vertical, 10)
                    .background(c.teal).clipShape(Capsule())
                    .shadow(radius: 6).padding(.bottom, 100)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .animation(.spring(), value: toastMessage)
            }
        }
        .sheet(item: $editingCustom) { tpl in
            TemplateEditSheet(template: tpl, store: store)
                .environment(\.lichSoColors, c)
        }
        .sheet(isPresented: $showCreateNew) {
            TemplateEditSheet(template: nil, store: store)
                .environment(\.lichSoColors, c)
        }
    }

    // MARK: Custom Templates Section
    @ViewBuilder
    private var customTemplatesSection: some View {
        if store.customTemplates.isEmpty {
            VStack(spacing: 16) {
                Image(systemName: "doc.badge.plus")
                    .font(.system(size: 40)).foregroundColor(c.textQuaternary)
                Text("Chưa có template nào")
                    .font(.system(size: 15, weight: .medium)).foregroundColor(c.textTertiary)
                Text("Clone từ template có sẵn hoặc tạo mới để bắt đầu")
                    .font(.system(size: 13)).foregroundColor(c.textQuaternary).multilineTextAlignment(.center)
                Button(action: { showCreateNew = true }) {
                    HStack(spacing: 6) {
                        Image(systemName: "plus.circle.fill")
                        Text("Tạo template mới")
                    }
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(c.isDark ? Color(hex: 0x1A1500) : .white)
                    .padding(.horizontal, 20).padding(.vertical, 11)
                    .background(c.gold).clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
            .frame(maxWidth: .infinity).padding(.vertical, 60)
        } else {
            ForEach(store.customTemplates) { tpl in
                CustomTemplateCard(
                    template: tpl, tasksVM: tasksVM, store: store,
                    onApplied: showToast,
                    onEdit: { editingCustom = tpl }
                )
            }
        }
    }

    private func showToast(_ msg: String) {
        toastMessage = msg
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) { toastMessage = nil }
    }
}

// MARK: - Template Models
struct TemplateItem: Identifiable {
    let id = UUID()
    let title: String
    let desc: String
    let icon: String
    let color: String
}

// MARK: - Category Chip (updated with badge)
struct CategoryChip: View {
    let title: String
    let icon: String
    let isSelected: Bool
    var badge: Int? = nil
    @Environment(\.lichSoColors) var c

    var body: some View {
        HStack(spacing: 5) {
            Image(systemName: icon).font(.system(size: 11))
            Text(title).font(.system(size: 12, weight: isSelected ? .semibold : .medium))
            if let b = badge {
                Text("\(b)")
                    .font(.system(size: 9, weight: .bold))
                    .foregroundColor(isSelected ? c.gold2 : c.textQuaternary)
                    .padding(.horizontal, 5).padding(.vertical, 1)
                    .background(isSelected ? c.gold.opacity(0.2) : c.surface)
                    .clipShape(Capsule())
            }
        }
        .foregroundColor(isSelected ? c.gold2 : c.textTertiary)
        .padding(.horizontal, 12).padding(.vertical, 6)
        .background(isSelected ? c.goldDim : c.bg2)
        .clipShape(Capsule())
        .overlay(Capsule().stroke(isSelected ? c.gold.opacity(0.3) : c.border, lineWidth: 1))
    }
}

// MARK: - Built-in Template Card (with Clone button)
struct TemplateCard: View {
    let template: TemplateItem
    let tasksVM: TasksViewModel
    let onApplied: (String) -> Void
    let onClone: () -> Void
    @Environment(\.lichSoColors) var c
    @State private var showDetail = false

    var accentColor: Color {
        switch template.color {
        case "gold": return c.gold2; case "teal": return c.teal2; case "red": return c.red2
        default: return c.gold2
        }
    }

    var body: some View {
        HStack(spacing: 14) {
            // Icon
            ZStack {
                RoundedRectangle(cornerRadius: 10).fill(accentColor.opacity(0.12)).frame(width: 44, height: 44)
                Image(systemName: template.icon).font(.system(size: 20)).foregroundColor(accentColor)
            }
            // Info
            VStack(alignment: .leading, spacing: 3) {
                Text(template.title).font(.system(size: 14, weight: .semibold)).foregroundColor(c.textPrimary)
                Text(template.desc).font(.system(size: 12)).foregroundColor(c.textSecondary).lineLimit(2)
            }
            Spacer()
            // Actions
            VStack(spacing: 6) {
                Button(action: onClone) {
                    Image(systemName: "doc.on.doc")
                        .font(.system(size: 14))
                        .foregroundColor(c.teal2)
                        .frame(width: 32, height: 32)
                        .background(c.teal.opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }
                .buttonStyle(.plain)
                Button(action: { showDetail = true }) {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 12))
                        .foregroundColor(c.textTertiary)
                        .frame(width: 32, height: 32)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(14)
        .background(c.bg2)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(c.border, lineWidth: 1))
        .sheet(isPresented: $showDetail) {
            TemplateDetailView(template: template, tasksVM: tasksVM, onApplied: { msg in
                showDetail = false; onApplied(msg)
            })
            .environment(\.lichSoColors, c)
        }
    }
}

// MARK: - Custom Template Card
struct CustomTemplateCard: View {
    let template: CustomTemplate
    let tasksVM: TasksViewModel
    let store: TemplateStore
    let onApplied: (String) -> Void
    let onEdit: () -> Void
    @Environment(\.lichSoColors) var c
    @State private var showDetail = false
    @State private var showDeleteConfirm = false

    var accentColor: Color {
        switch template.color {
        case "gold": return c.gold2; case "teal": return c.teal2; case "red": return c.red2
        case "orange": return c.noteOrange; case "purple": return c.notePurple; case "green": return c.noteGreen
        default: return c.gold2
        }
    }

    var body: some View {
        HStack(spacing: 14) {
            ZStack {
                RoundedRectangle(cornerRadius: 10).fill(accentColor.opacity(0.12)).frame(width: 44, height: 44)
                Image(systemName: template.icon).font(.system(size: 20)).foregroundColor(accentColor)
            }
            VStack(alignment: .leading, spacing: 3) {
                HStack(spacing: 6) {
                    Text(template.title).font(.system(size: 14, weight: .semibold)).foregroundColor(c.textPrimary)
                    if template.clonedFrom != nil {
                        Text("Clone").font(.system(size: 9, weight: .semibold))
                            .foregroundColor(c.teal2).padding(.horizontal, 5).padding(.vertical, 1)
                            .background(c.teal.opacity(0.12)).clipShape(Capsule())
                    }
                }
                Text("\(template.items.count) mục • \(template.desc)").font(.system(size: 12)).foregroundColor(c.textSecondary).lineLimit(1)
            }
            Spacer()
            // Action menu
            Menu {
                Button { showDetail = true } label: { Label("Sử dụng", systemImage: "plus.circle") }
                Button { onEdit() } label: { Label("Chỉnh sửa", systemImage: "pencil") }
                Divider()
                Button(role: .destructive) { showDeleteConfirm = true } label: { Label("Xoá", systemImage: "trash") }
            } label: {
                Image(systemName: "ellipsis")
                    .font(.system(size: 16))
                    .foregroundColor(c.textTertiary)
                    .frame(width: 36, height: 36)
                    .background(c.bg3).clipShape(RoundedRectangle(cornerRadius: 8))
            }
        }
        .padding(14)
        .background(c.bg2)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(accentColor.opacity(0.25), lineWidth: 1))
        .confirmationDialog("Xoá template?", isPresented: $showDeleteConfirm, titleVisibility: .visible) {
            Button("Xoá", role: .destructive) { store.delete(template.id) }
            Button("Huỷ", role: .cancel) {}
        } message: { Text("Template \"\(template.title)\" sẽ bị xoá vĩnh viễn.") }
        .sheet(isPresented: $showDetail) {
            CustomTemplateDetailView(template: template, tasksVM: tasksVM, onApplied: { msg in
                showDetail = false; onApplied(msg)
            })
            .environment(\.lichSoColors, c)
        }
        .interactiveRow(onEdit: { onEdit() }, onDeleteRequest: { showDeleteConfirm = true })
    }
}

// MARK: - Template Edit Sheet (Create / Edit custom template)
struct TemplateEditSheet: View {
    let template: CustomTemplate?   // nil = create new
    let store: TemplateStore
    @Environment(\.lichSoColors) var c
    @Environment(\.dismiss) var dismiss

    @State private var title: String = ""
    @State private var desc: String = ""
    @State private var icon: String = "checkmark.seal"
    @State private var color: String = "gold"
    @State private var items: [String] = [""]
    @State private var newItemText: String = ""
    @FocusState private var focusedIndex: Int?

    private var isEditing: Bool { template != nil }
    private var isValid: Bool { !title.trimmingCharacters(in: .whitespaces).isEmpty && items.filter { !$0.isEmpty }.count > 0 }

    let availableIcons: [(String, String)] = [
        ("checkmark.seal","Dấu tốt"), ("calendar","Lịch"), ("heart.fill","Cưới hỏi"),
        ("storefront","Khai trương"), ("airplane","Du lịch"), ("house.fill","Nhà"),
        ("briefcase","Công việc"), ("doc.badge.plus","Hồ sơ"), ("star.circle","Sự kiện"),
        ("flame","Nghi lễ"), ("book","Nhật ký"), ("bell","Nhắc nhở"),
        ("clock","Thời gian"), ("location.north.line","Hướng"), ("person.2","Khách mời"),
    ]

    let colorOptions: [(String, String)] = [
        ("gold","Vàng"), ("teal","Xanh lá"), ("red","Đỏ"),
        ("orange","Cam"), ("purple","Tím"), ("green","Lục"),
    ]

    var body: some View {
        NavigationView {
            ZStack { c.bg.ignoresSafeArea()
                ScrollView {
                    VStack(spacing: 20) {

                        // ── Tên & Mô tả ──
                        VStack(alignment: .leading, spacing: 8) {
                            SectionLabel("TÊN TEMPLATE")
                            HStack(spacing: 10) {
                                // Icon picker button
                                Menu {
                                    ForEach(availableIcons, id: \.0) { key, label in
                                        Button { icon = key } label: {
                                            Label(label, systemImage: key)
                                        }
                                    }
                                } label: {
                                    ZStack {
                                        RoundedRectangle(cornerRadius: 10).fill(accentColor.opacity(0.12)).frame(width: 42, height: 42)
                                        Image(systemName: icon).font(.system(size: 18)).foregroundColor(accentColor)
                                    }
                                }
                                TextField("Tên template...", text: $title)
                                    .font(.system(size: 14, weight: .semibold)).foregroundColor(c.textPrimary)
                            }
                            .padding(12).background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 12))
                            .overlay(RoundedRectangle(cornerRadius: 12).stroke(c.border, lineWidth: 1))

                            TextField("Mô tả ngắn...", text: $desc)
                                .font(.system(size: 13)).foregroundColor(c.textSecondary)
                                .padding(12).background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 10))
                                .overlay(RoundedRectangle(cornerRadius: 10).stroke(c.border, lineWidth: 1))
                        }

                        // ── Màu sắc ──
                        VStack(alignment: .leading, spacing: 8) {
                            SectionLabel("MÀU SẮC")
                            HStack(spacing: 8) {
                                ForEach(colorOptions, id: \.0) { key, _ in
                                    let col = colorValue(key)
                                    Button { color = key } label: {
                                        ZStack {
                                            Circle().fill(col.opacity(0.2)).frame(width: 36, height: 36)
                                            Circle().fill(col).frame(width: 22, height: 22)
                                            if color == key {
                                                Image(systemName: "checkmark").font(.system(size: 11, weight: .bold)).foregroundColor(.white)
                                            }
                                        }
                                    }
                                    .frame(maxWidth: .infinity)
                                }
                            }
                            .padding(12).background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 12))
                            .overlay(RoundedRectangle(cornerRadius: 12).stroke(c.border, lineWidth: 1))
                        }

                        // ── Checklist Items ──
                        VStack(alignment: .leading, spacing: 8) {
                            HStack {
                                SectionLabel("CHECKLIST (\(items.filter{!$0.isEmpty}.count) mục)")
                                Spacer()
                                Text("Kéo để sắp xếp").font(.system(size: 10)).foregroundColor(c.textQuaternary)
                            }

                            VStack(spacing: 0) {
                                ForEach(items.indices, id: \.self) { i in
                                    HStack(spacing: 8) {
                                        Image(systemName: "line.3.horizontal")
                                            .font(.system(size: 13)).foregroundColor(c.textQuaternary)
                                            .frame(width: 20)
                                        TextField("Mục \(i + 1)...", text: $items[i])
                                            .font(.system(size: 13)).foregroundColor(c.textPrimary)
                                            .focused($focusedIndex, equals: i)
                                            .onSubmit { appendItemIfNeeded() }
                                        Spacer()
                                        if items.count > 1 {
                                            Button { items.remove(at: i) } label: {
                                                Image(systemName: "xmark.circle.fill")
                                                    .font(.system(size: 16)).foregroundColor(c.textQuaternary)
                                            }
                                        }
                                    }
                                    .padding(.horizontal, 12).padding(.vertical, 9)
                                    if i < items.count - 1 {
                                        Divider().overlay(c.border).padding(.leading, 40)
                                    }
                                }
                            }
                            .background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 12))
                            .overlay(RoundedRectangle(cornerRadius: 12).stroke(c.border, lineWidth: 1))

                            // Add item
                            Button(action: appendItemIfNeeded) {
                                HStack(spacing: 6) {
                                    Image(systemName: "plus.circle.fill").foregroundColor(c.teal)
                                    Text("Thêm mục").font(.system(size: 13)).foregroundColor(c.teal2)
                                }
                                .padding(.horizontal, 14).padding(.vertical, 9)
                                .background(c.teal.opacity(0.08)).clipShape(RoundedRectangle(cornerRadius: 10))
                                .overlay(RoundedRectangle(cornerRadius: 10).stroke(c.teal.opacity(0.2), lineWidth: 1))
                            }
                        }

                        // ── Save Button ──
                        Button(action: save) {
                            Text(isEditing ? "Lưu thay đổi" : "Tạo template")
                                .font(.system(size: 15, weight: .semibold))
                                .foregroundColor(c.isDark ? Color(hex: 0x1A1500) : .white)
                                .frame(maxWidth: .infinity).padding(.vertical, 14)
                                .background(isValid ? c.gold : c.surface)
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                        .disabled(!isValid)

                        Spacer(minLength: 40)
                    }
                    .padding(20)
                }
            }
            .navigationTitle(isEditing ? "Chỉnh sửa Template" : "Tạo Template Mới")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Huỷ") { dismiss() }.foregroundColor(c.textSecondary)
                }
            }
            .onAppear { prefill() }
        }
    }

    private var accentColor: Color { colorValue(color) }

    private func colorValue(_ key: String) -> Color {
        switch key {
        case "gold": return c.gold2; case "teal": return c.teal2; case "red": return c.red2
        case "orange": return c.noteOrange; case "purple": return c.notePurple; case "green": return c.noteGreen
        default: return c.gold2
        }
    }

    private func prefill() {
        guard let t = template else {
            items = [""]
            return
        }
        title = t.title; desc = t.desc; icon = t.icon; color = t.color
        items = t.items.isEmpty ? [""] : t.items
    }

    private func appendItemIfNeeded() {
        // Only add a new row if the last one isn't empty
        if items.last?.isEmpty == false {
            items.append("")
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.05) {
                focusedIndex = items.count - 1
            }
        }
    }

    private func save() {
        let clean = items.map { $0.trimmingCharacters(in: .whitespaces) }.filter { !$0.isEmpty }
        guard !title.trimmingCharacters(in: .whitespaces).isEmpty, !clean.isEmpty else { return }
        if let existing = template {
            var updated = existing
            updated.title = title.trimmingCharacters(in: .whitespaces)
            updated.desc = desc.trimmingCharacters(in: .whitespaces)
            updated.icon = icon; updated.color = color; updated.items = clean
            store.update(updated)
        } else {
            let new = CustomTemplate(title: title.trimmingCharacters(in: .whitespaces),
                                     desc: desc.trimmingCharacters(in: .whitespaces),
                                     icon: icon, color: color, items: clean, clonedFrom: nil)
            store.add(new)
        }
        dismiss()
    }
}

// MARK: - Custom Template Detail (use custom template)
struct CustomTemplateDetailView: View {
    let template: CustomTemplate
    let tasksVM: TasksViewModel
    let onApplied: (String) -> Void
    @Environment(\.lichSoColors) var c
    @Environment(\.dismiss) var dismiss
    @State private var skipped: Set<String> = []
    @State private var deadlineDate = Calendar.current.date(byAdding: .day, value: 7, to: Date()) ?? Date()
    @State private var showDatePicker = false

    var accentColor: Color {
        switch template.color {
        case "gold": return c.gold2; case "teal": return c.teal2; case "red": return c.red2
        case "orange": return c.noteOrange; case "purple": return c.notePurple; case "green": return c.noteGreen
        default: return c.gold2
        }
    }

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    headerCard
                    Divider().overlay(c.border)
                    checklistSection
                    Divider().overlay(c.border)
                    deadlineSection
                    useButton
                }
                .padding(20)
            }
            .background(c.bg.ignoresSafeArea())
            .navigationTitle(template.title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .cancellationAction) { Button("Đóng") { dismiss() } } }
        }
    }

    @ViewBuilder
    private var headerCard: some View {
        HStack(spacing: 14) {
            ZStack {
                RoundedRectangle(cornerRadius: 12).fill(accentColor.opacity(0.12)).frame(width: 52, height: 52)
                Image(systemName: template.icon).font(.system(size: 24)).foregroundColor(accentColor)
            }
            VStack(alignment: .leading, spacing: 3) {
                Text(template.title).font(.system(size: 15, weight: .bold)).foregroundColor(c.textPrimary)
                if !template.desc.isEmpty {
                    Text(template.desc).font(.system(size: 12)).foregroundColor(c.textSecondary)
                }
                if let src = template.clonedFrom {
                    HStack(spacing: 4) {
                        Image(systemName: "doc.on.doc").font(.system(size: 9))
                        Text("Clone từ: \(src)").font(.system(size: 10))
                    }.foregroundColor(c.teal2)
                }
            }
        }
        .padding(14).background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(accentColor.opacity(0.2), lineWidth: 1))
    }

    @ViewBuilder
    private var checklistSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                SectionLabel("CHECKLIST (\(template.items.count) MỤC)")
                Spacer()
                let allSkipped = skipped.count == template.items.count
                Button(allSkipped ? "Chọn tất cả" : "Bỏ qua tất cả") {
                    skipped = allSkipped ? [] : Set(template.items)
                }
                .font(.system(size: 11)).foregroundColor(c.teal2)
            }
            ForEach(template.items, id: \.self) { item in
                let isSkipped = skipped.contains(item)
                Button {
                    if isSkipped { skipped.remove(item) } else { skipped.insert(item) }
                } label: {
                    HStack(spacing: 10) {
                        Image(systemName: isSkipped ? "minus.square" : "checkmark.square.fill")
                            .font(.system(size: 18))
                            .foregroundColor(isSkipped ? c.textQuaternary : accentColor)
                        Text(item).font(.system(size: 14))
                            .foregroundColor(isSkipped ? c.textQuaternary : c.textPrimary)
                            .strikethrough(isSkipped, color: c.textQuaternary)
                        Spacer()
                    }
                    .padding(.vertical, 5)
                }
                .buttonStyle(.plain)
            }
        }
    }

    @ViewBuilder
    private var deadlineSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionLabel("DEADLINE")
            Button(action: { showDatePicker.toggle() }) {
                HStack {
                    Image(systemName: "calendar.badge.clock").foregroundColor(accentColor)
                    Text(deadlineDate.formatted(date: .abbreviated, time: .omitted))
                        .font(.system(size: 14)).foregroundColor(c.textPrimary)
                    Spacer()
                    Image(systemName: showDatePicker ? "chevron.up" : "chevron.down")
                        .font(.system(size: 11)).foregroundColor(c.textTertiary)
                }
                .padding(12).background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 10))
                .overlay(RoundedRectangle(cornerRadius: 10).stroke(c.border, lineWidth: 1))
            }
            .buttonStyle(.plain)
            if showDatePicker {
                DatePicker("", selection: $deadlineDate, in: Date()..., displayedComponents: .date)
                    .labelsHidden().datePickerStyle(.graphical).tint(accentColor)
            }
        }
    }

    @ViewBuilder
    private var useButton: some View {
        let toAdd = template.items.filter { !skipped.contains($0) }
        let fgColor: Color = toAdd.isEmpty ? c.textQuaternary : (c.isDark ? Color(hex: 0x1A1500) : .white)
        let bgColor: Color = toAdd.isEmpty ? c.surface : accentColor
        let label: String = toAdd.isEmpty ? "Không có mục nào được chọn" : "Thêm \(toAdd.count) việc vào Công việc"
        Button(action: { apply(items: toAdd) }) {
            HStack {
                Image(systemName: "plus.circle.fill")
                Text(label)
            }
            .font(.system(size: 15, weight: .semibold))
            .foregroundColor(fgColor)
            .frame(maxWidth: .infinity).padding(.vertical, 14)
            .background(bgColor)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .disabled(toAdd.isEmpty)
    }

    private func apply(items: [String]) {
        for (i, item) in items.enumerated() {
            let dl = Calendar.current.date(byAdding: .day, value: i, to: deadlineDate) ?? deadlineDate
            tasksVM.addTask("[\(template.title)] \(item)", priority: 1, deadline: dl)
        }
        tasksVM.addNote(template.title,
            content: "Template: \(template.desc)\n\nChecklist:\n" + items.map { "• \($0)" }.joined(separator: "\n"),
            color: template.color)
        onApplied("✅ Đã thêm \(items.count) việc từ template!")
    }
}

// MARK: - Template Detail (built-in)
struct TemplateDetailView: View {
    let template: TemplateItem
    let tasksVM: TasksViewModel
    let onApplied: (String) -> Void
    @Environment(\.lichSoColors) var c
    @Environment(\.dismiss) var dismiss
    @State private var checkedItems: Set<String> = []
    @State private var deadlineDate = Calendar.current.date(byAdding: .day, value: 7, to: Date()) ?? Date()
    @State private var showDatePicker = false

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Mô tả").font(.system(size: 13, weight: .bold)).foregroundColor(c.textTertiary)
                        Text(template.desc).font(.system(size: 14)).foregroundColor(c.textPrimary)
                    }
                    Divider().overlay(c.border)
                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            SectionLabel("CHECKLIST MẪU")
                            Spacer()
                            Button(checkedItems.count == sampleItems.count ? "Bỏ chọn tất cả" : "Chọn tất cả") {
                                checkedItems = checkedItems.count == sampleItems.count ? [] : Set(sampleItems)
                            }
                            .font(.system(size: 11)).foregroundColor(c.teal2)
                        }
                        ForEach(sampleItems, id: \.self) { item in
                            Button {
                                if checkedItems.contains(item) { checkedItems.remove(item) } else { checkedItems.insert(item) }
                            } label: {
                                HStack(spacing: 10) {
                                    Image(systemName: checkedItems.contains(item) ? "checkmark.square.fill" : "square")
                                        .font(.system(size: 18)).foregroundColor(checkedItems.contains(item) ? c.teal : c.textTertiary)
                                    Text(item).font(.system(size: 14))
                                        .foregroundColor(checkedItems.contains(item) ? c.textSecondary : c.textPrimary)
                                        .strikethrough(checkedItems.contains(item), color: c.textTertiary)
                                    Spacer()
                                }
                                .padding(.vertical, 5)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    Divider().overlay(c.border)
                    VStack(alignment: .leading, spacing: 8) {
                        SectionLabel("DEADLINE")
                        Button(action: { showDatePicker.toggle() }) {
                            HStack {
                                Image(systemName: "calendar.badge.clock").foregroundColor(c.teal)
                                Text(deadlineDate.formatted(date: .abbreviated, time: .omitted))
                                    .font(.system(size: 14)).foregroundColor(c.textPrimary)
                                Spacer()
                                Image(systemName: showDatePicker ? "chevron.up" : "chevron.down")
                                    .font(.system(size: 11)).foregroundColor(c.textTertiary)
                            }
                            .padding(12).background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 10))
                            .overlay(RoundedRectangle(cornerRadius: 10).stroke(c.border, lineWidth: 1))
                        }
                        .buttonStyle(.plain)
                        if showDatePicker {
                            DatePicker("", selection: $deadlineDate, in: Date()..., displayedComponents: .date)
                                .labelsHidden().datePickerStyle(.graphical).tint(c.teal)
                        }
                    }
                    let selectedCount = sampleItems.count - checkedItems.count
                    Button(action: applyTemplate) {
                        HStack {
                            Image(systemName: "plus.circle.fill")
                            Text(selectedCount > 0 ? "Thêm \(selectedCount) việc vào Công việc" : "Thêm tất cả \(sampleItems.count) việc")
                        }
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(c.isDark ? Color(hex: 0x1A1500) : .white)
                        .frame(maxWidth: .infinity).padding(.vertical, 14)
                        .background(c.gold).clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                }
                .padding(20)
            }
            .background(c.bg.ignoresSafeArea())
            .navigationTitle(template.title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .cancellationAction) { Button("Đóng") { dismiss() } } }
        }
    }

    private func applyTemplate() {
        let toAdd = sampleItems.filter { !checkedItems.contains($0) }
        let items = toAdd.isEmpty ? sampleItems : toAdd
        for (i, item) in items.enumerated() {
            let dl = Calendar.current.date(byAdding: .day, value: i, to: deadlineDate) ?? deadlineDate
            tasksVM.addTask("[\(template.title)] \(item)", priority: 1, deadline: dl)
        }
        tasksVM.addNote(template.title,
            content: "Template: \(template.desc)\n\nChecklist:\n" + items.map { "• \($0)" }.joined(separator: "\n"),
            color: template.color == "red" ? "red" : template.color == "teal" ? "teal" : "gold")
        onApplied("✅ Đã thêm \(items.count) việc từ template!")
    }

    var sampleItems: [String] {
        TemplateStore.shared.defaultItems(for: template.title)
    }
}
