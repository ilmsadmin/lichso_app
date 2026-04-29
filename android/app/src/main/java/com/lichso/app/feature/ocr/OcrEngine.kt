package com.lichso.app.feature.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object OcrEngine {
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun recognizeText(context: Context, uri: Uri): String {
        val image = InputImage.fromFilePath(context, uri)
        return suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { result -> cont.resume(result.text) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
    }
}

/**
 * Phase 4 — Parser tiếng Việt cho text OCR từ lịch giấy / banner.
 * Bắt các pattern phổ biến: "12/05", "12/05/2025", "ngày 12 tháng 5", "rằm tháng 7",
 * "mùng 1 tháng 5", "mồng 3 tết".
 */
object VietnameseDateParser {

    data class ParsedDate(
        val raw: String,
        val day: Int,
        val month: Int,
        val year: Int?,
        val isLunar: Boolean,
        val description: String,
    )

    private val numericRegex = Regex("""(\b\d{1,2})/(\d{1,2})(?:/(\d{2,4}))?\b""")
    private val ngayThangRegex = Regex(
        """ng[àa]y\s+(\d{1,2})\s+th[áa]ng\s+(\d{1,2})(?:\s+n[ăa]m\s+(\d{2,4}))?""",
        RegexOption.IGNORE_CASE
    )
    private val mungRegex = Regex(
        """(?:mùng|mồng|mung|mong)\s+(\d{1,2})(?:\s+th[áa]ng\s+(\d{1,2}))?""",
        RegexOption.IGNORE_CASE
    )
    private val ramRegex = Regex(
        """rằm\s+th[áa]ng\s+(\d{1,2})""",
        RegexOption.IGNORE_CASE
    )

    fun parse(text: String): List<ParsedDate> {
        val results = mutableListOf<ParsedDate>()
        val seen = mutableSetOf<String>()

        fun add(p: ParsedDate) {
            val key = "${p.day}/${p.month}/${p.year}/${p.isLunar}"
            if (seen.add(key)) results.add(p)
        }

        ramRegex.findAll(text).forEach { m ->
            val month = m.groupValues[1].toIntOrNull() ?: return@forEach
            if (month in 1..12) add(
                ParsedDate(m.value, 15, month, null, true, "Rằm tháng $month (âm)")
            )
        }
        mungRegex.findAll(text).forEach { m ->
            val day = m.groupValues[1].toIntOrNull() ?: return@forEach
            val month = m.groupValues.getOrNull(2)?.toIntOrNull()
            if (day in 1..30) {
                val mm = month?.takeIf { it in 1..12 }
                add(
                    ParsedDate(
                        m.value, day, mm ?: 1, null, true,
                        "Mùng $day" + (mm?.let { " tháng $it" } ?: "") + " (âm)"
                    )
                )
            }
        }
        ngayThangRegex.findAll(text).forEach { m ->
            val day = m.groupValues[1].toIntOrNull() ?: return@forEach
            val month = m.groupValues[2].toIntOrNull() ?: return@forEach
            val yearRaw = m.groupValues.getOrNull(3)?.toIntOrNull()
            val year = yearRaw?.let { if (it < 100) 2000 + it else it }
            if (day in 1..31 && month in 1..12) {
                add(ParsedDate(m.value, day, month, year, false, "Ngày $day tháng $month" + (year?.let { " năm $it" } ?: "")))
            }
        }
        numericRegex.findAll(text).forEach { m ->
            val day = m.groupValues[1].toIntOrNull() ?: return@forEach
            val month = m.groupValues[2].toIntOrNull() ?: return@forEach
            val yearRaw = m.groupValues.getOrNull(3)?.toIntOrNull()
            val year = yearRaw?.let { if (it < 100) 2000 + it else it }
            if (day in 1..31 && month in 1..12) {
                add(ParsedDate(m.value, day, month, year, false, m.value))
            }
        }
        return results
    }
}
