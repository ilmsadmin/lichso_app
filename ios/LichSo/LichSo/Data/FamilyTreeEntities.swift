import Foundation
import SwiftData

// ═══════════════════════════════════════════
// Family Tree Entities — Giữ nguyên cấu trúc từ Android Room
// KHÔNG ĐƯỢC THAY ĐỔI CẤU TRÚC DATABASE
// ═══════════════════════════════════════════

@Model
final class FamilyMemberEntity {
    @Attribute(.unique) var id: String
    var name: String
    var role: String
    var gender: String              // "MALE" | "FEMALE"
    var generation: Int
    var birthYear: Int?
    var deathYear: Int?
    var birthDateLunar: String?
    var deathDateLunar: String?
    var canChi: String?
    var menh: String?
    var zodiacEmoji: String?
    var menhEmoji: String?
    var hanhEmoji: String?
    var menhDetail: String?
    var zodiacName: String?
    var menhName: String?
    var hometown: String?
    var occupation: String?
    var isSelf: Bool
    var isElder: Bool
    var emoji: String
    var spouseIds: String           // comma-separated spouse IDs
    var spouseOrder: Int
    var parentIds: String           // comma-separated IDs
    var note: String?
    var avatarPath: String?
    var createdAt: Int64
    var updatedAt: Int64

    init(id: String, name: String, role: String, gender: String, generation: Int,
         birthYear: Int? = nil, deathYear: Int? = nil,
         birthDateLunar: String? = nil, deathDateLunar: String? = nil,
         canChi: String? = nil, menh: String? = nil,
         zodiacEmoji: String? = nil, menhEmoji: String? = nil,
         hanhEmoji: String? = nil, menhDetail: String? = nil,
         zodiacName: String? = nil, menhName: String? = nil,
         hometown: String? = nil, occupation: String? = nil,
         isSelf: Bool = false, isElder: Bool = false,
         emoji: String = "👤", spouseIds: String = "", spouseOrder: Int = 0,
         parentIds: String = "", note: String? = nil, avatarPath: String? = nil) {
        self.id = id
        self.name = name
        self.role = role
        self.gender = gender
        self.generation = generation
        self.birthYear = birthYear
        self.deathYear = deathYear
        self.birthDateLunar = birthDateLunar
        self.deathDateLunar = deathDateLunar
        self.canChi = canChi
        self.menh = menh
        self.zodiacEmoji = zodiacEmoji
        self.menhEmoji = menhEmoji
        self.hanhEmoji = hanhEmoji
        self.menhDetail = menhDetail
        self.zodiacName = zodiacName
        self.menhName = menhName
        self.hometown = hometown
        self.occupation = occupation
        self.isSelf = isSelf
        self.isElder = isElder
        self.emoji = emoji
        self.spouseIds = spouseIds
        self.spouseOrder = spouseOrder
        self.parentIds = parentIds
        self.note = note
        self.avatarPath = avatarPath
        self.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
        self.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
    }
}

@Model
final class MemorialDayEntity {
    @Attribute(.unique) var id: String
    var memberId: String
    var memberName: String
    var relation: String
    var lunarDay: Int
    var lunarMonth: Int
    var lunarLeap: Int
    var note: String?
    var remindBefore3Days: Bool
    var remindBefore1Day: Bool
    var createdAt: Int64
    var updatedAt: Int64

    init(id: String, memberId: String, memberName: String, relation: String,
         lunarDay: Int, lunarMonth: Int, lunarLeap: Int = 0, note: String? = nil,
         remindBefore3Days: Bool = true, remindBefore1Day: Bool = true) {
        self.id = id
        self.memberId = memberId
        self.memberName = memberName
        self.relation = relation
        self.lunarDay = lunarDay
        self.lunarMonth = lunarMonth
        self.lunarLeap = lunarLeap
        self.note = note
        self.remindBefore3Days = remindBefore3Days
        self.remindBefore1Day = remindBefore1Day
        self.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
        self.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
    }
}

@Model
final class MemorialChecklistEntity {
    @Attribute(.unique) var id: Int64
    var memorialId: String
    var text: String
    var isDone: Bool
    var sortOrder: Int

    init(id: Int64 = 0, memorialId: String, text: String, isDone: Bool = false, sortOrder: Int = 0) {
        self.id = id
        self.memorialId = memorialId
        self.text = text
        self.isDone = isDone
        self.sortOrder = sortOrder
    }
}

@Model
final class FamilySettingsEntity {
    @Attribute(.unique) var id: Int
    var familyName: String
    var familyCrest: String
    var hometown: String
    var treeDisplayMode: String     // vertical, horizontal, fan
    var treeTheme: String           // classic, modern
    var showAvatar: Bool
    var showYears: Bool
    var remindMemorial: Bool
    var remindBirthday: Bool
    var remindDaysBefore: Int
    var createdAt: Int64
    var updatedAt: Int64

    init(id: Int = 1, familyName: String = "Gia phả của tôi", familyCrest: String = "GP",
         hometown: String = "", treeDisplayMode: String = "vertical", treeTheme: String = "classic",
         showAvatar: Bool = true, showYears: Bool = true, remindMemorial: Bool = true,
         remindBirthday: Bool = true, remindDaysBefore: Int = 3) {
        self.id = id
        self.familyName = familyName
        self.familyCrest = familyCrest
        self.hometown = hometown
        self.treeDisplayMode = treeDisplayMode
        self.treeTheme = treeTheme
        self.showAvatar = showAvatar
        self.showYears = showYears
        self.remindMemorial = remindMemorial
        self.remindBirthday = remindBirthday
        self.remindDaysBefore = remindDaysBefore
        self.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
        self.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
    }
}

@Model
final class MemberPhotoEntity {
    @Attribute(.unique) var id: Int64
    var memberId: String
    var filePath: String
    var caption: String?
    var sortOrder: Int
    var createdAt: Int64

    init(id: Int64 = 0, memberId: String, filePath: String, caption: String? = nil, sortOrder: Int = 0) {
        self.id = id
        self.memberId = memberId
        self.filePath = filePath
        self.caption = caption
        self.sortOrder = sortOrder
        self.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
    }
}
