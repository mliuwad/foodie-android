
package com.example.foodie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodie.model.Food
import com.example.foodie.ui.theme.FoodieTheme
import com.example.foodie.viewmodel.FoodViewModel
import java.text.NumberFormat
import java.util.Locale

private val Green = Color(0xFF1E6748)
private val Background = Color(0xFFFAFAF8)
private val TextDark = Color(0xFF202A24)
private val TextGray = Color(0xFF7B837C)

@Composable
fun FoodDetailScreen(
    foodId: Int,
    onBackClick: () -> Unit,
    foodViewModel: FoodViewModel = viewModel()
) {
    val foods by foodViewModel.foods.collectAsState()
    val isLoading by foodViewModel.isLoading.collectAsState()
    val error by foodViewModel.error.collectAsState()

    val food = foods.find { it.id == foodId }

    FoodDetailContent(
        food = food,
        isLoading = isLoading,
        error = error,
        onBackClick = onBackClick,
        onRetry = { foodViewModel.loadFoods() }
    )
}

@Composable
private fun FoodDetailContent(
    food: Food?,
    isLoading: Boolean,
    error: String?,
    onBackClick: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TextButton(onClick = onBackClick) {
            Text(
                text = "← Quay lại",
                color = Green
            )
        }

        Text(
            text = "Chi tiết món ăn",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green)
                }
            }

            error != null -> {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )

                Button(onClick = onRetry) {
                    Text("Thử lại")
                }
            }

            food == null -> {
                Text(
                    text = "Không tìm thấy món ăn",
                    color = TextGray
                )
            }

            else -> {
                FoodDetailInfo(food = food)
            }
        }
    }
}

@Composable
private fun FoodDetailInfo(food: Food) {
    var quantity by remember(food.id) {
        mutableIntStateOf(1)
    }

    val priceFormatter = NumberFormat.getNumberInstance(
        Locale("vi", "VN")
    )

    val emoji = when (food.id) {
        1 -> "🍗"
        2 -> "🧋"
        3 -> "🥖"
        4 -> "🍕"
        5 -> "🍔"
        else -> "🍽️"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .background(
                        Color(0xFFFFF3E4),
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 80.sp,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = food.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Text(
                text = food.description,
                fontSize = 14.sp,
                color = TextGray
            )

            Text(
                text = "${priceFormatter.format(food.price)}đ",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Green
            )

            Text(
                text = if (food.available) {
                    "● Còn hàng"
                } else {
                    "● Tạm hết hàng"
                },
                color = if (food.available) {
                    Green
                } else {
                    MaterialTheme.colorScheme.error
                },
                fontSize = 13.sp
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Số lượng",
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (quantity > 1) quantity--
                        },
                        enabled = quantity > 1
                    ) {
                        Text("−")
                    }

                    Text(
                        text = "$quantity",
                        modifier = Modifier.padding(
                            horizontal = 16.dp
                        ),
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = { quantity++ }
                    ) {
                        Text("+")
                    }
                }
            }

            Button(
                onClick = {
                    // TODO: Kết nối CartViewModel sau
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = false,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green
                )
            ) {
                Text(
                    text = "Thêm vào giỏ hàng · ${
                        priceFormatter.format(food.price * quantity)
                    }đ"
                )
            }

            Text(
                text = "Chức năng giỏ hàng sẽ được kết nối ở bước tiếp theo.",
                fontSize = 12.sp,
                color = TextGray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FoodDetailScreenPreview() {
    val sampleFood = Food(
        id = 4,
        name = "Pizza",
        description = "Phô mai tan chảy",
        price = 89000.0,
        imageUrl = "",
        available = true,
        category = "Ăn nhẹ"
    )

    FoodieTheme {
        FoodDetailContent(
            food = sampleFood,
            isLoading = false,
            error = null,
            onBackClick = {},
            onRetry = {}
        )
    }
}
