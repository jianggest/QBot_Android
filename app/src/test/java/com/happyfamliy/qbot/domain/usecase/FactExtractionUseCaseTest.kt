package com.happyfamliy.qbot.domain.usecase

import com.happyfamliy.qbot.data.local.entity.MessageEntity
import com.happyfamliy.qbot.domain.repository.GenerativeModelWrapper
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FactExtractionUseCaseTest {

    private lateinit var jsonWrapper: GenerativeModelWrapper
    private lateinit var useCase: FactExtractionUseCase

    @Before
    fun setup() {
        jsonWrapper = mockk()
        useCase = FactExtractionUseCase(jsonWrapper)
    }

    @Test
    fun `invoke analyzes history and returns extracted facts`() = runTest {
        val history = listOf(
            MessageEntity(sessionId = 1, role = "user", content = "I like green color"),
            MessageEntity(sessionId = 1, role = "model", content = "Okay, understood.")
        )

        // Simulate model returning a JSON array (possibly with markdown fencing)
        coEvery { jsonWrapper.generateContent(any()) } returns """
            ```json
            [
              {"fact": "User likes the color green", "topic": "Preferences"}
            ]
            ```
        """.trimIndent()

        val result = useCase(history)

        assertEquals(1, result.size)
        assertEquals("User likes the color green", result[0].fact)
        assertEquals("Preferences", result[0].topic)
    }

    @Test
    fun `invoke with empty history returns empty list`() = runTest {
        val result = useCase(emptyList())
        assertEquals(0, result.size)
    }

    @Test
    fun `invoke handles empty json array response`() = runTest {
        val history = listOf(
            MessageEntity(sessionId = 1, role = "user", content = "Hello"),
        )
        coEvery { jsonWrapper.generateContent(any()) } returns "[]"

        val result = useCase(history)
        assertEquals(0, result.size)
    }
}
