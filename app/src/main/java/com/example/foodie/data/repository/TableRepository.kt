package com.example.foodie.repository

import com.example.foodie.model.Table
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TableRepository {

    private val db = FirebaseFirestore.getInstance()

    private val tablesCollection = db.collection("tables")

    suspend fun getTables(): List<Table> {

        return tablesCollection
            .orderBy("tableName")
            .get()
            .await()
            .toObjects(Table::class.java)
    }
}