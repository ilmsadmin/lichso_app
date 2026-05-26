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

public struct ArticleCategory: Codable, Hashable {
    public let id: String
    public let name: String
    public let slug: String
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
