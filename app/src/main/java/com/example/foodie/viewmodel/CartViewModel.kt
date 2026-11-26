package com.example.foodie.viewmodel

import androidx.lifecycle.ViewModel
import com.example.foodie.CartItem
import com.example.foodie.model.Food
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartViewModel : ViewModel() {

    private val _cartItems =
        MutableStateFlow<List<CartItem>>(emptyList())

    val cartItems = _cartItems.asStateFlow()

    // Thêm món hoặc tăng số lượng
    fun addFood(food: Food) {
        _cartItems.update { currentItems ->

            val existingItem = currentItems.find {
                it.food.id == food.id
            }

            if (existingItem == null) {
                currentItems + CartItem(
                    food = food,
                    quantity = 1
                )
            } else {
                currentItems.map { item ->
                    if (item.food.id == food.id) {
                        item.copy(
                            quantity = item.quantity + 1
                        )
                    } else {
                        item
                    }
                }
            }
        }
    }

    // Giảm số lượng
    fun decreaseQuantity(foodId: Int) {
        _cartItems.update { currentItems ->
            currentItems.mapNotNull { item ->

                if (item.food.id != foodId) {
                    item
                } else if (item.quantity > 1) {
                    item.copy(
                        quantity = item.quantity - 1
                    )
                } else {
                    null
                }
            }
        }
    }

    val totalPrice: Double get() = cartItems.value.sumOf { item->
        item.food.price * item.quantity
    }
    val totalQuantity:Int get() = cartItems.value.sumOf { item->
        item.quantity
    }
    fun clearCart(){
        _cartItems.value = emptyList()
    }
}