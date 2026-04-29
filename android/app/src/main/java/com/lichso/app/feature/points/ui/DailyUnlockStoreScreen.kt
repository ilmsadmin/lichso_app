package com.lichso.app.feature.points.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lichso.app.R
import com.lichso.app.feature.points.domain.DailyUnlockKey
import com.lichso.app.feature.points.domain.SpendResult
import com.lichso.app.ui.theme.LichSoThemeColors
import kotlinx.coroutines.launch

/**
 * DailyUnlockStoreScreen — aggregate shopping view of today's unlocks.
 * Wired into navigation route "daily_store".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyUnlockStoreScreen(
    onBackClick: () -> Unit,
    vm: PointsViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val balance by vm.balance.collectAsState()
    val unlocked by vm.todayUnlocks.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = c.bg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            if (c.isDark) listOf(Color(0xFF5D1212), Color(0xFF4A1010))
                            else listOf(c.primary, c.deepRed)
                        )
                    )
                    .statusBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White,
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Kho mở khoá ngày",
                            style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp),
                        )
                        Text(
                            "Tiêu ⚡ điểm ngày để mở tính năng hôm nay",
                            style = TextStyle(color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp),
                        )
                    }
                    PointsPillContent(balance = balance, onClick = {}, modifier = Modifier.padding(end = 12.dp))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(vertical = 12.dp),
            modifier = Modifier.padding(padding).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                BucketHeader(
                    title = "HÔM NAY — ${balance.daily}⚡ khả dụng",
                    right = "${unlocked.size}/${DailyUnlockKey.entries.size} đã mở",
                )
            }
            items(DailyUnlockKey.entries.toList()) { key ->
                val isUnlocked = key.name in unlocked
                val canAfford = balance.daily >= key.cost
                UnlockStoreCard(
                    key = key,
                    isUnlocked = isUnlocked,
                    canAfford = canAfford,
                    onUnlock = {
                        vm.spendDaily(key) { r ->
                            val msg = when (r) {
                                SpendResult.Success -> "Đã mở ${key.label}"
                                SpendResult.AlreadyUnlocked -> "${key.label} đã mở sẵn"
                                is SpendResult.InsufficientPoints ->
                                    "Thiếu ${r.needed}⚡ để mở ${key.label}"
                            }
                            scope.launch { snackbar.showSnackbar(msg) }
                        }
                    }
                )
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun BucketHeader(title: String, right: String) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = c.textPrimary),
        )
        Text(right, style = TextStyle(fontSize = 11.sp, color = c.textTertiary))
    }
}

@Composable
private fun UnlockStoreCard(
    key: DailyUnlockKey,
    isUnlocked: Boolean,
    canAfford: Boolean,
    onUnlock: () -> Unit,
) {
    val c = LichSoThemeColors.current
    val borderColor = when {
        isUnlocked -> c.goodGreen
        canAfford -> c.outlineVariant
        else -> c.outlineVariant.copy(alpha = 0.5f)
    }
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surface)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        when {
                            isUnlocked -> listOf(c.goodGreen, c.teal)
                            canAfford -> listOf(c.primary, c.deepRed)
                            else -> listOf(c.outline, c.textQuaternary)
                        }
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(
                    if (isUnlocked) R.drawable.ic_lock_open else R.drawable.ic_lock_closed
                ),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                key.label,
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                key.description,
                style = TextStyle(fontSize = 12.sp, color = c.textTertiary, lineHeight = 16.sp),
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(c.gold.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bolt),
                        contentDescription = null,
                        tint = c.gold,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${key.cost}",
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = c.gold),
                    )
                }
                Spacer(Modifier.weight(1f))
                val btnBg = when {
                    isUnlocked -> c.goodGreen
                    canAfford -> c.primary
                    else -> c.outline.copy(alpha = 0.5f)
                }
                Button(
                    onClick = onUnlock,
                    enabled = !isUnlocked && canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = btnBg,
                        disabledContainerColor = btnBg,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(
                        if (isUnlocked) "Đã mở" else if (canAfford) "Mở khoá" else "Chưa đủ",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
