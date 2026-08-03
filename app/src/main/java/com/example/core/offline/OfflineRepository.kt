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

object OfflineRepository {

    private val _isOfflineAvailable = MutableStateFlow(true)
    val isOfflineAvailable: StateFlow<Boolean> = _isOfflineAvailable.asStateFlow()

    fun getPlayableAudioUrl(song: SongEntity): String {
        val downloadItem = DownloadManager.state.value.itemsMap[song.id]
        if (downloadItem?.state == DownloadState.DOWNLOADED && !downloadItem.localFilePath.isNullOrBlank()) {
            return downloadItem.localFilePath
        }

        // If cached in SmartCacheManager
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

    fun getOfflineSongs(): List<SongEntity> {
        val downloaded = DownloadManager.getDownloadedSongs()
        val cached = SmartCacheManager.getCachedSongs()
        val combined = (downloaded + cached).distinctBy { it.id }
        return combined
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
}
