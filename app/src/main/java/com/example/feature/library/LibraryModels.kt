package com.example.feature.library

import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity

data class UserPlaylist(
    val id: String,
    val name: String,
    val description: String = "",
    val coverUrl: String = "",
    val ownerId: String = "usr_9981",
    val ownerName: String = "Created by You",
    val songIds: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val pinned: Boolean = false
)

enum class SortOption(val label: String) {
    RECENTLY_ADDED("Recently Added"),
    RECENTLY_PLAYED("Recently Played"),
    MOST_PLAYED("Most Played"),
    ALPHABETICAL("Alphabetical A-Z")
}

enum class FilterType(val label: String) {
    ALL("All"),
    PLAYLISTS("Playlists"),
    SONGS("Liked Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    PINNED("Pinned"),
    HISTORY("History")
}

data class LibraryDataState(
    val playlists: List<UserPlaylist> = emptyList(),
    val likedSongs: List<SongEntity> = emptyList(),
    val savedAlbums: List<AlbumEntity> = emptyList(),
    val followedArtists: List<ArtistEntity> = emptyList(),
    val pinnedPlaylistIds: Set<String> = emptySet(),
    val pinnedSongIds: Set<String> = emptySet(),
    val pinnedAlbumIds: Set<String> = emptySet(),
    val pinnedArtistIds: Set<String> = emptySet(),
    val historySongs: List<SongEntity> = emptyList(),
    val playCountMap: Map<String, Int> = emptyMap(),
    val activeFilter: FilterType = FilterType.ALL,
    val activeSort: SortOption = SortOption.RECENTLY_ADDED,
    val searchQuery: String = "",
    val isLoading: Boolean = false
)
