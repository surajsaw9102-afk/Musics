package com.example.core.ai

import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.SongEntity

object AiDiscoveryComposer {

    suspend fun composeHomeDiscoverySections(context: AiContext): List<RecommendationSection> {
        return AiRecommendationEngine.fetchRecommendations(context, MusicCatalog.ALL_SONGS)
    }
}
