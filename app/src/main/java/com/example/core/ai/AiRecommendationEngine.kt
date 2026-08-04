package com.example.core.ai

import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.SongEntity

object AiRecommendationEngine {

    private val provider: AiProvider = GeminiAiProvider()

    suspend fun fetchRecommendations(context: AiContext, catalog: List<SongEntity> = MusicCatalog.ALL_SONGS): List<RecommendationSection> {
        return provider.getRecommendations(context, catalog)
    }
}
