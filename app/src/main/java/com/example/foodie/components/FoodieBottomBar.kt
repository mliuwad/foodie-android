package com.example.foodie.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodie.ui.theme.FoodieColors

@Composable
fun FoodieBottomBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val labels = listOf(
        "Thực đơn",
        "Đơn hàng",
        "Trợ lý"
    )

    val icons = listOf(
        Icons.Outlined.RestaurantMenu,
        Icons.Outlined.ReceiptLong,
        Icons.Outlined.SmartToy
    )

    Column {
        HorizontalDivider(
            color = FoodieColors.Border
        )

        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier.height(64.dp),
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            labels.forEachIndexed { index, label ->

                NavigationBarItem(
                    selected = selectedIndex == index,

                    onClick = {
                        onTabSelected(index)
                    },

                    icon = {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = label,
                            modifier = Modifier.size(23.dp)
                        )
                    },

                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp
                        )
                    },

                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FoodieColors.Green,
                        selectedTextColor = FoodieColors.Green,
                        indicatorColor = FoodieColors.SoftGreen,
                        unselectedIconColor = FoodieColors.TextGray,
                        unselectedTextColor = FoodieColors.TextGray
                    )
                )
            }
        }
    }
}