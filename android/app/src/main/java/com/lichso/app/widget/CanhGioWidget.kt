package com.lichso.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.lichso.app.MainActivity
import com.lichso.app.R
import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.util.CanChiCalculator
import java.time.LocalDate
import java.time.LocalTime

/**
 * Phase 4 — Widget 12 canh giờ.
 * 4×1 widget hiển thị canh giờ hiện tại (Tý/Sửu/Dần…) cùng emoji con giáp,
 * khoảng giờ và ý nghĩa hoàng/hắc đạo. Tự cập nhật theo periodic worker (30 phút).
 */
class CanhGioWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            try {
                updateWidget(context, mgr, id)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating $id", e)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in DATE_ACTIONS) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, CanhGioWidget::class.java))
            onUpdate(context, mgr, ids)
        }
    }

    companion object {
        private const val TAG = "CanhGioWidget"
        private val DATE_ACTIONS = setOf(
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED
        )

        // Index ↔ địa chi (12 canh giờ).  Tý = 23-01, Sửu = 01-03, ...
        private val CHI_NAMES = CanChiCalculator.DIA_CHI
        private val CHI_EMOJIS = listOf(
            "🐭", "🐮", "🐯", "🐰", "🐲", "🐍",
            "🐴", "🐐", "🐵", "🐔", "🐶", "🐷"
        )
        private val CHI_MEANINGS = listOf(
            "Tý — chuột linh hoạt, nên nghỉ ngơi",
            "Sửu — trâu chăm chỉ, nên ngủ sâu",
            "Dần — hổ mạnh mẽ, khởi sự tốt",
            "Mão — mèo khéo léo, nên dậy sớm",
            "Thìn — rồng quyền uy, nên hành sự",
            "Tỵ — rắn khôn ngoan, nên tập trung",
            "Ngọ — ngựa năng động, đỉnh năng lượng",
            "Mùi — dê hiền hòa, nên nghỉ trưa",
            "Thân — khỉ nhanh nhạy, nên kết nối",
            "Dậu — gà cần mẫn, nên kết thúc việc",
            "Tuất — chó trung thành, nên trở về",
            "Hợi — heo viên mãn, nên thư giãn"
        )

        // Hoàng/hắc đạo theo địa chi ngày (rút gọn) — đỏ là hắc, xanh là hoàng.
        // Để đơn giản: lấy theo ngày Tý/Sửu… từ DayInfoProvider rồi hiển thị nếu trùng giờ hoàng đạo.

        /** Hour-of-day (0..23) → chi index (0..11). Tý chia làm 23h..0h..1h. */
        fun chiIndexForHour(hour: Int): Int {
            // Tý = 23-1; mapping: (hour + 1) / 2 mod 12
            return ((hour + 1) / 2) % 12
        }

        fun rangeForChiIndex(idx: Int): String {
            // Tý = 23-1; index 0 → 23-01, index 1 → 01-03, ...
            val startHour = (idx * 2 + 23) % 24
            val endHour = (startHour + 2) % 24
            return "%02d:00 — %02d:00".format(startHour, endHour)
        }

        fun updateWidget(context: Context, mgr: AppWidgetManager, id: Int) {
            val now = LocalTime.now()
            val today = LocalDate.now()
            val idx = chiIndexForHour(now.hour)
            val name = CHI_NAMES[idx]
            val emoji = CHI_EMOJIS[idx]
            val meaning = CHI_MEANINGS[idx]
            val range = rangeForChiIndex(idx)

            val views = RemoteViews(context.packageName, R.layout.widget_canhgio)
            views.setTextViewText(R.id.tv_canhgio_emoji, emoji)
            views.setTextViewText(R.id.tv_canhgio_label, "Canh giờ $name")
            views.setTextViewText(R.id.tv_canhgio_range, range)
            views.setTextViewText(R.id.tv_canhgio_meaning, meaning)

            // Hoàng đạo? — kiểm tra trong dayInfo.gioHoangDao (text chứa địa chi này)
            try {
                val dayInfo = DayInfoProvider().getDayInfo(today.dayOfMonth, today.monthValue, today.year)
                val isHoangDao = dayInfo.gioHoangDao.any { it.name.contains(name) }
                if (isHoangDao) {
                    views.setTextViewText(R.id.tv_canhgio_rating, "✦")
                    views.setTextColor(R.id.tv_canhgio_rating, 0xFF2E7D32.toInt())
                } else {
                    views.setTextViewText(R.id.tv_canhgio_rating, "✶")
                    views.setTextColor(R.id.tv_canhgio_rating, 0xFFC62828.toInt())
                }
            } catch (_: Exception) {
                views.setTextViewText(R.id.tv_canhgio_rating, "✦")
            }

            // Click → mở app
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pi = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pi)

            mgr.updateAppWidget(id, views)
        }
    }
}
