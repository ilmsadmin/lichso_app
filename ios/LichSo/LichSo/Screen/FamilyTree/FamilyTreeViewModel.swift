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
        print("🔍 loadSettings: fetched \(fetched.count) settings entities")
        #endif
        if let existing = fetched.first {
            settings = existing
            #if DEBUG
            print("🔍 loadSettings: familyName=\(existing.familyName), crest=\(existing.familyCrest), hometown=\(existing.hometown)")
            #endif
        } else {
            let newSettings = FamilySettingsEntity()
            ctx.insert(newSettings)
            try? ctx.save()
            settings = newSettings
            #if DEBUG
            print("🔍 loadSettings: created new default settings")
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
        let parentIdList = member.parentIds.split(separator: ",").map { String($0).trimmingCharacters(in: .whitespaces) }
        for pid in parentIdList {
            if let parent = self.member(byId: pid) {
                result.append((parent, parent.gender == "MALE" ? "Cha" : "Mẹ"))
            }
        }

        // Spouses
        let spouseIdList = member.spouseIds.split(separator: ",").map { String($0).trimmingCharacters(in: .whitespaces) }
        for sid in spouseIdList {
            if let spouse = self.member(byId: sid) {
                result.append((spouse, spouse.gender == "MALE" ? "Chồng" : "Vợ"))
            }
        }

        // Children (members who list this member as parent)
        let children = members.filter { m in
            m.parentIds.split(separator: ",").map { String($0).trimmingCharacters(in: .whitespaces) }.contains(member.id)
        }
        for child in children {
            result.append((child, child.gender == "MALE" ? "Con trai" : "Con gái"))
        }

        return result
    }

    // ── CRUD: Members ──

    func addMember(_ member: FamilyMemberEntity) {
        guard let ctx = modelContext else { return }
        ctx.insert(member)
        try? ctx.save()
        loadMembers()
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
        try? ctx.save()
        loadMembers()
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
            print("⚠️ updateSettings: modelContext is nil")
            #endif
            return
        }
        guard let s = settings else {
            #if DEBUG
            print("⚠️ updateSettings: settings is nil")
            #endif
            return
        }
        s.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
        do {
            try ctx.save()
            #if DEBUG
            print("✅ updateSettings saved — familyName: \(s.familyName), crest: \(s.familyCrest), hometown: \(s.hometown)")
            #endif
        } catch {
            #if DEBUG
            print("⚠️ updateSettings save error: \(error)")
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
}
