import SwiftUI
import SwiftData

// ═══════════════════════════════════════════
// Family Tree Screen — Matches screen-family-tree.html
// Three tabs: Cây phả, Thành viên, Ngày giỗ
// Uses REAL data from SwiftData via FamilyTreeViewModel
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
private var OutlineVar: Color { LSTheme.outlineVariant }
private let BrownDark = Color(hex: "3E2723")
private let BrownMed = Color(hex: "4E342E")
private let BrownLight = Color(hex: "5D4037")

struct FamilyTreeScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @StateObject private var viewModel = FamilyTreeViewModel()

    @State private var selectedTab = 0
    @State private var showSettings = false
    @State private var showAddMember = false
    @State private var selectedMember: FamilyMemberEntity?

    var body: some View {
        VStack(spacing: 0) {
            // ═══ HEADER ═══
            TreeHeader(
                familyName: viewModel.settings?.familyName ?? "Gia phả của tôi",
                familyCrest: viewModel.settings?.familyCrest ?? "GP",
                generations: viewModel.totalGenerations,
                members: viewModel.totalMembers,
                onBack: { dismiss() },
                onSettings: { showSettings = true }
            )

            // ═══ TAB BAR ═══
            TreeTabBar(selectedTab: $selectedTab)

            // ═══ CONTENT ═══
            Group {
                switch selectedTab {
                case 0:
                    TreeViewTab(viewModel: viewModel, onMemberTap: { member in
                        selectedMember = member
                    })
                case 1:
                    MembersListTab(viewModel: viewModel, onMemberTap: { member in
                        selectedMember = member
                    })
                case 2:
                    MemorialsTab(viewModel: viewModel)
                default:
                    EmptyView()
                }
            }
        }
        .background(SurfaceBg)
        .navigationBarHidden(true)
        .onAppear {
            viewModel.setup(context: modelContext)
        }
        .sheet(isPresented: $showSettings) {
            NavigationStack {
                FamilySettingsScreen(viewModel: viewModel)
            }
        }
        .sheet(isPresented: $showAddMember) {
            NavigationStack {
                AddMemberSheet(viewModel: viewModel)
            }
        }
        .sheet(item: $selectedMember) { member in
            NavigationStack {
                MemberDetailScreen(viewModel: viewModel, member: member)
            }
        }
        .overlay(alignment: .bottomTrailing) {
            // FAB
            Button {
                showAddMember = true
            } label: {
                Image(systemName: "person.badge.plus")
                    .font(.system(size: 22))
                    .foregroundColor(GoldAccent)
                    .frame(width: 56, height: 56)
                    .background(
                        LinearGradient(
                            colors: [BrownMed, BrownDark],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .shadow(color: .black.opacity(0.25), radius: 8, y: 4)
            }
            .padding(.trailing, 20)
            .padding(.bottom, 24)
        }
    }
}

// ══════════════════════════════════════════
// HEADER
// ══════════════════════════════════════════

private struct TreeHeader: View {
    let familyName: String
    let familyCrest: String
    let generations: Int
    let members: Int
    let onBack: () -> Void
    let onSettings: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Nav row
            HStack {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.white.opacity(0.9))
                        .frame(width: 40, height: 40)
                        .background(.white.opacity(0.1))
                        .clipShape(Circle())
                }
                Text("Gia Phả")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.white)
                Spacer()
                Button(action: onSettings) {
                    Image(systemName: "ellipsis")
                        .font(.system(size: 18))
                        .foregroundColor(.white.opacity(0.9))
                        .frame(width: 40, height: 40)
                        .background(.white.opacity(0.1))
                        .clipShape(Circle())
                }
            }

            // Family name row
            HStack(spacing: 12) {
                // Crest
                Circle()
                    .fill(
                        LinearGradient(
                            colors: [GoldAccent, Color(hex: "B8860B")],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 48, height: 48)
                    .overlay(
                        Text(familyCrest)
                            .font(.system(size: 18, weight: .bold, design: .serif))
                            .foregroundColor(.white)
                    )
                    .overlay(Circle().stroke(.white.opacity(0.3), lineWidth: 2))

                VStack(alignment: .leading, spacing: 1) {
                    Text(familyName)
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.white)
                    Text("\(generations) thế hệ · \(members) thành viên")
                        .font(.system(size: 12))
                        .foregroundColor(.white.opacity(0.6))
                }
            }
            .padding(.top, 14)
        }
        .padding(.horizontal, 20)
        .padding(.top, 12)
        .padding(.bottom, 22)
        .background(
            LinearGradient(
                colors: [BrownDark, BrownMed, BrownLight],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
    }
}

// ══════════════════════════════════════════
// TAB BAR
// ══════════════════════════════════════════

private struct TreeTabBar: View {
    @Binding var selectedTab: Int

    private let tabs: [(icon: String, title: String)] = [
        ("list.bullet.indent", "Cây phả"),
        ("person.3.fill", "Thành viên"),
        ("flame.fill", "Ngày giỗ"),
    ]

    var body: some View {
        HStack(spacing: 0) {
            ForEach(0..<3, id: \.self) { index in
                Button {
                    withAnimation(.easeInOut(duration: 0.2)) {
                        selectedTab = index
                    }
                } label: {
                    VStack(spacing: 2) {
                        Image(systemName: tabs[index].icon)
                            .font(.system(size: 18))
                        Text(tabs[index].title)
                            .font(.system(size: 12, weight: .semibold))
                    }
                    .foregroundColor(selectedTab == index ? PrimaryRed : TextDim)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .overlay(alignment: .bottom) {
                        if selectedTab == index {
                            Rectangle()
                                .fill(PrimaryRed)
                                .frame(height: 2)
                        }
                    }
                }
                .buttonStyle(.plain)
            }
        }
        .background(SurfaceBg)
        .overlay(alignment: .bottom) {
            Divider().foregroundColor(OutlineVar)
        }
    }
}

// ══════════════════════════════════════════
// TAB 1: TREE VIEW
// ══════════════════════════════════════════

private struct TreeViewTab: View {
    @ObservedObject var viewModel: FamilyTreeViewModel
    let onMemberTap: (FamilyMemberEntity) -> Void

    var body: some View {
        if viewModel.members.isEmpty {
            EmptyFamilyView()
        } else {
            ScrollView([.horizontal, .vertical], showsIndicators: true) {
                VStack(spacing: 0) {
                    ForEach(viewModel.generations, id: \.self) { gen in
                        let members = viewModel.membersForGeneration(gen)

                        // Generation label
                        Text("Thế hệ \(gen)")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(TextDim)
                            .tracking(0.5)
                            .padding(.top, gen == viewModel.generations.first ? 16 : 4)
                            .padding(.bottom, 6)

                        // Members row
                        HStack(spacing: 12) {
                            ForEach(members, id: \.id) { member in
                                PersonNodeView(
                                    member: member,
                                    viewModel: viewModel,
                                    onTap: { onMemberTap(member) }
                                )
                            }
                        }

                        // Connector
                        if gen != viewModel.generations.last {
                            Rectangle()
                                .fill(OutlineVar)
                                .frame(width: 2, height: 16)
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 80)
            }
        }
    }
}

// ══════════════════════════════════════════
// PERSON NODE
// ══════════════════════════════════════════

private struct PersonNodeView: View {
    let member: FamilyMemberEntity
    let viewModel: FamilyTreeViewModel
    let onTap: () -> Void

    private var avatarBg: Color {
        if member.isElder { return Color(hex: "FFF8E1") }
        return member.gender == "MALE" ? Color(hex: "E3F2FD") : Color(hex: "FCE4EC")
    }

    private var borderColor: Color {
        if member.isSelf { return PrimaryRed }
        return member.gender == "MALE" ? Color(hex: "90CAF9") : Color(hex: "F48FB1")
    }

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 4) {
                // Avatar
                Circle()
                    .fill(avatarBg)
                    .frame(width: 40, height: 40)
                    .overlay(
                        Text(member.emoji)
                            .font(.system(size: 20))
                    )

                // Name
                Text(member.name)
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundColor(TextMain)
                    .lineLimit(2)
                    .multilineTextAlignment(.center)

                // Role
                Text(member.role)
                    .font(.system(size: 8, weight: .medium))
                    .foregroundColor(member.isSelf ? PrimaryRed : TextDim)

                // Years
                if let birth = member.birthYear {
                    let deathStr = member.deathYear.map { " - \($0)" } ?? ""
                    Text("\(birth)\(deathStr)")
                        .font(.system(size: 8))
                        .foregroundColor(OutlineVar)
                }
            }
            .frame(width: 90)
            .padding(.vertical, 10)
            .padding(.horizontal, 6)
            .background(SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(borderColor, lineWidth: member.isSelf ? 2 : 1.5)
            )
            .opacity(viewModel.isDeceased(member) ? 0.7 : 1.0)
        }
        .buttonStyle(.plain)
    }
}

// ══════════════════════════════════════════
// TAB 2: MEMBERS LIST
// ══════════════════════════════════════════

private struct MembersListTab: View {
    @ObservedObject var viewModel: FamilyTreeViewModel
    let onMemberTap: (FamilyMemberEntity) -> Void

    @State private var searchText = ""

    var body: some View {
        if viewModel.members.isEmpty {
            EmptyFamilyView()
        } else {
            VStack(spacing: 0) {
                // Search
                HStack(spacing: 8) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 16))
                        .foregroundColor(TextDim)
                    TextField("Tìm thành viên...", text: $searchText)
                        .font(.system(size: 14))
                        .foregroundColor(TextMain)
                        .onChange(of: searchText) { _, newVal in
                            viewModel.searchQuery = newVal
                        }
                }
                .padding(10)
                .background(SurfaceContainer)
                .clipShape(RoundedRectangle(cornerRadius: 14))
                .overlay(RoundedRectangle(cornerRadius: 14).stroke(OutlineVar, lineWidth: 1))
                .padding(.horizontal, 16)
                .padding(.vertical, 12)

                ScrollView(.vertical, showsIndicators: false) {
                    VStack(spacing: 0) {
                        ForEach(viewModel.generations, id: \.self) { gen in
                            let members = viewModel.membersForGeneration(gen)
                            if !members.isEmpty {
                                // Generation header
                                HStack {
                                    Text("Thế hệ \(gen)")
                                        .font(.system(size: 12, weight: .bold))
                                        .foregroundColor(PrimaryRed)
                                        .tracking(0.5)
                                    Spacer()
                                    Text("\(members.count) người")
                                        .font(.system(size: 11))
                                        .foregroundColor(TextDim)
                                }
                                .padding(.horizontal, 16)
                                .padding(.top, 12)
                                .padding(.bottom, 6)

                                ForEach(members, id: \.id) { member in
                                    MemberListRow(
                                        member: member,
                                        viewModel: viewModel,
                                        onTap: { onMemberTap(member) }
                                    )
                                }
                            }
                        }

                        Spacer().frame(height: 80)
                    }
                }
            }
        }
    }
}

private struct MemberListRow: View {
    let member: FamilyMemberEntity
    let viewModel: FamilyTreeViewModel
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                // Avatar
                Circle()
                    .fill(member.gender == "MALE" ? Color(hex: "E3F2FD") : Color(hex: "FCE4EC"))
                    .frame(width: 44, height: 44)
                    .overlay(Text(member.emoji).font(.system(size: 22)))

                VStack(alignment: .leading, spacing: 2) {
                    HStack(spacing: 4) {
                        Text(member.name)
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(TextMain)
                        if member.isSelf {
                            Text("(Bạn)")
                                .font(.system(size: 11, weight: .medium))
                                .foregroundColor(PrimaryRed)
                        }
                    }
                    Text("\(member.role) — Thế hệ \(member.generation)")
                        .font(.system(size: 11))
                        .foregroundColor(TextDim)
                }

                Spacer()

                if let age = viewModel.memberAge(member) {
                    Text("\(age) tuổi")
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(TextSub)
                }

                Image(systemName: "chevron.right")
                    .font(.system(size: 12))
                    .foregroundColor(OutlineVar)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(SurfaceBg)
        }
        .buttonStyle(.plain)
    }
}

// ══════════════════════════════════════════
// TAB 3: MEMORIALS
// ══════════════════════════════════════════

private struct MemorialsTab: View {
    @ObservedObject var viewModel: FamilyTreeViewModel

    @State private var showAddMemorial = false
    @State private var selectedMemorial: MemorialDayEntity?

    var body: some View {
        if viewModel.memorials.isEmpty {
            VStack(spacing: 16) {
                Image(systemName: "flame")
                    .font(.system(size: 40))
                    .foregroundColor(TextDim.opacity(0.4))
                Text("Chưa có ngày giỗ")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(TextMain)
                Text("Thêm ngày giỗ để được nhắc nhở trước ngày cúng giỗ")
                    .font(.system(size: 13))
                    .foregroundColor(TextDim)
                    .multilineTextAlignment(.center)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding()
        } else {
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 8) {
                    // Upcoming memorials
                    let sorted = viewModel.memorials.sorted { viewModel.daysUntilMemorial($0) < viewModel.daysUntilMemorial($1) }

                    HStack {
                        Text("SẮP TỚI")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(PrimaryRed)
                            .tracking(0.5)
                        Spacer()
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 16)

                    ForEach(sorted, id: \.id) { memorial in
                        MemorialRow(
                            memorial: memorial,
                            daysUntil: viewModel.daysUntilMemorial(memorial),
                            onTap: { selectedMemorial = memorial }
                        )
                    }

                    Spacer().frame(height: 80)
                }
            }
            .sheet(item: $selectedMemorial) { memorial in
                NavigationStack {
                    MemorialDetailScreen(viewModel: viewModel, memorial: memorial)
                }
            }
        }
    }
}

private struct MemorialRow: View {
    let memorial: MemorialDayEntity
    let daysUntil: Int
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                // Icon
                Image(systemName: "flame.fill")
                    .font(.system(size: 20))
                    .foregroundColor(.white)
                    .frame(width: 44, height: 44)
                    .background(
                        LinearGradient(
                            colors: [Color(hex: "FF6F00"), Color(hex: "E65100")],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 12))

                VStack(alignment: .leading, spacing: 2) {
                    Text("Giỗ \(memorial.relation) \(memorial.memberName)")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(TextMain)
                    Text("\(memorial.lunarDay)/\(memorial.lunarMonth) Âm lịch")
                        .font(.system(size: 11))
                        .foregroundColor(TextDim)
                }

                Spacer()

                // Days until
                VStack(spacing: 1) {
                    Text("\(daysUntil)")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(daysUntil <= 7 ? Color(hex: "E65100") : TextSub)
                    Text("ngày")
                        .font(.system(size: 9))
                        .foregroundColor(TextDim)
                }

                Image(systemName: "chevron.right")
                    .font(.system(size: 12))
                    .foregroundColor(OutlineVar)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(OutlineVar, lineWidth: 1)
            )
            .padding(.horizontal, 16)
        }
        .buttonStyle(.plain)
    }
}

// ══════════════════════════════════════════
// EMPTY STATE
// ══════════════════════════════════════════

private struct EmptyFamilyView: View {
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "person.3")
                .font(.system(size: 44))
                .foregroundColor(TextDim.opacity(0.4))
            Text("Chưa có thành viên")
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(TextMain)
            Text("Nhấn nút + ở góc dưới để bắt đầu\ntạo cây gia phả của bạn")
                .font(.system(size: 13))
                .foregroundColor(TextDim)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding()
    }
}

// ══════════════════════════════════════════
// ADD MEMBER SHEET
// ══════════════════════════════════════════

private struct AddMemberSheet: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @ObservedObject var viewModel: FamilyTreeViewModel

    @State private var name = ""
    @State private var role = ""
    @State private var gender = "MALE"
    @State private var generation = 1
    @State private var birthYear = ""
    @State private var isSelf = false

    private let genders = ["MALE", "FEMALE"]
    private let roles = ["Ông Cố", "Bà Cố", "Ông Nội", "Bà Nội", "Ông Ngoại", "Bà Ngoại", "Bố", "Mẹ", "Bản thân", "Vợ", "Chồng", "Con trai", "Con gái", "Anh", "Chị", "Em trai", "Em gái", "Cháu trai", "Cháu gái", "Khác"]

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button("Huỷ") { dismiss() }
                    .foregroundColor(TextSub)
                Spacer()
                Text("Thêm thành viên")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundColor(TextMain)
                Spacer()
                Button("Lưu") { saveMember() }
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(name.isEmpty ? TextDim : PrimaryRed)
                    .disabled(name.isEmpty)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .overlay(alignment: .bottom) { Divider() }

            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    // Name
                    FormField(label: "Họ và tên *", placeholder: "Nhập họ tên", text: $name)

                    // Role picker
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Vai trò")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(TextDim)
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 6) {
                                ForEach(roles, id: \.self) { r in
                                    Button {
                                        role = r
                                        if r == "Bản thân" { isSelf = true }
                                    } label: {
                                        Text(r)
                                            .font(.system(size: 12, weight: .medium))
                                            .padding(.horizontal, 12)
                                            .padding(.vertical, 6)
                                            .background(role == r ? PrimaryRed : SurfaceContainer)
                                            .foregroundColor(role == r ? .white : TextMain)
                                            .clipShape(RoundedRectangle(cornerRadius: 12))
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 12)
                                                    .stroke(role == r ? PrimaryRed : OutlineVar, lineWidth: 1)
                                            )
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                        }
                    }

                    // Gender
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Giới tính")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(TextDim)
                        HStack(spacing: 10) {
                            ForEach(genders, id: \.self) { g in
                                Button {
                                    gender = g
                                } label: {
                                    HStack(spacing: 6) {
                                        Image(systemName: gender == g ? "circle.inset.filled" : "circle")
                                            .font(.system(size: 16))
                                            .foregroundColor(gender == g ? PrimaryRed : TextDim)
                                        Text(g == "MALE" ? "Nam" : "Nữ")
                                            .font(.system(size: 14))
                                            .foregroundColor(TextMain)
                                    }
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }

                    // Generation
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Thế hệ")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(TextDim)
                        Stepper("Thế hệ \(generation)", value: $generation, in: 1...10)
                            .font(.system(size: 14))
                            .foregroundColor(TextMain)
                    }

                    // Birth year
                    FormField(label: "Năm sinh", placeholder: "VD: 1990", text: $birthYear)

                    // Self toggle
                    Toggle(isOn: $isSelf) {
                        Text("Đây là bạn")
                            .font(.system(size: 14))
                            .foregroundColor(TextMain)
                    }
                    .tint(PrimaryRed)
                }
                .padding(16)
            }
        }
        .background(SurfaceBg)
        .onAppear {
            viewModel.setup(context: modelContext)
        }
    }

    private func saveMember() {
        let emoji = gender == "MALE" ? (isSelf ? "🧑" : "👨") : "👩"
        let member = FamilyMemberEntity(
            id: UUID().uuidString,
            name: name,
            role: role.isEmpty ? "Thành viên" : role,
            gender: gender,
            generation: generation,
            birthYear: Int(birthYear),
            isSelf: isSelf,
            isElder: generation <= 2,
            emoji: emoji
        )
        viewModel.addMember(member)
        dismiss()
    }
}

private struct FormField: View {
    let label: String
    let placeholder: String
    @Binding var text: String

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(TextDim)
            TextField(placeholder, text: $text)
                .font(.system(size: 14))
                .foregroundColor(TextMain)
                .padding(12)
                .background(SurfaceContainer)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(OutlineVar, lineWidth: 1)
                )
        }
    }
}

// ══════════════════════════════════════════
// PREVIEW
// ══════════════════════════════════════════

#Preview {
    NavigationStack {
        FamilyTreeScreen()
    }
}
