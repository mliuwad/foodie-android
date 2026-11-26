package com.example.foodie.model

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean = false,
    val suggestedItems: List<SuggestedItem> = emptyList(),
    val budget: Double? = null
) {
    val totalAmount: Double
        get() = suggestedItems.sumOf {
            it.subTotal
        }

    val remainingBudget: Double?
        get() = budget?.minus(totalAmount)
}