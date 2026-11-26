package com.example.foodie.model

data class OrderItem(
    val foodId: Int = 0,
    val foodName: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Int = 0,
    val subTotal: Double = 0.0
)