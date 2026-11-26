
package com.example.foodie.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodie.data.repository.FoodRepository
import com.example.foodie.model.Food
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoodViewModel(
    private val repository: FoodRepository = FoodRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "FoodieDebug"
    }

    // 1. Danh sách món ăn
    private val _foods = MutableStateFlow<List<Food>>(emptyList())
    val foods: StateFlow<List<Food>> = _foods.asStateFlow()

    // 2. Trạng thái tải dữ liệu
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 3. Thông báo lỗi
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 4. Tự động tải dữ liệu
    init {
        loadFoods()
    }

    // 5. Tải danh sách món ăn từ Repository
    fun loadFoods() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                Log.d(TAG, "Bắt đầu tải danh sách món ăn")

                val result = repository.getFoods()

                _foods.value = result

                Log.d(
                    TAG,
                    "Tải thành công ${result.size} món ăn"
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Lỗi khi tải món ăn từ Firebase",
                    e
                )

                _error.value =
                    e.localizedMessage ?: "Không thể tải món ăn"

            } finally {
                _isLoading.value = false
            }
        }
    }
}
