package com.example.core.ai

import com.example.BuildConfig
import com.example.core.database.entities.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiProvider : AiProvider {

    private val localFallback = LocalRuleAiProvider()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String? {
        val key = try {
            BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String
        } catch (e: Exception) {
            null
        }
        return if (!key.isNull_or_blank() && key != "MY_GEMINI_API_KEY") key else null
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()

    override suspend fun generateAssistantResponse(prompt: String, context: AiContext): AiChatMessage = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey == null) {
            return@withContext localFallback.generateAssistantResponse(prompt, context)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val systemPrompt = """
                You are Aura AI, an intelligent, modern music assistant inside a music streaming app.
                Respond to the user request strictly as a music guide.
                Return brief, friendly conversational text.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().put("text", "System: $systemPrompt\nUser request: $prompt"))
                        })
                    })
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string()

            if (response.isSuccessful && !responseString.isNullOrBlank()) {
                val jsonResp = JSONObject(responseString)
                val candidates = jsonResp.optJSONArray("candidates")
                val text = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                if (!text.isNullOrBlank()) {
                    // Combine with local catalog matching for tracks and chips
                    val localMatch = localFallback.generateAssistantResponse(prompt, context)
                    return@withContext localMatch.copy(
                        sender = "ASSISTANT",
                        text = text.trim()
                    )
                }
            }
        } catch (e: Exception) {
            // Fallback gracefully on any API or network issue
        }

        return@withContext localFallback.generateAssistantResponse(prompt, context)
    }

    override suspend fun interpretSearchQuery(query: String, catalog: List<SongEntity>): SearchFilterResult {
        return localFallback.interpretSearchQuery(query, catalog)
    }

    override suspend fun generatePlaylist(
        prompt: String,
        mood: MoodType?,
        context: AiContext,
        catalog: List<SongEntity>
    ): GeneratedPlaylistResult {
        return localFallback.generatePlaylist(prompt, mood, context, catalog)
    }

    override suspend fun getRecommendations(
        context: AiContext,
        catalog: List<SongEntity>
    ): List<RecommendationSection> {
        return localFallback.getRecommendations(context, catalog)
    }

    override suspend fun generateInsights(
        history: List<SongEntity>,
        likes: List<SongEntity>
    ): MusicInsights {
        return localFallback.generateInsights(history, likes)
    }

    override suspend fun getSmartQueue(
        currentSong: SongEntity?,
        mode: SmartQueueMode,
        catalog: List<SongEntity>
    ): List<SongEntity> {
        return localFallback.getSmartQueue(currentSong, mode, catalog)
    }

    override suspend fun getMoodMusic(mood: MoodType, catalog: List<SongEntity>): List<SongEntity> {
        return localFallback.getMoodMusic(mood, catalog)
    }

    override suspend fun getDjHostSpeech(song: SongEntity?, mood: MoodType?): DjHostSpeech {
        return localFallback.getDjHostSpeech(song, mood)
    }
}
