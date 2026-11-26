package com.example.foodie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodie.components.FoodMenuCard
import com.example.foodie.viewmodel.CartViewModel
import com.example.foodie.ui.theme.FoodieColors.Green
import com.example.foodie.ui.theme.FoodieColors.Background
import com.example.foodie.ui.theme.FoodieColors.TextDark
import com.example.foodie.ui.theme.FoodieColors.TextGray
import com.example.foodie.ui.theme.FoodieColors.Border
import com.example.foodie.ui.theme.FoodieColors.SoftGreen
import com.example.foodie.formatPrice

@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onBackClick: () -> Unit,
    onOrderClick: () -> Unit = {}
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val totalPrice = cartItems.sumOf { it.quantity * it.food.price }
    val totalQuantity = cartItems.sumOf { it.quantity }

    Scaffold(
        containerColor = Background,
        topBar = { CartTopBar(onBackClick) },
        bottomBar = {
            if (cartItems.isNotEmpty()) CartBottomBar(
                totalQuantity = totalQuantity,
                totalPrice = totalPrice,
                enabled = cartItems.isNotEmpty(),
                onOrderClick = onOrderClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyCart(onBackClick)
                }
            } else {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Món đã chọn",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Kiểm tra trước khi gửi đến nhà bếp",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                    Surface(
                        color = SoftGreen,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${cartItems.size} loại món",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = Green,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(items = cartItems, key = { it.food.id }) { item ->
                        FoodMenuCard(
                            food = item.food,
                            quantity = item.quantity,
                            onAddClick = { cartViewModel.addFood(item.food) },
                            onDecreaseClick = { cartViewModel.decreaseQuantity(item.food.id) },
                            showDescription = false,
                            showSubTotal = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Green)
            .statusBarsPadding()
            .heightIn(min = 48.dp)
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = "Giỏ hàng",
            modifier = Modifier.weight(1f),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun EmptyCart(onBackClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(80.dp)
                .background(SoftGreen, RoundedCornerShape(40.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(36.dp)
            )
        }
        Text("Giỏ hàng đang trống", fontSize = 18.sp,
            fontWeight = FontWeight.Bold, color = TextDark)
        Text("Hãy chọn món ăn bạn yêu thích.", fontSize = 13.sp,
            color = TextGray, textAlign = TextAlign.Center)
        TextButton(
            onClick = onBackClick,
            colors = ButtonDefaults.textButtonColors(contentColor = Green)
        ) { Text("Khám phá thực đơn") }
    }
}

@Composable
private fun CartBottomBar(
    totalQuantity: Int,
    totalPrice: Double,
    enabled: Boolean,
    onOrderClick: () -> Unit
) {
    val priceText = formatPrice(totalPrice)

    Surface(color = Color.White, shadowElevation = 4.dp) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding()
        ) {
            HorizontalDivider(color = Border)
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Thanh toán khi kết thúc bữa ăn.",
                    fontSize = 12.sp,
                    color = TextGray
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tạm tính · $totalQuantity phần", fontSize = 12.sp,
                            color = TextGray)
                        Text(priceText, fontSize = 20.sp,
                            fontWeight = FontWeight.Bold, color = Green)
                    }
                    Button(
                        onClick = onOrderClick,
                        enabled = enabled,
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Gửi gọi món", fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
