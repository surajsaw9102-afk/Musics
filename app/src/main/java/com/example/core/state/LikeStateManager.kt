package com.example.core.state

import com.example.feature.library.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object LikeStateManager {

    val savedAlbumIds: Flow<Set<String>> = LibraryRepository.state.map { state ->
        state.savedAlbums.map { it.id }.toSet()
    }

    val likedTrackIds: Flow<Set<String>> = LibraryRepository.state.map { state ->
        state.likedSongs.map { it.id }.toSet()
    }

    fun isAlbumLiked(albumId: String): Boolean {
        return LibraryRepository.state.value.savedAlbums.any { it.id == albumId }
    }

    fun toggleAlbumLike(albumId: String) {
        LibraryRepository.toggleSaveAlbum(albumId)
    }

    fun isTrackLiked(trackId: String): Boolean {
        return LibraryRepository.state.value.likedSongs.any { it.id == trackId }
    }

    fun toggleTrackLike(trackId: String) {
        LibraryRepository.toggleLikeSong(trackId)
    }
}
