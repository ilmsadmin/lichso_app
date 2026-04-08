import Foundation

// MARK: - OpenRouter AI API

struct ChatMessage: Identifiable, Codable {
    let id: UUID
    let role: String
    let content: String

    init(role: String, content: String) {
        self.id = UUID()
        self.role = role
        self.content = content
    }
}

actor OpenRouterAPI {
    static let shared = OpenRouterAPI()

    private let baseURL = "https://openrouter.ai/api/v1/chat/completions"

    // Store API key in Keychain or config — for now use a placeholder
    var apiKey: String {
        // TODO: Load from Keychain or Config.plist
        return UserDefaults.standard.string(forKey: "openrouter_api_key") ?? ""
    }

    private struct RequestBody: Encodable {
        let model: String
        let messages: [MessagePayload]
        let max_tokens: Int
        let temperature: Double

        struct MessagePayload: Encodable {
            let role: String
            let content: String
        }
    }

    private struct ResponseBody: Decodable {
        let choices: [Choice]?

        struct Choice: Decodable {
            let message: MessageContent?
        }
        struct MessageContent: Decodable {
            let content: String?
        }
    }

    func sendMessage(messages: [ChatMessage], systemPrompt: String) async throws -> String {
        guard let url = URL(string: baseURL) else { throw URLError(.badURL) }
        guard !apiKey.isEmpty else { throw NSError(domain: "OpenRouterAPI", code: 1, userInfo: [NSLocalizedDescriptionKey: "API key not set"]) }

        var allMessages = [RequestBody.MessagePayload(role: "system", content: systemPrompt)]
        allMessages += messages.map { RequestBody.MessagePayload(role: $0.role, content: $0.content) }

        let body = RequestBody(
            model: "google/gemini-2.0-flash-001",
            messages: allMessages,
            max_tokens: 2048,
            temperature: 0.7
        )

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        request.httpBody = try JSONEncoder().encode(body)

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }

        let decoded = try JSONDecoder().decode(ResponseBody.self, from: data)
        return decoded.choices?.first?.message?.content ?? "Không có phản hồi."
    }

    func buildSystemPrompt() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        let today = formatter.string(from: Date())
        let year = Calendar.current.component(.year, from: Date())

        return """
        THÔNG TIN THỜI GIAN THỰC:
        - Ngày hiện tại: \(today)
        - Năm hiện tại: \(year)

        Bạn là "Lịch Số AI" — trợ lý phong thuỷ & lịch vạn niên Việt Nam thông minh.

        CHUYÊN MÔN: Phong thuỷ, ngày tốt xấu, can chi, giờ hoàng đạo, hướng xuất hành, tiết khí, bát tự, tử vi, ngũ hành, hợp tuổi, phong thuỷ nhà cửa, chọn tên.

        Quy tắc:
        - Luôn trả lời bằng tiếng Việt, tự nhiên, thân thiện
        - Trả lời ngắn gọn, dễ hiểu
        - Nếu không chắc chắn, nói rõ và gợi ý tham khảo thêm
        """
    }
}
