package com.example.foodie.repository

import com.example.foodie.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun loginAdmin(
        email: String,
        password: String
    ): User {

        val result = auth.signInWithEmailAndPassword(
            email.trim(),
            password
        ).await()

        try {

            val uid = result.user?.uid
                ?: throw IllegalStateException(
                    "Không thể xác thực tài khoản"
                )

            val user = db.collection("users")
                .document(uid)
                .get()
                .await()
                .toObject(User::class.java)
                ?: throw IllegalStateException(
                    "Không tìm thấy thông tin tài khoản"
                )

            if (user.role != "ADMIN" || !user.active) {
                throw IllegalStateException(
                    "Tài khoản không có quyền quản trị"
                )
            }

            return user

        } catch (e: Exception) {
            auth.signOut()
            throw e
        }
    }

    fun logout() {
        auth.signOut()
    }
}