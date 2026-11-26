package com.example.foodie

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodie.components.FoodieMessageDialog
import com.example.foodie.ui.theme.FoodieColors
import com.example.foodie.viewmodel.BillViewModel

@Composable
fun BillScreen(
    sessionId: String,
    billViewModel: BillViewModel,
    onBackClick: () -> Unit,
    onPaymentCompleted: () -> Unit
) {

    val bill by billViewModel.bill.collectAsState()

    val isLoading by
    billViewModel.isLoading.collectAsState()

    val error by
    billViewModel.error.collectAsState()

    LaunchedEffect(sessionId) {
        billViewModel.observeBill(sessionId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoodieColors.Background)
            .navigationBarsPadding()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FoodieColors.Green)
                .statusBarsPadding()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onBackClick) {

                Icon(
                    Icons.Outlined.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.White
                )
            }

            Text(
                text = "Thanh toán",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            val currentBill = bill

            when {

                isLoading -> {

                    CircularProgressIndicator(
                        color = FoodieColors.Green
                    )
                }

                error != null || currentBill == null -> {

                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment =
                        Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = error
                                ?: "Chưa tìm thấy hóa đơn",
                            color = FoodieColors.TextGray,
                            textAlign = TextAlign.Center
                        )

                        TextButton(
                            onClick = {
                                billViewModel.observeBill(sessionId)
                            }
                        ) {

                            Text(
                                "Thử lại",
                                color = FoodieColors.Green
                            )
                        }
                    }
                }

                else -> {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment =
                        Alignment.CenterHorizontally,
                        verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                    ) {

                        Icon(
                            Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = FoodieColors.Green,
                            modifier = Modifier.size(56.dp)
                        )

                        Text(
                            text = "Hóa đơn thanh toán",
                            color = FoodieColors.TextDark,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = currentBill.tableId,
                            color = FoodieColors.TextGray,
                            fontSize = 14.sp
                        )

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

                                BillInfoRow(
                                    "Mã hóa đơn",
                                    "#${currentBill.billId.takeLast(6).uppercase()}"
                                )

                                BillInfoRow(
                                    "Số lần gọi món",
                                    "${currentBill.orderCount} đơn"
                                )

                                BillInfoRow(
                                    "Tổng số lượng",
                                    "${currentBill.totalQuantity} món"
                                )

                                HorizontalDivider(
                                    color = FoodieColors.Border
                                )

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment =
                                    Alignment.CenterHorizontally
                                ) {

                                    Text(
                                        "Tổng tiền thanh toán",
                                        color = FoodieColors.TextGray,
                                        fontSize = 14.sp
                                    )

                                    Text(
                                        formatPrice(
                                            currentBill.totalAmount
                                        ),
                                        color = FoodieColors.Green,
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (
                            currentBill.paymentStatus == "UNPAID"
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
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment =
                                    Alignment.CenterHorizontally,
                                    verticalArrangement =
                                    Arrangement.spacedBy(12.dp)
                                ) {

                                    Text(
                                        "Thanh toán chuyển khoản",
                                        fontWeight = FontWeight.SemiBold,
                                        color = FoodieColors.TextDark
                                    )

                                    Icon(
                                        Icons.Outlined.QrCode,
                                        contentDescription = null,
                                        modifier = Modifier.size(100.dp),
                                        tint = FoodieColors.TextGray
                                    )

                                    Text(
                                        "QR minh họa - chưa cấu hình",
                                        color = FoodieColors.TextGray,
                                        fontSize = 13.sp
                                    )

                                    Text(
                                        "Vui lòng liên hệ nhân viên để thanh toán.",
                                        color = FoodieColors.TextGray,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = FoodieColors.SoftGreen,
                            shape = RoundedCornerShape(12.dp)
                        ) {

                            Text(
                                text = if (
                                    currentBill.paymentStatus == "PAID"
                                ) {
                                    "Đã thanh toán thành công"
                                } else {
                                    "Đang chờ nhân viên xác nhận thanh toán"
                                },
                                modifier = Modifier.padding(16.dp),
                                color = FoodieColors.Green,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    if (bill?.paymentStatus == "PAID") {

        FoodieMessageDialog(
            title = "Thanh toán thành công",
            message = "Cảm ơn quý khách đã sử dụng dịch vụ tại Foodie!",
            isSuccess = true,
            buttonText = "Hoàn tất",
            onDismiss = onPaymentCompleted
        )
    }
}

@Composable
private fun BillInfoRow(
    label: String,
    value: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            label,
            color = FoodieColors.TextGray,
            fontSize = 14.sp
        )

        Text(
            value,
            color = FoodieColors.TextDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}