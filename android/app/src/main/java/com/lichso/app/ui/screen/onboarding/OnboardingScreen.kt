package com.lichso.app.ui.screen.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import com.lichso.app.ui.icons.PrayerIcons
import com.lichso.app.ui.screen.profile.ProfileKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// ══════════════════════════════════════════
// Colors
// ══════════════════════════════════════════
private val PrimaryRed = Color(0xFFB71C1C)
private val DeepRed = Color(0xFF8B0000)
private val GoldAccent = Color(0xFFD4A017)
private val GoldLight = Color(0xFFE8C84A)
private val SurfaceBg = Color(0xFFFFFBF5)
private val TextMain = Color(0xFF1C1B1F)
private val TextSub = Color(0xFF534340)
private val TextDim = Color(0xFF857371)
private val Outline = Color(0xFFD8C2BF)

// ══════════════════════════════════════════
// Onboarding page data
// ══════════════════════════════════════════
private data class OnboardingPage(
    val icon: ImageVector,
    val iconBg: List<Color>,
    val accentColor: Color,
    val title: String,
    val subtitle: String,
    val description: String,
    val featureChips: List<Pair<ImageVector, String>>
)

// Một trang welcome duy nhất gộp các tính năng chính (trước đây là 5 trang lướt).
private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Filled.CalendarMonth,
        iconBg = listOf(PrimaryRed, Color(0xFFD32F2F)),
        accentColor = PrimaryRed,
        title = "Chào mừng đến Lịch Số",
        subtitle = "Lịch vạn niên · Phong thủy · Gia phả · AI",
        description = "Tất cả trong một ứng dụng: lịch âm dương, ngày tốt xấu, văn khấn, cây gia phả và trợ lý AI tử vi.",
        featureChips = listOf(
            Icons.Filled.CalendarMonth to "Lịch vạn niên",
            Icons.Filled.EventAvailable to "Ngày tốt xấu",
            PrayerIcons.Filled to "Văn khấn",
            Icons.Filled.AccountTree to "Gia phả"
        )
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    // Onboarding giờ chỉ còn 1 trang welcome — phần hướng dẫn tính năng
    // được thay bằng tour guide ngay trên màn Home.
    val page = onboardingPages.first()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
    ) {
        OnboardingPageContent(page = page, pageOffset = 0f)

        // ── Nút bắt đầu ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(listOf(PrimaryRed, DeepRed)))
                    .clickable { onFinish() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Bắt đầu sử dụng",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

// ══════════════════════════════════════════
// SINGLE PAGE CONTENT
// ══════════════════════════════════════════
@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    pageOffset: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 32.dp)
            .padding(top = 60.dp, bottom = 160.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // ── Big Icon with animated background ──
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .graphicsLayer {
                    // Parallax: icon moves slightly
                    translationX = pageOffset * -80f
                    alpha = 1f - (pageOffset * 0.5f).coerceAtMost(1f)
                    scaleX = 1f - (pageOffset * 0.15f)
                    scaleY = 1f - (pageOffset * 0.15f)
                }
        ) {
            // Decorative outer ring
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .alpha(0.12f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(page.accentColor, Color.Transparent)
                        ),
                        CircleShape
                    )
            )

            // Icon circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(page.iconBg)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    page.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // ── Title ──
        Text(
            text = page.title,
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.graphicsLayer {
                translationX = pageOffset * -40f
                alpha = 1f - (pageOffset * 0.6f).coerceAtMost(1f)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Subtitle ──
        Text(
            text = page.subtitle,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = page.accentColor,
                textAlign = TextAlign.Center,
                letterSpacing = 0.3.sp
            ),
            modifier = Modifier.graphicsLayer {
                translationX = pageOffset * -30f
                alpha = 1f - (pageOffset * 0.7f).coerceAtMost(1f)
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // ── Gold decorative line ──
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, GoldAccent, Color.Transparent)
                    ),
                    RoundedCornerShape(1.dp)
                )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Description ──
        Text(
            text = page.description,
            style = TextStyle(
                fontSize = 15.sp,
                color = TextSub,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            ),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .graphicsLayer {
                    translationX = pageOffset * -20f
                    alpha = 1f - (pageOffset * 0.8f).coerceAtMost(1f)
                }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ── Feature Chips ──
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.graphicsLayer {
                translationY = pageOffset * 40f
                alpha = 1f - (pageOffset * 0.9f).coerceAtMost(1f)
            }
        ) {
            // Row 1
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                page.featureChips.take(2).forEach { (icon, label) ->
                    FeatureChip(icon = icon, label = label, accentColor = page.accentColor)
                }
            }
            // Row 2
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                page.featureChips.drop(2).forEach { (icon, label) ->
                    FeatureChip(icon = icon, label = label, accentColor = page.accentColor)
                }
            }
        }
    }
}

@Composable
private fun FeatureChip(
    icon: ImageVector,
    label: String,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(
                accentColor.copy(alpha = 0.08f),
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                accentColor.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = accentColor
            )
        )
    }
}
