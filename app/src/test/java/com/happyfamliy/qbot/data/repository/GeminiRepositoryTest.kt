package com.happyfamliy.qbot.data.repository

import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.happyfamliy.qbot.data.local.entity.MessageEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GeminiRepositoryTest {

    private lateinit var generativeModel: GenerativeModel
    private lateinit var chat: Chat
    private lateinit var repository: GeminiRepositoryImpl

    @Before
    fun setup() {
        generativeModel = mockk()
        chat = mockk()
        // Mock startChat to return our mocked Chat instance
        every { generativeModel.startChat(any()) } returns chat
        repository = GeminiRepositoryImpl(generativeModel)
    }

    @Test
    fun `sendMessageStream maps history and returns text flow`() = runTest {
        val history = listOf(
            MessageEntity(sessionId = 1, role = "user", content = "Hello"),
            MessageEntity(sessionId = 1, role = "model", content = "Hi!")
        )
        val prompt = "How are you?"
        
        // Mock responses
        val mockResponse1 = mockk<GenerateContentResponse>()
        val mockResponse2 = mockk<GenerateContentResponse>()
        every { mockResponse1.text } returns "I am "
        every { mockResponse2.text } returns "good."
        
        // Mock sendMessageStream
        every { chat.sendMessageStream(prompt) } returns flowOf(mockResponse1, mockResponse2)

        val resultFlow = repository.sendMessageStream(history, prompt)
        val resultList = resultFlow.toList()

        assertEquals(2, resultList.size)
        assertEquals("I am ", resultList[0])
        assertEquals("good.", resultList[1])
    }
}
