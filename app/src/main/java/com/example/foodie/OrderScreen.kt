package com.example.foodie

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodie.components.FoodieBottomBar
import com.example.foodie.model.Order
import com.example.foodie.model.OrderStatus
import com.example.foodie.ui.theme.FoodieColors
import com.example.foodie.viewmodel.DiningSessionViewModel
import com.example.foodie.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrderScreen(
    modifier: Modifier = Modifier,
    orderViewModel: OrderViewModel,
    diningSessionViewModel: DiningSessionViewModel,
    onTabSelected: (Int) -> Unit,
    onCheckoutClick: (() -> Unit)? = null
) {

    val sessionId by
    diningSessionViewModel.sessionId.collectAsState()

    val selectedTable by
    diningSessionViewModel.selectedTable.collectAsState()

    val orders by
    orderViewModel.orders.collectAsState()

    val isLoading by
    orderViewModel.isOrdersLoading.collectAsState()

    val error by
    orderViewModel.ordersError.collectAsState()
    val checkoutRequested by
    diningSessionViewModel.checkoutRequested.collectAsState()

    LaunchedEffect(sessionId) {
        val id = sessionId

        if (id != null) {
            orderViewModel.observeOrders(id)
        } else {
            orderViewModel.stopObservingOrders()
        }
    }

    val validOrders = orders.filter {
        it.status != OrderStatus.CANCELLED.name
    }
    val canCheckout =
        validOrders.isNotEmpty() &&
                validOrders.all {
                    it.status == "SERVED"
                }

    val totalAmount = validOrders.sumOf {
        it.totalAmount
    }

    val totalQuantity = validOrders.sumOf {
        it.totalQuantity
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FoodieColors.Background)
            .navigationBarsPadding()
    ) {

        OrderTopBar(
            tableName = selectedTable?.tableName
                ?: "Chưa chọn bàn"
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(12.dp))

            OrderSummaryCard(
                totalAmount = totalAmount,
                orderCount = validOrders.size,
                totalQuantity = totalQuantity
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                Arrangement.SpaceBetween,
                verticalAlignment =
                Alignment.CenterVertically
            ) {
                Text(
                    text = "Các lần gọi món",
                    color = FoodieColors.TextDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${orders.size} đơn hàng",
                    color = FoodieColors.TextGray,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {

                when {

                    sessionId == null -> {
                        Text(
                            text = "Không tìm thấy phiên ăn",
                            color = FoodieColors.TextGray,
                            fontSize = 14.sp
                        )
                    }

                    isLoading -> {
                        CircularProgressIndicator(
                            color = FoodieColors.Green
                        )
                    }

                    error != null -> {

                        Column(
                            horizontalAlignment =
                            Alignment.CenterHorizontally,
                            verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                        ) {

                            Text(
                                text = error
                                    ?: "Không thể tải đơn hàng",
                                color = Color(0xFFDC2626),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )

                            TextButton(
                                onClick = {
                                    sessionId?.let {
                                        orderViewModel.observeOrders(
                                            sessionId = it,
                                            forceRefresh = true
                                        )
                                    }
                                },
                                colors =
                                ButtonDefaults.textButtonColors(
                                    contentColor =
                                    FoodieColors.Green
                                )
                            ) {
                                Text("Thử lại")
                            }
                        }
                    }

                    orders.isEmpty() -> {
                        EmptyOrders(
                            onMenuClick = {
                                onTabSelected(0)
                            }
                        )
                    }

                    else -> {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement =
                            Arrangement.spacedBy(12.dp),
                            contentPadding =
                            PaddingValues(bottom = 12.dp)
                        ) {

                            itemsIndexed(
                                items = orders,
                                key = { _, order ->
                                    order.orderId
                                }
                            ) { index, order ->

                                OrderCard(
                                    order = order,
                                    orderNumber =
                                    orders.size - index
                                )
                            }
                        }
                    }
                }
            }
        }

        if (
            validOrders.isNotEmpty() &&
            !isLoading &&
            error == null
        ) {

            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                    verticalArrangement =
                    Arrangement.spacedBy(8.dp)
                ) {

                    if (checkoutRequested) {

                        Text(
                            text = "Nhân viên đang xử lý yêu cầu thanh toán",
                            color = FoodieColors.Green,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                    } else if (!canCheckout) {

                        Text(
                            text = "Vui lòng chờ phục vụ đầy đủ các món trước khi thanh toán",
                            color = FoodieColors.TextGray,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = {
                            onCheckoutClick?.invoke()
                        },

                        enabled = (checkoutRequested || canCheckout) &&
                                onCheckoutClick != null,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),

                        shape = RoundedCornerShape(12.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = FoodieColors.Green,
                            contentColor = Color.White,
                            disabledContainerColor =
                            FoodieColors.SoftGreen,
                            disabledContentColor =
                            FoodieColors.TextGray
                        )
                    ) {

                        Text(
                            text = if (checkoutRequested)
                                "Xem hóa đơn"
                            else
                                "Yêu cầu thanh toán",

                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        FoodieBottomBar(
            selectedIndex = 1,
            onTabSelected = onTabSelected
        )
    }
}

// ==============================
// TOP BAR
// ==============================

@Composable
private fun OrderTopBar(
    tableName: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FoodieColors.Green)
            .statusBarsPadding()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        horizontalArrangement =
        Arrangement.SpaceBetween,
        verticalAlignment =
        Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Đơn hàng",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Các món đã gọi",
                color = FoodieColors.SoftGreen,
                fontSize = 12.sp
            )
        }

        Surface(
            color = Color.White.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp)
        ) {

            Column(
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 6.dp
                ),
                horizontalAlignment =
                Alignment.CenterHorizontally
            ) {

                Text(
                    text = tableName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Phiên ăn",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ==============================
// TỔNG TIỀN TẠM TÍNH
// ==============================

@Composable
private fun OrderSummaryCard(
    totalAmount: Double,
    orderCount: Int,
    totalQuantity: Int
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = FoodieColors.SoftGreen
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement =
            Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = "Tổng tiền tạm tính",
                color = FoodieColors.TextGray,
                fontSize = 13.sp
            )

            Text(
                text = formatPrice(totalAmount),
                color = FoodieColors.Green,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(
                color = FoodieColors.Green.copy(
                    alpha = 0.15f
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                SummaryItem(
                    modifier = Modifier.weight(1f),
                    label = "Số lần gọi",
                    value = orderCount.toString()
                )

                SummaryItem(
                    modifier = Modifier.weight(1f),
                    label = "Số lượng món",
                    value = totalQuantity.toString()
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {

    Column(
        modifier = modifier,
        verticalArrangement =
        Arrangement.spacedBy(4.dp)
    ) {

        Text(
            text = label,
            fontSize = 12.sp,
            color = FoodieColors.TextGray
        )

        Text(
            text = value,
            fontSize = 20.sp,
            color = FoodieColors.TextDark,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==============================
// CARD ĐƠN HÀNG
// ==============================

@Composable
private fun OrderCard(
    order: Order,
    orderNumber: Int
) {

    val dateText = remember(order.createdAt) {

        order.createdAt?.toDate()?.let { date ->

            SimpleDateFormat(
                "HH:mm · dd/MM/yyyy",
                Locale("vi", "VN")
            ).format(date)

        } ?: "Đang cập nhật"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            FoodieColors.Border
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                        text = "Lần gọi $orderNumber",
                        color = FoodieColors.TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = dateText,
                        color = FoodieColors.TextGray,
                        fontSize = 11.sp
                    )
                }

                OrderStatusLabel(
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
                            color = FoodieColors.Green,
                            fontSize = 12.sp,
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

                Text(
                    text = "Ghi chú: ${order.note}",
                    color = FoodieColors.TextGray,
                    fontSize = 12.sp
                )
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
                    text = "Tổng tiền",
                    color = FoodieColors.TextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = formatPrice(order.totalAmount),
                    color = FoodieColors.Green,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==============================
// TRẠNG THÁI ĐƠN HÀNG
// ==============================

@Composable
private fun OrderStatusLabel(
    status: String
) {

    val statusText = when (status) {
        OrderStatus.PENDING.name -> "Chờ xác nhận"
        OrderStatus.CONFIRMED.name -> "Đã xác nhận"
        OrderStatus.PREPARING.name -> "Đang chế biến"
        OrderStatus.SERVED.name -> "Đã phục vụ"
        OrderStatus.CANCELLED.name -> "Đã hủy"
        else -> "Không xác định"
    }

    val statusColor = when (status) {
        OrderStatus.PENDING.name -> Color(0xFFB7791F)
        OrderStatus.CONFIRMED.name -> Color(0xFF3564A5)
        OrderStatus.PREPARING.name -> Color(0xFFEA580C)
        OrderStatus.SERVED.name -> FoodieColors.Green
        OrderStatus.CANCELLED.name -> Color(0xFFDC2626)
        else -> FoodieColors.TextGray
    }

    Surface(
        color = statusColor.copy(alpha = 0.10f),
        shape = RoundedCornerShape(20.dp)
    ) {

        Text(
            text = statusText,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 6.dp
            ),
            color = statusColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ==============================
// CHƯA CÓ ĐƠN HÀNG
// ==============================

@Composable
private fun EmptyOrders(
    onMenuClick: () -> Unit
) {

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
            text = "Chưa có đơn hàng",
            color = FoodieColors.TextDark,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Hãy chọn món yêu thích và bắt đầu gọi món nhé!",
            color = FoodieColors.TextGray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onMenuClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FoodieColors.Green,
                contentColor = Color.White
            )
        ) {
            Text("Xem thực đơn")
        }
    }
}