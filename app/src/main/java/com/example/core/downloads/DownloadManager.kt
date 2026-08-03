package com.example.core.downloads

import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.SongEntity
import com.example.core.network.NetworkMonitor
import com.example.core.provider.DownloadQuality
import com.example.core.provider.MusicProviderRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DownloadManagerState(
    val itemsMap: Map<String, DownloadItem> = emptyMap(),
    val downloadQuality: DownloadQuality = DownloadQuality.LOSSLESS,
    val wifiOnlyDownloads: Boolean = true,
    val maxConcurrentDownloads: Int = 2
)

object DownloadManager {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val activeJobs = mutableMapOf<String, Job>()

    private val initialItems = mutableMapOf<String, DownloadItem>()

    private val _state = MutableStateFlow(DownloadManagerState(itemsMap = initialItems))
    val state: StateFlow<DownloadManagerState> = _state.asStateFlow()

    init {
        // Pre-populate with a completed downloaded song for demo / immediate feedback
        val initialSong = MusicCatalog.getSongById("song_101")
        if (initialSong != null) {
            val item = DownloadItem(
                song = initialSong,
                quality = DownloadQuality.LOSSLESS,
                state = DownloadState.DOWNLOADED,
                bytesDownloaded = 38_400_000L,
                totalBytes = 38_400_000L,
                progress = 1.0f,
                completedAt = System.currentTimeMillis() - 86400000L,
                localFilePath = "/storage/emulated/0/Android/data/com.aistudio/files/music/song_101.flac"
            )
            initialItems[initialSong.id] = item
            _state.value = _state.value.copy(itemsMap = initialItems.toMap())
        }
    }

    // --- Actions ---

    fun startDownload(
        song: SongEntity,
        quality: DownloadQuality = _state.value.downloadQuality,
        albumId: String? = null,
        playlistId: String? = null
    ) {
        val currentMap = _state.value.itemsMap.toMutableMap()
        val existing = currentMap[song.id]

        if (existing?.state == DownloadState.DOWNLOADED) return

        val newItem = DownloadItem(
            song = song,
            quality = quality,
            state = DownloadState.QUEUED,
            albumId = albumId,
            playlistId = playlistId,
            queuedAt = System.currentTimeMillis()
        )
        currentMap[song.id] = newItem
        _state.value = _state.value.copy(itemsMap = currentMap)

        processQueue()
    }

    fun downloadAlbum(album: AlbumEntity, songs: List<SongEntity>) {
        songs.forEach { song ->
            startDownload(song = song, albumId = album.id)
        }
    }

    fun downloadPlaylist(playlistId: String, songs: List<SongEntity>) {
        songs.forEach { song ->
            startDownload(song = song, playlistId = playlistId)
        }
    }

    fun pauseDownload(songId: String) {
        activeJobs[songId]?.cancel()
        activeJobs.remove(songId)

        updateItemState(songId) { it.copy(state = DownloadState.PAUSED, downloadSpeedKbps = 0.0) }
        processQueue()
    }

    fun resumeDownload(songId: String) {
        val item = _state.value.itemsMap[songId] ?: return
        if (item.state == DownloadState.PAUSED || item.state == DownloadState.FAILED) {
            updateItemState(songId) { it.copy(state = DownloadState.QUEUED, errorMessage = null) }
            processQueue()
        }
    }

    fun cancelDownload(songId: String) {
        activeJobs[songId]?.cancel()
        activeJobs.remove(songId)

        val currentMap = _state.value.itemsMap.toMutableMap()
        currentMap.remove(songId)
        _state.value = _state.value.copy(itemsMap = currentMap)

        processQueue()
    }

    fun retryDownload(songId: String) {
        resumeDownload(songId)
    }

    fun removeDownload(songId: String) {
        cancelDownload(songId)
    }

    fun removeAllDownloads() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
        _state.value = _state.value.copy(itemsMap = emptyMap())
    }

    fun setDownloadQuality(quality: DownloadQuality) {
        _state.value = _state.value.copy(downloadQuality = quality)
    }

    fun setWifiOnlyDownloads(wifiOnly: Boolean) {
        _state.value = _state.value.copy(wifiOnlyDownloads = wifiOnly)
        if (wifiOnly && !NetworkMonitor.status.value.isWifi) {
            // Pause active downloads if switched to Wi-Fi only while on cellular
            _state.value.itemsMap.filterValues { it.state == DownloadState.DOWNLOADING }.forEach { (id, _) ->
                pauseDownload(id)
            }
        } else {
            processQueue()
        }
    }

    fun getDownloadState(songId: String): DownloadState {
        return _state.value.itemsMap[songId]?.state ?: DownloadState.NOT_DOWNLOADED
    }

    fun isSongDownloaded(songId: String): Boolean {
        return _state.value.itemsMap[songId]?.state == DownloadState.DOWNLOADED
    }

    fun getDownloadedSongs(): List<SongEntity> {
        return _state.value.itemsMap.values
            .filter { it.state == DownloadState.DOWNLOADED }
            .map { it.song }
    }

    // --- Queue Processor ---

    private fun processQueue() {
        val network = NetworkMonitor.status.value
        if (!network.isOnline) return
        if (_state.value.wifiOnlyDownloads && !network.isWifi) return

        val downloadingCount = _state.value.itemsMap.values.count { it.state == DownloadState.DOWNLOADING }
        val maxAllowed = _state.value.maxConcurrentDownloads

        if (downloadingCount >= maxAllowed) return

        val queuedItems = _state.value.itemsMap.values
            .filter { it.state == DownloadState.QUEUED }
            .sortedBy { it.queuedAt }

        val slotsAvailable = maxAllowed - downloadingCount
        for (item in queuedItems.take(slotsAvailable)) {
            executeDownload(item)
        }
    }

    private fun executeDownload(item: DownloadItem) {
        val songId = item.song.id
        updateItemState(songId) { it.copy(state = DownloadState.DOWNLOADING) }

        val job = scope.launch {
            try {
                val provider = MusicProviderRegistry.getActive()
                val startTime = System.currentTimeMillis()

                val result = provider.fetchAudioDataChunks(
                    songId = songId,
                    quality = item.quality,
                    onProgress = { downloaded, total ->
                        val elapsedTimeSec = (System.currentTimeMillis() - startTime) / 1000.0
                        val speedKbps = if (elapsedTimeSec > 0) (downloaded * 8.0 / 1000.0) / elapsedTimeSec else 1200.0
                        val remainingBytes = (total - downloaded).coerceAtLeast(0)
                        val bytesPerSec = if (speedKbps > 0) (speedKbps * 1000.0 / 8.0) else 200_000.0
                        val remainingSec = if (bytesPerSec > 0) (remainingBytes / bytesPerSec).toInt() else 0
                        val progress = if (total > 0) downloaded.toFloat() / total.toFloat() else 0f

                        updateItemState(songId) { current ->
                            if (current.state == DownloadState.DOWNLOADING) {
                                current.copy(
                                    bytesDownloaded = downloaded,
                                    totalBytes = total,
                                    downloadSpeedKbps = speedKbps,
                                    remainingTimeSeconds = remainingSec,
                                    progress = progress
                                )
                            } else current
                        }
                    }
                )

                if (result is com.example.core.api.NetworkResult.Success) {
                    updateItemState(songId) { current ->
                        current.copy(
                            state = DownloadState.DOWNLOADED,
                            progress = 1.0f,
                            bytesDownloaded = current.totalBytes,
                            completedAt = System.currentTimeMillis(),
                            localFilePath = "/storage/emulated/0/Android/data/com.aistudio/files/music/${songId}.${item.quality.extension}"
                        )
                    }
                } else {
                    updateItemState(songId) { current ->
                        current.copy(
                            state = DownloadState.FAILED,
                            errorMessage = "Provider network error"
                        )
                    }
                }
            } catch (e: Exception) {
                updateItemState(songId) { current ->
                    current.copy(
                        state = DownloadState.FAILED,
                        errorMessage = e.localizedMessage ?: "Download interrupted"
                    )
                }
            } finally {
                activeJobs.remove(songId)
                processQueue()
            }
        }

        activeJobs[songId] = job
    }

    private fun updateItemState(songId: String, transform: (DownloadItem) -> DownloadItem) {
        val currentMap = _state.value.itemsMap.toMutableMap()
        val item = currentMap[songId] ?: return
        currentMap[songId] = transform(item)
        _state.value = _state.value.copy(itemsMap = currentMap)
    }
}
