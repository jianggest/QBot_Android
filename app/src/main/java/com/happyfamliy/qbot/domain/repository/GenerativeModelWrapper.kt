package com.happyfamliy.qbot.domain.repository

/**
 * A thin wrapper around GenerativeModel to make it mockable in unit tests.
 * The real GenerativeModel SDK class is final and cannot be mocked with MockK by default.
 */
interface GenerativeModelWrapper {
    suspend fun generateContent(prompt: String): String?
    fun startChatAndSendStream(history: List<ChatMessage>, prompt: String): kotlinx.coroutines.flow.Flow<String>
}

data class ChatMessage(val role: String, val content: String)
