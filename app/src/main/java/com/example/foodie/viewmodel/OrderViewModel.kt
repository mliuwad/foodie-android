package com.example.foodie.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.foodie.utils.NotificationHelper
import androidx.lifecycle.viewModelScope
import com.example.foodie.CartItem
import com.example.foodie.model.Order
import com.example.foodie.repository.OrderRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = OrderRepository()

    // ==============================
    // GỬI ĐƠN GỌI MÓN
    // ==============================

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _createdOrderId =
        MutableStateFlow<String?>(null)

    val createdOrderId = _createdOrderId.asStateFlow()

    fun createOrder(
        sessionId: String,
        tableId: String,
        cartItems: List<CartItem>,
        note: String = ""
    ) {
        if (_isLoading.value ||
            _createdOrderId.value != null
        ) return

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                _createdOrderId.value = repository.createOrder(
                    sessionId = sessionId,
                    tableId = tableId,
                    cartItems = cartItems,
                    note = note
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _error.value = e.message
                    ?: "Không thể gửi đơn gọi món"

            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearCreatedOrder() {
        _createdOrderId.value = null
    }

    // ==============================
    // DANH SÁCH ĐƠN HÀNG
    // ==============================

    private val _orders =
        MutableStateFlow<List<Order>>(emptyList())

    val orders = _orders.asStateFlow()

    private val _isOrdersLoading =
        MutableStateFlow(false)

    val isOrdersLoading =
        _isOrdersLoading.asStateFlow()

    private val _ordersError =
        MutableStateFlow<String?>(null)

    val ordersError = _ordersError.asStateFlow()

    private var ordersJob: Job? = null

    private var previousStatuses: Map<String, String>? = null

    private var observingSessionId: String? = null

    fun observeOrders(
        sessionId: String,
        forceRefresh: Boolean = false
    ) {

        if (!forceRefresh &&
            observingSessionId == sessionId &&
            ordersJob?.isActive == true
        ) return

        ordersJob?.cancel()

        if (observingSessionId != sessionId) {
            previousStatuses = null
            _orders.value = emptyList()
        }

        observingSessionId = sessionId

        _isOrdersLoading.value = true
        _ordersError.value = null

        ordersJob = viewModelScope.launch {

            try {
                repository.observeOrders(sessionId)
                    .collect { orders ->

                        val previous = previousStatuses
                        previousStatuses = orders.associate { it.orderId to it.status }
                        if (previous != null) {
                            orders.forEach { order ->
                                val old = previous[order.orderId]
                                if (old != null && old != order.status) {
                                    val message = when (order.status) {
                                        "CONFIRMED" -> "Nhà hàng đã xác nhận đơn của bạn."
                                        "PREPARING" -> "Món ăn đang được chuẩn bị."
                                        "SERVED" -> "Đơn hàng đã được phục vụ."
                                        "CANCELLED" -> "Đơn hàng đã bị hủy."
                                        else -> null
                                    }
                                    if (message != null) NotificationHelper.showNotification(
                                        getApplication(), "order:${order.orderId}".hashCode(),
                                        "Foodie", message
                                    )
                                }
                            }
                        }
                        _orders.value = orders
                        _isOrdersLoading.value = false
                    }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                _ordersError.value = e.message
                    ?: "Không thể tải đơn hàng"

                _isOrdersLoading.value = false
            }
        }
    }

    fun stopObservingOrders() {

        ordersJob?.cancel()
        ordersJob = null

        observingSessionId = null
        previousStatuses = null

        _orders.value = emptyList()
        _ordersError.value = null
        _isOrdersLoading.value = false
    }
}