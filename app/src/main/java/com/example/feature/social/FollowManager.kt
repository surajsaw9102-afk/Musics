package com.example.feature.social

import com.example.feature.library.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

object FollowManager {

    private val _followedUserIds = MutableStateFlow<Set<String>>(setOf("user_alex", "user_sara", "artist_synthwave_king"))
    val followedUserIds: StateFlow<Set<String>> = _followedUserIds.asStateFlow()

    private val _followedPlaylistIds = MutableStateFlow<Set<String>>(setOf("pl_neon_nights", "pl_cyber_chill"))
    val followedPlaylistIds: StateFlow<Set<String>> = _followedPlaylistIds.asStateFlow()

    // Artist follow IDs synchronized with LibraryRepository
    val followedArtistIds = LibraryRepository.state.map { state ->
        state.followedArtists.map { it.id }.toSet()
    }

    fun isFollowingArtist(artistId: String): Boolean {
        return LibraryRepository.state.value.followedArtists.any { it.id == artistId }
    }

    fun toggleFollowArtist(artistId: String) {
        LibraryRepository.toggleFollowArtist(artistId)
    }

    fun isFollowingPlaylist(playlistId: String): Boolean {
        return _followedPlaylistIds.value.contains(playlistId)
    }

    fun toggleFollowPlaylist(playlistId: String) {
        val current = _followedPlaylistIds.value.toMutableSet()
        if (current.contains(playlistId)) {
            current.remove(playlistId)
        } else {
            current.add(playlistId)
        }
        _followedPlaylistIds.value = current
    }

    fun isFollowingUser(userId: String): Boolean {
        return _followedUserIds.value.contains(userId)
    }

    fun toggleFollowUser(userId: String) {
        val current = _followedUserIds.value.toMutableSet()
        if (current.contains(userId)) {
            current.remove(userId)
        } else {
            current.add(userId)
        }
        _followedUserIds.value = current
    }
}
