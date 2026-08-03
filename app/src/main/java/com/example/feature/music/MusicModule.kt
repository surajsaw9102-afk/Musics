package com.example.feature.music

interface MusicModule {
    suspend fun fetchFeaturedMusic(): List<String>
    suspend fun streamAudioTrack(trackId: String): Boolean
}
