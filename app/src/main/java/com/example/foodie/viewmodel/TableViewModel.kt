package com.example.foodie.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodie.model.Table
import com.example.foodie.repository.TableRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TableViewModel : ViewModel() {

    private val repository = TableRepository()

    private val _tables =
        MutableStateFlow<List<Table>>(emptyList())

    val tables = _tables.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadTables()
    }

    fun loadTables() {

        if (_isLoading.value) return

        viewModelScope.launch {

            _isLoading.value = true
            _error.value = null

            try {

                _tables.value = repository.getTables()

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {

                _error.value =
                    e.message ?: "Không thể tải danh sách bàn"

            } finally {

                _isLoading.value = false
            }
        }
    }
}