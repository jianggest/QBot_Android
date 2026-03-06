package com.happyfamliy.qbot.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happyfamliy.qbot.data.local.dao.MessageDao
import com.happyfamliy.qbot.data.local.dao.SessionDao
import com.happyfamliy.qbot.data.local.entity.MessageEntity
import com.happyfamliy.qbot.data.local.entity.SessionEntity
import com.happyfamliy.qbot.domain.repository.GeminiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sessionDao: SessionDao,
    private val messageDao: MessageDao,
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private var currentSessionId: Long? = null

    private val _messages = MutableStateFlow<List<ChatUiMessage>>(emptyList())
    val messages: StateFlow<List<ChatUiMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun loadSession(sessionId: Long) {
        currentSessionId = sessionId
        viewModelScope.launch {
            reloadMessagesFromDb(sessionId)
        }
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty() || _isGenerating.value) return

        _inputText.value = ""
        _isGenerating.value = true

        viewModelScope.launch {
            val sessionId = getOrCreateSession()
            
            // Insert user message
            val userMsg = MessageEntity(sessionId = sessionId, role = "user", content = text)
            messageDao.insertMessage(userMsg)
            
            // Fetch history to send to Gemini (excluding the one we just generated)
            val dbHistory = messageDao.getMessagesForSession(sessionId).first()
            val historyWithoutCurrent = dbHistory.dropLast(1)
            
            // Reload UI
            reloadMessagesFromDb(sessionId)

            var currentStreamingText = ""
            val streamingMessageUi = ChatUiMessage(id = -1, role = "model", content = currentStreamingText, isStreaming = true)
            _messages.value = _messages.value + streamingMessageUi

            try {
                geminiRepository.sendMessageStream(historyWithoutCurrent, text).collect { chunk ->
                    currentStreamingText += chunk
                    updateStreamingMessage(currentStreamingText)
                }
                
                // Done streaming. Save AI message to DB
                val aiMsg = MessageEntity(sessionId = sessionId, role = "model", content = currentStreamingText)
                messageDao.insertMessage(aiMsg)
                
            } catch (e: Exception) {
                updateStreamingMessage(currentStreamingText + "\n[System Error]: \${e.message}")
            } finally {
                reloadMessagesFromDb(sessionId)
                _isGenerating.value = false
            }
        }
    }

    private fun updateStreamingMessage(newContent: String) {
        val currentList = _messages.value.toMutableList()
        val index = currentList.indexOfFirst { it.isStreaming }
        if (index != -1) {
             currentList[index] = currentList[index].copy(content = newContent)
             _messages.value = currentList
        }
    }

    private suspend fun reloadMessagesFromDb(sessionId: Long) {
        val dbMessages = messageDao.getMessagesForSession(sessionId).first()
        _messages.value = dbMessages.map { 
            ChatUiMessage(id = it.id, role = it.role, content = it.content, isStreaming = false) 
        }
    }

    private suspend fun getOrCreateSession(): Long {
        return currentSessionId ?: run {
            val newSession = SessionEntity(title = "New Chat")
            val id = sessionDao.insertSession(newSession)
            currentSessionId = id
            id
        }
    }
}

data class ChatUiMessage(
    val id: Long,
    val role: String,
    val content: String,
    val isStreaming: Boolean = false
)
