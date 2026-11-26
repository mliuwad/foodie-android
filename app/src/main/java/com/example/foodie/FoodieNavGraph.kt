package com.example.foodie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foodie.admin.AdminHomeScreen
import com.example.foodie.admin.AdminLoginScreen
import com.example.foodie.components.FoodieConfirmDialog
import com.example.foodie.components.FoodieLoadingDialog
import com.example.foodie.components.FoodieMessageDialog
import com.example.foodie.ui.theme.FoodieColors
import com.example.foodie.viewmodel.AuthViewModel
import com.example.foodie.viewmodel.AdminOrderViewModel
import com.example.foodie.viewmodel.BillViewModel
import com.example.foodie.viewmodel.ChatViewModel
import com.example.foodie.viewmodel.CartViewModel
import com.example.foodie.viewmodel.DiningSessionViewModel
import com.example.foodie.viewmodel.OrderViewModel

@Composable
fun FoodieNavGraph(
    modifier: Modifier = Modifier
) {

    val navController = rememberNavController()

    val cartViewModel: CartViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()

    val diningSessionViewModel:
            DiningSessionViewModel = viewModel()

    val orderViewModel: OrderViewModel = viewModel()

    val sessionId by
    diningSessionViewModel.sessionId.collectAsState()

    val selectedTable by
    diningSessionViewModel.selectedTable.collectAsState()

    val isCreatingOrder by
    orderViewModel.isLoading.collectAsState()

    val orderError by
    orderViewModel.error.collectAsState()

    val createdOrderId by
    orderViewModel.createdOrderId.collectAsState()

    var showConfirmOrder by rememberSaveable {
        mutableStateOf(false)
    }

    var showOrderSuccess by rememberSaveable {
        mutableStateOf(false)
    }

    var showSessionError by rememberSaveable {
        mutableStateOf(false)
    }
    val authViewModel: AuthViewModel = viewModel()
    val isRequestingCheckout by
    diningSessionViewModel
        .isRequestingCheckout.collectAsState()

    val checkoutRequested by
    diningSessionViewModel
        .checkoutRequested.collectAsState()

    val checkoutError by
    diningSessionViewModel
        .checkoutError.collectAsState()

    var showCheckoutConfirm by rememberSaveable {
        mutableStateOf(false)
    }

    var showCheckoutSuccess by rememberSaveable {
        mutableStateOf(false)
    }
    val billViewModel: BillViewModel = viewModel()
    val isRestoringSession by
    diningSessionViewModel
        .isRestoringSession.collectAsState()

    val restoreError by
    diningSessionViewModel
        .restoreError.collectAsState()
    // ==============================
    // CHUYỂN TAB
    // ==============================

    val onTabSelected: (Int) -> Unit = tabSelection@{ index ->

        if (isRequestingCheckout || isCreatingOrder || sessionId == null) {
            return@tabSelection
        }

        if (checkoutRequested && index == 0) {
            return@tabSelection
        }

        val route = when (index) {

            0 -> Screen.Home.route

            1 -> Screen.Orders.route

            2 -> Screen.Assistant.route

            else -> return@tabSelection
        }

        if (navController.currentDestination?.route != route) {

            navController.navigate(route) {

                popUpTo(Screen.Home.route) {
                    inclusive = false
                    saveState = true
                }

                launchSingleTop = true
                restoreState = true
            }
        }
    }
    if (isRestoringSession || restoreError != null) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FoodieColors.Background),
            contentAlignment = Alignment.Center
        ) {

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment =
                Alignment.CenterHorizontally,
                verticalArrangement =
                Arrangement.spacedBy(16.dp)
            ) {

                if (isRestoringSession) {

                    CircularProgressIndicator(
                        color = FoodieColors.Green
                    )

                    Text(
                        text = "Đang khôi phục phiên ăn...",
                        color = FoodieColors.TextGray
                    )

                } else {

                    Text(
                        text = "Không thể khôi phục phiên ăn",
                        color = FoodieColors.TextDark
                    )

                    Text(
                        text = restoreError ?: "",
                        color = FoodieColors.TextGray
                    )

                    TextButton(
                        onClick = {
                            diningSessionViewModel.restoreSession()
                        }
                    ) {

                        Text(
                            "Thử lại",
                            color = FoodieColors.Green
                        )
                    }
                }
            }
        }

        return
    }
    // ==============================
    // NAVIGATION
    // ==============================
    LaunchedEffect(sessionId) {
        val id = sessionId
        if (id != null) orderViewModel.observeOrders(id)
        else orderViewModel.stopObservingOrders()
    }

    LaunchedEffect(sessionId, checkoutRequested) {
        val id = sessionId
        if (id != null && checkoutRequested) billViewModel.observeBill(id)
    }

    val initialRoute = remember {

        when {

            sessionId == null ->
                Screen.Tables.route

            checkoutRequested ->
                Screen.Bill.route

            else ->
                Screen.Home.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = initialRoute,
        modifier = modifier
    ) {

        // Chọn bàn
        composable(Screen.Tables.route) {

            TableScreen(
                diningSessionViewModel =
                diningSessionViewModel,

                onSessionStarted = {
                    chatViewModel.clearChat()

                    cartViewModel.clearCart()

                    diningSessionViewModel.sessionId.value?.let { orderViewModel.observeOrders(it) }

                    navController.navigate(
                        Screen.Home.route
                    ) {
                        popUpTo(Screen.Tables.route) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                },

                onAdminClick = {

                    if (diningSessionViewModel.sessionId.value == null) {

                        navController.navigate(
                            Screen.AdminLogin.route
                        ) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        // Thực đơn
        composable(Screen.Home.route) {
            LaunchedEffect(checkoutRequested) {

                if (checkoutRequested) {

                    navController.navigate(Screen.Bill.route) {

                        popUpTo(Screen.Home.route) {
                            inclusive = false
                        }

                        launchSingleTop = true
                    }
                }
            }

            FoodList(
                cartViewModel = cartViewModel,

                diningSessionViewModel =
                diningSessionViewModel,

                onCartClick = {
                    if (sessionId != null && !checkoutRequested &&
                        !isRequestingCheckout && !isCreatingOrder
                    ) {
                        navController.navigate(Screen.Cart.route) {
                            launchSingleTop = true
                        }
                    }
                },

                onTabSelected = onTabSelected
            )
        }

        // Giỏ hàng
        composable(Screen.Cart.route) {

            CartScreen(
                cartViewModel = cartViewModel,

                onBackClick = {
                    navController.popBackStack()
                },

                onOrderClick = {

                    if (
                        !isCreatingOrder &&
                        !isRequestingCheckout &&
                        !checkoutRequested &&
                        cartViewModel.cartItems.value.isNotEmpty()
                    ) {

                        showConfirmOrder = true
                    }
                }
            )
        }

        // Đơn hàng
        composable(Screen.Orders.route) {

            OrderScreen(
                orderViewModel = orderViewModel,

                diningSessionViewModel =
                diningSessionViewModel,

                onTabSelected = onTabSelected,

                onCheckoutClick = {

                    if (checkoutRequested) {

                        navController.navigate(Screen.Bill.route) {
                            launchSingleTop = true
                        }

                    } else if (
                        !isRequestingCheckout &&
                        !isCreatingOrder
                    ) {

                        showCheckoutConfirm = true
                    }
                }
            )
        }

        // Trợ lý
        composable(Screen.Assistant.route) {

            AssistantScreen(
                chatViewModel = chatViewModel,
                cartViewModel = cartViewModel,
                diningSessionViewModel = diningSessionViewModel,
                onCartClick = {
                    if (sessionId != null && !checkoutRequested &&
                        !isRequestingCheckout && !isCreatingOrder
                    ) {
                        navController.navigate(Screen.Cart.route) {
                            launchSingleTop = true
                        }
                    }
                },
                onTabSelected = onTabSelected
            )
        }
        composable(Screen.AdminLogin.route) {

            AdminLoginScreen(
                authViewModel = authViewModel,

                onBackClick = {
                    navController.popBackStack()
                },

                onLoginSuccess = {

                    navController.navigate(
                        Screen.AdminHome.route
                    ) {

                        popUpTo(Screen.AdminLogin.route) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.AdminHome.route) {

            val adminOrders: AdminOrderViewModel = viewModel()
            val adminBills: BillViewModel = viewModel()
            LaunchedEffect(Unit) {
                adminOrders.observeOrders()
                adminBills.observeUnpaidBills()
            }
            DisposableEffect(Unit) {
                onDispose {
                    adminOrders.stopObservingOrders()
                    adminBills.stopObserving()
                }
            }
            AdminHomeScreen(
                authViewModel = authViewModel,
                adminOrderViewModel = adminOrders,
                billViewModel = adminBills,

                onLogout = {

                    navController.navigate(
                        Screen.Tables.route
                    ) {

                        popUpTo(Screen.Tables.route) {
                            inclusive = false
                        }

                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.Bill.route) {

            val currentSessionId = sessionId

            if (currentSessionId != null) {

                BillScreen(

                    sessionId = currentSessionId,

                    billViewModel = billViewModel,

                    onBackClick = {

                        if (!navController.popBackStack()) {

                            navController.navigate(Screen.Orders.route) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onPaymentCompleted = {
                        chatViewModel.clearChat()

                        billViewModel.stopObserving()

                        orderViewModel.stopObservingOrders()

                        cartViewModel.clearCart()

                        diningSessionViewModel.clearSession()

                        navController.navigate(Screen.Tables.route) {

                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }

                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }

    // ==============================
    // XÁC NHẬN GỌI MÓN
    // ==============================

    if (showConfirmOrder) {

        FoodieConfirmDialog(
            title = "Xác nhận gọi món",

            message = "Bạn có muốn gửi các món đã chọn đến nhà bếp không?",

            confirmText = "Gửi gọi món",

            onConfirm = {

                showConfirmOrder = false

                val currentSessionId = sessionId

                val currentTable = selectedTable

                if (currentSessionId != null &&
                    currentTable != null && !checkoutRequested &&
                    !isRequestingCheckout && !isCreatingOrder &&
                    cartViewModel.cartItems.value.isNotEmpty()
                ) {

                    orderViewModel.createOrder(
                        sessionId = currentSessionId,
                        tableId = currentTable.tableId,
                        cartItems =
                        cartViewModel.cartItems.value
                    )

                } else {
                    showSessionError = true
                }
            },

            onDismiss = {
                showConfirmOrder = false
            }
        )
    }

    // ==============================
    // LOADING
    // ==============================

    if (isCreatingOrder) {

        FoodieLoadingDialog(
            message = "Đang gửi gọi món..."
        )
    }

    // ==============================
    // GỌI MÓN THÀNH CÔNG
    // ==============================

    LaunchedEffect(createdOrderId) {

        if (createdOrderId != null) {

            cartViewModel.clearCart()

            orderViewModel.clearCreatedOrder()

            navController.popBackStack(
                Screen.Home.route,
                inclusive = false
            )

            showOrderSuccess = true
        }
    }

    if (showOrderSuccess) {

        FoodieMessageDialog(
            title = "Gọi món thành công",

            message = "Đơn gọi món đã được gửi. Bạn có thể theo dõi trạng thái tại mục Đơn hàng.",

            isSuccess = true,

            buttonText = "Xem đơn hàng",

            onDismiss = {

                showOrderSuccess = false

                onTabSelected(1)
            }
        )
    }

    // ==============================
    // THÔNG BÁO LỖI
    // ==============================

    if (showSessionError) {

        FoodieMessageDialog(
            title = "Không thể gọi món",

            message = "Không tìm thấy phiên ăn hiện tại.",

            isSuccess = false,

            onDismiss = {
                showSessionError = false
            }
        )
    }

    orderError?.let { message ->

        FoodieMessageDialog(
            title = "Không thể gọi món",

            message = message,

            isSuccess = false,

            onDismiss = {
                orderViewModel.clearError()
            }
        )
    }
    if (showCheckoutConfirm) {

        FoodieConfirmDialog(

            title = "Yêu cầu thanh toán",

            message = "Bạn có muốn yêu cầu thanh toán " +
                    "phiên ăn hiện tại không? " +
                    "Sau khi xác nhận, bạn sẽ không thể gọi thêm món.",

            confirmText = "Xác nhận",

            onConfirm = {

                showCheckoutConfirm = false

                if (sessionId != null && !checkoutRequested &&
                    !isRequestingCheckout && !isCreatingOrder
                ) {
                    diningSessionViewModel.requestCheckout()
                }
            },

            onDismiss = {

                showCheckoutConfirm = false
            }
        )
    }

    if (isRequestingCheckout) {

        FoodieLoadingDialog(
            message = "Đang gửi yêu cầu thanh toán..."
        )
    }

    LaunchedEffect(checkoutRequested) {

        if (checkoutRequested) {

            cartViewModel.clearCart()

            if (initialRoute != Screen.Bill.route) {

                showCheckoutSuccess = true
            }
        }
    }

    if (showCheckoutSuccess) {

        FoodieMessageDialog(

            title = "Đã gửi yêu cầu thanh toán",

            message = "Vui lòng kiểm tra hóa đơn và thực hiện thanh toán.",

            isSuccess = true,

            buttonText = "Xem hóa đơn",

            onDismiss = {

                showCheckoutSuccess = false

                navController.navigate(Screen.Bill.route) {
                    launchSingleTop = true
                }
            }
        )
    }
    checkoutError?.let { message ->

        FoodieMessageDialog(

            title = "Không thể thanh toán",

            message = message,

            isSuccess = false,

            onDismiss = {

                diningSessionViewModel.clearCheckoutError()
            }
        )
    }

}
