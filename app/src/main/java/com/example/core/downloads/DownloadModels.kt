package com.example.core.downloads

import com.example.core.database.entities.SongEntity
import com.example.core.provider.DownloadQuality

enum class DownloadState {
    NOT_DOWNLOADED,
    QUEUED,
    DOWNLOADING,
    PAUSED,
    DOWNLOADED,
    FAILED
}

data class DownloadItem(
    val song: SongEntity,
    val quality: DownloadQuality = DownloadQuality.LOSSLESS,
    val state: DownloadState = DownloadState.NOT_DOWNLOADED,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val downloadSpeedKbps: Double = 0.0,
    val remainingTimeSeconds: Int = 0,
    val progress: Float = 0f,
    val errorMessage: String? = null,
    val queuedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val albumId: String? = null,
    val playlistId: String? = null,
    val localFilePath: String? = null
)

data class DownloadedAlbum(
    val albumId: String,
    val albumTitle: String,
    val artistName: String,
    val coverUrl: String,
    val downloadedSongs: List<SongEntity>,
    val totalSizeMb: Double
)

data class DownloadedPlaylist(
    val playlistId: String,
    val playlistName: String,
    val coverUrl: String,
    val downloadedSongs: List<SongEntity>,
    val totalSizeMb: Double
)
