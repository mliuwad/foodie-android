package com.example.foodie.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodie.components.FoodieLoadingDialog
import com.example.foodie.components.FoodieMessageDialog
import com.example.foodie.formatPrice
import com.example.foodie.model.Bill
import com.example.foodie.model.PaymentMethod
import com.example.foodie.ui.theme.FoodieColors
import com.example.foodie.viewmodel.BillViewModel

@Composable
fun AdminPaymentScreen(
    billViewModel: BillViewModel
) {

    val bills by billViewModel.bills.collectAsState()

    val isLoading by
    billViewModel.isLoading.collectAsState()

    val isPaying by
    billViewModel.isPaying.collectAsState()

    val error by
    billViewModel.error.collectAsState()

    val paymentSuccess by
    billViewModel.paymentSuccess.collectAsState()

    var selectedBill by remember {
        mutableStateOf<Bill?>(null)
    }

    var paymentMethod by remember {
        mutableStateOf(PaymentMethod.CASH)
    }

    LaunchedEffect(Unit) {
        billViewModel.observeUnpaidBills()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement =
        Arrangement.spacedBy(12.dp)
    ) {

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FoodieColors.SoftGreen,
            shape = RoundedCornerShape(12.dp)
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement =
                Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    "Hóa đơn chờ thanh toán",
                    color = FoodieColors.TextGray,
                    fontSize = 14.sp
                )

                Text(
                    bills.size.toString(),
                    color = FoodieColors.Green,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider(
                    color = FoodieColors.Border
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                    Arrangement.SpaceBetween
                ) {

                    Text(
                        "Tổng tiền đang chờ",
                        color = FoodieColors.TextGray,
                        fontSize = 13.sp
                    )

                    Text(
                        formatPrice(
                            bills.sumOf { it.totalAmount }
                        ),
                        color = FoodieColors.Green,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            "Danh sách thanh toán",
            color = FoodieColors.TextDark,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

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

                bills.isEmpty() -> {

                    Column(
                        horizontalAlignment =
                        Alignment.CenterHorizontally,
                        verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                    ) {

                        Icon(
                            Icons.Outlined.Payments,
                            contentDescription = null,
                            tint = FoodieColors.Green,
                            modifier = Modifier.size(64.dp)
                        )

                        Text(
                            "Không có hóa đơn đang chờ",
                            color = FoodieColors.TextDark,
                            fontWeight = FontWeight.Medium
                        )

                        TextButton(
                            onClick = {
                                billViewModel.observeUnpaidBills()
                            }
                        ) {

                            Text(
                                "Tải lại",
                                color = FoodieColors.Green
                            )
                        }
                    }
                }

                else -> {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                    ) {

                        items(
                            items = bills,
                            key = { it.billId }
                        ) { bill ->

                            AdminBillCard(
                                bill = bill,
                                enabled = !isPaying,
                                onConfirmClick = {

                                    selectedBill = bill

                                    paymentMethod =
                                        PaymentMethod.CASH
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedBill?.let { bill ->

        AlertDialog(

            onDismissRequest = {
                selectedBill = null
            },

            containerColor = Color.White,

            shape = RoundedCornerShape(20.dp),

            tonalElevation = 0.dp,

            title = {

                Text(
                    "Xác nhận thanh toán",
                    color = FoodieColors.TextDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },

            text = {

                Column(
                    verticalArrangement =
                    Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        "${bill.tableId} · ${formatPrice(bill.totalAmount)}",
                        color = FoodieColors.Green,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Phương thức thanh toán",
                        color = FoodieColors.TextDark,
                        fontSize = 14.sp
                    )

                    PaymentMethod.entries.forEach { method ->

                        Row(
                            verticalAlignment =
                            Alignment.CenterVertically
                        ) {

                            RadioButton(
                                selected = paymentMethod == method,

                                onClick = {
                                    paymentMethod = method
                                },

                                colors = RadioButtonDefaults.colors(
                                    selectedColor = FoodieColors.Green,
                                    unselectedColor = FoodieColors.TextGray
                                )
                            )

                            Text(
                                text = if (
                                    method == PaymentMethod.CASH
                                ) "Tiền mặt"
                                else "Chuyển khoản",

                                color = FoodieColors.TextDark,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Text(
                        "Chỉ xác nhận khi đã kiểm tra nhận đủ tiền. Bàn sẽ được giải phóng sau khi thanh toán.",
                        color = FoodieColors.TextGray,
                        fontSize = 12.sp
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        selectedBill = null

                        billViewModel.confirmPayment(
                            bill.billId,
                            paymentMethod
                        )
                    },

                    shape = RoundedCornerShape(10.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = FoodieColors.Green,
                        contentColor = Color.White
                    )
                ) {

                    Text(
                        "Đã nhận tiền",
                        fontSize = 13.sp
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        selectedBill = null
                    }
                ) {

                    Text(
                        "Hủy",
                        color = FoodieColors.TextGray
                    )
                }
            }
        )
    }

    if (isPaying) {

        FoodieLoadingDialog(
            message = "Đang xác nhận thanh toán..."
        )
    }

    if (paymentSuccess) {

        FoodieMessageDialog(
            title = "Thanh toán thành công",
            message = "Hóa đơn đã hoàn tất và bàn ăn đã được giải phóng.",
            isSuccess = true,
            onDismiss = {
                billViewModel.clearPaymentSuccess()
            }
        )
    }

    error?.let { message ->

        FoodieMessageDialog(
            title = "Có lỗi xảy ra",
            message = message,
            isSuccess = false,
            onDismiss = {
                billViewModel.clearError()
            }
        )
    }
}

@Composable
private fun AdminBillCard(
    bill: Bill,
    enabled: Boolean,
    onConfirmClick: () -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            FoodieColors.Border
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
            Arrangement.spacedBy(12.dp)
        ) {

            Row(
                verticalAlignment =
                Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = bill.tableId.replace(
                            "TABLE",
                            "Bàn "
                        ),
                        color = FoodieColors.TextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "#${bill.billId.takeLast(6).uppercase()}",
                        color = FoodieColors.TextGray,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    color = Color(0xFFFFF1DB),
                    shape = RoundedCornerShape(20.dp)
                ) {

                    Text(
                        "Chưa thanh toán",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        color = Color(0xFFB7791F),
                        fontSize = 11.sp
                    )
                }
            }

            HorizontalDivider(
                color = FoodieColors.Border
            )

            Text(
                "Số lần gọi món: ${bill.orderCount}",
                color = FoodieColors.TextGray,
                fontSize = 13.sp
            )

            Text(
                "Tổng số lượng: ${bill.totalQuantity} món",
                color = FoodieColors.TextGray,
                fontSize = 13.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                Arrangement.SpaceBetween
            ) {

                Text(
                    "Tổng thanh toán",
                    color = FoodieColors.TextDark,
                    fontSize = 14.sp
                )

                Text(
                    formatPrice(bill.totalAmount),
                    color = FoodieColors.Green,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onConfirmClick,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),

                shape = RoundedCornerShape(10.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = FoodieColors.Green,
                    contentColor = Color.White
                )
            ) {

                Text(
                    "Xác nhận thanh toán",
                    fontSize = 13.sp
                )
            }
        }
    }
}