package com.example.foodie.data.repository

import android.util.Log
import com.example.foodie.model.Food
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FoodRepository(
    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "FoodieRepository"
    }

    suspend fun getFoods(): List<Food> {
        val snapshot = firestore
            .collection("foods")
            .get()
            .await()

        Log.d(TAG, "Số document: ${snapshot.size()}")

        return snapshot.documents.mapNotNull { document ->
            try {
                val food = documentToFood(document)

                if (food == null) {
                    Log.w(
                        TAG,
                        "Bỏ qua document ${document.id}: thiếu dữ liệu bắt buộc"
                    )
                }

                food
            } catch (e: RuntimeException) {
                Log.e(
                    TAG,
                    "Không thể chuyển document ${document.id} thành Food",
                    e
                )
                null
            }
        }
    }

    private fun documentToFood(
        document: DocumentSnapshot
    ): Food? {
        val id = document.id.toIntOrNull() ?: return null
        val name = document.getString("name") ?: return null

        val price = document.getDouble("price")
            ?: return null

        val category = document.getString("category")
            ?: return null

        return Food(
            id = id,
            name = name,
            description = document.getString("description") ?: "",
            price = price,
            imageUrl = document.getString("imageUrl") ?: "",
            available = document.getBoolean("available") ?: false,
            category = category
        )
    }
}