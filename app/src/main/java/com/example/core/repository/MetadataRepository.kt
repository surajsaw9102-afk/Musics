package com.example.core.repository

import com.example.core.api.NetworkResult
import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.SongEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class ExtendedTrackMetadata(
    val song: SongEntity,
    val lyricsAvailable: Boolean = true,
    val audioQualityBadge: String = "Lossless FLAC 24-bit / 96kHz",
    val bitRate: String = "1411 kbps",
    val sampleRate: String = "96 kHz",
    val codec: String = "FLAC",
    val trackNumber: Int = 1,
    val totalAlbumTracks: Int = 10,
    val copyright: String = "© 2026 Aura Music Recording Group",
    val isExplicit: Boolean = false
)

interface MetadataRepository {
    fun getTrackMetadata(trackId: String): Flow<NetworkResult<ExtendedTrackMetadata>>
}

class DefaultMetadataRepository : MetadataRepository {
    override fun getTrackMetadata(trackId: String): Flow<NetworkResult<ExtendedTrackMetadata>> = flow {
        val song = MusicCatalog.getSongById(trackId)
        if (song != null) {
            val albumSongs = MusicCatalog.getSongsByAlbum(song.albumId)
            val index = albumSongs.indexOfFirst { it.id == song.id }
            val trackNum = if (index >= 0) index + 1 else 1

            emit(
                NetworkResult.Success(
                    ExtendedTrackMetadata(
                        song = song,
                        lyricsAvailable = true,
                        audioQualityBadge = song.audioQuality,
                        bitRate = song.bitrate,
                        codec = song.codec,
                        trackNumber = trackNum,
                        totalAlbumTracks = albumSongs.size.coerceAtLeast(1),
                        isExplicit = song.isExplicit
                    )
                )
            )
        } else {
            emit(NetworkResult.Error(code = 404, message = "Metadata not found for track: $trackId"))
        }
    }
}
