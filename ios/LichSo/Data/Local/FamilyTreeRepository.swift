import Foundation
import SwiftData

// MARK: - Family Tree Repository

@MainActor
class FamilyTreeRepository: ObservableObject {
    private let modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    // MARK: - Members

    func getAllMembers() -> [FamilyMemberEntity] {
        let descriptor = FetchDescriptor<FamilyMemberEntity>(
            sortBy: [SortDescriptor(\.generation), SortDescriptor(\.name)]
        )
        return (try? modelContext.fetch(descriptor)) ?? []
    }

    func getMemberById(_ id: String) -> FamilyMemberEntity? {
        let descriptor = FetchDescriptor<FamilyMemberEntity>(
            predicate: #Predicate { $0.id == id }
        )
        return try? modelContext.fetch(descriptor).first
    }

    func searchMembers(query: String) -> [FamilyMemberEntity] {
        let descriptor = FetchDescriptor<FamilyMemberEntity>(
            predicate: #Predicate { member in
                member.name.localizedStandardContains(query) ||
                member.role.localizedStandardContains(query)
            },
            sortBy: [SortDescriptor(\.generation), SortDescriptor(\.name)]
        )
        return (try? modelContext.fetch(descriptor)) ?? []
    }

    func getMemberCount() -> Int {
        let descriptor = FetchDescriptor<FamilyMemberEntity>()
        return (try? modelContext.fetchCount(descriptor)) ?? 0
    }

    func addMember(_ member: FamilyMemberEntity) {
        modelContext.insert(member)
        try? modelContext.save()
    }

    func updateMember(_ member: FamilyMemberEntity) {
        member.updatedAt = Date()
        try? modelContext.save()
    }

    func deleteMember(_ id: String) {
        if let member = getMemberById(id) {
            modelContext.delete(member)
            // Also delete associated memorial
            if let memorial = getMemorialByMemberId(id) {
                deleteChecklistItems(forMemorialId: memorial.id)
                modelContext.delete(memorial)
            }
            try? modelContext.save()
        }
    }

    func deleteAllMembers() {
        try? modelContext.delete(model: FamilyMemberEntity.self)
        try? modelContext.delete(model: MemorialDayEntity.self)
        try? modelContext.save()
    }

    // MARK: - Memorials

    func getAllMemorials() -> [MemorialDayEntity] {
        let descriptor = FetchDescriptor<MemorialDayEntity>(
            sortBy: [SortDescriptor(\.lunarMonth), SortDescriptor(\.lunarDay)]
        )
        return (try? modelContext.fetch(descriptor)) ?? []
    }

    func getMemorialById(_ id: String) -> MemorialDayEntity? {
        let descriptor = FetchDescriptor<MemorialDayEntity>(
            predicate: #Predicate { $0.id == id }
        )
        return try? modelContext.fetch(descriptor).first
    }

    func getMemorialByMemberId(_ memberId: String) -> MemorialDayEntity? {
        let descriptor = FetchDescriptor<MemorialDayEntity>(
            predicate: #Predicate { $0.memberId == memberId }
        )
        return try? modelContext.fetch(descriptor).first
    }

    func addMemorial(_ memorial: MemorialDayEntity) {
        modelContext.insert(memorial)
        try? modelContext.save()
    }

    func updateMemorial(_ memorial: MemorialDayEntity) {
        memorial.updatedAt = Date()
        try? modelContext.save()
    }

    func deleteMemorial(_ id: String) {
        deleteChecklistItems(forMemorialId: id)
        if let memorial = getMemorialById(id) {
            modelContext.delete(memorial)
            try? modelContext.save()
        }
    }

    // MARK: - Memorial Checklist

    func getChecklist(forMemorialId memorialId: String) -> [MemorialChecklistEntity] {
        let descriptor = FetchDescriptor<MemorialChecklistEntity>(
            predicate: #Predicate { $0.memorialId == memorialId },
            sortBy: [SortDescriptor(\.sortOrder)]
        )
        return (try? modelContext.fetch(descriptor)) ?? []
    }

    func addChecklistItem(memorialId: String, text: String, sortOrder: Int = 0) {
        let item = MemorialChecklistEntity(memorialId: memorialId, text: text, sortOrder: sortOrder)
        modelContext.insert(item)
        try? modelContext.save()
    }

    func toggleChecklistItem(_ item: MemorialChecklistEntity) {
        item.isDone.toggle()
        try? modelContext.save()
    }

    func deleteChecklistItem(_ item: MemorialChecklistEntity) {
        modelContext.delete(item)
        try? modelContext.save()
    }

    private func deleteChecklistItems(forMemorialId memorialId: String) {
        let items = getChecklist(forMemorialId: memorialId)
        for item in items {
            modelContext.delete(item)
        }
    }

    // MARK: - Settings

    func getSettings() -> FamilySettingsEntity {
        let descriptor = FetchDescriptor<FamilySettingsEntity>()
        if let settings = try? modelContext.fetch(descriptor).first {
            return settings
        }
        let newSettings = FamilySettingsEntity()
        modelContext.insert(newSettings)
        try? modelContext.save()
        return newSettings
    }

    func updateSettings(_ settings: FamilySettingsEntity) {
        settings.updatedAt = Date()
        try? modelContext.save()
    }

    // MARK: - Member Photos

    func getPhotos(forMemberId memberId: String) -> [MemberPhotoEntity] {
        let descriptor = FetchDescriptor<MemberPhotoEntity>(
            predicate: #Predicate { $0.memberId == memberId },
            sortBy: [SortDescriptor(\.sortOrder)]
        )
        return (try? modelContext.fetch(descriptor)) ?? []
    }

    func addPhoto(memberId: String, filePath: String, caption: String? = nil) {
        let count = getPhotos(forMemberId: memberId).count
        let photo = MemberPhotoEntity(memberId: memberId, filePath: filePath, caption: caption, sortOrder: count)
        modelContext.insert(photo)
        try? modelContext.save()
    }

    func deletePhoto(_ photo: MemberPhotoEntity) {
        modelContext.delete(photo)
        try? modelContext.save()
    }

    // MARK: - Seed

    func seedIfEmpty() {
        let descriptor = FetchDescriptor<FamilySettingsEntity>()
        if (try? modelContext.fetch(descriptor).first) == nil {
            modelContext.insert(FamilySettingsEntity())
            try? modelContext.save()
        }
    }
}
