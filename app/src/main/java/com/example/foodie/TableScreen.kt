package com.example.foodie

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodie.components.FoodieConfirmDialog
import com.example.foodie.components.FoodieLoadingDialog
import com.example.foodie.components.FoodieMessageDialog
import com.example.foodie.model.Table
import com.example.foodie.model.TableStatus
import com.example.foodie.ui.theme.FoodieColors
import com.example.foodie.viewmodel.DiningSessionViewModel
import com.example.foodie.viewmodel.TableViewModel



@Composable
fun TableScreen(
    modifier: Modifier = Modifier,
    tableViewModel: TableViewModel = viewModel(),
    diningSessionViewModel: DiningSessionViewModel,
    onSessionStarted: () -> Unit,
    onAdminClick: () -> Unit
) {
    val sessionId by diningSessionViewModel.sessionId.collectAsState()

    val isStartingSession by
    diningSessionViewModel.isLoading.collectAsState()

    val sessionError by
    diningSessionViewModel.error.collectAsState()

    var selectedTable by remember {
        mutableStateOf<Table?>(null)
    }

    LaunchedEffect(sessionId) {
        if (sessionId != null) {
            onSessionStarted()
        }
    }
    val tables by tableViewModel.tables.collectAsState()
    val isLoading by tableViewModel.isLoading.collectAsState()
    val error by tableViewModel.error.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FoodieColors.Background)
            .navigationBarsPadding()
    ) {
        TableTopBar(
            onRefresh = {
                tableViewModel.loadTables()
            },
            onAdminClick = onAdminClick

        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Chọn bàn ăn",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = FoodieColors.TextDark
            )

            Text(
                text = "Chọn bàn để bắt đầu phục vụ",
                fontSize = 13.sp,
                color = FoodieColors.TextGray
            )

            Spacer(Modifier.height(16.dp))

            TableStatistics(tables = tables)

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            color = FoodieColors.Green
                        )
                    }

                    error != null -> {
                        Column(
                            horizontalAlignment =
                            Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = error ?: "Có lỗi xảy ra",
                                color = MaterialTheme.colorScheme.error
                            )

                            TextButton(
                                onClick = {
                                    tableViewModel.loadTables()
                                }
                            ) {
                                Text("Thử lại", color = FoodieColors.Green)
                            }
                        }
                    }

                    tables.isEmpty() -> {
                        Text(
                            text = "Chưa có bàn ăn",
                            color = FoodieColors.TextGray
                        )
                    }

                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement =
                            Arrangement.spacedBy(12.dp),
                            verticalArrangement =
                            Arrangement.spacedBy(12.dp),
                            contentPadding =
                            PaddingValues(bottom = 16.dp)
                        ) {
                            items(
                                items = tables,
                                key = { it.tableId }
                            ) { table ->
                                TableCard(
                                    table = table,
                                    onClick = {
                                        if (!isStartingSession &&
                                            table.isAvailable
                                        ) {
                                            selectedTable = table
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    // Xác nhận mở phiên ăn
    selectedTable?.let { table ->
        FoodieConfirmDialog(
            title = "Bắt đầu phục vụ",
            message = "Bạn có muốn mở phiên ăn cho ${table.tableName} không?",
            confirmText = "Xác nhận",
            onConfirm = {
                selectedTable = null
                diningSessionViewModel.startSession(table)
            },
            onDismiss = { selectedTable = null }
        )
    }

    if (isStartingSession) {
        FoodieLoadingDialog(message = "Đang mở phiên ăn...")
    }

    sessionError?.let { message ->
        FoodieMessageDialog(
            title = "Không thể mở phiên ăn",
            message = message,
            isSuccess = false,
            buttonText = "Đóng",
            onDismiss = {
                diningSessionViewModel.clearError()
                tableViewModel.loadTables()
            }
        )
    }
}

@Composable
private fun TableTopBar(
    onRefresh: () -> Unit,
    onAdminClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FoodieColors.Green)
            .statusBarsPadding()
            .height(48.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Foodie · Bàn ăn",
            modifier = Modifier.weight(1f),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        IconButton(onClick = onAdminClick) {
            Icon(
                imageVector = Icons.Outlined.AdminPanelSettings,
                contentDescription = "Đăng nhập nhân viên",
                tint = Color.White
            )
        }

        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Tải lại danh sách bàn",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun TableStatistics(
    tables: List<Table>
) {
    val available = tables.count {
        it.isAvailable
    }

    val occupied = tables.count {
        it.status == TableStatus.OCCUPIED.name && it.currentSessionId != null
    }

    val checkout = tables.count {
        it.status == TableStatus.CHECKOUT_REQUESTED.name && it.currentSessionId != null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            FoodieColors.Border
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement =
            Arrangement.SpaceEvenly
        ) {
            StatisticItem(
                count = available,
                label = "Bàn trống",
                color = FoodieColors.Green
            )

            StatisticItem(
                count = occupied,
                label = "Đang phục vụ",
                color = Color(0xFFDC2626)
            )

            StatisticItem(
                count = checkout,
                label = "Chờ thanh toán",
                color = Color(0xFF3564A5)
            )
        }
    }
}

@Composable
private fun StatisticItem(
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = count.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Text(
            text = label,
            fontSize = 11.sp,
            color = FoodieColors.TextGray
        )
    }
}

@Composable
private fun TableCard(
    table: Table,
    onClick: () -> Unit
) {
    val statusText = when {
        table.isAvailable -> "Bàn trống"
        table.status == TableStatus.OCCUPIED.name && table.currentSessionId != null -> "Đang phục vụ"
        table.status == TableStatus.CHECKOUT_REQUESTED.name && table.currentSessionId != null -> "Chờ thanh toán"
        else -> "Trạng thái không hợp lệ"
    }

    val statusColor = when {
        table.isAvailable -> FoodieColors.Green
        table.status == TableStatus.OCCUPIED.name && table.currentSessionId != null -> Color(0xFFDC2626)
        table.status == TableStatus.CHECKOUT_REQUESTED.name && table.currentSessionId != null -> Color(0xFF3564A5)
        else -> Color(0xFFDC2626)
    }

    Card(
        onClick = onClick,
        enabled = table.isAvailable,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            disabledContainerColor = statusColor.copy(alpha = 0.06f),
            disabledContentColor = FoodieColors.TextDark
        ),
        border = BorderStroke(
            1.dp,
            statusColor.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.TableRestaurant,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(42.dp)
            )

            Text(
                text = table.tableName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = FoodieColors.TextDark
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.People,
                    contentDescription = null,
                    tint = FoodieColors.TextGray,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "${table.capacity} người",
                    fontSize = 12.sp,
                    color = FoodieColors.TextGray
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = statusColor.copy(alpha = 0.10f)
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }
        }
    }
}
