package com.example.foodie.network

data class MenuFoodDto(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val price: Double
)

data class ChatHistoryDto(
    val role: String,
    val content: String
)

data class ChatRequest(
    val message: String,
    val menu: List<MenuFoodDto>,
    val history: List<ChatHistoryDto>
)

data class SuggestedItemDto(
    val foodId: Int,
    val quantity: Int
)

data class ChatResponse(
    val message: String?,
    val budget: Double?,
    val items: List<SuggestedItemDto>?
)