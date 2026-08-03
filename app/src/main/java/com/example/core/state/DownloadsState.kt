package com.example.core.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.cache.CacheStats
import com.example.core.cache.SmartCacheManager
import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.SongEntity
import com.example.core.downloads.DownloadItem
import com.example.core.downloads.DownloadManager
import com.example.core.downloads.DownloadState
import com.example.core.downloads.DownloadedAlbum
import com.example.core.downloads.DownloadedPlaylist
import com.example.core.network.NetworkMonitor
import com.example.core.offline.OfflineRepository
import com.example.core.provider.DownloadQuality
import com.example.core.storage.StorageManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DownloadsData(
    val downloadedSongs: List<SongEntity> = emptyList(),
    val allDownloadItems: List<DownloadItem> = emptyList(),
    val activeDownloads: List<DownloadItem> = emptyList(),
    val pausedDownloads: List<DownloadItem> = emptyList(),
    val failedDownloads: List<DownloadItem> = emptyList(),
    val downloadedAlbums: List<DownloadedAlbum> = emptyList(),
    val downloadedPlaylists: List<DownloadedPlaylist> = emptyList(),
    val totalMusicStorageMb: Double = 38.4,
    val cacheStorageMb: Double = 42.5,
    val freeDeviceSpaceGb: Double = 48.2,
    val totalDeviceSpaceGb: Double = 128.0,
    val downloadQuality: DownloadQuality = DownloadQuality.LOSSLESS,
    val wifiOnlyDownloads: Boolean = true,
    val isOfflineMode: Boolean = false,
    val isOnline: Boolean = true,
    val connectionTypeLabel: String = "Wi-Fi (High Speed)",
    val cacheStats: CacheStats = CacheStats()
) {
    val totalStorageUsedMb: Double
        get() = totalMusicStorageMb + cacheStorageMb
}

class DownloadsState : ViewModel() {

    val downloads: StateFlow<DownloadsData> = combine(
        DownloadManager.state,
        NetworkMonitor.status,
        SmartCacheManager.stats,
        StorageManager.stats
    ) { dlState, netStatus, cacheStats, storeStats ->

        val itemsMap = dlState.itemsMap.values.toList()
        val downloadedSongs = itemsMap.filter { it.state == DownloadState.DOWNLOADED }.map { it.song }
        val active = itemsMap.filter { it.state == DownloadState.DOWNLOADING || it.state == DownloadState.QUEUED }
        val paused = itemsMap.filter { it.state == DownloadState.PAUSED }
        val failed = itemsMap.filter { it.state == DownloadState.FAILED }

        val albums = OfflineRepository.getOfflineAlbums()
        val playlists = OfflineRepository.getOfflinePlaylists()

        DownloadsData(
            downloadedSongs = downloadedSongs,
            allDownloadItems = itemsMap,
            activeDownloads = active,
            pausedDownloads = paused,
            failedDownloads = failed,
            downloadedAlbums = albums,
            downloadedPlaylists = playlists,
            totalMusicStorageMb = storeStats.musicStorageMb,
            cacheStorageMb = storeStats.cacheStorageMb,
            freeDeviceSpaceGb = storeStats.freeDeviceSpaceGb,
            totalDeviceSpaceGb = storeStats.totalDeviceCapacityGb,
            downloadQuality = dlState.downloadQuality,
            wifiOnlyDownloads = dlState.wifiOnlyDownloads,
            isOfflineMode = netStatus.isForceOfflineMode || !netStatus.isOnline,
            isOnline = netStatus.isOnline,
            connectionTypeLabel = netStatus.connectionType.label,
            cacheStats = cacheStats
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DownloadsData()
    )

    // --- Actions ---

    fun startDownload(song: SongEntity) {
        DownloadManager.startDownload(song)
        StorageManager.updateStorageStats()
    }

    fun pauseDownload(songId: String) {
        DownloadManager.pauseDownload(songId)
    }

    fun resumeDownload(songId: String) {
        DownloadManager.resumeDownload(songId)
    }

    fun cancelDownload(songId: String) {
        DownloadManager.cancelDownload(songId)
        StorageManager.updateStorageStats()
    }

    fun retryDownload(songId: String) {
        DownloadManager.retryDownload(songId)
    }

    fun removeDownload(songId: String) {
        DownloadManager.removeDownload(songId)
        StorageManager.updateStorageStats()
    }

    fun removeAllDownloads() {
        StorageManager.deleteAllDownloads()
    }

    fun setDownloadQuality(quality: DownloadQuality) {
        DownloadManager.setDownloadQuality(quality)
    }

    fun setWifiOnly(enabled: Boolean) {
        DownloadManager.setWifiOnlyDownloads(enabled)
    }

    fun toggleOfflineMode(enabled: Boolean) {
        NetworkMonitor.setForceOffline(enabled)
    }

    fun clearCache() {
        StorageManager.clearCache()
    }

    fun downloadAlbum(albumId: String) {
        val album = MusicCatalog.getAlbumById(albumId) ?: return
        val songs = MusicCatalog.ALL_SONGS.filter { it.albumId == albumId }
        DownloadManager.downloadAlbum(album, songs)
        StorageManager.updateStorageStats()
    }

    fun downloadPlaylist(playlistId: String) {
        val songs = MusicCatalog.ALL_SONGS.take(5)
        DownloadManager.downloadPlaylist(playlistId, songs)
        StorageManager.updateStorageStats()
    }
}
