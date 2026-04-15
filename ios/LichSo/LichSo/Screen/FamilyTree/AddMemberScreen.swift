import SwiftUI
import SwiftData
import PhotosUI

// ═══════════════════════════════════════════
// Add Member Screen — Matches screen-add-member.html
// Full form: avatar, name, gender, relation, lunar birth date,
// can chi auto-calc, occupation, hometown, deceased toggle, notes
// Uses REAL data from SwiftData via FamilyTreeViewModel
// ═══════════════════════════════════════════

// MARK: - Theme aliases

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
private var OutlineColor: Color { LSTheme.outline }
private var OutlineVar: Color { LSTheme.outlineVariant }

// MARK: - Main Screen

struct AddMemberScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @ObservedObject var viewModel: FamilyTreeViewModel

    /// Optional: pre-select a member to connect relation to
    var preselectedMember: FamilyMemberEntity? = nil

    // ── Form state ──
    @State private var name = ""
    @State private var gender = "MALE"
    @State private var role = ""
    @State private var generation = 1

    // Lunar birth date
    @State private var birthDay = 15
    @State private var birthMonth = 1
    @State private var birthYear = 1985
    @State private var hasBirthDate = false

    // Can Chi (auto-calculated)
    @State private var canChiText = ""
    @State private var nguHanhText = ""
    @State private var menhText = ""

    // Other fields
    @State private var occupation = ""
    @State private var hometown = ""
    @State private var isDeceased = false
    @State private var deathDay = 1
    @State private var deathMonth = 1
    @State private var deathYear = 2020
    @State private var note = ""

    // Relation
    @State private var selectedRelation: String = ""
    @State private var relatedMember: FamilyMemberEntity? = nil
    @State private var secondParent: FamilyMemberEntity? = nil  // for child: pick both father & mother
    @State private var showPickMember = false
    @State private var pickingSecondParent = false  // true when picking the 2nd parent

    // Avatar
    @State private var avatarImage: UIImage? = nil
    @State private var showPhotoPicker = false
    @State private var selectedPhotoItem: PhotosPickerItem? = nil

    // Validation
    @State private var showValidationAlert = false

    private let relationTypes = [
        "Cha", "Mẹ", "Con trai", "Con gái",
        "Vợ", "Chồng"
    ]

    var body: some View {
        VStack(spacing: 0) {
            // ═══ HEADER ═══
            headerBar

            // ═══ FORM CONTENT ═══
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 16) {
                    // Avatar upload
                    avatarSection

                    // Họ và tên
                    fieldGroup(label: "HỌ VÀ TÊN", required: true) {
                        TextField("Nhập họ và tên đầy đủ", text: $name)
                            .font(.system(size: 15))
                            .foregroundColor(TextMain)
                            .padding(14)
                            .background(SurfaceContainer)
                            .clipShape(RoundedRectangle(cornerRadius: 14))
                            .overlay(
                                RoundedRectangle(cornerRadius: 14)
                                    .stroke(OutlineVar, lineWidth: 1.5)
                            )
                    }

                    // Giới tính
                    fieldGroup(label: "GIỚI TÍNH", required: true) {
                        genderToggle
                    }

                    // Mối quan hệ
                    fieldGroup(label: "MỐI QUAN HỆ", required: true) {
                        relationCard
                    }

                    // Thế hệ — auto nếu có mối quan hệ, manual nếu chưa có
                    fieldGroup(label: "THẾ HỆ") {
                        generationSection
                    }

                    // Ngày sinh (Âm lịch)
                    fieldGroup(label: "NGÀY SINH (ÂM LỊCH)") {
                        birthDateSection
                    }

                    // Nghề nghiệp
                    fieldGroup(label: "NGHỀ NGHIỆP") {
                        TextField("Ví dụ: Giáo viên, Kỹ sư...", text: $occupation)
                            .font(.system(size: 15))
                            .foregroundColor(TextMain)
                            .padding(14)
                            .background(SurfaceContainer)
                            .clipShape(RoundedRectangle(cornerRadius: 14))
                            .overlay(
                                RoundedRectangle(cornerRadius: 14)
                                    .stroke(OutlineVar, lineWidth: 1.5)
                            )
                    }

                    // Nơi ở hiện tại
                    fieldGroup(label: "NƠI Ở HIỆN TẠI") {
                        TextField("Tỉnh / Thành phố", text: $hometown)
                            .font(.system(size: 15))
                            .foregroundColor(TextMain)
                            .padding(14)
                            .background(SurfaceContainer)
                            .clipShape(RoundedRectangle(cornerRadius: 14))
                            .overlay(
                                RoundedRectangle(cornerRadius: 14)
                                    .stroke(OutlineVar, lineWidth: 1.5)
                            )
                    }

                    // Đã mất
                    fieldGroup(label: "ĐÃ MẤT") {
                        deceasedSection
                    }

                    // Ghi chú
                    fieldGroup(label: "GHI CHÚ") {
                        TextEditor(text: $note)
                            .font(.system(size: 14))
                            .foregroundColor(TextMain)
                            .frame(minHeight: 80)
                            .padding(10)
                            .scrollContentBackground(.hidden)
                            .background(SurfaceContainer)
                            .clipShape(RoundedRectangle(cornerRadius: 14))
                            .overlay(
                                RoundedRectangle(cornerRadius: 14)
                                    .stroke(OutlineVar, lineWidth: 1.5)
                            )
                            .overlay(alignment: .topLeading) {
                                if note.isEmpty {
                                    Text("Thông tin thêm về thành viên...")
                                        .font(.system(size: 14))
                                        .foregroundColor(OutlineColor)
                                        .padding(.horizontal, 14)
                                        .padding(.vertical, 18)
                                        .allowsHitTesting(false)
                                }
                            }
                    }

                    Spacer().frame(height: 20)
                }
                .padding(16)
            }
        }
        .background(SurfaceBg)
        .onAppear {
            viewModel.setup(context: modelContext)
            if let pre = preselectedMember {
                relatedMember = pre
            }
            recalculateCanChi()
        }
        .onChange(of: birthYear) { _, _ in recalculateCanChi() }
        .sheet(isPresented: $showPickMember) {
            NavigationStack {
                PickMemberScreen(
                    viewModel: viewModel,
                    excludeMemberIds: excludedMemberIds,
                    allowMemberIds: pickingSecondParent ? allowedSecondParentIds : nil
                ) { member in
                    if pickingSecondParent {
                        secondParent = member
                        pickingSecondParent = false
                    } else {
                        relatedMember = member
                        // Auto-suggest second parent when adding a child
                        if selectedRelation == "Con trai" || selectedRelation == "Con gái" {
                            autoSuggestSecondParent(for: member)
                        }
                    }
                    showPickMember = false
                }
            }
        }
        .onChange(of: selectedPhotoItem) { _, newItem in
            Task {
                if let data = try? await newItem?.loadTransferable(type: Data.self),
                   let img = UIImage(data: data) {
                    avatarImage = img
                }
            }
        }
        .alert("Thiếu thông tin", isPresented: $showValidationAlert) {
            Button("Đã hiểu", role: .cancel) {}
        } message: {
            Text("Vui lòng nhập họ tên thành viên.")
        }
    }

    // MARK: - Header Bar

    private var headerBar: some View {
        HStack {
            HStack(spacing: 8) {
                Button {
                    dismiss()
                } label: {
                    Image(systemName: "xmark")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(TextMain)
                        .frame(width: 36, height: 36)
                        .background(SurfaceContainer)
                        .clipShape(Circle())
                }

                Text("Thêm thành viên")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(TextMain)
            }

            Spacer()

            Button {
                saveMember()
            } label: {
                Text("Lưu")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 8)
                    .background(
                        RoundedRectangle(cornerRadius: 20)
                            .fill(name.isEmpty ? OutlineColor : PrimaryRed)
                    )
            }
            .disabled(name.isEmpty)
        }
        .padding(.horizontal, 16)
        .padding(.top, 14)
        .padding(.bottom, 14)
        .overlay(alignment: .bottom) {
            Rectangle()
                .fill(OutlineVar)
                .frame(height: 1)
        }
        .background(SurfaceBg)
    }

    // MARK: - Avatar Section

    private var avatarSection: some View {
        VStack(spacing: 8) {
            PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
                ZStack {
                    if let img = avatarImage {
                        Image(uiImage: img)
                            .resizable()
                            .scaledToFill()
                            .frame(width: 96, height: 96)
                            .clipShape(Circle())
                    } else {
                        Circle()
                            .fill(SurfaceContainerHigh)
                            .frame(width: 96, height: 96)
                            .overlay(
                                Circle()
                                    .strokeBorder(style: StrokeStyle(lineWidth: 3, dash: [6, 4]))
                                    .foregroundColor(OutlineVar)
                            )
                            .overlay {
                                Image(systemName: "person.badge.plus")
                                    .font(.system(size: 36))
                                    .foregroundColor(OutlineColor)
                            }
                    }

                    // Camera badge
                    VStack {
                        Spacer()
                        HStack {
                            Spacer()
                            ZStack {
                                Circle()
                                    .fill(PrimaryRed)
                                    .frame(width: 28, height: 28)
                                Image(systemName: "camera.fill")
                                    .font(.system(size: 12))
                                    .foregroundColor(.white)
                            }
                            .overlay(
                                Circle()
                                    .stroke(SurfaceBg, lineWidth: 2)
                            )
                        }
                    }
                    .frame(width: 96, height: 96)
                }
            }

            Text("Chạm để chọn ảnh đại diện")
                .font(.system(size: 12))
                .foregroundColor(OutlineColor)
        }
        .frame(maxWidth: .infinity)
        .padding(.bottom, 8)
    }

    // MARK: - Gender Toggle

    private var genderToggle: some View {
        HStack(spacing: 8) {
            genderButton(value: "MALE", emoji: "👨", label: "Nam")
            genderButton(value: "FEMALE", emoji: "👩", label: "Nữ")
        }
    }

    private func genderButton(value: String, emoji: String, label: String) -> some View {
        Button {
            gender = value
        } label: {
            VStack(spacing: 4) {
                Text(emoji)
                    .font(.system(size: 24))
                Text(label)
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(TextMain)
            }
            .frame(maxWidth: .infinity)
            .padding(12)
            .background(gender == value ? PrimaryContainer : SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(gender == value ? PrimaryRed : OutlineVar, lineWidth: 1.5)
            )
        }
        .buttonStyle(.plain)
    }

    // MARK: - Relation Card

    /// Filtered relation types based on gender context
    private var availableRelations: [String] {
        // "Cha", "Mẹ" = người mới là cha/mẹ CỦA thành viên được chọn
        // "Con trai", "Con gái" = người mới là con CỦA thành viên được chọn
        // "Vợ", "Chồng" = người mới là vợ/chồng CỦA thành viên được chọn
        return relationTypes
    }

    private var relationCard: some View {
        VStack(spacing: 10) {
            // Relation type picker
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    ForEach(availableRelations, id: \.self) { rel in
                        Button {
                            selectedRelation = rel
                            // Reset second parent when changing relation
                            secondParent = nil
                            // Auto-suggest second parent when switching to child relation
                            if (rel == "Con trai" || rel == "Con gái"), let related = relatedMember {
                                autoSuggestSecondParent(for: related)
                            }
                        } label: {
                            Text(relationLabel(rel))
                                .font(.system(size: 12, weight: .medium))
                                .padding(.horizontal, 12)
                                .padding(.vertical, 7)
                                .background(selectedRelation == rel ? PrimaryRed : SurfaceContainer)
                                .foregroundColor(selectedRelation == rel ? .white : TextMain)
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(selectedRelation == rel ? PrimaryRed : OutlineVar, lineWidth: 1)
                                )
                        }
                        .buttonStyle(.plain)
                    }
                }
            }

            // Primary member connection card
            Button {
                pickingSecondParent = false
                showPickMember = true
            } label: {
                memberConnectionCard(
                    member: relatedMember,
                    placeholder: "Chọn thành viên liên kết",
                    subtitle: relatedMember != nil
                        ? "Là \(relationLabel(selectedRelation).lowercased()) của \(relatedMember!.name)"
                        : "Liên kết với thành viên trong gia phả",
                    buttonText: relatedMember != nil ? "Đổi" : "Kết nối"
                )
            }
            .buttonStyle(.plain)

            // Second parent picker — only for Con trai / Con gái
            if (selectedRelation == "Con trai" || selectedRelation == "Con gái"),
               let firstParent = relatedMember {
                let spouseIds = allSpouseIds(of: firstParent)
                if !spouseIds.isEmpty {
                    // Show second parent card
                    Button {
                        pickingSecondParent = true
                        showPickMember = true
                    } label: {
                        memberConnectionCard(
                            member: secondParent,
                            placeholder: firstParent.gender == "MALE" ? "Chọn mẹ" : "Chọn cha",
                            subtitle: secondParent != nil
                                ? "\(secondParent!.gender == "MALE" ? "Cha" : "Mẹ"): \(secondParent!.name)"
                                : "Chọn \(firstParent.gender == "MALE" ? "mẹ" : "cha") để vẽ đúng cây phả",
                            buttonText: secondParent != nil ? "Đổi" : "Chọn",
                            icon: "person.fill"
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }

    /// Human-readable relation label (for display context)
    private func relationLabel(_ rel: String) -> String {
        return rel
    }

    /// Card UI for member connection
    private func memberConnectionCard(
        member: FamilyMemberEntity?,
        placeholder: String,
        subtitle: String,
        buttonText: String,
        icon: String = "person.2.fill"
    ) -> some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(
                        LinearGradient(
                            colors: [PrimaryRed, DeepRed],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 44, height: 44)
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .foregroundColor(.white)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(member?.name ?? placeholder)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(TextMain)
                Text(subtitle)
                    .font(.system(size: 11))
                    .foregroundColor(OutlineColor)
            }

            Spacer()

            Text(buttonText)
                .font(.system(size: 10, weight: .semibold))
                .foregroundColor(.white)
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(PrimaryRed)
                .clipShape(RoundedRectangle(cornerRadius: 10))
        }
        .padding(16)
        .background(
            LinearGradient(
                colors: [Color(hex: "EFEBE9"), Color(hex: "D7CCC8")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(OutlineVar, lineWidth: 1)
        )
    }

    // MARK: - Generation Section

    /// Show auto-calculated generation if relation is set, otherwise allow manual picker
    private var generationSection: some View {
        Group {
            if relatedMember != nil && !selectedRelation.isEmpty {
                // Auto-calculated — show read-only
                let gen = determineGeneration()
                HStack(spacing: 12) {
                    Image(systemName: "arrow.triangle.branch")
                        .font(.system(size: 18))
                        .foregroundColor(PrimaryRed)
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Thế hệ \(gen)")
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(TextMain)
                        Text("Tự động tính từ mối quan hệ")
                            .font(.system(size: 11))
                            .foregroundColor(OutlineColor)
                    }
                    Spacer()
                    Text("Tự động")
                        .font(.system(size: 9, weight: .semibold))
                        .foregroundColor(OutlineColor)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(SurfaceBg)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }
                .padding(14)
                .background(SurfaceContainer)
                .clipShape(RoundedRectangle(cornerRadius: 14))
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(OutlineVar, lineWidth: 1.5)
                )
            } else {
                // Manual picker
                Stepper("Thế hệ \(generation)", value: $generation, in: 1...20)
                    .font(.system(size: 15))
                    .foregroundColor(TextMain)
                    .padding(14)
                    .background(SurfaceContainer)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .overlay(
                        RoundedRectangle(cornerRadius: 14)
                            .stroke(OutlineVar, lineWidth: 1.5)
                    )
            }
        }
    }

    // MARK: - Helpers

    /// IDs to exclude from PickMember (self not applicable during add, but exclude already-related)
    private var excludedMemberIds: Set<String> {
        var ids: Set<String> = []
        if pickingSecondParent, let first = relatedMember {
            ids.insert(first.id)
            if let second = secondParent { ids.insert(second.id) }
        } else {
            if let related = relatedMember { ids.insert(related.id) }
            if let second = secondParent { ids.insert(second.id) }
        }
        return ids
    }

    /// When picking 2nd parent, only allow spouses of the first parent
    private var allowedSecondParentIds: Set<String>? {
        guard let firstParent = relatedMember else { return nil }
        let spouseIds = allSpouseIds(of: firstParent)
        guard !spouseIds.isEmpty else { return nil }
        return Set(spouseIds)
    }

    /// Get all spouse IDs for a member (bi-directional)
    private func allSpouseIds(of member: FamilyMemberEntity) -> [String] {
        var ids = member.spouseIds
            .split(separator: ",")
            .map { String($0).trimmingCharacters(in: .whitespaces) }
            .filter { !$0.isEmpty }

        // Also check reverse: any member who lists this member as spouse
        for m in viewModel.members {
            if m.id == member.id { continue }
            let theirSpouses = m.spouseIds
                .split(separator: ",")
                .map { String($0).trimmingCharacters(in: .whitespaces) }
            if theirSpouses.contains(member.id), !ids.contains(m.id) {
                ids.append(m.id)
            }
        }
        return ids
    }

    /// Auto-suggest second parent from first parent's spouses
    private func autoSuggestSecondParent(for firstParent: FamilyMemberEntity) {
        let spouseIds = allSpouseIds(of: firstParent)
        if spouseIds.count == 1, let spouse = viewModel.member(byId: spouseIds[0]) {
            // Only one spouse → auto-select
            secondParent = spouse
        } else {
            secondParent = nil
        }
    }

    // MARK: - Birth Date Section

    private var birthDateSection: some View {
        VStack(spacing: 8) {
            // Toggle to enable birth date
            if !hasBirthDate {
                Button {
                    withAnimation { hasBirthDate = true }
                } label: {
                    HStack(spacing: 10) {
                        Image(systemName: "calendar.badge.plus")
                            .font(.system(size: 18))
                            .foregroundColor(PrimaryRed)
                        Text("Nhấn để nhập ngày sinh âm lịch")
                            .font(.system(size: 14))
                            .foregroundColor(TextSub)
                        Spacer()
                    }
                    .padding(14)
                    .background(SurfaceContainer)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .overlay(
                        RoundedRectangle(cornerRadius: 14)
                            .stroke(OutlineVar, lineWidth: 1.5)
                    )
                }
                .buttonStyle(.plain)
            }

            if hasBirthDate {
                // Date selectors
                HStack(spacing: 8) {
                    datePickerColumn(label: "NGÀY", value: $birthDay, range: 1...30)
                    datePickerColumn(label: "THÁNG", value: $birthMonth, range: 1...12)
                    yearPickerColumn(label: "NĂM", value: $birthYear)
                }

                // Can Chi auto card
                if !canChiText.isEmpty {
                    canChiCard
                }

                // Button to remove birth date
                Button {
                    withAnimation { hasBirthDate = false }
                } label: {
                    HStack {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 14))
                        Text("Xoá ngày sinh")
                            .font(.system(size: 12, weight: .medium))
                    }
                    .foregroundColor(TextDim)
                }
                .buttonStyle(.plain)
            }
        }
    }

    private func datePickerColumn(label: String, value: Binding<Int>, range: ClosedRange<Int>) -> some View {
        VStack(spacing: 4) {
            Text(label)
                .font(.system(size: 9, weight: .semibold))
                .foregroundColor(OutlineColor)
                .textCase(.uppercase)
                .tracking(0.3)

            Picker(label, selection: value) {
                ForEach(Array(range), id: \.self) { v in
                    Text(String(format: "%02d", v)).tag(v)
                }
            }
            .pickerStyle(.wheel)
            .frame(height: 90)
            .clipShape(RoundedRectangle(cornerRadius: 14))
        }
        .frame(maxWidth: .infinity)
        .padding(8)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(OutlineVar, lineWidth: 1.5)
        )
    }

    private func yearPickerColumn(label: String, value: Binding<Int>) -> some View {
        VStack(spacing: 4) {
            Text(label)
                .font(.system(size: 9, weight: .semibold))
                .foregroundColor(OutlineColor)
                .textCase(.uppercase)
                .tracking(0.3)

            Picker(label, selection: value) {
                ForEach(1900...2100, id: \.self) { y in
                    Text(String(y)).tag(y)
                }
            }
            .pickerStyle(.wheel)
            .frame(height: 90)
            .clipShape(RoundedRectangle(cornerRadius: 14))
        }
        .frame(maxWidth: .infinity)
        .padding(8)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(OutlineVar, lineWidth: 1.5)
        )
    }

    // MARK: - Can Chi Card

    private var canChiCard: some View {
        HStack(spacing: 12) {
            ZStack {
                RoundedRectangle(cornerRadius: 10)
                    .fill(
                        LinearGradient(
                            colors: [GoldAccent, Color(hex: "B8860B")],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 36, height: 36)
                Image(systemName: "sparkles")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(.white)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(canChiText)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(PrimaryRed)
                Text("\(nguHanhText) — Mệnh \(menhText)")
                    .font(.system(size: 11))
                    .foregroundColor(OutlineColor)
            }

            Spacer()

            Text("Tự động")
                .font(.system(size: 9, weight: .semibold))
                .foregroundColor(OutlineColor)
                .padding(.horizontal, 8)
                .padding(.vertical, 3)
                .background(SurfaceBg)
                .clipShape(RoundedRectangle(cornerRadius: 8))
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(
            LinearGradient(
                colors: [SurfaceContainerHigh, PrimaryContainer],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(OutlineVar, lineWidth: 1)
        )
    }

    // MARK: - Deceased Section

    private var deceasedSection: some View {
        VStack(spacing: 10) {
            // Toggle
            HStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Người đã qua đời")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(TextMain)
                    Text("Bật để nhập thông tin ngày mất")
                        .font(.system(size: 11))
                        .foregroundColor(OutlineColor)
                }
                Spacer()
                Toggle("", isOn: $isDeceased)
                    .labelsHidden()
                    .tint(Color(hex: "4CAF50"))
            }
            .padding(14)
            .background(SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(OutlineVar, lineWidth: 1.5)
            )

            // Death date fields (shown when deceased)
            if isDeceased {
                VStack(alignment: .leading, spacing: 8) {
                    Text("NGÀY MẤT (ÂM LỊCH)")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(PrimaryRed)
                        .tracking(0.5)

                    HStack(spacing: 8) {
                        datePickerColumn(label: "NGÀY", value: $deathDay, range: 1...30)
                        datePickerColumn(label: "THÁNG", value: $deathMonth, range: 1...12)
                        yearPickerColumn(label: "NĂM", value: $deathYear)
                    }
                }
                .padding(12)
                .background(SurfaceContainerHigh.opacity(0.5))
                .clipShape(RoundedRectangle(cornerRadius: 14))
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .animation(.easeInOut(duration: 0.25), value: isDeceased)
    }

    // MARK: - Field Group Helper

    @ViewBuilder
    private func fieldGroup<Content: View>(label: String, required: Bool = false, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 4) {
                Text(label)
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(PrimaryRed)
                    .tracking(0.5)
                if required {
                    Text("*")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(Color(hex: "C62828"))
                }
            }
            content()
        }
    }

    // MARK: - Can Chi Calculation

    private func recalculateCanChi() {
        guard hasBirthDate || birthYear > 1900 else {
            canChiText = ""
            nguHanhText = ""
            menhText = ""
            return
        }

        let lunarYear = birthYear

        // Year Can Chi
        canChiText = CanChiCalculator.getYearCanChi(lunarYear: lunarYear)

        // Ngũ Hành Nạp Âm
        nguHanhText = getNguHanhNapAm(lunarYear: lunarYear)

        // Mệnh
        menhText = getMenhFromNguHanh(nguHanhText)
    }

    // ── Ngũ Hành Nạp Âm — 60-year cycle ──

    private func getNguHanhNapAm(lunarYear: Int) -> String {
        let napAmList = [
            "Hải Trung Kim", "Hải Trung Kim",
            "Lư Trung Hỏa", "Lư Trung Hỏa",
            "Đại Lâm Mộc", "Đại Lâm Mộc",
            "Lộ Bàng Thổ", "Lộ Bàng Thổ",
            "Kiếm Phong Kim", "Kiếm Phong Kim",
            "Sơn Đầu Hỏa", "Sơn Đầu Hỏa",
            "Giản Hạ Thủy", "Giản Hạ Thủy",
            "Thành Đầu Thổ", "Thành Đầu Thổ",
            "Bạch Lạp Kim", "Bạch Lạp Kim",
            "Dương Liễu Mộc", "Dương Liễu Mộc",
            "Tuyền Trung Thủy", "Tuyền Trung Thủy",
            "Ốc Thượng Thổ", "Ốc Thượng Thổ",
            "Tích Lịch Hỏa", "Tích Lịch Hỏa",
            "Tùng Bách Mộc", "Tùng Bách Mộc",
            "Trường Lưu Thủy", "Trường Lưu Thủy",
            "Sa Trung Kim", "Sa Trung Kim",
            "Sơn Hạ Hỏa", "Sơn Hạ Hỏa",
            "Bình Địa Mộc", "Bình Địa Mộc",
            "Bích Thượng Thổ", "Bích Thượng Thổ",
            "Kim Bạch Kim", "Kim Bạch Kim",
            "Phúc Đăng Hỏa", "Phúc Đăng Hỏa",
            "Thiên Hà Thủy", "Thiên Hà Thủy",
            "Đại Dịch Thổ", "Đại Dịch Thổ",
            "Thoa Xuyến Kim", "Thoa Xuyến Kim",
            "Tang Đố Mộc", "Tang Đố Mộc",
            "Đại Khê Thủy", "Đại Khê Thủy",
            "Sa Trung Thổ", "Sa Trung Thổ",
            "Thiên Thượng Hỏa", "Thiên Thượng Hỏa",
            "Thạch Lựu Mộc", "Thạch Lựu Mộc",
            "Đại Hải Thủy", "Đại Hải Thủy"
        ]
        let index = ((lunarYear - 4) % 60 + 60) % 60
        return index < napAmList.count ? napAmList[index] : "Không rõ"
    }

    private func getMenhFromNguHanh(_ nguHanh: String) -> String {
        if nguHanh.contains("Kim") { return "Kim" }
        if nguHanh.contains("Mộc") { return "Mộc" }
        if nguHanh.contains("Thủy") { return "Thủy" }
        if nguHanh.contains("Hỏa") { return "Hỏa" }
        if nguHanh.contains("Thổ") { return "Thổ" }
        return "Không rõ"
    }

    // MARK: - Zodiac helpers

    private func getZodiacEmoji(lunarYear: Int) -> String {
        let emojis = ["🐭", "🐮", "🐯", "🐱", "🐲", "🐍", "🐴", "🐐", "🐒", "🐔", "🐶", "🐷"]
        let index = ((lunarYear + 8) % 12 + 12) % 12
        return emojis[index]
    }

    private func getZodiacName(lunarYear: Int) -> String {
        let names = ["Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"]
        let index = ((lunarYear + 8) % 12 + 12) % 12
        return names[index]
    }

    // MARK: - Save

    private func saveMember() {
        guard !name.trimmingCharacters(in: .whitespaces).isEmpty else {
            showValidationAlert = true
            return
        }

        let trimmedName = name.trimmingCharacters(in: .whitespaces)
        let isSelf = selectedRelation == "Bản thân"
        let emoji = gender == "MALE" ? (isSelf ? "🧑" : "👨") : "👩"

        // Determine generation from relation
        let gen = determineGeneration()

        // Build lunar date string
        let birthDateLunar: String? = hasBirthDate
            ? String(format: "%02d/%02d/%04d", birthDay, birthMonth, birthYear)
            : nil

        let deathDateLunar: String? = isDeceased
            ? String(format: "%02d/%02d/%04d", deathDay, deathMonth, deathYear)
            : nil

        // Compute can chi / menh fields
        let yearBirth: Int? = hasBirthDate ? birthYear : nil
        let yearDeath: Int? = isDeceased ? deathYear : nil
        let canChi: String? = hasBirthDate ? canChiText : nil
        let menh: String? = hasBirthDate ? menhText : nil
        let menhDetail: String? = hasBirthDate ? nguHanhText : nil
        let zodiacEmoji: String? = hasBirthDate ? getZodiacEmoji(lunarYear: birthYear) : nil
        let zodiacName: String? = hasBirthDate ? getZodiacName(lunarYear: birthYear) : nil
        let menhName: String? = hasBirthDate ? menhText : nil

        // Determine role label
        let roleLabel = selectedRelation.isEmpty ? "Thành viên" : selectedRelation

        // Build parent/spouse IDs from relation
        var parentIds = ""
        var spouseIds = ""
        if let related = relatedMember {
            switch selectedRelation {
            case "Cha", "Mẹ":
                // New member IS a parent of the related member
                // We'll update the related member's parentIds after creation
                break
            case "Con trai", "Con gái":
                // New member is a child → related member is parent
                // Include both parents if second parent is selected
                var pids = [related.id]
                if let second = secondParent {
                    pids.append(second.id)
                }
                parentIds = pids.joined(separator: ",")
            case "Vợ", "Chồng":
                // Spouse link — bidirectional
                spouseIds = related.id
            default:
                break
            }
        }

        // Save avatar if present
        let memberId = UUID().uuidString
        var avatarPath: String? = nil
        if let img = avatarImage {
            avatarPath = saveAvatarImage(img, memberId: memberId)
        }

        let member = FamilyMemberEntity(
            id: memberId,
            name: trimmedName,
            role: roleLabel,
            gender: gender,
            generation: gen,
            birthYear: yearBirth,
            deathYear: yearDeath,
            birthDateLunar: birthDateLunar,
            deathDateLunar: deathDateLunar,
            canChi: canChi,
            menh: menh,
            zodiacEmoji: zodiacEmoji,
            menhDetail: menhDetail,
            zodiacName: zodiacName,
            menhName: menhName,
            hometown: hometown.isEmpty ? nil : hometown,
            occupation: occupation.isEmpty ? nil : occupation,
            isSelf: isSelf,
            isElder: gen <= 2,
            emoji: emoji,
            spouseIds: spouseIds,
            parentIds: parentIds,
            note: note.isEmpty ? nil : note,
            avatarPath: avatarPath
        )

        viewModel.addMember(member)

        // Update related member's connections
        if let related = relatedMember {
            switch selectedRelation {
            case "Cha", "Mẹ":
                // New member is parent of related member
                let existingParents = related.parentIds.isEmpty ? "" : related.parentIds + ","
                related.parentIds = existingParents + memberId
                viewModel.updateMember(related)
            case "Vợ", "Chồng":
                // Bidirectional spouse link
                let existingSpouses = related.spouseIds.isEmpty ? "" : related.spouseIds + ","
                related.spouseIds = existingSpouses + memberId
                viewModel.updateMember(related)
            case "Con trai", "Con gái":
                // parentIds already set on the new member, no update on related needed
                break
            default:
                break
            }
        }

        dismiss()
    }

    private func determineGeneration() -> Int {
        guard let related = relatedMember else { return generation }

        switch selectedRelation {
        case "Cha", "Mẹ":
            return max(1, related.generation - 1)
        case "Con trai", "Con gái":
            return related.generation + 1
        case "Vợ", "Chồng":
            return related.generation
        default:
            return generation
        }
    }

    private func saveAvatarImage(_ image: UIImage, memberId: String) -> String? {
        guard let data = image.jpegData(compressionQuality: 0.8) else { return nil }
        let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        let avatarDir = dir.appendingPathComponent("avatars", isDirectory: true)
        try? FileManager.default.createDirectory(at: avatarDir, withIntermediateDirectories: true)
        let filePath = avatarDir.appendingPathComponent("\(memberId).jpg")
        do {
            try data.write(to: filePath)
            return filePath.path
        } catch {
            #if DEBUG
            print("⚠️ Failed to save avatar: \(error)")
            #endif
            return nil
        }
    }
}

// MARK: - Preview

#Preview {
    NavigationStack {
        AddMemberScreen(viewModel: FamilyTreeViewModel())
    }
}
