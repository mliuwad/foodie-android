package com.example.foodie.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodie.model.Table
import com.example.foodie.repository.DiningSessionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiningSessionViewModel : ViewModel() {

    private val repository = DiningSessionRepository()

    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId = _sessionId.asStateFlow()

    private val _selectedTable = MutableStateFlow<Table?>(null)
    val selectedTable = _selectedTable.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isRequestingCheckout =
        MutableStateFlow(false)

    val isRequestingCheckout =
        _isRequestingCheckout.asStateFlow()

    private val _checkoutRequested =
        MutableStateFlow(false)

    val checkoutRequested =
        _checkoutRequested.asStateFlow()

    private val _checkoutError =
        MutableStateFlow<String?>(null)

    val checkoutError =
        _checkoutError.asStateFlow()

    private val _isRestoringSession =
        MutableStateFlow(true)

    val isRestoringSession =
        _isRestoringSession.asStateFlow()

    private val _restoreError =
        MutableStateFlow<String?>(null)

    val restoreError =
        _restoreError.asStateFlow()
    init {
        restoreSession()
    }

    fun startSession(table: Table) {

        if (_isLoading.value || _sessionId.value != null) return

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val id = repository.startSession(table.tableId)

                _selectedTable.value = table.copy(
                    status = "OCCUPIED",
                    currentSessionId = id
                )

                _sessionId.value = id

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value =
                    e.message ?: "Không thể mở phiên ăn"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSession() {
        _sessionId.value = null
        _selectedTable.value = null
        _error.value = null
        _checkoutRequested.value = false
        _checkoutError.value = null
    }
    fun requestCheckout() {

        if (
            _isRequestingCheckout.value ||
            _checkoutRequested.value
        ) return

        val currentSessionId = _sessionId.value

        val currentTable = _selectedTable.value

        if (
            currentSessionId == null ||
            currentTable == null
        ) {

            _checkoutError.value =
                "Không tìm thấy phiên ăn hiện tại"

            return
        }

        _isRequestingCheckout.value = true
        _checkoutError.value = null

        viewModelScope.launch {

            try {

                repository.requestCheckout(
                    sessionId = currentSessionId,
                    tableId = currentTable.tableId
                )

                _selectedTable.value = currentTable.copy(
                    status = "CHECKOUT_REQUESTED"
                )

                _checkoutRequested.value = true

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                _checkoutError.value = e.message
                    ?: "Không thể yêu cầu thanh toán"

            } finally {

                _isRequestingCheckout.value = false
            }
        }
    }

    fun clearCheckoutError() {
        _checkoutError.value = null
    }
    fun restoreSession() {

        _isRestoringSession.value = true
        _restoreError.value = null

        viewModelScope.launch {

            try {

                val result = repository.restoreSession()

                if (result != null) {

                    val (sessionId, table) = result

                    _selectedTable.value = table

                    _checkoutRequested.value =
                        table.status == "CHECKOUT_REQUESTED"

                    _sessionId.value = sessionId
                }

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                _restoreError.value = e.message
                    ?: "Không thể khôi phục phiên ăn"

            } finally {

                _isRestoringSession.value = false
            }
        }
    }
}