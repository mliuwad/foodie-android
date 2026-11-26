package com.example.foodie.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.foodie.utils.NotificationHelper
import androidx.lifecycle.viewModelScope
import com.example.foodie.model.Bill
import com.example.foodie.model.PaymentMethod
import com.example.foodie.repository.BillRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BillRepository()

    private val _bill = MutableStateFlow<Bill?>(null)
    val bill = _bill.asStateFlow()

    private val _bills =
        MutableStateFlow<List<Bill>>(emptyList())

    val bills = _bills.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isPaying = MutableStateFlow(false)
    val isPaying = _isPaying.asStateFlow()

    private val _error =
        MutableStateFlow<String?>(null)

    val error = _error.asStateFlow()

    private val _paymentSuccess =
        MutableStateFlow(false)

    val paymentSuccess = _paymentSuccess.asStateFlow()

    private var observingBillId: String? = null
    private var previousPaymentStatus: String? = null
    private var seenBills: MutableSet<String>? = null

    private var billJob: Job? = null
    private var billsJob: Job? = null

    fun observeBill(sessionId: String) {
        if (observingBillId == sessionId && billJob?.isActive == true) return
        if (observingBillId != sessionId) previousPaymentStatus = null
        observingBillId = sessionId

        billJob?.cancel()

        _bill.value = null
        _error.value = null
        _isLoading.value = true

        billJob = viewModelScope.launch {

            try {

                repository.observeBill(sessionId)
                    .collect {

                        if (previousPaymentStatus == "UNPAID" && it?.paymentStatus == "PAID") {
                            NotificationHelper.showNotification(
                                getApplication(), "paid:$sessionId".hashCode(),
                                "Thanh toán thành công", "Cảm ơn bạn đã dùng bữa tại Foodie."
                            )
                        }
                        previousPaymentStatus = it?.paymentStatus
                        _bill.value = it
                        _isLoading.value = false
                    }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                _error.value = e.message
                    ?: "Không thể tải hóa đơn"

                _isLoading.value = false
            }
        }
    }

    fun observeUnpaidBills() {

        if (billsJob?.isActive == true) return

        _isLoading.value = true
        _error.value = null

        billsJob = viewModelScope.launch {

            try {

                repository.observeUnpaidBills()
                    .collect {

                        val seen = seenBills
                        if (seen == null) {
                            seenBills = it.map { bill -> bill.billId }.toMutableSet()
                        } else {
                            it.forEach { bill ->
                                if (seen.add(bill.billId)) NotificationHelper.showNotification(
                                    getApplication(), "admin-bill:${bill.billId}".hashCode(),
                                    "Có bàn yêu cầu thanh toán", "Vui lòng kiểm tra hóa đơn bàn ${bill.tableId}."
                                )
                            }
                        }
                        _bills.value = it
                        _isLoading.value = false
                    }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                _error.value = e.message
                    ?: "Không thể tải danh sách hóa đơn"

                _isLoading.value = false
            }
        }
    }

    fun confirmPayment(
        billId: String,
        paymentMethod: PaymentMethod
    ) {

        if (_isPaying.value) return

        _isPaying.value = true
        _error.value = null
        _paymentSuccess.value = false

        viewModelScope.launch {

            try {

                repository.confirmPayment(
                    billId,
                    paymentMethod
                )

                _paymentSuccess.value = true

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                _error.value = e.message
                    ?: "Không thể xác nhận thanh toán"

            } finally {

                _isPaying.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearPaymentSuccess() {
        _paymentSuccess.value = false
    }

    fun stopObserving() {

        billJob?.cancel()
        billsJob?.cancel()

        observingBillId = null
        previousPaymentStatus = null
        seenBills = null
        billJob = null
        billsJob = null

        _bill.value = null
        _bills.value = emptyList()
        _isLoading.value = false
        _error.value = null
        _paymentSuccess.value = false
    }
}