package com.happyfamliy.qbot.domain.repository

import com.happyfamliy.qbot.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

interface GeminiRepository {
    fun sendMessageStream(history: List<MessageEntity>, prompt: String): Flow<String>
}
