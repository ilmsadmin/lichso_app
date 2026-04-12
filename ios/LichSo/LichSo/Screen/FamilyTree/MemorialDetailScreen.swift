import SwiftUI
import SwiftData
import UIKit

// ═══════════════════════════════════════════
// Memorial Detail Screen — Matches screen-memorial-detail.html
// Shows memorial day with hero, date cards, person card,
// offerings, checklist, prayer link, reminders, notes.
// Uses REAL data from SwiftData
// ═══════════════════════════════════════════

private var PrimaryRed: Color { LSTheme.primary }
private var DeepRed: Color { LSTheme.deepRed }
private var GoldAccent: Color { LSTheme.gold }
private var SurfaceBg: Color { LSTheme.bg }
private var SurfaceContainer: Color { LSTheme.surfaceContainer }
private var SurfaceContainerHigh: Color { LSTheme.surfaceContainerHigh }
private var PrimaryContainer: Color { LSTheme.primaryContainer }
private var TextMain: Color { LSTheme.textPrimary }
private var TextSub: Color { LSTheme.textSecondary }
private var TextDim: Color { LSTheme.textTertiary }
private var OutlineVariant: Color { LSTheme.outlineVariant }

struct MemorialDetailScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @ObservedObject var viewModel: FamilyTreeViewModel
    let memorial: MemorialDayEntity

    @State private var showDeleteAlert = false
    @State private var showEditSheet = false
    @State private var showMemberDetail = false
    @State private var newChecklistText = ""
    @State private var showAddChecklist = false

    var body: some View {
        VStack(spacing: 0) {
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 0) {
                    // ═══ HERO ═══
                    MemorialHero(
                        memorial: memorial,
                        daysUntil: viewModel.daysUntilMemorial(memorial),
                        deathYear: viewModel.member(byId: memorial.memberId)?.deathYear,
                        onBack: { dismiss() },
                        onShare: { shareMemorial() },
                        onMore: { showEditSheet = true }
                    )

                    VStack(alignment: .leading, spacing: 0) {
                        // ═══ DATE CARDS ═══
                        SectionLabel(icon: "calendar", text: "NGÀY GIỖ")
                        DateCardsRow(memorial: memorial, viewModel: viewModel)

                        // ═══ PERSON CARD ═══
                        SectionLabel(icon: "person.fill", text: "NGƯỜI ĐƯỢC TƯỞNG NHỚ")
                        if let member = viewModel.member(byId: memorial.memberId) {
                            PersonCard(member: member, viewModel: viewModel) {
                                showMemberDetail = true
                            }
                        }

                        // ═══ OFFERINGS ═══
                        SectionLabel(icon: "fork.knife", text: "LỄ VẬT CÚNG GIỖ")
                        OfferingsGrid()

                        // ═══ CHECKLIST ═══
                        SectionLabel(icon: "checklist", text: "VIỆC CẦN LÀM")
                        ChecklistSection(
                            items: viewModel.checklists,
                            onToggle: { item in viewModel.toggleChecklist(item) },
                            onDelete: { item in viewModel.deleteChecklistItem(item) },
                            onAdd: { showAddChecklist = true }
                        )

                        // ═══ PRAYER LINK ═══
                        SectionLabel(icon: "book.fill", text: "BÀI CÚNG")
                        PrayerLinkCard()

                        // ═══ REMINDERS ═══
                        SectionLabel(icon: "bell.fill", text: "NHẮC NHỞ")
                        ReminderToggleCard(
                            icon: "bell.badge.fill",
                            iconBg: Color(hex: "FFF3E0"),
                            iconFg: Color(hex: "E65100"),
                            title: "Nhắc trước 3 ngày",
                            isOn: Binding(
                                get: { memorial.remindBefore3Days },
                                set: { newVal in
                                    memorial.remindBefore3Days = newVal
                                    viewModel.updateMemorial(memorial)
                                }
                            )
                        )
                        ReminderToggleCard(
                            icon: "alarm.fill",
                            iconBg: Color(hex: "E3F2FD"),
                            iconFg: Color(hex: "1565C0"),
                            title: "Nhắc ngay ngày giỗ",
                            isOn: Binding(
                                get: { memorial.remindBefore1Day },
                                set: { newVal in
                                    memorial.remindBefore1Day = newVal
                                    viewModel.updateMemorial(memorial)
                                }
                            )
                        )

                        // ═══ NOTES ═══
                        if let note = memorial.note, !note.isEmpty {
                            SectionLabel(icon: "note.text", text: "GHI CHÚ")
                            Text(note)
                                .font(.system(size: 13))
                                .foregroundColor(TextSub)
                                .lineSpacing(4)
                                .padding(16)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(SurfaceContainer)
                                .clipShape(RoundedRectangle(cornerRadius: 14))
                                .overlay(RoundedRectangle(cornerRadius: 14).stroke(OutlineVariant, lineWidth: 1))
                                .padding(.bottom, 12)
                        }

                        // ═══ QUOTE ═══
                        QuoteSection()

                        Spacer().frame(height: 24)
                    }
                    .padding(.horizontal, 16)
                }
            }
        }
        .background(SurfaceBg)
        .navigationBarHidden(true)
        .onAppear {
            viewModel.setup(context: modelContext)
            viewModel.loadChecklists(for: memorial.id)
        }
        .alert("Thêm việc cần làm", isPresented: $showAddChecklist) {
            TextField("Nhập nội dung", text: $newChecklistText)
            Button("Huỷ", role: .cancel) { newChecklistText = "" }
            Button("Thêm") {
                if !newChecklistText.isEmpty {
                    viewModel.addChecklistItem(memorialId: memorial.id, text: newChecklistText)
                    newChecklistText = ""
                }
            }
        }
        .alert("Xoá ngày giỗ", isPresented: $showDeleteAlert) {
            Button("Huỷ", role: .cancel) { }
            Button("Xoá", role: .destructive) {
                viewModel.deleteMemorial(memorial)
                dismiss()
            }
        } message: {
            Text("Bạn chắc chắn muốn xoá ngày giỗ này?")
        }
        .sheet(isPresented: $showEditSheet) {
            EditMemorialSheet(memorial: memorial, viewModel: viewModel)
        }
        .sheet(isPresented: $showMemberDetail) {
            if let member = viewModel.member(byId: memorial.memberId) {
                NavigationStack {
                    MemberDetailScreen(viewModel: viewModel, member: member)
                }
            }
        }
    }

    private func shareMemorial() {
        let solar = viewModel.solarDateForMemorial(memorial)
        let text = """
        Giỗ \(memorial.relation) \(memorial.memberName)
        Âm lịch: \(memorial.lunarDay)/\(memorial.lunarMonth)
        Dương lịch: \(solar.day)/\(solar.month)/\(solar.year)
        Còn \(viewModel.daysUntilMemorial(memorial)) ngày
        """
        let av = UIActivityViewController(activityItems: [text], applicationActivities: nil)
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let root = windowScene.windows.first?.rootViewController {
            root.present(av, animated: true)
        }
    }
}

// ══════════════════════════════════════════
// SECTION LABEL (reusable)
// ══════════════════════════════════════════

private struct SectionLabel: View {
    let icon: String
    let text: String

    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundColor(PrimaryRed)
            Text(text)
                .font(.system(size: 12, weight: .bold))
                .foregroundColor(PrimaryRed)
                .tracking(0.5)
        }
        .padding(.top, 16)
        .padding(.bottom, 10)
    }
}

// ══════════════════════════════════════════
// MEMORIAL HERO
// ══════════════════════════════════════════

private struct MemorialHero: View {
    let memorial: MemorialDayEntity
    let daysUntil: Int
    let deathYear: Int?
    let onBack: () -> Void
    let onShare: () -> Void
    let onMore: () -> Void

    private var ordinalCount: Int {
        guard let dy = deathYear else { return 0 }
        return Calendar.current.component(.year, from: Date()) - dy
    }

    var body: some View {
        ZStack(alignment: .topTrailing) {
            Circle()
                .fill(RadialGradient(colors: [.white.opacity(0.08), .clear], center: .center, startRadius: 0, endRadius: 100))
                .frame(width: 180, height: 180)
                .offset(x: 60, y: -30)

            VStack(alignment: .leading, spacing: 0) {
                // Nav
                HStack {
                    Button(action: onBack) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(.white)
                            .frame(width: 40, height: 40)
                            .background(.white.opacity(0.15))
                            .clipShape(Circle())
                    }
                    Spacer()
                    HStack(spacing: 8) {
                        Button(action: onShare) {
                            Image(systemName: "square.and.arrow.up")
                                .font(.system(size: 16))
                                .foregroundColor(.white)
                                .frame(width: 40, height: 40)
                                .background(.white.opacity(0.15))
                                .clipShape(Circle())
                        }
                        Button(action: onMore) {
                            Image(systemName: "ellipsis")
                                .font(.system(size: 16))
                                .foregroundColor(.white)
                                .frame(width: 40, height: 40)
                                .background(.white.opacity(0.15))
                                .clipShape(Circle())
                        }
                    }
                }
                .padding(.bottom, 16)

                Text("Giỗ \(memorial.relation) \(memorial.memberName)")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(.white)

                Text("Ngày giỗ thường niên\(ordinalCount > 0 ? " — Lần thứ \(ordinalCount)" : "")")
                    .font(.system(size: 13))
                    .foregroundColor(.white.opacity(0.75))
                    .padding(.top, 4)

                // Countdown
                HStack(spacing: 8) {
                    Text("\(daysUntil)")
                        .font(.system(size: 24, weight: .heavy))
                        .foregroundColor(.white)
                    VStack(alignment: .leading, spacing: 0) {
                        Text("ngày nữa đến")
                            .font(.system(size: 12))
                            .foregroundColor(.white.opacity(0.8))
                        Text("ngày giỗ")
                            .font(.system(size: 12))
                            .foregroundColor(.white.opacity(0.8))
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(.white.opacity(0.15))
                .clipShape(RoundedRectangle(cornerRadius: 16))
                .padding(.top, 12)
            }
            .padding(16)
        }
        .background(
            LinearGradient(
                colors: [Color(hex: "E65100"), Color(hex: "FF6F00"), Color(hex: "FF8F00")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
    }
}

// ══════════════════════════════════════════
// DATE CARDS ROW
// ══════════════════════════════════════════

private struct DateCardsRow: View {
    let memorial: MemorialDayEntity
    let viewModel: FamilyTreeViewModel

    var body: some View {
        let solar = viewModel.solarDateForMemorial(memorial)
        HStack(spacing: 10) {
            // Lunar card
            VStack(spacing: 4) {
                Text("🌙")
                    .font(.system(size: 22))
                Text("\(memorial.lunarDay)")
                    .font(.system(size: 22, weight: .heavy))
                    .foregroundColor(TextMain)
                Text("Tháng \(memorial.lunarMonth)")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(TextDim)
                Text("ÂM LỊCH")
                    .font(.system(size: 9, weight: .semibold))
                    .foregroundColor(TextDim)
                    .tracking(0.5)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(
                LinearGradient(colors: [SurfaceContainerHigh, PrimaryContainer], startPoint: .topLeading, endPoint: .bottomTrailing)
            )
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(PrimaryContainer, lineWidth: 1))

            // Solar card
            VStack(spacing: 4) {
                Text("☀️")
                    .font(.system(size: 22))
                Text("\(solar.day)")
                    .font(.system(size: 22, weight: .heavy))
                    .foregroundColor(TextMain)
                Text("Tháng \(solar.month), \(String(solar.year))")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(TextDim)
                Text("DƯƠNG LỊCH")
                    .font(.system(size: 9, weight: .semibold))
                    .foregroundColor(TextDim)
                    .tracking(0.5)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))
        }
        .padding(.bottom, 12)
    }
}

// ══════════════════════════════════════════
// PERSON CARD
// ══════════════════════════════════════════

private struct PersonCard: View {
    let member: FamilyMemberEntity
    let viewModel: FamilyTreeViewModel
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 14) {
                Circle()
                    .fill(LinearGradient(colors: [GoldAccent, Color(hex: "B8860B")], startPoint: .topLeading, endPoint: .bottomTrailing))
                    .frame(width: 52, height: 52)
                    .overlay(
                        Text(viewModel.memberInitials(member))
                            .font(.system(size: 20, weight: .bold, design: .serif))
                            .foregroundColor(.white)
                    )

                VStack(alignment: .leading, spacing: 2) {
                    Text(member.name)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(TextMain)
                    HStack(spacing: 4) {
                        if let by = member.birthYear {
                            if let dy = member.deathYear {
                                Text("\(by) — \(dy)")
                            } else {
                                Text("\(by)")
                            }
                        }
                        Text("• Thế hệ \(member.generation) • \(member.role)")
                    }
                    .font(.system(size: 12))
                    .foregroundColor(TextDim)
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.system(size: 14))
                    .foregroundColor(OutlineVariant)
            }
            .padding(16)
            .background(SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))
            .padding(.bottom, 12)
        }
        .buttonStyle(.plain)
    }
}

// ══════════════════════════════════════════
// OFFERINGS GRID
// ══════════════════════════════════════════

private struct OfferingsGrid: View {
    private let offerings: [(emoji: String, name: String)] = [
        ("🍚", "Cơm"), ("🐔", "Gà luộc"), ("🍲", "Canh"), ("🍖", "Thịt"),
        ("🍌", "Trái cây"), ("🪷", "Hoa"), ("🪔", "Nhang đèn"), ("🍵", "Trà rượu")
    ]

    private let columns = Array(repeating: GridItem(.flexible(), spacing: 8), count: 4)

    var body: some View {
        LazyVGrid(columns: columns, spacing: 8) {
            ForEach(offerings, id: \.name) { item in
                VStack(spacing: 4) {
                    Text(item.emoji)
                        .font(.system(size: 26))
                    Text(item.name)
                        .font(.system(size: 10, weight: .medium))
                        .foregroundColor(TextSub)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(SurfaceContainer)
                .clipShape(RoundedRectangle(cornerRadius: 14))
                .overlay(RoundedRectangle(cornerRadius: 14).stroke(OutlineVariant, lineWidth: 1))
            }
        }
        .padding(.bottom, 12)
    }
}

// ══════════════════════════════════════════
// CHECKLIST SECTION
// ══════════════════════════════════════════

private struct ChecklistSection: View {
    let items: [MemorialChecklistEntity]
    let onToggle: (MemorialChecklistEntity) -> Void
    let onDelete: (MemorialChecklistEntity) -> Void
    let onAdd: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            ForEach(items, id: \.id) { item in
                HStack(spacing: 12) {
                    // Checkbox
                    Button { onToggle(item) } label: {
                        ZStack {
                            RoundedRectangle(cornerRadius: 6)
                                .stroke(item.isDone ? Color(hex: "4CAF50") : TextDim, lineWidth: 2)
                                .frame(width: 22, height: 22)
                            if item.isDone {
                                RoundedRectangle(cornerRadius: 6)
                                    .fill(Color(hex: "4CAF50"))
                                    .frame(width: 22, height: 22)
                                Image(systemName: "checkmark")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(.white)
                            }
                        }
                    }

                    Text(item.text)
                        .font(.system(size: 14))
                        .foregroundColor(item.isDone ? TextDim : TextMain)
                        .strikethrough(item.isDone)

                    Spacer()
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 14)
                .contextMenu {
                    Button(role: .destructive) { onDelete(item) } label: {
                        Label("Xoá", systemImage: "trash")
                    }
                }

                if item.id != items.last?.id {
                    Divider().padding(.leading, 50)
                }
            }

            // Add button
            Button(action: onAdd) {
                HStack(spacing: 12) {
                    Image(systemName: "plus.circle.fill")
                        .font(.system(size: 20))
                        .foregroundColor(PrimaryRed)
                    Text("Thêm việc cần làm")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(PrimaryRed)
                    Spacer()
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 14)
            }
        }
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))
        .padding(.bottom, 12)
    }
}

// ══════════════════════════════════════════
// PRAYER LINK CARD
// ══════════════════════════════════════════

private struct PrayerLinkCard: View {
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "book.fill")
                .font(.system(size: 20))
                .foregroundColor(.white)
                .frame(width: 40, height: 40)
                .background(LinearGradient(colors: [PrimaryRed, DeepRed], startPoint: .topLeading, endPoint: .bottomTrailing))
                .clipShape(RoundedRectangle(cornerRadius: 12))

            VStack(alignment: .leading, spacing: 1) {
                Text("Văn khấn cúng giỗ")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(TextMain)
                Text("Bài khấn đầy đủ cho ngày giỗ")
                    .font(.system(size: 11))
                    .foregroundColor(TextDim)
            }

            Spacer()

            Image(systemName: "chevron.right")
                .font(.system(size: 14))
                .foregroundColor(OutlineVariant)
        }
        .padding(16)
        .background(
            LinearGradient(colors: [SurfaceContainerHigh, PrimaryContainer], startPoint: .topLeading, endPoint: .bottomTrailing)
        )
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))
        .padding(.bottom, 12)
    }
}

// ══════════════════════════════════════════
// REMINDER TOGGLE CARD
// ══════════════════════════════════════════

private struct ReminderToggleCard: View {
    let icon: String
    let iconBg: Color
    let iconFg: Color
    let title: String
    @Binding var isOn: Bool

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 18))
                .foregroundColor(iconFg)
                .frame(width: 36, height: 36)
                .background(iconBg)
                .clipShape(RoundedRectangle(cornerRadius: 10))

            VStack(alignment: .leading, spacing: 1) {
                Text(title)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(TextMain)
            }

            Spacer()

            Toggle("", isOn: $isOn)
                .labelsHidden()
                .tint(Color(hex: "4CAF50"))
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(OutlineVariant, lineWidth: 1))
        .padding(.bottom, 8)
    }
}

// ══════════════════════════════════════════
// QUOTE SECTION
// ══════════════════════════════════════════

private struct QuoteSection: View {
    var body: some View {
        Text("\u{201C}Cây có cội, nước có nguồn.\nCon người có tổ có tông.\u{201D}")
            .font(.system(size: 14, design: .serif))
            .italic()
            .foregroundColor(Color(hex: "5D4037"))
            .multilineTextAlignment(.center)
            .lineSpacing(4)
            .frame(maxWidth: .infinity)
            .padding(16)
            .background(
                LinearGradient(colors: [Color(hex: "FFF8E1"), Color(hex: "FFECB3")], startPoint: .topLeading, endPoint: .bottomTrailing)
            )
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "FFD54F"), lineWidth: 1))
    }
}

// ══════════════════════════════════════════
// EDIT MEMORIAL SHEET
// ══════════════════════════════════════════

struct EditMemorialSheet: View {
    @Environment(\.dismiss) private var dismiss
    let memorial: MemorialDayEntity
    @ObservedObject var viewModel: FamilyTreeViewModel

    @State private var relation: String = ""
    @State private var lunarDay: Int = 1
    @State private var lunarMonth: Int = 1
    @State private var note: String = ""
    @State private var remindBefore3Days = true
    @State private var remindBefore1Day = true

    var body: some View {
        NavigationStack {
            Form {
                Section("Thông tin cơ bản") {
                    TextField("Quan hệ (Ông nội, Bà ngoại...)", text: $relation)
                    Stepper("Ngày Âm lịch: \(lunarDay)", value: $lunarDay, in: 1...30)
                    Stepper("Tháng Âm lịch: \(lunarMonth)", value: $lunarMonth, in: 1...12)
                }

                Section("Nhắc nhở") {
                    Toggle("Nhắc trước 3 ngày", isOn: $remindBefore3Days)
                    Toggle("Nhắc ngay ngày giỗ", isOn: $remindBefore1Day)
                }

                Section("Ghi chú") {
                    TextEditor(text: $note)
                        .frame(minHeight: 80)
                }
            }
            .navigationTitle("Sửa ngày giỗ")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Huỷ") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Lưu") {
                        memorial.relation = relation
                        memorial.lunarDay = lunarDay
                        memorial.lunarMonth = lunarMonth
                        memorial.note = note.isEmpty ? nil : note
                        memorial.remindBefore3Days = remindBefore3Days
                        memorial.remindBefore1Day = remindBefore1Day
                        viewModel.updateMemorial(memorial)
                        dismiss()
                    }
                }
            }
            .onAppear {
                relation = memorial.relation
                lunarDay = memorial.lunarDay
                lunarMonth = memorial.lunarMonth
                note = memorial.note ?? ""
                remindBefore3Days = memorial.remindBefore3Days
                remindBefore1Day = memorial.remindBefore1Day
            }
        }
    }
}
