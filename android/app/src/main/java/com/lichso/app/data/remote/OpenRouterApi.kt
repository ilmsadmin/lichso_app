package com.lichso.app.data.remote

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class ChatMessage(val role: String, val content: String)

data class OpenRouterRequest(
    val model: String = "google/gemini-2.0-flash-001",
    val messages: List<ChatMessage>,
    val max_tokens: Int = 1024,
    val temperature: Double = 0.7
)

data class OpenRouterResponse(
    val choices: List<Choice>?
) {
    data class Choice(val message: MessageContent?)
    data class MessageContent(val content: String?)
}

@Singleton
class OpenRouterApi @Inject constructor() {

    companion object {
        private const val BASE_URL = "https://openrouter.ai/api/v1/chat/completions"
        private const val API_KEY = "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"
    }

    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val systemPrompt = """
Bạn là "Lịch Số AI" — trợ lý phong thuỷ & lịch vạn niên Việt Nam thông minh.

Quy tắc:
- Luôn trả lời bằng tiếng Việt, tự nhiên, thân thiện
- Sử dụng emoji phù hợp để minh hoạ
- Nếu người dùng cung cấp thông tin ngày (can chi, âm lịch, giờ hoàng đạo), hãy tham khảo để trả lời chính xác
- Chuyên sâu: phong thuỷ, ngày tốt xấu, can chi, giờ hoàng đạo, hướng xuất hành, tiết khí, nghi lễ Việt Nam
- Trả lời ngắn gọn, dễ hiểu, có cấu trúc rõ ràng
- Nếu không chắc chắn, nói rõ và gợi ý tham khảo thêm
- Nếu người dùng cho biết tên, ngày sinh, sở thích, thói quen — hãy ghi nhận và dùng tên họ khi trả lời
- Nếu người dùng đặt tên cho bạn, hãy chấp nhận và tự giới thiệu bằng tên mới
- Hãy tỏ ra thân thiện, gọi người dùng bằng tên nếu biết
""".trimIndent()

    suspend fun chat(
        userMessage: String,
        contextInfo: String,
        memoryContext: String = "",
        history: List<ChatMessage> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val messages = mutableListOf<ChatMessage>()
            messages.add(ChatMessage("system", systemPrompt))

            if (memoryContext.isNotBlank()) {
                messages.add(ChatMessage("system", memoryContext))
            }

            if (contextInfo.isNotBlank()) {
                messages.add(ChatMessage("system", "Thông tin lịch hôm nay:\n$contextInfo"))
            }

            messages.addAll(history.takeLast(10))
            messages.add(ChatMessage("user", userMessage))

            val requestBody = OpenRouterRequest(messages = messages)
            val json = gson.toJson(requestBody)

            val request = Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer $API_KEY")
                .addHeader("Content-Type", "application/json")
                .addHeader("HTTP-Referer", "https://lichso.app")
                .addHeader("X-Title", "Lich So - Lich Van Nien")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            if (response.isSuccessful && body != null) {
                val parsed = gson.fromJson(body, OpenRouterResponse::class.java)
                val content = parsed.choices?.firstOrNull()?.message?.content
                if (content != null) {
                    Result.success(content.trim())
                } else {
                    Result.failure(Exception("Empty response from AI"))
                }
            } else {
                Result.failure(Exception("API error ${response.code}: ${body?.take(200)}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
