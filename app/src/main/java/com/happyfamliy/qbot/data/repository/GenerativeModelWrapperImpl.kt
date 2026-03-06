package com.happyfamliy.qbot.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.happyfamliy.qbot.domain.repository.ChatMessage
import com.happyfamliy.qbot.domain.repository.GenerativeModelWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GenerativeModelWrapperImpl(
    private val generativeModel: GenerativeModel
) : GenerativeModelWrapper {

    override suspend fun generateContent(prompt: String): String? {
        return generativeModel.generateContent(prompt).text
    }

    override fun startChatAndSendStream(history: List<ChatMessage>, prompt: String): Flow<String> {
        val chatHistory = history.map { msg ->
            content(role = msg.role) { text(msg.content) }
        }
        val chat = generativeModel.startChat(chatHistory)
        return chat.sendMessageStream(prompt).map { it.text ?: "" }
    }
}
