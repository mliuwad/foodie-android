package com.example.foodie.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class DiningSession(
    @set:DocumentId
    var sessionId: String = "",
    val tableId: String = "",
    val ownerUid: String = "",
    val status: String = DiningSessionStatus.ACTIVE.name,
    val startedAt: Timestamp? = null,
    val endedAt: Timestamp? = null
)