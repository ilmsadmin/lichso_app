package com.lichso.app.feature.ocr

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
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Image
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
import com.lichso.app.ui.theme.LichSoThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrCalendarScreen(
    onBackClick: () -> Unit = {},
    onPickDate: (day: Int, month: Int, year: Int?, isLunar: Boolean) -> Unit = { _, _, _, _ -> },
    viewModel: OcrCalendarViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val state by viewModel.state.collectAsState()

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) viewModel.setImageUri(uri) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quét lịch giấy", fontWeight = FontWeight.Bold) },
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
            // Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF455A64), Color(0xFF263238))),
                    )
                    .padding(16.dp),
            ) {
                Column {
                    Text(
                        "📜 OCR lịch giấy / banner",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Chụp/chọn ảnh lịch giấy → AI nhận diện chữ → bắt nhanh các ngày âm/dương để tra cứu.",
                        style = TextStyle(fontSize = 13.sp, color = Color(0xFFB0BEC5)),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Image area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.surfaceContainer)
                    .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
                    .clickable {
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
                        contentScale = ContentScale.Fit,
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
                            "Bấm để chọn ảnh lịch / banner",
                            style = TextStyle(fontSize = 13.sp, color = c.textSecondary),
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { viewModel.runOcr() },
                enabled = state.imageUri != null && !state.isProcessing,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Filled.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (state.isProcessing) "Đang quét..." else "Quét chữ trong ảnh",
                    fontWeight = FontWeight.Bold,
                )
            }

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

            // Parsed dates
            if (state.parsedDates.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Ngày phát hiện (${state.parsedDates.size})",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                )
                Spacer(Modifier.height(8.dp))
                state.parsedDates.forEach { p ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(c.surface)
                            .border(1.dp, c.outlineVariant, RoundedCornerShape(10.dp))
                            .clickable { onPickDate(p.day, p.month, p.year, p.isLunar) }
                            .padding(12.dp),
                    ) {
                        Column {
                            Text(
                                p.description,
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary),
                            )
                            Text(
                                if (p.isLunar) "Âm lịch · bấm để xem chi tiết" else "Dương lịch · bấm để xem chi tiết",
                                style = TextStyle(fontSize = 12.sp, color = c.textSecondary),
                            )
                        }
                    }
                }
            }

            // Raw text fallback
            if (state.rawText.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Văn bản gốc",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.textSecondary),
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(c.surfaceContainer)
                        .padding(12.dp),
                ) {
                    Text(
                        state.rawText,
                        style = TextStyle(fontSize = 12.sp, color = c.textSecondary),
                    )
                }
            }
        }
    }
}
