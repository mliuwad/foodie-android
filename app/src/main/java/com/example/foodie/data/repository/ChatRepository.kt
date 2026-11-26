package com.example.foodie.repository

import com.example.foodie.data.repository.FoodRepository
import com.example.foodie.model.ChatMessage
import com.example.foodie.model.SuggestedItem
import com.example.foodie.network.ChatHistoryDto
import com.example.foodie.network.ChatRequest
import com.example.foodie.network.MenuFoodDto
import com.example.foodie.network.RetrofitClient

class ChatRepository {

    private val foodRepository = FoodRepository()
    private val api = RetrofitClient.chatApi

    suspend fun getReply(
        message: String,
        history: List<ChatMessage>
    ): ChatMessage {

        // Lấy danh sách món đang phục vụ
        val foods = foodRepository.getFoods()
            .filter {
                it.available &&
                        it.price.isFinite() &&
                        it.price >= 0
            }

        if (foods.isEmpty()) {
            return ChatMessage(
                content = "Hiện tại nhà hàng chưa có món khả dụng."
            )
        }

        // Chuẩn bị thực đơn gửi cho AI
        val menu = foods.map { food ->
            MenuFoodDto(
                id = food.id,
                name = food.name,
                description = food.description,
                category = food.category,
                price = food.price
            )
        }

        // Chuẩn bị lịch sử trò chuyện
        val chatHistory = history
            .takeLast(12)
            .map { chat ->

                val content = buildString {

                    append(chat.content)

                    chat.budget?.let {
                        append("\nNgân sách: $it VNĐ")
                    }

                    if (chat.suggestedItems.isNotEmpty()) {

                        append("\nCác món đã đề xuất:")

                        chat.suggestedItems.forEach { item ->
                            append(
                                "\nID: ${item.food.id}, " +
                                        "${item.food.name}, " +
                                        "số lượng: ${item.quantity}"
                            )
                        }
                    }
                }

                ChatHistoryDto(
                    role = if (chat.isUser) {
                        "user"
                    } else {
                        "assistant"
                    },
                    content = content
                )
            }

        // Gọi REST API
        val response = api.sendMessage(
            ChatRequest(
                message = message,
                menu = menu,
                history = chatHistory
            )
        )

        // Kiểm tra ngân sách AI trả về
        val budget = response.budget?.takeIf {
            it.isFinite() && it >= 0
        }

        // Đối chiếu ID món với dữ liệu Firestore
        val foodMap = foods.associateBy { it.id }

        val rawItems = response.items.orEmpty()

        if (
            rawItems.size > 30 ||
            rawItems.any {
                it.foodId !in foodMap ||
                        it.quantity !in 1..20
            } ||
            rawItems.distinctBy {
                it.foodId
            }.size != rawItems.size
        ) {
            return ChatMessage(
                content = "Mình chưa tìm được danh sách món " +
                        "phù hợp. Bạn thử hỏi lại nhé.",
                budget = budget
            )
        }

        val suggestedItems = rawItems.mapNotNull { item ->

            val food = foodMap[item.foodId]
                ?: return@mapNotNull null

            SuggestedItem(
                food = food,
                quantity = item.quantity
            )
        }

        // Kiểm tra tổng tiền đề xuất
        val totalAmount = suggestedItems.sumOf {
            it.subTotal
        }

        if (
            !totalAmount.isFinite() ||
            (budget != null && totalAmount > budget)
        ) {
            return ChatMessage(
                content = "Mình chưa tìm được thực đơn phù hợp " +
                        "với ngân sách này. Bạn có thể yêu cầu " +
                        "mình gợi ý lại nhé.",
                budget = budget
            )
        }

        return ChatMessage(
            content = response.message
                ?.takeIf { it.isNotBlank() }
                ?: "Bạn có thể tham khảo những món sau:",
            suggestedItems = suggestedItems,
            budget = budget
        )
    }
}