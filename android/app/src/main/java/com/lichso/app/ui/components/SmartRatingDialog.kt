package com.lichso.app.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lichso.app.util.SmartRatingManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PrimaryRed = Color(0xFFB71C1C)
private val GoldAccent = Color(0xFFD4A017)
private val SurfaceBg  = Color(0xFFFFFBF5)
private val TextMain   = Color(0xFF1C1B1F)
private val TextSub    = Color(0xFF534340)
private val TextDim    = Color(0xFF857371)
private val Outline    = Color(0xFFD8C2BF)

private const val FEEDBACK_EMAIL = "zenixhq.com@gmail.com"

/**
 * SmartRatingDialog — Dialog xin đánh giá ứng dụng (luồng mới).
 *
 *   [stars]    → user chọn 1-5 sao
 *       → 4-5 sao → Google Play In-App Review (fallback Play Store listing)
 *       → 1-3 sao → [feedback] form gửi email
 *   [feedback] → gửi mail tới zenixhq.com@gmail.com
 *   [thanks]   → cảm ơn, tự đóng sau 2.5s
 *
 * Không còn EmotionStep "👍/👎" — đi thẳng vào sao luôn cho gọn.
 */
@Composable
fun SmartRatingDialog(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val context = LocalContext.current

    var step by remember { mutableStateOf("stars") }
    var feedbackText by remember { mutableStateOf("") }
    var selectedStars by remember { mutableIntStateOf(0) }

    val dialogScope = rememberCoroutineScope()

    // Helpers ghi outcome lên DataStore — dùng scope của composition để tránh leak.
    val recordSkippedSafe: () -> Unit = {
        dialogScope.launch { SmartRatingManager.recordSkipped(context.applicationContext) }
    }
    val recordFeedbackSafe: () -> Unit = {
        dialogScope.launch { SmartRatingManager.recordFeedbackSent(context.applicationContext) }
    }
    val recordReviewIntentSafe: () -> Unit = {
        dialogScope.launch { SmartRatingManager.recordReviewIntent(context.applicationContext) }
    }

    // Ghi nhận đã hiển thị (chỉ tăng quota nếu auto-trigger).
    LaunchedEffect(Unit) {
        SmartRatingManager.recordShown(context)
    }

    Dialog(
        onDismissRequest = {
            recordSkippedSafe()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                (slideInHorizontally { it } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it } + fadeOut())
            },
            label = "rating_step"
        ) { currentStep ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                when (currentStep) {
                    "stars" -> StarsStep(
                        selectedStars = selectedStars,
                        onStarSelect = { selectedStars = it },
                        onConfirm = { stars ->
                            if (stars >= 4) {
                                // 4-5 sao → in-app review API. Cần Activity.
                                val activity = context.findActivity()
                                if (activity != null) {
                                    SmartRatingManager.launchInAppReview(activity)
                                } else {
                                    SmartRatingManager.openPlayStoreListing(context)
                                }
                                recordReviewIntentSafe()
                                onDismiss()
                            } else {
                                // 1-3 sao → form feedback
                                step = "feedback"
                            }
                        },
                        onDismiss = {
                            recordSkippedSafe()
                            onDismiss()
                        }
                    )

                    "feedback" -> FeedbackStep(
                        feedbackText = feedbackText,
                        onFeedbackChange = { feedbackText = it },
                        onSend = {
                            sendFeedbackEmail(context, feedbackText, selectedStars)
                            recordFeedbackSafe()
                            step = "thanks"
                        },
                        onSkip = {
                            recordSkippedSafe()
                            onDismiss()
                        }
                    )

                    "thanks" -> ThanksStep(onDismiss = onDismiss)
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// STEP 1 — Chọn sao (1-5)
// ══════════════════════════════════════════
@Composable
private fun StarsStep(
    selectedStars: Int,
    onStarSelect: (Int) -> Unit,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val starLabels = listOf("Rất tệ", "Không tốt", "Tạm được", "Khá tốt", "Xuất sắc")
    val starColors = listOf(
        Color(0xFFE53935),
        Color(0xFFFF7043),
        Color(0xFFFFB300),
        Color(0xFF7CB342),
        Color(0xFF43A047)
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dismiss X
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Đóng",
                        tint = TextDim,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Bạn đánh giá Lịch Số\nbao nhiêu sao?",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                when {
                    selectedStars >= 4 -> "Cảm ơn bạn! Đánh giá của bạn trên Play Store sẽ giúp nhiều người khác khám phá Lịch Số."
                    selectedStars in 1..3 -> "Chúng tôi muốn lắng nghe phản hồi để cải thiện tốt hơn."
                    else -> "Chạm vào ngôi sao để chọn mức đánh giá của bạn."
                },
                style = TextStyle(
                    fontSize = 13.sp,
                    color = TextSub,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Star Row ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..5).forEach { star ->
                    val isFilled = star <= selectedStars
                    val starColor = if (isFilled && selectedStars > 0)
                        starColors[selectedStars - 1]
                    else
                        Color(0xFFD0C4C0)

                    val scale by animateFloatAsState(
                        targetValue = if (isFilled) 1.25f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "star_scale_$star"
                    )

                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "$star sao",
                        tint = starColor,
                        modifier = Modifier
                            .size(44.dp)
                            .scale(scale)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onStarSelect(star) }
                    )
                }
            }

            // ── Nhãn sao ──
            AnimatedVisibility(visible = selectedStars > 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        starLabels.getOrElse(selectedStars - 1) { "" },
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedStars > 0) starColors[selectedStars - 1] else TextDim
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Nút xác nhận ──
            Button(
                onClick = { if (selectedStars > 0) onConfirm(selectedStars) },
                enabled = selectedStars > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedStars >= 4) Color(0xFF43A047) else PrimaryRed,
                    disabledContainerColor = Outline
                )
            ) {
                Text(
                    text = when {
                        selectedStars == 0 -> "Chọn số sao để tiếp tục"
                        selectedStars >= 4 -> "Đánh giá ngay"
                        else -> "Gửi phản hồi cho chúng tôi"
                    },
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Bỏ qua",
                style = TextStyle(fontSize = 12.sp, color = TextDim),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDismiss() }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

// ══════════════════════════════════════════
// STEP 2 — Form phản hồi (1-3 sao)
// ══════════════════════════════════════════
@Composable
private fun FeedbackStep(
    feedbackText: String,
    onFeedbackChange: (String) -> Unit,
    onSend: () -> Unit,
    onSkip: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Feedback,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Hãy cho chúng tôi\nbiết vấn đề của bạn",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "Phản hồi của bạn sẽ được gửi thẳng đến đội phát triển và được xử lý trong vòng 24 giờ.",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = TextSub,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = feedbackText,
                onValueChange = onFeedbackChange,
                placeholder = {
                    Text(
                        "Bạn gặp khó khăn gì? Tính năng nào chưa tốt? Bạn mong muốn điều gì?",
                        style = TextStyle(fontSize = 13.sp, color = TextDim, lineHeight = 20.sp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(14.dp),
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryRed,
                    unfocusedBorderColor = Outline
                ),
                textStyle = TextStyle(fontSize = 14.sp, color = TextMain, lineHeight = 20.sp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Email,
                    contentDescription = null,
                    tint = TextDim,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "Phản hồi gửi tới: $FEEDBACK_EMAIL",
                    style = TextStyle(fontSize = 11.sp, color = TextDim)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Outline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSub)
                ) {
                    Text("Bỏ qua", fontSize = 14.sp)
                }

                Button(
                    onClick = onSend,
                    enabled = feedbackText.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRed,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        "Gửi phản hồi",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// STEP 3 — Cảm ơn
// ══════════════════════════════════════════
@Composable
private fun ThanksStep(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2500)
        onDismiss()
    }

    val transition = rememberInfiniteTransition(label = "heart_pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_scale"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier
                    .size(52.dp)
                    .scale(scale)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Cảm ơn bạn rất nhiều!",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Phản hồi của bạn đã được gửi. Chúng tôi sẽ nỗ lực cải thiện để mang lại trải nghiệm tốt nhất.",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = TextSub,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = onDismiss) {
                Text("Đóng", color = TextDim, fontSize = 13.sp)
            }
        }
    }
}

// ══════════════════════════════════════════
// Helpers
// ══════════════════════════════════════════

/** Tìm Activity từ Context (cần cho ReviewManager API). */
private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private fun sendFeedbackEmail(context: Context, feedback: String, stars: Int) {
    val starText = if (stars > 0) "$stars/5 sao" else "Không chọn"
    val subject = "[Lịch Số] Phản hồi – $starText"

    val body = buildString {
        appendLine(feedback)
        appendLine()
        appendLine("---")
        appendLine("Đánh giá: $starText")
        appendLine("Thiết bị: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
        appendLine("Android: ${android.os.Build.VERSION.RELEASE}")
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = androidx.core.content.pm.PackageInfoCompat.getLongVersionCode(pInfo)
            appendLine("App: Lịch Số ${pInfo.versionName} ($versionCode)")
        } catch (_: Exception) { /* ignore */ }
    }

    // mailto URI: encode subject + body thủ công, KHÔNG dùng appendQueryParameter
    // (sẽ double-encode dấu + thành %2B → Gmail hiểu sai).
    val encodedSubject = Uri.encode(subject)
    val encodedBody = Uri.encode(body)
    val mailtoUri = Uri.parse("mailto:$FEEDBACK_EMAIL?subject=$encodedSubject&body=$encodedBody")

    val intent = Intent(Intent.ACTION_SENDTO, mailtoUri)
    try {
        context.startActivity(intent)
    } catch (_: android.content.ActivityNotFoundException) {
        android.widget.Toast.makeText(
            context,
            "Không tìm thấy ứng dụng email. Vui lòng liên hệ: $FEEDBACK_EMAIL",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}
