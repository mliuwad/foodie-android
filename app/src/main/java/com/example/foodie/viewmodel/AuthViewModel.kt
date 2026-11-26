package com.example.foodie.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodie.model.User
import com.example.foodie.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _admin = MutableStateFlow<User?>(null)
    val admin = _admin.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun login(email: String, password: String) {

        if (_isLoading.value) return

        if (email.isBlank() || password.isBlank()) {
            _error.value =
                "Vui lòng nhập đầy đủ email và mật khẩu"
            return
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {

            try {

                _admin.value = repository.loginAdmin(
                    email,
                    password
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: FirebaseAuthException) {

                _error.value = when (e.errorCode) {

                    "ERROR_INVALID_EMAIL" ->
                        "Email không hợp lệ"

                    "ERROR_USER_DISABLED" ->
                        "Tài khoản đã bị vô hiệu hóa"

                    "ERROR_TOO_MANY_REQUESTS" ->
                        "Vui lòng thử lại sau"

                    else ->
                        "Email hoặc mật khẩu không chính xác"
                }

            } catch (e: Exception) {

                _error.value = e.message
                    ?: "Đăng nhập thất bại"

            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun logout() {
        repository.logout()
        _admin.value = null
        _error.value = null
    }
}