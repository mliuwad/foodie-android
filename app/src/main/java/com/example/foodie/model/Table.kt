package com.example.foodie.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Table(
    @set:DocumentId
    var tableId: String = "",
    val tableName: String = "",
    val capacity: Int = 0,
    val status: String = TableStatus.AVAILABLE.name,
    val currentSessionId: String? = null
) {
    @get:Exclude
    val isAvailable: Boolean
        get() = status == TableStatus.AVAILABLE.name && currentSessionId == null
}
