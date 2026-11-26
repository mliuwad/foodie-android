package com.example.foodie.model

data class SuggestedItem(
    val food: Food,
    val quantity: Int
) {
    val subTotal: Double
        get() = food.price * quantity
}