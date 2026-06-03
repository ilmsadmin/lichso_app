import Foundation

public struct ApiResponse<T: Codable>: Codable {
    public let success: Bool
    public let message: String?
    public let data: T?
}

public struct QuizQuestion: Codable, Identifiable, Hashable {
    public var id: Int64
    public let content: String
    public let optionA: String
    public let optionB: String
    public let optionC: String
    public let optionD: String
    public let category: String
    public let difficulty: String
    public let articleId: Int64?
    public let correct: String?
    public let correctAnswer: String?
    public let hint: String?
    public let explanation: String?

    enum CodingKeys: String, CodingKey {
        case id, content, category, difficulty, correct, hint, explanation
        case optionA = "option_a"
        case optionB = "option_b"
        case optionC = "option_c"
        case optionD = "option_d"
        case articleId = "article_id"
        case correctAnswer = "correct_answer"
    }
}

public struct SubmitAnswerResult: Codable {
    public let questionId: Int64
    public let chosen: String
    public let correct: String
    public let isCorrect: Bool
    public let explanation: String?
    public let articleId: Int64?
    public let pointsEarned: Int
    public var timeMs: Int

    enum CodingKeys: String, CodingKey {
        case chosen, correct, explanation
        case questionId = "question_id"
        case isCorrect = "is_correct"
        case articleId = "article_id"
        case pointsEarned = "points_earned"
        case timeMs = "time_ms"
    }

    public init(
        questionId: Int64,
        chosen: String,
        correct: String,
        isCorrect: Bool,
        explanation: String?,
        articleId: Int64?,
        pointsEarned: Int,
        timeMs: Int = 0
    ) {
        self.questionId = questionId
        self.chosen = chosen
        self.correct = correct
        self.isCorrect = isCorrect
        self.explanation = explanation
        self.articleId = articleId
        self.pointsEarned = pointsEarned
        self.timeMs = timeMs
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        questionId = try container.decode(Int64.self, forKey: .questionId)
        chosen = try container.decode(String.self, forKey: .chosen)
        correct = try container.decode(String.self, forKey: .correct)
        isCorrect = try container.decode(Bool.self, forKey: .isCorrect)
        explanation = try container.decodeIfPresent(String.self, forKey: .explanation)
        articleId = try container.decodeIfPresent(Int64.self, forKey: .articleId)
        pointsEarned = try container.decodeIfPresent(Int.self, forKey: .pointsEarned) ?? 0
        timeMs = try container.decodeIfPresent(Int.self, forKey: .timeMs) ?? 0
    }
}

public struct SessionResult: Codable {
    public let sessionId: String
    public let score: Int
    public let scoreV2: Int
    public let total: Int
    public let pointsEarned: Int
    public let bonusPoints: Int
    public let newWeekScore: Int
    public let rank: Int
    public let appPointsEarned: Int
    public let xpEarned: Int
    public let sessionTitle: String
    public let unlockedMasteryTitles: [String]
    public let unlockedBadges: [String]

    enum CodingKeys: String, CodingKey {
        case score, total, rank
        case sessionId = "session_id"
        case scoreV2 = "score_v2"
        case pointsEarned = "points_earned"
        case bonusPoints = "bonus_points"
        case newWeekScore = "new_week_score"
        case appPointsEarned = "app_points_earned"
        case xpEarned = "xp_earned"
        case sessionTitle = "session_title"
        case unlockedMasteryTitles = "unlocked_mastery_titles"
        case unlockedBadges = "unlocked_badges"
    }
}

public struct PointWallet: Codable {
    public let userId: String
    public let balance: Int
    public let lifetimeEarned: Int
    public let lifetimeSpent: Int
    public let updatedAt: String

    enum CodingKeys: String, CodingKey {
        case balance
        case userId = "user_id"
        case lifetimeEarned = "lifetime_earned"
        case lifetimeSpent = "lifetime_spent"
        case updatedAt = "updated_at"
    }
}

public struct LeaderboardEntry: Codable, Identifiable {
    public var id: String { userId }
    public var rank: Int?
    public let userId: String
    public let displayName: String
    public let avatarUrl: String?
    public let weekScore: Int
    public let monthScore: Int
    public let totalScore: Int

    enum CodingKeys: String, CodingKey {
        case userId = "user_id"
        case displayName = "display_name"
        case avatarUrl = "avatar_url"
        case weekScore = "week_score"
        case monthScore = "month_score"
        case totalScore = "total_score"
    }
}

public struct LeaderboardResponse: Codable {
    public let entries: [LeaderboardEntry]
}

public struct MyRankResponse: Codable {
    public let rank: Int
    public let weekScore: Int
    public let monthScore: Int
    public let totalScore: Int
    public let curStreak: Int

    enum CodingKeys: String, CodingKey {
        case rank
        case weekScore = "week_score"
        case monthScore = "month_score"
        case totalScore = "total_score"
        case curStreak = "cur_streak"
    }
}

public struct QuizSession: Codable {
    public let id: String
    public let sessionType: String
    public let category: String?
    public let questionIds: [Int64]
    public let questions: [QuizQuestion]?

    enum CodingKeys: String, CodingKey {
        case id, category, questions
        case sessionType = "session_type"
        case questionIds = "question_ids"
    }
}

public struct StartSessionResponse: Codable {
    public let session: QuizSession
    public let questions: [QuizQuestion]
}

public struct OfflineQuizAnswerPayload: Codable {
    public let questionId: Int64
    public let chosen: String
    public let timeMs: Int

    enum CodingKeys: String, CodingKey {
        case chosen
        case questionId = "question_id"
        case timeMs = "time_ms"
    }
}

public struct OfflineQuizSessionPayload: Codable {
    public let clientSessionId: String
    public let sessionType: String
    public let category: String?
    public let questionIds: [Int64]
    public let answers: [OfflineQuizAnswerPayload]
    public let startedAtMs: Int64
    public let finishedAtMs: Int64

    enum CodingKeys: String, CodingKey {
        case category, answers
        case clientSessionId = "client_session_id"
        case sessionType = "session_type"
        case questionIds = "question_ids"
        case startedAtMs = "started_at_ms"
        case finishedAtMs = "finished_at_ms"
    }
}

public struct SyncOfflineQuizResponse: Codable {
    public let syncedSessionIds: [String]
    public let skippedSessionIds: [String]

    enum CodingKeys: String, CodingKey {
        case syncedSessionIds = "synced_session_ids"
        case skippedSessionIds = "skipped_session_ids"
    }
}

public struct ArticleCategory: Codable, Hashable, Identifiable {
    public let id: String
    public let name: String
    public let slug: String
    public let imageUrl: String?
    public let children: [ArticleCategory]?

    enum CodingKeys: String, CodingKey {
        case id, name, slug, children
        case imageUrl = "image_url"
    }

    public init(id: String, name: String, slug: String, imageUrl: String? = nil, children: [ArticleCategory]? = nil) {
        self.id = id
        self.name = name
        self.slug = slug
        self.imageUrl = imageUrl
        self.children = children
    }

    public init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try c.decode(String.self, forKey: .id)
        name = try c.decodeIfPresent(String.self, forKey: .name) ?? ""
        slug = try c.decodeIfPresent(String.self, forKey: .slug) ?? ""
        imageUrl = try c.decodeIfPresent(String.self, forKey: .imageUrl)
        children = try c.decodeIfPresent([ArticleCategory].self, forKey: .children)
    }
}

// ── Khám phá (Knowledge Feed) — mirror Android ContentEvent/FamousPerson/Quote ──

public struct ContentEvent: Codable, Identifiable, Hashable {
    public let id: Int64
    public let title: String
    public let description: String?
    public let eventDay: Int
    public let eventMonth: Int
    public let eventYear: Int?
    public let imageUrl: String?
    public let shortDescription: String?

    enum CodingKeys: String, CodingKey {
        case id, title, description
        case eventDay = "event_day"
        case eventMonth = "event_month"
        case eventYear = "event_year"
        case imageUrl = "image_url"
        case shortDescription = "short_description"
    }

    public init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = (try? c.decode(Int64.self, forKey: .id)) ?? 0
        title = try c.decodeIfPresent(String.self, forKey: .title) ?? ""
        description = try c.decodeIfPresent(String.self, forKey: .description)
        eventDay = try c.decodeIfPresent(Int.self, forKey: .eventDay) ?? 0
        eventMonth = try c.decodeIfPresent(Int.self, forKey: .eventMonth) ?? 0
        eventYear = try c.decodeIfPresent(Int.self, forKey: .eventYear)
        imageUrl = try c.decodeIfPresent(String.self, forKey: .imageUrl)
        shortDescription = try c.decodeIfPresent(String.self, forKey: .shortDescription)
    }
}

public struct FamousPerson: Codable, Identifiable, Hashable {
    public let id: String
    public let name: String
    public let biography: String?
    public let birthYear: Int?
    public let avatarUrl: String?
    public let description: String?

    enum CodingKeys: String, CodingKey {
        case id, name, biography, description
        case birthYear = "birth_year"
        case avatarUrl = "avatar_url"
    }
}

public struct Quote: Codable, Identifiable, Hashable {
    public let id: String
    public let quote: String
    public let author: String?
}

// ═══════════════════════════════════════════
// Home banners & popups (matches Android Banner/Popup)
// ═══════════════════════════════════════════

public struct Banner: Codable, Identifiable, Hashable {
    public let id: String
    public let title: String
    public let subtitle: String?
    public let imageUrl: String?
    public let iconUrl: String?
    public let iconKey: String?
    public let ctaText: String?
    public let ctaType: String?
    public let ctaRoute: String?
    public let bgColor: String?
    public let type: String?
    public let locations: [String]?   // backend mới: banner gắn theo nhiều "location key"
    public let active: Bool
    public let sortOrder: Int

    enum CodingKeys: String, CodingKey {
        case id, title, subtitle, type, locations
        case imageUrl = "image_url"
        case iconUrl = "icon_url"
        case iconKey = "icon_key"
        case ctaText = "cta_text"
        case ctaType = "cta_type"
        case ctaRoute = "cta_route"
        case bgColor = "bg_color"
        case active = "is_active"
        case sortOrder = "sort_order"
    }

    public init(
        id: String,
        title: String,
        subtitle: String? = nil,
        imageUrl: String? = nil,
        iconUrl: String? = nil,
        iconKey: String? = nil,
        ctaText: String? = nil,
        ctaType: String? = nil,
        ctaRoute: String? = nil,
        bgColor: String? = nil,
        type: String? = nil,
        locations: [String]? = nil,
        active: Bool = true,
        sortOrder: Int = 0
    ) {
        self.id = id
        self.title = title
        self.subtitle = subtitle
        self.imageUrl = imageUrl
        self.iconUrl = iconUrl
        self.iconKey = iconKey
        self.ctaText = ctaText
        self.ctaType = ctaType
        self.ctaRoute = ctaRoute
        self.bgColor = bgColor
        self.type = type
        self.locations = locations
        self.active = active
        self.sortOrder = sortOrder
    }

    public init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try c.decode(String.self, forKey: .id)
        title = try c.decodeIfPresent(String.self, forKey: .title) ?? ""
        subtitle = try c.decodeIfPresent(String.self, forKey: .subtitle)
        imageUrl = try c.decodeIfPresent(String.self, forKey: .imageUrl)
        iconUrl = try c.decodeIfPresent(String.self, forKey: .iconUrl)
        iconKey = try c.decodeIfPresent(String.self, forKey: .iconKey)
        ctaText = try c.decodeIfPresent(String.self, forKey: .ctaText)
        ctaType = try c.decodeIfPresent(String.self, forKey: .ctaType)
        ctaRoute = try c.decodeIfPresent(String.self, forKey: .ctaRoute)
        bgColor = try c.decodeIfPresent(String.self, forKey: .bgColor)
        type = try c.decodeIfPresent(String.self, forKey: .type)
        locations = try c.decodeIfPresent([String].self, forKey: .locations)
        active = try c.decodeIfPresent(Bool.self, forKey: .active) ?? true
        sortOrder = try c.decodeIfPresent(Int.self, forKey: .sortOrder) ?? 0
    }
}

public struct Popup: Codable, Identifiable, Hashable {
    public let id: String
    public let title: String
    public let imageUrl: String
    public let ctaType: String?
    public let ctaRoute: String?
    public let active: Bool
    public let position: String?

    enum CodingKeys: String, CodingKey {
        case id, title, position
        case imageUrl = "image_url"
        case ctaType = "cta_type"
        case ctaRoute = "cta_route"
        case active = "is_active"
    }

    public init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try c.decode(String.self, forKey: .id)
        title = try c.decodeIfPresent(String.self, forKey: .title) ?? ""
        imageUrl = try c.decodeIfPresent(String.self, forKey: .imageUrl) ?? ""
        ctaType = try c.decodeIfPresent(String.self, forKey: .ctaType)
        ctaRoute = try c.decodeIfPresent(String.self, forKey: .ctaRoute)
        active = try c.decodeIfPresent(Bool.self, forKey: .active) ?? true
        position = try c.decodeIfPresent(String.self, forKey: .position)
    }
}

public struct Article: Codable, Identifiable, Hashable {
    public let id: String
    public let title: String
    public let slug: String
    public let excerpt: String?
    public let content: String?
    public let featuredImage: String?
    public let publishedAt: String?
    public let category: ArticleCategory?
    public let readingTime: Int?

    enum CodingKeys: String, CodingKey {
        case id, title, slug, excerpt, content, category
        case featuredImage = "featured_image"
        case publishedAt = "published_at"
        case readingTime = "reading_time"
    }
}
