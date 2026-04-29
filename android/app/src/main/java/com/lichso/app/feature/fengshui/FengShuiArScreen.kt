package com.lichso.app.feature.fengshui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.lichso.app.feature.points.domain.PermanentRank
import com.lichso.app.feature.points.domain.PermanentUnlockKey
import com.lichso.app.feature.points.ui.PointsViewModel
import com.lichso.app.ui.theme.LichSoThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FengShuiArScreen(
    onBackClick: () -> Unit = {},
    viewModel: FengShuiArViewModel = hiltViewModel(),
    pointsVm: PointsViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val state by viewModel.state.collectAsState()
    val balance by pointsVm.balance.collectAsState()
    val permUnlocks by pointsVm.permanentUnlocks.collectAsState()

    val unlocked = PermanentUnlockKey.AR_FENG_SHUI_WEEKLY.name in permUnlocks ||
        balance.rank.ordinal >= PermanentRank.DAO_SI.ordinal

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) viewModel.setImageUri(uri) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI Phong Thuỷ AR", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = c.surface,
                    titleContentColor = c.textPrimary,
                ),
            )
        },
        containerColor = c.bg,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // ── Hero ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF26A69A), Color(0xFF00695C)),
                        ),
                    )
                    .padding(16.dp),
            ) {
                Column {
                    Text(
                        "🏠 AI Phong Thuỷ Nhà ở",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Chụp ảnh phòng → AI phân tích bố cục, hướng, màu sắc, gợi ý vật phẩm phong thuỷ.",
                        style = TextStyle(fontSize = 13.sp, color = Color(0xFFB2DFDB)),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (!unlocked) {
                LockedBanner(currentRank = balance.rank)
                Spacer(Modifier.height(16.dp))
            }

            // ── Image area ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.surfaceContainer)
                    .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
                    .clickable(enabled = unlocked) {
                        pickImage.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (state.imageUri != null) {
                    AsyncImage(
                        model = state.imageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Image,
                            contentDescription = null,
                            tint = c.textTertiary,
                            modifier = Modifier.size(40.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (unlocked) "Bấm để chọn ảnh" else "Cần đạt Đạo sĩ để mở",
                            style = TextStyle(fontSize = 13.sp, color = c.textSecondary),
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.userPrompt,
                onValueChange = viewModel::setPrompt,
                placeholder = { Text("Mô tả thêm: phòng ngủ tuổi Tân Dậu, cần xem hướng giường…") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                enabled = unlocked,
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { viewModel.analyze() },
                enabled = unlocked && state.imageUri != null && !state.isAnalyzing,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (state.isAnalyzing) "AI đang phân tích..." else "Phân tích phong thuỷ",
                    fontWeight = FontWeight.Bold,
                )
            }

            // ── Result ──
            state.errorMessage?.let { err ->
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFEBEE))
                        .padding(12.dp),
                ) {
                    Text(err, style = TextStyle(fontSize = 13.sp, color = Color(0xFFB71C1C)))
                }
            }

            state.analysis?.let { text ->
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(c.surface)
                        .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Column {
                        Text(
                            "🪷 Kết quả phân tích",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text,
                            style = TextStyle(fontSize = 13.sp, color = c.textPrimary, lineHeight = 19.sp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LockedBanner(currentRank: PermanentRank) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF3E0))
            .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                tint = Color(0xFFE65100),
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    "Tính năng cần đạt cấp Đạo sĩ (15,000 ☯️)",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100)),
                )
                Text(
                    "Cấp hiện tại: ${currentRank.displayName}. Tích luỹ thêm để mở khoá.",
                    style = TextStyle(fontSize = 11.sp, color = c.textSecondary),
                )
            }
        }
    }
}
