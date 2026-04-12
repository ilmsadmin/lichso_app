package com.lichso.app.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PrimaryRed  = Color(0xFFB71C1C)
private val DeepRed     = Color(0xFF8B0000)
private val GoldAccent  = Color(0xFFD4A017)
private val SurfaceBg   = Color(0xFFFFFBF5)
private val TextMain    = Color(0xFF1C1B1F)
private val TextSub     = Color(0xFF534340)
private val TextDim     = Color(0xFF857371)
private val Outline     = Color(0xFFD8C2BF)

/**
 * SmartRatingDialog — Dialog xin đánh giá thông minh, 3 nhánh:
 *
 *   [emotion] → hỏi cảm xúc chung
 *       → "Rất hài lòng" → [stars] chọn 1–5 sao
 *           → 4–5 sao → ghi nhận đã rated + mở Play Store thật
 *           → 1–3 sao → [feedback] form gửi mail
 *       → "Chưa hài lòng" → [feedback] form gửi mail
 *   [feedback] → nhập góp ý → gửi email tới zenixhq.com@gmail.com
 *   [thanks]   → cảm ơn, tự đóng sau 2.5s
 *
 * Sử dụng:
 *   SmartRatingDialog(visible = ..., onDismiss = { ... })
 */
@Composable
fun SmartRatingDialog(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ── Step state ──
    // "emotion"  → hỏi hài lòng hay không
    // "stars"    → chọn số sao (1-5) khi user hài lòng
    // "feedback" → form nhập feedback (khi không hài lòng hoặc 1-3 sao)
    // "thanks"   → cảm ơn sau khi gửi feedback / đánh giá cao
    var step by remember { mutableStateOf("emotion") }
    var feedbackText by remember { mutableStateOf("") }
    var selectedStars by remember { mutableStateOf(0) }

    // ── Notify SmartRatingManager that we're showing ──
    LaunchedEffect(Unit) {
        SmartRatingManager.recordShown(context)
    }

    Dialog(
        onDismissRequest = {
            SmartRatingManager.dismiss()
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
            label = "dialog_step"
        ) { currentStep ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                when (currentStep) {
                    "emotion" -> EmotionStep(
                        onHappy = {
                            // Hài lòng → chuyển sang chọn số sao
                            step = "stars"
                        },
                        onUnhappy = {
                            // Chưa hài lòng → form feedback
                            step = "feedback"
                        },
                        onDismiss = {
                            SmartRatingManager.dismiss()
                            onDismiss()
                        }
                    )

                    "stars" -> StarsStep(
                        selectedStars = selectedStars,
                        onStarSelect = { selectedStars = it },
                        onConfirm = { stars ->
                            if (stars >= 4) {
                                // 4-5 sao → In-App Review, fallback Play Store nếu lỗi
                                coroutineScope.launch {
                                    SmartRatingManager.recordRated(context)
                                    val activity = context as? android.app.Activity
                                    if (activity != null) {
                                        launchInAppReviewOrFallback(activity)
                                    } else {
                                        openPlayStore(context)
                                    }
                                    onDismiss()
                                }
                            } else {
                                // 1-3 sao → chuyển sang feedback
                                step = "feedback"
                            }
                        },
                        onDismiss = {
                            SmartRatingManager.dismiss()
                            onDismiss()
                        }
                    )

                    "feedback" -> FeedbackStep(
                        feedbackText = feedbackText,
                        onFeedbackChange = { feedbackText = it },
                        onSend = {
                            sendFeedbackEmail(context, feedbackText, selectedStars)
                            step = "thanks"
                        },
                        onSkip = {
                            SmartRatingManager.dismiss()
                            onDismiss()
                        }
                    )

                    "thanks" -> ThanksStep(
                        onDismiss = {
                            SmartRatingManager.dismiss()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// STEP 1 — Hỏi cảm xúc
// ══════════════════════════════════════════
@Composable
private fun EmotionStep(
    onHappy: () -> Unit,
    onUnhappy: () -> Unit,
    onDismiss: () -> Unit
) {
    // Animate star icons
    val starScales = List(5) { i ->
        val infiniteTransition = rememberInfiniteTransition(label = "star_$i")
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = i * 100),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale_$i"
        ).value
    }

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
            // ── Dismiss X ──
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Đóng",
                        tint = TextDim, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Animated stars ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                starScales.forEach { scale ->
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier
                            .size(32.dp)
                            .scale(scale)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Bạn có hài lòng\nvới Lịch Số không?",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Chỉ mất 10 giây — đánh giá của bạn giúp chúng tôi cải thiện ứng dụng tốt hơn mỗi ngày 🙏",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = TextSub,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── 2 buttons ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Không hài lòng
                OutlinedButton(
                    onClick = onUnhappy,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Outline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSub)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("😕", fontSize = 20.sp)
                        Text("Chưa hài lòng",
                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSub))
                    }
                }

                // Hài lòng
                Button(
                    onClick = onHappy,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(listOf(GoldAccent, Color(0xFFF5CC3A))),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("😍", fontSize = 20.sp)
                            Text("Rất hài lòng!",
                                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D3A00)))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
// STEP 2 — Chọn số sao (1–5)
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
        Color(0xFFE53935), // 1 sao – đỏ
        Color(0xFFFF7043), // 2 sao – cam đỏ
        Color(0xFFFFB300), // 3 sao – vàng tối
        Color(0xFF7CB342), // 4 sao – xanh lá
        Color(0xFF43A047)  // 5 sao – xanh lá đậm
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
            // ── Dismiss X ──
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Đóng",
                        tint = TextDim, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text("⭐", fontSize = 48.sp)

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
                if (selectedStars >= 4)
                    "Cảm ơn bạn! Đánh giá của bạn trên Play Store sẽ giúp nhiều người khám phá Lịch Số 🙏"
                else if (selectedStars in 1..3)
                    "Chúng tôi muốn lắng nghe để cải thiện tốt hơn cho bạn 💬"
                else
                    "Chạm vào ngôi sao để chọn mức đánh giá của bạn",
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
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedStars >= 4) Color(0xFF43A047) else PrimaryRed,
                    disabledContainerColor = Outline
                )
            ) {
                Text(
                    text = when {
                        selectedStars == 0 -> "Chọn số sao để tiếp tục"
                        selectedStars >= 4 -> "⭐ Đánh giá ngay"
                        else -> "💬 Gửi phản hồi cho chúng tôi"
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
// STEP 3 — Form phản hồi
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
            // ── Icon ──
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
                "Phản hồi của bạn sẽ được gửi thẳng đến đội phát triển và được xử lý trong vòng 24 giờ",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = TextSub,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Text field ──
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

            // ── Email hint ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Email, contentDescription = null,
                    tint = TextDim, modifier = Modifier.size(14.dp))
                Text(
                    "Phản hồi gửi tới: zenixhq.com@gmail.com",
                    style = TextStyle(fontSize = 11.sp, color = TextDim)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Buttons ──
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
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null,
                        modifier = Modifier.size(15.dp))
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
    val coroutineScope = rememberCoroutineScope()

    // Auto-dismiss sau 2.5 giây
    LaunchedEffect(Unit) {
        delay(2500)
        onDismiss()
    }

    val scale by rememberInfiniteTransition(label = "heart").animateFloat(
        initialValue = 0.9f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
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
            Text("🙏", fontSize = 52.sp, modifier = Modifier.scale(scale))

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

private fun openPlayStore(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=com.lichso.app"))
    context.startActivity(intent)
}

/**
 * Thử launch In-App Review API của Google Play.
 * Nếu API thất bại (task.isSuccessful == false) → fallback mở Play Store trực tiếp.
 * Lưu ý: khi API thành công, Google có thể KHÔNG hiển thị dialog (quota/đã review rồi) —
 * đây là behaviour của Play, không phải lỗi code.
 */
private fun launchInAppReviewOrFallback(activity: android.app.Activity) {
    val reviewManager = com.google.android.play.core.review.ReviewManagerFactory.create(activity)
    reviewManager.requestReviewFlow().addOnCompleteListener { requestTask ->
        if (requestTask.isSuccessful) {
            reviewManager.launchReviewFlow(activity, requestTask.result)
                .addOnCompleteListener {
                    // flow hoàn thành (Google không cho biết user có submit hay không)
                    // Không cần làm gì thêm — dialog đã dismiss trước đó
                }
        } else {
            // Không lấy được ReviewInfo → mở Play Store
            openPlayStore(activity)
        }
    }
}

private fun sendFeedbackEmail(context: Context, feedback: String, stars: Int = 0) {
    val starText = if (stars > 0) "${"⭐".repeat(stars)} ($stars/5 sao)" else "Không chọn"
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
            appendLine("App: Lịch Số ${pInfo.versionName} (${pInfo.longVersionCode})")
        } catch (_: Exception) {}
    }

    // Build mailto URI dạng: mailto:to?subject=...&body=...
    // Phải encode subject và body thủ công, KHÔNG dùng Uri.Builder.appendQueryParameter
    // vì nó double-encode dấu + thành %2B khiến Gmail hiểu sai.
    val to = "zenixhq.com@gmail.com"
    val encodedSubject = Uri.encode(subject)
    val encodedBody = Uri.encode(body)
    val mailtoUri = Uri.parse("mailto:$to?subject=$encodedSubject&body=$encodedBody")

    val intent = Intent(Intent.ACTION_SENDTO, mailtoUri)

    try {
        context.startActivity(intent)
    } catch (_: android.content.ActivityNotFoundException) {
        android.widget.Toast.makeText(
            context,
            "Không tìm thấy ứng dụng email. Vui lòng liên hệ: $to",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}
