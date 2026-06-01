package com.lichso.app.feature.tour

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Tour guide (coachmark / spotlight) hướng dẫn người mới.
 *
 * Cách dùng:
 *  1. Tạo [TourController] với danh sách [TourStep] và callback khi xong.
 *  2. Bọc cây UI bằng `CompositionLocalProvider(LocalTourController provides controller)`.
 *  3. Gắn [Modifier.tourTarget] cho từng phần tử cần highlight (key trùng với step).
 *  4. Đặt [TourOverlay] ở lớp trên cùng của màn hình.
 *  5. Gọi `controller.start()` lần đầu mở app.
 */
data class TourStep(
    val key: String,
    val title: String,
    val text: String,
)

class TourController(
    val steps: List<TourStep>,
    private val onFinished: () -> Unit,
) {
    /** Vị trí (toạ độ gốc root) của các phần tử đã đăng ký. */
    val targets = mutableStateMapOf<String, Rect>()

    var currentIndex by mutableStateOf(-1)
        private set

    private var finishedOnce = false

    val isActive: Boolean get() = currentIndex in steps.indices
    val currentStep: TourStep? get() = steps.getOrNull(currentIndex)
    val isLastStep: Boolean get() = currentIndex == steps.lastIndex

    fun start() {
        if (steps.isNotEmpty() && !finishedOnce) currentIndex = 0
    }

    fun next() {
        if (currentIndex < steps.lastIndex) currentIndex++ else finish()
    }

    fun finish() {
        currentIndex = -1
        if (!finishedOnce) {
            finishedOnce = true
            onFinished()
        }
    }
}

val LocalTourController = staticCompositionLocalOf<TourController?> { null }

/**
 * Đăng ký phần tử này làm mục tiêu highlight cho tour (nếu có controller).
 * Không ảnh hưởng layout — chỉ đọc toạ độ.
 */
fun Modifier.tourTarget(key: String): Modifier = composed {
    val controller = LocalTourController.current
    if (controller == null) this
    else this.onGloballyPositioned { coords ->
        if (coords.isAttached) controller.targets[key] = coords.boundsInRoot()
    }
}

/** Lớp phủ tối + lỗ khoét quanh mục tiêu + thẻ hướng dẫn. Đặt trên cùng. */
@Composable
fun TourOverlay(controller: TourController) {
    if (!controller.isActive) return
    val step = controller.currentStep ?: return
    val target = controller.targets[step.key]

    BackHandler(enabled = true) { controller.finish() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenHpx = with(density) { maxHeight.toPx() }
        val padPx = with(density) { 8.dp.toPx() }
        val cornerPx = with(density) { 16.dp.toPx() }
        val marginPx = with(density) { 18.dp.toPx() }

        // ── Scrim tối + khoét lỗ tròn quanh mục tiêu ──
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(step.key) {
                    // Chạm bất kỳ đâu cũng sang bước kế tiếp cho tiện.
                    detectTapGestures { controller.next() }
                }
        ) {
            val scrim = Color.Black.copy(alpha = 0.76f)
            if (target == null || target.width <= 0f || target.height <= 0f) {
                drawRect(scrim)
                return@Canvas
            }
            val l = (target.left - padPx).coerceAtLeast(0f)
            val t = (target.top - padPx).coerceAtLeast(0f)
            val r = (target.right + padPx).coerceAtMost(size.width)
            val b = (target.bottom + padPx).coerceAtMost(size.height)
            val hole = Path().apply {
                addRect(Rect(0f, 0f, size.width, size.height))
                addRoundRect(RoundRect(l, t, r, b, CornerRadius(cornerPx, cornerPx)))
                fillType = PathFillType.EvenOdd
            }
            drawPath(hole, scrim)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.92f),
                topLeft = Offset(l, t),
                size = Size(r - l, b - t),
                cornerRadius = CornerRadius(cornerPx, cornerPx),
                style = Stroke(width = with(density) { 2.dp.toPx() })
            )
        }

        // ── Thẻ hướng dẫn (đặt trên/dưới mục tiêu tuỳ vị trí) ──
        var cardHeightPx by remember(step.key) { mutableStateOf(0) }
        val placeBelow = target == null || target.center.y < screenHpx * 0.45f
        val cardYpx = when {
            target == null -> screenHpx * 0.5f
            placeBelow -> target.bottom + marginPx
            else -> (target.top - marginPx - cardHeightPx)
                .coerceAtLeast(with(density) { 48.dp.toPx() })
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset { IntOffset(0, cardYpx.roundToInt()) }
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .onGloballyPositioned { cardHeightPx = it.size.height },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TourTooltipCard(
                step = step,
                index = controller.currentIndex,
                total = controller.steps.size,
                isLast = controller.isLastStep,
                onSkip = { controller.finish() },
                onNext = { controller.next() },
            )
        }
    }
}

@Composable
private fun TourTooltipCard(
    step: TourStep,
    index: Int,
    total: Int,
    isLast: Boolean,
    onSkip: () -> Unit,
    onNext: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 12.dp,
        modifier = Modifier.widthIn(max = 360.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = step.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = step.text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 21.sp,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onSkip) {
                    Text("Bỏ qua", fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${index + 1}/$total",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = onNext,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = if (isLast) "Xong" else "Tiếp theo",
                            color = Color.White,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}
