import SwiftUI
import SwiftData
import PhotosUI

// ═══════════════════════════════════════════
// Edit Member Screen — Full form matching AddMemberScreen design
// Pre-fills all data from an existing FamilyMemberEntity
// avatar, name, gender, relation, lunar birth/death date,
// can chi auto-calc, occupation, hometown, deceased toggle, notes
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

struct EditMemberScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @ObservedObject var viewModel: FamilyTreeViewModel
    let member: FamilyMemberEntity

    // ── Form state ──
    @State private var name = ""
    @State private var gender = "MALE"
    @State private var selectedRelation = ""
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
    @State private var hasDeathDate = false
    @State private var note = ""

    // Relation
    @State private var relatedMember: FamilyMemberEntity? = nil
    @State private var secondParent: FamilyMemberEntity? = nil  // for child: pick both father & mother
    @State private var showPickMember = false
    @State private var pickingSecondParent = false  // true when picking the 2nd parent

    // Avatar
    @State private var avatarImage: UIImage? = nil
    @State private var selectedPhotoItem: PhotosPickerItem? = nil
    @State private var existingAvatarPath: String? = nil

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
                    // Avatar
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

                    // Vai trò / Mối quan hệ
                    fieldGroup(label: "VAI TRÒ / MỐI QUAN HỆ") {
                        relationSection
                    }

                    // Thế hệ
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
            populateFromMember()
        }
        .onChange(of: birthYear) { _, _ in recalculateCanChi() }
        .sheet(isPresented: $showPickMember) {
            NavigationStack {
                PickMemberScreen(
                    viewModel: viewModel,
                    excludeMemberIds: excludedMemberIds,
                    allowMemberIds: pickingSecondParent ? allowedSecondParentIds : nil
                ) { selected in
                    if pickingSecondParent {
                        secondParent = selected
                        pickingSecondParent = false
                    } else {
                        relatedMember = selected
                        // Auto-suggest second parent when editing a child relation
                        if selectedRelation == "Con trai" || selectedRelation == "Con gái" {
                            autoSuggestSecondParent(for: selected)
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

    // MARK: - Populate from existing member

    private func populateFromMember() {
        name = member.name
        gender = member.gender
        selectedRelation = member.role
        generation = member.generation
        occupation = member.occupation ?? ""
        hometown = member.hometown ?? ""
        note = member.note ?? ""
        existingAvatarPath = member.avatarPath

        // Load existing avatar image
        if let path = member.avatarPath, FileManager.default.fileExists(atPath: path) {
            avatarImage = UIImage(contentsOfFile: path)
        }

        // Parse birth date lunar
        if let bdl = member.birthDateLunar, !bdl.isEmpty {
            let parts = bdl.split(separator: "/").compactMap { Int($0) }
            if parts.count == 3 {
                birthDay = parts[0]
                birthMonth = parts[1]
                birthYear = parts[2]
                hasBirthDate = true
            }
        } else if let by = member.birthYear {
            birthYear = by
            hasBirthDate = true
        }

        // Parse death info
        if member.deathYear != nil {
            isDeceased = true
            if let ddl = member.deathDateLunar, !ddl.isEmpty {
                let parts = ddl.split(separator: "/").compactMap { Int($0) }
                if parts.count == 3 {
                    deathDay = parts[0]
                    deathMonth = parts[1]
                    deathYear = parts[2]
                    hasDeathDate = true
                }
            } else if let dy = member.deathYear {
                deathYear = dy
                hasDeathDate = true
            }
        }

        // Find primary related member based on role and links
        // Reload members to ensure latest data
        viewModel.loadMembers()

        let parentIdList = member.parentIds.split(separator: ",").map { String($0).trimmingCharacters(in: .whitespaces) }.filter { !$0.isEmpty }
        let spouseIdList = member.spouseIds.split(separator: ",").map { String($0).trimmingCharacters(in: .whitespaces) }.filter { !$0.isEmpty }

        #if DEBUG
        print("🔍 EditMember populateFromMember: role=\(member.role), parentIds='\(member.parentIds)', spouseIds='\(member.spouseIds)', members count=\(viewModel.members.count)")
        #endif

        let role = member.role
        switch role {
        case "Con trai", "Con gái", "Cháu trai", "Cháu gái":
            // Member is a child/grandchild — first parent is the related member
            if let pid = parentIdList.first {
                relatedMember = viewModel.member(byId: pid)
            }
            // Load second parent if exists
            if parentIdList.count > 1, let pid2 = parentIdList.dropFirst().first {
                secondParent = viewModel.member(byId: pid2)
            }
        case "Cha", "Mẹ", "Ông Nội", "Bà Nội", "Ông Ngoại", "Bà Ngoại":
            // Member is a parent/grandparent — find a child that lists this member as parent
            let child = viewModel.members.first { m in
                m.id != member.id &&
                m.parentIds.split(separator: ",")
                    .map { String($0).trimmingCharacters(in: .whitespaces) }
                    .contains(member.id)
            }
            relatedMember = child
        case "Vợ", "Chồng":
            if let sid = spouseIdList.first {
                relatedMember = viewModel.member(byId: sid)
            }
        case "Anh", "Chị", "Em trai", "Em gái":
            // Siblings share the same parents — find a sibling via shared parentIds
            if let pid = parentIdList.first {
                let sibling = viewModel.members.first { m in
                    m.id != member.id &&
                    m.parentIds.split(separator: ",")
                        .map { String($0).trimmingCharacters(in: .whitespaces) }
                        .contains(pid)
                }
                relatedMember = sibling ?? viewModel.member(byId: pid)
            }
        default:
            break
        }

        // Fallback: if no related member found yet, try all strategies
        if relatedMember == nil {
            // Try parentIds first
            if let pid = parentIdList.first {
                relatedMember = viewModel.member(byId: pid)
            }
            // Then try spouseIds
            if relatedMember == nil, let sid = spouseIdList.first {
                relatedMember = viewModel.member(byId: sid)
            }
            // Then try reverse lookup: find any member that references this member
            if relatedMember == nil {
                // Check if any member lists this member as parent
                let child = viewModel.members.first { m in
                    m.id != member.id &&
                    m.parentIds.split(separator: ",")
                        .map { String($0).trimmingCharacters(in: .whitespaces) }
                        .contains(member.id)
                }
                if let child = child {
                    relatedMember = child
                } else {
                    // Check if any member lists this member as spouse
                    let spouse = viewModel.members.first { m in
                        m.id != member.id &&
                        m.spouseIds.split(separator: ",")
                            .map { String($0).trimmingCharacters(in: .whitespaces) }
                            .contains(member.id)
                    }
                    relatedMember = spouse
                }
            }
        }

        #if DEBUG
        print("🔍 EditMember populateFromMember: found relatedMember=\(relatedMember?.name ?? "nil")")
        #endif

        recalculateCanChi()
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

                Text("Chỉnh sửa")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(TextMain)
            }

            Spacer()

            Button {
                saveChanges()
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
                        // Show initials
                        Circle()
                            .fill(
                                LinearGradient(
                                    colors: [PrimaryRed, DeepRed],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                            .frame(width: 96, height: 96)
                            .overlay {
                                Text(viewModel.memberInitials(member))
                                    .font(.system(size: 32, weight: .bold))
                                    .foregroundColor(.white)
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

            Text("Chạm để đổi ảnh đại diện")
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

    // MARK: - Relation Section

    private var relationSection: some View {
        VStack(spacing: 10) {
            // Relation type picker
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    ForEach(relationTypes, id: \.self) { rel in
                        Button {
                            selectedRelation = rel
                            // Reset second parent when changing relation
                            secondParent = nil
                            // Auto-suggest second parent when switching to child relation
                            if (rel == "Con trai" || rel == "Con gái"), let related = relatedMember {
                                autoSuggestSecondParent(for: related)
                            }
                        } label: {
                            Text(rel)
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

            // Custom role input if not in predefined list
            if !relationTypes.contains(selectedRelation) && !selectedRelation.isEmpty {
                HStack(spacing: 8) {
                    Image(systemName: "tag.fill")
                        .font(.system(size: 14))
                        .foregroundColor(PrimaryRed)
                    Text("Vai trò hiện tại: \(selectedRelation)")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(TextMain)
                }
                .padding(12)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(PrimaryContainer.opacity(0.5))
                .clipShape(RoundedRectangle(cornerRadius: 12))
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
                        ? "Là \(selectedRelation.lowercased()) của \(relatedMember!.name)"
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
            if relatedMember != nil && !selectedRelation.isEmpty &&
               ["Cha", "Mẹ", "Con trai", "Con gái", "Vợ", "Chồng"].contains(selectedRelation) {
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
                generationStepper
            }
        }
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

    private var generationStepper: some View {
        HStack {
            HStack(spacing: 10) {
                Image(systemName: "person.3.sequence.fill")
                    .font(.system(size: 16))
                    .foregroundColor(PrimaryRed)
                Text("Thế hệ \(generation)")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(TextMain)
            }
            Spacer()
            HStack(spacing: 0) {
                Button {
                    if generation > 1 { generation -= 1 }
                } label: {
                    Image(systemName: "minus")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(generation > 1 ? PrimaryRed : TextDim)
                        .frame(width: 36, height: 36)
                        .background(SurfaceContainerHigh)
                        .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                }
                .disabled(generation <= 1)

                Text("\(generation)")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(TextMain)
                    .frame(width: 40)

                Button {
                    if generation < 20 { generation += 1 }
                } label: {
                    Image(systemName: "plus")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(generation < 20 ? PrimaryRed : TextDim)
                        .frame(width: 36, height: 36)
                        .background(SurfaceContainerHigh)
                        .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                }
                .disabled(generation >= 20)
            }
        }
        .padding(14)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(OutlineVar, lineWidth: 1.5)
        )
    }

    // MARK: - Helpers

    /// IDs to exclude from PickMember — always exclude self (the member being edited)
    private var excludedMemberIds: Set<String> {
        var ids: Set<String> = [member.id]  // Always exclude self!
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
            secondParent = spouse
        } else {
            secondParent = nil
        }
    }

    // MARK: - Birth Date Section

    private var birthDateSection: some View {
        VStack(spacing: 8) {
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
                HStack(spacing: 8) {
                    datePickerColumn(label: "NGÀY", value: $birthDay, range: 1...30)
                    datePickerColumn(label: "THÁNG", value: $birthMonth, range: 1...12)
                    yearPickerColumn(label: "NĂM", value: $birthYear)
                }

                if !canChiText.isEmpty {
                    canChiCard
                }

                Button {
                    withAnimation {
                        hasBirthDate = false
                        canChiText = ""
                        nguHanhText = ""
                        menhText = ""
                    }
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
        guard hasBirthDate, birthYear > 1900 else {
            canChiText = ""
            nguHanhText = ""
            menhText = ""
            return
        }

        let lunarYear = birthYear
        canChiText = CanChiCalculator.getYearCanChi(lunarYear: lunarYear)
        nguHanhText = getNguHanhNapAm(lunarYear: lunarYear)
        menhText = getMenhFromNguHanh(nguHanhText)
    }

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

    // MARK: - Save Changes

    private func saveChanges() {
        guard !name.trimmingCharacters(in: .whitespaces).isEmpty else {
            showValidationAlert = true
            return
        }

        let trimmedName = name.trimmingCharacters(in: .whitespaces)
        let isSelf = member.isSelf // preserve existing isSelf
        let emoji = gender == "MALE" ? (isSelf ? "🧑" : "👨") : "👩"

        // Determine generation from relation (auto) or manual
        let gen = determineGeneration()

        // Update basic info
        member.name = trimmedName
        member.gender = gender
        member.role = selectedRelation.isEmpty ? member.role : selectedRelation
        member.generation = gen
        member.emoji = emoji

        // Update birth info
        if hasBirthDate {
            member.birthYear = birthYear
            member.birthDateLunar = String(format: "%02d/%02d/%04d", birthDay, birthMonth, birthYear)
            member.canChi = canChiText.isEmpty ? nil : canChiText
            member.menh = menhText.isEmpty ? nil : menhText
            member.menhDetail = nguHanhText.isEmpty ? nil : nguHanhText
            member.menhName = menhText.isEmpty ? nil : menhText
            member.zodiacEmoji = getZodiacEmoji(lunarYear: birthYear)
            member.zodiacName = getZodiacName(lunarYear: birthYear)
        } else {
            member.birthYear = nil
            member.birthDateLunar = nil
            member.canChi = nil
            member.menh = nil
            member.menhDetail = nil
            member.menhName = nil
            member.zodiacEmoji = nil
            member.zodiacName = nil
        }

        // Update death info
        if isDeceased {
            member.deathYear = deathYear
            member.deathDateLunar = String(format: "%02d/%02d/%04d", deathDay, deathMonth, deathYear)
        } else {
            member.deathYear = nil
            member.deathDateLunar = nil
        }

        // Update other fields
        member.occupation = occupation.isEmpty ? nil : occupation
        member.hometown = hometown.isEmpty ? nil : hometown
        member.note = note.isEmpty ? nil : note
        member.isElder = gen <= 2

        // Update avatar if changed
        if let img = avatarImage {
            // Check if this is a NEW image (not the one we loaded from disk)
            if let existingPath = existingAvatarPath,
               let existingImg = UIImage(contentsOfFile: existingPath),
               existingImg.pngData() == img.pngData() {
                // Same image, no need to save
            } else {
                if let newPath = saveAvatarImage(img, memberId: member.id) {
                    member.avatarPath = newPath
                }
            }
        }

        // Update relationship links if a related member is selected
        if let related = relatedMember, !selectedRelation.isEmpty {
            switch selectedRelation {
            case "Cha", "Mẹ":
                // Member IS a parent → related is the child
                // → related's parentIds should contain this member's ID
                if !related.parentIds.split(separator: ",").map({ String($0).trimmingCharacters(in: .whitespaces) }).contains(member.id) {
                    let existing = related.parentIds.isEmpty ? "" : related.parentIds + ","
                    related.parentIds = existing + member.id
                    viewModel.updateMember(related)
                }
            case "Con trai", "Con gái":
                // Member IS a child → related is the parent
                // → member's parentIds should contain related's ID (and second parent if set)
                var pids = member.parentIds
                    .split(separator: ",")
                    .map { String($0).trimmingCharacters(in: .whitespaces) }
                    .filter { !$0.isEmpty }
                if !pids.contains(related.id) {
                    pids.append(related.id)
                }
                if let second = secondParent, !pids.contains(second.id) {
                    pids.append(second.id)
                }
                member.parentIds = pids.joined(separator: ",")
            case "Vợ", "Chồng":
                // Bidirectional spouse link
                if !member.spouseIds.split(separator: ",").map({ String($0).trimmingCharacters(in: .whitespaces) }).contains(related.id) {
                    let existing = member.spouseIds.isEmpty ? "" : member.spouseIds + ","
                    member.spouseIds = existing + related.id
                }
                if !related.spouseIds.split(separator: ",").map({ String($0).trimmingCharacters(in: .whitespaces) }).contains(member.id) {
                    let existing = related.spouseIds.isEmpty ? "" : related.spouseIds + ","
                    related.spouseIds = existing + member.id
                    viewModel.updateMember(related)
                }
            default:
                break
            }
        }

        viewModel.updateMember(member)
        dismiss()
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
    let vm = FamilyTreeViewModel()
    let sampleMember = FamilyMemberEntity(
        id: "preview-1",
        name: "Nguyễn Văn A",
        role: "Bố",
        gender: "MALE",
        generation: 2,
        birthYear: 1965,
        birthDateLunar: "15/03/1965",
        hometown: "Hà Nội",
        occupation: "Giáo viên",
        emoji: "👨"
    )
    NavigationStack {
        EditMemberScreen(viewModel: vm, member: sampleMember)
    }
}
