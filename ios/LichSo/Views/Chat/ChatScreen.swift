import SwiftUI

// MARK: - AI Chat Screen
struct ChatScreen: View {
    @ObservedObject var viewModel: ChatViewModel
    let onClose: () -> Void
    @Environment(\.lichSoColors) var c
    @State private var showClearAlert = false
    @State private var showMemorySheet = false
    @FocusState private var inputFocused: Bool

    var body: some View {
        VStack(spacing: 0) {
            // Header
            ChatHeader(
                aiName: viewModel.memory.displayName,
                onClose: onClose,
                onClear: { showClearAlert = true },
                onMemory: { showMemorySheet = true }
            )

            // Messages
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 8) {
                        ForEach(viewModel.messages) { msg in
                            ChatBubble(message: msg, viewModel: viewModel)
                                .id(msg.id)
                        }
                        if viewModel.isTyping {
                            TypingIndicatorView()
                                .id("typing")
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                }
                .onChange(of: viewModel.messages.count) { _ in
                    withAnimation {
                        if let last = viewModel.messages.last {
                            proxy.scrollTo(last.id, anchor: .bottom)
                        }
                    }
                }
                .onChange(of: viewModel.isTyping) { isTyping in
                    if isTyping {
                        withAnimation { proxy.scrollTo("typing", anchor: .bottom) }
                    }
                }
            }

            Divider().overlay(c.border)

            // Quick Topics
            QuickTopicsRow(onTap: { viewModel.sendMessage($0) })

            // Input bar
            ChatInputBar(
                text: $viewModel.inputText,
                isFocused: $inputFocused,
                isEnabled: !viewModel.isTyping,
                onSend: {
                    let t = viewModel.inputText.trimmingCharacters(in: .whitespaces)
                    if !t.isEmpty {
                        viewModel.sendMessage(t)
                        viewModel.inputText = ""
                    }
                }
            )
        }
        .background(c.bg.ignoresSafeArea())
        .alert("Xoá lịch sử chat?", isPresented: $showClearAlert) {
            Button("Xoá", role: .destructive) { viewModel.clearChat() }
            Button("Huỷ", role: .cancel) {}
        } message: {
            Text("Tất cả tin nhắn sẽ bị xoá và không thể khôi phục.")
        }
        .sheet(isPresented: $showMemorySheet) {
            AIMemorySheet(memory: viewModel.memory)
                .environment(\.lichSoColors, c)
        }
    }
}

// MARK: - Chat Header
struct ChatHeader: View {
    let aiName: String
    let onClose: () -> Void
    let onClear: () -> Void
    let onMemory: () -> Void
    @Environment(\.lichSoColors) var c

    var body: some View {
        HStack(spacing: 10) {
            // Robot avatar
            ZStack {
                Circle()
                    .fill(
                        LinearGradient(colors: [c.gold, c.teal], startPoint: .topLeading, endPoint: .bottomTrailing)
                    )
                    .frame(width: 32, height: 32)
                RobotIcon(color: .white)
                    .frame(width: 20, height: 20)
            }

            VStack(alignment: .leading, spacing: 1) {
                Text(aiName)
                    .font(.system(size: 18, weight: .bold, design: .serif))
                    .foregroundColor(c.gold2)
                Text("Trợ lý phong thuỷ thông minh")
                    .font(.system(size: 10.5))
                    .foregroundColor(c.textTertiary)
            }

            Spacer()

            Button(action: onMemory) {
                Image(systemName: "brain.head.profile")
                    .font(.system(size: 16))
                    .foregroundColor(c.teal2)
            }
            .frame(width: 34, height: 34)

            Button(action: onClear) {
                Image(systemName: "trash")
                    .font(.system(size: 16))
                    .foregroundColor(c.textSecondary)
            }
            .frame(width: 34, height: 34)

            Button(action: onClose) {
                Image(systemName: "xmark")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(c.textSecondary)
            }
            .frame(width: 34, height: 34)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 14)
        .background(c.bg)
        .overlay(alignment: .bottom) {
            Divider().overlay(c.border)
        }
    }
}

// MARK: - Chat Bubble
struct ChatBubble: View {
    let message: ChatMessage
    let viewModel: ChatViewModel
    @Environment(\.lichSoColors) var c

    var isUser: Bool { message.isUser }

    var bubbleBg: Color {
        isUser
        ? (c.isDark ? Color(hex: 0x1A2A25) : Color(hex: 0xE4F5F0))
        : c.bg2
    }

    var bubbleBorder: Color {
        isUser ? Color(hex: 0x4ABEAA).opacity(0.2) : c.border
    }

    var body: some View {
        HStack {
            if isUser { Spacer(minLength: 60) }

            VStack(alignment: isUser ? .trailing : .leading, spacing: 4) {
                Text(message.content)
                    .font(.system(size: 13))
                    .foregroundColor(isUser ? c.teal2 : c.textPrimary)
                    .padding(12)
                    .background(bubbleBg)
                    .clipShape(
                        RoundedRectangle(cornerRadius: 14)
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 14)
                            .stroke(bubbleBorder, lineWidth: 1)
                    )

                Text(viewModel.formatTime(message.timestamp))
                    .font(.system(size: 9.5))
                    .foregroundColor(c.textQuaternary)
            }

            if !isUser { Spacer(minLength: 60) }
        }
    }
}

// MARK: - Typing Indicator
struct TypingIndicatorView: View {
    @Environment(\.lichSoColors) var c
    @State private var dotOffset: [Double] = [0, 0, 0]

    var body: some View {
        HStack(alignment: .center, spacing: 6) {
            RobotIcon(color: c.teal)
                .frame(width: 16, height: 16)
            Text("Đang suy nghĩ...")
                .font(.system(size: 12, weight: .medium))
                .foregroundColor(c.textTertiary)
            HStack(spacing: 3) {
                ForEach(0..<3) { i in
                    Circle()
                        .fill(c.gold)
                        .frame(width: 5, height: 5)
                        .offset(y: dotOffset[i])
                        .animation(
                            .easeInOut(duration: 0.5).repeatForever().delay(Double(i) * 0.15),
                            value: dotOffset[i]
                        )
                }
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(c.bg2)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(c.border, lineWidth: 1))
        .frame(maxWidth: .infinity, alignment: .leading)
        .onAppear {
            for i in 0..<3 {
                DispatchQueue.main.asyncAfter(deadline: .now() + Double(i) * 0.15) {
                    withAnimation(.easeInOut(duration: 0.5).repeatForever(autoreverses: true)) {
                        dotOffset[i] = -4
                    }
                }
            }
        }
    }
}

// MARK: - Quick Topics
struct QuickTopicsRow: View {
    let onTap: (String) -> Void
    @Environment(\.lichSoColors) var c

    let topics: [(String, String)] = [
        ("calendar", "Hôm nay ngày tốt không?"),
        ("clock", "Giờ hoàng đạo"),
        ("location", "Hướng xuất hành"),
        ("heart", "Ngày cưới hỏi"),
        ("storefront", "Khai trương"),
        ("sparkles", "Can chi hôm nay"),
        ("sun.max", "Tiết khí"),
        ("house", "Động thổ xây nhà"),
        ("info.circle", "Bạn giúp gì được?"),
    ]

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 6) {
                ForEach(topics, id: \.1) { (icon, text) in
                    Button(action: { onTap(text) }) {
                        HStack(spacing: 5) {
                            Image(systemName: icon)
                                .font(.system(size: 11))
                                .foregroundColor(c.textSecondary)
                            Text(text)
                                .font(.system(size: 11))
                                .foregroundColor(c.textSecondary)
                                .lineLimit(1)
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(c.bg3)
                        .clipShape(Capsule())
                        .overlay(Capsule().stroke(c.border, lineWidth: 1))
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 6)
        }
    }
}

// MARK: - Chat Input Bar
struct ChatInputBar: View {
    @Binding var text: String
    var isFocused: FocusState<Bool>.Binding
    let isEnabled: Bool
    let onSend: () -> Void
    @Environment(\.lichSoColors) var c

    var canSend: Bool { !text.trimmingCharacters(in: .whitespaces).isEmpty && isEnabled }

    var body: some View {
        HStack(spacing: 8) {
            HStack {
                TextField("Hỏi về phong thuỷ, ngày tốt...", text: $text, axis: .vertical)
                    .font(.system(size: 13))
                    .foregroundColor(c.textPrimary)
                    .focused(isFocused)
                    .lineLimit(1...4)
                    .disabled(!isEnabled)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background(c.bg2)
            .clipShape(RoundedRectangle(cornerRadius: 24))
            .overlay(RoundedRectangle(cornerRadius: 24).stroke(c.border, lineWidth: 1))

            Button(action: onSend) {
                Image(systemName: "paperplane.fill")
                    .font(.system(size: 16))
                    .foregroundColor(canSend ? (c.isDark ? Color(hex: 0x1A1500) : .white) : c.textQuaternary)
                    .frame(width: 40, height: 40)
                    .background(
                        canSend
                        ? LinearGradient(colors: [c.gold, c.teal], startPoint: .topLeading, endPoint: .bottomTrailing)
                        : LinearGradient(colors: [c.surface, c.surface], startPoint: .top, endPoint: .bottom)
                    )
                    .clipShape(Circle())
            }
            .disabled(!canSend)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 8)
        .background(c.bg)
    }
}

// MARK: - AI Memory Sheet
struct AIMemorySheet: View {
    @ObservedObject var memory: AIMemory
    @Environment(\.lichSoColors) var c
    @Environment(\.dismiss) var dismiss
    @State private var editName = ""
    @State private var editAIName = ""
    @State private var showResetConfirm = false

    var body: some View {
        NavigationView {
            ZStack { c.bg.ignoresSafeArea()
                ScrollView {
                    VStack(spacing: 20) {

                        // ── Status ──
                        HStack(spacing: 12) {
                            ZStack {
                                Circle().fill(LinearGradient(colors: [c.gold, c.teal], startPoint: .topLeading, endPoint: .bottomTrailing)).frame(width: 48, height: 48)
                                Image(systemName: "brain.head.profile").font(.system(size: 22)).foregroundColor(.white)
                            }
                            VStack(alignment: .leading, spacing: 3) {
                                Text("Bộ nhớ AI").font(.system(size: 16, weight: .bold)).foregroundColor(c.gold2)
                                Text(memory.isOnboarded ? "Đã nhớ \(memory.habits.count) thói quen" : "Chưa có dữ liệu")
                                    .font(.system(size: 12)).foregroundColor(c.textTertiary)
                            }
                            Spacer()
                        }
                        .padding(16).background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 14))
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(c.border, lineWidth: 1))

                        // ── Tên người dùng ──
                        VStack(alignment: .leading, spacing: 8) {
                            Text("TÊN BẠN").font(.system(size: 10.5, weight: .bold)).foregroundColor(c.textTertiary).kerning(1)
                            HStack(spacing: 10) {
                                Image(systemName: "person.fill").font(.system(size: 14)).foregroundColor(c.teal).frame(width: 20)
                                TextField("Nhập tên của bạn...", text: $editName)
                                    .font(.system(size: 14)).foregroundColor(c.textPrimary)
                                    .onSubmit { if !editName.isEmpty { memory.userName = editName } }
                            }
                            .padding(.horizontal, 14).padding(.vertical, 12)
                            .background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 12))
                            .overlay(RoundedRectangle(cornerRadius: 12).stroke(c.border, lineWidth: 1))
                        }

                        // ── Tên AI ──
                        VStack(alignment: .leading, spacing: 8) {
                            Text("TÊN GỌI AI").font(.system(size: 10.5, weight: .bold)).foregroundColor(c.textTertiary).kerning(1)
                            HStack(spacing: 10) {
                                Image(systemName: "sparkles").font(.system(size: 14)).foregroundColor(c.gold2).frame(width: 20)
                                TextField("Tên bạn muốn gọi AI...", text: $editAIName)
                                    .font(.system(size: 14)).foregroundColor(c.textPrimary)
                                    .onSubmit { memory.aiNickname = editAIName }
                            }
                            .padding(.horizontal, 14).padding(.vertical, 12)
                            .background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 12))
                            .overlay(RoundedRectangle(cornerRadius: 12).stroke(c.border, lineWidth: 1))
                            Text("Hiện tại: \"\(memory.displayName)\"").font(.system(size: 11)).foregroundColor(c.textTertiary).padding(.leading, 4)
                        }

                        // ── Thói quen ──
                        if !memory.habits.isEmpty {
                            VStack(alignment: .leading, spacing: 10) {
                                HStack {
                                    Text("THÓI QUEN ĐÃ GHI NHỚ").font(.system(size: 10.5, weight: .bold)).foregroundColor(c.textTertiary).kerning(1)
                                    Spacer()
                                    Text("\(memory.habits.count)/20").font(.system(size: 10)).foregroundColor(c.textQuaternary)
                                }
                                VStack(spacing: 6) {
                                    ForEach(memory.habits.reversed(), id: \.self) { habit in
                                        HStack(spacing: 8) {
                                            Circle().fill(c.teal).frame(width: 6, height: 6)
                                            Text(habit).font(.system(size: 13)).foregroundColor(c.textSecondary)
                                            Spacer()
                                            Button { memory.habits.removeAll { $0 == habit } } label: {
                                                Image(systemName: "xmark").font(.system(size: 10)).foregroundColor(c.textQuaternary)
                                            }
                                        }
                                        .padding(.horizontal, 12).padding(.vertical, 8)
                                        .background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 8))
                                    }
                                }
                            }
                        }

                        // ── Bối cảnh hôm nay ──
                        if !memory.todayContext.isEmpty {
                            VStack(alignment: .leading, spacing: 8) {
                                Text("HÔM NAY").font(.system(size: 10.5, weight: .bold)).foregroundColor(c.textTertiary).kerning(1)
                                Text(memory.todayContext).font(.system(size: 12)).foregroundColor(c.textSecondary)
                                    .padding(12).background(c.bg2).clipShape(RoundedRectangle(cornerRadius: 10))
                                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(c.border, lineWidth: 1))
                            }
                        }

                        // ── Lưu ──
                        Button(action: {
                            if !editName.isEmpty { memory.userName = editName }
                            if !editAIName.isEmpty { memory.aiNickname = editAIName }
                            dismiss()
                        }) {
                            Text("Lưu thay đổi")
                                .font(.system(size: 15, weight: .semibold))
                                .foregroundColor(c.isDark ? Color(hex: 0x1A1500) : .white)
                                .frame(maxWidth: .infinity).padding(.vertical, 14)
                                .background(c.gold).clipShape(RoundedRectangle(cornerRadius: 12))
                        }

                        // ── Reset ──
                        Button(action: { showResetConfirm = true }) {
                            Text("Xoá toàn bộ bộ nhớ")
                                .font(.system(size: 13)).foregroundColor(c.red2)
                                .frame(maxWidth: .infinity).padding(.vertical, 10)
                                .background(c.red.opacity(0.08)).clipShape(RoundedRectangle(cornerRadius: 10))
                                .overlay(RoundedRectangle(cornerRadius: 10).stroke(c.red.opacity(0.2), lineWidth: 1))
                        }

                        Spacer(minLength: 30)
                    }
                    .padding(20)
                }
            }
            .navigationTitle("Bộ Nhớ AI")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Đóng") { dismiss() }.foregroundColor(c.textSecondary)
                }
            }
            .onAppear {
                editName = memory.userName
                editAIName = memory.aiNickname
            }
            .confirmationDialog("Xoá toàn bộ bộ nhớ?", isPresented: $showResetConfirm, titleVisibility: .visible) {
                Button("Xoá tất cả", role: .destructive) { memory.reset(); dismiss() }
                Button("Huỷ", role: .cancel) {}
            } message: {
                Text("Tên, thói quen và bối cảnh sẽ bị xoá. AI sẽ hỏi lại tên bạn.")
            }
        }
    }
}
