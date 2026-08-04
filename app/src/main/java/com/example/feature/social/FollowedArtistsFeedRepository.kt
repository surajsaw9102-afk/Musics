package com.example.feature.social

import com.example.core.catalog.MusicCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FollowedArtistsFeedRepository {

    private val releases = listOf(
        FollowedArtistRelease(
            artistId = "a1",
            artistName = "Synthwave King",
            artistAvatar = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300",
            releaseTitle = "Neon Skyline 2026",
            releaseType = "Album",
            coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600",
            releaseDate = "Just Released",
            songs = MusicCatalog.ALL_SONGS.take(3)
        ),
        FollowedArtistRelease(
            artistId = "a2",
            artistName = "Retro Dreamer",
            artistAvatar = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300",
            releaseTitle = "Midnight Echoes (Remix)",
            releaseType = "Single",
            coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600",
            releaseDate = "Yesterday",
            songs = MusicCatalog.ALL_SONGS.drop(3).take(2)
        )
    )

    private val _artistReleases = MutableStateFlow<List<FollowedArtistRelease>>(releases)
    val artistReleases: StateFlow<List<FollowedArtistRelease>> = _artistReleases.asStateFlow()
}
