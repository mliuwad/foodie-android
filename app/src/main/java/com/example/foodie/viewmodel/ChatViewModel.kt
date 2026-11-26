package com.example.foodie.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodie.model.ChatMessage
import com.example.foodie.repository.ChatRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _messages = MutableStateFlow(
        listOf(welcomeMessage())
    )

    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var chatJob: Job? = null
    private var requestVersion = 0

    fun sendMessage(content: String) {

        val message = content.trim()

        if (
            message.isBlank() ||
            message.length > 500 ||
            _isLoading.value
        ) {
            return
        }

        val history = _messages.value

        _messages.update {
            it + ChatMessage(
                content = message,
                isUser = true
            )
        }

        _isLoading.value = true

        val version = ++requestVersion

        chatJob = viewModelScope.launch {

            try {

                val reply = repository.getReply(
                    message = message,
                    history = history
                )

                if (version == requestVersion) {
                    _messages.update {
                        it + reply
                    }
                }

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                Log.e(
                    "FOODIE_CHAT",
                    "Chat API error",
                    e
                )

                if (version == requestVersion) {

                    _messages.update {
                        it + ChatMessage(
                            content = "Hiện tại mình chưa thể " +
                                    "phản hồi. Bạn vui lòng thử lại nhé."
                        )
                    }
                }

            } finally {

                if (version == requestVersion) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun clearChat() {

        requestVersion++

        chatJob?.cancel()
        chatJob = null

        _messages.value = listOf(
            welcomeMessage()
        )

        _isLoading.value = false
    }

    private fun welcomeMessage(): ChatMessage {

        return ChatMessage(
            content = "Xin chào! Mình là trợ lý Foodie. " +
                    "Bạn muốn ăn gì hôm nay? Hãy cho mình biết " +
                    "sở thích hoặc ngân sách của bạn nhé!"
        )
    }
}