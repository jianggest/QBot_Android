package com.happyfamliy.qbot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class CosineSimilarityUtilTest {

    @Test
    fun `cosineSimilarity of identical vectors is 1`() {
        val v = floatArrayOf(1f, 2f, 3f)
        val result = CosineSimilarityUtil.cosineSimilarity(v, v)
        assertEquals(1f, result, 0.0001f)
    }

    @Test
    fun `cosineSimilarity of orthogonal vectors is 0`() {
        val a = floatArrayOf(1f, 0f, 0f)
        val b = floatArrayOf(0f, 1f, 0f)
        val result = CosineSimilarityUtil.cosineSimilarity(a, b)
        assertEquals(0f, result, 0.0001f)
    }

    @Test
    fun `cosineSimilarity of zero vector returns 0`() {
        val zero = floatArrayOf(0f, 0f, 0f)
        val other = floatArrayOf(1f, 2f, 3f)
        val result = CosineSimilarityUtil.cosineSimilarity(zero, other)
        assertEquals(0f, result, 0.0001f)
    }

    @Test
    fun `floatArrayToString and stringToFloatArray are inverse operations`() {
        val original = floatArrayOf(0.1f, 0.5f, -0.3f, 1.0f)
        val serialized = CosineSimilarityUtil.floatArrayToString(original)
        val restored = CosineSimilarityUtil.stringToFloatArray(serialized)

        assertEquals(original.size, restored.size)
        for (i in original.indices) {
            assertEquals(original[i], restored[i], 0.0001f)
        }
    }

    @Test
    fun `stringToFloatArray handles blank string`() {
        val result = CosineSimilarityUtil.stringToFloatArray("")
        assertEquals(0, result.size)
    }
}
