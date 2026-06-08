package com.lichso.app.util

/**
 * Loại bỏ các thẻ HTML để chuyển đổi chuỗi rich text sang dạng text thuần,
 * phù hợp cho hiển thị preview hoặc tìm kiếm.
 */
fun String.stripHtml(): String {
    if (this.isBlank()) return ""
    return this.replace(Regex("<[^>]*>"), "")
        .replace("&nbsp;", " ")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace(Regex("\\s+"), " ") // Thu gọn khoảng trắng thừa
        .trim()
}
