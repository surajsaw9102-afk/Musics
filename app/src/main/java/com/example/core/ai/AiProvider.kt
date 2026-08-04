package com.example.core.ai

import com.example.core.database.entities.SongEntity

interface AiProvider {
    suspend fun generateAssistantResponse(prompt: String, context: AiContext): AiChatMessage
    suspend fun interpretSearchQuery(query: String, catalog: List<SongEntity>): SearchFilterResult
    suspend fun generatePlaylist(prompt: String, mood: MoodType?, context: AiContext, catalog: List<SongEntity>): GeneratedPlaylistResult
    suspend fun getRecommendations(context: AiContext, catalog: List<SongEntity>): List<RecommendationSection>
    suspend fun generateInsights(history: List<SongEntity>, likes: List<SongEntity>): MusicInsights
    suspend fun getSmartQueue(currentSong: SongEntity?, mode: SmartQueueMode, catalog: List<SongEntity>): List<SongEntity>
    suspend fun getMoodMusic(mood: MoodType, catalog: List<SongEntity>): List<SongEntity>
    suspend fun getDjHostSpeech(song: SongEntity?, mood: MoodType?): DjHostSpeech
}
