package com.example.feature.library

import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity
import com.example.feature.recommendation.UserSignalTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object LibraryRepository {

    private val initialLikedSongIds = mutableSetOf("song_101", "song_103", "song_105")
    private val initialSavedAlbumIds = mutableSetOf("alb_1", "alb_2")
    private val initialFollowedArtistIds = mutableSetOf("art_1", "art_2")

    private val initialPinnedPlaylistIds = mutableSetOf("pl_1")
    private val initialPinnedSongIds = mutableSetOf("song_101")
    private val initialPinnedAlbumIds = mutableSetOf("alb_1")
    private val initialPinnedArtistIds = mutableSetOf("art_1")

    private val initialPlaylists = mutableListOf(
        UserPlaylist(
            id = "pl_1",
            name = "Late Night Cyber Vibe",
            description = "Glassmorphic chill synth, zero-gravity ambient & lo-fi beats",
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500",
            ownerId = "usr_9981",
            ownerName = "Created by You",
            songIds = listOf("song_101", "song_102", "song_103", "song_105"),
            pinned = true,
            createdAt = System.currentTimeMillis() - 86400000L * 5
        ),
        UserPlaylist(
            id = "pl_2",
            name = "High Voltage Focus",
            description = "Deep electronic instrumentals with 24-bit/96kHz lossless clarity",
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
            ownerId = "usr_9981",
            ownerName = "Created by You",
            songIds = listOf("song_102", "song_106", "song_108"),
            pinned = false,
            createdAt = System.currentTimeMillis() - 86400000L * 2
        ),
        UserPlaylist(
            id = "pl_3",
            name = "Acoustic Sunset Chill",
            description = "Warm analog guitar, atmospheric rain & vinyl lofi",
            coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500",
            ownerId = "usr_9981",
            ownerName = "Created by You",
            songIds = listOf("song_103", "song_104", "song_107"),
            pinned = false,
            createdAt = System.currentTimeMillis() - 86400000L * 10
        )
    )

    private val initialHistory = mutableListOf("song_101", "song_103", "song_102", "song_104", "song_105")
    private val initialPlayCountMap = mutableMapOf(
        "song_101" to 14,
        "song_102" to 9,
        "song_103" to 18,
        "song_104" to 6,
        "song_105" to 11
    )

    private val _state = MutableStateFlow(
        LibraryDataState(
            playlists = initialPlaylists.toList(),
            likedSongs = initialLikedSongIds.mapNotNull { MusicCatalog.getSongById(it) },
            savedAlbums = initialSavedAlbumIds.mapNotNull { MusicCatalog.getAlbumById(it) },
            followedArtists = initialFollowedArtistIds.mapNotNull { MusicCatalog.getArtistById(it) },
            pinnedPlaylistIds = initialPinnedPlaylistIds.toSet(),
            pinnedSongIds = initialPinnedSongIds.toSet(),
            pinnedAlbumIds = initialPinnedAlbumIds.toSet(),
            pinnedArtistIds = initialPinnedArtistIds.toSet(),
            historySongs = initialHistory.mapNotNull { MusicCatalog.getSongById(it) },
            playCountMap = initialPlayCountMap.toMap()
        )
    )

    val state: StateFlow<LibraryDataState> = _state.asStateFlow()

    private fun updateState() {
        val current = _state.value
        _state.value = current.copy(
            playlists = initialPlaylists.toList(),
            likedSongs = initialLikedSongIds.mapNotNull { MusicCatalog.getSongById(it) },
            savedAlbums = initialSavedAlbumIds.mapNotNull { MusicCatalog.getAlbumById(it) },
            followedArtists = initialFollowedArtistIds.mapNotNull { MusicCatalog.getArtistById(it) },
            pinnedPlaylistIds = initialPinnedPlaylistIds.toSet(),
            pinnedSongIds = initialPinnedSongIds.toSet(),
            pinnedAlbumIds = initialPinnedAlbumIds.toSet(),
            pinnedArtistIds = initialPinnedArtistIds.toSet(),
            historySongs = initialHistory.mapNotNull { MusicCatalog.getSongById(it) },
            playCountMap = initialPlayCountMap.toMap()
        )
    }

    // --- Like / Save / Follow Actions ---

    fun toggleLikeSong(songId: String) {
        val song = MusicCatalog.getSongById(songId) ?: return
        if (initialLikedSongIds.contains(songId)) {
            initialLikedSongIds.remove(songId)
            UserSignalTracker.recordSongLike(song, false)
        } else {
            initialLikedSongIds.add(songId)
            UserSignalTracker.recordSongLike(song, true)
        }
        updateState()
    }

    fun isSongLiked(songId: String): Boolean {
        return initialLikedSongIds.contains(songId)
    }

    fun toggleSaveAlbum(albumId: String) {
        if (initialSavedAlbumIds.contains(albumId)) {
            initialSavedAlbumIds.remove(albumId)
        } else {
            initialSavedAlbumIds.add(albumId)
        }
        updateState()
    }

    fun isAlbumSaved(albumId: String): Boolean {
        return initialSavedAlbumIds.contains(albumId)
    }

    fun toggleFollowArtist(artistId: String) {
        if (initialFollowedArtistIds.contains(artistId)) {
            initialFollowedArtistIds.remove(artistId)
        } else {
            initialFollowedArtistIds.add(artistId)
        }
        updateState()
    }

    fun isArtistFollowed(artistId: String): Boolean {
        return initialFollowedArtistIds.contains(artistId)
    }

    // --- Pin Actions ---

    fun togglePinPlaylist(playlistId: String) {
        if (initialPinnedPlaylistIds.contains(playlistId)) {
            initialPinnedPlaylistIds.remove(playlistId)
        } else {
            initialPinnedPlaylistIds.add(playlistId)
        }
        val idx = initialPlaylists.indexOfFirst { it.id == playlistId }
        if (idx != -1) {
            val pl = initialPlaylists[idx]
            initialPlaylists[idx] = pl.copy(pinned = initialPinnedPlaylistIds.contains(playlistId))
        }
        updateState()
    }

    fun togglePinSong(songId: String) {
        if (initialPinnedSongIds.contains(songId)) {
            initialPinnedSongIds.remove(songId)
        } else {
            initialPinnedSongIds.add(songId)
        }
        updateState()
    }

    fun togglePinAlbum(albumId: String) {
        if (initialPinnedAlbumIds.contains(albumId)) {
            initialPinnedAlbumIds.remove(albumId)
        } else {
            initialPinnedAlbumIds.add(albumId)
        }
        updateState()
    }

    fun togglePinArtist(artistId: String) {
        if (initialPinnedArtistIds.contains(artistId)) {
            initialPinnedArtistIds.remove(artistId)
        } else {
            initialPinnedArtistIds.add(artistId)
        }
        updateState()
    }

    // --- Playlist Management Actions ---

    fun createPlaylist(
        name: String,
        description: String,
        coverUrl: String = "",
        initialSongIds: List<String> = emptyList()
    ): UserPlaylist {
        val newId = "pl_${UUID.randomUUID().toString().take(8)}"
        val defaultCover = if (coverUrl.isBlank()) {
            "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500"
        } else coverUrl

        val playlist = UserPlaylist(
            id = newId,
            name = name,
            description = description,
            coverUrl = defaultCover,
            ownerId = "usr_9981",
            ownerName = "Created by You",
            songIds = initialSongIds,
            pinned = false,
            createdAt = System.currentTimeMillis()
        )
        initialPlaylists.add(0, playlist)
        updateState()
        return playlist
    }

    fun updatePlaylistDetails(playlistId: String, name: String, description: String, coverUrl: String) {
        val idx = initialPlaylists.indexOfFirst { it.id == playlistId }
        if (idx != -1) {
            val existing = initialPlaylists[idx]
            val updatedCover = if (coverUrl.isBlank()) existing.coverUrl else coverUrl
            initialPlaylists[idx] = existing.copy(
                name = name,
                description = description,
                coverUrl = updatedCover
            )
            updateState()
        }
    }

    fun deletePlaylist(playlistId: String) {
        initialPlaylists.removeAll { it.id == playlistId }
        initialPinnedPlaylistIds.remove(playlistId)
        updateState()
    }

    fun addSongsToPlaylist(playlistId: String, songIds: List<String>) {
        val idx = initialPlaylists.indexOfFirst { it.id == playlistId }
        if (idx != -1) {
            val existing = initialPlaylists[idx]
            val updatedSongIds = (existing.songIds + songIds).distinct()
            initialPlaylists[idx] = existing.copy(songIds = updatedSongIds)
            updateState()
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        val idx = initialPlaylists.indexOfFirst { it.id == playlistId }
        if (idx != -1) {
            val existing = initialPlaylists[idx]
            val updatedSongIds = existing.songIds.filter { it != songId }
            initialPlaylists[idx] = existing.copy(songIds = updatedSongIds)
            updateState()
        }
    }

    fun reorderSongsInPlaylist(playlistId: String, fromIndex: Int, toIndex: Int) {
        val idx = initialPlaylists.indexOfFirst { it.id == playlistId }
        if (idx != -1) {
            val existing = initialPlaylists[idx]
            val mutableSongs = existing.songIds.toMutableList()
            if (fromIndex in mutableSongs.indices && toIndex in mutableSongs.indices) {
                val item = mutableSongs.removeAt(fromIndex)
                mutableSongs.add(toIndex, item)
                initialPlaylists[idx] = existing.copy(songIds = mutableSongs)
                updateState()
            }
        }
    }

    fun duplicatePlaylist(playlistId: String): UserPlaylist? {
        val existing = initialPlaylists.find { it.id == playlistId } ?: return null
        val copyName = "${existing.name} (Copy)"
        return createPlaylist(
            name = copyName,
            description = existing.description,
            coverUrl = existing.coverUrl,
            initialSongIds = existing.songIds
        )
    }

    fun getPlaylistById(playlistId: String): UserPlaylist? {
        return initialPlaylists.find { it.id == playlistId }
    }

    // --- Record Playback & Filters ---

    fun recordSongPlay(songId: String) {
        val song = MusicCatalog.getSongById(songId) ?: return
        initialHistory.remove(songId)
        initialHistory.add(0, songId)
        val currentCount = initialPlayCountMap[songId] ?: 0
        initialPlayCountMap[songId] = currentCount + 1

        UserSignalTracker.recordSongPlay(song)
        updateState()
    }

    fun setFilter(filter: FilterType) {
        _state.value = _state.value.copy(activeFilter = filter)
    }

    fun setSort(sort: SortOption) {
        _state.value = _state.value.copy(activeSort = sort)
    }

    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }
}
