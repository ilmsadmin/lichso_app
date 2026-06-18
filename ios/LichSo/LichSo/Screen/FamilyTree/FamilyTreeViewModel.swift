import SwiftUI
import SwiftData

// ═══════════════════════════════════════════
// FamilyTreeViewModel — Shared data layer for all
// family tree screens. Uses SwiftData with real data.
// ═══════════════════════════════════════════

@MainActor
class FamilyTreeViewModel: ObservableObject {
    // ── Members ──
    @Published var members: [FamilyMemberEntity] = []
    @Published var memorials: [MemorialDayEntity] = []
    @Published var settings: FamilySettingsEntity?
    @Published var checklists: [MemorialChecklistEntity] = []
    @Published var photos: [MemberPhotoEntity] = []

    // ── Filter state (for PickMember) ──
    @Published var searchQuery = ""
    @Published var activeFilter: MemberFilter = .all

    private var modelContext: ModelContext?

    enum MemberFilter: Hashable {
        case all
        case generation(Int)
        case male
        case female
    }

    func setup(context: ModelContext) {
        self.modelContext = context
        loadAll()
    }

    func loadAll() {
        loadMembers()
        loadMemorials()
        loadSettings()
    }

    // ── LOAD ──

    func loadMembers() {
        guard let ctx = modelContext else { return }
        let descriptor = FetchDescriptor<FamilyMemberEntity>(sortBy: [SortDescriptor(\.generation), SortDescriptor(\.name)])
        members = (try? ctx.fetch(descriptor)) ?? []
    }

    func loadMemorials() {
        guard let ctx = modelContext else { return }
        let descriptor = FetchDescriptor<MemorialDayEntity>(sortBy: [SortDescriptor(\.lunarMonth), SortDescriptor(\.lunarDay)])
        memorials = (try? ctx.fetch(descriptor)) ?? []
    }

    func loadSettings() {
        guard let ctx = modelContext else { return }
        let descriptor = FetchDescriptor<FamilySettingsEntity>()
        let fetched = (try? ctx.fetch(descriptor)) ?? []
        #if DEBUG
        debugLog("🔍 loadSettings: fetched \(fetched.count) settings entities")
        #endif
        if let existing = fetched.first {
            settings = existing
            #if DEBUG
            debugLog("🔍 loadSettings: familyName=\(existing.familyName), crest=\(existing.familyCrest), hometown=\(existing.hometown)")
            #endif
        } else {
            let newSettings = FamilySettingsEntity()
            ctx.insert(newSettings)
            try? ctx.save()
            settings = newSettings
            #if DEBUG
            debugLog("🔍 loadSettings: created new default settings")
            #endif
        }
    }

    func loadChecklists(for memorialId: String) {
        guard let ctx = modelContext else { return }
        let targetId = memorialId
        let descriptor = FetchDescriptor<MemorialChecklistEntity>(
            predicate: #Predicate<MemorialChecklistEntity> { item in
                item.memorialId == targetId
            },
            sortBy: [SortDescriptor(\.sortOrder)]
        )
        checklists = (try? ctx.fetch(descriptor)) ?? []
    }

    func loadPhotos(for memberId: String) {
        guard let ctx = modelContext else { return }
        let targetId = memberId
        let descriptor = FetchDescriptor<MemberPhotoEntity>(
            predicate: #Predicate<MemberPhotoEntity> { item in
                item.memberId == targetId
            },
            sortBy: [SortDescriptor(\.sortOrder)]
        )
        photos = (try? ctx.fetch(descriptor)) ?? []
    }

    // ── COMPUTED ──

    var filteredMembers: [FamilyMemberEntity] {
        var result = members
        // Apply search
        if !searchQuery.isEmpty {
            let q = searchQuery.lowercased()
            result = result.filter { $0.name.lowercased().contains(q) }
        }
        // Apply filter
        switch activeFilter {
        case .all: break
        case .generation(let gen):
            result = result.filter { $0.generation == gen }
        case .male:
            result = result.filter { $0.gender == "MALE" }
        case .female:
            result = result.filter { $0.gender == "FEMALE" }
        }
        return result
    }

    var generations: [Int] {
        Array(Set(members.map { $0.generation })).sorted()
    }

    var totalGenerations: Int {
        generations.count
    }

    var totalMembers: Int {
        members.count
    }

    var totalMemorials: Int {
        memorials.count
    }

    func membersForGeneration(_ gen: Int) -> [FamilyMemberEntity] {
        filteredMembers.filter { $0.generation == gen }
    }

    func member(byId id: String) -> FamilyMemberEntity? {
        members.first { $0.id == id }
    }

    func memorialsForMember(_ memberId: String) -> [MemorialDayEntity] {
        memorials.filter { $0.memberId == memberId }
    }

    func relatedMembers(for member: FamilyMemberEntity) -> [(member: FamilyMemberEntity, relation: String)] {
        var result: [(FamilyMemberEntity, String)] = []

        // Parents
        let parentIdList = idList(member.parentIds)
        for pid in parentIdList {
            if let parent = self.member(byId: pid) {
                let relation = kinshipTitle(for: parent).isEmpty ? (parent.gender == "MALE" ? "Cha" : "Mẹ") : kinshipTitle(for: parent)
                result.append((parent, relation))
            }
        }

        // Spouses
        let spouseIdList = idList(member.spouseIds)
        for sid in spouseIdList {
            if let spouse = self.member(byId: sid) {
                let relation = kinshipTitle(for: spouse).isEmpty ? (spouse.gender == "MALE" ? "Chồng" : "Vợ") : kinshipTitle(for: spouse)
                result.append((spouse, relation))
            }
        }

        // Children (members who list this member as parent)
        let children = members.filter { m in
            idList(m.parentIds).contains(member.id)
        }
        for child in children {
            let relation = kinshipTitle(for: child).isEmpty ? (child.gender == "MALE" ? "Con trai" : "Con gái") : kinshipTitle(for: child)
            result.append((child, relation))
        }

        return result
    }

    func kinshipTitle(for member: FamilyMemberEntity) -> String {
        kinshipTitle(for: member, in: members)
    }

    // ── CRUD: Members ──

    func addMember(_ member: FamilyMemberEntity) {
        guard let ctx = modelContext else { return }
        ctx.insert(member)
        syncMemorial(for: member, context: ctx)
        try? ctx.save()
        loadAll()
    }

    func deleteMember(_ member: FamilyMemberEntity) {
        guard let ctx = modelContext else { return }
        // Delete related memorials
        let mems = memorials.filter { $0.memberId == member.id }
        for m in mems { ctx.delete(m) }
        // Delete related photos
        let targetMemberId = member.id
        let descriptor = FetchDescriptor<MemberPhotoEntity>(
            predicate: #Predicate<MemberPhotoEntity> { item in
                item.memberId == targetMemberId
            }
        )
        if let phs = try? ctx.fetch(descriptor) {
            for p in phs { ctx.delete(p) }
        }
        ctx.delete(member)
        try? ctx.save()
        loadAll()
    }

    func updateMember(_ member: FamilyMemberEntity) {
        guard let ctx = modelContext else { return }
        member.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
        syncMemorial(for: member, context: ctx)
        try? ctx.save()
        loadAll()
    }

    // ── CRUD: Memorials ──

    func addMemorial(_ memorial: MemorialDayEntity) {
        guard let ctx = modelContext else { return }
        ctx.insert(memorial)
        try? ctx.save()
        loadMemorials()
    }

    func deleteMemorial(_ memorial: MemorialDayEntity) {
        guard let ctx = modelContext else { return }
        // Delete related checklists
        let targetMemorialId = memorial.id
        let descriptor = FetchDescriptor<MemorialChecklistEntity>(
            predicate: #Predicate<MemorialChecklistEntity> { item in
                item.memorialId == targetMemorialId
            }
        )
        if let items = try? ctx.fetch(descriptor) {
            for item in items { ctx.delete(item) }
        }
        ctx.delete(memorial)
        try? ctx.save()
        loadMemorials()
    }

    func updateMemorial(_ memorial: MemorialDayEntity) {
        guard let ctx = modelContext else { return }
        memorial.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
        try? ctx.save()
        loadMemorials()
    }

    // ── CRUD: Checklists ──

    func toggleChecklist(_ item: MemorialChecklistEntity) {
        guard let ctx = modelContext else { return }
        item.isDone.toggle()
        try? ctx.save()
        loadChecklists(for: item.memorialId)
    }

    func addChecklistItem(memorialId: String, text: String) {
        guard let ctx = modelContext else { return }
        let maxOrder = checklists.map { $0.sortOrder }.max() ?? 0
        let item = MemorialChecklistEntity(
            id: Int64(Date().timeIntervalSince1970 * 1000),
            memorialId: memorialId,
            text: text,
            sortOrder: maxOrder + 1
        )
        ctx.insert(item)
        try? ctx.save()
        loadChecklists(for: memorialId)
    }

    func deleteChecklistItem(_ item: MemorialChecklistEntity) {
        guard let ctx = modelContext else { return }
        let mid = item.memorialId
        ctx.delete(item)
        try? ctx.save()
        loadChecklists(for: mid)
    }

    // ── CRUD: Settings ──

    func updateSettings() {
        guard let ctx = modelContext else {
            #if DEBUG
            debugLog("⚠️ updateSettings: modelContext is nil")
            #endif
            return
        }
        guard let s = settings else {
            #if DEBUG
            debugLog("⚠️ updateSettings: settings is nil")
            #endif
            return
        }
        s.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
        do {
            try ctx.save()
            #if DEBUG
            debugLog("✅ updateSettings saved — familyName: \(s.familyName), crest: \(s.familyCrest), hometown: \(s.hometown)")
            #endif
        } catch {
            #if DEBUG
            debugLog("⚠️ updateSettings save error: \(error)")
            #endif
        }
        // Force @Published to fire by re-assigning the reference.
        // Simply calling objectWillChange.send() is not always enough
        // because SwiftUI may not detect nested-property mutations.
        let current = settings
        settings = nil
        settings = current
    }

    func deleteAllFamilyData() {
        guard let ctx = modelContext else { return }
        for m in members { ctx.delete(m) }
        for m in memorials { ctx.delete(m) }
        // Delete all checklists
        if let all = try? ctx.fetch(FetchDescriptor<MemorialChecklistEntity>()) {
            for item in all { ctx.delete(item) }
        }
        // Delete all photos
        if let all = try? ctx.fetch(FetchDescriptor<MemberPhotoEntity>()) {
            for item in all { ctx.delete(item) }
        }
        // Reset settings
        if let s = settings { ctx.delete(s) }
        try? ctx.save()
        members = []
        memorials = []
        checklists = []
        photos = []
        settings = nil
        loadSettings() // Recreate default
    }

    // ── CRUD: Photos ──

    func addPhoto(memberId: String, filePath: String, caption: String? = nil) {
        guard let ctx = modelContext else { return }
        let maxOrder = photos.map { $0.sortOrder }.max() ?? 0
        let photo = MemberPhotoEntity(
            id: Int64(Date().timeIntervalSince1970 * 1000),
            memberId: memberId,
            filePath: filePath,
            caption: caption,
            sortOrder: maxOrder + 1
        )
        ctx.insert(photo)
        try? ctx.save()
        loadPhotos(for: memberId)
    }

    func deletePhoto(_ photo: MemberPhotoEntity) {
        guard let ctx = modelContext else { return }
        let mid = photo.memberId
        // Delete file
        try? FileManager.default.removeItem(atPath: photo.filePath)
        ctx.delete(photo)
        try? ctx.save()
        loadPhotos(for: mid)
    }

    // ── Helpers ──

    func daysUntilMemorial(_ memorial: MemorialDayEntity) -> Int {
        let today = Date()
        let cal = Calendar.current
        let currentYear = cal.component(.year, from: today)

        // Convert lunar memorial date to solar for this year
        var solar = LunarCalendarUtil.convertLunar2Solar(
            lunarDay: memorial.lunarDay,
            lunarMonth: memorial.lunarMonth,
            lunarYear: currentYear,
            lunarLeap: memorial.lunarLeap
        )

        // Create date from solar
        var components = DateComponents()
        components.day = solar.0
        components.month = solar.1
        components.year = solar.2

        guard let memorialDate = cal.date(from: components) else { return 999 }

        let days = cal.dateComponents([.day], from: cal.startOfDay(for: today), to: cal.startOfDay(for: memorialDate)).day ?? 0

        // If already past this year, calculate for next year
        if days < 0 {
            solar = LunarCalendarUtil.convertLunar2Solar(
                lunarDay: memorial.lunarDay,
                lunarMonth: memorial.lunarMonth,
                lunarYear: currentYear + 1,
                lunarLeap: memorial.lunarLeap
            )
            components.day = solar.0
            components.month = solar.1
            components.year = solar.2
            guard let nextDate = cal.date(from: components) else { return 999 }
            return cal.dateComponents([.day], from: cal.startOfDay(for: today), to: cal.startOfDay(for: nextDate)).day ?? 999
        }

        return days
    }

    func solarDateForMemorial(_ memorial: MemorialDayEntity, year: Int? = nil) -> (day: Int, month: Int, year: Int) {
        let y = year ?? Calendar.current.component(.year, from: Date())
        let solar = LunarCalendarUtil.convertLunar2Solar(
            lunarDay: memorial.lunarDay,
            lunarMonth: memorial.lunarMonth,
            lunarYear: y,
            lunarLeap: memorial.lunarLeap
        )
        return (solar.0, solar.1, solar.2)
    }

    func memberAge(_ member: FamilyMemberEntity) -> Int? {
        guard let birthYear = member.birthYear else { return nil }
        return Calendar.current.component(.year, from: Date()) - birthYear
    }

    func memberInitials(_ member: FamilyMemberEntity) -> String {
        let parts = member.name.trimmingCharacters(in: .whitespaces).split(separator: " ").compactMap { $0.first.map(String.init) }
        if parts.count >= 2 { return "\(parts.first!)\(parts.last!)" }
        return parts.first ?? "?"
    }

    func isDeceased(_ member: FamilyMemberEntity) -> Bool {
        return member.deathYear != nil
    }

    // ── Kinship labels, mirrored from Android FamilyTreeViewModel ──

    private func kinshipTitle(for member: FamilyMemberEntity, in sourceMembers: [FamilyMemberEntity]) -> String {
        guard let selfMember = sourceMembers.first(where: { $0.isSelf }) else {
            return "\(member.role) · Đời \(member.generation)"
        }
        if member.id == selfMember.id { return "Bản thân" }

        let byId = Dictionary(uniqueKeysWithValues: sourceMembers.map { ($0.id, $0) })
        let selfParents = idList(selfMember.parentIds).compactMap { byId[$0] }

        if idList(selfMember.parentIds).contains(member.id) {
            return member.gender == "MALE" ? "Cha" : "Mẹ"
        }
        if idList(member.spouseIds).contains(selfMember.id) || idList(selfMember.spouseIds).contains(member.id) {
            return member.gender == "MALE" ? "Chồng" : "Vợ"
        }
        if idList(member.parentIds).contains(selfMember.id) {
            return member.gender == "MALE" ? "Con trai" : "Con gái"
        }

        let selfParentIds = idList(selfMember.parentIds)
        if !selfParentIds.isEmpty, member.id != selfMember.id,
           idList(member.parentIds).contains(where: { selfParentIds.contains($0) }) {
            let older: Bool
            if let memberBirth = member.birthYear, let selfBirth = selfMember.birthYear {
                older = memberBirth < selfBirth
            } else {
                older = member.isElder
            }
            switch (member.gender, older) {
            case ("MALE", true): return "Anh trai"
            case ("MALE", false): return "Em trai"
            case ("FEMALE", true): return "Chị gái"
            default: return "Em gái"
            }
        }

        if let path = ancestorPath(from: selfMember, targetId: member.id, byId: byId) {
            let side = path.first.flatMap { byId[$0] }.map { $0.gender == "MALE" ? "nội" : "ngoại" } ?? ""
            return ancestorTitle(depth: path.count, gender: member.gender, side: side)
        }

        if let depth = descendantDepth(from: member, ancestorId: selfMember.id, byId: byId) {
            return descendantTitle(depth: depth, gender: member.gender)
        }

        if let title = parentSiblingTitle(for: member, selfParents: selfParents, byId: byId) {
            return title
        }

        return "\(member.role) · Đời \(member.generation)"
    }

    private func ancestorPath(
        from current: FamilyMemberEntity,
        targetId: String,
        byId: [String: FamilyMemberEntity],
        path: [String] = []
    ) -> [String]? {
        for parentId in idList(current.parentIds) {
            let nextPath = path + [parentId]
            if parentId == targetId { return nextPath }
            guard let parent = byId[parentId] else { continue }
            if let found = ancestorPath(from: parent, targetId: targetId, byId: byId, path: nextPath) {
                return found
            }
        }
        return nil
    }

    private func descendantDepth(
        from member: FamilyMemberEntity,
        ancestorId: String,
        byId: [String: FamilyMemberEntity]
    ) -> Int? {
        if idList(member.parentIds).contains(ancestorId) { return 1 }
        for parentId in idList(member.parentIds) {
            guard let parent = byId[parentId] else { continue }
            if let depth = descendantDepth(from: parent, ancestorId: ancestorId, byId: byId) {
                return depth + 1
            }
        }
        return nil
    }

    private func ancestorTitle(depth: Int, gender: String, side: String) -> String {
        let base: String
        switch depth {
        case 1:
            base = gender == "MALE" ? "Cha" : "Mẹ"
        case 2:
            base = gender == "MALE" ? "Ông" : "Bà"
        case 3:
            base = gender == "MALE" ? "Cụ ông" : "Cụ bà"
        case 4:
            base = gender == "MALE" ? "Kỵ ông" : "Kỵ bà"
        default:
            base = gender == "MALE" ? "Cụ tổ ông" : "Cụ tổ bà"
        }
        return (2...4).contains(depth) && !side.isEmpty ? "\(base) \(side)" : base
    }

    private func descendantTitle(depth: Int, gender: String) -> String {
        let base: String
        switch depth {
        case 1: base = "Con"
        case 2: base = "Cháu"
        case 3: base = "Chắt"
        case 4: base = "Chút"
        default: return "Hậu duệ đời \(depth)"
        }
        return "\(base) \(gender == "MALE" ? "trai" : "gái")"
    }

    private func parentSiblingTitle(
        for member: FamilyMemberEntity,
        selfParents: [FamilyMemberEntity],
        byId: [String: FamilyMemberEntity],
        includeSpouse: Bool = true
    ) -> String? {
        for parent in selfParents {
            let parentParentIds = idList(parent.parentIds)
            let sharesGrandParent = !parentParentIds.isEmpty && idList(member.parentIds).contains(where: { parentParentIds.contains($0) })
            if !sharesGrandParent || member.id == parent.id { continue }

            let olderThanParent: Bool
            if let memberBirth = member.birthYear, let parentBirth = parent.birthYear {
                olderThanParent = memberBirth < parentBirth
            } else {
                olderThanParent = member.isElder
            }
            let parentIsFather = parent.gender == "MALE"

            if parentIsFather && member.gender == "MALE" && olderThanParent { return "Bác trai" }
            if parentIsFather && member.gender == "MALE" { return "Chú" }
            if parentIsFather && member.gender == "FEMALE" && olderThanParent { return "Bác gái" }
            if parentIsFather { return "Cô" }
            if !parentIsFather && member.gender == "MALE" { return "Cậu" }
            if olderThanParent { return "Bác gái" }
            return "Dì"
        }

        guard includeSpouse else { return nil }
        return sourceRelativeSpouse(for: member, selfParents: selfParents, byId: byId)
    }

    private func sourceRelativeSpouse(
        for member: FamilyMemberEntity,
        selfParents: [FamilyMemberEntity],
        byId: [String: FamilyMemberEntity]
    ) -> String? {
        let spouse = idList(member.spouseIds).compactMap { byId[$0] }.first
            ?? byId.values.first(where: { idList($0.spouseIds).contains(member.id) })
        guard let spouse,
              let spouseTitle = parentSiblingTitle(for: spouse, selfParents: selfParents, byId: byId, includeSpouse: false) else {
            return nil
        }
        switch spouseTitle {
        case "Bác trai": return "Bác gái"
        case "Bác gái": return "Bác trai"
        case "Chú": return "Thím"
        case "Cô": return "Chú"
        case "Cậu": return "Mợ"
        case "Dì": return "Dượng"
        default: return nil
        }
    }

    private func syncMemorial(for member: FamilyMemberEntity, context: ModelContext) {
        guard member.deathYear != nil,
              let lunar = parseLunarDate(member.deathDateLunar),
              (1...30).contains(lunar.day),
              (1...12).contains(lunar.month) else {
            return
        }

        let relation = kinshipTitle(for: member, in: members.filter { $0.id != member.id } + [member])
        if let existing = memorials.first(where: { $0.memberId == member.id }) {
            existing.memberName = member.name
            existing.relation = relation
            existing.lunarDay = lunar.day
            existing.lunarMonth = lunar.month
            existing.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
        } else {
            let memorial = MemorialDayEntity(
                id: UUID().uuidString,
                memberId: member.id,
                memberName: member.name,
                relation: relation,
                lunarDay: lunar.day,
                lunarMonth: lunar.month
            )
            context.insert(memorial)
        }
    }

    private func parseLunarDate(_ raw: String?) -> (day: Int, month: Int, year: Int)? {
        guard let raw else { return nil }
        let parts = raw.split(separator: "/").compactMap { Int($0.trimmingCharacters(in: .whitespaces)) }
        guard parts.count >= 3 else { return nil }
        return (parts[0], parts[1], parts[2])
    }

    private func idList(_ raw: String) -> [String] {
        raw.split(separator: ",")
            .map { String($0).trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }
    }
}
