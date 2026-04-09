import Foundation

// ═══════════════════════════════════════════
// OpenRouter API Service
// Connects to OpenRouter for AI chat capabilities
// ═══════════════════════════════════════════

struct ChatMessage: Codable {
    let role: String
    let content: String
}

struct OpenRouterRequest: Codable {
    let model: String
    let messages: [ChatMessage]
    let max_tokens: Int
    let temperature: Double
    
    init(messages: [ChatMessage], model: String = "google/gemini-2.0-flash-001", maxTokens: Int = 2048, temperature: Double = 0.7) {
        self.model = model
        self.messages = messages
        self.max_tokens = maxTokens
        self.temperature = temperature
    }
}

struct OpenRouterResponse: Codable {
    let choices: [Choice]?
    
    struct Choice: Codable {
        let message: MessageContent?
    }
    
    struct MessageContent: Codable {
        let content: String?
    }
}

@MainActor
class OpenRouterService: ObservableObject {
    static let shared = OpenRouterService()
    
    private let apiKey = "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"
    private let baseURL = "https://openrouter.ai/api/v1/chat/completions"
    
    private var systemPrompt: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        let today = formatter.string(from: Date())
        let currentYear = Calendar.current.component(.year, from: Date())
        
        return """
        THÔNG TIN THỜI GIAN THỰC (BẮT BUỘC SỬ DỤNG):
        - Ngày hiện tại: \(today)
        - Năm hiện tại: \(currentYear)
        - LUÔN LUÔN sử dụng năm \(currentYear) làm mốc thời gian khi tính toán tuổi, số năm, hoặc bất kỳ phép tính nào liên quan đến thời gian.

        Bạn là "Lịch Số AI" — trợ lý phong thuỷ & lịch vạn niên Việt Nam thông minh.

        CHUYÊN MÔN SÂU:
        - Phong thuỷ, ngày tốt xấu, can chi, giờ hoàng đạo, hướng xuất hành, tiết khí
        - BÁT TỰ (Tứ Trụ): Phân tích 4 trụ Năm-Tháng-Ngày-Giờ sinh, thiên can địa chi, ngũ hành sinh khắc, dụng thần, kỵ thần, vượng suy.
        - TỬ VI: Phân tích vận mệnh theo tử vi Việt Nam (các cung: Mệnh, Thân, Phụ Mẫu, Phúc Đức, Điền Trạch, Quan Lộc, Nô Bộc, Thiên Di, Tật Ách, Tài Bạch, Tử Tức, Phu Thê).
        - VẬN MỆNH & VẬN HẠN: Phân tích đại vận (10 năm), tiểu vận (năm), lưu niên, lưu nguyệt.
        - NGŨ HÀNH: Kim Mộc Thủy Hỏa Thổ — phân tích tương sinh tương khắc, nạp âm, hợp/kỵ màu sắc, hướng, số.
        - HỢP TUỔI: Tam hợp, lục hợp, tứ hành xung, lục xung, lục hại.
        - CON GIÁP & PHONG THỦY NĂM: Phân tích vận thế 12 con giáp trong năm \(currentYear).
        - PHONG THỦY NHÀ CỬA: Hướng nhà, vị trí bàn thờ, phòng ngủ, bếp theo tuổi và mệnh.
        - CHỌN TÊN: Gợi ý tên đẹp theo ngũ hành, mệnh, ý nghĩa.

        Quy tắc:
        - Luôn trả lời bằng tiếng Việt, tự nhiên, thân thiện
        - Trả lời ngắn gọn, dễ hiểu
        - Nếu người dùng cung cấp thông tin ngày (can chi, âm lịch, giờ hoàng đạo), hãy tham khảo để trả lời chính xác
        - Nếu không chắc chắn, nói rõ và gợi ý tham khảo thêm
        - Nếu người dùng cho biết tên, ngày sinh — hãy ghi nhận và dùng tên họ khi trả lời
        - TUYỆT ĐỐI KHÔNG dùng markdown heading (##), KHÔNG dùng markdown table (|---|---|)
        - Khi trả lời có dữ liệu dạng bảng/key-value, dùng format: KEY: VALUE (mỗi dòng một cặp)
        - Dùng ✦ ✓ ★ cho tốt, ✗ cho xấu. Dùng ↗ ↘ cho hướng.
        - Dùng **bold** CHỈ cho từ/cụm từ quan trọng BÊN TRONG đoạn văn
        - Giữ câu trả lời ngắn gọn, có cấu trúc rõ ràng
        - KHÔNG trả lời dài dòng. Tối đa 15 dòng key:value trong một bảng.

        GỢI Ý TIẾP TỤC (BẮT BUỘC):
        Sau mỗi câu trả lời, LUÔN LUÔN thêm phần gợi ý ở cuối với format CHÍNH XÁC:

        ~~~gợi ý
        📌 Gợi ý 1 ngắn gọn
        📌 Gợi ý 2 ngắn gọn
        📌 Gợi ý 3 ngắn gọn
        ~~~
        """
    }
    
    func chat(
        userMessage: String,
        contextInfo: String = "",
        profileContext: String = "",
        history: [ChatMessage] = []
    ) async -> Result<String, Error> {
        var messages: [ChatMessage] = []
        messages.append(ChatMessage(role: "system", content: systemPrompt))
        
        if !profileContext.isEmpty {
            messages.append(ChatMessage(role: "system", content: profileContext))
        }
        
        if !contextInfo.isEmpty {
            messages.append(ChatMessage(role: "system", content: "Thông tin lịch hôm nay:\n\(contextInfo)"))
        }
        
        messages.append(contentsOf: history.suffix(10))
        messages.append(ChatMessage(role: "user", content: userMessage))
        
        let requestBody = OpenRouterRequest(messages: messages)
        
        guard let url = URL(string: baseURL) else {
            return .failure(NSError(domain: "OpenRouter", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid URL"]))
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("https://lichso.app", forHTTPHeaderField: "HTTP-Referer")
        request.addValue("Lich So - Lich Van Nien", forHTTPHeaderField: "X-Title")
        request.timeoutInterval = 30
        
        do {
            request.httpBody = try JSONEncoder().encode(requestBody)
            let (data, response) = try await URLSession.shared.data(for: request)
            
            guard let httpResponse = response as? HTTPURLResponse else {
                return .failure(NSError(domain: "OpenRouter", code: -2, userInfo: [NSLocalizedDescriptionKey: "Invalid response"]))
            }
            
            guard httpResponse.statusCode == 200 else {
                let body = String(data: data, encoding: .utf8) ?? ""
                return .failure(NSError(domain: "OpenRouter", code: httpResponse.statusCode, userInfo: [NSLocalizedDescriptionKey: "API error \(httpResponse.statusCode): \(body.prefix(200))"]))
            }
            
            let decoded = try JSONDecoder().decode(OpenRouterResponse.self, from: data)
            if let content = decoded.choices?.first?.message?.content {
                return .success(content.trimmingCharacters(in: .whitespacesAndNewlines))
            } else {
                return .failure(NSError(domain: "OpenRouter", code: -3, userInfo: [NSLocalizedDescriptionKey: "Empty response from AI"]))
            }
        } catch {
            return .failure(error)
        }
    }
}
