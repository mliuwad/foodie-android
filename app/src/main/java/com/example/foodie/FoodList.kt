package com.example.foodie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodie.components.FoodMenuCard
import com.example.foodie.components.FoodieBottomBar
import com.example.foodie.ui.theme.FoodieColors.Green
import com.example.foodie.ui.theme.FoodieColors.Background
import com.example.foodie.ui.theme.FoodieColors.TextDark
import com.example.foodie.ui.theme.FoodieColors.TextGray
import com.example.foodie.ui.theme.FoodieColors.Border
import com.example.foodie.ui.theme.FoodieColors.SoftGreen
import com.example.foodie.viewmodel.CartViewModel
import com.example.foodie.viewmodel.FoodViewModel
import com.example.foodie.viewmodel.DiningSessionViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FoodList(
    modifier: Modifier = Modifier,
    onCartClick: () -> Unit,
    foodViewModel: FoodViewModel = viewModel(),
    cartViewModel: CartViewModel,
    diningSessionViewModel: DiningSessionViewModel,
    onTabSelected: (Int) -> Unit
) {

    val selectedTable by
    diningSessionViewModel.selectedTable.collectAsState()

    val tableName = selectedTable?.tableName
        ?: "Chưa chọn bàn"

    val foods by foodViewModel.foods.collectAsState()
    val isLoading by foodViewModel.isLoading.collectAsState()
    val error by foodViewModel.error.collectAsState()

    val cartItems by cartViewModel.cartItems.collectAsState()

    val totalQuantity = cartItems.sumOf {
        it.quantity
    }

    val totalPrice = cartItems.sumOf {
        it.quantity * it.food.price
    }

    var search by rememberSaveable {
        mutableStateOf("")
    }

    var category by rememberSaveable {
        mutableStateOf("Tất cả")
    }

    val filteredFoods = remember(
        foods,
        search,
        category
    ) {
        foods.filter { food ->
            food.available &&
                    food.name.contains(
                        search.trim(),
                        ignoreCase = true
                    ) &&
                    (
                            category == "Tất cả" ||
                                    food.category == category
                            )
        }
    }

    val quantities = remember(cartItems) {
        cartItems.associate {
            it.food.id to it.quantity
        }
    }

    val refreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            if (!isLoading) {
                foodViewModel.loadFoods()
            }
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .navigationBarsPadding()
    ) {

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Green)
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

            // Tiêu đề bên trái
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Foodie",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Thực đơn hôm nay",
                    color = SoftGreen,
                    fontSize = 12.sp
                )
            }

            // Thông tin bàn bên phải
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
                        text = if (selectedTable != null)
                            "Đang phục vụ"
                        else
                            "Chưa có phiên ăn",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Tìm kiếm và lọc món
        MenuHeader(
            search = search,
            onSearchChange = {
                search = it
            },
            category = category,
            onCategoryChange = {
                category = it
            },
            count = filteredFoods.size
        )

        HorizontalDivider(color = Border)

        // Danh sách món ăn
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pullRefresh(refreshState)
        ) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
                verticalArrangement =
                Arrangement.spacedBy(8.dp)
            ) {

                when {

                    isLoading && foods.isEmpty() -> {

                        item {
                            MenuMessage(
                                "Đang tải thực đơn..."
                            )
                        }
                    }

                    error != null -> {

                        item {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment =
                                Alignment.CenterHorizontally,
                                verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                            ) {

                                Text(
                                    text = error
                                        ?: "Có lỗi xảy ra",
                                    color = Color(0xFFDC2626),
                                    fontSize = 14.sp
                                )

                                TextButton(
                                    onClick = {
                                        foodViewModel.loadFoods()
                                    },
                                    colors =
                                    ButtonDefaults.textButtonColors(
                                        contentColor = Green
                                    )
                                ) {
                                    Text("Thử lại")
                                }
                            }
                        }
                    }

                    filteredFoods.isEmpty() -> {

                        item {
                            MenuMessage(
                                "Không tìm thấy món ăn"
                            )
                        }
                    }

                    else -> {

                        items(
                            items = filteredFoods,
                            key = { it.id }
                        ) { food ->

                            FoodMenuCard(
                                food = food,

                                quantity =
                                quantities[food.id] ?: 0,

                                onAddClick = {
                                    cartViewModel.addFood(food)
                                },

                                onDecreaseClick = {
                                    cartViewModel.decreaseQuantity(
                                        food.id
                                    )
                                }
                            )
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isLoading,
                state = refreshState,
                modifier = Modifier.align(
                    Alignment.TopCenter
                ),
                contentColor = Green,
                backgroundColor = Color.White
            )
        }

        // Thanh giỏ hàng
        if (totalQuantity > 0) {

            CartSummaryBar(
                totalQuantity = totalQuantity,
                totalPrice = totalPrice,
                onCartClick = onCartClick
            )
        }

        // Bottom Navigation dùng chung
        FoodieBottomBar(
            selectedIndex = 0,
            onTabSelected = onTabSelected
        )
    }
}

// ==============================
// TÌM KIẾM VÀ DANH MỤC
// ==============================

@Composable
private fun MenuHeader(
    search: String,
    onSearchChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    count: Int
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        verticalArrangement =
        Arrangement.spacedBy(6.dp)
    ) {

        OutlinedTextField(
            value = search,
            onValueChange = onSearchChange,

            modifier = Modifier.fillMaxWidth(),

            singleLine = true,

            placeholder = {
                Text(
                    text = "Tìm món ăn, đồ uống...",
                    fontSize = 13.sp,
                    color = TextGray
                )
            },

            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Tìm kiếm",
                    tint = TextGray
                )
            },

            trailingIcon = {

                if (search.isNotEmpty()) {

                    IconButton(
                        onClick = {
                            onSearchChange("")
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Xóa tìm kiếm",
                            tint = TextGray
                        )
                    }
                }
            },

            shape = RoundedCornerShape(12.dp),

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Green,
                unfocusedBorderColor = Border,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Green
            )
        )

        // Danh mục món ăn
        LazyRow(
            horizontalArrangement =
            Arrangement.spacedBy(8.dp)
        ) {

            items(
                listOf(
                    "Tất cả",
                    "Món chính",
                    "Ăn nhẹ",
                    "Đồ uống"
                )
            ) { item ->

                FilterChip(
                    selected = category == item,

                    onClick = {
                        onCategoryChange(item)
                    },

                    label = {
                        Text(
                            text = item,
                            fontSize = 12.sp
                        )
                    },

                    shape = RoundedCornerShape(8.dp),

                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        labelColor = TextGray,
                        selectedContainerColor = Green,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Tiêu đề danh sách
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
            Arrangement.SpaceBetween,
            verticalAlignment =
            Alignment.CenterVertically
        ) {

            Text(
                text = "Gợi ý cho bạn",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Text(
                text = "$count món",
                fontSize = 12.sp,
                color = TextGray
            )
        }
    }
}

// ==============================
// THÔNG BÁO DANH SÁCH
// ==============================

@Composable
private fun MenuMessage(
    message: String
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = message,
            color = TextGray,
            fontSize = 14.sp
        )
    }
}

// ==============================
// THANH TỔNG TIỀN GIỎ HÀNG
// ==============================

@Composable
private fun CartSummaryBar(
    totalQuantity: Int,
    totalPrice: Double,
    onCartClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        shape = RoundedCornerShape(12.dp),
        color = Green
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 4.dp
            ),
            verticalAlignment =
            Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Giỏ hàng · $totalQuantity món",
                    color = Color.White,
                    fontSize = 12.sp
                )

                Text(
                    text = formatPrice(totalPrice),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = onCartClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White
                )
            ) {

                Text(
                    text = "Xem giỏ hàng →",
                    fontSize = 13.sp
                )
            }
        }
    }
}