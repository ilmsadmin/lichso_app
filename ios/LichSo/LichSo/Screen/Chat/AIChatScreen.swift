import SwiftUI
import SwiftData

// ═══════════════════════════════════════════
// AI Chat Screen — Main AI Phong Thủy chat
// Matches screen-ai-chat.html design
// ═══════════════════════════════════════════

private let PrimaryRed = Color(hex: "B71C1C")
private let DeepRed = Color(hex: "8B0000")
private let GoldAccent = Color(hex: "D4A017")
private let SurfaceBg = Color(hex: "FFFBF5")
private let SurfaceContainer = Color(hex: "FFF8F0")
private let SurfaceContainerHigh = Color(hex: "FFF0E8")
private let TextMain = Color(hex: "1C1B1F")
private let TextSub = Color(hex: "534340")
private let TextDim = Color(hex: "857371")
private let OutlineVariant = Color(hex: "D8C2BF")
private let GoodGreen = Color(hex: "2E7D32")
private let BadRed = Color(hex: "C62828")

struct AIChatScreen: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    @StateObject private var viewModel = ChatViewModel()
    @State private var inputText = ""
    @State private var showClearConfirm = false
    @FocusState private var isInputFocused: Bool
    
    var initialMessage: String? = nil
    
    var body: some View {
        VStack(spacing: 0) {
            // ═══ AI HEADER ═══
            AiChatHeader(
                onBackClick: { dismiss() },
                onClearClick: { showClearConfirm = true }
            )
            
            // ═══ SUGGESTION CHIPS ═══
            SuggestionChipsRow(viewModel: viewModel)
            
            // ═══ CHAT MESSAGES ═══
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.messages, id: \.id) { message in
                            ChatBubbleView(message: message, viewModel: viewModel)
                                .id(message.id)
                        }
                        if viewModel.isTyping {
                            TypingIndicatorView()
                                .id("typing")
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
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
                        withAnimation {
                            proxy.scrollTo("typing", anchor: .bottom)
                        }
                    }
                }
            }
            
            // ═══ FOLLOW-UP SUGGESTIONS ═══
            if !viewModel.followUpSuggestions.isEmpty && !viewModel.isTyping {
                FollowUpSuggestionsRow(suggestions: viewModel.followUpSuggestions) { suggestion in
                    viewModel.sendMessage(suggestion)
                }
            }
            
            // ═══ INPUT BAR ═══
            ChatInputBar(
                text: $inputText,
                isTyping: viewModel.isTyping,
                isFocused: $isInputFocused,
                onSend: {
                    guard !inputText.trimmingCharacters(in: .whitespaces).isEmpty else { return }
                    viewModel.sendMessage(inputText)
                    inputText = ""
                }
            )
        }
        .background(SurfaceBg)
        .navigationBarHidden(true)
        .onAppear {
            viewModel.setModelContext(modelContext)
            if let msg = initialMessage, !msg.isEmpty {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    viewModel.sendMessage(msg)
                }
            }
        }
        .alert("Xoá lịch sử chat?", isPresented: $showClearConfirm) {
            Button("Huỷ", role: .cancel) { }
            Button("Xoá", role: .destructive) {
                viewModel.clearChat()
            }
        } message: {
            Text("Tất cả tin nhắn sẽ bị xoá và không thể khôi phục.")
        }
    }
}

// ══════════════════════════════════════════
// AI HEADER — Red gradient
// ══════════════════════════════════════════

private struct AiChatHeader: View {
    let onBackClick: () -> Void
    let onClearClick: () -> Void
    
    var body: some View {
        ZStack {
            // Background gradient
            LinearGradient(
                colors: [PrimaryRed, Color(hex: "C62828"), DeepRed],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            
            // Decorative gold radial
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
                // Back button
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(width: 36, height: 36)
                        .background(Color.white.opacity(0.12))
                        .clipShape(Circle())
                }
                
                // Avatar
                ZStack {
                    Circle()
                        .fill(Color.white.opacity(0.15))
                        .frame(width: 40, height: 40)
                    Image(systemName: "sparkles")
                        .font(.system(size: 20))
                        .foregroundColor(GoldAccent)
                }
                
                // Info
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
                
                // Clear button
                Button(action: onClearClick) {
                    Image(systemName: "trash")
                        .font(.system(size: 16))
                        .foregroundColor(.white)
                        .frame(width: 36, height: 36)
                        .background(Color.white.opacity(0.12))
                        .clipShape(Circle())
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 4)
        }
        .frame(height: 80)
    }
}

// ══════════════════════════════════════════
// SUGGESTION CHIPS ROW
// ══════════════════════════════════════════

private struct SuggestionChipsRow: View {
    let viewModel: ChatViewModel
    
    private let suggestions: [(String, String, String)] = [
        ("sparkles", "Tử vi hôm nay", "Xem tử vi hôm nay"),
        ("heart.fill", "Ngày cưới tốt", "Tìm ngày cưới tốt tháng này"),
        ("house.fill", "Ngày xây nhà", "Hôm nay có nên động thổ xây nhà?"),
        ("storefront.fill", "Giờ khai trương", "Giờ tốt khai trương hôm nay"),
        ("star.fill", "Phân tích bát tự", "Phân tích bát tự tứ trụ của tôi"),
        ("arrow.up.right", "Hướng xuất hành", "Hướng xuất hành tốt hôm nay"),
        ("person.2.fill", "Xem hợp tuổi", "Xem hợp tuổi vợ chồng"),
        ("paintpalette.fill", "Ngũ hành & mệnh", "Phân tích ngũ hành và mệnh của tôi"),
    ]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("GỢI Ý HỎI")
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(TextDim)
                .tracking(0.5)
                .padding(.leading, 16)
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    ForEach(suggestions, id: \.1) { icon, label, message in
                        Button {
                            viewModel.sendMessage(message)
                        } label: {
                            HStack(spacing: 4) {
                                Image(systemName: icon)
                                    .font(.system(size: 12))
                                Text(label)
                                    .font(.system(size: 12, weight: .medium))
                            }
                            .padding(.horizontal, 14)
                            .padding(.vertical, 8)
                            .background(SurfaceContainerHigh)
                            .foregroundColor(TextMain)
                            .cornerRadius(20)
                            .overlay(
                                RoundedRectangle(cornerRadius: 20)
                                    .stroke(OutlineVariant, lineWidth: 1)
                            )
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
        }
        .padding(.top, 12)
        .padding(.bottom, 6)
        .background(SurfaceBg)
    }
}

// ══════════════════════════════════════════
// FOLLOW-UP SUGGESTIONS
// ══════════════════════════════════════════

private struct FollowUpSuggestionsRow: View {
    let suggestions: [String]
    let onTap: (String) -> Void
    
    var body: some View {
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
            .padding(.horizontal, 16)
            .padding(.vertical, 6)
        }
    }
}

// ══════════════════════════════════════════
// CHAT BUBBLE
// ══════════════════════════════════════════

private struct ChatBubbleView: View {
    let message: ChatMessageEntity
    let viewModel: ChatViewModel
    
    var body: some View {
        HStack {
            if message.isUser { Spacer(minLength: 60) }
            
            VStack(alignment: message.isUser ? .trailing : .leading, spacing: 0) {
                if !message.isUser {
                    // AI header
                    HStack(spacing: 6) {
                        Image(systemName: "sparkles")
                            .font(.system(size: 12))
                            .foregroundColor(PrimaryRed)
                        Text("Phân tích AI")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(PrimaryRed)
                    }
                    .padding(.bottom, 6)
                }
                
                // Parse and render rich content
                RichContentView(content: message.content, isUser: message.isUser)
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
// RICH CONTENT VIEW — Parses AI response
// ══════════════════════════════════════════

private struct RichContentView: View {
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
                    ResultCardView(rows: rows)
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
    
    // MARK: - Content Block Types
    
    enum ContentBlock {
        case text(String)
        case keyValue([(String, String)])
        case header(String)
        case bullet([String])
    }
    
    // MARK: - Parser
    
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
            
            // Bullet: - item or * item or • item or › item
            if trimmed.hasPrefix("- ") || trimmed.hasPrefix("* ") || trimmed.hasPrefix("• ") || trimmed.hasPrefix("› ") {
                flushKV(); flushText()
                bulletItems.append(String(trimmed.dropFirst(2)))
                continue
            }
            
            // Key:Value detection
            if let colonRange = trimmed.range(of: ": ", options: .literal) {
                let key = String(trimmed[trimmed.startIndex..<colonRange.lowerBound])
                    .replacingOccurrences(of: "**", with: "")
                let value = String(trimmed[colonRange.upperBound...])
                    .replacingOccurrences(of: "**", with: "")
                let keyWords = key.split(separator: " ")
                if keyWords.count <= 5 && key.count <= 40 && !key.lowercased().hasPrefix("xin ") && !key.lowercased().hasPrefix("chào ") && !key.lowercased().hasPrefix("tôi ") {
                    flushBullets(); flushText()
                    kvRows.append((key, value))
                    continue
                }
            }
            
            // Regular text
            flushBullets(); flushKV()
            if !textBuffer.isEmpty { textBuffer += "\n" }
            textBuffer += trimmed.replacingOccurrences(of: "**", with: "")
        }
        
        flushAll()
        return blocks
    }
}

// ══════════════════════════════════════════
// RESULT CARD (Key-Value Pairs)
// ══════════════════════════════════════════

private struct ResultCardView: View {
    let rows: [(String, String)]
    
    var body: some View {
        VStack(spacing: 0) {
            ForEach(Array(rows.enumerated()), id: \.offset) { index, row in
                HStack {
                    Text(row.0)
                        .font(.system(size: 12))
                        .foregroundColor(TextSub)
                    Spacer()
                    Text(row.1)
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(colorForValue(row.1))
                        .multilineTextAlignment(.trailing)
                }
                .padding(.vertical, 6)
                
                if index < rows.count - 1 {
                    Divider().background(OutlineVariant.opacity(0.5))
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
            return BadRed
        }
        if value.contains("↗") || value.contains("↘") || value.contains("giờ") {
            return PrimaryRed
        }
        return TextMain
    }
}

// ══════════════════════════════════════════
// TYPING INDICATOR
// ══════════════════════════════════════════

private struct TypingIndicatorView: View {
    @State private var animateFirst = false
    @State private var animateSecond = false
    @State private var animateThird = false
    
    var body: some View {
        HStack {
            HStack(spacing: 4) {
                Circle().fill(TextDim).frame(width: 8, height: 8)
                    .offset(y: animateFirst ? -6 : 0)
                Circle().fill(TextDim).frame(width: 8, height: 8)
                    .offset(y: animateSecond ? -6 : 0)
                Circle().fill(TextDim).frame(width: 8, height: 8)
                    .offset(y: animateThird ? -6 : 0)
            }
            .padding(.horizontal, 18)
            .padding(.vertical, 14)
            .background(SurfaceContainer)
            .cornerRadius(20, corners: [.topLeft, .topRight, .bottomRight])
            
            Spacer()
        }
        .onAppear {
            withAnimation(.easeInOut(duration: 0.5).repeatForever(autoreverses: true)) { animateFirst = true }
            withAnimation(.easeInOut(duration: 0.5).repeatForever(autoreverses: true).delay(0.2)) { animateSecond = true }
            withAnimation(.easeInOut(duration: 0.5).repeatForever(autoreverses: true).delay(0.4)) { animateThird = true }
        }
    }
}

// ══════════════════════════════════════════
// CHAT INPUT BAR
// ══════════════════════════════════════════

private struct ChatInputBar: View {
    @Binding var text: String
    let isTyping: Bool
    var isFocused: FocusState<Bool>.Binding
    let onSend: () -> Void
    
    var body: some View {
        HStack(alignment: .bottom, spacing: 8) {
            TextField("Hỏi AI về tử vi, ngày tốt...", text: $text, axis: .vertical)
                .lineLimit(1...4)
                .font(.system(size: 14))
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(SurfaceContainer)
                .cornerRadius(24)
                .overlay(
                    RoundedRectangle(cornerRadius: 24)
                        .stroke(OutlineVariant, lineWidth: 1)
                )
                .focused(isFocused)
            
            Button(action: onSend) {
                Image(systemName: "arrow.up")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(width: 44, height: 44)
                    .background(
                        !isTyping && !text.trimmingCharacters(in: .whitespaces).isEmpty
                            ? PrimaryRed : PrimaryRed.opacity(0.4)
                    )
                    .clipShape(Circle())
            }
            .disabled(isTyping || text.trimmingCharacters(in: .whitespaces).isEmpty)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .padding(.bottom, 16)
        .background(SurfaceBg)
    }
}

// ══════════════════════════════════════════
// CORNER RADIUS EXTENSION
// ══════════════════════════════════════════

extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners
    
    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(roundedRect: rect, byRoundingCorners: corners, cornerRadii: CGSize(width: radius, height: radius))
        return Path(path.cgPath)
    }
}
