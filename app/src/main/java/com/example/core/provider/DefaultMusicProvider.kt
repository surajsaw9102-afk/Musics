package com.example.core.provider

import com.example.core.api.NetworkResult
import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity
import kotlinx.coroutines.delay

class DefaultMusicProvider : MusicProvider {
    override val providerId: String = "aura_default_catalog_v1"
    override val providerName: String = "Aura Free Music Catalog"
    override val providerVersion: String = "1.0.0"

    override suspend fun getSongDetails(songId: String): NetworkResult<SongEntity> {
        val song = MusicCatalog.getSongById(songId)
        return if (song != null) {
            NetworkResult.Success(song)
        } else {
            NetworkResult.Error(code = 404, message = "Song not found in catalog: $songId")
        }
    }

    override suspend fun getAlbumDetails(albumId: String): NetworkResult<AlbumEntity> {
        val album = MusicCatalog.getAlbumById(albumId)
        return if (album != null) {
            NetworkResult.Success(album)
        } else {
            NetworkResult.Error(code = 404, message = "Album not found in catalog: $albumId")
        }
    }

    override suspend fun getArtistDetails(artistId: String): NetworkResult<ArtistEntity> {
        val artist = MusicCatalog.getArtistById(artistId)
        return if (artist != null) {
            NetworkResult.Success(artist)
        } else {
            NetworkResult.Error(code = 404, message = "Artist not found in catalog: $artistId")
        }
    }

    override suspend fun searchSongs(query: String): NetworkResult<List<SongEntity>> {
        val results = MusicCatalog.searchSongs(query)
        return NetworkResult.Success(results)
    }

    override suspend fun getAudioStreamUrl(songId: String, quality: DownloadQuality): NetworkResult<String> {
        val song = MusicCatalog.getSongById(songId) ?: return NetworkResult.Error(code = 404, message = "Song not found")
        return NetworkResult.Success(song.audioUrl)
    }

    override suspend fun fetchAudioDataChunks(
        songId: String,
        quality: DownloadQuality,
        onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): NetworkResult<ByteArray> {
        val song = MusicCatalog.getSongById(songId) ?: return NetworkResult.Error(code = 404, message = "Song not found")

        // Estimate file size based on duration and quality
        val durationSec = (song.durationMs / 1000).coerceAtLeast(180)
        val bitrateKbps = quality.bitrateKbps
        val estimatedTotalBytes = (durationSec * (bitrateKbps * 1000L / 8L)).coerceAtLeast(1_500_000L)

        // Chunked simulation loop
        val chunkSize = 250_000L
        var downloaded = 0L

        while (downloaded < estimatedTotalBytes) {
            delay(120L) // simulated download chunk speed
            downloaded = (downloaded + chunkSize).coerceAtMost(estimatedTotalBytes)
            onProgress(downloaded, estimatedTotalBytes)
        }

        // Return simulated byte array placeholder for offline storage
        return NetworkResult.Success(ByteArray(1024) { 0.toByte() })
    }
}
