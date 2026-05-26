import Foundation

@MainActor
public class QuizService: ObservableObject {
    public static let shared = QuizService()
    
    private let baseURL = "https://api.lichso.vn/api"
    
    private init() {}
    
    private var token: String? {
        UserDefaults.standard.string(forKey: "backend_access_token")
    }

    private func makeRequest(path: String, method: String = "GET", body: Data? = nil) -> URLRequest {
        var request = URLRequest(url: URL(string: "\(baseURL)\(path)")!)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if let token = self.token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        request.httpBody = body
        return request
    }
    
    private func execute<T: Codable>(_ request: URLRequest) async throws -> T {
        let (data, response) = try await URLSession.shared.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw NSError(domain: "QuizService", code: -1, userInfo: [NSLocalizedDescriptionKey: "Yêu cầu mạng thất bại"])
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
    
    public func startSession(sessionType: String, category: String?) async throws -> (QuizSession, [QuizQuestion]) {
        let isGuest = token == nil || token?.isEmpty == true
        if isGuest {
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
        
        let bodyObj: [String: String?] = ["session_type": sessionType, "category": category]
        let bodyData = try? JSONSerialization.data(withJSONObject: bodyObj)
        let req = makeRequest(path: "/quiz/sessions", method: "POST", body: bodyData)
        do {
            let response: StartSessionResponse = try await execute(req)
            return (response.session, response.questions)
        } catch {
            // Fallback to guest mode
            let df = DateFormatter()
            df.dateFormat = "yyyy-MM-dd"
            let dateStr = df.string(from: Date())
            let questions = try await fetchQuestions(category: category)
            let session = QuizSession(
                id: "",
                sessionType: sessionType,
                category: category,
                questionIds: questions.map { $0.id },
                questions: questions
            )
            return (session, questions)
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
    
    public func fetchArticles(page: Int = 1, limit: Int = 20) async throws -> [Article] {
        let req = makeRequest(path: "/articles/?page=\(page)&limit=\(limit)")
        return try await execute(req)
    }
    
    public func fetchArticle(id: String) async throws -> Article {
        let req = makeRequest(path: "/articles/\(id)")
        return try await execute(req)
    }
}
