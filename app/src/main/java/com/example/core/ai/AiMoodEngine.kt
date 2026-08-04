package com.example.core.ai

import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.SongEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AiMoodEngine {

    private val provider: AiProvider = GeminiAiProvider()

    private val _currentMood = MutableStateFlow<MoodType?>(null)
    val currentMood: StateFlow<MoodType?> = _currentMood.asStateFlow()

    fun setMood(mood: MoodType?) {
        _currentMood.value = mood
    }

    suspend fun getSongsForMood(mood: MoodType, catalog: List<SongEntity> = MusicCatalog.ALL_SONGS): List<SongEntity> {
        return provider.getMoodMusic(mood, catalog)
    }
}
