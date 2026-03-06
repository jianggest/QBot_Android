package com.happyfamliy.qbot.ui.chat

import com.happyfamliy.qbot.data.local.dao.MessageDao
import com.happyfamliy.qbot.data.local.dao.SessionDao
import com.happyfamliy.qbot.data.local.entity.MessageEntity
import com.happyfamliy.qbot.domain.repository.GeminiRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private lateinit var sessionDao: SessionDao
    private lateinit var messageDao: MessageDao
    private lateinit var repository: GeminiRepository
    private lateinit var viewModel: ChatViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        sessionDao = mockk(relaxed = true)
        messageDao = mockk(relaxed = true)
        repository = mockk(relaxed = true)

        viewModel = ChatViewModel(sessionDao, messageDao, repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sendMessage clears input and sets generating`() = runTest(testDispatcher) {
        viewModel.onInputTextChanged("Hello World")
        
        coEvery { sessionDao.insertSession(any()) } returns 1L
        coEvery { messageDao.insertMessage(any()) } returns 1L
        coEvery { messageDao.getMessagesForSession(any()) } returns flowOf(
            listOf(MessageEntity(id = 1, sessionId = 1, role = "user", content = "Hello World"))
        )
        every { repository.sendMessageStream(any(), any()) } returns flowOf("Hi there!")
        
        viewModel.sendMessage()
        
        assertEquals("", viewModel.inputText.value)
        assertEquals(true, viewModel.isGenerating.value)

        // Run all coroutines to completion
        advanceUntilIdle()

        assertEquals(false, viewModel.isGenerating.value)
    }
}
