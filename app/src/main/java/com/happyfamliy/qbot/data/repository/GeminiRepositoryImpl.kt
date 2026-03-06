package com.happyfamliy.qbot.data.repository

import com.happyfamliy.qbot.data.local.entity.MessageEntity
import com.happyfamliy.qbot.domain.repository.ChatMessage
import com.happyfamliy.qbot.domain.repository.GeminiRepository
import com.happyfamliy.qbot.domain.repository.GenerativeModelWrapper
import kotlinx.coroutines.flow.Flow

class GeminiRepositoryImpl(
    private val chatWrapper: GenerativeModelWrapper
) : GeminiRepository {

    override fun sendMessageStream(history: List<MessageEntity>, prompt: String): Flow<String> {
        val chatHistory = history.map { ChatMessage(role = it.role, content = it.content) }
        return chatWrapper.startChatAndSendStream(chatHistory, prompt)
    }
}
