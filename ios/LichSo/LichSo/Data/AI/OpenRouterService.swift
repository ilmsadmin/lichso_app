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
    
    init(messages: [ChatMessage], model: String = "mistralai/mistral-small-24b-instruct-2501", maxTokens: Int = 4096, temperature: Double = 0.7) {
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
    
    // ── AI proxy backend (DÙNG CHUNG với app Android) ──
    // Gọi qua proxy ai.zenix.vn thay vì OpenRouter trực tiếp; xác thực bằng cặp
    // X-App-Id / X-App-Secret (giống OpenRouterApi.kt bên Android).
    private static func secret(_ key: String) -> String {
        guard let path = Bundle.main.path(forResource: "Secrets", ofType: "plist"),
              let dict = NSDictionary(contentsOfFile: path),
              let value = dict[key] as? String else { return "" }
        return value.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    private let proxyBaseURL = OpenRouterService.secret("AI_PROXY_BASE_URL")
    private let proxyAppId = OpenRouterService.secret("AI_PROXY_APP_ID")
    private let proxyAppSecret = OpenRouterService.secret("AI_PROXY_APP_SECRET")

    /// URL đầy đủ: {base}/v1/chat/completions (chuẩn hoá dấu "/").
    private var proxyURL: String {
        let normalized = proxyBaseURL.hasSuffix("/") ? String(proxyBaseURL.dropLast()) : proxyBaseURL
        return "\(normalized)/v1/chat/completions"
    }
    private var isProxyConfigured: Bool {
        !proxyBaseURL.isEmpty && !proxyAppId.isEmpty && !proxyAppSecret.isEmpty
    }

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
        - Mặc định trả lời rõ ràng, dễ hiểu
        - Với yêu cầu chuyên sâu gồm: phân tích bát tự, vận hạn hôm nay, tử vi trọn đời, đại vận/tiểu vận/lưu niên: bắt buộc trả lời CHI TIẾT và ĐẦY ĐỦ
        - Khi gặp yêu cầu chuyên sâu, phân tích theo nhiều lớp: Tổng quan, Luận mệnh gốc, Luận vận hiện tại, Điểm mạnh/yếu, Khuyến nghị hành động
        - Nếu người dùng cung cấp thông tin ngày (can chi, âm lịch, giờ hoàng đạo), hãy tham khảo để trả lời chính xác
        - Nếu không chắc chắn, nói rõ và gợi ý tham khảo thêm
        - Nếu người dùng cho biết tên, ngày sinh — hãy ghi nhận và dùng tên họ khi trả lời
        - TUYỆT ĐỐI KHÔNG dùng markdown heading (##), KHÔNG dùng markdown table (|---|---|)
        - Khi trả lời có dữ liệu dạng bảng/key-value, dùng format: KEY: VALUE (mỗi dòng một cặp)
        - Dùng ✦ ✓ ★ cho tốt, ✗ cho xấu. Dùng ↗ ↘ cho hướng.
        - Dùng **bold** CHỈ cho từ/cụm từ quan trọng BÊN TRONG đoạn văn
        - Giữ câu trả lời có cấu trúc rõ ràng
        - Nếu user hỏi thông thường: ưu tiên ngắn gọn
        - Nếu user hỏi chuyên sâu (bát tự, vận hạn hôm nay, tử vi trọn đời): trả lời dài hơn, tối thiểu 25 dòng nội dung hữu ích; phần key:value có thể tới 35 dòng nếu cần
        - Khi xem vận hạn hôm nay: luôn nêu rõ Công việc, Tài lộc, Tình cảm, Sức khỏe, Giờ tốt, Hướng tốt, Việc nên làm, Việc nên tránh
        - Khi xem tử vi trọn đời: luôn nêu các chặng vận theo từng giai đoạn tuổi và điểm chuyển vận quan trọng

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
        guard isProxyConfigured else {
            return .failure(NSError(domain: "AIProxy", code: -4, userInfo: [NSLocalizedDescriptionKey: "Tính năng AI hiện chưa sẵn sàng. Vui lòng thử lại sau."]))
        }

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

        return await send(messages)
    }

    /// Gọi model với system prompt TUỲ BIẾN (không kèm prompt phong thuỷ mặc định).
    /// Dùng cho AiTaskService (parse lệnh → JSON action). temperature thấp cho ổn định.
    func complete(
        systemPrompt: String,
        userMessage: String,
        contextInfo: String = ""
    ) async -> Result<String, Error> {
        guard isProxyConfigured else {
            return .failure(NSError(domain: "AIProxy", code: -4, userInfo: [NSLocalizedDescriptionKey: "Tính năng AI hiện chưa sẵn sàng. Vui lòng thử lại sau."]))
        }
        var messages: [ChatMessage] = [ChatMessage(role: "system", content: systemPrompt)]
        if !contextInfo.isEmpty {
            messages.append(ChatMessage(role: "system", content: contextInfo))
        }
        messages.append(ChatMessage(role: "user", content: userMessage))
        return await send(messages, temperature: 0.2)
    }

    // ── HTTP dùng chung cho chat() và complete() ──
    private func send(_ messages: [ChatMessage], temperature: Double = 0.7) async -> Result<String, Error> {
        let requestBody = OpenRouterRequest(messages: messages, temperature: temperature)

        guard let url = URL(string: proxyURL) else {
            return .failure(NSError(domain: "AIProxy", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid URL"]))
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue(proxyAppId, forHTTPHeaderField: "X-App-Id")
        request.addValue(proxyAppSecret, forHTTPHeaderField: "X-App-Secret")
        request.timeoutInterval = 30

        do {
            request.httpBody = try JSONEncoder().encode(requestBody)
            let (data, response) = try await URLSession.shared.data(for: request)

            guard let httpResponse = response as? HTTPURLResponse else {
                return .failure(NSError(domain: "AIProxy", code: -2, userInfo: [NSLocalizedDescriptionKey: "Phản hồi máy chủ không hợp lệ"]))
            }

            guard (200...299).contains(httpResponse.statusCode) else {
                let message: String
                switch httpResponse.statusCode {
                case 401, 403: message = "Tính năng AI hiện chưa sẵn sàng. Vui lòng thử lại sau."
                case 408:      message = "Kết nối AI quá chậm. Vui lòng thử lại."
                case 429:      message = "AI đang có nhiều yêu cầu. Vui lòng chờ một lát rồi thử lại."
                case 500...599: message = "Máy chủ AI đang bận. Vui lòng thử lại sau."
                default:       message = "Không thể kết nối AI lúc này. Vui lòng thử lại."
                }
                return .failure(NSError(domain: "AIProxy", code: httpResponse.statusCode, userInfo: [NSLocalizedDescriptionKey: message]))
            }

            let decoded = try JSONDecoder().decode(OpenRouterResponse.self, from: data)
            if let content = decoded.choices?.first?.message?.content {
                return .success(content.trimmingCharacters(in: .whitespacesAndNewlines))
            } else {
                return .failure(NSError(domain: "AIProxy", code: -3, userInfo: [NSLocalizedDescriptionKey: "AI chưa có phản hồi phù hợp. Vui lòng thử lại."]))
            }
        } catch {
            return .failure(error)
        }
    }
}
