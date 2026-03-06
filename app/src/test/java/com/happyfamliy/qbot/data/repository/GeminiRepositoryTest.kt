package com.happyfamliy.qbot.data.repository

import com.happyfamliy.qbot.data.local.entity.MessageEntity
import com.happyfamliy.qbot.domain.repository.GenerativeModelWrapper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GeminiRepositoryTest {

    private lateinit var chatWrapper: GenerativeModelWrapper
    private lateinit var repository: GeminiRepositoryImpl

    @Before
    fun setup() {
        chatWrapper = mockk()
        repository = GeminiRepositoryImpl(chatWrapper)
    }

    @Test
    fun `sendMessageStream maps history and returns text flow`() = runTest {
        val history = listOf(
            MessageEntity(sessionId = 1, role = "user", content = "Hello"),
            MessageEntity(sessionId = 1, role = "model", content = "Hi!")
        )
        val prompt = "How are you?"

        every { chatWrapper.startChatAndSendStream(any(), prompt) } returns flowOf("I am ", "good.")

        val resultList = repository.sendMessageStream(history, prompt).toList()

        assertEquals(2, resultList.size)
        assertEquals("I am ", resultList[0])
        assertEquals("good.", resultList[1])
    }
}
