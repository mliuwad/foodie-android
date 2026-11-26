package com.example.foodie.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Bill(

    @set:DocumentId
    var billId: String = "",

    val tableId: String = "",

    val orderCount: Int = 0,

    val totalQuantity: Int = 0,

    val totalAmount: Double = 0.0,

    val paymentStatus: String = "UNPAID",

    val paymentMethod: String? = null,

    val createdAt: Timestamp? = null,

    val paidAt: Timestamp? = null,

    val paidBy: String? = null
)

enum class PaymentStatus {
    UNPAID,
    PAID
}

enum class PaymentMethod {
    CASH,
    TRANSFER
}