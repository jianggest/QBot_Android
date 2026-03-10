package com.happyfamliy.qbot.data.repository

import android.util.Log
import com.happyfamliy.qbot.BuildConfig
import com.happyfamliy.qbot.domain.repository.EmbeddingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/**
 * Manual implementation of EmbeddingRepository using the Gemini REST API.
 * The Google AI SDK for Android doesn't seem to expose the embedContent method in its current stable version.
 */
class EmbeddingRepositoryImpl @Inject constructor() : EmbeddingRepository {

    override suspend fun getEmbedding(text: String): FloatArray = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val model = "gemini-embedding-001"
        val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/$model:embedContent?key=$apiKey"

        val connection = URL(apiUrl).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val requestBody = JSONObject().apply {
                put("content", JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply { put("text", text) })
                    })
                })
            }

            connection.outputStream.use { it.write(requestBody.toString().toByteArray()) }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().readText()
                Log.d("QBot/Embedding", "Embedding API success, response length: ${responseText.length}")
                val responseJson = JSONObject(responseText)
                val values = responseJson.getJSONObject("embedding").getJSONArray("values")
                val floatArray = FloatArray(values.length())
                for (i in 0 until values.length()) {
                    floatArray[i] = values.getDouble(i).toFloat()
                }
                Log.d("QBot/Embedding", "Embedding vector size: ${floatArray.size}")
                floatArray
            } else {
                val errorMsg = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                Log.e("QBot/Embedding", "Embedding API failed with code $responseCode: $errorMsg")
                throw Exception("Embedding API failed with code $responseCode: $errorMsg")
            }
        } finally {
            connection.disconnect()
        }
    }
}
