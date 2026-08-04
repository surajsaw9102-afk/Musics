package com.example.core.repository

import com.example.core.api.NetworkResult
import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.SongEntity
import com.example.core.provider.MusicProviderRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface TrackRepository {
    fun getTrackDetails(trackId: String): Flow<NetworkResult<SongEntity>>
    fun getRelatedTracks(trackId: String, artistId: String, genre: String): Flow<NetworkResult<List<SongEntity>>>
}

class DefaultTrackRepository : TrackRepository {
    override fun getTrackDetails(trackId: String): Flow<NetworkResult<SongEntity>> = flow {
        val providerResult = MusicProviderRegistry.getActive().getSongDetails(trackId)
        if (providerResult is NetworkResult.Success<SongEntity>) {
            emit(providerResult)
        } else {
            val fallback = MusicCatalog.getSongById(trackId)
            if (fallback != null) {
                emit(NetworkResult.Success(fallback))
            } else {
                emit(NetworkResult.Error(code = 404, message = "Track not found: $trackId"))
            }
        }
    }

    override fun getRelatedTracks(trackId: String, artistId: String, genre: String): Flow<NetworkResult<List<SongEntity>>> = flow {
        val related = MusicCatalog.ALL_SONGS.filter { it.id != trackId && (it.artistId == artistId || it.genre.equals(genre, ignoreCase = true)) }
        emit(NetworkResult.Success(related))
    }
}
