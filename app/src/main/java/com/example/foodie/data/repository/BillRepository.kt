package com.example.foodie.repository

import com.example.foodie.model.Bill
import com.example.foodie.model.PaymentMethod
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BillRepository {

    private val db = FirebaseFirestore.getInstance()

    private val auth = FirebaseAuth.getInstance()

    private val billsCollection =
        db.collection("bills")

    private val sessionsCollection =
        db.collection("dining_sessions")

    private val tablesCollection =
        db.collection("tables")

    // User theo dõi hóa đơn của phiên ăn
    fun observeBill(
        sessionId: String
    ): Flow<Bill?> = callbackFlow {

        val listener = billsCollection
            .document(sessionId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                try {

                    val bill = snapshot
                        ?.toObject(Bill::class.java)

                    trySend(bill)

                } catch (e: Exception) {
                    close(e)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    // Admin theo dõi các hóa đơn chưa thanh toán
    fun observeUnpaidBills():
            Flow<List<Bill>> = callbackFlow {

        val listener = billsCollection
            .whereEqualTo("paymentStatus", "UNPAID")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                try {

                    val bills = snapshot
                        ?.toObjects(Bill::class.java)
                        ?.sortedBy { it.createdAt }
                        ?: emptyList()

                    trySend(bills)

                } catch (e: Exception) {
                    close(e)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    // Admin xác nhận đã nhận tiền
    suspend fun confirmPayment(
        billId: String,
        paymentMethod: PaymentMethod
    ) {

        val adminUid = auth.currentUser?.uid
            ?: throw IllegalStateException(
                "Vui lòng đăng nhập lại"
            )

        val billRef = billsCollection
            .document(billId)

        val sessionRef = sessionsCollection
            .document(billId)

        val adminRef = db.collection("users")
            .document(adminUid)

        db.runTransaction { transaction ->

            val admin = transaction.get(adminRef)

            val bill = transaction.get(billRef)

            val session = transaction.get(sessionRef)

            if (
                admin.getString("role") != "ADMIN" ||
                admin.getBoolean("active") != true
            ) {
                throw IllegalStateException(
                    "Bạn không có quyền thanh toán"
                )
            }

            if (!bill.exists() || !session.exists()) {
                throw IllegalStateException(
                    "Không tìm thấy hóa đơn hoặc phiên ăn"
                )
            }

            if (
                bill.getString("paymentStatus") != "UNPAID"
            ) {
                throw IllegalStateException(
                    "Hóa đơn đã được xử lý"
                )
            }

            if (
                session.getString("status")
                != "CHECKOUT_REQUESTED"
            ) {
                throw IllegalStateException(
                    "Phiên ăn không hợp lệ"
                )
            }

            val tableId = bill.getString("tableId")
                ?: throw IllegalStateException(
                    "Không tìm thấy bàn ăn"
                )

            val tableRef = tablesCollection
                .document(tableId)

            val table = transaction.get(tableRef)

            if (
                !table.exists() ||
                session.getString("tableId") != tableId ||
                table.getString("currentSessionId") != billId ||
                table.getString("status") != "CHECKOUT_REQUESTED"
            ) {
                throw IllegalStateException(
                    "Thông tin bàn ăn không hợp lệ"
                )
            }

            // 1. Hoàn tất hóa đơn
            transaction.update(
                billRef,
                mapOf(
                    "paymentStatus" to "PAID",
                    "paymentMethod" to paymentMethod.name,
                    "paidAt" to FieldValue.serverTimestamp(),
                    "paidBy" to adminUid
                )
            )

            // 2. Kết thúc phiên ăn
            transaction.update(
                sessionRef,
                mapOf(
                    "status" to "COMPLETED",
                    "endedAt" to FieldValue.serverTimestamp()
                )
            )

            // 3. Giải phóng bàn
            transaction.update(
                tableRef,
                mapOf<String, Any?>(
                    "status" to "AVAILABLE",
                    "currentSessionId" to null
                )
            )

        }.await()
    }
}