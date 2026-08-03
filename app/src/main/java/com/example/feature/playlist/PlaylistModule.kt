package com.example.feature.playlist

interface PlaylistModule {
    suspend fun createPlaylist(name: String, description: String): String
    suspend fun addTrackToPlaylist(playlistId: String, songId: String): Boolean
}
