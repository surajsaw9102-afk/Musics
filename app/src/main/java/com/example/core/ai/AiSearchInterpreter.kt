package com.example.core.ai

import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.SongEntity

object AiSearchInterpreter {

    private val provider: AiProvider = GeminiAiProvider()

    suspend fun parseQuery(query: String, catalog: List<SongEntity> = MusicCatalog.ALL_SONGS): SearchFilterResult {
        return provider.interpretSearchQuery(query, catalog)
    }
}
