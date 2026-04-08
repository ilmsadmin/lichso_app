import Foundation
import SwiftUI
import SwiftData

// MARK: - Chat UI State
struct ChatUiState {
    var messages: [ChatMessageEntity] = []
    var isTyping: Bool = false
}

// MARK: - ChatViewModel
@MainActor
class ChatViewModel: ObservableObject {
    @Published var uiState = ChatUiState()

    private var modelContext: ModelContext?

    init() {}

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
            uiState.messages = try context.fetch(descriptor)
        } catch {
            print("Error loading messages: \(error)")
        }
    }

    func sendMessage(_ text: String) {
        guard !text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
        guard let context = modelContext else { return }

        // Save user message
        let userMsg = ChatMessageEntity(content: text, isUser: true)
        context.insert(userMsg)
        try? context.save()
        uiState.messages.append(userMsg)

        // Call AI
        uiState.isTyping = true
        Task {
            let systemPrompt = await OpenRouterAPI.shared.buildSystemPrompt()

            // Build messages for API
            let recentMessages = uiState.messages.suffix(20)
            let apiMessages: [ChatMessage] = recentMessages.map {
                ChatMessage(role: $0.isUser ? "user" : "assistant", content: $0.content)
            }

            let response: String
            do {
                response = try await OpenRouterAPI.shared.sendMessage(messages: apiMessages, systemPrompt: systemPrompt)
            } catch {
                response = "Xin lỗi, tôi không thể trả lời lúc này. Vui lòng thử lại sau."
            }

            let assistantMsg = ChatMessageEntity(content: response, isUser: false)
            context.insert(assistantMsg)
            try? context.save()

            uiState.messages.append(assistantMsg)
            uiState.isTyping = false
        }
    }

    func clearChat() {
        guard let context = modelContext else { return }
        for msg in uiState.messages {
            context.delete(msg)
        }
        try? context.save()
        uiState.messages.removeAll()
    }
}
