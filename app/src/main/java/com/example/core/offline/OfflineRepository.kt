package com.example.core.offline

import com.example.core.cache.SmartCacheManager
import com.example.core.database.entities.SongEntity
import com.example.core.downloads.DownloadManager
import com.example.core.downloads.DownloadState
import com.example.core.downloads.DownloadedAlbum
import com.example.core.downloads.DownloadedPlaylist
import com.example.core.network.NetworkMonitor
import com.example.feature.library.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class OfflineSortOption(val label: String) {
    TITLE("Song Title"),
    ARTIST("Artist Name"),
    DATE_DOWNLOADED("Recently Downloaded"),
    SIZE("File Size")
}

data class DownloadedArtist(
    val artistId: String,
    val artistName: String,
    val avatarUrl: String,
    val songCount: Int
)

object OfflineRepository {

    private val _isOfflineAvailable = MutableStateFlow(true)
    val isOfflineAvailable: StateFlow<Boolean> = _isOfflineAvailable.asStateFlow()

    fun getPlayableAudioUrl(song: SongEntity): String {
        val downloadItem = DownloadManager.state.value.itemsMap[song.id]
        if (downloadItem?.state == DownloadState.DOWNLOADED && !downloadItem.localFilePath.isNullOrBlank()) {
            return downloadItem.localFilePath
        }

        if (SmartCacheManager.isSongCached(song.id)) {
            return song.audioUrl
        }

        return song.audioUrl
    }

    fun isAvailableOffline(songId: String): Boolean {
        val isDownloaded = DownloadManager.isSongDownloaded(songId)
        val isCached = SmartCacheManager.isSongCached(songId)
        return isDownloaded || isCached
    }

    fun getOfflineSongs(sortOption: OfflineSortOption = OfflineSortOption.DATE_DOWNLOADED): List<SongEntity> {
        val downloadedItems = DownloadManager.state.value.itemsMap.values
            .filter { it.state == DownloadState.DOWNLOADED }

        val songs = downloadedItems.map { it.song }

        return when (sortOption) {
            OfflineSortOption.TITLE -> songs.sortedBy { it.title }
            OfflineSortOption.ARTIST -> songs.sortedBy { it.artistName }
            OfflineSortOption.DATE_DOWNLOADED -> {
                val itemMap = downloadedItems.associateBy { it.song.id }
                songs.sortedByDescending { itemMap[it.id]?.completedAt ?: 0L }
            }
            OfflineSortOption.SIZE -> songs.sortedByDescending { it.durationMs }
        }
    }

    fun searchOfflineSongs(query: String, sortOption: OfflineSortOption = OfflineSortOption.TITLE): List<SongEntity> {
        val allOffline = getOfflineSongs(sortOption)
        if (query.isBlank()) return allOffline
        val trimmed = query.trim().lowercase()
        return allOffline.filter {
            it.title.lowercase().contains(trimmed) ||
                    it.artistName.lowercase().contains(trimmed) ||
                    it.albumTitle.lowercase().contains(trimmed) ||
                    it.genre.lowercase().contains(trimmed)
        }
    }

    fun getRecentlyDownloaded(limit: Int = 5): List<SongEntity> {
        val downloadedItems = DownloadManager.state.value.itemsMap.values
            .filter { it.state == DownloadState.DOWNLOADED }
            .sortedByDescending { it.completedAt ?: 0L }

        return downloadedItems.take(limit).map { it.song }
    }

    fun getOfflineAlbums(): List<DownloadedAlbum> {
        val downloadedSongs = DownloadManager.getDownloadedSongs()
        val groupedByAlbum = downloadedSongs.groupBy { it.albumId }

        return groupedByAlbum.mapNotNull { (albumId, songs) ->
            if (songs.isEmpty()) null
            else {
                val first = songs.first()
                val totalMb = songs.size * 32.0
                DownloadedAlbum(
                    albumId = albumId,
                    albumTitle = first.albumTitle,
                    artistName = first.artistName,
                    coverUrl = first.coverUrl,
                    downloadedSongs = songs,
                    totalSizeMb = totalMb
                )
            }
        }
    }

    fun getOfflinePlaylists(): List<DownloadedPlaylist> {
        val downloadedSongsMap = DownloadManager.getDownloadedSongs().associateBy { it.id }
        val userPlaylists = LibraryRepository.state.value.playlists

        return userPlaylists.mapNotNull { playlist ->
            val offlineInPlaylist = playlist.songIds.mapNotNull { downloadedSongsMap[it] }
            if (offlineInPlaylist.isEmpty()) null
            else {
                DownloadedPlaylist(
                    playlistId = playlist.id,
                    playlistName = playlist.name,
                    coverUrl = playlist.coverUrl.ifBlank { offlineInPlaylist.first().coverUrl },
                    downloadedSongs = offlineInPlaylist,
                    totalSizeMb = offlineInPlaylist.size * 32.0
                )
            }
        }
    }

    fun getOfflineArtists(): List<DownloadedArtist> {
        val downloadedSongs = DownloadManager.getDownloadedSongs()
        val groupedByArtist = downloadedSongs.groupBy { it.artistId }

        return groupedByArtist.map { (artistId, songs) ->
            val first = songs.first()
            DownloadedArtist(
                artistId = artistId,
                artistName = first.artistName,
                avatarUrl = first.coverUrl,
                songCount = songs.size
            )
        }
    }
}
