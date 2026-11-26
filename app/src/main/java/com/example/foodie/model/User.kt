package com.example.foodie.model

import com.google.firebase.firestore.DocumentId

data class User(
    @set:DocumentId
    var userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "",
    val active: Boolean = false
)