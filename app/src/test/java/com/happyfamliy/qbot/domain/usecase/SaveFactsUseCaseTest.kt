package com.happyfamliy.qbot.domain.usecase

import com.happyfamliy.qbot.data.local.dao.FactDao
import com.happyfamliy.qbot.data.local.entity.FactEntity
import com.happyfamliy.qbot.domain.repository.EmbeddingRepository
import com.happyfamliy.qbot.util.CosineSimilarityUtil
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SaveFactsUseCaseTest {

    private lateinit var factDao: FactDao
    private lateinit var embeddingRepository: EmbeddingRepository
    private lateinit var useCase: SaveFactsUseCase

    @Before
    fun setup() {
        factDao = mockk(relaxed = true)
        embeddingRepository = mockk()
        useCase = SaveFactsUseCase(factDao, embeddingRepository)
    }

    @Test
    fun `invoke inserts new fact when no similar facts exist`() = runTest {
        val facts = listOf(ExtractedFact("User likes green", "Preferences"))
        val newEmbedding = floatArrayOf(1f, 0f, 0f)

        coEvery { factDao.getAllFactsSync() } returns emptyList()
        coEvery { embeddingRepository.getEmbedding(any()) } returns newEmbedding
        coEvery { factDao.insertFact(any()) } returns 1L

        useCase(facts)

        coVerify(exactly = 1) { factDao.insertFact(any()) }
        coVerify(exactly = 0) { factDao.updateFact(any()) }
    }

    @Test
    fun `invoke updates existing fact when similarity is above threshold`() = runTest {
        val existingEmbedding = floatArrayOf(1f, 0f, 0f)
        val existingFact = FactEntity(
            id = 1,
            content = "User likes green color",
            topic = "Preferences",
            embedding = CosineSimilarityUtil.floatArrayToString(existingEmbedding),
            accessCount = 1
        )
        val facts = listOf(ExtractedFact("User likes green", "Preferences"))
        // Same direction → cosine similarity = 1.0, which is > 0.92
        val newEmbedding = floatArrayOf(1f, 0f, 0f)

        coEvery { factDao.getAllFactsSync() } returns listOf(existingFact)
        coEvery { embeddingRepository.getEmbedding(any()) } returns newEmbedding
        coEvery { factDao.updateFact(any()) } just runs

        useCase(facts)

        val updatedSlot = slot<FactEntity>()
        coVerify(exactly = 0) { factDao.insertFact(any()) }
        coVerify(exactly = 1) { factDao.updateFact(capture(updatedSlot)) }
        assertEquals(2, updatedSlot.captured.accessCount)
    }

    @Test
    fun `invoke does nothing for empty facts list`() = runTest {
        useCase(emptyList())

        coVerify(exactly = 0) { factDao.getAllFactsSync() }
        coVerify(exactly = 0) { factDao.insertFact(any()) }
        coVerify(exactly = 0) { factDao.updateFact(any()) }
    }
}
