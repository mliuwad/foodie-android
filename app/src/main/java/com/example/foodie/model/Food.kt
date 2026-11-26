package com.example.foodie.model

data class Food(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val available: Boolean,
    val category: String
)