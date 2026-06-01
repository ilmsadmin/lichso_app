package com.lichso.app.feature.survey

import com.lichso.app.ui.theme.screenBackground
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.theme.LichSoThemeColors

@Composable
fun SurveyScreen(
    onBackClick: () -> Unit = {},
    viewModel: SurveyViewModel = hiltViewModel()
) {
    val c = LichSoThemeColors.current
    val uiState by viewModel.uiState.collectAsState()
    val submitting by viewModel.submitting.collectAsState()
    val submitSuccess by viewModel.submitSuccess.collectAsState()
    val submitError by viewModel.submitError.collectAsState()

    var showStatsAlert by remember { mutableStateOf(false) }

    LaunchedEffect(submitError) {
        if (submitError != null) {
            showStatsAlert = true
        }
    }

    if (submitSuccess) {
        SurveySuccessView(onBackClick = onBackClick)
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .screenBackground(c.bg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                title = "Khảo sát ý kiến",
                subtitle = "Đóng góp ý kiến để hoàn thiện Lịch Số",
                onBackClick = onBackClick
            )

            when (val state = uiState) {
                is SurveyUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = c.primary)
                    }
                }
                is SurveyUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = c.textTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Hiện tại không có khảo sát hoạt động",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = c.textPrimary
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cảm ơn bạn đã luôn quan tâm và đồng hành cùng Lịch Số!",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = c.textSecondary
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(containerColor = c.primary)
                        ) {
                            Text("Quay lại")
                        }
                    }
                }
                is SurveyUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = c.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Đã xảy ra lỗi",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = c.textPrimary
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = c.textSecondary
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { viewModel.loadActiveSurvey() },
                                colors = ButtonDefaults.buttonColors(containerColor = c.primary)
                            ) {
                                Text("Tải lại")
                            }
                            OutlinedButton(onClick = onBackClick) {
                                Text("Quay lại")
                            }
                        }
                    }
                }
                is SurveyUiState.Success -> {
                    val survey = state.survey
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Info Card
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = c.surfaceContainerHigh)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = survey.title,
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = c.primary
                                        )
                                    )
                                    if (!survey.description.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = survey.description,
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                color = c.textSecondary,
                                                lineHeight = 18.sp
                                            )
                                        )
                                    }
                                }
                            }

                            // Render Questions
                            survey.questions.forEachIndexed { qIdx, question ->
                                val selectedOpts = viewModel.selectedOptions[qIdx] ?: emptyList()
                                val textAns = viewModel.textAnswers[qIdx] ?: ""

                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = c.surfaceContainer),
                                    modifier = Modifier.border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "${qIdx + 1}. ${question.title}",
                                                style = TextStyle(
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = c.textPrimary,
                                                    lineHeight = 20.sp
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (question.required) {
                                                Text(
                                                    text = "*",
                                                    style = TextStyle(
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = c.primary
                                                    )
                                                )
                                            }
                                        }

                                        when (question.type) {
                                            "single_choice" -> {
                                                question.options?.forEach { option ->
                                                    val isSelected = selectedOpts.contains(option)
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(if (isSelected) c.primary.copy(alpha = 0.08f) else Color.Transparent)
                                                            .border(1.dp, if (isSelected) c.primary else c.outlineVariant, RoundedCornerShape(8.dp))
                                                            .clickable {
                                                                viewModel.selectedOptions[qIdx] = listOf(option)
                                                            }
                                                            .padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        RadioButton(
                                                            selected = isSelected,
                                                            onClick = {
                                                                viewModel.selectedOptions[qIdx] = listOf(option)
                                                            },
                                                            colors = RadioButtonDefaults.colors(selectedColor = c.primary)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = option,
                                                            style = TextStyle(
                                                                fontSize = 14.sp,
                                                                color = if (isSelected) c.textPrimary else c.textSecondary
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                            "multiple_choice" -> {
                                                question.options?.forEach { option ->
                                                    val isSelected = selectedOpts.contains(option)
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(if (isSelected) c.primary.copy(alpha = 0.08f) else Color.Transparent)
                                                            .border(1.dp, if (isSelected) c.primary else c.outlineVariant, RoundedCornerShape(8.dp))
                                                            .clickable {
                                                                val currentList = selectedOpts.toMutableList()
                                                                if (currentList.contains(option)) {
                                                                    currentList.remove(option)
                                                                } else {
                                                                    currentList.add(option)
                                                                }
                                                                viewModel.selectedOptions[qIdx] = currentList
                                                            }
                                                            .padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Checkbox(
                                                            checked = isSelected,
                                                            onCheckedChange = {
                                                                val currentList = selectedOpts.toMutableList()
                                                                if (currentList.contains(option)) {
                                                                    currentList.remove(option)
                                                                } else {
                                                                    currentList.add(option)
                                                                }
                                                                viewModel.selectedOptions[qIdx] = currentList
                                                            },
                                                            colors = CheckboxDefaults.colors(checkedColor = c.primary)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = option,
                                                            style = TextStyle(
                                                                fontSize = 14.sp,
                                                                color = if (isSelected) c.textPrimary else c.textSecondary
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                            "text" -> {
                                                OutlinedTextField(
                                                    value = textAns,
                                                    onValueChange = { viewModel.textAnswers[qIdx] = it },
                                                    placeholder = {
                                                        Text(
                                                            "Nhập câu trả lời của bạn...",
                                                            style = TextStyle(fontSize = 13.sp)
                                                        )
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    minLines = 3,
                                                    maxLines = 5,
                                                    textStyle = TextStyle(fontSize = 14.sp, color = c.textPrimary),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = c.primary,
                                                        unfocusedBorderColor = c.outline
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Submit Button Panel
                        Surface(
                            shadowElevation = 8.dp,
                            color = c.surfaceContainer
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.submitSurvey() },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    enabled = !submitting,
                                    colors = ButtonDefaults.buttonColors(containerColor = c.primary)
                                ) {
                                    if (submitting) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Gửi ý kiến đóng góp",
                                            style = TextStyle(
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showStatsAlert) {
        AlertDialog(
            onDismissRequest = {
                showStatsAlert = false
                viewModel.clearSubmitError()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = c.primary
                )
            },
            title = {
                Text("Không thể gửi phản hồi")
            },
            text = {
                Text(submitError ?: "Vui lòng kiểm tra lại kết nối và thử lại.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStatsAlert = false
                        viewModel.clearSubmitError()
                    }
                ) {
                    Text("Đồng ý", color = c.primary)
                }
            }
        )
    }
}

@Composable
private fun SurveySuccessView(onBackClick: () -> Unit) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Cảm ơn bạn!",
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = c.textPrimary
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Ý kiến đóng góp quý báu của bạn đã được lưu trữ thành công. Chúng tôi sẽ sử dụng phản hồi này để hoàn thiện ứng dụng Lịch Số ngày càng tốt hơn.",
            style = TextStyle(
                fontSize = 14.sp,
                color = c.textSecondary,
                lineHeight = 20.sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(containerColor = c.primary),
            modifier = Modifier.height(44.dp).width(160.dp)
        ) {
            Text("Về Trang Chủ")
        }
    }
}
