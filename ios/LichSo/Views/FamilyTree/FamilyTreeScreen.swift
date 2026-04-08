import SwiftUI
import SwiftData

struct FamilyTreeScreen: View {
    @Environment(\.lichSoColors) private var c
    @Environment(\.modelContext) private var modelContext
    @StateObject private var repository = FamilyTreeViewState()
    @State private var showAddMember = false
    @State private var selectedMember: FamilyMemberEntity? = nil
    var onBackClick: () -> Void = {}

    var body: some View {
        VStack(spacing: 0) {
            // Top bar
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(c.textPrimary)
                }
                Spacer()
                Text("CÂY GIA PHẢ")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(c.textPrimary)
                    .tracking(1)
                Spacer()
                HStack(spacing: 12) {
                    Button(action: { showAddMember = true }) {
                        Image(systemName: "person.badge.plus")
                            .font(.system(size: 18))
                            .foregroundColor(c.primary)
                    }
                    Menu {
                        Button("Xuất dữ liệu") { exportData() }
                        Button("Nhập dữ liệu") { /* Import */ }
                        Button("Cài đặt gia phả") { /* Settings */ }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                            .font(.system(size: 18))
                            .foregroundColor(c.textSecondary)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            // Content
            ScrollView {
                if repository.members.isEmpty {
                    emptyState
                } else {
                    // Family tree visualization
                    LazyVStack(spacing: 8) {
                        // Group by generation
                        ForEach(groupedGenerations, id: \.key) { generation, members in
                            VStack(alignment: .leading, spacing: 8) {
                                Text("Đời thứ \(generation)")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(c.gold)
                                    .padding(.horizontal, 16)
                                    .padding(.top, 8)

                                ForEach(members, id: \.id) { member in
                                    MemberCard(member: member, c: c) {
                                        selectedMember = member
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        .background(c.bg)
        .onAppear {
            repository.setContext(modelContext)
            repository.loadMembers()
        }
        .sheet(isPresented: $showAddMember) {
            AddMemberSheet(c: c) { name, gender, birthDate, deathDate, generation, role in
                repository.addMember(name: name, gender: gender, birthDate: birthDate, deathDate: deathDate, generation: generation, role: role)
            }
        }
        .sheet(item: $selectedMember) { member in
            MemberDetailSheet(member: member, c: c, repository: repository)
        }
    }

    private var emptyState: some View {
        VStack(spacing: 16) {
            Image(systemName: "person.3.sequence")
                .font(.system(size: 56))
                .foregroundColor(c.textQuaternary)
                .padding(.top, 80)
            Text("Chưa có thành viên nào")
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(c.textSecondary)
            Text("Nhấn nút + để thêm thành viên đầu tiên vào cây gia phả")
                .font(.system(size: 14))
                .foregroundColor(c.textTertiary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            Button(action: { showAddMember = true }) {
                Text("Thêm thành viên")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 12)
                    .background(c.primary)
                    .cornerRadius(24)
            }
        }
    }

    private var groupedGenerations: [(key: Int, value: [FamilyMemberEntity])] {
        let grouped = Dictionary(grouping: repository.members) { $0.generation }
        return grouped.sorted { $0.key < $1.key }.map { (key: $0.key, value: $0.value) }
    }

    private func exportData() {
        // Export family tree data to JSON
        let encoder = JSONEncoder()
        encoder.outputFormatting = .prettyPrinted
        // Simplified export
    }
}

// MARK: - FamilyTree View State
@MainActor
class FamilyTreeViewState: ObservableObject {
    @Published var members: [FamilyMemberEntity] = []
    private var context: ModelContext?

    func setContext(_ context: ModelContext) {
        self.context = context
    }

    func loadMembers() {
        guard let context = context else { return }
        let descriptor = FetchDescriptor<FamilyMemberEntity>(
            sortBy: [SortDescriptor(\.generation), SortDescriptor(\.name)]
        )
        members = (try? context.fetch(descriptor)) ?? []
    }

    func addMember(name: String, gender: String, birthDate: String?, deathDate: String?, generation: Int, role: String) {
        guard let context = context else { return }
        let birthYear = birthDate.flatMap { Int($0) }
        let deathYear = deathDate.flatMap { Int($0) }
        let member = FamilyMemberEntity(name: name, role: role, gender: gender, generation: generation, birthYear: birthYear, deathYear: deathYear)
        context.insert(member)
        try? context.save()
        loadMembers()
    }

    func deleteMember(_ member: FamilyMemberEntity) {
        guard let context = context else { return }
        context.delete(member)
        try? context.save()
        loadMembers()
    }
}

// MARK: - Member Card
struct MemberCard: View {
    let member: FamilyMemberEntity
    let c: LichSoColors
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                // Avatar
                ZStack {
                    Circle()
                        .fill(member.gender == "Nam" ? c.tealDim : c.goldDim)
                        .frame(width: 48, height: 48)
                    Image(systemName: member.gender == "Nam" ? "person.fill" : "person.fill")
                        .font(.system(size: 20))
                        .foregroundColor(member.gender == "Nam" ? c.teal : c.gold)
                }

                VStack(alignment: .leading, spacing: 3) {
                    Text(member.name)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(c.textPrimary)
                    HStack(spacing: 8) {
                        if !member.role.isEmpty {
                            Text(member.role)
                                .font(.system(size: 12))
                                .foregroundColor(c.textTertiary)
                        }
                        if let birth = member.birthYear {
                            Text("Sinh \(birth)")
                                .font(.system(size: 12))
                                .foregroundColor(c.textTertiary)
                        }
                    }
                    if member.deathYear != nil {
                        Text("Đã mất")
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(c.textTertiary)
                    }
                }
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.system(size: 12))
                    .foregroundColor(c.textQuaternary)
            }
            .padding(12)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(c.surface)
            )
            .padding(.horizontal, 16)
        }
    }
}

// MARK: - Add Member Sheet
struct AddMemberSheet: View {
    let c: LichSoColors
    let onSave: (String, String, String?, String?, Int, String) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var name = ""
    @State private var gender = "Nam"
    @State private var birthDate = ""
    @State private var deathDate = ""
    @State private var generation = 1
    @State private var role = ""

    var body: some View {
        NavigationView {
            Form {
                Section("Thông tin cơ bản") {
                    TextField("Họ và tên", text: $name)
                    Picker("Giới tính", selection: $gender) {
                        Text("Nam").tag("Nam")
                        Text("Nữ").tag("Nữ")
                    }
                    TextField("Vai trò (Cha, Mẹ, Con...)", text: $role)
                }
                Section("Ngày sinh / Ngày mất") {
                    TextField("Ngày sinh (dd/MM/yyyy)", text: $birthDate)
                    TextField("Ngày mất (để trống nếu còn sống)", text: $deathDate)
                }
                Section("Thế hệ") {
                    Stepper("Đời thứ \(generation)", value: $generation, in: 1...20)
                }
            }
            .navigationTitle("Thêm thành viên")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Hủy") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Lưu") {
                        onSave(
                            name, gender,
                            birthDate.isEmpty ? nil : birthDate,
                            deathDate.isEmpty ? nil : deathDate,
                            generation, role
                        )
                        dismiss()
                    }
                    .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
    }
}

// MARK: - Member Detail Sheet
struct MemberDetailSheet: View {
    let member: FamilyMemberEntity
    let c: LichSoColors
    @ObservedObject var repository: FamilyTreeViewState
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 16) {
                    // Avatar
                    ZStack {
                        Circle()
                            .fill(member.gender == "Nam" ? c.tealDim : c.goldDim)
                            .frame(width: 80, height: 80)
                        Image(systemName: "person.fill")
                            .font(.system(size: 36))
                            .foregroundColor(member.gender == "Nam" ? c.teal : c.gold)
                    }
                    .padding(.top, 16)

                    Text(member.name)
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(c.textPrimary)

                    // Info grid
                    VStack(alignment: .leading, spacing: 12) {
                        infoRow("Giới tính", member.gender)
                        infoRow("Vai trò", member.role)
                        infoRow("Đời thứ", "\(member.generation)")
                        if let birth = member.birthYear {
                            infoRow("Năm sinh", "\(birth)")
                        }
                        if let death = member.deathYear {
                            infoRow("Năm mất", "\(death)")
                        }
                    }
                    .padding(16)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(c.surface)
                    )
                    .padding(.horizontal, 16)

                    // Delete button
                    Button(role: .destructive) {
                        repository.deleteMember(member)
                        dismiss()
                    } label: {
                        Text("Xóa thành viên")
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(c.badRed)
                    }
                    .padding(.top, 16)
                }
            }
            .navigationTitle("Chi tiết")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Đóng") { dismiss() }
                }
            }
        }
    }

    @ViewBuilder
    private func infoRow(_ title: String, _ value: String) -> some View {
        HStack {
            Text(title)
                .font(.system(size: 13))
                .foregroundColor(c.textTertiary)
                .frame(width: 80, alignment: .leading)
            Text(value)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(c.textPrimary)
        }
    }
}
