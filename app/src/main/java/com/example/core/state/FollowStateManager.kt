package com.example.core.state

import com.example.feature.library.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object FollowStateManager {

    val followedArtistIds: Flow<Set<String>> = LibraryRepository.state.map { state ->
        state.followedArtists.map { it.id }.toSet()
    }

    fun isFollowing(artistId: String): Boolean {
        return LibraryRepository.state.value.followedArtists.any { it.id == artistId }
    }

    fun toggleFollow(artistId: String) {
        LibraryRepository.toggleFollowArtist(artistId)
    }
}
