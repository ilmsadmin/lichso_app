import SwiftUI
import SwiftData

// ═══════════════════════════════════════════
// AI Chat Day Screen — Ask AI about a specific date
// Matches screen-ai-chat-day.html design
// Shows date attachment card + AI conversation
// ═══════════════════════════════════════════

private var PrimaryRed: Color { LSTheme.primary }
private var DeepRed: Color { LSTheme.deepRed }
private var GoldAccent: Color { LSTheme.gold }
private var SurfaceBg: Color { LSTheme.bg }
private var SurfaceContainer: Color { LSTheme.surfaceContainer }
private var SurfaceContainerHigh: Color { LSTheme.surfaceContainerHigh }
private var TextMain: Color { LSTheme.textPrimary }
private var TextSub: Color { LSTheme.textSecondary }
private var TextDim: Color { LSTheme.textTertiary }
private var OutlineVariant: Color { LSTheme.outlineVariant }
private var GoodGreen: Color { LSTheme.goodGreen }

struct AIChatDayScreen: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    @StateObject private var viewModel = ChatViewModel()
    @State private var inputText = ""
    @FocusState private var isInputFocused: Bool
    
    let day: Int
    let month: Int
    let year: Int
    
    private var dayInfo: DayInfo {
        DayInfoProvider.shared.getDayInfo(dd: day, mm: month, yy: year)
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // ═══ AI HEADER ═══
            ChatDayHeader(
                onBackClick: { dismiss() },
                onMoreClick: { }
            )
            
            // ═══ CHAT AREA ═══
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 10) {
                        // ── Date Attachment Card ──
                        DateAttachmentCard(info: dayInfo, day: day, month: month, year: year)
                            .padding(.top, 4)
                        
                        // ── Messages ──
                        ForEach(viewModel.messages, id: \.id) { message in
                            ChatDayBubble(message: message)
                                .id(message.id)
                        }
                        
                        if viewModel.isTyping {
                            TypingDots()
                                .id("typing")
                        }
                        
                        // ── Inline Suggestions ──
                        if !viewModel.followUpSuggestions.isEmpty && !viewModel.isTyping {
                            InlineSuggestions(
                                dateLabel: String(format: "%02d/%02d", day, month),
                                suggestions: viewModel.followUpSuggestions
                            ) { suggestion in
                                viewModel.sendMessage(suggestion)
                            }
                        } else if viewModel.messages.count <= 1 && !viewModel.isTyping {
                            DateQuickSuggestions(day: day, month: month) { question in
                                viewModel.sendWithDateContext(dd: day, mm: month, yy: year, question: question)
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                }
                .onChange(of: viewModel.messages.count) { _, _ in
                    withAnimation {
                        if let last = viewModel.messages.last {
                            proxy.scrollTo(last.id, anchor: .bottom)
                        }
                    }
                }
                .onChange(of: viewModel.isTyping) { _, isTyping in
                    if isTyping {
                        withAnimation { proxy.scrollTo("typing", anchor: .bottom) }
                    }
                }
            }
            
            // ═══ INPUT BAR ═══
            ChatDayInputBar(
                text: $inputText,
                isTyping: viewModel.isTyping,
                isFocused: $isInputFocused,
                placeholder: "Hỏi thêm về ngày \(String(format: "%02d/%02d", day, month))...",
                onSend: {
                    guard !inputText.trimmingCharacters(in: .whitespaces).isEmpty else { return }
                    viewModel.sendWithDateContext(dd: day, mm: month, yy: year, question: inputText)
                    inputText = ""
                }
            )
        }
        .background(SurfaceBg)
        .navigationBarHidden(true)
        .onAppear {
            viewModel.setModelContext(modelContext)
            // Auto-send initial analysis request
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                viewModel.sendWithDateContext(
                    dd: day, mm: month, yy: year,
                    question: "Hãy phân tích chi tiết ngày \(String(format: "%02d/%02d/%04d", day, month, year)) cho tôi. Ngày này tốt xấu thế nào, nên làm gì và không nên làm gì?"
                )
            }
        }
    }
}

// ══════════════════════════════════════════
// CHAT DAY HEADER
// ══════════════════════════════════════════

private struct ChatDayHeader: View {
    let onBackClick: () -> Void
    let onMoreClick: () -> Void
    
    var body: some View {
        ZStack {
            LinearGradient(
                colors: [PrimaryRed, Color(hex: "C62828"), DeepRed],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            
            Circle()
                .fill(
                    RadialGradient(
                        colors: [Color.yellow.opacity(0.1), Color.clear],
                        center: .center,
                        startRadius: 0,
                        endRadius: 80
                    )
                )
                .frame(width: 160, height: 160)
                .offset(x: 100, y: -30)
            
            HStack(spacing: 12) {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(width: 36, height: 36)
                        .background(Color.white.opacity(0.12))
                        .clipShape(Circle())
                }
                
                ZStack {
                    Circle()
                        .fill(Color.white.opacity(0.15))
                        .frame(width: 40, height: 40)
                    Image(systemName: "sparkles")
                        .font(.system(size: 20))
                        .foregroundColor(GoldAccent)
                }
                
                VStack(alignment: .leading, spacing: 2) {
                    Text("Trợ lý Phong Thủy AI")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.white)
                    HStack(spacing: 4) {
                        Circle()
                            .fill(Color(hex: "4CAF50"))
                            .frame(width: 6, height: 6)
                        Text("Đang trực tuyến")
                            .font(.system(size: 11))
                            .foregroundColor(.white.opacity(0.7))
                    }
                }
                
                Spacer()
                
                Button(action: onMoreClick) {
                    Image(systemName: "ellipsis")
                        .font(.system(size: 18))
                        .foregroundColor(.white)
                        .frame(width: 36, height: 36)
                        .background(Color.white.opacity(0.12))
                        .clipShape(Circle())
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 4)
        }
        .frame(height: 76)
    }
}

// ══════════════════════════════════════════
// DATE ATTACHMENT CARD
// ══════════════════════════════════════════

private struct DateAttachmentCard: View {
    let info: DayInfo
    let day: Int
    let month: Int
    let year: Int
    
    var body: some View {
        VStack(alignment: .trailing, spacing: 4) {
            // Label
            HStack(spacing: 3) {
                Image(systemName: "paperclip")
                    .font(.system(size: 11))
                Text("Đính kèm ngày")
                    .font(.system(size: 10, weight: .semibold))
            }
            .foregroundColor(TextDim)
            
            // Card
            HStack(spacing: 12) {
                // Day box
                VStack(spacing: 2) {
                    Text(String(format: "%02d", day))
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(.white)
                    Text("Th\(month)")
                        .font(.system(size: 9, weight: .semibold))
                        .foregroundColor(.white.opacity(0.8))
                }
                .frame(width: 50, height: 50)
                .background(
                    LinearGradient(
                        colors: [PrimaryRed, Color(hex: "C62828")],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .cornerRadius(13)
                .shadow(color: PrimaryRed.opacity(0.2), radius: 4, y: 2)
                
                // Info
                VStack(alignment: .leading, spacing: 2) {
                    Text("\(info.dayOfWeek), \(String(format: "%02d/%02d/%04d", day, month, year))")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(TextMain)
                    
                    HStack(spacing: 3) {
                        Image(systemName: "moon.fill")
                            .font(.system(size: 11))
                            .foregroundColor(TextSub)
                        Text("\(info.lunar.day) tháng \(info.lunar.month) Âm lịch · \(info.dayCanChi)")
                            .font(.system(size: 11))
                            .foregroundColor(TextSub)
                    }
                    
                    HStack(spacing: 5) {
                        // Good day tag
                        if !info.activities.isXauDay {
                            HStack(spacing: 3) {
                                Image(systemName: "checkmark.seal.fill")
                                    .font(.system(size: 9))
                                Text("Hoàng Đạo")
                                    .font(.system(size: 10, weight: .semibold))
                            }
                            .padding(.horizontal, 7)
                            .padding(.vertical, 2)
                            .background(GoodGreen.opacity(0.12))
                            .foregroundColor(GoodGreen)
                            .cornerRadius(6)
                        }
                        
                        // Truc tag
                        HStack(spacing: 3) {
                            Image(systemName: "star.fill")
                                .font(.system(size: 9))
                            Text("Trực \(info.trucNgay.name)")
                                .font(.system(size: 10, weight: .semibold))
                        }
                        .padding(.horizontal, 7)
                        .padding(.vertical, 2)
                        .background(PrimaryRed.opacity(0.08))
                        .foregroundColor(PrimaryRed)
                        .cornerRadius(6)
                    }
                    .padding(.top, 3)
                }
                
                Spacer()
            }
            .padding(12)
            .background(
                LinearGradient(
                    colors: [Color(hex: "1A1814"), Color(hex: "2A2720")],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            )
            .cornerRadius(18)
            .overlay(
                RoundedRectangle(cornerRadius: 18)
                    .stroke(OutlineVariant, lineWidth: 1)
            )
        }
    }
}

// ══════════════════════════════════════════
// DATE QUICK SUGGESTIONS
// ══════════════════════════════════════════

private struct DateQuickSuggestions: View {
    let day: Int
    let month: Int
    let onTap: (String) -> Void
    
    private var suggestions: [(String, String)] {
        [
            ("storefront.fill", "Giờ khai trương tốt?"),
            ("heart.fill", "Hợp cưới hỏi không?"),
            ("house.fill", "Nên nhập trạch?"),
            ("safari.fill", "Hướng xuất hành?"),
        ]
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("GỢI Ý HỎI VỀ NGÀY \(String(format: "%02d/%02d", day, month))")
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(TextDim)
                .tracking(0.6)
            
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 6) {
                ForEach(suggestions, id: \.1) { icon, label in
                    Button {
                        onTap(label)
                    } label: {
                        HStack(spacing: 4) {
                            Image(systemName: icon)
                                .font(.system(size: 12))
                                .foregroundColor(PrimaryRed)
                            Text(label)
                                .font(.system(size: 12, weight: .medium))
                                .foregroundColor(TextMain)
                        }
                        .padding(.horizontal, 13)
                        .padding(.vertical, 7)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(SurfaceContainerHigh)
                        .cornerRadius(20)
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(OutlineVariant, lineWidth: 1)
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// INLINE SUGGESTIONS (from AI response)
// ══════════════════════════════════════════

private struct InlineSuggestions: View {
    let dateLabel: String
    let suggestions: [String]
    let onTap: (String) -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("GỢI Ý TIẾP")
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(TextDim)
                .tracking(0.6)
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    ForEach(suggestions, id: \.self) { suggestion in
                        Button {
                            onTap(suggestion)
                        } label: {
                            HStack(spacing: 4) {
                                Image(systemName: "sparkle")
                                    .font(.system(size: 10))
                                    .foregroundColor(PrimaryRed)
                                Text(suggestion)
                                    .font(.system(size: 12, weight: .medium))
                                    .foregroundColor(TextMain)
                            }
                            .padding(.horizontal, 12)
                            .padding(.vertical, 7)
                            .background(SurfaceContainerHigh)
                            .cornerRadius(18)
                            .overlay(
                                RoundedRectangle(cornerRadius: 18)
                                    .stroke(OutlineVariant, lineWidth: 1)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// CHAT DAY BUBBLE
// ══════════════════════════════════════════

private struct ChatDayBubble: View {
    let message: ChatMessageEntity
    
    var body: some View {
        HStack {
            if message.isUser { Spacer(minLength: 60) }
            
            VStack(alignment: message.isUser ? .trailing : .leading, spacing: 0) {
                if !message.isUser {
                    HStack(spacing: 6) {
                        Image(systemName: "sparkles")
                            .font(.system(size: 12))
                            .foregroundColor(PrimaryRed)
                        Text("Trợ lý Phong Thủy AI")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(PrimaryRed)
                    }
                    .padding(.bottom, 6)
                }
                
                DayRichContentView(content: message.content, isUser: message.isUser)
            }
            .padding(14)
            .background(
                message.isUser
                    ? AnyShapeStyle(LinearGradient(colors: [PrimaryRed, Color(hex: "C62828")], startPoint: .topLeading, endPoint: .bottomTrailing))
                    : AnyShapeStyle(SurfaceContainer)
            )
            .cornerRadius(20, corners: message.isUser ? [.topLeft, .topRight, .bottomLeft] : [.topLeft, .topRight, .bottomRight])
            
            if !message.isUser { Spacer(minLength: 60) }
        }
    }
}

// ══════════════════════════════════════════
// DAY RICH CONTENT (same parser as main chat)
// ══════════════════════════════════════════

private struct DayRichContentView: View {
    let content: String
    let isUser: Bool
    
    var body: some View {
        let blocks = parseBlocks(content)
        VStack(alignment: .leading, spacing: 8) {
            ForEach(Array(blocks.enumerated()), id: \.offset) { _, block in
                switch block {
                case .text(let text):
                    Text(text)
                        .font(.system(size: 14))
                        .lineSpacing(4)
                        .foregroundColor(isUser ? .white : TextMain)
                case .keyValue(let rows):
                    DayResultCard(rows: rows)
                case .header(let text):
                    Text(text)
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(isUser ? .white : PrimaryRed)
                case .bullet(let items):
                    VStack(alignment: .leading, spacing: 4) {
                        ForEach(items, id: \.self) { item in
                            HStack(alignment: .top, spacing: 6) {
                                Text("›")
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(isUser ? .white.opacity(0.7) : PrimaryRed)
                                Text(item)
                                    .font(.system(size: 13))
                                    .foregroundColor(isUser ? .white : TextMain)
                            }
                        }
                    }
                }
            }
        }
    }
    
    enum ContentBlock {
        case text(String)
        case keyValue([(String, String)])
        case header(String)
        case bullet([String])
    }
    
    private func parseBlocks(_ raw: String) -> [ContentBlock] {
        let lines = raw.components(separatedBy: "\n")
        var blocks: [ContentBlock] = []
        var textBuffer = ""
        var kvRows: [(String, String)] = []
        var bulletItems: [String] = []
        
        func flushText() {
            let t = textBuffer.trimmingCharacters(in: .whitespacesAndNewlines)
            if !t.isEmpty { blocks.append(.text(t)) }
            textBuffer = ""
        }
        func flushKV() {
            if !kvRows.isEmpty { blocks.append(.keyValue(kvRows)); kvRows = [] }
        }
        func flushBullets() {
            if !bulletItems.isEmpty { blocks.append(.bullet(bulletItems)); bulletItems = [] }
        }
        func flushAll() { flushBullets(); flushKV(); flushText() }
        
        for line in lines {
            let trimmed = line.trimmingCharacters(in: .whitespaces)
            
            if trimmed.isEmpty {
                flushBullets(); flushKV()
                if !textBuffer.isEmpty { textBuffer += "\n" }
                continue
            }
            
            if trimmed.hasPrefix("- ") || trimmed.hasPrefix("* ") || trimmed.hasPrefix("• ") || trimmed.hasPrefix("› ") {
                flushKV(); flushText()
                bulletItems.append(String(trimmed.dropFirst(2)))
                continue
            }
            
            if let colonRange = trimmed.range(of: ": ", options: .literal) {
                let key = String(trimmed[trimmed.startIndex..<colonRange.lowerBound]).replacingOccurrences(of: "**", with: "")
                let value = String(trimmed[colonRange.upperBound...]).replacingOccurrences(of: "**", with: "")
                let keyWords = key.split(separator: " ")
                if keyWords.count <= 5 && key.count <= 40 && !key.lowercased().hasPrefix("xin ") && !key.lowercased().hasPrefix("chào ") && !key.lowercased().hasPrefix("tôi ") {
                    flushBullets(); flushText()
                    kvRows.append((key, value))
                    continue
                }
            }
            
            flushBullets(); flushKV()
            if !textBuffer.isEmpty { textBuffer += "\n" }
            textBuffer += trimmed.replacingOccurrences(of: "**", with: "")
        }
        
        flushAll()
        return blocks
    }
}

// ══════════════════════════════════════════
// DAY RESULT CARD
// ══════════════════════════════════════════

private struct DayResultCard: View {
    let rows: [(String, String)]
    
    var body: some View {
        VStack(spacing: 0) {
            ForEach(Array(rows.enumerated()), id: \.offset) { index, row in
                HStack {
                    Text(row.0)
                        .font(.system(size: 11))
                        .foregroundColor(TextSub)
                    Spacer()
                    Text(row.1)
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(colorForValue(row.1))
                        .multilineTextAlignment(.trailing)
                }
                .padding(.vertical, 6)
                
                if index < rows.count - 1 {
                    Divider().background(Color.black.opacity(0.05))
                }
            }
        }
        .padding(12)
        .background(SurfaceContainerHigh)
        .cornerRadius(14)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(OutlineVariant, lineWidth: 1)
        )
    }
    
    private func colorForValue(_ value: String) -> Color {
        if value.contains("✦") || value.contains("✓") || value.contains("★") || value.contains("Tốt") || value.contains("Nên") || value.contains("Rất hợp") {
            return GoodGreen
        }
        if value.contains("✗") || value.contains("Xấu") || value.contains("Không nên") || value.contains("tránh") {
            return Color(hex: "C62828")
        }
        if value.contains("↗") || value.contains("↘") || value.contains("giờ") {
            return PrimaryRed
        }
        return TextMain
    }
}

// ══════════════════════════════════════════
// TYPING DOTS
// ══════════════════════════════════════════

private struct TypingDots: View {
    @State private var a1 = false
    @State private var a2 = false
    @State private var a3 = false
    
    var body: some View {
        HStack {
            HStack(spacing: 4) {
                Circle().fill(TextDim).frame(width: 7, height: 7).offset(y: a1 ? -5 : 0)
                Circle().fill(TextDim).frame(width: 7, height: 7).offset(y: a2 ? -5 : 0)
                Circle().fill(TextDim).frame(width: 7, height: 7).offset(y: a3 ? -5 : 0)
            }
            .padding(.horizontal, 18)
            .padding(.vertical, 14)
            .background(SurfaceContainer)
            .cornerRadius(20, corners: [.topLeft, .topRight, .bottomRight])
            Spacer()
        }
        .onAppear {
            withAnimation(.easeInOut(duration: 0.5).repeatForever(autoreverses: true)) { a1 = true }
            withAnimation(.easeInOut(duration: 0.5).repeatForever(autoreverses: true).delay(0.2)) { a2 = true }
            withAnimation(.easeInOut(duration: 0.5).repeatForever(autoreverses: true).delay(0.4)) { a3 = true }
        }
    }
}

// ══════════════════════════════════════════
// CHAT DAY INPUT BAR
// ══════════════════════════════════════════

private struct ChatDayInputBar: View {
    @Binding var text: String
    let isTyping: Bool
    var isFocused: FocusState<Bool>.Binding
    let placeholder: String
    let onSend: () -> Void
    
    var body: some View {
        HStack(alignment: .bottom, spacing: 8) {
            HStack(spacing: 6) {
                TextField(placeholder, text: $text, axis: .vertical)
                    .lineLimit(1...4)
                    .font(.system(size: 14))
                    .focused(isFocused)
                
                Button { } label: {
                    Image(systemName: "mic.fill")
                        .font(.system(size: 16))
                        .foregroundColor(TextDim)
                        .frame(width: 32, height: 32)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 6)
            .background(SurfaceContainer)
            .cornerRadius(24)
            .overlay(
                RoundedRectangle(cornerRadius: 24)
                    .stroke(OutlineVariant, lineWidth: 1.5)
            )
            
            Button(action: onSend) {
                Image(systemName: "paperplane.fill")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(width: 44, height: 44)
                    .background(
                        LinearGradient(
                            colors: [PrimaryRed, Color(hex: "C62828")],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .clipShape(Circle())
                    .shadow(color: PrimaryRed.opacity(0.3), radius: 4, y: 2)
            }
            .disabled(isTyping || text.trimmingCharacters(in: .whitespaces).isEmpty)
            .opacity(!isTyping && !text.trimmingCharacters(in: .whitespaces).isEmpty ? 1 : 0.5)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .padding(.bottom, 16)
        .background(SurfaceBg)
        .overlay(
            Divider().background(OutlineVariant),
            alignment: .top
        )
    }
}
