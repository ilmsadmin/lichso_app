package com.lichso.app.feature.fengshui

import kotlin.math.abs

enum class LoBanMode(val title: String, val cycleCm: Double, val subtitle: String) {
    DO_GIA_DUNG("Thước 52.2 cm", 52.2, "Cửa chính, cổng, cửa phòng"),
    BAN_THO("Thước 42.9 cm", 42.9, "Bàn thờ, đồ nội thất"),
    GIUONG_TU("Thước 38.8 cm", 38.8, "Giường, tủ, bàn nhỏ")
}

data class LoBanResult(
    val mode: LoBanMode,
    val rawLengthCm: Double,
    val normalizedCm: Double,
    val zoneIndex: Int,
    val zoneName: String,
    val isAuspicious: Boolean,
    val hint: String
)

data class CungPhiResult(
    val cungName: String,
    val genderLabel: String,
    val groupLabel: String,
    val favorableDirections: List<String>,
    val unfavorableDirections: List<String>,
    val note: String
)

fun compassDirectionForDegrees(degrees: Float): String {
    val normalized = ((degrees % 360f) + 360f) % 360f
    return when (normalized) {
        in 337.5f..359.9999f, in 0f..22.5f -> "Bắc"
        in 22.5f..67.5f -> "Đông Bắc"
        in 67.5f..112.5f -> "Đông"
        in 112.5f..157.5f -> "Đông Nam"
        in 157.5f..202.5f -> "Nam"
        in 202.5f..247.5f -> "Tây Nam"
        in 247.5f..292.5f -> "Tây"
        else -> "Tây Bắc"
    }
}

fun evaluateLoBan(lengthCm: Double, mode: LoBanMode): LoBanResult {
    val sanitizedLength = lengthCm.coerceAtLeast(0.0)
    val cycle = mode.cycleCm
    val normalized = if (cycle <= 0.0) 0.0 else ((sanitizedLength % cycle) + cycle) % cycle
    val zoneSize = cycle / 8.0
    val zoneIndex = if (zoneSize > 0.0) minOf((normalized / zoneSize).toInt(), 7) else 0
    val zoneNames = listOf(
        "Tài Lộc",
        "Bệnh Tật",
        "Ly Tán",
        "Nghĩa Đức",
        "Quan Lộc",
        "Kiếp Sát",
        "Họa Hại",
        "Bản Mệnh"
    )
    val zoneName = zoneNames.getOrElse(zoneIndex) { zoneNames.first() }
    val isAuspicious = zoneIndex in setOf(0, 3, 4, 7)
    val hint = when (zoneIndex) {
        0 -> "Thiên về tài khí, dễ dùng cho kích thước cát lợi."
        1 -> "Nghiêng về bệnh tật, nên cân nhắc chỉnh lại."
        2 -> "Dễ sinh ly tán, phù hợp kiểm tra lại số đo."
        3 -> "Nghiêng về nghĩa khí, thường được xem là khá ổn."
        4 -> "Thiên về quan lộc, hợp với kích thước cần sự bền vững."
        5 -> "Kiếp sát, nên hạn chế nếu đang chọn số đo chính."
        6 -> "Họa hại, nên xem lại nếu đang thiết kế chính xác."
        else -> "Bản mệnh, thường là mốc cân bằng trong chu kỳ thước."
    }

    return LoBanResult(
        mode = mode,
        rawLengthCm = sanitizedLength,
        normalizedCm = normalized,
        zoneIndex = zoneIndex,
        zoneName = zoneName,
        isAuspicious = isAuspicious,
        hint = hint
    )
}

fun calculateCungPhi(lunarYear: Int, gender: String): CungPhiResult {
    val cungNames = listOf("Khảm", "Ly", "Cấn", "Đoài", "Càn", "Khôn", "Tốn", "Chấn", "Trung Cung")
    val sum = digitSum(lunarYear)
    val isMale = gender != "Nữ"

    val cungIndex = if (isMale) {
        val value = (11 - sum % 9) % 9
        if (value == 0) 8 else value - 1
    } else {
        val value = (sum + 4) % 9
        if (value == 0) 8 else value - 1
    }

    val cungName = cungNames.getOrElse(cungIndex) { "Khảm" }
    val genderLabel = if (isMale) "Nam" else "Nữ"
    val isEastGroup = cungName in setOf("Khảm", "Ly", "Chấn", "Tốn")
    val groupLabel = if (cungName == "Trung Cung") {
        "Chưa xác định"
    } else if (isEastGroup) {
        "Đông tứ mệnh"
    } else {
        "Tây tứ mệnh"
    }
    val favorableDirections = if (isEastGroup) {
        listOf("Bắc", "Đông", "Nam", "Đông Nam")
    } else {
        listOf("Tây", "Tây Bắc", "Tây Nam", "Đông Bắc")
    }
    val unfavorableDirections = if (isEastGroup) {
        listOf("Tây", "Tây Bắc", "Tây Nam", "Đông Bắc")
    } else {
        listOf("Bắc", "Đông", "Nam", "Đông Nam")
    }

    val note = if (cungName == "Trung Cung") {
        "Số đo này rơi vào trường hợp đặc biệt, nên xem lại thông tin sinh để phân tích kỹ hơn."
    } else {
        "Màn này dùng quy tắc Bát trạch cơ bản: ưu tiên một trong các hướng thuộc nhóm của bạn."
    }

    return CungPhiResult(
        cungName = cungName,
        genderLabel = genderLabel,
        groupLabel = groupLabel,
        favorableDirections = favorableDirections,
        unfavorableDirections = unfavorableDirections,
        note = note
    )
}

private fun digitSum(value: Int): Int {
    var sum = 0
    var number = abs(value)
    while (number > 0) {
        sum += number % 10
        number /= 10
    }
    while (sum >= 10) {
        var reduced = 0
        var current = sum
        while (current > 0) {
            reduced += current % 10
            current /= 10
        }
        sum = reduced
    }
    return sum
}
