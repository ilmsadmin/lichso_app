import SwiftUI
import SwiftData
import PhotosUI

// ═══════════════════════════════════════════
// Member Detail Screen — Matches screen-member-detail.html
// Shows full member info with hero header, can chi, relations,
// photos, notes and action bar (edit/delete).
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

struct MemberDetailScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @ObservedObject var viewModel: FamilyTreeViewModel
    let member: FamilyMemberEntity

    @State private var showDeleteAlert = false
    @State private var showEditSheet = false
    @State private var showMemorialDetail = false
    @State private var selectedMemorial: MemorialDayEntity?
    @State private var showPhotosPicker = false
    @State private var selectedPhotoItem: PhotosPickerItem?

    var body: some View {
        VStack(spacing: 0) {
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 0) {
                    // ═══ HERO HEADER ═══
                    MemberHero(
                        member: member,
                        viewModel: viewModel,
                        onBack: { dismiss() },
                        onShare: { shareMember() }
                    )

                    VStack(alignment: .leading, spacing: 0) {
                        // ═══ CAN CHI & MENH ═══
                        if let birthYear = member.birthYear {
                            SectionLabel(icon: "sparkles", text: "CAN CHI & MỆNH")
                            CanChiCard(member: member, birthYear: birthYear)
                        }

                        // ═══ MEMORIAL DAYS ═══
                        let memberMemorials = viewModel.memorialsForMember(member.id)
                        if !memberMemorials.isEmpty {
                            SectionLabel(icon: "flame.fill", text: "NGÀY GIỖ")
                            ForEach(memberMemorials, id: \.id) { memorial in
                                MemorialMiniCard(
                                    memorial: memorial,
                                    daysUntil: viewModel.daysUntilMemorial(memorial),
                                    onTap: {
                                        selectedMemorial = memorial
                                        showMemorialDetail = true
                                    }
                                )
                            }
                        }

                        // ═══ PERSONAL INFO ═══
                        SectionLabel(icon: "info.circle.fill", text: "THÔNG TIN CÁ NHÂN")
                        PersonalInfoGroup(member: member)

                        // ═══ RELATIONS ═══
                        let relations = viewModel.relatedMembers(for: member)
                        if !relations.isEmpty {
                            SectionLabel(icon: "person.2.fill", text: "QUAN HỆ GIA ĐÌNH")
                            RelationsRow(relations: relations, viewModel: viewModel)
                        }

                        // ═══ PHOTOS ═══
                        SectionLabel(icon: "photo.on.rectangle.angled", text: "ẢNH")
                        PhotoGrid(
                            photos: viewModel.photos,
                            onAddPhoto: { showPhotosPicker = true },
                            onDeletePhoto: { photo in viewModel.deletePhoto(photo) }
                        )

                        // ═══ NOTES ═══
                        if let note = member.note, !note.isEmpty {
                            SectionLabel(icon: "note.text", text: "GHI CHÚ")
                            NotesBox(text: note)
                        }

                        Spacer().frame(height: 16)
                    }
                    .padding(.horizontal, 16)
                }
            }

            // ═══ ACTION BAR ═══
            ActionBar(
                onEdit: { showEditSheet = true },
                onDelete: { showDeleteAlert = true }
            )
        }
        .background(SurfaceBg)
        .navigationBarHidden(true)
        .onAppear {
            viewModel.setup(context: modelContext)
            viewModel.loadPhotos(for: member.id)
        }
        .alert("Xoá thành viên", isPresented: $showDeleteAlert) {
            Button("Huỷ", role: .cancel) { }
            Button("Xoá", role: .destructive) {
                viewModel.deleteMember(member)
                dismiss()
            }
        } message: {
            Text("Bạn chắc chắn muốn xoá \(member.name)? Hành động này không thể hoàn tác.")
        }
        .sheet(isPresented: $showEditSheet) {
            NavigationStack {
                EditMemberScreen(viewModel: viewModel, member: member)
            }
        }
        .sheet(item: $selectedMemorial) { memorial in
            NavigationStack {
                MemorialDetailScreen(viewModel: viewModel, memorial: memorial)
            }
        }
        .photosPicker(isPresented: $showPhotosPicker, selection: $selectedPhotoItem, matching: .images)
        .onChange(of: selectedPhotoItem) { _, newValue in
            Task {
                if let data = try? await newValue?.loadTransferable(type: Data.self) {
                    savePhotoData(data)
                }
            }
        }
    }

    private func shareMember() {
        let text = """
        \(member.name)
        Vai trò: \(member.role)
        Thế hệ: \(member.generation)
        \(member.birthYear.map { "Năm sinh: \($0)" } ?? "")
        """
        let av = UIActivityViewController(activityItems: [text], applicationActivities: nil)
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let root = windowScene.windows.first?.rootViewController {
            root.present(av, animated: true)
        }
    }

    private func savePhotoData(_ data: Data) {
        let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
            .appendingPathComponent("member_photos", isDirectory: true)
        try? FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        let fileName = "\(member.id)_\(Int(Date().timeIntervalSince1970)).jpg"
        let fileURL = dir.appendingPathComponent(fileName)
        try? data.write(to: fileURL)
        viewModel.addPhoto(memberId: member.id, filePath: fileURL.path)
    }
}

// ══════════════════════════════════════════
// MEMBER HERO
// ══════════════════════════════════════════

private struct MemberHero: View {
    let member: FamilyMemberEntity
    let viewModel: FamilyTreeViewModel
    let onBack: () -> Void
    let onShare: () -> Void

    var body: some View {
        ZStack(alignment: .topTrailing) {
            // Decorative glow
            Circle()
                .fill(RadialGradient(colors: [GoldAccent.opacity(0.1), .clear], center: .center, startRadius: 0, endRadius: 100))
                .frame(width: 180, height: 180)
                .offset(x: 60, y: -40)

            VStack(alignment: .leading, spacing: 0) {
                // Nav
                HStack {
                    Button(action: onBack) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(.white.opacity(0.9))
                            .frame(width: 40, height: 40)
                            .background(.white.opacity(0.12))
                            .clipShape(Circle())
                    }
                    Spacer()
                    HStack(spacing: 8) {
                        Button(action: onShare) {
                            Image(systemName: "square.and.arrow.up")
                                .font(.system(size: 16))
                                .foregroundColor(.white.opacity(0.9))
                                .frame(width: 40, height: 40)
                                .background(.white.opacity(0.12))
                                .clipShape(Circle())
                        }
                    }
                }
                .padding(.bottom, 20)

                // Profile row
                HStack(spacing: 16) {
                    // Avatar
                    Circle()
                        .fill(LinearGradient(colors: [GoldAccent, Color(hex: "B8860B")], startPoint: .topLeading, endPoint: .bottomTrailing))
                        .frame(width: 80, height: 80)
                        .overlay(
                            Text(viewModel.memberInitials(member))
                                .font(.system(size: 28, weight: .bold, design: .serif))
                                .foregroundColor(.white)
                        )
                        .overlay(Circle().stroke(.white.opacity(0.25), lineWidth: 3))

                    VStack(alignment: .leading, spacing: 2) {
                        Text(member.name)
                            .font(.system(size: 22, weight: .bold))
                            .foregroundColor(.white)
                        Text("\(member.role) — Thế hệ \(member.generation)")
                            .font(.system(size: 13))
                            .foregroundColor(.white.opacity(0.6))

                        // Chips
                        HStack(spacing: 6) {
                            if let zodiacEmoji = member.zodiacEmoji, let zodiacName = member.zodiacName {
                                HeroChip(text: "\(zodiacEmoji) \(zodiacName)", isGold: false)
                            }
                            if let menh = member.menh {
                                HeroChip(text: menh, isGold: true)
                            }
                            if let age = viewModel.memberAge(member) {
                                HeroChip(text: "\(age) tuổi", isGold: false)
                            }
                        }
                        .padding(.top, 6)
                    }
                }
                .padding(.bottom, 16)

                // Stats
                HStack(spacing: 20) {
                    let relations = viewModel.relatedMembers(for: member)
                    let children = relations.filter { $0.relation == "Con trai" || $0.relation == "Con gái" }
                    let spouses = relations.filter { $0.relation == "Chồng" || $0.relation == "Vợ" }

                    StatBadge(number: children.count, label: "Con")
                    StatBadge(number: spouses.count, label: member.gender == "MALE" ? "Vợ" : "Chồng")
                    StatBadge(number: viewModel.photos.count, label: "Ảnh")
                }
            }
            .padding(16)
        }
        .background(
            LinearGradient(
                colors: [Color(hex: "3E2723"), Color(hex: "4E342E"), Color(hex: "5D4037")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
    }
}

private struct HeroChip: View {
    let text: String
    let isGold: Bool

    var body: some View {
        Text(text)
            .font(.system(size: 11, weight: .medium))
            .foregroundColor(isGold ? GoldAccent : .white.opacity(0.8))
            .padding(.horizontal, 12)
            .padding(.vertical, 4)
            .background(isGold ? GoldAccent.opacity(0.2) : .white.opacity(0.1))
            .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

private struct StatBadge: View {
    let number: Int
    let label: String

    var body: some View {
        VStack(spacing: 1) {
            Text("\(number)")
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
            Text(label)
                .font(.system(size: 10))
                .foregroundColor(.white.opacity(0.5))
        }
    }
}

// ══════════════════════════════════════════
// SECTION LABEL
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
// CAN CHI CARD
// ══════════════════════════════════════════

private struct CanChiCard: View {
    let member: FamilyMemberEntity
    let birthYear: Int

    var body: some View {
        let yearCanChi = CanChiCalculator.getYearCanChi(lunarYear: birthYear)
        let canChiDetail = member.menhDetail ?? ""
        let menhName = member.menhName ?? member.menh ?? ""

        HStack(spacing: 14) {
            Image(systemName: "sparkles")
                .font(.system(size: 22))
                .foregroundColor(.white)
                .frame(width: 48, height: 48)
                .background(LinearGradient(colors: [GoldAccent, Color(hex: "B8860B")], startPoint: .topLeading, endPoint: .bottomTrailing))
                .clipShape(RoundedRectangle(cornerRadius: 14))

            VStack(alignment: .leading, spacing: 2) {
                Text("\(yearCanChi) — \(canChiDetail.isEmpty ? menhName : canChiDetail)")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(PrimaryRed)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
                Text("Mệnh \(menhName) • Tuổi \(member.zodiacName ?? "")")
                    .font(.system(size: 12))
                    .foregroundColor(TextDim)
            }
            Spacer()
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
// MEMORIAL MINI CARD
// ══════════════════════════════════════════

private struct MemorialMiniCard: View {
    let memorial: MemorialDayEntity
    let daysUntil: Int
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                Image(systemName: "flame.fill")
                    .font(.system(size: 20))
                    .foregroundColor(.white)
                    .frame(width: 42, height: 42)
                    .background(LinearGradient(colors: [Color(hex: "FF6F00"), Color(hex: "E65100")], startPoint: .topLeading, endPoint: .bottomTrailing))
                    .clipShape(RoundedRectangle(cornerRadius: 12))

                VStack(alignment: .leading, spacing: 1) {
                    Text("Giỗ \(memorial.relation) \(memorial.memberName)")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(TextMain)
                    Text("\(memorial.lunarDay) tháng \(memorial.lunarMonth) Âm lịch — Còn \(daysUntil) ngày")
                        .font(.system(size: 11))
                        .foregroundColor(TextDim)
                }

                Spacer()

                Text("\(daysUntil)")
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(Color(hex: "E65100"))
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(Color(hex: "E65100").opacity(0.1))
                    .clipShape(RoundedRectangle(cornerRadius: 10))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(
                LinearGradient(colors: [Color(hex: "FFF3E0"), Color(hex: "FFE0B2")], startPoint: .topLeading, endPoint: .bottomTrailing)
            )
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "FFD180"), lineWidth: 1))
            .padding(.bottom, 12)
        }
        .buttonStyle(.plain)
    }
}

// ══════════════════════════════════════════
// PERSONAL INFO GROUP
// ══════════════════════════════════════════

private struct PersonalInfoGroup: View {
    let member: FamilyMemberEntity

    private var rows: [(icon: String, iconBg: Color, iconFg: Color, label: String, value: String)] {
        var r: [(String, Color, Color, String, String)] = []
        if let birthDateLunar = member.birthDateLunar, !birthDateLunar.isEmpty {
            let yearCanChi = member.birthYear.map { CanChiCalculator.getYearCanChi(lunarYear: $0) } ?? ""
            r.append(("calendar", Color(hex: "FFEBEE"), Color(hex: "C62828"), "Ngày sinh Âm lịch", "\(birthDateLunar)\(yearCanChi.isEmpty ? "" : " (\(yearCanChi))")"))
        }
        if let by = member.birthYear {
            // Calculate solar from lunar if available
            r.append(("calendar.badge.clock", Color(hex: "E3F2FD"), Color(hex: "1565C0"), "Năm sinh", "\(by)"))
        }
        if let occupation = member.occupation, !occupation.isEmpty {
            r.append(("briefcase.fill", Color(hex: "E8F5E9"), Color(hex: "2E7D32"), "Nghề nghiệp", occupation))
        }
        if let hometown = member.hometown, !hometown.isEmpty {
            r.append(("mappin.and.ellipse", Color(hex: "FFF8E1"), Color(hex: "F57F17"), "Nơi ở", hometown))
        }
        r.append(("person.fill", Color(hex: "F3E5F5"), Color(hex: "7B1FA2"), "Giới tính", member.gender == "MALE" ? "Nam" : "Nữ"))
        r.append(("person.2.fill", Color(hex: "EFEBE9"), Color(hex: "5D4037"), "Vai trò", member.role))
        return r
    }

    var body: some View {
        VStack(spacing: 0) {
            ForEach(Array(rows.enumerated()), id: \.offset) { idx, row in
                HStack(spacing: 12) {
                    Image(systemName: row.icon)
                        .font(.system(size: 16))
                        .foregroundColor(row.iconFg)
                        .frame(width: 32, height: 32)
                        .background(row.iconBg)
                        .clipShape(RoundedRectangle(cornerRadius: 8))

                    VStack(alignment: .leading, spacing: 1) {
                        Text(row.label)
                            .font(.system(size: 12))
                            .foregroundColor(TextDim)
                        Text(row.value)
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(TextMain)
                    }
                    Spacer()
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 14)

                if idx < rows.count - 1 {
                    Divider().padding(.leading, 60)
                }
            }
        }
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))
        .padding(.bottom, 12)
    }
}

// ══════════════════════════════════════════
// RELATIONS ROW
// ══════════════════════════════════════════

private struct RelationsRow: View {
    let relations: [(member: FamilyMemberEntity, relation: String)]
    let viewModel: FamilyTreeViewModel

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 10) {
                ForEach(relations, id: \.member.id) { rel in
                    VStack(spacing: 8) {
                        Circle()
                            .fill(LinearGradient(
                                colors: rel.member.gender == "MALE" ?
                                    [Color(hex: "42A5F5"), Color(hex: "1565C0")] :
                                    [Color(hex: "EC407A"), Color(hex: "AD1457")],
                                startPoint: .topLeading, endPoint: .bottomTrailing
                            ))
                            .frame(width: 48, height: 48)
                            .overlay(
                                Text(viewModel.memberInitials(rel.member))
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(.white)
                            )

                        Text(rel.member.name.split(separator: " ").suffix(2).joined(separator: " "))
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(TextMain)
                            .lineLimit(1)

                        Text(rel.relation)
                            .font(.system(size: 10))
                            .foregroundColor(TextDim)
                    }
                    .frame(minWidth: 110)
                    .padding(.vertical, 14)
                    .padding(.horizontal, 10)
                    .background(SurfaceContainer)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))
                }
            }
        }
        .padding(.bottom, 12)
    }
}

// ══════════════════════════════════════════
// PHOTO GRID
// ══════════════════════════════════════════

private struct PhotoGrid: View {
    let photos: [MemberPhotoEntity]
    let onAddPhoto: () -> Void
    let onDeletePhoto: (MemberPhotoEntity) -> Void

    private let columns = Array(repeating: GridItem(.flexible(), spacing: 6), count: 3)

    var body: some View {
        LazyVGrid(columns: columns, spacing: 6) {
            ForEach(photos, id: \.id) { photo in
                if FileManager.default.fileExists(atPath: photo.filePath),
                   let img = UIImage(contentsOfFile: photo.filePath) {
                    Image(uiImage: img)
                        .resizable()
                        .scaledToFill()
                        .frame(minHeight: 100)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .contextMenu {
                            Button(role: .destructive) { onDeletePhoto(photo) } label: {
                                Label("Xoá ảnh", systemImage: "trash")
                            }
                        }
                } else {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(SurfaceContainerHigh)
                        .frame(minHeight: 100)
                        .overlay(
                            Image(systemName: "photo")
                                .font(.system(size: 28))
                                .foregroundColor(OutlineVariant)
                        )
                }
            }

            // Add photo button
            Button(action: onAddPhoto) {
                RoundedRectangle(cornerRadius: 12)
                    .strokeBorder(OutlineVariant, style: StrokeStyle(lineWidth: 2, dash: [6]))
                    .frame(minHeight: 100)
                    .overlay(
                        Image(systemName: "plus.rectangle.on.rectangle")
                            .font(.system(size: 28))
                            .foregroundColor(TextDim)
                    )
            }
        }
        .padding(.bottom, 12)
    }
}

// ══════════════════════════════════════════
// NOTES BOX
// ══════════════════════════════════════════

private struct NotesBox: View {
    let text: String

    var body: some View {
        Text(text)
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
}

// ══════════════════════════════════════════
// ACTION BAR
// ══════════════════════════════════════════

private struct ActionBar: View {
    let onEdit: () -> Void
    let onDelete: () -> Void

    var body: some View {
        HStack(spacing: 10) {
            Button(action: onEdit) {
                HStack(spacing: 6) {
                    Image(systemName: "pencil")
                        .font(.system(size: 16))
                    Text("Chỉnh sửa")
                        .font(.system(size: 13, weight: .semibold))
                }
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(PrimaryRed)
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }

            Button(action: onDelete) {
                HStack(spacing: 6) {
                    Image(systemName: "trash")
                        .font(.system(size: 16))
                    Text("Xoá")
                        .font(.system(size: 13, weight: .semibold))
                }
                .foregroundColor(Color(hex: "C62828"))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(Color(hex: "FFF5F5"))
                .clipShape(RoundedRectangle(cornerRadius: 14))
                .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color(hex: "FFCDD2"), lineWidth: 1.5))
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(SurfaceBg)
        .overlay(alignment: .top) {
            Divider().foregroundColor(OutlineVariant)
        }
    }
}

// Old EditMemberSheet removed — replaced by EditMemberScreen.swift
