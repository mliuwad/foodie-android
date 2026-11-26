package com.example.foodie.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Payments
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
import com.example.foodie.components.FoodieConfirmDialog
import com.example.foodie.ui.theme.FoodieColors
import com.example.foodie.viewmodel.AdminOrderViewModel
import com.example.foodie.viewmodel.AuthViewModel
import com.example.foodie.viewmodel.BillViewModel

@Composable

fun AdminHomeScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    adminOrderViewModel: AdminOrderViewModel = viewModel(),
    billViewModel: BillViewModel = viewModel()
) {
    val admin by authViewModel.admin.collectAsState()

    var selectedIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(admin) {
        if (admin == null) {
            onLogout()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoodieColors.Background)
            .navigationBarsPadding()
    ) {

        // Top Bar
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

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Foodie Admin",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Xin chào, ${admin?.fullName ?: ""}",
                    color = FoodieColors.SoftGreen,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = {
                    showLogoutDialog = true
                }
            ) {

                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = "Đăng xuất",
                    tint = Color.White
                )
            }
        }

        // Nội dung từng tab
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            if (admin != null) {

                when (selectedIndex) {

                    0 -> AdminOrderScreen(
                        adminOrderViewModel = adminOrderViewModel
                    )

                    1 -> AdminPaymentScreen(
                        billViewModel = billViewModel
                    )
                }
            }
        }

        AdminBottomBar(
            selectedIndex = selectedIndex,
            onTabSelected = {
                selectedIndex = it
            }
        )
    }

    if (showLogoutDialog) {

        FoodieConfirmDialog(
            title = "Đăng xuất",
            message = "Bạn có muốn đăng xuất khỏi hệ thống quản trị không?",
            confirmText = "Đăng xuất",
            onConfirm = {

                showLogoutDialog = false

                adminOrderViewModel.stopObservingOrders()

                billViewModel.stopObserving()

                authViewModel.logout()
            },
            onDismiss = {
                showLogoutDialog = false
            }
        )
    }
}

@Composable
private fun AdminPlaceholder(
    title: String,
    isOrder: Boolean
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Icon(
            imageVector =
            if (isOrder)
                Icons.Outlined.ReceiptLong
            else
                Icons.Outlined.Payments,

            contentDescription = null,
            tint = FoodieColors.Green,
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = title,
            color = FoodieColors.TextDark,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Đang phát triển chức năng",
            color = FoodieColors.TextGray,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun AdminBottomBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {

    val labels = listOf(
        "Đơn hàng",
        "Thanh toán"
    )

    val icons = listOf(
        Icons.Outlined.ReceiptLong,
        Icons.Outlined.Payments
    )

    Column {

        HorizontalDivider(
            color = FoodieColors.Border
        )

        NavigationBar(
            modifier = Modifier.height(64.dp),
            containerColor = Color.White,
            tonalElevation = 0.dp,
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