package com.example.feature.recommendation

import com.example.core.database.entities.SongEntity

interface RecommendationModule {
    suspend fun getPersonalizedRecommendations(userId: String): List<String>
    fun getTasteProfile(): UserTasteProfile
    fun recordSongPlay(song: SongEntity, playedDurationMs: Long = 0L)
    fun recordSongSkip(song: SongEntity)
    fun recordSongLike(song: SongEntity, isLiked: Boolean)
}

object DefaultRecommendationModule : RecommendationModule {
    override suspend fun getPersonalizedRecommendations(userId: String): List<String> {
        val profile = UserSignalTracker.tasteProfile.value
        return RecommendationEngine.getYourChoicesTracks(profile).map { it.id }
    }

    override fun getTasteProfile(): UserTasteProfile {
        return UserSignalTracker.tasteProfile.value
    }

    override fun recordSongPlay(song: SongEntity, playedDurationMs: Long) {
        UserSignalTracker.recordSongPlay(song, playedDurationMs)
    }

    override fun recordSongSkip(song: SongEntity) {
        UserSignalTracker.recordSongSkip(song)
    }

    override fun recordSongLike(song: SongEntity, isLiked: Boolean) {
        UserSignalTracker.recordSongLike(song, isLiked)
    }
}
