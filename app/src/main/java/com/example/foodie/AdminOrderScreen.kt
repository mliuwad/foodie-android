package com.example.foodie.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodie.components.FoodieConfirmDialog
import com.example.foodie.components.FoodieLoadingDialog
import com.example.foodie.components.FoodieMessageDialog
import com.example.foodie.formatPrice
import com.example.foodie.model.Order
import com.example.foodie.model.OrderStatus
import com.example.foodie.ui.theme.FoodieColors
import com.example.foodie.viewmodel.AdminOrderViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AdminOrderScreen(
    adminOrderViewModel: AdminOrderViewModel
) {

    val orders by
    adminOrderViewModel.orders.collectAsState()

    val isLoading by
    adminOrderViewModel.isLoading.collectAsState()

    val isUpdating by
    adminOrderViewModel.isUpdating.collectAsState()

    val loadError by
    adminOrderViewModel.loadError.collectAsState()

    val error by
    adminOrderViewModel.error.collectAsState()

    var selectedFilter by rememberSaveable {
        mutableStateOf("ALL")
    }

    var selectedOrder by remember {
        mutableStateOf<Order?>(null)
    }

    var targetStatus by remember {
        mutableStateOf<OrderStatus?>(null)
    }

    LaunchedEffect(Unit) {
        adminOrderViewModel.observeOrders()
    }

    val filteredOrders = remember(
        orders,
        selectedFilter
    ) {

        if (selectedFilter == "ALL") {
            orders
        } else {
            orders.filter {
                it.status == selectedFilter
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoodieColors.Background)
    ) {

        AdminOrderStatistics(orders)

        AdminOrderFilters(
            selectedFilter = selectedFilter,
            onFilterSelected = {
                selectedFilter = it
            }
        )

        HorizontalDivider(
            color = FoodieColors.Border
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            horizontalArrangement =
            Arrangement.SpaceBetween,
            verticalAlignment =
            Alignment.CenterVertically
        ) {

            Text(
                text = "Danh sách đơn hàng",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = FoodieColors.TextDark
            )

            Text(
                text = "${filteredOrders.size} đơn",
                fontSize = 12.sp,
                color = FoodieColors.TextGray
            )
        }

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

                loadError != null -> {

                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment =
                        Alignment.CenterHorizontally,
                        verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                    ) {

                        Text(
                            text = loadError
                                ?: "Không thể tải đơn hàng",
                            color = Color(0xFFDC2626),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )

                        TextButton(
                            onClick = {
                                adminOrderViewModel
                                    .observeOrders()
                            }
                        ) {

                            Text(
                                text = "Thử lại",
                                color = FoodieColors.Green
                            )
                        }
                    }
                }

                filteredOrders.isEmpty() -> {

                    AdminEmptyOrders()
                }

                else -> {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 4.dp
                        ),
                        verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                    ) {

                        items(
                            items = filteredOrders,
                            key = { it.orderId }
                        ) { order ->

                            AdminOrderCard(
                                order = order,
                                enabled = !isUpdating,
                                onStatusChange = { status ->

                                    selectedOrder = order
                                    targetStatus = status
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    val currentOrder = selectedOrder
    val newStatus = targetStatus

    if (currentOrder != null && newStatus != null) {

        FoodieConfirmDialog(
            title = actionLabel(newStatus),

            message = when (newStatus) {

                OrderStatus.CONFIRMED ->
                    "Bạn có muốn xác nhận đơn gọi món này không?"

                OrderStatus.PREPARING ->
                    "Xác nhận bắt đầu chế biến các món trong đơn?"

                OrderStatus.SERVED ->
                    "Bạn xác nhận đã phục vụ đầy đủ các món cho khách?"

                OrderStatus.CANCELLED ->
                    "Bạn có chắc chắn muốn hủy đơn gọi món này?"

                else ->
                    "Xác nhận cập nhật trạng thái đơn hàng?"
            },

            confirmText = "Xác nhận",

            onConfirm = {

                selectedOrder = null
                targetStatus = null

                adminOrderViewModel.updateOrderStatus(
                    orderId = currentOrder.orderId,
                    newStatus = newStatus
                )
            },

            onDismiss = {
                selectedOrder = null
                targetStatus = null
            }
        )
    }

    if (isUpdating) {

        FoodieLoadingDialog(
            message = "Đang cập nhật đơn hàng..."
        )
    }

    error?.let { message ->

        FoodieMessageDialog(
            title = "Cập nhật thất bại",
            message = message,
            isSuccess = false,
            onDismiss = {
                adminOrderViewModel.clearError()
            }
        )
    }
}

// ======================================
// THỐNG KÊ ĐƠN HÀNG
// ======================================

@Composable
private fun AdminOrderStatistics(
    orders: List<Order>
) {

    val pending = orders.count {
        it.status == OrderStatus.PENDING.name
    }

    val processing = orders.count {

        it.status == OrderStatus.CONFIRMED.name ||
                it.status == OrderStatus.PREPARING.name
    }

    val served = orders.count {
        it.status == OrderStatus.SERVED.name
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement =
        Arrangement.spacedBy(8.dp)
    ) {

        AdminStatisticItem(
            modifier = Modifier.weight(1f),
            label = "Chờ xác nhận",
            count = pending,
            color = Color(0xFFB7791F)
        )

        AdminStatisticItem(
            modifier = Modifier.weight(1f),
            label = "Đang xử lý",
            count = processing,
            color = Color(0xFFEA580C)
        )

        AdminStatisticItem(
            modifier = Modifier.weight(1f),
            label = "Đã phục vụ",
            count = served,
            color = FoodieColors.Green
        )
    }
}

@Composable
private fun AdminStatisticItem(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    color: Color
) {

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(
            1.dp,
            FoodieColors.Border
        )
    ) {

        Column(
            modifier = Modifier.padding(
                horizontal = 6.dp,
                vertical = 12.dp
            ),
            horizontalAlignment =
            Alignment.CenterHorizontally,
            verticalArrangement =
            Arrangement.spacedBy(4.dp)
        ) {

            Text(
                text = count.toString(),
                color = color,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                color = FoodieColors.TextGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ======================================
// BỘ LỌC TRẠNG THÁI
// ======================================

@Composable
private fun AdminOrderFilters(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {

    val filters = listOf(
        "ALL" to "Tất cả",
        "PENDING" to "Chờ xác nhận",
        "CONFIRMED" to "Đã xác nhận",
        "PREPARING" to "Đang chế biến",
        "SERVED" to "Đã phục vụ",
        "CANCELLED" to "Đã hủy"
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = 16.dp
        ),
        horizontalArrangement =
        Arrangement.spacedBy(8.dp)
    ) {

        items(filters) { filter ->

            FilterChip(
                selected =
                selectedFilter == filter.first,

                onClick = {
                    onFilterSelected(filter.first)
                },

                label = {

                    Text(
                        text = filter.second,
                        fontSize = 12.sp
                    )
                },

                shape = RoundedCornerShape(8.dp),

                colors = FilterChipDefaults
                    .filterChipColors(
                        containerColor = Color.White,
                        labelColor = FoodieColors.TextGray,
                        selectedContainerColor =
                        FoodieColors.Green,
                        selectedLabelColor = Color.White
                    )
            )
        }
    }
}

// ======================================
// CARD ĐƠN HÀNG
// ======================================

@Composable
private fun AdminOrderCard(
    order: Order,
    enabled: Boolean,
    onStatusChange: (OrderStatus) -> Unit
) {

    val dateText = remember(order.createdAt) {

        order.createdAt?.toDate()?.let { date ->

            SimpleDateFormat(
                "HH:mm · dd/MM/yyyy",
                Locale("vi", "VN")
            ).format(date)

        } ?: "Đang cập nhật"
    }

    val nextStatus = when (order.status) {

        OrderStatus.PENDING.name ->
            OrderStatus.CONFIRMED

        OrderStatus.CONFIRMED.name ->
            OrderStatus.PREPARING

        OrderStatus.PREPARING.name ->
            OrderStatus.SERVED

        else -> null
    }

    val canCancel =
        order.status == OrderStatus.PENDING.name ||
                order.status == OrderStatus.CONFIRMED.name

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(
            1.dp,
            FoodieColors.Border
        )
    ) {

        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement =
            Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                    Arrangement.spacedBy(4.dp)
                ) {

                    Text(
                        text = displayTableName(
                            order.tableId
                        ),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FoodieColors.TextDark
                    )

                    Text(
                        text = dateText,
                        fontSize = 11.sp,
                        color = FoodieColors.TextGray
                    )

                    Text(
                        text = "#${order.orderId.takeLast(6).uppercase()}",
                        fontSize = 11.sp,
                        color = FoodieColors.TextGray
                    )
                }

                AdminStatusLabel(
                    status = order.status
                )
            }

            HorizontalDivider(
                color = FoodieColors.Border
            )

            order.items.forEach { item ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment =
                    Alignment.CenterVertically,
                    horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
                ) {

                    Surface(
                        color = FoodieColors.SoftGreen,
                        shape = RoundedCornerShape(6.dp)
                    ) {

                        Text(
                            text = "${item.quantity}×",
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 4.dp
                            ),
                            fontSize = 12.sp,
                            color = FoodieColors.Green,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = item.foodName,
                        modifier = Modifier.weight(1f),
                        color = FoodieColors.TextDark,
                        fontSize = 14.sp
                    )

                    Text(
                        text = formatPrice(item.subTotal),
                        color = FoodieColors.TextDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (order.note.isNotBlank()) {

                Surface(
                    color = FoodieColors.Background,
                    shape = RoundedCornerShape(8.dp)
                ) {

                    Text(
                        text = "Ghi chú: ${order.note}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        color = FoodieColors.TextGray,
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(
                color = FoodieColors.Border
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                Arrangement.SpaceBetween,
                verticalAlignment =
                Alignment.CenterVertically
            ) {

                Text(
                    text = "Tổng tiền · ${order.totalQuantity} món",
                    color = FoodieColors.TextDark,
                    fontSize = 13.sp
                )

                Text(
                    text = formatPrice(order.totalAmount),
                    color = FoodieColors.Green,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (nextStatus != null) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
                ) {

                    if (canCancel) {

                        OutlinedButton(
                            onClick = {
                                onStatusChange(
                                    OrderStatus.CANCELLED
                                )
                            },
                            enabled = enabled,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                Color(0xFFDC2626)
                            ),
                            colors = ButtonDefaults
                                .outlinedButtonColors(
                                    contentColor =
                                    Color(0xFFDC2626)
                                )
                        ) {

                            Text(
                                text = "Hủy đơn",
                                fontSize = 12.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onStatusChange(nextStatus)
                        },
                        enabled = enabled,
                        modifier = Modifier
                            .weight(2f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FoodieColors.Green,
                            contentColor = Color.White
                        )
                    ) {

                        Text(
                            text = actionLabel(nextStatus),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ======================================
// NHÃN TRẠNG THÁI
// ======================================

@Composable
private fun AdminStatusLabel(
    status: String
) {

    val (label, color) = when (status) {

        OrderStatus.PENDING.name ->
            "Chờ xác nhận" to Color(0xFFB7791F)

        OrderStatus.CONFIRMED.name ->
            "Đã xác nhận" to Color(0xFF3564A5)

        OrderStatus.PREPARING.name ->
            "Đang chế biến" to Color(0xFFEA580C)

        OrderStatus.SERVED.name ->
            "Đã phục vụ" to FoodieColors.Green

        OrderStatus.CANCELLED.name ->
            "Đã hủy" to Color(0xFFDC2626)

        else ->
            "Không xác định" to FoodieColors.TextGray
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp)
    ) {

        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 6.dp
            )
        )
    }
}

// ======================================
// DANH SÁCH TRỐNG
// ======================================

@Composable
private fun AdminEmptyOrders() {

    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment =
        Alignment.CenterHorizontally,
        verticalArrangement =
        Arrangement.spacedBy(12.dp)
    ) {

        Icon(
            imageVector = Icons.Outlined.ReceiptLong,
            contentDescription = null,
            tint = FoodieColors.Green,
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = "Không có đơn hàng",
            color = FoodieColors.TextDark,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Các đơn gọi món sẽ được hiển thị tại đây",
            color = FoodieColors.TextGray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ======================================
// HÀM HỖ TRỢ
// ======================================

private fun actionLabel(
    status: OrderStatus
): String {

    return when (status) {

        OrderStatus.CONFIRMED ->
            "Xác nhận đơn"

        OrderStatus.PREPARING ->
            "Bắt đầu chế biến"

        OrderStatus.SERVED ->
            "Đã lên món"

        OrderStatus.CANCELLED ->
            "Hủy đơn"

        else ->
            "Xác nhận"
    }
}

private fun displayTableName(
    tableId: String
): String {

    return if (
        tableId.startsWith(
            "TABLE",
            ignoreCase = true
        )
    ) {

        "Bàn ${tableId.substring(5)}"

    } else {

        tableId
    }
}