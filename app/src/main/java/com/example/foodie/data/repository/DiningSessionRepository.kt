package com.example.foodie.repository

import com.example.foodie.model.DiningSessionStatus
import com.example.foodie.model.TableStatus
import com.example.foodie.model.OrderStatus
import com.example.foodie.model.Table
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

class DiningSessionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val tablesCollection =
        db.collection("tables")

    private val sessionsCollection =
        db.collection("dining_sessions")

    private val ordersCollection =
        db.collection("orders")

    private val billsCollection =
        db.collection("bills")

    // =========================================
    // MỞ PHIÊN ĂN
    // =========================================

    suspend fun startSession(tableId: String): String {

        // 1. Xác thực thiết bị
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }

        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException(
                "Không thể xác thực thiết bị"
            )

        val tableRef = tablesCollection.document(tableId)

        val sessionRef = sessionsCollection.document()

        return db.runTransaction { transaction ->

            // 2. Đọc thông tin bàn
            val tableSnapshot = transaction.get(tableRef)

            // 3. Kiểm tra bàn tồn tại
            if (!tableSnapshot.exists()) {
                throw IllegalStateException(
                    "Bàn ăn không tồn tại"
                )
            }

            // 4. Kiểm tra trạng thái bàn
            val status = tableSnapshot.getString("status")

            val currentSessionId =
                tableSnapshot.getString("currentSessionId")

            if (
                status != TableStatus.AVAILABLE.name ||
                currentSessionId != null
            ) {
                throw IllegalStateException(
                    "Bàn ăn không còn trống"
                )
            }

            // 5. Chuẩn bị dữ liệu phiên ăn
            val sessionData = mapOf(
                "tableId" to tableId,
                "ownerUid" to uid,
                "status" to DiningSessionStatus.ACTIVE.name,
                "startedAt" to FieldValue.serverTimestamp(),
                "endedAt" to null
            )

            // 6. Tạo phiên ăn
            transaction.set(
                sessionRef,
                sessionData
            )

            // 7. Cập nhật trạng thái bàn
            transaction.update(
                tableRef,
                mapOf(
                    "status" to TableStatus.OCCUPIED.name,
                    "currentSessionId" to sessionRef.id
                )
            )

            // 8. Trả về ID phiên ăn
            sessionRef.id

        }.await()
    }

    // =========================================
    // YÊU CẦU THANH TOÁN VÀ TẠO HÓA ĐƠN
    // =========================================

    suspend fun requestCheckout(
        sessionId: String,
        tableId: String
    ) {

        // 1. Kiểm tra tài khoản hiện tại
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException(
                "Không tìm thấy tài khoản hiện tại"
            )

        val sessionRef =
            sessionsCollection.document(sessionId)

        val tableRef =
            tablesCollection.document(tableId)

        // Sử dụng sessionId làm ID hóa đơn
        val billRef =
            billsCollection.document(sessionId)

        // 2. Lấy danh sách đơn hàng của phiên ăn
        val orders = ordersCollection
            .whereEqualTo("sessionId", sessionId)
            .get(Source.SERVER)
            .await()

        if (orders.isEmpty) {
            throw IllegalStateException(
                "Phiên ăn chưa có đơn hàng"
            )
        }

        db.runTransaction { transaction ->

            // =================================
            // ĐỌC DỮ LIỆU
            // =================================

            val session = transaction.get(sessionRef)

            val table = transaction.get(tableRef)

            val existingBill = transaction.get(billRef)

            val currentOrders = orders.documents.map {
                transaction.get(it.reference)
            }

            // =================================
            // KIỂM TRA PHIÊN ĂN
            // =================================

            if (!session.exists() || !table.exists()) {
                throw IllegalStateException(
                    "Không tìm thấy thông tin phiên ăn"
                )
            }

            if (session.getString("ownerUid") != uid) {
                throw IllegalStateException(
                    "Bạn không có quyền thanh toán phiên ăn này"
                )
            }

            if (
                session.getString("status") !=
                DiningSessionStatus.ACTIVE.name
            ) {
                throw IllegalStateException(
                    "Phiên ăn không còn hoạt động"
                )
            }

            // =================================
            // KIỂM TRA BÀN ĂN
            // =================================

            if (
                session.getString("tableId") != tableId ||
                table.getString("currentSessionId") != sessionId ||
                table.getString("status") != TableStatus.OCCUPIED.name
            ) {
                throw IllegalStateException(
                    "Thông tin bàn ăn không hợp lệ"
                )
            }

            // =================================
            // KIỂM TRA HÓA ĐƠN
            // =================================

            if (existingBill.exists()) {
                throw IllegalStateException(
                    "Phiên ăn đã có hóa đơn"
                )
            }

            // =================================
            // KIỂM TRA ĐƠN HÀNG
            // =================================

            val hasInvalidOrders = currentOrders.any {

                !it.exists() ||
                        it.getString("sessionId") != sessionId ||
                        it.getString("tableId") != tableId
            }

            if (hasInvalidOrders) {
                throw IllegalStateException(
                    "Thông tin đơn hàng không hợp lệ"
                )
            }

            val hasUnfinishedOrders = currentOrders.any {

                val status = it.getString("status")

                status != OrderStatus.SERVED.name &&
                        status != OrderStatus.CANCELLED.name
            }

            if (hasUnfinishedOrders) {
                throw IllegalStateException(
                    "Vui lòng chờ phục vụ đầy đủ các món"
                )
            }

            // Chỉ tính tiền những đơn đã phục vụ
            val servedOrders = currentOrders.filter {

                it.getString("status") ==
                        OrderStatus.SERVED.name
            }

            if (servedOrders.isEmpty()) {
                throw IllegalStateException(
                    "Không có đơn hàng cần thanh toán"
                )
            }

            // =================================
            // TÍNH TỔNG TIỀN HÓA ĐƠN
            // =================================

            val totalAmount = servedOrders.sumOf {

                val amount = it.getDouble("totalAmount")
                    ?: throw IllegalStateException(
                        "Đơn hàng thiếu thông tin tổng tiền"
                    )

                if (!amount.isFinite() || amount < 0) {
                    throw IllegalStateException(
                        "Tổng tiền đơn hàng không hợp lệ"
                    )
                }

                amount
            }

            val totalQuantity = servedOrders.sumOf {

                val quantity = it.getLong("totalQuantity")
                    ?: throw IllegalStateException(
                        "Đơn hàng thiếu thông tin số lượng"
                    )

                if (quantity <= 0) {
                    throw IllegalStateException(
                        "Số lượng món không hợp lệ"
                    )
                }

                quantity
            }

            if (!totalAmount.isFinite() || totalQuantity <= 0) {
                throw IllegalStateException(
                    "Thông tin tổng hợp hóa đơn không hợp lệ"
                )
            }

            // =================================
            // CHUẨN BỊ DỮ LIỆU HÓA ĐƠN
            // =================================

            val billData = hashMapOf<String, Any?>(
                "tableId" to tableId,
                "orderCount" to servedOrders.size,
                "totalQuantity" to totalQuantity,
                "totalAmount" to totalAmount,
                "paymentStatus" to "UNPAID",
                "paymentMethod" to null,
                "createdAt" to FieldValue.serverTimestamp(),
                "paidAt" to null,
                "paidBy" to null
            )

            // =================================
            // GHI DỮ LIỆU
            // =================================

            // 1. Tạo hóa đơn
            transaction.set(
                billRef,
                billData
            )

            // 2. Cập nhật phiên ăn
            transaction.update(
                sessionRef,
                "status",
                DiningSessionStatus.CHECKOUT_REQUESTED.name
            )

            // 3. Cập nhật trạng thái bàn
            transaction.update(
                tableRef,
                "status",
                TableStatus.CHECKOUT_REQUESTED.name
            )

        }.await()
    }
    suspend fun restoreSession(): Pair<String, Table>? {

        val currentUser = auth.currentUser
            ?: return null

        // Không khôi phục phiên khách từ tài khoản Admin
        if (!currentUser.isAnonymous) {
            return null
        }

        val sessions = sessionsCollection
            .whereEqualTo("ownerUid", currentUser.uid)
            .whereIn(
                "status",
                listOf(
                    "ACTIVE",
                    "CHECKOUT_REQUESTED"
                )
            )
            .get(Source.SERVER)
            .await()

        val sortedSessions = sessions.documents
            .sortedByDescending {
                it.getTimestamp("startedAt")
            }

        for (session in sortedSessions) {

            val tableId = session.getString("tableId")
                ?: continue

            val table = tablesCollection
                .document(tableId)
                .get(Source.SERVER)
                .await()
                .toObject(Table::class.java)
                ?: continue

            // Kiểm tra bàn còn thuộc phiên ăn này
            if (table.currentSessionId != session.id) {
                continue
            }

            val expectedStatus = when (
                session.getString("status")
            ) {

                "ACTIVE" -> "OCCUPIED"

                "CHECKOUT_REQUESTED" ->
                    "CHECKOUT_REQUESTED"

                else -> continue
            }

            if (table.status != expectedStatus) {
                continue
            }

            return Pair(session.id, table)
        }

        return null
    }
}