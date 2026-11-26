package com.example.foodie

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodie.components.FoodMenuCard
import com.example.foodie.components.FoodieBottomBar
import com.example.foodie.components.FoodieConfirmDialog
import com.example.foodie.model.ChatMessage
import com.example.foodie.ui.theme.FoodieColors
import com.example.foodie.viewmodel.CartViewModel
import com.example.foodie.viewmodel.ChatViewModel
import com.example.foodie.viewmodel.DiningSessionViewModel

@Composable
fun AssistantScreen(
    chatViewModel: ChatViewModel,
    cartViewModel: CartViewModel,
    diningSessionViewModel: DiningSessionViewModel,
    onCartClick: () -> Unit,
    onTabSelected: (Int) -> Unit
) {

    val messages by
    chatViewModel.messages.collectAsState()

    val isLoading by
    chatViewModel.isLoading.collectAsState()

    val cartItems by
    cartViewModel.cartItems.collectAsState()

    val checkoutRequested by
    diningSessionViewModel.checkoutRequested.collectAsState()

    val totalQuantity = cartItems.sumOf {
        it.quantity
    }

    val totalPrice = cartItems.sumOf {
        it.food.price * it.quantity
    }

    val quantities = cartItems.associate {
        it.food.id to it.quantity
    }

    var input by rememberSaveable {
        mutableStateOf("")
    }

    var showClearDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val listState = rememberLazyListState()
    val isKeyboardVisible =
        WindowInsets.ime.getBottom(LocalDensity.current) > 0

    // Tự cuộn đến tin nhắn mới nhất
    LaunchedEffect(messages.size, isLoading) {

        val lastIndex = if (isLoading) {
            messages.size
        } else {
            messages.lastIndex
        }

        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoodieColors.Background)
            .imePadding()
            .navigationBarsPadding()
    ) {

        AssistantTopBar(
            onClearClick = {
                showClearDialog = true
            }
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement =
            Arrangement.spacedBy(12.dp)
        ) {

            items(
                items = messages,
                key = { it.id }
            ) { message ->

                Column(
                    verticalArrangement =
                    Arrangement.spacedBy(10.dp)
                ) {

                    ChatBubble(message)

                    // Hiển thị danh sách món AI gợi ý
                    if (message.suggestedItems.isNotEmpty()) {

                        Text(
                            text = "Món ăn gợi ý",
                            color = FoodieColors.Green,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )

                        message.suggestedItems.forEach { item ->

                            val food = item.food

                            FoodMenuCard(
                                food = food,

                                quantity = quantities[food.id]
                                    ?: 0,

                                onAddClick = {
                                    if (!checkoutRequested) {
                                        cartViewModel.addFood(food)
                                    }
                                },

                                onDecreaseClick = {
                                    if (!checkoutRequested) {
                                        cartViewModel.decreaseQuantity(
                                            food.id
                                        )
                                    }
                                },

                                showDescription = true
                            )
                        }
                    }
                }
            }

            if (isLoading) {

                item {
                    AssistantLoading()
                }
            }
        }

        // Chỉ hiển thị câu hỏi mẫu khi bắt đầu chat
        if (messages.size == 1) {

            QuickSuggestions(
                enabled = !isLoading,
                onClick = {
                    chatViewModel.sendMessage(it)
                }
            )
        }

        ChatInputBar(
            value = input,
            onValueChange = {
                input = it
            },
            enabled = !isLoading,
            onSendClick = {

                if (input.isNotBlank()) {
                    chatViewModel.sendMessage(input)
                    input = ""
                }
            }
        )

        // Giỏ hàng đồng bộ với màn hình thực đơn
        if (totalQuantity > 0 && !checkoutRequested) {

            AssistantCartBar(
                totalQuantity = totalQuantity,
                totalPrice = totalPrice,
                onCartClick = onCartClick
            )
        }

        if (!isKeyboardVisible) {
            FoodieBottomBar(
                selectedIndex = 2,
                onTabSelected = onTabSelected
            )
        }
    }

    if (showClearDialog) {

        FoodieConfirmDialog(
            title = "Xóa cuộc trò chuyện",
            message = "Bạn có muốn bắt đầu cuộc trò chuyện mới không?",
            confirmText = "Xóa",
            onConfirm = {

                showClearDialog = false
                chatViewModel.clearChat()
                input = ""
            },
            onDismiss = {
                showClearDialog = false
            }
        )
    }
}

// =========================================
// THANH TIÊU ĐỀ
// =========================================

@Composable
private fun AssistantTopBar(
    onClearClick: () -> Unit
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
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = Icons.Outlined.SmartToy,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = "Trợ lý Foodie",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Tư vấn món ăn thông minh",
                color = FoodieColors.SoftGreen,
                fontSize = 12.sp
            )
        }

        IconButton(
            onClick = onClearClick
        ) {

            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Xóa cuộc trò chuyện",
                tint = Color.White
            )
        }
    }
}

// =========================================
// TIN NHẮN
// =========================================

@Composable
private fun ChatBubble(
    message: ChatMessage
) {

    if (message.isUser) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {

            Surface(
                modifier = Modifier.widthIn(max = 280.dp),
                color = FoodieColors.Green,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 4.dp
                )
            ) {

                Text(
                    text = message.content,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

    } else {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement =
            Arrangement.spacedBy(8.dp)
        ) {

            Surface(
                modifier = Modifier.size(36.dp),
                color = FoodieColors.SoftGreen,
                shape = CircleShape
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Outlined.SmartToy,
                        contentDescription = null,
                        tint = FoodieColors.Green,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                color = Color.White,
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                border = BorderStroke(
                    1.dp,
                    FoodieColors.Border
                )
            ) {

                Text(
                    text = message.content,
                    color = FoodieColors.TextDark,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

// =========================================
// TRẠNG THÁI ĐANG PHẢN HỒI
// =========================================

@Composable
private fun AssistantLoading() {

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
        Arrangement.spacedBy(10.dp)
    ) {

        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = FoodieColors.Green,
            strokeWidth = 2.dp
        )

        Text(
            text =  "Foodie đang trả lời bạn...",
            color = FoodieColors.TextGray,
            fontSize = 13.sp
        )
    }
}

// =========================================
// CÂU HỎI GỢI Ý
// =========================================

@Composable
private fun QuickSuggestions(
    enabled: Boolean,
    onClick: (String) -> Unit
) {

    val suggestions = listOf(
        "Tôi có 500k",
        "Tôi thích ăn thịt",
        "Gợi ý món cho 2 người",
        "Có món nào ngon không?"
    )

    LazyRow(
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 4.dp
        ),
        horizontalArrangement =
        Arrangement.spacedBy(8.dp)
    ) {

        items(suggestions) { suggestion ->

            AssistChip(
                onClick = {
                    onClick(suggestion)
                },
                enabled = enabled,
                label = {
                    Text(
                        text = suggestion,
                        fontSize = 12.sp
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color.White,
                    labelColor = FoodieColors.Green
                )
            )
        }
    }
}

// =========================================
// Ô NHẬP TIN NHẮN
// =========================================

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    onSendClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(
                horizontal = 16.dp,
                vertical = 10.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
        Arrangement.spacedBy(8.dp)
    ) {

        OutlinedTextField(
            value = value,
            onValueChange = {
                if (it.length <= 500) {
                    onValueChange(it)
                }
            },
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    text = "Bạn muốn ăn gì hôm nay?",
                    fontSize = 13.sp
                )
            },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 14.sp
            ),
            maxLines = 3,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FoodieColors.Green,
                unfocusedBorderColor = FoodieColors.Border,
                focusedTextColor = FoodieColors.TextDark,
                unfocusedTextColor = FoodieColors.TextDark,
                focusedPlaceholderColor = FoodieColors.TextGray,
                unfocusedPlaceholderColor = FoodieColors.TextGray,
                cursorColor = FoodieColors.Green,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        FilledIconButton(
            onClick = onSendClick,
            enabled = enabled && value.isNotBlank(),
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = FoodieColors.Green,
                contentColor = Color.White,
                disabledContainerColor = FoodieColors.SoftGreen,
                disabledContentColor = FoodieColors.TextGray
            )
        ) {

            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Gửi tin nhắn",
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// =========================================
// THANH GIỎ HÀNG
// =========================================

@Composable
private fun AssistantCartBar(
    totalQuantity: Int,
    totalPrice: Double,
    onCartClick: () -> Unit
) {

    Surface(
        onClick = onCartClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        color = FoodieColors.Green,
        shape = RoundedCornerShape(12.dp)
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
            Arrangement.spacedBy(12.dp)
        ) {

            Icon(
                imageVector = Icons.Outlined.ShoppingCart,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "$totalQuantity món trong giỏ",
                    color = FoodieColors.SoftGreen,
                    fontSize = 12.sp
                )

                Text(
                    text = formatPrice(totalPrice),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Text(
                text = "Xem giỏ hàng",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}