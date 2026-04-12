import SwiftUI
import SwiftData

// ═══════════════════════════════════════════
// Pick Member Screen — Matches screen-pick-member.html
// Radio-select a family member, grouped by generation
// Filters: All, Generation N, Male, Female
// Uses REAL data from SwiftData
// ═══════════════════════════════════════════

private var PrimaryRed: Color { LSTheme.primary }
private var SurfaceBg: Color { LSTheme.bg }
private var SurfaceContainer: Color { LSTheme.surfaceContainer }
private var SurfaceContainerHigh: Color { LSTheme.surfaceContainerHigh }
private var TextMain: Color { LSTheme.textPrimary }
private var TextSub: Color { LSTheme.textSecondary }
private var TextDim: Color { LSTheme.textTertiary }
private var OutlineVariant: Color { LSTheme.outlineVariant }
private var PrimaryContainer: Color { LSTheme.primaryContainer }

struct PickMemberScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @ObservedObject var viewModel: FamilyTreeViewModel

    @State private var selectedMemberId: String?
    @State private var searchText = ""
    @State private var activeFilter: FamilyTreeViewModel.MemberFilter = .all

    var onSelect: (FamilyMemberEntity) -> Void

    var body: some View {
        VStack(spacing: 0) {
            // ═══ HEADER ═══
            VStack(spacing: 12) {
                HStack {
                    HStack(spacing: 8) {
                        Button { dismiss() } label: {
                            Image(systemName: "xmark")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(TextMain)
                                .frame(width: 36, height: 36)
                                .background(SurfaceContainer)
                                .clipShape(Circle())
                        }
                        Text("Chọn thành viên")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(TextMain)
                    }
                    Spacer()
                    Text("\(viewModel.totalMembers) người")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(TextDim)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 4)
                        .background(SurfaceContainer)
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                }

                // Search bar
                HStack(spacing: 10) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 18))
                        .foregroundColor(TextDim)
                    TextField("Tìm theo tên...", text: $searchText)
                        .font(.system(size: 14))
                        .foregroundColor(TextMain)
                        .onChange(of: searchText) { _, newVal in
                            viewModel.searchQuery = newVal
                        }
                }
                .padding(10)
                .background(SurfaceContainer)
                .clipShape(RoundedRectangle(cornerRadius: 14))
                .overlay(RoundedRectangle(cornerRadius: 14).stroke(OutlineVariant, lineWidth: 1))
            }
            .padding(.horizontal, 16)
            .padding(.top, 14)
            .padding(.bottom, 14)
            .background(SurfaceBg)
            .overlay(alignment: .bottom) {
                Divider().foregroundColor(OutlineVariant)
            }

            // ═══ FILTER CHIPS ═══
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    FilterChip(title: "Tất cả", isActive: activeFilter == .all) {
                        activeFilter = .all
                        viewModel.activeFilter = .all
                    }
                    ForEach(viewModel.generations, id: \.self) { gen in
                        FilterChip(title: "Thế hệ \(gen)", isActive: {
                            if case .generation(let g) = activeFilter { return g == gen }
                            return false
                        }()) {
                            activeFilter = .generation(gen)
                            viewModel.activeFilter = .generation(gen)
                        }
                    }
                    FilterChip(title: "Nam", isActive: activeFilter == .male) {
                        activeFilter = .male
                        viewModel.activeFilter = .male
                    }
                    FilterChip(title: "Nữ", isActive: activeFilter == .female) {
                        activeFilter = .female
                        viewModel.activeFilter = .female
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }

            // ═══ MEMBER LIST ═══
            ScrollView(.vertical, showsIndicators: false) {
                LazyVStack(alignment: .leading, spacing: 0) {
                    ForEach(viewModel.generations, id: \.self) { gen in
                        let genMembers = viewModel.membersForGeneration(gen)
                        if !genMembers.isEmpty {
                            // Generation header
                            HStack(spacing: 6) {
                                Image(systemName: "figure.2.and.child.holdinghands")
                                    .font(.system(size: 12))
                                    .foregroundColor(PrimaryRed)
                                Text("THẾ HỆ \(gen)")
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(PrimaryRed)
                                    .tracking(0.5)
                                Text("\(genMembers.count)")
                                    .font(.system(size: 10, weight: .medium))
                                    .foregroundColor(TextDim)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 2)
                                    .background(SurfaceContainerHigh)
                                    .clipShape(RoundedRectangle(cornerRadius: 8))
                            }
                            .padding(.top, 14)
                            .padding(.bottom, 8)

                            ForEach(genMembers, id: \.id) { member in
                                MemberRow(
                                    member: member,
                                    isSelected: selectedMemberId == member.id,
                                    viewModel: viewModel,
                                    onTap: { selectedMemberId = member.id }
                                )
                            }
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 16)
            }

            // ═══ BOTTOM BAR ═══
            HStack(spacing: 10) {
                Button { dismiss() } label: {
                    Text("Huỷ")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(TextSub)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(SurfaceContainer)
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(OutlineVariant, lineWidth: 1.5))
                }

                Button {
                    if let id = selectedMemberId, let member = viewModel.member(byId: id) {
                        onSelect(member)
                        dismiss()
                    }
                } label: {
                    Text("Chọn")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(selectedMemberId != nil ? PrimaryRed : PrimaryRed.opacity(0.4))
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                }
                .disabled(selectedMemberId == nil)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(SurfaceBg)
            .overlay(alignment: .top) {
                Divider().foregroundColor(OutlineVariant)
            }
        }
        .background(SurfaceBg)
        .onAppear {
            viewModel.setup(context: modelContext)
        }
    }
}

// ══════════════════════════════════════════
// FILTER CHIP
// ══════════════════════════════════════════

private struct FilterChip: View {
    let title: String
    let isActive: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            Text(title)
                .font(.system(size: 12, weight: .medium))
                .foregroundColor(isActive ? .white : TextSub)
                .padding(.horizontal, 14)
                .padding(.vertical, 6)
                .background(isActive ? PrimaryRed : SurfaceContainer)
                .clipShape(RoundedRectangle(cornerRadius: 18))
                .overlay(
                    RoundedRectangle(cornerRadius: 18)
                        .stroke(isActive ? PrimaryRed : OutlineVariant, lineWidth: 1)
                )
        }
    }
}

// ══════════════════════════════════════════
// MEMBER ROW
// ══════════════════════════════════════════

private struct MemberRow: View {
    let member: FamilyMemberEntity
    let isSelected: Bool
    let viewModel: FamilyTreeViewModel
    let onTap: () -> Void

    private var isDeceased: Bool { viewModel.isDeceased(member) }

    private var avatarGradient: [Color] {
        if isDeceased { return [Color(hex: "9E9E9E"), Color(hex: "616161")] }
        if member.gender == "MALE" { return [Color(hex: "42A5F5"), Color(hex: "1565C0")] }
        return [Color(hex: "EC407A"), Color(hex: "AD1457")]
    }

    private var chipInfo: (text: String, bg: Color, fg: Color) {
        if isDeceased { return ("Đã mất", Color(hex: "F5F5F5"), Color(hex: "616161")) }
        if member.gender == "MALE" { return ("Nam", Color(hex: "E3F2FD"), Color(hex: "1565C0")) }
        return ("Nữ", Color(hex: "FCE4EC"), Color(hex: "AD1457"))
    }

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                // Radio
                ZStack {
                    Circle()
                        .stroke(isSelected ? PrimaryRed : TextDim, lineWidth: 2)
                        .frame(width: 22, height: 22)
                    if isSelected {
                        Circle()
                            .fill(PrimaryRed)
                            .frame(width: 22, height: 22)
                        Circle()
                            .fill(.white)
                            .frame(width: 8, height: 8)
                    }
                }

                // Avatar
                Circle()
                    .fill(LinearGradient(colors: avatarGradient, startPoint: .topLeading, endPoint: .bottomTrailing))
                    .frame(width: 44, height: 44)
                    .overlay(
                        Text(viewModel.memberInitials(member))
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.white)
                    )

                // Info
                VStack(alignment: .leading, spacing: 2) {
                    Text(member.name)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(TextMain)

                    HStack(spacing: 6) {
                        if let by = member.birthYear {
                            if let dy = member.deathYear {
                                Text("\(by) — \(dy)")
                                    .font(.system(size: 11))
                                    .foregroundColor(TextDim)
                            } else {
                                Text("\(by)")
                                    .font(.system(size: 11))
                                    .foregroundColor(TextDim)
                            }
                        }
                        let chip = chipInfo
                        Text(chip.text)
                            .font(.system(size: 9, weight: .semibold))
                            .foregroundColor(chip.fg)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 2)
                            .background(chip.bg)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                }

                Spacer()
            }
            .padding(.vertical, 12)
            .overlay(alignment: .bottom) {
                Rectangle().fill(OutlineVariant).frame(height: 0.5)
            }
        }
        .buttonStyle(.plain)
    }
}
