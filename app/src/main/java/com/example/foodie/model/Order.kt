package com.example.foodie.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Order(
    @set:DocumentId
    var orderId: String = "",

    val sessionId: String = "",
    val tableId: String = "",
    val status: String = OrderStatus.PENDING.name,
    val totalQuantity: Int = 0,
    val totalAmount: Double = 0.0,
    val note: String = "",
    val createdAt: Timestamp? = null,

    val items: List<OrderItem> = emptyList()
)