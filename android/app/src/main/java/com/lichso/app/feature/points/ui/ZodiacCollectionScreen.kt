package com.lichso.app.feature.points.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lichso.app.feature.points.domain.DailyUnlockKey
import com.lichso.app.feature.points.domain.ZodiacCard
import com.lichso.app.feature.points.domain.ZodiacDeck
import com.lichso.app.feature.points.domain.ZodiacRarity
import com.lichso.app.ui.theme.LichSoThemeColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZodiacCollectionScreen(
    onBackClick: () -> Unit,
    vm: ZodiacCollectionViewModel = hiltViewModel(),
    pointsVm: PointsViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val collected by vm.collected.collectAsState()
    val totalDraws by vm.totalDraws.collectAsState()
    val lastDrawn by vm.lastDrawn.collectAsState()
    val balance by pointsVm.balance.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var revealCard by remember { mutableStateOf<ZodiacCard?>(null) }

    LaunchedEffect(Unit) {
        vm.events.collect { ev ->
            when (ev) {
                is ZodiacEvent.Drawn -> {
                    revealCard = ev.card
                }
                ZodiacEvent.AlreadyDrawnToday -> scope.launch {
                    snackbar.showSnackbar("Hôm nay bạn đã rút thẻ rồi — quay lại ngày mai!")
                }
                is ZodiacEvent.NotEnoughPoints -> scope.launch {
                    snackbar.showSnackbar("Còn thiếu ${ev.needed} ⚡ — kiếm thêm điểm rồi quay lại!")
                }
                ZodiacEvent.FullSetCompleted -> scope.launch {
                    snackbar.showSnackbar("🎉 Hoàn thành bộ 12 con giáp! Phần thưởng +50 ☯️ đã ghi nhận.")
                }
            }
        }
    }

    val signsHeld = remember(collected) {
        collected.mapNotNull { ZodiacDeck.byId(it)?.name }.toSet()
    }
    val totalSigns = ZodiacDeck.zodiacSigns.size
    val cost = DailyUnlockKey.DAILY_ZODIAC_CARD.cost
    val canAfford = balance.daily >= cost

    Scaffold(
        containerColor = c.bg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bộ sưu tập 12 con giáp",
                    style = TextStyle(fontWeight = FontWeight.Bold), color = c.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = c.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = c.bg),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            // Header card: progress + balance
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF8B0000), Color(0xFFD4A017))
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        "Đã sưu tầm: ${signsHeld.size}/$totalSigns con giáp",
                        color = Color.White,
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Tổng số lần rút: $totalDraws · Số dư: ${balance.daily} ⚡",
                        color = Color(0xFFFFEEDB),
                        style = TextStyle(fontSize = 13.sp),
                    )
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { signsHeld.size.toFloat() / totalSigns.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Draw button
            Button(
                onClick = { vm.tryDraw() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = c.primary,
                    disabledContainerColor = c.outline.copy(alpha = 0.3f),
                ),
            ) {
                Text(
                    if (canAfford) "Rút 1 thẻ — tiêu $cost ⚡"
                    else "Cần thêm ${cost - balance.daily} ⚡ để rút thẻ",
                    color = Color.White, fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(16.dp))

            Text("Bộ sưu tập", color = c.textPrimary,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(ZodiacDeck.all) { card ->
                    val owned = card.id in collected
                    ZodiacGridItem(card = card, owned = owned)
                }
            }
        }
    }

    // Reveal modal
    revealCard?.let { card ->
        AlertDialog(
            onDismissRequest = { revealCard = null; vm.run {} },
            confirmButton = {
                TextButton(onClick = { revealCard = null }) { Text("Đóng") }
            },
            title = { Text("Bạn rút được:") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(card.emoji, style = TextStyle(fontSize = 64.sp))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${card.displayName} (${card.name} · ${card.han})",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Cấp: ${card.rarity.display}",
                        color = Color(card.rarity.colorHex),
                        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(card.description, textAlign = TextAlign.Center)
                }
            },
        )
    }
}

@Composable
private fun ZodiacGridItem(card: ZodiacCard, owned: Boolean) {
    val c = LichSoThemeColors.current
    val rarityColor = Color(card.rarity.colorHex)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface)
            .border(
                width = if (owned) 2.dp else 1.dp,
                color = if (owned) rarityColor else c.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
            )
            .alpha(if (owned) 1f else 0.35f)
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (owned) card.emoji else "❔",
                style = TextStyle(fontSize = 32.sp),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                card.name,
                color = c.textPrimary,
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
            )
            Text(
                if (owned) card.displayName else "Chưa mở",
                color = c.textSecondary,
                style = TextStyle(fontSize = 10.sp),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            if (owned) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(rarityColor.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                ) {
                    Text(
                        card.rarity.display,
                        color = rarityColor,
                        style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    )
                }
            }
        }
    }
}
