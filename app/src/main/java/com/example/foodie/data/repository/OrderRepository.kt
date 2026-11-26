package com.example.foodie.repository

import com.example.foodie.CartItem
import com.example.foodie.model.DiningSessionStatus
import com.example.foodie.model.Order
import com.example.foodie.model.OrderStatus
import com.example.foodie.model.TableStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OrderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val ordersCollection = db.collection("orders")
    private val sessionsCollection = db.collection("dining_sessions")
    private val tablesCollection = db.collection("tables")
    private val foodsCollection = db.collection("foods")

    suspend fun createOrder(
        sessionId: String,
        tableId: String,
        cartItems: List<CartItem>,
        note: String = ""
    ): String {

        require(cartItems.isNotEmpty()) {
            "Giỏ hàng đang trống"
        }

        require(cartItems.all { it.quantity in 1..99 }) {
            "Số lượng món không hợp lệ"
        }

        require(cartItems.size <= 100) {
            "Đơn hàng có quá nhiều món"
        }

        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException(
                "Không thể xác thực thiết bị"
            )

        val orderRef = ordersCollection.document()
        val sessionRef = sessionsCollection.document(sessionId)
        val tableRef = tablesCollection.document(tableId)

        return db.runTransaction { transaction ->

            // Kiểm tra phiên ăn
            val sessionSnapshot = transaction.get(sessionRef)

            if (!sessionSnapshot.exists()) {
                throw IllegalStateException(
                    "Phiên ăn không tồn tại"
                )
            }

            if (
                sessionSnapshot.getString("status") !=
                DiningSessionStatus.ACTIVE.name
            ) {
                throw IllegalStateException(
                    "Phiên ăn không còn hoạt động"
                )
            }

            if (sessionSnapshot.getString("ownerUid") != uid) {
                throw IllegalStateException(
                    "Thiết bị không có quyền gọi món"
                )
            }

            if (sessionSnapshot.getString("tableId") != tableId) {
                throw IllegalStateException(
                    "Thông tin bàn không hợp lệ"
                )
            }

            // Kiểm tra bàn ăn
            val tableSnapshot = transaction.get(tableRef)

            if (
                !tableSnapshot.exists() ||
                tableSnapshot.getString("status") !=
                TableStatus.OCCUPIED.name ||
                tableSnapshot.getString("currentSessionId") != sessionId
            ) {
                throw IllegalStateException(
                    "Bàn ăn không còn phục vụ phiên này"
                )
            }

            // Tạo danh sách chi tiết món
            val itemsData = cartItems.map { cartItem ->

                val foodRef = foodsCollection.document(
                    cartItem.food.id.toString()
                )

                val foodSnapshot = transaction.get(foodRef)

                if (!foodSnapshot.exists()) {
                    throw IllegalStateException(
                        "${cartItem.food.name} không tồn tại"
                    )
                }

                if (foodSnapshot.getBoolean("available") != true) {
                    throw IllegalStateException(
                        "${cartItem.food.name} hiện đã hết"
                    )
                }

                val foodName = foodSnapshot.getString("name")
                    ?: throw IllegalStateException(
                        "Tên món không hợp lệ"
                    )

                val unitPrice = foodSnapshot.getDouble("price")
                    ?: throw IllegalStateException(
                        "Giá món không hợp lệ"
                    )

                require(unitPrice.isFinite() && unitPrice >= 0) {
                    "Giá món không hợp lệ"
                }

                if (unitPrice != cartItem.food.price) {
                    throw IllegalStateException(
                        "$foodName đã thay đổi giá. Vui lòng tải lại thực đơn và chọn lại món."
                    )
                }

                mapOf(
                    "foodId" to cartItem.food.id,
                    "foodName" to foodName,
                    "unitPrice" to unitPrice,
                    "quantity" to cartItem.quantity,
                    "subTotal" to unitPrice * cartItem.quantity
                )
            }

            // Tính tổng số lượng và tổng tiền
            val totalQuantity = cartItems.sumOf {
                it.quantity
            }

            val totalAmount = itemsData.sumOf {
                it["subTotal"] as Double
            }

            // Tạo document Order chứa toàn bộ chi tiết
            val orderData = mapOf(
                "sessionId" to sessionId,
                "tableId" to tableId,
                "status" to OrderStatus.PENDING.name,
                "totalQuantity" to totalQuantity,
                "totalAmount" to totalAmount,
                "note" to note.trim(),
                "createdAt" to FieldValue.serverTimestamp(),
                "items" to itemsData
            )

            transaction.set(orderRef, orderData)

            orderRef.id

        }.await()
    }
    fun observeOrders(
        sessionId: String
    ): Flow<List<Order>> = callbackFlow {

        val listener = ordersCollection
            .whereEqualTo("sessionId", sessionId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        val orders = snapshot
                            .toObjects(Order::class.java)
                            .sortedByDescending {
                                it.createdAt
                            }

                        trySend(orders)

                    } catch (e: Exception) {
                        close(e)
                    }
                }
            }

        awaitClose {
            listener.remove()
        }
    }
    fun observeAllOrders(): Flow<List<Order>> = callbackFlow {

        val listener = ordersCollection
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {

                    try {

                        val orders = snapshot
                            .toObjects(Order::class.java)
                            .sortedByDescending {
                                it.createdAt
                            }

                        trySend(orders)

                    } catch (e: Exception) {
                        close(e)
                    }
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: OrderStatus
    ) {

        val orderRef = ordersCollection.document(orderId)

        db.runTransaction { transaction ->

            val snapshot = transaction.get(orderRef)

            if (!snapshot.exists()) {
                throw IllegalStateException(
                    "Đơn hàng không tồn tại"
                )
            }

            val currentStatus = snapshot.getString("status")

            val isValid = when (currentStatus) {

                OrderStatus.PENDING.name ->
                    newStatus == OrderStatus.CONFIRMED ||
                            newStatus == OrderStatus.CANCELLED

                OrderStatus.CONFIRMED.name ->
                    newStatus == OrderStatus.PREPARING ||
                            newStatus == OrderStatus.CANCELLED

                OrderStatus.PREPARING.name ->
                    newStatus == OrderStatus.SERVED

                else -> false
            }

            if (!isValid) {
                throw IllegalStateException(
                    "Trạng thái đơn hàng đã thay đổi hoặc không hợp lệ"
                )
            }

            transaction.update(
                orderRef,
                "status",
                newStatus.name
            )

        }.await()
    }

}