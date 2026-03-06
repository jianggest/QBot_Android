package com.happyfamliy.qbot.domain.usecase

import com.happyfamliy.qbot.data.local.entity.MessageEntity
import com.happyfamliy.qbot.domain.repository.GenerativeModelWrapper
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Named

class FactExtractionUseCase @Inject constructor(
    @Named("jsonWrapper") private val jsonWrapper: GenerativeModelWrapper
) {
    suspend operator fun invoke(history: List<MessageEntity>): List<ExtractedFact> {
        if (history.isEmpty()) return emptyList()

        val conversationText = history.joinToString("\n") { "${it.role}: ${it.content}" }
        val prompt = """
            Analyze the following conversation and extract any distinct atomic facts or preferences about the user.
            Output your findings strictly as a JSON array of objects.
            Each object must contain exactly two fields:
            - "fact": a concise statement of the fact
            - "topic": a short category or topic for this fact
            
            If there are no new facts, output an empty JSON array [].
            
            Conversation:
            $conversationText
        """.trimIndent()

        val result = mutableListOf<ExtractedFact>()
        try {
            val text = jsonWrapper.generateContent(prompt) ?: "[]"
            
            // Extract the JSON array, handling optional markdown code fences
            val startIndex = text.indexOf("[")
            val endIndex = text.lastIndexOf("]")

            if (startIndex != -1 && endIndex != -1 && endIndex >= startIndex) {
                val jsonStr = text.substring(startIndex, endIndex + 1)
                val jsonArray = JSONArray(jsonStr)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    if (obj.has("fact") && obj.has("topic")) {
                        result.add(
                            ExtractedFact(
                                fact = obj.getString("fact"),
                                topic = obj.getString("topic")
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}

data class ExtractedFact(
    val fact: String,
    val topic: String
)
