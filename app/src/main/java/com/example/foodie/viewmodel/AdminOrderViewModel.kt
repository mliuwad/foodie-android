package com.example.foodie.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.foodie.utils.NotificationHelper
import androidx.lifecycle.viewModelScope
import com.example.foodie.model.Order
import com.example.foodie.model.OrderStatus
import com.example.foodie.repository.OrderRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminOrderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = OrderRepository()

    private val _orders =
        MutableStateFlow<List<Order>>(emptyList())

    val orders = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    private val _loadError =
        MutableStateFlow<String?>(null)

    val loadError = _loadError.asStateFlow()

    private val _error =
        MutableStateFlow<String?>(null)

    val error = _error.asStateFlow()

    private var seenOrders: MutableSet<String>? = null

    private var ordersJob: Job? = null

    fun observeOrders() {

        if (ordersJob?.isActive == true) return

        _isLoading.value = true
        _loadError.value = null

        ordersJob = viewModelScope.launch {

            try {

                repository.observeAllOrders()
                    .collect { result ->

                        val seen = seenOrders
                        if (seen == null) {
                            seenOrders = result.map { it.orderId }.toMutableSet()
                        } else {
                            result.forEach { order ->
                                if (seen.add(order.orderId) && order.status == "PENDING") {
                                    NotificationHelper.showNotification(
                                        getApplication(), "admin-order:${order.orderId}".hashCode(),
                                        "Có đơn hàng mới", "Bàn ${order.tableId} vừa gửi đơn gọi món."
                                    )
                                }
                            }
                        }
                        _orders.value = result
                        _isLoading.value = false
                    }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                _loadError.value = e.message
                    ?: "Không thể tải đơn hàng"

                _isLoading.value = false
            }
        }
    }

    fun updateOrderStatus(
        orderId: String,
        newStatus: OrderStatus
    ) {

        if (_isUpdating.value) return

        _isUpdating.value = true
        _error.value = null

        viewModelScope.launch {

            try {

                repository.updateOrderStatus(
                    orderId = orderId,
                    newStatus = newStatus
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                _error.value = e.message
                    ?: "Không thể cập nhật đơn hàng"

            } finally {

                _isUpdating.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun stopObservingOrders() {

        ordersJob?.cancel()
        ordersJob = null
        seenOrders = null

        _orders.value = emptyList()
        _isLoading.value = false
        _loadError.value = null
        _error.value = null
    }
}