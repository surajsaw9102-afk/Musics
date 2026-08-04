package com.example.core.ai

import com.example.core.database.entities.SongEntity

object AiInsightGenerator {

    private val provider: AiProvider = GeminiAiProvider()

    suspend fun generateUserInsights(history: List<SongEntity>, likes: List<SongEntity>): MusicInsights {
        return provider.generateInsights(history, likes)
    }
}
