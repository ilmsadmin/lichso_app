import SwiftUI
import SwiftData

struct AIChatScreen: View {
    @Environment(\.lichSoColors) private var c
    @Environment(\.modelContext) private var modelContext
    @StateObject private var viewModel = ChatViewModel()
    @State private var inputText = ""
    @FocusState private var isInputFocused: Bool
    var onBackClick: () -> Void = {}
    var onNavigateToProfile: () -> Void = {}
    var initialMessage: String? = nil

    var body: some View {
        VStack(spacing: 0) {
            // Top bar
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(c.textPrimary)
                }
                Spacer()
                VStack(spacing: 2) {
                    HStack(spacing: 6) {
                        Image(systemName: "sparkles")
                            .font(.system(size: 14))
                            .foregroundColor(c.gold)
                        Text("Trợ lý AI")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(c.textPrimary)
                    }
                    Text("Tử vi • Lịch vạn niên • Phong thủy")
                        .font(.system(size: 11))
                        .foregroundColor(c.textTertiary)
                }
                Spacer()
                Menu {
                    Button("Xóa đoạn chat", role: .destructive) {
                        viewModel.clearChat()
                    }
                    Button("Hồ sơ cá nhân") {
                        onNavigateToProfile()
                    }
                } label: {
                    Image(systemName: "ellipsis.circle")
                        .font(.system(size: 20))
                        .foregroundColor(c.textSecondary)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(c.bg)

            Divider()

            // Chat messages
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 12) {
                        // Welcome message
                        if viewModel.uiState.messages.isEmpty {
                            welcomeSection
                        }

                        ForEach(viewModel.uiState.messages, id: \.id) { message in
                            ChatBubble(message: message, c: c)
                                .id(message.id)
                        }

                        if viewModel.uiState.isTyping {
                            typingIndicator
                        }
                    }
                    .padding(16)
                }
                .onChange(of: viewModel.uiState.messages.count) { _, _ in
                    if let lastMsg = viewModel.uiState.messages.last {
                        withAnimation {
                            proxy.scrollTo(lastMsg.id, anchor: .bottom)
                        }
                    }
                }
            }

            // Input bar
            HStack(spacing: 12) {
                TextField("Nhập câu hỏi...", text: $inputText, axis: .vertical)
                    .textFieldStyle(.plain)
                    .font(.system(size: 15))
                    .foregroundColor(c.textPrimary)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(
                        RoundedRectangle(cornerRadius: 24)
                            .fill(c.surface)
                    )
                    .lineLimit(1...5)
                    .focused($isInputFocused)

                Button(action: {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                }) {
                    Image(systemName: "arrow.up.circle.fill")
                        .font(.system(size: 36))
                        .foregroundColor(inputText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? c.outline : c.primary)
                }
                .disabled(inputText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || viewModel.uiState.isTyping)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(c.bg)
        }
        .background(c.bg)
        .onAppear {
            viewModel.setModelContext(modelContext)
            if let msg = initialMessage {
                viewModel.sendMessage(msg)
            }
        }
    }

    // MARK: - Welcome Section
    private var welcomeSection: some View {
        VStack(spacing: 16) {
            Image(systemName: "sparkles")
                .font(.system(size: 48))
                .foregroundColor(c.gold)
                .padding(.top, 40)

            Text("Xin chào! 👋")
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(c.textPrimary)

            Text("Tôi là trợ lý AI của Lịch Số. Tôi có thể giúp bạn xem tử vi, phong thủy, giải đáp về lịch vạn niên và nhiều hơn nữa.")
                .font(.system(size: 14))
                .foregroundColor(c.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)

            // Suggestion chips
            VStack(spacing: 8) {
                suggestionChip("Hôm nay ngày tốt hay xấu?")
                suggestionChip("Xem tử vi của tôi")
                suggestionChip("Ngày nào tốt để khai trương?")
                suggestionChip("Giải thích 12 con giáp")
            }
            .padding(.top, 8)
        }
    }

    @ViewBuilder
    private func suggestionChip(_ text: String) -> some View {
        Button(action: {
            inputText = text
            viewModel.sendMessage(text)
            inputText = ""
        }) {
            Text(text)
                .font(.system(size: 13, weight: .medium))
                .foregroundColor(c.primary)
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(c.primary.opacity(0.3), lineWidth: 1)
                        .background(c.primaryContainer.opacity(0.3).cornerRadius(20))
                )
        }
    }

    // MARK: - Typing Indicator
    private var typingIndicator: some View {
        HStack(spacing: 4) {
            ForEach(0..<3, id: \.self) { i in
                Circle()
                    .fill(c.textTertiary)
                    .frame(width: 8, height: 8)
                    .opacity(0.6)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(c.surface)
        )
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

// MARK: - Chat Bubble
struct ChatBubble: View {
    let message: ChatMessageEntity
    let c: LichSoColors

    private var isUser: Bool { message.isUser }

    var body: some View {
        HStack {
            if isUser { Spacer(minLength: 60) }

            VStack(alignment: isUser ? .trailing : .leading, spacing: 4) {
                Text(message.content)
                    .font(.system(size: 14))
                    .foregroundColor(isUser ? .white : c.textPrimary)
                    .textSelection(.enabled)

                Text(formatTime(message.timestamp))
                    .font(.system(size: 10))
                    .foregroundColor(isUser ? .white.opacity(0.7) : c.textTertiary)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background(
                RoundedRectangle(cornerRadius: 18)
                    .fill(isUser ? AnyShapeStyle(c.fabGradient) : AnyShapeStyle(c.surface))
            )

            if !isUser { Spacer(minLength: 60) }
        }
    }

    private func formatTime(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: date)
    }
}
