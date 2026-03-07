package com.happyfamliy.qbot.domain.repository

/**
 * Repository for generating text embeddings via Gemini.
 */
interface EmbeddingRepository {
    suspend fun getEmbedding(text: String): FloatArray
}
