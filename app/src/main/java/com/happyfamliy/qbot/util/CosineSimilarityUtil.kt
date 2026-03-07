package com.happyfamliy.qbot.util

object CosineSimilarityUtil {

    /**
     * Computes the cosine similarity between two float vectors.
     * Returns a value in [-1.0, 1.0]. Returns 0.0 if either vector is zero-magnitude.
     */
    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size) { "Vectors must have the same dimension (${a.size} vs ${b.size})" }

        var dotProduct = 0f
        var magA = 0f
        var magB = 0f

        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            magA += a[i] * a[i]
            magB += b[i] * b[i]
        }

        val denominator = Math.sqrt((magA * magB).toDouble()).toFloat()
        return if (denominator == 0f) 0f else dotProduct / denominator
    }

    /**
     * Serializes a FloatArray to a compact comma-separated String for Room storage.
     */
    fun floatArrayToString(array: FloatArray): String = array.joinToString(",")

    /**
     * Deserializes a comma-separated String back to a FloatArray.
     */
    fun stringToFloatArray(str: String): FloatArray {
        if (str.isBlank()) return FloatArray(0)
        return str.split(",").map { it.toFloat() }.toFloatArray()
    }
}
