package com.happyfamliy.qbot.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.happyfamliy.qbot.data.local.entity.MessageEntity
import com.happyfamliy.qbot.domain.repository.GeminiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GeminiRepositoryImpl(
    private val generativeModel: GenerativeModel
) : GeminiRepository {

    override fun sendMessageStream(history: List<MessageEntity>, prompt: String): Flow<String> {
        val chatHistory = history.map { message ->
            content(role = message.role) {
                text(message.content)
            }
        }
        val chat = generativeModel.startChat(chatHistory)
        return chat.sendMessageStream(prompt).map { response ->
            response.text ?: ""
        }
    }
}
