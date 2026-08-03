package com.example.core.provider

import com.example.core.api.NetworkResult
import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity

enum class DownloadQuality(
    val label: String,
    val bitrateKbps: Int,
    val extension: String,
    val description: String
) {
    AUTO("Auto", 256, "mp3", "Dynamic quality based on network speed"),
    LOW("Low (96 kbps)", 96, "mp3", "Saves maximum storage and mobile data"),
    MEDIUM("Medium (160 kbps)", 160, "mp3", "Standard balanced audio quality"),
    HIGH("High (320 kbps)", 320, "mp3", "Crisp studio quality audio"),
    LOSSLESS("Lossless FLAC", 1411, "flac", "Uncompressed 24-bit / 96kHz master audio")
}

interface MusicProvider {
    val providerId: String
    val providerName: String
    val providerVersion: String

    suspend fun getSongDetails(songId: String): NetworkResult<SongEntity>
    suspend fun getAlbumDetails(albumId: String): NetworkResult<AlbumEntity>
    suspend fun getArtistDetails(artistId: String): NetworkResult<ArtistEntity>
    suspend fun searchSongs(query: String): NetworkResult<List<SongEntity>>

    suspend fun getAudioStreamUrl(songId: String, quality: DownloadQuality): NetworkResult<String>
    suspend fun fetchAudioDataChunks(
        songId: String,
        quality: DownloadQuality,
        onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): NetworkResult<ByteArray>
}
