import SwiftUI
import SwiftData
import Combine

// ═══════════════════════════════════════════
// ChatViewModel — AI Chat with real OpenRouter API
// Uses SwiftData for message persistence
// ═══════════════════════════════════════════

@MainActor
class ChatViewModel: ObservableObject {
    @Published var messages: [ChatMessageEntity] = []
    @Published var isTyping = false
    @Published var followUpSuggestions: [String] = []
    
    private let openRouter = OpenRouterService.shared
    private let dayInfoProvider = DayInfoProvider.shared
    private var modelContext: ModelContext?
    
    // Profile data
    @AppStorage("displayName") private var displayName = "Người dùng"
    @AppStorage("gender") private var gender = ""
    @AppStorage("birthDay") private var birthDay = 0
    @AppStorage("birthMonth") private var birthMonth = 0
    @AppStorage("birthYear") private var birthYear = 0
    @AppStorage("birthHour") private var birthHour = -1
    @AppStorage("birthMinute") private var birthMinute = -1
    
    func setModelContext(_ context: ModelContext) {
        self.modelContext = context
        loadMessages()
    }
    
    func loadMessages() {
        guard let context = modelContext else { return }
        let descriptor = FetchDescriptor<ChatMessageEntity>(
            sortBy: [SortDescriptor(\.timestamp, order: .forward)]
        )
        do {
            messages = try context.fetch(descriptor)
            if messages.isEmpty {
                addGreetingMessage()
            }
        } catch {
            #if DEBUG
            print("Failed to load messages: \(error)")
            #endif
        }
    }
    
    private func addGreetingMessage() {
        let hasProfile = birthYear > 0 && birthMonth > 0 && birthDay > 0
        let yearCanChi = hasProfile ? CanChiCalculator.getYearCanChi(lunarYear: LunarCalendarUtil.convertSolar2Lunar(dd: birthDay, mm: birthMonth, yy: birthYear).lunarYear) : ""
        
        let greeting: String
        if hasProfile && displayName != "Người dùng" {
            greeting = "Chào \(displayName)! Tôi là Lịch Số AI — trợ lý phong thủy & lịch vạn niên của bạn.\n\nTôi đã có thông tin cá nhân của bạn (tuổi \(yearCanChi)). Mọi phân tích phong thủy sẽ được cá nhân hóa theo tuổi & mệnh của bạn.\n\nBạn muốn hỏi gì hôm nay?"
        } else {
            greeting = "Xin chào! Tôi là Lịch Số AI — trợ lý phong thuỷ & lịch vạn niên thông minh.\n\nTôi được hỗ trợ bởi AI tiên tiến, có thể giúp bạn:\n› Xem ngày tốt/xấu cho công việc\n› Tra cứu can chi, tiết khí, giờ hoàng đạo\n› Gợi ý ngày cưới, khai trương, động thổ\n› Phân tích bát tự, tử vi, vận mệnh\n› Giải đáp phong thủy chi tiết\n\n✦ Hãy cập nhật hồ sơ cá nhân (tên, ngày sinh, giới tính) để tôi phân tích phong thủy chính xác theo tuổi & mệnh của bạn!"
        }
        
        saveMessage(content: greeting, isUser: false)
    }
    
    // MARK: - Send Message
    
    func sendMessage(_ text: String) {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        
        saveMessage(content: trimmed, isUser: true)
        isTyping = true
        followUpSuggestions = []
        
        Task {
            let contextInfo = buildTodayContext()
            let profileContext = buildProfileContext()
            let history = messages.suffix(10).map { msg in
                ChatMessage(role: msg.isUser ? "user" : "assistant", content: msg.content)
            }
            
            let result = await openRouter.chat(
                userMessage: trimmed,
                contextInfo: contextInfo,
                profileContext: profileContext,
                history: history
            )
            
            let response: String
            switch result {
            case .success(let content):
                response = content
            case .failure(let error):
                let errMsg = error.localizedDescription
                let hint: String
                if errMsg.contains("401") || errMsg.contains("User not found") {
                    hint = "🔮 Thầy tử vi đang bận đi uống trà, chưa kịp trả lời!\nĐể tôi tra cứu sách vở giúp bạn trước nhé:\n\n"
                } else if errMsg.contains("402") || errMsg.contains("insufficient") {
                    hint = "💰 Thầy tử vi đang chờ nạp thêm linh khí (credit)!\nTạm thời tôi xem sách cổ giúp trước nhé:\n\n"
                } else if errMsg.contains("403") || errMsg.contains("Forbidden") {
                    hint = "🚫 Cánh cổng tâm linh tạm thời bị khóa!\nĐể tôi tra cứu sách vở giúp bạn trước:\n\n"
                } else if errMsg.contains("429") {
                    hint = "🍵 Thầy đang nghỉ giải lao vì quá nhiều người hỏi!\nBạn thử quay lại sau vài phút nhé. Tạm thời tôi xem giúp:\n\n"
                } else if errMsg.contains("500") || errMsg.contains("502") || errMsg.contains("503") {
                    hint = "🏚️ Đền thờ AI đang bảo trì...\nThầy sẽ quay lại sớm thôi! Tạm xem nhanh:\n\n"
                } else if errMsg.contains("timeout") || errMsg.contains("Timeout") || errMsg.contains("timed out") {
                    hint = "🌫️ Đường truyền tâm linh hơi chập chờn...\nKiểm tra kết nối mạng rồi thử lại nhé! Tạm xem nhanh:\n\n"
                } else if errMsg.contains("network") || errMsg.contains("not connected") || errMsg.contains("offline") {
                    hint = "📡 Mất kết nối với thế giới tâm linh!\nKiểm tra WiFi/4G rồi thử lại nhé. Tạm xem sách cổ:\n\n"
                } else if errMsg.contains("API key") || errMsg.contains("cấu hình") {
                    hint = "🔑 Chìa khóa tâm linh chưa được kích hoạt!\nĐể tôi xem sách cổ trả lời trước nhé:\n\n"
                } else {
                    hint = "🔮 Thầy tử vi đang đi vắng, để tôi xem sách cổ trả lời trước nhé!\n\n"
                }
                response = hint + generateLocalResponse(trimmed)
            }
            
            // Extract follow-up suggestions
            let (cleanContent, suggestions) = extractFollowUpSuggestions(response)
            followUpSuggestions = suggestions
            
            saveMessage(content: cleanContent, isUser: false)
            isTyping = false
        }
    }
    
    // MARK: - Send with Date Context (AI Chat Day)
    
    func sendWithDateContext(dd: Int, mm: Int, yy: Int, question: String) {
        let info = dayInfoProvider.getDayInfo(dd: dd, mm: mm, yy: yy)
        let lunar = LunarCalendarUtil.convertSolar2Lunar(dd: dd, mm: mm, yy: yy)
        
        let dateContext = """
        Ngày đang hỏi: \(String(format: "%02d/%02d/%04d", dd, mm, yy)) (\(info.dayOfWeek))
        Âm lịch: \(lunar.lunarDay)/\(lunar.lunarMonth) năm \(info.yearCanChi)
        Can chi ngày: \(info.dayCanChi)
        Trực: \(info.trucNgay.name) (\(info.trucNgay.rating))
        Sao chiếu: \(info.saoChieu.name) (\(info.saoChieu.rating))
        Giờ hoàng đạo: \(info.gioHoangDao.prefix(3).map { "\($0.name) (\($0.time))" }.joined(separator: ", "))
        Nên: \(info.activities.nenLam.prefix(5).joined(separator: ", "))
        Không nên: \(info.activities.khongNen.prefix(5).joined(separator: ", "))
        Hướng Thần Tài: \(info.huong.thanTai)
        Hướng Hỷ Thần: \(info.huong.hyThan)
        """
        
        let fullMessage = question.isEmpty
            ? "Hãy phân tích ngày \(String(format: "%02d/%02d/%04d", dd, mm, yy)) cho tôi."
            : question
        
        // Add date attachment as system context
        saveMessage(content: fullMessage, isUser: true)
        isTyping = true
        followUpSuggestions = []
        
        Task {
            let profileContext = buildProfileContext()
            let history = messages.suffix(10).map { msg in
                ChatMessage(role: msg.isUser ? "user" : "assistant", content: msg.content)
            }
            
            let result = await openRouter.chat(
                userMessage: fullMessage,
                contextInfo: dateContext,
                profileContext: profileContext,
                history: history
            )
            
            let response: String
            switch result {
            case .success(let content): response = content
            case .failure(let error):
                let errMsg = error.localizedDescription
                let hint: String
                if errMsg.contains("401") || errMsg.contains("User not found") {
                    hint = "🔮 Thầy tử vi đang bận đi uống trà, chưa kịp trả lời!\nĐể tôi tra cứu sách vở giúp bạn trước nhé:\n\n"
                } else if errMsg.contains("402") || errMsg.contains("insufficient") {
                    hint = "💰 Thầy tử vi đang chờ nạp thêm linh khí (credit)!\nTạm thời tôi xem sách cổ giúp trước nhé:\n\n"
                } else if errMsg.contains("403") || errMsg.contains("Forbidden") {
                    hint = "🚫 Cánh cổng tâm linh tạm thời bị khóa!\nĐể tôi tra cứu sách vở giúp bạn trước:\n\n"
                } else if errMsg.contains("429") {
                    hint = "🍵 Thầy đang nghỉ giải lao vì quá nhiều người hỏi!\nBạn thử quay lại sau vài phút nhé. Tạm thời tôi xem giúp:\n\n"
                } else if errMsg.contains("500") || errMsg.contains("502") || errMsg.contains("503") {
                    hint = "🏚️ Đền thờ AI đang bảo trì...\nThầy sẽ quay lại sớm thôi! Tạm xem nhanh:\n\n"
                } else if errMsg.contains("timeout") || errMsg.contains("Timeout") || errMsg.contains("timed out") {
                    hint = "🌫️ Đường truyền tâm linh hơi chập chờn...\nKiểm tra kết nối mạng rồi thử lại nhé! Tạm xem nhanh:\n\n"
                } else if errMsg.contains("network") || errMsg.contains("not connected") || errMsg.contains("offline") {
                    hint = "📡 Mất kết nối với thế giới tâm linh!\nKiểm tra WiFi/4G rồi thử lại nhé. Tạm xem sách cổ:\n\n"
                } else if errMsg.contains("API key") || errMsg.contains("cấu hình") {
                    hint = "🔑 Chìa khóa tâm linh chưa được kích hoạt!\nĐể tôi xem sách cổ trả lời trước nhé:\n\n"
                } else {
                    hint = "🔮 Thầy tử vi đang đi vắng, để tôi xem sách cổ trả lời trước nhé!\n\n"
                }
                response = hint + generateLocalDateResponse(dd: dd, mm: mm, yy: yy)
            }
            
            let (cleanContent, suggestions) = extractFollowUpSuggestions(response)
            followUpSuggestions = suggestions
            
            saveMessage(content: cleanContent, isUser: false)
            isTyping = false
        }
    }
    
    // MARK: - Clear Chat
    
    func clearChat() {
        guard let context = modelContext else { return }
        do {
            try context.delete(model: ChatMessageEntity.self)
            try context.save()
            messages = []
            followUpSuggestions = []
            addGreetingMessage()
        } catch {
            #if DEBUG
            print("Failed to clear chat: \(error)")
            #endif
        }
    }
    
    // MARK: - Persistence
    
    private func saveMessage(content: String, isUser: Bool) {
        guard let context = modelContext else { return }
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let entity = ChatMessageEntity(id: now, content: content, isUser: isUser)
        context.insert(entity)
        try? context.save()
        messages.append(entity)
    }
    
    // MARK: - Context Builders
    
    private func buildTodayContext() -> String {
        let now = Date()
        let cal = Calendar.current
        let dd = cal.component(.day, from: now)
        let mm = cal.component(.month, from: now)
        let yy = cal.component(.year, from: now)
        let info = dayInfoProvider.getDayInfo(dd: dd, mm: mm, yy: yy)
        
        var sb = ""
        sb += "Ngày: \(info.solar.dd)/\(info.solar.mm)/\(info.solar.yy) (\(info.dayOfWeek))\n"
        sb += "Âm lịch: \(info.lunar.day)/\(info.lunar.month) năm \(info.yearCanChi)\n"
        sb += "Can chi ngày: \(info.dayCanChi)\n"
        sb += "Can chi tháng: \(info.monthCanChi)\n"
        sb += "Can chi năm: \(info.yearCanChi)\n"
        sb += "Nên: \(info.activities.nenLam.joined(separator: ", "))\n"
        sb += "Không nên: \(info.activities.khongNen.joined(separator: ", "))\n"
        sb += "Giờ hoàng đạo: \(info.gioHoangDao.map { "\($0.name) (\($0.time))" }.joined(separator: ", "))\n"
        sb += "Hướng Thần Tài: \(info.huong.thanTai)\n"
        sb += "Hướng Hỷ Thần: \(info.huong.hyThan)\n"
        if let tietKhi = info.tietKhi.currentName { sb += "Tiết khí: \(tietKhi)\n" }
        sb += "Pha trăng: \(info.moonPhase.name) (ngày \(Int(info.moonPhase.age)))\n"
        if let sH = info.solarHoliday { sb += "Ngày lễ dương: \(sH)\n" }
        if let lH = info.lunarHoliday { sb += "Ngày lễ âm: \(lH)\n" }
        return sb
    }
    
    private func buildProfileContext() -> String {
        var sb = "=== THÔNG TIN CÁ NHÂN NGƯỜI DÙNG (BẮT BUỘC SỬ DỤNG, TUYỆT ĐỐI KHÔNG HỎI LẠI) ===\n"
        if displayName != "Người dùng" && !displayName.isEmpty {
            sb += "Tên: \(displayName)\n"
        }
        if !gender.isEmpty {
            sb += "Giới tính: \(gender)\n"
        }
        if birthYear > 0 && birthMonth > 0 && birthDay > 0 {
            sb += "Ngày sinh dương lịch: \(birthDay)/\(birthMonth)/\(birthYear)\n"
            let lunar = LunarCalendarUtil.convertSolar2Lunar(dd: birthDay, mm: birthMonth, yy: birthYear)
            sb += "Ngày sinh âm lịch: \(lunar.lunarDay)/\(lunar.lunarMonth)/\(lunar.lunarYear)\n"
            let yearCanChi = CanChiCalculator.getYearCanChi(lunarYear: lunar.lunarYear)
            sb += "Tuổi Can Chi: \(yearCanChi)\n"
            let chiIndex = (lunar.lunarYear + 8) % 12
            let conGiapNames = ["Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"]
            sb += "Con giáp: \(conGiapNames[chiIndex])\n"
        }
        if birthHour >= 0 && birthMinute >= 0 {
            sb += "Giờ sinh: \(birthHour)h\(String(format: "%02d", birthMinute))\n"
        }
        sb += "→ Tất cả thông tin trên đã được người dùng cung cấp. SỬ DỤNG TRỰC TIẾP, KHÔNG HỎI LẠI.\n"
        return sb
    }
    
    // MARK: - Follow-up Suggestions
    
    private func extractFollowUpSuggestions(_ content: String) -> (String, [String]) {
        let pattern = "~~~gợi ý\\s*\\n([\\s\\S]*?)(?:~~~|$)"
        guard let regex = try? NSRegularExpression(pattern: pattern),
              let match = regex.firstMatch(in: content, range: NSRange(content.startIndex..., in: content)) else {
            return (content, [])
        }
        
        guard let suggestionsRange = Range(match.range(at: 1), in: content) else {
            return (content, [])
        }
        let suggestionsBlock = String(content[suggestionsRange])
        let itemPattern = "📌\\s*(.+)"
        guard let itemRegex = try? NSRegularExpression(pattern: itemPattern) else {
            return (content, [])
        }
        let suggestions = itemRegex.matches(in: suggestionsBlock, range: NSRange(suggestionsBlock.startIndex..., in: suggestionsBlock)).compactMap { m -> String? in
            guard let range = Range(m.range(at: 1), in: suggestionsBlock) else { return nil }
            return String(suggestionsBlock[range]).trimmingCharacters(in: .whitespaces)
        }
        
        if suggestions.isEmpty { return (content, []) }
        guard let fullMatchRange = Range(match.range, in: content) else {
            return (content, suggestions)
        }
        let cleaned = content.replacingCharacters(in: fullMatchRange, with: "").trimmingCharacters(in: .whitespacesAndNewlines)
        return (cleaned, suggestions)
    }
    
    // MARK: - Local Fallback Responses
    
    private func generateLocalResponse(_ query: String) -> String {
        let now = Date()
        let cal = Calendar.current
        let dd = cal.component(.day, from: now)
        let mm = cal.component(.month, from: now)
        let yy = cal.component(.year, from: now)
        let info = dayInfoProvider.getDayInfo(dd: dd, mm: mm, yy: yy)
        let q = query.lowercased()
        
        if q.contains("hôm nay") || q.contains("ngày tốt") {
            return """
            🗓️ Hôm nay — \(info.solar.dd)/\(info.solar.mm)/\(info.solar.yy)
            ☽ Âm lịch: \(info.lunar.day)/\(info.lunar.month) \(info.yearCanChi)
            ◈ Ngày: \(info.dayCanChi) · \(info.dayOfWeek)
            
            ✓ Nên: \(info.activities.nenLam.prefix(5).joined(separator: ", "))
            ✗ Không nên: \(info.activities.khongNen.prefix(5).joined(separator: ", "))
            
            ◷ Giờ hoàng đạo: \(info.gioHoangDao.prefix(3).map { "\($0.name) (\($0.time))" }.joined(separator: ", "))
            ⊕ Hướng Thần Tài: \(info.huong.thanTai)
            """
        }
        
        if q.contains("giờ hoàng đạo") || q.contains("giờ tốt") {
            return """
            ◷ GIỜ HOÀNG ĐẠO — \(info.dayOfWeek) \(info.solar.dd)/\(info.solar.mm)
            ◈ Ngày: \(info.dayCanChi)
            
            \(info.gioHoangDao.map { "   ✦ \($0.name) (\($0.time))" }.joined(separator: "\n"))
            """
        }
        
        if q.contains("hướng") || q.contains("xuất hành") {
            return """
            ⊕ HƯỚNG TỐT — \(info.dayOfWeek) \(info.solar.dd)/\(info.solar.mm)
            
            ❖ Thần Tài: \(info.huong.thanTai)
            ⟡ Hỷ Thần: \(info.huong.hyThan)
            ⚠️ Hắc Thần (tránh): \(info.huong.hungThan)
            """
        }
        
        return """
        🗓️ \(info.solar.dd)/\(info.solar.mm)/\(info.solar.yy) · \(info.dayCanChi)
        ☽ Âm: \(info.lunar.day)/\(info.lunar.month) \(info.yearCanChi)
        
        Tôi chưa hiểu rõ câu hỏi. Bạn có thể thử:
        › Hỏi về ngày tốt/xấu, giờ hoàng đạo
        › Xem ngày cụ thể: "ngày 25/12"
        › Hỏi "giúp" để xem tất cả chức năng
        """
    }
    
    private func generateLocalDateResponse(dd: Int, mm: Int, yy: Int) -> String {
        let info = dayInfoProvider.getDayInfo(dd: dd, mm: mm, yy: yy)
        return """
        🗓️ \(dd)/\(mm)/\(yy) · \(info.dayOfWeek)
        ☽ Âm: \(info.lunar.day)/\(info.lunar.month) \(info.yearCanChi)
        ◈ \(info.dayCanChi)
        
        Trực: \(info.trucNgay.name) (\(info.trucNgay.rating))
        Sao: \(info.saoChieu.name) (\(info.saoChieu.rating))
        
        ✓ Nên: \(info.activities.nenLam.prefix(4).joined(separator: ", "))
        ✗ Không nên: \(info.activities.khongNen.prefix(4).joined(separator: ", "))
        ◷ Giờ tốt: \(info.gioHoangDao.prefix(3).map { "\($0.name) (\($0.time))" }.joined(separator: ", "))
        """
    }
}
