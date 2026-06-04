import Foundation
import UIKit

@MainActor
public class QuizService: ObservableObject {
    public static let shared = QuizService()
    
    private let baseURL = "https://api.lichso.vn/api"
    
    private init() {
        // Configure shared URLCache: 50MB memory, 150MB disk
        let memoryCapacity = 50 * 1024 * 1024
        let diskCapacity = 150 * 1024 * 1024
        let cache = URLCache(memoryCapacity: memoryCapacity, diskCapacity: diskCapacity, diskPath: "lichso_api_cache")
        URLCache.shared = cache
    }
    
    private var token: String? {
        TokenStore.accessToken
    }

    private var hasValidToken: Bool {
        guard let token, !token.isEmpty else { return false }
        return true
    }

    private func makeRequest(path: String, method: String = "GET", body: Data? = nil) -> URLRequest {
        var request = URLRequest(url: URL(string: "\(baseURL)\(path)")!)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.cachePolicy = .useProtocolCachePolicy
        applyClientHeaders(to: &request)
        if let token = self.token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        request.httpBody = body
        return request
    }
    
    private func execute<T: Codable>(_ request: URLRequest) async throws -> T {
        let (data, httpResponse) = try await performRequestWithAuthRetry(request)
        guard (200...299).contains(httpResponse.statusCode) else {
            let body = String(data: data, encoding: .utf8) ?? ""
            throw NSError(
                domain: "QuizService",
                code: httpResponse.statusCode,
                userInfo: [NSLocalizedDescriptionKey: "Yêu cầu mạng thất bại (\(httpResponse.statusCode)): \(body)"]
            )
        }
        
        let decoder = JSONDecoder()
        let wrapper = try decoder.decode(ApiResponse<T>.self, from: data)
        if !wrapper.success, let msg = wrapper.message {
            throw NSError(domain: "QuizService", code: -2, userInfo: [NSLocalizedDescriptionKey: msg])
        }
        guard let resultData = wrapper.data else {
            throw NSError(domain: "QuizService", code: -3, userInfo: [NSLocalizedDescriptionKey: "Dữ liệu trả về trống"])
        }
        return resultData
    }

    private func performRequestWithAuthRetry(_ request: URLRequest) async throws -> (Data, HTTPURLResponse) {
        let (data, response) = try await URLSession.shared.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NSError(domain: "QuizService", code: -1, userInfo: [NSLocalizedDescriptionKey: "Phản hồi máy chủ không hợp lệ"])
        }

        guard httpResponse.statusCode == 401, hasValidToken else {
            return (data, httpResponse)
        }

        await GoogleAuthService.shared.refreshTokenIfNeeded()
        guard let refreshedToken = token, !refreshedToken.isEmpty else {
            return (data, httpResponse)
        }

        var retriedRequest = request
        retriedRequest.setValue("Bearer \(refreshedToken)", forHTTPHeaderField: "Authorization")

        let (retryData, retryResponse) = try await URLSession.shared.data(for: retriedRequest)
        guard let retryHTTP = retryResponse as? HTTPURLResponse else {
            throw NSError(domain: "QuizService", code: -1, userInfo: [NSLocalizedDescriptionKey: "Phản hồi máy chủ không hợp lệ"])
        }
        return (retryData, retryHTTP)
    }

    private func applyClientHeaders(to request: inout URLRequest) {
        LichSoClientInfo.applyHeaders(to: &request)
    }
    
    public func fetchDailyQuestions(date: String) async throws -> [QuizQuestion] {
        let req = makeRequest(path: "/quiz/questions/daily?date=\(date)")
        return try await execute(req)
    }
    
    public func fetchQuestions(category: String?, limit: Int = 10) async throws -> [QuizQuestion] {
        var path = "/quiz/questions?limit=\(limit)"
        if let cat = category, let encoded = cat.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
            path += "&category=\(encoded)"
        }
        let req = makeRequest(path: path)
        return try await execute(req)
    }
    
    public func fetchLeaderboard(period: String = "weekly") async throws -> [LeaderboardEntry] {
        let req = makeRequest(path: "/quiz/leaderboard?period=\(period)&limit=50")
        let entries: [LeaderboardEntry] = try await execute(req)
        return entries.enumerated().map { (index, item) in
            var mutable = item
            mutable.rank = index + 1
            return mutable
        }
    }
    
    private func loadGuestSession(sessionType: String, category: String?) async throws -> (QuizSession, [QuizQuestion]) {
        let df = DateFormatter()
        df.dateFormat = "yyyy-MM-dd"
        let dateStr = df.string(from: Date())
        let questions: [QuizQuestion]
        if sessionType == "daily" {
            questions = try await fetchDailyQuestions(date: dateStr)
        } else {
            questions = try await fetchQuestions(category: category)
        }
        let session = QuizSession(
            id: "",
            sessionType: sessionType,
            category: category,
            questionIds: questions.map { $0.id },
            questions: questions
        )
        return (session, questions)
    }

    public func startSession(sessionType: String, category: String?, forceGuest: Bool = false) async throws -> (QuizSession, [QuizQuestion]) {
        // Ensure we don't accidentally fall back to guest mode while refresh is still pending.
        if token == nil || token?.isEmpty == true {
            await GoogleAuthService.shared.refreshTokenIfNeeded()
        }

        let isGuest = forceGuest || token == nil || token?.isEmpty == true
        if isGuest {
            return try await loadGuestSession(sessionType: sessionType, category: category)
        }
        
        var bodyObj: [String: Any] = ["session_type": sessionType]
        if let category = category {
            bodyObj["category"] = category
        }
        let bodyData = try? JSONSerialization.data(withJSONObject: bodyObj)
        let req = makeRequest(path: "/quiz/sessions", method: "POST", body: bodyData)
        
        do {
            let response: StartSessionResponse = try await execute(req)
            return (response.session, response.questions)
        } catch {
            // Any server error (like 401 Unauthorized) -> fall back to guest mode
            return try await loadGuestSession(sessionType: sessionType, category: category)
        }
    }
    
    public func submitAnswer(sessionId: String, questionId: Int64, chosen: String, timeMs: Int) async throws -> SubmitAnswerResult {
        let bodyObj: [String: Any] = [
            "question_id": questionId,
            "chosen": chosen,
            "time_ms": timeMs
        ]
        let bodyData = try? JSONSerialization.data(withJSONObject: bodyObj)
        let req = makeRequest(path: "/quiz/sessions/\(sessionId)/submit", method: "POST", body: bodyData)
        return try await execute(req)
    }
    
    public func finishSession(sessionId: String) async throws -> SessionResult {
        let req = makeRequest(path: "/quiz/sessions/\(sessionId)/finish", method: "POST")
        return try await execute(req)
    }
    
    public func getMyRank(period: String = "weekly") async throws -> MyRankResponse {
        let req = makeRequest(path: "/quiz/leaderboard/me?period=\(period)")
        return try await execute(req)
    }
    
    public func fetchPointWallet() async throws -> PointWallet {
        let req = makeRequest(path: "/points/wallet")
        return try await execute(req)
    }

    public func spendPoints(
        amount: Int,
        source: String,
        sourceId: String?,
        idempotencyKey: String?,
        metadata: [String: Any] = [:]
    ) async throws -> PointWallet {
        var bodyObj: [String: Any] = [
            "amount": amount,
            "source": source,
            "metadata": metadata
        ]
        if let sourceId {
            bodyObj["source_id"] = sourceId
        }
        if let idempotencyKey {
            bodyObj["idempotency_key"] = idempotencyKey
        }
        let bodyData = try? JSONSerialization.data(withJSONObject: bodyObj)
        let req = makeRequest(path: "/points/spend", method: "POST", body: bodyData)
        return try await execute(req)
    }

    public func fetchArticles(page: Int = 1, limit: Int = 20, category: String? = nil) async throws -> [Article] {
        var path = "/articles/?page=\(page)&limit=\(limit)"
        if let category, let encoded = category.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
            path += "&category=\(encoded)"
        }
        let req = makeRequest(path: path)
        return try await execute(req)
    }

    public func fetchArticle(id: String) async throws -> Article {
        let req = makeRequest(path: "/articles/\(id)")
        return try await execute(req)
    }

    // ── Khám phá (Knowledge Feed) — mirror Android content endpoints ──

    public func fetchEventsByDate(month: Int, day: Int) async throws -> [ContentEvent] {
        try await execute(makeRequest(path: "/events/date/\(month)/\(day)"))
    }

    public func fetchFamousPeople(month: Int, day: Int) async throws -> [FamousPerson] {
        try await execute(makeRequest(path: "/famous-people/birthday/\(month)/\(day)"))
    }

    public func fetchCategories() async throws -> [ArticleCategory] {
        try await execute(makeRequest(path: "/categories"))
    }

    public func fetchTodayQuote() async throws -> Quote {
        try await execute(makeRequest(path: "/quotes/today"))
    }

    /// Lấy banner theo "location key" (mới). Truyền danh sách phân tách bằng dấu phẩy,
    /// vd: "tools,tools_calendar,...". Backend lọc theo locations + platform (header).
    public func fetchBanners(location: String? = nil) async throws -> [Banner] {
        let path: String
        if let location, let encoded = location.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
            path = "/banners?location=\(encoded)"
        } else {
            path = "/banners"
        }
        let req = makeRequest(path: path)
        return try await execute(req)
    }

    public func fetchPopups() async throws -> [Popup] {
        let req = makeRequest(path: "/popups")
        return try await execute(req)
    }
}
