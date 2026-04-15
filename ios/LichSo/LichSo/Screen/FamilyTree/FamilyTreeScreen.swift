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
                AddMemberScreen(viewModel: viewModel)
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
// TAB 1: TREE VIEW  — Proper family-tree layout
// ══════════════════════════════════════════

/// A "family unit" is either a single member or a member with one or more spouses
/// displayed together in one generation row.
private struct FamilyUnit: Identifiable {
    let id: String                         // primary member id
    let primary: FamilyMemberEntity
    let spouses: [FamilyMemberEntity]      // empty if no spouse

    /// Convenience: first spouse (for backward compat)
    var spouse: FamilyMemberEntity? { spouses.first }
}

/// A hierarchical family group: parent node with children recursively nested.
/// For multi-spouse, children are grouped by wife.
private struct FamilyGroup: Identifiable {
    let id: String
    let parentUnit: FamilyUnit
    let children: [FamilyGroup]
    let generation: Int
    let wifeId: String?               // which wife's branch (for multi-spouse display)
    let isMultiSpouseBranch: Bool     // true if this group is a wife-branch sub-group

    init(parentUnit: FamilyUnit, children: [FamilyGroup], generation: Int,
         wifeId: String? = nil, isMultiSpouseBranch: Bool = false) {
        self.id = parentUnit.id + (wifeId ?? "")
        self.parentUnit = parentUnit
        self.children = children
        self.generation = generation
        self.wifeId = wifeId
        self.isMultiSpouseBranch = isMultiSpouseBranch
    }
}

private struct TreeViewTab: View {
    @ObservedObject var viewModel: FamilyTreeViewModel
    let onMemberTap: (FamilyMemberEntity) -> Void

    // Zoom / pan state
    @State private var scale: CGFloat = 1.0
    @State private var lastScale: CGFloat = 1.0
    @State private var offset: CGSize = .zero
    @State private var lastOffset: CGSize = .zero

    // Layout constants
    private let nodeWidth: CGFloat = 100
    private let coupleGap: CGFloat = 4
    private let unitGap: CGFloat = 16
    private let connectorH: CGFloat = 24

    var body: some View {
        Group {
            if viewModel.members.isEmpty {
                EmptyFamilyView()
            } else {
                GeometryReader { geo in
                ZStack {
                    // Scrollable & zoomable tree — content scales/pans inside a fixed viewport
                    treeContent
                        .scaleEffect(scale, anchor: .center)
                        .offset(offset)
                        .frame(width: geo.size.width, height: geo.size.height, alignment: .center)
                        .contentShape(Rectangle())
                        .gesture(dragGesture)
                        .gesture(magnificationGesture)

                    // Zoom controls
                    VStack {
                        Spacer()
                        HStack {
                            VStack(spacing: 6) {
                                zoomButton(icon: "plus") {
                                    withAnimation(.easeOut(duration: 0.2)) {
                                        scale = min(scale + 0.2, 3.0)
                                        lastScale = scale
                                    }
                                }
                                zoomButton(icon: "minus") {
                                    withAnimation(.easeOut(duration: 0.2)) {
                                        scale = max(scale - 0.2, 0.3)
                                        lastScale = scale
                                    }
                                }
                                zoomButton(icon: "arrow.up.left.and.arrow.down.right") {
                                    withAnimation(.spring(response: 0.35)) {
                                        scale = 1.0; lastScale = 1.0
                                        offset = .zero; lastOffset = .zero
                                    }
                                }
                            }
                            Spacer()
                        }
                        .padding(.leading, 16)
                        .padding(.bottom, 24)
                    }
                }
                .clipped()
            }
        }
        }
    }

    // MARK: - Gestures

    private var dragGesture: some Gesture {
        DragGesture()
            .onChanged { v in
                offset = CGSize(
                    width: lastOffset.width + v.translation.width,
                    height: lastOffset.height + v.translation.height
                )
            }
            .onEnded { _ in lastOffset = offset }
    }

    private var magnificationGesture: some Gesture {
        MagnifyGesture()
            .onChanged { v in
                let newScale = lastScale * v.magnification
                scale = min(max(newScale, 0.3), 3.0)
            }
            .onEnded { _ in lastScale = scale }
    }

    // MARK: - Tree content

    private var treeContent: some View {
        VStack(spacing: 0) {
            let familyTree = buildFamilyTree()

            if familyTree.isEmpty {
                Text("Chưa có thành viên")
                    .font(.system(size: 14))
                    .foregroundColor(TextDim)
                    .padding(.top, 40)
            } else {
                // Render root family groups side by side
                HStack(alignment: .top, spacing: 24) {
                    ForEach(familyTree) { rootGroup in
                        familyGroupView(rootGroup, showGenLabel: true)
                    }
                }
            }
        }
        .padding(.horizontal, 20)
        .padding(.top, 20)
        .padding(.bottom, 100)
    }

    // MARK: - Family group view (recursive)

    /// Recursively renders a family group: parents on top, connector down,
    /// then children side by side. For multi-spouse, each wife's children branch separately.
    private func familyGroupView(_ group: FamilyGroup, showGenLabel: Bool = false) -> AnyView {
        AnyView(
            VStack(spacing: 0) {
                // Generation label
                if showGenLabel {
                    Text("Thế hệ \(group.generation)")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(TextDim)
                        .tracking(0.5)
                        .padding(.bottom, 8)
                }

                let isMultiSpouse = group.parentUnit.spouses.count > 1

                if isMultiSpouse && !group.isMultiSpouseBranch {
                    // ══════════════════════════════════════════
                    // MULTI-SPOUSE LAYOUT (matching the sketch):
                    //
                    //   Bà2 ─❤️─ Ông A ─❤️─ Bà1 ─❤️─ Bà3
                    //       |           |           |
                    //     [con Bà2]  [con Bà1]   [con Bà3]
                    // ══════════════════════════════════════════

                    let sortedWives = group.parentUnit.spouses.sorted { $0.spouseOrder < $1.spouseOrder }

                    // Row of wife-branches: each column = [wife ❤️ husband] at top + children below
                    HStack(alignment: .top, spacing: 12) {
                        ForEach(Array(sortedWives.enumerated()), id: \.element.id) { wifeIdx, wife in
                            let wifeBranch = group.children.first(where: { $0.wifeId == wife.id })

                            VStack(spacing: 0) {
                                // Couple header for this wife
                                HStack(alignment: .center, spacing: coupleGap) {
                                    if wifeIdx == 0 {
                                        personNode(wife)
                                        Text("❤️").font(.system(size: 10))
                                        personNode(group.parentUnit.primary)
                                    } else {
                                        Text("❤️").font(.system(size: 10))
                                        personNode(wife)
                                    }
                                }

                                // Connector from this couple-pair to their children
                                if let branch = wifeBranch, !branch.children.isEmpty {
                                    connectorDown()

                                    if branch.children.count == 1 {
                                        familyGroupView(branch.children[0], showGenLabel: false)
                                    } else {
                                        childrenRow(branch.children)
                                    }
                                }
                            }
                        }
                    }

                } else if !group.isMultiSpouseBranch {
                    // ══════════════════════════════════════════
                    // STANDARD single/couple layout
                    // ══════════════════════════════════════════
                    coupleView(group.parentUnit)

                    if !group.children.isEmpty {
                        connectorDown()

                        if group.children.count == 1 {
                            familyGroupView(group.children[0], showGenLabel: false)
                        } else {
                            childrenRow(group.children)
                        }
                    }
                }
            }
        )
    }

    // MARK: - Children row (horizontal bar + children)

    /// Renders multiple children side by side with a horizontal connecting bar.
    private func childrenRow(_ children: [FamilyGroup]) -> AnyView {
        AnyView(
            VStack(spacing: 0) {
                // Horizontal branch line
                horizontalBar(count: children.count)

                HStack(alignment: .top, spacing: 16) {
                    ForEach(children) { child in
                        VStack(spacing: 0) {
                            connectorDown(height: 8)
                            familyGroupView(child, showGenLabel: false)
                        }
                    }
                }
            }
        )
    }

    // MARK: - Couple view (person or couple/polygamous group)

    /// For multi-spouse: Bà2 ❤️ Ông A ❤️ Bà1 ❤️ Bà3
    /// Each wife is positioned so the connector can drop from between each couple-pair.
    private func coupleView(_ unit: FamilyUnit) -> some View {
        HStack(alignment: .center, spacing: coupleGap) {
            if unit.spouses.isEmpty {
                personNode(unit.primary)
            } else if unit.spouses.count == 1 {
                // Standard couple
                personNode(unit.spouses[0])
                Text("❤️").font(.system(size: 10))
                personNode(unit.primary)
            } else {
                // Multi-spouse layout:
                // Sort wives by spouseOrder so display matches data order
                let sortedWives = unit.spouses.sorted { $0.spouseOrder < $1.spouseOrder }

                // First wife ❤️ Husband ❤️ Second wife ❤️ Third wife ...
                personNode(sortedWives[0])
                Text("❤️").font(.system(size: 10))
                personNode(unit.primary)
                ForEach(Array(sortedWives.dropFirst()), id: \.id) { wife in
                    Text("❤️").font(.system(size: 10))
                    personNode(wife)
                }
            }
        }
    }

    // MARK: - Connector helpers

    private func connectorDown(height: CGFloat = 16) -> some View {
        Rectangle()
            .fill(OutlineVar)
            .frame(width: 2, height: height)
    }

    /// Horizontal bar connecting N branches.
    /// Draws a horizontal line spanning the full width with invisible spacers to match child count.
    private func horizontalBar(count: Int) -> some View {
        GeometryReader { geo in
            let w = geo.size.width
            // Draw horizontal line from first child center to last child center
            Path { path in
                let step = w / CGFloat(count)
                let startX = step / 2
                let endX = w - step / 2
                path.move(to: CGPoint(x: startX, y: 0.75))
                path.addLine(to: CGPoint(x: endX, y: 0.75))
            }
            .stroke(OutlineVar, lineWidth: 1.5)
        }
        .frame(height: 1.5)
    }

    // MARK: - Person node

    private func personNode(_ member: FamilyMemberEntity) -> some View {
        let avatarBg: Color = {
            if member.isElder { return Color(hex: "FFF8E1") }
            return member.gender == "MALE" ? Color(hex: "E3F2FD") : Color(hex: "FCE4EC")
        }()
        let borderColor: Color = {
            if member.isSelf { return PrimaryRed }
            return member.gender == "MALE" ? Color(hex: "90CAF9") : Color(hex: "F48FB1")
        }()

        return Button { onMemberTap(member) } label: {
            VStack(spacing: 4) {
                // Avatar
                ZStack {
                    Circle()
                        .fill(avatarBg)
                        .frame(width: 40, height: 40)

                    if let path = member.avatarPath,
                       let img = UIImage(contentsOfFile: path) {
                        Image(uiImage: img)
                            .resizable()
                            .scaledToFill()
                            .frame(width: 40, height: 40)
                            .clipShape(Circle())
                    } else {
                        Text(member.emoji)
                            .font(.system(size: 20))
                    }
                }

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
            .frame(width: nodeWidth)
            .padding(.vertical, 10)
            .padding(.horizontal, 6)
            .background(SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(borderColor, lineWidth: member.isSelf ? 2 : 1.5)
            )
            .overlay(alignment: .topTrailing) {
                if viewModel.isDeceased(member) {
                    Text("✝")
                        .font(.system(size: 10))
                        .foregroundColor(TextDim)
                        .padding(4)
                }
            }
            .opacity(viewModel.isDeceased(member) ? 0.75 : 1.0)
        }
        .buttonStyle(.plain)
    }

    // MARK: - Zoom button

    private func zoomButton(icon: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: icon)
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(TextMain)
                .frame(width: 40, height: 40)
                .background(SurfaceContainer)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(OutlineVar, lineWidth: 1)
                )
                .shadow(color: .black.opacity(0.1), radius: 4, y: 2)
        }
        .buttonStyle(.plain)
    }

    // MARK: - Build tree (hierarchical)

    /// Parse comma-separated IDs string into array of trimmed non-empty strings
    private func parseIds(_ raw: String) -> [String] {
        raw.split(separator: ",")
            .map { String($0).trimmingCharacters(in: .whitespaces) }
            .filter { !$0.isEmpty }
    }

    /// Build a hierarchical family tree as a list of root FamilyGroups.
    private func buildFamilyTree() -> [FamilyGroup] {
        let allMembers = viewModel.members
        if allMembers.isEmpty { return [] }

        let memberById = Dictionary(uniqueKeysWithValues: allMembers.map { ($0.id, $0) })
        let grouped = Dictionary(grouping: allMembers, by: { $0.generation })
        let generations = grouped.keys.sorted()
        guard let rootGen = generations.first else { return [] }
        let rootMembers = grouped[rootGen] ?? []

        // Build parent nodes for root generation
        let rootUnits = buildParentUnits(from: rootMembers, memberById: memberById)

        // Track claimed members to avoid duplicates
        // Start by claiming all root unit members (primary + spouses, even from other gens)
        var claimedChildIds: Set<String> = []
        for unit in rootUnits {
            claimedChildIds.insert(unit.primary.id)
            for s in unit.spouses { claimedChildIds.insert(s.id) }
        }

        var result = rootUnits.map { unit in
            buildFamilyGroup(
                parentUnit: unit,
                generation: rootGen,
                memberById: memberById,
                allMembers: allMembers,
                claimedChildIds: &claimedChildIds
            )
        }

        // Handle orphan members not reachable from root tree
        // (e.g. members whose parentIds don't link back to any root member)
        let unclaimedMembers = allMembers.filter { !claimedChildIds.contains($0.id) }
        if !unclaimedMembers.isEmpty {
            let orphanUnits = buildParentUnits(from: unclaimedMembers, memberById: memberById)
            for unit in orphanUnits {
                claimedChildIds.insert(unit.primary.id)
                for s in unit.spouses { claimedChildIds.insert(s.id) }
            }
            let orphanGroups = orphanUnits.map { unit in
                buildFamilyGroup(
                    parentUnit: unit,
                    generation: unit.primary.generation,
                    memberById: memberById,
                    allMembers: allMembers,
                    claimedChildIds: &claimedChildIds
                )
            }
            result.append(contentsOf: orphanGroups)
        }

        return result
    }

    /// Build parent units (couples/singles) from a list of members.
    /// Mirrors Android's buildParentNodes:
    /// 1st pass: process MALE members → detect multi-spouse / couple / single
    /// 2nd pass: process remaining FEMALE members → merge into existing multi-spouse or create couple/single
    private func buildParentUnits(from members: [FamilyMemberEntity], memberById: [String: FamilyMemberEntity]) -> [FamilyUnit] {
        var units: [FamilyUnit] = []
        var processed: Set<String> = []
        var multiSpouseHusbandIds: Set<String> = []

        // ── First pass: males ──
        for member in members {
            if processed.contains(member.id) { continue }
            if member.gender != "MALE" { continue }

            // Look up ALL spouses from the FULL family (not just local list)
            let allSpouseIds = parseIds(member.spouseIds)
            let allSpouseMembers = allSpouseIds.compactMap { memberById[$0] }
            let localSpouses = allSpouseMembers.filter { !processed.contains($0.id) }

            if allSpouseMembers.count > 1 {
                // Multi-spouse husband
                let sortedWives = localSpouses.sorted { $0.spouseOrder < $1.spouseOrder }
                units.append(FamilyUnit(id: member.id, primary: member, spouses: sortedWives))
                processed.insert(member.id)
                for w in localSpouses { processed.insert(w.id) }
                multiSpouseHusbandIds.insert(member.id)
            } else if localSpouses.count == 1 {
                // Standard couple
                units.append(FamilyUnit(id: member.id, primary: member, spouses: [localSpouses[0]]))
                processed.insert(member.id)
                processed.insert(localSpouses[0].id)
            } else {
                // Single male
                units.append(FamilyUnit(id: member.id, primary: member, spouses: []))
                processed.insert(member.id)
            }
        }

        // ── Second pass: remaining females ──
        for member in members {
            if processed.contains(member.id) { continue }

            // Find husband: from this member's spouseIds, or reverse-lookup
            let spouseIds = parseIds(member.spouseIds)
            let husband = spouseIds.compactMap({ memberById[$0] }).first(where: { $0.gender == "MALE" })
                ?? memberById.values.first(where: { candidate in
                    candidate.gender == "MALE" && parseIds(candidate.spouseIds).contains(member.id)
                })

            if let husband = husband {
                if multiSpouseHusbandIds.contains(husband.id) {
                    // Merge this wife into existing MultiSpouse unit
                    if let idx = units.firstIndex(where: { $0.id == husband.id }) {
                        let existing = units[idx]
                        if !existing.spouses.contains(where: { $0.id == member.id }) {
                            let newSpouses = (existing.spouses + [member]).sorted { $0.spouseOrder < $1.spouseOrder }
                            units[idx] = FamilyUnit(id: husband.id, primary: husband, spouses: newSpouses)
                        }
                    }
                    processed.insert(member.id)
                } else if !processed.contains(husband.id) {
                    // Husband not yet processed — check if he has multiple wives
                    let husbandAllSpouseIds = parseIds(husband.spouseIds)
                    if husbandAllSpouseIds.count > 1 {
                        let allWives = husbandAllSpouseIds.compactMap { memberById[$0] }
                            .filter { !processed.contains($0.id) || $0.id == member.id }
                            .sorted { $0.spouseOrder < $1.spouseOrder }
                        units.append(FamilyUnit(id: husband.id, primary: husband, spouses: allWives))
                        processed.insert(husband.id)
                        for w in allWives { processed.insert(w.id) }
                        multiSpouseHusbandIds.insert(husband.id)
                    } else {
                        // Standard couple (husband from another gen)
                        units.append(FamilyUnit(id: husband.id, primary: husband, spouses: [member]))
                        processed.insert(husband.id)
                        processed.insert(member.id)
                    }
                } else {
                    // Husband already processed → upgrade existing unit
                    if let idx = units.firstIndex(where: { $0.id == husband.id }) {
                        let existing = units[idx]
                        if existing.spouses.isEmpty {
                            // Upgrade Single → Couple
                            units[idx] = FamilyUnit(id: husband.id, primary: husband, spouses: [member])
                        } else if !existing.spouses.contains(where: { $0.id == member.id }) {
                            // Upgrade Couple → MultiSpouse (or add wife to MultiSpouse)
                            let newSpouses = (existing.spouses + [member]).sorted { $0.spouseOrder < $1.spouseOrder }
                            units[idx] = FamilyUnit(id: husband.id, primary: husband, spouses: newSpouses)
                            multiSpouseHusbandIds.insert(husband.id)
                        }
                    }
                    processed.insert(member.id)
                }
            } else {
                // Single female (no husband found)
                units.append(FamilyUnit(id: member.id, primary: member, spouses: []))
                processed.insert(member.id)
            }
        }

        return units
    }

    /// Find children of a parent unit among all members.
    private func findChildrenOf(parentUnit: FamilyUnit, allMembers: [FamilyMemberEntity], claimedChildIds: inout Set<String>) -> [FamilyMemberEntity] {
        var nodeIds: Set<String> = [parentUnit.primary.id]
        for s in parentUnit.spouses { nodeIds.insert(s.id) }

        // Candidate children: not yet claimed, not a parent themselves, at least one parentId matches
        let candidates = allMembers.filter { member in
            if claimedChildIds.contains(member.id) { return false }
            if nodeIds.contains(member.id) { return false } // parent can't be own child
            let pids = parseIds(member.parentIds)
            if pids.isEmpty { return false }
            return pids.contains(where: { nodeIds.contains($0) })
        }

        // For single/couple units: prefer children whose ALL parentIds match this unit.
        // This prevents children from being claimed by the wrong parent when there are
        // multiple potential parents (e.g. siblings who are both parents).
        let isMultiSpouse = parentUnit.spouses.count > 1
        let children: [FamilyMemberEntity]
        if !isMultiSpouse {
            let strictMatch = candidates.filter { child in
                let pids = parseIds(child.parentIds)
                return pids.allSatisfy { nodeIds.contains($0) }
            }
            children = strictMatch.isEmpty ? candidates : strictMatch
        } else {
            children = candidates
        }

        for child in children {
            claimedChildIds.insert(child.id)
        }

        return children
    }

    /// Recursively build a FamilyGroup.
    /// For multi-spouse, children are grouped by their mother (wife).
    private func buildFamilyGroup(
        parentUnit: FamilyUnit,
        generation: Int,
        memberById: [String: FamilyMemberEntity],
        allMembers: [FamilyMemberEntity],
        claimedChildIds: inout Set<String>
    ) -> FamilyGroup {
        let children = findChildrenOf(parentUnit: parentUnit, allMembers: allMembers, claimedChildIds: &claimedChildIds)

        let isMultiSpouse = parentUnit.spouses.count > 1

        if isMultiSpouse {
            // Group children by their mother (wife)
            var wifeChildrenMap: [String: [FamilyMemberEntity]] = [:]
            for wife in parentUnit.spouses {
                wifeChildrenMap[wife.id] = []
            }

            var assignedIds: Set<String> = []
            for child in children {
                let pids = parseIds(child.parentIds)
                if let matchedWife = parentUnit.spouses.first(where: { pids.contains($0.id) }) {
                    wifeChildrenMap[matchedWife.id, default: []].append(child)
                    assignedIds.insert(child.id)
                }
            }

            // Unassigned children → assign to first wife
            let unassigned = children.filter { !assignedIds.contains($0.id) }
            if !unassigned.isEmpty, let firstWife = parentUnit.spouses.first {
                wifeChildrenMap[firstWife.id, default: []].append(contentsOf: unassigned)
            }

            // Build wife-branch groups
            var wifeGroups: [FamilyGroup] = []
            for wife in parentUnit.spouses {
                let wifeChildren = wifeChildrenMap[wife.id] ?? []
                let childUnits = buildParentUnits(from: wifeChildren, memberById: memberById)
                // Claim all members in child units (primary + spouses) before recursing
                for cu in childUnits {
                    claimedChildIds.insert(cu.primary.id)
                    for s in cu.spouses { claimedChildIds.insert(s.id) }
                }
                let childGroups = childUnits.map { childUnit in
                    buildFamilyGroup(
                        parentUnit: childUnit,
                        generation: generation + 1,
                        memberById: memberById,
                        allMembers: allMembers,
                        claimedChildIds: &claimedChildIds
                    )
                }
                wifeGroups.append(FamilyGroup(
                    parentUnit: FamilyUnit(id: parentUnit.primary.id, primary: parentUnit.primary, spouses: [wife]),
                    children: childGroups,
                    generation: generation,
                    wifeId: wife.id,
                    isMultiSpouseBranch: true
                ))
            }

            return FamilyGroup(
                parentUnit: parentUnit,
                children: wifeGroups,
                generation: generation
            )
        }

        // Standard single/couple path
        let childUnits = buildParentUnits(from: children, memberById: memberById)
        // Claim all members in child units (primary + spouses) before recursing
        for cu in childUnits {
            claimedChildIds.insert(cu.primary.id)
            for s in cu.spouses { claimedChildIds.insert(s.id) }
        }
        let childGroups = childUnits.map { childUnit in
            buildFamilyGroup(
                parentUnit: childUnit,
                generation: generation + 1,
                memberById: memberById,
                allMembers: allMembers,
                claimedChildIds: &claimedChildIds
            )
        }

        return FamilyGroup(
            parentUnit: parentUnit,
            children: childGroups,
            generation: generation
        )
    }

    /// Helper: wife label by 0-based index
    private func wifeLabel(_ index: Int) -> String {
        switch index {
        case 0: return "Vợ cả"
        case 1: return "Vợ hai"
        case 2: return "Vợ ba"
        case 3: return "Vợ tư"
        default: return "Vợ \(index + 1)"
        }
    }
}

/// Safe subscript for arrays
private extension Array {
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
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

// Old AddMemberSheet removed — replaced by AddMemberScreen.swift

// ══════════════════════════════════════════
// PREVIEW
// ══════════════════════════════════════════

#Preview {
    NavigationStack {
        FamilyTreeScreen()
    }
}
