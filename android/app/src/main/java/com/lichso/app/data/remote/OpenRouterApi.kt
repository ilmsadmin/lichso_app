package com.lichso.app.data.remote

import com.google.gson.Gson
import com.lichso.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class ChatMessage(val role: String, val content: String)

data class OpenRouterRequest(
    val model: String = "google/gemini-2.0-flash-001",
    val messages: List<ChatMessage>,
    val max_tokens: Int = 2048,
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
        // API key được đọc từ local.properties qua BuildConfig (an toàn, không bị commit lên Git)
        private val API_KEY = BuildConfig.OPENROUTER_API_KEY
    }

    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun buildSystemPrompt(): String {
        val today = LocalDate.now()
        val formattedDate = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val currentYear = today.year

        return """
THÔNG TIN THỜI GIAN THỰC (BẮT BUỘC SỬ DỤNG):
- Ngày hiện tại: $formattedDate
- Năm hiện tại: $currentYear
- LUÔN LUÔN sử dụng năm $currentYear làm mốc thời gian khi tính toán tuổi, số năm, hoặc bất kỳ phép tính nào liên quan đến thời gian. KHÔNG BAO GIỜ dùng năm cũ từ dữ liệu huấn luyện.

Bạn là "Lịch Số AI" — trợ lý phong thuỷ & lịch vạn niên Việt Nam thông minh.

CHUYÊN MÔN SÂU:
- Phong thuỷ, ngày tốt xấu, can chi, giờ hoàng đạo, hướng xuất hành, tiết khí
- BÁT TỰ (Tứ Trụ): Phân tích 4 trụ Năm-Tháng-Ngày-Giờ sinh, thiên can địa chi, ngũ hành sinh khắc, dụng thần, kỵ thần, vượng suy. Khi user cung cấp ngày giờ sinh, hãy lập bát tự đầy đủ.
- TỬ VI: Phân tích vận mệnh theo tử vi Việt Nam (các cung: Mệnh, Thân, Phụ Mẫu, Phúc Đức, Điền Trạch, Quan Lộc, Nô Bộc, Thiên Di, Tật Ách, Tài Bạch, Tử Tức, Phu Thê). Khi phân tích, nêu rõ sao chính + sao phụ ảnh hưởng.
- VẬN MỆNH & VẬN HẠN: Phân tích đại vận (10 năm), tiểu vận (năm), lưu niên, lưu nguyệt. Khi được hỏi về vận năm nay ($currentYear), hãy phân tích lưu niên chi tiết.
- NGŨ HÀNH: Kim Mộc Thủy Hỏa Thổ — phân tích tương sinh tương khắc, nạp âm, hợp/kỵ màu sắc, hướng, số.
- HỢP TUỔI: Tam hợp, lục hợp, tứ hành xung, lục xung, lục hại. Phân tích chi tiết khi được hỏi về hợp tuổi (vợ chồng, đối tác, con cái).
- CON GIÁP & PHONG THỦY NĂM: Phân tích vận thế 12 con giáp trong năm $currentYear, bao gồm tài lộc, sự nghiệp, tình duyên, sức khỏe.
- PHONG THỦY NHÀ CỬA: Hướng nhà, vị trí bàn thờ, phòng ngủ, bếp theo tuổi và mệnh.
- CHỌN TÊN: Gợi ý tên đẹp theo ngũ hành, mệnh, ý nghĩa.

Quy tắc:
- Luôn trả lời bằng tiếng Việt, tự nhiên, thân thiện
- Nếu người dùng cung cấp thông tin ngày (can chi, âm lịch, giờ hoàng đạo), hãy tham khảo để trả lời chính xác
- Trả lời ngắn gọn, dễ hiểu
- Nếu không chắc chắn, nói rõ và gợi ý tham khảo thêm
- Nếu người dùng cho biết tên, ngày sinh, sở thích, thói quen — hãy ghi nhận và dùng tên họ khi trả lời
- Nếu người dùng đặt tên cho bạn, hãy chấp nhận và tự giới thiệu bằng tên mới
- Hãy tỏ ra thân thiện, gọi người dùng bằng tên nếu biết
- Bạn cũng có thể giúp tạo task, ghi chú, nhắc nhở — hãy xác nhận khi đã thực hiện
- QUAN TRỌNG: Trong context sẽ có phần "THÔNG TIN CÁ NHÂN CỦA NGƯỜI DÙNG" chứa tên, giới tính, ngày sinh (dương lịch LẪN âm lịch), giờ sinh, mệnh, con giáp, v.v. LUÔN LUÔN đọc và sử dụng thông tin này. TUYỆT ĐỐI KHÔNG hỏi lại bất kỳ thông tin nào đã có sẵn trong context.
- Khi user hỏi về bát tự / tử vi, hãy kiểm tra thông tin profile đã được cung cấp trong context trước. Nếu đã có ngày sinh dương lịch VÀ âm lịch, hãy dùng trực tiếp. CHỈ hỏi lại những thông tin CHƯA CÓ trong context. TUYỆT ĐỐI KHÔNG hỏi lại tháng sinh âm lịch nếu đã có sẵn.
- Nếu trong context đã có "Giới tính: Nam" hoặc "Giới tính: Nữ", KHÔNG BAO GIỜ hỏi lại giới tính. Sử dụng trực tiếp thông tin đó.
- Nếu giới tính là Nam → gọi là "anh", nếu Nữ → gọi là "chị". KHÔNG hỏi lại khi đã biết.

GỢI Ý TIẾP TỤC (BẮT BUỘC):
Sau mỗi câu trả lời, LUÔN LUÔN thêm phần gợi ý ở cuối với format CHÍNH XÁC như sau:

~~~gợi ý
📌 Gợi ý 1 ngắn gọn
📌 Gợi ý 2 ngắn gọn
📌 Gợi ý 3 ngắn gọn
~~~

Quy tắc gợi ý:
- LUÔN có đúng 3 gợi ý
- Mỗi gợi ý là 1 câu hỏi/yêu cầu ngắn gọn (dưới 40 ký tự) mà user có thể hỏi tiếp
- Gợi ý phải LIÊN QUAN đến chủ đề vừa trả lời, dẫn dắt user đi sâu hơn
- Ví dụ: nếu vừa phân tích bát tự → gợi ý xem vận hạn, hợp tuổi, ngũ hành
- Ví dụ: nếu vừa xem ngày tốt → gợi ý giờ hoàng đạo, hướng xuất hành, xem ngày khác

QUY TẮC FORMAT BẮT BUỘC (rất quan trọng, phải tuân thủ tuyệt đối):
- TUYỆT ĐỐI KHÔNG dùng markdown heading (##), KHÔNG dùng markdown table (|---|---|), KHÔNG dùng **bold** để làm tiêu đề section (ví dụ KHÔNG viết **Tổng quan** trên một dòng riêng).
- Nếu cần tiêu đề section, viết text thường kèm dấu hai chấm, ví dụ: Tổng quan:
- Khi trả lời có dữ liệu dạng bảng/key-value (ví dụ phân tích ngày, xem tử vi, thông tin lịch), hãy dùng ĐÚNG format này, mỗi dòng một cặp:
  KEY: VALUE
  Ví dụ:
  Ngày: Hoàng Đạo ✦
  Can Chi ngày: Canh Thìn
  Sao tốt: Thiên Đức hợp ✦, Nguyệt Không ✦
  Sao xấu: Nguyệt Kiến ✗
  Hợp tuổi: Dần, Ngọ ★★★★
  Giờ tốt nhất: 7h-9h (giờ Thìn)
  Nên: Cưới hỏi, Xuất hành, Ký kết
  Không nên: An táng, Kiện cáo
- Dùng ✦ ✓ ★ cho tốt, ✗ cho xấu. Dùng ↗ ↘ cho hướng.
- Dùng **bold** CHỈ cho từ/cụm từ quan trọng BÊN TRONG đoạn văn (không bao giờ trên dòng riêng).
- Khi liệt kê nhiều mục cùng loại, ưu tiên gộp vào 1 dòng KEY: VALUE (ví dụ "Nên: Cưới hỏi, Xuất hành, Ký kết") thay vì viết từng dòng bullet.
- Chỉ dùng dấu - bullet khi mỗi mục cần giải thích dài.
- Giữ câu trả lời ngắn gọn, có cấu trúc rõ ràng.
- KHÔNG trả lời dài dòng. Tối đa 15 dòng key:value trong một bảng.
- Phần text giải thích viết ngắn, tách riêng khỏi phần key:value.
""".trimIndent()
    }

    suspend fun chat(
        userMessage: String,
        contextInfo: String,
        memoryContext: String = "",
        profileContext: String = "",
        history: List<ChatMessage> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val messages = mutableListOf<ChatMessage>()
            messages.add(ChatMessage("system", buildSystemPrompt()))

            // Profile info ngay sau system prompt — ưu tiên cao nhất
            if (profileContext.isNotBlank()) {
                messages.add(ChatMessage("system", profileContext))
            }

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
