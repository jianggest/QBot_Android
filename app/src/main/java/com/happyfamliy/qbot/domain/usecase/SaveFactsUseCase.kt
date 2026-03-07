package com.happyfamliy.qbot.domain.usecase

import com.happyfamliy.qbot.data.local.dao.FactDao
import com.happyfamliy.qbot.data.local.entity.FactEntity
import com.happyfamliy.qbot.domain.repository.EmbeddingRepository
import com.happyfamliy.qbot.util.CosineSimilarityUtil
import javax.inject.Inject

/**
 * Core smart memory use case.
 *
 * For each extracted fact:
 * 1. Generate a vector embedding via the Gemini embedding model.
 * 2. Compare against all stored fact embeddings using cosine similarity.
 * 3. If similarity > DUPLICATE_THRESHOLD → update the existing fact's accessCount + 1.
 * 4. Otherwise → insert as a brand-new FactEntity.
 */
class SaveFactsUseCase @Inject constructor(
    private val factDao: FactDao,
    private val embeddingRepository: EmbeddingRepository
) {
    companion object {
        const val DUPLICATE_THRESHOLD = 0.92f
    }

    suspend operator fun invoke(facts: List<ExtractedFact>) {
        if (facts.isEmpty()) return

        // Load all existing facts once for batch comparison
        val existingFacts = factDao.getAllFactsSync()

        for (extractedFact in facts) {
            val newEmbedding = embeddingRepository.getEmbedding(extractedFact.fact)

            // Find the most similar existing fact
            val bestMatch = existingFacts
                .map { existing ->
                    val existingEmbedding = CosineSimilarityUtil.stringToFloatArray(existing.embedding)
                    val similarity = CosineSimilarityUtil.cosineSimilarity(newEmbedding, existingEmbedding)
                    existing to similarity
                }
                .maxByOrNull { it.second }

            if (bestMatch != null && bestMatch.second > DUPLICATE_THRESHOLD) {
                // Duplicate detected – increment access count
                val updated = bestMatch.first.copy(
                    accessCount = bestMatch.first.accessCount + 1,
                    lastUpdatedAt = System.currentTimeMillis()
                )
                factDao.updateFact(updated)
            } else {
                // New fact – insert
                val embeddingStr = CosineSimilarityUtil.floatArrayToString(newEmbedding)
                factDao.insertFact(
                    FactEntity(
                        content = extractedFact.fact,
                        topic = extractedFact.topic,
                        embedding = embeddingStr
                    )
                )
            }
        }
    }
}
