package com.lichso.app.data.remote

import com.google.gson.Gson
import com.lichso.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Vision-capable wrapper of OpenRouter — gửi ảnh + prompt để AI phân tích phong thuỷ.
 *
 * Dùng OpenAI-compatible vision content format:
 * ```
 * {"role":"user","content":[
 *   {"type":"text","text":"..."},
 *   {"type":"image_url","image_url":{"url":"data:image/jpeg;base64,..."}}
 * ]}
 * ```
 */
@Singleton
class FengShuiVisionApi @Inject constructor(
    private val client: OkHttpClient,
) {
    private val gson = Gson()

    /** Model multimodal mặc định trên OpenRouter. */
    private val visionModel = "google/gemini-flash-1.5"

    suspend fun analyzeRoomPhoto(
        imageBase64: String,
        userContext: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val proxyBaseUrl = BuildConfig.AI_PROXY_BASE_URL.trim()
            val proxyAppId = BuildConfig.AI_PROXY_APP_ID.trim()
            val proxyAppSecret = BuildConfig.AI_PROXY_APP_SECRET.trim()

            require(proxyBaseUrl.isNotBlank() && proxyAppId.isNotBlank() && proxyAppSecret.isNotBlank()) {
                "AI proxy chưa được cấu hình."
            }

            val url = if (proxyBaseUrl.endsWith("/")) "${proxyBaseUrl}v1/chat/completions"
            else "$proxyBaseUrl/v1/chat/completions"

            val systemPrompt = buildSystemPrompt()

            val userContent = listOf(
                mapOf(
                    "type" to "text",
                    "text" to (userContext.ifBlank { "Hãy phân tích phong thuỷ căn phòng/không gian trong ảnh này." }),
                ),
                mapOf(
                    "type" to "image_url",
                    "image_url" to mapOf("url" to "data:image/jpeg;base64,$imageBase64"),
                ),
            )

            val requestMap = mapOf(
                "model" to visionModel,
                "max_tokens" to 1500,
                "temperature" to 0.5,
                "messages" to listOf(
                    mapOf("role" to "system", "content" to systemPrompt),
                    mapOf("role" to "user", "content" to userContent),
                ),
            )

            val json = gson.toJson(requestMap)

            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-App-Id", proxyAppId)
                .addHeader("X-App-Secret", proxyAppSecret)
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { resp ->
                val body = resp.body?.string()
                if (!resp.isSuccessful || body == null) {
                    error("API ${resp.code}: ${body?.take(180)}")
                }
                val parsed = gson.fromJson(body, OpenRouterResponse::class.java)
                parsed.choices?.firstOrNull()?.message?.content?.trim()
                    ?: error("Phản hồi rỗng từ AI")
            }
        }
    }

    private fun buildSystemPrompt(): String = """
Bạn là chuyên gia Phong Thuỷ Việt Nam phân tích ảnh không gian sống.

CÁCH TRẢ LỜI (BẮT BUỘC theo cấu trúc):
Tổng quan:
[2-3 câu mô tả không gian]

Điểm tốt:
- [điểm tốt 1]
- [điểm tốt 2]

Điểm cần cải thiện:
- [vấn đề 1, kèm gợi ý sửa]
- [vấn đề 2, kèm gợi ý sửa]

Gợi ý vật phẩm phong thuỷ:
- [vật phẩm 1 — vị trí đặt]
- [vật phẩm 2 — vị trí đặt]

QUY TẮC:
- Tiếng Việt, ngắn gọn, chuyên môn nhưng dễ hiểu
- Phân tích dựa trên: ánh sáng, hướng, bố cục đồ vật, ngũ hành màu sắc, sạch/bừa, gương, cây xanh, dòng chảy năng lượng
- Nếu ảnh không phải không gian sống/làm việc, hãy nói "Ảnh không phù hợp" + gợi ý chụp lại
- KHÔNG đưa lời khuyên y khoa hoặc tài chính cụ thể
- Tối đa 15 dòng
""".trimIndent()
}
