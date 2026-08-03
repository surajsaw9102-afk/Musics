package com.example.core.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.PlaylistEntity
import com.example.core.database.entities.SongEntity
import com.example.feature.library.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LibraryData(
    val favoriteSongs: List<SongEntity> = emptyList(),
    val savedAlbums: List<AlbumEntity> = emptyList(),
    val followedArtists: List<ArtistEntity> = emptyList(),
    val playlists: List<PlaylistEntity> = emptyList()
)

class LibraryState : ViewModel() {

    val libraryDataState: StateFlow<LibraryDataState> = LibraryRepository.state

    private val _legacyLibraryData = MutableStateFlow(LibraryData())
    val library: StateFlow<LibraryData> = _legacyLibraryData.asStateFlow()

    init {
        viewModelScope.launch {
            LibraryRepository.state.collect { data ->
                _legacyLibraryData.value = LibraryData(
                    favoriteSongs = data.likedSongs,
                    savedAlbums = data.savedAlbums,
                    followedArtists = data.followedArtists,
                    playlists = data.playlists.map { pl ->
                        PlaylistEntity(
                            id = pl.id,
                            name = pl.name,
                            description = pl.description,
                            coverUrl = pl.coverUrl,
                            ownerId = pl.ownerId,
                            trackCount = pl.songIds.size
                        )
                    }
                )
            }
        }
    }

    // --- Actions ---

    fun setFilter(filter: FilterType) {
        LibraryRepository.setFilter(filter)
    }

    fun setSort(sort: SortOption) {
        LibraryRepository.setSort(sort)
    }

    fun setSearchQuery(query: String) {
        LibraryRepository.setSearchQuery(query)
    }

    fun toggleLikeSong(songId: String) {
        LibraryRepository.toggleLikeSong(songId)
    }

    fun toggleSaveAlbum(albumId: String) {
        LibraryRepository.toggleSaveAlbum(albumId)
    }

    fun toggleFollowArtist(artistId: String) {
        LibraryRepository.toggleFollowArtist(artistId)
    }

    fun togglePinPlaylist(playlistId: String) {
        LibraryRepository.togglePinPlaylist(playlistId)
    }

    fun togglePinSong(songId: String) {
        LibraryRepository.togglePinSong(songId)
    }

    fun togglePinAlbum(albumId: String) {
        LibraryRepository.togglePinAlbum(albumId)
    }

    fun togglePinArtist(artistId: String) {
        LibraryRepository.togglePinArtist(artistId)
    }

    fun createPlaylist(
        name: String,
        description: String,
        coverUrl: String = "",
        initialSongIds: List<String> = emptyList()
    ): UserPlaylist {
        return LibraryRepository.createPlaylist(name, description, coverUrl, initialSongIds)
    }

    fun updatePlaylistDetails(playlistId: String, name: String, description: String, coverUrl: String) {
        LibraryRepository.updatePlaylistDetails(playlistId, name, description, coverUrl)
    }

    fun deletePlaylist(playlistId: String) {
        LibraryRepository.deletePlaylist(playlistId)
    }

    fun addSongsToPlaylist(playlistId: String, songIds: List<String>) {
        LibraryRepository.addSongsToPlaylist(playlistId, songIds)
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        LibraryRepository.removeSongFromPlaylist(playlistId, songId)
    }

    fun reorderSongsInPlaylist(playlistId: String, fromIndex: Int, toIndex: Int) {
        LibraryRepository.reorderSongsInPlaylist(playlistId, fromIndex, toIndex)
    }

    fun duplicatePlaylist(playlistId: String): UserPlaylist? {
        return LibraryRepository.duplicatePlaylist(playlistId)
    }

    fun recordSongPlay(songId: String) {
        LibraryRepository.recordSongPlay(songId)
    }
}
