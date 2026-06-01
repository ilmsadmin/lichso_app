package com.lichso.app.feature.worldclock

import com.lichso.app.ui.theme.screenBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.data.local.entity.WorldClockCityEntity
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.LichSoBadge
import com.lichso.app.ui.components.LichSoDeleteButton
import com.lichso.app.ui.components.LichSoDialogSurface
import com.lichso.app.ui.components.LichSoEmptyState
import com.lichso.app.ui.components.LichSoHeaderActionButton
import com.lichso.app.ui.components.LichSoSectionHeader
import com.lichso.app.ui.components.LichSoTextButton
import com.lichso.app.ui.components.LichSoTextField
import com.lichso.app.ui.theme.LichSoThemeColors

@Composable
fun WorldClockScreen(
    onBackClick: () -> Unit = {},
    viewModel: WorldClockViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().screenBackground(c.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                title = "Múi giờ thế giới",
                subtitle = "Thêm / xoá thành phố, đổi múi giờ nhanh",
                onBackClick = onBackClick,
                actions = {
                    LichSoHeaderActionButton(
                        icon = Icons.Filled.Add,
                        contentDescription = "Thêm thành phố",
                        onClick = viewModel::openAddDialog,
                    )
                }
            )

            if (state.cities.isEmpty()) {
                EmptyClockState { viewModel.openAddDialog() }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        ConverterCard(
                            state = state,
                            onSetBase = viewModel::setBase,
                            onSetTarget = viewModel::setTarget,
                        )
                    }

                    item {
                        LichSoSectionHeader(
                            title = "Thành phố theo dõi",
                            trailing = "${state.cities.size} thành phố",
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp),
                        )
                    }

                    items(state.cities, key = { it.entity.id }) { cityTime ->
                        CityClockRow(
                            cityTime = cityTime,
                            isBase = cityTime.entity.id == state.baseCity?.id,
                            onDelete = { viewModel.removeCity(cityTime.entity) },
                        )
                    }

                    item {
                        LichSoTextButton(
                            text = "Thêm thành phố",
                            onClick = viewModel::openAddDialog,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Filled.Add,
                        )
                    }
                }
            }
        }
    }

    if (state.showAddDialog) {
        AddCityDialog(
            state = state,
            onDismiss = viewModel::closeAddDialog,
            onSearch = viewModel::setSearch,
            onAdd = viewModel::addCity,
        )
    }
}

@Composable
private fun CityClockRow(
    cityTime: CityTimeUi,
    isBase: Boolean,
    onDelete: () -> Unit,
) {
    val c = LichSoThemeColors.current
    val bgColor = if (isBase) Color(0xFFE8F5E9) else c.surfaceContainer
    val borderColor = if (isBase) Color(0xFFA5D6A7) else c.outlineVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    cityTime.entity.cityName,
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                )
                if (isBase) {
                    LichSoBadge(text = "Cơ sở", backgroundColor = Color(0xFF2E7D32))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(cityTime.entity.country, style = TextStyle(fontSize = 12.sp, color = c.textTertiary))
                Text(cityTime.offsetLabel, style = TextStyle(fontSize = 11.sp, color = c.textTertiary))
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                cityTime.timeStr,
                style = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isBase) Color(0xFF1B5E20) else c.textPrimary,
                ),
            )
            Text(cityTime.dateStr, style = TextStyle(fontSize = 12.sp, color = c.textTertiary))
        }

        Spacer(Modifier.width(10.dp))

        LichSoDeleteButton(onClick = onDelete)
    }
}

@Composable
private fun ConverterCard(
    state: WorldClockUiState,
    onSetBase: (WorldClockCityEntity) -> Unit,
    onSetTarget: (WorldClockCityEntity) -> Unit,
) {
    val baseTime = state.cities.find { it.entity.id == state.baseCity?.id }
    val targetTime = state.cities.find { it.entity.id == state.targetCity?.id }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF0288D1))),
                shape = RoundedCornerShape(18.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Đổi múi giờ nhanh",
            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.85f)),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CityDropdown(
                modifier = Modifier.weight(1f),
                selected = state.baseCity,
                allCities = state.cities.map { it.entity },
                onSelect = onSetBase,
            )
            Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = Color.White)
            CityDropdown(
                modifier = Modifier.weight(1f),
                selected = state.targetCity,
                allCities = state.cities.map { it.entity },
                onSelect = onSetTarget,
            )
        }

        if (baseTime != null && targetTime != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(baseTime.timeStr, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
                    Text(baseTime.entity.cityName, style = TextStyle(fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f)))
                }
                Text("→", style = TextStyle(fontSize = 20.sp, color = Color.White.copy(alpha = 0.6f)))
                Column(horizontalAlignment = Alignment.End) {
                    Text(targetTime.timeStr, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFE082)))
                    Text(targetTime.entity.cityName, style = TextStyle(fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f)))
                }
            }
        }
    }
}

@Composable
private fun CityDropdown(
    modifier: Modifier = Modifier,
    selected: WorldClockCityEntity?,
    allCities: List<WorldClockCityEntity>,
    onSelect: (WorldClockCityEntity) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text(
                selected?.cityName ?: "Chọn...",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White),
                maxLines = 1,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            allCities.forEach { city ->
                DropdownMenuItem(
                    text = { Text(city.cityName) },
                    onClick = { expanded = false; onSelect(city) }
                )
            }
        }
    }
}

@Composable
private fun AddCityDialog(
    state: WorldClockUiState,
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit,
    onAdd: (WorldClockCityEntity) -> Unit,
) {
    val c = LichSoThemeColors.current
    val alreadyAdded = state.cities.map { it.entity.cityName }.toSet()
    val filtered = ALL_AVAILABLE_CITIES.filter { city ->
        city.cityName !in alreadyAdded &&
            (state.searchQuery.isBlank() ||
                city.cityName.contains(state.searchQuery, ignoreCase = true) ||
                city.country.contains(state.searchQuery, ignoreCase = true))
    }

    LichSoDialogSurface(onDismiss = onDismiss, title = "Thêm thành phố") {
            LichSoTextField(
                value = state.searchQuery,
                onValueChange = onSearch,
                label = "Tìm kiếm",
                placeholder = "Tìm thành phố hoặc quốc gia...",
                leadingIcon = Icons.Filled.Search,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(10.dp))

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("Không tìm thấy thành phố phù hợp", style = TextStyle(fontSize = 14.sp, color = c.textTertiary))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(filtered, key = { it.cityName + it.timezone }) { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onAdd(city) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(city.cityName, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary))
                                Text(city.country, style = TextStyle(fontSize = 12.sp, color = c.textSecondary))
                            }
                            Icon(Icons.Filled.Add, contentDescription = null, tint = c.primary, modifier = Modifier.size(20.dp))
                        }
                        HorizontalDivider(color = c.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Đóng") }
    }
}

@Composable
private fun EmptyClockState(onAdd: () -> Unit) {
    LichSoEmptyState(
        title = "Chưa có thành phố nào",
        message = "Thêm các thành phố bạn muốn theo dõi múi giờ quốc tế",
        actionText = "Thêm thành phố",
        onAction = onAdd,
        modifier = Modifier.fillMaxSize(),
        icon = Icons.Filled.Add,
    )
}
